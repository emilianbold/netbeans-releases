/*
 * AbbreviationsTest.java
 *
 * Created on August 28, 2002, 11:15 AM
 */

package org.netbeans.test.editor.suites.abbrevs;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.modules.editor.Abbreviations;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.test.editor.LineDiff;
import org.openide.options.SystemOption;

/**
 *
 * @author  Jan Lahoda
 */
public abstract class AbbreviationsTest extends JellyTestCase {
    
    /** Creates a new instance of AbbreviationsTest */
    public AbbreviationsTest(String name) {
        super(name);
    }
    
    public static class Abbreviation {
        private String name;
        private String expansion;
        private String oldName;
        private String oldExpansion;
        
        public Abbreviation(String name, String expansion, String oldName, String oldExpansion) {
            this.name = name;
            this.expansion = expansion;
            this.oldName = oldName;
            this.oldExpansion = oldExpansion;
        }
        
        public String getName() {
            return name;
        }
        
        public String getExpansion() {
            return expansion;
        }
        
        public String getOldName() {
            return oldName;
        }
        
        public String getOldExpansion() {
            return expansion;
        }
    }
    
    public abstract Abbreviation[] getDefaultAbbreviations();
    public abstract EditorOperator getTestEditor();
    public abstract void           prepareEditor();
    public abstract void           moveCaretIntoComment();
    public abstract void           moveCaretIntoCode();
    public abstract Abbreviation[] getAbbreviationsToAdd();
    public abstract Abbreviation[] getAbbreviationsToRemove();
    public abstract Abbreviation[] getAbbreviationsToModify();
    public abstract String         getEditorName();
    public abstract void           finishEditor();
    
    protected void flushResult() {
        getRef().print(getTestEditor().getText());
        getRef().flush();
        try {
            assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
        } catch (IOException e) {
            assertTrue("IOException: " + e.getMessage(), false);
        }
    }
    
    private void useAbbreviation(String abbreviation, boolean expand) {
        EditorOperator editor = getTestEditor();
        
        /*This seemed like a good idea. It works when I run the tests manually
         *using main method of JavaAbbreviationsTestPerformer, but does not
         *when I run them automaticaly.
         */
        //        editor.clickMouse();
        
        editor.txtEditorPane().typeText(abbreviation);
        if (expand)
            editor.typeKey(' ');
        else
            editor.typeKey(' ', KeyEvent.SHIFT_MASK);
        editor.txtEditorPane().typeText("CARET_POSITION");
        editor.pushKey(KeyEvent.VK_END);
        editor.pushKey(KeyEvent.VK_ENTER);
    }
    
    public void testAbbreviationTest() {
        log("testAbbreviationTest start");
        try {
            Abbreviation[] abbs = getDefaultAbbreviations();
            
            prepareEditor();
            
            for (int cntr = 0; cntr < abbs.length; cntr++) {
                moveCaretIntoCode();
                useAbbreviation(abbs[cntr].getName(), true);
            }
            
            log("testAbbreviationTest flush results:");
            
            flushResult();
        } finally {
            
            log("testAbbreviationTest closing editor:");
            
            finishEditor();
            
            log("testAbbreviationTest finished");
        }
    }
    
    public void testAbbreviationInsideComment() {
        log("testAbbreviationInsideComment start");
        try {
            Abbreviation[] abbs = getDefaultAbbreviations();
            
            prepareEditor();
            
            for (int cntr = 0; cntr < abbs.length; cntr++) {
                moveCaretIntoComment();
                useAbbreviation(abbs[cntr].getName(), true);
            }
            
            log("testAbbreviationInsideComment flush results:");
            
            flushResult();
        } finally {
            log("testAbbreviationInsideComment closing editor:");
            
            finishEditor();
            
            log("testAbbreviationInsideComment finished");
        }
    }
    
    public void testAbbreviationWithoutExpansion() {
        log("testAbbreviationWithoutExpansion start");
        try {
            Abbreviation[] abbs = getDefaultAbbreviations();
            
            prepareEditor();
            
            for (int cntr = 0; cntr < abbs.length; cntr++) {
                moveCaretIntoCode();
                useAbbreviation(abbs[cntr].getName(), false);
            }
            
            log("testAbbreviationWithoutExpansion flush results:");
            
            flushResult();
        } finally {
            log("testAbbreviationWithoutExpansion closing editor:");
            
            finishEditor();
            
            log("testAbbreviationWithoutExpansion finished");
        }
    }
    
    public void testAbbreviationAdd() {
        log("testAbbreviationAdd start");
        Object backup = Utilities.saveAbbreviationsState();
        
        try {
            Abbreviation[] toAdd = getAbbreviationsToAdd();
            
            prepareEditor();
            
            for (int cntr = 0; cntr < toAdd.length; cntr++) {
                Abbreviations.addAbbreviation(getEditorName(), toAdd[cntr].getName(), toAdd[cntr].getExpansion());
            }
            
            for (int cntr = 0; cntr < toAdd.length; cntr++) {
                moveCaretIntoCode();
                useAbbreviation(toAdd[cntr].getName(), true);
            }
            
            log("testAbbreviationAdd flush results:");
            
            flushResult();
        } finally {
            log("testAbbreviationAdd closing editor:");
            
            finishEditor();
            
            log("testAbbreviationAdd restoring abbreviations map:");
            
            Utilities.restoreAbbreviationsState(backup);
            
            log("testAbbreviationAdd finished");
        }
    }
    
    public void testAbbreviationChange() {
        log("testAbbreviationChange start");
        Object backup = Utilities.saveAbbreviationsState();
        
        try {
            Abbreviation[] toChange = getAbbreviationsToModify();
            
            prepareEditor();
            
            for (int cntr = 0; cntr < toChange.length; cntr++) {
                assertTrue("Editing of abbreviation with original name=\"" + toChange[cntr].getOldName() + "\" failed.",
                Abbreviations.editAbbreviation(getEditorName(),
                toChange[cntr].getOldName(),
                toChange[cntr].getName(),
                toChange[cntr].getExpansion()));
            }
            
            for (int cntr = 0; cntr < toChange.length; cntr++) {
                moveCaretIntoCode();
                //Test whether old abbreviation does NOT work:
                useAbbreviation(toChange[cntr].getOldName(), true);
                //Test whether new abbreviation works:
                useAbbreviation(toChange[cntr].getName(), true);
            }
            
            log("testAbbreviationChange flush results:");
            
            flushResult();
        } finally {
            log("testAbbreviationChange closing editor:");
            
            finishEditor();
            
            log("testAbbreviationChange results abbreviations map:");
            
            Utilities.restoreAbbreviationsState(backup);
            
            log("testAbbreviationChange finished");
        }
    }
    
    public void testAbbreviationOKCancel() {
        log("testAbbreviationOKCancel start");
        Object backup = Utilities.saveAbbreviationsState();
        
        try {
            Abbreviations dialog = Abbreviations.invoke(getEditorName());
            Abbreviation[] abbrevs = getDefaultAbbreviations();
            
            for (int cntr = 0; cntr < abbrevs.length; cntr++) {
                assertTrue("Removing of abbreviation \"" + abbrevs[cntr].getName() + "\" did not succeeded. Probably bug of test.",
                dialog.removeAbbreviation(abbrevs[cntr].getName()));
            }
            
            assertTrue("After removing all known abbreviations, some of them remained. Probably bug of test.",
            dialog.listAbbreviations().size() == 0);
            
            dialog.cancel();
            
            prepareEditor();
            
            for (int cntr = 0; cntr < abbrevs.length; cntr++) {
                moveCaretIntoCode();
                //Test whether the old abbreviation does NOT work:
                useAbbreviation(abbrevs[cntr].getName(), true);
            }
            
            log("testAbbreviationOKCancel flush results:");
            
            flushResult();
        } finally {
            log("testAbbreviationOKCancel closing editor:");
            
            finishEditor();
            
            log("testAbbreviationOKCancel restoring abbreviations map:");
            
            Utilities.restoreAbbreviationsState(backup);
            
            log("testAbbreviationOKCancel finished");
        }
    }
    
    public void testAbbreviationRemove() {
        log("testAbbreviationRemove start");
        Object backup = Utilities.saveAbbreviationsState();
        
        try {
            Abbreviations dialog = null;
            Abbreviation[] abbrevs = getDefaultAbbreviations();
            
            dialog = Abbreviations.invoke(getEditorName());
            for (int cntr = 0; cntr < abbrevs.length; cntr++) {
                assertTrue("Removing of abbreviation \"" + abbrevs[cntr].getName() + "\" did not succeeded. Probably bug of test.",
                dialog.removeAbbreviation(abbrevs[cntr].getName()));
            }
            
            assertTrue("After removing all known abbreviations, some of them remained. Probably bug of test.",
            dialog.listAbbreviations().size() == 0);
            
            dialog.oK();
            
            prepareEditor();
            
            for (int cntr = 0; cntr < abbrevs.length; cntr++) {
                moveCaretIntoCode();
                //Test whether the old abbreviation does NOT work:
                useAbbreviation(abbrevs[cntr].getName(), true);
            }
            
            /*Add back all abbreviations:*/
            dialog = Abbreviations.invoke(getEditorName());
            
            for (int cntr = 0; cntr < abbrevs.length; cntr++) {
                dialog.addAbbreviation(abbrevs[cntr].getName(), abbrevs[cntr].getExpansion());
            }
            
            dialog.oK();
            
            for (int cntr = 0; cntr < abbrevs.length; cntr++) {
                moveCaretIntoCode();
                //Test whether the newly added (old) abbreviation does NOT work:
                useAbbreviation(abbrevs[cntr].getName(), true);
            }
            
            log("testAbbreviationRemove flush results:");
            
            flushResult();
        } finally {
            log("testAbbreviationRemove closing editor:");
            
            finishEditor();
            
            log("testAbbreviationRemove restoring abbreviations map:");
            
            Utilities.restoreAbbreviationsState(backup);
            
            log("testAbbreviationRemove finished");
        }
    }
    
    public void setUp() {
        log("Starting abbreviations test. Test class=" + getClass());
        log("Test name=" + getName());
    }
    
    public void tearDown() throws Exception {
        log("Finishing abbreviations test. Test class=" + getClass());
    }
}
