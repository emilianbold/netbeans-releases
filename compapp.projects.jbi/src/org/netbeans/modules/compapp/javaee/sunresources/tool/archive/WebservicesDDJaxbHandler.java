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
