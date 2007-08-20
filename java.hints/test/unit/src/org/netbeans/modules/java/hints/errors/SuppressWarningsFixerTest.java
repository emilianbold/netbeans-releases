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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;

/**
 *
 * @author Jan Lahoda
 */
public class SuppressWarningsFixerTest extends HintsTestBase {
    
    public SuppressWarningsFixerTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.doSetUp("org/netbeans/modules/java/hints/resources/layer.xml");
        CompilerSettings.getNode().putBoolean(CompilerSettings.ENABLE_LINT, true);
        CompilerSettings.getNode().putBoolean(CompilerSettings.ENABLE_LINT_DEPRECATION, true);
        CompilerSettings.getNode().putBoolean(CompilerSettings.ENABLE_LINT_FALLTHROUGH, true);
        CompilerSettings.getNode().putBoolean(CompilerSettings.ENABLE_LINT_UNCHECKED, true);
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/SuppressWarningsFixerTest/";
    }
    
    public void testSuppressWarnings1() throws Exception {
        performTest("Test", "unchecked", 8, 5);
    }
    
    public void testSuppressWarnings2() throws Exception {
        performTest("Test", "unchecked", 11, 5);
    }
    
    public void testSuppressWarnings3() throws Exception {
        performTest("Test", "unchecked", 16, 5);
    }
    
    public void testSuppressWarnings4() throws Exception {
        performTest("Test", "unchecked", 22, 5);
    }
    
    public void testSuppressWarnings5() throws Exception {
        performTest("Test", "unchecked", 28, 5);
    }
    
    public void testSuppressWarnings6() throws Exception {
        performTest("Test", "unchecked", 35, 5);
    }
    
    public void testSuppressWarnings7() throws Exception {
        performTest("Test2", "unchecked", 10, 5);
    }
    
    public void testSuppressWarnings8() throws Exception {
        performTest("Test2", "unchecked", 16, 5);
    }
    
    public void testSuppressWarnings9() throws Exception {
        performTest("Test2", "unchecked", 22, 5);
    }
    
    public void testSuppressWarnings10() throws Exception {
        performTestDoNotPerform("Test2", 31, 5);
    }
    
    public void testSuppressWarnings11() throws Exception {
        performTestDoNotPerform("Test2", 38, 5);
    }
    
    public void testSuppressWarnings106794() throws Exception {
	performTestDoNotPerform("Test3", 3, 10);
    }
    
}
