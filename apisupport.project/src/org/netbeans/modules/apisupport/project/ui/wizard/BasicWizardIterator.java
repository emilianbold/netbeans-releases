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
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Convenient class for implementing {@link org.openide.WizardDescriptor.InstantiatingIterator}
 *
 * @author Radek Matous
 */
abstract public class BasicWizardIterator implements WizardDescriptor.InstantiatingIterator {
    private static final long serialVersionUID = 1L;
    private transient int position = 0;
    private transient BasicWizardIterator.PrivateWizardPanel[] wizardPanels;
    
    /** Create a new wizard iterator. */
    protected BasicWizardIterator() {}
    
    /** @return all panels provided by this {@link org.openide.WizardDescriptor.InstantiatingIterator} */
    protected abstract BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz);
    
    /** Basic visual panel.*/    
    public abstract static class Panel extends BasicVisualPanel {
        /** @return name of panel */
        protected abstract String getPanelName();
        /** update state of instance of {@link BasicWizardIterator.BasicDataModel}*/
        protected abstract void storeToDataModel();
        /** read state of instance of {@link BasicWizardIterator.BasicDataModel}*/        
        protected abstract void readFromDataModel();
        
        protected Panel(WizardDescriptor wiz) {
            super(wiz);
        }
        
    }

    /** DataModel that is passed through individual panels.*/        
    public static class BasicDataModel {
        private NbModuleProject project;
        private FileObject srcRoot = null;
        private SourceGroup sourceRootGroup;
        
        
        /** Creates a new instance of NewFileDescriptorData */
        public BasicDataModel(WizardDescriptor wiz) {
            Project tmpProject = Templates.getProject(wiz);
            
            if (!(tmpProject instanceof NbModuleProject)) {
                throw new IllegalArgumentException(project.getClass().toString());
            }
            
            project = (NbModuleProject)tmpProject;
        }
        
        public NbModuleProject getProject() {
            return project;
        }
        
        public FileObject getSourceRoot() {
            if (srcRoot == null) {
                NbModuleProject nbmProject  = getProject();
                File f  = nbmProject.getHelper().resolveFile(nbmProject.evaluator().getProperty("src.dir")); // NOI18N
                srcRoot = FileUtil.toFileObject(f);
            }
            
            return srcRoot;
        }
        
        public SourceGroup getSourceRootGroup() {
            if (sourceRootGroup == null) {
                FileObject tempSrcRoot = getSourceRoot();
                assert tempSrcRoot != null;
                
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (int i = 0; sourceRootGroup == null && i < groups.length; i++) {
                    if (groups[i].getRootFolder().equals(tempSrcRoot)) {
                        sourceRootGroup = groups[i];
                    }
                }
            }
            return sourceRootGroup;
        }
    }
    
    public void initialize(WizardDescriptor wiz) {
        position = 0;
        BasicWizardIterator.Panel[] panels = createPanels(wiz);
        String[] steps = BasicWizardIterator.createSteps(panels);
        wizardPanels = new BasicWizardIterator.PrivateWizardPanel[panels.length];
        
        for (int i = 0; i < panels.length; i++) {
            wizardPanels[i] = new BasicWizardIterator.PrivateWizardPanel(panels[i], steps, i);
        }
    }
    
    private static String[] createSteps(BasicWizardIterator.Panel[] panels) {
        String[] steps = new String[panels.length];
        
        for (int i = 0; i < panels.length; i++) {
            steps[i] = panels[i].getPanelName();
        }
        
        return steps;
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        wizardPanels = null;
    }
    
    
    public String name() {
        return ((BasicWizardIterator.PrivateWizardPanel)
        current()).getPanel().getPanelName();
    }
    
    public boolean hasNext() {
        return position < (wizardPanels.length - 1);
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
        return wizardPanels[position];
    }
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    protected final String getMessage(String key) {
        return NbBundle.getMessage(getClass(), key);
    }
    
    public final void addChangeListener(ChangeListener  l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    
    private static final class PrivateWizardPanel extends BasicWizardPanel {
        private BasicWizardIterator.Panel panel;
        PrivateWizardPanel(BasicWizardIterator.Panel panel, String[] allSteps, int stepIndex) {
            super(panel.getSettings());
            panel.addPropertyChangeListener(this);            
            panel.setName(panel.getPanelName()); // NOI18N
            this.panel = panel;
            if (panel instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)panel;
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(stepIndex)); // NOI18N
                // names of currently used steps
                jc.putClientProperty("WizardPanel_contentData", allSteps); // NOI18N
            }
            
        }
        
        private BasicWizardIterator.Panel getPanel() {
            return panel;
        }
        
        public Component getComponent() {
            return getPanel();
        }
        
        public void storeSettings(Object settings) {
            panel.storeToDataModel();
        }
        
        public void readSettings(Object settings) {
            panel.readFromDataModel();
        }
    }
}
