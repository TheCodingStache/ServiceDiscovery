package Cluster;

public interface OnElectionCallback {
    void onElectedLeader();

    void onWorker();
}
