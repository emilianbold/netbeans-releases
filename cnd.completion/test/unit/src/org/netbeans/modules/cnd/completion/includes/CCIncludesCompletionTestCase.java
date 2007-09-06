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
package org.netbeans.modules.cnd.completion.includes;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CCIncludesCompletionTestCase extends IncludesCompletionBaseTestCase {

    /**
     * Creates a new instance of CCIncludesCompletionTestCase
     */
    public CCIncludesCompletionTestCase(String testName) {
        super(testName);
    }    
    
    public void testNothing() throws Exception {
        performTest("file.cc", 1, 1, " ");
    }
    
    public void testEmtpy() throws Exception {
        performTest("file.cc", 1, 1, "#include ");
    }
    
    public void testEmtpyUsr() throws Exception {
        performTest("file.cc", 1, 1, "#include \"\"", -1);
    }
    
    public void testEmtpySys() throws Exception {
        performTest("file.cc", 1, 1, "#include <>", -1);
    } 
    
    public void testSmthUsr() throws Exception {
        performTest("file.cc", 1, 1, "#include \"us\"", -1);
    }
    
    public void testSmthSys() throws Exception {
        performTest("file.cc", 1, 1, "#include <inc>", -1);
    }    
}
