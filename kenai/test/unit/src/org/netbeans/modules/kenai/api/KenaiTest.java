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
package org.netbeans.modules.kenai.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public class KenaiTest extends AbstractKenaiTestCase {

    private static boolean firstRun = true;
    private static String UNITTESTUNIQUENAME_BASE = "kenaiutestunique";    
    private static String UNITTESTUNIQUENAME; 
    
    public KenaiTest(String S) {
        super(S);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        if (firstRun) {            
            UNITTESTUNIQUENAME = UNITTESTUNIQUENAME_BASE + System.currentTimeMillis();
            System.out.println("== Name: " + UNITTESTUNIQUENAME);
            firstRun = false;
            
            cleanupProjects();
            
        }        
    }

    /**
     * Test of searchProjects method, of class Kenai.
     */
    @Test
    public void testSearchProjects() throws Exception {
        System.out.println("testSearchProjects");
        String pattern = getTestProject();
        Collection<KenaiProject> result = getKenai().searchProjects(pattern);

        if (result.size() != 1) {
            fail("The search returned [" + result.size() + "] instead of [1] when [" + getTestProject() + "] was entered");
        }
        for (KenaiProject prj : result) {
            assertEquals("NetBeans java.net tests", prj.getDisplayName());
            System.out.println("Search projects: " + prj.getDisplayName());
        }
    }

    /**
     * Test of searchProjects method, of class Kenai.
     */
    @Test
    public void testSearchProjectsLessSpecific() throws Exception {
        System.out.println("testSearchProjects");
        String pattern = "netbeans";
        Collection<KenaiProject> result = getKenai().searchProjects(pattern);

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
        String pattern = "NetBeans java.net Tests";
        Collection<KenaiProject> result = getKenai().searchProjects(pattern);

        if (result.size() != 1) {
            fail("The search returned [" + result.size() + "] instead of [1] when [" + getTestProject() + "] was entered");
        }
        for (KenaiProject prj : result) {
            assertEquals("NetBeans java.net tests", prj.getDisplayName());
            System.out.println("Search projects: " + prj.getDisplayName());
        }
    }

    /**
     * Test of searchProjects method, of class Kenai.
     */
    @Test
    public void testSearchProjectsCAPITALIZED() throws Exception {
        System.out.println("testSearchProjectsWithSpace");
        String pattern = "NETBEANS JAVA.NET TESTS";
        Collection<KenaiProject> result = getKenai().searchProjects(pattern);

        if (result.size() != 1) {
            fail("The search returned [" + result.size() + "] instead of [1] when [" + getTestProject() + "] was entered");
        }
        for (KenaiProject prj : result) {
            assertEquals("NetBeans java.net tests", prj.getDisplayName());
            System.out.println("Search projects: " + prj.getDisplayName());
        }
    }
    
    @Test
    /**
     * Test of getProject method of class Kenai
     */
    public void testGetProject() throws Exception {
        System.out.println("testGetProject");
        KenaiProject prj = getKenai().getProject(getTestProject());
        System.out.println("Project: " + prj.getName());
        if (!prj.getName().equals(getTestProject())) {
            fail("Call to getProject failed.");
        }
    }

    @Test
    /**
     * Test of getDisplayName method of class KenaiProject
     */
    public void testGetDisplayName() throws Exception {
        System.out.println("testGetDisplayName");
        KenaiProject prj = getKenai().getProject(getTestProject());
        System.out.println("Project: " + prj.getDisplayName());
        if (!prj.getDisplayName().equals("NetBeans java.net tests")) {
            fail("Display Name of the project has changed.");
        }
    }

    @Test
    /**
     * Test of getDescription method of class KenaiProject
     */
    public void testGetDescription() throws Exception {
        System.out.println("testGetDescription");
        KenaiProject prj = getKenai().getProject(getTestProject());
        System.out.println(prj.getDescription());
        if (!prj.getDescription().equals("NetBeans test project.")) {
            fail("Description of the project has changed.");
        }
    }

    @Test
    /**
     * Test of getDescription method of class KenaiProject
     */
    public void testGetImageUrl() throws Exception {
        System.out.println("testGetImageURL");
        KenaiProject prj = getKenai().getProject(getTestProject());
        System.out.println(prj.getImageUrl());
        if (!prj.getImageUrl().equals("https://testjava.net/images/defaultProjectImage.jpg")) {
            fail("Image URL has changed.");
        }
    }

    @Test
    /**
     * Test of getWebLocation method of class KenaiProject
     */
    public void testGetWebLocation() throws Exception {
        System.out.println("testGetWebLocation");
        KenaiProject prj = getKenai().getProject(getTestProject());
        System.out.println(prj.getWebLocation());
        if (!prj.getWebLocation().toString().endsWith("java.net/projects/" + getTestProject())) {
            fail("Web Location of the project has changed.");
        }
    }

    @Test
    /**
     * Test of getTags method of class KenaiProject
     */
    public void testGetTags() throws Exception {
        System.out.println("testGetTags");
        KenaiProject prj = getKenai().getProject(getTestProject());
        System.out.println(prj.getTags());
        if (prj.getTags() == null || !prj.getTags().equals("tag1 tag2 tag3")) {
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
            getKenai().login("jerry_randomname", "mouse".toCharArray());
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
        PasswordAuthentication passAuth = getKenai().getPasswordAuthentication();
        assertEquals(getUsername(), passAuth.getUserName());
        assertEquals(getPassword(), new String(passAuth.getPassword()));
        if ("true".equals(System.getProperty("show_login"))) {
            System.out.println("cred:" + getUsername() + "/" + getPassword());
        }
    }

    @Test
    /**
     * Test of login method of class Kenai
     */
    public void testLogout() throws Exception {
        System.out.println("testLogout");
        // Check if user is logged in at the moment
        PasswordAuthentication passAuth = getKenai().getPasswordAuthentication();
        assertEquals(getUsername(), passAuth.getUserName());
        assertEquals(getPassword(),  new String(passAuth.getPassword()));
        // Do log out
        getKenai().logout();
        System.out.println("Originally logged in, OK");
        // User should be logged out
        assertNull(getKenai().getPasswordAuthentication());
        final Collection<KenaiProject> myProjects = getKenai().getMyProjects();
        assert myProjects.isEmpty(): "My Projects Must Be Empty";
        System.out.println("Logged out, OK - 1/2");
        System.out.println("Logged out, OK - 2/2");
        // Login again and check if user is logged in
        getKenai().login(getUsername(), getPassword().toCharArray(), false); // XXX
        passAuth = getKenai().getPasswordAuthentication();
        assertEquals(getUsername(), passAuth.getUserName());
        assertEquals(getPassword(),  new String(passAuth.getPassword()));
        System.out.println("Logged out, OK");
    }

    @Test
    /**
     * Test of isAuthorized method of class Kenai
     */
    public void testIsAuthorized() throws Exception {
        System.out.println("testIsAuthorized");
        KenaiProject prj = getKenai().getProject(getTestProject());

        boolean authorized = getKenai().isAuthorized(prj, KenaiActivity.FORUM_READ);
        System.out.println("Forum - Read? " + authorized);
        assertTrue(authorized);

        authorized = getKenai().isAuthorized(prj, KenaiActivity.FORUM_ADMIN);
        System.out.println("Forum - Admin? " + authorized);
        assertTrue(authorized);

        authorized = getKenai().isAuthorized(prj, KenaiActivity.ISSUES_READ);
        System.out.println("Issues - Read? " + authorized);
        assertTrue(authorized);

        authorized = getKenai().isAuthorized(prj, KenaiActivity.ISSUES_WRITE);
        System.out.println("Issues - Write? " + authorized);
        assertFalse(authorized);

        authorized = getKenai().isAuthorized(prj, KenaiActivity.WIKI_READ);
        System.out.println("Wiki - Read? " + authorized);
        assertTrue(authorized);

        authorized = getKenai().isAuthorized(prj, KenaiActivity.WIKI_WRITE);
        System.out.println("Wiki - Write? " + authorized);
        assertTrue(authorized);

        authorized = getKenai().isAuthorized(prj, KenaiActivity.PROJECTS_CREATE);
        System.out.println("Project - Create? " + authorized);
        assertTrue(authorized);

        authorized = getKenai().isAuthorized(prj, KenaiActivity.PROJECTS_DELETE);
        System.out.println("Projects - Delete? " + authorized);
        assertTrue(authorized);

        authorized = getKenai().isAuthorized(prj, KenaiActivity.SOURCE_WRITE);
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
//            KenaiProject prj = getKenai().getProject(name);
//
//            boolean authorized = getKenai().isAuthorized(prj, KenaiActivity.PROJECTS_ADMIN);
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
    @RandomlyFails
    public void testCreateProject() throws KenaiException {
        System.out.println("createProject");
        String name = UNITTESTUNIQUENAME;
        String displayName = "Test Display Name";
        String description = "Test Description";
        String[] licenses = {"MIT"};
        KenaiProject result;
        try {
            result = getKenai().createProject(name, displayName, description, licenses, "java");
            assert result.getName().equals(name);
            assert result.getDisplayName().equals(displayName);
            assert result.getDescription().equals(description);
        } catch (KenaiException kem) {
            System.out.println(kem.getAsString());
            throw kem;
        }
    }

    /**
     * Test of createProject method, of class Kenai.
     */
    @RandomlyFails
    @Test
    public void testDeleteProject() throws KenaiException {
        System.out.println("deleteProject");
        String name = UNITTESTUNIQUENAME;
        String displayName = "Test Display Name";
        String description = "Test Description";
        String[] licenses = {"MIT"};
        try {
            KenaiProject project = getKenai().getProject(UNITTESTUNIQUENAME);
            if(project == null) {
                project = getKenai().createProject(name, displayName, description, licenses, "java");
                assert project.getName().equals(name);
                assert project.getDisplayName().equals(displayName);
                assert project.getDescription().equals(description);
            }
            project.delete();

            try {
                getKenai().getProject(UNITTESTUNIQUENAME);
                fail(UNITTESTUNIQUENAME + " not deleted");
            } catch (KenaiException kenaiException) {
                System.out.println(kenaiException);
            }

            try {
                project.getDescription();
                fail(UNITTESTUNIQUENAME + " not deleted");
            } catch (KenaiException kenaiException) {
                System.out.println(kenaiException);
            }
        } catch (KenaiException kem) {
            System.out.println(kem.getAsString());
            throw kem;
        }
    }


    @RandomlyFails
    @Test
    /**
     * Test of createProjectFeature method of class Kenai
     */
    public void testCreateFeature() throws KenaiException {
        System.out.println("createFeature");
        String name = "unittestfeature01";
        String displayName = "Feature 1";
        String description = "Test Description - FORUM";
        KenaiProject project = getKenai().getProject(UNITTESTUNIQUENAME);
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
    public void testGetFeaturesGolden() throws KenaiException, FileNotFoundException {
        System.out.println("testGetFeatures");        
        System.out.println("getFeatures");
        KenaiProject project = getKenai().getProject(getTestProject());
        KenaiFeature[] kfs = project.getFeatures();
        String fileName = getDataDir().getAbsolutePath() + File.separatorChar + "features-golden.data";
       
        assertNotNull(kfs);   
        
        Map<String, FeatureDesc> goldenFeatures = getGoldenFeatures(fileName);
            
        if(kfs.length < goldenFeatures.size()) {
            fail("at least " + goldenFeatures.size() + " features expected but returned only " + kfs.length);
        }

        HashMap<String, KenaiFeature> features = new HashMap<String, KenaiFeature>();
        for (KenaiFeature kenaiFeature : kfs) {
            features.put(getFeatureID(kenaiFeature), kenaiFeature);
        }

        for (Entry<String, FeatureDesc> entry : goldenFeatures.entrySet()) {
            System.out.println("===");

            FeatureDesc fd = entry.getValue();
            KenaiFeature feature = features.get(getFeatureID(fd));
            if(feature == null) {
                fail("expected to find feature " + getFeatureID(fd));
            }
            // Check feature's name

            assertEquals(fd.name, feature.getName());
            System.out.println(feature.getName());
            // Check feature's type
            assertEquals(fd.type, feature.getType().toString());
            System.out.println(feature.getType().toString());
            // Check feature's display name
            assertEquals(fd.displayName, feature.getDisplayName());
            System.out.println(feature.getDisplayName());
            // Check feature's location
            if (fd.featureLocation.equals("null")) { // feature is not present
                assertEquals(null, feature.getLocation());
            } else {
                assertEquals(fd.featureLocation, feature.getLocation().toString());
            }
            System.out.println(feature.getLocation());
            // Check feature's service
            assertEquals(fd.service, feature.getService());
            System.out.println(feature.getService());
            // Check feature's web location
            assertEquals(fd.webLocation, feature.getWebLocation().toString());
            System.out.println(feature.getWebLocation().toString());
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
            for (KenaiLicense lic : getKenai().getLicenses()) {
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
        for (KenaiService ser : getKenai().getServices()) {
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
        Collection<KenaiProject> result = getKenai().getMyProjects();
        System.out.println("size: " + result.size());

        KenaiProject myprj = getKenai().getProject(getTestProject());
        assertTrue("My project was not found.", result.contains(myprj));
        for (KenaiProject prj : result) {
            System.out.println("My projects: " + prj.getDisplayName());
        }
    }

    @Test
    /**
     * Test of getMyProjects method of class Kenai
     */
    public void testJoinLeaveProject() throws Exception {
        KenaiProject prj = getKenai().getProject("eduni-hearts");
        KenaiUser user = KenaiUser.forName(getKenai().getPasswordAuthentication().getUserName() + "@" + getKenai().getName());
        prj.addMember(user, KenaiProjectMember.Role.OBSERVER);
        assert getKenai().getMyProjects().contains(prj);
        boolean found = false;
        int i=0;
        KenaiProjectMember members[] = prj.getMembers();
        while (!found) {
            KenaiProjectMember m = members[i++];
            if (m.getUserName().equals(user.getUserName())) {
                found=true;
                user = m.getKenaiUser();
            }
        }
        assert found : "User was not added";

        prj.deleteMember(user);
        assert !getKenai().getMyProjects().contains(prj);
    }

    private void cleanupProjects() throws KenaiException {
        Collection<KenaiProject> result = getKenai().getMyProjects();    
        for (KenaiProject kenaiProject : result) {
            
            if( kenaiProject.getName().startsWith(UNITTESTUNIQUENAME_BASE) && 
                !kenaiProject.getName().equals(TEST_PROJECT) ) 
            {
                System.out.println(" testcleanup - deleting [" + kenaiProject.getDisplayName() + "]");
                kenaiProject.delete();
            }
        }
    }

    private class FeatureDesc {
        String name;
        String type;
        String displayName;
        String featureLocation;
        String service;
        String webLocation;
    }

    private Map<String, FeatureDesc> getGoldenFeatures(String fileName) throws FileNotFoundException {
        HashMap<String, FeatureDesc> ret = new HashMap<String, FeatureDesc>();        
        BufferedReader br = new BufferedReader(new FileReader(fileName));
            
        while(true) {
            String line;
            try {
                line = br.readLine();
                if(line == null) {
                    br.close();
                    break;
                }
                FeatureDesc fd = new FeatureDesc();
                fd.name = line.trim();
                fd.type = br.readLine().trim();
                fd.displayName = br.readLine().trim();
                fd.featureLocation = br.readLine().trim();
                fd.service = br.readLine().trim();
                fd.webLocation = br.readLine().trim();
                if(!getKenai().getUrl().toString().startsWith("https://testjava.net")) {
                    ret.put(getFeatureID(fd), fd);
                }
            } catch (IOException ex) {                
                try { br.close(); } catch (IOException ex1) { }
                break;
            }          
        }        
        return ret;
    }
    
    private String getFeatureID(FeatureDesc fd) {
        return fd.type.toLowerCase() + "#" + fd.name.toLowerCase();
    }
    
    private String getFeatureID(KenaiFeature f) {
        return f.getType().getId().toLowerCase() + "#" + f.getName().toLowerCase();
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
        _suite.addTest(new KenaiTest("testGetMyProjects"));
        _suite.addTest(new KenaiTest("testIsAuthorized"));
        _suite.addTest(new KenaiTest("testDeleteProject"));
        return _suite;
    }
    
    
    
}
