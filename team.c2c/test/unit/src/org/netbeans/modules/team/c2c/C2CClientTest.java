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

package org.netbeans.modules.team.c2c;

import com.tasktop.c2c.server.profile.domain.project.Profile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import junit.framework.Test;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 *
 * @author tomas
 */
public class C2CClientTest extends NbTestCase  {

    public static Test suite() {
        return NbModuleSuite.create(C2CClientTest.class, null, null);
    }
    private static boolean firstRun = true;
    private static String uname;
    private static String passw;
    private static String proxy;
    private ClassPathXmlApplicationContext context;
    
    public C2CClientTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        if (firstRun) {
            if (uname == null) {
                BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-team")));
                uname = br.readLine();
                passw = br.readLine();
                proxy = br.readLine();
                br.close();
            }
            if (firstRun) {
                firstRun = false;
            }
        }
        if (!proxy.isEmpty()) {
            System.setProperty("netbeans.system_http_proxy", proxy);
        }
    }
    
    public void testCreateClient () throws Exception {
        CloudClient client = getClient();
        Profile currentProfile = client.getCurrentProfile();
        assertNotNull(currentProfile.getFirstName());
        assertNotNull(currentProfile.getLastName());
    }

    private CloudClient getClient () {
        return ClientFactory.getInstance().getClient(new WebLocation("https://q.tasktop.com/", 
                uname, 
                passw, 
                new ClientFactory.ProxyProvider()));
    }

}
