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

package org.netbeans.modules.j2ee.archive.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.j2ee.archive.project.ArchiveProjectProperties;
import org.openide.ErrorManager;

/** Class which makes creation of the GUI easier. Registers JComponent
 * property names and handles reading/storing the values from the components
 * automaticaly.
 *
 * @author Petr Hrebejk (original)
 */
public final class VisualPropertySupport {
    
    private static final String WRONG_TYPE = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/archive/customizer/Bundle").getString("WrongType");
    
    private ArchiveProjectProperties apProperties;
    private HashMap component2property;
    private ComponentListener componentListener;
    
    private int comboType;  //0 ... display text == value
    //1 ... display text != value
    private String[] comboValues;
    
    public VisualPropertySupport( ArchiveProjectProperties apProperties ) {
        this.apProperties = apProperties;
        this.component2property = new HashMap( 10 );
        this.componentListener = new ComponentListener();
    }
        
    /** Registers combo box.
     */
    public void register(JComboBox component, String displayNames[], String[] values, String propertyName) {
        comboType = 1;
        comboValues = values.clone();
        String value = (String) getAsType(propertyName, String.class);
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
    
    
    // Static methods for reading components and models ------------------------
    
    private static String readValue( JCheckBox checkBox ) {
        return checkBox.isSelected() ? "true" : "false"; // NOI18N
    }
    
    private static String readValue( Document document ) {
        try {
            return document.getText( 0, document.getLength() );
        } catch ( BadLocationException e ) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    e);
            return ""; // NOI18N
        }
    }
    
    private static String readValue( JComboBox comboBox ) {
        return (String)comboBox.getSelectedItem();
    }
    
    // Private methods ---------------------------------------------------------
    
    private Object getAsType( String propertyName, Class expectedType ) {
        return getAsType( propertyName, expectedType, true );
    }
    
    private Object getAsType( String propertyName, Class expectedType, boolean throwException ) {
        Object value = apProperties.get( propertyName );
        
        if ( value == null || expectedType.isInstance( value ) ) {
            return value;
        } else if ( throwException ) {
            throw new IllegalArgumentException( "Value of property: " + propertyName +        // NOI18N
                    " exbected to be: " + expectedType.getName() + // NOI18N
                    " but was: " + value.getClass().getName() + "!" );   // NOI18N
        } else {
            return WRONG_TYPE;
        }
        
    }
    
    private class ComponentListener implements ActionListener, DocumentListener {
        
        // Implementation of action listener -----------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            String propertyName = (String)component2property.get( source );
            if( propertyName != null ) {
                if ( source instanceof JCheckBox ) {
                    apProperties.put( propertyName, readValue( (JCheckBox)source ) );
                } else if ( source instanceof JComboBox ) {
                    if (((JComboBox) source).getItemCount() == 0) {
                        return;
                    }
                    
                    switch (comboType) {
                        case 0: apProperties.put(propertyName, readValue((JComboBox) source));
                        break;
                        case 1: apProperties.put(propertyName, comboValues[((JComboBox) source).getSelectedIndex()]);
                        break;
                    }
                }
            }
        }
        
        // Implementation of document listener ---------------------------------
        
        public void changedUpdate( DocumentEvent e ) {
            Document document = e.getDocument();
            String propertyName = (String)component2property.get( document );
            if( propertyName != null ) {
                apProperties.put( propertyName, readValue( document ) );
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
