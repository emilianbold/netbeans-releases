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
package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.SourceRoots;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Just as simple wrapper for the standard new file iterator as possible.
 * @author Tomas Mysik
 */
public final class NewFileWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    private static final Logger LOGGER = Logger.getLogger(NewFileWizardIterator.class.getName());

    private static final long serialVersionUID = 2262026971167469147L;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel<WizardDescriptor>[] wizardPanels;
    private int index;

    @Override
    public Set<FileObject> instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder(wizard);
        FileObject template = Templates.getTemplate(wizard);

        Map<String, Object> wizardProps = new HashMap<String, Object>();

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        String fname = Templates.getTargetName(wizard);
        String ext = FileUtil.getExtension(fname);

        FileObject foo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), fname);
        if (foo == null || !FileUtils.isPhpFile(foo)) {
            if (!StringUtils.hasText(ext)) {
                Templates.setTargetName(wizard, fname + ".php"); // NOI18N
                fname = Templates.getTargetName(wizard);
                ext = FileUtil.getExtension(fname);
            }
        }
        if (StringUtils.hasText(ext)) {
            String name = fname.substring(0, fname.length() - ext.length() - 1);
            name = name.replaceAll("\\W", ""); // NOI18N
            wizardProps.put("name", name); // NOI18N

            // #168723
            String templateExt = FileUtil.getExtension(template.getNameExt());
            if (StringUtils.hasText(templateExt)) {
                Templates.setTargetName(wizard, name);
            }
        }
        String targetName = Templates.getTargetName(wizard);
        if (dir.getFileObject(targetName) != null) {
            throw new IOException(NbBundle.getMessage(NewFileWizardIterator.class, "TXT_FileExists", targetName));
        }
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, targetName, wizardProps);

        return Collections.singleton(createdFile.getPrimaryFile());
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        if (targetFolder == null) {
            Project project = Templates.getProject(wizard);
            assert project instanceof PhpProject;
            PhpProject phpProject = (PhpProject) project;
            FileObject srcDir = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
            if (srcDir != null) {
                targetFolder = srcDir;
                Templates.setTargetFolder(wizard, srcDir);
            }
        }
        FileObject template = Templates.getTemplate(wizard);
        String targetName = targetFolder != null ? FileUtil.findFreeFileName(targetFolder, template.getName(), "php") : template.getName(); // NOI18N
        Templates.setTargetName(wizard, targetName + ".php"); // NOI18N
        wizardPanels = getPanels();

        // Make sure list of steps is accurate.
        String[] beforeSteps = (String[]) wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        int beforeStepLength = beforeSteps.length - 1;
        String[] steps = createSteps(beforeSteps);
        for (int i = 0; i < wizardPanels.length; i++) {
            Component c = wizardPanels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i + beforeStepLength - 1)); // NOI18N
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    private String[] createSteps(String[] beforeSteps) {
        int beforeStepLength = beforeSteps.length - 1;
        String[] res = new String[beforeStepLength + wizardPanels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeStepLength)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = wizardPanels[i - beforeStepLength].getComponent().getName();
            }
        }
        return res;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        wizardPanels = null;
    }

    @Override
    public String name() {
        return ""; // NOI18N
    }

    /** Get the current panel.
     * @return the panel
     */
    @Override
    public Panel<WizardDescriptor> current() {
        return wizardPanels[index];
    }

    /** Test whether there is a next panel.
     * @return <code>true</code> if so
     */
    @Override
    public boolean hasNext() {
        return index < wizardPanels.length - 1;
    }

    /** Test whether there is a previous panel.
     * @return <code>true</code> if so
     */
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    /** Move to the next panel.
     * I.e. increment its index, need not actually change any GUI itself.
     * @exception NoSuchElementException if the panel does not exist
     */
    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    /** Move to the previous panel.
     * I.e. decrement its index, need not actually change any GUI itself.
     * @exception NoSuchElementException if the panel does not exist
     */
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

    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        Project p = Templates.getProject(wizard);
        SourceGroup[] groups = PhpProjectUtils.getSourceGroups(p);
        // #180054
        if (groups != null && groups.length == 0) {
            PhpProject project = (PhpProject) p;
            FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
            FileObject tests = ProjectPropertiesSupport.getTestDirectory(project, false);
            FileObject selenium = ProjectPropertiesSupport.getSeleniumDirectory(project, false);
            SourceRoots sourceRoots = project.getSourceRoots();
            SourceRoots testRoots = project.getTestRoots();
            SourceRoots seleniumRoots = project.getSeleniumRoots();

            StringBuilder sb = new StringBuilder(200);
            // dirs
            sb.append("project directory equals sources: "); // NOI18N
            sb.append(project.getProjectDirectory().equals(sources));
            sb.append("; sources (not null, valid): "); // NOI18N
            sb.append(sources != null);
            sb.append(", "); // NOI18N
            sb.append(sources != null && sources.isValid());
            sb.append("; tests (not null, valid): "); // NOI18N
            sb.append(tests != null);
            sb.append(", "); // NOI18N
            sb.append(tests != null && tests.isValid());
            sb.append("; selenium (not null, valid): "); // NOI18N
            sb.append(selenium != null);
            sb.append(", "); // NOI18N
            sb.append(selenium != null && selenium.isValid());
            // sources
            sb.append("; sourceRoots: "); // NOI18N
            sb.append(Arrays.asList(sourceRoots.getRoots()));
            sb.append("; testRoots: "); // NOI18N
            sb.append(Arrays.asList(testRoots.getRoots()));
            sb.append("; seleniumRoots: "); // NOI18N
            sb.append(Arrays.asList(seleniumRoots.getRoots()));
            LOGGER.log(Level.WARNING, sb.toString(),
                    new IllegalStateException("No source roots found (attach your IDE log to https://netbeans.org/bugzilla/show_bug.cgi?id=180054)"));
            groups = null;
        }
        WizardDescriptor.Panel<WizardDescriptor> simpleTargetChooserPanel = Templates.createSimpleTargetChooser(p, groups);

        @SuppressWarnings("unchecked") // Generic Array Creation
        WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                    simpleTargetChooserPanel
                };
        return panels;
    }
}
