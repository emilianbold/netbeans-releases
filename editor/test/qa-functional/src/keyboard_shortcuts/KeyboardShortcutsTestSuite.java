package keyboard_shortcuts;

import junit.framework.TestResult;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of keyboard shortcuts in the editor.
 *
 * @author  Petr Felenda
 */
public class KeyboardShortcutsTestSuite extends NbTestSuite {
    
    public KeyboardShortcutsTestSuite() {
        super("Keyboard Shortcuts");
        
        addTestSuite(FindInFileTest.class);
        addTestSuite(ReplaceInFileTest.class);
    }
    
    public static NbTestSuite suite() {
        return new KeyboardShortcutsTestSuite();
    }
    
}
