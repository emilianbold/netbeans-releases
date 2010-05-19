/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.sun.manager.jbi.management.model;

import org.netbeans.modules.sun.manager.jbi.management.model.constraint.JBIComponentConfigurationConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.CompositeConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.JBIComponentConfigurationConstraintFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.sun.manager.jbi.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * JBI Component configuration parser.
 * 
 * @author jqian
 */
public class JBIComponentConfigurationParser {

    private static final String CONFIGURATION_NAMESPACE = "http://www.sun.com/jbi/Configuration/V1.0"; // NOI18N
    // element names
    private static final String PROPERTY = "Property";
    private static final String PROPERTY_GROUP = "PropertyGroup";
    private static final String APPLICATION_CONFIGURATION = "ApplicationConfiguration";
    private static final String APPLICATION_VARIABLE = "ApplicationVariable";
    private static final String CONFIGURATION = "Configuration"; // NOI18N
    private static final String CONSTRAINT = "Constraint"; // NOI18N
    // attribute names
    private static final String NAME = "name"; // NOI18N
    private static final String DISPLAY_NAME = "displayName"; // NOI18N
    private static final String DISPLAY_DESCRIPTION = "displayDescription"; // NOI18N
    private static final String ENCRYPTED = "encrypted"; // NOI18N
    private static final String IS_APPLICATION_RESTART_REQUIRED = "isApplicationRestartRequired"; // NOI18N
    private static final String IS_COMPONENT_RESTART_REQUIRED = "isComponentRestartRequired"; // NOI18N
    private static final String IS_SERVER_RESTART_REQUIRED = "isServerRestartRequired"; // NOI18N
    private static final String REQUIRED = "required"; // NOI18N
    private static final String SHOW_DISPLAY = "showDisplay"; // NOI18N
    private static final String ON_CHANGE_MESSAGE = "onChangeMessage"; // NOI18N
    private static final String TYPE = "type"; // NOI18N
    private static final String DEFAULT_VALUE = "defaultValue"; // NOI18N
    private static final String FACET = "facet"; // NOI18N
    private static final String FACET_VALUE = "value"; // NOI18N

    /**
     * Parses component jbi.xml and gets the component configuration 
     * meta data.
     * 
     * @return the top level configuration descriptor
     * 
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public static JBIComponentConfigurationDescriptor parse(String configXmlData)
            throws ParserConfigurationException, IOException, SAXException {

        JBIComponentConfigurationDescriptor ret = null;

        if (configXmlData != null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document jbiDoc = builder.parse(
                    new InputSource(new StringReader(configXmlData)));
            ret = parse(jbiDoc);
        }

        return ret;
    }

    public static JBIComponentConfigurationDescriptor parse(Document jbiDoc)
            throws ParserConfigurationException, IOException, SAXException {

        JBIComponentConfigurationDescriptor ret = null;

        NodeList configNodeList = jbiDoc.getElementsByTagNameNS(
                CONFIGURATION_NAMESPACE, CONFIGURATION);
        if (configNodeList != null && configNodeList.getLength() == 1) {
            Element configRootElement = (Element) configNodeList.item(0);
            ret = new JBIComponentConfigurationDescriptor();
            parseConfigurationElement(ret, configRootElement);
        }

        return ret;
    }

    private static void parseConfigurationElement(
            JBIComponentConfigurationDescriptor descriptor,
            Element element) {

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);

                String elementName = childElement.getLocalName();
                if (!elementName.equals(PROPERTY) &&
                        !elementName.equals(PROPERTY_GROUP) &&
                        !elementName.equals(APPLICATION_VARIABLE) &&
                        !elementName.equals(APPLICATION_CONFIGURATION)) {
                    continue;
                }                
                
                JBIComponentConfigurationDescriptor childDescriptor;
                
                if (elementName.equals(APPLICATION_VARIABLE)) {
                    childDescriptor = new JBIComponentConfigurationDescriptor.ApplicationVariable();
                } else if (elementName.equals(APPLICATION_CONFIGURATION)) {
                    childDescriptor = new JBIComponentConfigurationDescriptor.ApplicationConfiguration();                    
                } else { // Property or Property Group
                    childDescriptor = new JBIComponentConfigurationDescriptor();
                    
                    // The following are equired fields.
                    childDescriptor.setName(childElement.getAttribute(NAME));
                    childDescriptor.setDisplayName(childElement.getAttribute(DISPLAY_NAME));
                    childDescriptor.setDescription(childElement.getAttribute(DISPLAY_DESCRIPTION));
                    childDescriptor.setTypeQName(XmlUtil.getAttributeNSName(childElement, TYPE));
                    childDescriptor.setPropertyType(elementName); // not really required, but mandatory

                    // The following are not required.
                    String required = childElement.getAttribute(REQUIRED);
                    if (required.length() != 0) {
                        childDescriptor.setRequired(Boolean.valueOf(required));
                    }
                    String showDisplay = childElement.getAttribute(SHOW_DISPLAY);
                    if (showDisplay.length() != 0) {
                        childDescriptor.setShowDisplay(showDisplay);
                    }
                    String encrypted = childElement.getAttribute(ENCRYPTED);
                    if (encrypted.length() != 0) {
                        childDescriptor.setEncrypted(Boolean.valueOf(encrypted));
                    }
                    /*
                    String isApplicationRestartRequired =
                            childElement.getAttribute(IS_APPLICATION_RESTART_REQUIRED);
                    if (isApplicationRestartRequired.length() != 0) {
                        childDescriptor.setApplicationRestartRequired(
                                Boolean.valueOf(isApplicationRestartRequired));
                    }
                    String isComponentRestartRequired =
                            childElement.getAttribute(IS_COMPONENT_RESTART_REQUIRED);
                    if (isComponentRestartRequired.length() != 0) {
                        childDescriptor.setComponentRestartRequired(
                                Boolean.valueOf(isComponentRestartRequired));
                    }
                    String isServerRestartRequired =
                            childElement.getAttribute(IS_SERVER_RESTART_REQUIRED);
                    if (isServerRestartRequired.length() != 0) {
                        childDescriptor.setServerRestartRequired(
                                Boolean.valueOf(isServerRestartRequired));
                    }
                    */

                    String onChangeMessage = childElement.getAttribute(ON_CHANGE_MESSAGE);
                    if (onChangeMessage.length() != 0) {
                        childDescriptor.setOnChangeMessage(onChangeMessage);
                    }

                    String defaultValue = childElement.getAttribute(DEFAULT_VALUE);
                    if (defaultValue.length() != 0) {
                        childDescriptor.setDefaultValue(defaultValue);
                    }

                    CompositeConstraint compositeConstraint = new CompositeConstraint();
                    List<String> enumerationOptions = null;

                    NodeList constraintNodes = childElement.getElementsByTagNameNS(
                            CONFIGURATION_NAMESPACE, CONSTRAINT);

                    for (int j = 0; j < constraintNodes.getLength(); j++) {
                        Element constraintElement = (Element) constraintNodes.item(j);
                        String facet = constraintElement.getAttribute(FACET);
                        String facetValue = constraintElement.getAttribute(FACET_VALUE);
                        if (facet.equals(JBIComponentConfigurationConstraintFactory.ENUMERATION)) {
                            if (enumerationOptions == null) {
                                enumerationOptions = new ArrayList<String>();
                            }
                            enumerationOptions.add(facetValue);
                        } else {
                            JBIComponentConfigurationConstraint constraint =
                                    JBIComponentConfigurationConstraintFactory.newConstraint(
                                    facet, facetValue);
                            compositeConstraint.addConstraint(constraint);
                        }
                    }

                    if (enumerationOptions != null) {
                        JBIComponentConfigurationConstraint constraint =
                                JBIComponentConfigurationConstraintFactory.newConstraint(
                                JBIComponentConfigurationConstraintFactory.ENUMERATION,
                                enumerationOptions);
                        compositeConstraint.addConstraint(constraint);
                    }

                    childDescriptor.setConstraint(compositeConstraint);
                }
                
                String isApplicationRestartRequired =
                            childElement.getAttribute(IS_APPLICATION_RESTART_REQUIRED);
                if (isApplicationRestartRequired.length() != 0) {
                    childDescriptor.setApplicationRestartRequired(
                            Boolean.valueOf(isApplicationRestartRequired));
                }
                String isComponentRestartRequired =
                        childElement.getAttribute(IS_COMPONENT_RESTART_REQUIRED);
                if (isComponentRestartRequired.length() != 0) {
                    childDescriptor.setComponentRestartRequired(
                            Boolean.valueOf(isComponentRestartRequired));
                }
                String isServerRestartRequired =
                        childElement.getAttribute(IS_SERVER_RESTART_REQUIRED);
                if (isServerRestartRequired.length() != 0) {
                    childDescriptor.setServerRestartRequired(
                            Boolean.valueOf(isServerRestartRequired));
                }

                descriptor.addChild(childDescriptor);

                parseConfigurationElement(childDescriptor, childElement);
            }
        }
    }
}

