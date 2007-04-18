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

import java.net.URL;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.netbeans.modules.compapp.javaee.sunresources.generated.webservices11.*;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode;
import org.openide.util.NbBundle;

/**
 * @author echou
 *
 */
public class WebservicesDDJaxbHandler {

    EJBArchive ejbArchive;
    private WebservicesType root;
    
    public WebservicesDDJaxbHandler(EJBArchive ejbArchive, Object root) throws Exception {
        this.ejbArchive = ejbArchive;
        if (root instanceof WebservicesType) {
            this.root = (WebservicesType) root;
        } else {
            throw new Exception(
                    NbBundle.getMessage(WebservicesDDJaxbHandler.class, "EXC_bad_jaxbroot", root.getClass()));
        }
    }

    /*
     * resolve webservice description by reading the webservices.xml and WSDL file
     */
    public void resolvePortCompName(CMapNode node, java.lang.String portCompName) {
        for (Iterator<WebserviceDescriptionType> wsDescIter = root.getWebserviceDescription().iterator();
            wsDescIter.hasNext(); ) {
            WebserviceDescriptionType wsDesc = wsDescIter.next();
            for (Iterator<PortComponentType> portCompIter = wsDesc.getPortComponent().iterator();
                portCompIter.hasNext(); ) {
                PortComponentType portComp = portCompIter.next();
                if (portComp.getPortComponentName().getValue().equals(portCompName)) {
                    node.getProps().setProperty("wsdlLocation", // NOI18N
                            wsDesc.getWsdlFile().getValue());
                    java.lang.String mappingFile = wsDesc.getJaxrpcMappingFile().getValue();
                    try {
                        /*
                        URL mappingFileURL = ejbArchive.getJarURL(mappingFile);
                        JAXBContext jc = JAXBContext.newInstance("com.sun.wasilla.jaxb.jaxrpcmapping11");
                        Unmarshaller u = jc.createUnmarshaller();
                        JAXBElement<?> root = (JAXBElement<?>) u.unmarshal(mappingFileURL);
                        
                        // resolve using jaxrpc-mapping-file
                        JaxrpcMappingJaxbHandler mappingHandler = 
                            new JaxrpcMappingJaxbHandler(root.getValue());
                        mappingHandler.resolveWsdlMapping(node);
                        */
                    } catch (Exception e) {
                        System.out.println("Error when reading jaxrpc-mapping file: " + mappingFile);
                        continue;
                    }
                    return;
                }
            }
        }
    }
}
