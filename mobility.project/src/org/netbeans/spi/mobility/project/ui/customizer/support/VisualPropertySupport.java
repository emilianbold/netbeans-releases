/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.spi.mobility.project.ui.customizer.support;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.openide.ErrorManager;

/** Class which makes creation of the GUI easier. Registers JComponent
 * property names and handles reading/storing the values from the components
 * automaticaly.
 *
 * @author Petr Hrebejk, Adam Sotona
 */
public final class VisualPropertySupport {
    
    private static final String WRONG_TYPE = "WrongType"; //NOI18N
    private static final String CONFIG_PREFFIX = "configs."; //NOI18N
    private static Reference<VisualPropertySupport> cache = new WeakReference(null); 
    
    final private Map<String,Object> backup;
    final private ComponentListener componentListener;
    final private CheckBoxListener listener;
    
    final Map<String,Object> properties;
    final HashMap<Object,Object> component2property;
    final HashMap<JCheckBox,VisualPropertyGroup> checkbox2group;
    private String configuration;
    
    
    public static VisualPropertySupport getDefault(Map<String,Object> properties) {
        VisualPropertySupport vps = cache.get();
        if (vps == null || vps.properties != properties) {
            vps = new VisualPropertySupport(properties);
            cache = new WeakReference(vps);
        }
        return vps;
    }
    
    public static String translatePropertyName(final String configuration, final String propertyName, final boolean useDefault) {
        return useDefault || configuration == null ? propertyName : prefixPropertyName(configuration, propertyName);
    }
    
    public static String prefixPropertyName(final String configuration, final String propertyName) {
        return CONFIG_PREFFIX + configuration + '.' + propertyName;
    }
    
    private VisualPropertySupport(Map<String,Object> properties) {
        this.properties = properties;
        this.backup = new HashMap<String,Object>();
        this.component2property = new HashMap<Object,Object>( 10 );
        this.componentListener = new ComponentListener();
        this.checkbox2group = new HashMap<JCheckBox,VisualPropertyGroup>();
        this.listener = new CheckBoxListener();
    }
    
    /** Registers the checkbox with given group of properties and currently selected configuration,
     *  tests the group for any non-default value and fills the checkbox,
     *  initializes the group components by calling group.refreshGroup().
     *  Don't call this method from VisualPropertyGroup.refreshGroup() method !
     */
    public void register(final JCheckBox check, final String configuration, final VisualPropertyGroup group) {
        check.removeActionListener(listener);
        this.configuration = configuration; // currently only one selected configuration is supported
        checkbox2group.put(check, group);
        check.setEnabled(configuration != null);
        final boolean useDefaults = !testForNonDefaultValue(group.getGroupPropertyNames());
        check.setSelected(useDefaults);
        refreshGroup(group, useDefaults);
        check.addActionListener(listener);
    }
    
    /** Registers the component with given property, Fills the component
     * with given object according to default status.
     * Use this method to register components from VisualPropertyGroup.refreshGroup() method.
     * Use default property names, don't prefix them, it is our job !
     */
    public void register( final JCheckBox component, final String propertyName, final boolean useDefault ) {
        register(component, translatePropertyName(configuration, propertyName, useDefault));
        component.setEnabled(!useDefault);
    }
    
    /** Registers the component with given property, Fills the component
     * with given object.
     */
    public void register( final JCheckBox component, final String propertyName ) {
        component.removeActionListener( componentListener );
        final Object value = properties.get(propertyName);
        component2property.put( component, propertyName );
        component.setSelected((value instanceof Boolean && ((Boolean)value).booleanValue()) || (value instanceof String && Boolean.parseBoolean((String) value)));
        component.addActionListener( componentListener );
    }
    
    /** Registers the component with given property, Fills the component
     * with given object according to default status.
     * Use this method to register components from VisualPropertyGroup.refreshGroup() method.
     * Use default property names, don't prefix them, it is our job !
     */
    public void register( final JTextComponent component, final String propertyName, final boolean useDefault ) {
        register(component, translatePropertyName(configuration, propertyName, useDefault));
        component.setEnabled(!useDefault);
        component.setEditable(!useDefault);
    }
    
    
    
    
    /** Registers the component with given property, Fills the component
     * with given object.
     */
    public void register( final JTextComponent component, final String propertyName ) {
        component.getDocument().removeDocumentListener( componentListener );
        component2property.put( component.getDocument(), propertyName );
        component.setText(String.valueOf(properties.get(propertyName)));
        component.getDocument().addDocumentListener( componentListener );
    }
    
    /** Registers the component with given property, Fills the component
     * with given object according to default status.
     * Use this method to register components from VisualPropertyGroup.refreshGroup() method.
     * Use default property names, don't prefix them, it is our job !
     */
    public void register( final JComboBox component, final Object[] items, final String propertyName, final boolean useDefault ) {
        register(component, items, translatePropertyName(configuration, propertyName, useDefault));
        component.setEnabled(!useDefault);
    }
    
    private Document getDocumentFor(final JComboBox box) {
        if (box.isEditable()) {
            final ComboBoxEditor cbe = box.getEditor();
            if (cbe != null) {
                final Component c = cbe.getEditorComponent();
                if (c instanceof JTextComponent) {
                    return ((JTextComponent)c).getDocument();
                }
            }
        }
        return null;
    }
    
    /** Registers combo box.
     */
    public void register( final JComboBox component, Object[] items, final String propertyName ) {
        component.removeActionListener( componentListener );
        final Document doc = getDocumentFor(component);
        if (doc != null) doc.removeDocumentListener( componentListener );
        final Object value = properties.get(propertyName);
        component2property.put( component, propertyName );
        if (doc != null) component2property.put( doc, propertyName );
        // Add all items and find the selected one
        if (items == null) {
            final ComboBoxModel model = component.getModel();
            items = new Object[model.getSize()];
            for (int i=0; i<items.length; i++) items[i] = model.getElementAt(i);
        }
        component.removeAllItems();
        boolean selected = false;
        for ( int i = 0; i < items.length; i++ ) {
            component.addItem( items[i] );
            if (items[i].equals(value)) {
                selected = true;
                component.setSelectedIndex(i);
            }
        }
        if (!selected) {
            if (component.isEditable()) component.setSelectedItem(value);
            else if (items.length > 0) {
                
                // set special list cell renderer for wrong item
                
                component.setSelectedIndex(0);
                properties.put(propertyName, items[0]);
            }
        }
        component.addActionListener( componentListener );
        if (doc != null) doc.addDocumentListener( componentListener );
    }
    
    /** Registers the component with given property, Fills the component
     * with given object according to default status.
     * Use this method to register components from VisualPropertyGroup.refreshGroup() method.
     * Use default property names, don't prefix them, it is our job !
     */
    public void register( final JSlider component, final String propertyName, final boolean useDefault ) {
        register(component, translatePropertyName(configuration, propertyName, useDefault));
        component.setEnabled(!useDefault);
        final Enumeration en = component.getLabelTable().elements();
        while (en.hasMoreElements()) ((JComponent)en.nextElement()).setEnabled(!useDefault);
    }
    
    /** Registers the component with given property, Fills the component
     * with given object.
     */
    public void register( final JSlider component, final String propertyName ) {
        component.removeChangeListener( componentListener );
        final Object value = properties.get(propertyName);
        component2property.put( component, propertyName );
        component.setValue((value instanceof Number ? ((Number)value).intValue() : (value instanceof String ? Integer.parseInt((String) value) : component.getMinimum())));
        component.addChangeListener( componentListener );
    }
    
    /** Registers the component with given property, Fills the component
     * with given object according to default status.
     * Use this method to register components from VisualPropertyGroup.refreshGroup() method.
     * Use default property names, don't prefix them, it is our job !
     */
    public void register( final JSpinner component, final String propertyName, final boolean useDefault ) {
        register(component, translatePropertyName(configuration, propertyName, useDefault));
        component.setEnabled(!useDefault);
    }
    
    /** Registers the component with given property, Fills the component
     * with given object.
     */
    public void register( final JSpinner component, final String propertyName ) {
        component.removeChangeListener( componentListener );
        final Object value = properties.get(propertyName);
        component2property.put( component, propertyName );
        component.setValue((value instanceof Number ? ((Number)value).intValue() : (value instanceof String ? Integer.parseInt((String) value) : 0)));
        component.addChangeListener( componentListener );
    }
    
    /** Registers the component with given property.
     * Use this method to register components from VisualPropertyGroup.refreshGroup() method.
     * Use default property names, don't prefix them, it is our job !
     */
    public void register( final JRadioButton component, final String propertyName, final boolean useDefault ) {
        register(component, translatePropertyName(configuration, propertyName, useDefault));
        component.setEnabled(!useDefault);
    }
    
    /** Registers combo box.
     */
    public void register( final JRadioButton component, final String propertyName ) {
        component.removeActionListener( componentListener );
        final Object value = properties.get(propertyName);
        component2property.put( component, propertyName );
        component.setSelected(value != null && value.toString().equals(readValue(component)));
        component.addActionListener( componentListener );
    }
    
    public static String[] translatePropertyNames(final String configuration, final String[] propertyNames, final boolean useDefault) {
        String names[] = new String[propertyNames.length];
        for (int i=0; i<propertyNames.length; i++)
            names[i] = translatePropertyName(configuration, propertyNames[i], useDefault);
        return names;
    }
    
    /** Registers the component with given property.
     * Use this method to register components from VisualPropertyGroup.refreshGroup() method.
     * Use default property names, don't prefix them, it is our job !
     */
    public void register( final StorableTableModel component, final String[] propertyNames, final boolean useDefault ) {
        register(component, translatePropertyNames(configuration, propertyNames, useDefault));
    }
    
    /** Registers StorableTableModel
     */
    public void register( final StorableTableModel component, final String[] propertyNames ) {
        component.removeTableModelListener( componentListener );
        Object values[] = new Object[propertyNames.length];
        for (int i=0; i<values.length; i++)
            values[i] = properties.get( propertyNames[i] );
        component2property.put( component, propertyNames );
        component.setDataDelegates(values);
        component.addTableModelListener( componentListener );
    }
    
    // Static methods for reading components and models ------------------------
    
    protected static Boolean readValue( final JCheckBox checkBox ) {
        return checkBox.isSelected() ? Boolean.TRUE : Boolean.FALSE;
    }
    
    protected static String readValue( final Document document ) {
        try {
            return document.getText( 0, document.getLength() );
        } catch ( BadLocationException e ) {
            assert false : "Invalid document "; //NOI18N
            return ""; // NOI18N
        }
    }
    
    protected static String readValue( final JComboBox comboBox ) {
        Object selectedItem = comboBox.getSelectedItem();
        if (selectedItem == null)
            return null;
        return selectedItem.toString();
    }
    
    protected static Integer readValue( final JSlider slider ) {
        return new Integer(slider.getValue());
    }
    
    protected static Object readValue( final JSpinner slider ) {
        return slider.getValue();
    }
    
    protected static String readValue( final JRadioButton radio ) {
        return radio.getActionCommand();
    }
    
    // Private methods ---------------------------------------------------------
    
    private boolean testForNonDefaultValue(final String[] propertyNames) {
        if (configuration == null) return true;
        for (int i=0; i<propertyNames.length; i++) {
            if (properties.get(prefixPropertyName(configuration, propertyNames[i])) != null)
                return true;
        }
        return false;
    }
    
    private void removeConfigProperties(final String[] propertyNames) {
        if (configuration == null) return;
        for (int i=0; i<propertyNames.length; i++) {
            final String name = prefixPropertyName(configuration, propertyNames[i]);
            backup.put(name, properties.remove(name));
        }
    }
    
    private void copyFromDefaults(final String[] propertyNames) {
        if (configuration == null) return;
        final Class cl[]={};
        final Object ob[]={};
        for (int i=0; i<propertyNames.length; i++) {
            final String prefName = prefixPropertyName(configuration, propertyNames[i]);
            if (properties.get(prefName) == null) {
                Object backValue = backup.get(prefName);
                if (backValue == null) {
                    backValue = properties.get(propertyNames[i]);
                    if (backValue instanceof Cloneable) try {
                        final Method m = backValue.getClass().getMethod("clone", cl); //NOI18N
                        if (m != null) backValue = m.invoke(backValue, ob);
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,e);
                    }
                }
                properties.put(prefName, backValue);
            }
        }
    }
    
    protected void refreshGroup(final VisualPropertyGroup g, final boolean useDefaults) {
        if (useDefaults) {
            removeConfigProperties(g.getGroupPropertyNames());
            g.initGroupValues(true);
        } else {
            copyFromDefaults(g.getGroupPropertyNames());
            g.initGroupValues(false);
        }
        
    }
    
    private class ComponentListener implements ActionListener, DocumentListener, ChangeListener, TableModelListener {

        private ComponentListener()
        {
            //Just to avoid creation of accessor class
        }
        
        // Implementation of action listener -----------------------------------
        
        public void actionPerformed( final ActionEvent e ) {
            
            final Object source = e.getSource();
            
            final String propertyName = (String)component2property.get( source );
            if( propertyName != null ) {
                
                if ( source instanceof JCheckBox ) {
                    properties.put( propertyName, readValue( (JCheckBox)source ) );
                } else if ( source instanceof JComboBox ) {
                    properties.put( propertyName, readValue( (JComboBox)source ) );
                } else if ( source instanceof JRadioButton ) {
                    properties.put( propertyName,  readValue( (JRadioButton)source ));
                }
            }
            
        }
        
        // Implementation of document listener ---------------------------------
        
        public void changedUpdate( final DocumentEvent e ) {
            
            final Document document = e.getDocument();
            final String propertyName = (String)component2property.get( document );
            if( propertyName != null ) {
                properties.put( propertyName, readValue( document ) );
            }
        }
        
        public void insertUpdate( final DocumentEvent e ) {
            changedUpdate( e );
        }
        
        public void removeUpdate( final DocumentEvent e ) {
            changedUpdate( e );
        }
        
        public void stateChanged( final ChangeEvent e ) {
            final Object objectSource = e.getSource();
            String propertyName = null;
            if (objectSource instanceof JSlider) {
                final JSlider source = (JSlider) objectSource;
                propertyName = (String)component2property.get( source );
                if (propertyName != null)
                    properties.put( propertyName, readValue(source));
            } else if (objectSource instanceof JSpinner) {
                final JSpinner source = (JSpinner) objectSource;
                propertyName = (String) component2property.get(source);
                if (propertyName != null)
                    properties.put( propertyName, readValue(source));
            }
            
        }
        
        public void tableChanged(final TableModelEvent e) {
            final StorableTableModel source = (StorableTableModel) e.getSource();
            final String propertyNames[] = (String[])component2property.get( source );
            if (propertyNames != null) {
                final Object values[] = source.getDataDelegates();
                for (int i=0; i<propertyNames.length; i++)
                    properties.put( propertyNames[i], values[i]);
            }
        }
    }
    
    private class CheckBoxListener implements ActionListener {
        
        private CheckBoxListener()
        {
            //Just to avoid creation of accessor class
        }
        
        public void actionPerformed(final ActionEvent e) {
            final JCheckBox cb = (JCheckBox)e.getSource();
            final VisualPropertyGroup g = checkbox2group.get(cb);
            if (g != null) {
                refreshGroup(g, cb.isSelected());
            }
        }
        
    }
    
    public static interface StorableTableModel extends TableModel {
        public Object[] getDataDelegates();
        public void setDataDelegates(Object data[]);
    }
}
