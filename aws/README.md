# AWS Fargate Deployment Guide

## Prerequisites

1. **AWS CLI configured** with appropriate permissions
2. **Docker installed** and running
3. **VPC with public and private subnets** (or use default VPC)

## Quick Deployment

### 1. Update Configuration

Edit `aws/deploy.sh` and update these values:
```bash
AWS_REGION="us-east-1"  # Your preferred region
```

In the CloudFormation parameters section, replace:
- `vpc-xxxxxxxxx` with your VPC ID
- `subnet-xxxxxxxxx,subnet-yyyyyyyyy` with your private subnet IDs
- `subnet-zzzzzzzzz,subnet-aaaaaaaaa` with your public subnet IDs
- `YourSecurePassword123` with a secure database password

### 2. Run Deployment

```bash
./aws/deploy.sh
```

This script will:
- Build and push Docker image to ECR
- Deploy infrastructure (RDS, ECS, ALB)
- Register ECS task definition
- Create ECS service

### 3. Access Application

After deployment completes, access your application at:
- **API**: `http://YOUR_ALB_DNS/api/users/1/addresses`
- **Swagger UI**: `http://YOUR_ALB_DNS/swagger-ui/index.html`
- **Health Check**: `http://YOUR_ALB_DNS/actuator/health`

## Manual Deployment Steps

### 1. Create ECR Repository
```bash
aws ecr create-repository --repository-name addressbook --region us-east-1
```

### 2. Build and Push Image
```bash
# Get login token
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Build and push
docker build -t addressbook:1.0 .
docker tag addressbook:1.0 ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/addressbook:1.0
docker push ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/addressbook:1.0
```

### 3. Deploy Infrastructure
```bash
aws cloudformation create-stack \
  --stack-name addressbook-infrastructure \
  --template-body file://aws/infrastructure.yaml \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameters ParameterKey=VpcId,ParameterValue=vpc-xxxxxxxxx \
              ParameterKey=PrivateSubnetIds,ParameterValue=subnet-xxx,subnet-yyy \
              ParameterKey=PublicSubnetIds,ParameterValue=subnet-zzz,subnet-aaa \
              ParameterKey=DBPassword,ParameterValue=YourSecurePassword123
```

### 4. Register Task Definition
```bash
# Update task-definition.json with your account ID and region
aws ecs register-task-definition --cli-input-json file://aws/task-definition.json
```

### 5. Create ECS Service
```bash
aws ecs create-service \
  --cluster addressbook-cluster \
  --service-name addressbook-service \
  --task-definition addressbook-task \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxxxxxxxx],assignPublicIp=DISABLED}" \
  --load-balancers targetGroupArn=arn:aws:elasticloadbalancing:...,containerName=addressbook,containerPort=8080
```

## Configuration Details

### Database Configuration
- **Engine**: PostgreSQL 15.4
- **Instance**: db.t3.micro
- **Storage**: 20GB GP2
- **Backup**: 7 days retention

### ECS Configuration
- **CPU**: 256 (0.25 vCPU)
- **Memory**: 512 MB
- **Network**: awsvpc mode
- **Health Check**: `/actuator/health`

### Security
- Database credentials stored in AWS Systems Manager Parameter Store
- Security groups restrict access between components
- RDS in private subnets only

## Monitoring

### CloudWatch Logs
Logs are available in CloudWatch under `/ecs/addressbook`

### Health Checks
- **ECS Health Check**: `/actuator/health`
- **ALB Health Check**: `/actuator/health`

## Scaling

To scale the application:
```bash
aws ecs update-service \
  --cluster addressbook-cluster \
  --service addressbook-service \
  --desired-count 3
```

## Cleanup

To delete all resources:
```bash
# Delete ECS service first
aws ecs update-service --cluster addressbook-cluster --service addressbook-service --desired-count 0
aws ecs delete-service --cluster addressbook-cluster --service addressbook-service

# Delete CloudFormation stack
aws cloudformation delete-stack --stack-name addressbook-infrastructure

# Delete ECR repository
aws ecr delete-repository --repository-name addressbook --force
```

## Troubleshooting

### Common Issues

1. **Task fails to start**: Check CloudWatch logs for application errors
2. **Database connection fails**: Verify security groups and RDS endpoint
3. **Health check fails**: Ensure application starts on port 8080

### Useful Commands

```bash
# Check service status
aws ecs describe-services --cluster addressbook-cluster --services addressbook-service

# View task logs
aws logs tail /ecs/addressbook --follow

# Check RDS status
aws rds describe-db-instances --db-instance-identifier addressbook-db
```