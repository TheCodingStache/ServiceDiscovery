package Cluster;

import com.sun.org.apache.regexp.internal.RE;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {
    private static final String REGISTRY_ZNODE = "/registry/znode";
    private ZooKeeper zooKeeper;
    private String currentZnode = null;
    private List<String> allServicesAddresses = null;

    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createServiceRegistryZnode();
    }

    public void registerForUpdates() {
        try {
            updateAddresses();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized List<String> getAllServicesAddresses() {
        if (allServicesAddresses == null) {
            try {
                updateAddresses();
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return allServicesAddresses;
    }

    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {
        this.currentZnode = zooKeeper.create(REGISTRY_ZNODE + "\n "
                , metadata.getBytes()
                , ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to service registry");

    }

    private void createServiceRegistryZnode() {
        try {
            if (zooKeeper.exists(REGISTRY_ZNODE, false) == null) {
                zooKeeper.create(REGISTRY_ZNODE, new byte[]{}, ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            }
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    public void unregisterFromTheCluster() throws KeeperException, InterruptedException {
        if (currentZnode != null && zooKeeper.exists(currentZnode, false) != null) {
            zooKeeper.delete(currentZnode, -1);
        }
    }

    private synchronized void updateAddresses() throws KeeperException, InterruptedException {
        List<String> workerZnodes = zooKeeper.getChildren(REGISTRY_ZNODE, this);
        List<String> addresses = new ArrayList<>(workerZnodes.size());
        for (String workerZnode : workerZnodes) {
            String zNodeFullPath = REGISTRY_ZNODE + "/" + workerZnode;
            Stat stat = zooKeeper.exists(zNodeFullPath, false);
            if (stat == null) {
                continue;
            }
            byte[] addressBytes = zooKeeper.getData(zNodeFullPath, false, stat);
            String address = new String(addressBytes);
            addresses.add(address);
        }
        this.allServicesAddresses = Collections.unmodifiableList(addresses);
        System.out.println("This cluster addresses are " + this.allServicesAddresses);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddresses();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
