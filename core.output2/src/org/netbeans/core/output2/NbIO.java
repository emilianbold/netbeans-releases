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
/*
 * NbIO.java
 *
 * Created on February 27, 2004, 11:01 PM
 */

package org.netbeans.core.output2;

import org.openide.ErrorManager;
import org.openide.windows.OutputWriter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/** Implementation of InputOutput.  Implements calls as a set of 
 * "commands" which are passed up to Dispatcher to be run on the event
 * queue.
 *
 * @author  Tim Boudreau
 */
class NbIO implements CallbackInputOutput {

    private Boolean focusTaken = null;
    private Boolean closed = null;
    private String name;
    
    private Action[] actions;

    private NbWriter out = null;
    /** Creates a new instance of NbIO 
     * @param name The name of the IO
     * @param toolbarActions an optional set of toolbar actions
     */
    NbIO(String name, Action[] toolbarActions) {
        this(name);
        this.actions = toolbarActions;
    }
    
    /** Package private constructor for unit tests */
    NbIO (String name) {
        this.name = name;
    }
    
    public void closeInputOutput() {
        if (Controller.log) Controller.log("CLOSE INPUT OUTPUT CALLED FOR " + this);
        if (out != null) {
            if (Controller.log) Controller.log (" - Its output is non null, calling close() on " + out);
//            out.dispose();
            out.close();
        }
        post (this, IOEvent.CMD_CLOSE, true);
    }
    
    String getName() {
        return name;
    }
    
    public OutputWriter getErr() {
        return ((NbWriter) getOut()).getErr();
    }

    NbWriter writer() {
        return out;
    }

    void dispose() {
        if (Controller.log) Controller.log (this + ": IO " + getName() + " is being disposed");
        if (out != null) {
            if (Controller.log) Controller.log (this + ": Still has an OutWriter.  Disposing it");
            out().dispose();
            out = null;
            if (in != null) {
                in.eof();
                in = null;
            }
            focusTaken = null;
        }
        NbIOProvider.dispose(this);
    }
        
    public OutputWriter getOut() {
        synchronized (this) {
            if (out == null) {
                OutWriter realout = new OutWriter(this);
                out = new NbWriter(realout, this);
            }
        }
        return out;
    }
    
    /** Called by the view when polling */
    OutWriter out() {
        return out == null ? null : out.out();
    }
    
    public boolean isClosed() {
        return Boolean.TRUE.equals(closed);
    }

    public boolean isErrSeparated() {
        return false;
    }
    
    public boolean isFocusTaken() {
        return Boolean.TRUE.equals(focusTaken);
    }
    
    boolean isStreamClosed() {
        return out == null ? true : out.isClosed();
    }
    
    public void select() {
        if (Controller.log) Controller.log (this + ": select");
        post (this, IOEvent.CMD_SELECT, true);
    }
    
    public void setErrSeparated(boolean value) {
        //do nothing
    }
    
    public void setErrVisible(boolean value) {
        //do nothing
    }
    
    public void setFocusTaken(boolean value) {
        focusTaken = value ? Boolean.TRUE : Boolean.FALSE;
        post (this, IOEvent.CMD_FOCUS_TAKEN, value);
    }
    
    public void setInputVisible(boolean value) {
        if (Controller.log) Controller.log(NbIO.this + ": SetInputVisible");
        post (this, IOEvent.CMD_INPUT_VISIBLE, value);
    }
    
    public void setOutputVisible(boolean value) {
        //do nothing
    }

    private boolean streamClosed = true;
    public void setStreamClosed(boolean value) {
        if (streamClosed != isStreamClosed()) {
            streamClosed = value;
            post (this, IOEvent.CMD_STREAM_CLOSED, value);
        }
    }

    public void setToolbarActions(Action[] a) {
        this.actions = a;
        post (this, IOEvent.CMD_SET_TOOLBAR_ACTIONS, a);
    }

    Action[] getToolbarActions() {
        return actions;
    }

    public void reset() {
        if (Controller.log) Controller.log (this + ": reset");
        closed = null;

        if (in != null) {
            in.eof();
            in = null;
        }
        post (this, IOEvent.CMD_RESET, true);
    }
    
    private static void post (NbIO io, int command, boolean val) {
        IOEvent evt = new IOEvent (io, command, val);
        post (evt);
    }

    private static void post (NbIO io, int command, Object data) {
        IOEvent evt = new IOEvent (io, command, data);
        post (evt);
    }

    static void post (IOEvent evt) {
        if (SwingUtilities.isEventDispatchThread()) {
            if (Controller.log) Controller.log ("Synchronously dispatching " + evt + " from call on EQ");
            evt.dispatch();
        } else {
            if (Controller.log) Controller.log ("Asynchronously posting " + evt + " to EQ");
            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eq.postEvent(evt);
        }
    }
    
    public String toString() {
        return "NbIO@" + System.identityHashCode(this) + " " + getName();
    }

    IOReader in() {
        return in;
    }

    private IOReader in = null;
    public Reader getIn() {
        if (in == null) {
            in = new IOReader();
        }
        return in;
    }

    public Reader flushReader() {
        return getIn();
    }    
    
    class IOReader extends Reader {
        private boolean pristine = true;
        IOReader() {
            super (new StringBuffer());
        }
        
        private StringBuffer buffer() {
            return (StringBuffer) lock;
        }
        
        public void pushText (String txt) {
            if (Controller.log) Controller.log (NbIO.this + ": Input text: " + txt);
            synchronized (lock) {
                buffer().append (txt);
                lock.notifyAll();
            }
        }
        
        private boolean closed = false;
        public void eof() {
            synchronized (lock) {
                try {
                    close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        }
        
        private void checkPristine() throws IOException {
            if (SwingUtilities.isEventDispatchThread()) {
                throw new IOException ("Cannot call read() from the event thread, it will deadlock");
            }
            if (pristine) {
                NbIO.this.setInputVisible(true);
                pristine = false;
            }
        }
       
        public int read(char cbuf[], int off, int len) throws IOException {
             if (Controller.log) Controller.log  (NbIO.this + ":Input read: " + off + " len " + len);
            checkPristine();
            synchronized (lock) {
                while (!closed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        IOException ioe = new IOException ("Interrupted: " + e.getMessage());
                        ErrorManager.getDefault().annotate(ioe, e);
                        throw ioe;
                    }
                }
                if (closed) {
                    return -1;
                }
                int realLen = Math.min (buffer().length(), len);
                buffer().getChars(0, realLen, cbuf, off);
                buffer().delete(0, realLen);
                return realLen;
            }
        }
        
        public int read() throws IOException {
            if (Controller.log) Controller.log (NbIO.this + ": Input read one char");
            checkPristine();
            synchronized (lock) {
                while (!closed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        IOException ioe = new IOException ("Interrupted: " + e.getMessage());
                        ErrorManager.getDefault().annotate(ioe, e);
                        throw ioe;
                    }
                }
                if (closed) {
                    return -1;
                }
                int i = (int) buffer().charAt(0);
                buffer().deleteCharAt(0);
                return i;
            }
        }

        public boolean ready() throws IOException {        
            synchronized (lock) {
                return !closed && buffer().length() > 0;
            }
        }
        
        public long skip(long n) throws IOException {
            if (Controller.log) Controller.log (NbIO.this + ": Input skip " + n);
            checkPristine();
            synchronized (lock) {
                while (!closed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        IOException ioe = new IOException ("Interrupted: " + e.getMessage());
                        ErrorManager.getDefault().annotate(ioe, e);
                        throw ioe;
                    }
                }
                if (closed) {
                    return 0;
                }
                int realLen = Math.min (buffer().length(), (int) n);
                buffer().delete(0, realLen);
                return realLen;
            }
        }

        public void close() throws IOException {
            if (Controller.log) Controller.log (NbIO.this + ": Input close");
            setInputVisible(false);
            synchronized (lock) {
                closed = true;
                buffer().setLength(0);
                lock.notifyAll();
            }
        }
        
        public boolean isClosed() {
            return closed;
        }
    }
    
}
