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

import java.util.Iterator;
import javax.naming.Context;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** Utilities that define the "settings hierarchy".
 *
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public final class Environment extends Object {
    /** Result of query for all instances of Environment.Provider */
    private static Lookup.Result result;

    /** Constructor
     */
    private Environment() {
    }

    /** Finds a lookup for given data object.
     * @param obj the object
     * @return the lookup for this data object
     */
    public static Lookup find (DataObject obj) {
        while (obj != null) {
            Lookup l = findForOne (obj);
            if (l != null) {
                return l;
            }
            
            FileObject fo = obj.getPrimaryFile ().getParent ();
            if (fo == null) {
                break;
            }
            
            try {
                obj = DataObject.find (fo);
            } catch (DataObjectNotFoundException ex) {
                break;
            }
        }
        
        // ok, use empty lookup
        return Lookup.EMPTY;
    }
    
    /** Finds a JNDI context for a given data object.
     * This method is probably unused and useless.
     * @param obj the data object
     * @return the JNDI context for this data object
     * @since 3.13
     */
    public static javax.naming.Context findSettingsContext(DataObject obj) {
        Iterator it = getProviders().allInstances().iterator();
        while (it.hasNext()) {
            Environment.Provider ep = (Environment.Provider) it.next();
            Lookup lookup = ep.getEnvironment(obj);
            if (lookup != null) {
                Context ctx = (Context) lookup.lookup(Context.class);
                if (ctx != null) return ctx;
            }
        }
        
        // default impl
        return new DefaultSettingsContext(obj);
    }
    
    /** Find method that tries to scan for lookup of one data object.
     * @param obj the object
     * @return the lookup or null
     */
    static Lookup findForOne (DataObject obj) {
        /*
        DataLoader loader = obj.getLoader ();
        Provider provider = loader instanceof Provider ? (Provider)loader : null;
        if (provider != null) {
            Lookup lookup = provider.getEnvironment (obj);
            if (lookup != null) {
                return lookup;
            } else {
                return Lookup.EMPTY;
            }
            }
        */
        
        Iterator it = getProviders().allInstances().iterator();
        while (it.hasNext ()) {
            Environment.Provider ep = (Environment.Provider)it.next ();
            Lookup lookup = ep.getEnvironment (obj);
            if (lookup != null) {
                return lookup;
            }
        }
        
        // not found for this data object
        return null;
    }
    
    static Lookup.Result getProviders() {
        if (result == null) {
            result = Lookup.getDefault().lookupResult(Environment.Provider.class);
        }
        return result;
    }
    
    /** Cookie for objects that plan to provide environment.
     * @since 1.9
     */
    public static interface Provider {
        /** Returns a lookup that represents environment.
         * @return the lookup
         */
        public Lookup getEnvironment (DataObject obj);
    } // end of Provider
}
