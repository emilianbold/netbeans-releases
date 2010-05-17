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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.constants.FileWizardConstants;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.refactoring.AppletXMLRefactoringSupport;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Collection;
import org.netbeans.modules.javacard.spi.ProjectKind;

public class ClassicAppletDeploymentWizardPanel implements WizardDescriptor.Panel {

    public static final String PROP_APPLET_NAME = "appletName"; //NOI18N
    WizardDescriptor wizard;
    Collection<String> allAppletAIDs;

    public ClassicAppletDeploymentWizardPanel(WizardDescriptor wizard) {
        this.wizard = wizard;
        // Read the content of web descriptor and store all defined servlet names
        Project p = Templates.getProject(wizard);
        final JCProject jcProject = p.getLookup().lookup(JCProject.class);
        ProjectKind kind = jcProject == null ? null : jcProject.kind();
        if (kind == ProjectKind.CLASSIC_APPLET) {
            ProjectManager.mutex().readAccess(new Runnable() {
                public void run() {
                    FileObject webObj =
                            jcProject.getProjectDirectory().getFileObject(
                                    JCConstants.APPLET_DESCRIPTOR_PATH); //NOI18N
                    AppletXMLRefactoringSupport web =
                            AppletXMLRefactoringSupport.fromFile(FileUtil.toFile(webObj));
                    allAppletAIDs = web.getAllAppletAIDs();
                }
            });
        }
    }

    private ClassicAppletDeploymentVisualPanel component;

    public Component getComponent() {
        if (component == null) {
            component = new ClassicAppletDeploymentVisualPanel(this);
        }
        return component;
    }

    public void setClassName(String className) {
        component.setClassName(className);
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        String aid = component.getAppletAID();
        if (aid != null) {
            if (allAppletAIDs != null && allAppletAIDs.contains(aid)) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, //NOI18N
                        NbBundle.getMessage(ClassicAppletDeploymentWizardPanel.class,
                                "ERR_AID_EXISTS")); //NOI18N
                return false;
            }
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); //NOI18N
        return true;
    }

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public void readSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        //Do some evilness to get a fully qualified name to generate
        //a good (less likely to be duplicate) AID
        String pkg = findPackageNameFromWizard(wiz);
        String classname = pkg == null ? Templates.getTargetName(wiz) :
                pkg + '.' + Templates.getTargetName(wiz); //NOI18N
        component.setClassName(classname);
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        if (component.isAddInfoSelected()) {
            wiz.putProperty(PROP_APPLET_NAME, component.getAppletAID());
        }
        if (component.shouldCreateScript()) {
            wiz.putProperty(FileWizardConstants.CREATE_SCRIPT, "true"); //NOI18N
        } else {
            wiz.putProperty(FileWizardConstants.CREATE_SCRIPT, "false"); //NOI18N
        }
        if (component.shouldCreateInstance()) {
            wiz.putProperty(FileWizardConstants.CREATE_INSTANCE, "true"); //NOI18N
        } else {
            wiz.putProperty(FileWizardConstants.CREATE_INSTANCE, "false"); //NOI18N
        }
    }

    void setProblem(String problem) {
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, problem); //NOI18N
    }

    static String findPackageNameFromWizard(WizardDescriptor wiz) {
        FileObject target;
        //Don't ask...
        Object path = wiz.getProperty("folderToDelete"); //NOI18N
        if (path instanceof FileObject) {
            target = (FileObject) path;
        } else {
            target = Templates.getTargetFolder(wiz);
        }
        Project project = (Project) wiz.getProperty("project"); //NOI18N
        if (target != null && project != null) {
            ClassPathProvider p = project.getLookup().lookup(ClassPathProvider.class);
            ClassPath cp = p.findClassPath(target, ClassPath.SOURCE);
            FileObject root = cp.findOwnerRoot(target);
            if (root != null) {
                String relpath = FileUtil.getRelativePath(root, target).replace(
                        '/', '.'); //NOI18N
                return relpath;
            }
        }
        return null;
    }
}

