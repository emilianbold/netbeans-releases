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

package search_replace;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import lib.EditorTestCase;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jellytools.modules.editor.Find;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 * Test of search functionality in editor.
 *
 * @author Roman Strobl
 */
public class SearchTest extends EditorTestCase {
    EditorOperator editor;
    
    /** Creates a new instance of Main */
    public SearchTest(String testMethodName) {
        super(testMethodName);
    }
    
    /**
     * TC1 - open and close find dialog
     */
    public void testFindDialogOpenClose(){
        openDefaultProject();
        openDefaultSampleFile();
        
        editor = getDefaultSampleEditorOperator();
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        
        new FindAction().perform();
        txtOper.pushKey(KeyEvent.VK_ESCAPE);
        
        txtOper.pushKey(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
        Find find = new Find();
        find.btHelp().clickMouse();
        
        HelpOperator help = new HelpOperator();
        help.close();
        
        find.btClose().clickMouse();
        
        closeFileWithDiscard();
    }
    
}
