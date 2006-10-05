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
 * interface for inclusion of 3rd party content in project's lookup. Typically, if the 
 * project type allows composition of lookup from multiple sources, it will make a layer
 * location public where 3rd parties will register implementations of this interface.
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 */
public interface LookupProvider {
    
    /**
     * implementations will be asked to create their additional project lookup based on the baseContext
     * passed as parameter. The content of baseLookup is undefined on this level, is a contract
     * of the actual project type. Can be complete lookup of the project type, a portion of it or
     * something completely different that won't appear in the final project lookup.
     * Each implementation is only asked once for it's lookup for a given project instance at the time 
     * when project's lookup is being created.
     * @param baseContext implementation shall decide what to return for a given project instance based on context
     *  passed in.
     * @return a {@link org.openide.util.Lookup} instance that is to be added to the project's lookup, never null.
     */ 
    Lookup createAdditionalLookup(Lookup baseContext);
}
