/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.comprehensive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.IOProvider;
import org.openide.windows.IOContainer;


import org.netbeans.terminal.example.TerminalIOProviderSupport;

public final class CommandTerminalAction implements ActionListener {

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


	final TerminalIOProviderSupport support = new TerminalIOProviderSupport();

	boolean restartable = true;

	TerminalPanel.Provider provider = terminalPanel.getProvider();
	TerminalPanel.TC tc = terminalPanel.getTC();
	if (tc == TerminalPanel.TC.DEDICATED) {
	    /* OLD
	    if (false) {
		TerminalProvider terminalProvider = TerminalProvider.getDefault();
		String name = "command";
		String preferredID = "CommandTopComponent";
		Terminal terminal = terminalProvider.createTerminal(name, preferredID);
		Program program = new Command(cmd);
		boolean restartable = true;
		terminal.startProgram(program, restartable);
	    } else
	    */
	    {
		IOContainer container = TerminalIOProviderSupport.getIOContainer();
		IOProvider iop = TerminalIOProviderSupport.getIOProvider();
		support.executeCommand(iop, container, cmd, restartable);
	    }

	} else {
	    switch (provider) {
		case TERM:
		    {
		    IOProvider iop = TerminalIOProviderSupport.getIOProvider();
		    support.executeCommand(iop, null, cmd, restartable);
		    }
		    break;
		case DEFAULT:
		    {
		    IOProvider iop = IOProvider.getDefault();
		    support.executeCommand(iop, null, cmd, restartable);
		    }
		    break;

		case DIRECT:
		    /* OLD
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
		    terminal.startProgram(program, restartable);
		    }
		     */

		    break;
	    }
	}
    }
}