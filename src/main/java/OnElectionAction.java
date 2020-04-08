import Cluster.OnElectionCallback;
import Cluster.ServiceRegistry;
import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry serviceRegistry;
    private final int port;

    public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedLeader() {
        try {
            serviceRegistry.unregisterFromTheCluster();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        serviceRegistry.registerForUpdates();
    }

    @Override
    public void onWorker() {
        try {
            String currentServerAddress = String.format("http://%s:%d", InetAddress.getLocalHost().getCanonicalHostName(), port);
            serviceRegistry.registerToCluster(currentServerAddress);
        } catch (InterruptedException | KeeperException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
