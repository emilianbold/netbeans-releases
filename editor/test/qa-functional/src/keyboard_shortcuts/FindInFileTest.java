package keyboard_shortcuts;
/*
 * Main.java
 *
 * Created on 23. srpen 2004, 17:25
 */

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import lib.EditorTestCase;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jellytools.Bundle;


/**
 *
 * @author  Petr Felenda
 */
public class FindInFileTest extends EditorTestCase {
      
    private final String dialogTitle = Bundle.getStringTrimmed("org.netbeans.editor.Bundle","find-title"); // Find;
    private final int keyCode = KeyEvent.VK_F;
    private final int modifiers = KeyEvent.CTRL_MASK;
      
    /** Creates a new instance of Main */
    public FindInFileTest(String testMethodName) {
        super(testMethodName);
    }
    
    public void testFindInFile(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
        
            EditorOperator editor = getDefaultSampleEditorOperator();

            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(keyCode, modifiers);
            closeDialog(dialogTitle);

        } finally {
            closeFileWithDiscard();
        }
    }
    
}
