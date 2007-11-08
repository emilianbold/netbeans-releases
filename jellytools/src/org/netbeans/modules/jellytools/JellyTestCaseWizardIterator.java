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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
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
package org.netbeans.modules.jellytools;

import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Wizard to create a new JellyTestCase based test. */
public class JellyTestCaseWizardIterator implements TemplateWizard.Iterator {

    /** Singleton instance of JavaWizardIterator, should it be ever needed. */
    private static JellyTestCaseWizardIterator instance;
    /** Wizard instance. */
    private TemplateWizard wizard;
    /** index of step Name and Location */
    private static final int INDEX_TARGET = 2;
    /** name of panel Name and Location */
    private final String NAME_AND_LOCATION = NbBundle.getMessage(JellyTestCaseWizardIterator.class,
            "LBL_panel_Target");  //NOI18N
    /** index of the current panel */
    private int current;
    /** panel for choosing name and target location of the test class */
    private WizardDescriptor.Panel targetPanel;
    private Project lastSelectedProject = null;

    /** Returns JavaWizardIterator singleton instance. This method is used
     * for constructing the instance from filesystem.attributes.
     */
    public static synchronized JellyTestCaseWizardIterator singleton() {
        if (instance == null) {
            instance = new JellyTestCaseWizardIterator();
        }
        return instance;
    }

    /** Not needed.  */
    public void addChangeListener(ChangeListener l) {
    }

    /** Not needed. */
    public void removeChangeListener(ChangeListener l) {
    }

    /** Returns true if previous panel exists. */
    public boolean hasPrevious() {
        return current > INDEX_TARGET;
    }

    /** Returns true if next panel exists. */
    public boolean hasNext() {
        return current < INDEX_TARGET;
    }

    /** Returns previous panel index.  */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        current--;
    }

    /** Returns next panel index. */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        current++;
    }

    /** Returns current panel. */
    public WizardDescriptor.Panel current() {
        switch (current) {
            case INDEX_TARGET:
                return getTargetPanel();
            default:
                throw new IllegalStateException();
        }
    }

    /** Returns Name and Location panel for selected project. */
    private WizardDescriptor.Panel getTargetPanel() {
        final Project project = Templates.getProject(wizard);
        if (targetPanel == null || project != lastSelectedProject) {
            SourceGroup[] javaSourceGroups =
                    ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            targetPanel = JavaTemplates.createPackageChooser(project, javaSourceGroups);
            lastSelectedProject = project;
        }
        return targetPanel;
    }

    /** Returns name of panel.  */
    public String name() {
        switch (current) {
            case INDEX_TARGET:
                return NAME_AND_LOCATION;
            default:
                throw new AssertionError(current);
        }
    }

    /** Initialize wizard. */
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        current = INDEX_TARGET;

        String[] panelNames = new String[]{
            NbBundle.getMessage(JellyTestCaseWizardIterator.class, "LBL_panel_chooseFileType"),
            NAME_AND_LOCATION
        };

        ((javax.swing.JComponent) getTargetPanel().getComponent()).putClientProperty("WizardPanel_contentData", panelNames);
        ((javax.swing.JComponent) getTargetPanel().getComponent()).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0));
    }

    /** Uninitialize wizard. */
    public void uninitialize(TemplateWizard wiz) {
        this.wizard = null;
        targetPanel = null;
        lastSelectedProject = null;
    }

    /** Create a new file from template. */
    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        DataObject createdDO = wizard.getTemplate().createFromTemplate(wizard.getTargetFolder(), wizard.getTargetName());
        setPackageName(createdDO);
        return Collections.singleton(createdDO);
    }

    /** For Functional Test Packages it is not correctly initialized classpath and that's why
     * package is not set by createFromTemplate method. We need to get package
     * from wizard panel and set it after DataObject from template is created.
     */
    private void setPackageName(DataObject createdDO) {
        final String packageNameFromPanel;
        try {
            // find NetBeans SystemClassLoader in threads hierarchy
            ThreadGroup tg = Thread.currentThread().getThreadGroup();
            ClassLoader systemClassloader = Thread.currentThread().getContextClassLoader();
            while (!systemClassloader.getClass().getName().endsWith("SystemClassLoader")) { // NOI18N
                tg = tg.getParent();
                if (tg == null) {
                    Logger.getAnonymousLogger().log(Level.WARNING, "NetBeans SystemClassLoader not found!"); // NOI18N
                    // log and ignore
                    return;
                }
                Thread[] list = new Thread[tg.activeCount()];
                tg.enumerate(list);
                systemClassloader = list[0].getContextClassLoader();
            }

            // get package name from panel
            Class clazz = Class.forName("org.netbeans.modules.java.project.JavaTargetChooserPanelGUI", false, systemClassloader); //NOI18N
            Method method = clazz.getDeclaredMethod("getPackageName", (Class[]) null); //NOI18N
            method.setAccessible(true);
            packageNameFromPanel = method.invoke(targetPanel.getComponent(), (Object[]) null).toString();
        } catch (Exception e) {
            // log and ignore
            Logger.getAnonymousLogger().log(Level.WARNING, "Problem when using reflection to correct package name.", e); // NOI18N
            return;
        }
        // set package name
        final JavaSource js = JavaSource.forFileObject(createdDO.getPrimaryFile());
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    js.runModificationTask(new CancellableTask<WorkingCopy>() {
                        public void run(WorkingCopy workingCopy) throws IOException {
                            workingCopy.toPhase(Phase.RESOLVED);
                            CompilationUnitTree cut = workingCopy.getCompilationUnit();
                            TreeMaker make = workingCopy.getTreeMaker();
                            CompilationUnitTree copy = make.CompilationUnit(
                                    make.Identifier(packageNameFromPanel),
                                    cut.getImports(),
                                    cut.getTypeDecls(),
                                    cut.getSourceFile());
                            workingCopy.rewrite(cut, copy);
                        }
                        public void cancel() {
                        }
                    }).commit();
                } catch (IOException e) {
                    Logger.getLogger("").log(Level.SEVERE, e.getMessage(), e);
                }
            }
        });
    }
}
