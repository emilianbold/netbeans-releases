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

package org.netbeans.core;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExClipboard;

@org.openide.util.lookup.ServiceProviders({@org.openide.util.lookup.ServiceProvider(service=java.awt.datatransfer.Clipboard.class), @org.openide.util.lookup.ServiceProvider(service=org.openide.util.datatransfer.ExClipboard.class)})
public final class NbClipboard extends ExClipboard
implements LookupListener, Runnable, FlavorListener, AWTEventListener
{
    private Logger log;
    private Clipboard systemClipboard;
    private ExClipboard.Convertor[] convertors;
    private Lookup.Result<ExClipboard.Convertor> result;
    final boolean slowSystemClipboard;
    private Transferable last;
    private long lastWindowActivated;
    private long lastWindowDeactivated;
    private Reference<Object> lastWindowDeactivatedSource = new WeakReference<Object>(null);

    public NbClipboard() {
        //for unit testing
        this( Toolkit.getDefaultToolkit().getSystemClipboard() );
    }
    
    NbClipboard( Clipboard systemClipboard ) {
        super("NBClipboard");   // NOI18N
        this.systemClipboard = systemClipboard;
        log = Logger.getLogger("org.netbeans.core.NbClipboard"); // NOI18N

        result = Lookup.getDefault().lookupResult(ExClipboard.Convertor.class);
        result.addLookupListener(this);

        systemClipboard.addFlavorListener(this);

        resultChanged(null);

        if (System.getProperty("netbeans.slow.system.clipboard.hack") != null) {
            slowSystemClipboard = Boolean.getBoolean("netbeans.slow.system.clipboard.hack"); // NOI18N
        } else if (Utilities.isMac()) {
            slowSystemClipboard = false;
        }
        else {
            slowSystemClipboard = true;
        }




        if (!slowSystemClipboard) {
            if (System.getProperty("sun.awt.datatransfer.timeout") == null) { // NOI18N
                System.setProperty("sun.awt.datatransfer.timeout", "1000"); // NOI18N
            }
        } else {
            Toolkit.getDefaultToolkit().addAWTEventListener(
                this, AWTEvent.WINDOW_EVENT_MASK);
        }
    }
    
    protected synchronized ExClipboard.Convertor[] getConvertors () {
        return convertors;
    }

    public synchronized void resultChanged(LookupEvent ev) {
        Collection<? extends ExClipboard.Convertor> c = result.allInstances();
        ExClipboard.Convertor[] temp = new ExClipboard.Convertor[c.size()];
        convertors = c.toArray(temp);
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
        new RequestProcessor("System clipboard synchronizer").create(this, true); // NOI18N

    private Transferable data;
    private ClipboardOwner dataOwner;
    
    @Override
    public void setContents(Transferable contents, ClipboardOwner owner) {
        synchronized (this) {
            // XXX(-dstrupl) the following line might lead to a double converted
            // transferable. Can be fixed as Jesse describes in #32485
            if (log.isLoggable (Level.FINER)) {
                log.log (Level.FINER, "setContents called with: "); // NOI18N
                logFlavors (contents, Level.FINER, log.isLoggable(Level.FINEST));
            }
            contents = convert(contents);
            if (log.isLoggable (Level.FINER)) {
                log.log (Level.FINER, "After conversion:"); // NOI18N
                logFlavors (contents, Level.FINER, log.isLoggable(Level.FINEST));
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
        }
        fireClipboardChange();
    }

    @Override
    public Transferable getContents(Object requestor) {
        Transferable prev;

        try {
            log.log(Level.FINE, "getContents, slowSystemClipboard: {0}", slowSystemClipboard); // NOI18N
            if (slowSystemClipboard) {
                // The purpose of lastWindowActivated+100 is to ignore calls
                // which immediatelly follow WINDOW_ACTIVATED event.
                // This is workaround of JDK bug described in issue 41098.
                long curr = System.currentTimeMillis();
                if (lastWindowActivated != 0 && lastWindowActivated + 100 < curr) {
                    lastWindowActivated = 0;
                    
                    syncTask.schedule(0);
                    boolean finished = syncTask.waitFinished (100);
                    log.log(Level.FINE, "after syncTask wait, finished {0}", finished); // NOI18N
                } else {
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("no wait, last: " + lastWindowActivated + " now: " + curr); // NOI18N
                    }
                }
                
                
                prev = super.getContents (requestor);
            } else {
                syncTask.waitFinished ();
                log.log(Level.FINE, "after syncTask clipboard wait"); // NOI18N
                try {
                    prev = systemClipboard.getContents (requestor);
                } catch( IllegalStateException isE ) {
                    log.log (Level.INFO, "System clipboard not available.", isE); // NOI18N
                    prev = null;
                }
            }

            synchronized (this) {
                if (log.isLoggable (Level.FINE)) {
                    log.log (Level.FINE, "getContents by " + requestor); // NOI18N
                    logFlavors (prev, Level.FINE, log.isLoggable(Level.FINEST));
                }
                if (prev == null)  // if system clipboard has no contents
                    return null;

                Transferable res = convert (prev);
                if (log.isLoggable (Level.FINE)) {
                    log.log (Level.FINE, "getContents by " + requestor); // NOI18N
                    logFlavors (res, Level.FINE, log.isLoggable(Level.FINEST));

                    res = new LoggableTransferable (res);
                }
                return res;
            }
        } catch (ThreadDeath ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Logger.getLogger(NbClipboard.class.getName()).log(Level.WARNING, null, ex);
            return null;
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public void run() {
        Transferable cnts = null;
        ClipboardOwner ownr = null;

        log.fine("Running update");

        synchronized (this) {
            if (data != null) {
             cnts = data;
             ownr = dataOwner;
            }
            data = null;
            dataOwner = null;
        }
        if (cnts != null) {
            try {
                systemClipboard.setContents(cnts, ownr);
            } catch( IllegalStateException e ) {
                //#139616
                log.log (Level.FINE, "systemClipboard not available", e); // NOI18N
                data = cnts;
                dataOwner = ownr;
                syncTask.schedule(100);
                return;
            } 
            if (log.isLoggable (Level.FINE)) {
                log.log (Level.FINE, "systemClipboard updated:"); // NOI18N
                logFlavors (cnts, Level.FINE, log.isLoggable(Level.FINEST));
            }
            return;
        }

        try {
            Transferable transferable = systemClipboard.getContents(this);
            super.setContents(transferable, null);
            if (log.isLoggable (Level.FINE)) {
                log.log (Level.FINE, "internal clipboard updated:"); // NOI18N
                logFlavors (transferable, Level.FINE, log.isLoggable(Level.FINEST));
            }
            fireClipboardChange();
        }
        catch (ThreadDeath ex) {
            throw ex;
        }
        catch (Throwable ignore) {
        }
    }

    /** For testing purposes.
     */
    final void waitFinished () {
        syncTask.waitFinished ();
    }
    
    final void activateWindowHack (boolean reschedule) {
        // if WINDOW_DEACTIVATED is followed immediatelly with
        // WINDOW_ACTIVATED then it is JDK bug described in 
        // issue 41098.
        lastWindowActivated = System.currentTimeMillis();
        if (reschedule) {
            syncTask.schedule (0);
        }
    }
    
    private void logFlavors (Transferable trans, Level level, boolean content) {
        if (trans == null)
            log.log (level, "  no clipboard contents");
        else {
            java.awt.datatransfer.DataFlavor[] arr = trans.getTransferDataFlavors();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < arr.length; i++) {
                sb.append("  ").append(i).append(" = ").append(arr[i]);
                if (content) {
                    try {
                        sb.append(" contains: ").append(trans.getTransferData(arr[i]));
                    } catch (UnsupportedFlavorException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                sb.append("\n");
            }
            log.log (level, sb.toString());
        }
    }

    public void flavorsChanged(FlavorEvent e) {
        fireClipboardChange();
    }

    public void eventDispatched(AWTEvent ev) {
        if (!(ev instanceof WindowEvent))
            return;

        if (ev.getID() == WindowEvent.WINDOW_DEACTIVATED) {
            lastWindowDeactivated = System.currentTimeMillis();
            lastWindowDeactivatedSource = new WeakReference<Object>(ev.getSource());
        }
        if (ev.getID() == WindowEvent.WINDOW_ACTIVATED) {
            if (System.currentTimeMillis() - lastWindowDeactivated < 100 &&
                ev.getSource() == lastWindowDeactivatedSource.get()) {
                activateWindowHack (false);
            }
            if (log.isLoggable (Level.FINE)) {
                log.log (Level.FINE, "window activated scheduling update"); // NOI18N
            }
            syncTask.schedule(0);
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
            log.log (Level.FINE, "Request for flavor: " + flavor); // NOI18N
            Object res = delegate.getTransferData (flavor);
            log.log (Level.FINE, "Returning value: " + res); // NOI18N
            return res;
        }
        
        public DataFlavor[] getTransferDataFlavors () {
            return delegate.getTransferDataFlavors ();
        }
        
        public boolean isDataFlavorSupported (DataFlavor flavor) {
            boolean res = delegate.isDataFlavorSupported (flavor);
            log.log (Level.FINE, "isDataFlavorSupported: " + flavor + " result: " + res); // NOI18N
            return res;
        }
        
    }
}
