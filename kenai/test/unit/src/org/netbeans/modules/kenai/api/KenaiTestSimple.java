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
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.kenai.utils.ServicesChecker;
import org.openide.util.Exceptions;

/**
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public class KenaiTestSimple extends NbTestCase {

    private static Kenai instance;
    private static String uname = null;
    private static String passw = null;
    private static boolean firstRun = true;

    private static final String TEST_PROJECT = "nb-jnet-test";   
    
    static {
        try {
            instance = KenaiManager.getDefault().createKenai("testjava.net", "https://testjava.net");
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public KenaiTestSimple(String S) {
        super(S);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
        try {
            final Logger logger = Logger.getLogger("TIMER.kenai");
            logger.setLevel(Level.FINE);
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
                instance.login(uname, passw.toCharArray(), false);
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

    public void testForRepositoryHg() throws Exception {
        KenaiProjectTest kpt = new KenaiProjectTest(getName());
        kpt.testForRepositoryHg();
    }

    public void testForRepositorySvn() throws Exception {
        KenaiProjectTest kpt = new KenaiProjectTest(getName());
        kpt.testForRepositorySvn();
    }

    public void testCheckName() throws KenaiException, MalformedURLException {
        assertNull(instance.checkProjectName("non-existing-project"));
        assertNotNull("Project does not exist, but it should...", instance.checkProjectName(TEST_PROJECT));
        assertTrue(instance.checkProjectName(TEST_PROJECT).equals("Name has already been taken"));
    }

    public void testService() throws IOException {
        KenaiServiceTest kst = new KenaiServiceTest(getName());
        kst.setServicesChecker(new ServicesChecker(getDataDir().getAbsolutePath() + File.separatorChar + "services.data"));
        kst.testService();
    }

    public void testType() {
        new KenaiFeatureTest(getName()).testType();
    }

    public void testSearchProjects() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testSearchProjects();
    }

    public void testSearchProjectsLessSpecific() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testSearchProjectsLessSpecific();
    }

    public void testSearchProjectsWithSpace() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testSearchProjectsWithSpace();
    }

    public void testGetProject() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testGetProject();
    }

    public void testGetDescription() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testGetDescription();
    }

    public void testGetDisplayName() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testGetDisplayName();
    }

    public void testGetTags() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testGetTags();
    }

    public void testGetLicenses() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testGetLicenses();
    }
    
    public void testGetServices() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testGetServices();
    }
    
    public void testGetWebLocation() throws Exception {
        KenaiTest kt = new KenaiTest(getName());
        kt.testGetWebLocation();
    }

}
