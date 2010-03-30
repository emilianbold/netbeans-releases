/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.comprehensive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import org.netbeans.terminal.example.Config;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.IOProvider;
import org.openide.windows.IOContainer;


import org.netbeans.terminal.example.TerminalIOProviderSupport;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

public final class CommandTerminalAction implements ActionListener {

    private final TerminalPanel terminalPanel = new TerminalPanel();

    public static InputOutput lastIO;

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
	if (closer == DialogDescriptor.CANCEL_OPTION) {
	    System.out.printf("Dialog cancelled\n");
	    return;
	}
	if (closer == DialogDescriptor.CLOSED_OPTION) {
	    System.out.printf("Dialog closed\n");
	    return;
	}


	final Config config = terminalPanel.getConfig();
	final String cmd = config.getCommand();
        if (cmd == null || cmd.trim().equals(""))
            return;

	final TerminalIOProviderSupport support = new TerminalIOProviderSupport();
	final IOContainer container;
	final IOProvider iop;

	switch (config.getContainerProvider()) {
	    case TERM:
		container = TerminalIOProviderSupport.getIOContainer();
		break;
	    case DEFAULT:
	    default:
		container = null;
		break;
	}

	switch (config.getIOProvider()) {
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
		final InputOutput io;
		switch (config.getExecution()) {
		    case RICH:
			io = support.executeRichCommand(iop, container, config);
			break;
		    case NATIVE:
			io = support.executeNativeCommand(iop, container, config);
			break;
		    default:
			io = null;
			break;
		}
		lastIO = io;
	    }
	};

	switch (config.getThread()) {
	    case EDT:
		SwingUtilities.invokeLater(runnable);
		break;
	    case RP:
		RequestProcessor.getDefault().execute(runnable);
		break;
	}
    }
}