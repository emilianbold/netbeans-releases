/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2003.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick, David Konecny
 */
 
package org.apache.tools.ant.module.spi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.StringBuffer;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.run.AntOutputParser;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** OutputStream for wrapping output of Ant task and capable of
 * parsing Ant output.
 *
 * @since 2.15
 */
public abstract class AntOutputStream extends OutputStream {

    /** buffer which will be used for the next line */
    private StringBuffer buffer = new StringBuffer (1000);
    /** have we printed any lines yet? used to prevent initial blank line */
    private boolean hadFirst = false;
	    
    private AntOutputParser antOutputParser = AntOutputParser.getDefault();

    final public void close() throws IOException {
        flush ();
        handleClose();
    }

    /**
     * This method is called when the stream is closed and it allows
     * entensions of this class to do additional tasks.
     * For example, closing an underlying stream, etc.
     * The default implementation does nothing.
     */
    protected void handleClose() throws IOException {
    }

    final public void flush() throws IOException {
        flushLines (true);
    }

    final public void write(byte[] b) throws IOException {
        write (b, 0, b.length);
    }

    final public void write(byte[] b, int offset, int length) throws IOException {
        buffer.append (new String (b, offset, length));
        // Will usually contain at least one newline:
        flushLines (false);
    }

    final public void write(int b) throws IOException {
        buffer.append ((char) b);
        if ((char) b == '\n') {
            flushLines (false);
        }
    }

    private void flushLines (boolean flushEverything) throws IOException {
        // Not as efficient as it could be, surely, but keep it simple for now:
        //System.err.println("flushLines: buffer=" + buffer);
    MAIN:
        while (true) {
            int len = buffer.length ();
            for (int i = 0; i < len; i++) {
                if (buffer.charAt (i) == '\n') {
                    //System.err.println("flushing; i=" + i);
                    // For Windows:
                    int end = i;
                    if (end > 0 && buffer.charAt (end - 1) == '\r') {
                        end--;
                    }
                    flushLine (buffer.substring (0, end));
                    buffer.delete (0, i + 1);
                    continue MAIN;
                }
            }
            //System.err.println("not found");
            break MAIN;
        }
        if (flushEverything) {
            flushLine(buffer.substring (0, buffer.length()));
            buffer.delete(0, buffer.length());
        }
    }

    private void flushLine (String l) throws IOException {
        //System.err.println("flushing: " + l);
        if (! hadFirst) {
            hadFirst = true;
            // Do not print an initial blank line.
            if (l.trim ().length () == 0) {
                return;
            }
        }
        AntOutputParser.Result r = antOutputParser.parse(l);
        if (r == null) {
            writeLine(l);
        } else {
            writeLine(l, r.getFileObject(), r.getLineStart(), r.getColumnStart(), r.getLineEnd(), r.getColumnEnd(), r.getMessage());
        }
    }

    /** Write one line of the parsed text. All line and column parameters can be -1 what means
    * that value was not available or parsing was not successful.
    * @param line original text of the line
    * @param file file object for which this line was generated
    * @param line1 starting line of the message
    * @param col1 starting column of the message
    * @param line2 ending line of the message
    * @param col2 ending column of the message
    * @param message message 
    */
    abstract protected void writeLine(String line, FileObject file, int line1, int col1, int line2, int col2, String message) throws IOException;

    /** Write one line of text which was not parsed.
     */
    abstract protected void writeLine(String line) throws IOException;

    /** Create well formated message from the parsed information.
     */
    protected String formatMessage(String fileName, String message, int line1, int col1, int line2, int col2) {
        String m = (message != null ? message : NbBundle.getMessage (AntOutputStream.class, "ERR_unknown"));
        fileName = fileName.replace('\\', File.separatorChar);
        fileName = fileName.replace('/', File.separatorChar);
        if (line1 == -1) {
            return NbBundle.getMessage
                (AntOutputStream.class, "MSG_err", fileName, m); // NOI18N
        } else {
            if (col1 == -1) {
                return NbBundle.getMessage
                    (AntOutputStream.class, "MSG_err_line", fileName, m, // NOI18N
                     new Integer (line1 + 1));
            } else {
                if (line2 == -1 || col2 == -1 || (line1 == line2 && col1 == col2)) {
                    return NbBundle.getMessage
                        (AntOutputStream.class, "MSG_err_line_col", // NOI18N
                         new Object[] { fileName, m, new Integer (line1 + 1), new Integer (col1 + 1) });
                } else {
                    if (line1 == line2) {
                        return NbBundle.getMessage
                            (AntOutputStream.class, "MSG_err_line_col_col", // NOI18N
                             new Object[] { fileName, m, new Integer (line1 + 1), new Integer (col1 + 1), new Integer (col2 + 1) });
                    } else {
                        return NbBundle.getMessage
                            (AntOutputStream.class, "MSG_err_line_col_line_col", // NOI18N
                             new Object[] { fileName, m, new Integer (line1 + 1), new Integer (col1 + 1), new Integer (line2 + 1), new Integer (col2 + 1) });
                    }
                }
            }
        }
    }
    
}
