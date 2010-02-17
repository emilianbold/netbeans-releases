/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.comprehensive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.netbeans.lib.richexecution.program.Command;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.modules.terminal.api.HyperlinkListener;
import org.netbeans.modules.terminal.api.Terminal;
import org.netbeans.modules.terminal.api.TerminalProvider;
import org.netbeans.terminal.example.TerminalIOProviderSupport;
import org.openide.windows.IOProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

public final class CommandTerminalAction implements ActionListener {

    private final TerminalIOProviderSupport support = new TerminalIOProviderSupport();
    private final TerminalPanel terminalPanel = new TerminalPanel();

    public void actionPerformed(ActionEvent e) {

	DialogDescriptor dd = new DialogDescriptor(
		terminalPanel,
		"Start Terminal",
		true,
		new Object[] {
		    DialogDescriptor.OK_OPTION,
		    DialogDescriptor.CANCEL_OPTION,
		},
		DialogDescriptor.CANCEL_OPTION,
		DialogDescriptor.BOTTOM_ALIGN,
		null,		// HelpCtx
		null		// ActionListener
		);

	// null means all options close dialog:
        // 0-sized array means no option closes dialog
	dd.setClosingOptions(null);

	Object closer = DialogDisplayer.getDefault().notify(dd);
	if (closer == DialogDescriptor.CANCEL_OPTION ||
	    closer == DialogDescriptor.CLOSED_OPTION) {

	    return;
	}


	String cmd = terminalPanel.getCommand();
        if (cmd == null || cmd.trim().equals(""))
            return;


	TerminalPanel.Provider provider = terminalPanel.getProvider();
	TerminalPanel.TC tc = terminalPanel.getTC();
	if (tc == TerminalPanel.TC.DEDICATED) {
	    TerminalProvider terminalProvider = TerminalProvider.getDefault();
	    String preferredID = "CommandTopComponent";

	    Terminal terminal = terminalProvider.createTerminal("command", preferredID);
	    Program program = new Command(cmd);
	    boolean restartable = true;
	    terminal.startProgram(program, restartable);
	} else {
	    switch (provider) {
		case TERM:
		    {
		    IOProvider iop = TerminalIOProviderSupport.getIOProvider();
		    support.executeCommand(iop, cmd);
		    }
		    break;

		case DIRECT:
		    {
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

		    break;
		case DEFAULT:
		    {
		    IOProvider iop = IOProvider.getDefault();
		    support.executeCommand(iop, cmd);
		    }
		    break;
	    }
	}
    }
}