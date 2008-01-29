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
package org.netbeans.modules.sun.manager.jbi.management.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * JBI Component configuration property descriptor.
 * 
 * @deprecated to be removed
 * @author jqian
 */
public class OldJBIComponentConfigurationDescriptor {
     
    private static final String DISPLAY_NAME = "displayName"; // NOI18N
    private static final String DISPLAY_DESCRIPTION = "displayDescription"; // NOI18N
    private static final String IS_PASSWORD_FIELD = "isPasswordField"; // NOI18N
    private static final String NEEDS_APPLICATION_RESTART = "isApplicationRestartRequired"; // NOI18N
    private static final String NEEDS_COMPONENT_RESTART = "isComponentRestartRequired"; // NOI18N
    private static final String NEEDS_SERVER_RESTART = "isServerRestartRequired"; // NOI18N
    private static final String IS_REQUIRED_FIELD = "isRequiredField"; // NOI18N
    
    private String name;
    private String displayName;
    private String description;
    private boolean isPassword;
    private boolean needsApplicationRestart;
    private boolean needsComponentRestart;
    private boolean needsServerRestart;
    private boolean isRequired;
    
    private Map<String, OldJBIComponentConfigurationDescriptor> children;

    private OldJBIComponentConfigurationDescriptor() {        
    }
    
    private OldJBIComponentConfigurationDescriptor(String name,
            String displayName,
            String description,
            boolean isPassword,
            boolean needsApplicationRestart,
            boolean needsComponentRestart,
            boolean needsServerRestart,
            boolean isRequired) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.isPassword = isPassword;
        this.needsApplicationRestart = needsApplicationRestart;
        this.needsComponentRestart = needsComponentRestart;
        this.needsServerRestart = needsServerRestart;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description; //Utils.getTooltip(description);
    }

    public boolean isPassword() {
        return isPassword;
    }

    public boolean needsApplicationRestart() {
        return needsApplicationRestart;
    }

    public boolean needsComponentRestart() {
        return needsComponentRestart;
    }

    public boolean needsServerRestart() {
        return needsServerRestart;
    }      
    
    public boolean isRequired() {
        return isRequired;
    }
    
    public void addChild(OldJBIComponentConfigurationDescriptor descriptor) {
        if (children == null) {
            children = new LinkedHashMap<String, OldJBIComponentConfigurationDescriptor>();
        }
        children.put(descriptor.getName(), descriptor);
    }
    
    public Set<String> getChildNames() {
        return children == null ? new HashSet<String>() : children.keySet();
    }
    
    public OldJBIComponentConfigurationDescriptor getChild(String name) {
        return children == null ? null : children.get(name);
    }
        
    /**
     * Parses the component config xml data.
     * 
     * @return the top level configuration descriptor
     * 
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public static OldJBIComponentConfigurationDescriptor parse(String configXmlData) 
            throws ParserConfigurationException, IOException, SAXException {
        
        if (configXmlData == null) {
            return null;
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(
                new InputSource(new StringReader(configXmlData)));
        
        OldJBIComponentConfigurationDescriptor descriptor = new OldJBIComponentConfigurationDescriptor();
        
        parseConfigurationElement(descriptor, document.getDocumentElement());
        
        return descriptor;
    }
    
    private static void parseConfigurationElement(
            OldJBIComponentConfigurationDescriptor descriptor, 
            Element element) {
              
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);

                String isPassword = childElement.getAttribute(IS_PASSWORD_FIELD);
                String needsApplicationRestart = 
                        childElement.getAttribute(NEEDS_APPLICATION_RESTART);
                String needsComponentRestart = 
                        childElement.getAttribute(NEEDS_COMPONENT_RESTART);
                String needsServerRestart = 
                        childElement.getAttribute(NEEDS_SERVER_RESTART);
                String isRequired = 
                        childElement.getAttribute(IS_REQUIRED_FIELD);
                
                OldJBIComponentConfigurationDescriptor childDescriptor =
                        new OldJBIComponentConfigurationDescriptor(
                        childElement.getLocalName(),
                        childElement.getAttribute(DISPLAY_NAME),
                        childElement.getAttribute(DISPLAY_DESCRIPTION),
                        isPassword != null &&
                            Boolean.valueOf(isPassword),
                        needsApplicationRestart != null &&
                            Boolean.valueOf(needsApplicationRestart),
                        needsComponentRestart != null &&
                            Boolean.valueOf(needsComponentRestart),
                        needsServerRestart != null &&
                            Boolean.valueOf(needsServerRestart),
                        isRequired != null &&
                            Boolean.valueOf(isRequired));
                descriptor.addChild(childDescriptor);
                
                parseConfigurationElement(childDescriptor, childElement);
            }
        }
    }
}
