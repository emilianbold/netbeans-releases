/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.libraries.ui;


import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import javax.swing.event.*;
import javax.swing.*;
import java.beans.Customizer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 *
 * @author  tom
 */
public final class LibrariesCustomizer extends javax.swing.JPanel {
    

    /** Creates new form LibrariesCustomizer */
    public LibrariesCustomizer () {
        initComponents();
        postInitComponents ();
    }


    public void setSelectedLibrary (LibraryImplementation library) {
        if (library == null)
            return;
        ListModel model = this.libraries.getModel();
        for (int i=0; i< model.getSize(); i++) {
            LibraryImplementation tmp = (LibraryImplementation) model.getElementAt(i);
            if (tmp != null && library.getName().equals(tmp.getName())) {
                this.libraries.setSelectedIndex(i);
                break;
            }
        }
    }

    public boolean apply () {
        try {
            ((LibrariesModel)this.libraries.getModel()).apply();
            return true;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return false;
        }
    }

    public void cancel () {
        ((LibrariesModel)this.libraries.getModel()).cancel();
    }

    public void addNotify() {
        super.addNotify();
        this.libraries.requestFocus();
    }


    private void postInitComponents () {
        LibrariesModel model = new LibrariesModel ();
        this.libraries.setModel(model);
        this.libraries.setCellRenderer(new LibraryRenderer());
        this.libraryName.setColumns(25);
        this.libraryName.setEnabled(false);
        this.libraryName.addActionListener(
                new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        nameChanged();
                    }
                });
        this.libraries.addListSelectionListener(
                new ListSelectionListener () {
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting())
                            return;
                        selectLibrary(libraries.getSelectedIndex());
                        libraries.requestFocus();
                    }
                }
        );
        if (model.getSize()>0) {
            this.libraries.setSelectedIndex (0);
        }
    }


    private void nameChanged () {
        int index = LibrariesCustomizer.this.libraries.getSelectedIndex();
        if (index >= 0) {
            LibrariesModel model = (LibrariesModel) this.libraries.getModel();
            String newName = this.libraryName.getText();
            if (newName.length () == 0) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        NbBundle.getMessage(LibrariesCustomizer.class, "ERR_InvalidName"),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
            else if (isValidName (model, newName)) {
                ((LibraryImplementation)model.getElementAt(index)).setName(newName);
            }
            else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        MessageFormat.format(NbBundle.getMessage(LibrariesCustomizer.class, "ERR_ExistingName"),
                                new Object[] {newName}),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }


    private void selectLibrary (int index) {
        int tabCount = this.properties.getTabCount();
        for (int i=0; i<tabCount; i++) {
            this.properties.removeTabAt(0);
        }
        if (index < 0) {
            this.libraryName.setEnabled(false);
            this.deleteButton.setEnabled(false);
            return;
        }
        LibrariesModel model = (LibrariesModel) this.libraries.getModel();
        LibraryImplementation impl = (LibraryImplementation) model.getElementAt(index);
        boolean editable = model.isLibraryEditable (impl);
        this.libraryName.setEnabled(editable);
        this.deleteButton.setEnabled(editable);
        this.libraryName.setText (getLocalizedString(impl.getLocalizingBundle(),impl.getName()));
        String libraryType = impl.getType();
        LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (libraryType);
        if (provider == null)
            return;
        String[] volumeTypes = provider.getSupportedVolumeTypes();
        for (int i=0; i< volumeTypes.length; i++) {
            Customizer c = provider.getCustomizer (volumeTypes[i]);
            if (c instanceof JComponent) {
                c.setObject (impl);
                JComponent component = (JComponent) c;
                component.setEnabled (editable);
                String tabName = component.getName();
                if (tabName == null) {
                    tabName = volumeTypes[i];
                }
                this.properties.addTab(tabName, component);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        libraries = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        libraryName = new javax.swing.JTextField();
        properties = new javax.swing.JTabbedPane();
        createButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(libraries);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 6);
        add(jScrollPane1, gridBagConstraints);

        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("CTL_LibraryName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 2, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel1, gridBagConstraints);

        libraryName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 2, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 2.0;
        add(libraryName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 12, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 4.0;
        gridBagConstraints.weighty = 1.0;
        add(properties, gridBagConstraints);

        createButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("CTL_NewLibrary"));
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLibrary(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 6);
        add(createButton, gridBagConstraints);

        deleteButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("CTL_DeleteLibrary"));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLibrary(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 6);
        add(deleteButton, gridBagConstraints);

    }//GEN-END:initComponents

    private void deleteLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLibrary
        LibrariesModel model = (LibrariesModel)this.libraries.getModel();
        int index = this.libraries.getSelectedIndex();
        model.removeLibrary ((LibraryImplementation)model.getElementAt(index));
        if (index < model.getSize()) {
            this.libraries.setSelectedIndex (index);
        }
        else if (index >= 1) {
            this.libraries.setSelectedIndex (index-1);
        }
        this.libraries.requestFocus();
    }//GEN-LAST:event_deleteLibrary

    private void createLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLibrary
        Dialog dlg = null;
        try {
            Object[] options = new Object[]{
                new JButton (NbBundle.getMessage(LibrariesCustomizer.class,"CTL_Ok")),
                new JButton (NbBundle.getMessage(LibrariesCustomizer.class,"CTL_Cancel"))
            };

            NewLibraryPanel p = new NewLibraryPanel ((LibrariesModel)this.libraries.getModel(),(JButton)options[0]);
            DialogDescriptor dd = new DialogDescriptor (p, NbBundle.getMessage(LibrariesCustomizer.class,"CTL_CreateLibrary"),
                    true, options,options[0],DialogDescriptor.DEFAULT_ALIGN,null,null);
            dlg = DialogDisplayer.getDefault().createDialog (dd);
            dlg.setVisible(true);
            if (dd.getValue() == options[0]) {
                String libraryType = p.getLibraryType();
                String libraryName = p.getLibraryName();
                LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (libraryType);
                if (provider == null) {
                    return;
                }
                LibraryImplementation impl = provider.createLibrary();
                impl.setName (libraryName);
                LibrariesModel model = (LibrariesModel)this.libraries.getModel();
                model.addLibrary (impl);
                int index=0;
                for (int i=0; i<model.getSize(); i++) {
                    LibraryImplementation lib = (LibraryImplementation) model.getElementAt(i);
                    if (impl.equals(lib)) {
                        index=i;
                        break;
                    }
                }
                this.libraries.setSelectedIndex (index);
                this.libraryName.requestFocus();
                this.libraryName.selectAll();
            }
            else {
                this.libraries.requestFocus();
            }
        }
        finally {
            if (dlg != null)
                dlg.dispose();
        }
    }//GEN-LAST:event_createLibrary


    static boolean isValidName (LibrariesModel model, String name) {
        int count = model.getSize();
        for (int i=0; i<count; i++) {
            LibraryImplementation lib = (LibraryImplementation) model.getElementAt (i);
            if (lib != null && lib.getName().equals(name))
                return false;
        }
        return true;
    }


    static String getLocalizedString (String bundleResourceName, String key) {
        if (key == null)
            return null;
        if (bundleResourceName == null)
            return key;
        try {
            ResourceBundle bundle = NbBundle.getBundle (bundleResourceName);
            return bundle.getString (key);
        } catch (MissingResourceException mre) {
            return key;
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList libraries;
    private javax.swing.JTextField libraryName;
    private javax.swing.JTabbedPane properties;
    // End of variables declaration//GEN-END:variables
    
}
