/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

import junit.framework.TestCase;

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
    
    public CharsetDisplayPreferenceEditorTest(String testName) {
        super(testName);
    }
    
}
