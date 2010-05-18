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
package org.netbeans.modules.kenai;

import java.net.URL;
import org.codeviation.pojson.PojsonLoad;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.kenai.utils.Utils;
import static org.junit.Assert.*;

/**
 *
 * @author Maros Sandor
 */
public class KenaiRESTTest {

    public KenaiRESTTest() {
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

    @Test
    public void testParseProjectInfo() throws Exception {
        System.out.println("testParseProjectInfo");
        PojsonLoad pl = PojsonLoad.create();
        ProjectData pinfo;
        pinfo = pl.load(Utils.project_alice, ProjectData.class);
        assertEquals(pinfo.name, "alice");
        pinfo = pl.load(Utils.project_json, ProjectData.class);
        assertEquals(pinfo.name, "java-inline");
    // TODO: verify all fields
    }


    @Test
    public void testGetProject() throws Exception {
        System.out.println("testGetProject");

        //Golden data - project properties
        String href = "http://testkenai.com/api/projects/alice";
        String name = "alice";
        String display_name = "Alice";
        String image = "http://testkenai.com/images/defaultProjectImage.jpg";
        String owner = "denniscosgrove";
        String description = "Alice is a teaching tool that uses drag-and-drop style interaction to construct programs without syntax errors.  Students create movies and games as a motivating context.  3D animation provides program visualization which aids students in understanding the execution and catching bugs.\r\n\r\nThe third version of the system is under development which includes a smoother transition to programming in Java.";
        String created_at = "2008-08-15T10:47:27Z";
        String updated_at = "2008-11-25T21:46:35Z";
        int member_count = 45;

        //Goldem data - project license properties
        String lcHref = "http://testkenai.com/api/licenses/1";
        String lcName = "Apache-2.0";
        String lcDisplayName = "Apache License 2.0";
        String lcURI = "http://www.opensource.org/licenses/apache2.0.php";
        String lcPreferenceLevel = "recommended";

        System.setProperty("netbeans.t9y.kenai.testUrl", Utils.project_alice);

        String username = "";
        char[] password = null;
        KenaiREST kenaiInstance = new KenaiREST(new URL("http://peterp.czech.sun.com"));
        ProjectData result = kenaiInstance.getProject(name, null);
        LicenceData[] licenseData = result.licenses;
        FeatureData[] featureData = result.features;

        //verify project properties
        assertEquals(href, result.href);
        assertEquals(name, result.name);
        assertEquals(display_name, result.display_name);
        assertEquals(image, result.image);
        assertEquals(owner, result.owner);
        assertEquals(description, result.description);
        assertEquals(created_at, result.created_at);
        assertEquals(updated_at, result.updated_at);
        assertEquals(member_count, result.member_count);

        //verify project license properties
        assertEquals(lcHref, result.licenses[0].href);
        assertEquals(lcName, result.licenses[0].name);
        assertEquals(lcDisplayName, result.licenses[0].display_name);
        assertEquals(lcURI, result.licenses[0].license_uri);
        assertEquals(lcPreferenceLevel, result.licenses[0].preference_level);

        //verify project feature properties
        /* ToDo */

        System.setProperty("netbeans.t9y.kenai.testUrl", "");
    // TODO review the generated test code and remove the default call to fail.
    }

    @Test
    public void testGetProjectMissingMemberValue() throws Exception {
        System.out.println("testGetProjectMissingProperties");
        System.setProperty("netbeans.t9y.kenai.testUrl", Utils.project_alice_missing_member_value);

        String name = "";
        String username = "";
        char[] password = null;
        KenaiREST kenaiInstance = new KenaiREST(new URL("http://peterp.czech.sun.com"));
        IllegalArgumentException iae = null;
        try {
            ProjectData result = kenaiInstance.getProject(name, null);
        } catch (Exception e) {
            iae = (IllegalArgumentException) e;
        }
        assertNotNull(iae);

        System.setProperty("netbeans.t9y.kenai.testUrl", "");
    }

    @Test
    public void testGetPrqojectMissingValues() throws Exception {
        System.out.println("testGetProjectMissingValues");
        IllegalArgumentException iae = null;
        for (String json : Utils.project_alice_missing_values) {
            System.setProperty("netbeans.t9y.kenai.testUrl", json);
            String name = "";
            String username = "";
            char[] password = null;
            KenaiREST kenaiInstance = new KenaiREST(new URL("http://peterp.czech.sun.com"));
            iae = null;
            try {
                ProjectData result = kenaiInstance.getProject(name, null);
            } catch (Exception e) {
                iae = (IllegalArgumentException) e;
            }
            assertNotNull(iae);
        }
        System.setProperty("netbeans.t9y.kenai.testUrl", "");
    }
}
