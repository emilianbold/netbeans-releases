/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.platform.wizard;

import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.event.*;

import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;

/**
 *
 * @author  sd99038
 */
public class PlatformInstallIterator 
    implements TemplateWizard.Iterator, ChangeListener {
    TemplateWizard.Iterator typeIterator;
    boolean                 firstPanel;
    TemplateWizard          wizard;
    int                     panelNumber = 0;
    Collection              origSteps;

    ResourceBundle          bundle = NbBundle.getBundle(PlatformInstallIterator.class);
    LocationChooser.Panel   locationPanel = new LocationChooser.Panel();
    Collection              listeners = new ArrayList();
    
    PlatformInstallIterator() {
        locationPanel.addChangeListener(this);
    }
    
    static Object create() {
        return new PlatformInstallIterator();
    }
    
    void updatePanelsList() {
        Collection c = new LinkedList();
        c.addAll(origSteps);
        c.add(bundle.getString("TITLE_PlatformLocationChooser")); // NOI18N
        if (typeIterator != null) {
            // try to suck stuff out of the iterator's first panel :-(
            WizardDescriptor.Panel p = typeIterator.current();
            if (p != null) {
                javax.swing.JComponent pc = (javax.swing.JComponent)p.getComponent();
                String[] steps = (String[])pc.getClientProperty("WizardPanel_contentData");
                if (steps != null)
                    c.addAll(Arrays.asList(steps));
            }
        } else {
            c.add(bundle.getString("TITLE_PlatformLocationUnknown")); // NOI18N
        }
        ((JComponent)current().getComponent()).putClientProperty("WizardPanel_contentData",
            c.toArray(new String[c.size()]));
        ((JComponent)current().getComponent()).putClientProperty("WizardPanel_contentSelectedIndex",
            new Integer(panelNumber));
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public WizardDescriptor.Panel current() {
        if (firstPanel) {
            return locationPanel;
        } else {
            return typeIterator.current();
        }
    }
 
    /**
     * The overall iterator has the next panel iff:
     * - the current panel is the location chooser && the chooser has an iterator
     * selected && that iterator has at least one panel
     * - the current iterator reports it has the next panel
     */
    public boolean hasNext() {
        TemplateWizard.Iterator typeIt = locationPanel.getInstaller();
        if (firstPanel) {
            // need to decide
            if (typeIt == null) {
                return false;
            } else {
                WizardDescriptor.Panel p = typeIt.current();
                return p != null;
            }
        } else {
            return typeIt.hasNext();
        }
    }
    
    public boolean hasPrevious() {
        return !firstPanel;
    }
    
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        firstPanel = true;
        String[] steps = (String[])wizard.getProperty("WizardPanel_contentData");
        if (steps != null) 
            origSteps = Arrays.asList(steps);
        else
            origSteps = Collections.EMPTY_LIST;
        updatePanelsList();
    }
    
    public java.util.Set instantiate(TemplateWizard wiz) throws IOException {
        return typeIterator.instantiate(wiz);
    }
    
    public String name() {
        if (firstPanel) {
            return bundle.getString("TITLE_PlatformLocationChooser");
        } else {
            return typeIterator.name();
        }
    }
    
    public void nextPanel() {
        if (!firstPanel) {
            typeIterator.nextPanel();
        } else {
            firstPanel = false;
        }
        panelNumber++;
        wizard.putProperty("WizardPanel_contentSelectedIndex", 
            new Integer(panelNumber));
        updatePanelsList();
    }
    
    public void previousPanel() {
        if (!firstPanel) {
            if (typeIterator.hasPrevious()) {
                typeIterator.previousPanel();
            } else {
                firstPanel = true;
            }
        } 
        panelNumber--;
        wizard.putProperty("WizardPanel_contentSelectedIndex", 
            new Integer(panelNumber));
        updatePanelsList();
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    public void uninitialize(TemplateWizard wiz) {
        if (this.typeIterator != null)
            typeIterator.uninitialize (wiz);
    }
    
    public void stateChanged(ChangeEvent e) {
        TemplateWizard.Iterator it = locationPanel.getInstaller();
        if (it != typeIterator) {
            if (this.typeIterator != null) {
                this.typeIterator.uninitialize (this.wizard);
            }
            typeIterator = it;
            if (this.typeIterator != null) {
                typeIterator.initialize (this.wizard);
            }
            updatePanelsList();
        }
    }
}
