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

package org.netbeans.modules.java.examples;

import java.io.File;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * @author Martin Grebac
 */
public class J2SESampleProjectIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 4L;

    int currentIndex;
    PanelConfigureProject basicPanel;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new J2SESampleProjectIterator();
    }
    
    public J2SESampleProjectIterator () {
    }
    
    public void addChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current () {
        return basicPanel;
    }
    
    public boolean hasNext () {
        return false;
    }
    
    public boolean hasPrevious () {
        return false;
    }
    
    public void initialize (org.openide.loaders.TemplateWizard templateWizard) {
        this.wiz = templateWizard;
        String name = templateWizard.getTemplate().getNodeDelegate().getDisplayName();
        if (name != null) {
            name = name.replaceAll(" ", ""); //NOI18N
        }
        templateWizard.putProperty (WizardProperties.NAME, name);
        basicPanel = new PanelConfigureProject();
        currentIndex = 0;
        updateStepsList ();
    }
    
    public void uninitialize (org.openide.loaders.TemplateWizard templateWizard) {
        basicPanel = null;
        currentIndex = -1;
        this.wiz.putProperty("projdir",null);           //NOI18N
        this.wiz.putProperty("name",null);          //NOI18N
    }
    
    public java.util.Set instantiate (org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        File projectLocation = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        FileObject templateFO = templateWizard.getTemplate().getPrimaryFile();
        FileObject prjLoc = J2SESampleProjectGenerator.createProjectFromTemplate(
                              templateFO, projectLocation, name);

        java.util.Set set = new java.util.HashSet();
        set.add(DataObject.find(prjLoc));

        // open file from the project specified in the "defaultFileToOpen" attribute
        Object openFile = templateFO.getAttribute("defaultFileToOpen"); // NOI18N
        if (openFile instanceof String) {
            FileObject openFO = prjLoc.getFileObject((String)openFile);
            set.add(DataObject.find(openFO));
        }
        // also open a documentation file registered for this project
        // and copy the .url file for it to the project (#71985)
        FileObject docToOpen = Repository.getDefault().getDefaultFileSystem().findResource(
            "org-netbeans-modules-java-examples/OpenAfterCreated/" + templateFO.getName() + ".url"); // NOI18N
        if (docToOpen != null) {
            docToOpen = FileUtil.copyFile(docToOpen, prjLoc, "readme"); // NOI18N
            set.add(DataObject.find(docToOpen));
        }

        return set;
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        throw new NoSuchElementException ();
    }
    
    public void previousPanel() {
        throw new NoSuchElementException ();
    }
    
    void updateStepsList() {
        JComponent component = (JComponent) current ().getComponent ();
        if (component == null) {
            return;
        }
        String[] list;
        list = new String[] {
            NbBundle.getMessage(PanelConfigureProject.class, "LBL_NWP1_ProjectTitleName"), // NOI18N
        };
        component.putClientProperty ("WizardPanel_contentData", list); // NOI18N
        component.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (currentIndex)); // NOI18N
    }
    
}
