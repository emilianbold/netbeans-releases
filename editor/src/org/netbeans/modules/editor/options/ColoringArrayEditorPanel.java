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
import java.beans.*;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.lang.reflect.InvocationTargetException;

import org.openide.TopManager;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.util.HelpCtx;

import org.netbeans.editor.Coloring;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.SettingsNames;

/**
 * ColoringArrayEditorPanel is custom property editor operating over HashMap 
 * containing (String)name:(Coloring)value pairs. Special name=null is used
 * to identify default coloring.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class ColoringArrayEditorPanel extends javax.swing.JPanel {

    /** Access to our localized texts */
    static java.util.ResourceBundle bundle =
        org.openide.util.NbBundle.getBundle( ColoringArrayEditorPanel.class );

    /** support for distributing change events. */
    private PropertyChangeSupport support;

    /** Editor interface for visual editing of single coloring */
    PropertyModel coloringModel;

    /** Index of Coloring actually edited/displayed by coloringModel */
    int actValueIndex;

    /** Name of the the type of the kit, which coloring we're editing */
    private String typeName;

    /** Names of coloring obtained from HashMap, names[0] is 'default' in given locale  */
    private String names[];

    /** Table of all edited colorings, colorings[0] is default coloring */
    //  private Coloring[] colorings;

    /** HashMap hodling our value. Changes are made by modifying this HashMap */
    private HashMap value;

    /** Creates new form ColoringArrayEditorPanel */
    public ColoringArrayEditorPanel() {
        support = new PropertyChangeSupport(this);
        typeName = BaseOptions.BASE;

        value = new HashMap();
        names = new String[] { SettingsNames.DEFAULT_COLORING };

        actValueIndex = 0;
        value.put(names[0], new Coloring( Font.decode( null ), Color.red, Color.blue ) );

        initComponents ();

        coloringModel = new PropertyModelSupport( ColoringBean.class, ColoringEditor.class);
        coloringModel.addPropertyChangeListener( new PropertyChangeListener() {
                    public void propertyChange( PropertyChangeEvent evt ) {
                        try {
                            Coloring newColoring = ((ColoringBean)coloringModel.getValue()).coloring;
                            if( ! newColoring.equals( value.get( names[actValueIndex] ) ) ) {
                                //System.err.println("updating coloring[" + actValueIndex + "] from " + value.get( names[actValueIndex] ) + " to " + newColoring ); // NOI18N
                                //Need to recreate value here (because of equals(), then set!
                                value = (HashMap)value.clone();
                                value.put( names[actValueIndex], newColoring );
                                support.firePropertyChange( "value", null, null ); // NOI18N
                            }
                        } catch( InvocationTargetException e ) {
                            if( Boolean.getBoolean( "org.netbeans.exceptions" ) ) e.printStackTrace();   // NOI18N
                        }
                    }
                });


        syntaxList.setSelectedIndex( actValueIndex );
        //    setEditorValue( actValueIndex );
        PropertyPanel editorPanel = new PropertyPanel( coloringModel,  PropertyPanel.PREF_CUSTOM_EDITOR );
        detailPanel.add( editorPanel, BorderLayout.CENTER );

        HelpCtx.setHelpIDString (this, ColoringArrayEditorPanel.class.getName ());
    }

    public HashMap getValue() {
        return value;
    }

    public void setValue( HashMap map ) {
        if( map == null ) return;

        int oldIndex = actValueIndex;

        value = map;

        // Obtain name of the kits type
        try {
            Class kitClass = Class.forName( (String)map.get( null ), true, TopManager.getDefault ().systemClassLoader () );
            typeName = OptionSupport.getTypeName( kitClass );
        } catch( ClassNotFoundException e ) {
            if( Boolean.getBoolean( "netbeans.debug.exceptions" ) )
                e.printStackTrace();
            return;
        }

        Set keySet = map.keySet();

        String[] names = new String[keySet.size() - 1];
        String[] visualNames = new String[keySet.size() - 1];

        int i=0;
        Iterator iter = keySet.iterator();

        while( iter.hasNext() ) {
            String key = (String)iter.next();
            if( key == null ) continue;  // ignore the typeName item
            names[ (key == SettingsNames.DEFAULT_COLORING) ? 0 : (++i) ] = key;
        }

        for( i = 0; i < names.length; i++ ) {
            visualNames[i] = LocaleSupport.getString( "NAME_coloring_" /* + typeName + "_" */ + names[i] ); // NOI18N
            if( visualNames[i] == null )
                visualNames[i] = LocaleSupport.getString("NAME_coloring_" + BaseOptions.BASE + "-" + names[i], names[i] ); // NOI18N
        }

        this.names = names;
        syntaxList.setListData( visualNames );
        if( oldIndex < visualNames.length ) actValueIndex = oldIndex;
        else actValueIndex = 0;
        syntaxList.setSelectedIndex( actValueIndex );
    }

    void setEditorValue( int index ) {
        if( index < 0 ) return;

        String example = LocaleSupport.getString( "EXAMPLE_coloring_" /*+ typeName + "_" */ + names[index] ); // NOI18N
        if( example == null )
            example = LocaleSupport.getString("EXAMPLE_coloring_" + BaseOptions.BASE + "-" + names[index], names[index] ); // NOI18N

        Coloring c = (Coloring)value.get(names[index]);
        ColoringBean bean =
            new ColoringBean( (Coloring)value.get(names[index]), example, (Coloring)value.get(names[0]), index == 0 );

        try {
            coloringModel.setValue( bean );
        } catch( java.lang.reflect.InvocationTargetException e ) {
            if( Boolean.getBoolean( "org.netbeans.exceptions" ) ) e.printStackTrace();   // NOI18N
        }
    }


    /** Adds listener to change of the value. */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    /** Removes listener to change of the value. */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }


    /** This method is called from within the constructor to initialize the form. */
    private void initComponents() {//GEN-BEGIN:initComponents
        detailPanel = new javax.swing.JPanel();
        masterPanel = new javax.swing.JPanel();
        syntaxScroll = new javax.swing.JScrollPane();
        syntaxList = new javax.swing.JList();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        detailPanel.setLayout(new java.awt.GridLayout(1, 1));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(detailPanel, gridBagConstraints1);
        
        
        masterPanel.setLayout(new java.awt.GridLayout(1, 1));
        masterPanel.setBorder(new javax.swing.border.CompoundBorder( new javax.swing.border.TitledBorder(bundle.getString( "CAEP_SyntaxLabel" )), new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))));
        
        
          syntaxList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                    syntaxListValueChanged(evt);
                }
            }
            );
            syntaxScroll.setViewportView(syntaxList);
            
            masterPanel.add(syntaxScroll);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 8, 8, 8);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(masterPanel, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void syntaxListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_syntaxListValueChanged
        actValueIndex = syntaxList.getSelectedIndex();
        setEditorValue( actValueIndex );
    }//GEN-LAST:event_syntaxListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel detailPanel;
    private javax.swing.JPanel masterPanel;
    private javax.swing.JScrollPane syntaxScroll;
    private javax.swing.JList syntaxList;
    // End of variables declaration//GEN-END:variables

    private class PropertyModelSupport implements PropertyModel {

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
            if( v != null && (! v.equals( value ) ) ) {
                value = v;
                support.firePropertyChange( PROP_VALUE, null, null );
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
