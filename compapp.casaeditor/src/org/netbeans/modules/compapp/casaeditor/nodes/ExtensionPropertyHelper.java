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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.netbeans.modules.compapp.projects.jbi.api.JbiChoiceExtensionElement;
import org.netbeans.modules.compapp.projects.jbi.api.JbiExtensionAttribute;
import org.netbeans.modules.compapp.projects.jbi.api.JbiExtensionElement;
import org.netbeans.modules.compapp.projects.jbi.api.JbiExtensionInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiInstalledExtensionInfo;
import org.openide.nodes.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Helper class to set up property sheet for CASA conriguraton extensions.
 * 
 * @author jqian
 */
public class ExtensionPropertyHelper {

    private static Logger logger = Logger.getLogger(
            "org.netbeans.modules.compapp.casaeditor.nodes.ExtensionPropertyHelper"); // NOI18N

//    private static String EXTENSION_TARGET_ALL = "all"; // NOI18N


    /**
     * Sets up property sheet for a CASA extension point component's 
     * extensibility elements.
     * 
     * @param node              a CASA node
     * @param casaExtPoint      a CASA component which is an extension point, 
     *                          for example, casa:connection
     * @param sheet             the overall property sheet
     * @param extensionType     the type of the extension, 
     *                          e.x., "endpoint" or "connection"
     * @param extensionTarget   target component of the extension,
     *                          e.x., "sun-http-binding" or ".*"
     */
    public static void setupExtensionPropertySheet(CasaNode node,
            CasaComponent casaExtPoint,
            Sheet sheet,
            String extensionType,
            String extensionTarget) {

        JbiInstalledExtensionInfo installedExtInfo =
                JbiInstalledExtensionInfo.getInstalledExtensionInfo();

        Document document = casaExtPoint.getPeer().getOwnerDocument();
        
        // Assumptions: 
        // * Each extension (subtree) in the CASA model is either complete or
        //   doesn't exist at all.

        Set<QName> existingTopEEQNames = new HashSet<QName>();

        // 1. for top level extensibility elements existing in the CASA model 
        for (CasaExtensibilityElement ee : casaExtPoint.getExtensibilityElements()) {
            // ee:  <config:application-config name="FOO"/>
            QName eeQName = ee.getQName();

            existingTopEEQNames.add(eeQName);

            String eeNamespace = eeQName.getNamespaceURI();
            String eeLocalName = eeQName.getLocalPart();

            List<JbiExtensionInfo> extInfoList = installedExtInfo.getJbiExtensionList();
            Collections.sort(extInfoList, new Comparator<JbiExtensionInfo>() {

                public int compare(JbiExtensionInfo o1, JbiExtensionInfo o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (JbiExtensionInfo extInfo : extInfoList) {
                if (!(extInfo.getNameSpace().equals(eeNamespace)) ||
                        !(extensionType.equals(extInfo.getType()))) {
                    continue;
                }

                String extInfoTarget = extInfo.getTarget();
                if (!Pattern.matches(extInfoTarget, extensionTarget)) {
//                if (!(extensionTarget.equals(extInfoTarget)) &&
//                        !(EXTENSION_TARGET_ALL.equals(extInfoTarget))) {
                    continue;
                }

                String namespace = extInfo.getNameSpace();

                for (JbiExtensionElement extElement : extInfo.getElements()) {
                    if (extElement.getName().equals(eeLocalName)) {
                        Sheet.Set extPropertySet =
                                node.getPropertySet(sheet, extInfo.getDisplayName());
                        extPropertySet.setShortDescription(extInfo.getDescription());
                        createExistingProperties(node, document, extElement,
                                extPropertySet, casaExtPoint,
                                ee, ee, namespace, extInfo.getProvider());
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
            if (!Pattern.matches(extInfoTarget, extensionTarget)) {
//            if (!(extensionTarget.equals(extInfoTarget)) &&
//                    !(EXTENSION_TARGET_ALL.equals(extInfoTarget))) {
                continue;
            }

            // For each extension, create a new property sheet
            Sheet.Set extPropertySet = node.getPropertySet(sheet, extInfo.getDisplayName());
            extPropertySet.setShortDescription(extInfo.getDescription());

            String namespace = extInfo.getNameSpace();

            for (JbiExtensionElement extElement : extInfo.getElements()) {
                QName qname = new QName(namespace, extElement.getName());
                if (!existingTopEEQNames.contains(qname)) {
                    // extElement doesn't have a corresponding CASA 
                    // extensibility element yet               
                    createNonExistingProperties(node, document,
                            extElement, extPropertySet,
                            casaExtPoint, null, null, namespace, extInfo.getProvider(), true);
                }
            }
        }
    }

    /**
     * Creates a CASA extensibility element for the given 
     * <code>JbiExtensionElement</code> instance; installs attribute properties 
     * and choice properties into the node's property sheet.
     * 
     * @param node
     * @param document
     * @param extElement
     * @param extSheetSet
     * @param casaExtPoint
     * @param firstEE
     * @param lastEE
     * @param namespace
     * @param install       whether to install properties for attributes of the 
     *                      current or descendent JbiExtensionElements into
     *                      the property sheet 
     * 
     * @return  a newly constructed extensibility element
     */
    private static CasaExtensibilityElement createNonExistingProperties(
            CasaNode node,
            Document document,
            JbiExtensionElement extElement,
            Sheet.Set extSheetSet,
            CasaComponent casaExtPoint,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String namespace,
            String provider,
            boolean install) {

        CasaWrapperModel casaModel = (CasaWrapperModel) casaExtPoint.getModel();
        CasaComponentFactory casaFactory = casaModel.getFactory();

        String extElementName = extElement.getName();
        String extElementDisplayName = extElement.getDisplayName();
        String extElementDescription = extElement.getDescription();
        Element domElement = document.createElementNS(namespace, extElementName);

        CasaExtensibilityElement newEE = (CasaExtensibilityElement) casaFactory.create(domElement, casaExtPoint);

        if (firstEE == null) {
            firstEE = newEE;
        }

        if (lastEE != null) {
            lastEE.addAnyElement(newEE, lastEE.getAnyElements().size());
        }
        lastEE = newEE;

        if (extElement instanceof JbiChoiceExtensionElement) {
            // Note that multi-level choice element is not supported for now.

            JbiChoiceExtensionElement choiceExtElement =
                    (JbiChoiceExtensionElement) extElement;
            String defaultChoice = choiceExtElement.getDefaultChoice();

            // Build choice map.
            Map<String, CasaExtensibilityElement> choiceMap =
                    new HashMap<String, CasaExtensibilityElement>(); 
            
            // Build a map mapping choice element name to display name
            // with the order of choices in the layer preserved.
            Map<String, String> choiceDisplayNameMap =
                    new LinkedHashMap<String, String>();
            
            for (JbiExtensionElement childElement : extElement.getElements()) {
                CasaExtensibilityElement childEE =
                        createNonExistingProperties(node, document,
                        childElement, extSheetSet,
                        lastEE, null, null, namespace, provider, false);
                String name = childElement.getName();
                String displayName = childElement.getDisplayName();
                choiceMap.put(name, childEE);
                choiceDisplayNameMap.put(name, displayName);
            }

            // Add an artificial property for the choice extension element.
            PropertyUtils.installChoiceExtensionProperty(
                    extSheetSet, node, casaExtPoint,
                    firstEE, lastEE,
                    CasaNode.ALWAYS_WRITABLE_PROPERTY,
                    String.class,
                    extElementName, extElementDisplayName,
                    extElementDescription,
                    choiceMap,
                    choiceDisplayNameMap,
                    defaultChoice);

        } else {
            // Add properties for attributes of the current extenstion element.
            List<JbiExtensionAttribute> attributes = extElement.getAttributes();
            if (attributes != null) {
                for (JbiExtensionAttribute attr : extElement.getAttributes()) {
                    String attrName = attr.getName();
                    String attrDisplayName = attr.getDisplayName();
                    String attrType = attr.getType();
                    String attrDescription = attr.getDescription();
                    boolean codeGen = attr.getCodeGen();

                    if (codeGen) {
                        lastEE.setAttribute(attrName, ""); // NOI18N

                    }

                    if (install) {
                        PropertyUtils.installExtensionProperty(
                                extSheetSet, node, casaExtPoint,
                                firstEE, lastEE,
                                CasaNode.ALWAYS_WRITABLE_PROPERTY, attrType,
                                attrName, attrDisplayName, attrDescription, provider);
                    }
                }
            }

            // Add properties for child extension elements.
            List<JbiExtensionElement> childExtElements = extElement.getElements();
            if (childExtElements != null) {
                for (JbiExtensionElement childElement : childExtElements) {
                    createNonExistingProperties(node, document,
                            childElement, extSheetSet,
                            casaExtPoint, firstEE, lastEE, namespace, provider,
                            true);
                }
            }
        }

        return newEE;
    }

    private static void createExistingProperties(CasaNode node,
            Document document,
            JbiExtensionElement extElement,
            Sheet.Set extSheetSet,
            CasaComponent casaExtPoint,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String namespace,
            String provider) {

        // Add a property for choice extension element.
        if (extElement instanceof JbiChoiceExtensionElement) {

            JbiChoiceExtensionElement choiceExtElement =
                    (JbiChoiceExtensionElement) extElement;
            String defaultChoice = choiceExtElement.getDefaultChoice();

            String elementName = extElement.getName();
            String elementDisplayName = extElement.getDisplayName();
            String elementDescription = extElement.getDescription();

            CasaExtensibilityElement currentChild = null;
            String currentChildName = null;

            List<CasaExtensibilityElement> currentChildren =
                    lastEE.getExtensibilityElements();
            if (currentChildren != null && currentChildren.size() > 0) {
                assert currentChildren.size() == 1;
                currentChild = currentChildren.get(0);
                currentChildName = currentChild.getQName().getLocalPart();
            }

            // Build choice map.
            Map<String, CasaExtensibilityElement> choiceMap =
                    new HashMap<String, CasaExtensibilityElement>();  
            
            // Build a map mapping choice element name to display name 
            // with the order of choices defined in the layer preserved.
            Map<String, String> choiceDisplayNameMap =
                    new LinkedHashMap<String, String>();
            
            List<JbiExtensionElement> childExtElements = extElement.getElements();
            if (childExtElements != null) {
                // Add potential children defined in the layer.
                for (JbiExtensionElement childElement : childExtElements) {
                    String childElementName = childElement.getName();
                    if (!childElementName.equals(currentChildName)) {
                        CasaExtensibilityElement childEE =
                                createNonExistingProperties(node, document,
                                childElement, extSheetSet,
                                casaExtPoint, null, null, namespace, provider, false);
                        choiceMap.put(childElementName, childEE);
                    }
                        
                    String childDisplayName = childElement.getDisplayName();
                    choiceDisplayNameMap.put(childElementName, childDisplayName);
                }
            }

            if (currentChild != null) {
                // Add current child by cloning the existing one.
                CasaExtensibilityElement clonedExistingChildEE =
                        (CasaExtensibilityElement) currentChild.copy(lastEE);
                choiceMap.put(currentChildName, clonedExistingChildEE);
            }

            // Add an artificial property for the choice extension element.
            PropertyUtils.installChoiceExtensionProperty(
                    extSheetSet, node, casaExtPoint,
                    firstEE, lastEE,
                    CasaNode.ALWAYS_WRITABLE_PROPERTY,
                    String.class,
                    elementName, elementDisplayName,
                    elementDescription,
                    choiceMap,
                    choiceDisplayNameMap,
                    defaultChoice);
        }

        // Add properties for attributes of the current extenstion element.
        List<JbiExtensionAttribute> attributes = extElement.getAttributes();
        if (attributes != null) {
            for (JbiExtensionAttribute attr : attributes) {
                String attrName = attr.getName();
                String attrDisplayName = attr.getDisplayName();
                String attrType = attr.getType();
                String attrDescription = attr.getDescription();
                PropertyUtils.installExtensionProperty(
                        extSheetSet, node, casaExtPoint,
                        firstEE, lastEE,
                        CasaNode.ALWAYS_WRITABLE_PROPERTY,
                        attrType,
                        attrName, attrDisplayName, attrDescription, provider);
            }
        }

        // Add properties for child extension elements.
        List<JbiExtensionElement> childExtElements = extElement.getElements();
        if (childExtElements != null) {
            for (CasaExtensibilityElement ee : lastEE.getExtensibilityElements()) {
                String eeName = ee.getPeer().getNodeName();
                boolean found = false;
                for (JbiExtensionElement childExtElement : childExtElements) {
                    if (eeName.equals(childExtElement.getName())) {
                        createExistingProperties(node, document,
                                childExtElement,
                                extSheetSet, casaExtPoint,
                                firstEE, ee, namespace, provider);
                        found = true;
                        break;
                    }
                }
                assert found;
            }
        }
    }
}
