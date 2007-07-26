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

package org.netbeans.core.startup.layers;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.*;

/** Extends LocalFileSystem by useful features. It is used as
 * delegates being part of SystemFileSystem.
 *
 * @author  Vita Stejskal
 */
public final class LocalFileSystemEx extends LocalFileSystem {

    /** name -> FileObject */
    private static HashMap<String,FileObject> allLocks = new HashMap<String,FileObject> (7);
    private static HashSet<String> pLocks = new HashSet<String> (7);
//    private static HashMap allThreads = new HashMap (7);

    public static String [] getLocks () {
        synchronized (allLocks) {
            removeInvalid (pLocks);
            LinkedList<String> l = new LinkedList<String> ();
            l.addAll (allLocks.keySet ());
            l.addAll (pLocks);
            return l.toArray (new String [l.size ()]);
        }
    }

    public static boolean hasLocks () {
        synchronized (allLocks) {
            removeInvalid (pLocks);
            return !allLocks.isEmpty () || !pLocks.isEmpty ();
        }
    }
    
    public static void potentialLock (String name) {
        synchronized (allLocks) {
            pLocks.add (name);
        }
    }
    
    public static void potentialLock (String o, String n) {
        synchronized (allLocks) {
            if (pLocks.remove (o)) {
                pLocks.add (n);
            }
        }
    }

    private static void removeInvalid (Set names) {
        FileSystem sfs = Repository.getDefault ().getDefaultFileSystem ();
        Iterator i = names.iterator ();
        while (i.hasNext ()) {
            String name = (String) i.next ();
            FileObject fo = sfs.findResource (name);
            if (null == fo || !fo.isLocked()) {
                // file lock recorded in potentialLock has been used
                // in operation which masked file as hidden and nothing
                // was actually locked
                i.remove ();
            }
        }
    }

    /** Creates new LocalFileSystemEx */
    public LocalFileSystemEx () {
        this( false );
    }
    
    /**
     * @since 1.8
     */
    LocalFileSystemEx( boolean supportRemoveWritablesAttr ) {
        if( supportRemoveWritablesAttr ) {
            attr = new DelegatingAttributes( attr );
        }
    }

    protected void lock (String name) throws IOException {
        super.lock (name);
        synchronized (allLocks) {
            FileObject fo = findResource (name);
            allLocks.put (name, fo);
            pLocks.remove (name);
//            allThreads.put (name, new Throwable ("LocalFileSystemEx.lock() is locking file: " + name));
        }
    }    
    
    protected void unlock (String name) {
        synchronized (allLocks) {
            if (allLocks.containsKey (name)) {
                allLocks.remove (name);
//                allThreads.remove (name);
            } else {
                FileObject fo = findResource (name);
                if (fo != null) {
		    for (Map.Entry entry: allLocks.entrySet()) {
                        if (fo.equals (entry.getValue ())) {
                            allLocks.remove (entry.getKey ());
//                            allThreads.remove (entry.getKey ());
                            break;
                        }
                    }
                } else {
                    Logger.getLogger(LocalFileSystemEx.class.getName()).log(Level.WARNING, null,
                                      new Throwable("Can\'t unlock file " + name +
                                                    ", it\'s lock was not found or it wasn\'t locked."));
                }
            }
        }
        super.unlock (name);
    }
    
    private class DelegatingAttributes implements AbstractFileSystem.Attr {
        
        private AbstractFileSystem.Attr a;
        
        public DelegatingAttributes( AbstractFileSystem.Attr a ) {
            this.a = a;
        }

        public Object readAttribute(String name, String attrName) {
            if( "removeWritables".equals( attrName ) ) {
                return new WritableRemover( name );
            }
            return a.readAttribute( name, attrName );
        }

        public void writeAttribute(String name, String attrName, Object value) throws IOException {
            a.writeAttribute( name, attrName, value );
        }

        public Enumeration<String> attributes(String name) {
            return a.attributes( name );
        }

        public void renameAttributes(String oldName, String newName) {
            a.readAttribute( oldName, newName );
        }

        public void deleteAttributes(String name) {
            a.deleteAttributes( name );
        }
    }

    private class WritableRemover implements Callable {
        private String name;
        public WritableRemover( String name ) {
            this.name = name;
        }
        
        public Object call() throws Exception {
            FileObject fo = findResource( name );
            if( null != fo ) {
                fo.delete();
            }
            return null;
        }
        
    }
}
