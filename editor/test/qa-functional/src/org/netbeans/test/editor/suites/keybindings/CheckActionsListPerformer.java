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

package org.netbeans.test.editor.suites.keybindings;

import java.io.File;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.Abbreviations;
import org.netbeans.test.editor.LineDiff;

/**This is test in very development stage. I put it into the CVS mainly because
 * it simplifies testing on different platforms. This test may or may not
 * be reliable and may or may not work at all.
 *
 * @author  Jan Lahoda
 */
public class CheckActionsListPerformer extends JellyTestCase {

    public static String EDITOR_JAVA="";
    
    
    public CheckActionsListPerformer(String name) {
        super(name);
    }
        
    /**
     * @param args the command line arguments
     */
    public void doTest() throws Exception {
        log("doTest start");

        try {
            //For test testing, remove two testing abbreviations. Remove in final version.
            Abbreviations.addAbbreviation(editor, "ts", "Thread.dumpStack();");
            Abbreviations.addAbbreviation(editor, "tst", "Thread.sleep(1000);");
            
            checkAbbreviation("ts");
            checkAbbreviation("tst");
            Abbreviations.removeAbbreviation(editor, "ts");
            checkAbbreviation("ts");
            checkAbbreviation("tst");
            Abbreviations.removeAbbreviation(editor, "tst");
            checkAbbreviation("ts");
            checkAbbreviation("tst");
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
        log("Starting check Key Bindings actions test.");
	log("Test name=" + getName());
    }
    
    public void tearDown() throws Exception {
        log("Starting check Key Bindings actions test.");
        assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
    }
    
    public void testCheckActions() throws Exception {
        doTest();
    }
    
    public static void main(String[] args) throws Exception {
        new CheckActionsListPerformer("testCheckActions").doTest();
    }
    
}
