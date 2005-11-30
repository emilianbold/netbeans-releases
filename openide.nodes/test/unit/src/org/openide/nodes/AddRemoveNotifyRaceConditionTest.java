/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;
import org.openide.ErrorManager;

import org.netbeans.junit.*;
import org.openide.util.RequestProcessor;


public class AddRemoveNotifyRaceConditionTest extends NbTestCase {
    public AddRemoveNotifyRaceConditionTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testChildrenCanBeSetToNullIfGCKicksIn () throws Exception {
        Keys k = new Keys();
        AbstractNode n = new AbstractNode(k);
        
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Ok, one", 1, arr.length);
        final Reference ref = new SoftReference(arr[0]);
        arr = null;
        
        class R implements Runnable {
            public void run() {
                ErrorManager.getDefault().log("Ready to GC");
                try {
                    assertGC("Node can go away in the worst possible moment", ref);
                } catch (Throwable t) {
                    // ok, may not happen
                }
                ErrorManager.getDefault().log("Gone");
                System.runFinalization();
                System.runFinalization();
                
            }
        }
        k.run = new R();
        
        int cnt = n.getChildren().getNodes(true).length;
        
        assertEquals("Count is really one", 1, cnt);
    }

    private static class Keys extends Children.Keys implements Runnable {
        private Runnable run;
        private int removeNotify;
        private RequestProcessor.Task task = new RequestProcessor("blbni").create(this);
        
        protected void addNotify() {
            task.schedule(0);
        }
        
        public void run() {
            setKeys(Collections.singleton(new Integer(1)));
            if (run != null) {
                run.run();
                return;
            }
        }

        protected void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }

        protected Node[] createNodes(Object key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName(key.toString());
            return new Node[] { an };
        }
        
        public Node[] getNodes(boolean optimalResult) {
            if (optimalResult) {
                task.schedule(0);
                task.waitFinished();
            }
            Node[] res = getNodes();
            // they are no longer needed
            return res;
        }        
    }
}