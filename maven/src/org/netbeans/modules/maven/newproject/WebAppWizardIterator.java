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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 *@author Dafe Simonek
 */
public class WebAppWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private ArchetypeProviderImpl ngprovider;
    private WizardDescriptor wiz;
    
    private WebAppWizardIterator() {}
    
    public static WebAppWizardIterator createIterator() {
        return new WebAppWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new BasicWizardPanel(true)
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(WebAppWizardIterator.class, "LBL_CreateProjectStep2"),
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

            Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
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
                    NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
                    if (nbprj != null) { //#147006 how can this happen?
                        // maybe when the archetype contains netbeans specific project files?
                        prj.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
                    }
                }
            }
            return resultSet;
        } finally {
            handle.finish();
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
    
    public void initialize(WizardDescriptor wiz) {
        index = 0;
        panels = createPanels();
        this.wiz = wiz;
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
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
        ngprovider = new ArchetypeProviderImpl();
        wiz.putProperty(MavenWizardIterator.PROPERTY_CUSTOM_CREATOR, ngprovider);
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(WebAppWizardIterator.class, "NameFormat"),
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    public boolean hasNext() {
        return false;
    }
    
    public boolean hasPrevious() {
        return false;
    }
    
    public void nextPanel() {
    }
    
    public void previousPanel() {
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
