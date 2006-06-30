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

package org.netbeans.modules.queries;

import java.io.File;
import org.netbeans.junit.NbTestCase;

/**
 * Test of ParentChildCollocationQuery impl.
 *
 * @author David Konecny
 */
public class ParentChildCollocationQueryTest extends NbTestCase {

    public ParentChildCollocationQueryTest(String testName) {
        super(testName);
    }

    public void testAreCollocated() throws Exception {
        clearWorkDir();
        File base = getWorkDir();
        File proj1 = new File(base, "proj1");
        proj1.mkdirs();
        File proj3 = new File(proj1, "proj3");
        proj3.mkdirs();
        File proj2 = new File(base, "proj2");
        proj2.mkdirs();
        
        ParentChildCollocationQuery query = new ParentChildCollocationQuery();
        assertTrue("Must be collocated", query.areCollocated(proj1, proj3));
        assertTrue("Must be collocated", query.areCollocated(proj3, proj1));
        assertFalse("Cannot be collocated", query.areCollocated(proj1, proj2));
        assertFalse("Cannot be collocated", query.areCollocated(proj2, proj1));
        
        // folder does not exist:
        File proj4 = new File(base, "proj");
        assertFalse("Cannot be collocated", query.areCollocated(proj1, proj4));
        assertFalse("Cannot be collocated", query.areCollocated(proj4, proj1));
        proj4.mkdirs();
        assertFalse("Cannot be collocated", query.areCollocated(proj1, proj4));
        assertFalse("Cannot be collocated", query.areCollocated(proj4, proj1));
        
        // files do not exist:
        File file1 = new File(base, "file1.txt");
        File file2 = new File(base, "file1");
        assertFalse("Cannot be collocated", query.areCollocated(file1, file2));
        assertFalse("Cannot be collocated", query.areCollocated(file2, file1));
        
        // passing the same parameter
        assertTrue("A file must be collocated with itself", query.areCollocated(proj1, proj1));
    }

}
