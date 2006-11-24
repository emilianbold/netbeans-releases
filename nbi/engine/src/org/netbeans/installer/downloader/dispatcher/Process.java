package org.netbeans.installer.downloader.dispatcher;

public interface Process {

    void init();

    void run();

    void terminate();
}
