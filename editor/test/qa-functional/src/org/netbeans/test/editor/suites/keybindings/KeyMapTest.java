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

package org.netbeans.test.editor.suites.keybindings;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.ListModel;
import javax.swing.tree.TreePath;
import junit.textui.TestRunner;
import lib.EditorTestCase.ValueResolver;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.editor.AddShortcutDialog;
import org.netbeans.jellytools.modules.editor.CreateNewProfileDialog;
import org.netbeans.jellytools.modules.editor.KeyMapOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;


/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class KeyMapTest extends JellyTestCase{
    
    private static final String testFile = "Test"; // NOI18N
    
    public static final String SRC_PACKAGES_PATH =
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "NAME_src.dir");
    
    private static final String testPackage = "keymap";  // NOI18N
    
    private static final String NEW_PROFILE = "DupProfile2";
    
    private static String[] exceptions = {};
    
    private static String[] exceptionsNetBeans = {"Go To|Go to Previous Document"};
    
    private static String[] exceptionsNetBeans55 = {"Go To|Go to Previous Document"};
    
    private static String[] exceptionsEmacs = {};
    
    private static String[] exceptionsEclipse = {};
    
    private EditorOperator editor;
    /** Creates a new instance of KeyMapTest
     * @param name Test name
     */
    public KeyMapTest(String name) {
        super(name);
    }
    
    private EditorOperator openFile() {
        Node pn = new ProjectsTabOperator().getProjectRootNode("editor_test");
        pn.select();
        //Open Test.java from editor_test project
        Node n = new Node(pn, SRC_PACKAGES_PATH + "|" + testPackage + "|" + testFile);
        n.select();
        new OpenAction().perform();
        new EventTool().waitNoEvent(500);
        return new EditorOperator("Test");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        editor=openFile();
        System.out.println("Starting: "+getName());
    }
    
    private String getActionName(String globalAction) {
        int startPos = globalAction.indexOf('[');
        int endPos = globalAction.indexOf(':');
        if(startPos <0 || endPos<0) return globalAction;
        return globalAction.substring(startPos+1, endPos);
    }
    
    private String getCompountActionName(String globalAction) {
        int eqPos = globalAction.indexOf('=');
        if(eqPos<0) return globalAction;
        int startPos = globalAction.indexOf('[',eqPos);
        int endPos = globalAction.indexOf(':');
        if(startPos <0 || endPos<0) return globalAction;
        return globalAction.substring(startPos+1, endPos);
    }
    
    private String lastVisitedCategory = "n/a";
            
    private boolean dumpException(String path) {
        for (int i = 0; i < exceptions.length; i++) {
            String string = exceptions[i];
            if(path.equalsIgnoreCase(string)) return true;
        }
        return false;
        
    }
        
    
    private void dump(String path, JTreeOperator tree,KeyMapOperator kmo) {
        Map<String,String> usedShortcuts = new HashMap<String, String>(); //used shortcuts <shortcut, action>
        List<String> duplicates = new LinkedList<String>();    // list of dupes        
        
        int rows = tree.getRowCount();
        for (int i = rows-1; i >= 0; i--) {
            tree.expandRow(i);
        }
        rows = tree.getRowCount();
        for (int i = 0; i < rows; i++) { //TODO
            tree.selectRow(i);
            TreePath tp = tree.getSelectionPath();
            int parts = tp.getPathCount();
            if(parts == 2) {
                lastVisitedCategory = (String) tp.getPathComponent(1);
            } else {
                Object o = tp.getPathComponent(2);
                System.out.println(o);
                System.out.println(o.getClass().getName());
                String name = "<unknown>";
                if(o.toString().startsWith("GlobalAction") || o.toString().startsWith("EditorAction")) {
                    name = getActionName(o.toString());
                }
                if(o.toString().startsWith("CompountAction")) {
                    name = getCompountActionName(o.toString());
                }
                ListModel model = kmo.shortcuts().getModel();                
                if (model.getSize() > 0 && !dumpException(lastVisitedCategory + "|" + name)) {
                    getRef().println(lastVisitedCategory + "|" + name); //log acition name
                    for (int j = 0; j < model.getSize(); j++) {
                        String shortcut = model.getElementAt(j).toString();
                        getRef().println("  " + shortcut); //log shortcut
                        if (usedShortcuts.get(shortcut) != null) {
                            duplicates.add(shortcut + " " + usedShortcuts.get(shortcut));
                            duplicates.add(shortcut + " " + lastVisitedCategory + "|" + name);
                        } else {
                            usedShortcuts.put(shortcut, lastVisitedCategory + "|" + name);
                        }
                    }
                }
            }            
        }
        if(!duplicates.isEmpty()) {            
            for (String dup : duplicates) {
                getLog().println(dup);
            }
            //fail("Keymap contains multiple action for the same shortcut"); //do not fail some dupes are as designed
        }
    }
    
    public void testAllKeyMapNetbeans() throws IOException {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("NetBeans");
            JTreeOperator tree =kmo.actions();
            exceptions = exceptionsNetBeans;
            dump("",tree,kmo);
            kmo.ok().push();
            closed = true;
            assertFile(new File(getWorkDir(),getName()+".ref"), getGoldenFile(), new File(getWorkDir(),getName()+".diff"));
        } finally {
            if(!closed) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    public void testAllKeyMapNetbeans55() throws IOException {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("NetBeans55");
            JTreeOperator tree =kmo.actions();
            exceptions = exceptionsNetBeans55;
            dump("",tree,kmo);
            kmo.ok().push();
            closed = true;
            assertFile(new File(getWorkDir(),getName()+".ref"), getGoldenFile(), new File(getWorkDir(),getName()+".diff"));
        } finally {
            if(!closed) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    
    public void testAllKeyMapEmacs() throws IOException {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("Emacs");
            JTreeOperator tree =kmo.actions();
            exceptions = exceptionsEmacs;
            dump("",tree,kmo);
            kmo.ok().push();
            closed = true;
            assertFile(new File(getWorkDir(),getName()+".ref"), getGoldenFile(), new File(getWorkDir(),getName()+".diff"));
        } finally {
            if(!closed) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    public void testAllKeyMapEclipse() throws IOException {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("Eclipse");
            JTreeOperator tree =kmo.actions();
            exceptions = exceptionsEclipse;
            dump("",tree,kmo);
            kmo.ok().push();
            closed = true;
            assertFile(new File(getWorkDir(),getName()+".ref"), getGoldenFile(), new File(getWorkDir(),getName()+".diff"));
        } finally {
            if(!closed) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    public void testAddShortcut() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("NetBeans");
            kmo.selectAction("Other|select-line");
            // there should be no shortcuts for this action
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.add().push();
            AddShortcutDialog asd = new AddShortcutDialog();
            asd.txtJTextField().pushKey(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK);
            asd.btOK().push();
            checkListContents(kmo.shortcuts(), "Ctrl+B");
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(2000);
            editor.requestFocus();
            new EventTool().waitNoEvent(100);
            editor.setCaretPosition(7, 1);
            ValueResolver vr = new ValueResolver() {
                public Object getValue() {                    
                    editor.pushKey(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if(selected==null) return false;
                    return selected.startsWith("        System.out.println(\"Hello\");");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            String text =  editor.txtEditorPane().getSelectedText();
            assertEquals("        System.out.println(\"Hello\");",text);
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
        
    }
    
    public void testRemoveShortcut() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("NetBeans");
            kmo.selectAction("Other|selection-end-line");
            checkListContents(kmo.shortcuts(), "Shift+END");
            kmo.shortcuts().selectItem(0);
            kmo.remove().push();
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            editor.setCaretPosition(7, 1);
            editor.pushKey(KeyEvent.VK_END, InputEvent.SHIFT_DOWN_MASK);
            new EventTool().waitNoEvent(100);
            String text =  editor.txtEditorPane().getSelectedText();
            assertEquals(null,text);
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    public void testAddDuplicate() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("NetBeans");
            kmo.selectAction("Other|selection-end-word");
            kmo.add().push();
            AddShortcutDialog asd = new AddShortcutDialog();
            asd.txtJTextField().pushKey(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK);
            assertEquals("Shortcut already assigned to Extend Selection Up Action.",asd.lblConflict().getText());
            asd.btOK().push();
            checkListContents(kmo.shortcuts(), "Shift+UP");
            kmo.selectAction("Other|selection-up");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            ValueResolver vr = new ValueResolver() {
                public Object getValue() {
                    editor.setCaretPosition(7, 9);
                    editor.pushKey(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK);
                    new EventTool().waitNoEvent(200);
                    String text =  editor.txtEditorPane().getSelectedText();
                    return text!=null;
                }
            };
            waitMaxMilisForValue(4000, vr, Boolean.TRUE);
            String text =  editor.txtEditorPane().getSelectedText();
            assertEquals("System",text);
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
        
    }
    
    public void testCancelAdding() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("NetBeans");
            kmo.selectAction("Other|remove-selection");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.add().push();
            AddShortcutDialog asd = new AddShortcutDialog();
            asd.txtJTextField().pushKey(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK);
            asd.btCancel().push();
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            editor.setCaretPosition(7, 9);
            new EventTool().waitNoEvent(100);
            editor.txtEditorPane().setSelectionStart(1);
            editor.txtEditorPane().setSelectionEnd(8);
            editor.pushKey(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK);
            String text =  editor.txtEditorPane().getSelectedText();
            assertEquals("package",text);
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
        
    }
    
    public void testCancelOptions() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("NetBeans");
            kmo.selectAction("Other|selection-line-first-column");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.add().push();
            AddShortcutDialog asd = new AddShortcutDialog();
            asd.txtJTextField().pushKey(KeyEvent.VK_Z, InputEvent.ALT_DOWN_MASK);
            asd.btOK().push();
            checkListContents(kmo.shortcuts(), "Alt+Z");
            kmo.cancel().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            editor.setCaretPosition(7, 9);
            editor.pushKey(KeyEvent.VK_Z, InputEvent.ALT_DOWN_MASK);
            new EventTool().waitNoEvent(100);
            String text =  editor.txtEditorPane().getSelectedText();
            assertEquals(null,text);
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    public void testAddShortCutDialog() {
        KeyMapOperator kmo = null;
        boolean closedKMO = true;
        AddShortcutDialog asd = null;
        boolean closedASD = true;
        try {
            kmo = KeyMapOperator.invoke();
            closedKMO = false;
            kmo.selectProfile("NetBeans");
            kmo.selectAction("Other|selection-line-first-column");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.add().push();
            closedASD = false;
            asd = new AddShortcutDialog();
            asd.txtJTextField().pushKey(KeyEvent.VK_Z, InputEvent.ALT_DOWN_MASK);   // Alt+Z
            assertEquals("Alt+Z",asd.txtJTextField().getText());
            
            asd.btClear().push(); // clear
            assertEquals("",asd.txtJTextField().getText());
            
            asd.txtJTextField().requestFocus();
            asd.txtJTextField().pushKey(KeyEvent.VK_X,InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
            assertEquals("Alt+Shift+X",asd.txtJTextField().getText());
            
            asd.btTab().push();
            assertEquals("Alt+Shift+X TAB",asd.txtJTextField().getText());
            
            asd.txtJTextField().requestFocus();
            asd.txtJTextField().pushKey(KeyEvent.VK_ESCAPE);
            assertEquals("Alt+Shift+X TAB ESCAPE",asd.txtJTextField().getText());
            
            asd.btClear().push(); // clear
            asd.txtJTextField().requestFocus();
            asd.txtJTextField().pushKey(KeyEvent.VK_BACK_SPACE);
            assertEquals("BACK_SPACE",asd.txtJTextField().getText());
            
            asd.txtJTextField().pushKey(KeyEvent.VK_BACK_SPACE);
            assertEquals("",asd.txtJTextField().getText());
            
            asd.btCancel().push();
            closedASD = true;
            kmo.cancel().push();
            closedKMO = true;
        } finally {
            if(!closedASD && asd!=null) asd.btCancel().push();
            if(!closedKMO && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
        
    }
    
    public void testHelp() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.help().push();
            final HelpOperator help = new HelpOperator();
            ValueResolver vr = new ValueResolver() {
                public Object getValue() {
                    return help.getContentText().contains("Options Window: Keymap");
                }
            };
            waitMaxMilisForValue(5000, vr, Boolean.TRUE);
            boolean ok = help.getContentText().contains("Options Window: Keymap");
            if(!ok) log(help.getContentText());
            assertTrue("Wrong help page opened",ok);
            help.close();
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
        
        
    }
    
    public void testProfileSwitch() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile("Eclipse");
            kmo.selectAction("Other|remove-line");
            checkListContents(kmo.shortcuts(), "Ctrl+D");
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            editor.setCaretPosition(7, 1);
            editor.pushKey(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
            new EventTool().waitNoEvent(500);
            String text =  editor.getText();
            assertFalse("Line not removed",text.contains("\"Hello\""));
            kmo = KeyMapOperator.invoke();
            closed = false;
            assertEquals(kmo.profile().getSelectedItem(),"Eclipse");
            kmo.ok().push();
            closed = true;
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    public void testProfileDouble() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            //  duplicating Netbeans profile
            kmo.selectProfile("NetBeans");
            int originalSize = kmo.profile().getModel().getSize();
            kmo.duplicate().push();
            CreateNewProfileDialog cnpd = new CreateNewProfileDialog();
            cnpd.txtProfileName().clearText();
            cnpd.txtProfileName().typeText(NEW_PROFILE);
            cnpd.btOK().push();
            new EventTool().waitNoEvent(100);
            assertEquals(kmo.profile().getSelectedItem(),NEW_PROFILE);
            //  adding shortcut to new profile
            kmo.selectAction("Other|toggle-case-identifier-begin");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.add().push();
            AddShortcutDialog asd = new AddShortcutDialog();
            asd.txtJTextField().pushKey(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK);
            asd.btOK().push();
            checkListContents(kmo.shortcuts(), "Alt+C");
            kmo.ok().push();
            new EventTool().waitNoEvent(2500);
            closed = true;
            // testing new shortcut
            editor.setCaretPosition(7, 12);
            editor.pushKey(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK);
            ValueResolver res = new ValueResolver() {
                public Object getValue() {
                    editor.setCaretPosition(7, 12);
                    editor.pushKey(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK);
                    return editor.getText().contains("system.out.println");
                }
            };
            waitMaxMilisForValue(5000, res, Boolean.TRUE);
            assertTrue("Action not performed",editor.getText().contains("system.out.println"));
            kmo = KeyMapOperator.invoke();
            closed = false;
            // switching back to NetbeansProfile
            assertEquals(kmo.profile().getSelectedItem(),NEW_PROFILE);
            kmo.selectProfile("NetBeans");
            kmo.selectAction("Other|toggle-case-identifier-begin");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.ok().push();
            new EventTool().waitNoEvent(500);
            closed = true;
            //verifying that shortcut in not propagated to Netbeans profile
            editor.setCaretPosition(7, 12);
            editor.pushKey(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK);
            waitMaxMilisForValue(5000, res, Boolean.FALSE);
            assertTrue("Action performed",editor.getText().contains("system.out.println"));
            //deleting profile
            kmo = KeyMapOperator.invoke();
            closed = false;
            assertEquals("NetBeans",kmo.profile().getSelectedItem());
            kmo.selectProfile(NEW_PROFILE);
            kmo.delete().push();
            assertEquals("Wrong number of profiles",originalSize,kmo.profile().getModel().getSize());
            kmo.ok().push();
            new EventTool().waitNoEvent(500);
            closed = true;
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    public void testProfileRestore() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            // add shortcut
            kmo.selectProfile("NetBeans");
            kmo.selectAction("Other|selection-last-non-white");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.add().push();
            new EventTool().waitNoEvent(100);
            AddShortcutDialog asd = new AddShortcutDialog();
            asd.txtJTextField().pushKey(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK);
            asd.btOK().push();
            checkListContents(kmo.shortcuts(), "Alt+Q");
            // remove shortcut
            kmo.selectAction("Other|caret-begin-line");
            checkListContents(kmo.shortcuts(), "HOME");
            kmo.shortcuts().setSelectedIndex(0);
            kmo.remove().push();
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            // test in editor
            editor.setCaretPosition(7, 12);
            ValueResolver vr = new ValueResolver() {
                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK);
                    String text =  editor.txtEditorPane().getSelectedText();
                    return "tem.out.println(\"Hello\")".equals(text);
                }
            };
            waitMaxMilisForValue(5000, vr, Boolean.TRUE);
            String text =  editor.txtEditorPane().getSelectedText();
            assertEquals("tem.out.println(\"Hello\")",text);
            int caretPositionOriginal = editor.txtEditorPane().getCaretPosition();
            editor.pushKey(KeyEvent.VK_HOME);
            int caretPosition = editor.txtEditorPane().getCaretPosition();
            assertEquals("Caret was moved", caretPositionOriginal,caretPosition);
            System.out.println("Caret position:"+caretPosition);
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.restore().push();
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(2000);
            kmo = KeyMapOperator.invoke();
            closed = false;
            new EventTool().waitNoEvent(100);
            kmo.selectAction("Other|selection-last-non-white");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.selectAction("Other|caret-begin-line");
            checkListContents(kmo.shortcuts(), "HOME");
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            editor.setCaretPosition(7, 12);
            caretPositionOriginal = editor.txtEditorPane().getCaretPosition();
            editor.txtEditorPane().setSelectionStart(-1);
            editor.txtEditorPane().setSelectionEnd(-1);
            editor.pushKey(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK);
            new EventTool().waitNoEvent(100);
            text =  editor.txtEditorPane().getSelectedText();
            assertEquals(null,text);
            editor.pushKey(KeyEvent.VK_HOME);
            caretPosition = editor.txtEditorPane().getCaretPosition();
            assertEquals("Caret was not moved", caretPositionOriginal,caretPosition);
            System.out.println("Caret position:"+caretPosition);
        } finally {
            if(!closed && kmo!=null) kmo.cancel().push();
            editor.close(false);
        }
    }
    
    protected boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue){
        int time = maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null){
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)){
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
        }
        return false;
    }
    
    
    private void checkListContents(JListOperator oper,Object ... items) {
        ListModel model = oper.getModel();
        assertEquals("List does not contains expected number of items", items.length, model.getSize());
        for (int i = 0; i < items.length; i++) {
            Object object = items[i];
            assertEquals(items[i],model.getElementAt(i));
        }
    }
    
    public static void main(String[] args) {               
        TestRunner.run(new NbTestSuite(KeyMapTest.class));
    }
    
}
