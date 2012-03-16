/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.library;

import java.util.HashMap;
import java.util.StringTokenizer;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.coherence.server.util.Version;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class LibraryUtilsTest extends NbTestCase {

    public LibraryUtilsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registerLibraryTypeProvider();
    }

    public void testGetCoherenceLibraryDisplayName() throws Exception {
        String coherenceLibraryDisplayName = LibraryUtils.getCoherenceLibraryDisplayName(null);
        assertEquals("Coherence", coherenceLibraryDisplayName);

        Version version = Version.fromDottedNotationWithFallback("3.7.1.1.1");
        coherenceLibraryDisplayName = LibraryUtils.getCoherenceLibraryDisplayName(version);
        assertEquals("Coherence 3.7.1", coherenceLibraryDisplayName);
    }

    public void testRegisterCoherenceLibrary() throws Exception {
        LibraryManager libManager = LibraryManager.getDefault();
        assertNull(libManager.getLibrary("Coherence-3.7.1"));

        Version version = Version.fromDottedNotationWithFallback("3.7.1.1.1");
        String coherenceLibraryDisplayName = LibraryUtils.getCoherenceLibraryDisplayName(version);
        boolean wasCreated = LibraryUtils.registerCoherenceLibrary(coherenceLibraryDisplayName, getWorkDir());
        assertEquals(true, wasCreated);
        assertNotNull(libManager.getLibrary("coherence-3.7.1"));
        assertEquals(false, LibraryUtils.registerCoherenceLibrary(coherenceLibraryDisplayName, getWorkDir()));
    }

    public void testParseLibraryName() throws Exception {
        Version version = Version.fromDottedNotationWithFallback("3.7.1.1.1");
        String coherenceLibraryDisplayName = LibraryUtils.getCoherenceLibraryDisplayName(version);
        assertEquals("coherence-3.7.1", LibraryUtils.parseLibraryName(coherenceLibraryDisplayName));
    }

    private static void registerLibraryTypeProvider () throws Exception {
        StringTokenizer tk = new StringTokenizer("org-netbeans-api-project-libraries/LibraryTypeProviders","/");
        FileObject root = FileUtil.getConfigRoot();
        while (tk.hasMoreElements()) {
            String pathElement = tk.nextToken();
            FileObject tmp = root.getFileObject(pathElement);
            if (tmp == null) {
                tmp = root.createFolder(pathElement);
            }
            root = tmp;
        }
        if (root.getChildren().length == 0) {
            InstanceDataObject.create (DataFolder.findFolder(root),"TestLibraryTypeProvider",TestLibraryTypeProvider.class);
        }
    }

    public static class TestLibraryTypeProvider implements LibraryTypeProvider {

        private static final String LIBRARY_TYPE = "j2se";
        private static final String[] VOLUME_TYPES = new String[] {
            "classpath"
        };

        public String getDisplayName() {
            return LIBRARY_TYPE;
        }

        public String getLibraryType() {
            return LIBRARY_TYPE;
        }

        public String[] getSupportedVolumeTypes() {
            return VOLUME_TYPES;
        }

        public LibraryImplementation createLibrary() {
            assert !ProjectManager.mutex().isReadAccess();
            DefaultLibraryImplementation libraryImpl = new DefaultLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
            libraryImpl.setProperties(new HashMap<String, String>());
            return libraryImpl;
        }

        public void libraryDeleted(LibraryImplementation library) {
        }

        public void libraryCreated(LibraryImplementation library) {
        }

        public java.beans.Customizer getCustomizer(String volumeType) {
            return null;
        }

        public org.openide.util.Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

}
