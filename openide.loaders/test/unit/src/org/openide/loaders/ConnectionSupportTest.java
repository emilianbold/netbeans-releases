/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import junit.framework.*;
import java.io.*;
import java.util.*;
import org.openide.ErrorManager;
import org.openide.cookies.ConnectionCookie;
import org.openide.filesystems.*;
import org.openide.nodes.Node;

/** Some tests for the ConnectionSupport
 *
 * @author Jaroslav Tulach
 */
public class ConnectionSupportTest extends TestCase {
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.loaders.ConnectionSupportTest$Lkp"); // NOI18N
    }
    
    public ConnectionSupportTest (String testName) {
        super (testName);
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    protected void setUp () throws java.lang.Exception {
    }

    protected void tearDown () throws java.lang.Exception {
    }

    public static junit.framework.Test suite () {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(ConnectionSupportTest.class);
        
        return suite;
    }

    public void testFireEvent () throws Exception {
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        FileObject fo = FileUtil.createData (root, "SomeData.txt");
        
        DataObject obj = DataObject.find (fo);
        if (!  (obj instanceof MultiDataObject)) {
            fail ("It should be multi data object: " + obj);
        }
        
        final T t = new T ();
        final MultiDataObject.Entry e = ((MultiDataObject)obj).getPrimaryEntry ();
        final org.openide.loaders.ConnectionSupport sup = new org.openide.loaders.ConnectionSupport (
            e, new T[] { t }
        );
        
        sup.register (t, MN.myNode);
        
        class BreakIt implements ConnectionSupport.Listener, Runnable {
            public boolean called;
            public boolean finished;
            
            public void notify (ConnectionCookie.Event ev) {
                called = true;
                org.openide.util.RequestProcessor.getDefault ().post (this).waitFinished ();
                finished = true;
            }
            
            public void run () {
                try {
                    sup.unregister (t, MN.myNode);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        BreakIt b = new BreakIt ();
        MN.myNode.b = b;
        
        sup.fireEvent (new ConnectionSupport.Event (e.getDataObject ().getNodeDelegate (), t));
        
        assertTrue ("Notify called", b.called);
        assertTrue ("Plus when calling notify none holds a lock that would prevent" +
                "other thread from reentering the ConnectionSupport", b.finished);
    }
    
    private static final class MN extends org.openide.nodes.AbstractNode {
        public static MN myNode = new MN ();
        
        public ConnectionCookie.Listener b;
        private MN () {
            super (org.openide.nodes.Children.LEAF);
        }
        
        public Node.Cookie getCookie (Class c) {
            if (c == ConnectionCookie.Listener.class) {
                return b;
            }
            return null;
        }
        
        public Handle getHandle () {
            return new H ();
        }
        
    }
    
    private static final class H implements Node.Handle, Serializable {
        public Node getNode () {
            return MN.myNode;
        }
    }
    
    private static final class T implements ConnectionSupport.Type {
        public Class getEventClass () {
            return javax.swing.event.ChangeListener.class;
        }

        public boolean isPersistent () {
            return true;
        }

        public boolean overlaps(org.openide.cookies.ConnectionCookie.Type type) {
            return getClass () == type.getClass ();
        }
        
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new Pool ());
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        static List loaders;
        
        public Pool () {
        }

        public java.util.Enumeration loaders () {
            if (loaders == null) {
                return org.openide.util.Enumerations.empty ();
            }
            return Collections.enumeration (loaders);
        }
    }
    
}
