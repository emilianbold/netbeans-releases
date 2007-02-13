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

package org.netbeans.spi.project;

import org.openide.util.Lookup;

/**
 * Allows project lookup to merge instances of known classes and replace them
 * with single instance. To be used in conjunction with the {@link org.netbeans.spi.project.LookupProvider}
 * and {@link org.netbeans.spi.project.support.LookupProviderSupport}
 * The interface is to be implemented by the project owner which decides which contracts make sense to have merged and
 * how they are to be merged.
 * The 3rd party {@link org.netbeans.spi.project.LookupProvider} implementors provide instances of mergeableClass.
 * {@link org.netbeans.spi.project.support.LookupProviderSupport#createCompositeLookup} handles the hiding of individual mergeable instances 
 * and exposing the merged instance created by the <code>LookupMerger</code>.
 * @param T the type of object being merged (see {@link org.netbeans.api.project.Project#getLookup} for examples)
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 */
public interface LookupMerger<T> {
    
    /**
     * Returns a class which is merged by this implementation of LookupMerger
     * @return Class instance
     */
    Class<T> getMergeableClass();
    
    /**
     * Merge instances of the given class in the given lookup and return merged 
     * object which substitutes them.
     * @param lookup lookup with the instances
     * @return object to be used instead of instances in the lookup
     */
    T merge(Lookup lookup);

}
