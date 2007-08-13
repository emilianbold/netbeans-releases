/*
 * Main.java
 *
 * Created on April 6, 2004, 3:39 PM
 */

package org.netbeans.modules.websvc.metro.samples;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class WebSampleProjectIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 4L;
    
    int currentIndex;
    PanelConfigureProject basicPanel;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new WebSampleProjectIterator();
    }
    
    public WebSampleProjectIterator () {
    }
    
    public void addChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current () {
        return basicPanel;
    }
    
    public boolean hasNext () {
        return false;
    }
    
    public boolean hasPrevious () {
        return false;
    }
    
    public void initialize (org.openide.loaders.TemplateWizard templateWizard) {
        this.wiz = templateWizard;
        String name = templateWizard.getTemplate().getNodeDelegate().getDisplayName();
        if (name != null) {
            name = name.replaceAll(" ", ""); //NOI18N
        }
        templateWizard.putProperty (WizardProperties.NAME, name);
        basicPanel = new PanelConfigureProject();
        currentIndex = 0;
        updateStepsList ();
    }
    
    public void uninitialize (org.openide.loaders.TemplateWizard templateWizard) {
        basicPanel = null;
        this.wiz.putProperty(WizardProperties.PROJECT_DIR,null);
        this.wiz.putProperty(WizardProperties.NAME,null);
        currentIndex = -1;
    }
    
    public java.util.Set instantiate (org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        File projectLocation = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
                        
        Collection<FileObject> prjLocs = null;
        prjLocs = WebSampleProjectGenerator.createProjectFromTemplate(templateWizard.getTemplate().getPrimaryFile(), projectLocation, name);
        
        Set hset = new HashSet();
        for (FileObject prj : prjLocs) {
            FileObject webRoot = prj.getFileObject("web");    //NOI18N
            FileObject index = getIndexFile(webRoot);
            if (webRoot != null) hset.add(DataObject.find(prj));
            if (index != null) hset.add(DataObject.find(index));
        }
        return hset;
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        throw new NoSuchElementException ();
    }
    
    public void previousPanel() {
        throw new NoSuchElementException ();
    }
    
    void updateStepsList() {
        JComponent component = (JComponent) current ().getComponent ();
        if (component == null) {
            return;
        }
        String[] list;
        list = new String[] {
            NbBundle.getMessage(PanelConfigureProject.class, "LBL_NWP1_ProjectTitleName"), // NOI18N
        };
        component.putClientProperty ("WizardPanel_contentData", list); // NOI18N
        component.putClientProperty ("WizardPanel_contentSelectedIndex", Integer.valueOf(currentIndex)); // NOI18N
    }
    
    private FileObject getIndexFile(FileObject webRoot) {
        FileObject file = null;
        file = webRoot.getFileObject("index", "jsp");
        if (file == null) {
            file = webRoot.getFileObject("index", "html");
        }
        return file;
    }
    
}
