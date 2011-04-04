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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-200? Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.InputStream;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class TemplatePanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private TemplatePanelVisual component;
    private WizardDescriptor wizard;
    
    /** Creates a new instance of TemplatePanel */
    public TemplatePanel(WizardDescriptor wizard) {
        this.wizard = wizard;
        component = null;
    }
    
    public Component getComponent() {
        if (component == null)
            component = new TemplatePanelVisual();
        
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(TemplatePanel.class);
    }
    
    public void readSettings(Object settings) {
        wizard = (WizardDescriptor) settings;
    }
    
    public void storeSettings(Object settings) {
    }
    
    public boolean isValid() {
        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            Preferences preferences = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
            if (preferences.get("Facelets", "").equals("")) { //NOI18N
                ClassPath cp  = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
                boolean faceletsPresent = cp.findResource(JSFUtils.MYFACES_SPECIFIC_CLASS.replace('.', '/') + ".class") != null || //NOI18N
                                          cp.findResource("com/sun/facelets/Facelet.class") !=null || //NOI18N
                                          cp.findResource("com/sun/faces/facelets/Facelet.class") !=null || // NOI18N
                                          cp.findResource("javax/faces/view/facelets/FaceletContext.class") != null; //NOI18N
                if (!faceletsPresent) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(TemplatePanel.class, "ERR_NoJSFLibraryFound"));
                    return false;
                }
            }
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    InputStream getTemplate(){
        getComponent();
        return component.getTemplate();
    }
    
    InputStream getDefaultCSS(){
        getComponent();
        return component.getDefaultCSS();
    }
    
    InputStream getLayoutCSS(){
        getComponent();
        return component.getLayoutCSS();
    }
    
    String getLayoutFileName(){
        getComponent();
        return component.getLayoutFileName();
    }
}
