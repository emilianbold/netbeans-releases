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

package org.netbeans.modules.web.wizards;

import java.awt.Component;

import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;

/* Wizard panel that collects data for the Servlet and Filter
 * wizards. 
 *
 * @author Ana von Klopp, Milan Kuchtiak
 */
public class ServletPanel implements WizardDescriptor.FinishPanel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private transient BaseWizardPanel wizardPanel;
    private transient TemplateWizard wizard; 
    /** listener to changes in the wizard */
    private ChangeListener listener;
    private DeployData deployData;
    private transient TargetEvaluator evaluator;

    /** Create the wizard panel descriptor. */
    private ServletPanel(TargetEvaluator evaluator, TemplateWizard wizard, 
			 boolean first) {
        this.evaluator=evaluator;                   
	this.wizard = wizard; 
	this.deployData = evaluator.getDeployData(); 
	if(first) 
	    this.wizardPanel = new DeployDataPanel(evaluator); 
	else 
	    this.wizardPanel = new DeployDataExtraPanel(evaluator); 
    }

    /** Create the wizard panel descriptor. */
    public static ServletPanel createServletPanel(TargetEvaluator evaluator, 
						  TemplateWizard wizard) { 
	return new ServletPanel(evaluator, wizard, true); 
    }

    /** Create the wizard panel descriptor. */
    public static ServletPanel createFilterPanel(TargetEvaluator evaluator, 
						  TemplateWizard wizard) {
	return new ServletPanel(evaluator, wizard, false); 
    }
    
    public Component getComponent () {
        return wizardPanel; 
    }

    public boolean isValid() {
	if(deployData.isValid()) { 
	    wizard.putProperty("WizardPanel_errorMessage", ""); //NOI18N
	    return true;
	}
	wizard.putProperty("WizardPanel_errorMessage", //NOI18N
			   deployData.getErrorMessage()); 
	return false; 
    } 
    
    public HelpCtx getHelp() {
        return null;
        //return wizardPanel.getHelp(); 
    }
    
    /** Add a listener to changes of the panel's validity.
    * @param l the listener to add
    * @see #isValid
    */
    public void addChangeListener (ChangeListener l) {
        if (listener != null) throw new IllegalStateException ();
        if (wizardPanel != null)
	    wizardPanel.addChangeListener (l);
        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
    * @param l the listener to remove
    */
    public void removeChangeListener (ChangeListener l) {
        listener = null;
        if (wizardPanel != null)
	    wizardPanel.removeChangeListener (l);
    }
    
    public void readSettings (Object settings) {
        if(settings instanceof TemplateWizard) {
            TemplateWizard w = (TemplateWizard)settings;
            //Project project = Templates.getProject(w);
            String targetName = w.getTargetName();
            org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(w);
            Project project = Templates.getProject( w );
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String packageName = null;
            for (int i = 0; i < groups.length && packageName == null; i++) {
                if (WebModule.getWebModule (groups [i].getRootFolder ()) != null) {
                    packageName = org.openide.filesystems.FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
                }
            }
            if (packageName!=null)
                packageName = packageName.replace('/','.');
            else packageName="";
            if (targetName==null)
                evaluator.setClassName(w.getTemplate().getName(),packageName);
            else 
                evaluator.setClassName(targetName,packageName);
        }
	wizardPanel.setData(); 
    }
    
    public void storeSettings (Object settings) {
	// do nothing
    }
}