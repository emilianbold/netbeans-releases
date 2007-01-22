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

package org.netbeans.modules.j2me.cdc.platform;

import java.util.Collection;

import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.modules.j2me.cdc.platform.wizard.CDCWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Installer factory for standard CDC Platforms
 *
 * @author Svatopluk Dedic
 */
class CDCInstallImpl extends org.netbeans.spi.java.platform.PlatformInstall {

    private CDCPlatformDetector detector;

    CDCInstallImpl() {
    }
    
    static CDCInstallImpl create() {
        return new CDCInstallImpl();
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
        
        Collection<CDCPlatformDetector> col = Lookup.getDefault().lookup(new Lookup.Template(CDCPlatformDetector.class)).allInstances();
        for (CDCPlatformDetector detector:col) {
            if (detector.accept(dir)){
                this.detector = detector;
                return true;
            }
        }
        return false;
    }
    
    public WizardDescriptor.InstantiatingIterator createIterator(FileObject baseFolder) {
        return new CDCWizardIterator(baseFolder, detector);
    }

    public String getDisplayName() {
        return  NbBundle.getMessage(CDCInstallImpl.class,"TXT_CDCPlatform");//NOI18N
    }
}
