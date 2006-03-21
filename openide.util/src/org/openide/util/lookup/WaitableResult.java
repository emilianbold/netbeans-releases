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
package org.openide.util.lookup;

import java.util.Collection;
import org.openide.util.Lookup;


/** A special subclass of lookup that is able to wait before queries.
 *
 * @author  Jaroslav Tulach
 */
abstract class WaitableResult<T> extends Lookup.Result<T> {
    /** Used by proxy results to synchronize before lookup.
     */
    protected abstract void beforeLookup(Lookup.Template t);
    
    /** Needed to group notification of outside the package listeners
     * after all AbstractLookup and ProxyLookups have been updated.
     * @param evAndListeners LookupEvent, LookupListener, LookupEvent, LookupListener, etc.
     */
    protected abstract void collectFires(Collection<Object> evAndListeners);
     
}
