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


package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Catalog for persistence related schemas.
 *
 * @author Erno Mononen
 */
public class PersistenceCatalog implements CatalogReader, CatalogDescriptor, org.xml.sax.EntityResolver {
    
    private static final String PERSISTENCE_1_0_XSD = "persistence_1_0.xsd"; // NOI18N
    private static final String PERSISTENCE_1_0_URL = "nbres:org/netbeans/modules/j2ee/persistence/dd/resources/persistence_1_0.dtd"; // NOI18N
    private static final String PERSISTENCE_1_0 = "http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"; // NOI18N
    private static final String PERSISTENCE_1_0_ID = "SCHEMA:" + PERSISTENCE_1_0; // NOI18N
    
    public PersistenceCatalog() {
    }
    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (PERSISTENCE_1_0.equals(systemId)){
            return new org.xml.sax.InputSource(PERSISTENCE_1_0_URL);
        }
        if (systemId != null && systemId.endsWith(PERSISTENCE_1_0_XSD)){
            return new org.xml.sax.InputSource(PERSISTENCE_1_0_URL);
        }
        return null;
    }
    
    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(PERSISTENCE_1_0_ID);
        return list.iterator();
    }
    
    public void refresh() {
    }
    
    public String getSystemID(String publicId) {
        if (PERSISTENCE_1_0_ID.equals(publicId)){
            return PERSISTENCE_1_0_URL;
        }
        return null;
    }
    
    public String resolveURI(String name) {
        return null;
    }
    
    public String resolvePublic(String publicId) {
        return null;
    }
    
    public void addCatalogListener(CatalogListener l) {
    }
    
    public void removeCatalogListener(CatalogListener l) {
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/j2ee/persistence/dd/resources/persistenceCatalog.gif"); // NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(PersistenceCatalog.class, "LBL_PersistenceCatalog");
    }
    
    public String getShortDescription() {
        return NbBundle.getMessage(PersistenceCatalog.class, "DESC_PersistenceCatalog");
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
}
