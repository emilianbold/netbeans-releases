/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.refactoring;

import java.io.IOException;
import org.netbeans.modules.javacard.constants.XmlTagNames;
import org.openide.util.Exceptions;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.SAXException;

public class WebXMLRefactoringSupport {


    private WebXMLRefactoringSupport(Document doc) {
        this.doc = doc;
    }

    public static WebXMLRefactoringSupport fromFile(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder docBuilder;
            docBuilder = factory.newDocumentBuilder();
            return new WebXMLRefactoringSupport(docBuilder.parse(file));
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException (ex);
        } catch (SAXException ex) {
            throw new IllegalStateException (ex);
        } catch (IOException ex) {
            throw new IllegalStateException (ex);
        }
    }

    public NodeList getServletClassElements() {
        try {
            return (NodeList) servletClassXPression.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WebXMLRefactoringSupport.class.getName()).log(
                    Level.WARNING, null, ex);
            return null;
        }
    }

    public NodeList getListenerClassElements() {
        try {
            return (NodeList) listenerClassXPression.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WebXMLRefactoringSupport.class.getName()).log(
                    Level.WARNING, null, ex);
            return null;
        }
    }

    public NodeList getFilterClassElements() {
        try {
            return (NodeList) filterClassXPression.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WebXMLRefactoringSupport.class.getName()).log(
                    Level.WARNING, null, ex);
            return null;
        }
    }

    public Document getDocument() {
        return doc;
    }

    /**
     * Checks if there is a servlet element with specified Servlet name.
     * 
     * @param servletName   Servlet name to check.
     * 
     * @return  <code>true</code> if servle with specified name is present,
     *          <code>false</code> otherwise.
     */
    public boolean isServletDefined(String servletName) {
        try {
            XPathExpression xPression = xPath.compile(
                    "/web-app/servlet/servlet-name[text()='" + servletName + "']");
            return (Boolean) xPression.evaluate(doc, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException ex) {
            return false;
        }
    }

    /**
     * Adds servlet and servlet-mapping elements for specified servlet info.
     * 
     * @param servletName   Servlet name.
     * @param className     Name of the Servlet class.
     * @param urlPattern    URL pattern for the Servlet.
     * 
     * @throws IllegalArgumentException if argument are invalid or the specified
     *      servlet already exists.
     */
    public void addServletInfo(String servletName, String className,
            String urlPattern) {
        if (isServletDefined(servletName)) {
            throw new IllegalArgumentException("Servlet with name '" + servletName + "' is already defined.");
        }
        addServletElement(servletName, className);
        if (urlPattern != null) {
            addServletMappingElement(servletName, urlPattern);
        }
    }

    public void addServletElement(String servletName, String className) {
        Element servlet = doc.createElement(XmlTagNames.XML_SERVLET_TAG);
        Element sName = doc.createElement(XmlTagNames.XML_SERVLET_NAME_TAG);
        sName.setTextContent(servletName);
        Element sClass = doc.createElement(XmlTagNames.XML_SERVLET_CLASS_TAG);
        sClass.setTextContent(className);
        servlet.appendChild(sName);
        servlet.appendChild(sClass);
        getWebAppNode().appendChild(servlet);
    }

    public void addServletMappingElement(String servletName, String urlPattern) {
        Element mapping = doc.createElement(XmlTagNames.XML_SERVLET_MAPPING_TAG);
        Element sName = doc.createElement(XmlTagNames.XML_SERVLET_NAME_TAG);
        sName.setTextContent(servletName);
        Element url = doc.createElement(XmlTagNames.XML_URL_PATTERN_TAG);
        url.setTextContent(urlPattern);
        mapping.appendChild(sName);
        mapping.appendChild(url);
        getWebAppNode().appendChild(mapping);
    }

    public Node getWebAppNode() {
        Node node = doc.getDocumentElement();

        if (node == null) {
            Element root = doc.createElement(XmlTagNames.XML_WEB_APP_TAG);
            Attr attr = doc.createAttribute(XmlTagNames.XML_VERSION_ATTR);
            attr.setNodeValue(XmlTagNames.XML_WEB_APP_VERSION);
            doc.appendChild(root);
            return root;
        } else if (node.getNodeName().equals(XmlTagNames.XML_WEB_APP_TAG)) {
            return node;
        }
        return null;
    }

    public Collection<String> getAllServletNames() {
        Set<String> names = new HashSet<String>();

        try {
            NodeList nodes = (NodeList) servletNameXPression.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                names.add(nodes.item(i).getNodeValue());
            }
        } catch (XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return names;
    }

    public String[] getURLPatterns(String servletName) {
        try {
            XPathExpression xPression = xPath.compile(
                    "/web-app/servlet-mapping[servlet-name/text()=\"" + servletName + "\"]/url-pattern/text()");
            NodeList nodes = (NodeList) xPression.evaluate(
                    doc, XPathConstants.NODESET);
            String[] patterns = new String[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++) {
                patterns[i] = nodes.item(i).getNodeValue();
            }
            return patterns;
        } catch (XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new String[]{};
    }
    protected Document doc;

    private XPathFactory xpFactory = XPathFactory.newInstance();
    private XPath xPath = xpFactory.newXPath();
    private XPathExpression servletClassXPression;
    private XPathExpression filterClassXPression;
    private XPathExpression listenerClassXPression;
    private XPathExpression servletNameXPression;
    

    {
        try {
            servletClassXPression =
                    xPath.compile("/web-app/servlet/servlet-class/text()");
            filterClassXPression =
                    xPath.compile("/web-app/filter/filter-class/text()");
            listenerClassXPression =
                    xPath.compile("/web-app/listener/listener-class/text()");
            servletNameXPression =
                    xPath.compile("/web-app/servlet/servlet-name/text()");
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Internal initialization failure", e);
        }
    }
}
