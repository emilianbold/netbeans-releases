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

package org.netbeans.modules.kenai.util;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.kenai.util.NbModuleOwnerSupport.ConfigData;
import org.netbeans.modules.kenai.util.NbModuleOwnerSupport.OwnerInfo;

/**
 * @author Marian Petras
 */
public class NbModuleOwnerSupportTest extends NbTestCase {

    private TrivialModuleOwnerSupport moduleOwnerSupport;

    public NbModuleOwnerSupportTest() {
        super(NbModuleOwnerSupport.class.getSimpleName());
        MockServices.setServices(TrivialModuleOwnerSupport.class);
    }

    @Override
    public void setUp() {
        moduleOwnerSupport = (TrivialModuleOwnerSupport)
                             NbModuleOwnerSupport.getInstance();
    }

    @Override
    public void tearDown() {
        moduleOwnerSupport = null;
    }

    public void testEmpty() throws IOException {
        loadCfgData("empty");

        checkOwner("/test", null);
    }

    public void testSimple1() throws IOException {
        loadCfgData("simple1");

        checkOwner("/test", "alpha/beta");
        checkOwner("/tes", null);
    }

    public void testSimple2() throws IOException {
        loadCfgData("simple2");

        checkOwner("/foo", null);
        checkOwner("/alexa", null);
        checkOwner("/alpha", "Paul");
        checkOwner("/beta", "Jane");

        checkOwner("/alpha/bar", "Paul");
        checkOwner("/alpha/boba", "Paul");
        checkOwner("/alpha/beta", "John");
        checkOwner("/alpha/gamma", "David");
    }

    public void testWildcard1() throws IOException {
        loadCfgData("wildcard1");

        checkOwner("/banana", "Chris");
        checkOwner("/bananarama", "Paul");
        checkOwner("/bali", "Jenny");
        checkOwner("/balina", "Jenny");
    }

    public void testWildcard1Universal() throws IOException {
        loadCfgData("wildcard1-uni");

        checkOwner("/banana", "Chris");
        checkOwner("/bananarama", "Paul");
        checkOwner("/bali", "Jenny");
        checkOwner("/balina", "Jenny");
        checkOwner("/hooo", "Rob");
    }

    public void testWildcard2() throws IOException {
        loadCfgData("wildcard2");

        checkOwner("/alpha", null);
        checkOwner("/alpha/beta", "Kirk");

        checkOwner("/beta", null);
        checkOwner("/beta/foo", "Steve");

        checkOwner("/delta", "Lenny");
        checkOwner("/delta/omega", "Lenny");

        checkOwner("/gamma/omega", "Thomas");
    }

    private void checkOwner(String path, String expectedComponentSpec) {
        OwnerInfo expected;
        if (expectedComponentSpec != null) {
            expected = OwnerInfo.parseSpec(expectedComponentSpec);
            if (expected == null) {
                throw new IllegalArgumentException(
                        "Illegal component/subcomponent specification: "
                        + expectedComponentSpec);
            }
        } else {
            expected = null;
        }

        assertEquals(expected, moduleOwnerSupport.getOwner("dummy", new File(path)));
    }

    private void loadCfgData(String fileName) throws IOException {
        final File dataDir = getDataDir();
        final File configFile = new File(dataDir, fileName + ".cfg");
        assert configFile.isFile() && configFile.canRead();
        moduleOwnerSupport.loadData(configFile);
    }

    @Override
    public File getDataDir() {
        return new File(super.getDataDir(), "NbModuleOwnerSupport-files");
    }

    public static class TrivialModuleOwnerSupport extends NbModuleOwnerSupport {

        private ConfigData configData;

        void loadData(File configFile) throws IOException {
            configData = ConfigData.load(configFile);
        }

        @Override
        protected ConfigData getData(File configFile) {
            return configData;
        }

        @Override
        public OwnerInfo getOwnerImpl(String configFileName,
                                      File absolutePath) {
            return configData.getMatchingInfo(getRelativePath(absolutePath));
        }

        private String getRelativePath(File absolutePath) {
            File root = getRoot(absolutePath);
            
            String relative = absolutePath.getPath().substring(
                                                       root.getPath().length());
            char firstChar = relative.charAt(0);
            if ((firstChar == '/') || (firstChar == File.separatorChar)) {
                relative = relative.substring(1);
            }
            return relative;
        }

        private File getRoot(File path) {
            File parent = path.getParentFile();
            while (parent != null) {
                path = parent;
                parent = path.getParentFile();
            }

            return path;
        }

    }

    private static void assertEquals(OwnerInfo expected,
                                     OwnerInfo actual) {
        assertEquals(null, expected, actual);
    }

    private static void assertEquals(String message,
                                     OwnerInfo expected,
                                     OwnerInfo actual) {
        if (expected == null) {
            if (actual == null) {
                return;
            }
        } else if (expected.equals(actual)) {
            return;
        }

        failNotEquals(message, getParamString(expected),
                               getParamString(actual));
    }

    private static String getParamString(OwnerInfo ownerInfo) {
        return (ownerInfo != null) ? ownerInfo.paramString() : null;
    }

}