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

package org.netbeans.core;

import java.awt.Toolkit;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.awt.datatransfer.*;
import java.util.Collection;

import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.RequestProcessor;

public final class NbClipboard extends ExClipboard
    implements LookupListener, AWTEventListener, Runnable
{
    private org.openide.ErrorManager log;
    private Clipboard systemClipboard;
    private Convertor[] convertors;
    private Lookup.Result result;
    private boolean slowSystemClipboard;
    private Transferable last;

    public NbClipboard() {
        super("NBClipboard");   // NOI18N
        systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        log = org.openide.ErrorManager.getDefault ().getInstance ("org.netbeans.core.NbClipboard"); // NOI18N

        result = Lookup.getDefault().lookup(new Lookup.Template(ExClipboard.Convertor.class));
        result.addLookupListener(this);
        resultChanged(null);

        if (System.getProperty("netbeans.slow.system.clipboard.hack") != null) // NOI18N
            slowSystemClipboard = Boolean.getBoolean("netbeans.slow.system.clipboard.hack"); // NOI18N
        else
            slowSystemClipboard = true;
        
        if (slowSystemClipboard) {
            Toolkit.getDefaultToolkit().addAWTEventListener(
                this, AWTEvent.WINDOW_EVENT_MASK);
        }
    }
    
    protected synchronized Convertor[] getConvertors () {
        return convertors;
    }

    public synchronized void resultChanged(LookupEvent ev) {
        Collection c = result.allInstances();
        Convertor[] temp = new Convertor[c.size()];
        convertors = (Convertor[]) c.toArray(temp);
    }

    // XXX(-ttran) on Unix (and also on Windows as we discovered recently)
    // calling getContents() on the system clipboard is very expensive, the
    // call can take up to 1000 milliseconds.  We need to examine the clipboard
    // contents each time the Node is activated, the method must be fast.
    // Therefore we cache the contents of system clipboard and use the cache
    // when someone calls getContents().  The cache is sync'ed with the system
    // clipboard when _any_ of our Windows gets WINDOW_ACTIVATED event.  It
    // means if some other apps modify the contents of the system clipboard in
    // the background then the change won't be propagated to us immediately.
    // The other drawback is that if module code bypasses NBClipboard and
    // accesses the system clipboard directly then we don't see these changes.
    //
    // The other problem is an AWT bug
    // 
    // http://developer.java.sun.com/developer/bugParade/bugs/4818143.html
    //
    // sun.awt.datatransfer.ClipboardTransferable.getClipboardData() can hang
    // for very long time (maxlong == eternity).  We tries to avoid the hang by
    // access the system clipboard from a separate thread.  If the hang happens
    // the thread will wait for the system clipboard forever but not the whole
    // IDE

    private RequestProcessor.Task syncTask =
        new RequestProcessor("System clipboard synchronizer").create(this); // NOI18N

    private Transferable data;
    private ClipboardOwner dataOwner;
    
    public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
        // XXX(-dstrupl) the following line might lead to a double converted
        // transferable. Can be fixed as Jesse describes in #32485
        if (log.isLoggable (log.INFORMATIONAL)) {
            log.log (log.INFORMATIONAL, "setContents called with: "); // NOI18N
            logFlavors (contents.getTransferDataFlavors ());
        }
        contents = convert(contents);
        if (log.isLoggable (log.INFORMATIONAL)) {
            log.log (log.INFORMATIONAL, "After conversion:"); // NOI18N
            logFlavors (contents.getTransferDataFlavors ());
        }

        if (slowSystemClipboard) {
            super.setContents(contents, owner);
        } else {
	    if (last != null) transferableOwnershipLost(last);
	    last = contents;
	}

        data = contents;
        dataOwner = owner;
        syncTask.schedule(0);
        fireClipboardChange();
    }

    public Transferable getContents(Object requestor) {
        Transferable prev;

        try {
            if (slowSystemClipboard) {
                prev = super.getContents (requestor);
            } else {
                syncTask.waitFinished ();
                prev = systemClipboard.getContents (requestor);
            }

            synchronized (this) {
                if (log.isLoggable (log.INFORMATIONAL)) {
                    log.log (log.INFORMATIONAL, "getContents by " + requestor); // NOI18N
                    logFlavors (prev.getTransferDataFlavors ());
                }
                Transferable res = convert (prev);
                if (log.isLoggable (log.INFORMATIONAL)) {
                    log.log (log.INFORMATIONAL, "getContents by " + requestor); // NOI18N
                    logFlavors (res.getTransferDataFlavors ());

                    res = new LoggableTransferable (res);
                }
                return res;
            }
        } catch (ThreadDeath ex) {
            throw ex;
        } catch (Throwable ex) {
            return null;
        }
    }

    public void run() {
        Transferable contents = null;
        ClipboardOwner owner = null;

        synchronized (this) {
            if (data != null) {
             contents = data;
             owner = dataOwner;
            }
            data = null;
            dataOwner = null;
        }
        if (contents != null) {
            systemClipboard.setContents(contents, owner);
            return;
        }

        try {
            Transferable transferable = systemClipboard.getContents(this);
            super.setContents(transferable, null);
            fireClipboardChange();
        }
        catch (ThreadDeath ex) {
            throw ex;
        }
        catch (Throwable ignore) {
        }
    }


    public void eventDispatched(AWTEvent ev) {
        if (!(ev instanceof WindowEvent))
            return;

        if (ev.getID() == WindowEvent.WINDOW_ACTIVATED) {
            syncTask.schedule(0);
        }
    }
    
    private void logFlavors (java.awt.datatransfer.DataFlavor[] arr) {
        for (int i = 0; i < arr.length; i++) {
            log.log (log.INFORMATIONAL, "  " + i + " = " + arr[i]);
        }
    }
    
    /** Transferable that logs operations on itself.
     */
    private final class LoggableTransferable implements Transferable {
        private Transferable delegate;
        
        public LoggableTransferable (Transferable delegate) {
            this.delegate = delegate;
        }
        public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException {
            log.log (log.INFORMATIONAL, "Request for flavor: " + flavor); // NOI18N
            Object res = delegate.getTransferData (flavor);
            log.log (log.INFORMATIONAL, "Returning value: " + res); // NOI18N
            return res;
        }
        
        public DataFlavor[] getTransferDataFlavors () {
            return delegate.getTransferDataFlavors ();
        }
        
        public boolean isDataFlavorSupported (DataFlavor flavor) {
            boolean res = delegate.isDataFlavorSupported (flavor);
            log.log (log.INFORMATIONAL, "isDataFlavorSupported: " + flavor + " result: " + res); // NOI18N
            return res;
        }
        
    }
}
