/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.explorer.propertysheet.PropertyModel;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.beaninfo.editors.FontEditor;
import org.netbeans.beaninfo.editors.ColorEditor;

/**
 * ColoringEditorPanel is custom property editor operating
 * over ColoringBean, it is interfaced only through ColoringEditor
 * @author  Petr Nejedly
 * 
 * TODO: remove repaints as Hans will repair PropertyPanel
 */
public class ColoringEditorPanel extends javax.swing.JPanel {

    public static final String PROP_COLORING = "Coloring";  // NOI18N

    /** Access to our localized texts */
    static java.util.ResourceBundle bundle =
        org.openide.util.NbBundle.getBundle( ColoringEditorPanel.class );

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
        gridBagConstraints1.insets = new java.awt.Insets (8, 8, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.3;
        fontPanel = new PropWithDefaultPanel( Font.class, FontEditor.class,
                                              bundle.getString( "CEP_FontTitle" ), bundle.getString( "CEP_FontTrans" ) );  // NOI18N
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
        gridBagConstraints1.insets = new java.awt.Insets (0, 8, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        fgColorPanel = new PropWithDefaultPanel( Color.class, ColorEditor.class,
                       bundle.getString( "CEP_FgTitle" ), bundle.getString( "CEP_FgTrans" ) );    // NOI18N
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
        gridBagConstraints1.insets = new java.awt.Insets (0, 8, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        bgColorPanel = new PropWithDefaultPanel( Color.class, ColorEditor.class,
                       bundle.getString( "CEP_BgTitle" ), bundle.getString( "CEP_BgTrans" ) );    // NOI18N
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
        gridBagConstraints1.insets = new java.awt.Insets (0, 8, 8, 8);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        JPanel previewPanel = new JPanel( new BorderLayout () );
        previewPanel.setBorder( new CompoundBorder(
                                    new TitledBorder( bundle.getString( "CEP_PreviewTitle" ) ), // NOI18N
                                    new EmptyBorder( new Insets( 8, 8, 8, 8 ) )
                                ) );

        preview = new ColoringPreview();
        previewPanel.add( preview );
        add( previewPanel, gridBagConstraints1 );

        updateEditors();

        Dimension small = getPreferredSize();
        small.width *= 2;
        small.height *= 1.4;
        setPreferredSize( small );
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


    private void initComponents () {//GEN-BEGIN:initComponents
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    //----------------------------------------------------------------

    private class ColoringPreview extends javax.swing.JComponent {
        ColoringBean value;

        void setValue( ColoringBean c ) {
            value = c;
            repaint();
        }

        public void paint( java.awt.Graphics g ) {
            if (value != null) {
                Coloring coloring = value.coloring.apply( value.defaultColoring );

                java.awt.Rectangle box = getBounds();

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
    }

    //-------------------------------------------------
    private static class PropWithDefaultPanel extends JPanel {

        public static final String PROP_VALUE = "RealValue";

        Object value;
        Object defaultValue;

        PropertyModel model;
        JCheckBox defaultCheckBox;

        public PropWithDefaultPanel( Class propertyClass, Class propertyEditorClass, String title, String checkBoxTitle ) {

            setLayout( new java.awt.BorderLayout() );
            setBorder( new CompoundBorder( new TitledBorder( title ),
                                           new EmptyBorder( new Insets( 8, 8, 8, 8 ) ) ) );

            model = new PropertyModelSupport( propertyClass, propertyEditorClass );
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

            add( new PropertyPanel( model, 0 ), BorderLayout.CENTER );

            defaultCheckBox = new JCheckBox();
            defaultCheckBox.setText( checkBoxTitle );
            defaultCheckBox.addActionListener( new ActionListener() {
                                                   public void actionPerformed( ActionEvent evt ) {
                                                       if( !defaultCheckBox.isSelected() ) { // unchecked - set real value
                                                           value = defaultValue;
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
            defaultCheckBox.setEnabled( ! isDefault );
            if( isDefault ) defaultCheckBox.setSelected( false );
        }

        private void modelSetValue( Object val ) {
            try {
                model.setValue( val );
            } catch( InvocationTargetException e ) {
                if( Boolean.getBoolean( "org.netbeans.exceptions" ) ) e.printStackTrace();   // NOI18N
            }
            repaint(); // XXX - hack for updating PropertyPanel
        }

        private class PropertyModelSupport implements PropertyModel {

            public static final String PROP_MOD_VALUE = "value";

            /** support for the properties changes. */
            private PropertyChangeSupport support;

            Class type;
            Class editor;
            Object value;

            public PropertyModelSupport( Class propertyType, Class propertyEditor ) {
                support = new PropertyChangeSupport(this);
                this.type = propertyType;
                this.editor = propertyEditor;
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
        }

    }

}

/*
 * Log
 *  20   Jaga      1.12.1.3.1.23/24/00  Miloslav Metelka 
 *  19   Jaga      1.12.1.3.1.13/21/00  Miloslav Metelka 
 *  18   Jaga      1.12.1.3.1.03/15/00  Miloslav Metelka after structural change
 *  17   Gandalf-post-FCS1.12.1.3    3/8/00   Petr Nejedly    fix for 
 *       firePropertyChange
 *  16   Gandalf-post-FCS1.12.1.2    3/1/00   Petr Nejedly    
 *  15   Gandalf-post-FCS1.12.1.1    2/29/00  Petr Nejedly    compilation fix
 *  14   Gandalf-post-FCS1.12.1.0    2/28/00  Petr Nejedly    Redesign of 
 *       ColoringEditor
 *  13   Gandalf   1.12        2/16/00  Petr Nejedly    Changed behaviour of 
 *       Inherited checkboxes
 *  12   Gandalf   1.11        1/13/00  Miloslav Metelka Localization
 *  11   Gandalf   1.10        1/11/00  Petr Nejedly    ScrollPane, distribution
 *       of changes
 *  10   Gandalf   1.9         1/4/00   Miloslav Metelka 
 *  9    Gandalf   1.8         12/28/99 Miloslav Metelka 
 *  8    Gandalf   1.7         11/14/99 Miloslav Metelka 
 *  7    Gandalf   1.6         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         7/29/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/26/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
