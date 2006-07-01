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
/*
 * DataTransferSupport.java
 *
 * Created on June 18, 2001, 1:26 PM
 */

package org.openide.loaders;


import java.awt.datatransfer.Transferable;
import java.io.*;
import java.util.Arrays;
import java.util.logging.*;
import org.openide.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.datatransfer.*;

/** Support for data transfer Paste operation.
 * @author  Vita Stejskal
 */
abstract class DataTransferSupport {

    /** Defines array of classes implementing paste for specified clipboard operation.
     * @param op clopboard operation to specify paste types for
     * @return array of classes extending PasteTypeExt class
     */
    protected abstract PasteTypeExt [] definePasteTypes (int op);
    /** Defines array of data clipboard operations recognized by this paste support.
     * @return array of DataFlavors
     */
    protected abstract int [] defineOperations ();
    /** Override in order to support additional paste types.
     * @param t clipboard Transferable object, list of transfered DataObjects with their flavors
     * @param s list of paste types supported for transfered objects
     */
    protected void handleCreatePasteTypes (Transferable t, java.util.List s) {
    }
    /** Fills in the list of paste types available for given set to transfered
     * DataObjects.
     * @param t clipboard Transferable object, list of transfered DataObjects with their flavors
     * @param s list of paste types supported for transfered objects
     */
    public final void createPasteTypes (Transferable t, java.util.List s) {
        /** All supported operations. */
        int [] ops = defineOperations ();

        for (int i = 0; i < ops.length; i++) {
            DataObject objs [] = LoaderTransfer.getDataObjects (t, ops[i]);
            PasteTypeExt pts [];

            if (objs == null || objs.length == 0)
                continue;

            pts = definePasteTypes (ops[i]);

            for (int j = 0; j < pts.length; j++) {
                pts[j].setDataObjects (objs);
                if (pts[j].canPaste ())
                    s.add (pts[j]);
            }
        }

        handleCreatePasteTypes (t, s);
    }
    
    private static final Logger err = Logger.getLogger("org.openide.loaders.DataTransferSupport"); //NOI18N
    
    /** Supports paste of multiple DataObject at once.
     */
    static abstract class PasteTypeExt extends PasteType {
        /** All DataObjects being pasted. */
        private DataObject objs [];
        /** Create paste type. */
        public PasteTypeExt () {
        }
        /** Can DataObject be pasted.
         * @param obj DataObject to be pasted
         * @return result of the test
         */
        protected abstract boolean handleCanPaste (DataObject obj);
        /** Handles the paste action
        * @param obj pasted DataObject
        */
        protected abstract void handlePaste (DataObject obj) throws IOException;
        /** Could be clipboard cleand up after the paste operation is finished or
         * should its content be preserved.
         * @return default implementation returns <code>false</code>
         */
        protected boolean cleanClipboard () {
            return false;
        }
        /** Paste all DataObjects */
        public final boolean canPaste () {
            for (int i = 0; i < objs.length; i++) {
                if (!handleCanPaste (objs[i]))
                    return false;
            }
            return true;
        }
        /** Paste all DataObjects */
        public final Transferable paste() throws IOException {
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                org.openide.util.RequestProcessor.getDefault().post(new java.lang.Runnable() {

                                                                        public void run() {
                                                                            java.lang.String n = org.openide.awt.Actions.cutAmpersand(getName());
                                                                            org.netbeans.api.progress.ProgressHandle h = org.netbeans.api.progress.ProgressHandleFactory.createHandle(n);

                                                                            h.start();
                                                                            h.switchToIndeterminate();
                                                                            try {
                                                                                doPaste();
                                                                            }
                                                                            catch (java.io.IOException ioe) {
                                                                                Exceptions.printStackTrace(ioe);
                                                                            }
                                                                            finally {
                                                                                h.finish();
                                                                            }
                                                                        }
                                                                    });
            } else {
                doPaste();
            }
            // clear clipboard or preserve content
            return cleanClipboard() ? ExTransferable.EMPTY : null;
        }
        
        private void doPaste () throws IOException {
	    if (err.isLoggable (Level.FINE)) {
		err.log(Level.FINE, null, new Throwable ("Issue #58666: Called " + this + " doPaste() on objects " + Arrays.asList (objs))); // NOI18N
	    }
            for (int i = 0; i < objs.length; i++)
                handlePaste (objs[i]);
        }
	
        public final void setDataObjects (DataObject objs []) {
            this.objs = objs;
        }
    }

    /** Paste types for data objects.
    */
    static class SerializePaste extends PasteType {
        private InstanceCookie cookie;
        private DataFolder target;
        
        /**
        * @param obj object to work with
        */
        public SerializePaste (DataFolder target, InstanceCookie cookie) {
            this.cookie = cookie;
            this.target = target;
        }

        /** The name is obtained from the bundle.
        * @return the name
        */
        public String getName () {
            return DataObject.getString ("PT_serialize");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (SerializePaste.class);
        }

        /** Paste.
        */
        public final Transferable paste () throws IOException {
            final DataFolder trg = getTargetFolder();
            String name = cookie.instanceName ();
            int i = name.lastIndexOf ('.') + 1;
            if (i != 0 && i != name.length ()) {
                name = name.substring (i);
            }

            name = FileUtil.findFreeFileName (trg.getPrimaryFile (), name, "ser"); // NOI18N


            final NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine (
                                                      DataObject.getString ("SerializeBean_Text"),
                                                      DataObject.getString ("SerializeBean_Title")
                                                  );
            nd.setInputText (name);

            if (NotifyDescriptor.OK_OPTION == DialogDisplayer.getDefault ().notify (nd)) {
                DataObjectPool.getPOOL().runAtomicAction (trg.getPrimaryFile (), new FileSystem.AtomicAction () {
                            public void run () throws IOException {
                                FileObject fo = trg.getPrimaryFile ().createData (nd.getInputText (), "ser"); // NOI18N
                                FileLock lock = fo.lock ();
                                ObjectOutputStream oos = null;
                                try {
                                    oos = new ObjectOutputStream (
                                              new java.io.BufferedOutputStream (fo.getOutputStream (lock))
                                          );
                                    oos.writeObject (cookie.instanceCreate ());
                                } catch (ClassNotFoundException e) {
                                    throw new IOException (e.getMessage ());
                                } finally {
                                    if (oos != null) oos.close ();
                                    lock.releaseLock ();
                                }
                            }
                        });
            }

            // preserve clipboard
            return null;
        }

        protected DataFolder getTargetFolder() throws IOException {
            return target;
        }
    }

    /** Paste types for data objects.
    */
    static class InstantiatePaste extends PasteType {
        private InstanceCookie cookie;
        private DataFolder target;
        
        /**
        * @param obj object to work with
        */
        public InstantiatePaste (DataFolder target, InstanceCookie cookie) {
            this.cookie = cookie;
            this.target = target;
        }

        /** The name is obtained from the bundle.
        * @return the name
        */
        public String getName () {
            return DataObject.getString ("PT_instance");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (InstantiatePaste.class);
        }

        /** Paste.
        */
        public final Transferable paste () throws IOException {
            try {
                Class clazz = cookie.instanceClass ();
                
                // create the instance
                InstanceDataObject.create(getTargetFolder(), null, clazz);
            } catch (ClassNotFoundException ex) {
                throw new IOException (ex.getMessage ());
            }

            // preserve clipboard
            return null;
        }

        protected DataFolder getTargetFolder() throws IOException {
            return target;
        }
    }
}
