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

package org.netbeans.modules.openide.windows;

import org.openide.util.Lookup;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/** An interface that can be registered in a lookup by subsystems
 * wish to provide a global context actions should react to.
 *
 * @author Jaroslav Tulach
*/
public final class GlobalActionContextImpl extends Object 
implements ContextGlobalProvider, Lookup.Provider, java.beans.PropertyChangeListener {
    /** registry to work with */
    private TopComponent.Registry registry;
    
    public GlobalActionContextImpl () {
        this (TopComponent.getRegistry());
    }
    
    public GlobalActionContextImpl (TopComponent.Registry r) {
        this.registry = r;
    }
    
    /** the lookup to temporarily use */
    private static volatile Lookup temporary;
    /** Temporarily provides different action map in the lookup.
     */
    public static void blickActionMap (javax.swing.ActionMap map) {
        Object obj = Lookup.getDefault ().lookup (ContextGlobalProvider.class);
        if (obj instanceof GlobalActionContextImpl) {
            GlobalActionContextImpl g = (GlobalActionContextImpl)obj;
            
            Lookup[] arr = {
                Lookups.singleton (map),
                Lookups.exclude (g.getLookup (), new Class[] { javax.swing.ActionMap.class }),
            };
            
            Lookup prev = temporary;
            try {
                temporary = new ProxyLookup (arr);
                Object q = org.openide.util.Utilities.actionsGlobalContext ().lookup (javax.swing.ActionMap.class);
                assert q == map : "We really get map from the lookup. Map: " + map + " returned: " + q; // NOI18N
            } finally {
                temporary = prev;
                // fire the changes about return of the values back
                org.openide.util.Utilities.actionsGlobalContext ().lookup (javax.swing.ActionMap.class);
            }
        }
    }
    
    /** Let's create the proxy listener that delegates to currently 
     * selected top component.
     */
    public Lookup createGlobalContext() {
        registry.addPropertyChangeListener(this);
        return org.openide.util.lookup.Lookups.proxy(this);
    }
    
    /** The current component lookup */
    public Lookup getLookup() {
        Lookup l = temporary;
        if (l != null) {
            return l;
        }
        
        TopComponent tc = registry.getActivated();
        return tc == null ? Lookup.EMPTY : tc.getLookup();
    }
    
    /** Requests refresh of our lookup everytime component is chagned.
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals (evt.getPropertyName())) {
            org.openide.util.Utilities.actionsGlobalContext ().lookup (javax.swing.ActionMap.class);
        }
    }
    
}
