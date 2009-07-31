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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.kenai.utils.ServicesChecker;
import static org.junit.Assert.*;
import org.netbeans.modules.kenai.utils.ServicesChecker.ServiceDescr;

/**
 *
 * @author tester
 */
public class KenaiServiceTest extends NbTestCase {

    private static ServicesChecker servicesChecker = null;
    private static Collection<KenaiService> services = null;

    public KenaiServiceTest(String name) {
        super(name);
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
            System.setProperty("kenai.com.url", "https://testkenai.com");
            if (servicesChecker == null) {
                final String _fileName = getDataDir().getAbsolutePath() + File.separatorChar + "services.data";
                System.out.println(_fileName);
                servicesChecker = new ServicesChecker(_fileName);
            }
            if (services == null) {
                services = Kenai.getDefault().getServices();
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
     * Test if number of services returned by the server is the same as the number of service we count with
     * <em>Note: This test doesn't cover any API method</em>
     */
    @Test
    public void testNumberOfServices() {
        // Services returned from the server but unsupported in the IDE, currently those:
        // - WIKIHome
        // - WebSite
        int UNKNOWN_SERVICES = 2;
        if (services.size() + UNKNOWN_SERVICES != servicesChecker.serviceDescriptions.size()) {
            System.out.println("UNKNOWN SERVICES: " + UNKNOWN_SERVICES);
            System.out.println("SERVER RETURNED SERVICES: " + services.size());
            System.out.println("GOLDEN SERVICES: " + servicesChecker.serviceDescriptions.size());
            fail("The list of service returned from the server has different length than a list in the golden file.");
        }
    }

    public void setServicesChecker(ServicesChecker sc) throws KenaiException {
        servicesChecker = sc;
        services = Kenai.getDefault().getServices();
    }

    /**
     * Test if a kenai service contained in the golden file also exists in the list returned by the server
     */
    @Test
    public void testService() {
        System.out.println("testService");
        try {
            // Iterate over every service from the golden file
            for (Iterator<ServiceDescr> it = servicesChecker.serviceDescriptions.iterator(); it.hasNext();) {
                ServiceDescr goldenService = it.next();
                boolean found = false;
                String goldenServiceName = goldenService.name.toLowerCase();
                String goldenServiceType = goldenService.type.toLowerCase();
                System.out.println("\nTesting if s service with the name [" + goldenServiceName + "] and type [" + goldenServiceType + "] is returned from the server");
                // Iterate over all services returned by the server
                for (KenaiService kenaiService : services) {
                    String kenaiServiceName = kenaiService.getName().toLowerCase();
                    String kenaiServiceType = kenaiService.getType().getId().toLowerCase();
                    // If service with a proper name and type was found, test another service from the golden file
                    if (kenaiServiceName.equals(goldenServiceName) && kenaiServiceType.equals(goldenServiceType)) {
                        System.out.println("OK");
                        found = true;
                        break;
                    }
                }
                // Fail test if a service wasn't found
                if (!found) {
                    System.out.println("NOT FOUND");
                    fail("Service with name [" + goldenServiceName + "] of type [" + goldenServiceType + "] was not returned from the server (or Kenai API is not able to recognize the service).\n" +
                            "Check what is in the kenaiService variable.");
                }
            }
        } catch (Throwable e) {
            System.out.println("ERROR");
            e.printStackTrace();
            fail("An unknown exception occured, printing stacktrace and failing.");
        }
    }

    /**
     * Test if description and display name of kenai services stored in the golden file match the ones returned by the server
     */
    @Test
    public void testServiceDescriptionAndDisplayableName() {
        System.out.println("testServiceDescriptionAndDisplayableName");
        try {
            // Iterate over every service from the golden file
            for (Iterator<ServiceDescr> it = servicesChecker.serviceDescriptions.iterator(); it.hasNext();) {
                ServiceDescr goldenService = it.next();
                String goldenServiceName = goldenService.name.toLowerCase();
                String goldenServiceType = goldenService.type.toLowerCase();
                System.out.println("\nTesting description and display name of service with name [" + goldenServiceName + "] and type [" + goldenServiceType + "]");
                // Iterate over all services returned by the server
                for (KenaiService kenaiService : services) {
                    String kenaiServiceName = kenaiService.getName().toLowerCase();
                    String kenaiServiceType = kenaiService.getType().getId().toLowerCase();
                    // If service with a proper name and type was found, test if the description and display name is OK
                    if (kenaiServiceName.equals(goldenServiceName) && kenaiServiceType.equals(goldenServiceType)) {
                        if (!kenaiService.getDescription().equals(goldenService.description)) {
                            System.out.println("Description of the service " + kenaiServiceName + " has changed:\n- expected:" + goldenService.description + "\n- found: " + kenaiService.getDescription());
                            fail("Description of the service " + kenaiServiceName + "  has changed.");
                        }
                        System.out.println("DESCRIPTION - OK");
                        if (!kenaiService.getDisplayName().equals(goldenService.displayName)) {
                            System.out.println("Display name of the service " + kenaiServiceName + " has changed:\n- expected:" + goldenService.displayName + "\n- found: " + kenaiService.getDisplayName());
                            fail("Display name of the service " + kenaiServiceName + "  has changed.");
                        }
                        System.out.println("DISPLAYNAME - OK");
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            System.out.println("ERROR");
            e.printStackTrace();
            fail("An unknown exception occured, printing stacktrace and failing.");
        }
    }
}