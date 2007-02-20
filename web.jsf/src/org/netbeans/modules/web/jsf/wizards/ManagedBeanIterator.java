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

package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.editor.JSFEditorUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory;
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
        
        WizardDescriptor.Panel secondPanel = new ManagedBeanPanel(project, wizard);
        
        WizardDescriptor.Panel javaPanel;
        if (sourceGroups.length == 0)
            javaPanel = Templates.createSimpleTargetChooser(project, sourceGroups, secondPanel);
        else
            javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, secondPanel);

        panels = new WizardDescriptor.Panel[] { javaPanel };
        
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
        FacesConfig facesConfig = ConfigurationUtils.getConfigModel(fo, true).getRootComponent();
                
        ManagedBean bean = facesConfig.getModel().getFactory().createManagedBean();
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
        
        bean.setManagedBeanName(getUniqueName(targetName, facesConfig));
        bean.setManagedBeanClass(className);
        bean.setManagedBeanScope((String) wizard.getProperty(WizardProperties.SCOPE));
        
        String description = (String) wizard.getProperty(WizardProperties.DESCRIPTION);
        if (description != null && description.length() > 0){
            String newLine = System.getProperty("line.separator");
            bean.setDescription(newLine + description + newLine);
        }
        facesConfig.getModel().startTransaction();
        facesConfig.addManagedBean(bean);
        facesConfig.getModel().endTransaction();
        facesConfig.getModel().sync();
        
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
    
    private String getUniqueName(String original, FacesConfig facesConfig){
        String value = original;
        Collection<ManagedBean> beans = facesConfig.getManagedBeans();
        int count = 0;
        for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
            ManagedBean managedBean = it.next();
            if (!value.equals(managedBean.getManagedBeanName())){
                index++;
            }
            else {
                index = 0;
                count++;
                value = original+count;
            }
        }
        return value;
    }

}
