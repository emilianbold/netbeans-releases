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

package org.netbeans.upgrade;

import java.util.Set;
import org.netbeans.junit.NbTestCase;

/** Tests to check that copy of files works.
 *
 * @author Jaroslav Tulach
 */
public final class IncludeExcludeTest extends NbTestCase {
    private Set includeExclude;

    public IncludeExcludeTest (String name) {
        super (name);
    }

    protected void setUp() throws Exception {
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
