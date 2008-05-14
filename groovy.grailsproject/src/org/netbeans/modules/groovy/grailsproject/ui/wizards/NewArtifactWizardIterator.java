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
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import java.util.concurrent.Callable;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsRuntime;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.execution.DefaultDescriptor;
import org.netbeans.modules.groovy.grailsproject.execution.ExecutionService;
import org.openide.util.Task;


/**
 *
 * @author schmidtm
 */
public class NewArtifactWizardIterator implements  WizardDescriptor.InstantiatingIterator<WizardDescriptor>,
                                                      WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor>{
    
     GetArtifactNameStep pls = null;
    boolean        serverRunning = false;
    boolean        serverConfigured = true;
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
                serverCommand = "create-domain-class"; // NOI18N
                break;
            case CONTROLLERS:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_CONTROLLERS");
                serverCommand = "create-controller"; // NOI18N
                break;
            case SERVICES:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_SERVICES");
                serverCommand = "create-service"; // NOI18N
                break; 
            case VIEWS:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_VIEWS");
                serverCommand = "generate-views"; // NOI18N
                break;    
            case TAGLIB:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_TAGLIB");
                serverCommand = "create-tag-lib"; // NOI18N
                break;    
            case SCRIPTS:
                wizardTitle = NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_SCRIPTS");
                serverCommand = "create-script"; // NOI18N
                break;    
            }
        }
   
   public NewArtifactWizardIterator (GrailsProject project, SourceCategory cat, String artifactName) {
        this (project, cat);
        this.artifactName = artifactName;
    } 
       
   public Set instantiate(ProgressHandle handle) throws IOException {
            Set<FileObject> resultSet = new HashSet<FileObject>();
            
            serverRunning = true;

            handle.start(100);
            try {
                ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
                String displayName = inf.getDisplayName() + " (" + serverCommand + ")"; // NOI18N

                Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                        serverCommand, GrailsProjectConfig.forProject(project), pls.getDomainClassName());
                ExecutionService service = new ExecutionService(callable, displayName,
                        new DefaultDescriptor(project, new ProgressSnooper(handle, 100, 2) , null, false, false, true, true, false));

                Task task = service.run();
                task.waitFinished();
            } finally {
                handle.progress(100);
            }
            serverRunning = false;
            
            LOG.log(Level.FINEST, "Artifact Name: " + pls.getFileName());
            File artifactFile = new File(pls.getFileName());
            
            if (artifactFile != null) {
                LOG.log(Level.FINEST, "Created File: " + artifactFile.getAbsolutePath());
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
        if(!GrailsRuntime.getInstance().isConfigured()) {
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
            jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(1)); // NOI18N
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
            new Object[] {Integer.valueOf(1), Integer.valueOf(1)});
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
