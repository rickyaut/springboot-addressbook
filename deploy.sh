#!/bin/bash

echo "Deploying to Kubernetes..."

# Deploy all resources
kubectl apply -k k8s/

# Wait for postgres to be ready
echo "Waiting for PostgreSQL..."
kubectl wait --for=condition=ready pod -l app=postgres --timeout=300s

# Wait for app to be ready
echo "Waiting for application..."
kubectl wait --for=condition=ready pod -l app=addressbook --timeout=300s

echo "Deployment complete!"
echo "Access the app: kubectl port-forward svc/addressbook-service 8080:80"