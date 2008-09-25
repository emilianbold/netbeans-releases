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
package org.netbeans.modules.xml.util;

import java.util.*;

import org.openide.util.*;

/**
 *
 * @author Libor Kramolis, Jesse Glick
 * @version 0.2
 */
public abstract class LookupManager {
    /** */
    private static final Map<Class,Handle> handles = new WeakHashMap<Class,Handle>();

    /** */
    private Handle handle = null;


    //
    // init
    //

    /**
     * Create new LookupManager. Call register() when ready.
     */
    public LookupManager () {
    }


    /** To be called when it is fully initialized and ready to receive events.
     * Subclasses may wish to call addedToResult (getResults()) immediately.
     */
    protected final void register (Class clazz) {
        if ( handle != null ) {
            throw new IllegalStateException();
        }
        synchronized (handles) {
            handle = (Handle)handles.get (clazz);
            if ( handle == null ) {
                handles.put (clazz, handle = new Handle (clazz));
            }
        }
        handle.register (this);
    }


    //
    // itself
    //

    /**
     */
    protected final Collection getResult() {
        return handle.getInstances();
    }


    /**
     */
    protected abstract void removedFromResult (Collection removed);

    /**
     */
    protected abstract void addedToResult (Collection added);


    //
    // class Handle
    //

    /**
     *
     */
    private static final class Handle implements LookupListener {

        private final Class clazz;
        private Lookup.Result lookupResult = null;
        private Collection lastResult = null;
        private final Set<LookupManager> lms = new WeakSet<LookupManager>(300);

        //
        // init
        //

        /**
         */
        private Handle (Class clazz) {
            this.clazz = clazz;
        }

        /**
         */
        public void register (LookupManager lm) {
            synchronized (lms) {
                lms.add (lm);
            }
        }

        
        //
        // itself
        //
        
        /**
         */
        private Lookup.Result getLookupResult () {
            if ( lookupResult == null ) {
                lookupResult = (Lookup.getDefault()).lookup (new Lookup.Template (clazz));
                lookupResult.addLookupListener (this);
            }
            return lookupResult;
        }

        /**
         */
        public void resultChanged (LookupEvent evt) {
            Collection currentResult = getLookupResult().allInstances();

            Collection removed = new HashSet (lastResult);
            removed.removeAll (currentResult);
            Collection added = new HashSet (currentResult);
            added.removeAll (lastResult);

            if ( ( removed.isEmpty() == false ) ||
                 ( added.isEmpty() == false ) ) {
                synchronized (lms) {
                    Iterator it = lms.iterator();
                    while (it.hasNext()) {
                        LookupManager lm = (LookupManager)it.next();
                        if ( removed.isEmpty() == false ) {
                            lm.removedFromResult(removed);
                        }
                        if ( added.isEmpty() == false ) {
                            lm.addedToResult(added);
                        }
                    }
                }
            }
            
            lastResult = currentResult;
        }

        /**
         */
        public Collection getInstances() {
            //!!! can we use caching? I'm affraid we cannot because
            // lookup callbakcs are asynchronous so we can miss some
            // registrations (it may be crucuial for cookies)
            if (lastResult == null) {
                lastResult = getLookupResult().allInstances();
            }
            return lastResult;
        }

    } // end: class Handle
    
}
