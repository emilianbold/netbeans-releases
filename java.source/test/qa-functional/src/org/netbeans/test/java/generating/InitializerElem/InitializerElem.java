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
 * FieldElem.java
 *
 * Created on June 26, 2000, 9:29 AM
 */

package org.netbeans.test.java.generating.InitializerElem;

import org.netbeans.test.java.Common;
import java.lang.reflect.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.*;
import org.openide.filesystems.FileObject;

/** <B>Java Module General API Test: InitializerElement</B>
 * <BR><BR><I>What it tests:</I><BR>
 * Creating and handling with InitializerElement.
 * Test is focused on checking of correctness of generated code.
 * <BR><BR><I>How it works:</I><BR>
 * New class is created using DataObject.createFromTemplate() and also some InitializerElements are created.
 * These are customized using setters and then added using ClassElement.addInitializer() into ClassElement.
 * These actions cause generating of .java code. This code is compared with supposed one.
 * <BR><BR><I>Output:</I><BR>
 * Generated Java code.
 * <BR><BR><I>Possible reasons of failure:</I><BR>
 * <U>Initialzers are not inserted properly:</U><BR>
 * If there are some Initialzers in .diff file.
 * <BR><BR><U>Initializers have/return bad properies</U><BR>
 * See .diff file to get which ones
 * <BR><BR><U>Bad indentation</U><BR>
 * This is probably not a bug of Java Module. (->Editor Bug)
 * In .diff file could be some whitespaces.
 * <BR><BR><U>Exception occured:</U><BR>
 * See .log file for StackTrace
 *
 *
 * @author Jan Becicka <Jan.Becicka@sun.com>
 */


public class InitializerElem extends org.netbeans.test.java.XRunner {
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public InitializerElem() {
        super("");
    }
    
    public InitializerElem(java.lang.String testName) {
        super(testName);
    }
    
    public static NbTest suite() {
        return new NbTestSuite(InitializerElem.class);
    }
    
    /** "body" of this TestCase
     * @param o SourceElement - target for generating
     * @param log log is used for logging StackTraces
     * @throws Exception
     * @return true if test passed
     * false if failed
     */
    public boolean go(Object o, java.io.PrintWriter log) throws Exception {
        
        FileObject fo = (FileObject) o;
        JavaSource js = JavaSource.forFileObject(fo);    
//      
        boolean passed = true;
        Common.addInitializer(js, false);
        Common.addInitializer(js, true);
        Common.addInitializer(js, false);//      
        return passed;
    }
    
    /**
     */
    protected void setUp() {
        super.setUp();
        name = "JavaTestSourceInitializerElem";
        packageName = "org.netbeans.test.java.testsources";
    }
    
}
