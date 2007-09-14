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

package org.netbeans.test.java.generating.MethodElem;

import org.netbeans.test.java.Common;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.*;
import org.openide.filesystems.FileObject;

/** <B>Java Module General API Test: MethodElement</B>
 * <BR><BR><I>What it tests:</I><BR>
 * Creating and handling with MethodElement.
 * Test is focused on checking of correctness of generated code.
 * <BR><BR><I>How it works:</I><BR>
 * New class is created using DataObject.createFromTemplate() and also some MethodElements are created.
 * These are customized using setters and then added using ClassElement.addMethod() into ClassElement.
 * These actions cause generating of .java code. This code is compared with supposed one.
 * <BR><BR><I>Output:</I><BR>
 * Generated Java code.
 * <BR><BR><I>Possible reasons of failure:</I><BR>
 * <U>Methods are not inserted properly</U><BR>
 * If there are some Interfaces in .diff file.
 * <BR><BR><U>Methods have/return bad properies</U><BR>
 * See .diff file to get which ones
 * <BR><BR><U>Bad indentation</U><BR>
 * This is probably not a bug of Java Module. (Editor Bug)
 * In .diff file could be some whitespaces.
 * <BR><BR><I>Exception occured:</I><BR>
 * See .log file for StackTrace
 *
 * @author Jan Becicka <Jan.Becicka@sun.com>
 */


public class MethodElem extends org.netbeans.test.java.XRunner {
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public MethodElem() {
        super("");
    }
    
    public MethodElem(java.lang.String testName) {
        super(testName);
    }
    
    public static NbTest suite() {
        return new NbTestSuite(MethodElem.class);
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
        boolean passed = true;
        FileObject fo = (FileObject) o;
        JavaSource js = JavaSource.forFileObject(fo);
        
        Common.addMethod(js, "method1",Common.PARS1,"void", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));        
        Common.addMethod(js, "method1",Common.PARS2,"int", EnumSet.of(Modifier.PRIVATE,Modifier.SYNCHRONIZED));        
        Common.addMethod(js, "method1",Common.PARS3,"float", EnumSet.of(Modifier.PRIVATE,Modifier.FINAL));        
        Common.addMethod(js, "method2",Common.PARS1,"double", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));
        Common.addMethod(js, "method2",Common.PARS2,"boolean", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));        
        Common.addMethod(js, "method2",Common.PARS3,"void", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));
        
        return passed;
    }
    
    /**
     */
    protected void setUp() {
        super.setUp();
        name = "JavaTestSourceMethodElem";
        packageName = "org.netbeans.test.java.testsources";
    }
    
}
