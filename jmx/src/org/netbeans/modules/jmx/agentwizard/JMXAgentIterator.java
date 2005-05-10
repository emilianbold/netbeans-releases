/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.agentwizard;

import javax.swing.JOptionPane;
import java.text.MessageFormat;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import javax.swing.KeyStroke;
import java.io.OutputStream;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.openide.loaders.TemplateWizard;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;

import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.javacore.api.JavaModel;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardIntegration;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.configwizard.ConfigPanel;

import org.netbeans.modules.jmx.runtime.J2SEProjectType;

/**
 *
 * Main Wizard class : manage the panel navigation and the code generation.
 *
 */
public class JMXAgentIterator implements TemplateWizard.Iterator,
                                      WizardIntegration
{
    private static final long serialVersionUID = 1L;
    
    /** private variables */
    private transient TemplateWizard wizard;
    private transient ResourceBundle bundle;
    
    // all the panels of the wizard
    private transient AgentPanel.AgentWizardPanel agentOptionsPanel;
    private transient TemplateWizard.Panel agentPanel;
    private transient WizardDescriptor.Panel currentPanel;

    //****************************************************************
    // Called with the menu new->file->JMX Agent
    //****************************************************************
    public static JMXAgentIterator createAgentIterator()
    {
        return new JMXAgentIterator();
    }

    //****************************************************************
    // default constructor : 
    //****************************************************************
    public JMXAgentIterator()
    {
        bundle = NbBundle.getBundle(JMXAgentIterator.class);
    }
    
    //*********************************************************************
    // Called to really start the wizard in 
    // case of a direct call from the menu 
    //*********************************************************************
    public void initialize (TemplateWizard wiz)
    {
        this.wizard = wiz;

        String[] steps = initSteps(false);
        
        wiz.putProperty("setAsMain", false); // NOI18N

        try {
            // setup project location for the current project (mimic cacao wizard)
            WizardHelpers.setProjectValues(wiz);

            // initialize each panel
            initializeComponents(steps, 0);

        } catch (Exception ex) {
            WizardHelpers.logErrorMessage("initialize", ex);
        }
    }
    
    /**
     *  set the step names of this wizard
     *
     *@param wizardIntegrated true if this wizard is integrated in another
     */
    private String[] initSteps (boolean wizardIntegrated) 
    {
        int size = 2;
        if (wizardIntegrated)
            size--;
        String[] steps = new String[size];
        if (!wizardIntegrated) {
            steps[0] = new String("Choose File Type"); // should be added by netbeans
        }
        steps[size - 1] = bundle.getString("LBL_Agent");
        return steps;
    }

    //*********************************************************************
    // WizardIntegration method :
    // 
    // Called when integrating this wizard within a higher level wizard.
    //
    //*********************************************************************
    public String[] initializeSteps(WizardDescriptor wiz)
    {
        this.wizard = (TemplateWizard) wiz;
        return initSteps(true);
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
    public void initializeComponents(String[] steps, int panelOffset)
    {
        JComponent jc = null;
        
        agentOptionsPanel = new AgentPanel.AgentWizardPanel();
        initializeComponent(steps,panelOffset + 0,
                (JComponent)agentOptionsPanel.getComponent());
        Project project = Templates.getProject(wizard);
        SourceGroup[] agentSrcGroups = 
                    WizardHelpers.getSourceGroups(project);
        agentPanel = JavaTemplates.createPackageChooser(project,
                                                        agentSrcGroups,
                                                        agentOptionsPanel);
        initializeComponent(steps,panelOffset + 0,
                (JComponent)agentPanel.getComponent());
        currentPanel = agentPanel;
    }

    /**
     *
     */
    private void initializeComponent(String[] steps, int panelOffset,JComponent jc) 
    {
        jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        jc.putClientProperty("WizardPanel_contentSelectedIndex", panelOffset);
    }
    
    //*********************************************************************
    //*********************************************************************
    public void uninitialize(TemplateWizard wiz)
    {
        this.wizard = null;
    }

    //*********************************************************************
    // real code / file generation
    //*********************************************************************
    public java.util.Set/*<FileObject>*/ instantiate (TemplateWizard wiz)
          throws java.io.IOException
    {
        // agent generation 
        try {
            AgentGenerator gen = new AgentGenerator();

            java.util.Set set = gen.generateAgent(wizard).getCreated();
            FileObject agentFile = (FileObject) set.toArray()[0];
            
            try {
                //Set project main class
                Boolean mainMethodSelected = (Boolean) wiz.getProperty(
                        WizardConstants.PROP_AGENT_MAIN_METHOD_SELECTED);
                Boolean mainProjectClassSelected = (Boolean) wiz.getProperty(
                        WizardConstants.PROP_AGENT_MAIN_CLASS_SELECTED);
                
                if ( ((mainMethodSelected != null) && (mainMethodSelected)) &&
                        ((mainProjectClassSelected != null) && (mainProjectClassSelected)) ) {
                    Project project = Templates.getProject(wizard);
                    Resource agentRc = JavaModel.getResource(agentFile);
                    JavaClass agentClass = WizardHelpers.getAgentJavaClass(agentRc,
                            agentFile.getName());
                    J2SEProjectType.overwriteProperty(project, "main.class", agentClass.getName());
                }
            } catch (Exception ex) {
                WizardHelpers.logErrorMessage("Setting project Main Class failure : ", ex);
            }
            
            return set;
                
        } catch (Exception ex) {
            WizardHelpers.logErrorMessage("Agent generation failure : ", ex);
            return Collections.EMPTY_SET;
        }
        
    }

    //*********************************************************************
    //
    //*********************************************************************
    public String name ()
    {
        Component c = currentPanel.getComponent();

        if (c != null)
            return c.getName();

        return null;
    }

    //*********************************************************************
    //
    //*********************************************************************
    public org.openide.WizardDescriptor.Panel current()
    {
       return currentPanel;
    }

    //*********************************************************************
    //
    //*********************************************************************
    public boolean hasNext()
    {
        return false; 
    }

    //*********************************************************************
    //
    //*********************************************************************
    public boolean hasPrevious()
    {
        return false;
    }

    //*********************************************************************
    //
    //*********************************************************************
    public void nextPanel ()
    {   if (!hasNext()) {
            throw new NoSuchElementException();
        } 
    }

    //*********************************************************************
    //
    //*********************************************************************
    public void previousPanel ()
    {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        } 
    }

    //*********************************************************************
    //
    //*********************************************************************
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
