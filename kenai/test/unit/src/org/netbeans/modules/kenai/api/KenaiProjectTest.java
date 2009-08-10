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

package org.netbeans.modules.kenai.api;

import java.net.MalformedURLException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Becicka
 */
public class KenaiProjectTest extends NbTestCase {

    static String UNITTESTUNIQUENAME = "golden-project-1";

    public KenaiProjectTest(String s) {
        super(s);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public void setProjectName(String name) {
        UNITTESTUNIQUENAME = name;
    }

    @Before
    @Override
    public void setUp() {
        try {
            System.setProperty("kenai.com.url","https://testkenai.com");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Test of forRepository method, of class KenaiProject.
     */
    @Test
    public void testForRepositorySvn() throws Exception {
        System.out.println("forRepositorySvn");
        KenaiProject proj = null;
        try {
            proj = Kenai.getDefault().getProject(UNITTESTUNIQUENAME);
        } catch (KenaiException e) {
            fail("Project " + UNITTESTUNIQUENAME + " not found - testForRepository fails");
        }
        KenaiFeature[] features = proj.getFeatures(KenaiService.Type.SOURCE);
        String uri = "";
        for (int i = 0; i < features.length; i++) {
            KenaiFeature kenaiFeature = features[i];
            if (kenaiFeature.getLocation().toString().toLowerCase().contains("svn")) {
                uri = kenaiFeature.getLocation().toString();
                break;
            }
        }
        KenaiProject result = KenaiProject.forRepository(uri);
        assert result.getName().equals(UNITTESTUNIQUENAME);
    }
    /**
     * Test of forRepository method, of class KenaiProject.
     */
    @Test
    public void testForRepositoryHg() throws Exception {
        System.out.println("forRepositoryHg");
        KenaiProject proj = null;
        try {
            proj = Kenai.getDefault().getProject(UNITTESTUNIQUENAME);
        } catch (KenaiException e) {
            fail("Project " + UNITTESTUNIQUENAME + " not found - testForRepository fails");
        }
        KenaiFeature[] features = proj.getFeatures(KenaiService.Type.SOURCE);
        String uri = "";
        for (int i = 0; i < features.length; i++) {
            KenaiFeature kenaiFeature = features[i];
            if (kenaiFeature.getLocation().toString().toLowerCase().contains("hg")) {
                uri = kenaiFeature.getLocation().toString();
                break;
            }
        }
        KenaiProject result = KenaiProject.forRepository(uri);
        assert result.getName().equals(UNITTESTUNIQUENAME);
    }

    @Test
    public void testCheckName() throws KenaiException, MalformedURLException {
        assertNull(KenaiProject.checkName("non-existing-project"));
        assertNotNull("Project does not exist, but it should...", KenaiProject.checkName(UNITTESTUNIQUENAME));
        assertTrue(KenaiProject.checkName(UNITTESTUNIQUENAME).equals("Name has already been taken"));
    }
}