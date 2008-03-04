/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import javax.swing.JComponent;
import org.openide.util.NbBundle;
import java.io.File;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.netbeans.modules.groovy.grails.api.GrailsServerFactory;
import org.netbeans.api.progress.ProgressHandle;
import java.io.BufferedReader;
import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.actions.PublicSwingWorker;


/**
 *
 * @author schmidtm
 */
public class NewArtifactWizardIterator implements  WizardDescriptor.InstantiatingIterator<WizardDescriptor>,
                                                      WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor>{
    
    private transient WizardDescriptor wiz;
    
    BufferedReader procOutput = null;
    GetArtifactNameStep pls = null;
    ProgressHandle handle = null;
    CountDownLatch serverFinished = new CountDownLatch(1);
    boolean        serverRunning = false;
    boolean        serverConfigured = true;
    GrailsServer server = GrailsServerFactory.getServer();
    GrailsProject project;
    SourceCategory cat;
    String wizardTitle;
    String serverCommand;
    String artifactName = "";
    
    private  final Logger LOG = Logger.getLogger(NewArtifactWizardIterator.class.getName());
    
    public NewArtifactWizardIterator (GrailsProject project, SourceCategory cat) {
        this.project = project;
        this.cat = cat;
        
        switch(cat){
            case DOMAIN:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_DOMAIN");
                serverCommand = NbBundle.getMessage(NewArtifactWizardIterator.class,"SERVER_COMMAND_DOMAIN");
                break;
            case CONTROLLERS:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_CONTROLLERS");
                serverCommand = NbBundle.getMessage(NewArtifactWizardIterator.class,"SERVER_COMMAND_CONTROLLERS");
                break;
            case SERVICES:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_SERVICES");
                serverCommand = NbBundle.getMessage(NewArtifactWizardIterator.class,"SERVER_COMMAND_SERVICES");
                break; 
            case VIEWS:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_VIEWS");
                serverCommand = NbBundle.getMessage(NewArtifactWizardIterator.class,"SERVER_COMMAND_VIEWS");
                break;    
            }
        }
   
   public NewArtifactWizardIterator (GrailsProject project, SourceCategory cat, String artifactName) {
        this (project, cat);
        this.artifactName = artifactName;
    } 
       
   public Set instantiate(ProgressHandle handle) throws IOException {
        
            this.handle = handle;

            Set<FileObject> resultSet = new HashSet<FileObject>();
            
            serverRunning = true;
            
            new PublicSwingWorker(project, serverCommand  + " " + pls.getDomainClassName(),
                                    null, handle, serverFinished).start();
            
            try {
                serverFinished.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    }

            serverRunning = false;
            
            LOG.log(Level.WARNING, "Artifact Name: " + pls.getFileName());
            File artifactFile = new File(pls.getFileName());
            
            if (artifactFile != null) {
                LOG.log(Level.WARNING, "Created File: " + artifactFile.getAbsolutePath());
                project.getProjectDirectory().getFileSystem().refresh(true);
                artifactFile = FileUtil.normalizeFile(artifactFile);
                FileObject fo = FileUtil.toFileObject(artifactFile);
                
                if (fo == null){
                    LOG.log(Level.WARNING, "Problem creating FileObject(null): " + artifactFile.getAbsolutePath());
                    }
                resultSet.add(fo);
            }

            return resultSet;

    }
    
    public Set instantiate() throws IOException {

            Set<FileObject> resultSet = new HashSet<FileObject>();

            return resultSet;
    }

    
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        
        if(!server.serverConfigured()) {
            wizard.putProperty("WizardPanel_errorMessage", 
                    NbBundle.getMessage(NewArtifactWizardIterator.class, 
                    "NewGrailsProjectWizardIterator.NoGrailsServerConfigured"));
            serverConfigured = false;
            }
        
        pls = new GetArtifactNameStep(serverRunning, serverConfigured, project, cat);
        
        Component c = pls.getComponent();
        
        pls.setArtifactName(artifactName);
        
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent)c;
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1)); // NOI18N
            jc.putClientProperty("WizardPanel_contentData", new String[] { wizardTitle }  ); // NOI18N
            }
        
    }

    public void uninitialize(WizardDescriptor wizard) {

    }

    public WizardDescriptor.Panel<WizardDescriptor> current() {
            return pls;
    }

    public String name() {
        return MessageFormat.format (NbBundle.getMessage(NewArtifactWizardIterator.class,"LAB_IteratorName"),
            new Object[] {new Integer (1), new Integer (1) });      
    }

    public boolean hasNext() {
            return false;
    }

    public boolean hasPrevious() {
            return false;
    }

    public void nextPanel() {
        // do nothing, there's only one
    }

    public void previousPanel() {
        // do nothing, there's only one
    }

    public void addChangeListener(ChangeListener l) {}

    public void removeChangeListener(ChangeListener l) {}
    
}
