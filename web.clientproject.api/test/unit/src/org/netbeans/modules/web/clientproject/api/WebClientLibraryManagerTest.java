/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.clientproject.libraries.CDNJSLibrariesProviderTest;
import org.openide.util.test.MockLookup;

public class WebClientLibraryManagerTest extends NbTestCase {

    public WebClientLibraryManagerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new CDNJSLibrariesProviderTest.FakeInstalledFileLocator(getDataDir()));
    }

    /**
     * Test of getLibraries method, of class WebClientLibraryManager.
     */
    public void testGetLibraries() {
        List result = WebClientLibraryManager.getDefault().getLibraries();
        assertTrue("libraries are succcessfully parsed and available", result.size() >= (111+476));
    }

    /**
     * Test of findLibrary method, of class WebClientLibraryManager.
     */
    public void testFindLibrary() {
        Library result = WebClientLibraryManager.getDefault().findLibrary("backbone.js", "0.9.2");
        assertNotNull("backbone 0.9.2 is available", result);
    }

    /**
     * Test of getVersions method, of class WebClientLibraryManager.
     */
    public void testGetVersions() {
        Set<String> result = new HashSet<String>(Arrays.asList(WebClientLibraryManager.getDefault().getVersions("backbone.js")));
        assertTrue("backbone 0.9.2 is available", result.contains("0.9.2"));
        assertTrue("backbone 0.5.3 is available", result.contains("0.5.3"));
    }

    /**
     * Test of getLibraryFilePaths method, of class WebClientLibraryManager.
     */
    public void testGetLibraryFilePaths() {
        Library lib = WebClientLibraryManager.getDefault().findLibrary("backbone.js", "0.9.2");
        List result = WebClientLibraryManager.getDefault().getLibraryFilePaths(lib, WebClientLibraryManager.VOL_MINIFIED);
        assertEquals("backbone has one path", 1, result.size());
        assertEquals("backbone path is right", "backbone.js-0.9.2/backbone-min.js", result.get(0));
    }

}
