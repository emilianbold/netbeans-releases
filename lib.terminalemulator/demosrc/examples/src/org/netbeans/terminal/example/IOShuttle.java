package org.netbeans.terminal.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.openide.ErrorManager;
import org.openide.windows.OutputWriter;

/**
 * Shuttles data between the io of a {@link java.lang.Process}, or any
 * sort of derivative/mimic, and an NB-style IOProvider.
 * @author ivan
 */
final class IOShuttle {

    private final OutputMonitor outputMonitor;
    private final InputMonitor inputMonitor;

    public IOShuttle(OutputStream pin, InputStream pout,
                      OutputWriter toIO, Reader fromIO) {
        InputStreamReader fromProc = new InputStreamReader(pout);
        outputMonitor = new IOShuttle.OutputMonitor(fromProc, toIO);
        if (fromIO != null) {
            final OutputStreamWriter toProc = new OutputStreamWriter(pin);
            inputMonitor = new IOShuttle.InputMonitor(fromIO, toProc);
        } else {
            inputMonitor = null;
        }
    }

    public void run() {
        outputMonitor.start();
        if (inputMonitor != null)
            inputMonitor.start();
    }

    private static class OutputMonitor extends Thread {

        private final InputStreamReader reader;
        private final Writer writer;
        private static int nextSerial = 0;
        private int serial = nextSerial++;
        private static final int BUFSZ = 1024;
        private final char[] buf = new char[BUFSZ];

        OutputMonitor(InputStreamReader reader, Writer writer) {
            super("ExecutorUnix.OutputMonitor");
            // NOI18N
            this.reader = reader;
            this.writer = writer;

            // see bug 4921071
            setPriority(1);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int nread = reader.read(buf, 0, BUFSZ);
                    if (nread == -1) {
                        break;
                    }
                    writer.write(buf, 0, nread);
                }
            } catch (IOException e) {
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }

    /**
     * Copies kestrokes in io window to external process.
     */
    private static class InputMonitor extends Thread {

        private final Reader reader;
        private final Writer writer;
        private static int nextSerial = 0;
        private int serial = nextSerial++;
        private static final int BUFSZ = 1024;
        private final char[] buf = new char[BUFSZ];

        InputMonitor(Reader reader, Writer writer) {
            super("ExecutorUnix.InputMonitor");			// NOI18N
            this.reader = reader;
            this.writer = writer;

            // see bug 4921071
            setPriority(1);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int nread = reader.read(buf, 0, BUFSZ);
                    if (nread == -1) {
                        break;
                    }
                    writer.write(buf, 0, nread);
                    writer.flush();
                }
                writer.close();
            } catch (IOException e) {
		// no-op
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
}
