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

package org.netbeans.modules.maven.api;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

/**
 *
 * @author mkleint
 */
public class ModelUtilsTest extends TestCase {
    
    public ModelUtilsTest(String testName) {
        super(testName);
    }

    public void testCheckLibrary() throws MalformedURLException {
        URL pom = new URL("http://repo1.maven.org/maven2/junit/junit/3.8.2/junit-3.8.2.pom");
        ModelUtils.LibraryDescriptor result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://repo1.maven.org/maven2/", result.getRepoRoot());
        assertEquals("junit", result.getGroupId());
        assertEquals("junit", result.getArtifactId());
        assertEquals("3.8.2", result.getVersion());
        pom = new URL("http://download.java.net/maven/1/toplink.essentials/poms/toplink-essentials-agent-2.0-36.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("legacy", result.getRepoType());
        assertEquals("http://download.java.net/maven/1/", result.getRepoRoot());
        assertEquals("toplink.essentials", result.getGroupId());
        assertEquals("toplink-essentials-agent", result.getArtifactId());
        assertEquals("2.0-36", result.getVersion());

        pom = new URL("http://download.java.net/maven/1/javax.jws/poms/jsr181-api-1.0-MR1.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("legacy", result.getRepoType());
        assertEquals("http://download.java.net/maven/1/", result.getRepoRoot());
        assertEquals("javax.jws", result.getGroupId());
        assertEquals("jsr181-api", result.getArtifactId());
        assertEquals("1.0-MR1", result.getVersion());


        pom = new URL("http://repo1.maven.org/maven2/org/codehaus/mevenide/netbeans-deploy-plugin/1.2.3/netbeans-deploy-plugin-1.2.3.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://repo1.maven.org/maven2/", result.getRepoRoot());
        assertEquals("org.codehaus.mevenide", result.getGroupId());
        assertEquals("netbeans-deploy-plugin", result.getArtifactId());
        assertEquals("1.2.3", result.getVersion());
        
        pom = new URL("http://repository.jboss.org/maven2/junit/junit/3.8.2/junit-3.8.2.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://repository.jboss.org/maven2", result.getRepoRoot());
        assertEquals("junit", result.getGroupId());
        assertEquals("junit", result.getArtifactId());
        assertEquals("3.8.2", result.getVersion());


        pom = new URL("http://repo1.maven.org/maven2/org/testng/testng/5.8/testng-5.8.pom#jdk15");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://repo1.maven.org/maven2/", result.getRepoRoot());
        assertEquals("org.testng", result.getGroupId());
        assertEquals("testng", result.getArtifactId());
        assertEquals("5.8", result.getVersion());
        assertEquals("jdk15", result.getClassifier());



        pom = new URL("http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo/org/eclipse/persistence/javax.persistence/2.0.0-M12/javax.persistence-2.0.0-M12.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo/", result.getRepoRoot());
        assertEquals("org.eclipse.persistence", result.getGroupId());
        assertEquals("javax.persistence", result.getArtifactId());
        assertEquals("2.0.0-M12", result.getVersion());

        pom = new URL("http://download.java.net/maven/glassfish/org/glassfish/extras/glassfish-embedded-all/3.0/glassfish-embedded-all-3.0.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://download.java.net/maven/glassfish/", result.getRepoRoot());
        assertEquals("org.glassfish.extras", result.getGroupId());
        assertEquals("glassfish-embedded-all", result.getArtifactId());
        assertEquals("3.0", result.getVersion());
        
        pom = new URL("http://download.eclipse.org/rt/eclipselink/maven.repo/org/eclipse/persistence/eclipselink/2.3.0/eclipselink-2.3.0.pom");
        result = ModelUtils.checkLibrary(pom);
        assertNotNull(result);
        assertEquals("default", result.getRepoType());
        assertEquals("http://download.eclipse.org/rt/eclipselink/maven.repo/", result.getRepoRoot());
        assertEquals("org.eclipse.persistence", result.getGroupId());
        assertEquals("eclipselink", result.getArtifactId());
        assertEquals("2.3.0", result.getVersion());
    }
    
}
