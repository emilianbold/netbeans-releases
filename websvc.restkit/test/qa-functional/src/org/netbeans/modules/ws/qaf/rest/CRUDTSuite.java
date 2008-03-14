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

package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;

/**
 * Tests for New REST web services from Entity Classes wizard
 *
 * @author lukas
 */
public class CRUDTSuite extends RestTestBase {

    /** Default constructor.
     * @param testName name of particular test case
    */
    public CRUDTSuite(String name) {
        super(name);
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CRUDTSuite("testRfE")); //NOI18N
        suite.addTest(new CRUDTSuite("testDeploy")); //NOI18N
//        suite.addTest(new CRUDTSuite("testGet")); //NOI18N
//        suite.addTest(new CRUDTSuite("testPost")); //NOI18N
//        suite.addTest(new CRUDTSuite("testPut")); //NOI18N
//        suite.addTest(new CRUDTSuite("testDelete")); //NOI18N
        suite.addTest(new CRUDTSuite("testCreateRestClient")); //NOI18N
        suite.addTest(new CRUDTSuite("testUndeploy")); //NOI18N
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }

    public String getProjectName() {
        return "FromEntities"; //NOI18N
    }

    protected String getRestPackage() {
        return "o.n.m.ws.qaf.rest.crud"; //NOI18N
    }

    /**
     * Create new web project with entity classes from sample database
     * (jdbc/sample), create new RESTful web services from created entities
     * and deploy the project
     */
    public void testRfE() {
        prepareEntityClasses();
        //RESTful Web Services from Entity Classes
        String restLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromEntities");
        createNewWSFile(getProject(), restLabel);
        WizardOperator wo = new WizardOperator(restLabel);
        //have to wait until "retrieving message dissapers (see also issue 122802)
        new EventTool().waitNoEvent(1500);
        //Add All
        new JButtonOperator(wo, 4).pushNoBlock();
        wo.next();
        wo = new WizardOperator(restLabel);
        //Resource Package
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 2);
        jcbo.clearText();
        jcbo.typeText(getRestPackage() + ".service"); //NOI18N
        //Converter Package
        jcbo = new JComboBoxOperator(wo, 1);
        jcbo.clearText();
        jcbo.typeText(getRestPackage() + ".converter"); //NOI18N
        wo.finish();
        //Generating RESTful Web Services from Entity Classes
        String restGenTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RestSevicicesFromEntitiesProgress");
        waitDialogClosed(restGenTitle);
        Set<File> files = getFiles(getRestPackage() + ".service"); //NOI18N
        files.addAll(getFiles(getRestPackage() + ".converter")); //NOI18N
        assertEquals("Some files were not generated", 37, files.size()); //NOI18N
        checkFiles(files);
        //make sure all REST services nodes are visible in project log. view
        assertEquals("missing nodes?", 14, getRestNode().getChildren().length);
    }

    /**
     * Test HTTP Get method
     */
    public void testGet() throws SAXException, IOException {
//        WebResponse wr = doGet(
//                getResourcesURL() + "/microMarkets/?max=30", //NOI18N
//                MimeType.APPLICATION_XML);
//        int i = wr.getDOM().getDocumentElement().getChildNodes().getLength();
//        int j = 0;
//        try {
//            ResultSet rs = doQuery("select * from \"APP\".\"MICRO_MARKET\""); //NOI18N
//            while (rs.next()) {
//                j++;
//            }
//        } catch (SQLException ex) {
//        }
//        assertEquals(i, j);
    }

    /**
     * Test HTTP Post method (add new purchaseOrder and check its stored into DB)
     */
    public void testPost() throws IOException, SAXException {
//        WebResponse wr = doPost(
//                getResourcesURL() + "/purchaseOrders/", //NOI18N
//                new FileInputStream(new File(getRestDataDir(), "purchaseOrder-new.xml")), //NOI18N
//                MimeType.APPLICATION_XML);
//        int quantity = 0;
//        try {
//            ResultSet rs = doQuery("select * from \"APP\".\"PURCHASE_ORDER\" where order_num = 99999999"); //NOI18N
//            rs.next();
//            quantity = rs.getInt("quantity"); //NOI18N
//        } catch (SQLException ex) {
//        }
//        assertEquals(75, quantity);
    }

    /**
     * Test HTTP Put method (modify new purchaseOrder and check changes in DB)
     */
    public void testPut() throws IOException, SAXException {
//        WebResponse wr = doPut(
//                getResourcesURL() + "/purchaseOrders/99999999/", //NOI18N
//                new FileInputStream(new File(getRestDataDir(), "purchaseOrder-update.xml")), //NOI18N
//                MimeType.APPLICATION_XML);
//        int quantity = 0;
//        try {
//            ResultSet rs = doQuery("select * from \"APP\".\"PURCHASE_ORDER\" where order_num = 99999999"); //NOI18N
//            rs.next();
//            quantity = rs.getInt("quantity"); //NOI18N
//        } catch (SQLException ex) {
//        }
//        assertEquals(199, quantity);
    }

    /**
     * Test HTTP Delete method (remove new purchaseOrder and check changes in DB)
     */
    public void testDelete() throws IOException, SAXException {
//        WebResponse wr = doDelete(
//                getResourcesURL() + "/purchaseOrders/99999999/", //NOI18N
//                MimeType.APPLICATION_XML);
//        try {
//            ResultSet rs = doQuery("select * from \"APP\".\"PURCHASE_ORDER\" where order_num = 99999999"); //NOI18N
//            assertFalse(rs.next());
//        } catch (SQLException ex) {
//        }
    }

    public void testCreateRestClient() throws IOException {
        prepareRestClient();
    }

    private void prepareEntityClasses() {
        //Persistence
        String persistenceLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.ui.resources.Bundle", "Templates/Persistence");
        //Entity Classes from Database
        String fromDbLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "Templates/Persistence/RelatedCMP");
        createNewFile(getProject(), persistenceLabel, fromDbLabel);
        WizardOperator wo = new WizardOperator(fromDbLabel);
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 1);
        jcbo.clickMouse();
        //choose jdbc/sample connection
        jcbo.selectItem(1);
        //skip Connecting to Database dialog
        //wait only for Please Wait dialog
        String waitTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.util.Bundle", "MSG_PleaseWait");
        waitDialogClosed(waitTitle);
        //Add all >>
        new JButtonOperator(wo, 4).pushNoBlock();
        wo.next();
        wo = new WizardOperator(fromDbLabel);
        jcbo = new JComboBoxOperator(wo, 0);
        jcbo.clearText();
        jcbo.typeText(getRestPackage());
        //Create persistence unit
        new JButtonOperator(wo, 4).pushNoBlock();
        //Create Persistence Unit
        String puDlgTitle = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "LBL_CreatePersistenceUnit");
        NbDialogOperator ndo = new NbDialogOperator(puDlgTitle);
        //Create
        new JButtonOperator(ndo, 2).pushNoBlock();
        //end create pu dialog
        //Finish
        new JButtonOperator(wo, 7).pushNoBlock();
        String generationTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "TXT_EntityClassesGeneration");
        waitDialogClosed(generationTitle);
        new EventTool().waitNoEvent(1500);
    }

    private Set<File> getFiles(String pkg) {
        Set<File> files = new HashSet<File>();
        File fo = FileUtil.toFile(getProject().getProjectDirectory());
        File pkgRoot = new File (fo, "src/java/" + pkg.replace('.', '/') + "/"); //NOI18N
        files.addAll(Arrays.asList(pkgRoot.listFiles()));
        return files;
    }
}
