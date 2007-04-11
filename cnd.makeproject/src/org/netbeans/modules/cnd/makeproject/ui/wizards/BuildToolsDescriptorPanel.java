/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.ui.options.LocalToolsPanelModel;
import org.netbeans.modules.cnd.ui.options.ToolsPanel;
import org.netbeans.modules.cnd.ui.options.ToolsPanelModel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author gordonp
 */
public class BuildToolsDescriptorPanel implements WizardDescriptor.Panel,
        NewMakeProjectWizardIterator.Name, ChangeListener, PropertyChangeListener {
    
    private WizardDescriptor wizardDescriptor;
    private ToolsPanel component;
    private String name;
    private boolean initialized = false;
    
    private boolean valid = false;
    
    /** Creates a new instance of BuildToolsDescriptorPanel */
    public BuildToolsDescriptorPanel() {
	name = NbBundle.getMessage(BuildToolsDescriptorPanel.class, "LBL_BuildToolsName"); // NOI18N
    }
    
    public Component getComponent() {
        if (component == null) {
            ToolsPanelModel model = new LocalToolsPanelModel();
            model.setGdbEnabled(false);
            component = new ToolsPanel(model);
            component.addPropertyChangeListener(this);
	    component.setName(name);
            component.update();
        }
        return component;
    }

    public String getName() {
	return name;
    }

    public WizardDescriptor getWizardDescriptor() {
	return wizardDescriptor;
    } 

    public HelpCtx getHelp() {
        return new HelpCtx("NewMakeWizardP2"); // NOI18N
    }
    
    public boolean isValid() {
	return valid;
    }
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
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
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public void stateChanged(ChangeEvent e) {
	fireChangeEvent();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getSource() instanceof ToolsPanel && ev.getPropertyName().equals(ToolsPanel.PROP_VALID)) {
            valid = ((Boolean) ev.getNewValue()).booleanValue();
            fireChangeEvent();
        }
    }
    
    public void readSettings(Object settings) {
        if (initialized)
            return;
        wizardDescriptor = (WizardDescriptor)settings;
        initialized = true;
    }
    
    public void storeSettings(Object settings) {
        component.applyChanges(true);
    }
    
}
