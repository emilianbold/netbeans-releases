/*
 * ExecuteTestAction.java
 *
 * Created on 2/6/03 2:21 PM
 */

package org.netbeans.jellytools.modules.junit.actions;

import org.netbeans.jellytools.actions.*;
import java.awt.event.KeyEvent;

/** ExecuteTestAction Class
 * @author dave
 */
public class ExecuteTestAction extends Action {

    /** creates new ExecuteTestAction instance */
    public ExecuteTestAction() {
        super("Tools|JUnit Tests|Execute Test", "Tools|JUnit Tests|Execute Test", new Action.Shortcut(KeyEvent.CTRL_MASK|KeyEvent.ALT_MASK, KeyEvent.VK_L));
    }
}
