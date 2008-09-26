/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.sync;


import org.openide.loaders.*;
import org.openide.nodes.*;
import java.util.Vector;
import java.util.Arrays;
import org.netbeans.modules.xml.util.Util;

/**
 * A generic support for synchronizing text, tree and file representation
 * of a <b>DataObject</b>. It can be notifified about a change in any of the above
 * representations by:
 * <li>treeChanged()  at TreeEditorSupport
 * <li>textChanged()  at TextEditorSupport
 * <li>fileChanged()  at FileObject
 * it then propagates change to other representation by calling ...Impl methods
 * of above (vitrual methods of extending classes). This support should
 * break possible cycles in ...Impl methods if <b>synchronous</b> implementation is used.
 *
 * <p>
 * It is passive object it must be controlled by calling <code>representationChanged()</code>
 * from e.g. listeners.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public abstract class SyncSupport {

    private /*final*/ DataObject dobj;
    
    /** Just performing synchronization. */
    protected static final int JUST_SYNCHRONIZING = 1;
    
    /** Conflict alleared, waiting for user selection. */
    protected static final int JUST_RESOLVING_CONFLICT = 2;
    
    /** Inactive */
    protected static final int NOP = 0;
    
    private int syncOperation; // current operation
    
    // lock that synchronizes access to all above fields
    private final Object syncOperationLock = new SyncSupportLock();
    
//~~~~~~~~~~~~~~~~~~ INIT ~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Creates new SyncSupport */
    public SyncSupport (DataObject dobj) throws IllegalArgumentException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Creating sychronizator " + System.identityHashCode(this) + " for: " + dobj.getPrimaryFile() ); // NOI18N
        
        this.dobj = dobj;
        syncOperation = NOP;
    }


    // wait until
    private void waitFor(int state) throws InterruptedException {
        while (getSyncOp() != NOP) syncOperationLock.wait();
    }

    // change state
    private void setSyncOp(int op) {
        synchronized (syncOperationLock) {            
            
            if (op == syncOperation) return;
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ( "" + System.identityHashCode(this) + " syncOperation " + syncOperation + " => " + op + " Thread:" + Thread.currentThread().getName()); // NOI18N

            syncOperation = op;
            syncOperationLock.notifyAll();
        }
    }

    private int getSyncOp() {
        synchronized (syncOperationLock) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("" + System.identityHashCode(this) + " syncOperation = " + syncOperation + " Thread:" + Thread.currentThread().getName()); // NOI18N

            return syncOperation;
        }
    }


    /** 
     * @return true if just synchronizing i.e. no other sync call would succed
     */
    public boolean isInSync() {
        return getSyncOp() == JUST_SYNCHRONIZING;
    }

    
    /**
     * It is thread save way how to enter synchronizator.
     */
    public void postRequest(Runnable task) {

        boolean leave = false;
        
        try {
            synchronized (syncOperationLock) {
                waitFor(NOP);                
                leave = true;
                setSyncOp(JUST_SYNCHRONIZING);
            }
            
            task.run();
        } catch (InterruptedException ex) {
            // let finally does it
        } finally {
            if (leave) setSyncOp(NOP);
        }
    }
    
    /**
     * Retrieve cookie from associated DataObject.
     */
    protected final Node.Cookie getCookie(Class klass) {
        return getDO().getCookie(klass);
    }
    
    /**
     * @return DataObject this object is serving for
     */
    protected final DataObject getDO() {
        return dobj;
    }

    
// ~~~~~~~~~~~~~~~~~~ Representations management ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    

    /**
     * A new model appeared.
     * It may my change result returned by getRepresentations().
     */
    public abstract void addRepresentation(Representation rep);
    
    /**
     * Some model disppeared.
     * It may my change result returned by getRepresentations().
     */
    public abstract void removeRepresentation(Representation rep);
    
    /**
     * Propagate change to other representations.
     */
    protected void representationChanged(Class type) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("" + System.identityHashCode(this) + " entering synchronizator: " + type); // NOI18N
        
        if (isInSync()) return;  //??? prevent double enter if update() fires
        
        try {
            setSyncOp(JUST_SYNCHRONIZING);
        
            Representation master = null;
            Representation all[] = getRepresentations(null);

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\tReps: " + Arrays.asList(all)); // NOI18N
            
            // look for comodifications

            Vector modified = new Vector();
            for (int i=0; i<all.length; i++) {
                if (all[i].represents(type)) {
                    master = all[i];
                    modified.add(all[i]);                    
                    continue;
                }

                if (all[i].isModified()) {
                    modified.add(all[i]);
                }
            }
            
            if (modified.size() > 1) {
                master = selectMasterRepresentation((Representation[])modified.toArray(new Representation[0]));
            }

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\t" + System.identityHashCode(this) + " master: " + master); // NOI18N
            
            if (master == null) return;

            // propagate

            for (int i=0; i<all.length; i++) {
                if (all[i] == master) continue;
                
                // try prefered update then arbitrary
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("\tpreffered update class: " + all[i].getUpdateClass()); // NOI18N

                Object change = master.getChange(all[i].getUpdateClass());
                if (change == null) change = master.getChange(null);
                
                if (change != null) {
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\t" + System.identityHashCode(this) + " updating: " + all[i] + " with:" + change.getClass()); // NOI18N

                    all[i].update(change);
                }
            }
        } finally {
            setSyncOp(NOP);
        }
    }
    
        
    /**
     * Return all loaded representations (ordered). The order is importent for
     * all subsequent calls It is guaranteed that all SyncSupport callbacks
     * will be in the same order.
     */
    protected abstract Representation[] getRepresentations();
            
    /**
     * Return given representations.
     * @param type (FileObject, TreeDocumentRoot, Document)
     */
    protected Representation[] getRepresentations(Class type) {
        
        Representation[] all = getRepresentations();
        if (type == null) {
            return all;
        } else {
            throw new RuntimeException("Not Implemened."); // NOI18N
        }
    }
    
    
    
    /**
     * A comodification occured resolve conflict by selecting new master or
     * null on user cancel.
     */
    protected Representation selectMasterRepresentation(Representation[] choices){
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("COMODIFICATION:"); // NOI18N
//         for (int i = 0; i<choices.length; i++) {
//             if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ((i == 0 ? "=>" : "  ") + choices[i]); // NOI18N
//         }
        
        return choices[0];        
    }
    
    
    /** Just for better thread dumps. */
    private static class SyncSupportLock {
    }
    
}
