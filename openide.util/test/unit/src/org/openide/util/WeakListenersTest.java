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

package org.openide.util;

import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Vector;
import junit.framework.*;
import org.netbeans.junit.*;

public class WeakListenersTest extends NbTestCase {
    private static Thread activeQueueThread;
    
    public WeakListenersTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(WeakListenersTest.class));
    }
    
    protected void setUp () throws Exception {
        if (activeQueueThread == null) {
            class WR extends WeakReference implements Runnable {
                public WR (Object o) {
                    super (o, Utilities.activeReferenceQueue ());
                }
                public synchronized void run () {
                    activeQueueThread = Thread.currentThread();
                    notifyAll ();
                }
            }
            
            Object obj = new Object ();
            WR wr = new WR (obj);
            synchronized (wr) {
                obj = null;
                assertGC ("Has to be cleared", wr);
                // and has to execute run method
                while (activeQueueThread != null) {
                    wr.wait ();
                }
            }
        }
    }
    
    public void testOneCanCallHashCodeOrOnWeakListener () {
        Listener l = new Listener ();
        Object weak = WeakListeners.create (PropertyChangeListener.class, l, null);
        weak.hashCode ();
    }
    
    /** Useful for next test */
    interface X extends java.util.EventListener {
        public void invoke ();
    }
    /** Useful for next test */
    class XImpl implements X {
        public int cnt;
        public void invoke () {
            cnt++;
        }
    }
    public void testCallingMethodsWithNoArgumentWorks() {
        XImpl l = new XImpl ();
        X weak = (X)WeakListeners.create (X.class, l, null);
        weak.invoke ();
        assertEquals ("One invocation", 1, l.cnt);
    }

    public void testReleaseOfListenerWithNullSource () throws Exception {
        doTestReleaseOfListener (false);
    }
    
    public void testReleaseOfListenerWithSource () throws Exception {
        doTestReleaseOfListener (true);
    }
    
    private void doTestReleaseOfListener (final boolean source) throws Exception {   
        Listener l = new Listener ();
        
        class MyButton extends javax.swing.JButton {
            private Thread removedBy;
            private int cnt;
            
            public synchronized void removePropertyChangeListener (PropertyChangeListener l) {
                // notify prior
                if (source && cnt == 0) {
                    notifyAll ();
                    try {
                        // wait for 1
                        wait ();
                    } catch (InterruptedException ex) {
                        fail ("Not happen");
                    }
                }
                super.removePropertyChangeListener (l);
                removedBy = Thread.currentThread();
                cnt++;
                notifyAll ();
            }
            
            public synchronized void waitListener () throws Exception {
                int cnt = 0;
                while (removedBy == null) {
                    wait (500);
                    if (cnt++ == 5) {
                        fail ("Time out: removePropertyChangeListener was not called at all");
                    } else {
                        System.gc ();
                        System.runFinalization();
                    }
                }
            }
        }
        
        MyButton button = new MyButton ();
        java.beans.PropertyChangeListener weakL = WeakListeners.propertyChange (l, source ? button : null);
        button.addPropertyChangeListener(weakL);
        assertTrue ("Weak listener is there", Arrays.asList (button.getPropertyChangeListeners()).indexOf (weakL) >= 0);
        
        button.setText("Ahoj");
        assertEquals ("Listener called once", 1, l.cnt);
        
        WeakReference ref = new WeakReference (l);
        l = null;

        synchronized (button) {
            assertGC ("Can disappear", ref);
            
            if (source) {
                button.wait ();
                // this should not remove the listener twice
                button.setText ("Hoj");
                // go on (wait 1)
                button.notify ();
                
                button.waitListener ();
            } else {
                // trigger the even firing so weak listener knows from
                // where to unregister
                button.setText ("Hoj");
            }
            
            button.waitListener ();
            Thread.sleep (500);
        }

        assertEquals ("Weak listener has been removed", -1, Arrays.asList (button.getPropertyChangeListeners()).indexOf (weakL));
        assertEquals ("Button released from a thread", activeQueueThread, button.removedBy);
        assertEquals ("Unregister called just once", 1, button.cnt);
        
        // and because it is not here, it can be GCed
        WeakReference weakRef = new WeakReference (weakL);
        weakL = null;
        assertGC ("Weak listener can go away as well", weakRef);
    }
    
    
    public void testSourceCanBeGarbageCollected () {
        javax.swing.JButton b = new javax.swing.JButton ();
        Listener l = new Listener ();
        
        b.addPropertyChangeListener (WeakListeners.propertyChange (l, b));
        
        WeakReference ref = new WeakReference (b);
        b = null;
        
        assertGC ("Source can be GC", ref);
    }
    
    public void testNamingListenerBehaviour () throws Exception {
        Listener l = new Listener ();
        ImplEventContext c = new ImplEventContext ();
        javax.naming.event.NamingListener weakL = (javax.naming.event.NamingListener)WeakListeners.create (
            javax.naming.event.ObjectChangeListener.class,
            javax.naming.event.NamingListener.class,
            l,
            c
        );
        
        c.addNamingListener("", javax.naming.event.EventContext.OBJECT_SCOPE, weakL);
        assertEquals ("Weak listener is there", weakL, c.listener);
        
        WeakReference ref = new WeakReference (l);
        l = null;

        synchronized (c) {
            assertGC ("Can disappear", ref);
            c.waitListener ();
        }
        assertNull ("Listener removed", c.listener);
    }
    
    public void testExceptionIllegalState () {
        Listener l = new Listener ();
        try {
            WeakListeners.create (PropertyChangeListener.class, javax.naming.event.NamingListener.class, l, null);
            fail ("This shall not be allowed as NamingListener is not superclass of PropertyChangeListener");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        
        try {
            WeakListeners.create (Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        
        try {
            WeakListeners.create (Object.class, Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        
        try {
            WeakListeners.create (PropertyChangeListener.class, Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
    }
    
    public void testHowBigIsWeakListener () throws Exception {
        Listener l = new Listener ();
        javax.swing.JButton button = new javax.swing.JButton ();
        ImplEventContext c = new ImplEventContext ();
        
        Object[] ignore = new Object[] {
            l, 
            button,
            c,
            Utilities.activeReferenceQueue()
        };
        
        
        PropertyChangeListener pcl = WeakListeners.propertyChange(l, button);
        assertSize ("Not too big (plus 32 from ReferenceQueue)", java.util.Collections.singleton (pcl), 112, ignore);
        
        Object ocl = WeakListeners.create (javax.naming.event.ObjectChangeListener.class, javax.naming.event.NamingListener.class, l, c);
        assertSize ("A bit bigger (plus 32 from ReferenceQueue)", java.util.Collections.singleton (ocl), 128, ignore);
        
        Object nl = WeakListeners.create (javax.naming.event.NamingListener.class, l, c);
        assertSize ("The same (plus 32 from ReferenceQueue)", java.util.Collections.singleton (nl), 128, ignore);
        
    }

    public void testPrivateRemoveMethod() throws Exception {
        PropChBean bean = new PropChBean();
        Listener listener = new Listener();
        PCL weakL = (PCL) WeakListeners.create(PCL.class, listener, bean);
        WeakReference ref = new WeakReference(listener);
        
        bean.addPCL(weakL);
        
        bean.listeners.firePropertyChange (null, null, null);
        assertEquals ("One call to the listener", 1, listener.cnt);
        listener.cnt = 0;
        
        listener = null;
        assertGC("Listener wasn't GCed", ref);
        
        ref = new WeakReference(weakL);
        weakL = null;
        assertGC("WeakListener wasn't GCed", ref);
        
        // this shall enforce the removal of the listener
        bean.listeners.firePropertyChange (null, null, null);
        
        assertEquals ("No listeners", 0, bean.listeners.getPropertyChangeListeners ().length);
    }
    
    private static final class Listener 
    implements java.beans.PropertyChangeListener, javax.naming.event.ObjectChangeListener {
        public int cnt;
        
        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            cnt++;
        }
        
        public void namingExceptionThrown(javax.naming.event.NamingExceptionEvent evt) {
            cnt++;
        }
        
        public void objectChanged(javax.naming.event.NamingEvent evt) {
            cnt++;
        }
    } // end of Listener
    
    private static final class ImplEventContext extends javax.naming.InitialContext 
    implements javax.naming.event.EventContext {
        public javax.naming.event.NamingListener listener;
        
        public ImplEventContext () throws Exception {
        }
        
        public void addNamingListener(javax.naming.Name target, int scope, javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertNull (listener);
            listener = l;
        }
        
        public void addNamingListener(String target, int scope, javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertNull (listener);
            listener = l;
        }
        
        public synchronized void removeNamingListener(javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertEquals ("Removing the same listener", listener, l);
            listener = null;
            notifyAll ();
        }
        
        public boolean targetMustExist() throws javax.naming.NamingException {
            return false;
        }
        
        public synchronized void waitListener () throws Exception {
            int cnt = 0;
            while (listener != null) {
                wait (500);
                if (cnt++ == 5) {
                    fail ("Time out: removeNamingListener was not called at all");
                } else {
                    System.gc ();
                    System.runFinalization();
                }
            }
        }
        
    }
    
    private static class PropChBean {
        private java.beans.PropertyChangeSupport listeners = new java.beans.PropertyChangeSupport (this);
        private void addPCL(PCL l) { listeners.addPropertyChangeListener (l); }
        private void removePCL(PCL l) { listeners.removePropertyChangeListener (l); }
    } // End of PropChBean class

    // just a marker, its name will be used to construct the name of add/remove methods, e.g. addPCL, removePCL
    private static interface PCL extends PropertyChangeListener {
    } // End of PrivatePropL class
}
