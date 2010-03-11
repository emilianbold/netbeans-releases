/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s): Ivan Soleimanipour.
 */
package org.netbeans.lib.terminalemulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;

import javax.swing.SwingUtilities;

public class StreamTerm extends Term {

    private enum IOState {
	NONE,
	INTERNAL,		// getIn()/getOut() were used for internal i/o or
				// explicit i/o shuttling to external process
	EXTERNAL,		// connect() was used to deal with external process
    }

    private IOState ioState = IOState.NONE;

    // Objects used when ioState == INTERNAL
    private Writer writer;      // processes writes from child process
    private Pipe pipe;          // buffers keystrokes to child process

    // Objects used when ioState == EXTERNAL
    private OutputStreamWriter outputStreamWriter;	// writes to child process

    /*
     * Return the OutputStreamWriter used for writing to the child.
     *
     * This can be used to send characters to the child process explicitly
     * as if they were typed at the keyboard.
     */
    public OutputStreamWriter getOutputStreamWriter() {
	switch (ioState) {
	    case NONE:
	    case INTERNAL:
		throw new IllegalStateException("getOutputStreamWriter() can only be used after connect()"); //NOI18N
	    case EXTERNAL:
		break;
	}
        return outputStreamWriter;
    }

    /**
     * Transfers typed keystrokes to an OutputStreamWriter which usually
     * passes stuff on to an external process.
     */
    private static final class InputMonitor implements TermInputListener {
	private final OutputStreamWriter outputStreamWriter;

	public InputMonitor(OutputStreamWriter outputStreamWriter) {
	    this.outputStreamWriter = outputStreamWriter;
	}

	@Override
	public void sendChars(char c[], int offset, int count) {
	    if (outputStreamWriter == null) {
		return;
	    }
	    try {
		outputStreamWriter.write(c, offset, count);
		outputStreamWriter.flush();
	    } catch (Exception x) {
		x.printStackTrace();
	    }
	}

	@Override
	public void sendChar(char c) {
	    if (outputStreamWriter == null) {
		return;
	    }
	    try {
		outputStreamWriter.write(c);
		// writer is buffered, need to use flush!
		// perhaps SHOULD use an unbuffered writer?
		// Also fix send_chars()
		outputStreamWriter.flush();
	    } catch (Exception x) {
		x.printStackTrace();
	    }
	}
    }

    public StreamTerm() {
    }

    /*
     * Monitor output from process and forward to terminal
     */
    private static final class OutputMonitor extends Thread {

        private static final int BUFSZ = 1024;
        private char[] buf = new char[BUFSZ];
        private Term term;
        private InputStreamReader reader;

        OutputMonitor(InputStreamReader reader, Term term) {
            super("StreamTerm.OutputMonitor");	// NOI18N
            this.reader = reader;
            this.term = term;

            // Fix for bug 4921071
            // NetBeans has many request processors running at P1 so
            // a default priority of this thread will swamp all the RPs
            // if we have a firehose sub-process.
            setPriority(1);
        }

        private void db_echo_receipt(char buf[], int offset, int count) {
            /*
             * Debugging function
             */
            System.out.println("Received:");	// NOI18N
            final int width = 20;
            int cx = 0;
            while (cx < count) {
                // print numbers
                int cx0 = cx;
		System.out.printf("%4d: ", cx);
                for (int x = 0; x < width && cx < count; cx++, x++) {
		    System.out.printf("%02x ", (int) buf[offset+cx]);
                }
                System.out.println();

                // print charcters
                cx = cx0;
                System.out.print("      ");	// NOI18N
                for (int x = 0; x < width && cx < count; cx++, x++) {
                    char c = buf[offset + cx];
                    if (Character.isISOControl(c)) {
                        c = ' ';
                    }
		    System.out.printf("%2c ", c);
                }
                System.out.println();
            }
        }

        private final class Trampoline implements Runnable {

            public int nread;

	    @Override
            public void run() {
                term.putChars(buf, 0, nread);
            }
        }

        @Override
        public void run() {
            Trampoline tramp = new Trampoline();

            // A note on catching IOExceptions:
            //
            // In general a close of the fd's writing to 'reader' should
            // generate an EOF and cause 'read' to return -1.
            // However, in practice we get miscellaneous bizarre behaviour:
            // - On linux, with non-packet ptys, we get an
            //   "IOException: Input/output" error ultimately from an EIO
            //   returned by read(2).
            //   I suspect this to be a linux bug which hasn't become visible
            //   because single-threaded termulators like xterm on konsole
            //   use poll/select and after detecting an exiting child just
            //   remove the fd from poll and close it etc. I.e. they don't depend 
            //   on read seeing on EOF.
            // At least one java based termulator I've seen also doesn't
            // bother with -1 and silently handles IOException as here.

            try {
                while (true) {
                    int nread = -1;
                    try {
                        nread = reader.read(buf, 0, BUFSZ);
                    } catch (IOException x) {
                    }
                    if (nread == -1) {
                        // This happens if someone closes the input stream,
                        // say the master end of the pty.
			/* When we clean up this gets closed so it's not
                        always an error.
                        System.err.println("com.sun.spro.Term.OutputMonitor: " +	// NOI18N
                        "Input stream closed");inp	// NOI18N
                         */
                        break;
                    }
                    if (term.debugInput()) {
                        db_echo_receipt(buf, 0, nread);
                    }

                    if (false) {
                        term.putChars(buf, 0, nread);
                    } else {
                        // InvokeAndWait() is surprisingly fast and
                        // eliminates one whole set of MT headaches.
                        tramp.nread = nread;
                        SwingUtilities.invokeAndWait(tramp);
                    }
                }
                reader.close();
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    /**
     * Connect an I/O stream pair or triple to this Term.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     */
    public void connect(OutputStream pin, InputStream pout, InputStream perr) {

	switch (ioState) {
	    case NONE:
		break;
	    case INTERNAL:
		throw new IllegalStateException("Cannot call connect() after getIn()/getOut"); //NOI18N
	    case EXTERNAL:
		throw new IllegalStateException("Cannot call connect() twice"); //NOI18N
	}

        // Now that we have a stream force resize notifications to be sent out.
        updateTtySize();

        if (pin != null) {
            outputStreamWriter = new OutputStreamWriter(pin);
	    addInputListener(new InputMonitor(outputStreamWriter));
        }

	if (pout != null) {
	    InputStreamReader pout_reader = new InputStreamReader(pout);
	    OutputMonitor out_monitor = new OutputMonitor(pout_reader, this);
	    out_monitor.start();
	}

        if (perr != null) {
            InputStreamReader err_reader = new InputStreamReader(perr);
            OutputMonitor err_monitor = new OutputMonitor(err_reader, this);
            err_monitor.start();
        }
    }

    /**
     * Help pass keystrokes to process.
     */
    private static final class Pipe {
        // OLD private final Term term;
        private final PipedReader pipedReader;
        private final PipedWriter pipedWriter;

        private final class TermListener implements TermInputListener {
	    @Override
            public void sendChars(char[] c, int offset, int count) {
                try {
                    pipedWriter.write(c, offset, count);
                    pipedWriter.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

	    @Override
            public void sendChar(char c) {
                try {
                    pipedWriter.write(c);
                    pipedWriter.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        Pipe(Term term) throws IOException {
            // OLD this.term = term;
            pipedReader = new PipedReader();
            pipedWriter = new PipedWriter(pipedReader);

            term.addInputListener(new TermListener());
        }

        Reader reader() {
            return pipedReader;
        }
    }

    /**
     * Delegate writes to a Term.
     */
    private final class TermWriter extends Writer {

        private boolean closed = false;

        TermWriter() {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (closed)
                throw new IOException();
            putChars(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            if (closed)
                throw new IOException();
            //flush();
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
     * Stream to read from stuff typed into the terminal.
     * @return the reader.
     */
    public Reader getIn() {
	switch (ioState) {
	    case NONE:
		break;
	    case INTERNAL:
		break;
	    case EXTERNAL:
		throw new IllegalStateException("Cannot call getIn() after connect()"); //NOI18N
	}
        if (pipe == null) {
            try {
                pipe = new Pipe(this);
            } catch (IOException ex) {
                return null;
            }
        }
        return pipe.reader();
    }

    /**
     * Stream to write to stuff being destined for the terminal.
     * @return the writer.
     */
    public Writer getOut() {
	switch (ioState) {
	    case NONE:
		break;
	    case INTERNAL:
		break;
	    case EXTERNAL:
		throw new IllegalStateException("Cannot call getIn() after connect()"); //NOI18N
	}
        if (writer == null)
            writer = new TermWriter();
        return writer;
    }
}
