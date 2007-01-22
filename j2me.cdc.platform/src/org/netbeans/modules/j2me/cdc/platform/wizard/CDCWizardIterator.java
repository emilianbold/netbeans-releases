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

package org.netbeans.modules.j2me.cdc.platform.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.event.ChangeListener;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Wizard Iterator for standard J2SE platforms. It assumes that there is a
 * 'bin{/}java[.exe]' underneath the platform's directory, which can be run to
 * produce the target platform's VM environment.
 *
 * @author Svata Dedic, Tomas Zezula
 */
public class CDCWizardIterator implements WizardDescriptor.InstantiatingIterator {

    static final String CDC_PLATFORM_PROP = "cdcplatfrom";
            
    DataFolder                  installFolder;
    DetectPanel.WizardPanel     detectPanel;
    Collection<ChangeListener>  listeners;
    CDCPlatform                 platform;
    WizardDescriptor            wizard;
    int                         currentIndex;
    CDCPlatformDetector         detector;
    
    /**
     * @param type @see CDCPlatformImpl.java
     */
    public CDCWizardIterator(FileObject installFolder, CDCPlatformDetector detector) {
        this.detector = detector;
        this.installFolder = DataFolder.findFolder(installFolder);
        listeners= new ArrayList<ChangeListener>();
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
        this.detectPanel = new DetectPanel.WizardPanel(this, detector);
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
        CDCPlatform newPlatform = getPlatform();
        final String systemName = platform.getAntName();
        FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource(
                "Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
        if (platformsFolder.getFileObject(systemName,"xml")!=null) {   //NOI18N
            String msg = NbBundle.getMessage(CDCWizardIterator.class,"ERROR_InvalidName");
            throw (IllegalStateException)ErrorManager.getDefault().annotate(
                new IllegalStateException(msg), ErrorManager.USER, null, msg,null, null);
        }
        DataObject dobj = PlatformConvertor.create(newPlatform, DataFolder.findFolder(platformsFolder),systemName);
        JavaPlatform platform = (JavaPlatform) dobj.getNodeDelegate().getLookup().lookup(JavaPlatform.class);
        return Collections.singleton(platform);
    }

    public String name() {
        return NbBundle.getMessage(CDCWizardIterator.class, "TITLE_PlatformName");
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
    
    public CDCPlatform getPlatform(){
        return platform;
    }

    public void setPlatform(CDCPlatform platform){
        this.platform = platform;
    }
}
