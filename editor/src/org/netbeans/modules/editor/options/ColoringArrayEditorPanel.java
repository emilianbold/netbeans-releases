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
import java.beans.*;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.SwingUtilities;

import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.explorer.propertysheet.PropertyModel;

import org.netbeans.editor.Coloring;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.SettingsNames;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * ColoringArrayEditorPanel is custom property editor operating over HashMap
 * containing (String)name:(Coloring)value pairs. Special name=null is used
 * to identify default coloring.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class ColoringArrayEditorPanel extends javax.swing.JPanel {

    /** Editor interface for visual editing of single coloring */
    PropertyModel coloringModel;

    /** Index of Coloring actually edited/displayed by coloringModel */
    int actValueIndex;
    // Bug #18539 temporary index value
    int newValueIndex;


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
        typeName = BaseOptions.BASE;

        value = new HashMap();
        names = new String[] { SettingsNames.DEFAULT_COLORING };

        actValueIndex = 0;
        value.put(names[0], new Coloring( Font.decode( null ), Color.red, Color.blue ) );

        initComponents ();

        getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_CAEP_Panel")); // NOI18N
        syntaxList.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_CAEP_Syntax")); // NOI18N

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
                                // Bug #18539 Hack to prevent changing selected  index by firePropertyChange fired below
                                actValueIndex = newValueIndex;
                                ColoringArrayEditorPanel.this.firePropertyChange( "value", null, null ); // NOI18N
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
    }

    private String getBundleString(String s) {
        return NbBundle.getMessage(ColoringArrayEditorPanel.class, s);
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
            ClassLoader l = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            Class kitClass = Class.forName( (String)map.get( null ), true, l );
            typeName = OptionSupport.getTypeName( kitClass );
        } catch( ClassNotFoundException e ) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
            return;
        }

        Set keySet = map.keySet();
        HashMap tempMap = new HashMap(keySet.size() - 1);
        String[] names = new String[keySet.size() - 1];

        Iterator iter = keySet.iterator();
        String defaultName = null;
        while (iter.hasNext()){
            String name = (String) iter.next();
            if (name == null) continue;
            String visualName = LocaleSupport.getString( "NAME_coloring_"  + name ); // NOI18N
            if( visualName == null )
                visualName = LocaleSupport.getString("NAME_coloring_" + BaseOptions.BASE + "-" + name, name ); // NOI18N
            if (name == SettingsNames.DEFAULT_COLORING) defaultName = visualName;
            tempMap.put(visualName, name);
        }
        
        List visualNamesList = new ArrayList(tempMap.keySet());
        
        Collections.sort(visualNamesList);

        if (defaultName!=null){
            boolean removed = visualNamesList.remove(defaultName);
            if (removed){
                visualNamesList.add(0, defaultName);
            }
        }
        
        for (int i = 0; i<visualNamesList.size(); i++){
            names[i] = (String)tempMap.get(visualNamesList.get(i));
        }
        
        this.names = names;
        
        syntaxList.setListData(new Vector(visualNamesList));
        if( oldIndex < visualNamesList.size() ) actValueIndex = oldIndex;
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


    /** This method is called from within the constructor to initialize the form. */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        detailPanel = new javax.swing.JPanel();
        masterPanel = new javax.swing.JPanel();
        syntaxLabel = new javax.swing.JLabel();
        syntaxScroll = new javax.swing.JScrollPane();
        syntaxList = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        detailPanel.setLayout(new java.awt.GridLayout(1, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 11, 11);
        add(detailPanel, gridBagConstraints);

        masterPanel.setLayout(new java.awt.GridBagLayout());

        syntaxLabel.setLabelFor(syntaxList);
        org.openide.awt.Mnemonics.setLocalizedText(syntaxLabel, getBundleString("CAEP_SyntaxLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        masterPanel.add(syntaxLabel, gridBagConstraints);

        syntaxList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                syntaxListValueChanged(evt);
            }
        });
        syntaxScroll.setViewportView(syntaxList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        masterPanel.add(syntaxScroll, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 12);
        add(masterPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void syntaxListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_syntaxListValueChanged
        // Bug #18539 invoking List value change after property sheet changes the property value
        if( syntaxList.getSelectedIndex() < 0 )
            return;
        if( actValueIndex != syntaxList.getSelectedIndex()) {
            newValueIndex = syntaxList.getSelectedIndex();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    actValueIndex = newValueIndex;
                    setEditorValue( actValueIndex );
                }
            });
        }else{
            actValueIndex = syntaxList.getSelectedIndex();
            setEditorValue( actValueIndex );
        }
    }//GEN-LAST:event_syntaxListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel detailPanel;
    private javax.swing.JPanel masterPanel;
    private javax.swing.JLabel syntaxLabel;
    private javax.swing.JList syntaxList;
    private javax.swing.JScrollPane syntaxScroll;
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
