
package org.netbeans.modules.uihandler;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import junit.framework.TestCase;
import java.util.logging.LogRecord;
import javax.swing.Action;

/**
 *
 * @author Jaroslav Tulach
 */
public class UIHandlerTest extends TestCase {
    private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions");

    
    public UIHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Installer o = Installer.findObject(Installer.class, true);
        assertNotNull("Installer created", o);
        o.restored();
    }

    protected void tearDown() throws Exception {
    }

    public void testPublish() {
        
        MyAction a = new MyAction();
        a.putValue(Action.NAME, "Tmp &Action");
        JButton b = new JButton(a);
        
        LogRecord rec = new LogRecord(Level.FINER, "UI_ACTION_BUTTON_PRESS"); // NOI18N
        rec.setParameters(new Object[] { 
            b, 
            b.getClass().getName(), 
            a, 
            a.getClass().getName(), 
            a.getValue(Action.NAME) }
        );
        UILOG.log(rec);        
        
        List<LogRecord> logs = Installer.getLogs();
        assertEquals("One log: " + logs, 1, logs.size());
        LogRecord first = logs.get(0);
        
        assertSame("This is the logged record", rec, first);
        
        
    }
    
    private static final class MyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        }
    }
}
