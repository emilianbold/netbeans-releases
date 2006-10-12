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

import java.util.HashMap;
import org.netbeans.modules.xml.xam.locator.*;

/**
 *
 * @author girix
 */
public interface CatalogEntry {
    
    /**
     * entryType - Catalog entry type as in public, system, rewriteSystem, etc.
     **/
    public CatalogElement getEntryType();
    
    /**
     * one example of source is: systemId attribute value for system tag of catalog
     **/
    public String getSource();
    /**
     * one example of mappingEntity is: uri attribute value for system tag of catalog
     */
    public String getTarget();
    
    /**
     * If catalog is augmented with extra attributes, then the key of the HashMap will
     * be the attribute key and the value of the HashMap entry will be the value of the 
     * attribute
     */
    public HashMap<String,String> getExtraAttributeMap();
    
    
    /**
     * If this catalog entry is does not resolve to a file then valid=false
     */
    public boolean isValid();
    
}
