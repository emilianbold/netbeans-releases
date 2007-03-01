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
import javax.swing.ListModel;
import junit.textui.TestRunner;
import lib.EditorTestCase;
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
    
    public String getHumanReadablePath(String path) {
        int pos = path.lastIndexOf("|");
        String prefix = path.substring(0, pos+1);
        String action = path.substring(pos+1);
        int pos2 = action.lastIndexOf(":");
        if(pos2<0) action = "<unknown action>";
        else action = action.substring(pos2+1);
        return prefix+action;
    }
    
    private void dump(String path, JTreeOperator tree,KeyMapOperator kmo) {
        Node n = new Node(tree, path);
        if(path.length()!=0) n.select();
        String[] s = n.getChildren();
        if(s.length==0) {
            JListOperator shortcuts = kmo.shortcuts();
            ListModel model = shortcuts.getModel();
            if(model.getSize()>0) {
                getRef().println(getHumanReadablePath(path));
            }
            for (int i = 0; i < model.getSize(); i++) {
                getRef().println("  "+model.getElementAt(i));
            }
        } else {
            for (int i = 0; i < s.length; i++) {
                String item = s[i];
                if(item.equals("Other")) continue;  //performance
                if(path.length()!=0) dump(path+"|"+item,tree,kmo);
                else dump(item,tree,kmo);
            }
            n.collapse();
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
            asd.txtJTextField().pushKey(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK);            
            asd.btOK().push();
            checkListContents(kmo.shortcuts(), "Alt+L");
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            editor.setCaretPosition(7, 1);
            editor.pushKey(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK);            
            new EventTool().waitNoEvent(100);
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
            editor.setCaretPosition(7, 9);
            editor.pushKey(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK);
            new EventTool().waitNoEvent(100);
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
            HelpOperator help = new HelpOperator();
            
            boolean ok = help.getContentText().contains("Options Window: Keymap");
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
            new EventTool().waitNoEvent(100);
            String text =  editor.getText();
            assertEquals("\n" +
                    "package keymap;\n" +
                    "\n" +
                    "public class Test {\n" +
                    "    \n" +
                    "    public Test() {\n" +
                    "    }\n" +
                    "    \n" +
                    "}\n",text);
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
            new EventTool().waitNoEvent(1000);
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
            waitMaxMilisForValue(10000, res, Boolean.TRUE);
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
            //test in editor                                    
            editor.setCaretPosition(7, 12);
            editor.pushKey(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK);
            new EventTool().waitNoEvent(100);
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
            kmo.selectAction("Other|selection-last-non-white");
            checkListContents(kmo.shortcuts(), new Object[]{});
            kmo.selectAction("Other|caret-begin-line");
            checkListContents(kmo.shortcuts(), "HOME");
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(500);
            editor.setCaretPosition(7, 9);
            caretPositionOriginal = editor.txtEditorPane().getCaretPosition();
            editor.txtEditorPane().setSelectionStart(-1);
            editor.txtEditorPane().setSelectionEnd(-1);
            editor.setCaretPosition(7, 12);
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
        int time = (int) maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null){
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)){
                return true;
            }
            try {
                Thread.currentThread().sleep(100);
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
