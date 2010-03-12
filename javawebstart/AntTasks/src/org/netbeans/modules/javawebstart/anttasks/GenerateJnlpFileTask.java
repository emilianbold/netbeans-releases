
package org.netbeans.modules.javawebstart.anttasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PathTokenizer;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Milan Kubec
 */
public class GenerateJnlpFileTask extends Task {
    
    private File destFile;
    private File destDir;
    private File template;
    private File properties;
    
    private static final String EXT_RESOURCE_PROPNAME_PREFIX = "jnlp.ext.resource.";
    private static String[] EXT_RESOURCE_SUFFIXES = new String[] { "href", "name", "version" };
    private static String[] EXT_RESOURCE_SUFFIXES_REQUIRED = new String[] { "href" };
    
    private static final String APPLET_PARAM_PROPNAME_PREFIX = "jnlp.applet.param.";
    private static String[] APPLET_PARAM_SUFFIXES = new String[] { "name", "value" };
    
    private static final String DEFAULT_JNLP_CODEBASE = "${jnlp.codebase}";
    private static final String DEFAULT_JNLP_FILENAME = "launch.jnlp";
    private static final String DEFAULT_APPLICATION_TITLE = "${APPLICATION.TITLE}";
    private static final String DEFAULT_APPLICATION_VENDOR = "${APPLICATION.VENDOR}";
    private static final String DEFAULT_APPLICATION_HOMEPAGE = "${APPLICATION.HOMEPAGE}";
    private static final String DEFAULT_APPLICATION_DESC = "${APPLICATION.DESC}";
    private static final String DEFAULT_APPLICATION_DESC_SHORT = "${APPLICATION.DESC.SHORT}";
    private static final String DEFAULT_JNLP_ICON = "${JNLP.ICONS}";
    private static final String DEFAULT_JNLP_OFFLINE = "${JNLP.OFFLINE.ALLOWED}";
    private static final String JNLP_UPDATE = "${JNLP.UPDATE}";
    private static final String DEFAULT_JNLP_SECURITY = "${JNLP.SECURITY}";
    private static final String DEFAULT_JNLP_RESOURCES_RUNTIME = "${JNLP.RESOURCES.RUNTIME}";
    private static final String DEFAULT_JNLP_RESOURCES_MAIN_JAR = "${JNLP.RESOURCES.MAIN.JAR}";
    private static final String DEFAULT_JNLP_RESOURCES_JARS = "${JNLP.RESOURCES.JARS}";
    private static final String DEFAULT_JNLP_RESOURCES_EXTENSIONS = "${JNLP.RESOURCES.EXTENSIONS}";
    private static final String DEFAULT_JNLP_MAIN_CLASS = "${jnlp.main.class}";
    private static final String DEFAULT_JNLP_APPLICATION_ARGS = "${JNLP.APPLICATION.ARGS}";
    private static final String DEFAULT_JNLP_APPLET_PARAMS = "${JNLP.APPLET.PARAMS}";
    private static final String DEFAULT_JNLP_APPLET_WIDTH = "${jnlp.applet.width}";
    private static final String DEFAULT_JNLP_APPLET_HEIGHT = "${jnlp.applet.height}";
    
    private static final String DESC_APPLICATION = "application-desc";
    private static final String DESC_APPLET = "applet-desc";
    private static final String DESC_COMPONENT = "component-desc";
    private static final String DESC_INSTALLER = "installer-desc";
    
    public void setDestfile(File file) {
        this.destFile = file;
    }
    
    public void setDestDir(File dir) {
        this.destDir = dir;
    }
    
    public void setTemplate(File file) {
        this.template = file;
    }
    
    // XXX ??? properties that will override those 
    // available via getProject().getProperty()
    public void setProperties(File file) {
        this.properties = file;
    }
    
    private Document loadTemplate(File tempFile) throws IOException {
        Document docDom = null;
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            docDom = docBuilder.parse(tempFile);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GenerateJnlpFileTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(GenerateJnlpFileTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return docDom;
    }
    
    @Override
    public void execute() throws BuildException {
        
        checkParameters();
        
        Document docDom = null;
        if (template != null) {
            try {
                docDom = loadTemplate(template);
            } catch (IOException ex) {
                throw new BuildException(ex, getLocation());
            }
        }
        
        if (docDom == null) {
            throw new BuildException("Template file is either missing or broken XML document, cannot generate JNLP file.", getLocation());
        }
        
        // LoadProperties ??
        processDocument(docDom);
        
        Transformer tr;
        try {
            tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD,"xml");
            tr.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
            tr.transform(new DOMSource(docDom), new StreamResult(new FileOutputStream(destFile)));
        } catch (TransformerConfigurationException ex) {
            throw new BuildException(ex, getLocation());
        } catch (TransformerException ex) {
            throw new BuildException(ex, getLocation());
        } catch (FileNotFoundException ex) {
            throw new BuildException(ex, getLocation());
        }
        
    }
    
    private void checkParameters() {
        if (destFile == null) {
            throw new BuildException("Destination file is not set, jnlp file cannot be created.");
        }
        if (destDir == null) {
            throw new BuildException("Destination directory is not set, jnlp file cannot be created.");
        }
        if (template == null) {
            throw new BuildException("Template file is not set, jnlp file cannot be created.");
        }
    }
    
    private void processDocument(Document docDom) {
        processJnlpElem(docDom);
    }
    
    private void processJnlpElem(Document docDom) {
        
        Node jnlpElem = docDom.getElementsByTagName("jnlp").item(0);
        assert jnlpElem != null;
        
        String specAttr = ((Element) jnlpElem).getAttribute("spec");
        String specProp = getProject().getProperty("jnlp.spec"); // property in project.properties
        log("jnlp.spec = " + specProp, Project.MSG_VERBOSE);
        if (specProp!= null && !specAttr.equals(specProp)) {
            ((Element) jnlpElem).setAttribute("spec", specProp);
        }
        
        String codebaseAttr = ((Element) jnlpElem).getAttribute("codebase");
        String codebaseTypeProp = getProject().getProperty("jnlp.codebase.type"); // property in project.properties
        String codebaseProp = null;
        if (codebaseTypeProp.equals("local")) {
            codebaseProp = getProject().getProperty("jnlp.local.codebase.url");
        } else if (codebaseTypeProp.equals("web")) {
            codebaseProp = getProject().getProperty("jnlp.codebase.url"); // property in project.properties
        } else if (codebaseTypeProp.equals("user")) {
            codebaseProp = getProject().getProperty("jnlp.codebase.user"); // property in project.properties
        }
        log("jnlp.codebase.url = " + codebaseProp, Project.MSG_VERBOSE);
        if (codebaseAttr.equals(DEFAULT_JNLP_CODEBASE)) {   // default value => replace
            if (codebaseTypeProp.equals("no.codebase")) {   //NOI18N
                ((Element)jnlpElem).removeAttribute("codebase");    //NOI18N
            } else if (codebaseProp != null) {
                ((Element) jnlpElem).setAttribute("codebase", codebaseProp);
            }
        }
        
        String hrefAttr = ((Element) jnlpElem).getAttribute("href");
        String jnlpFileNameProp = getProject().getProperty("jnlp.file.name"); // property in project.properties
        log("jnlp.file.name = " + jnlpFileNameProp, Project.MSG_VERBOSE);
        if (jnlpFileNameProp != null && (hrefAttr.equals(DEFAULT_JNLP_FILENAME))) { // default value => replace
            ((Element) jnlpElem).setAttribute("href", jnlpFileNameProp);
        }
        
        processInformationElem(docDom);
        processBackgroundElem(docDom, jnlpElem);
        processSecurityElem(docDom, jnlpElem);
        processResourcesElem(docDom);
        processDescriptorElem(docDom);
        
    }

    private void processInformationElem(Document docDom) {
        
        NodeList nodeList = docDom.getElementsByTagName("information");
        int listLen = nodeList.getLength();
        for (int j = 0; j < listLen; j++) {
            Node informationElem = nodeList.item(j);
            assert informationElem != null;
            
            NodeList childNodes = informationElem.getChildNodes();
            int len = childNodes.getLength();
            for (int i = 0; i < len; i++) {
                Node node = childNodes.item(i);
                if (node != null) { // node might be null (don't know why)
                    String elemName = node.getNodeName();
                    String elemText = node.getTextContent();
                    switch (node.getNodeType()) {
                        case Node.ELEMENT_NODE:
                            if (elemName.equals("title")) {
                                String titleProp = getProperty("application.title", "Application Title"); // property in project.properties
                                log("application.title = " + titleProp, Project.MSG_VERBOSE); // NOI18N
                                if (elemText.equals(DEFAULT_APPLICATION_TITLE)) {
                                    node.setTextContent(titleProp);
                                }
                            } else if (elemName.equals("vendor")) {
                                String vendorProp = getProperty("application.vendor", "Application Vendor"); // property in project.properties
                                log("application.vendor = " + vendorProp, Project.MSG_VERBOSE); // NOI18N
                                if (elemText.equals(DEFAULT_APPLICATION_VENDOR)) {
                                    node.setTextContent(vendorProp);
                                }
                            } else if (elemName.equals("homepage")) {
                                // process attribute 'href'
                                String hrefAttr = ((Element) node).getAttribute("href");
                                String hrefProp = getProperty("application.homepage", null); // property in project.properties
                                log("application.homepage = " + hrefProp, Project.MSG_VERBOSE); // NOI18N
                                if (hrefAttr.equals(DEFAULT_APPLICATION_HOMEPAGE)) {
                                    if (hrefProp != null) {
                                        ((Element) node).setAttribute("href", hrefProp);
                                    } else {
                                        ((Element) node).setAttribute("href", "");
                                    }
                                }
                            } else if (elemName.equals("description")) {
                                // title will be used as default if no desc or desc == ""
                                String titleProp = getProperty("application.title", null); // property in project.properties
                                // two possible texts: description and short description
                                String descProp = getProperty("application.desc", null); // property in project.properties
                                String descShortProp = getProperty("application.desc.short", null); // property in project.properties
                                String descPropVal = descProp != null && !descProp.equals("") ? descProp : titleProp;
                                String descShortPropVal = descShortProp != null && !descShortProp.equals("") ? descShortProp : titleProp;
                                if (elemText.equals(DEFAULT_APPLICATION_DESC)) {
                                    node.setTextContent(descPropVal);
                                } else if (elemText.equals(DEFAULT_APPLICATION_DESC_SHORT)) {
                                    node.setTextContent(descShortPropVal);
                                }
                            }
                            break;
                        case Node.COMMENT_NODE:
                            String nodeValue = node.getNodeValue();
                            if (nodeValue.equals(DEFAULT_JNLP_ICON)) {
                                informationElem.removeChild(node);
                                String splashProp = getProperty("application.splash", null); // property in project.properties
                                if (splashProp != null && fileExists(splashProp)) {
                                    copyFile(new File(splashProp), destDir);
                                    String fileName = stripFilename(splashProp);
                                    informationElem.appendChild(createIconElement(docDom, fileName, "splash"));
                                }
                                String iconProp = getProperty("jnlp.icon", null); // property in project.properties
                                if (iconProp != null && fileExists(iconProp)) {
                                    copyFile(new File(iconProp), destDir);
                                    String fileName = stripFilename(iconProp);
                                    informationElem.appendChild(createIconElement(docDom, fileName, "default"));
                                }
                            } else if (nodeValue.equals(DEFAULT_JNLP_OFFLINE)) {
                                //Has to be here to keep compatibility with NB 6.8
                                informationElem.removeChild(node);
                                String offlineProp = getProperty("jnlp.offline-allowed", null); // property in project.properties
                                if (offlineProp.equalsIgnoreCase("true")) {
                                    informationElem.appendChild(docDom.createElement("offline-allowed"));
                                }
                            }
                            break;
                        default:
                    }
                    
                }
                
            }
        }   
    }

    private void processBackgroundElem(final Document docDom, final Node parent) {
        assert docDom != null;
        assert parent != null;
        NodeList childNodes = parent.getChildNodes();
        int len = childNodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = childNodes.item(i);
            if (node != null && node.getNodeType() == Node.COMMENT_NODE) { // node might be null (don't know why)
                if (node.getNodeValue().equals(JNLP_UPDATE)) {
                    String offlineProp = getProperty("jnlp.offline-allowed", null); // property in project.properties
                    final Element updateElm = docDom.createElement("update");
                    final String updateVal = offlineProp.equalsIgnoreCase("true") ? //NOI18N
                        "background" :  //NOI18N
                        "always";       //NOI18N
                    updateElm.setAttribute("check", updateVal); //NOI18N
                    parent.replaceChild(updateElm, node);
                }
            }
        }
    }

    private Element createIconElement(Document doc, String href, String kind) {
        Element iconElem = doc.createElement("icon");
        iconElem.setAttribute("href", href);
        iconElem.setAttribute("kind", kind);
        return iconElem;
    }
    
    private boolean fileExists(String path) {
        assert path != null;
        return new File(path).exists();
    }
    
    private String getProperty(String propName, String defaultVal) {
        String propVal = getProject().getProperty(propName);
        if (propVal == null) {
            log("Property " + propName + " is not defined, using default value: " + defaultVal, Project.MSG_VERBOSE);
            return defaultVal;
        }
        return propVal.trim();
    }
    
    private void copyFile(File src, File dest) {
        Copy copyTask = (Copy) getProject().createTask("copy");
        copyTask.setFile(src);
        copyTask.setTodir(dest);
        copyTask.setFailOnError(false);
        copyTask.init();
        copyTask.setLocation(getLocation());
        copyTask.execute();
    }
    
    private void processSecurityElem(Document docDom, Node parent) {
        NodeList childNodes = parent.getChildNodes();
        int len = childNodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = childNodes.item(i);
            if (node != null && node.getNodeType() == Node.COMMENT_NODE) { // node might be null (don't know why)
                if (node.getNodeValue().equals(DEFAULT_JNLP_SECURITY)) {
                    String securityProp = getProperty("jnlp.signed", null); // property in project.properties
                    if (securityProp != null && securityProp.equalsIgnoreCase("true")) {
                        parent.replaceChild(createSecurityElement(docDom), node);
                    } else {
                        parent.removeChild(node);
                    }
                }
            }
        }
    }
    
    // should be extended to support all security types
    private Element createSecurityElement(Document doc) {
        Element secElem = doc.createElement("security");
        Element allPermElem = doc.createElement("all-permissions");
        secElem.appendChild(allPermElem);
        return secElem;
    }
    
    private void processResourcesElem(Document docDom) {
        NodeList nodeList = docDom.getElementsByTagName("resources"); // NOI18N
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
            Node resourceElem = nodeList.item(i);
            NodeList childNodes = resourceElem.getChildNodes();
            int lenChild = childNodes.getLength();
            for (int j = 0; j < lenChild; j++) {
                Node node = childNodes.item(j);
                if (node != null && node.getNodeType() == Node.COMMENT_NODE) { // node might be null (don't know why)
                    String nodeValue = node.getNodeValue();
                    if (nodeValue.equals(DEFAULT_JNLP_RESOURCES_RUNTIME)) {
                        resourceElem.replaceChild(createJ2seElement(docDom), node);
                    } else if (nodeValue.equals(DEFAULT_JNLP_RESOURCES_MAIN_JAR)) {
                        String fileName = stripFilename(getProject().getProperty("dist.jar")); // NOI18N
                        resourceElem.replaceChild(createJarElement(docDom, fileName, true, true), node);
                    } else if (nodeValue.equals(DEFAULT_JNLP_RESOURCES_JARS)) {
                        resourceElem.removeChild(node);
                        String cpProp = getProperty("run.classpath", null); // property in project.properties
                        log("run.classpath = " + cpProp, Project.MSG_VERBOSE);
                        PathTokenizer ptok = new PathTokenizer(cpProp);
                        while (ptok.hasMoreTokens()) {
                            String fileName = stripFilename(ptok.nextToken());
                            if (fileName.endsWith("jar") && !fileName.equals("javaws.jar")) {
                                // lib/ should be probably taken from some properties file ? 
                                resourceElem.appendChild(createJarElement(docDom, "lib/" + fileName, false, false));
                            }
                        }
                    } else if (nodeValue.equals(DEFAULT_JNLP_RESOURCES_EXTENSIONS)) {
                        resourceElem.removeChild(node);
                        List<Map<String,String>> extResProps = readMultiProperties(EXT_RESOURCE_PROPNAME_PREFIX, EXT_RESOURCE_SUFFIXES);
                        for (Map<String,String> map : extResProps) {
                            List<String> requiredKeys = Arrays.asList(EXT_RESOURCE_SUFFIXES_REQUIRED);
                            Set<String> keys = map.keySet();
                            if (keys.containsAll(requiredKeys)) {
                                resourceElem.appendChild(createPropElement(docDom, "extension", map));
                            }
                        }
                    }
                }
            }
        }
    }
    
    private Element createJ2seElement(Document doc) {
        // element should be <java ...> but we want to support version JNLP 1.0+
        Element j2seElem = doc.createElement("j2se"); // NOI18N
        String javacTargetProp = getProperty("javac.target", null); // property in project.properties
        j2seElem.setAttribute("version", javacTargetProp + "+"); // NOI18N
        String runArgsProp = getProperty("run.jvmargs", null); // property in project.properties
        if (runArgsProp != null && !runArgsProp.equals("")) {
            j2seElem.setAttribute("java-vm-args", runArgsProp); // NOI18N
        }
        String initHeapProp = getProperty("jnlp.initial-heap-size", null); // property in project.properties
        if (initHeapProp != null && !initHeapProp.equals("")) {
            j2seElem.setAttribute("initial-heap-size", initHeapProp); // NOI18N
        }
        String maxHeapProp = getProperty("jnlp.max-heap-size", null); // property in project.properties
        if (maxHeapProp != null && !maxHeapProp.equals("")) {
            j2seElem.setAttribute("max-heap-size", maxHeapProp); // NOI18N
        }
        return j2seElem;
    }
    
    private Element createJarElement(Document doc, String href, boolean main, boolean eager) {
        assert href != null;
        Element jarElem = doc.createElement("jar"); // NOI18N
        jarElem.setAttribute("href", href); // NOI18N
        if (main) {
            jarElem.setAttribute("main", "true"); // NOI18N
        }
        if (!eager) {
            jarElem.setAttribute("download", "lazy"); // NOI18N
        }
        return jarElem;
    }
    
    private void processDescriptorElem(Document docDom) {
        
        String elemNames[] = new String[] { DESC_APPLICATION, DESC_APPLET, DESC_COMPONENT, DESC_INSTALLER };
        String descName = null;
        Element descElem = null;
        for (String elemName : elemNames) {
            Node node = docDom.getElementsByTagName(elemName).item(0);
            if (node != null) {
                descName = elemName;
                descElem = (Element) node;
                break;
            }
        }
        if (DESC_APPLICATION.equals(descName)) { // APPLICATION
            if (DEFAULT_JNLP_MAIN_CLASS.equals(descElem.getAttribute("main-class"))) {
                descElem.setAttribute("main-class", getProject().getProperty("main.class")); // NOI18N
            }
            // process subelements - arguments
            // only if there is ${JNLP.APPLICATION.ARGS} comment element
            NodeList childNodes = descElem.getChildNodes();
            int len = childNodes.getLength();
            for (int i = 0; i < len; i++) {
                Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getNodeType() == Node.COMMENT_NODE && 
                        childNode.getNodeValue().equals(DEFAULT_JNLP_APPLICATION_ARGS)) {
                    descElem.removeChild(childNode);
                    // create new elements
                    String appArgsProp = getProject().getProperty("application.args");
                    if (appArgsProp != null) {
                        StringTokenizer stok = new StringTokenizer(appArgsProp);
                        while (stok.hasMoreTokens()) {
                            String arg = stok.nextToken();
                            Element argElem = docDom.createElement("argument"); // NOI18N
                            argElem.setTextContent(arg);
                            descElem.appendChild(argElem);
                        }
                    }
                }
            }
        } else if (DESC_APPLET.equals(descName)) { // APPLET
            if (DEFAULT_JNLP_MAIN_CLASS.equals(descElem.getAttribute("main-class"))) { // NOI18N
                descElem.setAttribute("main-class", getProject().getProperty("jnlp.applet.class")); // NOI18N
            }
            if (DEFAULT_APPLICATION_TITLE.equals(descElem.getAttribute("name"))) { // NOI18N
                descElem.setAttribute("name", getProperty("application.title", "Application Title")); // NOI18N
            }
            if (DEFAULT_JNLP_APPLET_WIDTH.equals(descElem.getAttribute("width"))) { // NOI18N
                descElem.setAttribute("width", getProperty("jnlp.applet.width", "300")); // NOI18N
            }
            if (DEFAULT_JNLP_APPLET_HEIGHT.equals(descElem.getAttribute("height"))) { // NOI18N
                descElem.setAttribute("height", getProperty("jnlp.applet.height", "300")); // NOI18N
            }
            // process subelements - params
            // only if there is ${JNLP.APPLET.PARAMS} comment element
            NodeList childNodes = descElem.getChildNodes();
            int len = childNodes.getLength();
            for (int i = 0; i < len; i++) {
                Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getNodeType() == Node.COMMENT_NODE && 
                        childNode.getNodeValue().equals(DEFAULT_JNLP_APPLET_PARAMS)) {
                    descElem.removeChild(childNode);
                    // create new elements
                    List<Map<String,String>> appletParamProps = readMultiProperties(APPLET_PARAM_PROPNAME_PREFIX, APPLET_PARAM_SUFFIXES);
                    for (Map<String,String> map : appletParamProps) {
                        if (map.size() == APPLET_PARAM_SUFFIXES.length) {
                            descElem.appendChild(createPropElement(docDom, "param", map)); // NOI18N
                        }
                    }
                }
            }
        } else if (DESC_COMPONENT.equals(descName)) {
            // do nothing - there is nothing to change
        } else if (DESC_INSTALLER.equals(descName)) {
            // XXX TBD
        }
    }
    
    private Element createPropElement(Document doc, String elemName, Map<String,String> props) {
        Element propElem = doc.createElement(elemName);
        for (String propName : props.keySet()) {
            String propValue = props.get(propName);
            propElem.setAttribute(propName, propValue);
        }
        return propElem;
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * Loads properties in form of ${propPrefix}.{0..n}.${propSuffixes[i]}
     * 
     * @param propPrefix prefix of the property
     * @param propSuffixes array of all suffixes to load for each prefix
     * @return list of maps of propSuffix to value of the property
     */
    private List<Map<String,String>> readMultiProperties(String propPrefix, String[] propSuffixes) {
        
        ArrayList<Map<String,String>> listToReturn = new ArrayList<Map<String,String>>();
        int index = 0;
        while (true) {
            HashMap<String,String> map = new HashMap<String,String>();
            int numProps = 0;
            for (String propSuffix : propSuffixes) {
                String propValue = getProject().getProperty(propPrefix + index + "." + propSuffix);
                if (propValue != null) {
                    map.put(propSuffix, propValue);
                    numProps++;
                }
            }
            if (numProps == 0) {
                break;
            }
            listToReturn.add(map);
            index++;
        }
        return listToReturn;
        
    }

    private String stripFilename(String path) {
        int sepIndex = path.lastIndexOf('/') == -1 ? path.lastIndexOf('\\') : path.lastIndexOf('/');
        return  path.substring(sepIndex + 1);
    }
    
}
