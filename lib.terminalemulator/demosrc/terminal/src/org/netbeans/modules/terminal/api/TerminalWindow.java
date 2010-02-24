/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.api;

import org.openide.windows.IOContainer;

/**
 *
 * @author ivan
 */
public interface TerminalWindow {
    /**
     * @return The IOContainer owned by this TerminalWindow.
     */
    public IOContainer ioContainer();
}
