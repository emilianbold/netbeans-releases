package editor_actions;

import lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 * Basic Editor Actions Test class. 
 * It contains basic editor actions functionality methods.
 * 
 *
 * @author Martin Roskanin
 */
  public class EditorActionsTest extends EditorTestCase {
      
      
    /** Creates a new instance of Main */
    public EditorActionsTest(String testMethodName) {
        super(testMethodName);
    }

    
    protected void waitForMilis(int maxMiliSeconds){
        int time = (int) maxMiliSeconds / 100;
        while (time > 0) {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
            
        }
    }
    
}
