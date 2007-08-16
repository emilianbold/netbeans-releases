/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new NetBeans Module project.
 *
 * @author Martin Krauskopf
 */
public class NewNbModuleWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator {
    
    /** Either standalone module, suite component or NB CVS module. */
    static final int TYPE_MODULE = 1;
    
    /** Suite wizard. */
    static final int TYPE_SUITE = 2;
    
    /** Library wrapper module wizard. */
    static final int TYPE_LIBRARY_MODULE = 3;
    
    /** Pure suite component wizard. */
    static final int TYPE_SUITE_COMPONENT = 4;
    
    /**
     * Property under which a suite to be selected in the suite combo can be
     * stored.
     */
    static final String PREFERRED_SUITE_DIR = "preferredSuiteDir"; // NOI18N
    
    /** Tells whether the wizard should be run in a suite dedicate mode. */
    static final String ONE_SUITE_DEDICATED_MODE = "oneSuiteDedicatedMode"; // NOI18N
    
    private final NewModuleProjectData data;
    private int position;
    private WizardDescriptor.Panel[] panels;
    private FileObject createdProjectFolder;
    
    /** See {@link #PREFERRED_SUITE_DIR}. */
    private String preferredSuiteDir;
    
    /** See {@link #ONE_SUITE_DEDICATED_MODE}. */
    private Boolean suiteDedicated = Boolean.FALSE; // default
    
    /** Create a new wizard iterator. */
    private NewNbModuleWizardIterator(int type) {
        data = new NewModuleProjectData(type);
    }
    
    /**
     * Returns wizard for creating NetBeans module in general - i.e. either
     * standalone module, suite component or NB CVS module.
     */
    public static NewNbModuleWizardIterator createModuleIterator() {
        return new NewNbModuleWizardIterator(TYPE_MODULE);
    }
    
    /**
     * Returns wizard for creating suite component <strong>only</strong>.
     */
    public static NewNbModuleWizardIterator createSuiteComponentIterator(final SuiteProject suite) {
        NewNbModuleWizardIterator iterator = new NewNbModuleWizardIterator(TYPE_SUITE_COMPONENT);
        iterator.preferredSuiteDir = suite.getProjectDirectoryFile().getAbsolutePath();
        iterator.suiteDedicated = Boolean.TRUE;
        return iterator;
    }
    
    public static NewNbModuleWizardIterator createSuiteIterator() {
        return new NewNbModuleWizardIterator(TYPE_SUITE);
    }
    
    /**
     * Returns wizard for creating library wrapper module
     * <strong>only</strong>. Given project <strong>must</strong> have an
     * instance of {@link SuiteProvider} in its lookup.
     */
    public static NewNbModuleWizardIterator createLibraryModuleIterator(final Project project) {
        NewNbModuleWizardIterator iterator = new NewNbModuleWizardIterator(TYPE_LIBRARY_MODULE);
        iterator.preferredSuiteDir = SuiteUtils.getSuiteDirectoryPath(project);
        assert iterator.preferredSuiteDir != null : project + " does not have a SuiteProvider in its lookup?"; // NOI18N
        iterator.suiteDedicated = Boolean.TRUE;
        return iterator;
    }
    
    public static NewNbModuleWizardIterator createLibraryModuleIterator() {
        return new NewNbModuleWizardIterator(TYPE_LIBRARY_MODULE);
    }
    
    public FileObject getCreateProjectFolder() {
        return createdProjectFolder;
    }
    
    public Set instantiate() throws IOException {
        final File projectFolder = new File(data.getProjectFolder());
        ProjectChooser.setProjectsFolder(new File(data.getProjectLocation()));
        ModuleUISettings.getDefault().setLastUsedPlatformID(data.getPlatformID());
        WizardDescriptor settings = data.getSettings();
        switch (data.getWizardType()) {
            case NewNbModuleWizardIterator.TYPE_SUITE:
                ModuleUISettings.getDefault().setNewSuiteCounter(data.getSuiteCounter());
                SuiteProjectGenerator.createSuiteProject(projectFolder, data.getPlatformID());
                break;
            case NewNbModuleWizardIterator.TYPE_MODULE:
            case NewNbModuleWizardIterator.TYPE_SUITE_COMPONENT:
                ModuleUISettings.getDefault().setNewModuleCounter(data.getModuleCounter());
                if (data.isNetBeansOrg()) {
                    // create module within the netbeans.org CVS tree
                    NbModuleProjectGenerator.createNetBeansOrgModule(projectFolder,
                            data.getCodeNameBase(), data.getProjectDisplayName(),
                            data.getBundle(), data.getLayer());
                } else if (data.isStandalone()) {
                    // create standalone module
                    NbModuleProjectGenerator.createStandAloneModule(projectFolder,
                            data.getCodeNameBase(), data.getProjectDisplayName(),
                            data.getBundle(), data.getLayer(), data.getPlatformID());
                } else {
                    // create suite-component module
                    NbModuleProjectGenerator.createSuiteComponentModule(projectFolder,
                            data.getCodeNameBase(), data.getProjectDisplayName(),
                            data.getBundle(), data.getLayer(), new File(data.getSuiteRoot()));
                }
                break;
            case NewNbModuleWizardIterator.TYPE_LIBRARY_MODULE:
                // create suite-component module
                File[] jars = LibraryStartVisualPanel.convertStringToFiles((String) settings.getProperty(LibraryStartVisualPanel.PROP_LIBRARY_PATH));
                
                File license = null;
                String licPath = (String) settings.getProperty(LibraryStartVisualPanel.PROP_LICENSE_PATH);
                if (licPath != null && licPath.length() > 0) {
                    license = new File(licPath);
                }
                NbModuleProjectGenerator.createSuiteLibraryModule(projectFolder,
                        data.getCodeNameBase(), data.getProjectDisplayName(),
                        data.getBundle(), new File(data.getSuiteRoot()),
                        license, jars);
                break;
            default:
                throw new IllegalStateException("Uknown wizard type: " + data.getWizardType()); // NOI18N
        }
        
        this.createdProjectFolder = FileUtil.toFileObject(FileUtil.normalizeFile(projectFolder));
        
        Set<FileObject> resultSet = new HashSet<FileObject>();
        resultSet.add(createdProjectFolder);
        
        UIUtil.setProjectChooserDirParent(projectFolder);
        
        // XXX this constant should be defined somewhere!
        settings.putProperty("setAsMain", Boolean.valueOf(data.isMainProject())); // NOI18N
        
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        data.setSettings(wiz);
        if (preferredSuiteDir == null) {
            Project mainPrj = OpenProjects.getDefault().getMainProject();
            if (mainPrj != null) {
                preferredSuiteDir = SuiteUtils.getSuiteDirectoryPath(mainPrj);
            }
        }
        if (preferredSuiteDir != null) {
            wiz.putProperty(PREFERRED_SUITE_DIR, preferredSuiteDir);
            wiz.putProperty(ONE_SUITE_DEDICATED_MODE, suiteDedicated);
        }
        
        position = 0;
        String[] steps = null;
        switch (data.getWizardType()) {
            case TYPE_MODULE:
                steps = initModuleWizard();
                break;
            case TYPE_SUITE_COMPONENT:
                steps = initModuleWizard();
                break;
            case TYPE_SUITE:
                steps = initSuiteModuleWizard();
                break;
            case TYPE_LIBRARY_MODULE:
                steps = initLibraryModuleWizard();
                break;
            default:
                assert false : "Should never get here. type: "  + data.getWizardType();
        }
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // step number
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // names of currently used steps
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                
                // Following is actually needed only by direct usage of this wizard.
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    private String[] initModuleWizard() {
        panels = new WizardDescriptor.Panel[] {
            new BasicInfoWizardPanel(data),
            new BasicConfWizardPanel(data)
        };
        String[] steps = new String[] {
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
            getMessage("LBL_BasicConfigPanel_Title") // NOI18N
        };
        return steps;
    }
    
    private String[] initSuiteModuleWizard() {
        panels = new WizardDescriptor.Panel[] {
            new BasicInfoWizardPanel(data),
        };
        String[] steps = new String[] {
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
        };
        return steps;
    }
    
    private String[] initLibraryModuleWizard() {
        panels = new WizardDescriptor.Panel[] {
            new LibraryStartWizardPanel(data),
            new BasicInfoWizardPanel(data),
            new LibraryConfWizardPanel(data)
        };
        String[] steps = new String[] {
            getMessage("LBL_LibraryStartPanel_Title"), //NOi18N
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
            getMessage("LBL_PlatformSelectionPanel_Title") // NOI18N
        };
        return steps;
    }
    
    public String name() {
        // TemplateWizard internally does not use the value returned by this
        // method so we may return whatever (e.g. null) in the meantime. But it
        // would be resolved as "null" string by MessageFormat. So probably the
        // safest is to return empty string.
        return "";
    }
    
    public boolean hasNext() {
        return position < (panels.length - 1);
    }
    
    public boolean hasPrevious() {
        return position > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        position++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        position--;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[position];
    }
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    static String getMessage(String key) {
        return NbBundle.getMessage(NewNbModuleWizardIterator.class, key);
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
