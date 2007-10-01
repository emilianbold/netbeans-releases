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

package org.netbeans.modules.editor.options;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.border.*;
import javax.accessibility.*;

import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.explorer.propertysheet.PropertyModel;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import java.awt.Dimension;
import java.beans.FeatureDescriptor;
import org.openide.explorer.propertysheet.ExPropertyModel;

/**
 * ColoringEditorPanel is custom property editor operating
 * over ColoringBean, it is interfaced only through ColoringEditor
 * @author  Petr Nejedly
 * 
 * TODO: remove repaints as Hans will repair PropertyPanel
 */
public class ColoringEditorPanel extends javax.swing.JPanel {

    public static final String PROP_COLORING = "Coloring";  // NOI18N


    /** the value we're operating over. */
    private ColoringBean value;

    /** PropertyPanels for visual editing of Coloring's properties.
     * We need'em for enabling/disabling  
     */
    private PropWithDefaultPanel fontPanel;
    private PropWithDefaultPanel fgColorPanel;
    private PropWithDefaultPanel bgColorPanel;

    /** Component for preview actual coloring composed of value and defaultColoring
     */
    private ColoringPreview preview;

    /** Creates new form ColoringEditorPanel */
    public ColoringEditorPanel() {

        value = new ColoringBean(SettingsDefaults.defaultColoring, "null", // NOI18N
                                 SettingsDefaults.defaultColoring, true );

        initComponents ();

        GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        // There should always be non-null property editors for Font and Color 
        Class fontEditorClass = PropertyEditorManager.findEditor(Font.class).getClass();
        Class colorEditorClass = PropertyEditorManager.findEditor(Color.class).getClass();
        fontPanel = new PropWithDefaultPanel( Font.class, fontEditorClass,
                                              getBundleString("CEP_FontTitle"),        // NOI18N
                                              getBundleString("ACSD_CEP_Font"),        // NOI18N
                                              getBundleString("CEP_FontTrans"),        // NOI18N
                                              getBundleString("CEP_FontTrans_Mnemonic").charAt(0),  // NOI18N
                                              getBundleString("ACSD_CEP_FontTrans")); // NOI18N
        fontPanel.addPropertyChangeListener( new PropertyChangeListener() {
                                                 public void propertyChange( PropertyChangeEvent evt ) {
                                                     if( PropWithDefaultPanel.PROP_VALUE.equals( evt.getPropertyName() ) ) {
                                                         Font newValue = (Font)fontPanel.getValue();
                                                         setValueImpl( Coloring.changeFont( value.coloring, newValue ) );
                                                     }
                                                 }
                                             } );
        add( fontPanel, gridBagConstraints1 );

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (12, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        fgColorPanel = new PropWithDefaultPanel( Color.class, colorEditorClass,
                       getBundleString("CEP_FgTitle"),       // NOI18N
                       getBundleString("ACSD_CEP_Fg"),       // NOI18N
                       getBundleString("CEP_FgTrans"),       // NOI18N
                       getBundleString("CEP_FgTrans_Mnemonic").charAt(0),  // NOI18N
                       getBundleString("ACSD_CEP_FgTrans")); // NOI18N
        fgColorPanel.addPropertyChangeListener( new PropertyChangeListener() {
                                                    public void propertyChange( PropertyChangeEvent evt ) {
                                                        if( PropWithDefaultPanel.PROP_VALUE.equals( evt.getPropertyName() ) ) {
                                                            Color newValue = (Color)fgColorPanel.getValue();
                                                            setValueImpl( Coloring.changeForeColor( value.coloring, newValue) );
                                                        }
                                                    }
                                                } );
        add( fgColorPanel, gridBagConstraints1 );

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (12, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        bgColorPanel = new PropWithDefaultPanel( Color.class, colorEditorClass,
                       getBundleString("CEP_BgTitle"),       // NOI18N
                       getBundleString("ACSD_CEP_Bg"),       // NOI18N
                       getBundleString("CEP_BgTrans"),       // NOI18N
                       getBundleString("CEP_BgTrans_Mnemonic").charAt(0),  // NOI18N
                       getBundleString("ACSD_CEP_BgTrans")); // NOI18N

        bgColorPanel.addPropertyChangeListener( new PropertyChangeListener() {
                                                    public void propertyChange( PropertyChangeEvent evt ) {
                                                        if( PropWithDefaultPanel.PROP_VALUE.equals( evt.getPropertyName() ) ) {
                                                            Color newValue = (Color)bgColorPanel.getValue();
                                                            setValueImpl( Coloring.changeBackColor( value.coloring, newValue) );
                                                        }
                                                    }
                                                } );
        add( bgColorPanel, gridBagConstraints1 );

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (12, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        JPanel previewPanel = new JPanel( new BorderLayout () );
        previewPanel.setBorder( new CompoundBorder(
                                    new TitledBorder( getBundleString("CEP_PreviewTitle" ) ), // NOI18N
                                    new EmptyBorder( new Insets( 9, 12, 11, 11) )
                                ) );

        preview = new ColoringPreview();
        preview.getAccessibleContext().setAccessibleName(getBundleString("ACSN_CEP_Preview"));
        preview.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_CEP_Preview"));
        previewPanel.add( preview );
        add( previewPanel, gridBagConstraints1 );

        updateEditors();

        Dimension small = getPreferredSize();
        small.width *= 2;
        small.height *= 1.4;
        setPreferredSize( small );
    }
    
    private String getBundleString(String s) {
        return org.openide.util.NbBundle.getMessage(ColoringEditorPanel.class, s);
    }

    /**
     *  Used by underlaying ColoringEditor to query actual Coloring
     */
    public ColoringBean getValue() {
        return value;
    }

    /**
     * Used to adjust current coloring from underlaying
     * ColoringEditor - initial setup of displayed / edited value
     */
    public void setValue( ColoringBean value ) {
        if( (value == null) || (value.coloring == null) ) {
            return;
        }

        if( this.value != value ) {
            this.value = value;
            updateEditors();
            preview.setValue( value );

            firePropertyChange( "value", null, null ); // NOI18N
        }
    }


    private void setValueImpl( Coloring newColoring ) {

        value = value.changeColoring( newColoring );

        preview.setValue( value );
        //repaint();

        firePropertyChange( "value", null, null ); // NOI18N
    }


    private void updateEditors() {
        if( value == null ) {
            return;
        }

        fontPanel.setValue( value.coloring.getFont() );
        fontPanel.setDefaultValue( value.defaultColoring.getFont() );
        fontPanel.setDefault( value.isDefault );

        fgColorPanel.setValue( value.coloring.getForeColor() );
        fgColorPanel.setDefaultValue( value.defaultColoring.getForeColor() );
        fgColorPanel.setDefault( value.isDefault );

        bgColorPanel.setValue( value.coloring.getBackColor() );
        bgColorPanel.setDefaultValue( value.defaultColoring.getBackColor() );
        bgColorPanel.setDefault( value.isDefault );
    }


    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.GridBagLayout());

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    //----------------------------------------------------------------

    private class ColoringPreview extends javax.swing.JComponent implements Accessible {
        ColoringBean value;

        void setValue( ColoringBean c ) {
            value = c;
            this.repaint();
        }

        public void paint( java.awt.Graphics g ) {
            if (value != null) {
                Coloring coloring = value.coloring.apply( value.defaultColoring );

                java.awt.Rectangle box = this.getBounds();

                // clear background
                g.setColor(coloring.getBackColor());
                g.fillRect(0, 0 /*box.x, box.y*/, box.width - 1, box.height - 1);

                // draw example text
                g.setColor(coloring.getForeColor());
                g.setFont(coloring.getFont());
                FontMetrics fm = g.getFontMetrics();
                int x = Math.max((box.width - fm.stringWidth(value.example)) / 2, 0);
                int y = Math.max((box.height - fm.getHeight()) / 2 + fm.getAscent(), 0);
                g.drawString(value.example, x, y);
            }
        }
        
        public AccessibleContext getAccessibleContext () {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleJComponent() {
                    public AccessibleRole getAccessibleRole() {
                        return AccessibleRole.PANEL;
                    }
                };
            }
            return accessibleContext;
        }
        
    }

    //-------------------------------------------------
    private static class PropWithDefaultPanel extends JPanel {

        public static final String PROP_VALUE = "RealValue"; // NOI18N

        Object value;
        Object defaultValue;
        boolean isDefault;

        PropertyModel model;
        JCheckBox defaultCheckBox;

        public PropWithDefaultPanel( Class propertyClass, Class propertyEditorClass, String title, String description,
                                     String checkBoxTitle, char checkBoxMnemonic, String checkBoxDescription) {

            setLayout( new java.awt.BorderLayout());
            setBorder( new CompoundBorder( new TitledBorder( title ),
                                           new EmptyBorder( new Insets( 8, 12, 6, 11) ) ) );

            model = new PropertyModelSupport( propertyClass, propertyEditorClass, title );
            model.addPropertyChangeListener( new PropertyChangeListener() {
                                                 public void propertyChange( PropertyChangeEvent evt ) {
                                                     if( PropertyModelSupport.PROP_MOD_VALUE.equals( evt.getPropertyName() ) ) {

                                                         Object newValue = null;
                                                         try {
                                                             newValue = model.getValue();
                                                         } catch( InvocationTargetException e ) {
                                                             if( Boolean.getBoolean( "org.netbeans.exceptions" ) ) e.printStackTrace();   // NOI18N
                                                         }

                                                         if( value == null && newValue.equals( defaultValue ) ) {  // setValue( null ) or setDefaultValue( )
                                                             return; // void change
                                                         }

                                                         value = newValue;
                                                         defaultCheckBox.setSelected( false ); // uncheck default
                                                         firePropertyChange( PROP_VALUE, null, null );
                                                         repaint(); // XXX - Hack for PropertyPanel not updating
                                                     }
                                                 }
                                             } );
            PropertyPanel pp = new PropertyPanel (model, 0);
            pp.setMinimumSize(new Dimension(50,22));
            pp.getAccessibleContext().setAccessibleName(title);
            pp.getAccessibleContext().setAccessibleDescription(description);
            add (pp, BorderLayout.CENTER);
            

            defaultCheckBox = new JCheckBox();
            defaultCheckBox.setText(checkBoxTitle);
            defaultCheckBox.setMnemonic(checkBoxMnemonic);
            defaultCheckBox.getAccessibleContext().setAccessibleDescription(checkBoxDescription);
            defaultCheckBox.addActionListener( new ActionListener() {
                                                   public void actionPerformed( ActionEvent evt ) {
                                                       if( !defaultCheckBox.isSelected() ) { // unchecked - set real value
                                                           value = defaultValue;
                                                           modelSetValue( defaultValue );
                                                           firePropertyChange( PROP_VALUE, null, null );
                                                           
                                                       } else { // checked, provide model with default color
                                                           value = null;
                                                           modelSetValue( defaultValue );
                                                           firePropertyChange( PROP_VALUE, null, null );
                                                       }
                                                   }
                                               } );
            add( defaultCheckBox, BorderLayout.SOUTH );
        }

        public void firePropertyChange( String s, Object old, Object newVal ) {
            super.firePropertyChange( s, old, newVal );
        }


        public void setValue( Object value ) {
            this.value = value;
            if( value == null ) {
                modelSetValue( defaultValue );
                defaultCheckBox.setSelected( true );
            } else {
                modelSetValue( value );
                defaultCheckBox.setSelected( false );
            }
        }

        Object getValue() {
            return value;
        }

        public void setDefaultValue( Object def ) {
            //System.err.println( "Got setDefaultValue( " + def + " )" );
            defaultValue = def;
            if( value == null ) modelSetValue( defaultValue );
        }

        public void setDefault( boolean isDefault ) {
            this.isDefault = isDefault;
        }

        public void addNotify() {
            super.addNotify();
            if (isDefault) {
                defaultCheckBox.setEnabled(false);
                defaultCheckBox.setSelected(false);
            }
        }

        private void modelSetValue( Object val ) {
            try {
                model.setValue( val );
            } catch( InvocationTargetException e ) {
                if( Boolean.getBoolean( "org.netbeans.exceptions" ) ) e.printStackTrace();   // NOI18N
            }
            repaint(); // XXX - hack for updating PropertyPanel
        }

        private class PropertyModelSupport implements ExPropertyModel {

            public static final String PROP_MOD_VALUE = "value"; // NOI18N

            /** support for the properties changes. */
            private PropertyChangeSupport support;

            Class type;
            Class editor;
            Object value;
            String displayName;

            public PropertyModelSupport( Class propertyType, Class propertyEditor, String displayName ) {
                support = new PropertyChangeSupport(this);
                this.type = propertyType;
                this.editor = propertyEditor;
                this.displayName = displayName;
            }

            public Class getPropertyType() {
                return type;
            }

            public Class getPropertyEditorClass() {
                return editor;
            }

            public Object getValue() {
                return value;
            }

            public void setValue(Object v) {
                //System.err.println("  PropModel: got setValue( " + v + " )" );
                if( v != null && (!v.equals( value )) ) {
                    //System.err.println("    propagating" );
                    value = v;
                    support.firePropertyChange( PROP_MOD_VALUE, null, null );
                }
            }


            /** Adds listener to change of the value.
             */                                                                           
            public void addPropertyChangeListener(PropertyChangeListener l) {
                support.addPropertyChangeListener(l);
            }

            /** Removes listener to change of the value.
             */                                                                           
            public void removePropertyChangeListener(PropertyChangeListener l) {
                support.removePropertyChangeListener(l);
            }
            
            public Object[] getBeans() {
                return new Object[0];
            }
            
            public FeatureDescriptor getFeatureDescriptor() {
                FeatureDescriptor desc = new FeatureDescriptor();
                desc.setDisplayName(displayName);
                if (editor == PropertyEditorManager.findEditor(Font.class).getClass()){
                    //#39916
                    desc.setValue("canEditAsText", Boolean.FALSE); //NOI18N 
                }
                return desc;
            }
            
        }

    }

}
