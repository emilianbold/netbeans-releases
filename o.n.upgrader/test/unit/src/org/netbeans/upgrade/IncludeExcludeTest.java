/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.upgrade;

import java.util.*;

/** Tests to check that copy of files works.
 *
 * @author Jaroslav Tulach
 */
public final class IncludeExcludeTest extends org.netbeans.junit.NbTestCase {
    private Set includeExclude;
    
    public IncludeExcludeTest (String name) {
        super (name);
    }
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new junit.framework.TestSuite (IncludeExcludeTest.class));
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();

        String reader = "# ignore comment\n" +
        "include one/file.txt\n" +
        "include two/dir/.*\n" +
        "\n" +
        "exclude two/dir/sub/.*\n";
        
        includeExclude = IncludeExclude.create (new java.io.StringReader (reader));
    }    

    public void testOneFileIsThere () {
        assertTrue (includeExclude.contains ("one/file.txt"));
    }
    
    public void testDoesNotContainRoot () {
        assertFalse (includeExclude.contains (""));
    }
    
    public void testContainsSomethingInDir () {
        assertTrue (includeExclude.contains ("two/dir/a.file"));
    }
    
    public void testContainsSomethingUnderTheDir () {
        assertTrue (includeExclude.contains ("two/dir/some/folder/a.file"));
    }
    
    public void testDoesNotContainSubDir () {
        assertFalse (includeExclude.contains ("two/dir/sub/not.there"));
    }
    
    public void testWrongContentDetected () {
        try {
            IncludeExclude.create (new java.io.StringReader ("some strange line"));
            fail ("Should throw exception");
        } catch (java.io.IOException ex) {
        }
    }
 }
