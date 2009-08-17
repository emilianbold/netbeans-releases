/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Endpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.netbeans.modules.compapp.projects.jbi.JbiConstants.*;

/**
 * Helper class to enhance endpoints in SE/BC SU descriptor with various
 * extensions.
 * 
 * @author jqian
 */
public class ServiceUnitDescriptorEnhancer {
    
    // See config extension module
    private static final String DISABLE_IN_BC = "disableInBC";
    private static final String CODEGEN = "codegen";
    private static final String ENDPOINT_CONFIG_NS = "http://www.sun.com/jbi/descriptor/config-endpoint";

    /**
     * Decorates consumes/provides endpoints using extension elements in CASA.
     * 
     * @param jbiDocument  plain SU JBI document
     * @param casaDocument CASA document
     */
    public static void decorateEndpoints(Document jbiDocument, Document casaDocument) {
        NodeList consumesNodeList = 
                jbiDocument.getElementsByTagName(JBI_CONSUMES_ELEM_NAME);
        for (int i = consumesNodeList.getLength() - 1; i >= 0; i--) {
            Element consumes = (Element) consumesNodeList.item(i);
            decorateEndpointElement(jbiDocument, consumes, casaDocument);
        }
        
        NodeList providesNodeList = 
                jbiDocument.getElementsByTagName(JBI_PROVIDES_ELEM_NAME);
        for (int i = providesNodeList.getLength() - 1; i >= 0; i--) {
            Element provides = (Element) providesNodeList.item(i);
            decorateEndpointElement(jbiDocument, provides, casaDocument);
        }
    }
    
    /**
     * Adds CASA extension elements to a consumes/provides endpoint into the
     * JBI document.
     * 
     * @param jbiDocument        plain SU JBI document
     * @param jbiEndpointElement a JBI consumes/provides element
     * @param casaDocument       CASA document
     */
    private static void decorateEndpointElement(Document jbiDocument, 
            Element jbiEndpointElement, 
            Document casaDocument) {
        
        if (casaDocument == null) {
            return;
        }
        
        boolean isConsumes = 
                jbiEndpointElement.getNodeName().equals(JBI_CONSUMES_ELEM_NAME);
        
        // 1. Find corresponding CASA consumes/provides element
        
        Endpoint endpoint = getEndpointInJBI(jbiEndpointElement);
        
        Element casaEndpointRefElement = CasaBuilder.getEndpointRefElement(
                casaDocument, endpoint, isConsumes);
        
        // 2. Copy child extension elements over from CASA to JBI
        if (casaEndpointRefElement != null) {            
            NodeList casaEndpointChildren = casaEndpointRefElement.getChildNodes();
            for (int k = 0; k < casaEndpointChildren.getLength(); k++) {
                Node casaEndpointChild = casaEndpointChildren.item(k);
                if (casaEndpointChild instanceof Element) {    
                    
                    // #136868 Remove disabled BC endpoint when applicable, 
                    // or skip cloning of this type of endpoint extension 
                    // 'cause runtime doesn't understand it.
                    Element casaEndpointExtension = (Element) casaEndpointChild;
                    String extName = casaEndpointExtension.getNodeName();
                    String nsValue = casaEndpointExtension.getAttribute("xmlns");
                    // The cloned extension elements in the new CASA document  
                    // are not namespace aware.
                    if (CODEGEN.equals(extName) && ENDPOINT_CONFIG_NS.equals(nsValue)) {
                        String disableInBCValue = casaEndpointExtension.getAttribute(DISABLE_IN_BC);
                        if ("true".equalsIgnoreCase(disableInBCValue)) {
                            jbiEndpointElement.getParentNode().removeChild(jbiEndpointElement);
                        }
                        continue;
                    }

                    Node clonedNode = CasaBuilder.deepCloneCasaNode(
                                    casaEndpointChild, jbiDocument);
                    jbiEndpointElement.appendChild(clonedNode);                    
                }
            }
        }
    }    
           
    /**
     * Gets an Endpoint object from an endpoint element in JBI DOM.
     */
    private static Endpoint getEndpointInJBI(Element jbiEndpointElement) {
        String endpointName = 
                jbiEndpointElement.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
        QName serviceQName = XmlUtil.getAttributeNSName(
                jbiEndpointElement, JBI_SERVICE_NAME_ATTR_NAME);
        QName interfaceQName = XmlUtil.getAttributeNSName(
                jbiEndpointElement, JBI_INTERFACE_NAME_ATTR_NAME);
        
        return new Endpoint(endpointName, serviceQName, interfaceQName); 
    }
}
