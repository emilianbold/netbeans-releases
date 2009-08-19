/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public class KenaiTest extends NbTestCase {

    static String UNITTESTUNIQUENAME_BASE = "test";
    static String UNITTESTUNIQUENAME = "java-inline"; // initial value, will be changed in setUpClass method
    private static Kenai instance;
    private static boolean firstRun = true;
    private static String uname = null;
    private static String passw = null;

    public KenaiTest(String S) {
        super(S);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public void setInstance() {
        instance = Kenai.getDefault();
    }

    @Before
    @Override
    public void setUp() {
        try {
            final Logger logger = Logger.getLogger("TIMER.kenai");
            logger.setLevel(Level.FINE);
            System.setProperty("kenai.com.url", "https://testkenai.com");
            instance = Kenai.getDefault();
            if (uname == null) {
                uname = System.getProperty("kenai.user.login");
                passw = System.getProperty("kenai.user.password");
            }
            if (uname == null) { // if it is still null, check the file in ~
                BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
                uname = br.readLine();
                passw = br.readLine();
                br.close();
            }
            if (firstRun) {
                instance.login(uname, passw.toCharArray());
                UNITTESTUNIQUENAME = UNITTESTUNIQUENAME_BASE + System.currentTimeMillis();
                System.out.println("== Name: " + UNITTESTUNIQUENAME);
                firstRun = false;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test of searchProjects method, of class Kenai.
     */
    @Test
    public void testSearchProjects() throws Exception {
        System.out.println("testSearchProjects");
        String pattern = "JRuby-parser";
        Collection<KenaiProject> result = instance.searchProjects(pattern);

        if (result.size() != 1) {
            fail("The search returned incorrect number of the results when [JRuby-parser] was entered");
        }
        for (KenaiProject prj : result) {
            assertEquals(prj.getDisplayName(), "JRuby-parser: A Ruby parser");
            System.out.println("Search projects: " + prj.getDisplayName());
        }
    }

    /**
     * Test of searchProjects method, of class Kenai.
     */
    @Test
    public void testSearchProjectsLessSpecific() throws Exception {
        System.out.println("testSearchProjects");
        String pattern = "java";
        Collection<KenaiProject> result = instance.searchProjects(pattern);

        if (result.size() == 0) {
            fail("The search returned no results when [java] was entered");
        }
    }

    /**
     * Test of searchProjects method, of class Kenai.
     */
    @Test
    public void testSearchProjectsWithSpace() throws Exception {
        System.out.println("testSearchProjectsWithSpace");
        String pattern = "java inline";
        Collection<KenaiProject> result = instance.searchProjects(pattern);

        if (result.size() != 1) {
            fail("The search returned incorrect number of the results when [java inline] was entered");
        }
        for (KenaiProject prj : result) {
            assertEquals(prj.getDisplayName(), "JavaInline for JRuby");
            System.out.println("Search projects: " + prj.getDisplayName());
        }
    }

    @Test
    /**
     * Test of getProject method of class Kenai
     */
    public void testGetProject() throws Exception {
        System.out.println("testGetProject");
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);
        System.out.println("Project: " + prj.getName());
        if (!prj.getName().equals("java-inline")) {
            fail("Call to getProject failed.");
        }
    }

    @Test
    /**
     * Test of getDisplayName method of class KenaiProject
     */
    public void testGetDisplayName() throws Exception {
        System.out.println("testGetDisplayName");
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);
        System.out.println("Project: " + prj.getDisplayName());
        if (!prj.getDisplayName().equals("JavaInline for JRuby")) {
            fail("Display Name of the project has changed.");
        }
    }

    @Test
    /**
     * Test of getDescription method of class KenaiProject
     */
    public void testGetDescription() throws Exception {
        System.out.println("testGetDescription");
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);
        System.out.println(prj.getDescription());
        if (!prj.getDescription().equals("JavaInline provides a way to embed Java code into Ruby code under JRuby and have it compiled and available at runtime. Depends on Java 6 compiler API.")) {
            fail("Description of the project has changed.");
        }
    }

    @Test
    /**
     * Test of getDescription method of class KenaiProject
     */
    public void testGetImageUrl() throws Exception {
        System.out.println("testGetImageURL");
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);
        System.out.println(prj.getImageUrl());
        if (!prj.getImageUrl().equals("https://testkenai.com/images/defaultProjectImage.jpg")) {
            fail("Image URL has changed.");
        }
    }

    @Test
    /**
     * Test of getWebLocation method of class KenaiProject
     */
    public void testGetWebLocation() throws Exception {
        System.out.println("testGetWebLocation");
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);
        System.out.println(prj.getWebLocation());
        if (!prj.getWebLocation().toString().endsWith("kenai.com/projects/java-inline")) {
            fail("Web Location of the project has changed.");
        }
    }

    @Test
    /**
     * Test of getTags method of class KenaiProject
     */
    public void testGetTags() throws Exception {
        System.out.println("testGetTags");
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);
        System.out.println(prj.getTags());
        if (prj.getTags() == null || !prj.getTags().equals("java javac jruby ruby")) {
            fail("Tags of the project have changed.");
        }
    }

    @Test
    /**
     * Test of login method of class Kenai
     */
    public void testLogin() throws Exception {
        System.out.println("testLogin");
        try {
            instance.login("jerry_randomname", "mouse".toCharArray());
            assert false : "Bogus login successful";
        } catch (KenaiException e) {
            // this is the expected result
        }
    }

    @Test
    /**
     * Test of login method of class Kenai
     */
    public void testPasswordAuthentication() throws Exception {
        System.out.println("testPasswordAuthentication");
        PasswordAuthentication passAuth = instance.getPasswordAuthentication();
        assertEquals(uname, passAuth.getUserName());
        assertEquals(passw, new String(passAuth.getPassword()));
        if ("true".equals(System.getProperty("show_login"))) {
            System.out.println("cred:" + uname + "/" + passw);
        }
    }

    @Test
    /**
     * Test of login method of class Kenai
     */
    public void testLogout() throws Exception {
        System.out.println("testLogout");
        // Check if user is logged in at the moment
        PasswordAuthentication passAuth = instance.getPasswordAuthentication();
        assertEquals(uname, passAuth.getUserName());
        assertEquals(passw,  new String(passAuth.getPassword()));
        // Do log out
        instance.logout();
        System.out.println("Originally logged in, OK");
        // User should be logged out
        assertNull(instance.getPasswordAuthentication());
        final Collection<KenaiProject> myProjects = instance.getMyProjects();
        assert myProjects.isEmpty(): "My Projects Must Be Empty";
        System.out.println("Logged out, OK - 1/2");
        System.out.println("Logged out, OK - 2/2");
        // Login again and check if user is logged in
        instance.login(uname, passw.toCharArray());
        passAuth = instance.getPasswordAuthentication();
        assertEquals(uname, passAuth.getUserName());
        assertEquals(passw,  new String(passAuth.getPassword()));
        System.out.println("Logged out, OK");
    }

    @Test
    /**
     * Test of isAuthorized method of class Kenai
     */
    public void testIsAuthorized() throws Exception {
        System.out.println("testIsAuthorized");
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);

        boolean authorized = instance.isAuthorized(prj, KenaiActivity.FORUM_READ);
        System.out.println("Forum - Read? " + authorized);
        assertTrue(authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.FORUM_ADMIN);
        System.out.println("Forum - Admin? " + authorized);
        assertFalse(authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.ISSUES_READ);
        System.out.println("Issues - Read? " + authorized);
        assertTrue(authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.ISSUES_WRITE);
        System.out.println("Issues - Write? " + authorized);
        assertFalse(authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.WIKI_READ);
        System.out.println("Wiki - Read? " + authorized);
        assertTrue(authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.WIKI_WRITE);
        System.out.println("Wiki - Write? " + authorized);
        assertFalse(authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.PROJECTS_CREATE);
        System.out.println("Project - Create? " + authorized);
        assertTrue(authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.PROJECTS_DELETE);
        System.out.println("Projects - Delete? " + authorized);
        assertFalse(authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.SOURCE_WRITE);
        System.out.println("Source - Write? " + authorized);
        assertFalse(authorized);

    }

//    @Test
//    /**
//     * Test of isAuthorized method of class Kenai
//     */
//    public void testIsAuthorized2() throws Exception {
//        System.out.println("testIsAuthorized2");
//        String name = UNITTESTUNIQUENAME;
//        try {
//            KenaiProject prj = instance.getProject(name);
//
//            boolean authorized = instance.isAuthorized(prj, KenaiActivity.PROJECTS_ADMIN);
//            System.out.println("PROJECTS_ADMIN? " + authorized);
//            assertTrue(authorized);
//        } catch (KenaiException mes) {
//            System.out.println(mes.getAsString());
//            throw mes;
//        }
//    }

    /**
     * Test of createProject method, of class Kenai.
     */
    @Test
    public void testCreateProject() throws KenaiException {
        System.out.println("createProject");
        String name = UNITTESTUNIQUENAME;
        String displayName = "Test Display Name";
        String description = "Test Description";
        String[] licenses = {"MIT"};
        KenaiProject result;
        try {
            result = instance.createProject(name, displayName, description, licenses, "java");
            assert result.getName().equals(name);
            assert result.getDisplayName().equals(displayName);
            assert result.getDescription().equals(description);
        } catch (KenaiException kem) {
            System.out.println(kem.getAsString());
            throw kem;
        }
    }

    @Test
    /**
     * Test of createProjectFeature method of class Kenai
     */
    public void testCreateFeature() throws KenaiException {
        System.out.println("createFeature");
        String name = "unittestfeature01";
        String displayName = "Feature 1";
        String description = "Test Description - FORUM";
        KenaiProject project = instance.getProject(UNITTESTUNIQUENAME);
        try {
            KenaiFeature feature = project.createProjectFeature(name, displayName, description, KenaiService.Type.FORUM.getId(), null, null, null);
            assert feature.getName().equals(name);
            assert feature.getDisplayName().equals(displayName);

            name = "unittestfeature02";
            displayName = "Feature 2";
            description = "Test Description - JIRA";
            feature = project.createProjectFeature(name, displayName, description, "jira", null, null, null);
            assert feature.getName().equals(name);
            assert feature.getDisplayName().equals(displayName);

            name = "unittestfeature02a";
            displayName = "Feature 2a";
            description = "Test Description - downloads";
            feature = project.createProjectFeature(name, displayName, description, KenaiService.Type.DOWNLOADS.getId(), null, null, null);
            assert feature.getName().equals(name);
            assert feature.getDisplayName().equals(displayName);

            name = "unittestfeature02b";
            displayName = "Feature 2b";
            description = "Test Description - chat";
            feature = project.createProjectFeature(name, displayName, description, KenaiService.Type.CHAT.getId(), null, null, null);
            assert feature.getName().equals(UNITTESTUNIQUENAME);
            assert feature.getDisplayName().equals(displayName);

            name = "unittestfeature03";
            displayName = "Feature 3";
            description = "Test Description - LISTS";
            feature = project.createProjectFeature(name, displayName, description, KenaiService.Type.LISTS.getId(), null, null, null);
            assert feature.getName().equals(name);
            assert feature.getDisplayName().equals(displayName);

            name = "unittestfeature04";
            displayName = "Feature 4";
            description = "Test Description - HG";
            feature = project.createProjectFeature(name, displayName, description, "mercurial", null, null, null);
            assert feature.getName().equals(name);
            assert feature.getDisplayName().equals(displayName);

            try { // Wiki should be only one - this test should fail...
                name = "unittestfeature05";
                displayName = "Feature 5";
                description = "Test Description - WIKI";
                feature = project.createProjectFeature(name, displayName, description, KenaiService.Type.WIKI.getId(), null, null, null);
                fail("Second wiki feature was created for the project!");
            } catch (Throwable _t) {
                // expected result
            }
        } catch (KenaiException kem) {
            System.out.println(kem.getAsString());
            throw kem;
        }
    }

    @Test
    /**
     * Test of getFeatures method of class Kenai<br />
     * Note: This test also checks all methods from KenaiFeature
     */
    public void testGetFeatures() throws KenaiException {
        BufferedReader br = null;
        try {
            System.out.println("testGetFeatures");
            String _fileName = getDataDir().getAbsolutePath() + File.separatorChar + "features-java-inline.data";
            br = new BufferedReader(new FileReader(_fileName));
            String line = null;
            System.out.println("getFeature");
            KenaiProject project = instance.getProject("java-inline");
            for (KenaiFeature feature : project.getFeatures()) {
                System.out.println("===");
                // Check feature's name
                line = br.readLine().trim();
                assertEquals(line, feature.getName());
                System.out.println(feature.getName());
                // Check feature's type
                line = br.readLine().trim();
                assertEquals(line, feature.getType().toString());
                System.out.println(feature.getType().toString());
                // Check feature's display name
                line = br.readLine().trim();
                assertEquals(line, feature.getDisplayName());
                System.out.println(feature.getDisplayName());
                // Check feature's location
                line = br.readLine().trim();
                if (line.equals("null")) { // feature is not present
                    assertEquals(null, feature.getLocation());
                } else {
                    assertEquals(line, feature.getLocation().toString());
                }
                System.out.println(feature.getLocation());
                // Check feature's service
                line = br.readLine().trim();
                assertEquals(line, feature.getService());
                System.out.println(feature.getService());
                // Check feature's web location
                line = br.readLine().trim();
                assertEquals(line, feature.getWebLocation().toString());
                System.out.println(feature.getWebLocation().toString());
            }
        } catch (KenaiException mes) {
            System.out.println(mes.getAsString());
            throw mes;
        } catch (IOException ex) {
            fail("Failure while reading the features-java-inline.data golden file.");
        }
    }

    @Test
    /**
     * Test of getFeatures method of class Kenai<br />
     * Note: This test also checks all methods from KenaiFeature
     */
    public void testGetFeaturesGolden() throws KenaiException {
        BufferedReader br = null;
        boolean doAsserts = true; //set false to list all features returned by json to stdout
        try {
            System.out.println("testGetFeatures");
            String _fileName = getDataDir().getAbsolutePath() + File.separatorChar + "features-golden.data";
            br = new BufferedReader(new FileReader(_fileName));
            String line = null;
            System.out.println("getFeature");
            KenaiProject project = instance.getProject("golden-project-1");
            for (KenaiFeature feature : project.getFeatures()) {
                System.out.println("===");
                // Check feature's name
                if (doAsserts) {
                    line = br.readLine().trim();
                    assertEquals(line, feature.getName());
                }
                System.out.println(feature.getName());
                // Check feature's type
                if (doAsserts) {
                    line = br.readLine().trim();
                    assertEquals(line, feature.getType().toString());
                }
                System.out.println(feature.getType().toString());
                // Check feature's display name
                if (doAsserts) {
                    line = br.readLine().trim();
                    assertEquals(line, feature.getDisplayName());
                }
                System.out.println(feature.getDisplayName());
                // Check feature's location
                if (doAsserts) {
                    line = br.readLine().trim();
                    if (line.equals("null")) { // feature is not present
                        assertEquals(null, feature.getLocation());
                    } else {
                        assertEquals(line, feature.getLocation().toString());
                    }
                }
                System.out.println(feature.getLocation());
                // Check feature's service
                if (doAsserts) {
                    line = br.readLine().trim();
                    assertEquals(line, feature.getService());
                }
                System.out.println(feature.getService());
                // Check feature's web location
                if (doAsserts) {
                    line = br.readLine().trim();
                    assertEquals(line, feature.getWebLocation().toString());
                }
                System.out.println(feature.getWebLocation().toString());
            }
        } catch (KenaiException mes) {
            System.out.println(mes.getAsString());
            throw mes;
        } catch (IOException ex) {
            fail("Failure while reading the features-java-inline.data golden file.");
        }
    }

    @Test
    /**
     * Test of getLicences method of class Kenai<br />
     * Note: This test also checks all methods from KenaiLicense
     */
    public void testGetLicenses() throws KenaiException {
        BufferedReader br = null;
        try {
            System.out.println("testGetLicenses");
            String _fileName = getDataDir().getAbsolutePath() + File.separatorChar + "licences.data";
            br = new BufferedReader(new FileReader(_fileName));
            String line = null;
            for (KenaiLicense lic : Kenai.getDefault().getLicenses()) {
                // Check the licence name
                line = br.readLine().trim();
                assertEquals(line, lic.getName());
                System.out.println(lic.getName());
                // Check the licence display name
                line = br.readLine().trim();
                assertEquals(line, lic.getDisplayName());
                System.out.println(lic.getDisplayName());
                // Check the licence uri
                line = br.readLine().trim();
                assertEquals(line, lic.getUri().toString());
                System.out.println(lic.getUri().toString());
            }
        } catch (IOException ex) {
            fail("Failure while reading the licences.data golden file.");
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                //
            }
        }
    }

    @Test
    /**
     * Test of getServices method of class Kenai<br />
     * Note: more detailed tests are in KenaiServiceTest.java
     */
    public void testGetServices() throws KenaiException {
        System.out.println("testGetServices");
        for (KenaiService ser : Kenai.getDefault().getServices()) {
            System.out.println(ser.getName());
            System.out.println(ser.getDescription());
            System.out.println(ser.getDisplayName());
            System.out.println(ser.getType());
        }
    }

    @Test
    /**
     * Test of getMyProjects method of class Kenai
     */
    public void testGetMyProjects() throws Exception {
        System.out.println("testGetMyProjects (takes quite long - please wait...)");
        Collection<KenaiProject> result = instance.getMyProjects();
        System.out.println("size: " + result.size());

        KenaiProject myprj = instance.getProject("my-project-1");
        assertTrue("My project was not found.", result.contains(myprj));
        for (KenaiProject prj : result) {
            System.out.println("My projects: " + prj.getDisplayName());
        }
    }

    static public junit.framework.Test suite() {
        junit.framework.TestSuite _suite = new junit.framework.TestSuite();
        _suite.addTest(new KenaiTest("testSearchProjects"));
        _suite.addTest(new KenaiTest("testSearchProjectsLessSpecific"));
        _suite.addTest(new KenaiTest("testSearchProjectsWithSpace"));
        _suite.addTest(new KenaiTest("testGetProject"));
        _suite.addTest(new KenaiTest("testGetDescription"));
        _suite.addTest(new KenaiTest("testGetImageUrl"));
        _suite.addTest(new KenaiTest("testGetDisplayName"));
        _suite.addTest(new KenaiTest("testGetWebLocation"));
        _suite.addTest(new KenaiTest("testGetTags"));
        _suite.addTest(new KenaiTest("testLogin"));
        _suite.addTest(new KenaiTest("testLogout"));
        _suite.addTest(new KenaiTest("testPasswordAuthentication"));
        _suite.addTest(new KenaiTest("testCreateProject"));
        _suite.addTest(new KenaiTest("testCreateFeature"));
        _suite.addTest(new KenaiTest("testGetFeaturesGolden"));
        _suite.addTest(new KenaiTest("testGetLicenses"));
        _suite.addTest(new KenaiTest("testGetServices"));
//        _suite.addTest(new KenaiTest("testGetMyProjects"));
        _suite.addTest(new KenaiTest("testIsAuthorized"));
        return _suite;
    }
    ;
}
