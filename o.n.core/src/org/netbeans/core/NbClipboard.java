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

public final class NbClipboard extends ExClipboard implements LookupListener, AWTEventListener
{
    private Clipboard systemClipboard;
    private Convertor[] convertors;
    private Lookup.Result result;
    private boolean slowSystemClipboard;
    private Transferable last;

    public NbClipboard() {
        super("NBClipboard");   // NOI18N
        systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        result = Lookup.getDefault().lookup(new Lookup.Template(ExClipboard.Convertor.class));
        result.addLookupListener(this);
        resultChanged(null);

        if (System.getProperty("netbeans.slow.system.clipboard.hack") != null) // NOI18N
            slowSystemClipboard = Boolean.getBoolean("netbeans.slow.system.clipboard.hack"); // NOI18N
        else
            slowSystemClipboard = Utilities.isUnix();
        
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

    // XXX(-ttran) on Unix calling getContents() on the system clipboard is
    // very expensive, the call can take up to 1000 milliseconds.  We need to
    // examine the clipboard contents each time the Node is activated, the
    // method must be fast.  Therefore we cache the contents of system
    // clipboard and use the cache when someone calls getContents().  The cache
    // is sync'ed with the system clipboard when _any_ of our Windows gets
    // WINDOW_ACTIVATED event.  It means if some other apps modify the contents
    // of the system clipboard in the background then the change won't be
    // propagated to us immediately.  The other drawback is that if module code
    // bypasses NBClipboard and accesses the system clipboard directly then we
    // don't see these changes.

    public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
        // XXX(-dstrupl) the following line might lead to a double converted
        // transferable. Can be fixed as Jesse describes in #32485
        contents = convert(contents);

        if (slowSystemClipboard) {
            super.setContents(contents, owner);
        } else {
	    if (last != null) transferableOwnershipLost(last);
	    last = contents;
	}
        
        systemClipboard.setContents(contents, owner);
        fireClipboardChange();
    }

    public synchronized Transferable getContents(Object requestor) {
        try {
            if (slowSystemClipboard)
                return convert(super.getContents(requestor));
            else
                return convert(systemClipboard.getContents(requestor));
        }
        catch (ThreadDeath ex) {
            throw ex;
        }
        catch (Throwable ex) {
            return null;
        }
    }

    public void eventDispatched(AWTEvent ev) {
        if (!(ev instanceof WindowEvent))
            return;

        if (ev.getID() == WindowEvent.WINDOW_ACTIVATED) {
            try {
                Transferable transferable = systemClipboard.getContents(this);
                super.setContents(transferable, null);
            }
            catch (ThreadDeath ex) {
                throw ex;
            }
            catch (Throwable ignore) {
            }
        }
    }
}
