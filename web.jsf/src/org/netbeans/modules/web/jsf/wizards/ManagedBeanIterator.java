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

package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.config.model.FacesConfig;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;

/** A template wizard iterator for new struts action
 *
 * @author Petr Pisl
 * 
 */

public class ManagedBeanIterator implements TemplateWizard.Iterator {
    
    private int index;
    
    private transient WizardDescriptor.Panel[] panels;

    private transient boolean debug = false;
    
    public void initialize (TemplateWizard wizard) {
        if (debug) log ("initialize");
        index = 0;
        // obtaining target folder
        Project project = Templates.getProject( wizard );
        DataFolder targetFolder=null;
        try {
            targetFolder = wizard.getTargetFolder();
        } catch (IOException ex) {
            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
        }
        
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (debug) {
            log ("\tproject: " + project);
            log ("\ttargetFolder: " + targetFolder);
            log ("\tsourceGroups.length: " + sourceGroups.length);
        }
        
        WizardDescriptor.Panel secondPanel = new ManagedBeanPanel(project);               
        panels = new WizardDescriptor.Panel[] {
            JavaTemplates.createPackageChooser(project, sourceGroups, secondPanel),
        };
        
        // Creating steps.
        Object prop = wizard.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) { 
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i)); // NOI18N 
	    jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
	}
    }
    
    public void uninitialize (TemplateWizard wizard) {
        panels = null;
    }
    
    public Set instantiate(TemplateWizard wizard) throws IOException {
//how to get dynamic form bean properties
//String formBeanClassName = (String) wizard.getProperty(WizardProperties.FORMBEAN_CLASS); //NOI18N
        
        if (debug)
            log("instantiate"); //NOI18N
        
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );
        
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wizard )  );
        
        String configFile = (String) wizard.getProperty(WizardProperties.CONFIG_FILE);
        Project project = Templates.getProject( wizard );
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        dir = wm.getDocumentBase();
        FileObject fo = dir.getFileObject(configFile); //NOI18N
        JSFConfigDataObject configDO = (JSFConfigDataObject)DataObject.find(fo);
        FacesConfig config= configDO.getFacesConfig();
        
        ManagedBean bean = new ManagedBean();
        String targetName = Templates.getTargetName(wizard);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(wizard);
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = org.openide.filesystems.FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
            if (packageName!=null) break;
        }
        if (packageName!=null) packageName = packageName.replace('/','.');
            else packageName="";
        String className=null;
        if (packageName.length()>0)
            className=packageName+"."+targetName;//NOI18N
        else
            className=targetName;
        
        bean.setManagedBeanName(targetName);
        bean.setManagedBeanClass(className);
        bean.setManagedBeanScope((String) wizard.getProperty(WizardProperties.SCOPE));
        
        String description = (String) wizard.getProperty(WizardProperties.DESCRIPTION);
        if (description != null && description.length() > 0){
            String newLine = System.getProperty("line.separator");
            bean.setDescription(new String[]{newLine + description + newLine});
        }
        config.addManagedBean(bean);
        configDO.write(config);        
        return Collections.singleton(dobj);
    }
    
    public void previousPanel () {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }
    
    public void nextPanel () {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }
    
    public boolean hasPrevious () {
        return index > 0;
    }
    
    public boolean hasNext () {
        return index < panels.length - 1;
    }
    
    public String name () {
        return NbBundle.getMessage (ManagedBeanIterator.class, "TITLE_x_of_y",
            new Integer (index + 1), new Integer (panels.length));
    }
    
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    
    
    private void log (String message){
        System.out.println("ActionIterator:: \t" + message);
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
    
    private void replaceInDocument(javax.swing.text.Document document, String replaceFrom, String replaceTo) {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0,doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index>=0) {
                doc.replace(index,len,replaceTo,null);
                content=content.substring(0,index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (javax.swing.text.BadLocationException ex){}
    }

}
