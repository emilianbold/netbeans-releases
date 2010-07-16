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

package org.netbeans.modules.maven;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

/**
 *
 * @author mkleint
 */
public class CPExtenderTest extends TestCase {
    
    public CPExtenderTest(String testName) {
        super(testName);
    }

    public void testCheckLibrary() throws MalformedURLException {
        System.out.println("checkLibrary");
        URL[] repos = new URL[] {
            new URL("http://repo1.maven.org/maven2/"),
            new URL("http://download.java.net/maven/1/")
        };
        URL pom = new URL("http://repo1.maven.org/maven2/junit/junit/3.8.2/junit-3.8.2.pom");
        String[] result = CPExtender.checkLibrary(pom, repos);
        assertNotNull(result);
        assertEquals("default", result[0]);
        assertEquals("http://repo1.maven.org/maven2/", result[1]);
        assertEquals("junit", result[2]);
        assertEquals("junit", result[3]);
        assertEquals("3.8.2", result[4]);
        pom = new URL("http://download.java.net/maven/1/toplink.essentials/poms/toplink-essentials-agent-2.0-36.pom");
        result = CPExtender.checkLibrary(pom, repos);
        assertNotNull(result);
        assertEquals("legacy", result[0]);
        assertEquals("http://download.java.net/maven/1/", result[1]);
        assertEquals("toplink.essentials", result[2]);
        assertEquals("toplink-essentials-agent", result[3]);
        assertEquals("2.0-36", result[4]);

        pom = new URL("http://download.java.net/maven/1/javax.jws/poms/jsr181-api-1.0-MR1.pom");
        result = CPExtender.checkLibrary(pom, repos);
        assertNotNull(result);
        assertEquals("legacy", result[0]);
        assertEquals("http://download.java.net/maven/1/", result[1]);
        assertEquals("javax.jws", result[2]);
        assertEquals("jsr181-api", result[3]);
        assertEquals("1.0-MR1", result[4]);


        pom = new URL("http://repo1.maven.org/maven2/org/codehaus/mevenide/netbeans-deploy-plugin/1.2.3/netbeans-deploy-plugin-1.2.3.pom");
        result = CPExtender.checkLibrary(pom, repos);
        assertNotNull(result);
        assertEquals("default", result[0]);
        assertEquals("http://repo1.maven.org/maven2/", result[1]);
        assertEquals("org.codehaus.mevenide", result[2]);
        assertEquals("netbeans-deploy-plugin", result[3]);
        assertEquals("1.2.3", result[4]);
        
        pom = new URL("http://repository.jboss.org/maven2/junit/junit/3.8.2/junit-3.8.2.pom");
        result = CPExtender.checkLibrary(pom, repos);
        assertNotNull(result);
        assertEquals("default", result[0]);
        assertEquals("http://repository.jboss.org/maven2", result[1]);
        assertEquals("junit", result[2]);
        assertEquals("junit", result[3]);
        assertEquals("3.8.2", result[4]);


        pom = new URL("http://repo1.maven.org/maven2/org/testng/testng/5.8/testng-5.8.pom#jdk15");
        result = CPExtender.checkLibrary(pom, repos);
        assertNotNull(result);
        assertEquals("default", result[0]);
        assertEquals("http://repo1.maven.org/maven2/", result[1]);
        assertEquals("org.testng", result[2]);
        assertEquals("testng", result[3]);
        assertEquals("5.8", result[4]);
        assertEquals("jdk15", result[5]);



        pom = new URL("http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo/org/eclipse/persistence/javax.persistence/2.0.0-M12/javax.persistence-2.0.0-M12.pom");
        result = CPExtender.checkLibrary(pom, repos);
        assertNotNull(result);
        assertEquals("default", result[0]);
        assertEquals("http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo", result[1]);
        assertEquals("org.eclipse.persistence", result[2]);
        assertEquals("javax.persistence", result[3]);
        assertEquals("2.0.0-M12", result[4]);
    }
    
}
