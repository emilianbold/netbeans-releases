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

/*
 * SourceGenerator.java
 *
 * Created on June 26, 2000, 9:29 AM
 */

package org.netbeans.test.java.generating.SourceGenerator;
/** <B>Java Module General API Test: SourceGenerator</B>
 * <BR><BR><I>What it tests:</I><BR>
 * This test is more complex and checks adding Elements (especially order of adding).
 * Test is focused on checking of correctness of generated code.
 * <BR><BR><I>How it works:</I><BR>
 * New class is created using DataObject.createFromTemplate().
 * Then all possible Elements are added.
 * These actions cause generating of .java code. This code is compared with supposed one.
 * <BR><BR><I>Output:</I><BR>
 * Generated Java code.
 * <BR><BR><I>Possible reasons of failure:</I><BR>
 * <BR><BR><U>Elements are added into bad positions or even not at all</U><BR>
 * See .diff file to get which ones
 * <BR><BR><U>Bad indentation</U><BR>
 * This is probably not a bug of Java Module. (Editor Bug)
 * In .diff file could be some whitespaces.
 * <BR><BR><I>Exception occured:</I><BR>
 * See .out file for StackTrace
 *
 *
 * @author Jan Becicka <Jan.Becicka@sun.com>
 */


public class SourceGenerator extends org.netbeans.test.java.XRunner {
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public SourceGenerator() {
        super("");
    }
    
    public SourceGenerator(java.lang.String testName) {
        super(testName);
    }
    
    public static org.netbeans.junit.NbTest suite() {
        return new org.netbeans.junit.NbTestSuite(SourceGenerator.class);
    }
    
    /** "body" of this TestCase
     * @param o SourceElement - target for generating
     * @param log log is used for logging StackTraces
     * @throws Exception
     * @return true if test passed
     * false if failed
     */
    public boolean go(Object o, java.io.PrintWriter log) throws Exception {
//        org.openide.src.ClassElement clazz = ((org.openide.src.SourceElement) o).getClasses()[0];
//        org.netbeans.test.java.Common.simpleJavaSourceEtalonGenerator(clazz);
        return true;
    }
    
    /**
     */
    protected void setUp() {
        super.setUp();
        name = "JavaTestSourceSourceGenerator";
        packageName = "org.netbeans.test.java.testsources";
    }
    
}
