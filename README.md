Run Java
```sh
mvn compile exec:java
```

Build
```sh
# pack build --builder=gcr.io/buildpacks/builder:v1 sample-app:1.0.0

pack config default-builder gcr.io/buildpacks/builder:v1
pack build sample-app:1.0.0
```

Run Docker
```sh
docker run -it --rm \
  --mount type=bind,source=$TEMPORAL_TLS_KEY,target=/certs/tls.key,readonly \
  --mount type=bind,source=$TEMPORAL_TLS_CERT,target=/certs/tls.crt,readonly \
  -e TEMPORAL_ADDRESS \
  -e TEMPORAL_NAMESPACE \
  -e TEMPORAL_TLS_CERT=/certs/tls.crt \
  -e TEMPORAL_TLS_KEY=/certs/tls.key \
  sample-app:1.0.0
```

Start Workflow
```sh
temporal workflow start --type MyWorkflow -t MyTaskQueue
temporal workflow start --type GreetingWorkflow -t MyTaskQueue -i '"Peter"'
```