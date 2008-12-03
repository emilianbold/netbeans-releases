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

package org.netbeans.modules.hudson.ui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Michal Mocnak
 */
public class InstanceWizardIterator implements WizardDescriptor.InstantiatingIterator<InstanceWizard>,
         ChangeListener {
    
    private final InstanceWizard wizard;

    public InstanceWizardIterator(InstanceWizard wizard) {
        this.wizard = wizard;
    }
    
    private InstancePropertiesPanel propertiesPanel = new InstancePropertiesPanel();
    {
        decoratePanels(Collections.<WizardDescriptor.Panel<?>>singletonList(propertiesPanel));
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    
    public Set instantiate() throws IOException {
        Set<HudsonInstance> results = new HashSet<HudsonInstance>();
        
        if (null == wizard.name || null == wizard.url)
            return results;
        
        // Create a new hudson instance
        HudsonInstance instance = HudsonInstanceImpl.createHudsonInstance(wizard.name, wizard.url, wizard.sync);
        
        if (null != instance)
            results.add(instance);
        
        return results;
    }
    
    public void initialize(WizardDescriptor wizard) {}
    
    public void uninitialize(WizardDescriptor wizard) {}
    
    public Panel<InstanceWizard> current() {
        return propertiesPanel;
    }
    
    public String name() {
        return "Add a new Hudson instance wizard";
    }
    
    public boolean hasNext() {
        return false;
    }
    
    public boolean hasPrevious() {
        return false;
    }
    
    public void nextPanel() {
        throw new NoSuchElementException();
    }
    
    public void previousPanel() {
        throw new NoSuchElementException();
    }
    
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    public void stateChanged(ChangeEvent e) {
        cs.fireChange();
    }
    
    private void decoratePanels(List<WizardDescriptor.Panel<?>> panels) {
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i);  //NOI18N
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps);                     //NOI18N
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", true);         //NOI18N
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", true);         //NOI18N
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", true);          //NOI18N
            }
        }
    }
}