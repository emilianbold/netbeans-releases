/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new NetBeans Module project.
 *
 * @author mkrauskopf
 */
public class NewNbModuleWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    private final static String KIND_ATTR = "kind"; // NOI18N
    private final static String KIND_STANDALONE = "standalone"; // NOI18N
    private final static String KIND_SUITE = "suite"; // NOI18N
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N
    
    private transient int position;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor settings;
    
    /** Create a new wizard iterator. */
    public NewNbModuleWizardIterator() {}
    
    public Set instantiate() throws IOException {
        final NewModuleProjectData data = (NewModuleProjectData) settings.
                getProperty("moduleProjectData"); // XXX should be constant
        
        final File projectFolder = new File(data.getProjectFolder());
        if (data.getKind() == KIND_STANDALONE) {
            // create standalone project
            NbModuleProjectGenerator.createStandAloneModule(projectFolder,
                    data.getCodeNameBase(), data.getProjectDisplayName(),
                    data.getBundle(), data.getLayer(), data.getPlatform());
        } else  if (data.getKind() == KIND_SUITE) {
            SuiteProjectGenerator.createSuiteModule(projectFolder, data.getPlatform());
        } else {
            throw new IllegalStateException("Uknown kind: " + data.getKind()); // NOI18N
        }
        
        FileObject projectFolderFO = FileUtil.toFileObject(FileUtil.normalizeFile(projectFolder));
        
        Set/*<FileObject>*/ resultSet = new HashSet();
        resultSet.add(projectFolderFO);
        
        final File chooserFolder = (projectFolder != null) ?
            projectFolder.getParentFile() : null;
        if (chooserFolder != null && chooserFolder.exists()) {
            ProjectChooser.setProjectsFolder(chooserFolder);
        }
        
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        String kind = null;
        this.settings = wiz;
        try {
            DataObject obj = DataObject.find(Templates.getTemplate(settings));
            assert obj != null : "Cannot find data object for the template"; // NOI18N
            kind = (String) obj.getPrimaryFile().getAttribute(KIND_ATTR);
            assert kind != null : "kind attribute wasn't found"; // NOI18N
        } catch (DataObjectNotFoundException e) {
            assert false : "Cannot find data object for the template"; // NOI18N
        }
        NewModuleProjectData data = new NewModuleProjectData();
        data.setKind(kind);
        settings.putProperty("moduleProjectData", data); // NOI18N
        position = 0;
        String[] steps = null;
        if (kind.equals("standalone")) { // NOI18N
            steps = initStandaloneModuleWizard();
        } else if (kind.equals("suite")) { // NOI18N
            steps = initSuiteModuleWizard();
        } else {
            assert false : "Illegal value for the king attribute"; // NOI18N
        }
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // step number
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // names of currently used steps
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        this.settings = null;
        panels = null;
    }
    
    private String[] initStandaloneModuleWizard() {
        panels = new WizardDescriptor.Panel[] {
            new BasicInfoWizardPanel(settings),
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
            new BasicInfoWizardPanel(settings),
            new PlatformSelectionWizardPanel(settings)
        };
        String[] steps = new String[] {
            getMessage("LBL_BasicInfoPanel_Title"), // NOI18N
            getMessage("LBL_PlatformSelectionPanel_Title") // NOI18N
        };
        return steps;
    }
    
    public String name() {
        return null; // XXX is this used somewhere actually
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
