package popup_menus;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;


/**
 * Test behavior of Editor Popup Menus
 *
 * @author Martin Roskanin
 */
  public class PopupMenusTestSuite extends NbTestSuite {
      
    public PopupMenusTestSuite() {
        super("Popup Menus");
        
        addTestSuite(MainMenuTest.class);
    }
    

    public static NbTestSuite suite() {
        return new PopupMenusTestSuite();
    }
    
}
