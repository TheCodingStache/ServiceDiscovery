import org.apache.log4j.Logger;
import org.apache.log4j.lf5.viewer.LogTable;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import sun.rmi.runtime.Log;

import java.io.IOException;

public class ElectionLeader implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;
    private static Logger logger;

    public static void main(String[] args) throws IOException, InterruptedException {
        logger = Logger.getLogger(ElectionLeader.class);
        ElectionLeader electionLeader = new ElectionLeader();
        electionLeader.connectToZookeeper();
        electionLeader.run();
        electionLeader.close();
        System.out.println("Disconnected from Zookeeper, exiting application");
    }

    private void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    private void close() throws InterruptedException {
        zooKeeper.close();
    }

    private void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    logger.info("Connected successfully");
                    System.out.println("Connected Successfully");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
