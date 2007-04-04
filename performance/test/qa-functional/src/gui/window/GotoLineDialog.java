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

import gui.Utilities;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Go To Line dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class GotoLineDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private static EditorOperator editor;
    private String TITLE;
    
    /** Creates a new instance of GotoLineDialog */
    public GotoLineDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of GotoLineDialog */
    public GotoLineDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.editor.Bundle", "goto-title");
        // open a java file in the editor
        editor = Utilities.openFile("jEdit","bsh","Parser.java", true);
    }
    
    public void prepare() {
        // do nothing
        Utilities.workarroundMainMenuRolledUp();
   }
    
    public ComponentOperator open(){
        // press CTRL+G
        new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK)).performShortcut(editor);
        return new NbDialogOperator(TITLE); // NOI18N
    }
    
    public void shutdown(){
        if(editor!=null && editor.isShowing())
            editor.closeDiscard();
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        repeat = 3;
        junit.textui.TestRunner.run(new GotoLineDialog("measureTime"));
    }
    
}
