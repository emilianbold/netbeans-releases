/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.project.ui;

import java.util.HashMap;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.KeyStroke;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;

/**
 * @author Martin Krauskopf
 */
public class UIUtilTest extends LayerTestBase {
    
    public UIUtilTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
    }
    
    /**
     * Test of createLayerPresenterComboModel method, of class org.netbeans.modules.apisupport.project.ui.UIUtil.
     */
    public void testCreateLayerPresenterComboModel() throws Exception {
        Project project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        Map<String,Object> excludes = new HashMap<String,Object>();
        excludes.put("template", true);
        excludes.put("simple", false);
        String sfsRoot = "Templates";
        ComboBoxModel allModel = UIUtil.createLayerPresenterComboModel(project, sfsRoot);
        ComboBoxModel excludedModel = UIUtil.createLayerPresenterComboModel(project, sfsRoot, excludes);
        assertTrue("UIUtil.createLayerPresenterComboModel() doesn't work.", allModel.getSize() >= excludedModel.getSize());
    }
    
    public void testKeyToLogicalString() throws Exception {
        assertKeyLogicalString("X", "pressed X");
        assertKeyLogicalString("D-X", "ctrl pressed X");
        assertKeyLogicalString("DO-X", "ctrl alt pressed X");
        assertKeyLogicalString("DS-X", "shift ctrl pressed X");
        assertKeyLogicalString("OS-X", "shift alt pressed X");
        assertKeyLogicalString("DOS-X", "shift ctrl alt pressed X");
        assertKeyLogicalString("ENTER", "pressed ENTER");
    }
    
    private void assertKeyLogicalString(String expected, String swingKeyStroke) {
        assertEquals(swingKeyStroke + " corresponding to " + expected, expected, UIUtil.keyToLogicalString(KeyStroke.getKeyStroke(swingKeyStroke)));
    }

    /* XXX rewrite to not rely on nb.org:
    public void testLayerItemPresenterCompareTo() throws Exception {
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(project);
        FileObject root = fs.getRoot().getFileObject("Templates/Project/APISupport");
        FileObject module = root.getFileObject("emptyModule");
        FileObject suite = root.getFileObject("emptySuite");
        FileObject library = root.getFileObject("libraryModule");
        LayerItemPresenter moduleLIP = new LayerItemPresenter(module, root);
        LayerItemPresenter moduleLIP1 = new LayerItemPresenter(module, root);
        LayerItemPresenter suiteLIP = new LayerItemPresenter(suite, root);
        LayerItemPresenter libraryLIP = new LayerItemPresenter(library, root);
        assertTrue("'Module Project' < 'Module Suite Project'", moduleLIP.compareTo(suiteLIP) < 0);
        assertTrue("'Module Project' == 'Module Project'", moduleLIP.compareTo(moduleLIP1) == 0);
        assertTrue("'Library Wrapper Module Project < 'Module Project'", libraryLIP.compareTo(moduleLIP) < 0);
        assertTrue("'Library Wrapper Module Project < 'Module Suite Project'", libraryLIP.compareTo(suiteLIP) < 0);
    }
     */
    
}
