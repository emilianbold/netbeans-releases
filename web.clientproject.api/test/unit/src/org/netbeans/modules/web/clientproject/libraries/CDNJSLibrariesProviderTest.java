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
package org.netbeans.modules.web.clientproject.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.test.MockLookup;

public class CDNJSLibrariesProviderTest extends NbTestCase {

    public CDNJSLibrariesProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new FakeInstalledFileLocator(getDataDir()));
    }

    public void testReadLibraries() throws IOException {
        InputStream is = new FileInputStream(new File(getDataDir(), "cdnjs-test.zip"));
        List<LibraryImplementation> libs = CDNJSLibrariesProvider.getDefault().readLibraries(is, null);
        assertEquals(3, libs.size());
        LibraryImplementation l = findLibrary(libs, "cdnjs-backbone.js-0.9.10");
        assertNotNull(l);
        assertEquals("http://cdnjs.cloudflare.com/ajax/libs/backbone.js/0.9.10/backbone-min.js",
                l.getContent(WebClientLibraryManager.VOL_MINIFIED).get(0).toExternalForm());
        assertTrue(l.getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty());
        l = findLibrary(libs, "cdnjs-backbone.js-1.0.0");
        assertNotNull(l);
        assertEquals("http://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.0.0/backbone.js",
                l.getContent(WebClientLibraryManager.VOL_REGULAR).get(0).toExternalForm());
        assertEquals("http://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.0.0/backbone-min.js",
                l.getContent(WebClientLibraryManager.VOL_MINIFIED).get(0).toExternalForm());
    }

    LibraryImplementation findLibrary (List<LibraryImplementation> libs, String name) {
        for (LibraryImplementation l : libs) {
            if (l.getName().equals(name)) {
                return l;
            }
        }
        return null;
    }
    /*
     * Below method will dump of files which causes some inconsistence. The inconsistency
     * means that a library has minified and corresponding regular files but not
     * all minified files have these regular files. Most the these inconsisntencies
     * are harmless, for example it will reports "lodash.js/0.10.0/lodash.underscore.min.js"
     * and that's OK - underscore library is dependency of lodash library which is provided
     * both in release and debug versions but undescore dependency come only in minimized
     * version. The only two real problems I discovered so far are:
     *  - jquery-ui-map/3.0-rc1
     *  - jqueryui/1.10.3
     * These two libraries bundle minified files in a different folder. Something we could handle
     * on case by case basis. Because these are two libraries out of few hundreds I'm ignoring
     * it for now.
     */
    public void testZipSanity() throws IOException {
        InputStream is = CDNJSLibrariesProvider.getDefaultSnapshostFile();
        List<String> unmatchedMinifiedFiles = new ArrayList<>();
        List<LibraryImplementation> libs = CDNJSLibrariesProvider.getDefault().readLibraries(is, unmatchedMinifiedFiles);
        assertTrue("libraries are succcessfully parsed and recreated " + libs.size(), libs.size() >= 1000);
        if (!unmatchedMinifiedFiles.isEmpty()) {
            for (String s : unmatchedMinifiedFiles) {
                System.out.println(s);
            }
        }
    }

    public static class FakeInstalledFileLocator extends InstalledFileLocator {

        private File dataDir;

        public FakeInstalledFileLocator(File dataDir) {
            this.dataDir = dataDir;
        }
        
        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if ("modules/ext/cdnjs.zip".equals(relativePath)) {
                File f = FileUtil.normalizeFile(new File(dataDir, "../../../../external/cdnjs.zip"));
                assertTrue("cannot find "+f, f.exists());
                return f;
            }
            return null;
        }

    }
}
