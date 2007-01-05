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
package org.netbeans.modules.xml.retriever.catalog.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

public enum CatalogQNames {
    CATALOG("catalog"),
    SYSTEM("system"),
    NEXTCATALOG("nextCatalog");//,
    
    /*SYSTEMID("systemId"),
    URI("uri"),
    XPROJECT_CATALOGFILE_LOCATION("xprojectCatalogFileLocation"),
    REFERENCING_FILE("referencingFile");*/
    
    
    public static final String CATALOG_NS = "urn:oasis:names:tc:entity:xmlns:xml:catalog";
    public static final String CATALOG_PREFIX = "cat";
    
    private static Set<QName> mappedQNames = new HashSet<QName>();
    static {
        mappedQNames.add(CATALOG.getQName());
        mappedQNames.add(SYSTEM.getQName());
        mappedQNames.add(NEXTCATALOG.getQName());
        
        /*mappedQNames.add(SYSTEMID.getQName());
        mappedQNames.add(URI.getQName());
        mappedQNames.add(XPROJECT_CATALOGFILE_LOCATION.getQName());
        mappedQNames.add(REFERENCING_FILE.getQName());*/
    }

    private QName qname;
    
    CatalogQNames(String localName) {
        qname = new QName(CATALOG_NS, localName, CATALOG_PREFIX);
    }
    
    public QName getQName() { 
        return qname; 
    }

    public String getLocalName() { 
        return qname.getLocalPart();
    }
    
    public String getQualifiedName() {
        return qname.getPrefix() + ":" + qname.getLocalPart();
    }
    
    public static Set<QName> getMappedQNames() {
        return Collections.unmodifiableSet(mappedQNames);
    }
}
