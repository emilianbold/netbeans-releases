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
import java.util.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

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
                    org.openide.cookies.InstanceCookie ic = (org.openide.cookies.InstanceCookie) now[i].getCookie(org.openide.cookies.InstanceCookie.class);

                    if (ic != null) {
                        try {
                            java.lang.Object instance = ic.instanceCreate();

                            if (instance instanceof javax.swing.Action) {
                                nowToObj.put(instance, now[i]);
                                continue;
                            }
                            if (instance instanceof javax.swing.JSeparator) {
                                sepObjs.add(now[i]);
                                continue;
                            }
                        }
                        catch (java.lang.ClassNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
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
                    Exceptions.printStackTrace(ex);
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
