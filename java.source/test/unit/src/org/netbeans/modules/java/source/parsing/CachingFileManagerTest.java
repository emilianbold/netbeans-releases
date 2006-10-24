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

package org.netbeans.modules.java.source.parsing;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.modules.java.source.util.Iterators;

/**
 *
 * @author Petr Hrebejk
 */
public class CachingFileManagerTest extends TestCase {

    public CachingFileManagerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(CachingFileManagerTest.class);
        
        return suite;
    }

    /*
    public void testList() {
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    public void testGetFileForInput() {
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    public void testGetFileForOutput() throws Exception {
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    public void testSetLocation() {
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    public void testFlush() throws Exception {
        fail("The test case is empty.");
    }

    public void testClose() throws Exception {
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    public void testGetInputFile() {
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    public void testIsWritable() {
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    public void testGetFileObjects() {
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    */
}
