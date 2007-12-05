/*
 * To change this template, choose Tools | Templates | Licenses | Default License
 * and open the template in the editor.
 */
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.webapp.UIComponentTagBase;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author winstonp
 */
public class JsfTagSupport {

    // Static map of Taglib URI and taglib location in a jar
    // TODO: dynamically fecth this map using JSP parser API - Winston

    private static String facesConfigResourceLocation = "META-INF/faces-config.xml";
    private static Map<String, String> statictTaglibLocationMap = new HashMap<String, String>();
    static {
        // TODO - Winston
            // For now use the predefined TLD location. This is not a 
            // good solution, because we do not know the tld location
            // in case of third party components. The best solution is 
            // to use org.netbeans.modules.web.jspparser modules
            // Use the API JspParserAPI.getTaglibMap() 
        statictTaglibLocationMap.put("http://www.sun.com/webui/webuijsf", "META-INF/webui-jsf.tld");
        statictTaglibLocationMap.put("http://java.sun.com/jsf/html", "META-INF/html_basic.tld");
        statictTaglibLocationMap.put("http://java.sun.com/jsf/core", "META-INF/jsf_core.tld");
        statictTaglibLocationMap.put("http://www.sun.com/web/ui", "META-INF/webui.tld");
    }
    // Cache of Taglib URI and JSF tag support 
    private static Map<String, JsfTagSupport> cachedTagLibraryInfoMap = new HashMap<String, JsfTagSupport>();
    private static Object lock = new Object();
    
    Map<String, TagInfo> tagInfoMap = new HashMap<String, TagInfo>();
    Map<String, ComponentInfo> componentInfoMap = new HashMap<String, ComponentInfo>();

    // Factory Method to get TagLibrarySupport for a particular taglibUri

    public static JsfTagSupport getInstance(ClassLoader classLoader, String taglibUri) {
        synchronized (lock) {
            if (!cachedTagLibraryInfoMap.containsKey(taglibUri)) {
                cachedTagLibraryInfoMap.put(taglibUri, new JsfTagSupport(classLoader, taglibUri));
            }
        }
        return cachedTagLibraryInfoMap.get(taglibUri);
    }

    public Object getTagHandler(ClassLoader classLoader, String tagName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        TagInfo tagInfo = tagInfoMap.get(tagName);
        return classLoader.loadClass(tagInfo.getTagClass()).newInstance();
    }

    public Object getComponent(ClassLoader classLoader, String tagName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIComponentTagBase componentTag = (UIComponentTagBase) getTagHandler(classLoader, tagName);
        String componentType = componentTag.getComponentType();
        ComponentInfo componentInfo = componentInfoMap.get(componentType);
        String componentClass = componentInfo.getComponentClass();
        return classLoader.loadClass(componentClass).newInstance();
    }
    
    public String getComponentClass(ClassLoader classLoader, String tagName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIComponentTagBase componentTag = (UIComponentTagBase) getTagHandler(classLoader, tagName);
        String componentType = componentTag.getComponentType();
        ComponentInfo componentInfo = componentInfoMap.get(componentType);
        return componentInfo.getComponentClass();
    }

    private JsfTagSupport(ClassLoader classLoader, String taglibUri) {
        try {
            String taglibResourceLocation = statictTaglibLocationMap.get(taglibUri);
            URL taglibUrl = classLoader.getResource(taglibResourceLocation);
            Enumeration<URL> facesConfigUrlList = classLoader.getResources(facesConfigResourceLocation);
            URL facesConfigUrl = null;
            while (facesConfigUrlList.hasMoreElements()) {
                URL currUrl = facesConfigUrlList.nextElement();
                if (taglibUrl.getPath().regionMatches(0, currUrl.getPath(), 0, currUrl.getPath().indexOf("META-INF"))) {
                    facesConfigUrl = currUrl;
                    break;
                }
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            Document facesConfigdocument = factory.newDocumentBuilder().parse(facesConfigUrl.openStream());
            parseFacesConfig(facesConfigdocument);

            // Create the builder and parse XML data from input stream
            Document tagLibdocument = factory.newDocumentBuilder().parse(taglibUrl.openStream());
            parseTagLibary(tagLibdocument);
        } catch (SAXException ex) {
            Logger.getLogger(JsfTagSupport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JsfTagSupport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(JsfTagSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Map<String, ComponentInfo> parseFacesConfig(Document facesConfigdocument) {
        NodeList componentNodes = facesConfigdocument.getElementsByTagName("component");
        for (int i = 0; i < componentNodes.getLength(); i++) {
            ComponentInfo componentInfo = new ComponentInfo(componentNodes.item(i));
            componentInfoMap.put(componentInfo.getComponentType(), componentInfo);
        }
        return componentInfoMap;
    }

    private void parseTagLibary(Document tagLibdocument) {
        NodeList tagNodes = tagLibdocument.getElementsByTagName("tag");
        for (int i = 0; i < tagNodes.getLength(); i++) {
            TagInfo tagInfo = new TagInfo(tagNodes.item(i));
            tagInfoMap.put(tagInfo.getName(), tagInfo);
        }
    }

    private class TagInfo {

        private String tagName;
        private String tagClass;

        TagInfo(Node tagNode) {
            Node nameNode = ((Element) tagNode).getElementsByTagName("name").item(0);
            tagName = nameNode.getTextContent();
            Node tagClassNode = ((Element) tagNode).getElementsByTagName("tag-class").item(0);
            tagClass = tagClassNode.getTextContent();
        }

        public String getName() {
            return tagName;
        }

        public String getTagClass() {
            return tagClass;
        }
    }

    private class ComponentInfo {

        private String componentType;
        private String componentClass;

        ComponentInfo(Node componentNode) {
            Node componentTypeNode = ((Element) componentNode).getElementsByTagName("component-type").item(0);
            componentType = componentTypeNode.getTextContent();
            Node componentClassNode = ((Element) componentNode).getElementsByTagName("component-class").item(0);
            componentClass = componentClassNode.getTextContent();
        }

        public String getComponentType() {
            return componentType;
        }

        public String getComponentClass() {
            return componentClass;
        }
    }
}
