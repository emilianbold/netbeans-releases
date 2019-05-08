/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

/**
 *
 *
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
        
    // IZ 119931 : Class name is suggested in include directive
    public void testPrefix1() throws Exception {
        performTest("file.cc", 1, 1, "#include incl");
    }    

    // IZ 119931 : Class name is suggested in include directive
    public void testPrefix2() throws Exception {
        performTest("file.cc", 1, 1, "#include us");
    }

    public void testInclWoExt() throws Exception {
        // IZ#158074: Qt headers doesn't appear in code completion list
        performTest("file.cc", 1, 1, "#include \"usr_incl/no\"", -1);
    }
}
