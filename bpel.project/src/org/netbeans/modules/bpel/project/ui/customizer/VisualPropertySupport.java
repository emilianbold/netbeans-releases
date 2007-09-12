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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.bpel.project.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;

/** Class which makes creation of the GUI easier. Registers JComponent
 * property names and handles reading/storing the values from the components
 * automaticaly.
 *
 * @author Petr Hrebejk
 */
public final class VisualPropertySupport {
    
    private static final String WRONG_TYPE = "WrongType";
    
    private IcanproProjectProperties webProperties;
    private HashMap component2property;
    private ComponentListener componentListener;
    
    private int comboType;  //0 ... display text == value
                            //1 ... display text != value
    private String[] comboValues;
    
    public VisualPropertySupport( IcanproProjectProperties webProperties ) {
        this.webProperties = webProperties;
        this.component2property = new HashMap( 10 );
        this.componentListener = new ComponentListener();
    }
        
    /** Registers the component with given property, Fills the component 
     * with given object.
     */
    public void register( JCheckBox component, String propertyName ) {
        
        Boolean value = (Boolean)getAsType( propertyName, Boolean.class );
        component2property.put( component, propertyName );
        component.setSelected( value != null && value.booleanValue() );
        component.removeActionListener( componentListener );
        component.addActionListener( componentListener );
    } 
    
    /** Registers the component with given property, Fills the component
     * with given object.
     */
    public void register( JTextField component, String propertyName ) {
        String value = (String)getAsType( propertyName, String.class );
        component2property.put( component.getDocument(), propertyName );
        component.setText( value != null ? value : "" );
        component.getDocument().addDocumentListener( componentListener );
    }
    
    /** Registers JTable containing VisualClassPath items and acompaniing
     *  buttons for handling the class path
     */
    public void register( VisualClasspathSupport component, String propertyName ) {
        List value = (List)getAsType( propertyName, List.class );
        component2property.put( component, propertyName );
        component.setVisualClassPathItems( value != null ? value : Collections.EMPTY_LIST );
        component.removeActionListener( componentListener );
        component.addActionListener( componentListener );
    }
    
    /** Registers combo box.
     */
    public void register( JComboBox component, String items[], String propertyName ) {
        comboType = 0;
        String value = (String)getAsType( propertyName, String.class );
        component2property.put( component, propertyName );
        // Add all items and find the selected one
        component.removeAllItems();
        int selectedIndex = 0;
        for ( int i = 0; i < items.length; i++ ) {
            component.addItem( items[i] );
            if ( items[i].equals( value ) ) {
                selectedIndex = i;
            }
        }        
        component.setSelectedIndex( selectedIndex );                            
        component.removeActionListener( componentListener );
        component.addActionListener( componentListener );
    }

    /** Registers combo box.
     */
    public void register(JComboBox component, String displayNames[], String[] values, String propertyName) {
        comboType = 1;
        comboValues = values;
        String value = (String) getAsType(propertyName, String.class);
        component2property.put(component, propertyName);
        // Add all items and find the selected one
        component.removeAllItems();
        int selectedIndex = 0;
        for (int i = 0; i < displayNames.length; i++) {
            component.addItem(displayNames[i]);
            if (values[i].equals(value))
                selectedIndex = i;
        }
        component.setSelectedIndex(selectedIndex);                            
        component.removeActionListener( componentListener );
        component.addActionListener(componentListener);
    }

    /**
     * Registers JList containing VisualClassPath items and acompaniing buttons for handling the
     * class path
     *
     * @param component DOCUMENT ME!
     * @param propertyName DOCUMENT ME!
     */
    public void register(VisualArchiveIncludesSupport component, String propertyName) {
        List value = (List) getAsType(propertyName, List.class);
        component2property.put(component, propertyName);
        component.setVisualWarItems((value != null) ? value : Collections.EMPTY_LIST);
        component.removeActionListener(componentListener);
        component.addActionListener(componentListener);
    }
    
    // Static methods for reading components and models ------------------------
    
    private static Boolean readValue( JCheckBox checkBox ) {
        return checkBox.isSelected() ? Boolean.TRUE : Boolean.FALSE;
    }
    
    private static String readValue( Document document ) {
        try {
            return document.getText( 0, document.getLength() );            
        }
        catch ( BadLocationException e ) {
            assert false : "Invalid document "; //NOI18N
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
        
        Object value = webProperties.get( propertyName );
        
        if ( value == null || expectedType.isInstance( value ) ) {
            return value;
        }
        else if ( throwException ) {            
            throw new IllegalArgumentException( "Value of property: " + propertyName +        // NOI18N
                                                " exbected to be: " + expectedType.getName() + // NOI18N
                                                " but was: " + value.getClass().getName() + "!" );   // NOI18N
        }
        else {
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
                    webProperties.put( propertyName, readValue( (JCheckBox)source ) );                    
                } else if ( source instanceof VisualClasspathSupport ) {
                    webProperties.put( propertyName, ((VisualClasspathSupport)source).getVisualClassPathItems() );
                } else if ( source instanceof JComboBox ) {
                    if (((JComboBox) source).getItemCount() == 0) {
                        return;
                    }
                    
                    switch (comboType) {
                        case 0: webProperties.put(propertyName, readValue((JComboBox) source));
                                break;
                        case 1: webProperties.put(propertyName, comboValues[((JComboBox) source).getSelectedIndex()]);
                                break;
                    }
                }
                else if ( source instanceof VisualArchiveIncludesSupport ) {
                    webProperties.put( propertyName, ((VisualArchiveIncludesSupport)source).getVisualWarItems() );
                }
            }
        }                
               
        // Implementation of document listener ---------------------------------
        
        public void changedUpdate( DocumentEvent e ) {
            Document document = e.getDocument();            
            String propertyName = (String)component2property.get( document );            
            if( propertyName != null ) {
                webProperties.put( propertyName, readValue( document ) );                
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
