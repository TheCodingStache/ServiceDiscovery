import com.sun.org.apache.regexp.internal.RE;
import org.apache.zookeeper.*;

import java.util.List;

public class ServiceRegistry implements Watcher {
    private static final String REGISTRY_ZNODE = "/registry/znode";
    private ZooKeeper zooKeeper;
    private String currentZnode = null;
    private List<String> allServicesAddresses = null;

    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    private void registerToCluster(String metadata) throws KeeperException, InterruptedException {
        this.currentZnode = zooKeeper.create(REGISTRY_ZNODE + "\n "
                , metadata.getBytes()
                , ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                 System.out.println("Registered to service registry");

    }

    @Override
    public void process(WatchedEvent watchedEvent) {

    }
}
