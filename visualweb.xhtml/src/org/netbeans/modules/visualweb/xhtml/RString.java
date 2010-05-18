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
package org.netbeans.modules.visualweb.xhtml;

import org.w3c.dom.*;

/**
 * <b>RString</b> is a text container class which is used for mixed.
 *
 * @version xhtml.rng 1.0 (Tue Apr 20 01:31:09 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class RString implements java.io.Serializable, Cloneable, IBlockMixMixed, IInlineModelMixed, IFlowModelMixed {
    private java.lang.Object value_;
    private boolean cdata_;

    /**
     * Creates a <code>RString</code>.
     *
     */
    public RString() {
    }

    /**
     * Creates a <code>RString</code> by the String <code>text</code>.
     *
     * @param text
     */
    public RString(String text) {
        value_ = text;
    }

    /**
     * Creates a <code>RString</code> by the DOM node <code>node</code>.
     *
     * @param node
     */
    public RString(org.w3c.dom.Node node) {
        value_ = node;
    }

    /**
     * Creates a <code>RString</code> by the Object <code>object</code>.
     *
     * @param object
     */
    public RString(java.lang.Object object) {
        value_ = object;
    }

    /**
     * Creates a <code>RString</code> by the Rstring <code>source</code>.
     *
     * @param source
     */
    public RString(RString source) {
        this(source.getContent());
    }

    /**
     * Creates a DOM representation of the object.
     * Result is appended to the Node <code>parent</code>.
     *
     * @param node
     */
    public void makeElement(Node node) {
        Document doc = node.getOwnerDocument();
        if (value_ instanceof org.w3c.dom.Node) {
            node.appendChild(doc.importNode((Node)value_, true));
        } else if (value_ != null) {
            if (cdata_) {
                node.appendChild(doc.createCDATASection(value_.toString()));
            } else {
                node.appendChild(doc.createTextNode(value_.toString()));
            }
        }
    }

    /**
     * Gets the text.
     *
     * @return String
     */
    public String getText() {
        if (value_ instanceof String) {
            return ((String)value_);
        } else {
            return (null);
        }
    }

    /**
     * Sets the text.
     *
     * @param text
     */
    public void setText(String text) {
        value_ = text;
    }

    /**
     * Gets the DOM node.
     *
     * @return org.w3c.dom.Node
     */
    public org.w3c.dom.Node getNode() {
        if (value_ instanceof org.w3c.dom.Node) {
            return ((org.w3c.dom.Node)value_);
        } else {
            return (null);
        }
    }

    /**
     * Sets the DOM node.
     *
     * @param node
     */
    public void setNode(org.w3c.dom.Node node) {
        value_ = node;
    }

    /**
     * Gets the object.
     *
     * @return Object
     */
    public java.lang.Object getObject() {
        if (value_ instanceof String || value_ instanceof org.w3c.dom.Node) {
            return (null);
        } else {
            return (value_);
        }
    }

    /**
     * Sets the DOM node.
     *
     * @param object
     */
    public void setObject(java.lang.Object object) {
        value_ = object;
    }

    /**
     * Gets the content.
     *
     * @return Object
     */
    public java.lang.Object getContent() {
        return (value_);
    }

    /**
     * Sets the content.
     *
     * @param value
     */
    public void setContent(Object value) {
        value_ = value;
    }

    /**
     * Checks whether cdata or not.
     *
     * @return boolean
     */
    public boolean isCdata() {
        return (cdata_);
    }

    /**
     * Sets wheter cdata or not.
     *
     * @param cdata
     */
    public void setCdata(boolean cdata) {
        cdata_ = cdata;
    }

    /**
     * Gets the text content as String.
     *
     * @return String
     */
    public String getContentAsString() {
        if (value_ == null) {;
            return (null);
        } else if (value_ instanceof org.w3c.dom.Node) {
            return (node2String4Data((Node)value_));
        } else {
            return (value_.toString());
        }
    }
    
    public static String node2String4Data(Node node) {
        StringBuffer buffer = new StringBuffer();
        _node2String4Data(node, buffer);
        return (new String(buffer));
    }

    private static void _node2String4Data(Node node, StringBuffer buffer) {
        switch(node.getNodeType()) {

        case Node.DOCUMENT_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.ELEMENT_NODE:
            Element element = (Element)node;
            String tag = element.getTagName();
            buffer.append('<');
            buffer.append(tag);
            NamedNodeMap attrs = element.getAttributes();
            int size = attrs.getLength();
            for (int i = 0;i < size;i++) {
                Attr attr = (Attr)attrs.item(i);
                buffer.append(' ');
                buffer.append(attr.getName());
                buffer.append("=\"");
                buffer.append(attr.getValue());
                buffer.append('\"');
            }
            buffer.append('>');
            NodeList nodes = element.getChildNodes();
            int nNodes = nodes.getLength();
            for (int i = 0;i < nNodes;i++) {
                _node2String4Data(nodes.item(i), buffer);
            }
            buffer.append("</");
            buffer.append(tag);
            buffer.append('>');
            break;
        case Node.ATTRIBUTE_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.COMMENT_NODE:
            throw (new UnsupportedOperationException("not supported yet"));
        case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE:
            Text text = (Text)node;
            buffer.append(text.getData());
            break;
        default:
            throw (new UnsupportedOperationException("not supported yet"));
        }
    }

    /**
     * Gets the String.
     *
     * @return String
     */
    public String toString() {
        return (getContentAsString());
    }

    /**
     * Clones the String.
     *
     * @return Object
     */
    public java.lang.Object clone() {
        return (new RString(this));
    }
}
