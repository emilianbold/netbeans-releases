/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.windows;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.openide.util.io.NullOutputStream;
import org.openide.util.io.NullInputStream;

/** An I/O connection to one tab on the Output Window.
 * <p>
 * Note: take also a look at the class OutputWriter. The method to clean
 * an Output tab resides there.
 * 
 * @see OutputWriter
 * @author   Ian Formanek, Jaroslav Tulach, Petr Hamernik, Ales Novak, Jan Jancura
 */
public interface InputOutput {


    /** Null InputOutput */
    /*public static final*/ InputOutput NULL = new InputOutput$Null();

    /** Acquire an output writer to write to the tab.
    * This is the usual use of a tab--it writes to the main output pane.
    * @return the writer
    */
    public OutputWriter getOut();

    /** Get a reader to read from the tab.
    * If a reader is ever requested, an input line is added to the
    * tab and used to read one line at a time.
    * @return the reader
    */
    public Reader getIn();

    /** Get an output writer to write to the tab in error mode.
    * This might show up in a different color than the regular output, e.g., or
    * appear in a separate pane.
    * @return the writer
    */
    public OutputWriter getErr();

    /** Closes this tab. */
    public void closeInputOutput();

    /** Test whether this tab is closed.
    * @see #closeInputOutput
    * @return <code>true</code> if it is closed
    */
    public boolean isClosed();

    /** Show or hide the standard output pane.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setOutputVisible(boolean value);

    /** Show or hide the error pane.
    * If the error is mixed into the output, this may not be useful.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setErrVisible(boolean value);

    /** Show or hide the input line.
    * @param value <code>true</code> to show, <code>false</code> to hide
    */
    public void setInputVisible(boolean value);

    /**
    * Make this pane visible.
    * For example, may select this tab in a multi-window.
    */
    public void select ();

    /** Test whether the error output is mixed into the regular output or not.
    * @return <code>true</code> if separate, <code>false</code> if mixed in
    */
    public boolean isErrSeparated();

    /** Set whether the error output should be mixed into the regular output or not.
    * @return <code>true</code> to separate, <code>false</code> to mix in
    */
    public void setErrSeparated(boolean value);

    /** Test whether the output window takes focus when anything is written to it.
    * @return <code>true</code> if so
    */
    public boolean isFocusTaken();

    /** Set whether the output window should take focus when anything is written to it.
    * @return <code>true</code> to take focus
    */
    public void setFocusTaken(boolean value);

    /** Flush pending data in the input-line's reader.
    * Called when the reader is about to be reused.
    * @return the flushed reader
    */
    public Reader flushReader();

    /** @deprecated Use {@link #NULL} instead. */
    /*public static final*/ Reader nullReader = new InputStreamReader(new NullInputStream());

    /** @deprecated Use {@link #NULL} instead. */
    /*public static final*/ OutputWriter nullWriter = new InputOutput$NullOutputWriter();

}

final class InputOutput$Null extends Object implements InputOutput {
    public InputOutput$Null () {
    }
    
    public OutputWriter getOut() {
        return nullWriter;
    }
    public Reader getIn() {
        return nullReader;
    }
    public OutputWriter getErr() {
        return nullWriter;
    }
    public void closeInputOutput() {
    }
    public boolean isClosed() {
        return true;
    }
    public void setOutputVisible(boolean value) {
    }
    public void setErrVisible(boolean value) {
    }
    public void setInputVisible(boolean value) {
    }
    public void select () {
    }
    public boolean isErrSeparated() {
        return false;
    }
    public void setErrSeparated(boolean value) {
    }
    public boolean isFocusTaken() {
        return false;
    }
    public void setFocusTaken(boolean value) {
    }
    public Reader flushReader() {
        return nullReader;
    }
}

final class InputOutput$NullOutputWriter extends OutputWriter {
    InputOutput$NullOutputWriter() {
        super(new OutputStreamWriter(new NullOutputStream()));
    }
    public void reset() {
    }
    public void println(String s, OutputListener l) {
    }
}

