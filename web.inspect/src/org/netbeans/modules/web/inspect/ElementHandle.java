/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handle for a DOM element. It contains data necessary to locate
 * the element in various structures (DOM, AST, etc.)
 *
 * @author Jan Stola
 */
public class ElementHandle {
    /** Name of JSON attribute where parent is stored. */
    private static final String JSON_PARENT = "parent"; // NOI18N
    /** Name of JSON attribute where index of the element (in parent) is stored. */
    private static final String JSON_INDEX_IN_PARENT = "indexInParent"; // NOI18N
    /** Name of JSON attribute where tag names of all children of the parent are stored. */
    private static final String JSON_SIBLING_TAG_NAMES = "siblingTagNames"; // NOI18N
    /** Key in element's user data under which the handle is cached. */
    private static final String ELEMENT_USER_DATA_HANDLE = "handle"; // NOI18N
    /** Index of the element among parent's sub-elements. */
    private int indexInParent;
    /** Tag names of all parent's sub-elements. */
    private String[] siblingTagNames;
    /** Handle for parent element. */
    private ElementHandle parent;

    // Handles should be created using factory methods only.
    private ElementHandle() {
    }

    /**
     * Returns tag name of the element.
     * 
     * @return tag name of the element.
     */
    private String getTagName() {
        return siblingTagNames[indexInParent];
    }

    /**
     * Creates an element handle from its JSON representation
     * (obtained from the browser plugin).
     * 
     * @param json JSON representation of the handle.
     * @return handle that corresponds to the one given in JSON format.
     */
    public static ElementHandle create(JSONObject json) {
        try {
            ElementHandle handle = new ElementHandle();
            if (!json.isNull(JSON_PARENT)) {
                JSONObject parent = json.getJSONObject(JSON_PARENT);
                handle.parent = create(parent);
            }
            handle.indexInParent = json.getInt(JSON_INDEX_IN_PARENT);
            JSONArray siblings = json.getJSONArray(JSON_SIBLING_TAG_NAMES);
            handle.siblingTagNames = new String[siblings.length()];
            for (int i=0; i<siblings.length(); i++) {
                handle.siblingTagNames[i] = siblings.getString(i);
            }
            return handle;
        } catch (JSONException ex) {
            Logger.getLogger(ElementHandle.class.getName()).log(Level.INFO, null, ex);            
        }
        return null;
    }

    /**
     * Creates an element handle for the specified DOM element.
     * The handles are cached, i.e., the same handle is returned
     * for the same DOM element instance when the method is called
     * more than once.
     * 
     * @param element DOM element for which the handle should be created.
     * @return handle that corresponds to the given DOM element.
     */
    public static ElementHandle create(Element element) {
        Object storedHandle = element.getUserData(ELEMENT_USER_DATA_HANDLE);
        if (storedHandle instanceof ElementHandle) {
            return (ElementHandle)storedHandle;
        }

        ElementHandle handle = new ElementHandle();
        Node parentNode = element.getParentNode();
        if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
            Element parentElement = (Element)parentNode;
            ElementHandle parentHandle = create(parentElement);
            handle.parent = parentHandle;
            NodeList siblings = parentElement.getChildNodes();
            List<String> siblingTags = new ArrayList<String>(siblings.getLength());
            for (int i=0; i<siblings.getLength(); i++) {
                Node siblingNode = siblings.item(i);
                if (siblingNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element siblingElement = (Element)siblingNode;
                    if (siblingElement == element) {
                        handle.indexInParent = siblingTags.size();
                    }
                    siblingTags.add(siblingElement.getTagName());
                }
            }
            handle.siblingTagNames = siblingTags.toArray(new String[siblingTags.size()]);
        } else {
            handle.indexInParent = 0;
            handle.siblingTagNames = new String[] {element.getTagName()};
        }

        element.setUserData(ELEMENT_USER_DATA_HANDLE, handle, null);
        return handle;
    }

    /**
     * Finds {@code AstNode} that corresponds to this handle.
     * 
     * @param root root of AST tree where this handle should be located.
     * @return array with two items, the first item is {@code AstNode}
     * that seem to match the handle exactly. If no such node was found
     * then the first item is {@code null} and the second item is the nearest
     * node to this handle.
     */
    public AstNode[] locateInAst(AstNode root) {
        AstNode nearest;
        ElementHandle parentHandle = parent;
        if (parentHandle != null) {
            AstNode[] result = parentHandle.locateInAst(root);
            if (result[0] == null) {
                return result;
            } else {
                AstNode astParent = result[0];
                int index = 0;
                String name = siblingTagNames[index].toLowerCase();
                for (AstNode child : astParent.children()) {
                    if (child.type() == AstNode.NodeType.OPEN_TAG) {
                        String astName = child.name().toLowerCase();
                        if (name.equals(astName)) {
                            if (index == indexInParent) {
                                return new AstNode[] {child, null};
                            } else {
                                index++;
                                if (index == siblingTagNames.length) {
                                    break; // AST doesn't match the handle
                                } else {
                                    name = siblingTagNames[index].toLowerCase();
                                }
                            }
                        }
                    }
                }
                nearest = astParent;
            }
        } else {
            String elemName = getTagName().toLowerCase();
            for (AstNode child : root.children()) {
                if (child.type() == AstNode.NodeType.OPEN_TAG) {
                    String astName = child.name().toLowerCase();
                    if (elemName.equals(astName)) {
                        return new AstNode[] {child, null};
                    }
                }
            }
            nearest = root;
        }
        return new AstNode[] {null, nearest};
    }

    /**
     * Finds {@code Element} that corresponds to this handle.
     * 
     * @param document document where this handle should be located.
     * @return array with two items, the first item is {@code Element}
     * that seem to match the handle exactly. If no such element was found
     * then the first item is {@code null} and the second item is the nearest
     * element to this handle.
     */
    public Element[] locateInDocument(Document document) {
        Element nearest;
        ElementHandle parentHandle = parent;
        if (parentHandle != null) {
            Element[] result = parentHandle.locateInDocument(document);
            if (result[0] == null) {
                return result;
            } else {
                Element parentElement = result[0];
                int index = 0;
                String name = siblingTagNames[index].toLowerCase();
                NodeList childNodes = parentElement.getChildNodes();
                for (int i=0; i<childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        Element childElement = (Element)child;
                        String childName = childElement.getTagName().toLowerCase();
                        if (name.equals(childName)) {
                            if (index == indexInParent) {
                                return new Element[] {childElement, null};
                            } else {
                                index++;
                                if (index == siblingTagNames.length) {
                                    break; // Document doesn't match the handle
                                } else {
                                    name = siblingTagNames[index].toLowerCase();
                                }
                            }
                        }
                    }
                }
                nearest = parentElement;
            }
        } else {
            String elemName = getTagName().toLowerCase();
            Element documentElement = document.getDocumentElement();
            String documentElementName = documentElement.getTagName().toLowerCase();
            if (elemName.equals(documentElementName)) {
                return new Element [] {documentElement, null};
            } else {
                nearest = documentElement;
            }
        }
        return new Element[] {null, nearest};
    }

    /**
     * Returns JSON representation of this handle (suitable for passing
     * to browser plugin).
     * 
     * @return JSON representation of this handle.
     */
    public JSONObject toJSONObject() {
        try {
            JSONObject json = new JSONObject();
            if (parent == null) {
                json.put(JSON_PARENT, JSONObject.NULL);
            } else {
                json.put(JSON_PARENT, parent.toJSONObject());
            }
            json.put(JSON_INDEX_IN_PARENT, indexInParent);
            JSONArray siblings = new JSONArray(Arrays.asList(siblingTagNames));
            json.put(JSON_SIBLING_TAG_NAMES, siblings);
            return json;
        } catch (JSONException ex) {
            Logger.getLogger(ElementHandle.class.getName()).log(Level.INFO, null, ex);
        }
        return null;
    }
    
}
