/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import junit.framework.*;
import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.nodes.Node;

/**
 *
 * @author jarda
 */
public class StringArrayEditorTest extends TestCase {
    static {
        PropertyEditorManager.registerEditor (String[].class, StringArrayEditor.class);
    }
    
    public StringArrayEditorTest (String testName) {
        super (testName);
    }
    
    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }

    public static Test suite () {
        TestSuite suite = new TestSuite(StringArrayEditorTest.class);
        
        return suite;
    }

 
    public void testTheEditorHonoursSeparatorAttribute () throws Exception {
        NP np = new NP ();
        np.setValue ("item.separator", "-");
        
        PropertyEditor p = np.getPropertyEditor ();
        assertNotNull ("There is some editor", p);
        assertEquals ("It is StringArrayEditor", StringArrayEditor.class, p.getClass ());
        ((StringArrayEditor)p).readEnv (np);
        
        p.setAsText ("A-B");
        
        String[] value = (String[])p.getValue ();
        
        assertNotNull ("Values is there", value);
        if (value.length != 2 || !"A".equals (value[0]) || !"B".equals(value[1])) {
            fail ("Unexpected arrays: " + Arrays.asList (value));
        }
        
        p.setValue (new String[] { "X", "Y" });
        String t = np.getPropertyEditor ().getAsText ();
        if (!"X- Y".equals (t)) {
            fail ("Wrong text: " + t);
        }
    }
    
    class NP extends Node.Property {
        public String[] value;
        
        public NP () {
            super (String[].class);
        }

        public void setValue (Object val) throws IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            value = (String[])val;
        }

        public Object getValue () throws IllegalAccessException, java.lang.reflect.InvocationTargetException {
            return value;
        }

        public boolean canWrite () {
            return true;
        }

        public boolean canRead () {
            return true;
        }
        
        
    }
}
