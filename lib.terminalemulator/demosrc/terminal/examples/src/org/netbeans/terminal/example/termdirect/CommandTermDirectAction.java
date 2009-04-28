/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.termdirect;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.netbeans.lib.richexecution.program.Command;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.modules.terminal.api.HyperlinkListener;
import org.netbeans.modules.terminal.api.Terminal;
import org.netbeans.modules.terminal.api.TerminalProvider;

/**
 * Run a command under a pty and interact with it though the 
 * terminalprovider API as opposed to IOProvider.
 * @author ivan
 */
public final class CommandTermDirectAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {

        // Ask user what command they want to run
        String cmd = JOptionPane.showInputDialog("Command");
        if (cmd == null || cmd.trim().equals(""))
            return;

        TerminalProvider terminalProvider = TerminalProvider.getDefault();
        Terminal terminal = terminalProvider.createTerminal("command: " + cmd);

        // need to be dtterm to demonstrate hyperlinks
        terminal.term().setEmulation("dtterm");
        terminal.setHyperlinkListener(new HyperlinkListener() {
            public void action(String clientData) {
                JOptionPane.showMessageDialog(null, clientData);
            }
        });

        Program program = new Command(cmd);
        boolean restartable = true;
        terminal.startProgram(program, restartable);
    }
}
