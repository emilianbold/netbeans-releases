/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.palette;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.Component;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.File;
import java.net.URL;

import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.netbeans.api.project.libraries.*;

/** 
 * The first panel in the wizard for adding new components to the palette from
 * a library. In this panel the user chooses a library from available libraries
 * installed in the IDE.
 *
 * @author Tomas Pavek
 */

class ChooseLibraryWizardPanel implements WizardDescriptor.Panel {

    private LibrarySelector librarySelector;

//    private AddToPaletteWizard wizard;

    private EventListenerList listenerList;

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (librarySelector == null) {
            librarySelector = new LibrarySelector();

            // wizard API: set the caption and index of this panel
            librarySelector.setName(
                PaletteUtils.getBundleString("CTL_SelectLibrary_Caption")); // NOI18N
            librarySelector.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                              new Integer(0));

            librarySelector.list.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        fireStateChanged();
                    }
                });

//            librarySelector.list.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent ev) {
//                    wizard.stepToNext();
//                }
//            });
        }

        return librarySelector;
    }

    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        return librarySelector != null
               && librarySelector.getSelectedLibraries() != null;
    }

    public void readSettings(Object settings) {
//        wizard = (AddToPaletteWizard) settings;
    }

    public void storeSettings(Object settings) {
        if (librarySelector != null) { // create the UI component for the wizard step
            Library[] libraries = librarySelector.getSelectedLibraries();

            // collect the roots making up the classpath of the libraries
            // (presumably JAR files)

            Map fileMap = new HashMap(); // to avoid duplicities in case some JAR file is in more libraries
            Map libraryMap = new HashMap(); // to remember libraries for JAR files

            for (int i=0; i < libraries.length; i++) {
                List content = libraries[i].getContent("classpath"); // NOI18N
                // go through classpath roots of the library
                for (Iterator it=content.iterator(); it.hasNext(); ) {
                    URL rootURL = (URL) it.next();
                    if ("jar".equals(rootURL.getProtocol())) { // NOI18N
                        String path = rootURL.getPath();
                        int index = path.lastIndexOf('!');
                        if (index != -1) {
                            try {
                                rootURL = new URL(path.substring(0, index));
                            } catch (java.net.MalformedURLException mex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mex);
                                continue;
                            }
                        }
                    }
                    File rootFile = FileUtil.toFile(URLMapper.findFileObject(rootURL));
                    String rootPath = rootFile.getAbsolutePath();
                    fileMap.put(rootPath, rootFile);
                    libraryMap.put(rootPath, libraries[i].getName());
                }
            }

            File[] libFiles = new File[fileMap.size()];
            fileMap.values().toArray(libFiles);

            AddToPaletteWizard wizard = (AddToPaletteWizard) settings;
            wizard.setJARFiles(libFiles);
            wizard.libraryNameMap = libraryMap;
        }
    }

    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    // -------

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i=listeners.length-2; i >= 0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }

    // ---------

    static class LibrarySelector extends JPanel {

        JList list;
        List libList;

        LibrarySelector() {
            list = new JList();
            list.setCellRenderer(new LibraryRenderer());
            list.setLayoutOrientation(JList.VERTICAL_WRAP);
            list.setVisibleRowCount(0);
            updateLibraryList();

            setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints;

            JLabel label1 = new JLabel();
            org.openide.awt.Mnemonics.setLocalizedText(
                label1, PaletteUtils.getBundleString("CTL_Libraries")); // NOI18N
            label1.setLabelFor(list);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            add(label1, gridBagConstraints);

//            JButton button1 = new JButton();
//            org.openide.awt.Mnemonics.setLocalizedText(
//                button1, PaletteUtils.getBundleString("CTL_LibrariesManager")); // NOI18N
//            gridBagConstraints = new java.awt.GridBagConstraints();
//            gridBagConstraints.gridx = 2;
//            gridBagConstraints.gridy = 2;
//            gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
//            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
//            add(button1, gridBagConstraints);

            JScrollPane scrollpane1 = new javax.swing.JScrollPane();
            scrollpane1.setViewportView(list);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(scrollpane1, gridBagConstraints);

//            button1.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent ev) {
//                    Library lib = null;
//                    Object[] selected = list.getSelectedValues();
//                    if (selected.length > 0)
//                        lib = (Library) selected[0];
//                    if (LibrariesCustomizer.showCustomizer(lib))
//                        updateLibraryList();
//                }
//            });
        }

        Library[] getSelectedLibraries() {
            Object[] selected = list.getSelectedValues();
            Library[] libraries = new Library[selected.length];
            for (int i=0; i < selected.length; i++)
                libraries[i] = (Library) selected[i];
            return libraries;
        }

        void updateLibraryList() {
            Library[] libraries = LibraryManager.getDefault().getLibraries();
            libList = new ArrayList(libraries.length);
            for (int i=0; i < libraries.length; i++)
                if (libraries[i].getType().equals("j2se")) // NOI18N
                    libList.add(libraries[i]);

            list.setModel(new AbstractListModel() {
                public int getSize() { return libList.size(); }
                public Object getElementAt(int i) { return libList.get(i); }
            });
        }

        public void addNotify() {
            super.addNotify();
            list.requestFocus();
        }

        public java.awt.Dimension getPreferredSize() {
            return new java.awt.Dimension(400, 300);
        }
    }

    private static class LibraryRenderer extends JLabel
                                         implements ListCellRenderer
    {
        private static final Border hasFocusBorder =
            new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
        private static final Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        public LibraryRenderer() {
            setOpaque(true);
            setBorder(noFocusBorder);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            Library lib = (Library) value;
            setText(lib.getDisplayName());

            if (isSelected){
                setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);
            return this;
        }
    }
}
