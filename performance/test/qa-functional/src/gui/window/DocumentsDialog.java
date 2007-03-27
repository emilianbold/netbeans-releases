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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.DocumentsDialogOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.DocumentsAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Documents dialog
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class DocumentsDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    /** Creates a new instance of DocumentsDialog */
    public DocumentsDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of DocumentsDialog */
    public DocumentsDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize(){
        gui.Utilities.openFiles("jEdit", getTenSelectedFiles());
        waitNoEvent(20000);
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
   }
    
    public ComponentOperator open() {
        // invoke Window / Documents from the main menu
        new DocumentsAction().performMenu();
        return new DocumentsDialogOperator();
    }

    public void shutdown(){
        EditorWindowOperator.closeDiscard();
    }
    
    private static String[][] getTenSelectedFiles(){
        String[][] files_path = {
            {"bsh","Interpreter.java"},
            {"bsh","JThis.java"},
            {"bsh","Name.java"},
            {"bsh","Parser.java"},
            {"bsh","Primitive.java"},
            {"com.microstar.xml","XmlParser.java"},
            {"org.gjt.sp.jedit","BeanShell.java"},
            {"org.gjt.sp.jedit","Buffer.java"},
            {"org.gjt.sp.jedit","EditPane.java"},
            {"org.gjt.sp.jedit","EditPlugin.java"},
            {"org.gjt.sp.jedit","EditServer.java"}
        };
        return files_path;
    }
    
}
