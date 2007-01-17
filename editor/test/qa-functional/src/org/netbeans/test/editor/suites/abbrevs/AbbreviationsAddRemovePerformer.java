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
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.editor.Abbreviations;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.test.editor.LineDiff;

/**
 * Test adding/removing of abbreviation via advanced options panel
 * @author Jan Lahoda
 * @author Max Sauer
 */
public class AbbreviationsAddRemovePerformer extends JellyTestCase {
    
    /** 'Source Packages' string from j2se project bundle */
    public static final String SRC_PACKAGES_PATH =
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "NAME_src.dir");
    
    private static final String editor = "Java Editor";
    private boolean isInFramework;
    
    /** Creates a new instance of AbbreviationsAddRemove */
    public AbbreviationsAddRemovePerformer(String name) {
        super(name);
        isInFramework = false;
    }
    
    public EditorOperator openFile() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                "editor_test");
        pn.select();
        //Open Test.java from editor_test project
        Node n = new Node(pn, SRC_PACKAGES_PATH + "|" + "abbrev" + "|" + "Test");
        n.select();
        new OpenAction().perform();
        new EventTool().waitNoEvent(500);
        
        return new EditorOperator("Test");
    }
    
    private void checkAbbreviation(String abbreviation) throws Exception {
        //Open an editor:
        System.out.println("### Checking abbreviation \"" + abbreviation + "\"");
        EditorOperator editor = openFile();
        
        //This line is reserved for testing. All previous content is destroyed
        //(and fails test!).
        editor.setCaretPosition(24, 1);
        
        //Write abbreviation:
        editor.txtEditorPane().typeText(abbreviation);
        //Expand abbreviation:
        editor.pushTabKey();
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
            
            //remove "ts"
            boolean remove0 = Abbreviations.removeAbbreviation(editor, "ts");
            System.out.println("remove0: " + remove0);
            assertTrue("Previously added \"ts\" abbrev could not be removed.", remove0);
            
            //try to remove "ts" again -- should not be possible
            boolean remove1 = Abbreviations.removeAbbreviation(editor, "ts");
            System.out.println("remove1: "+ remove1);
            assertFalse("Previously removed \"ts\" abbrev should not be aviable.", remove1);
            
            checkAbbreviation("ts");
            checkAbbreviation("tst");
            System.out.println("remove0: "+Abbreviations.removeAbbreviation(editor, "tst"));
            System.out.println("remove1: "+Abbreviations.removeAbbreviation(editor, "tst"));
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
