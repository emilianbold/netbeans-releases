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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.Iterator;

/**
 * Defines which <code>DataObject</code>s should be searched.
 * Iterator returned by this interface's method enumerates
 * <code>DataObject</code>s that should be searched.
 * <p>
 * <code>SearchInfo</code> objects are used by module User Utilities
 * &ndash; in actions <em>Find</em> (since User Utilities 1.16)
 * and <em>Find in Projects</em> (since User Utilities 1.23).
 * Action <em>Find</em> obtains <code>SearchInfo</code> from
 * <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getLookup()"><code>Lookup</code> of nodes</a>
 * the action was invoked on. Action <em>Find in Projects</em> obtains
 * <code>SearchInfo</code> from
 * <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup()"><code>Lookup</code>
 * of the projects</a>.
 * </p>
 *
 * @see  SearchInfoFactory
 * @see  <a href="@org-openide-loaders@/org/openide/loaders/DataObject.html"><code>DataObject</code></a>
 * @see  <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getLookup()"><code>Node.getLookup()</code></a>
 * @see  <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup()"><code>Project.getLookup()</code></a>
 * @since  org.openidex.util/3 3.2
 * @author  Marian Petras
 */
public interface SearchInfo {

    /**
     * Determines whether the object which provided this <code>SearchInfo</code>
     * can be searched.
     * This method determines whether the <em>Find</em> action should be enabled
     * for the object or not.
     * <p>
     * This method must be very quick as it may be called frequently and its
     * speed may influence responsiveness of the whole application. If the exact
     * algorithm for determination of the result value should be slow, it is
     * better to return <code>true</code> than make the method slow.
     *
     * @return  <code>false</code> if the object is known that it cannot be
     *          searched; <code>true</code> otherwise
     * @since  org.openidex.util/3 3.3
     */
    public boolean canSearch();

    /**
     * Specifies which <code>DataObject</code>s should be searched.
     * The returned <code>Iterator</code> needn't implement method
     * {@link java.util.Iterator#remove remove()} (i.e. it may throw
     * <code>UnsupportedOperationException</code> instead of actual
     * implementation).
     *
     * @return  iterator which iterates over <code>DataObject</code>s
     *          to be searched
     */
    public Iterator objectsToSearch();
    
}
