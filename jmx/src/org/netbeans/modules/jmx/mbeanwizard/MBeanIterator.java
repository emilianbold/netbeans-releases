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
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard;

import java.awt.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.FinishableDelegatedWizardPanel;
import org.netbeans.modules.jmx.WizardPanelWithoutReadSettings;
import org.netbeans.modules.jmx.mbeanwizard.generator.GeneratorControler;

/**
 *
 * Main Wizard class for MXBean and Standard MBean
 *
 */
public abstract class MBeanIterator implements TemplateWizard.Iterator {
    private static final long serialVersionUID = 1L;
    
    /** private variables */
    private transient TemplateWizard wiz;
    
    private transient MBeanPanel.MBeanPanelWizardPanel mbeanTemplatePanel;
    private transient FinishableDelegatedWizardPanel mbeanPanel;
    private transient WizardDescriptor.Panel currentPanel;
    private String[] steps;
    private transient ResourceBundle bundle;
    private Project lastSelectedProject = null;
    
    //****************************************************************
    // Called with the menu new->file->Standard MBean
    //****************************************************************
    
    //****************************************************************
    // default constructor :
    //*************************************JMXMBeanIterator_1***********
    /**
     * The default constructor
     */
    protected MBeanIterator() {
         bundle = NbBundle.getBundle(MBeanIterator.class);
    }
    
    //*********************************************************************
    // Called to really start the wizard in
    // case of a direct call from the menu
    //*********************************************************************
    
    /**
     * Initializing method, called to really start the wizard
     * @param wiz a TemplateWizard
     */
    public void initialize(TemplateWizard wiz) {
        // kludge to work around a netbeans bug :
        
        // create step description array
        //String[] steps = initializeSteps(wiz);
        
        this.wiz = wiz;
        
        steps = new String[2];
        steps[0] = new String("Choose File Type"); // NOI18N // should be added by netbeans
        steps[1] = bundle.getString("LBL_Standard_Panel");// NOI18N
        
        // end of work around
        
        // Don't set the generated project as the new Netbeans "main project" !!
        //
        // cf ../projects/projectui/src/org/netbeans/modules/project/ui/actions/NewProject.java
        //
        wiz.putProperty("setAsMain", false); // NOI18N
        
        try {
            
            // setup project location for the current project
            WizardHelpers.setProjectValues(wiz);
            // initialize each panel
            initializeComponents(steps, 0);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            WizardHelpers.logErrorMessage("initialize", ex);// NOI18N
        }
    }
    
    //*********************************************************************
    // WizardIntegration method :
    //
    // Called when integrating this wizard within a higher level wizard.
    //
    //*********************************************************************
    
    /**
     * Method which defines the different steps of our wizard;
     * Called when integrating this wizard within a higher level
     * wizard
     * @param wiz a WizardDescriptor
     * @return <CODE>String[]</CODE> step names
     */
    
    public String[] initializeSteps(WizardDescriptor wiz) {
        this.wiz = (TemplateWizard) wiz;
        
        steps = new String[1];
        steps[0] = bundle.getString("LBL_Standard_Panel");// NOI18N
        return steps;
    }
    
    //*********************************************************************
    // WizardIntegration method :
    //
    // Called when integrating this wizard within a higher level wizard.
    //
    // Parameters :
    //
    // Steps       : Panels list to use
    // panelOffset : number of the first panel of this wizard
    //
    //*********************************************************************
    
    protected abstract String getGeneratedMBeanType();
    
    /**
     * Method which initialises the different components
     * Called when integrating this wizard within a higher level wizard
     * @param steps Panels list to use
     * @param panelOffset number of the first panel of this wizard
     */
    
    public void initializeComponents(String[] steps, int panelOffset) {
        mbeanTemplatePanel = new MBeanPanel.MBeanPanelWizardPanel(getGeneratedMBeanType());
        initializeComponent(steps,panelOffset + 0,
                (JComponent)mbeanTemplatePanel.getComponent());
        
        Project project = Templates.getProject(wiz);
        SourceGroup[] mbeanSrcGroups =
                WizardHelpers.getSourceGroups(project);
        WizardDescriptor.Panel delegateMBeanPanel =
                JavaTemplates.createPackageChooser(project,
                mbeanSrcGroups,
                mbeanTemplatePanel);
        mbeanPanel = new WizardPanelWithoutReadSettings(
                delegateMBeanPanel,mbeanTemplatePanel);
        mbeanPanel.getComponent().setName(
                bundle.getString("LBL_Standard_Panel"));// NOI18N
        initializeComponent(steps,panelOffset + 0,
                (JComponent)mbeanPanel.getComponent());
        ((MBeanPanel.MBeanPanelWizardPanel) mbeanTemplatePanel).
                setListenerEnabled(delegateMBeanPanel,mbeanTemplatePanel,wiz);
        mbeanPanel.readAllSettings(wiz);
        
        currentPanel = mbeanPanel;
    }
    
    private void initializeComponent(String[] steps, int panelOffset,JComponent jc) {
        jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        jc.putClientProperty("WizardPanel_contentSelectedIndex", panelOffset);// NOI18N
    }
    
    /**
     * Method which releases the wizard
     * @param wiz a TemplateWizard
     */
    public void uninitialize(TemplateWizard wiz) {
        this.wiz = null;
    }
    
    //*********************************************************************
    // real code / file generation
    //*********************************************************************
    
    /**
     * Method which recalls the stored data and generates the mbean
     * @return <CODE>Set</CODE> set of generated files to open
     * @param wizard the wizard which contains all the data
     * @throws java.io.IOException <CODE>IOException</CODE>
     */
    public java.util.Set/*<FileObject>*/ instantiate(TemplateWizard wizard)
    throws java.io.IOException {
        mbeanPanel.storeAllSettings(wizard);
        
        // mbean generation
        try {
            
            return GeneratorControler.generate(wizard);
            
        } catch (Exception ex) {
            WizardHelpers.logErrorMessage("MBean generation ", ex);// NOI18N
            return Collections.EMPTY_SET;
        }
    }
    
    /**
     * Method returning the name of a component contained in the current panel
     * @return name the name of the component
     */
    public String name() {
        Component c = currentPanel.getComponent();
        
        if (c != null)
            return c.getName();
        
        return null;
    }
    
    /**
     * Method returning the current panel
     * @return currentPanel the current panel
     */
    public org.openide.WizardDescriptor.Panel current() {
       return currentPanel;
    }
    
    /**
     * Method returning if the current panel has a next panel or not
     * Enables the next button
     * @return next true if the current panel has a next one
     */
    public boolean hasNext() {
        
        if (currentPanel == mbeanPanel) {
            return false;
        } else return true;
        
    }
    
    /**
     * Method returning if the current panel has a previous panel or not
     * Enables the back button
     * @return next true if the current panel has a previous one
     */
    public boolean hasPrevious() {
            return false;
    }
    
    /**
     * Method reaffecting the current panel variable to the next panel
     */
    public void nextPanel() {
    }
    
    
    /**
     * Method reaffecting the current panel variable to the previous panel
     */
    public void previousPanel() {
    }
    
    private transient Set listeners = new HashSet(1); // Set<ChangeListener>
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    /**
     * Fire a change event.
     */
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
}
