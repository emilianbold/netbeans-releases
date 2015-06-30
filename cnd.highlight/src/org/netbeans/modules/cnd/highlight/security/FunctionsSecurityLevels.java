/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.security;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Danila Sergeyev
 */
public class FunctionsSecurityLevels {
    private static final String levelTagName = "level"; // NOI18N
    private static final String[] levelTagAttributes = {"value"}; // NOI18N
    private static final String categoryTagName = "category"; // NOI18N
    private static final String[] categoryTagAttributes = {"name"}; // NOI18N
    private static final String functionTagName = "function"; // NOI18N
    private static final String[] functionTagAttributes = {"name", "header"}; // NOI18N
    
    public enum Level {
        AVOID,
        UNSAFE,
        CAUTION
    }
    
    private final Map<String, Map<String, String>> functionsCategories;
    private final String level;
    
    private FunctionsSecurityLevels(Level level) {
        this.level = level.name().toLowerCase();
        functionsCategories = new HashMap<>();
        processXml();
    }
    
    private void processXml() {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                          .newDocumentBuilder()
                          .parse(FunctionsSecurityLevels.class.getResourceAsStream("Functions.xml")); // NOI18N
            
            NodeList levelNodes = doc.getElementsByTagName(levelTagName);
            
            // iterate through all security levels
            for (int i = 0, ilimit = levelNodes.getLength(); i < ilimit; i++) {
                Node levelNode = levelNodes.item(i);
                if (levelNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element levelElement = (Element) levelNode;
                    String value = levelElement.getAttribute(levelTagAttributes[0]);
                    if (value != null && value.equals(level)) {
                        NodeList categoryNodes = levelNode.getChildNodes();
                        
                        // iterate through all categories within level
                        for (int j = 0, jlimit = categoryNodes.getLength(); j < jlimit; j++) {
                            Node categoryNode = categoryNodes.item(j);
                            if (categoryNode.getNodeName().equals(categoryTagName) && categoryNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element categoryElement = (Element) categoryNode;
                                String categoryName = categoryElement.getAttribute(categoryTagAttributes[0]);
                                NodeList funcNodes = categoryNode.getChildNodes();
                                Map<String, String> functions = new HashMap<>();
                                
                                // iterate through all functions within category
                                for (int fnNdx = 0, flimit = funcNodes.getLength(); fnNdx < flimit; fnNdx++) {
                                    Node node = funcNodes.item(fnNdx);
                                    if (node.getNodeName().equals(functionTagName) && node.getNodeType() == Node.ELEMENT_NODE) {
                                        Element element = (Element) node;
                                        String fnName = element.getAttribute(functionTagAttributes[0]);
                                        String header = element.getAttribute(functionTagAttributes[1]);
                                        functions.put(fnName, header);
                                    }
                                }
                                
                                functionsCategories.put(categoryName, functions);
                            }
                        }
                    }
                }
            }
            
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static FunctionsSecurityLevels getInstance(Level level) {
        return new FunctionsSecurityLevels(level);
    }
    
    public Map<String, Map<String, String>> getCategories() {
        return Collections.unmodifiableMap(functionsCategories);
    }
    
    public Map<String, String> getFunctionsInCategory(String categoryName) {
        return Collections.unmodifiableMap(functionsCategories.get(categoryName));
    }
    
}
