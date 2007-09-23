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
package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ClassContentTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of ClassContentTestCase
     */
    public ClassContentTestCase(String testName) {
        super(testName, true);
    }
       
    public void testDestructorByClassPrefix() throws Exception {
        super.performTest("file.h", 20, 9, "D::");
    } 
     
    public void testDestructor() throws Exception {
        super.performTest("file.cc", 6, 5, "d.");
    }
    
    public void testDestructorByClassPrefixAndTilda() throws Exception {
        super.performTest("file.h", 20, 9, "D::~");
    } 
    
    public void testDestructorTilda() throws Exception {
        super.performTest("file.cc", 6, 5, "pD->~");
    }  
    
}
