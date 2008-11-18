/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.api;

/**
 *
 * @author ivan
 */
public interface TerminalWindow {
    /**
     * @return The TerminalContainer owned by this TerminalWindow.
     */
    public TerminalContainer terminalContainer();
}
