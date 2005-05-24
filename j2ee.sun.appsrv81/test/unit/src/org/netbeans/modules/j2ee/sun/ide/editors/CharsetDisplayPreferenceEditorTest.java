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
/*
 * CharsetDisplayPreferenceEditorTest.java
 * JUnit based test
 *
 * Created on March 19, 2004, 1:54 PM
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

import junit.framework.*;
import org.openide.util.NbBundle;

/**
 *
 * @author vkraemer
 */
public class CharsetDisplayPreferenceEditorTest extends TestCase {
    
    public void testGetSetAsString() {
        CharsetDisplayPreferenceEditor foo =
            new CharsetDisplayPreferenceEditor();
        
        String ret = null;
        foo.setAsText(foo.choices[0]);
        ret = foo.getAsText();
        assertEquals(foo.choices[0], ret);
        foo.setAsText(foo.choices[1]);
        ret = foo.getAsText();
        assertEquals(foo.choices[1], ret);
        foo.setAsText(foo.choices[2]);
        ret = foo.getAsText();
        assertEquals(foo.choices[2], ret);
        foo.setAsText("bogus");
        ret = foo.getAsText();
        assertEquals(foo.choices[1], ret);
    }
        
    
    public void testGetSet() {
        CharsetDisplayPreferenceEditor foo =
            new CharsetDisplayPreferenceEditor();

        Integer ret = null;
        foo.setValue(Integer.valueOf("0"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("0"), ret);
        foo.setValue(Integer.valueOf("1"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("1"),ret);
        foo.setValue(Integer.valueOf("2"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("2"), ret);
        foo.setValue(Integer.valueOf("3"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("1"),ret);
        foo.setValue(Integer.valueOf("-1"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("1"), ret );
        
    }
        
    
    public void testCreate() {
        CharsetDisplayPreferenceEditor foo =
            new CharsetDisplayPreferenceEditor();
    }
    
    public CharsetDisplayPreferenceEditorTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CharsetDisplayPreferenceEditorTest.class);
        return suite;
    }
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
