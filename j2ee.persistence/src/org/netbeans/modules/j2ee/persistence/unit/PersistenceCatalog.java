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
    
    private static final String PERSISTENCE_NS = "http://java.sun.com/xml/ns/persistence"; // NOI18N
    private static final String ORM_NS = PERSISTENCE_NS +  "/orm"; // NOI18N
    private static final String RESOURCE_PATH = "nbres:org/netbeans/modules/j2ee/persistence/dd/resources/"; //NO18N 
    
    private List<SchemaInfo> schemas = new ArrayList<SchemaInfo>();

    public PersistenceCatalog() {
        initialize();
    }

    private void initialize(){
        // persistence
        schemas.add(new SchemaInfo("persistence_1_0.xsd", RESOURCE_PATH, PERSISTENCE_NS));
        // orm
        schemas.add(new SchemaInfo("orm_1_0.xsd", RESOURCE_PATH, ORM_NS));
    }
    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (systemId == null){
            return null;
        }
        for (SchemaInfo each : schemas){
            if (systemId.endsWith(each.getSchemaName())){
                return new InputSource(each.getResourcePath());
            }
        }
        return null;
    }
    
    public Iterator getPublicIDs() {
        List<String> result = new ArrayList<String>();
        for (SchemaInfo each : schemas){
            result.add(each.getPublicId());
        }
        return result.iterator();
    }
    
    public void refresh() {
    }
    
    public String getSystemID(String publicId) {
        if (publicId == null){
            return null;
        }
        for (SchemaInfo each : schemas){
            if (each.getPublicId().equals(publicId)){
                return each.getResourcePath();
            }
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

    /**
     * A simple holder for the information needed
     * for resolving the resource path and public id of a schema.
     * <i>copied from j2ee/ddloaders EnterpriseCatalog</i>
     */ 
    private static class SchemaInfo {
        
        private final String schemaName;
        private final String resourcePath;
        private final String namespace;

        public SchemaInfo(String schemaName, String resourcePath, String namespace) {
            this.schemaName = schemaName;
            this.resourcePath = resourcePath + schemaName;
            this.namespace = namespace;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public String getSchemaName() {
            return schemaName;
        }
        
        public String getPublicId(){
            return "SCHEMA:" + namespace + "/" + schemaName; //NO18N
        }
        
    }
    
}
