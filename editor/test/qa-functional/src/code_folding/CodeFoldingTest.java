package code_folding;

import java.awt.event.KeyEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import lib.EditorTestCase;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * Basic Code Folding Test class. 
 * It contains basic folding functionality methods.
 * 
 *
 * @author Martin Roskanin
 */
  public class CodeFoldingTest extends EditorTestCase {
      
    /** Creates a new instance of Main */
    public CodeFoldingTest(String testMethodName) {
        super(testMethodName);
    }

    protected static void appendSpaces(StringBuffer sb, int spaces) {
        while (--spaces >= 0) {
            sb.append(' ');
        }
    }
    
    protected static String foldToStringChildren(Fold fold, int indent) {
        indent += 4;
        StringBuffer sb = new StringBuffer();
        sb.append(fold);
        sb.append('\n');
        int foldCount = fold.getFoldCount();
        for (int i = 0; i < foldCount; i++) {
            appendSpaces(sb, indent);
            sb.append('[');
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(foldToStringChildren(fold.getFold(i), indent));
        }
        
        return sb.toString();
    }

    
    public static String foldHierarchyToString(JTextComponent target){
        String ret = "";
        AbstractDocument adoc = (AbstractDocument)target.getDocument();

        // Dump fold hierarchy
        FoldHierarchy hierarchy = FoldHierarchy.get(target);
        adoc.readLock();
        try {
            hierarchy.lock();
            try {
                Fold root = hierarchy.getRootFold();
                ret = (root == null) ? "root is null" : foldToStringChildren(root, 0); //NOI18N
            } finally {
                hierarchy.unlock();
            }
        } finally {
            adoc.readUnlock();
        }
        return ret;
    }
    
    protected void waitForFolding(JTextComponent target, int maxMiliSeconds){
        //wait for parser and folding hierarchy creation
        int time = (int) maxMiliSeconds / 100;
        
        AbstractDocument adoc = (AbstractDocument)target.getDocument();
        
        // Dump fold hierarchy
        FoldHierarchy hierarchy = FoldHierarchy.get(target);
        int foldCount = 0;
        while (foldCount==0 && time > 0) {
            
            adoc.readLock();
            try {
                hierarchy.lock();
                try {
                    foldCount = hierarchy.getRootFold().getFoldCount();
                } finally {
                    hierarchy.unlock();
                }
            } finally {
                adoc.readUnlock();
            }
            
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
            
        }
            
    }
    

    protected void collapseFoldAtCaretPosition(EditorOperator editor, int line, int column){
        // 1. move to adequate place 
        editor.setCaretPosition(line, column);

        // 2. hit CTRL -
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        txtOper.pushKey(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK);
        
        // wait a while
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException ex) {
        }
        
    }

    protected void expandFoldAtCaretPosition(EditorOperator editor, int line, int column){
        // 1. move to adequate place 
        editor.setCaretPosition(line, column);

        // 2. hit CTRL +
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        txtOper.pushKey(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK);
        
        // wait a while
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException ex) {
        }
    }
    
    protected void collapseAllFolds(EditorOperator editor){
        // hit CTRL Shift -
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        txtOper.pushKey(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        
        // wait a while
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException ex) {
        }
    }

    protected void expandAllFolds(EditorOperator editor){
        // hit CTRL Shift +
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        txtOper.pushKey(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        
        // wait a while
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException ex) {
        }
    }
    
}
