/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.platform.wizard;

import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.event.*;
import org.netbeans.modules.java.platform.InstallerRegistry;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;
import org.netbeans.spi.java.platform.PlatformInstall;

import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;

/**
 *
 * @author  sd99038
 */
public class PlatformInstallIterator implements WizardDescriptor.InstantiatingIterator, ChangeListener {
    
    WizardDescriptor.InstantiatingIterator typeIterator;
    int                     panelIndex; // -1 - not set, 0 - the first panel, 1 - files chooser, 2 - custom panel from PlatformInstall, 3 - custom panel from CustomPlatformInstall
    boolean                 hasSelectorPanel;
    WizardDescriptor          wizard;
    int                     panelNumber = -1;

    ResourceBundle          bundle = NbBundle.getBundle(PlatformInstallIterator.class);
    LocationChooser.Panel   locationPanel = new LocationChooser.Panel();
    SelectorPanel.Panel     selectorPanel = new SelectorPanel.Panel ();
    Collection              listeners = new ArrayList();
    
    PlatformInstallIterator() {
        selectorPanel.addChangeListener(this);
        locationPanel.addChangeListener(this);        
    }
    
    public static PlatformInstallIterator create() {
        return new PlatformInstallIterator();
    }
    
    
    /**
     * Used by unit tests
     * Returns the current state of the wizard iterator
     */  
    int getPanelIndex () {
        return this.panelIndex;
    }
    
    void updatePanelsList (JComponent[] where) {
        Collection c = new LinkedList();
        if (this.hasSelectorPanel) {
            c.add (bundle.getString("TXT_SelectPlatformTypeTitle"));
        }
        if (this.panelIndex == 1 || this.panelIndex == 2 || 
            (this.panelIndex == 0 && this.selectorPanel.getInstallerIterator()==null)) {
            c.add(bundle.getString("TXT_PlatformFolderTitle")); // NOI18N
        }
        if (typeIterator != null) {
            // try to suck stuff out of the iterator's first panel :-(
            WizardDescriptor.Panel p = typeIterator.current();
            if (p != null) {
                javax.swing.JComponent pc = (javax.swing.JComponent)p.getComponent();
                String[] steps = (String[])pc.getClientProperty("WizardPanel_contentData"); // NOI18N
                if (steps != null)
                    c.addAll(Arrays.asList(steps));
            }
        } else {
            c.add(bundle.getString("TITLE_PlatformLocationUnknown")); // NOI18N
        }
        String[] names = (String[])c.toArray(new String[c.size()]);
        for (int i=0; i< where.length; i++) {
            where[i].putClientProperty("WizardPanel_contentData",names); // NOI18N
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public WizardDescriptor.Panel current() {
        if (panelIndex == 0) {
            return selectorPanel;
        }
        else if (panelIndex == 1) {
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
        if (panelIndex == 0) {
            WizardDescriptor.InstantiatingIterator typeIt = this.selectorPanel.getInstallerIterator();
            // need to decide
            if (typeIt == null) {
                return true;
            }
            else {
                return typeIt.current() != null;
            }
        }
        else if (panelIndex == 1) {
            WizardDescriptor.InstantiatingIterator typeIt = locationPanel.getInstallerIterator();
            if (typeIt == null) {
                return false;
            } else {
                WizardDescriptor.Panel p = typeIt.current();
                return p != null;
            }            
        } else {
            return this.typeIterator.hasNext();
        }
    }
    
    public boolean hasPrevious() {
        return this.panelIndex != 0 && 
             !(this.panelIndex == 1 && !hasSelectorPanel) && 
             !(this.panelIndex == 3 && !hasSelectorPanel && this.typeIterator != null && !this.typeIterator.hasPrevious());
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wizard = wiz;
        List installers = InstallerRegistry.getDefault().getAllInstallers();
        if (installers.size()>1) {
            panelIndex = 0;
            hasSelectorPanel = true;
        }
        else {
            if (installers.get(0) instanceof CustomPlatformInstall) {
                panelIndex = 3;
                hasSelectorPanel = false;
                this.typeIterator = ((CustomPlatformInstall) installers.get(0)).createIterator();
            }
            else {
                panelIndex = 1;
                hasSelectorPanel = false;
                this.locationPanel.setPlatformInstall((PlatformInstall) installers.get(0));
            }
        }            
        updatePanelsList(new JComponent[]{((JComponent)current().getComponent())});
        this.wizard.setTitle(NbBundle.getMessage(PlatformInstallIterator.class,"TXT_AddPlatformTitle"));
        panelNumber = 0;
        wizard.putProperty("WizardPanel_contentSelectedIndex", // NOI18N
            new Integer(panelNumber));
    }
    
    public java.util.Set instantiate() throws IOException {
        return typeIterator.instantiate();
    }
    
    public String name() {
        if (panelIndex == 0) {
            return bundle.getString("TXT_PlatformSelectorTitle");
        }
        else if (panelIndex == 1) {
            return bundle.getString("TXT_PlatformFolderTitle");
        } else {
            return typeIterator.name();
        }
    }
    
    public void nextPanel() {
        if (this.panelIndex == 0) {
            if (this.selectorPanel.getInstallerIterator()  == null) {
                panelIndex = 1;
            }
            else {
                panelIndex = 3;
            }
        } else if (panelIndex == 1) {
            panelIndex = 2;
        }
        panelNumber++;
        wizard.putProperty("WizardPanel_contentSelectedIndex", // NOI18N
            new Integer(panelNumber));
    }
    
    public void previousPanel() {
        if (panelIndex == 1) {
            panelIndex = 0;
        }
        else if (panelIndex == 2) {
            if (typeIterator.hasPrevious()) {
                typeIterator.previousPanel();
            } else {
                panelIndex = 1;
            }
        } else if (panelIndex == 3) {
            if (typeIterator.hasPrevious()) {
                typeIterator.previousPanel();
            } else {
                panelIndex = 0;
            }                
        } 
        panelNumber--;
        wizard.putProperty("WizardPanel_contentSelectedIndex", // NOI18N
            new Integer(panelNumber));
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        if (this.typeIterator != null)
            typeIterator.uninitialize (wiz);
    }
    
    public void stateChanged(ChangeEvent e) {
        WizardDescriptor.InstantiatingIterator it;
        if (e.getSource() == this.locationPanel) {
            it = locationPanel.getInstallerIterator();
        }
        else if (e.getSource() == this.selectorPanel) {
            GeneralPlatformInstall installer = this.selectorPanel.getInstaller();
            if (installer instanceof CustomPlatformInstall) {
                it = ((CustomPlatformInstall)installer).createIterator();
            }
            else {
                it = null;
                this.locationPanel.setPlatformInstall ((PlatformInstall)installer);
            }
        }
        else {
            assert false : "Unknown event source";  //NOI18N
            return;
        }        
        if (it != typeIterator) {
            if (this.typeIterator != null) {
                this.typeIterator.uninitialize (this.wizard);
            }
            typeIterator = it;
            if (this.typeIterator != null) {
                typeIterator.initialize (this.wizard);
                updatePanelsList(new JComponent[]{
                    (JComponent)selectorPanel.getComponent(),
                    (JComponent)locationPanel.getComponent(),
                    (JComponent)typeIterator.current().getComponent(),
                });
            }
            else {
                updatePanelsList(new JComponent[]{
                    (JComponent)selectorPanel.getComponent(),
                    (JComponent)locationPanel.getComponent()
                });
            }
            wizard.putProperty("WizardPanel_contentSelectedIndex", new Integer(panelNumber)); // NOI18N
        }
    }
}
