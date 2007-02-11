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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.DependencyType;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.Feature;
import org.netbeans.installer.utils.helper.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class XMLUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String ATTR_BEGIN  = "[@";
    private static final String ATTR_END    = "]";
    private static final String ATTR_DELIM  = "=";
    private static final String ATTRS_DELIM = " and ";
    
    public static final String XSLT_REFORMAT_RESOURCE =
            "org/netbeans/installer/utils/xml/reformat.xslt";
    public static final String XSLT_REFORMAT_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + XSLT_REFORMAT_RESOURCE;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static void saveXMLDocument(Document document, File file) throws XMLException {
        FileOutputStream output = null;
        try {
            saveXMLDocument(document, output = new FileOutputStream(file));
        } catch (IOException e) {
            throw new XMLException("Cannot save XML document", e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    ErrorManager.notifyDebug("Could not close the stream", e);
                }
            }
        }
    }
    
    public static void saveXMLDocument(Document document, OutputStream output) throws XMLException {
        try {
            final Source domSource = new DOMSource(
                    document);
            final Result streamResult = new StreamResult(
                    output);
            final Source xsltSource = new StreamSource(
                    FileProxy.getInstance().getFile(XSLT_REFORMAT_URI));
            final Transformer transformer = TransformerFactory.
                    newInstance().newTransformer(xsltSource);
            
            transformer.transform(domSource, streamResult);
        } catch (DownloadException e) {
            throw new XMLException("Cannot save XML document", e);
        } catch (TransformerConfigurationException e) {
            throw new XMLException("Cannot save XML document", e);
        } catch (TransformerException e) {
            throw new XMLException("Cannot save XML document", e);
        }
    }
    
    public static Document loadXMLDocument(File file) throws XMLException {
        FileInputStream input = null;
        try {
            return loadXMLDocument(input = new FileInputStream(file));
        } catch (IOException e) {
            throw new XMLException("Cannot open XML file", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    ErrorManager.notifyDebug("Cannot close the stream", e);
                }
            }
        }
    }
    
    public static Document loadXMLDocument(InputStream input) throws XMLException {
        try {
            return DocumentBuilderFactory.
                    newInstance().newDocumentBuilder().parse(input);
        } catch (ParserConfigurationException e) {
            throw new XMLException("Cannot parse XML", e);
        } catch (SAXException e) {
            throw new XMLException("Cannot parse XML", e);
        } catch (IOException e) {
            throw new XMLException("Cannot parse XML", e);
        }
    }
    
    public static List<Node> getChildList(Node root, String... children) {
        List<Node> resultList = new LinkedList<Node>();
        
        if (root != null)  {
            if (children.length > 0) {
                resultList.add(root);
                if (children.length == 1) {
                    if (children[0].startsWith("./")) {
                        children [0] = children [0].substring("./".length());
                    }
                    children = children [0].split("/");
                }
                
                for (String child: children) {
                    resultList = getChildListFromRootList(resultList, child);
                }
            }
        }
        return resultList;
    }
    
    public static List<Element> getChildren(Element element) {
        List<Element> children = new LinkedList<Element>();
        
        final NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            
            if (node instanceof Element) {
                children.add((Element) node);
            }
        }
        
        return children;
    }
    
    public static List<Element> getChildren(Element element, String... names) {
        List<Element> children = new LinkedList<Element>();
        
        final NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            
            if (node instanceof Element) {
                for (int j = 0; j < names.length; j++) {
                    if (node.getNodeName().equals(names[j])) {
                        children.add((Element) node);
                        break;
                    }
                }
            }
        }
        
        return children;
    }
    
    public static Element getChild(Element element, String name) {
        Element child = null;
        
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if ((node instanceof Element) && node.getNodeName().equals(name)) {
                child = (Element) node;
                break;
            }
        }
        
        return child;
    }
    
    public static Node getChildNode(Node root, String... children) throws ParseException {
        List<Node> list = getChildList(root, children);
        
        if (list.size() == 0) {
            return null;
        }
        
        if (list.size() > 1 ) {
            throw new ParseException("Requested single node, returned " +
                    list.size() + " nodes");
        }
        
        return list.get(0);
    }
    
    public static String getAttribute(Node node, String name) {
        String value = null;
        
        if ((node != null) && (name != null)) {
            if (name.startsWith("./@")) {
                name = name.substring("./@".length());
            }
            
            NamedNodeMap map = node.getAttributes();
            if (map != null) {
                Node attribute = map.getNamedItem(name);
                if ((attribute != null) && (attribute.getNodeType() == Node.ATTRIBUTE_NODE)) {
                    value = attribute.getNodeValue();
                }
            }
        }
        
        return value;
    }
    
    public static String getTextContent(Node node) {
        return (node == null) ? null : node.getTextContent();
    }
    
    public static String getChildNodeTextContent(Node root, String... childs) throws ParseException {
        return getTextContent(getChildNode(root,childs));
    }
    
    public static Element addChildNode(Node parentNode, String tag, String textContent) {
        Element result = null;
        if (parentNode!=null && tag!=null) {
            
            result = parentNode.getOwnerDocument().createElement(tag);
            if (textContent!=null) {
                result.setTextContent(textContent);
            }
            parentNode.appendChild(result);
        }
        return result;
    }
    
    // object <-> dom ///////////////////////////////////////////////////////////////
    public static Dependency parseDependency(final Element element) throws ParseException {
        final DependencyType type =
                StringUtils.parseDependencyType(element.getNodeName());
        final String uid =
                XMLUtils.getAttribute(element, "uid");
        final Version lower =
                Version.getVersion(element.getAttribute("version-lower"));
        final Version upper =
                Version.getVersion(element.getAttribute("version-upper"));
        final Version resolved =
                Version.getVersion(element.getAttribute("version-resolved"));
        
        return new Dependency(type, uid, lower, upper, resolved);
    }
    
    public static Element saveDependency(final Dependency dependency, final Element element) {
        element.setAttribute("uid",
                dependency.getUid());
        
        if (dependency.getVersionLower() != null) {
            element.setAttribute("version-lower",
                    dependency.getVersionLower().toString());
        }
        if (dependency.getVersionUpper() != null) {
            element.setAttribute("version-upper",
                    dependency.getVersionUpper().toString());
        }
        if (dependency.getVersionResolved() != null) {
            element.setAttribute("version-resolved",
                    dependency.getVersionResolved().toString());
        }
        
        return element;
    }
    
    public static List<Dependency> parseDependencies(final Element element) throws ParseException {
        final List<Dependency> dependencies = new LinkedList<Dependency>();
        
        for (Element child: getChildren(element)) {
            dependencies.add(parseDependency(child));
        }
        
        return dependencies;
    }
    
    public static Element saveDependencies(final List<Dependency> dependencies, final Element element) {
        final Document document = element.getOwnerDocument();
        
        for (Dependency dependency: dependencies) {
            element.appendChild(saveDependency(
                    dependency,
                    document.createElement(dependency.getType().toString())));
        }
        
        return element;
    }
    
    public static Properties parseProperties(final Element element) throws ParseException {
        final Properties properties = new Properties();
        
        if (element != null) {
            for (Element child: XMLUtils.getChildren(element, "property")) {
                String name  = XMLUtils.getAttribute(child, "name");
                String value = XMLUtils.getTextContent(child);
                
                properties.setProperty(name, value);
            }
        }
        
        return properties;
    }
    
    public static Element saveProperties(final Properties properties, final Element element) {
        final Document document = element.getOwnerDocument();
        
        for (Object key: properties.keySet()) {
            final Element propertyElement = document.createElement("property");
            
            propertyElement.setAttribute("name", key.toString());
            propertyElement.setTextContent(properties.get(key).toString());
            
            element.appendChild(propertyElement);
        }
        
        return element;
    }
    
    public static ExtendedUri parseExtendedUri(final Element element) throws ParseException {
        try {
            final URI uri = new URI(XMLUtils.getChildNodeTextContent(
                    element,
                    "default-uri"));
            final long size = Long.parseLong(XMLUtils.getAttribute(
                    element,
                    "size"));
            final String md5 = XMLUtils.getAttribute(
                    element,
                    "md5");
            
            final List<URI> alternates = new LinkedList<URI>();
            
            for (Element alternateElement: XMLUtils.getChildren(
                    element, "alternate-uri")) {
                alternates.add(new URI(alternateElement.getTextContent()));
            }
            
            if (uri.getScheme().equals("file")) {
                return new ExtendedUri(uri, alternates, uri, size, md5);
            } else {
                return new ExtendedUri(uri, alternates, size, md5);
            }
        } catch (URISyntaxException e) {
            throw new ParseException("Cannot parse extended URI", e);
        } catch (NumberFormatException e) {
            throw new ParseException("Cannot parse extended URI", e);
        }
    }
    
    public static Element saveExtendedUri(final ExtendedUri uri, final Element element) {
        final Document document = element.getOwnerDocument();
        
        element.setAttribute("size", Long.toString(uri.getSize()));
        element.setAttribute("md5", uri.getMd5());
        
        // the default uri would be either "local" (if it's present) or the
        // "remote" one
        Element uriElement = document.createElement("default-uri");
        if (uri.getLocal() != null) {
            uriElement.setTextContent(uri.getLocal().toString());
        } else {
            uriElement.setTextContent(uri.getRemote().toString());
        }
        element.appendChild(uriElement);
        
        // if the "local" uri is not null, we should save the "remote" uri as the
        // first alternate, unless it's the same as the local
        if ((uri.getLocal() != null) && !uri.getRemote().equals(uri.getLocal())) {
            uriElement = document.createElement("alternate-uri");
            
            uriElement.setTextContent(uri.getRemote().toString());
            element.appendChild(uriElement);
        }
        
        for (URI alternateUri: uri.getAlternates()) {
            if (!alternateUri.equals(uri.getRemote())) {
                uriElement = document.createElement("alternate-uri");
                
                uriElement.setTextContent(alternateUri.toString());
                element.appendChild(uriElement);
            }
        }
        
        return element;
    }
    
    public static List<ExtendedUri> parseExtendedUrisList(final Element element) throws ParseException {
        final List<ExtendedUri> uris = new LinkedList<ExtendedUri>();
        
        for (Element uriElement: getChildren(element)) {
            uris.add(parseExtendedUri(uriElement));
        }
        
        return uris;
    }
    
    public static Element saveExtendedUrisList(final List<ExtendedUri> uris, final Element element) {
        final Document document = element.getOwnerDocument();
        
        for (ExtendedUri uri: uris) {
            element.appendChild(
                    saveExtendedUri(uri, document.createElement("file")));
        }
        
        return element;
    }
    
    public static Map<Locale, String> parseLocalizedString(final Element element) throws ParseException {
        final Map<Locale, String> map = new HashMap<Locale, String>();
        
        final Element defaultElement = getChild(element, "default");
        map.put(Locale.getDefault(), defaultElement.getTextContent());
        
        for (Element localizedElement: getChildren(element, "localized")) {
            final Locale locale = StringUtils.parseLocale(
                    localizedElement.getAttribute("locale"));
            final String localizedString = StringUtils.parseAscii(
                    localizedElement.getTextContent());
            
            map.put(locale, localizedString);
        }
        
        return map;
    }
    
    public static Element saveLocalizedString(final Map<Locale, String> map, final Element element) {
        final Document document = element.getOwnerDocument();
        
        final Element defaultElement = document.createElement("default");
        defaultElement.setTextContent(map.get(Locale.getDefault()));
        element.appendChild(defaultElement);
        
        for (Locale locale: map.keySet()) {
            if (!map.get(locale).equals(map.get(Locale.getDefault()))) {
                final Element localizedElement = document.createElement("localized");
                
                localizedElement.setAttribute("locale", locale.toString());
                localizedElement.setTextContent(map.get(locale));
                
                element.appendChild(localizedElement);
            }
        }
        
        return element;
    }
    
    public static Feature parseFeature(final Element element) throws ParseException {
        final String id = element.getAttribute("id");
        final long offset = Long.parseLong(element.getAttribute("offset"));
        final ExtendedUri iconUri = parseExtendedUri(getChild(element, "icon"));
        
        final Map<Locale, String> displayNames =
                parseLocalizedString(getChild(element, "display-name"));
        final Map<Locale, String> descriptions =
                parseLocalizedString(getChild(element, "description"));
        
        return new Feature(id, offset, iconUri, displayNames, descriptions);
    }
    
    public static Element saveFeature(final Feature feature, final Element element) {
        final Document document = element.getOwnerDocument();
        
        element.setAttribute("id", feature.getId());
        element.setAttribute("offset", Long.toString(feature.getOffset()));
        
        element.appendChild(saveExtendedUri(
                feature.getIconUri(), document.createElement("icon")));
        element.appendChild(saveLocalizedString(
                feature.getDisplayNames(), document.createElement("display-name")));
        element.appendChild(saveLocalizedString(
                feature.getDescriptions(), document.createElement("description")));
        
        return element;
    }
    
    public static List<Feature> parseFeaturesList(final Element element) throws ParseException {
        final List<Feature> features = new LinkedList<Feature>();
        
        for (Element featureElement: XMLUtils.getChildren(element)) {
            features.add(XMLUtils.parseFeature(featureElement));
        }
        
        return features;
    }
    
    public static Element saveFeaturesList(
            final List<Feature> features, final Element element) {
        final Document document = element.getOwnerDocument();
        
        for (Feature feature: features) {
            element.appendChild(
                    saveFeature(feature, document.createElement("feature")));
        }
        
        return element;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static String[] getChildNamesFromString(String childname, String name) {
        String[] result = new String[] {};
        if(childname!=null) {
            if (childname.equals(name)) {
                result = new String[] { childname };
            } else if (childname.startsWith("(") && childname.endsWith(")")) {
                // several childs in round brackets separated by commas
                int len = childname.length();
                String[] names = childname.substring(1,len-1).split(",");
                int index =0;
                for (String n:names) {
                    if (name.equals(n)) {
                        index ++;
                    }
                }
                result = new String [index];
                index  = 0 ;
                for (String n:names) {
                    if (name.equals(n)) {
                        result[index] = n;
                        index ++;
                    }
                }
            }
        }
        return result;
    }
    
    private static HashMap<String,String> getAttributesFromChildName(String childname) {
        HashMap<String,String> map = new HashMap<String,String> ();
        if(childname!=null) {
            int start = childname.indexOf(ATTR_BEGIN);
            int end = childname.indexOf(ATTR_END);
            if (start!=-1 && end == (childname.length()-1 )) {
                // child with specified attribute
                String sub = childname.substring(start + ATTR_BEGIN.length(), end);
                String[] attrs = sub.split(ATTRS_DELIM);
                for (String s: attrs) {
                    String[] nameValue = s.split(ATTRS_DELIM);
                    if (nameValue.length==2) {
                        if (nameValue[1].indexOf("\"")==0 && nameValue[1].lastIndexOf("\"")==(nameValue[1].length()-1)) {
                            nameValue[1] = nameValue[1].substring(1,nameValue[1].length()-1);
                        }
                        map.put(nameValue[0],nameValue[1]);
                    }
                }
            }
        }
        return map;
    }
    
    private static boolean hasAttributes(Node childNode, HashMap<String, String> attributes) {
        int size = attributes.size();
        if (size==0) {
            return true;
        } else {
            Object [] keys = attributes.keySet().toArray();
            for (int i=0;i<size;i++) {
                if (keys[i] instanceof String) {
                    if (!getAttribute(childNode,(String)keys[i]).equals(attributes.get(keys[i]))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static void processChild(List<Node> result, Node childNode, String childnameString) {
        String name =  childNode.getNodeName();
        String[] names = getChildNamesFromString(childnameString,name);
        HashMap<String,String> attributes = getAttributesFromChildName(childnameString);
        for (String n:names) {
            if (name.equals(n) && hasAttributes(childNode,attributes)) {
                result.add(childNode);
            }
        }
    }
    
    private static List<Node> getChildListFromRootList(List<Node> rootlist, String childname) {
        List<Node> result = new LinkedList<Node>();
        
        for (int i = 0; i < rootlist.size(); i++) {
            Node node = rootlist.get(i);
            if (node == null) {
                continue;
            }
            
            NodeList childsList = node.getChildNodes();
            for (int j = 0; j < childsList.getLength(); j++) {
                processChild(result, childsList.item(j), childname);
            }
        }
        return result;
    }
}
