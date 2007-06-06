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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.installer.product.dependencies;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Version;

/**
 *
 * @author Dmitry Lipin
 */
public class Requirement extends Dependency {
    public static final String NAME = "requirement"; //NOI18N
    private List <List <Requirement>> alternatives ;
    
    public Requirement(
            final String uid,
            final Version versionLower,
            final Version versionUpper,
            final Version versionResolved) {
        this(uid,versionLower,versionUpper,versionResolved,
                new ArrayList <List <Requirement>>());
    }
    public Requirement(
            final String uid,
            final Version versionLower,
            final Version versionUpper,
            final Version versionResolved,
            final List <List <Requirement>> altRequirements) {
        super(uid,versionLower,versionUpper,versionResolved);
        alternatives = new ArrayList <List <Requirement>> (altRequirements);
    }
    
   
    public String getName() {
        return NAME;
    }
    
    public boolean satisfies(Product product) {
        if (getVersionResolved() != null) {
            return product.getUid().equals(getUid()) &&
                    product.getVersion().equals(getVersionResolved());
            
        }
        
        // if the requirement is not resolved, we check uid equality and
        // upper/lower version compatibility
        return product.getUid().equals(getUid()) &&
                product.getVersion().newerOrEquals(getVersionLower()) &&
                product.getVersion().olderOrEquals(getVersionUpper());        
    }   

    public List<List<Requirement>> getAlternatives() {
        return alternatives;
    }

}
