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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.File;
import java.util.*;
import java.util.jar.*;
import javax.swing.event.*;

import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
//import org.openide.filesystems.*;

class ChooseBeansWizardPanel implements WizardDescriptor.Panel {

    private File[] currentFiles;
    private java.util.List allBeans;
    private BeanSelector beanSelector;

    private EventListenerList listenerList;

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (beanSelector == null) {
            beanSelector = new BeanSelector();

            // wizard API: set the caption and index of this panel
            beanSelector.setName(PaletteUtils.getBundleString("CTL_SelectBeans_Caption")); // NOI18N
            beanSelector.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                           new Integer(1));
            if (allBeans != null)
                beanSelector.setBeans(allBeans);

            beanSelector.list.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        fireStateChanged();
                    }
                });
        }

        return beanSelector;
    }

    public org.openide.util.HelpCtx getHelp() {
        // TBD
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        return beanSelector != null && beanSelector.getSelectedBeans().size() > 0;
    }

    public void readSettings(Object settings) {
        File[] jarFiles = ((AddToPaletteWizard)settings).getJARFiles();

        if (currentFiles != null && currentFiles.length == jarFiles.length)
            for (int i=0; i < jarFiles.length; i++)
                if (jarFiles[i].equals(currentFiles[i])) {
                    if (i+1 == jarFiles.length)
                        return; // no change from the last time
                }
                else break;

        currentFiles = jarFiles;

//        JarFileSystem[] jars = new JarFileSystem[jarFiles.length];
//        for (int i=0; i < jarFiles.length; i++) {
//            jars[i] = new JarFileSystem();
//            try {
//                jars[i].setJarFile(jarFiles[i]);
//            }
//            catch (Exception ex) { // should not happen
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            }
//        }

        allBeans = findJavaBeans(jarFiles);
        if (beanSelector != null)
            beanSelector.setBeans(allBeans);
    }

    public void storeSettings(Object settings) {
        if (beanSelector != null) {
            java.util.List beans = beanSelector.getSelectedBeans();
            String[] classes = new String[beans.size()];
            beans.toArray(classes);
            ((AddToPaletteWizard)settings).setSelectedClasses(classes);
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

    // -----

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

    private static java.util.List findJavaBeans(File[] jarFiles) {
        ArrayList beans = new ArrayList(100);

        for (int j=0; j < jarFiles.length; j++) {
            Manifest manifest;
            try {
                manifest = new JarFile(jarFiles[j]).getManifest();
            }
            catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                continue;
            }

            Map entries = manifest.getEntries();
            Iterator it = entries.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (!key.endsWith(".class"))
                    continue;

                String value = ((Attributes)entries.get(key)).getValue("Java-Bean"); // NOI18N
                if (!"True".equalsIgnoreCase(value)) // NOI18N
                    continue;

                String classname =  key.substring(0, key.length()-6) // cut off ".class"
                                    .replace('\\', '/').replace('/', '.');
                if (classname.startsWith(".")) // NOI18N
                    classname = classname.substring(1);
                beans.add(classname);
            }
        }

        return beans;
    }

    // --------

    static class BeanSelector extends JPanel {

        private JList list;

        BeanSelector() {
            list = new JList();
//            list.setLayoutOrientation(JList.VERTICAL_WRAP);
//            list.setVisibleRowCount(0);
            list.setCellRenderer(new BeanClassNameRenderer());

            setBorder(new EmptyBorder(12, 12, 0, 11));
            setLayout(new java.awt.BorderLayout(0, 2));
            JLabel label = new JLabel(PaletteUtils.getBundleString("CTL_SelectBeans")); // NOI18N
            label.setLabelFor(list);
            label.setDisplayedMnemonic(PaletteUtils.getBundleString("CTL_SelectBeans_Mnemonic").charAt(0));
            list.getAccessibleContext().setAccessibleDescription(PaletteUtils.getBundleString("ACSD_CTL_SelectBeans"));
            getAccessibleContext().setAccessibleDescription(PaletteUtils.getBundleString("ACSD_SelectBeansDialog"));
            add(label, java.awt.BorderLayout.NORTH);
            add(new JScrollPane(list), java.awt.BorderLayout.CENTER); // NOI18N
        }

        void setBeans(final java.util.List beans) {
            list.setModel(new AbstractListModel() {
                public int getSize() { return beans.size(); }
                public Object getElementAt(int i) { return beans.get(i); }
            });
        }

        java.util.List getSelectedBeans() {
            Object[] sel = list.getSelectedValues();
            ArrayList al = new ArrayList(sel.length);
            for (int i = 0; i < sel.length; i++)
                al.add(sel[i]);
            return al;
        }

//        public java.awt.Dimension getMinimumSize() {
//            return getPreferredSize();
//        }
//
//        public java.awt.Dimension getMaximumSize() {
//            return getPreferredSize();
//        }
//
//        public java.awt.Dimension getPreferredSize() {
//            java.awt.Dimension ret = super.getPreferredSize();
//            ret.width = Math.max(ret.width, 350);
//            ret.height = Math.max(ret.height, 250);
//            return ret;
//        }
    }

    private static class BeanClassNameRenderer extends JLabel
                                               implements ListCellRenderer
    {
        private static final Border hasFocusBorder =
            new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public BeanClassNameRenderer() {
            setOpaque(true);
            setBorder(noFocusBorder);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            String name = (String) value;
            int i = name.lastIndexOf('.');
            if (i >= 0)
                name = name.substring(i+1);

            setText(name);

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
