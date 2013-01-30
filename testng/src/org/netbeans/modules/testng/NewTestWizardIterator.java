/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.testng;

import java.awt.Component;
import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.ui.TestNGPlugin;
import org.netbeans.modules.testng.ui.TestNGPluginTrampoline;
import org.netbeans.modules.testng.ui.TestNGSettings;
import org.netbeans.modules.testng.ui.TestUtil;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new TestNG file.
 */
@TemplateRegistration(folder = "UnitTests", position = 1000,
        content = "resources/EmptyTestNGTest.java.template",
        scriptEngine = "freemarker",
        displayName = "#EmptyTestNGTest_displayName",
        iconBase = "org/netbeans/modules/testng/resources/testng.gif",
        description = "/org/netbeans/modules/testng/resources/newTest.html",
        category="junit")
@NbBundle.Messages("EmptyTestNGTest_displayName=TestNG Test Case")
public final class NewTestWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    public NewTestWizardIterator() {
    }

    private WizardDescriptor.Panel[] createPanels(final WizardDescriptor wizardDescriptor) {
        // Ask for Java folders
        Project project = Templates.getProject(wizardDescriptor);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = getTestRoots(sources);
        if (groups.length == 0) {
            if (SourceGroupModifier.createSourceGroup(project, JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_HINT_TEST) != null) {
                groups = getTestRoots(sources);
            }
        }
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            return new WizardDescriptor.Panel[]{
                        Templates.buildSimpleTargetChooser(project, groups).create()
                    };
        } else {
            return new WizardDescriptor.Panel[]{
                        JavaTemplates.createPackageChooser(project, groups, new EmptyTestStepLocation())
                    };
        }
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }

    public Set<FileObject> instantiate() throws IOException {
        saveSettings(wiz);        
        
        /* collect and build necessary data: */
        String name = Templates.getTargetName(wiz);
        FileObject targetFolder = Templates.getTargetFolder(wiz);
        TestNGSupport.findTestNGSupport(FileOwnerQuery.getOwner(targetFolder)).configureProject(targetFolder);
        
        Map<CommonPlugin.CreateTestParam, Object> params
                = TestUtil.getSettingsMap(false);
        params.put(CommonPlugin.CreateTestParam.CLASS_NAME,
                   Templates.getTargetName(wiz));
                
        /* create the test class: */
        final TestNGPlugin plugin = TestUtil.getPluginForProject(
                                                Templates.getProject(wiz));
        
        if (!TestNGPluginTrampoline.DEFAULT.createTestActionCalled(
                                            plugin,
                                            new FileObject[] {targetFolder})) {
            return null;
        }

        /*
         * The TestNGPlugin instance must be initialized _before_ field
         * TestNGPluginTrampoline.DEFAULT gets accessed.
         * See issue #74744.
         */
        final FileObject[] testFileObjects
                = TestNGPluginTrampoline.DEFAULT.createTests(
                     plugin,
                     null,
                     targetFolder,
                     params);
        
        if (testFileObjects == null) {
            throw new IOException();
        }
        
        FileObject createdFile = testFileObjects[0];
        return Collections.singleton(createdFile);
    }

    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels(wiz);
        loadSettings(wiz);
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }   
    
    private void loadSettings(WizardDescriptor wizard) {
        TestNGSettings settings = TestNGSettings.getDefault();
        
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(settings.isGenerateSetUp()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(settings.isGenerateTearDown()));
        wizard.putProperty(GuiUtils.CHK_BEFORE_CLASS,
                           Boolean.valueOf(settings.isGenerateClassSetUp()));
        wizard.putProperty(GuiUtils.CHK_AFTER_CLASS,
                           Boolean.valueOf(settings.isGenerateClassTearDown()));
        wizard.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(settings.isBodyComments()));
    }

    private void saveSettings(WizardDescriptor wizard) {
        TestNGSettings settings = TestNGSettings.getDefault();
        
        settings.setGenerateSetUp(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_SETUP)));
        settings.setGenerateTearDown(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_TEARDOWN)));
        settings.setGenerateClassSetUp(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_BEFORE_CLASS)));
        settings.setGenerateClassTearDown(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_AFTER_CLASS)));
        settings.setBodyComments(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_HINTS)));
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return ""; // NOI18N
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

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

    private static String getSelectedPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups[i].getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replaceAll("/", "."); // NOI18N
        }
        return packageName;
    }
    
    private SourceGroup[] getTestRoots(Sources srcs) {
        SourceGroup[] groups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + srcs;

        //XXX - have to filter out regular source roots, there should
        //be better way to do this... (Hint: use UnitTestForSourceQuery)
        //${test - Ant based projects
        //2TestSourceRoot - Maven projects
        List<SourceGroup> result = new ArrayList<SourceGroup>(2);
        for (SourceGroup sg : groups) {
            if (sg.getName().startsWith("${test") || "2TestSourceRoot".equals(sg.getName())) { //NOI18N
                result.add(sg);
            }            
        }
        return result.toArray(new SourceGroup[result.size()]);
    }
}
