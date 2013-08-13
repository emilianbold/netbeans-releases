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

package org.netbeans.modules.team.server.ui.common;

import org.netbeans.modules.team.server.ui.common.NbModuleOwnerSupport;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.team.server.ui.common.NbModuleOwnerSupport.ConfigData;
import org.netbeans.modules.team.server.ui.common.NbModuleOwnerSupport.OwnerInfo;

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

    public void testSimple1() throws IOException {
        loadCfgData("simple1");

        checkOwner("/test", "alpha", "beta");
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

    public void testSpaceAroundEqualSign() throws Exception {
        loadCfgData("space_around_equalsign");

        checkOwner("/path001", "value001");
        checkOwner("/path002", "value002");
        checkOwner("/path003", "value003");
        checkOwner("/path004", "value004");
        checkOwner("/path005", "value005");
        checkOwner("/path006", "value006");
        checkOwner("/path007", "value007");
        checkOwner("/path010", "value010");
        checkOwner("/path011", "value011");
        checkOwner("/path012", "value012");

        checkOwner("/path013", null);
        checkOwner("/path013 ", "value013");
        checkOwner("/path014", null);
        checkOwner("/path014  ", "value014");
        checkOwner("/path015", null);
        checkOwner("/path015  ", "value015");
        checkOwner("/path016", null);
        checkOwner("/path016   ", "value016");
        checkOwner("/path017", null);
        checkOwner("/path017   ", "value017");
        checkOwner("/path018", null);
        checkOwner("/path018   ", "value018");
        checkOwner("/path019", null);
        checkOwner("/path019     ", "value019");
        checkOwner("/path020", null);
        checkOwner("/path020 ", "value020");
        checkOwner("/path021", null);
        checkOwner("/path021\t ", "value021");
        checkOwner("/path022", null);
        checkOwner("/path022\t", "value022");
        checkOwner("/path023", null);
        checkOwner("/path023\t\t", "value023");
        checkOwner("/path024", null);
        checkOwner("/path024\t\t", "value024");
        checkOwner("/path025", null);
        checkOwner("/path025 =", "value025");
        checkOwner("/path026", null);
        checkOwner("/path026  =", "value026");
        checkOwner("/path027", null);
        checkOwner("/path027  =", "value027");
        checkOwner("/path028", null);
        checkOwner("/path028  ==", "value028");
        checkOwner("/path029", null);
        checkOwner("/path029  = =", "value029");
        checkOwner("/path030", null);
        checkOwner("/path030  =", "value030");

        checkOwner("/path131", "value131");
        checkOwner("/path132", "value132");
        checkOwner("/path133", "value133");
        checkOwner("/path134", "value134");
        checkOwner("/path135", "value135");
        checkOwner("/path136", "value136");
        checkOwner("/path137", "value137");
        checkOwner("/path140", "value140");
        checkOwner("/path141", "value141");
        checkOwner("/path142", "value142");

        checkOwner("/path143", " value143");
        checkOwner("/path144", "  value144");
        checkOwner("/path145", "  value145");
        checkOwner("/path146", "   value146");
        checkOwner("/path147", "   value147");
        checkOwner("/path148", "   value148");
        checkOwner("/path149", "     value149");
        checkOwner("/path150", " value150");
        checkOwner("/path151", " \tvalue151");
        checkOwner("/path152", "\tvalue152");
        checkOwner("/path153", "\t\tvalue153");
        checkOwner("/path154", "\t\tvalue154");
    }

    public void testEscapingOnRightSide() throws Exception {
        loadCfgData("escaping_chars_in_assigned_data");

        checkOwner("/path00", null);
        checkOwner("/path01", null);
        checkOwner("/path02", null);
        checkOwner("/path03", null);
        checkOwner("/path04", null);
        checkOwner("/path05", null);
        checkOwner("/path06", null);
        checkOwner("/path07", null);
        checkOwner("/path08", null);
        checkOwner("/path09", " ");
        checkOwner("/path10", "\t");
        checkOwner("/path11", "  ");
        checkOwner("/path12", "   ");
        checkOwner("/path13", "  \t");
        checkOwner("/path14", "\t\t");
        checkOwner("/path15", "\t\t\t");
        checkOwner("/path16", "\t \t");
        checkOwner("/path17", "\t  ");
        checkOwner("/path18", "=  ");
        checkOwner("/path19", "==");
        checkOwner("/path20", "==");
        checkOwner("/path21", "=");
        checkOwner("/path22", "===");
        checkOwner("/path23", "=  value");
        checkOwner("/path24", "==value");
        checkOwner("/path25", "==value");
        checkOwner("/path26", "=value");
        checkOwner("/path27", "===value");
        checkOwner("/path28", "\\");
        checkOwner("/path29", "\\=");
        checkOwner("/path30", "\\ ");
        checkOwner("/path31", "\\\t");
        checkOwner("/path32", "\\=");
        checkOwner("/path33", "\\value");
        checkOwner("/path34", "\\=value");
        checkOwner("/path35", "\\ value");
        checkOwner("/path36", "\\\tvalue");
        checkOwner("/path37", "\\=value");
        checkOwner("/path38", "alpha beta");
        checkOwner("/path39", "alpha\tbeta");
        checkOwner("/path40", "alpha=beta");
        checkOwner("/path41", "alpha\\beta");
        checkOwner("/path42", "abcdefghijklmnopqrstuvwxyz");
        checkOwner("/path43", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        checkOwner("/path44", "0123456789");
        checkOwner("/path45", ".,;:?!@&$%*()#+=|[]{}\"'`<>-~");

        checkOwner("/path101", "alpha", "beta", "gamma");
        checkOwner("/path102", "alpha/beta", "gamma");
        checkOwner("/path103", "alpha", "beta/gamma");
        checkOwner("/path104", "alpha", "", "beta", "gamma");
        checkOwner("/path105", "alpha", "/beta", "gamma");
    }

    public void testExcludingTreeFromAssignment() throws Exception {
        loadCfgData("excluding_tree_from_assignment");

        checkOwner("/path01", "foo");
        checkOwner("/path01/subtree", "foo");
        checkOwner("/path01/excluded", null);
    }

    public void testComments() throws Exception {
        loadCfgData("commented_lines");

        checkOwner("/path01", "foo");
        checkOwner("/path02", null);
        checkOwner("/#path02", null);
        checkOwner("/\\#path02", null);
        checkOwner("/#path03", "baz");
    }

    private void checkOwner(String path, String expectedOwner, String... expectedExtraData) {
        OwnerInfo expected;
        if (expectedOwner != null) {
            expected = new OwnerInfo(expectedOwner, expectedExtraData);
        } else {
            expected = null;
        }

        assertEquals(expected, moduleOwnerSupport.getOwnerInfo("dummy", new File(path)));
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
