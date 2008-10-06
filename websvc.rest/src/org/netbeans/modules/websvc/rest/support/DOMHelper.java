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
package org.netbeans.modules.websvc.rest.support;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 *
 * @author PeterLiu
 */
public class DOMHelper {

    private static final int TIME_TO_WAIT = 300;
    private static final String IDENT = "    ";     //NOI18N

    private FileObject fobj;
    private Document document;

    public DOMHelper(FileObject fobj) {
        this.fobj = fobj;
        this.document = getDocument();
    }

    private Document getDocument() {
        if (document == null) {
            DocumentBuilder builder = getDocumentBuilder();

            if (builder == null) {
                return null;
            }
            FileLock lock = null;
            InputStream is = null;

            try {
                lock = fobj.lock();
                is = fobj.getInputStream();
                document = builder.parse(is);
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
        return document;
    }

    public Element findElement(String tag, String value) {
        NodeList nodeList = document.getElementsByTagName(tag);
        int len = nodeList.getLength();

        for (int i = 0; i < len; i++) {
            Element element = (Element) nodeList.item(i);

            if (containsValue(element, value)) {
                return element;
            }
        }

        return null;
    }

    public Element findElement(String tag) {
        NodeList nodeList = document.getElementsByTagName(tag);

        if (nodeList.getLength() > 0) {
            return (Element) nodeList.item(0);
        }

        return null;
    }
    
    public NodeList findElements(String tag) {
        return document.getElementsByTagName(tag);
    }

    public Element findElementById(String id) {
        return document.getElementById(id);
    }
    public void appendChild(Element child) {
        document.getDocumentElement().appendChild(child);
    }

    public Element createElement(String tag) {
        return document.createElement(tag);
    }

    public Element createElement(String tag, String value) {
        Element element = document.createElement(tag);
        Text text = document.createTextNode(value);
        element.appendChild(text);

        return element;
    }

    public boolean containsValue(Element element, String value) {
        Node child = element.getFirstChild();

        if (child instanceof Text) {
            return (((Text) child).getWholeText().equals(value));
        }

        return false;
    }

    public String getValue(Element element) {
        Node child = element.getFirstChild();

        if (child instanceof Text) {
            return ((Text) child).getWholeText();
        }

        return "";      //NOI18N
    }

    public void setValue(Element element, String value) {
        Node child = element.getFirstChild();

        if (child instanceof Text) {
            ((Text) child).setData(value);
        }
    }

    public void save() {
        //beautify();
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                FileLock lock = null;
                OutputStream os = null;

                try {
                    DocumentType docType = document.getDoctype();
                    TransformerFactory factory = TransformerFactory.newInstance();
                    Transformer transformer = factory.newTransformer();
                    DOMSource source = new DOMSource(document);

                    lock = fobj.lock();
                    os = fobj.getOutputStream(lock);
                    StreamResult result = new StreamResult(os);

                    //transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
                    //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");        //NOI18N

                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");        //NOI18N

                    transformer.transform(source, result);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }, TIME_TO_WAIT);
    }

    private DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(false);
        factory.setValidating(false);

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return builder;
    }

    private void beautify() {
        beautify(document.getDocumentElement(), "\n");   //NOI18N

    }

    private void beautify(Node node, String indent) {
        NodeList children = node.getChildNodes();
        int length = children.getLength();
        ArrayList<Node> list = new ArrayList<Node>();

        for (int i = 0; i < length; i++) {
            list.add(children.item(i));
        }

        for (int i = 0; i < length; i++) {
            Node child = list.get(i);

            if (child instanceof Text) {
                continue;
            }
            node.insertBefore(document.createTextNode(indent + IDENT), child);
            beautify(child, indent + IDENT);

            if (i + 1 == length) {
                node.appendChild(document.createTextNode(indent));
            }
        }
    }
}
