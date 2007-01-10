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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.managerwizard;

import java.awt.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.openide.loaders.TemplateWizard;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;

import org.netbeans.modules.jmx.runtime.J2SEProjectType;


/**
 *
 * Main Wizard class : manage the panel navigation and the code generation.
 *
 */
public class JMXManagerIterator implements TemplateWizard.Iterator {
    private static final long serialVersionUID = 1L;
    
    /** private variables */
    private transient TemplateWizard wizard;
    private transient ResourceBundle bundle;
    
    // all the panels of the wizard
    private transient ManagerPanel.ManagerWizardPanel managerOptionsPanel;  
    private transient AgentURLPanel.AgentURLWizardPanel agentURLPanel;
    
    private transient TemplateWizard.Panel managerPanel;
    private transient WizardDescriptor.Panel currentPanel;

    /**
     * Returns an manager wizard. Called with the menu new->file->JMX Manager
     * @return <CODE>JMXAgentIterator</CODE>
     */
    public static JMXManagerIterator createManagerIterator()
    {
        return new JMXManagerIterator();
    }

    /**
     * Contruct a Manager wizard.
     */
    public JMXManagerIterator()
    {
        bundle = NbBundle.getBundle(JMXManagerIterator.class);
    }
    
    /**
     * Called to really start the wizard in 
     * case of a direct call from the menu
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     */
    public void initialize (TemplateWizard wiz)
    {
        this.wizard = wiz;

        String[] steps = initSteps(false);

        wiz.putProperty("setAsMain", false);// NOI18N

        try {
            // setup project location for the current project
            WizardHelpers.setProjectValues(wiz);

            // initialize each panel
            initializeComponents(steps, 0);

        } catch (Exception ex) {
            WizardHelpers.logErrorMessage("initialize", ex);// NOI18N
        }
    }
    
    /**
     *  set the step names of this wizard
     *
     *@param wizardIntegrated true if this wizard is integrated in another
     */
    private String[] initSteps (boolean wizardIntegrated) 
    {
        
        String[] steps = new String[3];
        steps[0] = new String("Choose File Type");// NOI18N
        steps[1] = bundle.getString("LBL_Manager");// NOI18N
        steps[2] = bundle.getString("LBL_AgentURL");// NOI18N
        
        return steps;
    }

    /**
     * WizardIntegration method :
     * Called when integrating this wizard within a higher level wizard.
     * @param steps Panels list to use
     * @param panelOffset number of the first panel of this wizard
     */
    public void initializeComponents(String[] steps, int panelOffset)
    {
        JComponent jc = null;
        
        managerOptionsPanel = new ManagerPanel.ManagerWizardPanel();
        initializeComponent(steps,panelOffset + 0,
                (JComponent)managerOptionsPanel.getComponent());
        Project project = Templates.getProject(wizard);
        SourceGroup[] managerSrcGroups = 
                    WizardHelpers.getSourceGroups(project);
        managerPanel = JavaTemplates.createPackageChooser(project,
                                                        managerSrcGroups,
                                                        managerOptionsPanel);
        managerPanel.getComponent().setName(bundle.getString("LBL_Manager"));// NOI18N
        initializeComponent(steps,panelOffset + 0,
                (JComponent)managerPanel.getComponent());
        agentURLPanel = new AgentURLPanel.AgentURLWizardPanel();
        initializeComponent(steps,panelOffset + 1,
                (JComponent)agentURLPanel.getComponent());
        
        currentPanel = managerPanel;
    }

    /**
     *
     */
    private void initializeComponent(String[] steps, int panelOffset,JComponent jc) 
    {
        jc.putClientProperty("WizardPanel_contentData", steps);// NOI18N
        jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(panelOffset));// NOI18N
    }
    
    public void uninitialize(TemplateWizard wiz)
    {
        this.wizard = null;
    }

    public java.util.Set/*<FileObject>*/ instantiate (TemplateWizard wiz)
          throws java.io.IOException
    {
        //((FinishableDelegatedWizardPanel)managerPanel).storeAllSettings(wiz);
        // manager generation 
        try {
            ManagerGenerator gen = new ManagerGenerator();

            java.util.Set set = gen.generateManager(wizard);
            FileObject managerFile = (FileObject) set.toArray()[0];
            
            try {
                //Set project main class
                Boolean mainProjectClassSelected = (Boolean) wiz.getProperty(
                        WizardConstants.PROP_MANAGER_MAIN_CLASS_SELECTED);
                
                  if ((mainProjectClassSelected != null) && 
                        (mainProjectClassSelected)) {
                    Project project = Templates.getProject(wizard);
                    
                    final String agentName = Templates.getTargetName(wiz);
                    J2SEProjectType.overwriteProperty(project, "main.class", agentName);// NOI18N
                }
            } catch (Exception ex) {
                WizardHelpers.logErrorMessage("Setting project Main Class failure : ", ex);// NOI18N
            }
            
            return set;
                
        } catch (Exception ex) {
            WizardHelpers.logErrorMessage("Manager generation failure : ", ex);// NOI18N
            return Collections.EMPTY_SET;
        }
        
    }

    public String name ()
    {
        Component c = currentPanel.getComponent();

        if (c != null)
            return c.getName();

        return null;
    }

    public org.openide.WizardDescriptor.Panel current()
    {
       return currentPanel;
    }

    public boolean hasNext()
    {
        if (currentPanel == agentURLPanel)
            return false;
        else 
            return true;
    }

    public boolean hasPrevious()
    {
        if (currentPanel != managerPanel)
            return true;
        else
            return false;
    }

    public void nextPanel ()
    {   if (currentPanel == managerPanel)
            currentPanel = agentURLPanel;
          else
            throw new NoSuchElementException();
    }

    public void previousPanel ()
    {
        if (currentPanel == agentURLPanel)
            currentPanel = managerPanel;
        else
            throw new NoSuchElementException();
    }

    private transient Set listeners = new HashSet (1); // Set<ChangeListener>

    public final void addChangeListener (ChangeListener l)
    {
        synchronized (listeners) {
            listeners.add (l);
        }
    }

    public final void removeChangeListener (ChangeListener l)
    {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }

    /**
     * Fire a ChangeEvent.
     */
    protected final void fireChangeEvent ()
    {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet (listeners).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);
        while (it.hasNext ()) {
            ((ChangeListener) it.next ()).stateChanged (ev);
        }
    } 

}
