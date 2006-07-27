/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ErrWriter.java
 *
 * Created on May 9, 2004, 5:06 PM
 */

package org.netbeans.core.output2;

import java.util.logging.Logger;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import java.io.IOException;

/**
 * Wrapper OutputWriter for the standard out which marks its lines as being
 * stderr.
 *
 * @author  Tim Boudreau
 */
class ErrWriter extends OutputWriter {
    private OutWriter wrapped;
    private NbWriter parent;
    /** Creates a new instance of ErrWriter */
    ErrWriter(OutWriter wrapped, NbWriter parent) {
        super (new OutWriter.DummyWriter());
        this.wrapped = wrapped;
        this.parent = parent;
    }

    synchronized void setWrapped (OutWriter wrapped) {
        this.wrapped = wrapped;
        closed = true;
    }

    public void println(String s, OutputListener l) throws java.io.IOException {
        println(s, l, false);
    }

    public void println(String s, OutputListener l, boolean important) throws java.io.IOException {
        closed = false;
        synchronized (wrapped) {
            wrapped.println (s, l, important);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void reset() throws IOException {
        Logger.getAnonymousLogger().warning("Do not call reset() on the error io," +
        " only on the output IO.  Reset on the error io does nothing.");
        closed = false;
    }
    
    public void close() {
        if (!closed) {
            closed = true;
            parent.notifyErrClosed();
        }
    }

    boolean closed = true;
    boolean isClosed() {
        return closed;
    }

    public void flush() {
        wrapped.flush();
    }
    
    public boolean checkError() {
        return wrapped.checkError();
    }    
    
    public void write(int c) {
        closed = false;
        synchronized (wrapped) {
            wrapped.write(c);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void write(char buf[], int off, int len) {
        closed = false;
        synchronized (wrapped) {
            wrapped.write(buf, off, len);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void write(String s, int off, int len) {
        closed = false;
        synchronized (wrapped) {
            wrapped.write(s, off, len);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println(boolean x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println (x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }

    public void println(int x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println(char x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println(long x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println(float x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println(double x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println(char x[]) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println(String x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println(Object x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }

    public void print(char[] s) {
        closed = false;
        synchronized (wrapped) {
            wrapped.print(s);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }

    public void print(Object obj) {
        closed = false;
        synchronized (wrapped) {
            wrapped.print(obj);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }

    public void print(char c) {
        closed = false;
        synchronized (wrapped) {
            wrapped.print(c);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }

    public void print(int i) {
        closed = false;
        synchronized (wrapped) {
            wrapped.print(i);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }

    public void print(String s) {
        closed = false;
        synchronized (wrapped) {
            wrapped.print(s);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }

    public void print(boolean b) {
        closed = false;
        synchronized (wrapped) {
            wrapped.print(b);
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }
    
    public void println() {
        closed = false;
        synchronized (wrapped) {
            wrapped.println();
            ((AbstractLines) wrapped.getLines()).markErr();
        }
    }

    
}
