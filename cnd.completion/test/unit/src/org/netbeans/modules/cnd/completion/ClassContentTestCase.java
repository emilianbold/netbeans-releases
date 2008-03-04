/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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
       
    public void testConstructorDereference() throws Exception {
        super.performTest("file.h", 6, 9, "D(1).");
    }
    
    public void testNewDConstructor() throws Exception {
        super.performTest("file.cc", 6, 5, "new D()", -1);
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

    public void testUnnamedDefinitions() throws Exception {
        super.performTest("file.cc", 6, 5, "F f; f.");
    }
    
    public void testUnnamedEnums() throws Exception {
        super.performTest("file.cc", 6, 5, "F::");
    }
   
    public void testStaticUnnamedUnions() throws Exception {
        super.performTest("file2.cc", 15, 4);
    }
}
