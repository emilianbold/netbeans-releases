/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javacard.templates;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.javacard.constants.FileWizardConstants;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.refactoring.WebXMLRefactoringSupport;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.javacard.spi.ProjectKind;

public final class ServletWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel<org.openide.WizardDescriptor>[] panels;
    private boolean needsToCancel;

    private WizardDescriptor.Panel createJavaSourcePanel(
            WizardDescriptor wizardDescriptor) {

        // Ask for Java folders
        Project project = Templates.getProject(wizardDescriptor);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources;
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            return Templates.createSimpleTargetChooser(project, groups);
        } else {
            return JavaTemplates.createPackageChooser(project, groups);
        }

    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<org.openide.WizardDescriptor>[] getPanels() {
        if (needsToCancel) {
            return new WizardDescriptor.Panel[]{
                        new ErrorPanel("Web Applications")
                    };
        }
        if (panels == null) {
            WizardDescriptor.Panel javaSourcePanel = createJavaSourcePanel(wizard);
            ServletDeploymentWizardPanel servletPanel = new ServletDeploymentWizardPanel(wizard);
            panels = new WizardDescriptor.Panel[]{
                        javaSourcePanel, servletPanel
                    };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }


    public Set instantiate() throws IOException {
        FileObject fo = Templates.getTemplate(wizard);
        FileObject dir = Templates.getTargetFolder(wizard);
        String targetName = Templates.getTargetName(wizard);

        DataFolder df = DataFolder.findFolder(dir);
        FileObject template = Templates.getTemplate(wizard);

        FileObject createdFile = null;

        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, targetName);
        createdFile = dobj.getPrimaryFile();

        addInfoToWebDescriptor(createdFile);

        return Collections.singleton(createdFile);
    }

    private void addInfoToWebDescriptor(final FileObject createdFile) {
        Project p = Templates.getProject(wizard);
        final JCProject jcProject = p.getLookup().lookup(JCProject.class);
        if (jcProject == null || jcProject.kind() != ProjectKind.WEB) {
            return;
        }
        final String servletName = (String) wizard.getProperty(FileWizardConstants.PROP_SERVLET_NAME);

        if (servletName != null) {
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                    try {
                        String urlPattern = (String) wizard.getProperty(FileWizardConstants.PROP_URL_PATTERN);
                        ClassPathProvider cpProvider = jcProject.getLookup().lookup(ClassPathProvider.class);
                        String className = createdFile.getName();
                        if (cpProvider != null) {
                            className = cpProvider.findClassPath(
                                    createdFile, ClassPath.SOURCE).getResourceName(createdFile, '.', false);
                        }

                        FileObject webObj = jcProject.getProjectDirectory().getFileObject(JCConstants.WEB_DESCRIPTOR_PATH);
                        WebXMLRefactoringSupport web = WebXMLRefactoringSupport.fromFile(FileUtil.toFile(webObj));
                        web.addServletInfo(servletName, className, urlPattern);
                        OutputStream out = webObj.getOutputStream();
                        try {
                            XMLUtil.write(web.getDocument(), out, "UTF-8");
                        } finally {
                            out.close();
                        }
                    } catch (FileAlreadyLockedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }


    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        Project p = Templates.getProject(wizard);
        if (p.getLookup().lookup(ProjectKind.class) != ProjectKind.WEB) {
            needsToCancel = true;
        }
    }


    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }


    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }


    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }


    public boolean hasNext() {
        return index < getPanels().length - 1;
    }


    public boolean hasPrevious() {
        return index > 0;
    }


    public void nextPanel() {
        if (index == 0) {
            // Settings from 1st pannel (class name) are needed in 2nd panel
            panels[0].storeSettings(wizard);
        }
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

    // If nothing unusual changes in the middle of the wizard, simply:

    public void addChangeListener(ChangeListener l) {
    }

    
    public void removeChangeListener(ChangeListener l) {
    }

    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }
}
