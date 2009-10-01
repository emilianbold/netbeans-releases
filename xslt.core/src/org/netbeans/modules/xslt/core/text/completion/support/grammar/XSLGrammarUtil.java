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

package org.netbeans.modules.xslt.core.text.completion.support.grammar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Alex Petrov (22.07.2008)
 */
public class XSLGrammarUtil implements XSLGrammarConstants {
    private static final String
        ATTRIBUTE_NAME_VERSION = "version"; // NOI18N
    
    private static final Set<String> setSupportedXslVersions = new HashSet<String>(
        Arrays.asList(new String[] {XSL_VERSION_1_0, XSL_VERSION_1_1, XSL_VERSION_2_0}));

    public static boolean isSupportedXslVersion(String xslVersion) {
        return (setSupportedXslVersions.contains(xslVersion));
    }
    
    public static Tag getStylesheet(Tag currentTag) {
        if (currentTag == null) return null;
        
        String tagName = currentTag.getTagName();
        if ((tagName != null) && (tagName.contains(STYLESHEET_ELEMENT_NAME)))
            return currentTag;
        else
            return getParentTagByName(currentTag, STYLESHEET_ELEMENT_NAME);
    }
    
    public static Tag getStylesheet(Document document) {
        if (document == null) return null;
        NodeList childNodes = document.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            String nodeName = childNode.getNodeName();
            if ((nodeName.contains(STYLESHEET_ELEMENT_NAME)) && 
                (childNode instanceof Tag)) {
                return (Tag) childNode;
            }
        }
        return null;
    }
    
    public static Tag getParentTagByName(Tag currentTag, String requiredParentTagName) {
        if ((currentTag == null) || (requiredParentTagName == null)) return null;
        
        Node parentTag = currentTag;
        while (parentTag != null) {
            parentTag = parentTag.getParentNode();
            if (parentTag instanceof Tag) { // null isn't instance of any class
                String parentTagName = ((Tag) parentTag).getTagName();
                if (parentTagName.contains(requiredParentTagName))
                    return ((Tag) parentTag);
            }
        }
        return null; // parent tag hasn't been found
    }

    public static String getCurrentXslVersion(Object objCurrentNode, 
        COMPLETION_QUERY_TYPE completionQueryType) {
        if ((objCurrentNode == null) || (completionQueryType == null)) return null;
        
        String version = null; 
        Tag tag = null;
        try {
            if (completionQueryType.equals(COMPLETION_QUERY_TYPE.QueryTagName)) {
                tag = (Tag) ((Node) objCurrentNode).getParentNode();
            } else if (completionQueryType.equals(COMPLETION_QUERY_TYPE.QueryAttributeName)) {
                Node parentNode = ((Node) objCurrentNode).getParentNode();
                if (parentNode instanceof Document) { // current node objCurrentNode is <xsl:styleheet ...>
                    // tag = getStylesheet((Document) parentNode);
                    version = getCurrentXslVersion((Node) objCurrentNode);
                } else if (parentNode instanceof Tag) {
                    tag = (Tag) parentNode;
                }
            } else if (completionQueryType.equals(COMPLETION_QUERY_TYPE.QueryAttributeValue)) {
                tag = (Tag) ((Attr) objCurrentNode).getOwnerElement();
            }
            if (version == null) {
                Tag tagStylesheet = getStylesheet(tag);
                version = tagStylesheet.getAttribute(ATTRIBUTE_NAME_VERSION);
            }
        } catch(Exception e) {
            Logger.getLogger(XSLGrammarUtil.class.getName()).log(Level.INFO, 
                (e.getMessage() == null ? e.getClass().getName() : e.getMessage()), e);
            return null;
        }
        return (isSupportedXslVersion(version) ? version : null);    
    }
    
    private static String getCurrentXslVersion(Node nodeStyleheet) {
        if (nodeStyleheet == null) return null;
        NamedNodeMap mapAttributes = nodeStyleheet.getAttributes();
        if (mapAttributes == null) return null;

        Node nodeVersion = mapAttributes.getNamedItem(ATTRIBUTE_NAME_VERSION);
        if (nodeVersion == null) return null;
        
        String version = nodeVersion.getNodeValue();
        if ((version != null) && (version.length() == 0)) version = null;
        return version; 
    }
}
