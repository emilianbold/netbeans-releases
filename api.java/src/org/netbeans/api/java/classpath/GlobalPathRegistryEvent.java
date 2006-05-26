/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.classpath;

import java.util.EventObject;
import java.util.Set;

/**
 * Event object giving details of a change in the path registry.
 */
public final class GlobalPathRegistryEvent extends EventObject {

    private final String id;
    private final Set<ClassPath> changed;

    GlobalPathRegistryEvent(GlobalPathRegistry r, String id, Set<ClassPath> changed) {
        super(r);
        assert id != null;
        assert changed != null && !changed.isEmpty();
        this.id = id;
        this.changed = changed;
    }

    /**
     * Get the affected registry.
     * @return the registry
     */
    public GlobalPathRegistry getRegistry() {
        return (GlobalPathRegistry)getSource();
    }

    /**
     * Get the type of classpaths that were added or removed.
     * @return the type, e.g. {@link ClassPath#SOURCE}
     */
    public String getId() {
        return id;
    }

    /**
     * Get a set of classpaths that were added or removed.
     * @return an immutable and nonempty set of {@link ClassPath}s of the given type
     */
    public Set<ClassPath> getChangedPaths() {
        return changed;
    }

}
