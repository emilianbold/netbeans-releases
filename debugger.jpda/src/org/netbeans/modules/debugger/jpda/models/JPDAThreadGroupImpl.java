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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.util.Iterator;

import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * The implementation of JPDAThreadGroup.
 */
public class JPDAThreadGroupImpl implements JPDAThreadGroup {

    private ThreadGroupReference tgr;
    private JPDADebuggerImpl debugger;
    
    public JPDAThreadGroupImpl (ThreadGroupReference tgr, JPDADebuggerImpl debugger) {
        this.tgr = tgr;
        this.debugger = debugger;
    }

    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    public JPDAThreadGroup getParentThreadGroup () {
        ThreadGroupReference ptgr = tgr.parent ();
        if (ptgr == null) return null;
        return debugger.getThreadGroup(ptgr);
    }
    
    public JPDAThread[] getThreads () {
        List l = tgr.threads ();
        int i, k = l.size ();
        JPDAThread[] ts = new JPDAThread [k];
        for (i = 0; i < k; i++)
            ts [i] = debugger.getThread((ThreadReference) l.get (i));
        return ts;
    }
    
    public JPDAThreadGroup[] getThreadGroups () {
        List l = tgr.threadGroups ();
        int i, k = l.size ();
        JPDAThreadGroup[] ts = new JPDAThreadGroup [k];
        for (i = 0; i < k; i++)
            ts [i] = debugger.getThreadGroup((ThreadGroupReference) l.get (i));
        return ts;
    }
    
    public String getName () {
        return tgr.name ();
    }
    
    // XXX Add some synchronization so that the threads can not be resumed at any time
    public void resume () {
        List threads = tgr.threads();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            JPDAThreadImpl thread = (JPDAThreadImpl) debugger.getThread((ThreadReference) it.next());
            thread.notifyToBeResumed();
        }
        tgr.resume ();
    }
    
    // XXX Add some synchronization
    public void suspend () {
        tgr.suspend ();
        List threads = tgr.threads();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            JPDAThreadImpl thread = (JPDAThreadImpl) debugger.getThread((ThreadReference) it.next());
            thread.notifySuspended();
        }
    }
    
}
