# Play Akka Messenger
A Play Framework application for running akka-messenger cluster nodes.

## Run Local
To run locally start a few Play cluster nodes.

sbt "run -Dhttp.port=9000 -DRUN_LOCAL=1 -DCLUSTER_PORT=2552"

sbt "run -Dhttp.port=9001 -DRUN_LOCAL=1 -DCLUSTER_PORT=2553"

sbt "run -Dhttp.port=9002 -DRUN_LOCAL=1 -DCLUSTER_PORT=2554"

## Run in AWS
The application will run in a Docker container in AWS.
EC2 instances tagged with `Name: akka-messenger` will be used by Akka Service Discovery and 
Akka Bootstrap will create the cluster.

https://developer.lightbend.com/docs/akka-management/current/discovery/index.html

https://developer.lightbend.com/docs/akka-management/current/discovery/index.html#discovery-method-aws-api

https://developer.lightbend.com/docs/akka-management/current/bootstrap/index.html