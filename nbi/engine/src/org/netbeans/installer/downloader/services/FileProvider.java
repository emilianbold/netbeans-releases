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
 */
package org.netbeans.installer.downloader.services;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.Pumping.State;
/**
 *
 * @author Danila_Dugurov
 */
//todo: may be very general synchronization - optimize!
public class FileProvider {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Static
  private static final FileProvider fileProvider = new FileProvider();
  
  public static FileProvider getProvider() {
    return fileProvider;
  }
  
  /////////////////////////////////////////////////////////////////////////////////
  // Instance
  private final DownloadManager downloadManager = DownloadManager.instance;
  
  private final DownloadListener listener = new MyListener();
  
  private final PersistentCache cache = new PersistentCache();
  
  private final Map<URL, State> scheduledURL2State = new HashMap<URL, State>();
  
  protected FileProvider() {
    downloadManager.registerListener(listener);
  }
  
  public synchronized void clearCaches() {
    for (URL url: cache.keys()) {
      cache.delete(url);
    }
  }
  
  public synchronized boolean isInCache(URL url) {
    return cache.isIn(url);
  }
  
  public synchronized void asynchDownload(URL url, File folder) {
    if (isInCache(url)) return;
    if (scheduledURL2State.containsKey(url)) return;
    if (!downloadManager.isActive()) downloadManager.invoke();
    scheduledURL2State.put(url, State.NOT_PROCESSED);
    downloadManager.queue().add(url, folder != null ? folder: downloadManager.defaultFolder());
  }
  
  public synchronized File get(URL url) throws InterruptedException {
    return get(url, null, true);
  }
  
  public synchronized File get(URL url, File folder) throws InterruptedException {
    return get(url, folder, true);
  }
  
  public synchronized File get(URL url, boolean useCache) throws InterruptedException {
    return get(url, null, useCache);
  }
  
  public synchronized File get(URL url, File folder, boolean useCache) throws InterruptedException {
    while (true) {
      final File file = tryGet(url);
      if (file != null) {
        if (useCache) return file;
        cache.delete(url);
        useCache = true;
      }
      asynchDownload(url, folder);
      wait();
      //todo: refactor this agly pice of code below
      if (scheduledURL2State.containsKey(url) && scheduledURL2State.get(url) == State.FAILED) {
        scheduledURL2State.remove(url);
        return null;// this temporary. unlike good reaction to return null if faild to load!
      }
    }
  }
  
  public synchronized File tryGet(URL url) {
    if (cache.isIn(url)) return cache.getByURL(url);
    return null;
  }
  
  public synchronized boolean manuallyDelete(URL url) {
    return cache.delete(url);
  }  
  
  /////////////////////////////////////////////////////////////////////////////////
  // Inner Classes
  private class MyListener extends EmptyQueueListener {
    public void pumpingStateChange(String id) {
      final Pumping pumping = downloadManager.queue().getById(id);
      final URL url = pumping.declaredURL();
      scheduledURL2State.put(url, pumping.state());
      switch(pumping.state()) {
        case FINISHED: {
          cache.put(url, pumping.outputFile());
          scheduledURL2State.remove(url);
        }
        case FAILED:
          synchronized(FileProvider.this) {
            FileProvider.this.notifyAll();
          }
      }
    }
  }
}