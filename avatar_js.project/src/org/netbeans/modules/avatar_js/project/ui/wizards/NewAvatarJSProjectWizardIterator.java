/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.avatar_js.project.AvatarJSProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard that creates a new Avatar.js project.
 * 
 * @author Martin
 */
public class NewAvatarJSProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    enum WizardType { MAIN }
    
    private final WizardType type;
    private transient WizardDescriptor wiz;
    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private transient int index;
    
    private NewAvatarJSProjectWizardIterator(WizardType type) {
        this.type = type;
    }
    
    @TemplateRegistration(folder="Project/Avatar_js", position=100, displayName="#template_main", iconBase="org/netbeans/modules/avatar_js/project/ui/resources/avatarJSProject.png", description="../resources/emptyProject.html")
    @NbBundle.Messages({"template_main=Avatar.js Server"})
    public static NewAvatarJSProjectWizardIterator wmain() {
        return new NewAvatarJSProjectWizardIterator(WizardType.MAIN);
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        switch (type) {
            default:
                return new WizardDescriptor.Panel[] {
                    new PanelConfigureProject(type),
                    //new PanelConfigureJDKAndLibs()
                };
        }
    }
    
    @NbBundle.Messages({"LAB_ConfigureProject=Name and Location",
                        "LAB_ConfigureLibs=Libraries"})
    private String[] createSteps() {
        switch (type) {
            default:
                return new String[] {
                    Bundle.LAB_ConfigureProject(),
                    //Bundle.LAB_ConfigureLibs()
                };
        }
    }
    
    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
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
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        // For external sources
        //this.wiz.putProperty(WizardSettings.PROP_SOURCE_ROOT, new File[0]);
        //this.wiz.putProperty("testRoot", new File[0]);      //NOI18N
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardSettings.PROP_PRJ_DIR, null);
            this.wiz.putProperty(WizardSettings.PROP_NAME, null);
            this.wiz.putProperty(WizardSettings.PROP_MAIN_FILE, null);
            /*
            switch (type) {
            case EXT:
                this.wiz.putProperty("sourceRoot",null);    //NOI18N
                this.wiz.putProperty("testRoot",null);      //NOI18N
            }
            */
            this.wiz = null;
            panels = null;
        }
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @NbBundle.Messages({"MSG_InstantiateNewProject=Creating new project",
                        "MSG_InstantiatePreparingToOpen=Preparing new project to be opened"})
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start (4);
        Set<FileObject> resultSet = new HashSet<>();
        File dirF = (File) wiz.getProperty(WizardSettings.PROP_PRJ_DIR);
        if (dirF == null) {
            throw new NullPointerException ("projdir == null, props:" + wiz.getProperties());
        }
        dirF = FileUtil.normalizeFile(dirF);
        String name = (String) wiz.getProperty(WizardSettings.PROP_NAME);
        String mainFile = (String) wiz.getProperty(WizardSettings.PROP_MAIN_FILE);
        String port = (String) wiz.getProperty(WizardSettings.PROP_SERVER_FILE_PORT);
        JavaPlatform platform = (JavaPlatform) wiz.getProperty(WizardSettings.PROP_JAVA_PLATFORM);
        String avatarLibsFolder = (String) wiz.getProperty(WizardSettings.PROP_AVATAR_LIBS);
        File avatar_jsJAR = (File) wiz.getProperty(WizardSettings.PROP_AVATAR_JAR);
        handle.progress (Bundle.MSG_InstantiateNewProject(), 1);
        AntProjectHelper ah;
        switch (type) {
            case MAIN:
                ah = AvatarJSProjectGenerator.createProject(dirF,
                                                            name,
                                                            mainFile,
                                                            port,
                                                            platform,
                                                            avatarLibsFolder,
                                                            avatar_jsJAR);
                handle.progress(2);
                if (mainFile != null && mainFile.length () > 0) {
                    final FileObject sourcesRoot = ah.getProjectDirectory ().getFileObject ("src");        //NOI18N
                    if (sourcesRoot != null) {
                        final FileObject mainClassFo = sourcesRoot.getFileObject(mainFile);
                        if (mainClassFo != null) {
                            resultSet.add (mainClassFo);
                        }
                    }
                }
                break;
            default:
                throw new IllegalStateException(type.name());
        }
        FileObject dir = FileUtil.toFileObject(dirF);
        handle.progress (3);

        // Returning FileObject of project diretory. 
        // Project will be open and set as main
        final Integer ind = (Integer) wiz.getProperty(WizardSettings.PROP_NAME_INDEX);
        if (ind != null) {
            switch (type) {
                case MAIN:
                    WizardSettings.setNewApplicationCount(ind);
                    break;
            }
        }
        resultSet.add (dir);
        handle.progress (Bundle.MSG_InstantiatePreparingToOpen(), 4);
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);    
        }
         
        return resultSet;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @NbBundle.Messages({"# {0} - step number",
                        "# {1} - number of steps",
                        "LAB_IteratorName={0} of {1}"})
    @Override
    public String name() {
        return Bundle.LAB_IteratorName(index + 1, panels.length);
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException("index = "+index+", panels.length = "+panels.length);
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException("index = "+index+", panels.length = "+panels.length);
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    
}
