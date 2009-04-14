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

package org.netbeans.modules.bugtracking.ui.query;

import java.beans.PropertyChangeEvent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;

/**
 *
 * @author tomas
 */
public class QueryTopComponentTest {

    public QueryTopComponentTest() {
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
     * Test of forKenai method, of class QueryTopComponent.
     */
    @Test
    public void testForKenai() {
        System.out.println("forKenai");
        Query query = null;
        Repository toSelect = null;
        QueryTopComponent expResult = null;
        QueryTopComponent result = QueryTopComponent.forKenai(query, toSelect);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefault method, of class QueryTopComponent.
     */
    @Test
    public void testGetDefault() {
        System.out.println("getDefault");
        QueryTopComponent expResult = null;
        QueryTopComponent result = QueryTopComponent.getDefault();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findInstance method, of class QueryTopComponent.
     */
    @Test
    public void testFindInstance() {
        System.out.println("findInstance");
        QueryTopComponent expResult = null;
        QueryTopComponent result = QueryTopComponent.findInstance();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of find method, of class QueryTopComponent.
     */
    @Test
    public void testFind() {
        System.out.println("find");
        Query query = null;
        QueryTopComponent expResult = null;
        QueryTopComponent result = QueryTopComponent.find(query);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPersistenceType method, of class QueryTopComponent.
     */
    @Test
    public void testGetPersistenceType() {
        System.out.println("getPersistenceType");
        QueryTopComponent instance = new QueryTopComponent();
        int expResult = 0;
        int result = instance.getPersistenceType();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of componentOpened method, of class QueryTopComponent.
     */
    @Test
    public void testComponentOpened() {
        System.out.println("componentOpened");
        QueryTopComponent instance = new QueryTopComponent();
        instance.componentOpened();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of componentClosed method, of class QueryTopComponent.
     */
    @Test
    public void testComponentClosed() {
        System.out.println("componentClosed");
        QueryTopComponent instance = new QueryTopComponent();
        instance.componentClosed();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeReplace method, of class QueryTopComponent.
     */
    @Test
    public void testWriteReplace() {
        System.out.println("writeReplace");
        QueryTopComponent instance = new QueryTopComponent();
        Object expResult = null;
        Object result = instance.writeReplace();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of preferredID method, of class QueryTopComponent.
     */
    @Test
    public void testPreferredID() {
        System.out.println("preferredID");
        QueryTopComponent instance = new QueryTopComponent();
        String expResult = "";
        String result = instance.preferredID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of propertyChange method, of class QueryTopComponent.
     */
    @Test
    public void testPropertyChange() {
        System.out.println("propertyChange");
        PropertyChangeEvent evt = null;
        QueryTopComponent instance = new QueryTopComponent();
        instance.propertyChange(evt);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of started method, of class QueryTopComponent.
     */
    @Test
    public void testStarted() {
        System.out.println("started");
        QueryTopComponent instance = new QueryTopComponent();
        instance.started();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of notifyData method, of class QueryTopComponent.
     */
    @Test
    public void testNotifyData() {
        System.out.println("notifyData");
        Issue issue = null;
        QueryTopComponent instance = new QueryTopComponent();
        instance.notifyData(issue);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of finished method, of class QueryTopComponent.
     */
    @Test
    public void testFinished() {
        System.out.println("finished");
        QueryTopComponent instance = new QueryTopComponent();
        instance.finished();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}