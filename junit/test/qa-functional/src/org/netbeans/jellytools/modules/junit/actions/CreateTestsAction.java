/*
 * CreateTestsAction.java
 *
 * Created on 2/6/03 2:21 PM
 */

package org.netbeans.jellytools.modules.junit.actions;

import org.netbeans.jellytools.actions.*;
import java.awt.event.KeyEvent;

/** CreateTestsAction Class
 * @author dave
 */
public class CreateTestsAction extends Action {

    /** creates new CreateTestsAction instance */
    public CreateTestsAction() {
        super("Tools|JUnit Tests|Create Tests", "Tools|JUnit Tests|Create Tests", new Action.Shortcut(KeyEvent.CTRL_MASK|KeyEvent.ALT_MASK, KeyEvent.VK_J));
    }
}
