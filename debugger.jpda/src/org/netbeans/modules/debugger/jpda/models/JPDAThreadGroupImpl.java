/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ThreadGroupReference;

import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
*/
public class JPDAThreadGroupImpl implements JPDAThreadGroup {
    
    private ThreadGroupReference tgr;
    private ThreadsTreeModel ttm;
    
    public JPDAThreadGroupImpl (ThreadGroupReference tgr, ThreadsTreeModel ttm) {
        this.tgr = tgr;
        this.ttm = ttm;
    }

    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    public JPDAThreadGroup getParentThreadGroup () {
        ThreadGroupReference ptgr = tgr.parent ();
        if (ptgr == null) return null;
        try {
            return (JPDAThreadGroup) ttm.translate (ptgr);
        } catch (UnknownTypeException e) {
            e.printStackTrace ();
            return null;
        }
    }
    
    public JPDAThread[] getThreads () {
        try {
            List l = tgr.threads ();
            int i, k = l.size ();
            JPDAThread[] ts = new JPDAThread [k];
            for (i = 0; i < k; i++)
                ts [i] = (JPDAThread) ttm.translate (l.get (i));
            return ts;
        } catch (UnknownTypeException e) {
            e.printStackTrace ();
            return new JPDAThread [0];
        }
    }
    
    public JPDAThreadGroup[] getThreadGroups () {
        try {
            List l = tgr.threadGroups ();
            int i, k = l.size ();
            JPDAThreadGroup[] ts = new JPDAThreadGroup [k];
            for (i = 0; i < k; i++)
                ts [i] = (JPDAThreadGroup) ttm.translate (l.get (i));
            return ts;
        } catch (UnknownTypeException e) {
            e.printStackTrace ();
            return new JPDAThreadGroup [0];
        }
    }
    
    public String getName () {
        return tgr.name ();
    }
    
    // XXX Add some synchronization so that the threads can not be resumed at any time
    public void resume () {
        tgr.resume ();
    }
    
    // XXX Add some synchronization
    public void suspend () {
        tgr.suspend ();
    }
    
}
