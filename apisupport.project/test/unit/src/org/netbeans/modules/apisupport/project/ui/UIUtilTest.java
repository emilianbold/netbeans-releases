/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.util.HashMap;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.KeyStroke;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.UIUtil.LayerItemPresenter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

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
    
}
