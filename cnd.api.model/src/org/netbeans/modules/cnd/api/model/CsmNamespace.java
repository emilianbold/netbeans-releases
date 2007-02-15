/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;
import java.util.List;

/**
 * Represents a "logical" namespace - not a particular namespace declaration,
 * but a join of all namespace declarations, which have thje given name
 * (see interface CsmNamespaceDeclaration)
 *
 * @author Vladimir Kvashin
 */
public interface CsmNamespace extends CsmQualifiedNamedElement, CsmScope, CsmIdentifiable<CsmNamespace> {

    CsmNamespace getParent();
    Collection/*<CsmNamespace>*/ getNestedNamespaces();

    /** Gets top-level objects */
    //TODO: what is the common ancestor for the namespace objects?
    Collection/*<CsmDeclaration>*/ getDeclarations();

    /**
     * Gets all definitions for this namespace
     */
    Collection/*<CsmNamespaceDefinition>*/ getDefinitions();
    
    //TODO: think over the relationship between projects and namespaces
    ///** Gets the project, to which this namespace belong */
    //CsmProject getProject();
    
    /** returns true if this is default namespace */
    boolean isGlobal();
    
    /** the project where the namespace (or it's particular part) is defined */
    CsmProject getProject();
}
