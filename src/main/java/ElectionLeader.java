import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ElectionLeader implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NAMESPACE = "/election";
    private static final String TARGET_ZNODE = "/target_znode";
    private ZooKeeper zooKeeper;
    private static Logger logger;
    private String currentZnodeName;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        logger = Logger.getLogger(ElectionLeader.class);
        ElectionLeader electionLeader = new ElectionLeader();
        electionLeader.connectToZookeeper();
        electionLeader.volunteerForLeaderShip();
        electionLeader.electLeader();
        electionLeader.run();
        electionLeader.close();
        System.out.println("Disconnected from Zookeeper, exiting application");
    }

    private void volunteerForLeaderShip() throws KeeperException, InterruptedException {
        String zNodePrefix = ELECTION_NAMESPACE + "/c_";
        String zNodeFullPath = zooKeeper.create(zNodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("znode name " + zNodeFullPath);
        this.currentZnodeName = zNodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    private void watcherTargetNode() throws IOException, InterruptedException, KeeperException {
        Stat stat = zooKeeper.exists(TARGET_ZNODE, this);
        if (stat == null) {
            return;
        }
        byte[] metaData = zooKeeper.getData(TARGET_ZNODE, this, stat);
        List<String> children = zooKeeper.getChildren(TARGET_ZNODE, this);
        System.out.println("Data : " + new String(metaData) + " children: " + children);
    }

    private void electLeader() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
        Collections.sort(children);
        String smallestChild = children.get(0);
        if (smallestChild.equals(currentZnodeName)) {
            System.out.println("I am the leader");
        } else {
            System.out.println("I am not the leader " + smallestChild + " is the leader");
        }
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
                break;
            case NodeDeleted:
                System.out.println(TARGET_ZNODE + " was deleted");
                break;
            case NodeCreated:
                System.out.println(TARGET_ZNODE + " was created");
                break;
            case NodeDataChanged:
                System.out.println(TARGET_ZNODE + " data changed");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_ZNODE + "children changed");
                break;
        }

        try {
            watcherTargetNode();
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
}
