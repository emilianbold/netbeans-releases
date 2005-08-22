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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.platform.InstallerRegistry;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Tomas Zezula
 */
class SelectorPanel extends javax.swing.JPanel implements ItemListener {
        
    private Map installersByButtonModels = new IdentityHashMap ();
    private ButtonGroup group;
    private SelectorPanel.Panel firer;
    
    /** Creates new form SelectorPanel */
    public SelectorPanel(SelectorPanel.Panel firer) {
        this.firer = firer;
        initComponents();
        postInitComponents ();
        this.setName (NbBundle.getMessage(SelectorPanel.class,"TXT_SelectPlatformTypeTitle"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectorPanel.class,"AD_SelectPlatformType"));
    }
    
    private void postInitComponents () {
        InstallerRegistry regs = InstallerRegistry.getDefault();
        List/*<GeneralPlatformInstall>*/ installers =  regs.getAllInstallers();
        this.group = new ButtonGroup ();        
        JLabel label = new JLabel (NbBundle.getMessage(SelectorPanel.class,"TXT_SelectPlatform"));
        label.setDisplayedMnemonic(NbBundle.getMessage(SelectorPanel.class,"AD_SelectPlatform").charAt(0));        
        GridBagConstraints c = new GridBagConstraints ();
        c.gridx = c.gridy = GridBagConstraints.RELATIVE;
        c.gridheight = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.insets = new Insets (12, 12, 6, 12);
        ((GridBagLayout)this.getLayout()).setConstraints(label,c);
        this.add (label);
        Iterator/*<GeneralPlatformInstall>*/ it = installers.iterator();
        for (int i=0; it.hasNext(); i++) {
            GeneralPlatformInstall pi = (GeneralPlatformInstall) it.next ();            
            JRadioButton button = new JRadioButton (pi.getDisplayName());
            if (i==0) {
                label.setLabelFor(button);
            }
            button.addItemListener(this);
            this.installersByButtonModels.put (button.getModel(), pi);
            this.group.add(button);
            c = new GridBagConstraints ();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridheight = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.insets = new Insets (6, 18, it.hasNext()? 0 : 12, 12);
            ((GridBagLayout)this.getLayout()).setConstraints(button,c);
            this.add (button);
        }
        JPanel pad = new JPanel ();
        c = new GridBagConstraints ();
        c.gridx = c.gridy = GridBagConstraints.RELATIVE;
        c.gridheight = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets (12,0,0,12);
        ((GridBagLayout)this.getLayout()).setConstraints(pad,c);
        this.add (pad);
    }
    
    private void readSettings () {
        if (this.group.getSelection()==null) {
            java.util.Enumeration buttonEnum = this.group.getElements();
            assert buttonEnum.hasMoreElements();
            ((JRadioButton)buttonEnum.nextElement()).setSelected(true);
        }
    }

    public void itemStateChanged(java.awt.event.ItemEvent e) {
        this.firer.fireChange();
    }
    
    
    /**
     * Used by unit tests
     * Select the GeneralPlatformInstall to allow step over this panel
     */
    boolean selectInstaller (GeneralPlatformInstall install) {
        assert install != null;
        for (Iterator/*<Map.Entry<ButtonModel, GeneralPlatformInstall>>*/ it = this.installersByButtonModels.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue().equals(install)) {
                ButtonModel model = (ButtonModel) entry.getKey();
                model.setSelected(true);
                return true;
            }
        }
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    
    public static class Panel implements WizardDescriptor.Panel {
        
        private List/*<ChangeListener>*/ listeners;
        private SelectorPanel component;
        
        public synchronized void removeChangeListener(ChangeListener l) {
            assert l != null;
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(l);
        }

        public synchronized void addChangeListener(ChangeListener l) {
            assert l != null;
            if (this.listeners == null) {
                this.listeners = new ArrayList ();
            }
            this.listeners.add(l);
        }                

        public void readSettings(Object settings) {
            ((SelectorPanel)this.getComponent()).readSettings();
        }

        public void storeSettings(Object settings) {
        }

        public HelpCtx getHelp() {
            return new HelpCtx (SelectorPanel.class);
        }

        public boolean isValid() {
            return this.component != null;
        }

        public java.awt.Component getComponent() {
            if (this.component == null) {
                this.component = new SelectorPanel (this);
            }
            return this.component;
        }
        
        public GeneralPlatformInstall getInstaller () {
            SelectorPanel c = (SelectorPanel) getComponent ();
            ButtonModel bm = c.group.getSelection();
            if (bm != null) {            
                return (GeneralPlatformInstall) c.installersByButtonModels.get (bm);
            }
            return null;
        }
        
        public TemplateWizard.InstantiatingIterator getInstallerIterator () {
            GeneralPlatformInstall platformInstall = getInstaller ();
            if (platformInstall instanceof CustomPlatformInstall) {
                return ((CustomPlatformInstall)platformInstall).createIterator();
            }
            return null;
        }
        
        void fireChange () {
            ChangeListener[] _listeners;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                _listeners = (ChangeListener[]) this.listeners.toArray(new ChangeListener[this.listeners.size()]);
            }
            ChangeEvent event = new ChangeEvent (this);
            for (int i=0; i<_listeners.length; i++) {
                _listeners[i].stateChanged(event);
            }
        }        
    }
}
