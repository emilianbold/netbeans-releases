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

import org.netbeans.test.oo.gui.jelly.Explorer;
import org.netbeans.test.oo.gui.jelly.Editor;
import java.awt.event.KeyEvent;
import org.netbeans.test.oo.gui.jelly.JellyProperties;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileObject;
import org.netbeans.junit.NbTestCase;
import java.io.File;
import org.netbeans.test.editor.LineDiff;
import org.netbeans.test.editor.suites.componenets.Utilities;

/**This is test in very development stage. I put it into the CVS mainly because
 * it simplifies testing on different platforms. This test may or may not
 * be reliable and may or may not work at all.
 *
 * @author  Jan Lahoda
 */
public class AbbreviationsAddRemovePerformer extends NbTestCase {
    
    private static String testFile = 
    "org/netbeans/test/editor/suites/abbrevs/data/testfiles/AbbreviationsAddRemovePerformer/Test.java";
    
    private static String testFileDelim = 
    "org" + Explorer.delim + "netbeans" + Explorer.delim + "test" + Explorer.delim + "editor" + Explorer.delim + "suites" + Explorer.delim + "abbrevs" + Explorer.delim + "data" + Explorer.delim + "testfiles" + Explorer.delim + "AbbreviationsAddRemovePerformer" + Explorer.delim + "Test";
    
    private boolean isInFramework;
    
    /** Creates a new instance of AbbreviationsAddRemove */
    public AbbreviationsAddRemovePerformer(String name) {
        super(name);
        isInFramework = false;
    }
    
    private void checkAbbreviation(String abbreviation) throws Exception {
        //Open an editor:
        FileObject resource = Repository.getDefault().findResource(testFile);
        
        String fileSystemDisplayName = resource.getFileSystem().getDisplayName();
        
        Editor.openFile(fileSystemDisplayName + Explorer.delim + testFileDelim);

        Thread.sleep(1000); //Sometimes the menu was not closed.
        
        Editor editor = Editor.find("Test");
        
        editor.activateWindow();
        
        //This line is reserved for testing. All previous content is removed
        //(and fails test!).
        editor.goToLine(24);
        
        //Write abbreviation:
        editor.getJEditorPaneOperator().typeText(abbreviation);
        //Expand abbreviation:
        editor.getJEditorPaneOperator().typeKey(' ');
        //Select written line:
        editor.pushHomeKey();
        editor.pushKey(KeyEvent.VK_END, KeyEvent.SHIFT_MASK);
        //Flush it on output (ref output!)
        ref(editor.getSelectedText());
        //Delete what we have written:
        editor.pushKey(KeyEvent.VK_DELETE, 0);
        editor.pushKey(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
        editor.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public void doTest() throws Exception {
        JellyProperties.setDefaults();
//        JellyProperties.setJemmyOutput(null, null, null);
        JellyProperties.setJemmyDebugTimeouts();
        
        //For test testing, remove two testing abbreviations. Remove in final version.
        Utilities.removeAbbreviation("Java", new String[] {"ts", "tst"});
        Utilities.addAbbreviation("Java",
                new String[] {
            "ts", "tst"},
                new String [] {
            "Thread.dumpStack();",
            "Thread.sleep(1000);"});
        checkAbbreviation("ts");
        checkAbbreviation("tst");
        Utilities.removeAbbreviation("Java", new String[] {"ts"});
        checkAbbreviation("ts");
        checkAbbreviation("tst");
        Utilities.removeAbbreviation("Java", new String[] {"tst"});
        checkAbbreviation("ts");
        checkAbbreviation("tst");
    }
    
    public void ref(String ref) {
        if (isInFramework)
            super.ref(ref);
        else
            System.out.println("TEST_OUTPUT:" + ref);
    }

    public void log(String log) {
        if (isInFramework)
            super.log(log);
        else
            System.err.println(log);
    }

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
