/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.simpleunit.spi.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item.ItemFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 */
public abstract class AbstractUnitTestIterator implements TemplateWizard.Iterator {
    private final ChangeSupport cs;
    private TemplateWizard wizard;
    private Panel<WizardDescriptor>[] panels;
    private int index;
    
    public static final String CND_UNITTEST_DEFAULT_NAME = "UnitTestDefaultName"; // NOI18N
    public static final String CND_UNITTEST_FUNCTIONS = "UnitTestFunctions"; // NOI18N
    public static final String CND_UNITTEST_LOOKUP = "UnitTestContextLookup"; // NOI18N
    public static final String CND_UNITTEST_GENERATION = "UnitTestCodeGeneration"; // NOI18N
    public static final String CND_UNITTEST_KIND = "UnitTestKind"; // NOI18N
    public static final String CND_UNITTEST_KIND_CPPUNIT = "UnitTestCppUnit"; // NOI18N

    public AbstractUnitTestIterator() {
        index = 0;
        cs = new ChangeSupport(this);
    }

    @Override
    public abstract Set<DataObject> instantiate(TemplateWizard wiz) throws IOException;
    
    protected abstract WizardDescriptor.Panel<WizardDescriptor>[] createPanels();

    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            WizardDescriptor.Panel<WizardDescriptor>[] otherPanels = createPanels();
            if (isTestGenerationMode()) {
                Lookup lookup = (Lookup) wizard.getProperty(CND_UNITTEST_LOOKUP); // NOI18N
                assert lookup != null : "lookup must be found in wizard.getProperty(\"" + CND_UNITTEST_LOOKUP + "\")";
                @SuppressWarnings("unchecked")
                WizardDescriptor.Panel<WizardDescriptor>[] aPanels = new WizardDescriptor.Panel[otherPanels.length + 1];
                panels = aPanels;
                panels[0] = UnitTestTemplates.createFunctionsPanel(lookup, (String) wizard.getProperty(CND_UNITTEST_KIND));
                System.arraycopy(otherPanels, 0, panels, 1, otherPanels.length);
                String[] steps = new String[panels.length];
                for (int i = 0; i < panels.length; i++) {
                    Component c = panels[i].getComponent();
                    steps[i] = c.getName();
                    setupComponent(steps, i, c);
                }
            } else {
                panels = otherPanels;
            }
        }
        return panels;
    }

    private void setupComponent(final String[] steps, final int i, final Component c) {
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent) c;
            // Sets step number of a component
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
            // Sets steps names for a panel
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            // Turn on subtitle creation on each step
            jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
            // Show steps on the left side with the image on the background
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
            // Turn on numbering of all steps
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        }
    }

    /**
     * can be overriden, but must call this method as well
     * @param wiz
     */
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
    }

    public void uninitialize(TemplateWizard wiz) {
        
    }

    public final Panel<WizardDescriptor> current() {
        return getPanels()[index];
    }

    public final String name() {
        return ""; // NOI18N
    }

    public final boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public final boolean hasPrevious() {
        return index > 0;
    }

    public final void nextPanel() {
        index++;
    }

    public final void previousPanel() {
        index--;
    }

    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        cs.fireChange();
    }

    protected final boolean isTestGenerationMode() {
        Boolean generate = wizard == null ? null : (Boolean)wizard.getProperty(CND_UNITTEST_GENERATION);
        return generate == null ? false : generate.booleanValue();
    }

    protected final TemplateWizard getWizard() {
        return wizard;
    }

    protected final MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project p) {
        ConfigurationDescriptorProvider pdp = p.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp == null) {
            return null;
        }
        return pdp.getConfigurationDescriptor();
    }

    protected final Folder getTestsRootFolder(Project project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();

        Folder root = projectDescriptor.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if (folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }
        return testRootFolder;
    }

    protected final Folder createTestsRootFolder(Project project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();
        Folder root = projectDescriptor.getLogicalFolders();
        Folder newFolder = root.addNewFolder(MakeConfigurationDescriptor.TEST_FILES_FOLDER,
                NbBundle.getMessage(MakeConfigurationDescriptor.class, "TestsFilesTxt"), false, Folder.Kind.TEST_LOGICAL_FOLDER); // NOI18N
        return newFolder;
    }

    protected final boolean addItemToLogicalFolder(Project project, Folder folder, DataObject dataObject) {
        FileObject file = dataObject.getPrimaryFile();
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor(project);
        if (file.isFolder()) {
            return false;
        } // don't add directories.
        if (!makeConfigurationDescriptor.okToChange()) {
            return false;
        }
        String itemPath = ProjectSupport.toProperPath(makeConfigurationDescriptor.getBaseDirFileObject(), file.getPath(), project);
        itemPath = CndPathUtilities.normalizeSlashes(itemPath);
        Item item = ItemFactory.getDefault().createInFileSystem(makeConfigurationDescriptor.getBaseDirFileSystem(), itemPath);
        folder.addItemAction(item);
        return true;
    }

}

