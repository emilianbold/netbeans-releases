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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997/2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.upgrade.systemoptions;

/**
 * @author Radek Matous
 */
public class FormSettingsTest extends BasicTestForImport {
    public FormSettingsTest(String testName) {
        super(testName, "formsettings.settings");
    }
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/form");
    }
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "applyGridToPosition",
                    "applyGridToSize",
                    "connectionBorderColor",
                    "displayWritableOnly",
                    "dragBorderColor",
                    "editorSearchPath",
                    "eventVariableName",
                    "foldGeneratedCode",
                    "formDesignerBackgroundColor",
                    "formDesignerBorderColor",
                    "generateMnemonicsCode",
                    "gridX",
                    "gridY",
                    "guidingLineColor",
                    "listenerGenerationStyle",
                    "registeredEditors",
                    "selectionBorderColor",
                    "selectionBorderSize",
                    "toolBarPalette",
                    "useIndentEngine",
                    "variablesLocal",
                    "variablesModifier"
        });
    }
    
    public void testApplyGridToSize() throws Exception { assertProperty("applyGridToSize","true"); }
    public void testDisplayWritableOnly() throws Exception { assertProperty("displayWritableOnly","true"); }
    public void testEventVariableName() throws Exception { assertProperty("eventVariableName","evt"); }
    public void testFoldGeneratedCode() throws Exception { assertProperty("foldGeneratedCode","true"); }
    public void testGenerateMnemonicsCode() throws Exception { assertProperty("generateMnemonicsCode","false"); }
    public void testGridX() throws Exception { assertProperty("gridX","10"); }
    public void testGridY() throws Exception { assertProperty("gridY","10"); }
    public void testListenerGenerationStyle() throws Exception { assertProperty("listenerGenerationStyle","0"); }
    public void testSelectionBorderSize() throws Exception { assertProperty("selectionBorderSize","1"); }
    public void testToolBarPalette() throws Exception { assertProperty("toolBarPalette","true"); }
    public void testUseIndentEngine() throws Exception { assertProperty("useIndentEngine","false"); }
    public void testVariablesLocal() throws Exception { assertProperty("variablesLocal","true"); }
    public void testVariablesModifie() throws Exception { assertProperty("variablesModifier","0"); }
    public void testApplyGridToPosition() throws Exception { assertProperty("applyGridToPosition","true"); }
    
    public void testEditorSearchPath() throws Exception { assertProperty("editorSearchPath","org.netbeans.modules.form.editors2"); }    
    public void testRegisteredEditors() throws Exception { assertProperty("registeredEditors","aaaaaaa | bbbbbbbbb"); }    

    public void testConnectionBorderColor() throws Exception { assertProperty("connectionBorderColor","-65536"); }
    public void testDragBorderColor() throws Exception { assertProperty("dragBorderColor","-8355712"); }
    public void testFormDesignerBackgroundColor() throws Exception { assertProperty("formDesignerBackgroundColor","-1"); }
    public void testFormDesignerBorderColor() throws Exception { assertProperty("formDesignerBorderColor","-2039553"); }
    public void testGuidingLineColor() throws Exception { assertProperty("guidingLineColor","-7361596"); }
    public void testSelectionBorderColor() throws Exception { assertProperty("selectionBorderColor","-23552"); }    
}
