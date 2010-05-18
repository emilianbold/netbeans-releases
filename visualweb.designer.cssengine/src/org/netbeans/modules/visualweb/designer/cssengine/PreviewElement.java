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
package org.netbeans.modules.visualweb.designer.cssengine;

import java.net.URL;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;


// XXX Moved from designer/PreviewElement.java.
/**
 * This class represents a DOM element which can be used for
 * preview purposes where I want to do layout based on styles but
 * don't have an associated comment. I can set a stylestring on
 * the element and then have the CSS lookup routines find these
 * styles using the standard style interface on the element.
 * @author Tor Norbye
 */
class PreviewElement implements CSSStylableElement {
    private final Document document;
    private StyleMap computedStyleMap;
//    private XhtmlCssEngine engine;
    private URL base;
    private String styles;
    private static final String NYI = "Not supported for PreviewElement";

    public PreviewElement(Document document, /*XhtmlCssEngine engine,*/ URL base, String styles) {
        this.document = document;
        this.base = base;
//        this.engine = engine;
        this.styles = styles;
    }

//    public CSSEngine getEngine() {
////        return engine;
//        return CssEngineServiceProvider.getDefault().getCssEngine(document);
//    }

    /**
     * Returns the computed style of this element/pseudo-element.
     */
    public StyleMap getComputedStyleMap(String pseudoElement) {
        return computedStyleMap;
    }

    /**
     * Sets the computed style of this element/pseudo-element.
     */
    public void setComputedStyleMap(String pseudoElement, StyleMap sm) {
        computedStyleMap = sm;
    }

    /**
     * Returns the ID of this element.
     */
    public String getXMLId() {
        return "";
    }

    /**
     * Returns the class of this element.
     */
    public String getCSSClass() {
        return "";
    }

    /**
     * Returns the CSS base URL of this element.
     */
    public URL getCSSBase() {
        return base;
    }

    /**
     * Tells whether this element is an instance of the given pseudo
     * class.
     */
    public boolean isPseudoInstanceOf(String pseudoClass) {
        return false;
    }

    public org.w3c.dom.Node appendChild(org.w3c.dom.Node node)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Node cloneNode(boolean param) {
        throw new RuntimeException(NYI);
    }

    public String getAttribute(String str) {
        //throw new RuntimeException(NYI);
        if (HtmlAttribute.STYLE.equals(str)) {
            return styles;
        }

        return "";
    }

    public String getAttributeNS(String str, String str1) {
        //throw new RuntimeException(NYI);
        if (HtmlAttribute.STYLE.equals(str1)) {
            return styles;
        }

        return "";
    }

    public org.w3c.dom.Attr getAttributeNode(String str) {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Attr getAttributeNodeNS(String str, String str1) {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.NamedNodeMap getAttributes() {
        //throw new RuntimeException(NYI);
        return null;
    }

    public org.w3c.dom.NodeList getChildNodes() {
        return new NodeList() {
                public int getLength() {
                    return 0;
                }

                public Node item(int index) {
                    return null;
                }
            };
    }

    public org.w3c.dom.NodeList getElementsByTagName(String str) {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.NodeList getElementsByTagNameNS(String str, String str1) {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Node getFirstChild() {
        return null;
    }

    public org.w3c.dom.Node getLastChild() {
        return null;
    }

    public String getLocalName() {
        return "preview";
    }

    public String getNamespaceURI() {
        return "preview";
    }

    public org.w3c.dom.Node getNextSibling() {
        return null;
    }

    public String getNodeName() {
        return getLocalName();
    }

    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }

    public String getNodeValue() throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Document getOwnerDocument() {
//        System.err.println("Warning: Previewelement getOwnerDocument called...");
//        return null;
        return document;
    }

    public org.w3c.dom.Node getParentNode() {
        return null;
    }

    public String getPrefix() {
        return "preview";
    }

    public org.w3c.dom.Node getPreviousSibling() {
        return null;
    }

    public String getTagName() {
        return getLocalName();
    }

    public boolean hasAttribute(String str) {
        return false;
    }

    public boolean hasAttributeNS(String str, String str1) {
        return false;
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node node, org.w3c.dom.Node node1)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public boolean isSupported(String str, String str1) {
        return false;
    }

    public void normalize() {
    }

    public void removeAttribute(String str) throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public void removeAttributeNS(String str, String str1)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr attr)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Node removeChild(org.w3c.dom.Node node)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node node, org.w3c.dom.Node node1)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public void setAttribute(String str, String str1) throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public void setAttributeNS(String str, String str1, String str2)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr attr)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr attr)
        throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public void setNodeValue(String str) throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    public void setPrefix(String str) throws org.w3c.dom.DOMException {
        throw new RuntimeException(NYI);
    }

    // TODO: Implement these methods?
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        throw new RuntimeException(NYI);
    }
    
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        throw new RuntimeException(NYI);
    }
    
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        throw new RuntimeException(NYI);
    }
    
    public TypeInfo getSchemaTypeInfo() {
        throw new RuntimeException(NYI);
    }
    
    public Object getUserData(String key) {
        throw new RuntimeException(NYI);
    }
    
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        throw new RuntimeException(NYI);
    }
    
    public Object getFeature(String feature, String version) {
        throw new RuntimeException(NYI);
    }
    
    public boolean isEqualNode(Node arg) {
        throw new RuntimeException(NYI);
    }
    
    public String lookupNamespaceURI(String prefix) {
        throw new RuntimeException(NYI);
    }
    
    public boolean isDefaultNamespace(String namespaceURI) {
        throw new RuntimeException(NYI);
    }
    
    public String lookupPrefix(String namespaceURI) {
        throw new RuntimeException(NYI);
    }
    
    public boolean isSameNode(Node other) {
        throw new RuntimeException(NYI);
    }
    
    public void setTextContent(String textContent) {
        throw new RuntimeException(NYI);
    }
    
    public String getTextContent() {
        throw new RuntimeException(NYI);
    }
    
    public short compareDocumentPosition(Node other) {
        throw new RuntimeException(NYI);
    }
    
    public String getBaseURI() {
        throw new RuntimeException(NYI);
    }
}
