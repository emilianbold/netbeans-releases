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
import java.util.ArrayList;
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

/**
 *
 * @author Michal Mocnak
 */
public class InstanceWizardIterator implements WizardDescriptor.InstantiatingIterator,
        InstanceWizardConstants, ChangeListener {
    
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private int index;
    
    private InstancePropertiesPanel propertiesPanel = new InstancePropertiesPanel();
    
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {propertiesPanel};
            decoratePanels(panels);
        }
        
        return panels;
    }
    
    public Set instantiate() throws IOException {
        Set<HudsonInstance> results = new HashSet<HudsonInstance>();
        
        String name = (String) wizard.getProperty(PROP_DISPLAY_NAME);
        String url = (String) wizard.getProperty(PROP_URL);
        String sync = (String) wizard.getProperty(PROP_SYNC);
        
        if (null == name || null == url)
            return results;
        
        // Create a new hudson instance
        HudsonInstance instance = HudsonInstanceImpl.createHudsonInstance(name, url, sync);
        
        if (null != instance)
            results.add(instance);
        
        return results;
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    public void uninitialize(WizardDescriptor wizard) {}
    
    public Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return "Add a new Hudson instance wizard";
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext())
            throw new NoSuchElementException();
        
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious())
            throw new NoSuchElementException();
        
        index--;
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChangeEvent() {
        ArrayList<ChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<ChangeListener>(listeners);
        }
        
        ChangeEvent event = new ChangeEvent(this);
        
        for (ChangeListener l : tempList) {
            l.stateChanged(event);
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
    private void decoratePanels(WizardDescriptor.Panel[] panels) {
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty(PROP_CONTENT_SELECTED_INDEX, new Integer(i));  //NOI18N
                // Sets steps names for a panel
                jc.putClientProperty(PROP_CONTENT_DATA, steps);                     //NOI18N
                // Turn on subtitle creation on each step
                jc.putClientProperty(PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);         //NOI18N
                // Show steps on the left side with the image on the background
                jc.putClientProperty(PROP_CONTENT_DISPLAYED, Boolean.TRUE);         //NOI18N
                // Turn on numbering of all steps
                jc.putClientProperty(PROP_CONTENT_NUMBERED, Boolean.TRUE);          //NOI18N
            }
        }
    }
}