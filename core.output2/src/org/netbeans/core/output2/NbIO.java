/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 */

package org.netbeans.core.output2;

import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import org.openide.util.Exceptions;

/** Implementation of InputOutput.  Implements calls as a set of
 * "commands" which are passed up to Dispatcher to be run on the event
 * queue.
 *
 * @author  Tim Boudreau
 */
class NbIO implements InputOutput {

    private Boolean focusTaken = null;
    private Boolean closed = null;
    private final String name;
    
    private Action[] actions;

    private NbWriter out = null;

    private Icon icon;

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
        if (Controller.LOG) Controller.log("CLOSE INPUT OUTPUT CALLED FOR " + this);
        if (out != null) {
            if (Controller.LOG) Controller.log (" - Its output is non null, calling close() on " + out);
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
        if (Controller.LOG) Controller.log (this + ": IO " + getName() + " is being disposed");
        if (out != null) {
            if (Controller.LOG) Controller.log (this + ": Still has an OutWriter.  Disposing it");
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

    void setClosed (boolean val) {
        closed = val ? Boolean.TRUE : Boolean.FALSE;
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
        return out == null ? true : streamClosedSet ? streamClosed : false;
    }
    
    public void select() {
        if (Controller.LOG) Controller.log (this + ": select");
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
        if (Controller.LOG) Controller.log(NbIO.this + ": SetInputVisible");
        post (this, IOEvent.CMD_INPUT_VISIBLE, value);
    }
    
    public void setOutputVisible(boolean value) {
        //do nothing
    }

    boolean hasStreamClosed() {
        return streamClosedSet;
    }

    private boolean streamClosed = false;
    private boolean streamClosedSet = false;
    public void setStreamClosed(boolean value) {
        if (Controller.LOG) Controller.log ("setStreamClosed on " + this + " to " + value);
        if (streamClosed != value || !streamClosedSet) {
            streamClosed = value;
            streamClosedSet = true;
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

    boolean checkReset() {
        // XXX won't this always return false? -jglick
        boolean result = wasReset;
        wasReset = false;
        return result;
    }
    
    private boolean wasReset = false;
    public void reset() {
        if (Controller.LOG) Controller.log (this + ": reset");
        closed = null;
        streamClosedSet = false;
        streamClosed = false;

        if (in != null) {
            in.eof();
            in.reuse();
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
            if (Controller.LOG) Controller.log ("Synchronously dispatching " + evt + " from call on EQ");
            evt.dispatch();
        } else {
            if (Controller.LOG) Controller.log ("Asynchronously posting " + evt + " to EQ");
            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eq.postEvent(evt);
        }
    }
    
    @Override public String toString() {
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

    @SuppressWarnings("deprecation")
    public Reader flushReader() {
        return getIn();
    }    

    public void setIcon(Icon icn) {
        icon = icn;
        post (this, IOEvent.CMD_ICON, icn);
        
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    class IOReader extends Reader {
        private boolean pristine = true;
        IOReader() {
            super (new StringBuffer());
        }

        void reuse() {
             pristine = true;
             inputClosed = false;
        }

        private StringBuffer buffer() {
            return (StringBuffer) lock;
        }
        
        public void pushText (String txt) {
            if (Controller.LOG) Controller.log (NbIO.this + ": Input text: " + txt);
            synchronized (lock) {
                buffer().append (txt);
                lock.notifyAll();
            }
        }
        
        private boolean inputClosed = false;
        public void eof() {
            synchronized (lock) {
                try {
                    close();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
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
             if (Controller.LOG) Controller.log  (NbIO.this + ":Input read: " + off + " len " + len);
            checkPristine();
            synchronized (lock) {
                while (!inputClosed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw (IOException) new IOException("Interrupted: " +
                                                            e.getMessage()).initCause(e);
                    }
                }
                if (inputClosed) {
                    reuse();
                    return -1;
                }
                int realLen = Math.min (buffer().length(), len);
                buffer().getChars(0, realLen, cbuf, off);
                buffer().delete(0, realLen);
                return realLen;
            }
        }
        
        @Override
        public int read() throws IOException {
            if (Controller.LOG) Controller.log (NbIO.this + ": Input read one char");
            checkPristine();
            synchronized (lock) {
                while (!inputClosed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw (IOException) new IOException("Interrupted: " +
                                                            e.getMessage()).initCause(e);
                    }
                }
                if (inputClosed) {
                    reuse();
                    return -1;
                }
                int i = (int) buffer().charAt(0);
                buffer().deleteCharAt(0);
                return i;
            }
        }

        @Override
        public boolean ready() throws IOException {
            synchronized (lock) {
                if (inputClosed) {
                    reuse();
                    return false;
                }
                return buffer().length() > 0;
            }
        }
        
        @Override
        public long skip(long n) throws IOException {
            if (Controller.LOG) Controller.log (NbIO.this + ": Input skip " + n);
            checkPristine();
            synchronized (lock) {
                while (!inputClosed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw (IOException) new IOException("Interrupted: " +
                                                            e.getMessage()).initCause(e);
                    }
                }
                if (inputClosed) {
                    reuse();
                    return 0;
                }
                int realLen = Math.min (buffer().length(), (int) n);
                buffer().delete(0, realLen);
                return realLen;
            }
        }

        public void close() throws IOException {
            if (Controller.LOG) Controller.log (NbIO.this + ": Input close");
            setInputVisible(false);
            synchronized (lock) {
                inputClosed = true;
                buffer().setLength(0);
                lock.notifyAll();
            }
        }
        
        public boolean isClosed() {
            return inputClosed;
        }
    }
    
}
