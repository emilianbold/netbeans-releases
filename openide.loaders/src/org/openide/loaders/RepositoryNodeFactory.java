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

import org.openide.nodes.Node;
import org.openide.util.Lookup;

/** Provisional mechanism for displaying the Repository object.
 * It will show all filesystems, possibly with a filter.
 * @deprecated Probably unwise to call this for any reason; obsolete UI.
 * @author Jesse Glick
 * @since 3.14
 */
@Deprecated
public abstract class RepositoryNodeFactory {

    /** Get the default factory.
     * @return the default instance from lookup
     */
    public static RepositoryNodeFactory getDefault() {
        return (RepositoryNodeFactory)Lookup.getDefault().lookup(RepositoryNodeFactory.class);
    }

    /** Subclass constructor. */
    protected RepositoryNodeFactory() {}
    
    /** Create a node representing a subset of the repository of filesystems.
     * You may filter out certain data objects.
     * If you do not wish to filter out anything, just use {@link DataFilter#ALL}.
     * Nodes might be reused between calls, so if you plan to add this node to a
     * parent, clone it first.
     * @param f a filter
     * @return a node showing part of the repository
     */
    public abstract Node repository(DataFilter f);

}
