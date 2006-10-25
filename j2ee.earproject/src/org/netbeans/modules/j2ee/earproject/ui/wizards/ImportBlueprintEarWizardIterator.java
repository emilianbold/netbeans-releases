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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.ModuleType;
import org.netbeans.modules.j2ee.earproject.ui.FoldersListSettings;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Wizard for importing a new Enterprise Application project.
 * @author Jesse Glick
 */
public class ImportBlueprintEarWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    transient WizardDescriptor wiz;
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new PanelConfigureProject(PROP_NAME_INDEX,
                    NbBundle.getBundle(ImportBlueprintEarWizardIterator.class),
                    new HelpCtx(this.getClass()), true),
            new PanelModuleDetection()
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
            NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_IW_ApplicationModulesStep")
        };
    }
    
    public Set<FileObject> instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        handle.progress(NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        File srcF = (File) wiz.getProperty(WizardProperties.SOURCE_ROOT);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        //        String contextPath = (String) wiz.getProperty(WizardProperties.CONTEXT_PATH);
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String platformName = (String)wiz.getProperty(WizardProperties.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(WizardProperties.SOURCE_LEVEL);
        @SuppressWarnings("unchecked")
        Map<FileObject, ModuleType> userModules = (Map<FileObject, ModuleType>)
                wiz.getProperty(WizardProperties.USER_MODULES);
        return testableInstantiate(platformName, sourceLevel, j2eeLevel, dirF,
                srcF, serverInstanceID, name, userModules, handle);
    }
    
    /** <strong>Package private for unit test only</strong>. */
    static Set<FileObject> testableInstantiate(final String platformName,
            final String sourceLevel, final String j2eeLevel, final File dirF,
            final File srcF, final String serverInstanceID, final String name,
            final Map<FileObject, ModuleType> userModules, ProgressHandle handle) throws IOException {
        
        EarProjectGenerator.importProject(dirF, srcF, name, j2eeLevel,
                serverInstanceID, platformName, sourceLevel, userModules);
        if (handle != null)
            handle.progress(2);

        FileObject dir = FileUtil.toFileObject(FileUtil.normalizeFile(dirF));
        
        // remember last used server
        FoldersListSettings.getDefault().setLastUsedServer(serverInstanceID);
        Set<FileObject> resultSet = new HashSet<FileObject>();
        resultSet.add(dir);
        
        NewEarProjectWizardIterator.setProjectChooserFolder(dirF);
        
        if (handle != null)
            handle.progress(NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);

        // Returning set of FileObject of project diretory.
        // Project will be open and set as main
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
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
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardProperties.PROJECT_DIR,null);
            this.wiz.putProperty(WizardProperties.NAME,null);
        }
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(
                NbBundle.getMessage(ImportBlueprintEarWizardIterator.class, "LBL_WizardStepsCount"),
                index + 1, panels.length);
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
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    // helper methods, finds indexJSP's FileObject
    FileObject getIndexJSPFO(FileObject webRoot, String indexJSP) {
        // XXX: ignore unvalid mainClass?
        return webRoot.getFileObject(indexJSP.replace('.', '/'), "jsp"); // NOI18N
    }
    
}
