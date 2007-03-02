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

package org.netbeans;

import java.util.logging.Level;
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
    private final Set<Change> changes = new LinkedHashSet<Change>(100);
    private final Set<Module> modulesCreated = new HashSet<Module>(100);
    private final Set<Module> modulesDeleted = new HashSet<Module>(10);
    
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
            for (Change c: changes) {
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
            Util.err.log(Level.SEVERE, null, e);
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
