# Service Discovery & Service Registry based on Leader Election Algorithm

This project demonstrates how to use zookeeper libraries threading model.
Zookeeper client creates two additional threads in my application the I/O thread which handles the connection between the app
and the zookeeper server and the event thread on which we get all the events coming from the zookeeper server. Except from that, in this project you will the implementation of a service registry and service discovery mechanism where every node will store its addresses by using the functionallities of the apache zookeeper.

Summary
-IO Thread
-Event Thread
-Connection(SyncConnected)
-Disconnection(Disconnected/Expired)
