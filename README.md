# simple-tcp-proxy
simple tcp proxy made with java

## usage
java -jar <jar> --listenHost=<listenIP> --listenPort=<listenPort> --remoteHost=<remoteHost> --remotePort=<remortPort>

## example
java -jar simple-tcp-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar --listenHost=0.0.0.0 --listenPort=81 --remoteHost=127.0.0.1 --remotePort=80
