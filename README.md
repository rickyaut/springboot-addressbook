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

# Build Docker image
```
docker build -t springboot-addressbook:1.1 .
```


# Run container
```
docker run -p 8080:8080 springboot-addressbook:1.1
docker run -p 8080:8080 -e SPRING_FLYWAY_ENABLED=false springboot-addressbook:1.1
```

# Push to dockerhub
```
docker tag springboot-addressbook:1.1 rickyaut/springboot-addressbook:1.1
docker push rickyaut/springboot-addressbook:1.1
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