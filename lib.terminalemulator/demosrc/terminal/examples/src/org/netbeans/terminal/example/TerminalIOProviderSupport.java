/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.terminal.example;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;

import org.netbeans.lib.richexecution.program.Command;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.lib.richexecution.Pty;
import org.netbeans.lib.richexecution.PtyException;
import org.netbeans.lib.richexecution.PtyExecutor;
import org.netbeans.lib.terminalemulator.StreamTerm;

import org.netbeans.modules.terminal.ioprovider.IOEmulation;
import org.netbeans.modules.terminal.ioprovider.IOResizable;
import org.netbeans.modules.terminal.ioprovider.TerminalInputOutput;

import org.netbeans.modules.terminal.ui.TermTopComponent;

import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOContainer;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Support for running @{link Command}s under @{link IOProvider}s.
 * @author ivan
 */
public final class TerminalIOProviderSupport {

    public static IOContainer getIOContainer() {
	TermTopComponent ttc = TermTopComponent.findInstance();
	return ttc.ioContainer();
    }

    /**
     * Declare whether io to 'io' is internal to the IDE or external, via a pty.
     * For internal io Term requires a proper line discipline, for example,
     * to convert the "\n" emitted by println() to a "\n\r" and so on.
     * @param io The InputOutput to modify.
     * @param b Add line discipline if true.
     */
    public static void setInternal(InputOutput io, boolean b) {
	if (IOEmulation.isSupported(io) && b)
	    IOEmulation.setDisciplined(io);
    }

    private void startShuttle(InputOutput io, OutputStream pin, InputStream pout) {
	OutputWriter toIO = io.getOut();
	Reader fromIO = io.getIn();
	IOShuttle shuttle = new IOShuttle(pin, pout, toIO, fromIO);
	shuttle.run();
    }

    public void executeCommand(InputOutput io, String cmd) {
        try {
            IOColorLines.println(io, "GREETINGS\r", Color.GREEN);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        TerminalInputOutput tio = null;
        if (io instanceof TerminalInputOutput)
            tio = (TerminalInputOutput) io;

        io.select();

	//
	// Create a pty, handle window size changes
	//
	final Pty pty;
	try {
	    pty = Pty.create(Pty.Mode.REGULAR);
	} catch (PtyException ex) {
	    Exceptions.printStackTrace(ex);
	    return;
	}

	if (IOResizable.isSupported(io)) {
	    IOResizable.addListener(io, new IOResizable.Listener() {
		public void sizeChanged(Dimension cells, Dimension pixels) {
		    pty.masterTIOCSWINSZ(cells.height, cells.width,
					 pixels.height, pixels.width);
		}
	    });
	}

	//
	// Create a program and process
	//
	Program program = new Command(cmd);
	Map<String, String> env = program.environment();
	if (IOEmulation.isSupported(io)) {
	    env.put("TERM", IOEmulation.getEmulation(io));
	} else {
	    env.put("TERM", "dumb");
	}

	PtyExecutor executor = new PtyExecutor();
	executor.start(program, pty);

	//
	// connect them up
	//

	// Hmm, what's the difference between the PtyProcess io streams
	// and the Pty's io streams?
	// Nothing.
	OutputStream pin = pty.getOutputStream();
	InputStream pout = pty.getInputStream();

	boolean implicit = false;
	if (implicit) {
	    StreamTerm term = (tio != null)? tio.term(): null;
	    if (term != null)
		term.connect(pin, pout, null);
	    else
	    startShuttle(io, pin, pout);
	} else {
	    startShuttle(io, pin, pout);
	}
    }
}
