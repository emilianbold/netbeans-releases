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
import java.io.IOException;
import org.netbeans.installer.Installer;

import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.utils.FileUtils;

/**
 * @author Danila_Dugurov
 */

/**
 * It's main downloader class. It's singleton.
 * Only from here client can access download service and register there own listeners.
 * Also from here managed execution of downloding process.
 */
public class DownloadManager {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Constants
  public static final DownloadManager instance = new DownloadManager();
  private static File defaultFolder;
  
  /////////////////////////////////////////////////////////////////////////////////
  // Instance
  private final DispatchedQueue queue;
  private final File wd;
  
  private DownloadManager() {
    wd = new File(Installer.DEFAULT_LOCAL_DIRECTORY_PATH, "wd");
    defaultFolder = DownloadConfig.DEFAULT_OUTPUT_DIR;
    wd.mkdirs();
    queue = new DispatchedQueue(new File(wd, "state.xml"));
    queue.reset();
  }
  
  public PumpingsQueue queue() {
    return queue;
  }
  
  public void registerListener(DownloadListener listener) {
    queue.addListener(listener);
  }
  
  public void invoke() {
    queue.invoke();
  }
  
  public void terminate() {
    queue.terminate();
  }
  
  public boolean isActive() {
    return queue.isActive();
  }
  
  public File getWd() {
    return wd;
  }
  
  public File defaultFolder() {
    defaultFolder.mkdirs();
    return defaultFolder;
  }
}