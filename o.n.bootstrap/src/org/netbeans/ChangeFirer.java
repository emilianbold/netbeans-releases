/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import org.openide.util.Utilities;
import java.util.*;

/** Thread which fires changes in the modules.
 * Used to separate property change events and
 * lookup changes from the dynamic scope of the
 * changes themselves. Also to batch up possible
 * changes and avoid firing duplicates.
 * Accepts changes at any time
 * and fires them from within the mutex (as a reader).
 * @author Jesse Glick
 */
final class ChangeFirer {
    
    private final ModuleManager mgr;
    // Pending things to perform:
    private final Set changes = new LinkedHashSet(100); // Set<Change>
    private final Set modulesCreated = new HashSet(100); // Set<Module>
    private final Set modulesDeleted = new HashSet(10); // Set<Module>
    
    /** Make a new change firer.
     * @param mgr the associated module manager
     */
    public ChangeFirer(ModuleManager mgr) {
        this.mgr = mgr;
    }
    
    /** Add a change to the list of pending things to be fired.
     * @param c the change which will be fired
     */
    public void change(Change c) {
        changes.add(c);
    }
    
    /** Add a module creation event to the list of pending things to be fired.
     * @param m the module whose creation event will be fired
     */
    public void created(Module m) {
        modulesCreated.add(m);
    }
    
    /** Add a module deletion event to the list of pending things to be fired.
     * Note that this will cancel any pending creation event for the same module!
     * @param m the module whose creation event will be fired
     */
    public void deleted(Module m) {
        // Possible that a module was added and then removed before any change
        // was fired; in this case skip it.
        if (! modulesCreated.remove(m)) {
            modulesDeleted.add(m);
        }
    }
    
    /** Fire all pending changes.
     * While this is happening, the manager is locked in a read-only mode.
     * Should only be called from within a write mutex!
     */
    public void fire() {
        mgr.readOnly(true);
        try {
            Iterator it = changes.iterator();
            while (it.hasNext()) {
                Change c = (Change) it.next();
                if (c.source instanceof Module) {
                    ((Module) c.source).firePropertyChange0(c.prop, c.old, c.nue);
                } else if (c.source == mgr) {
                    mgr.firePropertyChange(c.prop, c.old, c.nue);
                } else {
                    throw new IllegalStateException("Strange source: " + c.source); // NOI18N
                }
            }
            changes.clear();
            if (! modulesCreated.isEmpty() || ! modulesDeleted.isEmpty()) {
                mgr.fireModulesCreatedDeleted(modulesCreated, modulesDeleted);
            }
            modulesCreated.clear();
            modulesDeleted.clear();
        } catch (RuntimeException e) {
            // Recover gracefully.
            Util.err.notify(e);
        } finally {
            mgr.readOnly(false);
        }
    }
    
    /** Possible change event to be fired.
     * Used instead of PropertyChangeEvent as it can be stored in a set.
     */
    public static final class Change {
        public final String prop;
        public final Object source, old, nue;
        public Change(Object source, String prop, Object old, Object nue) {
            this.source = source;
            this.prop = prop;
            this.old = old;
            this.nue = nue;
        }
        // Semantic equality, to avoid duplicate changes:
        public boolean equals(Object o) {
            if (! (o instanceof Change)) return false;
            Change c = (Change) o;
            return Utilities.compareObjects(prop, c.prop) &&
                   Utilities.compareObjects(source, c.source) &&
                   Utilities.compareObjects(old, c.old) &&
                   Utilities.compareObjects(nue, c.nue);
        }
        public int hashCode() {
            return source.hashCode() ^ (prop == null ? 0 : prop.hashCode());
        }
        public String toString() {
            return "Change[" + source + ":" + prop + ";" + old + "->" + nue + "]"; // NOI18N
        }
    }
    
}
