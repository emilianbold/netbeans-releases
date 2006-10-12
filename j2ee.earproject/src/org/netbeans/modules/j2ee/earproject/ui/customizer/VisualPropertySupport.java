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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/** 
 * Class which makes creation of the GUI easier. Registers JComponent
 * property names and handles reading/storing the values from the components
 * automatically.
 *
 * @author Petr Hrebejk
 */
public final class VisualPropertySupport {
    
    private EarProjectProperties earProperties;
    private Map<Object, String> component2property;
    private ComponentListener componentListener;
    
    /**
     * 0 ... display text == value<br>
     * 1 ... display text != value
     */
    private int comboType = -1;
    
    private String[] comboValues;
    
    public VisualPropertySupport(final EarProjectProperties earProperties) {
        this.earProperties = earProperties;
        this.component2property = new HashMap<Object, String>(10);
        this.componentListener = new ComponentListener();
    }
        
    /** Registers the component with given property, Fills the component 
     * with given object.
     */
    public void register( JCheckBox component, String propertyName ) {
        Boolean value = getAsType(earProperties, propertyName, Boolean.class);
        component2property.put( component, propertyName );
        component.setSelected( value != null && value.booleanValue() );
        component.removeActionListener( componentListener );
        component.addActionListener( componentListener );
    } 
    
    /** Registers the component with given property, Fills the component
     * with given object.
     */
    public void register( JTextField component, String propertyName ) {
        String value = getAsType(earProperties, propertyName, String.class);
        component2property.put( component.getDocument(), propertyName );
        component.setText( value != null ? value : "" );
        component.getDocument().addDocumentListener( componentListener );
    }
    
    /** 
     * Registers JTable containing VisualClassPath items and accompanying
     * buttons for handling the class path.
     */
    public void register( VisualClasspathSupport component, String propertyName ) {
        @SuppressWarnings("unchecked")
        List<VisualClassPathItem> value = getAsType(earProperties, propertyName, List.class);
        component2property.put( component, propertyName );
        component.setVisualClassPathItems( value != null ? value : Collections.<VisualClassPathItem>emptyList());
        component.removeActionListener( componentListener );
        component.addActionListener( componentListener );
    }
    
    /** 
     * Registers JList containing VisualClassPath items and accompanying
     * buttons for handling the class path.
     */
    public void register(VisualArchiveIncludesSupport component, String propertyName) {
        @SuppressWarnings("unchecked")
        List<VisualClassPathItem> value = getAsType(earProperties, propertyName, List.class);
        component2property.put(component, propertyName);
        component.setVisualWarItems(value != null ? value : Collections.<VisualClassPathItem>emptyList());
        component.removeActionListener( componentListener );
        component.addActionListener(componentListener);
    }
    
    /** Registers combo box. */
    public void register(JComboBox component, String items[], String propertyName) {
        checkJComboBoxRegistered();
        comboType = 0;
        String value = getAsType(earProperties, propertyName, String.class);
        component2property.put( component, propertyName );
        // Add all items and find the selected one
        component.removeAllItems();
        int selectedIndex = -1;
        for ( int i = 0; i < items.length; i++ ) {
            component.addItem( items[i] );
            if ( items[i].equals( value ) ) {
                selectedIndex = i;
            }
        }    
        if (selectedIndex > -1) {
            component.setSelectedIndex( selectedIndex );
        }
        component.removeActionListener( componentListener );
        component.addActionListener( componentListener );
    }

    /** Registers combo box. */
    public void register(JComboBox component, String displayNames[], String[] values, String propertyName) {
        checkJComboBoxRegistered();
        comboType = 1;
        comboValues = values;
        String value = getAsType(earProperties, propertyName, String.class);
        component2property.put(component, propertyName);
        // Add all items and find the selected one
        component.removeAllItems();
        int selectedIndex = 0;
        for (int i = 0; i < displayNames.length; i++) {
            component.addItem(displayNames[i]);
            if (values[i].equals(value)) {
                selectedIndex = i;
            }
        }
        if (selectedIndex < component.getItemCount()) {
            component.setSelectedIndex( selectedIndex );
        }
        component.removeActionListener( componentListener );
        component.addActionListener(componentListener);
    }
    
    private void checkJComboBoxRegistered() {
        assert comboType == -1 : "JComboBox already registered and only " +
                "one instance per VisualPropertySupport is supported. " + // NOI18N
                "Another VisualPropertySupport instance may be used."; // NOI18N
    }
    
    // Static methods for reading components and models ------------------------
    
    private static Boolean readValue( JCheckBox checkBox ) {
        return checkBox.isSelected();
    }
    
    private static String readValue( Document document ) {
        try {
            return document.getText( 0, document.getLength() );
        } catch ( BadLocationException e ) {
            assert false : e;
            return "";
        }
    }
    
    private static String readValue( JComboBox comboBox ) {
        return (String)comboBox.getSelectedItem();
    }
    
    // Private methods ---------------------------------------------------------
    
    private static <T> T getAsType(final EarProjectProperties earProperties, 
            String propertyName, Class<T> expectedType) {
        return getAsType(earProperties, propertyName, expectedType, true);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T getAsType(final EarProjectProperties earProperties,
            String propertyName, Class<T> expectedType, boolean throwException) {
        T result = null;
        Object value = earProperties.get(propertyName);
        if (value == null || expectedType.isInstance(value)) {
            result = (T) value;
        } else if ( throwException ) {
            throw new IllegalArgumentException( "Value of property: " + propertyName + // NOI18N
                    " exbected to be: " + expectedType.getClass().getName() + // NOI18N
                    " but was: " + value.getClass().getName() + "!" ); // NOI18N
        }
        return result;
    }
    
    private class ComponentListener implements ActionListener, DocumentListener {
        
        // Implementation of action listener -----------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            String propertyName = component2property.get( source );
            if( propertyName != null ) {
                if ( source instanceof JCheckBox ) {
                    earProperties.put( propertyName, readValue( (JCheckBox)source ) );                    
                } else if ( source instanceof VisualClasspathSupport ) {
                    earProperties.put( propertyName, ((VisualClasspathSupport)source).getVisualClassPathItems() );
                } else if ( source instanceof JComboBox ) {
                    if (((JComboBox) source).getItemCount() == 0) {
                        return;
                    }
                    switch (comboType) {
                        case 0:
                            earProperties.put(propertyName, readValue((JComboBox) source));
                            break;
                        case 1:
                            earProperties.put(propertyName, comboValues[((JComboBox) source).getSelectedIndex()]);
                            break;
                        default:
                            assert false : "Unknown comboType: " + comboType;
                    }
                } else if ( source instanceof VisualArchiveIncludesSupport ) {
                    earProperties.put( propertyName, ((VisualArchiveIncludesSupport)source).getVisualWarItems() );
                }
            }
        }                
               
        // Implementation of document listener ---------------------------------
        
        public void changedUpdate( DocumentEvent e ) {
            Document document = e.getDocument();            
            String propertyName = component2property.get( document );            
            if( propertyName != null ) {
                earProperties.put( propertyName, readValue( document ) );                
            }
        }
        
        public void insertUpdate( DocumentEvent e ) {
            changedUpdate( e );
        }
        
        public void removeUpdate( DocumentEvent e ) {
            changedUpdate( e );
        }
    }

}
