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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.registry.jaxrpc;


public class Configuration {

    private WsdlType wsdl;

    public Configuration() {
    }

    public void setWsdl(WsdlType value) {
        wsdl = value;
    }

    public WsdlType getWsdl() {
        return wsdl;
    }

    public void write(java.io.OutputStream out) throws java.io.IOException {
        write(out, null);
    }

    public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
        java.io.Writer w;
        if (encoding == null) {
            encoding = "UTF-8";	// NOI18N
        }
        w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
        write(w, encoding);
        w.flush();
    }
    
    public void write(java.io.Writer out, String encoding) throws java.io.IOException {
        out.write("<?xml version='1.0'");	// NOI18N
        if (encoding != null)
            out.write(" encoding='"+encoding+"'");	// NOI18N
        out.write(" ?>\n");	// NOI18N
        writeNode(out, "configuration", "");	// NOI18N
    }
    
    public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
        out.write(indent);
        out.write("<");
        out.write(nodeName);
        out.write(" xmlns='");	// NOI18N
        out.write("http://java.sun.com/xml/ns/jax-rpc/ri/config");	// NOI18N
        out.write("'");	// NOI18N
        out.write(">\n");
        String nextIndent = indent + "	";
        if (wsdl != null) {
            wsdl.writeNode(out, "wsdl", nextIndent);
        }
        out.write(indent);
        out.write("</"+nodeName+">\n");
    }
    
    public static Configuration read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        return read(new org.xml.sax.InputSource(in), false, null, null);
    }
    
    public static Configuration read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setValidating(validate);
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        if (er != null)	db.setEntityResolver(er);
        if (eh != null)	db.setErrorHandler(eh);
        org.w3c.dom.Document doc = db.parse(in);
        return read(doc);
    }
    
    public static Configuration read(org.w3c.dom.Document document) {
        Configuration aConfiguration = new Configuration();
        aConfiguration.readNode(document.getDocumentElement());
        return aConfiguration;
    }
    
    public void readNode(org.w3c.dom.Node node) {
        if (node.hasAttributes()) {
            org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
            org.w3c.dom.Attr attr;
            java.lang.String attrValue;
            attr = (org.w3c.dom.Attr) attrs.getNamedItem("xsi:schemaLocation");
            if (attr != null) {
                attrValue = attr.getValue();
            } else {
                attrValue = null;
            }
            //schemaLocation = attrValue;
        }
        org.w3c.dom.NodeList children = node.getChildNodes();
        for (int i = 0, size = children.getLength(); i < size; ++i) {
            org.w3c.dom.Node childNode = children.item(i);
            String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
            String childNodeValue = "";
            if (childNode.getFirstChild() != null) {
                childNodeValue = childNode.getFirstChild().getNodeValue();
            }else if (childNodeName == "wsdl") {
                wsdl = new WsdlType();
                wsdl.readNode(childNode);
            }else {
                // Found extra unrecognized childNode
            }
        }
    }
    
    public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
        writeXML(out, msg, true);
    }
    
    public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
        if (msg == null)
            return;
        int msgLength = msg.length();
        for (int i = 0; i < msgLength; ++i) {
            char c = msg.charAt(i);
            writeXML(out, c, attribute);
        }
    }
    
    public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
        if (msg == '&')
            out.write("&amp;");
        else if (msg == '<')
            out.write("&lt;");
        else if (msg == '>')
            out.write("&gt;");
        else if (attribute && msg == '"')
            out.write("&quot;");
        else if (attribute && msg == '\'')
            out.write("&apos;");
        else if (attribute && msg == '\n')
            out.write("&#xA;");
        else if (attribute && msg == '\t')
            out.write("&#x9;");
        else
            out.write(msg);
    }
}


