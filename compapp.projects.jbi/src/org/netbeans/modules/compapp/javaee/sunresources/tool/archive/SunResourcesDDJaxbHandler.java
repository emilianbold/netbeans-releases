/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
