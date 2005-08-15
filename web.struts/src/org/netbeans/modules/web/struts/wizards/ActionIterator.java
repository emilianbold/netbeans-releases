/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.struts.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.struts.StrutsConfigDataObject;
import org.netbeans.modules.web.struts.config.model.*;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.SourceGroup;
//import org.netbeans.modules.web.core.Util;
//import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;


/** A template wizard iterator for new struts action
 *
 * @author Petr Pisl
 * 
 */

public class ActionIterator implements TemplateWizard.Iterator {
    
    private int index;
    
    private transient WizardDescriptor.Panel[] panels;
    
    private transient boolean debug = true;
    
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
        
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (debug) {
            log ("\tproject: " + project);
            log ("\ttargetFolder: " + targetFolder);
            log ("\tsourceGroups.length: " + sourceGroups.length);
        }
        
        WizardDescriptor.Panel secondPanel = new ActionPanel(project);
        WizardDescriptor.Panel thirdPanel = new ActionPanel1(project);
        
        panels = new WizardDescriptor.Panel[] {
            JavaTemplates.createPackageChooser(project, sourceGroups, secondPanel),
            thirdPanel
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
        if (debug)
            log("instantiate");
        
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );
        
        String superclass=(String)wizard.getProperty(WizardProperties.ACTION_SUPERCLASS);
        if (debug)
            log("superclass="+superclass);
        
        if (ActionPanelVisual.DISPATCH_ACTION.equals(superclass)) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("DispatchAction","java"); //NOI18N
        } else if (ActionPanelVisual.MAPPING_DISPATCH_ACTION.equals(superclass)) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("MappingDispatchAction","java"); //NOI18N
        } else if (ActionPanelVisual.LOOKUP_DISPATCH_ACTION.equals(superclass)) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("LookupDispatchAction","java"); //NOI18N
        }
        
        String targetName = Templates.getTargetName(wizard);
        DataObject dTemplate = DataObject.find( template );
        DataObject dobj = dTemplate.createFromTemplate( df, targetName  );
        
        Project project = Templates.getProject( wizard );
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        dir = wm.getDocumentBase();
        String configFile = (String) wizard.getProperty(WizardProperties.ACTION_CONFIG_FILE);
        FileObject fo = dir.getFileObject(configFile); 
        StrutsConfigDataObject configDO = (StrutsConfigDataObject)DataObject.find(fo);
        StrutsConfig config= configDO.getStrutsConfig();
        Action action = new Action();
        
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
        action.setAttributeValue("type", className);
        
        String path = (String) wizard.getProperty(WizardProperties.ACTION_PATH);
        action.setAttributeValue("path", path.startsWith("/") ? path : "/" + path);
        
        String formName = (String) wizard.getProperty(WizardProperties.ACTION_FORM_NAME);
        if (formName!=null) {
            action.setAttributeValue("name", formName);
            action.setAttributeValue("scope",(String) wizard.getProperty(WizardProperties.ACTION_SCOPE));
            action.setAttributeValue("input",(String) wizard.getProperty(WizardProperties.ACTION_INPUT));
            action.setAttributeValue("attribute",(String) wizard.getProperty(WizardProperties.ACTION_ATTRIBUTE));
            Boolean validate = (Boolean) wizard.getProperty(WizardProperties.ACTION_VALIDATE);
            if (Boolean.FALSE.equals(validate)) action.setAttributeValue("validate","false"); //NOI18N
            action.setAttributeValue("attribute",(String) wizard.getProperty(WizardProperties.ACTION_ATTRIBUTE));
        }
        action.setAttributeValue("parameter",(String) wizard.getProperty(WizardProperties.ACTION_PARAMETER));
        
        config.getActionMappings().addAction(action);
        configDO.write(config);
       // config.getActionMappings().addAction( new Action())
        
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
        return NbBundle.getMessage (ActionIterator.class, "TITLE_x_of_y",
            new Integer (index + 1), new Integer (panels.length));
    }
    
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    
    
    private void log (String message){
        System.out.println("ActionIterator:: \t" + message);    //NOI18N
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
    //private static final long serialVersionUID = -4147344271705652643L;

}
