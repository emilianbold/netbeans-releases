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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;

/**
 *
 * @author Jan Lahoda
 */
public class OrigCreateMethodTest extends HintsTestBase {

    public OrigCreateMethodTest(String name) {
        super(name);
    }
    
    public void testCreateElement1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement1", "Method", 23, 16);
    }

    public void testCreateElement2() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement2", "Method", 23, 16);
    }

    public void testCreateElement3() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement3", "Method", 24, 16);
    }

    public void testCreateElement4() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement4", "Method", 23, 16);
    }

    public void testCreateElement5() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement5", "Method", 23, 16);
    }

    public void testCreateElement6() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement6", "Method", 23, 16);
    }

    public void testCreateElement7() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement7", "Method", 23, 16);
    }

    public void testCreateElement8() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement8", "Method", 24, 16);
    }

    public void testCreateElement9() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElement9", "Method", 23, 16);
    }

    public void testCreateElementa() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateElementa", "Method", 23, 16);
    }
    
    public void testCreateConstructor1() throws Exception {
        performTest("org.netbeans.test.java.hints.CreateConstructor1", "Constructor", 9, 16);
    }

    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/OrigCreateMethodTest/";
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
}
