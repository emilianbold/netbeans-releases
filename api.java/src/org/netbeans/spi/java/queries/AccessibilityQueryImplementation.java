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
package org.netbeans.spi.java.queries;

import org.openide.filesystems.FileObject;

/**
 * Permits providers to mark certain Java packages as being inaccessible to
 * outside code despite possibly containing public classes.
 * <p>
 * A default implementation is registered by the
 * <code>org.netbeans.modules.java.project</code> module which looks up the
 * project corresponding to the file (if any) and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Java Project module and uses this style.
 * </p>
 * @see org.netbeans.api.java.queries.AccessibilityQuery
 * @see org.netbeans.api.queries.FileOwnerQuery
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public interface AccessibilityQueryImplementation {

    /**
     * Checks whether a given Java package (folder of source files)
     * is intended to be publicly accessed by code residing in other
     * compilation units.
     * @param pkg a Java source package
     * @return true if it is definitely intended for public access, false if it
     *         is definitely not, or null if nothing is known about it
     */
    public Boolean isPubliclyAccessible(FileObject pkg);

}
