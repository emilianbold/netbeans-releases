package general;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import lib.EditorTestCase;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 * Test of typing at begining/end and other typing tests.
 *
 * @author Miloslav Metelka
 */
public class GeneralTypingTest extends EditorTestCase {
      
    public GeneralTypingTest(String testMethodName) {
        super(testMethodName);
    }
    
    public void testJavaEnterBeginAndEnd(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
        
            EditorOperator editor = getDefaultSampleEditorOperator();

            // 1. move to position [1:1]
            editor.setCaretPosition(0);

            // 2. hit Enter 
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_ENTER);
            
            // 3. move to end of the file
            editor.setCaretPosition(txtOper.getDocument().getLength());
            
            // 4. hit Enter
            txtOper.pushKey(KeyEvent.VK_ENTER);

            // Compare document content to golden file
            compareReferenceFiles(txtOper.getDocument());

        } finally {
            closeFileWithDiscard();
        }
    }
 
}
