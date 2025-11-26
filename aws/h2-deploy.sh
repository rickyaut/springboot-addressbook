#!/bin/bash

# Configuration
AWS_REGION="ap-southeast-2"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REPOSITORY="addressbook"
IMAGE_TAG="1.0"
STACK_NAME="addressbook-h2"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}Deploying AddressBook with H2 Database (~$0.50/month)...${NC}"

# Step 1: Build and push Docker image
echo -e "${YELLOW}Step 1: Building and pushing Docker image...${NC}"

aws ecr describe-repositories --repository-names $ECR_REPOSITORY --region $AWS_REGION 2>/dev/null || \
aws ecr create-repository --repository-name $ECR_REPOSITORY --region $AWS_REGION

aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

docker build -t $ECR_REPOSITORY:$IMAGE_TAG .
docker tag $ECR_REPOSITORY:$IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$IMAGE_TAG
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$IMAGE_TAG

# Step 2: Deploy minimal infrastructure
echo -e "${YELLOW}Step 2: Deploying infrastructure...${NC}"

if aws cloudformation describe-stacks --stack-name $STACK_NAME --region $AWS_REGION 2>/dev/null; then
    aws cloudformation update-stack \
        --stack-name $STACK_NAME \
        --template-body file://aws/h2-infrastructure.yaml \
        --capabilities CAPABILITY_NAMED_IAM \
        --region $AWS_REGION
else
    aws cloudformation create-stack \
        --stack-name $STACK_NAME \
        --template-body file://aws/h2-infrastructure.yaml \
        --capabilities CAPABILITY_NAMED_IAM \
        --region $AWS_REGION
fi

aws cloudformation wait stack-create-complete --stack-name $STACK_NAME --region $AWS_REGION || \
aws cloudformation wait stack-update-complete --stack-name $STACK_NAME --region $AWS_REGION

# Step 3: Register task definition
echo -e "${YELLOW}Step 3: Registering task definition...${NC}"

sed -i.bak "s/YOUR_ACCOUNT_ID/$AWS_ACCOUNT_ID/g" aws/h2-task-definition.json
sed -i.bak "s/YOUR_REGION/$AWS_REGION/g" aws/h2-task-definition.json

aws ecs register-task-definition \
    --cli-input-json file://aws/h2-task-definition.json \
    --region $AWS_REGION

# Step 4: Create ECS service
echo -e "${YELLOW}Step 4: Creating ECS service...${NC}"

CLUSTER_NAME="addressbook-cluster"
PUBLIC_SUBNET=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $AWS_REGION --query 'Stacks[0].Outputs[?OutputKey==`PublicSubnetId`].OutputValue' --output text)
SECURITY_GROUP=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $AWS_REGION --query 'Stacks[0].Outputs[?OutputKey==`SecurityGroupId`].OutputValue' --output text)

aws ecs create-service \
    --cluster $CLUSTER_NAME \
    --service-name addressbook-h2-service \
    --task-definition addressbook-h2-task \
    --desired-count 1 \
    --capacity-provider-strategy capacityProvider=FARGATE_SPOT,weight=1 \
    --network-configuration "awsvpcConfiguration={subnets=[$PUBLIC_SUBNET],securityGroups=[$SECURITY_GROUP],assignPublicIp=ENABLED}" \
    --region $AWS_REGION 2>/dev/null || \
aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service addressbook-h2-service \
    --task-definition addressbook-h2-task \
    --desired-count 1 \
    --region $AWS_REGION

# Get public IP and wait for application to be ready
echo "Waiting for task to start..."
sleep 30

TASK_ARN=$(aws ecs list-tasks --cluster $CLUSTER_NAME --service-name addressbook-h2-service --region $AWS_REGION --query 'taskArns[0]' --output text)
if [ "$TASK_ARN" = "None" ] || [ -z "$TASK_ARN" ]; then
    echo -e "${RED}No tasks found. Checking service status...${NC}"
    aws ecs describe-services --cluster $CLUSTER_NAME --services addressbook-h2-service --region $AWS_REGION --query 'services[0].events[0:3]'
    exit 1
fi

PUBLIC_IP=$(aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks $TASK_ARN --region $AWS_REGION --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text | xargs -I {} aws ec2 describe-network-interfaces --network-interface-ids {} --region $AWS_REGION --query 'NetworkInterfaces[0].Association.PublicIp' --output text)

echo -e "${GREEN}Task started! Waiting for application to be ready...${NC}"
echo "Public IP: $PUBLIC_IP"

# Wait for application to be ready
for i in {1..12}; do
    if curl -s -f "http://$PUBLIC_IP:8080/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}Application is ready!${NC}"
        break
    fi
    echo "Attempt $i/12: Application not ready yet, waiting 15 seconds..."
    sleep 15
done

# Test the health endpoint
echo "Testing health endpoint..."
curl -s "http://$PUBLIC_IP:8080/actuator/health" || echo "Health check failed"

echo -e "${GREEN}Deployment completed! Cost: ~$0.50/month${NC}"
echo -e "${YELLOW}Application: http://$PUBLIC_IP:8080${NC}"
echo -e "${YELLOW}Swagger UI: http://$PUBLIC_IP:8080/swagger-ui/index.html${NC}"
echo -e "${YELLOW}H2 Console: http://$PUBLIC_IP:8080/h2-console${NC}"
echo -e "${YELLOW}API: http://$PUBLIC_IP:8080/api/users/1/addresses${NC}"

echo ""
echo "Test commands:"
echo "curl http://$PUBLIC_IP:8080/actuator/health"
echo "curl http://$PUBLIC_IP:8080/api/users/1/addresses"
echo ""
echo "If curl fails, check logs with:"
echo "aws logs tail /ecs/addressbook --follow --region $AWS_REGION"