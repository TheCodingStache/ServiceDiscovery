# Cluster.ElectionLeader

This project demonstrates how to use zookeeper libraries threading model.
Zookeeper client creates two additional threads in my application the I/O thread which handles the connection between the app
and the zookeeper server and the event thread on which we get all the events coming from the zookeeper server.

Summary
-IO Thread
-Event Thread
-Connection(SyncConnected)
-Disconnection(Disconnected/Expired)
