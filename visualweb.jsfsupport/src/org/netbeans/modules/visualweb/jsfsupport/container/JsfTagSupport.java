/*
 * To change this template, choose Tools | Templates | Licenses | Default License
 * and open the template in the editor.
 */
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.faces.webapp.UIComponentTagBase;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A Support class to find the JSF component corresponding to the JSP tag and TagLib URI
 * @author Winston Prakash
 */
public class JsfTagSupport {

    private Map<String, TagInfo> tagInfoMap = new HashMap<String, TagInfo>();
    private Map<String, ComponentInfo> componentInfoMap = new HashMap<String, ComponentInfo>();
    // Static map of Taglib URI and taglib and faces-config locations in a jar
    private static Map<String, TagLibFacesConfigInfo> statictTaglibFacesConfigLocationMap = new HashMap<String, TagLibFacesConfigInfo>();
    // Cache of Taglib URI and JSF tag support 
    private static Map<String, JsfTagSupport> cachedTagLibraryInfoMap = new HashMap<String, JsfTagSupport>();
    private static Object lock = new Object();

    /**
     * This method should be called only once when the designtime JSF container is initialzed,
     * passing the project classloader. The classpath (jars) of the classloader is scanned for
     * TLD files and faces config files. Their locations are cached for later use 
     * @param classLoader
     */
    public static synchronized void initialize(ClassLoader classLoader) {
        try {
            Enumeration<URL> urls = classLoader.getResources("META-INF/faces-config.xml");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (!url.getPath().contains("jsfcl.jar")) {
                    addTaglibFacesConfigMapEntry(url);
                }
            }

            // Bug Fix 124610 - Unfortunately the JSF RI component informations are not kept
            // in the standard location (META-INF/faces-config.xml)
            URL facesConfigUrl =  classLoader.getResource("com/sun/faces/jsf-ri-runtime.xml");
            URL tagLibUrl = new URL(facesConfigUrl.toString().split("!")[0] + "!/META-INF/html_basic.tld");
            String taglibUri = "http://java.sun.com/jsf/html";
            TagLibFacesConfigInfo tagLibFacesConfigInfo = new TagLibFacesConfigInfo(taglibUri);
            tagLibFacesConfigInfo.addTagLibUrl(tagLibUrl);
            tagLibFacesConfigInfo.addFacesConfigUrl(facesConfigUrl);
            statictTaglibFacesConfigLocationMap.put(taglibUri, tagLibFacesConfigInfo);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void addTaglibFacesConfigMapEntry(URL facesConfigUrl) throws IOException, ParserConfigurationException, SAXException {
        FileObject facesConfigFileObject = URLMapper.findFileObject(facesConfigUrl);
        String zipFilePath = FileUtil.toFile(FileUtil.getArchiveFile(facesConfigFileObject)).getAbsolutePath();
        ZipFile in = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> entries = in.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".tld")) {
                URL tagLibUrl = new URL(facesConfigUrl.toString().split("!")[0] + "!/" + entry.getName());
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                documentBuilder.setEntityResolver(new EmptyEntityResolver());
                Document tagLibdocument = documentBuilder.parse(tagLibUrl.openStream());
                NodeList tagNodes = tagLibdocument.getElementsByTagName("uri");
                // Scan the TLD file to find the taglib URI
                String taglibUri = tagNodes.item(0).getTextContent().trim();

                TagLibFacesConfigInfo tagLibFacesConfigInfo = new TagLibFacesConfigInfo(taglibUri);
                tagLibFacesConfigInfo.addTagLibUrl(tagLibUrl);
                tagLibFacesConfigInfo.addFacesConfigUrl(facesConfigUrl);

                statictTaglibFacesConfigLocationMap.put(taglibUri, tagLibFacesConfigInfo);
            }
        }
    }

    /** 
     *Factory Method to get TagLibrarySupport for a particular taglibUri and ClassLoader
     */
    public static JsfTagSupport getInstance(
            String taglibUri) throws JsfTagSupportException, SAXException, ParserConfigurationException, IOException {
        synchronized (lock) {
            if (!cachedTagLibraryInfoMap.containsKey(taglibUri)) {
                cachedTagLibraryInfoMap.put(taglibUri, new JsfTagSupport(taglibUri));
            }

        }
        return cachedTagLibraryInfoMap.get(taglibUri);
    }

    public Object getTagHandler(
            ClassLoader classLoader, String tagName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        TagInfo tagInfo = tagInfoMap.get(tagName);
        return classLoader.loadClass(tagInfo.getTagClass()).newInstance();
    }

    public Object getComponent(
            ClassLoader classLoader, String tagName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIComponentTagBase componentTag = (UIComponentTagBase) getTagHandler(classLoader, tagName);
        String componentType = componentTag.getComponentType();
        ComponentInfo componentInfo = componentInfoMap.get(componentType);
        String componentClass = componentInfo.getComponentClass();
        return classLoader.loadClass(componentClass).newInstance();
    }

    public String getComponentClass(
            ClassLoader classLoader, String tagName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIComponentTagBase componentTag = (UIComponentTagBase) getTagHandler(classLoader, tagName);
        String componentType = componentTag.getComponentType();
        ComponentInfo componentInfo = componentInfoMap.get(componentType);
        return componentInfo.getComponentClass();
    }

    private JsfTagSupport(String taglibUri) throws JsfTagSupportException, SAXException, ParserConfigurationException, IOException {
        TagLibFacesConfigInfo tagLibFacesConfigInfo = statictTaglibFacesConfigLocationMap.get(taglibUri);

        if (tagLibFacesConfigInfo != null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            List<URL> tagLibUrlList = tagLibFacesConfigInfo.getTagLibUrls();
            for (URL tagLibUrl : tagLibUrlList) {
                // Create the builder and parse XML data from input stream
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                documentBuilder.setEntityResolver(new EmptyEntityResolver());
                Document tagLibdocument = documentBuilder.parse(tagLibUrl.openStream());
                parseTagLibary(tagLibdocument);
            }

            List<URL> facesConfigUrlList = tagLibFacesConfigInfo.getFacesConfigUrls();
            for (URL facesConfigUrl : facesConfigUrlList) {
                // Create the builder and parse XML data from input stream
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                documentBuilder.setEntityResolver(new EmptyEntityResolver());
                Document facesConfigdocument = documentBuilder.parse(facesConfigUrl.openStream());
                parseFacesConfig(facesConfigdocument);
            }
        } else {
            throw new JsfTagSupportException(NbBundle.getMessage(JsfTagSupport.class, "UNRECOGNIZED_TAGLIB") + taglibUri);
        }
    }

    private Map<String, ComponentInfo> parseFacesConfig(Document facesConfigdocument) {
        NodeList componentNodes = facesConfigdocument.getElementsByTagName("component");
        for (int i = 0; i <
                componentNodes.getLength(); i++) {
            ComponentInfo componentInfo = new ComponentInfo(componentNodes.item(i));
            componentInfoMap.put(componentInfo.getComponentType(), componentInfo);
        }

        return componentInfoMap;
    }

    private void parseTagLibary(Document tagLibdocument) {
        NodeList tagNodes = tagLibdocument.getElementsByTagName("tag");
        for (int i = 0; i <
                tagNodes.getLength(); i++) {
            TagInfo tagInfo = new TagInfo(tagNodes.item(i));
            tagInfoMap.put(tagInfo.getName(), tagInfo);
        }
    }

    private static class TagLibFacesConfigInfo {

        private String taglibUri;
        private List<URL> tagLibUrls = new ArrayList<URL>(3);
        private List<URL> facesConfigUrls = new ArrayList<URL>(3);

        TagLibFacesConfigInfo(String taglibUri) {
            this.taglibUri = taglibUri;
        }

        public void addTagLibUrl(URL taglib) {
            tagLibUrls.add(taglib);
        }

        public List<URL> getTagLibUrls() {
            return tagLibUrls;
        }

        public void addFacesConfigUrl(URL facesConfig) {
            facesConfigUrls.add(facesConfig);
        }

        public List<URL> getFacesConfigUrls() {
            return facesConfigUrls;
        }
    }

    private static class TagInfo {

        private String tagName;
        private String tagClass;

        TagInfo(Node tagNode) {
            Node nameNode = ((Element) tagNode).getElementsByTagName("name").item(0);
            tagName = nameNode.getTextContent().trim();
            Node tagClassNode = ((Element) tagNode).getElementsByTagName("tag-class").item(0);
            tagClass = tagClassNode.getTextContent().trim();
        }

        public String getName() {
            return tagName;
        }

        public String getTagClass() {
            return tagClass;
        }
    }

    private static class ComponentInfo {

        private String componentType;
        private String componentClass;

        ComponentInfo(Node componentNode) {
            Node componentTypeNode = ((Element) componentNode).getElementsByTagName("component-type").item(0);
            componentType = componentTypeNode.getTextContent().trim();
            Node componentClassNode = ((Element) componentNode).getElementsByTagName("component-class").item(0);
            componentClass = componentClassNode.getTextContent().trim();
        }

        public String getComponentType() {
            return componentType;
        }

        public String getComponentClass() {
            return componentClass;
        }
    }
    
    private static class EmptyEntityResolver implements EntityResolver {
         public InputSource resolveEntity(String pubid, String sysid) {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
    }
}
