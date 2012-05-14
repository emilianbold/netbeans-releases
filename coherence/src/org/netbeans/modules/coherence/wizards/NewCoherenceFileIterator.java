/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.spi.project.ui.templates.support.Templates.SimpleTargetChooserBuilder;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;

/**
 * Iterator for creation of all Coherence related templates.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class NewCoherenceFileIterator implements WizardDescriptor.InstantiatingIterator {

    private static final Logger LOGGER = Logger.getLogger(NewCoherenceFileIterator.class.getName());

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;

    BottomWizardDescriptorPanel bottomPanel;

    @Override
    public Set instantiate() throws IOException {
        final FileObject targetFolder = Templates.getTargetFolder(wizard);
        final String targetName = Templates.getTargetName(wizard);

        // add Coherence library to the project classpath if choosen
        Library selectedLibrary = bottomPanel.getSelectedLibrary();
        if (selectedLibrary != null) {
            Project project = Templates.getProject(wizard);
            try {
                SourceGroup[] group = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (group.length > 0) {
                    ProjectClassPathModifier.addLibraries(
                            new Library[]{selectedLibrary},
                            group[0].getRootFolder(),
                            ClassPath.COMPILE);
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Libraries required for the Coherence project not added", ioe);
            } catch (UnsupportedOperationException uoe) {
                LOGGER.log(Level.WARNING, "This project does not support adding these types of libraries to the classpath", uoe);
            }
        }

        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        FileObject templateFO = Templates.getTemplate(wizard);
        DataObject templateDO = DataObject.find(templateFO);
        FileObject createdFile = templateDO.createFromTemplate(dataFolder, targetName).getPrimaryFile();

        return Collections.singleton(createdFile);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public Panel current() {
        return getPanels()[index];
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().length; //NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private Panel[] getPanels() {
        if (panels == null) {
            Project project = Templates.getProject(wizard);
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] sourceGroups;

            if (Templates.getTemplate(wizard).getPath().endsWith(".xml")) { //NOI18N
                sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
                if (sourceGroups.length == 0) {
                    sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                }
            } else {
                sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            }
            SimpleTargetChooserBuilder simpleTargetChooser = Templates.buildSimpleTargetChooser(project, sourceGroups);
            WizardDescriptor.Panel bottom = new BottomWizardDescriptorPanel();
            bottomPanel = (BottomWizardDescriptorPanel) bottom;
            simpleTargetChooser.bottomPanel(bottom);
            WizardDescriptor.Panel generalPanel = simpleTargetChooser.create();
            panels = new Panel[]{generalPanel};

            for (int i = 0; i < panels.length; i++) {
                JComponent jc = (JComponent) panels[i].getComponent();
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
            }
        }
        return panels;
    }

    private class BottomWizardDescriptorPanel implements WizardDescriptor.Panel {

        private BottomWizardPanel panel;

        @Override
        public Component getComponent() {
            if (panel == null) {
                panel = new BottomWizardPanel(wizard);
            }
            return panel;
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public void readSettings(Object settings) {
        }

        @Override
        public void storeSettings(Object settings) {
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public boolean isValid() {
            return getComponent().isValid();
        }

        public Library getSelectedLibrary() {
            return panel.getSelectedLibrary();
        }
    }
}
