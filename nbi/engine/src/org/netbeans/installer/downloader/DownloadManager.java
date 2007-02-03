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
import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.utils.helper.FinishHandler;

/**
 * It's main downloader class. It's singleton.
 * Only from here client can access download service and register there own listeners.
 * Also from here managed execution of downloding process.
 * 
 * @author Danila_Dugurov
 */
public class DownloadManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static final DownloadManager instance = new DownloadManager();
    
    public static DownloadManager getInstance() {
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File          localDirectory;
    private FinishHandler finishHandler;
    
    private File defaultFolder;
    private DispatchedQueue queue;
    private File wd;
    
    private DownloadManager() {
    }
    
    public void init() {
        defaultFolder = new File(localDirectory, "downloads");
        defaultFolder.mkdirs();
        
        wd = new File(localDirectory, "wd");
        wd.mkdirs();
        
        queue = new DispatchedQueue(new File(wd, "state.xml"));
        queue.reset();
    }
    
    public PumpingsQueue queue() {
        return queue;
    }
    
    public void registerListener(final DownloadListener listener) {
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
        return defaultFolder;
    }
    
    public File getLocalDirectory() {
        return localDirectory;
    }
    
    public void setLocalDirectory(final File localDirectory) {
        this.localDirectory = localDirectory;
    }
    
    public FinishHandler getFinishHandler() {
        return finishHandler;
    }
    
    public void setFinishHandler(final FinishHandler finishHandler) {
        this.finishHandler = finishHandler;
    }
}