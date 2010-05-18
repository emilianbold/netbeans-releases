/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder.xsd;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A sample DOM writer. This class traverses a DOM tree to print an xml document.
 * 
 * @author
 */

public class DOMWriter {

    /** Default Encoding */
    private static final String PRINTWRITER_ENCODING = "UTF-8";

    private static final String TAB = "  ";

    /** Canonical output. */
    protected boolean canonical;

    /** Print writer. */
    protected PrintWriter out;

    /**
     * @param w Writer
     * @param canonical canonical
     * @exception UnsupportedEncodingException Unsupported encoding exception
     * @todo Document this constructor
     */
    public DOMWriter(final Writer w, final boolean canonical) throws UnsupportedEncodingException {
        this.out = new PrintWriter(w);
        this.canonical = canonical;
    }

    /**
     * Returns the Writer Encoding
     * 
     * @return Encoding used
     */
    public static String getWriterEncoding() {
        return DOMWriter.PRINTWRITER_ENCODING;
    }

    // getWriterEncoding

    /**
     * @param node Node
     * @todo Document this method
     */
    public void print(final String indent, final Node node) {
        this.print(indent, node, true);
    }

    /**
     * Prints the specified node, recursively.
     * 
     * @param node Node
     * @param prettyprint Pretty print the result
     */
    public void print(final String indent, final Node node, final boolean prettyprint) {
        // is there anything to do?
        if (node == null) {
            return;
        }

        final int type = node.getNodeType();
        switch (type) {
        // print document
        case Node.DOCUMENT_NODE: {
            if (!this.canonical) {
                String encoding = DOMWriter.getWriterEncoding();
                if (encoding.equalsIgnoreCase("DEFAULT")) {
                    encoding = "UTF-8";
                } else if (encoding.equalsIgnoreCase("Unicode")) {
                    encoding = "UTF-16";
                } else {
                    // encoding = MIME2Java.reverse(encoding);
                }

                this.out.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
            }
            final NodeList children = node.getChildNodes();
            for (int iChild = 0; iChild < children.getLength(); iChild++) {
                this.print(indent, children.item(iChild));
            }
            this.out.flush();
            break;
        }
            // print element with attributes
        case Node.ELEMENT_NODE: {
            this.out.print(indent + '<');
            this.out.print(node.getNodeName());
            final Attr attrs[] = this.sortAttributes(node.getAttributes());
            for (int i = 0; i < attrs.length; i++) {
                final Attr attr = attrs[i];
                this.out.print(' ');
                this.out.print(attr.getNodeName());
                this.out.print("=\"");
                this.out.print(this.normalize(attr.getNodeValue()));
                this.out.print('"');
            }
            this.out.print('>');
            final NodeList children = node.getChildNodes();
            if (children != null) {
                final int len = children.getLength();
                for (int i = 0; i < len; i++) {
                    final Node child = children.item(i);
                    if (child.getNodeType() != Node.TEXT_NODE) {
                        this.out.println();
                    }
                    this.print(indent + DOMWriter.TAB, children.item(i));
                }
            }
            break;
        }
            // handle entity reference nodes
        case Node.ENTITY_REFERENCE_NODE: {
            if (this.canonical) {
                final NodeList children = node.getChildNodes();
                if (children != null) {
                    final int len = children.getLength();
                    for (int i = 0; i < len; i++) {
                        this.print(indent, children.item(i));
                    }
                }
            } else {
                this.out.print('&');
                this.out.print(node.getNodeName());
                this.out.print(';');
            }
            break;
        }
            // print cdata sections
        case Node.CDATA_SECTION_NODE: {
            if (this.canonical) {
                this.out.print(this.normalize(node.getNodeValue()));
            } else {
                this.out.print("<![CDATA[");
                this.out.print(node.getNodeValue());
                this.out.print("]]>");
            }
            break;
        }
            // print text
        case Node.TEXT_NODE: {
            this.out.print(this.normalize(node.getNodeValue()));
            break;
        }
            // print processing instruction
        case Node.PROCESSING_INSTRUCTION_NODE: {
            this.out.print("<?");
            this.out.print(node.getNodeName());
            final String data = node.getNodeValue();
            if (data != null && data.length() > 0) {
                this.out.print(' ');
                this.out.print(data);
            }
            this.out.println("?>");
            break;
        }
        }

        if (type == Node.ELEMENT_NODE) {
            if (this.containsOnlyTextNode(node)) {
                this.out.print("</");
            } else {
                this.out.println();
                this.out.print(indent + "</");
            }
            this.out.print(node.getNodeName());
            this.out.print('>');
            // if (prettyprint) {
            // out.println();
            // }
        }

        this.out.flush();

    }

    /**
     * Normalizes the given string.
     * 
     * @param s String to be normalized
     * @return normalized string
     */
    protected String normalize(final String s) {
        final StringBuffer str = new StringBuffer();
        final int len = s != null ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            final char ch = s.charAt(i);
            switch (ch) {
            case '<': {
                str.append("&lt;");
                break;
            }
            case '>': {
                str.append("&gt;");
                break;
            }
            case '&': {
                str.append("&amp;");
                break;
            }
            case '"': {
                str.append("&quot;");
                break;
            }
            case '\r':
            case '\n': {
                if (this.canonical) {
                    str.append("&#");
                    str.append(Integer.toString(ch));
                    str.append(';');
                } else {
                    // else, default append char
                    str.append(ch);
                }
                break;
            }
            default: {
                str.append(ch);
            }
            }
        }
        return str.toString();
    }

    /**
     * Returns a sorted list of attributes.
     * 
     * @param attrs Map of named nodes
     * @return Array of sorted list of attributes
     */
    protected Attr[] sortAttributes(final NamedNodeMap attrs) {
        final int len = attrs != null ? attrs.getLength() : 0;
        final Attr array[] = new Attr[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Attr) attrs.item(i);
        }

        for (int i = 0; i < len - 1; i++) {
            String name = array[i].getNodeName();
            int index = i;
            for (int j = i + 1; j < len; j++) {
                final String curName = array[j].getNodeName();
                if (curName.compareTo(name) < 0) {
                    name = curName;
                    index = j;
                }
            }

            if (index != i) {
                final Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }

        return array;
    }

    private boolean containsOnlyTextNode(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() != Node.TEXT_NODE) {
                return false;
            }
        }
        return true;
    }

}
