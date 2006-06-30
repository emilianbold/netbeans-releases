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

package org.netbeans.test.editor.suites.abbrevs;

import java.awt.event.KeyEvent;
import java.io.File;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.Abbreviations;
import org.netbeans.test.editor.LineDiff;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**This is test in very development stage. I put it into the CVS mainly because
 * it simplifies testing on different platforms. This test may or may not
 * be reliable and may or may not work at all.
 *
 * @author  Jan Lahoda
 */
public class AbbreviationsAddRemovePerformer extends JellyTestCase {
    
    private static final String editor = "Java Editor";
    private boolean isInFramework;
    
    /** Creates a new instance of AbbreviationsAddRemove */
    public AbbreviationsAddRemovePerformer(String name) {
        super(name);
        isInFramework = false;
    }
    
    public EditorOperator openFile() {
        FileObject fo = Repository.getDefault().findResource("org/netbeans/test/editor/suites/abbrevs/data/testfiles/AbbreviationsAddRemovePerformer/Test.java");
        
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
            
            ec.open();
            
            return new EditorOperator("Test");
        } catch (DataObjectNotFoundException e) {
            assertTrue(false);
            return null;
        }
        
/*        assertTrue(false);
        return null;*/
    }
    
    private void checkAbbreviation(String abbreviation) throws Exception {
        //Open an editor:
        EditorOperator editor = openFile();
        
        //This line is reserved for testing. All previous content is destroyed
        //(and fails test!).
        editor.setCaretPosition(24, 1);
        
        //Write abbreviation:
        editor.txtEditorPane().typeText(abbreviation);
        //Expand abbreviation:
        editor.txtEditorPane().typeKey(' ');
        //Flush current line to output (ref output!)
        ref(editor.getText(editor.getLineNumber()));
        //Delete what we have written:
        editor.select(editor.getLineNumber());
        editor.pushKey(KeyEvent.VK_DELETE, 0);
        editor.pushKey(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
        editor.closeDiscard();
    }
    
    /**
     * @param args the command line arguments
     */
    public void doTest() throws Exception {
        log("doTest start");
        Object backup = Utilities.saveAbbreviationsState();
        
        try {
            //For test testing, remove two testing abbreviations. Remove in final version.
            Abbreviations.addAbbreviation(editor, "ts", "Thread.dumpStack();");
            Abbreviations.addAbbreviation(editor, "tst", "Thread.sleep(1000);");
            
            checkAbbreviation("ts");
            checkAbbreviation("tst");
            System.out.println("remove1: "+Abbreviations.removeAbbreviation(editor, "ts"));
            System.out.println("remove2: "+Abbreviations.removeAbbreviation(editor, "ts"));
            checkAbbreviation("ts");
            checkAbbreviation("tst");
            System.out.println("remove1: "+Abbreviations.removeAbbreviation(editor, "tst"));
            System.out.println("remove2: "+Abbreviations.removeAbbreviation(editor, "tst"));
            checkAbbreviation("ts");
            checkAbbreviation("tst");
            
        } catch (Exception ex) {
            log("Bug in test: "+ex.getMessage()+" by "+ex.getClass());
            ex.printStackTrace(getLog());
        } finally {
            Utilities.restoreAbbreviationsState(backup);
            log("doTest finished");
        }
    }
    
/*    public void ref(String ref) {
        if (isInFramework) {
            getRef().println(ref);
            getRef().flush();
        } else {
            System.out.println("TEST_OUTPUT:" + ref);
        }
    }
 
    public void log(String log) {
        if (isInFramework) {
            getLog().println(log);
            getLog().flush();
        } else {
            System.err.println(log);
        }
    }*/
    
    public void setUp() {
        isInFramework = true;
        log("Starting abbreviations add/remove test.");
        log("Test name=" + getName());
        try {
            EditorOperator.closeDiscardAll();
            log("Closed Welcome screen.");
        } catch (Exception ex) {
        }
    }
    
    public void tearDown() throws Exception {
        log("Finishing abbreviations add/remove test.");
        assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"),
        new File(getWorkDir(), this.getName() + ".diff"), new LineDiff(false));
        isInFramework = false;
    }
    
    public void testAddRemove() throws Exception {
        doTest();
    }
    
    public static void main(String[] args) throws Exception {
        new AbbreviationsAddRemovePerformer("testAddRemove").doTest();
    }
    
}
