/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.highlight.error;

/**
 * Test for IdentifierErrorProvider.
 *
 * @author Alexey Vladykin
 */
public class UnresolvedCpp11IdentifierTest extends ErrorHighlightingBaseTestCase {

    public UnresolvedCpp11IdentifierTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.language.flavor.cpp11", "true"); 
        super.setUp();
    }
    
    public void testBug214185() throws Exception {
        // Bug 214185 - Alternative function syntax
        performStaticTest("bug214185.cpp");
    }   

    public void testBug214184() throws Exception {
        // Bug 214184 - C++11 override keyword not supported by Code Assistance
        performStaticTest("bug214184.cpp");
    }   

    public void testBug214864() throws Exception {
        // Bug 214864 - C++11 parser error on constexpr 
        performStaticTest("bug214864.cpp");
    }   
    
    public void testBug217067() throws Exception {
        // Bug 217067 - Unable to resolver identifier function
        performStaticTest("bug217067.cpp");
    }        

    public void testBug217052() throws Exception {
        // Bug 217052 - unexpected token: override in editor
        performStaticTest("bug217052.cpp");
    }        

    public void testBug217470() throws Exception {
        // Bug 217470 - Unexpected token: struct in simple template
        performStaticTest("bug217470.cpp");
    }        
    
    /////////////////////////////////////////////////////////////////////
    // FAILS

    public static class Failed extends ErrorHighlightingBaseTestCase {

        public Failed(String testName) {
            super(testName);
        }

    }
}
