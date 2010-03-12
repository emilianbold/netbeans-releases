/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.comprehensive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.IOProvider;
import org.openide.windows.IOContainer;


import org.netbeans.terminal.example.TerminalIOProviderSupport;
import org.openide.util.RequestProcessor;

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


	final String cmd = terminalPanel.getCommand();
        if (cmd == null || cmd.trim().equals(""))
            return;

	final TerminalIOProviderSupport support = new TerminalIOProviderSupport();
	final boolean restartable = terminalPanel.isRestartable();
	final IOContainer container;
	final IOProvider iop;

	switch (terminalPanel.getContainerProvider()) {
	    case TERM:
		container = TerminalIOProviderSupport.getIOContainer();
		break;
	    case DEFAULT:
	    default:
		container = null;
		break;
	}

	switch (terminalPanel.getIOProvider()) {
	    case TERM:
		iop = TerminalIOProviderSupport.getIOProvider();
		break;
	    case DEFAULT:
	    default:
		iop = IOProvider.getDefault();
		break;
	}

	final Runnable runnable = new Runnable() {
	    public void run() {
		support.executeCommand(iop, container, cmd, restartable);
	    }
	};

	switch (terminalPanel.getThread()) {
	    case EDT:
		SwingUtilities.invokeLater(runnable);
		break;
	    case RP:
		RequestProcessor.getDefault().execute(runnable);
		break;
	}
    }
}