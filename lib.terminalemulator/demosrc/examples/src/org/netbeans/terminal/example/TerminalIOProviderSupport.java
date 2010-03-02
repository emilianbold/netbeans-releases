/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.terminal.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.lib.richexecution.program.Command;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.lib.richexecution.Pty;
import org.netbeans.lib.richexecution.PtyException;
import org.netbeans.lib.richexecution.PtyExecutor;
import org.netbeans.lib.richexecution.PtyProcess;
import org.netbeans.lib.richexecution.program.Shell;

import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOTerm;

import org.netbeans.modules.terminal.ui.TermTopComponent;

import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Support for running @{link Command}s under @{link IOProvider}s.
 * @author ivan
 */
public final class TerminalIOProviderSupport {

    private InputOutput io;
    private PtyProcess termProcess;
    private Program lastProgram;
    private final Action stopAction = new StopAction();
    private final Action rerunAction = new RerunAction();

    private final class RerunAction extends AbstractAction {
        public RerunAction() {
            setEnabled(false);
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(TerminalIOProviderSupport.class.getResource("rerun.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return "Re-run";
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            System.out.printf("Re-run pressed!\n");
            if (!isEnabled())
                return;
            if (termProcess != null)
                return;     // still someone running
            if (lastProgram == null)
                return;
            startProgram(lastProgram, true);
        }
    }

    private final class StopAction extends AbstractAction {
        public StopAction() {
            setEnabled(false);
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(TerminalIOProviderSupport.class.getResource("stop.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return "Stop";
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            System.out.printf("Stop pressed!\n");
            if (!isEnabled())
                return;
            if (termProcess == null)
                return;
            termProcess.terminate();
        }
    }


    public static IOContainer getIOContainer() {
	TermTopComponent ttc = TermTopComponent.findInstance();
	return ttc.ioContainer();
    }

    public static IOProvider getIOProvider() {
        IOProvider iop = IOProvider.get("Terminal");       // NOI18N
        if (iop == null) {
            System.out.printf("IOProviderActionSupport.getTermIOProvider() couldn't find our provider\n");
            iop = IOProvider.getDefault();
        }
        return iop;
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

    private void setupIO(IOProvider iop,
	                 IOContainer ioContainer,
			 String title,
			 boolean restartable) {
	Action[] actions = null;
	if (restartable) {
	    actions = new Action[] {rerunAction, stopAction};
	} else {
	    actions = new Action[0];
	}

	io = iop.getIO(title, actions, ioContainer);

	// comment out to verify fix for bug #181064
        io.select();
        try {
            IOColorLines.println(io, "GREETINGS\r", Color.GREEN);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

	/* OLD
	//
	// Create a pty, handle window size changes
	//
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
	 */
    }

    private void startProgram(Program program, final boolean restartable) {
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

	Map<String, String> env = program.environment();
	if (IOEmulation.isSupported(io)) {
	    env.put("TERM", IOEmulation.getEmulation(io));
	} else {
	    env.put("TERM", "dumb");
	}

	if (restartable) {
	    lastProgram = program;
	} else {
	    lastProgram = null;
	}

	PtyExecutor executor = new PtyExecutor();
	termProcess = executor.start(program, pty);

	if (restartable) {
	    stopAction.setEnabled(true);
	    rerunAction.setEnabled(false);
	}

        Thread reaperThread = new Thread() {
            @Override
            public void run() {
                termProcess.waitFor();
                if (restartable /* LATER && !closing */) {
                    stopAction.setEnabled(false);
                    rerunAction.setEnabled(true);
                } else {
		    /* LATER
                    closing = true;
                    closeWork();
		    */
                }
                // This doesn't yield the desired result because we need to
                // wait for all the output to be processed:
                // LATER tprintf("Exited with %d\n\r", termProcess.exitValue());
                termProcess = null;
            }
        };
        reaperThread.start();

	//
	// connect them up
	//

	// Hmm, what's the difference between the PtyProcess io streams
	// and the Pty's io streams?
	// Nothing.
	OutputStream pin = pty.getOutputStream();
	InputStream pout = pty.getInputStream();

	boolean implicit = true;
	if (implicit) {
	    if (IOTerm.isSupported(io)) {
		IOTerm.connect(io, pin, pout, null);
	    } else {
		startShuttle(io, pin, pout);
	    }
	} else {
	    startShuttle(io, pin, pout);
	}
    }

    public void executeCommand(IOProvider iop, IOContainer ioContainer, String cmd, boolean restartable) {
        if (termProcess != null)
            throw new IllegalStateException("Process already running");

	final String title = "Cmd: " + cmd;
	setupIO(iop, ioContainer, title, restartable);
	Program program = new Command(cmd);
	startProgram(program, restartable);
    }


    public void executeShell(IOProvider iop, IOContainer ioContainer) {
	final String title = "Shell";
	setupIO(iop, ioContainer, title, false);
	Program program = new Shell();
	startProgram(program, false);
    }

}
