/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import java.util.Collection;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vkraemer
 */
public class GlassfishInstanceProviderTest {

    public GlassfishInstanceProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of computeNeedToRegister method, of class GlassfishInstanceProvider.
     */
    @Test
    public void testComputeNeedToRegister() {
//        System.out.println("computeNeedToRegister");
//
//        // the first-run value is false.. so the userdir doesn't have a value for
//        // prop-first-run
//        String firstRunValue = "false";
//        String candidate = "/a";
//        Collection<String> registeredInstalls = new HashSet<String>();
//        boolean expResult = true;
//        boolean result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the value is set to the same value as the candidate...
//        firstRunValue ="/a";
//        expResult = false;
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the first-run and candidate are different
//        firstRunValue ="/b";
//        expResult = true;
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the value is true... so we have run before using the old mechanism
//        firstRunValue = "true";
//        expResult = true;
//
//        // the list of registered servers is empty...
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the list is not empty... but does not contain a match.
//        registeredInstalls.add("/b");
//        registeredInstalls.add("/c");
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the list contains a match...
//        expResult = false;
//        registeredInstalls.add("/a");
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
    }

}
