/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Ivan Soleimanipour.
 */

package org.netbeans.lib.terminalemulator;

import java.io.*;
import java.awt.*;
import javax.swing.SwingUtilities;

public class StreamTerm extends Term {

    static class Fmt {
	public static String pad(int what, int width) {
	    return pad("" + what, width);
	}
	public static String pad(byte what, int width) {
	    return pad("" + (char) what, width);
	}
	public static String pad0(String what, int width) {
	    return padwith(what, width, '0');
	}
	public static String pad(String what, int width) {
	    return padwith(what, width, ' ');
	}
	private static String padwith(String what, int width, char with) {
	    boolean left = false;
	    if (width < 0) {
		left = true;
		width = -width;
	    }
	    String sub;
	    if (what.length() > width)
		sub = what.substring(0, width);     // prevent overflow
	    else
		sub = what;
	    int pad = width - sub.length();
	    StringBuffer buf = new StringBuffer(sub);
	    if (left) {
		while(pad-- > 0)
		    buf.append(with);
	    } else {
		while(pad-- > 0)
		    buf.insert(0, with);
	    }
	    return new String(buf);
	}
    }

    private OutputStreamWriter writer;	// writes to child process

    /*
     * Return the OutputStreamWriter used for writing to the child.
     *
     * This can be used to send characters to the child process explicitly
     * as if they were typed at the keyboard.
     */
    public OutputStreamWriter getOutputStreamWriter() {
	return writer;
    } 

    public StreamTerm() {
	super();

	addInputListener(new TermInputListener() {
	    public void sendChars(char c[], int offset, int count) {
		if (writer == null)
		    return;
		try {
		    writer.write(c, offset, count);
		    writer.flush();
		} catch(Exception x) {
		    x.printStackTrace();
		} 
	    }

	    public void sendChar(char c) {
		if (writer == null)
		    return;
		try {
		    writer.write(c);
		    // writer is buffered, need to use flush!
		    // perhaps SHOULD use an unbuffered writer?
		    // Also fix send_chars()
		    writer.flush();
		} catch(Exception x) {
		    x.printStackTrace();
		} 
	    }
	} );
    }

    /*
     * Monitor output from process and forward to terminal
     */
    private class OutputMonitor extends Thread {
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
		System.out.print(Fmt.pad(cx, 4) + ": ");	// NOI18N
		for (int x = 0; x < width && cx < count; cx++, x++) {
		    String hex = Integer.toHexString(buf[offset+cx]);
		    System.out.print(Fmt.pad0(hex, 2) + " ");	// NOI18N
		}
		System.out.println();

		// print charcters
		cx = cx0;
		System.out.print("      ");	// NOI18N
		for (int x = 0; x < width && cx < count; cx++, x++) {
		    char c = (char) buf[offset+cx];
		    if (Character.isISOControl(c))
			c = ' ';
		    System.out.print(Fmt.pad((byte)c, 2) + " ");	// NOI18N
		}
		System.out.println();
	    } 
	}

	final private class Trampoline implements Runnable {
	    public int nread;
	    public void run() {
		term.putChars(buf, 0, nread);
	    }
	}

	public void run() {
	    Trampoline tramp = new Trampoline();

	    try {
		while(true) {
		    int nread = reader.read(buf, 0, BUFSZ);
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
		    if (debugInput())
			db_echo_receipt(buf, 0, nread);

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
	    } catch(Exception x) {
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

	// Now that we have a stream force resize notifications to be sent out.
	updateTtySize();

	if (pin != null)
	    writer = new OutputStreamWriter(pin);

	InputStreamReader out_reader = new InputStreamReader(pout);
	OutputMonitor out_monitor = new OutputMonitor(out_reader, this);
	out_monitor.start();

	if (perr != null) {
	    InputStreamReader err_reader = new InputStreamReader(perr);
	    OutputMonitor err_monitor = new OutputMonitor(err_reader, this);
	    err_monitor.start();
	}
    }

}
