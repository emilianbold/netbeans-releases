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

package org.netbeans.modules.visualweb.complib;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utilities to provide a simpler interface to JAXP, the standard Java API for
 * XML Processing. Currently based on DOM.
 *
 * @author Edwin Goei
 */
public class XmlUtil {
    private static Method xalanMethod;

    private Document doc;

    private DocumentBuilder db;

    public XmlUtil() {
        this(null);
    }

    public XmlUtil(Document doc) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            assert false;
            throw new RuntimeException(e.getMessage(), e);
        }
        this.doc = doc;
    }

    public Document read(File inFile) throws XmlException {
        try {
            doc = db.parse(inFile);
        } catch (Exception e) {
            throw new XmlException("Unable to parse input file: " + inFile, e);
        }
        return doc;
    }

    public Document read(URI uri) throws XmlException {
        // TODO check how to close the input
        try {
            doc = db.parse(uri.toASCIIString());
        } catch (Exception e) {
            throw new XmlException("Unable to parse input URI: " + uri, e);
        }
        return doc;
    }

    public Document read(URL url) throws XmlException {
        URI uri;
        try {
            uri = new URI(url.toExternalForm());
        } catch (URISyntaxException e) {
            throw new XmlException("Unable to parse input URL: " + url, e);
        }
        read(uri);
        return doc;
    }

    public Document createDocument() {
        doc = db.newDocument();
        return doc;
    }

    public Document getDocument() {
        if (doc == null) {
            createDocument();
        }
        return doc;
    }

    /**
     * Use namespace version of createElement to create an element with a
     * unqualified name
     *
     * @param name
     * @return
     */
    public Element createElement(String name) {
        return getDocument().createElementNS(null, name);
    }

    /**
     * Write document to File using formatting. This will also close the output
     * file (I think).
     *
     * @param outFile
     * @throws XmlException
     */
    public void write(File outFile) throws XmlException {
        DOMSource domSource = new DOMSource(getDocument());
        StreamResult streamResult = new StreamResult(outFile);
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer xform = tf.newTransformer();
            // Format using 2 space indent. Output property name may depend
            // on version of Xalan being used so set both names.
            xform.setOutputProperty(OutputKeys.INDENT, "yes");
            xform.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
            xform.setOutputProperty(
                    "{http://xml.apache.org/xalan}indent-amount", "2");
            xform.transform(domSource, streamResult);
        } catch (TransformerException e) {
            throw new XmlException("Unable to write to file: " + outFile, e);
        }
    }

    /**
     * @param contextNode
     *            Node to start from
     * @param xpath
     *            XPath expression
     * @return NodeList containing resulting Nodes
     * @throws XmlException
     */
    public static NodeList selectNodeList(Node contextNode, String xpath)
            throws XmlException {
        // TODO maybe we should return an empty NodeList instead of null
        if (xalanMethod == null) {
            // FIXME This is a hack to get an XPath API working. We should use
            // the javax XPath API instead in Java 5, but we need to compile on
            // JDK 1.4 and run on JDK 5.
            Class clazz;
            try {
                clazz = Class.forName(
                        "com.sun.org.apache.xpath.internal.XPathAPI", true, // NOI18N
                        Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                IdeUtil.logWarning(
                        "JDK 5 version failed, so will next try JDK 1.4", e); // NOI18N
                try {
                    clazz = Class.forName("org.apache.xpath.XPathAPI", true, // NOI18N
                            Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e1) {
                    IdeUtil.logWarning(e1);
                    return null;
                }
            }

            try {
                xalanMethod = clazz.getDeclaredMethod("selectNodeList", // NOI18N
                        new Class[] { Node.class, String.class });
            } catch (Exception e) {
                IdeUtil.logWarning(e);
                return null;
            }
        }

        Object result = null;
        try {
            result = xalanMethod.invoke(null,
                    new Object[] { contextNode, xpath });
        } catch (Exception e) {
            IdeUtil.logWarning(e);
        }
        return (NodeList) result;

        // The following doesn't work with the NB Module ClassLoader
        // try {
        // return XPathAPI.selectNodeList(contextNode, xpath);
        // } catch (TransformerException e) {
        // throw new XmlException("selectNodeList", e);
        // }
    }

    /**
     * @param contextNode
     *            Node to start from
     * @param xpath
     *            XPath expression
     * @return The first node found that matches the XPath, or null.
     * @throws XmlException
     */
    public static Node selectSingleNode(Node contextNode, String xpath)
            throws XmlException {
        // TODO Use the javax XPath API instead
        NodeList nl = selectNodeList(contextNode, xpath);
        if (nl.getLength() == 0) {
            return null;
        } else {
            return nl.item(0);
        }
    }
}
