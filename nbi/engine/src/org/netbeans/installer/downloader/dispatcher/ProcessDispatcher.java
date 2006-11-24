package org.netbeans.installer.downloader.dispatcher;

public interface ProcessDispatcher {

    boolean schedule(Process process);

    void terminate(Process process);

    boolean isActive();

    int activeCount();

    int waitingCount();

    void start();

    void stop();
}
