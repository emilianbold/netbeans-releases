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
 */

package org.netbeans.modules.deployment.wm;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.*;
import org.netbeans.mobility.activesync.*;

/**
 *
 * @author Martin Ryzl
 */
public class WindowsMobileDeployTask extends Task {
    
    private String jarFile;
    private String jadFile;
    private String appLocation;
   
    
    /** Creates a new instance of WindowsMobileDeployTask */
    public WindowsMobileDeployTask() {
    }
    
    public String getJarFile() {
        return this.jarFile;
    }
    
    public void setJarFile(final String jarFile) {
        this.jarFile = jarFile;
    }
    
    public String getJadFile() {
        return this.jadFile;
    }
    
    public void setJadFile(final String jadFile) {
        this.jadFile = jadFile;
    }
    
    public String getAppLocation() {
        return this.appLocation;
    }
    
    public void setAppLocation(final String appLocation) {
        this.appLocation = appLocation;
    }
    
    public void execute() throws BuildException {
        ActiveSyncOps activeSync = ActiveSyncOps.getDefault();
        log("Windows Mobile Deployment: ");
        log("jar: " + jarFile);
        log("jad: " + jadFile);
        
        if (activeSync == null || !activeSync.isAvailable()) throw new BuildException("active sync is not available");
        
            
        try {
            // it make take a while to recognize attacheddevice
            try {
                for (int i = 0; i < 5 && !activeSync.isDeviceConnected(); i++) {
                    Thread.sleep(400);
                }
            } catch (InterruptedException ex) {
                // ignore
            }

            if (!activeSync.isDeviceConnected()) throw new BuildException("device not connected");

            File fjar = new File(jarFile);
            if (!fjar.exists()) throw new BuildException("jar file does not exist");
            File fjad = new File(jadFile);
            if (!fjad.exists()) throw new BuildException("jad file does not exist");


            RemoteFile appdir = new RemoteFile(appLocation, basename(fjad.getName()));

            appdir = createRemoteDir(activeSync, appdir); 

            RemoteFile rjar = new RemoteFile(appdir.getFullPath(), fjar.getName()), rapp = rjar;
            RemoteFile rjad = new RemoteFile(appdir.getFullPath(), fjad.getName());
            if (rjar.exists()) activeSync.delete(rjar);
            activeSync.copyToDevice(fjar, appdir);

            if (rjad.exists()) activeSync.delete(rjad);

            if (fjad.exists()) {
                activeSync.copyToDevice(fjad, appdir);
                rapp = rjad;
            }

            // install MIDlet
            String process = "\\SUN_JVM_1.1.3\\runMidlet.exe";
            RemoteProcess rp = activeSync.executeRemoteProcess(process, new String[] {"install_file", "\"file:\\" + rapp.getFullPath() + "\""});
            StringBuffer sb = new StringBuffer(rp.getProcessName());
            String[] ar = rp.getProcessArguments();
            for(int i = 0; i < ar.length; i++) {
                sb.append(' ');
                sb.append(ar[i]);
            }
            log("remote process: " + sb.toString());

        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    
    private static RemoteFile createRemoteDir(ActiveSyncOps activeSync, final RemoteFile rf) throws IOException {
        if (!rf.exists()) {
            createRemoteDir(activeSync, rf.getParentDir());
            return activeSync.createNewDirectory(rf.getParentDir().getFullPath(), rf.getName());
        }
        return rf;
    }
    
    private static String basename(final String string) {
        int index = string.lastIndexOf('.');
        if (index != -1) return string.substring(0, index);
        return string;
    }    
    
}
