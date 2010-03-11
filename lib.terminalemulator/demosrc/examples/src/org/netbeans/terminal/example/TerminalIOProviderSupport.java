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
	private boolean restartable = false;
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

	public final void setupIO(IOProvider iop,
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
	    try {
		IOColorLines.println(io, "GREETINGS\r", Color.GREEN);
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}

	protected final void startShuttle(OutputStream pin, InputStream pout) {
	    OutputWriter toIO = io.getOut();
	    Reader fromIO = io.getIn();
	    IOShuttle shuttle = new IOShuttle(pin, pout, toIO, fromIO);
	    shuttle.run();
	}

	public abstract boolean isRunning();
	public abstract void execute(String cmd);
	public abstract void sizeChanged(Dimension c, Dimension p);
	public abstract void reRun();
	public abstract void stop();
    }

    private static final class RichExecutionSupport extends ExecutionSupport {
	private PtyProcess richProcess;
	private Pty pty;
	private Program lastProgram;

	public boolean isRunning() {
	    return richProcess != null;
	}

	public void execute(String cmd) {
	    Program program = new Command(cmd);
	    startProgram(program);
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

	    if (isRestartable()) {
		stopAction.setEnabled(true);
		rerunAction.setEnabled(false);
	    }

	    Thread reaperThread = new Thread() {
		@Override
		public void run() {
		    richProcess.waitFor();
		    if (isRestartable() /* LATER && !closing */) {
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
		    richProcess = null;
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
		    startShuttle(pin, pout);
		}
	    } else {
		startShuttle(pin, pout);
	    }
	}

	public void reRun() {
            if (richProcess != null)
                return;     // still someone running
            if (lastProgram == null)
                return;
            startProgram(lastProgram);
	}

	public void stop() {
            if (richProcess == null)
                return;
            richProcess.terminate();
	}
    }

    private static final class NativeExecutionSupport extends ExecutionSupport {
	private NativeProcess nativeProcess;
	private PtyImplementation impl;

	public boolean isRunning() {
	    return nativeProcess != null;
	}

	public void execute(String cmd) {
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

	    if (isRestartable()) {
		stopAction.setEnabled(true);
		rerunAction.setEnabled(false);
	    }

	    //
	    // Connect the IO
	    //
	    org.netbeans.modules.nativeexecution.api.pty.PtySupport.Pty
	    pty = PtySupport.getPty(nativeProcess);
	    PtyImpl ptyImpl = PtyImplAccessor.getDefault().getImpl(pty);
	    impl = (PtyImplementation) ptyImpl;
	    IOTerm.connect(io,
			   impl.getOutputStream(),
			   impl.getInputStream(),
			   null);

	    if (IOResizable.isSupported(io))
		IOResizable.addListener(io, new ResizeListener(this));

	    //
	    // Start a reaper and wait for processes completion
	    //
	    Thread reaperThread = new Thread() {
		@Override
		public void run() {
		    try {
			nativeProcess.waitFor();
		    } catch (InterruptedException ex) {
			Exceptions.printStackTrace(ex);
		    }
		    if (isRestartable() /* LATER && !closing */) {
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
		    nativeProcess = null;
		}
	    };
	    reaperThread.start();
	}

	public void sizeChanged(Dimension c, Dimension p) {
	    impl.masterTIOCSWINSZ(c.width, c.height, p.width, p.height);
	}

	public void reRun() {
	}

	public void stop() {
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


    public void executeRichCommand(IOProvider iop, IOContainer ioContainer, String cmd, final boolean restartable) {
	if (richExecutionSupport.isRunning())
            throw new IllegalStateException("Process already running");

	final String title = "Cmd: " + cmd;
	Action stopAction = new StopAction(richExecutionSupport);
	Action rerunAction = new RerunAction(richExecutionSupport);
	if (restartable)
	    nativeExecutionSupport.setRestartable(stopAction, rerunAction);

	richExecutionSupport.setupIO(iop, ioContainer, title);

	richExecutionSupport.execute(cmd);
    }

    public void executeShell(IOProvider iop, IOContainer ioContainer) {
	executeRichCommand(iop, ioContainer, "/bin/bash", false);
	/* OLD
	final String title = "Shell";
	setupIO(iop, ioContainer, title, false);
	Program program = new Shell();
	startProgram(program, false);
	 */
    }

    public void executeNativeCommand(IOProvider iop, IOContainer ioContainer, String cmd, final boolean restartable) {
	if (nativeExecutionSupport.isRunning())
            throw new IllegalStateException("Process already running");

	final String title = "Cmd: " + cmd;

	Action stopAction = new StopAction(richExecutionSupport);
	Action rerunAction = new RerunAction(richExecutionSupport);
	if (restartable)
	    nativeExecutionSupport.setRestartable(stopAction, rerunAction);

	nativeExecutionSupport.setupIO(iop, ioContainer, title);

	nativeExecutionSupport.execute(cmd);
    }



}
