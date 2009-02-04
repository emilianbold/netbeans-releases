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
import java.net.URL;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public class KenaiTest {

    private Kenai instance;

    public KenaiTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
//            instance = new Kenai(new KenaiMockup());
            Kenai.setURL(new URL("http://testkenai.com"));
            instance = Kenai.getDefault();
            BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            String username = br.readLine();
            String password = br.readLine();
            br.close();
            instance.login(username, password.toCharArray());
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of searchProjects method, of class Kenai.
     */
    @Test
    public void testSearchProjects() throws Exception {
        String pattern = "ja";
        Iterator<KenaiProject> result = instance.searchProjects(pattern);

        for (;result.hasNext();) {
            KenaiProject prj = result.next();
            System.out.println("Search projects: " + prj.getDisplayName());
        }
    }

    @Test
    public void testGetProject() throws Exception {
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);

        System.out.println("Project: " + prj.getDisplayName());
    }

    @Test
    public void testLogin() throws Exception {
        try {
            instance.login("jerry", "mouse".toCharArray());
            assert false : "Bogus login successful";
        } catch (KenaiException e) {
            // this is the expected result
        }
    }

    @Test
    public void testIsAuthorized() throws Exception {
        String name = "java-inline";
        KenaiProject prj = instance.getProject(name);

        boolean authorized = instance.isAuthorized(prj, KenaiActivity.FORUM_READ);
        System.out.println("Read? " + authorized);

        authorized = instance.isAuthorized(prj, KenaiActivity.FORUM_ADMIN);
        System.out.println("Admin? " + authorized);
    }

    @Test
    public void testIsAuthorized2() throws Exception {
        String name = "unittestuniquename01";
        KenaiProject prj = instance.getProject(name);

        boolean authorized = instance.isAuthorized(prj, KenaiActivity.PROJECTS_ADMIN);
        System.out.println("PROJECTS_ADMIN? " + authorized);

    }


    /**
     * Test of createProject method, of class Kenai.
     */
    @Test
    public void testCreateProject() throws KenaiException {
        System.out.println("createProject");
        String name = "unittestuniquename02";
        String displayName = "Test Display Name";
        String description = "Test Description";
        String[] licenses = {"MIT"};
        KenaiProject result;
        try {
            result = instance.createProject(name, displayName, description, licenses, "java");
            assert result.getName().equals(name);
            assert result.getDisplayName().equals(displayName);
            assert result.getDescription().equals(description);
        } catch (KenaiErrorMessage kem) {
            System.out.println(kem.getAsString());
            throw kem;
        }
    }

    @Test
    public void testCreateFeature() throws KenaiException {
        System.out.println("createFeature");
        String name = "unittestfeature1";
        String displayName = "Feature 1";
        String description = "Test Description";
        KenaiProject project = instance.getProject("unittestuniquename01");
        try {
            KenaiProjectFeature feature = project.createProjectFeature(name, displayName, description, KenaiFeature.FORUM.getId(), null, null, null);
            assert feature.getName().equals(name);
            assert feature.getDisplayName().equals(displayName);
        } catch (KenaiErrorMessage kem) {
            System.out.println(kem.getAsString());
            throw kem;
        }
    }

    @Test
    public void testGetFeatures() throws KenaiException {
        System.out.println("getFeature");
        KenaiProject project = instance.getProject("unittestuniquename01");
        for (KenaiProjectFeature feature: project.getFeatures()) {
            System.out.println(feature.getName());
        }
    }

    @Test
    public void testGetLicenses() throws KenaiException {
        System.out.println("testGetLicenses");
        final Iterator<KenaiLicense> licenses = Kenai.getDefault().getLicenses();
        while(licenses.hasNext()) {
            KenaiLicense lic = licenses.next();
            System.out.println(lic.getName());
            System.out.println(lic.getDisplayName());
            System.out.println(lic.getUri());
        }
    }

    @Test
    public void testGetServices() throws KenaiException {
        System.out.println("testGetServices");
        final Iterator<KenaiService> services = Kenai.getDefault().getServices();
        while(services.hasNext()) {
            KenaiService ser = services.next();
            System.out.println(ser.getName());
            System.out.println(ser.getDescription());
        }
    }
}
