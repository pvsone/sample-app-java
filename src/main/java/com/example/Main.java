package com.example;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {

  static final String TASK_QUEUE = "MyTaskQueue";

  public static void main(String[] args) throws Exception {
    InputStream clientCert = new FileInputStream(System.getenv("TEMPORAL_TLS_CERT"));
    InputStream clientKey = new FileInputStream(System.getenv("TEMPORAL_TLS_KEY"));
    String targetEndpoint = System.getenv("TEMPORAL_ADDRESS");
    String namespace = System.getenv("TEMPORAL_NAMESPACE");

    // Create SSL enabled client by passing SslContext, created by SimpleSslContextBuilder.
    WorkflowServiceStubs service =
        WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setSslContext(SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build())
                .setTarget(targetEndpoint)
                .build());

    WorkflowClient client =
        WorkflowClient.newInstance(
            service, WorkflowClientOptions.newBuilder().setNamespace(namespace).build());

    // worker factory that can be used to create workers for specific task queues
    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);

    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);
    worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
    worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

    factory.start();

    // Create the workflow client stub. It is used to start our workflow execution.
    MyWorkflow myWorkflow =
        client.newWorkflowStub(
            MyWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId("HelloWorkflow")
                .setTaskQueue(TASK_QUEUE)
                .build());
    String message = myWorkflow.execute();
    System.out.println(message);
    
    GreetingWorkflow greetingWorkflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId("GreetingWorkflow")
                .setTaskQueue(TASK_QUEUE)
                .build());
    String greeting = greetingWorkflow.getGreeting("World");
    System.out.println(greeting);

    // System.exit(0);
  }
}
