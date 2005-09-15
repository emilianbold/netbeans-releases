/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import junit.framework.TestCase;
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

        public void setValue (Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            value = (String[])val;
        }

        public Object getValue () throws IllegalAccessException, InvocationTargetException {
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
