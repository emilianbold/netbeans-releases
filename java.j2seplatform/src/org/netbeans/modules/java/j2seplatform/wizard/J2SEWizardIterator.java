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

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * Wizard Iterator for standard J2SE platforms. It assumes that there is a
 * 'bin{/}java[.exe]' underneath the platform's directory, which can be run to
 * produce the target platform's VM environment.
 *
 * @author Svata Dedic, Tomas Zezula
 */
public class J2SEWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final String[] SOLARIS_64_FOLDERS = {"sparcv9","amd64"};     //NOI18N

    DataFolder                  installFolder;
    DetectPanel.WizardPanel     detectPanel;
    Collection                  listeners;
    NewJ2SEPlatform             platform;
    NewJ2SEPlatform             secondaryPlatform;
    WizardDescriptor            wizard;
    int                         currentIndex;

    public J2SEWizardIterator(FileObject installFolder) throws IOException {
        this.installFolder = DataFolder.findFolder(installFolder);
        this.platform = NewJ2SEPlatform.create (installFolder);        
        String archFolder = null;
        for (int i = 0; i< SOLARIS_64_FOLDERS.length; i++) {
            if (Util.findTool("java",Collections.singleton(installFolder),SOLARIS_64_FOLDERS[i]) != null) {
                archFolder = SOLARIS_64_FOLDERS[i];
                break;
            }
        }
        if (archFolder != null) {
            this.secondaryPlatform  = NewJ2SEPlatform.create (installFolder);
            this.secondaryPlatform.setArchFolder(archFolder);
        }
    }

    FileObject getInstallFolder() {
        return installFolder.getPrimaryFile();
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public WizardDescriptor.Panel current() {
        switch (this.currentIndex) {
            case 0:
                return this.detectPanel;
            default:
                throw new IllegalStateException();
        }
    }

    public boolean hasNext() {
        return false;
    }

    public boolean hasPrevious() {
        return false;
    }

    public void initialize(WizardDescriptor wiz) {
        this.wizard = wiz;
        this. detectPanel = new DetectPanel.WizardPanel(this);
        this.currentIndex = 0;
    }

    /**
     * This finally produces the java platform's XML that represents the basic
     * platform's properties. The XML is returned in the resulting Set.
     * @return singleton Set with java platform's instance DO inside.
     */
    public java.util.Set instantiate() throws IOException {
        //Workaround #44444
        this.detectPanel.storeSettings (this.wizard);
        Set result = new HashSet ();
        for (Iterator it = getPlatforms().iterator(); it.hasNext();) {        
            NewJ2SEPlatform platform = (NewJ2SEPlatform) it.next();
            if (platform.isValid()) {
                final String systemName = platform.getAntName();
                FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource(
                        "Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
                if (platformsFolder.getFileObject(systemName,"xml")!=null) {   //NOI18N
                    String msg = NbBundle.getMessage(J2SEWizardIterator.class,"ERROR_InvalidName");
                    throw (IllegalStateException)ErrorManager.getDefault().annotate(
                        new IllegalStateException(msg), ErrorManager.USER, null, msg,null, null);
                }                       
                DataObject dobj = PlatformConvertor.create(platform, DataFolder.findFolder(platformsFolder),systemName);
                result.add((JavaPlatform) dobj.getNodeDelegate().getLookup().lookup(JavaPlatform.class));
            }
        }        
        return Collections.unmodifiableSet(result);
        
    }

    public String name() {
        return NbBundle.getMessage(J2SEWizardIterator.class, "TITLE_PlatformName");
    }

    public void nextPanel() {
        this.currentIndex++;
    }

    public void previousPanel() {
        this.currentIndex--;
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wizard = null;        
        this.detectPanel = null;
    }

    public NewJ2SEPlatform getPlatform() {
        return this.platform;
    }      
    
    public NewJ2SEPlatform getSecondaryPlatform () {
        return this.secondaryPlatform;
    }
    
    private List getPlatforms () {
        List result = new ArrayList ();
        result.add(this.platform);
        if (this.secondaryPlatform != null) {
            result.add(this.secondaryPlatform);
        }
        return result;
    }
}
