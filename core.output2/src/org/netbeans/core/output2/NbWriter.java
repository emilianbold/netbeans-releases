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

package org.netbeans.core.output2;

import org.openide.windows.OutputWriter;
import org.openide.windows.OutputListener;
import java.io.IOException;
import org.openide.util.Exceptions;


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

    
    public void println(String s, OutputListener l, boolean important) throws IOException {
        ((OutWriter) out).println (s, l, important);
    }

    /**
     * Replaces the wrapped OutWriter.
     *
     * @throws IOException
     */
    public void reset() throws IOException {
        if (!((OutWriter) out).hasStorage() && !((OutWriter) out).isDisposed() || ((OutWriter) out).isEmpty()) {
            //Someone calling reset multiple times or on initialization
            if (!out().isDisposed()) {
                if (Controller.LOG) Controller.log ("Extra call to Reset on " + this + " for " + out);
                //#49173 - Clear action causes call to reset(); call to start writing
                //more output is another call to reset(), so it is ignored - so
                //the tab title is not updated when a new stream is updated.
                owner.setStreamClosed(false);
                return;
            }
        }
        synchronized (this) {
            if (out != null) {
                if (Controller.LOG) Controller.log ("Disposing old OutWriter");
                out().dispose();
            }
            if (Controller.LOG) Controller.log ("NbWriter.reset() replacing old OutWriter");
            out = new OutWriter(owner);
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

    @Override
    public void close() {
        boolean wasClosed = isClosed();
        if (Controller.LOG) Controller.log ("NbWriter.close wasClosed=" + wasClosed + " out is " + out + " out is closed " + ((OutWriter) out).isClosed());
        if (!wasClosed || !((OutWriter) out).isClosed()) {
            synchronized (lock) {
                try {
                    if (Controller.LOG) Controller.log ( "Now closing OutWriter");
                    out.close();
                    if (err != null) {
                        err.close();
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        boolean isClosed = isClosed();
        if (wasClosed != isClosed) {
            if (Controller.LOG) Controller.log ("Setting streamClosed on InputOutput to " + isClosed);
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
            if (Controller.LOG) Controller.log ("NbWriter.notifyErrClosed - error stream has been closed");
            owner.setStreamClosed(isClosed());
        }
    }
    
    /**
     * If not overridden, the super impl will append extra \n's
     */
    @Override
    public void println (String s) {
        synchronized (lock) {
            ((OutWriter) out).println(s);
        }
    }
}
