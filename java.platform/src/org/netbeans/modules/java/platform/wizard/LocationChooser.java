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

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.io.File;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.java.platform.InstallerRegistry;
import org.netbeans.spi.java.platform.PlatformInstall;

/**
 *
 * @author  sd99038, Tomas  Zezula
 */
public class LocationChooser extends javax.swing.JFileChooser  implements PropertyChangeListener {

    private TemplateWizard.Iterator iterator;
    private LocationChooser.Panel firer;
    private InstallerRegistry regs;
    private PlatformAccessory accessory;

    public LocationChooser (LocationChooser.Panel firer) {
        super ();
        this.setFileSelectionMode(DIRECTORIES_ONLY);
        this.setMultiSelectionEnabled(false);
        this.setControlButtonsAreShown(false);
        this.accessory = new PlatformAccessory ();
        this.setFileFilter (new PlatformFileFilter());
        this.setAccessory (this.accessory);
        this.firer = firer;
        this.regs = InstallerRegistry.getDefault();
        this.addPropertyChangeListener (this);
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
            this.iterator = null;
            this.accessory.setType ("");    //NOI18N
            File file = this.getSelectedFile();
            if (file != null) {
                FileObject fo = FileUtil.toFileObject (file);
                if (fo != null) {
                    for (Iterator it = this.regs.getInstallers().iterator(); it.hasNext();) {
                        PlatformInstall install = (PlatformInstall) it.next ();
                        if (install.accept(fo)) {
                            this.accessory.setType (install.getDisplayName());
                            this.iterator = install.createIterator(fo);
                            break;
                        }
                    }
                }
            }
            this.firer.fireStateChanged();
        }
    }


    private boolean valid () {
        return this.getInstaller() != null;
    }

    private void read (WizardDescriptor settings) {

    }

    private void store (WizardDescriptor settings) {

    }

    private TemplateWizard.Iterator getInstaller () {
        return this.iterator;
    }

    private static class PlatformFileFilter extends FileFilter {

        public boolean accept(File f) {
            return f.isDirectory();
        }

        public String getDescription() {
            return NbBundle.getMessage (LocationChooser.class,"TXT_PlatformFolder");
        }
    }

    private static class PlatformAccessory extends JComponent {

        private JTextField tf;

        public PlatformAccessory () {
            this.initComponents ();
        }

        private void setType (String type) {
            this.tf.setText(type);
        }

        private void initComponents () {
            GridBagLayout l = new GridBagLayout();
            this.setLayout (l);
            JLabel label = new JLabel (NbBundle.getMessage(LocationChooser.class,"TXT_PlatformType"));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (0,12,3,12);
            c.anchor = GridBagConstraints.NORTHWEST;
            l.setConstraints(label,c);
            this.add (label);
            this.tf = new JTextField();
            this.tf.setEditable(false);
            this.tf.setColumns(15);
            c = new GridBagConstraints();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (3,12,12,12);
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            l.setConstraints(this.tf,c);
            this.add (tf);
            JPanel fill = new JPanel ();
            c = new GridBagConstraints();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (0,12,12,12);
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = c.weighty = 1.0;
            l.setConstraints(fill,c);
            this.add (fill);
        }

    }


    /**
     * Controller for the LocationChooser panel.
     */
    public static class Panel implements WizardDescriptor.Panel {
            
        LocationChooser             component;
        Collection                  listeners = new ArrayList();



        public java.awt.Component getComponent() {
            if (component == null) {
                this.component = new LocationChooser (this);
            }
            return component;
        }
        
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        public boolean isValid() {
            return ((LocationChooser)this.getComponent()).valid();
        }
        
        public void readSettings(Object settings) {
            ((LocationChooser)this.getComponent()).read ((TemplateWizard) settings);
        }
        
        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        public void storeSettings(Object settings) {
            ((LocationChooser)this.getComponent()).store((TemplateWizard)settings);
        }
        
        /**
         * Returns the currently selected installer.
         */
        TemplateWizard.Iterator getInstaller() {
            return ((LocationChooser)this.getComponent()).getInstaller ();
        }
        
        void fireStateChanged() {
            ChangeListener[] ll;
            synchronized (this) {
                if (listeners.isEmpty())
                    return;
                ll = (ChangeListener[])listeners.toArray(new ChangeListener[0]);
            }
            ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < ll.length; i++)
                ll[i].stateChanged(ev);
        }

    }
}
