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
package org.netbeans.modules.jmx.mbeanwizard;

import org.netbeans.modules.jmx.FinishableDelegatedWizardPanel;
import org.netbeans.modules.jmx.StepProblemMessage;
import org.netbeans.modules.jmx.WizardPanelWithoutReadSettings;

import java.awt.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;

import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.generator.MBeanGeneratorControl;



/**
 *
 * Main Wizard class : manage the panel navigation and the code generation.
 *
 */
public class JMXMBeanIterator implements TemplateWizard.Iterator {
    private static final long serialVersionUID = 1L; 

    /** private variables */
    private transient TemplateWizard wiz;

    private transient StandardMBeanPanel.StandardMBeanWizardPanel mbeanOptionsPanel;
    private transient MBeanAttrAndMethodPanel.AttributesWizardPanel attributePanel;
    private transient MBeanNotificationPanel.NotificationsWizardPanel notificationPanel;
    private transient MBeanJUnitPanel.JUnitWizardPanel junitOptionsPanel; 
    private transient TemplateWizard.Panel junitPanel;
    private transient FinishableDelegatedWizardPanel mbeanPanel;
    private transient WizardDescriptor.Panel currentPanel;
    private String[] steps; 
    private transient ResourceBundle bundle;
    private Project lastSelectedProject = null;

    //****************************************************************
    // Called with the menu new->file->Standard MBean
    //****************************************************************
    
    /**
     * Method called with the menu new->file->Standard MBean which provides 
     * an instance of an iterator
     * @return JMXMBeanIterator an iterator
     */
    public static JMXMBeanIterator createMBeanIterator()
    {
        return new JMXMBeanIterator();
    }

    //****************************************************************
    // default constructor : 
    //****************************************************************
    /**
     * The default constructor
     */
    public JMXMBeanIterator()
    {
        bundle = NbBundle.getBundle(JMXMBeanIterator.class);
    }

    //*********************************************************************
    // Called to really start the wizard in 
    // case of a direct call from the menu 
    //*********************************************************************
    
    /**
     * Initializing method, called to really start the wizard
     * @param wiz a TemplateWizard
     */
    public void initialize (TemplateWizard wiz)
    {
        // kludge to work around a netbeans bug :

        // create step description array
        //String[] steps = initializeSteps(wiz);
        
        this.wiz = wiz;

        steps = new String[5];
        steps[0] = new String("Choose File Type"); // should be added by netbeans
        steps[1] = bundle.getString("LBL_Standard_Panel");
        steps[2] = bundle.getString("LBL_Attribute_Panel");
        steps[3] = bundle.getString("LBL_Notification_Panel");
        steps[4] = bundle.getString("LBL_JUnit_Panel");   
        
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
            WizardHelpers.logErrorMessage("initialize", ex);
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
    public String[] initializeSteps(WizardDescriptor wiz)
    {
        this.wiz = (TemplateWizard) wiz;

        steps = new String[4];
        steps[0] = bundle.getString("LBL_Standard_Panel");
        steps[1] = bundle.getString("LBL_Attribute_Panel");
        steps[2] = bundle.getString("LBL_Notification_Panel");
        steps[3] = bundle.getString("LBL_JUnit_Panel");

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
    
    /**
     * Method which initialises the different components
     * Called when integrating this wizard within a higher level wizard
     * @param steps Panels list to use
     * @param panelOffset number of the first panel of this wizard
     */
    public void initializeComponents(String[] steps, int panelOffset)
    {
        mbeanOptionsPanel = new StandardMBeanPanel.StandardMBeanWizardPanel();
        initializeComponent(steps,panelOffset + 0,
                (JComponent)mbeanOptionsPanel.getComponent());
        
        Project project = Templates.getProject(wiz);
        SourceGroup[] mbeanSrcGroups = 
                    WizardHelpers.getSourceGroups(project);
        WizardDescriptor.Panel delegateMBeanPanel = 
                JavaTemplates.createPackageChooser(project,
                                                   mbeanSrcGroups,
                                                   mbeanOptionsPanel);
        mbeanPanel = new WizardPanelWithoutReadSettings(
                delegateMBeanPanel,mbeanOptionsPanel);
        mbeanPanel.getComponent().setName(
                bundle.getString("LBL_Standard_Panel"));
        initializeComponent(steps,panelOffset + 0,
                (JComponent)mbeanPanel.getComponent());
        ((StandardMBeanPanel.StandardMBeanWizardPanel) mbeanOptionsPanel).
                   setListenerEnabled(delegateMBeanPanel,mbeanOptionsPanel,wiz);
        mbeanPanel.readAllSettings(wiz);
        
        attributePanel = new MBeanAttrAndMethodPanel.AttributesWizardPanel();
        initializeComponent(steps,panelOffset + 1,
                (JComponent)attributePanel.getComponent());

        notificationPanel = 
                new MBeanNotificationPanel.NotificationsWizardPanel();
        initializeComponent(steps,panelOffset + 2,
                (JComponent)notificationPanel.getComponent());
        
        junitPanel = getTestChooserPanel();
        initializeComponent(steps,panelOffset + 3,
                (JComponent)junitPanel.getComponent());
        
        currentPanel = mbeanPanel;
    }

    private WizardDescriptor.Panel getTestChooserPanel() {
        final Project project = Templates.getProject(wiz);
        if ((junitPanel == null) || (project != lastSelectedProject)) {
            if (WizardHelpers.getSourcesToTestsMap(project,true).isEmpty()) {
                junitPanel = new StepProblemMessage(
                        project,
                        bundle.getString("MSG_NoTestSourceGroup"));  //NOI18N
                wiz.putProperty(WizardConstants.PROP_JUNIT_SELECTED, false);
            } else {
                if (junitPanel == null) {
                    junitPanel = new MBeanJUnitPanel.JUnitWizardPanel();
                }
                ((MBeanJUnitPanel) ((MBeanJUnitPanel.JUnitWizardPanel) junitPanel).
                        getComponent()).setUp(project);
            }
        }
        junitPanel.readSettings(wiz);
        junitPanel.storeSettings(wiz);
        lastSelectedProject = project;
        return junitPanel;
    }
    
    private void initializeComponent(String[] steps, int panelOffset,JComponent jc) 
    {
        jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        jc.putClientProperty("WizardPanel_contentSelectedIndex", panelOffset);
    }
    
    /**
     * Method which releases the wizard
     * @param wiz a TemplateWizard
     */
    public void uninitialize(TemplateWizard wiz)
    {
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
    public java.util.Set/*<FileObject>*/ instantiate (TemplateWizard wizard)
          throws java.io.IOException
    {
        mbeanPanel.storeAllSettings(wizard);
        junitPanel.readSettings(wizard);
        junitPanel.storeSettings(wizard);
 
        // mbean generation 
        try {
            MBeanGeneratorControl gen = new MBeanGeneratorControl();

            return gen.generateMBean(wizard).getCreated();
                
        } catch (Exception ex) {
            WizardHelpers.logErrorMessage("MBean generation ", ex);
            return Collections.EMPTY_SET;
        }
    }

    /**
     * Method returning the name of a component contained in the current panel
     * @return name the name of the component
     */
    public String name ()
    {
        Component c = currentPanel.getComponent();

        if (c != null)
            return c.getName();

        return null;
    }

    /**
     * Method returning the current panel
     * @return currentPanel the current panel
     */
    public org.openide.WizardDescriptor.Panel current()
    {
       return currentPanel;
    }

    /**
     * Method returning if the current panel has a next panel or not
     * Enables the next button
     * @return next true if the current panel has a next one
     */
    public boolean hasNext()
    {
       
        if (currentPanel == junitPanel) {
            return false;
        } else return true;
         
    }

    /**
     * Method returning if the current panel has a previous panel or not
     * Enables the back button
     * @return next true if the current panel has a previous one
     */
    public boolean hasPrevious()
    {
        if (currentPanel != mbeanPanel)
            return true;
        else
            return false;
    }

    /**
     * Method reaffecting the current panel variable to the next panel
     */
    public void nextPanel ()
    {  
        if (currentPanel == mbeanPanel) {
            currentPanel = attributePanel;
        } else if (currentPanel == attributePanel) {
            currentPanel = notificationPanel;
        } else if (currentPanel == notificationPanel) {
            currentPanel = getTestChooserPanel();
        }
    }

    /**
     * Method reaffecting the current panel variable to the previous panel
     */
    public void previousPanel ()
    {
        if (currentPanel == attributePanel) {
            currentPanel = mbeanPanel;
        } else if (currentPanel == notificationPanel) {
            currentPanel = attributePanel;
        } else if (currentPanel == junitPanel) {
            currentPanel = notificationPanel;
        } 
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
     * Fire a change event.
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
