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
            assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"),
            new File(getWorkDir(), this.getName() + ".diff"), new LineDiff(false));
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
        try {
            Thread.currentThread().sleep(100);   //wait for abbrevition
        } catch (InterruptedException ex) {
        }
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
    
    public void testAbbreviationRemoveCancel() {
        log("testAbbreviationRemoveCancel start");
        Object backup = Utilities.saveAbbreviationsState();
        
        prepareEditor();
        
        try {
            Abbreviations dialog = Abbreviations.invoke(getEditorName());
            
            Object[] keys=dialog.listAbbreviations().keySet().toArray();
            
            for (int cntr = 0; cntr < keys.length; cntr++) {
                if (!dialog.removeAbbreviation((String)keys[cntr]))
                    log("Couldn't remove abbreviation: "+keys[cntr]);
            }
            
            if (dialog.listAbbreviations().size() > 0) {
                Object[] lst=dialog.listAbbreviations().values().toArray();
                for (int i=0;i < lst.length;i++) {
                    log("Remained abbreviation: "+lst[i]);
                }
                assertTrue("After removing all known abbreviations, some of them remained. Probably bug of test.", false);
            }
            dialog.cancel();
            
            for (int cntr = 0; cntr < keys.length; cntr++) {
                moveCaretIntoCode();
                //Test whether the old abbreviation does NOT work:
                useAbbreviation((String)keys[cntr], true);
            }
            
            log("testAbbreviationRemoveCancel flush results:");
            
            flushResult();
        } catch (Exception ex) {
            ex.printStackTrace(getLog());
        } finally {
            log("testAbbreviationRemoveCancel closing editor:");
            
            finishEditor();
            
            log("testAbbreviationRemoveCancel restoring abbreviations map:");
            
            Utilities.restoreAbbreviationsState(backup);
            
            log("testAbbreviationRemoveCancel finished");
        }
    }
    
    public void testAbbreviationRemove() {
        log("testAbbreviationRemove start");
        Object backup = Utilities.saveAbbreviationsState();
        
        prepareEditor();
        
        try {
            Abbreviations dialog = null;
            dialog = Abbreviations.invoke(getEditorName());
            
            Object[] keysold;
            Object[] keys=dialog.listAbbreviations().keySet().toArray();
            
            for (int cntr = 0; cntr < keys.length; cntr++) {
                if (!dialog.removeAbbreviation((String)keys[cntr]))
                    log("Couldn't remove abbreviation: "+keys[cntr]);
            }
            
            if (dialog.listAbbreviations().size() > 0) {
                keysold=keys;
                keys=dialog.listAbbreviations().keySet().toArray();
                for (int i=0;i < keys.length;i++) {
                    log("Remained abbreviation: "+keys[i]);
                    for (int j=0;j < keysold.length;j++) {
                        if (((String)keysold[j]).compareTo((String)keys[i]) == 0) {
                            log("is in default list.");
                            break;
                        }
                    }
                }
                //assertTrue("After removing all known abbreviations, some of them remained. Probably bug of test.", false);
            }
            dialog.oK();
            log("Abbreviations removing confirmed.");
            for (int cntr = 0; cntr < keys.length; cntr++) {
                moveCaretIntoCode();
                //Test whether the old abbreviation does NOT work:
                useAbbreviation((String)keys[cntr], true);
            }
            
            log("testAbbreviationRemove flush results:");
            
            flushResult();
        } catch (Exception ex) {
            ex.printStackTrace(getLog());
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
        System.setOut(getLog());
    }
    
    public void tearDown() throws Exception {
        log("Finishing abbreviations test. Test class=" + getClass());
    }
}
