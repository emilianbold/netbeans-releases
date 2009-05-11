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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.configwizard;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;

import org.netbeans.modules.jmx.common.FinishableDelegatedWizardPanel;
import org.netbeans.modules.jmx.common.GenericWizardPanel;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.WizardHelpers;
import org.netbeans.modules.jmx.common.WizardPanelWithoutReadSettings;
import org.netbeans.modules.jmx.configwizard.generator.ConfigGenerator;



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

    /** name of panel &quot;Name &amp; Location&quot; */
    private final String nameTarget = NbBundle.getMessage(
            JMXConfigWizardIterator.class,
            "LBL_panel_Target");  // NOI18N
    
    private final String nameRMI = NbBundle.getMessage(
            JMXConfigWizardIterator.class,
            "LBL_RMI_Panel");  // NOI18N
    
    private final String nameSNMP = NbBundle.getMessage(
            JMXConfigWizardIterator.class,
            "LBL_SNMP_Panel");  // NOI18N
    
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
        return current < INDEX_SNMP;
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
            if(propSrcGroups != null && propSrcGroups.length == 0)
                propSrcGroups = null;
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
                  "LBL_panel_chooseFileType"),// NOI18N
          NbBundle.getMessage(JMXConfigWizardIterator.class,
                  "LBL_panel_Target"),// NOI18N
          NbBundle.getMessage(JMXConfigWizardIterator.class,
                  "LBL_RMI_Panel"),// NOI18N
          NbBundle.getMessage(JMXConfigWizardIterator.class,
                  "LBL_SNMP_Panel"),// NOI18N
          };

        ((javax.swing.JComponent)getTargetPanel().getComponent()).
                putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, panelNames); // NOI18N
        ((javax.swing.JComponent)getTargetPanel().getComponent()).
                putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(0)); // NOI18N
        ((javax.swing.JComponent)getRMIPanel().getComponent()).
                putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, panelNames); // NOI18N
        ((javax.swing.JComponent)getRMIPanel().getComponent()).
                putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(1)); // NOI18N
        ((javax.swing.JComponent)getSNMPPanel().getComponent()).
                putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, panelNames); // NOI18N
        ((javax.swing.JComponent)getSNMPPanel().getComponent()).
                putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(2)); // NOI18N
        
        wiz.putProperty(WizardConstants.RMI_PORT, 
                Integer.valueOf(NbBundle.getBundle("org.netbeans.modules.jmx.configwizard.Bundle_noi18n").getString("RMI_Port_Default")));// NOI18N
        wiz.putProperty(WizardConstants.SNMP_PORT, 
                Integer.valueOf(NbBundle.getBundle("org.netbeans.modules.jmx.configwizard.Bundle_noi18n").getString("SNMP_Port_Default")));// NOI18N
        wiz.putProperty(WizardConstants.SNMP_TRAP_PORT, 
                Integer.valueOf(NbBundle.getBundle("org.netbeans.modules.jmx.configwizard.Bundle_noi18n").getString("SNMP_Trap_Port_Default")));// NOI18N
        wiz.putProperty(WizardConstants.SNMP_INTERFACES,
                NbBundle.getBundle("org.netbeans.modules.jmx.configwizard.Bundle_noi18n").getString("SNMP_Interface_Default"));// NOI18N
        
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
                    "Management Configuration generation ", ex);// NOI18N
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
