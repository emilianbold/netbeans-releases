package org.netbeans.installer.downloader.dispatcher.impl;

import org.netbeans.installer.downloader.dispatcher.Process;

/**
 * @author Danila_Dugurov
 */
public class Worker extends Thread {
    
    Process current;
    
    public Worker() {
        super();
        setDaemon(true);
    }
    
    //if worker busy return false
    public synchronized boolean setCurrent(Process newCurrent) {
        if (!isFree()) return false;
        this.current = newCurrent;
        notify();
        return true;
    }
    
    public synchronized boolean isFree() {
        return current == null;
    }
    
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    if (current == null) wait();
                }
                current.init();
                current.run();
            } catch (InterruptedException ignored) {
            } finally {
                synchronized (this) {
                    current = null;
                }
            }
        }
    }
}
