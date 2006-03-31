/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.explorer.view;

import java.lang.reflect.InvocationTargetException;
import javax.swing.JCheckBox;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;

/*
 * Tests for class NodeTableModelTest
 */
public class NodeTableModelTest extends NbTestCase {
    
    public NodeTableModelTest(String name) {
        super(name);
    }
   
    protected boolean runInEQ() {
        return true;
    }
    
    public void testMakeAccessibleCheckBox() {
        MyNodeTableModel model = new MyNodeTableModel( 0 );
        
        MyProperty p;
        JCheckBox checkBox;

        p = new MyProperty();
        p.setDisplayName( "displayName1" );
        p.setShortDescription( "shortDescription1" );
        p.setValue( "ColumnMnemonicCharTTV", "" );
        
        checkBox = new JCheckBox( "displayName" );
        model.makeAccessibleCheckBox( checkBox, p );
        assertEquals( "Invalid accessible name", checkBox.getAccessibleContext().getAccessibleName(), p.getDisplayName() );
        assertEquals( "Invalid accessible description", checkBox.getAccessibleContext().getAccessibleDescription(), p.getShortDescription() );
        assertEquals( "Invalid mnemonic", checkBox.getMnemonic(), 0 );

        
        p = new MyProperty();
        p.setDisplayName( "displayName" );
        p.setShortDescription( "shortDescription2" );
        p.setValue( "ColumnMnemonicCharTTV", "d" );
        
        checkBox = new JCheckBox( "displayName2" );
        model.makeAccessibleCheckBox( checkBox, p );
        assertEquals( "Invalid accessible name", checkBox.getAccessibleContext().getAccessibleName(), p.getDisplayName() );
        assertEquals( "Invalid accessible description", checkBox.getAccessibleContext().getAccessibleDescription(), p.getShortDescription() );
        assertEquals( "Invalid mnemonic", checkBox.getMnemonic(), 'D' );

        
        p = new MyProperty();
        p.setDisplayName( "displayName3" );
        p.setShortDescription( "shortDescription3" );
        p.setValue( "ColumnMnemonicCharTTV", "N" );
        
        checkBox = new JCheckBox( "displayName" );
        model.makeAccessibleCheckBox( checkBox, p );
        assertEquals( "Invalid accessible name", checkBox.getAccessibleContext().getAccessibleName(), p.getDisplayName() );
        assertEquals( "Invalid accessible description", checkBox.getAccessibleContext().getAccessibleDescription(), p.getShortDescription() );
        assertEquals( "Invalid mnemonic", checkBox.getMnemonic(), 'N' );

        
        p = new NullGetValueProperty();
        p.setDisplayName( "displayName4" );
        p.setShortDescription( "shortDescription4" );
        
        checkBox = new JCheckBox( "displayName" );
        model.makeAccessibleCheckBox( checkBox, p );
        assertEquals( "Invalid accessible name", checkBox.getAccessibleContext().getAccessibleName(), p.getDisplayName() );
        assertEquals( "Invalid accessible description", checkBox.getAccessibleContext().getAccessibleDescription(), p.getShortDescription() );
        assertEquals( "Invalid mnemonic", checkBox.getMnemonic(), 0 );
    }
    

    private static class MyNodeTableModel extends NodeTableModel {
        public MyNodeTableModel( int columnCount ) {
            this.allPropertyColumns = new NodeTableModel.ArrayColumn[columnCount];
            for( int i=0; i<allPropertyColumns.length; i++ ) {
                allPropertyColumns[i] = new NodeTableModel.ArrayColumn();
                allPropertyColumns[i].setProperty( new MyProperty() );
            }
        }
        
        Node.Property getProperty( int index ) {
            return allPropertyColumns[index].getProperty();
        }
        
        void setProperty( int index, Node.Property p ) {
            allPropertyColumns[index].setProperty( p );
        }
    }
    
    private static class MyProperty extends Node.Property {
        public MyProperty() {
            super( Object.class );
        }
        
        public void setValue(Object val) 
            throws IllegalAccessException, 
                IllegalArgumentException, 
                InvocationTargetException {
        }

        public Object getValue() 
            throws IllegalAccessException, 
                InvocationTargetException {
            return null;
        }

        public boolean canWrite() {
            return true;
        }

        public boolean canRead() {
            return true;
        }
    }
    
    private static class NullGetValueProperty extends MyProperty {
        public Object getValue(String attributeName) {
            return null;
        }
    }
}
