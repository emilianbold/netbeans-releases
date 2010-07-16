/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.comprehensive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class SelectLastAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
	if (CommandTerminalAction.lastIO == null)
	    return;
	else
	    CommandTerminalAction.lastIO.select();
    }
}
