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

package org.openide.loaders;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import junit.framework.TestCase;
import org.openide.cookies.ConnectionCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

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
    
    public void testFireEvent () throws Exception {
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        FileObject fo = FileUtil.createData (root, "SomeData.txt");
        
        DataObject obj = DataObject.find (fo);
        if (!  (obj instanceof MultiDataObject)) {
            fail ("It should be multi data object: " + obj);
        }
        
        final T t = new T ();
        final MultiDataObject.Entry e = ((MultiDataObject)obj).getPrimaryEntry ();
        final ConnectionSupport sup = new ConnectionSupport (
            e, new T[] { t }
        );
        
        sup.register (t, MN.myNode);
        
        class BreakIt implements ConnectionSupport.Listener, Runnable {
            public boolean called;
            public boolean finished;
            
            public void notify (ConnectionCookie.Event ev) {
                called = true;
                RequestProcessor.getDefault ().post (this).waitFinished ();
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
    
    private static final class MN extends AbstractNode {
        public static MN myNode = new MN ();
        
        public ConnectionCookie.Listener b;
        private MN () {
            super (Children.LEAF);
        }
        
        public Node.Cookie getCookie (Class c) {
            if (c == ConnectionCookie.Listener.class) {
                return b;
            }
            return null;
        }
        
        public Node.Handle getHandle () {
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

        public boolean overlaps(ConnectionCookie.Type type) {
            return getClass () == type.getClass ();
        }
        
    }
    
    public static final class Lkp extends AbstractLookup {
        public Lkp () {
            this (new InstanceContent ());
        }
        
        private Lkp (InstanceContent ic) {
            super (ic);
            ic.add (new Pool ());
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        static List loaders;
        
        public Pool () {
        }

        public Enumeration loaders () {
            if (loaders == null) {
                return Enumerations.empty ();
            }
            return Collections.enumeration (loaders);
        }
    }
    
}
