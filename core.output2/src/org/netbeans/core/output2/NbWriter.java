/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.output2;

import org.openide.windows.OutputWriter;
import org.openide.windows.OutputListener;
import org.openide.ErrorManager;

import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.io.IOException;


/**
 * Wrapper around a replacable instance of OutWriter.  An OutWriter can be disposed on any thread, but it may
 * still be visible in the GUI until the tab change gets handled in the EDT;  also, a writer once obtained
 * should be reusable, but OutWriter is useless once it has been disposed.  So this class wraps an OutWriter,
 * which it replaces when reset() is called;  an OutputDocument is implemented directly over an 
 * OutWriter, so the immutable OutWriter lasts until the OutputDocument is destroyed.
 */
class NbWriter extends OutputWriter {
    private final NbIO owner;
    /**
     * Make an output writer.
     */
    public NbWriter(OutWriter real, NbIO owner) {
        super(real);
        this.owner = owner;
    }

    public void println(String s, OutputListener l) throws IOException {
        ((OutWriter) out).println (s, l);
    }

    /**
     * Replaces the wrapped OutWriter.
     *
     * @throws IOException
     */
    public void reset() throws IOException {
        if (!((OutWriter) out).hasStorage() && !((OutWriter) out).isDisposed() || ((OutWriter) out).isEmpty()) {
            //Someone calling reset multiple times or on initialization
            if (!((OutWriter)out).isDisposed()) {
                if (Controller.log) Controller.log ("Extra call to Reset on " + this + " for " + out);
                return;
            }
        }
        synchronized (this) {
            out = new OutWriter();
            lock = out;
            if (err != null) {
                err.setWrapped((OutWriter) out);
            }
            owner.reset();
        }
    }

    OutWriter out() {
        return (OutWriter) out;
    }
    
    ErrWriter err() {
        return err;
    }

    private ErrWriter err = null;
    public synchronized ErrWriter getErr() {
        if (err == null) {
            err = new ErrWriter ((OutWriter) out, this);
        }
        return err;
    }

    public void close() {
        boolean wasClosed = isClosed();
        if (!wasClosed) {
            synchronized (lock) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify (ioe);
                }
            }
        }
        boolean isClosed = isClosed();
        if (wasClosed != isClosed) {
            owner.setStreamClosed(isClosed);
        }
    }

    public boolean isClosed() {
        OutWriter ow = (OutWriter) out;
        synchronized (ow) {
            boolean result = ow.isClosed();
            if (result && err != null && !(ow.checkError())) {
                result &= err.isClosed();
            }
            return result;
        }
    }

    public void notifyErrClosed() {
        if (isClosed()) {
            owner.setStreamClosed(isClosed());
        }
    }
    
    /**
     * If not overridden, the super impl will append extra \n's
     */
    public void println (String s) {
        synchronized (lock) {
            ((OutWriter) out).println(s);
        }
    }
}
