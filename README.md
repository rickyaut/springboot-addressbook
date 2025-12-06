# Build and run
```
mvn clean package spring-boot:repackage
java -jar target/addressbook-0.0.1-SNAPSHOT.jar
mvn spring-boot:run
mvn spotless:check
mvn spotless:apply
```

# Access swagger ui:
```
http://localhost:8080/swagger-ui/index.html
```

# Access the H2 Console
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:addressbook
Username: sa
Password: (leave blank)
```

# Trigger API
* curl command to list addresses of a user
```
curl --request GET \
  --url http://localhost:8080/api/users/1/addresses
```
* curl command to create an address for a user
```
curl --request POST \
  --url http://localhost:8080/api/users/1/addresses \
  --header 'content-type: application/json' \
  --data '{
  "name": "Customer One",
  "phone": "0432123456",
  "email": "customer1@gmail.com",
  "street": "Melbourne"
}'
```
* curl command to delete an address of a user
```
curl --request DELETE \
  --url http://localhost:8080/api/users/1/addresses/1
```

* curl command to send graphql query request
```
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'content-type: application/json' \
  --data '{"query":"query {\n  addresses {\n    id\n    name\n    email\n  }\n}"}'
```

* curl command to send graphql create request
```
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'content-type: application/json' \
  --data '{"query":"mutation {\n  createAddress(userId: 2, input: {\n    name: \"John Doe2\"\n    phone: \"123456789\"\n    email: \"john2@example.com\"\n    street: \"Main St\"\n  }) {\n    id\n    name\n  }\n}"}'
```


* curl command to send graphql query by user request
```
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'content-type: application/json' \
  --data '{"query":"query {\n  addressesByUser(userId: 2) {\n    id\n    name\n    email\n  }\n}"}'
```

# Build Docker image
```
docker build -t springboot-addressbook:1.0 .
```


# Run container
```
docker run -p 8080:8080 springboot-addressbook:1.0
```

# Push to dockerhub
```
docker tag springboot-addressbook:1.0 rickyaut/springboot-addressbook:1.0
docker push rickyaut/springboot-addressbook:1.0
```

# Kubernetes Deployment
```
minikube start
minikube stop
minikube delete
minikube delete --all --purge

pkill -f "kubectl port-forward"
kubectl port-forward svc/addressbook-service 8080:80

kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl get pods
kubectl get svc addressbook-service
kubectl get pods -o wide
kubectl logs addressbook-deployment-75b5d56766-nb2pr
kubectl rollout restart deployment addressbook-deployment
curl http://$(minikube ip):30701/api/users/1/addresses

```