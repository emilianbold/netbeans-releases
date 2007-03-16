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
package org.netbeans.modules.xml.retriever.catalog;

import org.netbeans.modules.xml.xam.locator.*;

/**
 *
 * @author girix
 */
public enum CatalogAttribute {
    /*
     * Attribute on the catalog node
     */
    xmlns,
    
    /**
     * Attribute on all elements
     */
    id,
    /**
     * Key for 
     * - group 
     * - catalog
     */
    prefer,
    /**
     * Key for 
     * - public
     */
    publicId,
    /** 
     * Key for 
     * - system
     */
    systemId,
    /**
     * Value for 
     * - public
     * - system 
     * - uri
     */
    uri,
    /**
     * Key for 
     * - rewriteSystem 
     * - delegateSystem
     */
    systemIdStartString,
    /**
     * Value for 
     * - rewriteSystem 
     * - rewriteURI
     */
    rewritePrefix,
    /**
     * Key for 
     * - delegatePublic
     */
    publicIdStartString,
    /**
     * Value for 
     * -delegatePublic
     * -delegateSystem
     * -nextCatalog
     */
    catalog,
    /**
     * Key for 
     * - uri
     */
    name,
    /**
     * Key for 
     * - rewriteURI
     * - delegateURI
     */
    uriStartString,
    
    /*
     * Key for storing the Original URI of the local/internet resource
     * - not in the catalog definition
     */
    originalResourcePointer,
    
    /* 
     *Attribute for cross project resource's catalog file location
     */
    xprojectCatalogFileLocation,
    
    /*
     *Attribute for storing reference back to the resource that created this cross
     *project catalog entry.
     */
    referencingFiles;
}
