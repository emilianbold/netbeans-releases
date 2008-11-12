/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.ioprovider;

import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.ActiveTermListener;
import org.netbeans.lib.terminalemulator.Extent;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermInputListener;
import org.netbeans.modules.terminal.api.Terminal;
import org.netbeans.modules.terminal.api.TerminalProvider;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * An implementation of {@link InputOutput} based on
 * {@link org.netbeans.lib.terminalemulator.Term}.
 * <p>
 * This class is public to allow access to the underlying Term.
 * <p>
 * A note on println()'s with OutputListeners:
 * <ul>
 * <li>
 * outputLineAction() works when hyperlinks are clicked.
 * <p>
 * <li>
 * outputLineCleared() didn't make much sense for output2 because output2 had
 * "infinte" history. However, it did make sense when the buffer was cleared.
 * <p>
 * For us issuing Cleared() when the buffer is cleared makes sense but isn't
 * implemented.
 * <br>
 * Issuing Cleared() when a hyperlink scrolls out of the history window
 * also makes sense and is even more work to implement.
 * <li>
 * outputLineSelected() tracked the "caret" in output2. However output2 was
 * "editor" based whereas we're a terminal and a terminals cursor is not
 * a caret ... it doesn't move around that much. (It can move under the
 * control of a program, like vi, but one doesn't generally use hyperlinks
 * in such situations).
 * <p>
 * Term can in principle notify when the cursor is hovering over a hyperlink
 * and perhaps that is the right time to issue Selected().
 * </ul>
 * @author ivan
 */
public final class TerminalInputOutput implements InputOutput {

    private final Terminal terminal;
    private final Term term;
    private OutputWriter outputWriter;
    private Pipe pipe;

    /**
     * Delegate writes to a Term.
     */
    private static class TermWriter extends Writer {

        private final Term term;
        private boolean closed = false;

        TermWriter(Term term) {
            this.term = term;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (closed)
                throw new IOException();
            term.putChars(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            if (closed)
                throw new IOException();
            term.flush();
        }

        @Override
        public void close() throws IOException {
            if (closed)
                return;
            flush();
            closed = true;
        }
    }

    /**
     * Delegate prints and writes to a Term via TermWriter.
     */
    private static class TermOutputWriter extends OutputWriter {
        private final Term term;

        TermOutputWriter(Term term) {
            super(new TermWriter(term));
            this.term = term;
        }

        @Override
        public void println(String s, OutputListener l) throws IOException {
            if ( !(term instanceof ActiveTerm))
                throw new IOException("Term is not an ActiveTerm");

            ActiveTerm at = (ActiveTerm) term;
            ActiveRegion ar = at.beginRegion(true);
            ar.setUserObject(l);
            ar.setLink(true);
            println(s);
            at.endRegion();
        }

        @Override
        public void reset() throws IOException {
            term.clearHistory();
        }
    }

    /**
     * Help pass keystrokes to process.
     */
    private static class Pipe {
        private final Term term;
        private final PipedReader pipedReader;
        private final PipedWriter pipedWriter;

        private class TermListener implements TermInputListener {
            public void sendChars(char[] c, int offset, int count) {
                try {
                    pipedWriter.write(c, offset, count);
                    pipedWriter.flush();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            public void sendChar(char c) {
                try {
                    pipedWriter.write(c);
                    pipedWriter.flush();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        Pipe(Term term) throws IOException {
            this.term = term;
            pipedReader = new PipedReader();
            pipedWriter = new PipedWriter(pipedReader);

            term.addInputListener(new TermListener());
        }

        Reader reader() {
            return pipedReader;
        }
    }

    private static class TerminalOutputEvent extends OutputEvent {
        private final String text;

        public TerminalOutputEvent(InputOutput io, String text) {
            super(io);
            this.text = text;
        }

        @Override
        public String getLine() {
            return text;
        }
    }

    TerminalInputOutput(String name) {
        terminal = TerminalProvider.getDefault().createTerminal(name);
        term = terminal.term();

        if (! (term instanceof ActiveTerm))
            return;

        ActiveTerm at = (ActiveTerm) term;

        // Set up to convert clicks on active regions, created by OutputWriter.
        // println(), to outputLineAction notifications.
        at.setActionListener(new ActiveTermListener() {
            public void action(ActiveRegion r, InputEvent e) {
                OutputListener ol = (OutputListener) r.getUserObject();
                if (ol == null)
                    return;
                Extent extent = r.getExtent();
                String text = term.textWithin(extent.begin, extent.end);
                OutputEvent oe =
                    new TerminalOutputEvent(TerminalInputOutput.this, text);
                ol.outputLineAction(oe);
            }
        });
    }

    public Term term() {
        return term;
    }
    
    /**
     * Stream to write to stuff being output by the proceess destined for the
     * terminal.
     * @return the writer.
     */
    public OutputWriter getOut() {
        if (outputWriter == null)
            outputWriter = new TermOutputWriter(term);
        return outputWriter;
    }

    /**
     * Stream to read from stuff typed into the terminal destined for the process.
     * @return the reader.
     */
    public Reader getIn() {
        if (pipe == null) {
            try {
                pipe = new Pipe(term);
            } catch (IOException ex) {
                return null;
            }
        }
        return pipe.reader();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Output written to this Writer may appear in a different tab (not
     * supported) or different color (easily doable).
     * <p>
     * I'm hesitant to implement this because traditionally separation of
     * stdout and stderr (as done by {@link Process#getErrorStream}) is a dead
     * end. That is why {@link ProcessBuilder}'s redirectErrorStream property is
     * false by default. It is also why
     * {@link org.netbeans.lib.termsupport.TermExecutor#start} will
     * pre-combine stderr and stdout.
     */
    public OutputWriter getErr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void closeInputOutput() {
        terminal.close();
    }

    public boolean isClosed() {
        return terminal.isClosed();
    }

    public void setOutputVisible(boolean value) {
        // no-op in output2
    }

    public void setErrVisible(boolean value) {
        // no-op in output2
    }

    public void setInputVisible(boolean value) {
        // no-op
    }

    public void select() {
        terminal.select();
    }

    public boolean isErrSeparated() {
        return false;
    }

    public void setErrSeparated(boolean value) {
        // no-op in output2
    }

    public boolean isFocusTaken() {
        return false;
    }

    /**
     * output2 considered this to be a "really bad" operation so we will
     * outright not support it.
     */
    public void setFocusTaken(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public Reader flushReader() {
        return pipe.reader();
    }
}