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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.projects.jbi.CasaConstants;
import org.netbeans.modules.compapp.projects.jbi.JbiConstants;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.ConnectionContainer;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Endpoint;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Connection;
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

/**
 *
 * @author jqian
 */
public class CasaBuilder implements JbiConstants, CasaConstants {
    
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
        
    // mapping binding component namespace to binding component name,
    // e.x., 
    private Map<String, String> bcNamespace2NameMap;
        
    // mapping SE/BC SU name to endpoints defined in the SU's jbi.xml
    private Map<String, List<Endpoint>> su2Endpoints =
                new HashMap<String, List<Endpoint>>();  
        
    // A Map mapping fully qualified endpoint name to endpoint ID for all 
    // the endpoints in the new casa document.
    private Map<String, String> newEndpointMap = new HashMap<String, String>();
        
    // index used for creating endpoint IDs in the new casa document
    private int endpointIndex = 1;
    
    // a list of endpoints defined in all the WSDL files in the compapp and
    // its component projects
    private List<Endpoint> newWSDLEndpoints;
        
    // mapping BC name to list of deleted endpoints for that BC type in the 
    // old casa document
    private Map<String, List<Endpoint>> deletedBCEndpointsMap =
            new HashMap<String, List<Endpoint>>();
      
    // mapping BC name to a list of unconnected endpoints in the old casa document
    private Map<String, List<Endpoint>> oldUnconnectedBCEndpointsMap;
    
    private String serviceUnitsDirLoc;
    private String confDirLoc;
    
    private Project project;
    private wsdlRepository wsdlRepository;
    private Task task;
    private Document oldCasaDocument;
    private Document newCasaDocument;
        
    public CasaBuilder(Project project, wsdlRepository wsdlRepository, 
            Task task, String casaFileLoc) {
        
        this.project = project;
        this.wsdlRepository = wsdlRepository;
        this.task = task;
        
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
        
        String projPath = project.getProperty("basedir") + File.separator;
        serviceUnitsDirLoc = projPath + "src" + File.separator + "jbiServiceUnits";
        confDirLoc = projPath + "src" + File.separator + "conf";
    }
    
    /**
     * Creates a new CASA document based on sa jbi.xml and optionally 
     * an old CASA file.
     * 
     * @param jbiDocument   service assembly JBI document
     * @return              new CASA document
     */
    public Document createCasaDocument(Document jbiDocument)
    throws ParserConfigurationException, SAXException, IOException {      
        
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        
        DocumentBuilder builder = factory.newDocumentBuilder();        
        newCasaDocument = builder.newDocument();
        
        Element casaRoot = newCasaDocument.createElement("casa");
        newCasaDocument.appendChild(casaRoot);
        casaRoot.setAttribute("xmlns", "http://java.sun.com/xml/ns/casa");        
        casaRoot.setAttribute("xmlns:" + XLINK_NAMESPACE_PREFIX, XLINK_NAMESPACE_URI);
        
        // build binding component namespace to ID map
        bcNamespace2NameMap = wsdlRepository.buildBindingComponentMap(project);
            
        // Prepare various endpoint lists    
        newWSDLEndpoints = getNewWsdlEndpoints(jbiDocument);        
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
                          
        mergeLocations(jbiDocument);
        
        return newCasaDocument;
    }
        
    /**
     * Gets an Endpoint object from an endpoint element in CASA DOM.
     */
    private static Endpoint getEndpoint(Element endpointElement) {
        String endpointName = 
                endpointElement.getAttribute(CASA_ENDPOINT_NAME_ATTR_NAME);
        QName serviceQName = XmlUtil.getAttributeNSName(
                endpointElement, CASA_SERVICE_NAME_ATTR_NAME);
        QName interfaceQName = XmlUtil.getAttributeNSName(
                endpointElement, CASA_INTERFACE_NAME_ATTR_NAME);
        
        return new Endpoint(endpointName, serviceQName, interfaceQName); 
    }
    
    /**
     * Creates the service-units element in the new casa document.
     */
    private Element createSUs(Document jbiDocument)
            throws SAXException, IOException, ParserConfigurationException {
        
        Element casaSUs = newCasaDocument.createElement(CASA_SERVICE_UNITS_ELEM_NAME);
        
        // 1. "Copy" SE and BC SUs from jbi document over
        List<String> componentIDs = new ArrayList<String>();
        NodeList jbiSUs = 
                jbiDocument.getElementsByTagName(JBI_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < jbiSUs.getLength(); i++) {
            Element jbiSU = (Element) jbiSUs.item(i);
            String componentID = getJBIServiceUnitComponentName(jbiSU);
            componentIDs.add(componentID);
            
            Element casaSU = bcNamespace2NameMap.values().contains(componentID) ?
                createBCSUFromJbiElement(jbiSU) :
                createSESUFromJbiElement(jbiSU);

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
            
            // 3. Merge external SE SUs from old casa
            try {
                List<Element> externalSESUs = getExternalSESUs();
                for (Element oldSESU : externalSESUs) {
                    Node newSESU = deepCloneCasaNodeWithEndpointConversion(oldSESU);
                    casaSUs.appendChild(newSESU);
                }
            } catch (Exception e) {
                log("ERROR: Problem merging external service units from old casa: " + e +
                        ". This does not affect regular compapp build." );
            }
        }
        
        return casaSUs;
    }
    
    private Element deepCloneCasaNodeWithEndpointConversion(Element oldElement) {
        
        Element newElement = (Element) deepCloneCasaNode(oldElement, newCasaDocument);
        
        NodeList oldConsumesNodeList = oldElement.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
        NodeList newConsumesNodeList = newElement.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);        
        for (int i = 0; i < oldConsumesNodeList.getLength(); i++) {
            Element oldConsumes = (Element) oldConsumesNodeList.item(i);
            Element newConsumes = (Element) newConsumesNodeList.item(i);
            fixEndpointID(oldConsumes, newConsumes);
        }
        
        NodeList oldProvidesNodeList = oldElement.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
        NodeList newProvidesNodeList = newElement.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
        for (int i = 0; i < oldProvidesNodeList.getLength(); i++) {
            Element oldProvides = (Element) oldProvidesNodeList.item(i);
            Element newProvides = (Element) newProvidesNodeList.item(i);
            fixEndpointID(oldProvides, newProvides);
        }
        
        return newElement;
    }        
         
    /**
     * Fixes the ID attribute of an Endpoint element in the new casa document 
     * so that both the Endpoint element in the new casa document and the 
     * Endpoint element in the old casa document refer to the same fully 
     * qualified endpoint.
     * 
     * @param oldEndpoint an Endpoint element in the old casa document
     * @param newEndpoint an Endpoint element in the new casa document
     */
    private void fixEndpointID(Element oldEndpoint, Element newEndpoint) {
        String oldEndpointID = oldEndpoint.getAttribute(CASA_ENDPOINT_ATTR_NAME);
        Endpoint endpoint = getEndpoint(oldCasaDocument, oldEndpointID);
        String newEndpointID = addEndpoint(endpoint);     
        newEndpoint.setAttribute(CASA_ENDPOINT_ATTR_NAME, newEndpointID);
    }
    
    /**
     * Gets a list of binding component service units in the old casa document
     * whose ports are all marked as "deleted".
     */ /*
    private List<Element> getGhostBindingComponentServiceUnitElements() {
        List<Element> ret = new ArrayList<Element>();
        
        NodeList bcSUs = oldCasaDocument.getElementsByTagName(
                        CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < bcSUs.getLength(); i++) {
            Element bcSU = (Element) bcSUs.item(i);
            NodeList portNodeList = bcSU.getElementsByTagName(CASA_PORT_ELEM_NAME);
            boolean ghost = true;
            for (int j = 0; j < portNodeList.getLength(); j++) {
                Element port = (Element) portNodeList.item(j);
                String state = port.getAttribute(CASA_STATE_ATTR_NAME);
                if (!CASA_DELETED_ATTR_VALUE.equals(state)) {
                    ghost = false;
                    break;
                }
            }
            
            if (ghost) {
                ret.add(bcSU);
            }
        }
        
        return ret;
    }
    */
    
    /*
    private List<String> getOldBindingComponentNames() {
        List<String> ret = new ArrayList<String>();
        
        NodeList bcSUs = oldCasaDocument.getElementsByTagName(
                        CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < bcSUs.getLength(); i++) {
            Element bcSU = (Element) bcSUs.item(i);
            String bcName = bcSU.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME);
            ret.add(bcName);
        }
        
        return ret;
    }
    */

    /**
     * Creates a BC SU element in the new casa document from a BC SU element 
     * in the jbi document.
     */
    private Element createBCSUFromJbiElement(Element jbiSU)
            throws SAXException, IOException, ParserConfigurationException {

        Element bcSU = newCasaDocument.createElement(CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
        String suName = getJBIServiceUnitName(jbiSU);

        Element identification = (Element) jbiSU.getElementsByTagName(JBI_IDENTIFICATION_ELEM_NAME).item(0);
        String name = ((Element) identification.getElementsByTagName(JBI_NAME_ELEM_NAME).item(0)).
                getFirstChild().getNodeValue();
        String description = ((Element) identification.getElementsByTagName(JBI_DESCRIPTION_ELEM_NAME).item(0)).
                getFirstChild().getNodeValue();

        Element target = (Element) jbiSU.getElementsByTagName(JBI_TARGET_ELEM_NAME).item(0);
        String componentName = ((Element) target.getElementsByTagName(JBI_COMPONENT_NAME_ELEM_NAME).item(0)).
                getFirstChild().getNodeValue();
        String artifactsZip = ((Element) target.getElementsByTagName(JBI_ARTIFACTS_ZIP_ELEM_NAME).item(0)).
                getFirstChild().getNodeValue();

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
        String suName = getJBIServiceUnitName(jbiSU);

        List<Endpoint> suEndpointList = su2Endpoints.get(suName); 
        if (suEndpointList == null || suEndpointList.size() == 0) {
            log("ERROR: Invalid service unit name in service assembly jbi.xml: " + suName);
            return null;
        }
        
        Element identification = (Element) jbiSU.getElementsByTagName(JBI_IDENTIFICATION_ELEM_NAME).item(0);
        String name = ((Element) identification.getElementsByTagName(JBI_NAME_ELEM_NAME).item(0)).
                getFirstChild().getNodeValue();
        String description = ((Element) identification.getElementsByTagName(JBI_DESCRIPTION_ELEM_NAME).item(0)).
                getFirstChild().getNodeValue();

        Element target = (Element) jbiSU.getElementsByTagName(JBI_TARGET_ELEM_NAME).item(0);
        String componentName = ((Element) target.getElementsByTagName(JBI_COMPONENT_NAME_ELEM_NAME).item(0)).
                getFirstChild().getNodeValue();
        String artifactsZip = ((Element) target.getElementsByTagName(JBI_ARTIFACTS_ZIP_ELEM_NAME).item(0)).
                getFirstChild().getNodeValue();

        seSU.setAttribute(CASA_X_ATTR_NAME, "-1");
        seSU.setAttribute(CASA_Y_ATTR_NAME, "-1");

        seSU.setAttribute(CASA_INTERNAL_ATTR_NAME, "true");
        seSU.setAttribute(CASA_DEFINED_ATTR_NAME, "true");
        seSU.setAttribute(CASA_UNKNOWN_ATTR_NAME, "false");
        seSU.setAttribute(CASA_NAME_ATTR_NAME, name);
        seSU.setAttribute(CASA_UNIT_NAME_ATTR_NAME, suName);
        seSU.setAttribute(CASA_COMPONENT_NAME_ATTR_NAME, componentName);
        seSU.setAttribute(CASA_DESCRIPTION_ATTR_NAME, description);
        seSU.setAttribute(CASA_ARTIFACTS_ZIP_ATTR_NAME, artifactsZip);
        
        for (Endpoint endpoint : suEndpointList) {
            Element endpointRef = endpoint.isConsumes() ?
                (Element) newCasaDocument.createElement(CASA_CONSUMES_ELEM_NAME) :
                (Element) newCasaDocument.createElement(CASA_PROVIDES_ELEM_NAME);
            String endpointID = getNewEndpointID(endpoint);
            endpointRef.setAttribute(CASA_ENDPOINT_ATTR_NAME, endpointID);
            seSU.appendChild(endpointRef);
        }
        return seSU;
    }

    /**
     * Creates the Connections element in the new casa document.
     */
    private Element createConnections(Document jbiDocument) {

        Element casaConnections = newCasaDocument.createElement(CASA_CONNECTIONS_ELEM_NAME);

        // 1. Copy connections from jbi document over.
        NodeList jbiConnections =
                jbiDocument.getElementsByTagName(JBI_CONNECTION_ELEM_NAME);

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
                            newConnection.setAttribute(CASA_STATE_ATTR_NAME, CASA_NEW_ATTR_VALUE);
                        }
                    } 
                }
            } catch (Exception e) {
                log("ERROR: Problem merging deleted/new connections from old casa: " + e +
                        ". This does not affect regular compapp build." );
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
                        ". This does not affect regular compapp build." );
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
        return compName.getFirstChild().getNodeValue();
    }

    private String getJBIServiceUnitName(Element jbiSU) {
        Element target = (Element) jbiSU.getElementsByTagName(JBI_TARGET_ELEM_NAME).item(0);
        Element artifactsZip = (Element) target.getElementsByTagName(JBI_ARTIFACTS_ZIP_ELEM_NAME).item(0);
        String zipFileName = artifactsZip.getFirstChild().getNodeValue();
        assert zipFileName.endsWith(".jar");
        return zipFileName.substring(0, zipFileName.length() - 4);
    }

    /*
    private Endpoint getEndpoint(List<Element> oldEndpointList, String name) {
        if (name == null) {
            return null;
        }
        for (Element oldEndpoint : oldEndpointList) {
            String oldName = oldEndpoint.getAttribute(CASA_NAME_ATTR_NAME);
            if ((oldName != null) && oldName.equalsIgnoreCase(name)) {
                String oldEndpointName = oldEndpoint.getAttribute(
                        CASA_ENDPOINT_NAME_ATTR_NAME);
                QName oldServiceQName = XmlUtil.getAttributeNSName(
                        oldEndpoint, CASA_SERVICE_NAME_ATTR_NAME);
                QName oldInterfaceQName = XmlUtil.getAttributeNSName(
                        oldEndpoint, CASA_INTERFACE_NAME_ATTR_NAME);
                Endpoint p = new Endpoint(//"either",
                        oldEndpointName, oldServiceQName, oldInterfaceQName);
                return p;
            }
        }
        return null;
    }
    */

    /**
     * Maps endpoint name to binding component name.
     */
    private Map<String, String> hashBcNames(NodeList bcList) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < bcList.getLength(); i++) {
            Element bc = (Element) bcList.item(i);
            String bcName = bc.getAttribute(CASA_UNIT_NAME_ATTR_NAME);
            NodeList ports = bc.getElementsByTagName(CASA_PORTS_ELEM_NAME);
            for (int j = 0; j < ports.getLength(); j++) {
                Element port = (Element) ports.item(j);
                NodeList cons = bc.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
                for (int k = 0; k < cons.getLength(); k++) {
                    Element con = (Element) cons.item(k);
                    String endpoint = con.getAttribute(CASA_ENDPOINT_ELEM_NAME);
                    map.put(endpoint, bcName);
                }
                NodeList pros = bc.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
                for (int k = 0; k < pros.getLength(); k++) {
                    Element pro = (Element) pros.item(k);
                    String endpoint = pro.getAttribute(CASA_ENDPOINT_ELEM_NAME);
                    map.put(endpoint, bcName);
                }
            }
        }
        return map;
    }

    private void updateBcNameList(String bcName, Connection connection,
            Map<String, List[]> bcConnections, boolean isConsumes) {
        if (bcName != null) {
            Object cmap = bcConnections.get(bcName);
            ArrayList[] clist = new ArrayList[2];
            if (cmap != null) {
                clist = ((ArrayList[]) cmap);
            } else {
                clist[0] = new ArrayList(); // port on consume...
                clist[1] = new ArrayList(); // port on provide...
            }
            if (isConsumes) {
                clist[0].add(connection);
            } else {
                clist[1].add(connection);
            }
            bcConnections.put(bcName, clist);
        }
    }
    
    public void mergeCasaConnection(ConnectionContainer cc,
            Map<String, List[]> bcConnections) {
                
        if (oldCasaDocument == null) {
            return;
        }
        
        try {
            Element oldConnections = (Element) oldCasaDocument.getElementsByTagName(
                    CASA_CONNECTIONS_ELEM_NAME).item(0);
            NodeList oldConnectionList = oldConnections.getElementsByTagName(
                    CASA_CONNECTION_ELEM_NAME);
            Element sus = (Element) oldCasaDocument.getElementsByTagName(
                    CASA_SERVICE_UNITS_ELEM_NAME).item(0);
            
            if (sus == null) {
                log("WARNING: Old version of casa format is not supported.");
                return;
            }
            
            NodeList bcList = sus.getElementsByTagName(
                    CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
            
            Map<String, String> endpoint2BCName = hashBcNames(bcList);
            List glist = cc.getConnectionList();
            List nlist = new ArrayList(); // new list
            List rlist = new ArrayList(); // remove list
            for (int i = oldConnectionList.getLength() - 1; i >= 0; i--) {
                Element oldConnection = (Element) oldConnectionList.item(i);
                String oldConnectionState = oldConnection.getAttribute(CASA_STATE_ATTR_NAME);
                
                if (oldConnectionState.equals(CASA_DELETED_ATTR_VALUE)) {
                    // Remove user deleted connections.
                    String cname = oldConnection.getAttribute(CASA_CONSUMER_ATTR_NAME);
                    String pname = oldConnection.getAttribute(CASA_PROVIDER_ATTR_NAME);
                    Endpoint c = getEndpoint(oldCasaDocument, cname); 
                    Endpoint p = getEndpoint(oldCasaDocument, pname);
                    if ((c != null) && (p != null)) {
                        boolean inList = false;
                        Iterator iterator = glist.iterator();
                        while ((iterator != null) && (iterator.hasNext() == true) && !inList) {
                            Connection con = (Connection) iterator.next();
                            if (con != null) {
                                if (c.equals(con.getConsume()) && p.equals(con.getProvide())) {
                                    // remove this...
                                    inList = true;
                                    rlist.add(con);
                                }
                            }
                        }
                    }
                } else { // if (oldConnectionState.equals(CASA_NEW_ATTR_VALUE)) {
                    // Add user created connections.
                    String cname = oldConnection.getAttribute(CASA_CONSUMER_ATTR_NAME);
                    String pname = oldConnection.getAttribute(CASA_PROVIDER_ATTR_NAME);
                    Endpoint c = getEndpoint(oldCasaDocument, cname); 
                    Endpoint p = getEndpoint(oldCasaDocument, pname); 
                    if ((c != null) && (p != null)) {
                        // try to find it in the generated CompApp jbixml list
                        boolean inList = false;
                        Iterator iterator = glist.iterator();
                        while ((iterator != null) && (iterator.hasNext() == true) && !inList) {
                            Connection con = (Connection) iterator.next();
                            if (con != null) {
                                if (c.equals(con.getConsume()) && p.equals(con.getProvide())) {
                                    // skip alread added...
                                    inList = true;
                                }
                            }
                        }
                        if (!inList) { // add the connection..
                            Connection connection = new Connection(c, p);
                            nlist.add(connection);
                            
                            // update bc list..
                            String cBcName = endpoint2BCName.get(cname);
                            String pBcName = endpoint2BCName.get(pname);
                            updateBcNameList(cBcName, connection, bcConnections, true);
                            updateBcNameList(pBcName, connection, bcConnections, false);
                        }
                    }
                }
            }
            
            if (rlist.size() > 0) {
                glist.removeAll(rlist);
            }
            if (nlist.size() > 0) {
                glist.addAll(nlist);
            }
            cc.setConnectionList(glist);
            
            // clear deleted binding component endpoints
            List<Endpoint> deletedBCEndpoints = getDeletedBCEndpoints();
            for (String bcsuName : bcConnections.keySet()) {
                List<Connection>[] clist = bcConnections.get(bcsuName);
                
                for (int i = clist[0].size() - 1; i >= 0; i--) {
                    Connection connection = clist[0].get(i);
                    Endpoint consumes = connection.getConsume();
                    for (Endpoint e : deletedBCEndpoints) {
                        if (e.equals(consumes)) {
                            clist[0].remove(connection);
                            break;
                        }
                    }
                }
                for (int i = clist[1].size() - 1; i >= 0; i--) {
                    Connection connection = clist[1].get(i);
                    Endpoint provides = connection.getProvide();
                    for (Endpoint e : deletedBCEndpoints) {
                        if (e.equals(provides)) {
                            clist[1].remove(connection);
                            break;
                        }
                    }
                }
                
                if (clist[0].size() == 0 && clist[1].size() == 0) {
                    bcConnections.remove(bcsuName);
                }
            }
            
        } catch (Exception e) {
            log("ERROR: Problem merging old casa connections: " + e);
        }
    }

    /*
    private WSDLComponent getWSDLComponentFromXLinkHref(String linkHref) {

        WSDLComponent wsdlComponent = null;

        String regex = "(.*)#xpointer\\((.*)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(linkHref);

        if (! matcher.matches()) {
            throw new IllegalArgumentException("Invalid xlink href: " + linkHref);
        }

        String uriString = matcher.group(1);
        String xpathString = matcher.group(2);

        WSDLModel wsdlModel = getWSDLModel(uriString);

        if (wsdlModel != null) {
            try {
                WSDLComponent root = wsdlModel.getRootComponent();
                wsdlComponent =
                        new FindWSDLComponent().findComponent(root, xpathString);
            } catch (Exception ex) {
                // Readonly model doesn't support xpath searching.

                // FIXME: only implemented for service/port

                regex = "/definitions/service\\[@name='(.*)'\\]/port\\[@name='(.*)'\\]";
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(xpathString);
                if (! matcher.matches()) {
                    throw new IllegalArgumentException("Invalid xpathString: " + xpathString);
                }

                String serviceName = matcher.group(1);
                String portName = matcher.group(2);

                Definitions defs = wsdlModel.getDefinitions();
                for (Service service : defs.getServices()) {
                    if (service.getName().equals(serviceName)) {
                        for (Port port : service.getPorts()) {
                            if (port.getName().equals(portName)) {
                                return port;
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("WARNING: WSDL model for " + uriString + " is not available.");
        }

        return wsdlComponent;
    }
    */
    
    private static File getFile(WSDLModel model) {
        Lookup lookup = model.getModelSource().getLookup();
        File f = (File) lookup.lookup(File.class);
        if (f == null) {
            FileObject fo = (FileObject) lookup.lookup(FileObject.class);
            f = FileUtil.toFile(fo);
        }
        return f;
    }

    /*
    private String getFilePath(WSDLModel model) {
        File f = getFile(model);
        return f.getPath().replaceAll("\\\\", "/");
    }
    */
    
    /*
    private WSDLModel getWSDLModel(String uriString) {
         List<WSDLModel> models = wsdlRepository.getWsdlCollection();
         for (WSDLModel model : models) {
             String fPath = getFilePath(model);
             //System.out.println("uriString: " + uriString + "=>" + wsdlFilePath);
             if (fPath.endsWith(uriString.substring(3))) {
                 return model;
             }
         }
         return null;
    }
    */
    
    /**
     * Gets a list of endpoints that are marked as "deleted" in the old CASA
     * document whose owning WSDL files are still in the compapp.
     */ /*
    private List<Endpoint> getLiveDeletedBCEndpoints() // RENAME ME
            throws DOMException {
        List<Endpoint> ret = new ArrayList<Endpoint>();
        
        List<Endpoint> deletedBCEndpoints = getDeletedBCEndpoints();
        
        for (Endpoint deletedEndpoint : deletedBCEndpoints) {
            for (Endpoint wsdlEndpoint : newWSDLEndpoints) {
                if (deletedEndpoint.equals(wsdlEndpoint)) {
                    ret.add(deletedEndpoint);
                    break;
                }
            }
        }
        
        return ret;
    }
    */
        
    /**
     * Gets a non-null list of deleted BC endpoints in the old casa document.
     */
    // Side Effect: also build deltedBCEndpointsMap
    private List<Endpoint> getDeletedBCEndpoints() {
         
         List<Endpoint> ret = new ArrayList<Endpoint>();
         
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
                         ret.add(endpoint);
                         
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
         
         return ret;
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
                             
                             Endpoint endpoint = getEndpoint(endpointElement);
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
     * Gets a non-null list of endpoints from the old casa document. 
     * (The external endpoints in the new casa document will be the same.)
     */
    private List<Endpoint> getExternalEndpoints() {
         
         List<Endpoint> endpointList = new ArrayList<Endpoint>();
         
         List<Element> sesus = getExternalSESUs();
         for (Element sesu : sesus) {
             NodeList consumesNodeList =
                     sesu.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
             for (int j = 0; j < consumesNodeList.getLength(); j++) {
                 Element consumes = (Element) consumesNodeList.item(j);
                 String endpointID = consumes.getAttribute(CASA_ENDPOINT_ATTR_NAME);
                 Endpoint endpoint = getEndpoint(oldCasaDocument, endpointID);
                 endpointList.add(endpoint);
             }
             NodeList providesNodeList =
                     sesu.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
             for (int j = 0; j < providesNodeList.getLength(); j++) {
                 Element provides = (Element) providesNodeList.item(j);
                 String endpointID = provides.getAttribute(CASA_ENDPOINT_ATTR_NAME);
                 Endpoint endpoint = getEndpoint(oldCasaDocument, endpointID);
                 endpointList.add(endpoint);
             }
         }

         return endpointList;
    }
    
    /*
    private boolean containsEndpoint(List<Endpoint> endpointList, Endpoint endpoint) {
        for (Endpoint e : endpointList) {
            if (e.equals(endpoint)) {
                return true;
            }
        }
        return false;
    }
    */
    
    /*
    private List<Element> getValidExternalEndpointList() throws DOMException { // TODO: renameme

        List<Element> validExternalEndpointList = new ArrayList<Element>();

        NodeList oldEndpointList =
                oldCasaDocument.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);

        List<Element> pcList = new ArrayList<Element>();

        NodeList cList = oldCasaDocument.getElementsByTagName(CASA_CONSUMES_ELEM_NAME);
        for (int j = 0; j < cList.getLength(); j++) {
            pcList.add((Element)cList.item(j));
        }
        NodeList pList = oldCasaDocument.getElementsByTagName(CASA_PROVIDES_ELEM_NAME);
        for (int j = 0; j < pList.getLength(); j++) {
            pcList.add((Element)pList.item(j));
        }

        for (int i = 0; i < oldEndpointList.getLength(); i++) {
            Element oldEndpoint = (Element) oldEndpointList.item(i);
            String name = oldEndpoint.getAttribute(CASA_NAME_ATTR_NAME);
            String endpointName =
                    oldEndpoint.getAttribute(CASA_ENDPOINT_NAME_ATTR_NAME);
            for (Element pc : pcList) {
                if (pc.getAttribute(CASA_ENDPOINT_ATTR_NAME).equals(name)) {
                    Element parent = (Element) pc.getParentNode();
                    if (parent.getNodeName().equals(CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME) &&
                            parent.getAttribute(CASA_INTERNAL_ATTR_NAME).equals("false")) {
                        validExternalEndpointList.add(oldEndpoint);
                    }
                    break;
                }
            }
        }

        return validExternalEndpointList;
    }
    */
    
    private void mergeLocations(Document jbiDocument) {
        
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
        String oldEndpointID = isConsumes ?
            oldConnection.getAttribute(CASA_CONSUMER_ATTR_NAME) :
            oldConnection.getAttribute(CASA_PROVIDER_ATTR_NAME);

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

    /*
    private Element getConnectionEndpoint(Element connection, boolean consumer) {
        String elementName = consumer? JBI_CONSUMER_ELEM_NAME : JBI_PROVIDER_ELEM_NAME;
        NodeList endpoints = connection.getElementsByTagName(elementName);
        assert endpoints != null && endpoints.getLength() == 1;
        return (Element) endpoints.item(0);
    }
    */
    
    /**
     * Deep clones a node.
     */ /*
    private static Node deepCloneJBINode(final Node node, final Document targetDocument, boolean jbiOnly)
    throws DOMException {
        String nodeName = node.getNodeName();

        Node clonedNode = null;

        if (node instanceof Element) {
            clonedNode = targetDocument.createElement(nodeName);

            NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attrNode = attrs.item(i);
                String name = attrNode.getNodeName();
                String value = attrNode.getNodeValue();

                if (!jbiOnly || !name.startsWith("casa:")) {

                    // side effect: updating target document's namespaces
                    if (value.indexOf(":") != -1) {
                        //System.out.println("deep cloning " + value);
                        String valuePrefix = value.substring(0, value.indexOf(":"));
                        String valueLocal = value.substring(value.indexOf(":") + 1);
                        String oldNamespaceURI = XmlUtil.getNamespaceURI((Element)node, valuePrefix); // REFACTOR ME

                        String newNamespaceURI = targetDocument.getDocumentElement().getAttribute("xmlns:" + valuePrefix);
                        //System.out.println("  getMyNamespace: " + valuePrefix + " => oldNamespace=" + oldNamespace + "  newNamespace=" + newNamespace);

                        if (newNamespaceURI == null || newNamespaceURI.equals("")) {
                            targetDocument.getDocumentElement().setAttribute("xmlns:" + valuePrefix, oldNamespaceURI);
                        } else if (newNamespaceURI.equals(oldNamespaceURI)) {
                            ;
                        } else {
                            System.out.println("throw new RuntimeException(not implemented...)");
                        }
                    }

                    ((Element)clonedNode).setAttribute(name, value);
                }
            }

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (!(child instanceof Element) ||
                        (!jbiOnly || ! child.getNodeName().startsWith("casa:"))) {
                    Node clonedChild = deepCloneJBINode(child, targetDocument, jbiOnly);
                    clonedNode.appendChild(clonedChild);
                }
            }
        } else if (node instanceof Text) {
            clonedNode = targetDocument.createTextNode(((Text)node).getWholeText());
        } else {
            assert false : "deep clone node of type " + node.getClass().getName() + " is not implemented yet.";
        }

        return clonedNode;
    }*/

    private static Node deepCloneCasaNode(final Node node, final Document targetDocument)
    throws DOMException {
        String nodeName = node.getNodeName();

        Node clonedNode = null;

        if (node instanceof Element) {
            clonedNode = targetDocument.createElement(nodeName);

            NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attrNode = attrs.item(i);
                String name = attrNode.getNodeName();
                String value = attrNode.getNodeValue();

                // side effect: updating target document's namespaces
                if (value.indexOf(":") != -1) {
                    //System.out.println("deep cloning " + value);
                    String valuePrefix = value.substring(0, value.indexOf(":"));
                    String valueLocal = value.substring(value.indexOf(":") + 1);
                    String oldNamespaceURI = XmlUtil.getNamespaceURI((Element)node, valuePrefix); // REFACTOR ME

                    String newNamespaceURI = targetDocument.getDocumentElement().getAttribute("xmlns:" + valuePrefix);
                    //System.out.println("  getMyNamespace: " + valuePrefix + " => oldNamespace=" + oldNamespace + "  newNamespace=" + newNamespace);

                    if (newNamespaceURI == null || newNamespaceURI.equals("")) {
                        targetDocument.getDocumentElement().setAttribute("xmlns:" + valuePrefix, oldNamespaceURI);
                    } else if (newNamespaceURI.equals(oldNamespaceURI)) {
                        ;
                    } else {
                        System.out.println("throw new RuntimeException(not implemented...)");
                    }
                }

                ((Element)clonedNode).setAttribute(name, value);
            }

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                Node clonedChild = deepCloneCasaNode(child, targetDocument);
                clonedNode.appendChild(clonedChild);
            }
        } else if (node instanceof Text) {
            clonedNode = targetDocument.createTextNode(((Text)node).getWholeText());
        } else {
            assert false : "deep clone node of type " + node.getClass().getName() + " is not implemented yet.";
        }

        return clonedNode;
    }

    /**
     * Finds a service engine service unit element from a list of service unit elements.
     */ /*
    private Element findServiceEngineServiceUnit(NodeList serviceUnitList, Element serviceUnit) {

        if (serviceUnit.hasAttribute(CASA_NAME_ATTR_NAME)) {
            String serviceUnitName = serviceUnit.getAttribute(CASA_NAME_ATTR_NAME);

            for (int j = 0; j < serviceUnitList.getLength(); j++) {
                Element myServiceUnit = (Element) serviceUnitList.item(j);
                if (myServiceUnit.hasAttribute(CASA_NAME_ATTR_NAME)) {
                    String myServiceUnitName = myServiceUnit.getAttribute(CASA_NAME_ATTR_NAME);
                    if (serviceUnitName.equals(myServiceUnitName)) {
                        return myServiceUnit;
                    }
                }
            }
        } else {
            return findLinkContainerElement(serviceUnitList, serviceUnit);
        }

        return null;
    }*/


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
//    /** 
//     * Builds a mapping from SE/BC SU name to the endpoint list found 
//     * in SU's jbi.xml under jbiServiceUnits/${suName}/.
//     * All the endpoints in this map will definitely go into the new CASA file.
//     * Other endpoints that should go into the new CASA file include: 
//     * (1) User-deleted endpoints coming from component projects (so that we
//     * can persist the deletion)
//     * 
//     * There are three types of the endpoints in the new CASA document:
//     * (1) the newly generated endpoints in jbiServiceUnits/${suName}/jbi.xml;
//     * (2) the (unconnected) endpoints in the old CASA document if the owning
//     *     WSDL file is still part of compapp;
//     * (3) the "deleted" endpoints from old CASA document if the owning WSDL
//     *     file is still part of compapp.
//     */
//    private void buildServiceUnitEndpointsMap(Document jbiDocument)
//    throws SAXException, IOException, ParserConfigurationException {
//
//        NodeList jbiSUs =
//                jbiDocument.getElementsByTagName(JBI_SERVICE_UNIT_ELEM_NAME);
//        for (int i = 0; i < jbiSUs.getLength(); i++) {
//            Element jbiSU = (Element) jbiSUs.item(i);
//            String suName = getJBIServiceUnitName(jbiSU);
//            List<Endpoint> suEndpoints = loadSUEndpoints(suName);
//            suEndpointsMap.put(suName, suEndpoints);
//        }
//    }

    /*
    // We need to preserve unconnected (typed or untyped) WSDL ports defined
    // in casa wsdl with compapp rebuild.
    private void preserveCasaWSDLEndpointsAndPorts() {
        if (oldCasaDocument == null) {
            return;
        }
        
        String casaWSDLFileName = getCompAppWSDLFileName();

        Element newServiceUnits = (Element) newCasaDocument.getElementsByTagName(CASA_SERVICE_UNITS_ELEM_NAME).item(0);

        NodeList oldPortList = oldCasaDocument.getElementsByTagName(CASA_PORT_ELEM_NAME);
        for (int i = 0; i < oldPortList.getLength(); i++) {
            Element oldPort = (Element) oldPortList.item(i);
            // If the port is defined in casa wsdl, then check if the endpoint
            // is in suEndpointList. If not, we need to duplicate the port and
            // add it into the new casa document.
            Element oldPortLink = (Element) oldPort.getElementsByTagName(CASA_LINK_ELEM_NAME).item(0);
            String oldPortLinkHref = oldPortLink.getAttributeNS(
                    XLINK_NAMESPACE_URI, XLINK_HREF_ATTR_NAME);
            if (!oldPortLinkHref.startsWith("../jbiasa/" + casaWSDLFileName)) {
                continue;
            }

            // Adding port into new casa document
            Element oldBCSU = (Element) oldPort.getParentNode().getParentNode();
            String componentName = oldBCSU.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME);
            Element newBCSU = getBindingComponentServiceUnit(newCasaDocument, componentName);

            Element newPort = null;
            if (newBCSU == null) {
                // The bc type doesn't exist yet, we need to clone the old BC SU.
                newBCSU = deepCloneCasaNodeWithEndpointConversion(oldBCSU);
                newServiceUnits.appendChild(newBCSU);
            } else {
                boolean portExists = isPortLinkInBindingComponentServiceUnit(
                        newBCSU, oldPortLinkHref);

                if (!portExists) {
                    // The port doesn't exist yet, we need to clone the port.
                    Node clonedPort = deepCloneCasaNodeWithEndpointConversion(oldPort);
                    Node ports = newBCSU.getElementsByTagName(CASA_PORTS_ELEM_NAME).item(0);
                    ports.appendChild(clonedPort);
                }
            }
        }
    }
    */

    /*
    private boolean isPortLinkInBindingComponentServiceUnit(Element bcSU,
            String portLinkHref) {
        Element ports = (Element) bcSU.getElementsByTagName(CASA_PORTS_ELEM_NAME).item(0);
        if (ports != null) {
            NodeList links = ports.getElementsByTagName(CASA_LINK_ELEM_NAME);

            for (int k = 0; k < links.getLength(); k++) {
                Element link = (Element) links.item(k);
//                String linkHref = link.getAttributeNS(
//                        XLINK_NAMESPACE_URI, XLINK_HREF_ATTR_NAME);
                String linkHref = link.getAttribute("xlink:href"); // TMP FIXME

                if (linkHref.equals(portLinkHref)) {
                    return true;
                }
            }
        }
        return false;
    }
    */
    
    /*
    private static Element getBindingComponentServiceUnit(Document casaDocument,
            String bcName) {
        NodeList bcSUs = casaDocument.getElementsByTagName(
                CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < bcSUs.getLength(); i++) {
            Element bcSU = (Element) bcSUs.item(i);
            if (bcSU.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME).equals(bcName)) {
                return bcSU;
            }
        }
        return null;
    }
    */
    
    /**
     * Gets the endpoint object in a casa document with the given endpoint ID.
     */
    private static Endpoint getEndpoint(Document casaDocument, String endpointID) {
        NodeList endpointList = 
                casaDocument.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
        for (int i = 0; i < endpointList.getLength(); i++) {
            Element endpoint = (Element) endpointList.item(i);
            if (endpoint.getAttribute(CASA_NAME_ATTR_NAME).equals(endpointID)) {
                return getEndpoint(endpoint);
            }
        }

        return null;
    }
    
    /**
     * Gets a non-null list of all the endpoints defined in a casa document.
     */
    private static List<Endpoint> getEndpoints(Document casaDocument) {
        List<Endpoint> ret = new ArrayList<Endpoint>();
        
        if (casaDocument != null) {
            NodeList endpointList =
                    casaDocument.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
            for (int i = 0; i < endpointList.getLength(); i++) {
                Element endpointElement = (Element) endpointList.item(i);
                Endpoint endpoint = getEndpoint(endpointElement);
                ret.add(endpoint);
            }
        }
        
        return ret;
    }    
    
    /**
     * Computes a non-null list of endpoints that are defined in the WSDL files 
     * in the compapp project and the component projects.
     */  
    private List<Endpoint> getNewWsdlEndpoints(Document jbiDocument) {
        List<Endpoint> ret = new ArrayList<Endpoint>();
        
        for (WSDLModel model : wsdlRepository.getWsdlCollection()) {            
            String relativePath = MyFileUtil.getRelativePath(new File(confDirLoc), getFile(model));
            String tns = model.getRootComponent().getPeer().getAttribute("targetNamespace");
                        //wsdlDocument.getDocumentElement().getAttribute("targetNamespace");
            for (Service service : model.getDefinitions().getServices()) {
                QName serviceQName = new QName(tns, service.getName()); // ?                
                for (Port port : service.getPorts()) {
                    String portName = port.getName();
                    Binding binding = port.getBinding().get();
                    PortType portType = binding.getType().get();
                    QName interfaceQName = //XmlUtil.getAttributeNSName(portType.getPeer(), CASA_INTERFACE_NAME_ATTR_NAME);
                            new QName(tns, portType.getName()); //?
                    Endpoint endpoint = new Endpoint(portName, serviceQName, interfaceQName);
                    if (!ret.contains(endpoint)) {
                        ret.add(endpoint);
                    }
                }
            }
        }
        
        return ret;
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
        
        // 1. Get the oldCasaEndpoints list
        List<Endpoint> oldCasaEndpoints = getEndpoints(oldCasaDocument);        
       
        // 2. Loop through all WSDL files, create an allWSDLEndpoints list which
        // contains all the ports defined in the WSDL files in the compapp 
        // project and the component projects.
        newWSDLEndpoints = new ArrayList<Endpoint>();
        for (WSDLModel model : wsdlRepository.getWsdlCollection()) {            
            String relativePath = MyFileUtil.getRelativePath(new File(confDirLoc), getFile(model));
            String tns = model.getRootComponent().getPeer().getAttribute("targetNamespace");
                        //wsdlDocument.getDocumentElement().getAttribute("targetNamespace");
            for (Service service : model.getDefinitions().getServices()) {
                QName serviceQName = new QName(tns, service.getName()); // ?                
                for (Port port : service.getPorts()) {
                    String portName = port.getName();
                    Binding binding = port.getBinding().get();
                    PortType portType = binding.getType().get();
                    QName interfaceQName = //XmlUtil.getAttributeNSName(portType.getPeer(), CASA_INTERFACE_NAME_ATTR_NAME);
                            new QName(tns, portType.getName()); //?
                    Endpoint endpoint = new Endpoint(portName, serviceQName, interfaceQName);
                    if (!newWSDLEndpoints.contains(endpoint)) {
                        newWSDLEndpoints.add(endpoint);
                    }
                }
            }
        }
        
        // 3. Compute externalEndpoints list
        List<Endpoint> externalEndpoints = getExternalEndpoints();
        
        // 4. = newWSDLEndpoints + externalEndpoints
        List<Endpoint> allEndpoints = new ArrayList<Endpoint>();
        allEndpoints.addAll(newWSDLEndpoints);
        for (Endpoint externalEndpoint : externalEndpoints) {
            if (!allEndpoints.contains(externalEndpoint)) {
                allEndpoints.add(externalEndpoint);
            }
        }
                    
        // 5. Loop through all jbiServiceUnits/$suName/jbi.xml, create a 
        // newConnectedEndpoints list
        List<Endpoint> newConnectedEndpoints = new ArrayList<Endpoint>();
                 
        NodeList jbiSUs =
                jbiDocument.getElementsByTagName(JBI_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < jbiSUs.getLength(); i++) {
            Element jbiSU = (Element) jbiSUs.item(i);
            String suName = getJBIServiceUnitName(jbiSU);
            List<Endpoint> suEndpoints = loadSUEndpoints(suName);
            su2Endpoints.put(suName, suEndpoints);
            if (suEndpoints != null) {
                for (Endpoint suEndpoint : suEndpoints) {
                    if (!newConnectedEndpoints.contains(suEndpoint)) {
                        newConnectedEndpoints.add(suEndpoint);
                    }
                }
            } 
        }
        
        // 6. Compute newCasaEndpoints: 
        //       newCasaEndpoints = 
        //              Intersection(oldCasaEndpoints, allEndpoints)  
        //              + newConnectedEndpoints 
        // When computing the list of newCasaEndpoints, we want to preserve the 
        // order (and therefore the ID) of endpoints in the oldCasaEndpoints
        // list as much as possible to avoid unnecessary (endpoint ID) changes 
        // across compapp builds.
        List<Endpoint> newCasaEndpoints = new ArrayList<Endpoint>();
        for (Endpoint oldCasaEndpoint : oldCasaEndpoints) {
            for (Endpoint endpoint : allEndpoints) {
                if (oldCasaEndpoint.equals(endpoint)) {
                    newCasaEndpoints.add(oldCasaEndpoint);
                    break;
                }
            }
        }
        for (Endpoint suEndpoint : newConnectedEndpoints) {
            if (!newCasaEndpoints.contains(suEndpoint)) {
                newCasaEndpoints.add(suEndpoint);
            }
        }
        
        debugLog("old CASA Endpoints:");
        for (Endpoint endpoint : oldCasaEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }    
        debugLog("new WSDL Endpoints:");
        for (Endpoint endpoint : newWSDLEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }    
        debugLog("external Endpoints:");
        for (Endpoint endpoint : externalEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        } 
        debugLog("new SU Endpoints:");
        for (Endpoint endpoint : newConnectedEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }            
        debugLog("new CASA Endpoints:");
        for (Endpoint endpoint : newCasaEndpoints) {
            debugLog("    " + endpoint.getFullyQualifiedName());
        }        
        
        return newCasaEndpoints;
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
        String attrValueNamespace =attrValueQName.getNamespaceURI();
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

    /*
    private static List<String> getDeletedCasaPortLinkHrefs(Document casaDocument) {
        List<String> ret = new ArrayList<String>();
        
        NodeList casaPortNodeList = 
                casaDocument.getElementsByTagName(CASA_PORT_ELEM_NAME);
        for (int i = 0; i < casaPortNodeList.getLength(); i++) {
            Element casaPort = (Element) casaPortNodeList.item(i);
            String state = casaPort.getAttribute(CASA_STATE_ATTR_NAME);
            if (state != null && state.equals(CASA_DELETED_ATTR_VALUE)) {
                NodeList links = casaPort.getElementsByTagName(CASA_LINK_ELEM_NAME);
                if (links != null && links.getLength() == 1) {
                    Element link = (Element) links.item(0);
                    String href = link.getAttributeNS(XLINK_NAMESPACE_URI, XLINK_HREF_ATTR_NAME);
                    ret.add(href);
                }
            }
        }
        
        return ret;
    }
    */
    
    private List<Endpoint> loadSUEndpoints(String suName) {
        
        List<Endpoint> suEndpointList = new ArrayList<Endpoint>();

        File suJbiFile = new File(serviceUnitsDirLoc + File.separator + suName + File.separator + "jbi.xml"); // NOI18N
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
                            String endpointName = e.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
                            
                            String serviceName = e.getAttribute(JBI_SERVICE_NAME_ATTR_NAME);
                            String serviceNS = null;
                            int idx = serviceName.indexOf(':');
                            if (idx > 0) {
                                String prefix = serviceName.substring(0, idx);
                                serviceNS = suJbiDocument.getDocumentElement().getAttribute("xmlns:"+prefix);
                                serviceName = serviceName.substring(idx+1);
                            }
                            
                            String interfaceName = e.getAttribute(JBI_INTERFACE_NAME_ATTR_NAME);
                            String interfaceNS = "";
                            idx = interfaceName.indexOf(':');
                            if (idx > 0) {
                                String prefix = interfaceName.substring(0, idx);
                                interfaceNS = suJbiDocument.getDocumentElement().getAttribute("xmlns:"+prefix);
                                interfaceName = interfaceName.substring(idx+1);
                            }

                            // 06/05/07, T. Li skip extension elements.
                            if ((interfaceName != null) && (interfaceName.length() > 0)) {
                                Endpoint endpoint = new Endpoint(endpointName,
                                        new QName(serviceNS, serviceName),
                                        new QName(interfaceNS, interfaceName),
                                        e.getLocalName().equals("consumes"));
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
    
    /*
    private boolean isSUPort(List<Endpoint> endpointList, 
            QName serviceQName, String portName) { 
        for (Endpoint endpoint : endpointList) {
            if (endpoint.getEndpointName().equals(portName) &&
                    endpoint.getServiceQName().equals(serviceQName)) {
                return true;
            }
        }
        
        return false;
    }
    */
    
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
                    
                    boolean isSUEndpoint = isInEndpointList(suEndpoints, serviceQName, portName);
                    boolean isLiveDeletedBCEndpoint = false;
                    boolean isLiveUnconnectedEndpoint = false;
                    
                    if (isSUEndpoint) {
                        debugLog("              is a SU endpoint.");
                    } else {
                        List<Endpoint> deletedBCEndpoints = deletedBCEndpointsMap.get(bcName);
                        if (isInEndpointList(deletedBCEndpoints, serviceQName, portName) &&
                                isInEndpointList(newWSDLEndpoints, serviceQName, portName)) {
                            isLiveDeletedBCEndpoint = true;
                            debugLog("              is a live deleted endpoint.");
                        }
                        
                        if (!isLiveDeletedBCEndpoint) {
                            List<Endpoint> unconnectedBCEndpoints = oldUnconnectedBCEndpointsMap.get(bcName);
                            if (isInEndpointList(unconnectedBCEndpoints, serviceQName, portName) &&
                                    isInEndpointList(newWSDLEndpoints, serviceQName, portName)) {
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
        
        return ports;
    }
              
    /**
     * Gets all the deleted Port elements in a casa document under the given
     * binding component service unit.
     */ /*
    private static List<Element> getDeletedPortElements(Document casaDocument, String bcsuName) {
        List<Element> ret = new ArrayList<Element>();
        
        if (casaDocument != null) {
            NodeList bcSUs = casaDocument.getElementsByTagName(CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
            for (int i = 0; i < bcSUs.getLength(); i++) {
                Element bcSU = (Element) bcSUs.item(i);
                String compName = bcSU.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME);
                if (compName.equals(bcsuName)) {
                    NodeList ports = bcSU.getElementsByTagName(CASA_PORT_ELEM_NAME);
                    for (int j = 0; j < ports.getLength(); j++) {
                        Element port = (Element) ports.item(j);
                        String state = port.getAttribute(CASA_STATE_ATTR_NAME);
                        if (CASA_DELETED_ATTR_VALUE.equals(state)) {
                            ret.add(port);
                        }
                    }
                    break;
                }
            }
        }
        
        return ret;
    }
    */
    
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
      
    /*
    private static String getQName(String namespace, String name) {
        if (namespace == null) {
            return name;
        }
        return "{" + namespace + "}" + name;
    }     
    */    
    
    /*
    private String getCompAppWSDLFileName() {        
        String projName = project.getProperty(JbiProjectProperties.ASSEMBLY_UNIT_UUID);
        return projName + ".wsdl";
    }
    */
    
    private void log(String msg) {
        task.log(msg);
    }
    
    private void debugLog(String msg) {
        task.log(msg, Project.MSG_DEBUG);
    }
}
