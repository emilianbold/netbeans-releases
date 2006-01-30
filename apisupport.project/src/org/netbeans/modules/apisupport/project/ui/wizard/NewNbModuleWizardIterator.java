/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new NetBeans Module project.
 *
 * @author mkrauskopf
 */
public class NewNbModuleWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    /** Either standalone module, suite component or NB CVS module. */
    final static int TYPE_MODULE = 1;
    
    /** Suite wizard. */
    final static int TYPE_SUITE = 2;
    
    /** Library wrapper module wizard. */
    final static int TYPE_LIBRARY_MODULE = 3;
    
    /** Pure suite component wizard. */
    final static int TYPE_SUITE_COMPONENT = 4;
    
    private transient int position;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor settings;
    
    private transient int type;
    
    private FileObject createdProjectFolder;
    
    /**
     * Property under which a suite to be selected in the suite combo can be
     * stored.
     */
    static final String PREFERRED_SUITE_DIR = "preferredSuiteDir"; // NOI18N
    
    private String preferredSuiteDir;
    
    /** Create a new wizard iterator. */
    private NewNbModuleWizardIterator(int type) {
        this.type = type;
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
        iterator.preferredSuiteDir = FileUtil.toFile(suite.getProjectDirectory()).getAbsolutePath();
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
        return iterator;
    }
    
    public static NewNbModuleWizardIterator createLibraryModuleIterator() {
        return new NewNbModuleWizardIterator(TYPE_LIBRARY_MODULE);
    }
    
    public FileObject getCreateProjectFolder() {
        return createdProjectFolder;
    }
    
    public Set instantiate() throws IOException {
        final NewModuleProjectData data = NewModuleProjectData.getData(settings);
        final File projectFolder = new File(data.getProjectFolder());
        ModuleUISettings.getDefault().setLastUsedModuleLocation(data.getProjectLocation());
        ModuleUISettings.getDefault().setLastUsedPlatformID(data.getPlatformID());
        if (type == TYPE_MODULE || type == TYPE_SUITE_COMPONENT) {
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
        } else if (type == TYPE_LIBRARY_MODULE) {
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
            
        } else if (this.type == TYPE_SUITE) {
            ModuleUISettings.getDefault().setNewSuiteCounter(data.getSuiteCounter());
            SuiteProjectGenerator.createSuiteProject(projectFolder, data.getPlatformID());
        } else {
            throw new IllegalStateException("Uknown wizard type: " + this.type); // NOI18N
        }
        
        this.createdProjectFolder = FileUtil.toFileObject(FileUtil.normalizeFile(projectFolder));
        
        Set/*<FileObject>*/ resultSet = new HashSet();
        resultSet.add(createdProjectFolder);
        
        UIUtil.setProjectChooserDirParent(projectFolder);
        
        // XXX this constant should be defined somewhere!
        settings.putProperty("setAsMain", Boolean.valueOf(data.isMainProject())); // NOI18N
        
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.settings = wiz;
        if (preferredSuiteDir == null) {
            Project mainPrj = OpenProjects.getDefault().getMainProject();
            if (mainPrj != null) {
                preferredSuiteDir = SuiteUtils.getSuiteDirectoryPath(mainPrj);
            }
        }
        if (preferredSuiteDir != null) {
            settings.putProperty(PREFERRED_SUITE_DIR, preferredSuiteDir);
        }
        position = 0;
        String[] steps = null;
        switch (type) {
            case TYPE_MODULE:
                steps = initModuleWizard(TYPE_MODULE);
                break;
            case TYPE_SUITE_COMPONENT:
                steps = initModuleWizard(TYPE_SUITE_COMPONENT);
                break;
            case TYPE_SUITE:
                steps = initSuiteModuleWizard();
                break;
            case TYPE_LIBRARY_MODULE:
                steps = initLibraryModuleWizard();
                break;
            default:
                assert false : "Should never get here. type: "  + type; // NOI18N
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
        this.settings = null;
        panels = null;
    }
    
    private String[] initModuleWizard(final int wizardType) {
        panels = new WizardDescriptor.Panel[] {
            new BasicInfoWizardPanel(settings, wizardType),
            new BasicConfWizardPanel(settings)
        };
        String[] steps = new String[] {
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
            getMessage("LBL_BasicConfigPanel_Title") // NOI18N
        };
        return steps;
    }
    
    private String[] initSuiteModuleWizard() {
        panels = new WizardDescriptor.Panel[] {
            new BasicInfoWizardPanel(settings, TYPE_SUITE),
        };
        String[] steps = new String[] {
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
        };
        return steps;
    }
    
    private String[] initLibraryModuleWizard() {
        panels = new WizardDescriptor.Panel[] {
            new LibraryStartWizardPanel(settings),
            new BasicInfoWizardPanel(settings, TYPE_LIBRARY_MODULE),
            new LibraryConfWizardPanel(settings)
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
