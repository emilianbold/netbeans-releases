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
import org.netbeans.lib.terminalemulator.LineDiscipline;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermListener;
import org.netbeans.lib.richexecution.program.Command;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.lib.richexecution.Pty;
import org.netbeans.lib.richexecution.PtyException;
import org.netbeans.lib.richexecution.PtyExecutor;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.modules.terminal.ioprovider.IOExecution;
import org.netbeans.modules.terminal.ioprovider.TerminalInputOutput;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Support for running @{link Command}s under @{link IOProvider}s.
 * @author ivan
 */
public final class TerminalIOProviderSupport {

    public static IOProvider getIOProvider() {
        IOProvider iop = null;
        iop = IOProvider.get("Terminal");       // NOI18N
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
        TerminalInputOutput tio = null;
        if (io instanceof TerminalInputOutput)
            tio = (TerminalInputOutput) io;

        Term term = null;
        if (tio != null) {
            term = tio.term();
            term.pushStream(new LineDiscipline());
        }
    }

    public void executeCommand(IOProvider iop, String cmd) {
        // 
        // Create ...
        // ... A standard NB i/o window, if iop is the default IOP
        // ... A Term based i/o window, if iop is TerminalIOProvider
        //
        InputOutput io = iop.getIO("Cmd: " + cmd, true);
        try {
            IOColorLines.println(io, "GREETINGS\r", Color.GREEN);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        TerminalInputOutput tio = null;
        if (io instanceof TerminalInputOutput)
            tio = (TerminalInputOutput) io;

        io.select();

        /* LATER
        if (false) {
            // Adds a line discipline so newlines etc work correctly
            TerminalIOProviderSupport.setInternal(io, true);

            if (IOColorLines.isSupported(io))
                IOColorLines.println(io, "Hello", null, true, Color.red);

            if (IOColorPrint.isSupported(io)) {
                IOColorPrint.print(io, "Hello in red\n", Color.red);

                IOColorPrint.print(io, "Hello ", Color.blue);
                IOColorPrint.print(io, "in ", Color.yellow);
                IOColorPrint.print(io, "rainbow\n", Color.green);

                IOColorPrint.print(io, "Hello ", Color.blue.darker());
                IOColorPrint.print(io, "in ", Color.yellow.darker());
                IOColorPrint.print(io, "dark rainbow\n", Color.green.darker());
            }

            // Take out line discipline -- doesn't really work.
            // TerminalIOProviderSupport.setInternal(io, false);
        }
        */

        if (IOExecution.isSupported(io)) {
            Program program = new Command(cmd);
            IOExecution.execute(io, program);

        } else {
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

            StreamTerm term = null;
            if (tio != null) {
                term = tio.term();
                term.addListener(new TermListener() {
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
            if (term != null) {
                Map<String, String> env = program.environment();
                env.put("TERM", term.getEmulation());
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

            boolean implicit = true;
            if (implicit) {
                term.connect(pin, pout, null);
            } else {
                OutputWriter toIO = io.getOut();
                Reader fromIO = io.getIn();
                IOShuttle shuttle = new IOShuttle(pin, pout, toIO, fromIO);
                shuttle.run();
            }
        }
    }
}
