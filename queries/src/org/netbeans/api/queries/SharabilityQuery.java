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

package org.netbeans.api.queries;

import java.util.Iterator;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

// XXX perhaps should be in the Filesystems API instead of here?
// XXX should perhaps use File not FileObject (cf. CollocationQuery)

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * Likely to be of use only to a VCS filesystem.
 * <p>
 * This query can be considered to obsolete {@link FileObject#setImportant}.
 * Unlike that method, the information is pulled by the VCS filesystem on
 * demand, which may be more reliable than ensuring that the information
 * is pushed by a project type (or other implementor) eagerly.
 * @see SharabilityQueryImplementation
 * @author Jesse Glick
 */
public final class SharabilityQuery {
    
    private static final Lookup.Result/*<SharabilityQueryImplementation>*/ implementations =
        Lookup.getDefault().lookup(new Lookup.Template(SharabilityQueryImplementation.class));
    
    private SharabilityQuery() {}
    
    // XXX may also need a related query: check whether a given file is
    // machine-generated and should thus be made read-only in the editor
    // (e.g. generated servlets, or build-impl.xml)
    
    /**
     * Check whether an existing file is sharable.
     * @param file a file or directory
     * @return true if it should be shared, false if it should not, or null if
     *         no definite answer is available
     */
    public static Boolean isSharable(FileObject file) {
        if (file == null) throw new IllegalArgumentException();
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            SharabilityQueryImplementation sqi = (SharabilityQueryImplementation)it.next();
            Boolean b = sqi.isSharable(file);
            if (b != null) {
                return b;
            }
        }
        return null;
    }
    
    /**
     * Check whether a new file should be sharable.
     * @param parent an existing parent directory
     * @param childName the proposed name of the child
     * @param directory true if the proposed child will be a directory
     * @return true if it should be shared, false if it should not, or null if
     *         no definite answer is available
     */
    public static Boolean willBeSharable(FileObject parent, String childName, boolean directory) {
        if (parent == null || childName == null) throw new IllegalArgumentException();
        if (!parent.isFolder()) throw new IllegalArgumentException();
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            SharabilityQueryImplementation sqi = (SharabilityQueryImplementation)it.next();
            Boolean b = sqi.willBeSharable(parent, childName, directory);
            if (b != null) {
                return b;
            }
        }
        return null;
    }
    
}
