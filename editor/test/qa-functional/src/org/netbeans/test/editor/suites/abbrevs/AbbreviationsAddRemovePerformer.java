/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
    
    private static String testFile = 
    "org/netbeans/test/editor/suites/abbrevs/data/testfiles/AbbreviationsAddRemovePerformer/Test.java";
    
    private boolean isInFramework;
    
    /** Creates a new instance of AbbreviationsAddRemove */
    public AbbreviationsAddRemovePerformer(String name) {
        super(name);
        isInFramework = false;
    }
    
    public EditorOperator openFile() {
        FileObject fo = Repository.getDefault().findResource(testFile);
        
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
        
        //This line is reserved for testing. All previous content is removed
        //(and fails test!).
        editor.setCaretPosition(24, 1);
        
        //Write abbreviation:
        editor.txtEditorPane().typeText(abbreviation);
        //Expand abbreviation:
        editor.txtEditorPane().typeKey(' ');
        //Flush current on output (ref output!)
        ref(editor.getText(editor.getLineNumber()));
        //Delete what we have written:
        editor.select(editor.getLineNumber());
        editor.pushKey(KeyEvent.VK_DELETE, 0);
        editor.pushKey(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
        editor.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public void doTest() throws Exception {
        Object backup = Utilities.saveAbbreviationsState();
        
        Abbreviations abbs = Abbreviations.invoke("Java Editor");
        
        //For test testing, remove two testing abbreviations. Remove in final version.
        abbs.addAbbreviation("ts", "Thread.dumpStack();");
        abbs.addAbbreviation("tst", "Thread.sleep(1000);");

        checkAbbreviation("ts");
        checkAbbreviation("tst");
        abbs.removeAbbreviation("ts");
        checkAbbreviation("ts");
        checkAbbreviation("tst");
        abbs.removeAbbreviation("tst");
        checkAbbreviation("ts");
        checkAbbreviation("tst");
        
        Utilities.restoreAbbreviationsState(backup);
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
    }
    
    public void tearDown() throws Exception {
        log("Finishing abbreviations add/remove test.");
        assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
        isInFramework = false;
    }
    
    public void testAddRemove() throws Exception {
        doTest();
    }
    
    public static void main(String[] args) throws Exception {
        new AbbreviationsAddRemovePerformer("testAddRemove").doTest();
    }
    
}
