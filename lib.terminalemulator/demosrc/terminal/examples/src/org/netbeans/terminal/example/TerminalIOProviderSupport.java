/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.terminal.example;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import org.netbeans.lib.terminalemulator.LineDiscipline;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermListener;
import org.netbeans.lib.richexecution.Command;
import org.netbeans.lib.richexecution.Program;
import org.netbeans.lib.richexecution.Pty;
import org.netbeans.lib.richexecution.PtyException;
import org.netbeans.lib.richexecution.PtyExecutor;
import org.netbeans.modules.terminal.ioprovider.TerminalIOProvider;
import org.netbeans.modules.terminal.ioprovider.TerminalInputOutput;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
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

        Lookup lookup = Lookup.getDefault();
        Collection<? extends IOProvider> ioProviders =
            lookup.lookupAll(IOProvider.class);
        if (ioProviders.size() == 0) {
            System.out.printf("IOProviderActionSupport.getTermIOProvider() lookupAll yielded no results\n");
        } else {
            System.out.printf("IOProviderActionSupport.getTermIOProvider():\n");
            for (IOProvider iopCandidate : ioProviders) {
                System.out.printf("\tIOProvider: %s\n", iopCandidate);
                if (iopCandidate instanceof TerminalIOProvider)
                    iop = iopCandidate;
            }
        }

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

    public void performAction(IOProvider iop, String cmd) {
        // 
        // Create ...
        // ... A standard NB i/o window, if iop is the default IOP
        // ... A Term based i/o window, if iop is TerminalIOProvider
        //
        InputOutput io = iop.getIO("Cmd: " + cmd, true);

        TerminalInputOutput tio = null;
        if (io instanceof TerminalInputOutput)
            tio = (TerminalInputOutput) io;

        OutputWriter toIO = io.getOut();
        Reader fromIO = io.getIn();
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

        Term term = null;
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

        IOShuttle shuttle = new IOShuttle(pin, pout, toIO, fromIO);
        shuttle.run();
    }
}
