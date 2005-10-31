/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        TestBase.initializeBuildProperties(getWorkDir());
    }
    
    /**
     * Test of createLayerPresenterComboModel method, of class org.netbeans.modules.apisupport.project.ui.UIUtil.
     */
    public void testCreateLayerPresenterComboModel() throws Exception {
        Project project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        Map excludes = new HashMap();
        excludes.put("template", Boolean.TRUE);
        excludes.put("simple", Boolean.FALSE);
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
    
    public void assertKeyLogicalString(String expected, String swingKeyStroke) {
        assertEquals(swingKeyStroke + " corresponding to " + expected, expected, UIUtil.keyToLogicalString(KeyStroke.getKeyStroke(swingKeyStroke)));
    }
    
}
