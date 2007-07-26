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
 */

package org.netbeans.modules.compapp.javaee.sunresources.tool.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.*;


/**
 * @author echou
 *
 */
public class SunResourcesDDJaxbHandler {

    private File resourceFile;
    private JAXBContext jc;
    private Resources resources;
    private Marshaller marshaller;
    
    public SunResourcesDDJaxbHandler(File file) throws Exception {
        this.resourceFile = file;
        
        jc = JAXBContext.newInstance("com.sun.wasilla.jaxb.sunresources13", // NOI18N
                this.getClass().getClassLoader());
        boolean unmarshalSuccess = true;
        // unmarshal it to memory
        try {
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            resources = (Resources) unmarshaller.unmarshal(resourceFile);
        } catch (Exception e) {
            unmarshalSuccess = false;
        }
        
        // if unable to marshal xml, then create a new empty one
        if(!unmarshalSuccess){
            // construct JAXB tree
            ObjectFactory factory = new ObjectFactory();
            resources = factory.createResources();
        }
        
    }
    
    public boolean containsResource(String jndiName) {
        for (Iterator<Object> iter = resources.getCustomResourceOrExternalJndiResourceOrJdbcResourceOrMailResourceOrPersistenceManagerFactoryResourceOrAdminObjectResourceOrConnectorResourceOrResourceAdapterConfigOrJdbcConnectionPoolOrConnectorConnectionPool().iterator(); 
            iter.hasNext(); ) {
            Object obj = iter.next();
            if (obj instanceof CustomResource) {
                CustomResource customResource = (CustomResource) obj;
                if (customResource.getJndiName().equals(jndiName)) {
                    return true;
                }
            } else if (obj instanceof ExternalJndiResource) {
                ExternalJndiResource externalJndiResource = (ExternalJndiResource) obj;
                if (externalJndiResource.getJndiName().equals(jndiName)) {
                    return true;
                }
            } else if (obj instanceof JdbcResource) {
                JdbcResource jdbcResource = (JdbcResource) obj;
                if (jdbcResource.getJndiName().equals(jndiName)) {
                    return true;
                }
            } else if (obj instanceof MailResource) {
                MailResource mailResource = (MailResource) obj;
                if (mailResource.getJndiName().equals(jndiName)) {
                    return true;
                }
            } else if (obj instanceof PersistenceManagerFactoryResource) {
                PersistenceManagerFactoryResource pmfResource = (PersistenceManagerFactoryResource) obj;
                if (pmfResource.getJndiName().equals(jndiName)) {
                    return true;
                }
            } else if (obj instanceof AdminObjectResource) {
                AdminObjectResource adminObjectResource = (AdminObjectResource) obj;
                if (adminObjectResource.getJndiName().equals(jndiName)) {
                    return true;
                }
            } else if (obj instanceof ConnectorResource) {
                ConnectorResource connectorResource = (ConnectorResource) obj;
                if (connectorResource.getJndiName().equals(jndiName)) {
                    return true;
                }
            }
            /*
            else if (obj instanceof ResourceAdapterConfig) {
                
            } else if (obj instanceof JdbcConnectionPool) {
                
            } else if (obj instanceof ConnectorConnectionPool) {
                
            }
            */
            
        }
        
        return false;
    }
    
    public void saveXML() throws Exception {
        if (marshaller == null) {
            marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE); // NOI18N
        }
        // write preamble
        FileOutputStream fos = new FileOutputStream(resourceFile);
        PrintWriter out = new PrintWriter(fos);
        try {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
            out.println("<!DOCTYPE resources PUBLIC \"-//Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions //EN\" \"http://www.sun.com/software/appserver/dtds/sun-resources_1_3.dtd\">"); // NOI18N
            marshaller.marshal(resources, out);
        } finally {
            FileUtil.safeclose(out);
            FileUtil.safeclose(fos);
        }
    }

    public void addAdminObjectResource(String jndiName, String resType, 
            String resAdapter, Properties adminObjProps) {
        AdminObjectResource adminObjResource = new AdminObjectResource();
        adminObjResource.setJndiName(jndiName);
        adminObjResource.setResType(resType);
        adminObjResource.setResAdapter(resAdapter);
        for (Enumeration<?> e = adminObjProps.propertyNames(); e.hasMoreElements(); ) {
            String propName = (String) e.nextElement();
            String propValue = adminObjProps.getProperty(propName);
            Property p = new Property();
            p.setName(propName);
            p.setValue(propValue);
            adminObjResource.getProperty().add(p);
        }
        resources.getCustomResourceOrExternalJndiResourceOrJdbcResourceOrMailResourceOrPersistenceManagerFactoryResourceOrAdminObjectResourceOrConnectorResourceOrResourceAdapterConfigOrJdbcConnectionPoolOrConnectorConnectionPool().add(adminObjResource);
    }
    
    public Resources getResources() {
        return this.resources;
    }
}
