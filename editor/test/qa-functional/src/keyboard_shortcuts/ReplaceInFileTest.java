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
public class ReplaceInFileTest extends EditorTestCase {
      
    private final String dialogTitle = Bundle.getStringTrimmed("org.netbeans.editor.Bundle","replace-title"); // Replace;
    private final int keyCode = KeyEvent.VK_H;
    private final int modifiers = KeyEvent.CTRL_MASK;
      
    /** Creates a new instance of Main */
    public ReplaceInFileTest(String testMethodName) {
        super(testMethodName);
    }

    public void testReplaceInFile(){
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
