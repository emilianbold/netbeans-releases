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

package org.openide.loaders;

import java.beans.*;
import java.io.*;
import java.util.*;

import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.nodes.NodeOp;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.SafeException;

/** Manages actions read and write for a given loader.
 *
 * @author Jaroslav Tulach
 */
final class DataLdrActions extends FolderInstance {
    /** Reference<DataLoader> to know for what loader we work */
    private java.lang.ref.Reference ref;
    /** last creating task */
    private org.openide.util.Task creation;
    /** processor to use */
    private static org.openide.util.RequestProcessor RP = new org.openide.util.RequestProcessor ("Loader Actions");
    
    public DataLdrActions (DataFolder f, DataLoader l) {
        super (f);
        
        this.ref = new java.lang.ref.WeakReference (l);
    }
    
    /** Asks the manager to store these actions to disk. Provided for
     * backward compatibility.
     */
    public synchronized void setActions (final SystemAction[] arr) {
        class DoTheWork implements Runnable, FileSystem.AtomicAction {
            private int state;
            
            /** The goal of this method is to make sure that all actions
             * will really be stored on the disk.
             */
            private void work () throws IOException {
                DataObject[] now = folder.getChildren ();
                HashMap nowToObj = new HashMap ();
                LinkedList sepObjs = new LinkedList ();
                for (int i = 0; i < now.length; i++) {
                    InstanceCookie ic = (InstanceCookie)now[i].getCookie (InstanceCookie.class);
                    if (ic != null) {
                        try {
                            Object instance = ic.instanceCreate ();
                            if (instance instanceof javax.swing.Action) {
                                nowToObj.put (instance, now[i]);
                                continue;
                            }
                            if (instance instanceof javax.swing.JSeparator) {
                                sepObjs.add (now[i]);
                                continue;
                            }
                        } catch (ClassNotFoundException ex) {
                            ErrorManager.getDefault ().notify (ex);
                        }
                    }
                }
                
                ArrayList order = new ArrayList ();
                
                for (int i = 0; i < arr.length; i++) {
                    DataObject obj = (DataObject)nowToObj.remove (arr[i]);
                    if (obj == null) {
                        if (arr[i] != null) {
                            obj = InstanceDataObject.create (folder, null, arr[i].getClass ());
                        } else {
                            if (!sepObjs.isEmpty ()) {
                                obj = (DataObject)sepObjs.removeFirst ();
                            } else {
                                obj = InstanceDataObject.create (folder, "Separator" + order.size (), javax.swing.JSeparator.class);
                            }
                        }
                    }
                    order.add (obj);
                }
                
                // these were there but are not there anymore
                for (Iterator it = nowToObj.values ().iterator (); it.hasNext (); ) {
                    DataObject obj = (DataObject)it.next ();
                    obj.delete ();
                }
                for (Iterator it = sepObjs.iterator (); it.hasNext (); ) {
                    DataObject obj = (DataObject)it.next ();
                    obj.delete ();
                }
                
                folder.setOrder ((DataObject[])order.toArray (new DataObject[0]));
            }
            
            public void run () {
                try {
                    switch (state) {
                        case 0:
                            state = 1;
                            folder.getPrimaryFile ().getFileSystem ().runAtomicAction (this);
                            break;
                        case 1:
                            work ();
                            break;
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            }
        }
        
        DoTheWork dtw = new DoTheWork ();
        creation = RP.post (dtw);
    }
    
    
    /** Creates the actions and notifies the loader.
     */
    protected Object createInstance (org.openide.cookies.InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
        ArrayList list = new ArrayList ();
        for (int i = 0; i < cookies.length; i++) {
            Class clazz = cookies[i].instanceClass ();
            if (javax.swing.JSeparator.class.isAssignableFrom (clazz)) {
                list.add (null);
                continue;
            }
            
            Object action = cookies[i].instanceCreate ();
            if (action instanceof javax.swing.Action) {
                list.add (action);
                continue;
            }
        }
        
        DataLoader l = (DataLoader)ref.get ();
        if (l != null) {
            l.setSwingActions (list);
        }
        
        return list.toArray (new javax.swing.Action[0]);
    }

    /** Currently not recursive */
    protected org.openide.cookies.InstanceCookie acceptFolder (DataFolder df) {
        return null;
    }

    /** Creation in our own thread, so we can exclude storage modifications */
    protected org.openide.util.Task postCreationTask (Runnable run) {
        return RP.post (run);
    }
    
    public void waitFinished () {
        org.openide.util.Task t;
        synchronized (this) {
            t = creation;
        }
        
        if (t != null) {
            t.waitFinished ();
        }
        
        super.waitFinished ();
    }
    
}
