/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import org.netbeans.modules.maven.api.archetype.Archetype;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.options.MavenCommandSettings;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 *@author mkleint
 */
public class MavenWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    private static final String USER_DIR_PROP = "user.dir"; //NOI18N
    static final String PROPERTY_CUSTOM_CREATOR = "customCreator"; //NOI18N
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private final List<ChangeListener> listeners;
    private ArchetypeProviderImpl ngprovider;
    
    public MavenWizardIterator() {
        listeners = new ArrayList<ChangeListener>();
    }
    
    public static MavenWizardIterator createIterator() {
        return new MavenWizardIterator();
    }

    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ChooseWizardPanel(),
            new BasicWizardPanel()
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(MavenWizardIterator.class, "LBL_CreateProjectStep"),
            NbBundle.getMessage(MavenWizardIterator.class, "LBL_CreateProjectStep2")
        };
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator."; //NOI18N
        return null;
    }
    
    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start(4);
            handle.progress(1);
            final File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); //NOI18N
            final File parent = dirF.getParentFile();
            if (parent != null && parent.exists()) {
                ProjectChooser.setProjectsFolder(parent);
            }
            
            Set resultSet = new LinkedHashSet();
//            final Archetype archetype = (Archetype)wiz.getProperty("archetype"); //NOI18N<
            dirF.getParentFile().mkdirs();
            
            handle.progress(NbBundle.getMessage(MavenWizardIterator.class, "PRG_Processing_Archetype"), 2);
            ngprovider.runArchetype(dirF.getParentFile(), wiz);
//            } else {
//                final String art = (String)wiz.getProperty("artifactId"); //NOI18N
//                final String ver = (String)wiz.getProperty("version"); //NOI18N
//                final String gr = (String)wiz.getProperty("groupId"); //NOI18N
//                final String pack = (String)wiz.getProperty("package"); //NOI18N
//                runArchetype(dirF.getParentFile(), gr, art, ver, pack, archetype);
//            }
            handle.progress(3);
            // Always open top dir as a project:
            FileObject fDir = FileUtil.toFileObject(dirF);
            if (fDir != null) {
                // the archetype generation didn't fail.
                resultSet.add(fDir);
                addJavaRootFolders(fDir);
                // Look for nested projects to open as well:
                Enumeration e = fDir.getFolders(true);
                while (e.hasMoreElements()) {
                    FileObject subfolder = (FileObject) e.nextElement();
                    if (ProjectManager.getDefault().isProject(subfolder)) {
                        resultSet.add(subfolder);
                        addJavaRootFolders(subfolder);
                    }
                }
                Project prj = ProjectManager.getDefault().findProject(fDir);
                if (prj != null) {
                    prj.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
                }
            }
            return resultSet;
        } finally {
            handle.finish();
        }
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        ngprovider = new ArchetypeProviderImpl();
        wiz.putProperty(PROPERTY_CUSTOM_CREATOR, ngprovider);
        index = 0;
        panels = createPanels();
        updateSteps();
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir",null); //NOI18N
        this.wiz.putProperty("name",null); //NOI18N
        this.wiz = null;
        panels = null;
        listeners.clear();
    }
    
    public String name() {
        return MessageFormat.format(org.openide.util.NbBundle.getMessage(MavenWizardIterator.class, "NameFormat"),
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
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
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireChange() {
        synchronized (listeners) {
            for (ChangeListener list : listeners) {
                list.stateChanged(new ChangeEvent(this));
            }
        }
    }

    private int runArchetype(File dirF, String gr, String art, String ver, String pack, Archetype arch) {
        BeanRunConfig config = new BeanRunConfig();
        config.setActivatedProfiles(Collections.EMPTY_LIST);
        config.setExecutionDirectory(dirF);
        config.setExecutionName(NbBundle.getMessage(MavenWizardIterator.class, "RUN_Project_Creation"));
        config.setGoals(Collections.singletonList(MavenCommandSettings.getDefault().getCommand(MavenCommandSettings.COMMAND_CREATE_ARCHETYPE))); //NOI18N
        Properties props = new Properties();
        props.setProperty("archetypeArtifactId", arch.getArtifactId()); //NOI18N
        props.setProperty("archetypeGroupId", arch.getGroupId()); //NOI18N
        props.setProperty("archetypeVersion", arch.getVersion()); //NOI18N
        if (arch.getRepository() != null) {
            props.setProperty("remoteRepositories", arch.getRepository()); //NOI18N
        }
        props.setProperty("artifactId", art); //NOI18N
        props.setProperty("groupId", gr); //NOI18N
        props.setProperty("version", ver); //NOI18N
        if (pack != null && pack.trim().length() > 0) {
            props.setProperty("packageName", pack); //NOI18N
        }
        config.setProperties(props);
        config.setTaskDisplayName(NbBundle.getMessage(MavenWizardIterator.class, "RUN_Project_Creation"));
        // setup executor now..
        //hack - we need to setup the user.dir sys property..
        String oldUserdir = System.getProperty(USER_DIR_PROP); //NOI18N
        System.setProperty(USER_DIR_PROP, dirF.getAbsolutePath()); //NOI18N
        try {
            ExecutorTask task = RunUtils.executeMaven(config); //NOI18N
            return task.result();
        } finally {
            if (oldUserdir == null) {
                System.getProperties().remove(USER_DIR_PROP); //NOI18N
            } else {
                System.setProperty(USER_DIR_PROP, oldUserdir); //NOI18N
            }
        }
        
    }
    
    private void addJavaRootFolders(FileObject fo) {
        try {
            Project prj = ProjectManager.getDefault().findProject(fo);
            if (prj == null) { //#143596
                return;
            }
            NbMavenProject watch = prj.getLookup().lookup(NbMavenProject.class);
            if (watch != null) {
                // do not create java/test for pom type projects.. most probably not relevant.
                if (! NbMavenProject.TYPE_POM.equals(watch.getPackagingType())) {
                    URI mainJava = FileUtilities.convertStringToUri(watch.getMavenProject().getBuild().getSourceDirectory());
                    URI testJava = FileUtilities.convertStringToUri(watch.getMavenProject().getBuild().getTestSourceDirectory());
                    File file = new File(mainJava);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    file = new File(testJava);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateSteps() {
        // Make sure list of steps is accurate.
        String[] steps = new String[panels.length];
        String[] basicOnes = createSteps();
        System.arraycopy(basicOnes, 0, steps, 0, basicOnes.length);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (i >= basicOnes.length || steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) {
                // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
}
