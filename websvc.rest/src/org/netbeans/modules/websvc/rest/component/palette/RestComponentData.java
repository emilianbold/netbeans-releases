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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.xpath.XPathExpressionException;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.support.Utils;
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
        for(Icon icon:iconList) {
            if(icon.getSize().equals("16"))
                return icon.getUrl();
        }
        return ICON16;
    }
    
    /*
     *
     * return size 32 icon default to ICON32
     */
    public String getIcon32() {
        for(Icon icon:iconList) {
            if(icon.getSize().equals("32"))
                return icon.getUrl();
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
        return type.startsWith(WSDL) || type.contains(WSDL_NAMESPACE);
    }
    
    public static boolean isWADL(String type) {
        return type.startsWith(WADL) || type.contains(WADL_NAMESPACE);
    }
    
    public void init(Document doc) throws XPathExpressionException, IOException {
        
        name = RestUtils.getAttributeValue(doc, "//component", "name");
        categoryPath = RestUtils.getAttributeValue(doc, "//component", "category");
        if(name == null || categoryPath == null)
            return;
        String nameKey = RestUtils.getAttributeValue(doc, "//component", "nameKey");
        String categoryKey = RestUtils.getAttributeValue(doc, "//component", "categoryKey");
        String bundle = RestUtils.getAttributeValue(doc, "//component", "bundle");
        displayName = RestPaletteUtils.getLocalizedString(bundle, nameKey, name);
        categoryName = RestPaletteUtils.getLocalizedString(bundle, categoryKey, 
            categoryPath.startsWith("/")?categoryPath.substring(1):categoryPath);        
        
        NodeList descNodes = RestUtils.getNodeList(doc, "//component/description");
        if(descNodes != null && descNodes.getLength() > 0) {
            Node descNode = descNodes.item(0);
            if(descNode.getAttributes() != null &&
                    descNode.getAttributes().getNamedItem("key") != null &&
                    descNode.getAttributes().getNamedItem("bundle") != null) {
                description = RestPaletteUtils.getLocalizedString(
                        descNode.getAttributes().getNamedItem("bundle").getNodeValue(),
                        descNode.getAttributes().getNamedItem("key").getNodeValue(), displayName);
            }
        }
        
        //Icons
        NodeList icons = RestUtils.getNodeList(doc, "//component/icons/icon");
        if(icons != null && icons.getLength() > 0) {
            for(int i=0;i<icons.getLength();i++) {
                Node icon = icons.item(i);
                String iconUrl = null;
                String iconSize = "16";
                Attr urlAttr = (Attr) icon.getAttributes().getNamedItem("url");
                if(urlAttr != null)
                    iconUrl = urlAttr.getNodeValue();
                Attr sizeAttr = (Attr) icon.getAttributes().getNamedItem("size");
                if(sizeAttr != null)
                    iconSize = sizeAttr.getNodeValue();
                if(iconUrl != null)
                    addIcon(new Icon(iconUrl, iconSize));
            }
        }
        
        //Service
        NodeList serviceNodes = RestUtils.getNodeList(doc, "//component/service");
        if(serviceNodes != null && serviceNodes.getLength() > 0) {         
            service = new Service(RestUtils.getAttributeValue(doc, "//component/service", "name"));
            
            //Methods
            List<Method> methodList = new ArrayList<Method>();
            NodeList methods = RestUtils.getNodeList(doc, "//component/service/method");
            if(methods != null && methods.getLength() > 0) {
                for(int i=0;i<methods.getLength();i++) {
                    Node method = methods.item(i);
                    NamedNodeMap attrList = method.getAttributes();
                    String methodName = null;
                    String serviceName = null;
                    String portName = null;
                    String type = null;
                    String typeUrl = null;
                    String url = null;
                    Attr nameAttr = (Attr) attrList.getNamedItem("name");
                    if(nameAttr != null)
                        methodName = nameAttr.getNodeValue();
                    Attr serviceNameAttr = (Attr) attrList.getNamedItem("serviceName");
                    if(serviceNameAttr != null)
                        serviceName = serviceNameAttr.getNodeValue();
                    Attr portNameAttr = (Attr) attrList.getNamedItem("portName");
                    if(portNameAttr != null)
                        portName = portNameAttr.getNodeValue();
                    Attr urlAttr = (Attr) attrList.getNamedItem("url");
                    if(urlAttr != null)
                        url = urlAttr.getNodeValue();
                    Attr typeAttr = (Attr) attrList.getNamedItem("type");
                    if(typeAttr != null)
                        type = typeAttr.getNodeValue();
                    
                    if(methodName != null)
                        service.addMethod(new Method(methodName, 
                                serviceName, portName, type, url));
                }
            }
        } else {
            NodeList classNodes = RestUtils.getNodeList(doc, "//component/classes/class");
            if(classNodes != null && classNodes.getLength() > 0) {
                className = RestUtils.getAttributeValue(doc, "//component/classes/class", "name");
            }
        }
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
        
        public Method(String name, String serviceName, String portName, 
                String type, String url) {
            this.name = name;
            this.serviceName = serviceName;
            this.portName = portName;
            this.type = type;
            this.url = url;
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
    }
}
