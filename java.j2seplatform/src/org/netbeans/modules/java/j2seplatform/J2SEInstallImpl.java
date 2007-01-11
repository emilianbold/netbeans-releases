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

package org.netbeans.modules.java.j2seplatform;

import org.openide.filesystems.*;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.modules.java.j2seplatform.wizard.J2SEWizardIterator;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;

import java.io.IOException;
import java.io.File;
import java.util.Collections;

import org.openide.WizardDescriptor;

/**
 * Installer factory for standard J2SE Platforms
 *
 * @author Svatopluk Dedic
 */
class J2SEInstallImpl extends org.netbeans.spi.java.platform.PlatformInstall {

    private static final String APPLE_JAVAVM_FRAMEWORK_PATH = "/System/Library/Frameworks/JavaVM.framework/Versions/";//NOI18N

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
        if (!dir.isFolder()) {
            return false;
        }
        if (Utilities.isMac()) {
            //For MacOS X only the version folder is interesting
            // all other places are links to it
            File f = FileUtil.toFile(dir);
            if (f == null || !f.getAbsolutePath().startsWith(APPLE_JAVAVM_FRAMEWORK_PATH))
                return false;
        }
        FileObject tool = Util.findTool("java", Collections.singleton(dir));    //NOI18N
        if (tool == null) {
            return false;
        }
        tool = Util.findTool("javac", Collections.singleton(dir));  //NOI18N
        return tool != null;
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
