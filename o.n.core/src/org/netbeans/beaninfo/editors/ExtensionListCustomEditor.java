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

package org.netbeans.beaninfo.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import org.openide.loaders.ExtensionList;

import org.openide.util.NbBundle;

/** A custom editor for array of Strings.
*
* @author  Ian Formanek
*/
public class ExtensionListCustomEditor extends javax.swing.JPanel {

    private ExtensionList value;
    private ExtensionListEditor editor;
    
    static final long serialVersionUID =-4347656479280614636L;
    
    @SuppressWarnings("unchecked") 
    private String[] getStrings() {
        List<String> l = new ArrayList<String> ();
        if (value == null) return new String[0];

        Enumeration<String> e = (Enumeration<String>)value.extensions ();
        while (e.hasMoreElements ()) l.add (e.nextElement ());
                
        e = (Enumeration<String>)value.mimeTypes ();
        while (e.hasMoreElements ()) l.add (e.nextElement ());
        
        return l.toArray (new String[l.size ()]);
    }

    /** Initializes the Form */
    public ExtensionListCustomEditor(ExtensionListEditor ed) {
        editor = ed;
        value = (ExtensionList)((ExtensionList)editor.getValue()).clone ();
        initComponents ();
        itemList.setCellRenderer (new EmptyStringListCellRenderer ());
        itemList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        itemList.setListData (getStrings());

        ResourceBundle bundle = NbBundle.getBundle (
                                       ExtensionListCustomEditor.class);

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ELCE"));
        itemField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ELCE_Item"));
        itemList.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ELCE_ItemList"));
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ELCE_Add"));
        changeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ELCE_Change"));
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ELCE_Remove"));

        if ( ! editor.isEditable() ) {
            // set read-only
            itemField.setEnabled( false );
            addButton.setEnabled( false );
            changeButton.setEnabled( false );
            removeButton.setEnabled( false );
        }
        updateButtons ();
        itemField.addKeyListener(new KeyAdapter() {
           public void keyReleased(KeyEvent event) {
                boolean containsCurrent = containsCurrent();
                String txt = itemField.getText().trim();
                boolean en = itemField.isEnabled() &&
                    txt.length() > 0 &&
                    !containsCurrent;
                addButton.setEnabled(en);
                changeButton.setEnabled(en && itemList.getSelectedIndex() != -1);
                if (containsCurrent) {
                    itemList.setSelectedIndex(idxOfCurrent());
                }
           }
        });
        itemField.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent ae) {
                if (addButton.isEnabled()) {
                    doAdd();
                }
            }
        }); 
        addButton.setEnabled(false);
        changeButton.setEnabled(false);
    }
    
    /** Determine if the text of the text field matches an item in the 
     * list */
    private boolean containsCurrent() {
        return idxOfCurrent() != -1;
    }
    
    private int idxOfCurrent() {
        String txt = itemField.getText().trim();
        if (txt.length() > 0) {
            int max = itemList.getModel().getSize();
            for (int i=0; i < max; i++) {
                if (txt.equals(itemList.getModel().getElementAt(i))) return i;
            }
        }
        return -1;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        addButton = new javax.swing.JButton();
        changeButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        itemListScroll = new javax.swing.JScrollPane();
        itemList = new javax.swing.JList();
        itemLabel = new javax.swing.JLabel();
        itemField = new javax.swing.JTextField();
        itemListLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(ExtensionListCustomEditor.class, "CTL_ELCE_Add", new Object[] {}));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 8);
        add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(changeButton, org.openide.util.NbBundle.getMessage(ExtensionListCustomEditor.class, "CTL_ELCE_Change", new Object[] {}));
        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        add(changeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(ExtensionListCustomEditor.class, "CTL_ELCE_Remove", new Object[] {}));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(removeButton, gridBagConstraints);

        itemList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                itemListValueChanged(evt);
            }
        });

        itemListScroll.setViewportView(itemList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(itemListScroll, gridBagConstraints);

        itemLabel.setLabelFor(itemField);
        org.openide.awt.Mnemonics.setLocalizedText(itemLabel, org.openide.util.NbBundle.getMessage(ExtensionListCustomEditor.class, "CTL_ELCE_Item", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 8);
        add(itemLabel, gridBagConstraints);

        itemField.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(itemField, gridBagConstraints);

        itemListLabel.setLabelFor(itemList);
        org.openide.awt.Mnemonics.setLocalizedText(itemListLabel, org.openide.util.NbBundle.getMessage(ExtensionListCustomEditor.class, "CTL_ItemList", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(itemListLabel, gridBagConstraints);

    }//GEN-END:initComponents

    private String addTexts() {
        StringTokenizer st = new StringTokenizer (itemField.getText (), ",. \n\t"); // NOI18N
        String last = null;
        while(st.hasMoreTokens()) {
            last = st.nextToken();
            if (last.indexOf('/') >= 0) { // mime type!?
                value.addMimeType(last);
            } else {
                value.addExtension (last);
            }
        }
        return last;
    }
    
    private void changeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = (String) itemList.getModel().getElementAt(sel);
        if (s.indexOf('/') >= 0) { // mime type!?
            value.removeMimeType(s);
        } else {
            value.removeExtension(s);
        }
        doAdd();
    }//GEN-LAST:event_changeButtonActionPerformed

    private int indexOf(String[] array, String item) {
        for (int i = 0; i<array.length; i++) {
            if (array[i].equals(item)) return i;
        }
        return -1;
    }
    
    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = (String) itemList.getModel().getElementAt(sel);
        if (s.indexOf('/') >= 0) { // mime type!?
            value.removeMimeType(s);
        } else {
            value.removeExtension(s);
        }
        itemList.setListData (getStrings());

        int count = itemList.getModel().getSize();
        // set new selection
        if (count != 0) {
            if (sel >= count) sel = count - 1;
            itemList.setSelectedIndex (sel);
        }

        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void itemListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_itemListValueChanged
        // Add your handling code here:
        updateButtons ();
        int sel = itemList.getSelectedIndex ();
        if (sel != -1) {
            itemField.setText ((String) itemList.getModel().getElementAt(sel));
            changeButton.setEnabled(false);
            addButton.setEnabled (false);
        }
    }//GEN-LAST:event_itemListValueChanged

    private void doAdd() {
        String last = addTexts();
        String[] values = getStrings();
        int index = indexOf(values, last);
        itemList.setListData (values);
        if (index >= 0) itemList.setSelectedIndex (index);
        itemList.repaint ();
        updateValue ();
    }
    
    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        doAdd();
    }//GEN-LAST:event_addButtonActionPerformed

    private void updateButtons () {
        int sel = itemList.getSelectedIndex ();
        if (sel == -1 || !editor.isEditable()) {
            removeButton.setEnabled (false);
            changeButton.setEnabled (false);
        } else {
            removeButton.setEnabled (true);
            changeButton.setEnabled (true);
        }
    }

    private void updateValue () {
        editor.setValue(value);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton changeButton;
    private javax.swing.JTextField itemField;
    private javax.swing.JLabel itemLabel;
    private javax.swing.JList itemList;
    private javax.swing.JLabel itemListLabel;
    private javax.swing.JScrollPane itemListScroll;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    static class EmptyStringListCellRenderer extends JLabel implements ListCellRenderer {

        protected static Border hasFocusBorder;
        protected static Border noFocusBorder;

        static {
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        static final long serialVersionUID =487512296465844339L;
        /** Creates a new NodeListCellRenderer */
        public EmptyStringListCellRenderer () {
            setOpaque (true);
            setBorder (noFocusBorder);
        }

        /** This is the only method defined by ListCellRenderer.  We just
        * reconfigure the Jlabel each time we're called.
        */
        public java.awt.Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
        {
            if (!(value instanceof String)) return this;
            String text = (String)value;
            if ("".equals (text)) text = NbBundle.getMessage (
                    ExtensionListCustomEditor.class, "CTL_ELCE_Empty");

            setText(text);
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
