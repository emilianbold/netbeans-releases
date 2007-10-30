/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.websvc.rest.component.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.xpath.XPathExpressionException;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Owner
 */
public class RestComponentData {

    public static final String WSDL = "WSDL";
    public static final String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WADL = "WADL";
    public static final String WADL_NAMESPACE = "http://research.sun.com/wadl/2006/10";
    public static final String CUSTOM = "custom";
    public static final String CLASS_NAME = "org.netbeans.modules.websvc.rest.component.palette.RestComponentHandler";
    public static final String ICON16 = "org/netbeans/modules/websvc/rest/resources/RESTServiceIcon.png";
    public static final String ICON32 = "org/netbeans/modules/websvc/rest/resources/RESTServiceIcon.png";
    public static final String BUNDLE_NAME = "org.netbeans.modules.websvc.rest.component.palette.Bundle";
    private Document doc;
    private String name;
    private String displayName;
    private String categoryName;
    private String categoryPath;
    private String description;
    private String className = CLASS_NAME;
    private List<Icon> iconList;
    private Service service;

    public RestComponentData(Document doc) {
        this.doc = doc;
        this.iconList = new ArrayList<Icon>();
        try {
            init(doc);
        } catch (XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /*
     *
     * return component name
     */
    public String getName() {
        return name;
    }

    /*
     *
     * return localized name from bundle, else name if not found in bundle
     */
    public String getDisplayName() {
        return displayName;
    }

    /*
     *
     * return component path (path of components parent)
     */
    public String getCategoryPath() {
        return categoryPath;
    }

    /*
     *
     * return localized name from bundle, null if not found in bundle
     */
    public String getCategoryName() {
        return categoryName;
    }

    /*
     *
     * return description from bundle when <description bundle="" name=""/> is used, else
    return text node value of description. If text node not found, then
     *          return displayname of component
     */
    public String getDescription() {
        return description;
    }

    /*
     *
     * return category name from bundle, null if not found in bundle
     */
    public String getClassName() {
        return className;
    }

    /*
     *
     * return size 16 icon default to ICON16
     */
    public String getIcon16() {
        for (Icon icon : iconList) {
            if (icon.getSize().equals("16")) {
                return icon.getUrl();
            }
        }
        return ICON16;
    }

    /*
     *
     * return size 32 icon default to ICON32
     */
    public String getIcon32() {
        for (Icon icon : iconList) {
            if (icon.getSize().equals("32")) {
                return icon.getUrl();
            }
        }
        return ICON32;
    }

    /*
     *
     * return all available icons for use by client code.
     */
    public List<Icon> getIcons() {
        return iconList;
    }

    private void addIcon(Icon ic) {
        iconList.add(ic);
    }

    /*
     *
     * return Service data for the component
     */
    public Service getService() {
        return service;
    }

    public static boolean isWSDL(String type) {
        return type.equalsIgnoreCase(WSDL) || type.contains(WSDL_NAMESPACE);
    }

    public static boolean isWADL(String type) {
        return type.equalsIgnoreCase(WADL) || type.contains(WADL_NAMESPACE);
    }

    public static boolean isCustom(String type) {
        return type.equalsIgnoreCase(CUSTOM);
    }

    public void init(Document doc) throws XPathExpressionException, IOException {

        name = RestUtils.getAttributeValue(doc, "//component", "name");
        categoryPath = RestUtils.getAttributeValue(doc, "//component", "category");
        if (name == null || categoryPath == null) {
            return;
        }
        String nameKey = RestUtils.getAttributeValue(doc, "//component", "nameKey");
        String categoryKey = RestUtils.getAttributeValue(doc, "//component", "categoryKey");
        String bundle = RestUtils.getAttributeValue(doc, "//component", "bundle");
        displayName = RestPaletteUtils.getLocalizedString(bundle, nameKey, name);
        categoryName = RestPaletteUtils.getLocalizedString(bundle, categoryKey, categoryPath.startsWith("/") ? categoryPath.substring(1) : categoryPath);

        NodeList descNodes = RestUtils.getNodeList(doc, "//component/description");
        if (descNodes != null && descNodes.getLength() > 0) {
            Node descNode = descNodes.item(0);
            if (descNode.getAttributes() != null && descNode.getAttributes().getNamedItem("key") != null && descNode.getAttributes().getNamedItem("bundle") != null) {
                description = RestPaletteUtils.getLocalizedString(descNode.getAttributes().getNamedItem("bundle").getNodeValue(), descNode.getAttributes().getNamedItem("key").getNodeValue(), displayName);
            }
        }

        //Icons
        NodeList icons = RestUtils.getNodeList(doc, "//component/icons/icon");
        if (icons != null && icons.getLength() > 0) {
            for (int i = 0; i < icons.getLength(); i++) {
                Node icon = icons.item(i);
                String iconUrl = null;
                String iconSize = "16";
                Attr urlAttr = (Attr) icon.getAttributes().getNamedItem("url");
                if (urlAttr != null) {
                    iconUrl = urlAttr.getNodeValue();
                }
                Attr sizeAttr = (Attr) icon.getAttributes().getNamedItem("size");
                if (sizeAttr != null) {
                    iconSize = sizeAttr.getNodeValue();
                }
                if (iconUrl != null) {
                    addIcon(new Icon(iconUrl, iconSize));
                }
            }
        }

        //Service
        NodeList serviceNodes = RestUtils.getNodeList(doc, "//component/service");
        if (serviceNodes != null && serviceNodes.getLength() > 0) {
            service = new Service(RestUtils.getAttributeValue(doc, "//component/service", "name"));

            //Methods
            List<Method> methodList = new ArrayList<Method>();
            NodeList methods = RestUtils.getNodeList(doc, "//component/service/method");
            if (methods != null && methods.getLength() > 0) {
                for (int i = 0; i < methods.getLength(); i++) {
                    Node method = methods.item(i);
                    NamedNodeMap attrList = method.getAttributes();
                    String methodName = null;
                    String serviceName = null;
                    String portName = null;
                    String type = null;
                    String typeUrl = null;
                    String url = null;
                    Attr nameAttr = (Attr) attrList.getNamedItem("name");
                    if (nameAttr != null) {
                        methodName = nameAttr.getNodeValue();
                    }
                    Attr serviceNameAttr = (Attr) attrList.getNamedItem("serviceName");
                    if (serviceNameAttr != null) {
                        serviceName = serviceNameAttr.getNodeValue();
                    }
                    Attr portNameAttr = (Attr) attrList.getNamedItem("portName");
                    if (portNameAttr != null) {
                        portName = portNameAttr.getNodeValue();
                    }
                    Attr urlAttr = (Attr) attrList.getNamedItem("url");
                    if (urlAttr != null) {
                        url = urlAttr.getNodeValue();
                    }
                    Attr typeAttr = (Attr) attrList.getNamedItem("type");
                    if (typeAttr != null) {
                        type = typeAttr.getNodeValue();
                    }
                    List<Parameter> inputParams = getInputParams(method);
                    String mediaType = getMediaType(method);

                    if (methodName != null) {
                        service.addMethod(new Method(methodName, serviceName, portName, type, url, inputParams, mediaType));
                    }
                }
            }
        } else {
            NodeList classNodes = RestUtils.getNodeList(doc, "//component/classes/class");
            if (classNodes != null && classNodes.getLength() > 0) {
                className = RestUtils.getAttributeValue(doc, "//component/classes/class", "name");
            }
        }
    }

    private List<Parameter> getInputParams(Node methodNode) {
        List<Parameter> inputParams = new ArrayList<Parameter>();
        Node inputNode = getChildNode(methodNode, "input"); //NOI18N
        if (inputNode != null) {
            Node paramsNode = getChildNode(inputNode, "params");

            if (paramsNode != null) {
                NodeList children = paramsNode.getChildNodes();
                int size = children.getLength();

                for (int i = 0; i < size; i++) {
                    Node paramNode = children.item(i);

                    if (paramNode.getNodeName().equals("param")) {
                        NamedNodeMap attributes = paramNode.getAttributes();
                        String name = attributes.getNamedItem("name").getNodeValue();
                        String type = attributes.getNamedItem("type").getNodeValue();
                        String defaultValue = null;

                        Attr defaultAttr = (Attr) attributes.getNamedItem("default");

                        if (defaultAttr != null) {
                            defaultValue = defaultAttr.getNodeValue();
                        }

                        inputParams.add(new Parameter(name, type, defaultValue));
                    }
                }
            }
        }

        return inputParams;
    }

    private String getMediaType(Node methodNode) {
        String mediaType = "application/xml";
        Node outputNode = getChildNode(methodNode, "output"); //NOI18N
        if (outputNode != null) {
            Node mediaNode = getChildNode(outputNode, "media"); //NOI18N
            if (mediaNode != null) {
                NamedNodeMap attributes = mediaNode.getAttributes();
                mediaType = attributes.getNamedItem("type").getNodeValue();
            }
        }

        return mediaType;
    }

    private Node getChildNode(Node node, String name) {
        NodeList children = node.getChildNodes();
        int size = children.getLength();

        for (int i = 0; i < size; i++) {
            Node child = children.item(i);

            if (child.getNodeName().equals(name)) {
                return child;
            }
        }

        return null;
    }

    public class Icon {

        String url;
        String size;

        public Icon(String url, String size) {
            this.url = url;
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public String getSize() {
            return size;
        }
    }

    public class Service {

        private String name;
        private List<Method> methodList = Collections.emptyList();

        public Service(String name) {
            this.name = name;
            this.methodList = new ArrayList<Method>();
        }

        public String getName() {
            return name;
        }

        public List<Method> getMethods() {
            return methodList;
        }

        private void addMethod(Method m) {
            methodList.add(m);
        }
    }

    public class Method {

        private String name;
        private String serviceName;
        private String portName;
        /*'type' defines the type of service WSDL, WADL etc.,
        For WSDL it is WSDL or 'http://schemas.xmlsoap.org/wsdl/' or
        WSDL:[http://schemas.xmlsoap.org/wsdl/],
        for WADL it is WADL or 'http://research.sun.com/wadl/2006/10' or
        WADL:[http://research.sun.com/wadl/2006/10]
         */
        private String type;
        //URL of document (In case of WSDL it is the url of WSDL document)
        private String url;
        private List<Parameter> inputParams;
        private String mediaType;

        public Method(String name, String serviceName, String portName, String type, String url, List<Parameter> inputParams, String mediaType) {
            this.name = name;
            this.serviceName = serviceName;
            this.portName = portName;
            this.type = type;
            this.url = url;
            this.inputParams = inputParams;
            this.mediaType = mediaType;
        }

        public String getName() {
            return name;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getPortName() {
            return portName;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public List<Parameter> getInputParams() {
            return inputParams;
        }

        public String getMediaType() {
            return mediaType;
        }
    }

    public class Parameter {

        private String name;
        private String type;
        private String defaultValue;

        public Parameter(String name, String type, String defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }
}