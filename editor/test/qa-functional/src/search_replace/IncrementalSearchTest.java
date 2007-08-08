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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package search_replace;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import javax.swing.JToolBar;
import junit.textui.TestRunner;
import lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author Jiri Prox
 */
public class IncrementalSearchTest extends EditorTestCase{
    
    public IncrementalSearchTest(String name) {
        super(name);
    }
    
    JToolBar searchBar;
    
    public boolean getSearchBar(Container comp) {
        if(comp.getClass().getName().equals("org.netbeans.modules.editor.impl.SearchBar")) {
            searchBar = (JToolBar) comp;
            return true;
        }
        Component[] coms = comp.getComponents();
        for (Component component : coms) {
            if(Container.class.isAssignableFrom(component.getClass())) {
                if(getSearchBar((Container)component)) return true;
            }
        }
        return false;
    }
    
//    public void testSearchForward() {
//        openDefaultProject();
//        openDefaultSampleFile();
//        EditorOperator editor = getDefaultSampleEditorOperator();
//        editor.setCaretPosition(4, 1);
//        openSearchBar(editor);        
//        ContainerOperator c = new ContainerOperator(searchBar);
//        JTextFieldOperator t = new JTextFieldOperator(c);
//        t.clearText();
//        new EventTool().waitNoEvent(100);
//        t.typeText("p");
//        t.pushKey(KeyEvent.VK_ENTER);
//        assertSelection(editor, 97, 98);
//        t.typeText("u");
//        t.pushKey(KeyEvent.VK_ENTER);
//        assertSelection(editor, 113, 115);
//        t.typeText("b");
//        t.pushKey(KeyEvent.VK_ENTER);
//        assertSelection(editor, 126, 129);
//        t.pushKey(KeyEvent.VK_ENTER);
//        assertSelection(editor, 170, 173);
//        JLabelOperator status = new JLabelOperator(editor,3);
//        t.pushKey(KeyEvent.VK_ENTER);
//        assertSelection(editor, 47, 50);
//        assertEquals("'pub' found at 3:1; End of the document reached. Continuing search from beginning.",status.getText());
//        t.pushKey(KeyEvent.VK_ESCAPE);
//        new EventTool().waitNoEvent(100);
//        assertFalse("ToolBar not closed",searchBar.isVisible());
//        closeFileWithDiscard();
//    }
//    
//    public void testSearchBackwards() {
//        openDefaultProject();
//        openFile("Source Packages|search_replace.IncrementalSearchTest", "testSearchForward");
//        EditorOperator editor = new EditorOperator("testSearchForward");
//        editor.setCaretPosition(11, 1);
//        openSearchBar(editor);        
//        ContainerOperator c = new ContainerOperator(searchBar);
//        JTextFieldOperator t = new JTextFieldOperator(c);
//        t.clearText();
//        new EventTool().waitNoEvent(100);
//        t.typeText("pub");
//        t.pushKey(KeyEvent.VK_ENTER,KeyEvent.SHIFT_DOWN_MASK);
//        assertSelection(editor, 126, 129);
//        t.pushKey(KeyEvent.VK_ENTER,KeyEvent.SHIFT_DOWN_MASK);
//        assertSelection(editor, 47, 50);
//        t.pushKey(KeyEvent.VK_ENTER,KeyEvent.SHIFT_DOWN_MASK);
//        assertSelection(editor, 170, 173);
//        JLabelOperator status = new JLabelOperator(editor,3);        
//        assertEquals("'pub' found at 12:5; Beginning of the document reached. Continuing search from end.",status.getText());
//        t.pushKey(KeyEvent.VK_ESCAPE);
//        new EventTool().waitNoEvent(100);
//        assertFalse("ToolBar not closed",searchBar.isVisible());
//        editor.closeDiscard();
//    }
//    
//    public void testMatchCase() {
//        openDefaultProject();
//        openFile("Source Packages|search_replace.IncrementalSearchTest", "match.txt");
//        EditorOperator editor = new EditorOperator("match.txt");
//        editor.setCaretPosition(1, 1);
//        new EventTool().waitNoEvent(500);
//        openSearchBar(editor);        
//        ContainerOperator c = new ContainerOperator(searchBar);
//        JCheckBoxOperator jcbo = new JCheckBoxOperator(c);
//        jcbo.setSelected(true);
//        JTextFieldOperator t = new JTextFieldOperator(c);
//        t.clearText();
//        new EventTool().waitNoEvent(100);
//        t.typeText("Abc");
//        t.pushKey(KeyEvent.VK_ENTER);
//        assertSelection(editor, 8, 11);
//        t.pushKey(KeyEvent.VK_ESCAPE);
//        new EventTool().waitNoEvent(100);
//        assertFalse("ToolBar not closed",searchBar.isVisible());
//        openSearchBar(editor);
//        assertTrue("Checkbox state is not persisten",jcbo.isSelected());
//        assertEquals("Last searched text not persisten",t.getText(),"Abc");
//        jcbo.setSelected(false);
//        t.pushKey(KeyEvent.VK_ESCAPE);
//        new EventTool().waitNoEvent(100);
//        assertFalse("ToolBar not closed",searchBar.isVisible());
//        editor.closeDiscard();
//    }
//    
//    public void testNextButton() {
//        openDefaultProject();
//        openFile("Source Packages|search_replace.IncrementalSearchTest", "match.txt");
//        EditorOperator editor = new EditorOperator("match.txt");
//        editor.setCaretPosition(1, 1);
//        openSearchBar(editor);        
//        ContainerOperator c = new ContainerOperator(searchBar);        
//        JTextFieldOperator t = new JTextFieldOperator(c);
//        t.clearText();
//        new EventTool().waitNoEvent(100);
//        t.typeText("abc");
//        JButtonOperator b = new JButtonOperator(c,1); //Newxt button
//        b.push();
//        assertSelection(editor, 12, 15);
//        b.push();
//        assertSelection(editor, 8, 11);
//        t.pushKey(KeyEvent.VK_ESCAPE);
//        new EventTool().waitNoEvent(100);
//        assertFalse("ToolBar not closed",searchBar.isVisible());
//        editor.closeDiscard();
//    }
//    
//    public void testPrevButton() {
//        openDefaultProject();
//        openFile("Source Packages|search_replace.IncrementalSearchTest", "match.txt");
//        EditorOperator editor = new EditorOperator("match.txt");
//        editor.setCaretPosition(3, 1);
//        openSearchBar(editor);                
//        ContainerOperator c = new ContainerOperator(searchBar);                
//        JTextFieldOperator t = new JTextFieldOperator(c);
//        t.clearText();
//        new EventTool().waitNoEvent(100);
//        t.typeText("abc");
//        JButtonOperator b = new JButtonOperator(c,2);  // Previous button
//        b.push();
//        assertSelection(editor, 8, 11);
//        b.push();
//        assertSelection(editor, 12, 15);
//        t.pushKey(KeyEvent.VK_ESCAPE);
//        new EventTool().waitNoEvent(100);
//        assertFalse("ToolBar not closed",searchBar.isVisible());
//        editor.closeDiscard();
//    }
//    
//    public void testCloseButton() {
//        openDefaultProject();
//        openFile("Source Packages|search_replace.IncrementalSearchTest", "match.txt");
//        EditorOperator editor = new EditorOperator("match.txt");
//        editor.setCaretPosition(3, 1);
//        openSearchBar(editor);        
//        ContainerOperator c = new ContainerOperator(searchBar);
//        JTextFieldOperator t = new JTextFieldOperator(c);        
//        JButtonOperator b = new JButtonOperator(c,3);  // close        
//        b.push();
//        new EventTool().waitNoEvent(250);
//        assertFalse("ToolBar not closed",searchBar.isVisible());
//        editor.closeDiscard();
//    }
//    
//    public void testNotFound() {
//        openDefaultProject();
//        openFile("Source Packages|search_replace.IncrementalSearchTest", "match.txt");
//        EditorOperator editor = new EditorOperator("match.txt");
//        editor.setCaretPosition(3, 1);
//        openSearchBar(editor);        
//        ContainerOperator c = new ContainerOperator(searchBar);
//        JTextFieldOperator t = new JTextFieldOperator(c);
//        t.clearText();
//        new EventTool().waitNoEvent(100);
//        t.typeText("XYZ");
//        assertEquals(new Color(178,0,0),t.getForeground());
//        t.pushKey(KeyEvent.VK_ENTER);
//        JLabelOperator status = new JLabelOperator(editor,3);        
//        assertEquals("'XYZ' not found",status.getText());
//        t.pushKey(KeyEvent.VK_ESCAPE);
//        new EventTool().waitNoEvent(100);
//        assertFalse("ToolBar not closed",searchBar.isVisible());
//        editor.closeDiscard();
//    }
//    
//    public void testSearchForwardBackward() {
//        openDefaultProject();
//        openFile("Source Packages|search_replace.IncrementalSearchTest", "match.txt");
//        EditorOperator editor = new EditorOperator("match.txt");
//        editor.setCaretPosition(2, 1);
//        openSearchBar(editor);        
//        ContainerOperator c = new ContainerOperator(searchBar);
//        JTextFieldOperator t = new JTextFieldOperator(c);
//        t.clearText();
//        new EventTool().waitNoEvent(100);
//        t.typeText("abc");
//        t.pushKey(KeyEvent.VK_ENTER);
//        assertSelection(editor, 4, 7);
//        t.pushKey(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
//        assertSelection(editor, 0, 3);
//        editor.txtEditorPane().requestFocus();
//        new EventTool().waitNoEvent(100);
//        assertFalse("ToolBar not closed by focus transher ",searchBar.isVisible());
//        editor.closeDiscard();
//    }
    
    public void testWholeWords() {
        openDefaultProject();        
        openFile("Source Packages|search_replace.IncrementalSearchTest", "match2.txt");
        EditorOperator editor = new EditorOperator("match2.txt");        
        editor.setCaretPosition(1, 1);
        openSearchBar(editor);        
        ContainerOperator c = new ContainerOperator(searchBar);
        JTextFieldOperator t = new JTextFieldOperator(c);
        JCheckBoxOperator ch1 = new JCheckBoxOperator(c,1);        
        ch1.setSelected(true);
        t.clearText();        
        t.typeText("whole");
        t.pushKey(KeyEvent.VK_ENTER);        
        assertSelection(editor, 12, 17);                
        t.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(100);
        assertFalse("ToolBar not closed",searchBar.isVisible());
        closeFileWithDiscard();
    }

    public void testRegularExpression() {
        openDefaultProject();        
        openFile("Source Packages|search_replace.IncrementalSearchTest", "match2.txt");
        EditorOperator editor = new EditorOperator("match2.txt");        
        editor.setCaretPosition(1, 1);
        openSearchBar(editor);        
        ContainerOperator c = new ContainerOperator(searchBar);
        JTextFieldOperator t = new JTextFieldOperator(c);
        JCheckBoxOperator ch1 = new JCheckBoxOperator(c,1);
        ch1.setSelected(false);
        JCheckBoxOperator ch2 = new JCheckBoxOperator(c,2);
        ch2.setSelected(true);
        t.clearText();        
        t.typeText("[aA]b*cd?");
        t.pushKey(KeyEvent.VK_ENTER);
        assertSelection(editor, 19, 23);        
        t.pushKey(KeyEvent.VK_ENTER);
        assertSelection(editor, 36, 39);        
        t.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(100);
        assertFalse("ToolBar not closed",searchBar.isVisible());
        closeFileWithDiscard();
    }

    public void testFindNext() {
        openDefaultProject();        
        openFile("Source Packages|search_replace.IncrementalSearchTest", "match2.txt");
        EditorOperator editor = new EditorOperator("match2.txt");        
        editor.setCaretPosition(1, 1);
        openSearchBar(editor);        
        ContainerOperator c = new ContainerOperator(searchBar);
        JTextFieldOperator t = new JTextFieldOperator(c);
        JCheckBoxOperator ch1 = new JCheckBoxOperator(c,1);
        ch1.setSelected(false);
        JCheckBoxOperator ch2 = new JCheckBoxOperator(c,2);
        ch2.setSelected(false);
        t.clearText();        
        t.typeText("ab");
        t.pushKey(KeyEvent.VK_ENTER);
        assertSelection(editor, 19, 21);        
        t.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(100);
        assertFalse("ToolBar not closed",searchBar.isVisible());
        t.pushKey(KeyEvent.VK_F3);
        assertSelection(editor, 24, 26);                
        closeFileWithDiscard();
    }
    
    public void testFindPrev() {     
        openDefaultProject();        
        openFile("Source Packages|search_replace.IncrementalSearchTest", "match2.txt");
        EditorOperator editor = new EditorOperator("match2.txt");        
        editor.setCaretPosition(6, 1);
        openSearchBar(editor);        
        ContainerOperator c = new ContainerOperator(searchBar);
        JTextFieldOperator t = new JTextFieldOperator(c);
        JCheckBoxOperator ch1 = new JCheckBoxOperator(c,1);
        ch1.setSelected(false);
        JCheckBoxOperator ch2 = new JCheckBoxOperator(c,2);
        ch2.setSelected(false);
        t.clearText();        
        t.typeText("ab");
        t.pushKey(KeyEvent.VK_ENTER);
        assertSelection(editor, 36, 38);        
        t.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(100);
        assertFalse("ToolBar not closed",searchBar.isVisible());
        t.pushKey(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK);
        assertSelection(editor, 36, 38);                
        t.pushKey(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK);
        assertSelection(editor, 24, 26);                
        closeFileWithDiscard();           
    }


    
    public void assertSelection(EditorOperator editor,int start, int end) {
        JEditorPaneOperator txtEditorPane = editor.txtEditorPane();
        int actStart = txtEditorPane.getSelectionStart();
        int actEnd = txtEditorPane.getSelectionEnd();        
        if(actStart!=start || actEnd!=end) fail("Wrong text selected in editor, actual selection <"+actStart+","+actEnd+">, expected <"+start+","+end+">");
    }
    
    public static void main(String[] args) {
        TestRunner.run(IncrementalSearchTest.class);
    }

    private void openSearchBar(EditorOperator editor) {
        editor.pressKey(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
        new EventTool().waitNoEvent(250);
        getSearchBar((Container)editor.getSource());        
        assertTrue("ToolBar not opened",searchBar.isVisible());
    }
    
}
