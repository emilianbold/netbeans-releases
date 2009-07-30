package org.netbeans.modules.visualweb.jsfsupport.converter;

import org.netbeans.junit.NbTestCase;

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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
         
/**
 * @author dongmeic
 */
public class SqlTimestampConverterTest extends NbTestCase {

    /** Default constructor.
     * @param testName name of particular test case
    */
    public SqlTimestampConverterTest(String testName) {
        super(testName);
    }        
        
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SqlTimestampConverterTest("test1"));
        suite.addTest(new SqlTimestampConverterTest("test2"));
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new FacesConatainerTest("test1"));
    }
    
    /** Called before every test case. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Called after every test case. */
    public void tearDown() {
    }

    // Add test methods here, they have to start with 'test'.
    
    /** Test case 1. */
    public void test1() {
    }

    /** Test case 2. */
    public void test2() {
    }
}
