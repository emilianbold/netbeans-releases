/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 
 * Test output.
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class TestOut {

    private InputStream input;
    private PrintWriter output;
    private PrintWriter errput;
    private PrintWriter golden_output;
    private BufferedReader buffInput;
    private boolean autoFlushMode = true;

    /**
     * Constructor.
     */
    public TestOut(InputStream in, PrintStream out, PrintStream err) {
	this(in, out, err, null);
    }

    /**
     * Constructor.
     */
    public TestOut(InputStream in, PrintStream out, PrintStream err, PrintStream golden) {
	super();
	PrintWriter tout = null;
	if(out != null) {
	    tout = new PrintWriter(out);
	}
	PrintWriter terr = null;
	if(err != null) {
	    terr = new PrintWriter(err);
	}
	PrintWriter tgolden = null;
	if(golden != null) {
	    tgolden = new PrintWriter(golden);
	}
	initStreams(in, tout, terr, tgolden);
    }

    /**
     * Constructor.
     */
    public TestOut(InputStream in, PrintWriter out, PrintWriter err) {
	this(in, out, err, null);
    }

    /**
     * Constructor.
     */
    public TestOut(InputStream in, PrintWriter out, PrintWriter err, PrintWriter golden) {
	super();
	initStreams(in, out, err, golden);
	autoFlushMode = true;
    }

    /**
     * Creates unstance using System.in, System.out and System.err streams.
     */
    public TestOut() {
	this(System.in, 
	     new PrintWriter(System.out), 
	     new PrintWriter(System.err),
	     null);
    }

    /**
     * Creates output which does not print any message anywhere.
     */
    public static TestOut getNullOutput() {
	return(new TestOut((InputStream)null, (PrintWriter)null, (PrintWriter)null));
    }
    
    /**
     * @param autoFlushMode If true flush is invoking after any output.
     * @return Old value of the auto flush mode.
     */
    public boolean setAutoFlushMode(boolean autoFlushMode) {
	boolean oldValue = getAutoFlushMode();
	this.autoFlushMode = autoFlushMode;
	return(oldValue);
    }
    
    /**
     * @return Value of the auto flush mode.
     */
    public boolean getAutoFlushMode() {
	return(autoFlushMode);
    }

    /**
     * Read one byte from input.
     */
    public int read() throws IOException{
	if(input != null) {
	    return(input.read());
	} else {
	    return(-1);
	}
    }

    /**
     * Read a line from input.
     */
    public String readLine() throws IOException{
	if(buffInput != null) {
	    return(buffInput.readLine());
	} else {
	    return(null);
	}
    }

    /**
     * Prints a line into output.
     */
    public void print(String line) {
	if(output != null) {
	    output.print(line);
	    if(autoFlushMode) {
		output.flush();
	    }
	}
    }

    /**
     * Prints a line and then terminate the line by writing the line separator string.
     */
    public void printLine(String line) {
	if(output != null) {
	    output.println(line);
	    if(autoFlushMode) {
		output.flush();
	    }
	}
    }

    /**
     * Prints a line into golden output.
     */
    public void printGolden(String line) {
	if(golden_output != null) {
	    golden_output.println(line);
	    if(autoFlushMode) {
		golden_output.flush();
	    }
	}
    }

    /**
     * Prints a line into error output.
     */
    public void printErrLine(String line) {
	if(errput != null) {
	    errput.println(line);
	    if(autoFlushMode) {
		errput.flush();
	    }
	}
    }

    /**
     * Prints a line into either output or errput.
     * @param toOut If true prints a line into output.
     */
    public void printLine(boolean toOut, String line) {
	if(toOut) {
	    printLine(line);
	} else {
	    printErrLine(line);
	}
    }

    /**
     * Prints a trace line.
     */
    public void printTrace(String text) {
	printLine("Trace:");
	printLine(text);
    }

    /**
     * Prints a error line.
     */
    public void printError(String text) {
	printErrLine("Error:");
	printErrLine(text);
    }

    /**
     * Prints an exception stack trace.
     */
    public void printStackTrace(Throwable e) {
	if(errput != null) {
	    e.printStackTrace(errput);
	    if(autoFlushMode) {
		errput.flush();
	    }
	}
    }

    /**
     * Returns input stream.
     */
    public InputStream getInput() {
	return(input);
    }

    /**
     * Returns output writer.
     */
    public PrintWriter getOutput() {
	return(output);
    }

    /**
     * Returns errput writer.
     */
    public PrintWriter getErrput() {
	return(errput);
    }

    /**
     * Returns golden output writer.
     */
    public PrintWriter getGolden() {
	return(golden_output);
    }

    /**
     * Creates an output which prints only error messages.
     */
    public TestOut createErrorOutput() {
	return(new TestOut(null, null, getErrput()));
    }
    
    /*
     */
    public void flush() {
	if(output != null) {
	    output.flush();
	}
	if(errput != null) {
	    errput.flush();
	}
	if(golden_output != null) {
	    golden_output.flush();
	}	
    }

    private void initStreams(InputStream in, PrintWriter out, PrintWriter err, PrintWriter golden) {
	input = in;
	output = out;
	errput = err;
	golden_output = golden;
	if(input != null) {
	    buffInput = new BufferedReader(new InputStreamReader(in));
	} else {
	    buffInput = null;
	}
    }
}
