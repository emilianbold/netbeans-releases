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

package org.netbeans.modules.bpel.debugger.api.variables;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Alexander Zgursky
 */
public interface XmlElementValue extends Value {
    String VALUE_OBJECT_KEY = "ValueObject";
    String XPATH_KEY = "Xpath";
    
    Element getElement();
    
    final class Helper {
        private Helper() {};
        public static XmlElementValue find(Node node) {
            Node documentNode = node.getNodeType() == Node.DOCUMENT_NODE ?
                    node : node.getOwnerDocument();
            Object userData = documentNode.getUserData(VALUE_OBJECT_KEY);
            if (userData instanceof XmlElementValue) {
                return (XmlElementValue)userData;
            } else {
                return null;
            }
        }
        
        public static void bind(Document document, XmlElementValue value) {
            document.setUserData(VALUE_OBJECT_KEY, value, null);
        }
        
        public static String xpath(Node node) {
            StringBuffer sb = new StringBuffer(255);
            if (node.getNodeType() == node.ELEMENT_NODE) {
                appendElementXpath(node, sb);
            } else {
                Object xpath = node.getUserData(XPATH_KEY);
                if (xpath != null) {
                    return (String)xpath;
                }
                if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                    appendElementXpath(((Attr)node).getOwnerElement(), sb);
                    sb.append("/@").append(node.getNodeName());
                } else if (node.getNodeType() == Node.TEXT_NODE) {
                    appendElementXpath(node.getParentNode(), sb);
                    sb.append("/text()");
                } else {
                    appendElementXpath(node.getParentNode(), sb);
                }
                node.setUserData(XPATH_KEY, sb.toString(), null);
            }
            return sb.toString();
        }
        
        private static void appendElementXpath(Node node, StringBuffer sb) {
            assert node.getNodeType() == Node.ELEMENT_NODE;
            
            Object xpath = node.getUserData(XPATH_KEY);
            if (xpath != null) {
                sb.append((String)xpath);
                return;
            }
            Node parent = node.getParentNode();
            if (parent.getNodeType() == Node.DOCUMENT_NODE) {
                return;
            }
            
            appendElementXpath(parent, sb);
            
            int index = 1;
            Node prev = node.getPreviousSibling();
            while (prev != null) {
                if (prev.getNodeType() == Node.ELEMENT_NODE) {
                    index++;
                }
                prev = prev.getPreviousSibling();
            }
            
            sb.append("/node()[").append(index).append(']');
            node.setUserData(XPATH_KEY, sb.toString(), null);
        }
    }
}
