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
import org.netbeans.modules.extexecution.api.input.InputProcessor;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor;
import org.netbeans.modules.extexecution.api.ExecutionService;
import org.netbeans.modules.extexecution.api.input.InputProcessors;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsRuntime;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.util.Exceptions;

/**
 *
 * @author schmidtm
 * @author Martin Adamek
 */
public class NewArtifactWizardIterator implements  WizardDescriptor.InstantiatingIterator<WizardDescriptor>,
                                                      WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor>{
    
    private GetArtifactNameStep pls = null;
    private boolean serverRunning = false;
    private boolean serverConfigured = true;
    private WizardDescriptor wizard;
    private SourceCategory sourceCategory;
    private String serverCommand;
    private String artifactName = "";
    private GrailsProject project;
    
    private final Logger LOG = Logger.getLogger(NewArtifactWizardIterator.class.getName());

    public static NewArtifactWizardIterator create() {
        return new NewArtifactWizardIterator();
    }

    public NewArtifactWizardIterator(GrailsProject project, SourceCategory category, String artifactName) {
        this.project = project;
        this.sourceCategory = category;
        this.artifactName = artifactName;
    }

    private NewArtifactWizardIterator() {
    }

   public Set instantiate(final ProgressHandle handle) throws IOException {
            Set<FileObject> resultSet = new HashSet<FileObject>();
            
            serverRunning = true;
            handle.start(100);
            try {
                ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
                String displayName = inf.getDisplayName() + " (" + serverCommand + ")"; // NOI18N

                Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                        serverCommand, GrailsProjectConfig.forProject(project), pls.getArtifactName());

                ExecutionDescriptor descriptor = new ExecutionDescriptor()
                        .frontWindow(true).inputVisible(true);
                descriptor = descriptor.outProcessorFactory(new InputProcessorFactory() {
                    public InputProcessor newInputProcessor() {
                        return InputProcessors.bridge(new ProgressSnooper(handle, 100, 2));
                    }
                });
                descriptor = descriptor.postExecution(new RefreshProjectRunnable(project));

                ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
                Future<Integer> future = service.run();
                try {
                    // TODO handle return value
                    future.get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex.getCause());
                }
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
        FileObject template = Templates.getTemplate(wizard);

        this.wizard = wizard;
        if (sourceCategory == null) {
            // might be already initialized by non-wizard actions like Create View
            this.sourceCategory = GrailsArtifacts.getCategoryForTemplate(template);
        }
        if (project == null) {
            // might be already initialized by non-wizard actions like Create View
            project = (GrailsProject) Templates.getProject(wizard);
        }
        this.serverCommand = sourceCategory.getCommand();

        if(!GrailsRuntime.getInstance().isConfigured()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                    NbBundle.getMessage(NewArtifactWizardIterator.class, 
                    "NewGrailsProjectWizardIterator.NoGrailsServerConfigured"));
            serverConfigured = false;
        }
        
        pls = new GetArtifactNameStep(serverRunning, serverConfigured, project, sourceCategory);
        Component c = pls.getComponent();
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent)c;
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(1));
            String title = GrailsArtifacts.getWizardTitle(sourceCategory);
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] {
                NbBundle.getMessage(NewGrailsProjectWizardIterator.class, "LBL_ChooseFileType"),
                title
            });
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
