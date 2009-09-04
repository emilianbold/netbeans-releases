/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Description;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
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
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;

/** A template wizard iterator for new struts action
 *
 * @author Petr Pisl, Alexey BUtenko
 * 
 */

public class ManagedBeanIterator implements TemplateWizard.Iterator {
    
    private int index;
    private ManagedBeanPanel managedBeanPanel;

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
        
        managedBeanPanel = new ManagedBeanPanel(project, wizard);
        
        WizardDescriptor.Panel javaPanel;
        if (sourceGroups.length == 0)
            javaPanel = Templates.createSimpleTargetChooser(project, sourceGroups, managedBeanPanel);
        else
            javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, managedBeanPanel);

        javaPanel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                managedBeanPanel.updateManagedBeanName((WizardDescriptor.Panel) e.getSource());
            }
        });

        panels = new WizardDescriptor.Panel[] { javaPanel };
        
        // Creating steps.
        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
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
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer (i)); // NOI18N 
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
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

        String configFile = (String) wizard.getProperty(WizardProperties.CONFIG_FILE);
        Project project = Templates.getProject( wizard );
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        dir = wm.getDocumentBase();
        if (configFile == null) {
            if (!JSFConfigUtilities.hasJsfFramework(dir)) {
                JSFConfigUtilities.extendJsfFramework(dir, false);
            }
        }
        String beanName = getUniqueName((String) wizard.getProperty(WizardProperties.NAME), wm);
        Scope scope = (ManagedBean.Scope) wizard.getProperty(WizardProperties.SCOPE);
        boolean isAnnotate = !managedBeanPanel.isAddBeanToConfig();
        DataObject dobj = null;

        if (isAnnotate && Utilities.isJavaEE6(wizard)) {
            HashMap<String, String> templateProperties = new HashMap<String, String>();
            templateProperties.put("classAnnotation", "@ManagedBean(name=\""+beanName+"\")");   //NOI18N
            templateProperties.put("scopeAnnotation", SCOPES.get(scope).toString());    //NOI18N
            dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wizard ),templateProperties  );
        } else {
            FileObject fo = dir.getFileObject(configFile); //NOI18N
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(fo, true).getRootComponent();
            JSFBeanCache.getBeans(wm);
            dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wizard ));

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

            bean.setManagedBeanName(beanName);
            bean.setManagedBeanClass(className);
            bean.setManagedBeanScope(scope);

            String description = (String) wizard.getProperty(WizardProperties.DESCRIPTION);
            if (description != null && description.length() > 0){
                Description beanDescription = bean.getModel().getFactory().createDescription();
                beanDescription.setValue(description);
                bean.addDescription(beanDescription);
            }
            facesConfig.getModel().startTransaction();
            facesConfig.addManagedBean(bean);
            facesConfig.getModel().endTransaction();
            facesConfig.getModel().sync();
        }
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

    private String getUniqueName(String original, WebModule wm) {
        String value = original;
        int count=0;
        for (FacesManagedBean managedBean: JSFBeanCache.getBeans(wm)) {
            if (value.equals(managedBean.getManagedBeanName())) {
                count++;
                value = original+count;
            }
        }
        return value;
    }

    private final static Map<ManagedBean.Scope, String> SCOPES
                = new HashMap<Scope, String>();
    static {
        SCOPES.put(ManagedBean.Scope.APPLICATION,
                "ApplicationScoped"); // NOI18N
        SCOPES.put(ManagedBean.Scope.NONE,
                "NoneScoped"); // NOI18N
        SCOPES.put(ManagedBean.Scope.REQUEST,
                "RequestScoped");        // NOI18N
        SCOPES.put(ManagedBean.Scope.SESSION,
                "SessionScoped");        // NOI18N
        SCOPES.put(ManagedBean.Scope.VIEW,
                "ViewScoped");           // NOI18N
    }
}
