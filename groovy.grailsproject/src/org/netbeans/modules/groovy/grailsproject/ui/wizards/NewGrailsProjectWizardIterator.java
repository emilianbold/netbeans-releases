/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.api.extexecution.input.InputProcessor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import org.openide.util.NbBundle;
import java.io.File;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grailsproject.GrailsProjectSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author schmidtm
 */
public class NewGrailsProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private static final Logger LOGGER = Logger.getLogger(NewGrailsProjectWizardIterator.class.getName());
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private PanelConfigureProject pls;
    private int baseCount;

    public NewGrailsProjectWizardIterator() {
        super();
    }

    public static NewGrailsProjectWizardIterator create() {
        return new NewGrailsProjectWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        pls = new PanelConfigureProject();
        return new WizardDescriptor.Panel[]{pls};
    }

    private String[] createSteps() {
        return new String[] {NbBundle.getMessage(NewGrailsProjectWizardIterator.class, "LAB_ConfigureProject")};
    }

    public Set instantiate(final ProgressHandle handle) throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();

        handle.start(100);
        try {
            String displayName = "<new project> (create-app)"; // NOI18N
            Callable<Process> callable = ExecutionSupport.getInstance().createCreateApp(
                    (File) wiz.getProperty("projectFolder")); // NOI18N

            // we need a special descriptor here
            ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true).inputVisible(true);
            descriptor = descriptor.outProcessorFactory(new InputProcessorFactory() {
                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(new ProgressLineProcessor(handle, 100, 2)));
                }
            });
            // TODO refresh

            ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
            Future<Integer> future = service.run();
            try {
                Integer ret = future.get();
                if (ret.intValue() != 0) {
                    String msg = NbBundle.getMessage(NewGrailsProjectWizardIterator.class, "WIZARD_ERROR_MESSAGE_APPLICATION");
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex.getCause());
            }
        } finally {
            handle.progress(100);
            handle.finish();
        }

        File dirF = (File) wiz.getProperty("projectFolder");

        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
            FileObject dir = FileUtil.toFileObject(dirF);

            if (dir == null) {
                LOGGER.warning("Folder was expected, but not found: " + dirF.getCanonicalPath());
            } else {
                resultSet.add(dir);
                GrailsProjectSettings.getDefault().setNewProjectCount(baseCount);

                File parentDir = dirF.getParentFile();

                if (parentDir != null && parentDir.exists() && parentDir.isDirectory()) {
                    GrailsProjectSettings.getDefault().setLastUsedArtifactFolder(dirF.getParentFile());
                }
            }
            dirF = dirF.getParentFile();
            if (dirF != null && dirF.exists()) {
                ProjectChooser.setProjectsFolder(dirF);
            }
        }

        return resultSet;
    }

    public Set instantiate() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        return resultSet;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        index = 0;

        // get project counter from GrailsConfiguration
        baseCount = GrailsProjectSettings.getDefault().getNewProjectCount() + 1;
        wizard.putProperty("WizardPanel_GrailsProjectCounter", Integer.valueOf(baseCount));

        panels = createPanels();
        String[] steps = createSteps();

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    public void uninitialize(WizardDescriptor wizard) {
    }

    public Panel current() {
        return panels[index];
    }

    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewGrailsProjectWizardIterator.class, "LAB_IteratorName"),
                new Object[]{Integer.valueOf(index + 1), Integer.valueOf(panels.length)});
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
}
