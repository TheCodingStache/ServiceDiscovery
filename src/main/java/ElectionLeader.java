import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import java.util.Collections;
import java.util.List;

public class ElectionLeader implements Watcher {
    private static final String ELECTION_NAMESPACE = "/election";
    private ZooKeeper zooKeeper;
    private String currentZnodeName;
    private final OnElectionCallback onElectionCallback;

    public ElectionLeader(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
        this.zooKeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;
    }


    public void volunteerForLeaderShip() throws KeeperException, InterruptedException {
        String zNodePrefix = ELECTION_NAMESPACE + "/c_";
        String zNodeFullPath = zooKeeper.create(zNodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("znode name " + zNodeFullPath);
        this.currentZnodeName = zNodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reElectLeader() throws KeeperException, InterruptedException {
        String predecessorZnodeName = "";
        Stat predecessorStat = null;
        while (predecessorStat == null) {
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String smallestChild = children.get(0);
            if (smallestChild.equals(currentZnodeName)) {
                System.out.println("I am the leader");
                onElectionCallback.onElectedLeader();
                return;
            } else {
                System.out.println("I am not the leader " + smallestChild + " is the leader");
                int predecessorId = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorId);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
            }
        }
        onElectionCallback.onWorker();
        System.out.println("Watching znode " + predecessorZnodeName);
        System.out.println();
    }
    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Connected Successfully");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from event");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                try {
                    reElectLeader();
                } catch (InterruptedException | KeeperException e) {
                    e.printStackTrace();
                }
        }


    }
}
