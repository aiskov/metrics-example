# Example of Using Prometheus for Monitoring

## Goal

Show work configuration of two services and monitoring for them. 

Monitoring should allow to show:

* Application URL access via load balancer to show that application accessible for users
* Application System metrics CPU, Memory, DB connections, Thread pools.
* Application Business metrics - some custom metrics calculated on user requests programmatically
* DB availability
* Server resources metrics CPU, Memory, Disk (node_exporter)

Based on these metrics should be developed:

* Alarm triggers
* Graph dashboards

## Application & Infrastructure

We will have ToDo application which manage list of tasks. In order to store data it will use MySQL. Because it will be 
deployed in several instances we will use Nginx server as L7 (application/http layer) load balancer.

**NB:** It is not an AIM of this example to show: application architecture, scaling, load balancing and so on. Please do not
use presented configuration as some production ready examples. 

![Deployment Diagram](docs/deployment.png)

## Run example

Requirements (based on configuration, not required for monitoring it self):

* Gradle 6+
* Java 14+
* Docker
* Docker Compose


```bash
# Build JAR artifact to run in container
gradle clean bootJar 

# Create common network for two docker compose's
docker network create metrics-demo

# Run infrastructure: API documentation, Monitoring, DB
docker-compose -f './docker-compose-inf.yml' -p demo-inf up -d --force-recreate

# Please wait here until infrastructure will be available (time to coffee? =)
 
# Run application: Instances, Load balancer
docker-compose -f './docker-compose-app.yml' -p demo-app up -d 
```

**NB:** Start of demo application and infrastructure separated into different compose files because application
requires to have access to the mysql at start of the instances.

Dont forget to clean up your environment at the end 

```bash
docker-compose -f './docker-compose-inf.yml' -p demo-inf down
docker-compose -f './docker-compose-app.yml' -p demo-app down

docker network rm metrics-demo
```

## Show how it works 

### Application deployment

How works load balancer
