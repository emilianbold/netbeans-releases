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

package org.netbeans.modules.j2ee.common.method.impl;

import org.netbeans.modules.j2ee.common.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.openide.util.NbBundle;

/**
 *
 * ReturnTypeUIHelper populates and manages the content of the combobox for a return type of method
 * 
 * @see DatasourceUIHelper
 *
 * @author Martin Adamek
 */
public final class ReturnTypeUIHelper {

    private static final List<String> TEMP_TYPES = Arrays.asList(new String[] { "int", "String", "Object" });
    
    private static final class Separator extends JSeparator {
        Separator() {
            setPreferredSize(new Dimension(getWidth(), 1));
            setForeground(Color.BLACK);
        }
    }
    
    static final Separator SEPARATOR_ITEM = new Separator();
    static final Object NEW_ITEM = new Object() {
        public String toString() {
            return NbBundle.getMessage(ReturnTypeUIHelper.class, "LBL_Choose"); // NOI18N
        }
    };
    
    private static class ReturnTypeComboBoxModel extends AbstractListModel implements MutableComboBoxModel {
        
        private List<Object> items;
        private Object selectedItem;
        private List<String> returnTypes;
        private Object previousItem;
        
        private ReturnTypeComboBoxModel(List<String> returnTypes, List<Object> items) {
            this.returnTypes = returnTypes;
            this.items = items;
        }

        public void setSelectedItem(Object anItem) {
            
            if (selectedItem == null || !selectedItem.equals(anItem)) {
                        
                previousItem = selectedItem;
                selectedItem = anItem;

                fireContentsChanged(this, 0, -1);
            }
        }

        public Object getSelectedItem() {
            return selectedItem;
        }
        
        public Object getElementAt(int index) {
            return items.get(index);
        }
        
        public int getSize() {
            return items.size();
        }
        
        Object getPreviousItem() {
            return previousItem;
        }
        
        List<String> getReturnTypes() {
            return returnTypes;
        }
        
        public void addElement(Object elem) {
           items.add(elem);
        }

        public void removeElement(Object elem) {
            items.remove(elem);
        }

        public void insertElementAt(Object elem, int index) {
            items.set(index, elem);
        }

        public void removeElementAt(int index) {
            items.remove(index);
        }
        
    }
    
    /**
     * Get data source list cell renderer.
     * @return data source list cell renderer instance.
     * @since 1.16
     */
    public static ListCellRenderer createDatasourceListCellRenderer() {
        return new ReturnTypeListCellRenderer();
    }
    
    private static class ReturnTypeListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value == SEPARATOR_ITEM) {
                return SEPARATOR_ITEM;
            }
            else {
                setText(value != null ? value.toString() : ""); // NOI18N
                setToolTipText(""); // NOI18N
            }
            
            return this;
        }

    }
    
    private static class ReturnTypeComboBoxEditor implements ComboBoxEditor {
        
        private ComboBoxEditor delegate;
        private Object oldValue;
        
        ReturnTypeComboBoxEditor(ComboBoxEditor delegate) {
            this.delegate = delegate;
        }

        public Component getEditorComponent() {
            return delegate.getEditorComponent();
        }

        public void setItem(Object anObject) {
            
            JTextComponent editor = getEditor();
            
            if (anObject != null)  {
                String text = anObject.toString();
                editor.setText(text);

                oldValue = anObject;
            } 
            else {
                editor.setText("");
            }
        }

        // this method is taken from javax.swing.plaf.basic.BasicComboBoxEditor
        public Object getItem() {
            
            JTextComponent editor = getEditor();
            
            Object newValue = editor.getText();

            if (oldValue != null && !(oldValue instanceof String))  {
                // The original value is not a string. Should return the value in it's
                // original type.
                if (newValue.equals(oldValue.toString()))  {
                    return oldValue;
                } else {
                    // Must take the value from the editor and get the value and cast it to the new type.
                    Class<?> cls = oldValue.getClass();
                    try {
                        Method method = cls.getMethod("valueOf", String.class); // NOI18N
                        newValue = method.invoke(oldValue, new Object[] { editor.getText() });
                    } catch (Exception ex) {
                        // Fail silently and return the newValue (a String object)
                    }
                }
            }
            return newValue;
        }

        public void selectAll() {
            delegate.selectAll();
        }

        public void addActionListener(ActionListener l) {
            delegate.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            delegate.removeActionListener(l);
        }
        
        private JTextComponent getEditor() {
            
            Component comp = getEditorComponent();
            assert (comp instanceof JTextComponent);
            
            return (JTextComponent)comp;
        }

    }
    
    private ReturnTypeUIHelper() {
    }

    /**
     * Entry point for the combobox initialization. It connects combobox with its content and 
     * add items for the combobox content management.
     *
     * @param provider Java EE module provider.
     * @param combo combobox to manage.
     */
    public static void connect(JComboBox combo) {
        connect(combo, null);
    }
    
    private static final void connect(final JComboBox combo, final String selectedType) {
    
        combo.setEditable(true);
        
        combo.setEditor(new ReturnTypeComboBoxEditor(combo.getEditor()));
        
        combo.setRenderer(new ReturnTypeListCellRenderer());
        
        populate(TEMP_TYPES, true, combo, selectedType, false);
        Component toListenOn = (combo.isEditable() ? combo.getEditor().getEditorComponent() : combo);
            
        toListenOn.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (KeyEvent.VK_ENTER == keyCode) {
                    Object selectedItem = combo.getSelectedItem();
                    if (selectedItem == NEW_ITEM) {
                        performBrowseType(combo);
                        e.consume();
                    }
                }
            }
        });
        
        combo.addActionListener(new ActionListener() {
            
            Object previousItem;
            int previousIndex = combo.getSelectedIndex();

            public void actionPerformed(ActionEvent e) {

                Object selectedItem = combo.getSelectedItem();
                // skipping of separator
                if (selectedItem == SEPARATOR_ITEM) {
                    int selectedIndex = combo.getSelectedIndex();
                    if (selectedIndex > previousIndex) {
                        previousIndex = selectedIndex + 1;
                        previousItem = combo.getItemAt(previousIndex);
                    } else {
                        previousIndex = selectedIndex - 1;
                        previousItem = combo.getItemAt(previousIndex);
                    }
                    combo.setSelectedItem(previousItem);
                    // handling mouse click, see KeyEvent.getKeyModifiersText(e.getModifiers())
                } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                    if (selectedItem == NEW_ITEM) {
                        performBrowseType(combo);
                    }
                }
            }
        });

    }
    
    private static void performBrowseType(final JComboBox combo) {
        final ReturnTypeComboBoxModel model = (ReturnTypeComboBoxModel) combo.getModel();
        combo.setPopupVisible(false);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setSelectedItem(combo, model.getPreviousItem());
            }
        });
    }
    
    private static List populate(List<String> types, boolean creationSupported, final JComboBox combo, final String selectedType, boolean selectItemLater) {    

        
        List<Object> items = (types == null ? new LinkedList<Object>() : new LinkedList<Object>(types));
        
        if (items.size() > 0) {
            items.add(SEPARATOR_ITEM);
        }   
        
        if (creationSupported) {
            items.add(NEW_ITEM);
        }
        
        
        ReturnTypeComboBoxModel model = new ReturnTypeComboBoxModel(types, items);

        combo.setModel(model);
        
        if (selectedType != null) {

            // Ensure that the correct item is selected before listeners like FocusListener are called.
            // ActionListener.actionPerformed() is not called if this method is already called from 
            // actionPerformed(), in that case selectItemLater should be set to true and setSelectedItem()
            // below is called asynchronously so that the actionPerformed() is called
            setSelectedItem(combo, selectedType); 

            if (selectItemLater) {
                SwingUtilities.invokeLater(new Runnable() { // postpone item selection to enable event firing from JCombobox.setSelectedItem()
                    public void run() {
                        populate(TEMP_TYPES, true, combo, "Object", false);
                    }
                });
            }

        }
        
        return types;
    }

    private static void setSelectedItem(final JComboBox combo, final Object item) {
        combo.setSelectedItem(item);
        if (combo.isEditable() && combo.getEditor() != null) {
            // item must be set in the editor in case of editable combobox
            combo.configureEditor(combo.getEditor(), combo.getSelectedItem()); 
        }
    }
    
}