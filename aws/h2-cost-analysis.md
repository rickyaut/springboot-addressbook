# H2 In-Memory Database Deployment - Ultra Low Cost

## Monthly Cost: ~$0.50

### Cost Breakdown:
- **ECS Fargate Spot**: ~$0.15/month (10 min/day usage)
- **CloudWatch Logs**: ~$0.05/month (1-day retention)
- **Data Transfer**: ~$0.05/month
- **ECR Storage**: ~$0.25/month

**Total: ~$0.50/month** (99% cost reduction from original setup)

## What's Removed:
- ❌ **Database**: No RDS/Aurora ($6-13/month saved)
- ❌ **Load Balancer**: Direct IP access ($16/month saved)
- ❌ **NAT Gateway**: Public subnets ($32/month saved)
- ❌ **Database backups**: In-memory only ($2/month saved)

## What's Included:
- ✅ **H2 In-Memory Database**: Fast, zero-cost database
- ✅ **Fargate Spot**: 70% compute discount
- ✅ **Auto-scaling**: ECS handles container lifecycle
- ✅ **Health checks**: Application monitoring
- ✅ **CloudWatch logs**: Basic monitoring

## Trade-offs:

### Pros:
- **Ultra-low cost**: $0.50/month
- **Fast startup**: No database connection delays
- **Simple deployment**: Minimal infrastructure
- **Perfect for demos/testing**: Quick and cheap

### Cons:
- **Data loss**: All data lost when container restarts
- **No persistence**: Database resets on each deployment
- **Single instance**: No high availability
- **Memory limited**: Database size limited by container memory

## Use Cases:
- **Development/Testing**: Perfect for non-production environments
- **Demos**: Quick application showcases
- **Prototypes**: Early-stage development
- **Learning**: AWS/containerization education

## Deployment:

### Quick Start:
```bash
# Update subnet IDs in h2-deploy.sh
./aws/h2-deploy.sh
```

### Access:
- **Application**: `http://PUBLIC_IP:8080`
- **H2 Console**: `http://PUBLIC_IP:8080/h2-console`
- **API**: `http://PUBLIC_IP:8080/api/users/1/addresses`

## Data Persistence Options:

If you need data persistence later:

### Option 1: EFS Volume (~$3/month)
Mount EFS to persist H2 file database

### Option 2: RDS PostgreSQL (~$13/month)
Switch to managed database

### Option 3: Aurora Serverless (~$6/month)
Auto-scaling managed database

## Monitoring:
```bash
# Service status
aws ecs describe-services --cluster addressbook-cluster --services addressbook-h2-service

# View logs
aws logs tail /ecs/addressbook --follow

# Cost tracking
aws ce get-cost-and-usage --time-period Start=2024-01-01,End=2024-01-31 --granularity MONTHLY --metrics BlendedCost
```

## Cleanup:
```bash
# Stop service
aws ecs update-service --cluster addressbook-cluster --service addressbook-h2-service --desired-count 0
aws ecs delete-service --cluster addressbook-cluster --service addressbook-h2-service

# Delete stack
aws cloudformation delete-stack --stack-name addressbook-h2

# Delete ECR
aws ecr delete-repository --repository-name addressbook --force
```