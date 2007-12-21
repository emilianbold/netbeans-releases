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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.netbeans.modules.compapp.projects.jbi.api.JbiExtensionAttribute;
import org.netbeans.modules.compapp.projects.jbi.api.JbiExtensionElement;
import org.netbeans.modules.compapp.projects.jbi.api.JbiExtensionInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiInstalledExtensionInfo;
import org.openide.nodes.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class ExtensionPropertyHelper {
          
    private static Logger logger = Logger.getLogger(
            "org.netbeans.modules.compapp.casaeditor.nodes.ExtensionPropertyHelper"); // NOI18N
           
    private static Map<String, Class> classMap = new HashMap<String, Class>();
    
    static {
        classMap.put("String", String.class); // NOI18N
        classMap.put("Integer", Integer.class); // NOI18N
        classMap.put("QName", QName.class); // NOI18N
    }
    
    private static String EXTENSION_TARGET_ALL = "all"; // NOI18N
    
    /**
     * Sets up property sheet for a CASA extension point component's 
     * extensibility elements.
     * 
     * @param node              a CASA node
     * @param casaExtensionPointComponent  a CASA component which is an 
     *                          extension point, for example, a CASA 
     *                          consumes/provides.
     * @param sheet             the overall property sheet
     * @param extensionType     the type of the extension, 
     *                          e.x., "endpoint" or "connection"
     * @param extensionTarget   target of the extension, 
     *                          e.x., "sun-http-binding" or "all"
     */
    public static void setupExtensionPropertySheet(CasaNode node,
            CasaComponent casaExtensionPointComponent,
            Sheet sheet, 
            String extensionType, 
            String extensionTarget) {
        
        JbiInstalledExtensionInfo installedExtInfo = 
                JbiInstalledExtensionInfo.getInstalledExtensionInfo();
        
        // Assumptions: 
        // * Each extension (subtree) in the CASA model is either complete or
        //   doesn't exist at all.
        
        Set<QName> existingTopEEQNames = new HashSet<QName>();
        
        // 1. for top level extensibility elements existing in the CASA model 
        for (CasaExtensibilityElement ee : 
                casaExtensionPointComponent.getExtensibilityElements()) {
            // ee:  <config:application-config name="FOO"/>
            QName eeQName = ee.getQName();
            
            existingTopEEQNames.add(eeQName);
            
            String eeNamespace = eeQName.getNamespaceURI();
            String eeLocalName = eeQName.getLocalPart();

            for (JbiExtensionInfo extInfo : installedExtInfo.getJbiExtensionList()) {
                if (!(extInfo.getNameSpace().equals(eeNamespace)) ||
                        !(extensionType.equals(extInfo.getType()))) {
                    continue;
                }
                
                String extInfoTarget = extInfo.getTarget();
                if (!(extensionTarget.equals(extInfoTarget)) && 
                         !(EXTENSION_TARGET_ALL.equals(extInfoTarget))) {
                    continue;
                }
                
                for (JbiExtensionElement extElement : extInfo.getElements()) {
                    if (extElement.getName().equals(eeLocalName)) {
                        Sheet.Set extPropertySet = 
                                node.getPropertySet(sheet, extInfo.getName());
                        createExistingProperties(node, extElement, 
                                extPropertySet, casaExtensionPointComponent, 
                                ee, ee);
                        break;
                    }
                }
            }
        }
        
        // 2. for top level extensibility elements that do not exist 
        //    in the CASA model yet
        for (JbiExtensionInfo extInfo : installedExtInfo.getJbiExtensionList()) {
            logger.fine(extInfo.toString());
            
            if (!(extensionType.equals(extInfo.getType()))) {
                continue;
            }            
             
            String extInfoTarget = extInfo.getTarget();
            if (!(extensionTarget.equals(extInfoTarget)) && 
                     !(EXTENSION_TARGET_ALL.equals(extInfoTarget))) {
                continue;
            }
            
            // For each extension, create a new property sheet
            Sheet.Set extPropertySet = node.getPropertySet(sheet, extInfo.getName());
            
            String namespace = extInfo.getNameSpace();

            for (JbiExtensionElement extElement : extInfo.getElements()) {
                QName qname = new QName(namespace, extElement.getName());
                if (!existingTopEEQNames.contains(qname)) {
                    // extElement doesn't have a corresponding CASA 
                    // extensibility element yet               
                    createNonExistingProperties(node, extElement, extPropertySet, 
                            casaExtensionPointComponent, null, null, namespace);
                }
            }
        }
    }
        
    private static void createNonExistingProperties(CasaNode node,
            JbiExtensionElement extElement,
            Sheet.Set extSheetSet,
            CasaComponent casaExtensionPointComponent, 
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String namespace) {
        
        Document document = casaExtensionPointComponent.getPeer().getOwnerDocument(); 
        CasaWrapperModel casaModel = (CasaWrapperModel) casaExtensionPointComponent.getModel();
        CasaComponentFactory casaFactory = casaModel.getFactory();

        Element domElement = document.createElementNS(namespace, extElement.getName());
        
        CasaExtensibilityElement newEE = (CasaExtensibilityElement) 
                casaFactory.create(domElement, casaExtensionPointComponent);
        
        if (firstEE == null) {
            firstEE = newEE;
        }        
        
        if (lastEE != null) {
            lastEE.addAnyElement(newEE, lastEE.getAnyElements().size());
        }
        lastEE = newEE;
                
        List<JbiExtensionAttribute> attributes = extElement.getAttributes();
        if (attributes != null) {
            for (JbiExtensionAttribute attr : extElement.getAttributes()) {
                String attrName = attr.getName();
                String attrType = attr.getType();
                String attrDescription = attr.getDescription();
                
                lastEE.setAttribute(attrName, ""); // NOI18N
                
                PropertyUtils.installExtensionProperty(
                    extSheetSet, node, casaExtensionPointComponent, 
                    firstEE, lastEE,
                    CasaNode.ALWAYS_WRITABLE_PROPERTY, classMap.get(attrType), 
                    attrName, attrName, attrDescription);
            }
        } 
        
        List<JbiExtensionElement> childExtElements = extElement.getElements();
        if (childExtElements != null) {
            for (JbiExtensionElement childElement : extElement.getElements()) {
                createNonExistingProperties(node, childElement, extSheetSet, 
                        casaExtensionPointComponent, firstEE, lastEE, namespace);
            }
        }
    }
                
    private static void createExistingProperties(CasaNode node,
            JbiExtensionElement extElement,
            Sheet.Set extSheetSet,
            CasaComponent casaExtensionPointComponent, 
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE) {
        
        List<JbiExtensionAttribute> attributes = extElement.getAttributes();
        if (attributes != null) {
            for (JbiExtensionAttribute attr : attributes) {
                String attrName = attr.getName();
                String attrType = attr.getType();
                String attrDescription = attr.getDescription();
                PropertyUtils.installExtensionProperty(
                    extSheetSet, node, casaExtensionPointComponent, 
                    firstEE, lastEE,
                    CasaNode.ALWAYS_WRITABLE_PROPERTY, classMap.get(attrType), 
                    attrName, attrName, attrDescription);
            }
        } 
        
        List<JbiExtensionElement> childExtElements = extElement.getElements();
        if (childExtElements != null) {
            for (CasaExtensibilityElement ee : lastEE.getExtensibilityElements()) {
                String eeName = ee.getPeer().getNodeName();
                boolean found = false;
                for (JbiExtensionElement childExtElement : extElement.getElements()) {
                    if (eeName.equals(childExtElement.getName())) {
                        createExistingProperties(node, childExtElement, 
                                extSheetSet, casaExtensionPointComponent, 
                                firstEE, ee);
                        found = true;
                        break;
                    }
                }                
                assert found;
            }
        }
    }
}
