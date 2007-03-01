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

package popup_menus;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import lib.EditorTestCase;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * Test behavior of main menus - Edit, View
 * @author Martin Roskanin
 */
public class MainMenuTest extends MenuTest {
    
    String xmlFile =  "testMainMenu.xml";
    
    /** Creates a new instance of Main */
    public MainMenuTest(String testMethodName) {
        super(testMethodName);
    }
    
    
    public void testMainMenu(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            
            EditorOperator editor = getDefaultSampleEditorOperator();
            JTextComponentOperator text = new JTextComponentOperator(editor);
            final JTextComponent target = (JTextComponent)text.getSource();
            
            
            boolean lineNumberVisibleSetting = SettingsUtil.getBoolean(org.netbeans.editor.Utilities.getKitClass(target),
                    SettingsNames.LINE_NUMBER_VISIBLE,
                    SettingsDefaults.defaultLineNumberVisible);
            
            //enable line number
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_S);
            
            ValueResolver resolver = new ValueResolver(){
                public Object getValue(){
                    return SettingsUtil.getValue(org.netbeans.editor.Utilities.getKitClass(target),
                            SettingsNames.LINE_NUMBER_VISIBLE,
                            Boolean.FALSE);
                }
            };
            
            waitMaxMilisForValue(2000, resolver, Boolean.TRUE);
            
            lineNumberVisibleSetting = SettingsUtil.getBoolean(org.netbeans.editor.Utilities.getKitClass(target),
                    SettingsNames.LINE_NUMBER_VISIBLE,
                    SettingsDefaults.defaultLineNumberVisible);
            
            if (lineNumberVisibleSetting == false){
                log("Java editor set line number fails:"+org.netbeans.editor.Utilities.getKitClass(target));
            }
            
            //assert lineNumberVisibleSetting == true;
            assertTrue("Java editor - line numbers not visible", lineNumberVisibleSetting);
            
            openSourceFile(getDefaultSamplePackage(), xmlFile);
            
            EditorOperator editorXML = new EditorOperator(xmlFile);
            JTextComponentOperator textXML = new JTextComponentOperator(editorXML);
            final JTextComponent targetXML = (JTextComponent)textXML.getSource();
            
            //enable line number
            JEditorPaneOperator txtOperXML = editorXML.txtEditorPane();
            txtOperXML.pushKey(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK);
            txtOperXML.pushKey(KeyEvent.VK_S);
            
            ValueResolver resolverXML = new ValueResolver(){
                public Object getValue(){
                    return SettingsUtil.getValue(org.netbeans.editor.Utilities.getKitClass(targetXML),
                            SettingsNames.LINE_NUMBER_VISIBLE,
                            Boolean.FALSE);
                }
            };
            
            
            waitMaxMilisForValue(2000, resolverXML, Boolean.TRUE);
            
            lineNumberVisibleSetting = SettingsUtil.getBoolean(org.netbeans.editor.Utilities.getKitClass(targetXML),
                    SettingsNames.LINE_NUMBER_VISIBLE,
                    SettingsDefaults.defaultLineNumberVisible);
            
            if (lineNumberVisibleSetting == false){
                log("XML editor set line number fails:"+org.netbeans.editor.Utilities.getKitClass(targetXML));
            }
            
            // assert lineNumberVisibleSetting == true;
            assertTrue("XML editor - line numbers not visible", lineNumberVisibleSetting);
        } finally {
            // now close XML file
            try {
                //find editor
                EditorOperator editor = new EditorOperator(xmlFile);
                editor.closeDiscard();
            } catch ( TimeoutExpiredException ex) {
                log(ex.getMessage());
                log("Can't close the file:"+xmlFile);
            }
            
            //and java file
            closeFileWithDiscard();
            
            
        }
    }
    
    
}
