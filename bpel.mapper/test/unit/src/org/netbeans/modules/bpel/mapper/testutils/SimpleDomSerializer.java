/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.testutils;

import java.io.IOException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Serialize DOM Element to a string without indentions and line separators.
 * Comments are ignored.
 *
 * @author nikita
 */
public class SimpleDomSerializer {

    public String serializeNode(Node node) throws IOException {
        StringBuilder result = new StringBuilder();
        serializeNode(node, result);
        return result.toString();
    }

    private void serializeNode(Node node, StringBuilder result) throws IOException {
        //
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                result.append("<?xml version=\"1.0\"?>");

                // recurse on each child
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                        serializeNode(nodes.item(i), result);
                    }
                }
                break;

            case Node.ELEMENT_NODE:
                String name = node.getNodeName();
                result.append("<").append(name);
                NamedNodeMap attributes = node.getAttributes();
                for (int i=0; i<attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    result.append(" ").
                            append(current.getNodeName()).
                            append("=\"").
                            append(current.getNodeValue()).
                            append("\"");
                }
                result.append(">");

                // recurse on each child
                NodeList children = node.getChildNodes();
                if (children != null) {
                    if ((children.item(0) != null) &&
                        (children.item(0).getNodeType() ==
                        Node.ELEMENT_NODE)) {

                    }
                    for (int i=0; i<children.getLength(); i++) {
                        serializeNode(children.item(i), result);
                    }
                    if ((children.item(0) != null) &&
                        (children.item(children.getLength()-1)
                                .getNodeType() ==
                        Node.ELEMENT_NODE)) {
                    }
                }

                result.append("</").append(name).append(">");
                break;

            case Node.TEXT_NODE:
                String text = node.getNodeValue();
                text = text.replaceAll("\\s{2,}", "");
                if (text != null && text.length() != 0) {
                    result.append(text);
                }
                break;

            case Node.CDATA_SECTION_NODE:
                result.append("<![CDATA[").append(node.getNodeValue()).append("]]>");
                break;

                // Ignoring comments
//            case Node.COMMENT_NODE:
//                result.append("<!-- ").append(node.getNodeValue()).append(" -->");
//                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                result.append("<?").
                        append(node.getNodeName()).
                        append(" ").
                        append(node.getNodeValue()).
                        append("?>");
                break;
            case Node.ENTITY_REFERENCE_NODE:
                result.append("&").
                        append(node.getNodeName()).
                        append(";");
                break;
        }
    }
}