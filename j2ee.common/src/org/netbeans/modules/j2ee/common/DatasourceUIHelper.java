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

package org.netbeans.modules.j2ee.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.Action;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.AsynchronousAction;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.Context;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * DatasourceUIHelper populates and manages the content of the combobox for a data sources management.
 *
 * @author Libor Kotouc
 *
 * @since 1.6
 */
public final class DatasourceUIHelper {

    private static final class Separator extends JSeparator {
        Separator() {
            setPreferredSize(new Dimension(getWidth(), 1));
            setForeground(Color.BLACK);
        }
    }
    
    static final Separator SEPARATOR_ITEM = new Separator();
    static final Object NEW_ITEM = new Object() {
        public String toString() {
            return NbBundle.getMessage(DatasourceUIHelper.class, "LBL_NEW_DATASOURCE"); // NOI18N
        }
    };
    
    private static class DatasourceComboBoxModel extends AbstractListModel implements ComboBoxModel {
        
        private Object[] items;
        private Object selectedItem;
        private List<Datasource> datasources;
        private Object previousItem;
        
        private DatasourceComboBoxModel(List<Datasource> datasources, Object[] items) {
            this.datasources = datasources;
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
            return items[index];
        }
        
        public int getSize() {
            return items.length;
        }
        
        Object getPreviousItem() {
            return previousItem;
        }
        
        List<Datasource> getDatasources() {
            return datasources;
        }
    }
    
    private static class DatasourceListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value instanceof Datasource) {
                Datasource ds = (Datasource) value;
                setText(ds != null ? ds.getDisplayName() : ""); // NOI18N
                setToolTipText(ds.toString());
            }
            else 
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
    
    private static class DatasourceComparator implements Comparator<Datasource> {
        
        public int compare(Datasource ds1, Datasource ds2) {
            
            if (ds1 == null) {
                return ds2 == null ? 0 : -1;
            }
            else {
                if (ds2 == null) {
                    return 1;
                }
                else {
                    String dispName1 = ds1.getDisplayName();
                    String dispName2 = ds2.getDisplayName();
                    if (dispName1 == null) {
                        return dispName2 == null ? 0 : -1;
                    }
                    else {
                        return dispName2 == null ? 1 : dispName1.compareToIgnoreCase(dispName2);
                    }
                }
            }
        }
    }
    
    private static class DatasourceComboBoxEditor implements ComboBoxEditor {
        
        private ComboBoxEditor delegate;
        private Object oldValue;
        
        DatasourceComboBoxEditor(ComboBoxEditor delegate) {
            this.delegate = delegate;
        }

        public Component getEditorComponent() {
            return delegate.getEditorComponent();
        }

        public void setItem(Object anObject) {
            
            JTextComponent editor = getEditor();
            
            if (anObject != null)  {
                String text = (anObject instanceof Datasource ? ((Datasource)anObject).getJndiName() : anObject.toString());
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
    
    private DatasourceUIHelper() {
    }

    /**
     * Entry point for the combobox initialization. It connects combobox with its content and 
     * add items for the combobox content management.
     *
     * @param combo combobox to manage
     */
    public static void connect(J2eeModuleProvider provider, JComboBox combo) {
        connect(provider, combo, null);
    }
    
    private static final void connect(final J2eeModuleProvider provider, final JComboBox combo, final Datasource selectedDatasource) {
        
        assert(provider != null);
        
        combo.setEditor(new DatasourceComboBoxEditor(combo.getEditor()));
        
        combo.setRenderer(new DatasourceListCellRenderer());
        
        // fetch datasources asynchronously
        Collection<Action> actions = new ArrayList<Action>();
        final List<Datasource> datasources = new ArrayList<Datasource>();
        actions.add(new AsynchronousAction() {
            public void run(Context actionContext) {
                String msg = NbBundle.getMessage(DatasourceUIHelper.class, "MSG_retrievingDS");
                actionContext.getProgress().progress(msg);
                datasources.addAll(getDatasources(provider));
            }
        });
        EventRequestProcessor eventRP = new EventRequestProcessor();
        eventRP.invoke(actions);

        populate(datasources, provider.isDatasourceCreationSupported(), combo, selectedDatasource, false);
        Component toListenOn = (combo.isEditable() ? combo.getEditor().getEditorComponent() : combo);
            
        toListenOn.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (KeyEvent.VK_ENTER == keyCode) {
                    Object selectedItem = combo.getSelectedItem();
                    if (selectedItem == NEW_ITEM) {
                        performCreateDatasource(provider, combo, false);
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
                        performCreateDatasource(provider, combo, true);
                    }
                }
            }
        });

    }
    
    private static void performCreateDatasource(final J2eeModuleProvider provider, final JComboBox combo, boolean selectItemLater) {
        
        final DatasourceComboBoxModel model = (DatasourceComboBoxModel) combo.getModel();
        final DatasourceCustomizer dsc = new DatasourceCustomizer(model.getDatasources());
        boolean accept = dsc.showDialog();
        Collection<Action> actions = new ArrayList<Action>();                        
        final Datasource[] ds = new Datasource[1];
        
        // creating datasources asynchronously
        if (accept) {
            final String password = dsc.getPassword();
            final String jndiName = dsc.getJndiName();
            final String url      = dsc.getUrl();
            final String username = dsc.getUsername();
            final String driverClassName = dsc.getDriverClassName();
            actions.add(new AsynchronousAction() {
                public void run(Context actionContext) {
                    String msg = NbBundle.getMessage(DatasourceUIHelper.class, "MSG_creatingDS");
                    actionContext.getProgress().progress(msg);
                    try {
                        ds[0] = provider.createDatasource(jndiName, url, username, password, driverClassName);
                    } catch (DatasourceAlreadyExistsException daee) { // it should not occur bcs it should be already handled in DatasourceCustomizer
                        StringBuilder sb = new StringBuilder();
                        for (Object conflict : daee.getDatasources()) {
                            sb.append(conflict.toString() + "\n"); // NOI18N
                        }
                        ErrorManager.getDefault().annotate(daee, NbBundle.getMessage(DatasourceUIHelper.class, "ERR_DsConflict", sb.toString()));
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, daee);
                    }
                }
                
                public boolean isEnabled() {
                    return password != null;
                }
            });
        }
        // fetch datasources asynchronously
        final List<Datasource> datasources = new ArrayList<Datasource>();
        actions.add(new AsynchronousAction() {
            public void run(Context actionContext) {
                String msg = NbBundle.getMessage(DatasourceUIHelper.class, "MSG_retrievingDS");
                actionContext.getProgress().progress(msg);
                datasources.addAll(getDatasources(provider));
            }

            public boolean isEnabled() {
                return ds[0] != null;
            }
        });
        EventRequestProcessor eventRP = new EventRequestProcessor();
        eventRP.invoke(actions);
        
        combo.setPopupVisible(false);
        if (ds[0] == null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setSelectedItem(combo, model.getPreviousItem());
                }
            });
        } else {
            populate(datasources, provider.isDatasourceCreationSupported(), combo, ds[0], selectItemLater);
        }
    }
    
    /**
     * Returns a sorted list of all datasources (from the module and the server)
     */
    private static List<Datasource> getDatasources(final J2eeModuleProvider provider) {
        
        Set<Datasource> moduleDatasources = provider.getModuleDatasources();
        Set<Datasource> serverDatasources = provider.getServerDatasources();
        
        int initSize = moduleDatasources.size() + serverDatasources.size();
        
        Set<Datasource> datasources = new HashSet<Datasource>(initSize);
        datasources.addAll(moduleDatasources);
        datasources.addAll(serverDatasources);
        
        ArrayList<Datasource> sortedDatasources = new ArrayList<Datasource>(datasources);
        Collections.sort(sortedDatasources, new DatasourceComparator());
        return sortedDatasources;
    }
    
    private static List populate(List<Datasource> datasources, boolean creationSupported, final JComboBox combo, final Datasource selectedDatasource, boolean selectItemLater) {    

        
        List<Object> items = (datasources == null ? new LinkedList<Object>() : new LinkedList<Object>(datasources));
        
        if (items.size() > 0) {
            items.add(SEPARATOR_ITEM);
        }   
        
        if (creationSupported) {
            items.add(NEW_ITEM);
        }
        
        
        Object[] itemsArray = items.toArray();
        DatasourceComboBoxModel model = new DatasourceComboBoxModel(datasources, itemsArray);

        combo.setModel(model);
        
        if (selectedDatasource != null) {

            // Ensure that the correct item is selected before listeners like FocusListener are called.
            // ActionListener.actionPerformed() is not called if this method is already called from 
            // actionPerformed(), in that case selectItemLater should be set to true and setSelectedItem()
            // below is called asynchronously so that the actionPerformed() is called
            setSelectedItem(combo, selectedDatasource); 

            if (selectItemLater) {
                SwingUtilities.invokeLater(new Runnable() { // postpone item selection to enable event firing from JCombobox.setSelectedItem()
                    public void run() {
                        setSelectedItem(combo, selectedDatasource);
                    }
                });
            }

        }
        
        return datasources;
    }

    private static void setSelectedItem(final JComboBox combo, final Object item) {
        combo.setSelectedItem(item);
        if (combo.isEditable() && combo.getEditor() != null) {
            // item must be set in the editor in case of editable combobox
            combo.configureEditor(combo.getEditor(), combo.getSelectedItem()); 
        }
    }
    
}
