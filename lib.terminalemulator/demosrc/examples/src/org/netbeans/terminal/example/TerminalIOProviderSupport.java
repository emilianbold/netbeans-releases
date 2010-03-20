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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.pty.PtyCreatorImpl.PtyImplementation;
import org.netbeans.modules.nativeexecution.spi.pty.PtyImpl;
import org.netbeans.modules.nativeexecution.spi.support.pty.PtyImplAccessor;

import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOTerm;
import org.netbeans.terminal.example.topcomponent.TerminalTopComponent;

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

    private static abstract class ExecutionSupport {
	protected enum State {
	    INIT,
	    RUNNING,
	    EXITED,
	};

	private boolean restartable = false;
	private boolean interalIOShuttle = true;	// use Term.connect()
	private State state = State.INIT;

	protected Action stopAction;
	protected Action rerunAction;
	protected InputOutput io;

	protected static final class ResizeListener implements IOResizable.Listener {

	    private final ExecutionSupport executionSupport;

	    public ResizeListener(ExecutionSupport executionSupport) {
		this.executionSupport = executionSupport;
	    }

	    public void sizeChanged(Dimension c, Dimension p) {
		executionSupport.sizeChanged(c, p);
	    }
	}


	public void setRestartable(Action stopAction, Action rerunAction) {
	    this.restartable = true;
	    this.stopAction = stopAction;
	    this.rerunAction = rerunAction;
	}

	protected final boolean isRestartable() {
	    return restartable;
	}

	public void setInternalIOShuttle(boolean internalIOShuttle) {
	    this.interalIOShuttle = internalIOShuttle;
	}

	public boolean isInternalIOShuttle() {
	    return interalIOShuttle;
	}

	public final void setState(State state) {
	    this.state = state;

	    switch (this.state) {
		case INIT:
		    break;
		case RUNNING:
		    if (isRestartable()) {
			stopAction.setEnabled(true);
			rerunAction.setEnabled(false);
		    }
		    break;
		case EXITED:
		    if (isRestartable() /* LATER && !closing */) {
			stopAction.setEnabled(false);
			rerunAction.setEnabled(true);
		    } else {
			/* LATER
			closing = true;
			closeWork();
			*/
		    }
		    break;
	    }
	}

	public final State getState() {
	    return state;
	}

	public final boolean isRunning() {
	    return state == State.RUNNING;
	}

	private void tprintln(String msg) {
	    try {
		IOColorLines.println(io, msg + "\r", Color.GREEN);
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}

	public final InputOutput setupIO(IOProvider iop,
			     IOContainer ioContainer,
			     String title) {
	    Action[] actions = null;
	    if (isRestartable()) {
		actions = new Action[] {rerunAction, stopAction};
	    } else {
		actions = new Action[0];
	    }

	    io = iop.getIO(title, actions, ioContainer);

	    // comment out to verify fix for bug #181064
	    io.select();

	    if (IOTerm.isSupported(io)) {
		// Term term = IOTerm.term(io);
		// term.setDebugFlags(Term.DEBUG_INPUT);
	    }
	    tprintln("GREETINGS");
	    return io;
	}

	/* *
	 * It's important to start the reaper after setting up the io
	 * connections because a very short-lived process might finish before
	 * the io is setup and we'll be in a situatin of disconnecting before
	 * connecting.
	 */
	protected final void startReaper() {
	    //
	    // Start a reaper and wait for processes completion
	    //
	    Thread reaperThread = new Thread() {
		@Override
		public void run() {
		    final int exitValue = waitFor();

		    if (isInternalIOShuttle() && IOTerm.isSupported(io)) {
			System.out.printf("Process exited. Calling disconnect ...\n");
			IOTerm.disconnect(io, new Runnable() {
			    public void run() {
				System.out.printf("Disconnected.\n");
				String exitMsg = String.format("Exited with %d", exitValue);
				tprintln(exitMsg);
				setState(ExecutionSupport.State.EXITED);
			    }
			});
		    } else {
			System.out.printf("Process exited.\n");
		    }
		}
	    };
	    reaperThread.start();
	}

	protected final void startShuttle(OutputStream pin, InputStream pout) {
	    OutputWriter toIO = io.getOut();
	    Reader fromIO = io.getIn();
	    IOShuttle shuttle = new IOShuttle(pin, pout, toIO, fromIO);
	    shuttle.run();
	}

	public abstract void execute(String cmd);
	public abstract int waitFor();
	public abstract void sizeChanged(Dimension c, Dimension p);
	public abstract void reRun();
	public abstract void stop();
    }

    private static final class RichExecutionSupport extends ExecutionSupport {
	private PtyProcess richProcess;
	private Pty pty;
	private Program lastProgram;

	public void execute(String cmd) {
	    Program program = new Command(cmd);
	    startProgram(program);
	}

	public int waitFor() {
	    return richProcess.waitFor();
	}

	public void sizeChanged(Dimension cells, Dimension pixels) {
	    pty.masterTIOCSWINSZ(cells.height, cells.width,
				 pixels.height, pixels.width);
	}

	private void startProgram(Program program) {
	    //
	    // Create a pty, handle window size changes
	    //
	    try {
		pty = Pty.create(Pty.Mode.REGULAR);
	    } catch (PtyException ex) {
		Exceptions.printStackTrace(ex);
		return;
	    }

	    if (IOResizable.isSupported(io))
		IOResizable.addListener(io, new ResizeListener(this));

	    Map<String, String> env = program.environment();
	    if (IOEmulation.isSupported(io)) {
		env.put("TERM", IOEmulation.getEmulation(io));
	    } else {
		env.put("TERM", "dumb");
	    }

	    if (isRestartable()) {
		lastProgram = program;
	    } else {
		lastProgram = null;
	    }

	    PtyExecutor executor = new PtyExecutor();
	    richProcess = executor.start(program, pty);

	    setState(State.RUNNING);

	    // OLD startReaper();

	    //
	    // connect them up
	    //

	    // Hmm, what's the difference between the PtyProcess io streams
	    // and the Pty's io streams?
	    // Nothing.
	    OutputStream pin = pty.getOutputStream();
	    InputStream pout = pty.getInputStream();

	    if (isInternalIOShuttle() && IOTerm.isSupported(io)) {
		IOTerm.connect(io, pin, pout, null);
	    } else {
		startShuttle(pin, pout);
	    }

	    startReaper();
	}

	public void reRun() {
            if (lastProgram != null)
		startProgram(lastProgram);
	}

	public void stop() {
            richProcess.terminate();
	}
    }

    private static final class NativeExecutionSupport extends ExecutionSupport {
	private String cmd;
	private NativeProcess nativeProcess;
	private PtyImplementation impl;

	public void execute(String cmd) {
	    this.cmd = cmd;

	    ExecutionEnvironment ee = ExecutionEnvironmentFactory.getLocal();
	    NativeProcessBuilder pb =
		    NativeProcessBuilder.newProcessBuilder(ee);
	    // pb = pb.setCommandLine(cmd);
	    pb.setExecutable("/bin/sh");
	    pb.setArguments(new String[] {
		    "-c",
		    cmd
		});
	    pb = pb.setUsePty(true);
	    if (IOEmulation.isSupported(io))
		pb.getEnvironment().put("TERM", IOEmulation.getEmulation(io));
	    else
		pb.getEnvironment().put("TERM", "dumb");

	    //
	    // Start the command
	    //
	    try {
		nativeProcess = pb.call();
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
		return;
	    }

	    setState(State.RUNNING);

	    //
	    // Connect the IO
	    //
	    org.netbeans.modules.nativeexecution.api.pty.PtySupport.Pty
	    pty = PtySupport.getPty(nativeProcess);
	    PtyImpl ptyImpl = PtyImplAccessor.getDefault().getImpl(pty);
	    impl = (PtyImplementation) ptyImpl;
	    if (isInternalIOShuttle() && IOTerm.isSupported(io)) {
		IOTerm.connect(io,
			       impl.getOutputStream(),
			       impl.getInputStream(),
			       null);
	    } else {
		startShuttle(impl.getOutputStream(), impl.getInputStream());
	    }

	    if (IOResizable.isSupported(io))
		IOResizable.addListener(io, new ResizeListener(this));

	    startReaper();
	}

	public int waitFor() {
	    try {
		return nativeProcess.waitFor();
	    } catch (InterruptedException ex) {
		Exceptions.printStackTrace(ex);
		return 0;
	    }
	}

	public void sizeChanged(Dimension c, Dimension p) {
	    // TMP impl.masterTIOCSWINSZ(c.width, c.height, p.width, p.height);
	}

	public void reRun() {
	    if (cmd != null)
		execute(cmd);
	}

	public void stop() {
	    nativeProcess.destroy();
	}
    }

    private ExecutionSupport richExecutionSupport = new RichExecutionSupport();
    private ExecutionSupport nativeExecutionSupport = new NativeExecutionSupport();

    private final class RerunAction extends AbstractAction {
	private final ExecutionSupport executionSupport;

        public RerunAction(ExecutionSupport executionSupport) {
	    this.executionSupport = executionSupport;
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
	    if (executionSupport.getState() == ExecutionSupport.State.RUNNING)
                return;     // still someone running
            // TMP setEnabled(false);
	    executionSupport.reRun();
        }
    }

    private final class StopAction extends AbstractAction {
	private final ExecutionSupport executionSupport;

        public StopAction(ExecutionSupport executionSupport) {
	    this.executionSupport = executionSupport;
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
	    if (executionSupport.getState() != ExecutionSupport.State.RUNNING)
                return;
            // TMP setEnabled(false);
	    executionSupport.stop();
        }
    }


    public static IOContainer getIOContainer() {
	TerminalTopComponent ttc = TerminalTopComponent.findInstance();
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


    public InputOutput executeRichCommand(IOProvider iop, IOContainer ioContainer, String cmd,
	                           final boolean restartable,
				   final boolean internalIOShuttle) {
	if (richExecutionSupport.isRunning())
            throw new IllegalStateException("Process already running");

	final String title = "Cmd: " + cmd;
	if (restartable) {
	    Action stopAction = new StopAction(richExecutionSupport);
	    Action rerunAction = new RerunAction(richExecutionSupport);
	    richExecutionSupport.setRestartable(stopAction, rerunAction);
	}

	richExecutionSupport.setInternalIOShuttle(internalIOShuttle);
	InputOutput io = richExecutionSupport.setupIO(iop, ioContainer, title);

	richExecutionSupport.execute(cmd);
	return io;
    }

    public void executeShell(IOProvider iop, IOContainer ioContainer) {
	executeRichCommand(iop, ioContainer, "/bin/bash", false, true);
	/* OLD
	final String title = "Shell";
	setupIO(iop, ioContainer, title, false);
	Program program = new Shell();
	startProgram(program, false);
	 */
    }

    public InputOutput executeNativeCommand(IOProvider iop, IOContainer ioContainer, String cmd,
	                             final boolean restartable,
				     final boolean internalIOShuttle) {
	if (nativeExecutionSupport.isRunning())
            throw new IllegalStateException("Process already running");

	final String title = "Cmd: " + cmd;

	if (restartable) {
	    Action stopAction = new StopAction(nativeExecutionSupport);
	    Action rerunAction = new RerunAction(nativeExecutionSupport);
	    nativeExecutionSupport.setRestartable(stopAction, rerunAction);
	}

	nativeExecutionSupport.setInternalIOShuttle(internalIOShuttle);
	InputOutput io = nativeExecutionSupport.setupIO(iop, ioContainer, title);

	nativeExecutionSupport.execute(cmd);
	return io;
    }
}
