/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.dedicatedtc;

import org.netbeans.modules.terminal.api.*;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.netbeans.lib.richexecution.program.Command;
import org.netbeans.lib.richexecution.program.Program;
import org.openide.util.NbBundle;

/**
 * Run a command in a dedicated top component.
 */
public class CommandAction extends AbstractAction {

    public CommandAction() {
        super(NbBundle.getMessage(CommandAction.class, "CTL_CommandAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(CommandTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {

        // Ask user what command they want to run
        String cmd = JOptionPane.showInputDialog("Command");
        if (cmd == null || cmd.trim().equals(""))
            return;

        TerminalProvider terminalProvider = TerminalProvider.getDefault();
        String preferredID = "CommandTopComponent";
        Terminal terminal = terminalProvider.createTerminal("command", preferredID);
        Program program = new Command(cmd);
        boolean restartable = true;
        terminal.startProgram(program, restartable);
    }
}
