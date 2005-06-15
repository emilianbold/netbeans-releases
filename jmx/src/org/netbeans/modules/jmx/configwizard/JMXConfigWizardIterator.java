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
package org.netbeans.modules.jmx.configwizard;
import org.netbeans.modules.jmx.FinishableDelegatedWizardPanel;
import org.netbeans.modules.jmx.GenericWizardPanel;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
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
import org.openide.loaders.TemplateWizard;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.configwizard.generator.ConfigGenerator;
import org.netbeans.modules.jmx.WizardPanelWithoutReadSettings;



/**
 *
 * Main Wizard class : manage the panel navigation and the code generation.
 *
 */
public class JMXConfigWizardIterator implements TemplateWizard.Iterator
{
      /** */
    private static JMXConfigWizardIterator instance;
    
    private transient ResourceBundle bundle;
    /** */
    private TemplateWizard wizard;

    /** index of step &quot;Name &amp; Location&quot; */
    private static final int INDEX_TARGET = 2;
    private static final int INDEX_RMI = 3;
    private static final int INDEX_SNMP = 4;
    private static final int INDEX_OTHER = 5;

    /** name of panel &quot;Name &amp; Location&quot; */
    private final String nameTarget = NbBundle.getMessage(
            JMXConfigWizardIterator.class,
            "LBL_panel_Target");  
    
    private final String nameRMI = NbBundle.getMessage(
            JMXConfigWizardIterator.class,
            "LBL_RMI_Panel");  
    
    private final String nameSNMP = NbBundle.getMessage(
            JMXConfigWizardIterator.class,
            "LBL_SNMP_Panel");  
    
    private final String nameOther = NbBundle.getMessage(
            JMXConfigWizardIterator.class,
            "LBL_OTHER_Panel");  
    
    /** index of the current panel */
    private int current;
    /** registered change listeners */
    private List changeListeners;   //PENDING - what is this useful for?
    /** panel for choosing name and target location of the management files */
    private FinishableDelegatedWizardPanel targetPanel;
    private Project lastSelectedProject = null;
    /** */
    private WizardDescriptor.Panel rmiPanel;
    private WizardDescriptor.Panel snmpPanel;
    private WizardDescriptor.Panel otherPanel;
    private GenericWizardPanel configPanel;

    /**
     */
    public void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList(2);
        }
        changeListeners.add(l);
    }

    /**
     */
    public void removeChangeListener(ChangeListener l) {
        if (changeListeners != null) {
            changeListeners.remove(l);
            if (changeListeners.isEmpty()) {
                changeListeners = null;
            }
        }
    }

    /**
     * Notifies all registered listeners about a change.
     *
     * @see  #addChangeListener
     * @see  #removeChangeListener
     */
    private void fireChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            Iterator i = changeListeners.iterator();
            while (i.hasNext()) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }

    /**
     */
    public boolean hasPrevious() {
        return current > INDEX_TARGET;
    }

    /**
     */
    public boolean hasNext() {
        return current < INDEX_OTHER;
    }

    /**
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        current--;
    }

    /**
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }           
        
        current++;
    }

    /**
     */
    public WizardDescriptor.Panel current() {
        switch (current) {
            case INDEX_TARGET:
                return getTargetPanel();
            case INDEX_RMI:
                return getRMIPanel();
            case INDEX_SNMP:
                return getSNMPPanel();
            case INDEX_OTHER:
                return getOtherPanel();
            default:
                throw new IllegalStateException();
        }
    }

    private FinishableDelegatedWizardPanel getTargetPanel() {
        final Project project = Templates.getProject(wizard);
        if (targetPanel == null || project != lastSelectedProject) {
            SourceGroup[] propSrcGroups = 
                    WizardHelpers.getPropSourceGroups(project);
            if (configPanel == null) {
                configPanel = new ConfigPanel.ConfigWizardPanel();
            }
            WizardDescriptor.Panel targetChooserPanel = 
                    Templates.createSimpleTargetChooser(project,
                                                        propSrcGroups,
                                                        configPanel);
            targetPanel = new WizardPanelWithoutReadSettings(
                    targetChooserPanel,configPanel);
            targetPanel.getComponent().setName(nameTarget);
            ((ConfigPanel.ConfigWizardPanel) configPanel).
                    setListenerEnabled(targetPanel,configPanel,wizard);
            lastSelectedProject = project;
            targetPanel.readAllSettings(wizard);
        }

        return targetPanel;
    }
    
    private WizardDescriptor.Panel getRMIPanel() {
        if (rmiPanel == null) {
            rmiPanel = new RMIPanel.RMIWizardPanel();
        }
        return rmiPanel;
    }
    
    private WizardDescriptor.Panel getSNMPPanel() {
        if (snmpPanel == null) {
            snmpPanel = new SNMPPanel.SNMPWizardPanel();
        }
        return snmpPanel;
    }
    
    private WizardDescriptor.Panel getOtherPanel() {
        if (otherPanel == null) {
            otherPanel = new OtherPanel.OtherWizardPanel();
        }
        return otherPanel;
    }

    /**
     */
    public String name() {
        switch (current) {
            case INDEX_TARGET:
                return nameTarget;
            case INDEX_RMI:
                return nameRMI;
            case INDEX_SNMP:
                return nameSNMP;
            case INDEX_OTHER:
                return nameOther;
            default:
                throw new AssertionError(current);
        }
    }

    private void loadSettings(TemplateWizard wizard) {
        
    }

    private void saveSettings(TemplateWizard wizard) {
        
    }

    /**
     * <!-- PENDING -->
     */
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        bundle = NbBundle.getBundle(JMXConfigWizardIterator.class);
        current = INDEX_TARGET;
        loadSettings(wiz);
        

        String [] panelNames =  new String [] {
          NbBundle.getMessage(JMXConfigWizardIterator.class,
                  "LBL_panel_chooseFileType"),
          NbBundle.getMessage(JMXConfigWizardIterator.class,
                  "LBL_panel_Target"),
          NbBundle.getMessage(JMXConfigWizardIterator.class,
                  "LBL_RMI_Panel"),
          NbBundle.getMessage(JMXConfigWizardIterator.class,
                  "LBL_SNMP_Panel"),
          NbBundle.getMessage(JMXConfigWizardIterator.class,
                  "LBL_OTHER_Panel"),
          };

        ((javax.swing.JComponent)getTargetPanel().getComponent()).
                putClientProperty("WizardPanel_contentData", panelNames); 
        ((javax.swing.JComponent)getTargetPanel().getComponent()).
                putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0)); 
        ((javax.swing.JComponent)getRMIPanel().getComponent()).
                putClientProperty("WizardPanel_contentData", panelNames); 
        ((javax.swing.JComponent)getRMIPanel().getComponent()).
                putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1)); 
        ((javax.swing.JComponent)getSNMPPanel().getComponent()).
                putClientProperty("WizardPanel_contentData", panelNames); 
        ((javax.swing.JComponent)getSNMPPanel().getComponent()).
                putClientProperty("WizardPanel_contentSelectedIndex", new Integer(2)); 
        ((javax.swing.JComponent)getOtherPanel().getComponent()).
                putClientProperty("WizardPanel_contentData", panelNames); 
        ((javax.swing.JComponent)getOtherPanel().getComponent()).
                putClientProperty("WizardPanel_contentSelectedIndex", new Integer(3)); 
        
        wiz.putProperty(WizardConstants.RMI_PORT, 
                Integer.valueOf(bundle.getString("RMI_Port_Default")));
        wiz.putProperty(WizardConstants.SNMP_PORT, 
                Integer.valueOf(bundle.getString("SNMP_Port_Default")));
        wiz.putProperty(WizardConstants.SNMP_TRAP_PORT, 
                Integer.valueOf(bundle.getString("SNMP_Trap_Port_Default")));
        wiz.putProperty(WizardConstants.SNMP_INTERFACES, 
                bundle.getString("SNMP_Interface_Default"));
        
    }

    /**
     * <!-- PENDING -->
     */
    public void uninitialize(TemplateWizard wiz) {
        this.wizard = null;
        
        targetPanel = null;
        lastSelectedProject = null;
        configPanel = null;
        rmiPanel = null;
        snmpPanel = null;
        otherPanel = null;
        
        changeListeners = null;
    }

    public Set instantiate(TemplateWizard wiz) throws IOException {
        saveSettings(wiz);
        targetPanel.storeAllSettings(wiz);
        FileObject createdFile = null;
        try {
            ConfigGenerator gen = new ConfigGenerator();

            createdFile = gen.generateConfig(wiz);
                
        } catch (Exception ex) {
            WizardHelpers.logErrorMessage(
                    "Management Configuration generation ", ex);
        }
               
        return Collections.singleton(createdFile);
    }

    /**
     * Returns a singleton of this class.
     * @return <CODE>JMXConfigWizardIterator</CODE>
     */
    public static JMXConfigWizardIterator singleton() {
        instance = new JMXConfigWizardIterator();
        return instance;
    }

}
