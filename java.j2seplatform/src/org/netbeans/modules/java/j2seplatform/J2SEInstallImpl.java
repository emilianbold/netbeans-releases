/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform;

import org.openide.filesystems.*;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.modules.java.j2seplatform.wizard.J2SEWizardIterator;

import java.io.IOException;
import org.openide.WizardDescriptor;

/**
 * Installer factory for standard J2SE Platforms
 *
 * @author Svatopluk Dedic
 */
class J2SEInstallImpl extends org.netbeans.spi.java.platform.PlatformInstall {
    private boolean winOS;
    
    J2SEInstallImpl(boolean winOS) {
        this.winOS = winOS;
    }
    
    static J2SEInstallImpl create() {
        boolean windows = Utilities.isWindows();
        
        return new J2SEInstallImpl(windows);
    }
    
    /**
     * Performs a quick & dirty check whether there's a JRE installed.
     * The method looks into the folder for something, which - depending on 
     * the platform's conventions - has name "java.exe" or "java"
     */
    public boolean accept(FileObject dir) {
        if (!dir.isFolder())
            return false;
        dir = dir.getFileObject("bin"); // NOI18N
        if (dir == null)
            return false;
        FileObject javaRunnable;
        if (winOS) {
            javaRunnable = dir.getFileObject("java", "exe");
        } else {
            javaRunnable = dir.getFileObject("java");
        }
        return javaRunnable != null;
    }
    
    public WizardDescriptor.InstantiatingIterator createIterator(FileObject baseFolder) {
        try {
            return new J2SEWizardIterator(baseFolder);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify (ioe);
            return null;
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(J2SEInstallImpl.class,"TXT_J2SEPlatform");
    }
}
