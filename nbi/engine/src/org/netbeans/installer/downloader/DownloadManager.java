/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * $Id$
 */
package org.netbeans.installer.downloader;

import java.io.File;
import java.net.URL;
import org.netbeans.installer.downloader.queue.EmptyQueueListener;

import org.netbeans.installer.downloader.queue.URLQueue;
import org.netbeans.installer.downloader.queue.URLStatus;
import org.netbeans.installer.downloader.queue.URLWrap;
import org.netbeans.installer.downloader.queue.actions.DownloadAction;
import org.netbeans.installer.downloader.queue.impl.SimpleQueue;

/**
 * @author Danila_Dugurov
 *
 */
public class DownloadManager {
  
  private static final String WORKERS_GROUP_NAME = "workers";
  private static final String SERVICE_GROUP_NAME = "service";
  
  private static final int WORKERS_COUNT = 8;
  
  final ThreadGroup workersPool = new ThreadGroup(WORKERS_GROUP_NAME);
  final ThreadGroup serviceGroup = new ThreadGroup(SERVICE_GROUP_NAME);
  
  final SimpleQueue queue;
  final DownloadFilesBase filesBase;
  
  final File workDir;
  final File outputDir;
  
  private static DownloadManager MANAGER;
  
  private DownloadManager(File workDir, File outputDir) {
    this.workDir = workDir;
    this.outputDir = outputDir;
    queue = new SimpleQueue();
    filesBase = new DownloadFilesBase();
    queue.addListener(filesBase);
  }
  
  public static DownloadManager getInstance() {
    if (MANAGER == null) {
      MANAGER = new DownloadManager(DownloaderConsts.getWorkingDirectory(),
        DownloaderConsts.getOutputDirectory());
      return MANAGER;
    }
    return MANAGER;
  }
  
  private boolean isRunning = false;
  
  public synchronized void start() {
    if (!isRunning) {
      for (int i = 0; i < WORKERS_COUNT; i++) {
        new Thread(workersPool, new Worker()).start();
      }
      final StateDumper dumper = new StateDumper();
      queue.addListener(dumper);
      new Thread(serviceGroup, dumper).start();
      isRunning = true;
    }
  }
  
  public synchronized void shutdown() {
    if (isRunning) {
      synchronized (queue) {
        workersPool.interrupt();
        serviceGroup.interrupt();
      }
      while (workersPool.activeCount() > 0) {
        try {
          Thread.currentThread().sleep(100);
        } catch(InterruptedException ex) {}
      }
      filesBase.dump();
      //    queue.dump();// currently team dicide not to save queue state
      isRunning = false;
      // queue.suspendAll();
    }
  }
  
  public URLQueue getURLQueue() {
    return queue;
  }
  
  protected DownloadFilesBase getFilesBase() {
    return filesBase;
  }
  
  private class Worker implements Runnable {
    
    private URLWrap processingURL;
    
    private DownloadAction downloadAction;
    
    public void run() {
      while (true) {
        if (processingURL != null) {
          downloadAction = new DownloadAction(queue, processingURL);
          while (true) {
            queue.performAction(downloadAction);
            if (downloadAction.getResult() == null) break;
            if (Thread.interrupted()) return;
            if (!processingURL.write(downloadAction.getResult()))
              //TODO: write may fails on shutdown.
              //  throw new RuntimeException("can't write to fileBase");
              Thread.yield();
          }
        }
        processingURL = null;
        synchronized (queue) {
          try {
            if (Thread.interrupted()) return;
            if (queue.isEmpty()) {
              queue.wait();
              continue;
            }
          } catch (InterruptedException exit) {
            return;
          }
          processingURL = queue.pop();
        }
      }
    }
  }
  
  private class StateDumper extends EmptyQueueListener implements Runnable {
    
    public void run() {
      try {
        while (true) {
          if (Thread.interrupted()) return;
          synchronized (this) {
            wait();
          }
          //queue.dump();// currently team dicide not to save queue state
          filesBase.dump();
        }
      } catch(InterruptedException exit) {}
    }
    
    public void URLStatusChanged(URL url){
      if (queue.getStatus(url) == URLStatus.DOWNLOAD_FINISHED) {
        synchronized (this) {
          notify();
        }
      }
    }
  }
}
