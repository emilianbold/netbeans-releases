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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ddloaders.catalog;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A catalog that provides web, application, web services, web services client, javaee, j2ee, 
 * ejb-jar and application client deployment descriptor schemas for code completion and validation.
 * 
 * @author Erno Mononen
 */
public final class EnterpriseCatalog implements CatalogReader, CatalogDescriptor, EntityResolver  {

    private static final String J2EE_NS = "http://java.sun.com/xml/ns/j2ee"; //NOI18N
    private static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee"; //NOI18N
    private static final String RESOURCE_PATH = "nbres:/org/netbeans/modules/j2ee/ddloaders/catalog/resources/"; //NO18N 
    
    private List<SchemaInfo> schemas = new ArrayList<SchemaInfo>();
    
    public EnterpriseCatalog() {
        initialize();
    }

    private void initialize(){
        // application-client
        schemas.add(new SchemaInfo("application-client_1_4.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("application-client_5.xsd", JAVAEE_NS));
        // application
        schemas.add(new SchemaInfo("application_1_4.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("application_5.xsd", JAVAEE_NS));
        // web services
        schemas.add(new SchemaInfo("j2ee_web_services_1_1.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("javaee_web_services_1_2.xsd", JAVAEE_NS));
        // web services client
        schemas.add(new SchemaInfo("j2ee_web_services_client_1_1.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("javaee_web_services_client_1_2.xsd", JAVAEE_NS));
        // JCA connector 1.5
        schemas.add(new SchemaInfo("connector_1_5.xsd", J2EE_NS));
        // ejb-jar
        schemas.add(new SchemaInfo("ejb-jar_2_1.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("ejb-jar_3_0.xsd", JAVAEE_NS));
        // web-app
        schemas.add(new SchemaInfo("web-app_2_4.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("web-app_2_5.xsd", JAVAEE_NS));
        // jsp
        schemas.add(new SchemaInfo("jsp_2_0.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("jsp_2_1.xsd", JAVAEE_NS));
        // j2ee and java ee schemas
        schemas.add(new SchemaInfo("j2ee_1_4.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("javaee_5.xsd", JAVAEE_NS));
        // web 2.2 and 2.3 dtds
        schemas.add(new SchemaInfo("web-app_2_2.dtd", "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN", true)); //NO18N
        schemas.add(new SchemaInfo("web-app_2_3.dtd", "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN", true)); //NO18N
       
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/j2ee/ddloaders/catalog/resources/DDCatalog.gif"); // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage (EnterpriseCatalog.class, "LBL_EnterpriseCatalog");
    }

    public String getShortDescription() {
        return NbBundle.getMessage (EnterpriseCatalog.class, "DESC_EnterpriseCatalog");
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
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

    /**
     * A simple holder for the information needed
     * for resolving the resource path and public id of a schema.
     */ 
    private static class SchemaInfo {
        
        private final String schemaName;
        private final String namespace;
        private final boolean dtd;

        public SchemaInfo(String schemaName, String namespace) {
            this(schemaName, namespace, false);
        }

        public SchemaInfo(String schemaName, String namespace, boolean dtd){
            this.schemaName = schemaName;
            this.namespace = namespace;
            this.dtd = dtd;
        }
        
        public String getResourcePath() {
            return RESOURCE_PATH + getSchemaName();
        }

        public String getSchemaName() {
            return schemaName;
        }
        
        public String getPublicId(){
            if (dtd){
                return namespace;
            }
            return "SCHEMA:" + namespace + "/" + schemaName; //NO18N
        }
        
    }
}
