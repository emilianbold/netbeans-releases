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
import javax.xml.xpath.*;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.xml.sax.SAXException;

public class AppletXMLRefactoringSupport {
    protected Document doc;
    protected final DocumentBuilder docBuilder;
    private XPathFactory xpFactory = XPathFactory.newInstance();
    private XPath xPath = xpFactory.newXPath();
    private XPathExpression appletClassXPression;
    private XPathExpression appletAIDXPression;

    private AppletXMLRefactoringSupport(DocumentBuilderFactory factory, File file) throws SAXException, IOException {
        try {
            docBuilder = factory.newDocumentBuilder();
            doc = docBuilder.parse(file);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Internal error: failed to obtain" + //NOI18N
                    " DocumentBuilder instance.", e); //NOI18N
        }
    }

    public static AppletXMLRefactoringSupport fromFile(File file) {
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);
            return new AppletXMLRefactoringSupport(factory, file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public NodeList getAppletClassElements() {
        try {
            return (NodeList) appletClassXPression.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
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
    public boolean isAppletDefined(String appletAID) {
        try {
            XPathExpression xPression = xPath.compile(
                    "/applet-app/applet/applet-AID[text()='" + appletAID + "']"); //NOI18N
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
    public void addAppletInfo(String appletAID, String className) {
        if (isAppletDefined(appletAID)) {
            throw new IllegalArgumentException("Servlet with name '" + appletAID  //NOI18N
                    + "' is already defined."); //NOI18N
        }
        addAppletElement(appletAID, className);
    }

    public void addAppletElement(String appletAID, String className) {
        Element servlet = doc.createElement(XmlTagNames.XML_APPLET_TAG);
        Element sName = doc.createElement(XmlTagNames.XML_APPLET_AID_TAG);
        sName.setTextContent(appletAID);
        Element sClass = doc.createElement(XmlTagNames.XML_APPLET_CLASS_TAG);
        sClass.setTextContent(className);
        servlet.appendChild(sClass);
        servlet.appendChild(sName);
        getAppletAppNode().appendChild(servlet);
    }

    public Node getAppletAppNode() {
        Node node = doc.getDocumentElement();

        if (node == null) {
            Element root = doc.createElement(XmlTagNames.XML_APPLET_APP_TAG);
            Attr attr = doc.createAttribute(XmlTagNames.XML_VERSION_ATTR);
            attr.setNodeValue(XmlTagNames.XML_APPLET_APP_VERSION);
            doc.appendChild(root);
            return root;
        } else if (node.getNodeName().equals(XmlTagNames.XML_APPLET_APP_TAG)) {
            return node;
        }
        return null;
    }

    public Collection<String> getAllAppletAIDs() {
        Set<String> names = new HashSet<String>();

        try {
            NodeList nodes = (NodeList) appletAIDXPression.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                names.add(nodes.item(i).getNodeValue());
            }
        } catch (XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return names;
    }


    {
        try {
            appletClassXPression = xPath.compile(
                    "/applet-app/applet/applet-class/text()"); //NOI18N
            appletAIDXPression = xPath.compile(
                    "/applet-app/applet/applet-AID/text()"); //NOI18N
        } catch (XPathExpressionException e) {
            Exceptions.printStackTrace(e);
        }
    }
}
