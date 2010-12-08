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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.api;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.kenai.utils.ServicesChecker;
import static org.junit.Assert.*;
import org.netbeans.modules.kenai.utils.ServicesChecker.ServiceDescr;

/**
 *
 * @author tester
 */
public class KenaiServiceTest extends AbstractKenaiTestCase {

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
    public void setUp() throws Exception {
        super.setUp();
        try {
            if (servicesChecker == null) {
                final String _fileName = getDataDir().getAbsolutePath() + File.separatorChar + "services.data";
                System.out.println(_fileName);
                servicesChecker = new ServicesChecker(_fileName);
            }
            if (services == null) {
                services = getKenai().getServices();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setServicesChecker(ServicesChecker sc) throws KenaiException {
        servicesChecker = sc;
        services = getKenai().getServices();
    }

    /**
     * Test if a kenai service contained in the golden file also exists in the list returned by the server
     */
    @Test
    public void testService() {
        System.out.println("testService");                
        // Iterate over every service from the golden file
        for (Iterator<ServiceDescr> it = servicesChecker.serviceDescriptions.iterator(); it.hasNext();) {
            boolean found = false;
            ServiceDescr goldenService = it.next();
            String goldenServiceName = goldenService.name.toLowerCase();
            String goldenServiceType = goldenService.type.toLowerCase();
            try {
                System.out.println("\nTesting if s service with the name [" + goldenServiceName + "] and type [" + goldenServiceType + "] is returned from the server");
                // Iterate over all services returned by the server
                for (KenaiService kenaiService : services) {
                    String kenaiServiceName = kenaiService.getName().toLowerCase();
                    String kenaiServiceType = kenaiService.getType().getId().toLowerCase();
                    // If service with a proper name and type was found, test another service from the golden file
                    if (kenaiServiceName.equals(goldenServiceName) && kenaiServiceType.equals(goldenServiceType)) {
                        System.out.println("OK");
                        found = true;

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
            } catch (Throwable e) {
                System.out.println("ERROR");
                e.printStackTrace();
                fail("An unknown exception occured, printing stacktrace and failing.");
            }
            // Fail test if a service wasn't found
            if (!found && getKenai().getUrl().toString().startsWith("https://testjava.net") && !"instant_messenger".equals(goldenServiceName)) {
                System.out.println("NOT FOUND");
                fail("Service with name [" + goldenServiceName + "] of type [" + goldenServiceType + "] was not returned from the server (or Kenai API is not able to recognize the service).\n" +
                        "Check what is in the kenaiService variable.");
            }
        }
    }
}
