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
package org.netbeans.modules.compapp.projects.jbi.anttasks;

import org.netbeans.modules.compapp.projects.jbi.anttasks.utils.BuildHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Endpoint;
import org.netbeans.modules.compapp.projects.jbi.util.MyFileUtil;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import static org.netbeans.modules.compapp.projects.jbi.CasaConstants.*;
import static org.netbeans.modules.compapp.projects.jbi.JbiConstants.*;
import static org.netbeans.modules.compapp.projects.jbi.api.JbiEndpointExtensionConstants.*;

/**
 * Builder of the CompApp project's CASA document.
 *
 * @author jqian
 */
public class CasaBuilder {

    // WSDL Domain
    public static final String WSDL_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_PORT_ELEM_NAME = "port";
    public static final String WSDL_SERVICE_ELEM_NAME = "service";
    public static final String WSDL_NAME_ATTR_NAME = "name";
    
    // XLink Domain
    public static final String XLINK_NAMESPACE_URI = "http://www.w3.org/2000/xlink";
    public static final String XLINK_NAMESPACE_PREFIX = "xlink";
    public static final String XLINK_HREF_ATTR_NAME = "href";
    public static final String XLINK_TYPE_ATTR_NAME = "type";
    public static final String WSDL_ENDPOINTS_REGION_NAME = "WSDL Endpoints";
    public static final String JBI_MODULES_REGION_NAME = "JBI Modules";
    public static final String EXTERNAL_MODULES_REGION_NAME = "External Modules";
    public static final String DEFAULT_WSDL_ENDPOINTS_REGION_WIDTH = "150";
    public static final String DEFAULT_JBI_MODULES_REGION_WIDTH = "500";
    public static final String DEFAULT_EXTERNAL_MODULES_REGION_WIDTH = "200";

    // WSIT Callback Java project support
    public static final String CASA_NAMESPACE_URI = "http://java.sun.com/xml/ns/casa";
    public static final String WSIT_CALLBACK_ELEMENT = "WsitCallback";
    public static final String WSIT_CALLBACK_PROJECT = "CallbackProject";

    // mapping binding component namespace to binding component name,
    private Map<String, String> bcNS2NameMap;
    // mapping SE/BC SU name to endpoints defined in the SU's jbi.xml
    private Map<String, List<Endpoint>> su2Endpoints =
            new HashMap<String, List<Endpoint>>();
    // A Map mapping fully qualified endpoint name to endpoint ID for all 
    // the endpoints in the new casa document.
    private Map<String, String> newEndpointMap = new HashMap<String, String>();
    // index used for creating endpoint IDs in the new casa document
    private int endpointIndex = 1;
    // a list of external endpoints in the old casa document
    private List<Endpoint> externalEndpoints;
    // a list of endpoints defined in all the WSDL files in the compapp and
    // its component projects
    private List<Endpoint> newWsdlEndpoints;
    // mapping BC name to list of deleted endpoints for that BC type in the 
    // old casa document
    private Map<String, List<Endpoint>> deletedBCEndpointsMap;
    // mapping BC name to a list of unconnected endpoints in the old casa document
    private Map<String, List<Endpoint>> oldUnconnectedBCEndpointsMap;
    private String serviceUnitsDirLoc;
    private String confDirLoc;
    private String casaFileLoc;
    private Project project;
    private wsdlRepository wsdlRepository;
    private Task task;
    private Document oldCasaDocument;
    private Document newCasaDocument;

    private List<String> externalSuNames;

    /**
     *
     * @param project           the compapp project as an Ant project
     * @param wsdlRepository    WSDL repository
     * @param task              the Ant task
     * @param externalSUNames   a non-null list of names for external SU
     * @param bcNS2NameMap      a map mapping BC namespace to name
     */
    public CasaBuilder(Project project, wsdlRepository wsdlRepository, 
            Task task, List<String> externalSuNames,
            Map<String, String> bcNS2NameMap) {

        this.project = project;
        this.wsdlRepository = wsdlRepository;
        this.task = task;
        this.externalSuNames = externalSuNames;
        this.bcNS2NameMap = bcNS2NameMap;

        String projName = BuildHelper.getServiceAssemblyID(project);
        String projPath = project.getProperty("basedir") + File.separator;
        String srcDirLoc = projPath + "src" + File.separator;
        confDirLoc = srcDirLoc + "conf" + File.separator;
        serviceUnitsDirLoc = srcDirLoc + JbiProjectConstants.FOLDER_JBISERVICEUNITS + File.separator;
        casaFileLoc = confDirLoc + projName + ".casa";

        File casaFile = new File(casaFileLoc);
        if (casaFile.exists()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            try {
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                oldCasaDocument = documentBuilder.parse(casaFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    final Document getOldCasaDocument() {
        return oldCasaDocument;
    }

    /**
     * Creates a new CASA document based on sa jbi.xml and optionally 
     * an old CASA file.
     * 
     * @param jbiDocument   service assembly JBI document
     * 
     * @return the new CASA document
     */
    public Document createCasaDocument(Document jbiDocument) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        newCasaDocument = builder.newDocument();

        Element casaRoot = newCasaDocument.createElement(CASA_ELEM_NAME);
        newCasaDocument.appendChild(casaRoot);
        casaRoot.setAttribute("xmlns", CASA_NAMESPACE_URI);
        casaRoot.setAttribute("xmlns:" + XLINK_NAMESPACE_PREFIX, XLINK_NAMESPACE_URI);

        // Prepare various endpoint lists
        deletedBCEndpointsMap = getDeletedBCEndpointsMap();
        oldUnconnectedBCEndpointsMap = getUnconnectedBCEndpointsMap();

        // endpoints
        Element casaEndpoints = createEndpoints(jbiDocument);
        casaRoot.appendChild(casaEndpoints);

        // service units
        Element casaServiceUnits = createSUs(jbiDocument);
        casaRoot.appendChild(casaServiceUnits);

        // connections
        Element casaConnections = createConnections(jbiDocument);
        casaRoot.appendChild(casaConnections);

        // porttypes, bindings, services
        List<Element> casaWSDLReferences = createWSDLReferenceElements();
        for (Element casaElement : casaWSDLReferences) {
            casaRoot.appendChild(casaElement);
        }

        // regions
        Element casaRegions = createRegions();
        casaRoot.appendChild(casaRegions);

        //        preserveCasaWSDLEndpointsAndPorts();

        mergeLocations();

        // Merge endpoint extension elements from old casa
        mergeEndpointExtensions(true);
        mergeEndpointExtensions(false);

        // Merge connection extension elements from old casa
        mergeConnectionExtensions();

        File oldCasaFile = new File(casaFileLoc);
        if (!oldCasaFile.exists()) {
            XmlUtil.writeToFile(casaFileLoc, newCasaDocument);
        } else {
            File tmpNewCasaFile = File.createTempFile(project.getBaseDir().getName() /*project.getName()*/, "casa");
            tmpNewCasaFile.deleteOnExit();
            XmlUtil.writeToFile(tmpNewCasaFile.getCanonicalPath(), newCasaDocument);

            long oldChecksum = MyFileUtil.getFileChecksum(oldCasaFile);
            long newChecksum = MyFileUtil.getFileChecksum(tmpNewCasaFile);
            if (oldChecksum != newChecksum) {
                if (oldCasaFile.canWrite()) {
                    XmlUtil.writeToFile(casaFileLoc, newCasaDocument);
                } else {
                    throw new IOException("Can not update src/conf/" + oldCasaFile.getName() + " during build. "
                            + "You might need to check out this file if it is under version control. "
                            + "See http://www.netbeans.org/issues/show_bug.cgi?id=161537 for more info.");
                }
            }
        }

        return newCasaDocument;
    }

    /**
     * Gets an Endpoint object from an endpoint element in CASA DOM.
     */
    private static Endpoint getEndpointInCASA(Element casaEndpointElement) {
        String endpointName =
                casaEndpointElement.getAttribute(CASA_ENDPOINT_NAME_ATTR_NAME);
        QName serviceQName = XmlUtil.getAttributeNSName(
                casaEndpointElement, CASA_SERVICE_NAME_ATTR_NAME);
        QName interfaceQName = XmlUtil.getAttributeNSName(
                casaEndpointElement, CASA_INTERFACE_NAME_ATTR_NAME);

        String displayName = casaEndpointElement.getAttribute(JBI_ENDPOINT_EXTENSION_DISPLAY_NAME);
        String processName = casaEndpointElement.getAttribute(JBI_ENDPOINT_EXTENSION_PROCESS_NAME);
        String filePath = casaEndpointElement.getAttribute(JBI_ENDPOINT_EXTENSION_FILE_PATH);
        
        if (displayName == null && processName == null && filePath == null) {
            return new Endpoint(endpointName, serviceQName, interfaceQName);
        } else {
            EndpointWithExtension ret =
                    new EndpointWithExtension(endpointName, serviceQName, interfaceQName);
            ret.setDisplayName(displayName);
            ret.setProcessName(processName);
            ret.setFilePath(filePath);

            return ret;
        }
    }

    /**
     * Gets the ID of the given endpoint in the CASA document.
     */
    private static String getEndpointID(Document casaDocument, Endpoint endpoint) {
        NodeList endpointNodeList = casaDocument.getElementsByTagName(
                CASA_ENDPOINT_ELEM_NAME);

        for (int i = 0; i < endpointNodeList.getLength(); i++) {
            Element endpointElement = (Element) endpointNodeList.item(i);
            Endpoint casaEndpoint = getEndpointInCASA(endpointElement);
            if (endpoint.equals(casaEndpoint)) {
                return endpointElement.getAttribute(CASA_NAME_ATTR_NAME);
            }
        }

        return null;
    }

    /**
     * Creates the SE/BC service-units element in the new casa document.
     */
    private Element createSUs(Document jbiDocument)
            throws SAXException, IOException, ParserConfigurationException {

        Element casaSUs = newCasaDocument.createElement(CASA_SERVICE_UNITS_ELEM_NAME);

        // 1. "Copy" SE and BC SUs from jbi document over
        List<String> componentIDs = new ArrayList<String>();

        if (jbiDocument == null) {
          return casaSUs;
        }
        NodeList jbiSUs =
                jbiDocument.getElementsByTagName(JBI_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < jbiSUs.getLength(); i++) {
            Element jbiSU = (Element) jbiSUs.item(i);
            String componentID = getJBIServiceUnitComponentName(jbiSU);
            componentIDs.add(componentID);

            Element casaSU = bcNS2NameMap.values().contains(componentID) ?
                createBCSUFromJbiElement(jbiSU) : createSESUFromJbiElement(jbiSU);

            if (casaSU != null) {
                casaSUs.appendChild(casaSU);
            }
        }

        if (oldCasaDocument != null) {
            // 2. Merge BC SUs from old casa that contains only unconnected
            // and/or deleted ports.
            NodeList bcSUs = oldCasaDocument.getElementsByTagName(
                    CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
            for (int i = 0; i < bcSUs.getLength(); i++) {
                Element bcSU = (Element) bcSUs.item(i);
                String bcName = bcSU.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME);
                if (!componentIDs.contains(bcName)) {
                    Element casaSU = createBCSUFromCasaElement(bcSU);
                    // Only add this BC SU if it still contains at least one
                    // unconnected or deleted port.
                    Element ports = (Element) casaSU.getElementsByTagName(CASA_PORTS_ELEM_NAME).item(0);
                    if (ports.getChildNodes().getLength() > 0) {
                        casaSUs.appendChild(casaSU);
                    }
                }
            }

            // 3. Merge external SE SUs from old casa (only if the extneral SU
            // is not added into the CompApp project as an external JBI module)
            try {
                List<Element> externalSESUs = getExternalSESUs();
                for (Element oldSESU : externalSESUs) {
                    String unknown = oldSESU.getAttribute(CASA_UNKNOWN_ATTR_NAME);
                    if ("true".equalsIgnoreCase(unknown)) {
                        Node newSESU = deepCloneCasaNodeWithEndpointConversion(oldSESU);
                        casaSUs.appendChild(newSESU);
                    }
                }
            } catch (Exception e) {
                log("ERROR: Problem merging external service units from old casa: " + e +
                        ". This does not affect regular compapp build.");
            }
        }

        return casaSUs;
    }

    /**
     * Merge connection extension elements from the old CASA document.
     * 
     * @param isConsumes    is consumes endpoint or provides endpoint
     */
    private void mergeConnectionExtensions() {
        if (oldCasaDocument == null) {
            return;
        }

        NodeList oldConnections = oldCasaDocument.getElementsByTagName(
                CASA_CONNECTION_ELEM_NAME);

        for (int i = 0; i < oldConnections.getLength(); i++) {
            Element oldConnection = (Element) oldConnections.item(i);
            Element newConnection = findConnection(
                    oldCasaDocument, oldConnection, newCasaDocument);

            // Copy child extension elements over from old CASA to new CASA
            if (newConnection != null) {
                deepCloneChildren(oldConnection, newConnection);
            }
        }
    }

    private static Element findConnection(Document oldCasaDocument,
            Element oldConnection, Document newCasaDocument) {
        Element ret = null;

        String oldConsumerEndpointID = oldConnection.getAttribute(CASA_CONSUMER_ATTR_NAME);
        Endpoint consumerEndpoint = getEndpoint(oldCasaDocument, oldConsumerEndpointID);
        String newConsumerEndpointID = getEndpointID(newCasaDocument, consumerEndpoint);

        String oldProviderEndpointID = oldConnection.getAttribute(CASA_PROVIDER_ATTR_NAME);
        Endpoint providerEndpoint = getEndpoint(oldCasaDocument, oldProviderEndpointID);
        String newProviderEndpointID = getEndpointID(newCasaDocument, providerEndpoint);

        NodeList newConnections = newCasaDocument.getElementsByTagName(
                CASA_CONNECTION_ELEM_NAME);

        for (int i = 0; i < newConnections.getLength(); i++) {
            Element newConnection = (Element) newConnections.item(i);
            if (newConnection.getAttribute(CASA_CONSUMER_ATTR_NAME).
                    equals(newConsumerEndpointID) &&
                    newConnection.getAttribute(CASA_PROVIDER_ATTR_NAME).
                    equals(newProviderEndpointID)) {
                ret = newConnection;
                break;
            }
        }

        return ret;
    }

    /**
     * Merge endpoint extension elements from the old CASA document.
     */
    private void mergeEndpointExtensions(boolean isConsumes) {
        if (oldCasaDocument == null) {
            return;
        }

        NodeList oldEndpointRefs = oldCasaDocument.getElementsByTagName(
                isConsumes ? CASA_CONSUMES_ELEM_NAME : CASA_PROVIDES_ELEM_NAME);

        for (int j = 0; j < oldEndpointRefs.getLength(); j++) {
            Element oldEndpointRef = (Element) oldEndpointRefs.item(j);
            Element newEndpointRef = findEndpointRef(oldCasaDocument,
                    oldEndpointRef, newCasaDocument, isConsumes);

            // Copy child extension elements over from old CASA to new CASA
            if (newEndpointRef != null) {
                deepCloneChildren(oldEndpointRef, newEndpointRef);
            }
        }
    }

    private static Element findEndpointRef(Document oldCasaDocument,
            Element oldEndpointRef, Document newCasaDocument, boolean isConsumes) {
        Element newEndpointRef = null;

        String oldEndpointID = oldEndpointRef.getAttribute(CASA_ENDPOINT_ATTR_NAME);
        Endpoint endpoint = getEndpoint(oldCasaDocument, oldEndpointID);
        String newEndpointID = getEndpointID(newCasaDocument, endpoint);

        if (newEndpointID != null) {
            newEndpointRef = getEndpointRefElement(
                    newCasaDocument, newEndpointID, isConsumes);
        }

        return newEndpointRef;
    }

    public static void deepCloneChildren(Element src, Element dest) {
        assert src != null && dest != null;

        Document destDocument = dest.getOwnerDocument();

        NodeList oldChildren = src.getChildNodes();
        for (int k = 0; k < oldChildren.getLength(); k++) {
            Node oldChild = oldChildren.item(k);
            if (oldChild instanceof Element) {
                Node clonedNode = deepCloneCasaNode(oldChild, destDocument);
                dest.appendChild(clonedNode);
            }
        }
    }

    /**
     * Gets the CASA endpoint reference element (consumes/provides) that 
     * corresponds to the given endpoint object.
     * 
     * @param casaDocument  CASA document
     * @param endpoint      an endpoint object
     * @param isConsumes    if <code>true</code>, the endpoint is a consumes,
     *                      if <code>false</code>, the endpoint is a provides.
     * 
     * @return  the consumes/provides element with the given ID.
     */
    static Element getEndpointRefElement(Document casaDocument,
            Endpoint endpoint, boolean isConsumes) {

        String endpointID = getEndpointID(casaDocument, endpoint);
        return getEndpointRefElement(casaDocument, endpointID, isConsumes);
    }

    /**
     * Gets the CASA endpoint reference element (consumes/provides) with the 
     * given enpoint ID.
     * 
     * @param casaDocument  CASA document
     * @param endpointID    ID of an endpoint
     * @param isConsumes    if <code>true</code>, the endpoint is a consumes,
     *                      if <code>false</code>, the endpoint is a provides.
     * 
     * @return  the consumes/provides element with the given ID.
     */
    private static Element getEndpointRefElement(Document casaDocument,
            String endpointID, boolean isConsumes) {

        NodeList endpointRefs = casaDocument.getElementsByTagName(
                isConsumes ? CASA_CONSUMES_ELEM_NAME : CASA_PROVIDES_ELEM_NAME);

        for (int j = 0; j < endpointRefs.getLength(); j++) {
            Element endpointRef = (Element) endpointRefs.item(j);
            if (endpointRef.getAttribute(CASA_ENDPOINT_ATTR_NAME).
                    equals(endpointID)) {
                return endpointRef;
            }
        }

        return null;
    }

    private Element deepCloneCasaNodeWithEndpointConversion(Element oldElement) {

        Element newElement = (Element) deepCloneCasaNode(oldElement, newCasaDocument);

        NodeList oldConsumesNodeList = oldElement.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
        NodeList newConsumesNodeList = newElement.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
        for (int i = 0; i < oldConsumesNodeList.getLength(); i++) {
            Element oldConsumes = (Element) oldConsumesNodeList.item(i);
            Element newConsumes = (Element) newConsumesNodeList.item(i);
            fixEndpointRefElementID(oldConsumes, newConsumes);
        }

        NodeList oldProvidesNodeList = oldElement.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
        NodeList newProvidesNodeList = newElement.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
        for (int i = 0; i < oldProvidesNodeList.getLength(); i++) {
            Element oldProvides = (Element) oldProvidesNodeList.item(i);
            Element newProvides = (Element) newProvidesNodeList.item(i);
            fixEndpointRefElementID(oldProvides, newProvides);
        }

        return newElement;
    }

    /**
     * Fixes the ID attribute of an endpoint reference element 
     * (consumes/provides) in the new CASA document so that both the 
     * endpoint referebce element in the new CASA document and the 
     * endpoint reference element in the old CASA document refer to 
     * the same fully-qualified endpoint.
     * 
     * @param oldEndpoint an endpoint reference element in the old CASA document
     * @param newEndpoint an endpoint reference element in the new CASA document
     */
    private void fixEndpointRefElementID(Element oldEndpointRef, Element newEndpointRef) {
        String oldEndpointID = oldEndpointRef.getAttribute(CASA_ENDPOINT_ATTR_NAME);
        Endpoint endpoint = getEndpoint(oldCasaDocument, oldEndpointID);
        String newEndpointID = addEndpoint(endpoint);
        newEndpointRef.setAttribute(CASA_ENDPOINT_ATTR_NAME, newEndpointID);
    }

    /**
     * Creates a BC SU element in the new casa document from a BC SU element 
     * in the jbi document.
     */
    private Element createBCSUFromJbiElement(Element jbiSU)
            throws SAXException, IOException, ParserConfigurationException {

        Element bcSU = newCasaDocument.createElement(CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
        String suName = BuildHelper.getJBIServiceUnitShortName(jbiSU);

        Element identification = (Element) jbiSU.getElementsByTagName(JBI_IDENTIFICATION_ELEM_NAME).item(0);
        String name = ((Element) identification.getElementsByTagName(JBI_NAME_ELEM_NAME).item(0)).getFirstChild().getNodeValue();
        String description = ((Element) identification.getElementsByTagName(JBI_DESCRIPTION_ELEM_NAME).item(0)).getFirstChild().getNodeValue();

        Element target = (Element) jbiSU.getElementsByTagName(JBI_TARGET_ELEM_NAME).item(0);
        String componentName = ((Element) target.getElementsByTagName(JBI_COMPONENT_NAME_ELEM_NAME).item(0)).getFirstChild().getNodeValue();
        String artifactsZip = ((Element) target.getElementsByTagName(JBI_ARTIFACTS_ZIP_ELEM_NAME).item(0)).getFirstChild().getNodeValue();

        bcSU.setAttribute(CASA_NAME_ATTR_NAME, name);
        bcSU.setAttribute(CASA_UNIT_NAME_ATTR_NAME, suName);
        bcSU.setAttribute(CASA_COMPONENT_NAME_ATTR_NAME, componentName);
        bcSU.setAttribute(CASA_DESCRIPTION_ATTR_NAME, description);
        bcSU.setAttribute(CASA_ARTIFACTS_ZIP_ATTR_NAME, artifactsZip);

        Element casaPorts = createPorts(suName);
        bcSU.appendChild(casaPorts);

        return bcSU;
    }

    /**
     * Creates a BC SU element in the new casa document from a BC SU element 
     * in the old casa document.
     */
    private Element createBCSUFromCasaElement(Element casaBCSU)
            throws SAXException, IOException, ParserConfigurationException {

        Element ret = newCasaDocument.createElement(CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);

        NamedNodeMap attrs = casaBCSU.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attrNode = attrs.item(i);
            String name = attrNode.getNodeName();
            String value = attrNode.getNodeValue();
            ret.setAttribute(name, value);  // no namespace requirement here
        }

        String suName = ret.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME);
        Element casaPorts = createPorts(suName);
        ret.appendChild(casaPorts);

        return ret;
    }

    /**
     * Creates a SE SU element in the new casa document from a SE SU element
     * in the jbi document.
     */
    private Element createSESUFromJbiElement(Element jbiSU)
            throws SAXException, IOException, ParserConfigurationException {

        Element seSU = newCasaDocument.createElement(CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
        String suName = BuildHelper.getJBIServiceUnitShortName(jbiSU);

        List<Endpoint> suEndpointList = su2Endpoints.get(suName);
//        if (suEndpointList == null) {
//            log("ERROR: Invalid service unit name in service assembly jbi.xml: " + suName);
//            return null;
//        }

//        if (suEndpointList.size() == 0) {
//            // This is OK. It's possible that a SU doesn't contain any endpoints, 
//            // for example, an empty BPEL SU.
//        } 

        Element identification = (Element) jbiSU.getElementsByTagName(JBI_IDENTIFICATION_ELEM_NAME).item(0);
        String name = ((Element) identification.getElementsByTagName(JBI_NAME_ELEM_NAME).item(0)).getFirstChild().getNodeValue();
        Node descriptionChildNode = ((Element) identification.getElementsByTagName(JBI_DESCRIPTION_ELEM_NAME).item(0)).getFirstChild();
        String description = descriptionChildNode == null ? "" : descriptionChildNode.getNodeValue();

        Element target = (Element) jbiSU.getElementsByTagName(JBI_TARGET_ELEM_NAME).item(0);
        String componentName = ((Element) target.getElementsByTagName(JBI_COMPONENT_NAME_ELEM_NAME).item(0)).getFirstChild().getNodeValue();
        String artifactsZip = ((Element) target.getElementsByTagName(JBI_ARTIFACTS_ZIP_ELEM_NAME).item(0)).getFirstChild().getNodeValue();

        seSU.setAttribute(CASA_X_ATTR_NAME, "-1");
        seSU.setAttribute(CASA_Y_ATTR_NAME, "-1");

        seSU.setAttribute(CASA_INTERNAL_ATTR_NAME, 
                isExternalServiceUnit(suName) ? "false" : "true");

        seSU.setAttribute(CASA_DEFINED_ATTR_NAME, "true");
        seSU.setAttribute(CASA_UNKNOWN_ATTR_NAME, "false");
        seSU.setAttribute(CASA_NAME_ATTR_NAME, name);
        seSU.setAttribute(CASA_UNIT_NAME_ATTR_NAME, suName);
        seSU.setAttribute(CASA_COMPONENT_NAME_ATTR_NAME, componentName);
        seSU.setAttribute(CASA_DESCRIPTION_ATTR_NAME, description);
        seSU.setAttribute(CASA_ARTIFACTS_ZIP_ATTR_NAME, artifactsZip);

        if (suEndpointList != null) {
            for (Endpoint endpoint : suEndpointList) {
                Element endpointRef = endpoint.isConsumes() ? (Element) newCasaDocument.createElement(CASA_CONSUMES_ELEM_NAME) : (Element) newCasaDocument.createElement(CASA_PROVIDES_ELEM_NAME);
                String endpointID = getNewEndpointID(endpoint);
                endpointRef.setAttribute(CASA_ENDPOINT_ATTR_NAME, endpointID);
                seSU.appendChild(endpointRef);
            }
        }
        return seSU;
    }

    /**
     * Creates the Connections element in the new casa document.
     */
    private Element createConnections(Document jbiDocument) {

        Element casaConnections = newCasaDocument.createElement(CASA_CONNECTIONS_ELEM_NAME);

        if (jbiDocument == null) {
          return casaConnections;
        }

        // 1. Copy jbi:connections from jbi document over.
        NodeList jbiConnections =
                jbiDocument.getElementsByTagNameNS(JBI_NAMESPACE_URI, JBI_CONNECTION_ELEM_NAME);

        for (int i = 0; i < jbiConnections.getLength(); i++) {
            Element jbiConnection = (Element) jbiConnections.item(i);

            Element consumer =
                    (Element) jbiConnection.getElementsByTagName(JBI_CONSUMER_ELEM_NAME).item(0);
            String consumerEndpointName =
                    consumer.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
            QName consumerServiceQName = XmlUtil.getAttributeNSName(
                    consumer, JBI_SERVICE_NAME_ATTR_NAME);
            String consumerID = getNewEndpointID(consumerServiceQName, consumerEndpointName);
            if (consumerID == null || consumerID.trim().length() == 0) {
                continue;   // the consumes endpoint no longer exists in compapp
            // This could happen when a JBI module is deleted outside of 
            // CASA, in which case, the SE SU is removed from CASA model
            // but all the connections are left untouched. A second build
            // of compapp still sees the connections which are no longer
            // valid. A clean and build should have no problem though.
            }

            Element provider =
                    (Element) jbiConnection.getElementsByTagName(JBI_PROVIDER_ELEM_NAME).item(0);
            String providerEndpointName =
                    provider.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
            QName providerServiceQName = XmlUtil.getAttributeNSName(
                    provider, JBI_SERVICE_NAME_ATTR_NAME);
            String providerID = getNewEndpointID(providerServiceQName, providerEndpointName);
            if (providerID == null || providerID.trim().length() == 0) {
                continue;   // the provides endpoint no longer exists in compapp
            }

            Element casaConnection = newCasaDocument.createElement(CASA_CONNECTION_ELEM_NAME);
            casaConnection.setAttribute(CASA_STATE_ATTR_NAME, CASA_UNCHANGED_ATTR_VALUE);
            casaConnection.setAttribute(CASA_CONSUMER_ATTR_NAME, consumerID);
            casaConnection.setAttribute(CASA_PROVIDER_ATTR_NAME, providerID);
            casaConnections.appendChild(casaConnection);
        }

        // 2. Mark user created connection as "new";
        //    Merge deleted connections from the old casa document.
        if (oldCasaDocument != null) {
            try {
                NodeList oldConnectionList = oldCasaDocument.getElementsByTagName(
                        CASA_CONNECTION_ELEM_NAME);

                for (int i = oldConnectionList.getLength() - 1; i >= 0; i--) {
                    Element oldConnection = (Element) oldConnectionList.item(i);
                    String oldConnectionState = oldConnection.getAttribute(CASA_STATE_ATTR_NAME);
                    if (oldConnectionState.equals(CASA_DELETED_ATTR_VALUE) ||
                            oldConnectionState.equals(CASA_NEW_ATTR_VALUE)) {
                        String newConsumerID =
                                findNewEndpointID(oldCasaDocument, newCasaDocument, oldConnection, true);
                        if (newConsumerID == null || newConsumerID.trim().length() == 0) {
                            continue;   // the consumes endpoint no longer exists in compapp
                        }

                        String newProviderID =
                                findNewEndpointID(oldCasaDocument, newCasaDocument, oldConnection, false);
                        if (newProviderID == null || newProviderID.trim().length() == 0) {
                            continue;   // the provides endpoint no longer exists in compapp
                        }

                        Element newConnection =
                                findConnection(casaConnections, newConsumerID, newProviderID);

                        if (oldConnectionState.equals(CASA_DELETED_ATTR_VALUE)) {
                            // Merge deleted connections from the old casa document
                            assert newConnection == null;
                            // Add deleted casa connection
                            newConnection = newCasaDocument.createElement(CASA_CONNECTION_ELEM_NAME);
                            newConnection.setAttribute(CASA_CONSUMER_ATTR_NAME, newConsumerID);
                            newConnection.setAttribute(CASA_PROVIDER_ATTR_NAME, newProviderID);
                            newConnection.setAttribute(CASA_STATE_ATTR_NAME, CASA_DELETED_ATTR_VALUE);
                            casaConnections.appendChild(newConnection);
                        } else {
                            // Mark user created connection as "new"
                            assert newConnection != null : "Cannot find user-created connection: " + newConsumerID + "->" + newProviderID;
                            newConnection.setAttribute(CASA_STATE_ATTR_NAME, CASA_NEW_ATTR_VALUE);
                        }
                    }
                }
            } catch (Exception e) {
                log("ERROR: Problem merging deleted/new connections from old casa: " + e +
                        ". This does not affect regular compapp build.");
            }
        }

        return casaConnections;
    }

    private List<Element> createWSDLReferenceElements() {

        List<Element> ret = new ArrayList<Element>();

        Element casaPortTypes = newCasaDocument.createElement(CASA_PORTTYPES_ELEM_NAME);
        Element casaBindings = newCasaDocument.createElement(CASA_BINDINGS_ELEM_NAME);
        Element casaServices = newCasaDocument.createElement(CASA_SERVICES_ELEM_NAME);

        for (WSDLModel model : wsdlRepository.getWsdlCollection()) {

            String relativePath =
                    MyFileUtil.getRelativePath(new File(confDirLoc), getFile(model));

            Definitions defs = model.getDefinitions();

            // Add casa:porttypes
            for (PortType pt : defs.getPortTypes()) {
                String ptName = pt.getName();
                Element linkElement = createLink(relativePath,
                        "/definitions/portType" + "[@name='" + ptName + "']");
                casaPortTypes.appendChild(linkElement);
            }

            // Skip Binding and Service definitions from external SE SUs
            if (wsdlRepository.isDefinedInExternalSU(model)) {
                continue;
            }

            // Add casa:bindings
            for (Binding b : defs.getBindings()) {
                String bName = b.getName();
                Element linkElement = createLink(relativePath,
                        "/definitions/binding" + "[@name='" + bName + "']");
                casaBindings.appendChild(linkElement);
            }

            // Add casa:services
            for (Service s : defs.getServices()) {
                String sName = s.getName();
                Element linkElement = createLink(relativePath,
                        "/definitions/service" + "[@name='" + sName + "']");
                casaServices.appendChild(linkElement);
            }
        }

        ret.add(casaPortTypes);
        ret.add(casaBindings);
        ret.add(casaServices);

        return ret;
    }

    /**
     * Creates the Regions element in the new casa document.
     */
    private Element createRegions() {

        if (oldCasaDocument != null) {
            try {
                Element oldRegions = (Element) oldCasaDocument.getElementsByTagName(
                        CASA_REGIONS_ELEM_NAME).item(0);
                return (Element) deepCloneCasaNode(oldRegions, newCasaDocument);
            } catch (Exception e) {
                log("ERROR: Problem merging regsions from old casa: " + e +
                        ". This does not affect regular compapp build.");
            }
        }

        Element regions = newCasaDocument.createElement(CASA_REGIONS_ELEM_NAME);

        Element region = newCasaDocument.createElement(CASA_REGION_ELEM_NAME);
        region.setAttribute(CASA_NAME_ATTR_NAME, WSDL_ENDPOINTS_REGION_NAME);
        region.setAttribute(CASA_WIDTH_ATTR_NAME, DEFAULT_WSDL_ENDPOINTS_REGION_WIDTH);
        regions.appendChild(region);

        region = newCasaDocument.createElement(CASA_REGION_ELEM_NAME);
        region.setAttribute(CASA_NAME_ATTR_NAME, JBI_MODULES_REGION_NAME);
        region.setAttribute(CASA_WIDTH_ATTR_NAME, DEFAULT_JBI_MODULES_REGION_WIDTH);
        regions.appendChild(region);

        region = newCasaDocument.createElement(CASA_REGION_ELEM_NAME);
        region.setAttribute(CASA_NAME_ATTR_NAME, EXTERNAL_MODULES_REGION_NAME);
        region.setAttribute(CASA_WIDTH_ATTR_NAME, DEFAULT_EXTERNAL_MODULES_REGION_WIDTH);

        regions.appendChild(region);

        return regions;
    }

    private String getJBIServiceUnitComponentName(Element jbiSU) {
        Element target = (Element) jbiSU.getElementsByTagName(JBI_TARGET_ELEM_NAME).item(0);
        Element compName = (Element) target.getElementsByTagName(JBI_COMPONENT_NAME_ELEM_NAME).item(0);
        return compName.getTextContent();
    }

    private static File getFile(WSDLModel model) {
        Lookup lookup = model.getModelSource().getLookup();
        File f = lookup.lookup(File.class);
        if (f == null) {
            FileObject fo = lookup.lookup(FileObject.class);
            f = FileUtil.toFile(fo);
        }
        return f;
    }

//    private List<String> getDeletedBCSUs() {
//        List<String> ret = new ArrayList<String>();
//         
//         if (oldCasaDocument != null) {
//             NodeList bcsuNodeList =
//                     oldCasaDocument.getElementsByTagName(CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
//             
//             for (int i = 0; i < bcsuNodeList.getLength(); i++) {
//                 Element bcSU = (Element) bcsuNodeList.item(i);
//                 String bcName = bcSU.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME);
//                 
//                 NodeList portNodeList = bcSU.getElementsByTagName(CASA_PORT_ELEM_NAME);
//                 for (int j = 0; j < portNodeList.getLength(); j++) {
//                     Element port = (Element) portNodeList.item(j);
//                     String state = port.getAttribute(CASA_STATE_ATTR_NAME);
//                     if (CASA_DELETED_ATTR_VALUE.equals(state)) {
//                         
//                     }
//                 }
//             }
//         }
//        
//        return ret;
//    }
    private Map<String, List<Endpoint>> getDeletedBCEndpointsMap() {

        if (deletedBCEndpointsMap == null) {
            deletedBCEndpointsMap = new HashMap<String, List<Endpoint>>();

            if (oldCasaDocument != null) {
                NodeList portNodeList =
                        oldCasaDocument.getElementsByTagName(CASA_PORT_ELEM_NAME);

                for (int i = 0; i < portNodeList.getLength(); i++) {
                    Element port = (Element) portNodeList.item(i);
                    String state = port.getAttribute(CASA_STATE_ATTR_NAME);
                    if (CASA_DELETED_ATTR_VALUE.equals(state)) {
                        // assume both Consumes and Provides coexist and give
                        // the same endpoint info
                        NodeList consumesNodeList =
                                port.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
                        if (consumesNodeList != null && consumesNodeList.getLength() == 1) {
                            Element consumes = (Element) consumesNodeList.item(0);
                            String endpointID = consumes.getAttribute(CASA_ENDPOINT_ATTR_NAME);
                            Endpoint endpoint = getEndpoint(oldCasaDocument, endpointID);

                            // build deltedBCEndpointsMap
                            Element bcsuElement = (Element) port.getParentNode().getParentNode();
                            String bcName = bcsuElement.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME);
                            List<Endpoint> deletedBCEndpoints = deletedBCEndpointsMap.get(bcName);
                            if (deletedBCEndpoints == null) {
                                deletedBCEndpoints = new ArrayList<Endpoint>();
                                deletedBCEndpointsMap.put(bcName, deletedBCEndpoints);
                            }
                            deletedBCEndpoints.add(endpoint);
                        }
                    }
                }
            }
        }

        return deletedBCEndpointsMap;
    }

    private Map<String, List<Endpoint>> getUnconnectedBCEndpointsMap() {

        Map<String, List<Endpoint>> endpointMap = new HashMap<String, List<Endpoint>>();

        if (oldCasaDocument != null) {
            // compute the list of connected endpoint IDs in the old casa
            List<String> connectedEndpointIDs = new ArrayList<String>();
            NodeList connectionNodeList =
                    oldCasaDocument.getElementsByTagName(CASA_CONNECTION_ELEM_NAME);
            for (int i = 0; i < connectionNodeList.getLength(); i++) {
                Element connection = (Element) connectionNodeList.item(i);
                String state = connection.getAttribute(CASA_STATE_ATTR_NAME);
                if (!state.equals(CASA_DELETED_ATTR_VALUE)) {
                    connectedEndpointIDs.add(connection.getAttribute(CASA_CONSUMER_ATTR_NAME));
                    connectedEndpointIDs.add(connection.getAttribute(CASA_PROVIDER_ATTR_NAME));
                }
            }

            NodeList portNodeList =
                    oldCasaDocument.getElementsByTagName(CASA_PORT_ELEM_NAME);

            List<Endpoint> endpointList = new ArrayList<Endpoint>();
            NodeList endpointNodeList =
                    oldCasaDocument.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
            for (int i = 0; i < endpointNodeList.getLength(); i++) {
                Element endpointElement = (Element) endpointNodeList.item(i);
                String endpointID = endpointElement.getAttribute(CASA_NAME_ATTR_NAME);
                if (!connectedEndpointIDs.contains(endpointID)) {
                    // this is an unconnected SE/BC endpoint
                    for (int j = 0; j < portNodeList.getLength(); j++) {
                        Element portElement = (Element) portNodeList.item(j);
                        String id = getCasaPortEndpointID(portElement);
                        if (id.equals(endpointID)) {
                            // this is an unconnected BC endpoint
                            Element bcsuElement = (Element) portElement.getParentNode().getParentNode();
                            String componentName = bcsuElement.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME);
                            List<Endpoint> list = endpointMap.get(componentName);
                            if (list == null) {
                                list = new ArrayList<Endpoint>();
                                endpointMap.put(componentName, list);
                            }

                            Endpoint endpoint = getEndpointInCASA(endpointElement);
                            list.add(endpoint);
                            break;
                        }
                    }
                }
            }
        }

        return endpointMap;
    }

    private String getCasaPortEndpointID(Element portElement) {
        NodeList consumesNodeList =
                portElement.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
        if (consumesNodeList != null && consumesNodeList.getLength() == 1) {
            Element consumes = (Element) consumesNodeList.item(0);
            return consumes.getAttribute(CASA_ENDPOINT_ATTR_NAME);
        } else {
            return null;
        }
    }

    /**
     * Gets a non-null list of external SE SU elements in the old casa document.
     */
    private List<Element> getExternalSESUs() {
        List<Element> ret = new ArrayList<Element>();

        if (oldCasaDocument != null) {
            NodeList oldCasaSUs = oldCasaDocument.getElementsByTagName(
                    CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
            for (int i = 0; i < oldCasaSUs.getLength(); i++) {
                Element oldCasaSU = (Element) oldCasaSUs.item(i);
                String type = oldCasaSU.getAttribute(CASA_INTERNAL_ATTR_NAME);
                if (type != null && type.equals("false")) {
                    ret.add(oldCasaSU);
                }
            }
        }

        return ret;
    }

    /**
     * Gets a non-null list of endpoints from the old CASA document. 
     * (The external endpoints in the new CASA document will always be the same.)
     */
    private List<Endpoint> getExternalEndpoints() {
        if (externalEndpoints == null) {
            externalEndpoints = new ArrayList<Endpoint>();

            for (Element sesu : getExternalSESUs()) {
                NodeList consumesNodeList =
                        sesu.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
                for (int j = 0; j < consumesNodeList.getLength(); j++) {
                    Element consumes = (Element) consumesNodeList.item(j);
                    String endpointID = consumes.getAttribute(CASA_ENDPOINT_ATTR_NAME);
                    Endpoint endpoint = getEndpoint(oldCasaDocument, endpointID);
                    externalEndpoints.add(endpoint);
                }
                NodeList providesNodeList =
                        sesu.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
                for (int j = 0; j < providesNodeList.getLength(); j++) {
                    Element provides = (Element) providesNodeList.item(j);
                    String endpointID = provides.getAttribute(CASA_ENDPOINT_ATTR_NAME);
                    Endpoint endpoint = getEndpoint(oldCasaDocument, endpointID);
                    externalEndpoints.add(endpoint);
                }
            }
        }

        return externalEndpoints;
    }

    private void mergeLocations() {

        if (oldCasaDocument == null) {
            return;
        }

        // 1. Merge SE SU locations
        NodeList oldSESUs = oldCasaDocument.getElementsByTagName(
                CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
        NodeList newSESUs = newCasaDocument.getElementsByTagName(
                CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);

        for (int i = 0; i < oldSESUs.getLength(); i++) {
            Element oldSESU = (Element) oldSESUs.item(i);
            // CASA_UNIT_NAME_ATTR_NAME uniquely identifies a SE/BC SU.
            String oldSESUName = oldSESU.getAttribute(CASA_UNIT_NAME_ATTR_NAME);
            for (int j = 0; j < newSESUs.getLength(); j++) {
                Element newSESU = (Element) newSESUs.item(j);
                String newSESUName = newSESU.getAttribute(CASA_UNIT_NAME_ATTR_NAME);
                if (newSESUName.equals(oldSESUName)) {
                    newSESU.setAttribute(CASA_X_ATTR_NAME,
                            oldSESU.getAttribute(CASA_X_ATTR_NAME));
                    newSESU.setAttribute(CASA_Y_ATTR_NAME,
                            oldSESU.getAttribute(CASA_Y_ATTR_NAME));
                    break;
                }
            }
        }

        // 2. Merge BC SU port locations and other attributes
        NodeList oldPorts = oldCasaDocument.getElementsByTagName(CASA_PORT_ELEM_NAME);
        NodeList newPorts = newCasaDocument.getElementsByTagName(CASA_PORT_ELEM_NAME);

        for (int i = 0; i < oldPorts.getLength(); i++) {
            Element oldPort = (Element) oldPorts.item(i);
            Element newPort = findLinkContainerElement(newPorts, oldPort);
            if (newPort != null) {
                newPort.setAttribute(CASA_X_ATTR_NAME,
                        oldPort.getAttribute(CASA_X_ATTR_NAME));
                newPort.setAttribute(CASA_Y_ATTR_NAME,
                        oldPort.getAttribute(CASA_Y_ATTR_NAME));
                String bindingType = oldPort.getAttribute(CASA_BINDING_TYPE_ATTR_NAME);
                if (bindingType != null && bindingType.trim().length() > 0) {
                    newPort.setAttribute(CASA_BINDING_TYPE_ATTR_NAME, bindingType);
                }

                // 2.1 merge casa port extensibilty elements
                NodeList pNodes = oldPort.getChildNodes();
                for (int k = 0; k < pNodes.getLength(); k++) {
                    if (pNodes.item(k) instanceof Element) {
                        Element pNode = (Element) pNodes.item(k);
                        String ns = pNode.getNamespaceURI();
                        // todo: Assume non CASA elemeents are extensions...
                        if (!CASA_NAMESPACE_URI.equals(ns)) {
                            Node clonedNode = deepCloneCasaNode(pNode, newCasaDocument);
                            newPort.appendChild(clonedNode);
                        }
                    }
                }

            }
        }
    }

    private static Element getCasaEndpointElement(Document casaDocument, String endpointID) {
        NodeList endpoints = casaDocument.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
        for (int i = 0; i < endpoints.getLength(); i++) {
            Element endpoint = (Element) endpoints.item(i);
            if (endpoint.getAttribute(CASA_NAME_ATTR_NAME).equals(endpointID)) {
                return endpoint;
            }
        }
        return null;
    }

    private String findNewEndpointID(Document oldCasaDocument, Document newCasaDocument,
            Element oldConnection, boolean isConsumes) {
        String oldEndpointID = isConsumes ? oldConnection.getAttribute(CASA_CONSUMER_ATTR_NAME) : oldConnection.getAttribute(CASA_PROVIDER_ATTR_NAME);

        Element oldEndpoint = getCasaEndpointElement(oldCasaDocument, oldEndpointID);
        if (oldEndpoint != null) {
            String oldEndpointName =
                    oldEndpoint.getAttribute(CASA_ENDPOINT_NAME_ATTR_NAME);
            QName oldServiceQName =
                    XmlUtil.getAttributeNSName(oldEndpoint, CASA_SERVICE_NAME_ATTR_NAME);
            return getNewEndpointID(oldServiceQName, oldEndpointName);
        }

        return null;
    }

    private Element findConnection(Element connections,
            String consumerID, String providerID) {

        NodeList connectionsNodeList =
                connections.getElementsByTagName(CASA_CONNECTION_ELEM_NAME);
        for (int i = 0; i < connectionsNodeList.getLength(); i++) {
            Element connection = (Element) connectionsNodeList.item(i);
            if (connection.getAttribute(CASA_CONSUMER_ATTR_NAME).equals(consumerID) &&
                    connection.getAttribute(CASA_PROVIDER_ATTR_NAME).equals(providerID)) {
                return connection;
            }
        }

        return null;
    }

    // REFACTOR ME
    static Node deepCloneCasaNode(final Node node,
            final Document targetDocument) throws DOMException {
        String nodeName = node.getNodeName();

        Node clonedNode = null;

        if (node instanceof Element) {
            clonedNode = targetDocument.createElement(nodeName);

            NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attrNode = attrs.item(i);
                String attrName = attrNode.getNodeName();
                String attrValue = attrNode.getNodeValue();

                if (!attrName.equals("xmlns") && attrValue.indexOf(":") != -1) {
                    String oldValuePrefix =
                            attrValue.substring(0, attrValue.indexOf(":"));
                    String oldNamespaceURI =
                            XmlUtil.getNamespaceURI((Element) node, oldValuePrefix); // REFACTOR ME

                    // We don't really know if the attr value is a QName or not.
                    if (!oldNamespaceURI.equals("")) {
                        // Add the namespace definition to the cloned node.
                        ((Element) clonedNode).setAttribute(
                                "xmlns:" + oldValuePrefix, oldNamespaceURI);
                    }
                }

                ((Element) clonedNode).setAttribute(attrName, attrValue);
            }

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                Node clonedChild = deepCloneCasaNode(child, targetDocument);
                clonedNode.appendChild(clonedChild);
            }
        } else if (node instanceof Text) {
            clonedNode = targetDocument.createTextNode(((Text) node).getWholeText());
        } else {
            assert false : "deep clone node of type " + node.getClass().getName() + " is not implemented yet.";
        }

        return clonedNode;
    }

    // TODO
    /**
     * Finds a link container element from a list of link container elements.
     */
    private Element findLinkContainerElement(NodeList newPortNodeList, Element oldPort) {

        // Note that a casa port might contain multiple links.
        NodeList oldLinkNodeList = oldPort.getElementsByTagName(CASA_LINK_ELEM_NAME);
        for (int i = 0; i < oldLinkNodeList.getLength(); i++) {
            Element oldLink = (Element) oldLinkNodeList.item(i);
            String oldLinkHref = oldLink.getAttributeNS(XLINK_NAMESPACE_URI, XLINK_HREF_ATTR_NAME);
            for (int j = 0; j < newPortNodeList.getLength(); j++) {
                Element newPort = (Element) newPortNodeList.item(j);
                NodeList newLinkNodeList = newPort.getElementsByTagName(CASA_LINK_ELEM_NAME);
                for (int k = 0; k < newLinkNodeList.getLength(); k++) {
                    Element newLink = (Element) newLinkNodeList.item(k);
                    // String myLinkHref = myLink.getAttributeNS(XLINK_NAMESPACE_URI, XLINK_HREF_ATTR_NAME); // FIXME ?????
                    String newLinkHref = newLink.getAttribute(XLINK_NAMESPACE_PREFIX + ":" + XLINK_HREF_ATTR_NAME);
                    if (newLinkHref.equals(oldLinkHref)) {
                        return newPort;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the Endpoint object in the given CASA document with the given
     * endpoint ID.
     * 
     * @param casaDocument  CASA document
     * @param endpointID    ID of an endpoint
     * 
     * @return an Endpoint object
     */
    static Endpoint getEndpoint(Document casaDocument, String endpointID) {
        NodeList endpointList =
                casaDocument.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
        for (int i = 0; i < endpointList.getLength(); i++) {
            Element endpoint = (Element) endpointList.item(i);
            if (endpoint.getAttribute(CASA_NAME_ATTR_NAME).equals(endpointID)) {
                return getEndpointInCASA(endpoint);
            }
        }

        return null;
    }

    /**
     * Gets a non-null list of Endpoints defined in the given CASA document.
     */
    private static List<Endpoint> getEndpoints(Document casaDocument) {
        List<Endpoint> ret = new ArrayList<Endpoint>();

        if (casaDocument != null) {
            NodeList endpointList =
                    casaDocument.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
            for (int i = 0; i < endpointList.getLength(); i++) {
                Element endpointElement = (Element) endpointList.item(i);
                Endpoint endpoint = getEndpointInCASA(endpointElement);
                ret.add(endpoint);
            }
        }

        return ret;
    }

    /**
     * Computes a non-null list of endpoints that are defined in the WSDL files 
     * in the compapp project and the component projects.
     */
    // Loop through all WSDL files, create an allWSDLEndpoints list which
    // contains all the ports defined in the WSDL files in the compapp 
    // project and the component projects.
    private List<Endpoint> getNewWsdlEndpoints() {
        if (newWsdlEndpoints == null) {
            newWsdlEndpoints = new ArrayList<Endpoint>();

            for (WSDLModel model : wsdlRepository.getWsdlCollection()) {
                String tns = model.getDefinitions().getTargetNamespace(); // model.getRootComponent().getPeer().getAttribute("targetNamespace");
                for (Service service : model.getDefinitions().getServices()) {
                    QName serviceQName = new QName(tns, service.getName());
                    for (Port port : service.getPorts()) {
                        String portName = port.getName();
                        Binding binding = port.getBinding().get();
                        if (binding != null) {
                            QName interfaceQName = binding.getType().getQName();
                            Endpoint endpoint = new Endpoint(portName, serviceQName, interfaceQName);
                            if (!newWsdlEndpoints.contains(endpoint)) {
                                newWsdlEndpoints.add(endpoint);
                            }
                        }
                    }
                }
            }
        }

        return newWsdlEndpoints;
    }

    /**
     * Computes a non-null list of endpoints that need to go into the new 
     * CASA document.
     */
    // Those endpoints come from either the old CASA document, or the new WSDL 
    // files added into compapp. The following rules apply:
    // (1) All the old endpoints with owning WSDL no longer in compapp need to 
    //     be removed, whether the endpoint was connected, unconnected, or 
    //     marked as "deleted";
    // (2) All the old endpoints with owning WSDL still in compapp need to 
    //     be preserved; whether the endpoint was connected, unconnected, or 
    //     marked as "deleted";
    // (3) All the old endpoints without owning WSDL (external endpoints) 
    //     need to be preserved;
    // (4) Only the connected endpoints in the newly added WSDLs need to be added;     
    private List<Endpoint> getNewCasaEndpoints(Document jbiDocument) {

        List<Endpoint> oldCasaEndpoints = getEndpoints(oldCasaDocument);

        // Loop through all jbiServiceUnits/$suName/jbi.xml, create a 
        // newSUEndpoints list
        List<Endpoint> newSUEndpoints = new ArrayList<Endpoint>();

        if (jbiDocument == null) {
          return new ArrayList<Endpoint>();
        }

        NodeList jbiSUs = jbiDocument.getElementsByTagName(JBI_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < jbiSUs.getLength(); i++) {
            Element jbiSU = (Element) jbiSUs.item(i);
            String suName = BuildHelper.getJBIServiceUnitShortName(jbiSU);

            // Skip consumes endpoints in external SE SUs
            boolean includeConsumesEndpoints = true;
            if (BuildServiceAssembly.SKIP_EXTERNAL_CONSUMES_ENDPOINTS &&
                    isExternalServiceUnit(suName)) {
                includeConsumesEndpoints = false;
            }
            List<Endpoint> suEndpoints = 
                    loadSUEndpoints(suName, includeConsumesEndpoints, true);

            su2Endpoints.put(suName, suEndpoints);
            if (suEndpoints != null) {
                for (Endpoint suEndpoint : suEndpoints) {
                    if (!newSUEndpoints.contains(suEndpoint)) {
                        newSUEndpoints.add(suEndpoint);
                    }
                }
            }
        }

        // SU name defined in AssemblyInformation.xml might not match the
        // project reference name defined in project.xml. Possible reasons:
        //   * (#160061) SU project containing "." in its name gets a different
        //               project reference name ("." replaced by "_")
        //   * (#160062) SU project gets renamed after being added into CompApp
        File projectDir = project.getBaseDir();
        for (String projRefName : getProjectReferenceNames()) {

            String suProjLoc = project.getProperty("project." + projRefName);
            File suProjDir = new File(suProjLoc);
            if (!suProjDir.isAbsolute()) {
                suProjDir = new File(projectDir, suProjLoc);
            }

            // Get SU name from the SU project's project.xml
            String suName = getProjectName(suProjDir);

            // Skip consumes endpoints in external SE SUs
            boolean includeConsumesEndpoints = true;
            if (BuildServiceAssembly.SKIP_EXTERNAL_CONSUMES_ENDPOINTS &&
                    isExternalServiceUnit(suName)) {
                includeConsumesEndpoints = false;
            }
            List<Endpoint> suEndpoints = 
                    loadSUEndpoints(projRefName, includeConsumesEndpoints, true);

            su2Endpoints.put(suName, suEndpoints);
            if (suEndpoints != null) {
                for (Endpoint suEndpoint : suEndpoints) {
                    if (!newSUEndpoints.contains(suEndpoint)) {
                        newSUEndpoints.add(suEndpoint);
                    }
                }
            }
        }

        // Compute externalAndWsdlEndpoints:
        // externalAndWsdlEndpoints =
        //      externalEndpoints + newWsdlEndpoints 
        List<Endpoint> externalAndWsdlEndpoints = new ArrayList<Endpoint>();
        externalAndWsdlEndpoints.addAll(getExternalEndpoints());
        externalAndWsdlEndpoints.addAll(getNewWsdlEndpoints());

        // Compute newCasaEndpoints: 
        //       newCasaEndpoints = 
        //              Intersection(oldCasaEndpoints, externalEndpoints + newWsdlEndpoints)
        //              + newSUEndpoints
        // When computing the list of newCasaEndpoints, we want to preserve the 
        // order (and therefore the ID) of endpoints in the oldCasaEndpoints
        // list as much as possible to avoid unnecessary (endpoint ID) changes 
        // across compapp builds.
        List<Endpoint> newCasaEndpoints = new ArrayList<Endpoint>();
        for (Endpoint oldCasaEndpoint : oldCasaEndpoints) {
            for (Endpoint endpoint : externalAndWsdlEndpoints) {
                if (oldCasaEndpoint.equals(endpoint)) {
                    newCasaEndpoints.add(oldCasaEndpoint);
                    break;
                }
            }
        }

        for (Endpoint suEndpoint : newSUEndpoints) {
            if (!newCasaEndpoints.contains(suEndpoint)) {
                newCasaEndpoints.add(suEndpoint);
            }
        }

        debugLog("old CASA Endpoints:");
        for (Endpoint endpoint : oldCasaEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }
        debugLog("new WSDL Endpoints:");
        for (Endpoint endpoint : newWsdlEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }
        debugLog("external Endpoints:");
        for (Endpoint endpoint : externalEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }
        debugLog("new SU Endpoints:");
        for (Endpoint endpoint : newSUEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }
        debugLog("new CASA Endpoints:");
        for (Endpoint endpoint : newCasaEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }

        return newCasaEndpoints;
    }

    private List<String> getProjectReferenceNames() {
        List<String> ret = new ArrayList<String>();

        try {
            File projectXmlFile = new File(project.getBaseDir(), "nbproject/project.xml");
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            Document document = builder.parse(projectXmlFile);
            NodeList foreignProjectNodes =
                    document.getElementsByTagName("foreign-project");
            for (int i = 0; i < foreignProjectNodes.getLength(); i++) {
                ret.add(foreignProjectNodes.item(i).getTextContent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private String getProjectName(File projBaseDir) {

        try {
            File projectXmlFile = new File(projBaseDir, "nbproject/project.xml");
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            Document document = builder.parse(projectXmlFile);
            return document.getElementsByTagName("name").item(0).getTextContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates the Endpoints element in the new casa document.
     */
    private Element createEndpoints(Document jbiDocument) {

        Element endpoints = newCasaDocument.createElement(CASA_ENDPOINTS_ELEM_NAME);
        Element casaRoot = (Element) newCasaDocument.getElementsByTagName("casa").item(0);
        casaRoot.appendChild(endpoints);

        List<Endpoint> newCasaEndpoints = getNewCasaEndpoints(jbiDocument);
        for (Endpoint endpoint : newCasaEndpoints) {
            addEndpoint(endpoint);
        }

        return endpoints;
    }

    /**
     * Adds an endpoint to the new casa document. Do nothing if the given 
     * endpoint already exists.
     *
     * @return the ID of the endpoint
     */
    private String addEndpoint(Endpoint endpoint) {

        String key = endpoint.getFullyQualifiedName();
        String endpointID = newEndpointMap.get(key);

        if (endpointID == null) {
            endpointID = "endpoint" + endpointIndex;
            endpointIndex++;
            newEndpointMap.put(key, endpointID);

            Element endpoints =
                    (Element) newCasaDocument.getElementsByTagName("endpoints").item(0);

            Element casaEndpoint = newCasaDocument.createElement(CASA_ENDPOINT_ELEM_NAME);
            casaEndpoint.setAttribute(CASA_NAME_ATTR_NAME, endpointID);
            casaEndpoint.setAttribute(CASA_ENDPOINT_NAME_ATTR_NAME,
                    endpoint.getEndpointName());
            setAttributeQName(casaEndpoint, CASA_SERVICE_NAME_ATTR_NAME,
                    endpoint.getServiceQName());
            setAttributeQName(casaEndpoint, CASA_INTERFACE_NAME_ATTR_NAME,
                    endpoint.getInterfaceQName());

            if (endpoint instanceof EndpointWithExtension) {
                EndpointWithExtension endpointWithExtension =
                        (EndpointWithExtension) endpoint;

                String displayName = endpointWithExtension.getDisplayName();
                String processName = endpointWithExtension.getProcessName();
                String filePath = endpointWithExtension.getFilePath();

                if (displayName != null && displayName.length() > 0) {
                    casaEndpoint.setAttribute(JBI_ENDPOINT_EXTENSION_DISPLAY_NAME, displayName);
                }

                if (processName != null && processName.length() > 0) {
                    casaEndpoint.setAttribute(JBI_ENDPOINT_EXTENSION_PROCESS_NAME, processName);
                }

                if (filePath != null && filePath.length() > 0) {
                    casaEndpoint.setAttribute(JBI_ENDPOINT_EXTENSION_FILE_PATH, filePath);
                }
            }

            endpoints.appendChild(casaEndpoint);
        }

        return endpointID;
    }

    /** 
     * Gets the ID of an endpoint in the new casa document.
     */
    private String getNewEndpointID(QName serviceQName, String endpointName) {
        String key = serviceQName.toString() + "." + endpointName;
        return newEndpointMap.get(key);
    }

    /** 
     * Gets the ID of an endpoint in the new casa document.
     */
    private String getNewEndpointID(Endpoint endpoint) {
        String key = endpoint.getFullyQualifiedName();
        return newEndpointMap.get(key);
    }

    // TODO
    // Mapping namespace URI to namespace prefix
    private Map<String, String> nsMap = new HashMap<String, String>();
    private int prefixID = 1;

    private void setAttributeQName(Element element, String attrName,
            QName attrValueQName) {
        String attrValueNamespace = attrValueQName.getNamespaceURI();
        String attrValue = attrValueQName.getLocalPart();

        String attrQValue;

        if (attrValueNamespace.equals("") && attrValue.equals("")) {
            attrQValue = "";
        } else {
            String prefix = nsMap.get(attrValueNamespace);
            if (prefix == null) {
                prefix = "ns" + prefixID;
                prefixID++;
                element.getOwnerDocument().getDocumentElement().setAttribute(
                        "xmlns:" + prefix, attrValueNamespace);
                nsMap.put(attrValueNamespace, prefix);
            }
            attrQValue = prefix + ":" + attrValue;
        }

        element.setAttribute(attrName, attrQValue);
    }

    /**
     * Gets all the Consumes and/or Provides endpoints from the given service unit.
     *
     * @param projRefName       foreign project reference name (as defined in project.xml)
     * @param includeConsumes   whether to include consumes endpoints
     * @param includeProvides   whether to include provides endpoints
     *
     * @return              a list of endpoints from the given service unit
     */
    private List<Endpoint> loadSUEndpoints(String projRefName,
            boolean includeConsumes, boolean includeProvides) {

        List<Endpoint> suEndpointList = new ArrayList<Endpoint>();

        File suJbiFile = new File(serviceUnitsDirLoc + projRefName + File.separator + "jbi.xml"); // NOI18N
        if (suJbiFile.exists()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            try {
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                Document suJbiDocument = documentBuilder.parse(suJbiFile);
                NodeList servicesElements = suJbiDocument.getElementsByTagNameNS(
                        JBI_NAMESPACE_URI, JBI_SERVICES_ELEM_NAME);
                for (int i = 0; i < servicesElements.getLength(); i++) {
                    Element servicesElement = (Element) servicesElements.item(i);
                    NodeList children = servicesElement.getChildNodes();
                    for (int k = 0; k < children.getLength(); k++) {
                        Node child = children.item(k);
                        if (child instanceof Element) {
                            Element e = (Element) child;
                            String localName = e.getLocalName();

                            if (localName.equals(JBI_CONSUMES_ELEM_NAME) && !includeConsumes ||
                                    localName.equals(JBI_PROVIDES_ELEM_NAME) && !includeProvides) {
                                continue;
                            }

                            String endpointName = e.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);

                            String serviceName = e.getAttribute(JBI_SERVICE_NAME_ATTR_NAME);
                            String serviceNS = null;
                            int idx = serviceName.indexOf(':');
                            if (idx > 0) {
                                String prefix = serviceName.substring(0, idx);
                                serviceNS = suJbiDocument.getDocumentElement().getAttribute("xmlns:" + prefix);
                                serviceName = serviceName.substring(idx + 1);
                            }

                            String interfaceName = e.getAttribute(JBI_INTERFACE_NAME_ATTR_NAME);
                            String interfaceNS = "";
                            idx = interfaceName.indexOf(':');
                            if (idx > 0) {
                                String prefix = interfaceName.substring(0, idx);
                                interfaceNS = suJbiDocument.getDocumentElement().getAttribute("xmlns:" + prefix);
                                interfaceName = interfaceName.substring(idx + 1);
                            }

                            // 06/05/07, T. Li skip extension elements.
                            if ((interfaceName != null) && (interfaceName.length() > 0)) {
                                EndpointWithExtension endpoint = new EndpointWithExtension(endpointName,
                                        new QName(serviceNS, serviceName),
                                        new QName(interfaceNS, interfaceName),
                                        e.getLocalName().equals("consumes"));

//                                NodeList endpointExtensions =
//                                        e.getElementsByTagNameNS(
//                                        JBI_ENDPOINT_EXTENSION_NAMESPACE,
//                                        JBI_ENDPOINT_EXTENSION_ELEMENT);
//
//                                if (endpointExtensions.getLength() == 1) {
//                                    Element endpointExtension = (Element) endpointExtensions.item(0);
//
//                                    String displayName = endpointExtension.getAttribute(JBI_ENDPOINT_EXTENSION_DISPLAY_NAME);
//                                    String processName = endpointExtension.getAttribute(JBI_ENDPOINT_EXTENSION_PROCESS_NAME);
//                                    String filePath = endpointExtension.getAttribute(JBI_ENDPOINT_EXTENSION_FILE_PATH);
//
//                                    endpoint.setDisplayName(displayName);
//                                    endpoint.setProcessName(processName);
//                                    endpoint.setFilePath(filePath);
//                                }

                                NodeList displayNameElements = e.getElementsByTagNameNS(
                                        JBI_ENDPOINT_EXTENSION_NAMESPACE,
                                        JBI_ENDPOINT_EXTENSION_DISPLAY_NAME);
                                if (displayNameElements.getLength() == 1) {
                                    String displayName = displayNameElements.item(0).getTextContent();
                                    endpoint.setDisplayName(displayName);
                                } 
                                                                
                                NodeList processNameElements = e.getElementsByTagNameNS(
                                        JBI_ENDPOINT_EXTENSION_NAMESPACE,
                                        JBI_ENDPOINT_EXTENSION_PROCESS_NAME);
                                if (processNameElements.getLength() == 1) {
                                    String processName = processNameElements.item(0).getTextContent();
                                    endpoint.setProcessName(processName);
                                }
                                
                                NodeList filePathElements = e.getElementsByTagNameNS(
                                        JBI_ENDPOINT_EXTENSION_NAMESPACE,
                                        JBI_ENDPOINT_EXTENSION_FILE_PATH);
                                if (filePathElements.getLength() == 1) {
                                    String filePath = filePathElements.item(0).getTextContent();
                                    endpoint.setFilePath(filePath);
                                }

                                suEndpointList.add(endpoint);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return suEndpointList;

        }
        return null;
    }

    private boolean isInEndpointList(List<Endpoint> endpointList,
            QName serviceQName, String portName) {
        if (endpointList != null) {
            // TODO?: change Endpoint.equals() to ignore interface checking 
            // and use List.contains() here
            for (Endpoint endpoint : endpointList) {
                if (endpoint.getEndpointName().equals(portName) &&
                        endpoint.getServiceQName().equals(serviceQName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates a complete Ports element for a BC SU in the new casa document.
     * 
     * @param bcName  name of a BC SU, e.x., sun-jms-binding
     */
    private Element createPorts(String bcName) {
        debugLog("create Ports for binding component " + bcName);

        // Create casa ports element.
        Element ports = newCasaDocument.createElement(CASA_PORTS_ELEM_NAME);

        // Mapping fully qualified port names to casa port elements in the 
        // new casa document. (This is to solve the duplicate WSDL problem.)
        // Note that this map is scoped by each BC SU and should not be used
        // across different BC SUs!
        Map<String, Element> casaPortMap = new HashMap<String, Element>();

        List<Endpoint> suEndpoints = su2Endpoints.get(bcName);

        // 02/12/08, used to skip ports from duplicate copies of wsdl files
        Map<String, Port> portMap = new HashMap<String, Port>();

        // Loop through all WSDLs and add casa port elements of the given 
        // BC type. This includes:
        // (1) BC endpoints in the corresponding SU's jbi.xml, 
        // (2) unconnected BC endpoints from old casa that are still live, and 
        // (3) "deleted" BC endpoints from old casa that are still live.
        for (WSDLModel model : wsdlRepository.getWsdlCollection()) {
            /*
            if (wsdlRepository.isJavaEEWsdl(model)) {
            continue;
            }
             */

            String relativePath = MyFileUtil.getRelativePath(new File(confDirLoc), getFile(model));
            debugLog("    WSDL: " + relativePath);
            String tns = model.getRootComponent().getPeer().getAttribute("targetNamespace");
            //wsdlDocument.getDocumentElement().getAttribute("targetNamespace");

            // Add casa port
            for (Service s : model.getDefinitions().getServices()) {
                String serviceName = s.getName();
                QName serviceQName = new QName(tns, serviceName);
                for (Port p : s.getPorts()) {
                    String portName = p.getName();
                    debugLog("          Endpoint: " + serviceQName + ":" + portName);
                    String key = tns+":"+serviceName+":"+portName;
                    if (portMap.get(key) == null) {
                        portMap.put(key, p);

                        boolean isSUEndpoint = isInEndpointList(suEndpoints, serviceQName, portName);
                        boolean isLiveDeletedBCEndpoint = false;
                        boolean isLiveUnconnectedEndpoint = false;

                        if (isSUEndpoint) {
                            debugLog("              is a SU endpoint.");
                        } else {
                            List<Endpoint> deletedBCEndpoints = deletedBCEndpointsMap.get(bcName);
                            if (isInEndpointList(deletedBCEndpoints, serviceQName, portName) &&
                                    isInEndpointList(newWsdlEndpoints, serviceQName, portName)) {
                                isLiveDeletedBCEndpoint = true;
                                debugLog("              is a live deleted endpoint.");
                            }

                            if (!isLiveDeletedBCEndpoint) {
                                List<Endpoint> unconnectedBCEndpoints = oldUnconnectedBCEndpointsMap.get(bcName);
                                if (isInEndpointList(unconnectedBCEndpoints, serviceQName, portName) &&
                                        isInEndpointList(newWsdlEndpoints, serviceQName, portName)) {
                                    isLiveUnconnectedEndpoint = true;
                                    debugLog("              is a live unconnected endpoint.");
                                }
                            }
                        }

                        if (isSUEndpoint || isLiveDeletedBCEndpoint || isLiveUnconnectedEndpoint) {
                            // Instead of creating a new casa port every time, check
                            // if a casa port has already been created. This allows
                            // multiple (duplicate) WSDL Ports to share the same
                            // CASA Port element.
                            String fullyQualifiedPortName = serviceQName.toString() + "." + portName;
                            Element port = casaPortMap.get(fullyQualifiedPortName);
                            if (port == null) {
                                // Create new casa port
                                port = createPort(relativePath, serviceQName, portName);
                                ports.appendChild(port);
                                casaPortMap.put(fullyQualifiedPortName, port);

                                // Mark casa port as "deleted" if the old casa says so.
                                if (isLiveDeletedBCEndpoint) {
                                    port.setAttribute(CASA_STATE_ATTR_NAME, CASA_DELETED_ATTR_VALUE);
                                }
                            } else {
                                // Reuse old casa port by adding a new link
                                Element casaLinkElement = createLinkForWsdlPort(
                                        relativePath, serviceName, portName);
                                port.appendChild(casaLinkElement);
                            }
                        }
                    }
                }
            }
        }

        return ports;
    }

    /**
     * Creates a Port element for a BC SU in the new casa document.
     */
    private Element createPort(String relativePath, QName serviceQName, String portName) {
        // 1. Create casa port
        Element ret = newCasaDocument.createElement(CASA_PORT_ELEM_NAME);
        ret.setAttribute(CASA_X_ATTR_NAME, "0");
        ret.setAttribute(CASA_Y_ATTR_NAME, "-1");

        // 2. Add link. (Note that additional links might be added later.)
        Element linkElement = createLinkForWsdlPort(
                relativePath, serviceQName.getLocalPart(), portName);
        ret.appendChild(linkElement);

        // 3. Add one consumes and one provides to the casa port
        String endpointID = getNewEndpointID(serviceQName, portName);

        Element cEndpointRef = newCasaDocument.createElement(CASA_CONSUMES_ELEM_NAME);
        cEndpointRef.setAttribute(CASA_ENDPOINT_ATTR_NAME, endpointID);
        ret.appendChild(cEndpointRef);

        Element pEndpointRef = newCasaDocument.createElement(CASA_PROVIDES_ELEM_NAME);
        pEndpointRef.setAttribute(CASA_ENDPOINT_ATTR_NAME, endpointID);
        ret.appendChild(pEndpointRef);

        return ret;
    }

    /**
     * Creates a Link element in the new casa document for a WSDL port.
     */
    private Element createLinkForWsdlPort(String relativePath,
            String serviceName, String portName) {
        String uri = "/definitions/service[@name='" + serviceName +
                "']/port[@name='" + portName + "']";
        return createLink(relativePath, uri);
    }

    /**
     * Creates a Link element in the new casa document.
     */
    private Element createLink(String relativePath, String uri) {
        Element ret = newCasaDocument.createElement(CASA_LINK_ELEM_NAME);
        ret.setAttribute(XLINK_NAMESPACE_PREFIX + ":type", "simple");
        ret.setAttribute(
                XLINK_NAMESPACE_PREFIX + ":href",
                relativePath + "#xpointer(" + uri + ")");
        return ret;
    }

    private void log(String msg) {
        task.log(msg);
    }

    private void debugLog(String msg) {
        task.log(msg, Project.MSG_DEBUG);
    }

    private boolean isExternalServiceUnit(String suName) {
        return externalSuNames.contains(suName);
    }
}
