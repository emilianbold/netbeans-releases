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

package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComboBox;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.PropertySupport;

/** Tests basic functionality of InplaceEditorFactory and its code to
 *  correctly configure a property editor and associated InplaceEditor
 *  with the data encapsulated by a Node.Property.
 *
 * @author Tim Boudreau
 */

public class InplaceEditorFactoryTest extends NbTestCase {
    public InplaceEditorFactoryTest(String name) {
        super(name);
    }
    
    Component edComp = null;
    PropertyEditor ped = null;
    InplaceEditor ied = null;
    
    protected void setUp() throws Exception {
        PropUtils.forceRadioButtons=false;
        // Create new TestProperty
        tp = new TProperty("TProperty", true);
        // Create new TEditor
        te = new TagsEditor();
        
        try {
            ied = new InplaceEditorFactory(true, new ReusablePropertyEnv()).getInplaceEditor(tp, false);
            edComp = ied.getComponent();
            ped = ied.getPropertyEditor();
        } catch (Exception e) {
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
    }
    
    public void testInplaceIsCombo() throws Exception {
        assertTrue("Editor for tagged value not a combo box", edComp instanceof JComboBox);
    }
    
    public void testCorrectInplaceEditorValue() throws Exception {
        assertTrue("InplaceEditor.getValue() returns " + ied.getValue() + " should be \"Value\"", "Value".equals(ied.getValue()));
    }
    
    public void testCorrectPropertyEditorValue() throws Exception {
        assertTrue("PropertyEditor.getValue() returns " + ped.getValue() + " should be \"Value\"", "Value".equals(ped.getValue()));
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private String myValue = "Value";
        // Create new Property
        public TProperty(String name, boolean isWriteable) {
            super(name, String.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value.toString();
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return te;
        }
    }
    
    public class TagsEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public TagsEditor() {
        }
        
        public String[] getTags() {
            return new String[] {"a","b","c","d","Value"};
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
    }
    
    private TProperty tp;
    private TagsEditor te;
    private String initEditorValue;
    private String initPropertyValue;
    private String postChangePropertyValue;
    private String postChangeEditorValue;
}
