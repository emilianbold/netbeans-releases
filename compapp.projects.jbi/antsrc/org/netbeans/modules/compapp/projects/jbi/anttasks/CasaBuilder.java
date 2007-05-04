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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.util.MyFileUtil;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
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
        
    // mapping binding component namespace to binding component name
    private Map<String, String> bcNamespace2NameMap;
        
    // mapping service unit name to a list of endpoints for that service unit
    private Map<String, List<Endpoint>> suEndpointsMap = 
            new HashMap<String, List<Endpoint>>();    
         
    // mapping fully qualified port names to casa port elements in casa document
    // (This is to solve the duplicate WSDL problem.)
    private Map<String, Element> casaPortMap = new HashMap<String, Element>();
    
    // a list of xlink hrefs of all casa ports that were marked as "deleted"
    // in the old casa document
    private List<String> deletedCasaPortLinkHrefs;
    
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
                deletedCasaPortLinkHrefs = 
                    getDeletedCasaPortLinkHrefs(oldCasaDocument); // todo: remove me
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }        
        
        String projPath = project.getProperty("basedir") + File.separator;
        serviceUnitsDirLoc = projPath + "src" + File.separator + "jbiServiceUnits";
        confDirLoc = projPath + "src" + File.separator + "conf";
    }
    
    /**
     * Creates a new casa document based on sa jbi.xml and optionally 
     * an old casa file.
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
        buildServiceUnitEndpointsMap(jbiDocument);
        
        // endpoints
        Element casaEndpoints = createCasaEndpointsElement(jbiDocument);
        casaRoot.appendChild(casaEndpoints);       
        
        // service units
        Element casaServiceUnits = createCasaServiceUnitsElement(jbiDocument);
        casaRoot.appendChild(casaServiceUnits);
        
        // connections
        Element casaConnections = createCasaConnectionsElement(jbiDocument);
        casaRoot.appendChild(casaConnections);
        
        // porttypes, bindings, services
        List<Element> casaWSDLReferences = createCasaWSDLReferenceElements();
        for (Element casaElement : casaWSDLReferences) {
            casaRoot.appendChild(casaElement);
        }
        
        // regions
        Element casaRegions = createCasaRegionsElement();
        casaRoot.appendChild(casaRegions);
                
        preserveCasaWSDLEndpointsAndPorts();
                          
        mergeCasaChanges(jbiDocument);
        
        return newCasaDocument;
    }
        
    /**
     * Gets an Endpoint object from an endpoint element in CASA DOM.
     */
    private static Endpoint getEndpoint(Element endpointElement) {
        String endpointName = 
                endpointElement.getAttribute(CASA_ENDPOINT_NAME_ATTR_NAME);
        QName endpointServiceQName = XmlUtil.getAttributeNSName(
                endpointElement, CASA_SERVICE_NAME_ATTR_NAME);
        QName endpointInterfaceQName = XmlUtil.getAttributeNSName(
                endpointElement, CASA_INTERFACE_NAME_ATTR_NAME);
        
        return new Endpoint(endpointName, endpointServiceQName, endpointInterfaceQName); 
    }
    
    /**
     * Creates the service-units element in the new casa document.
     */
    private Element createCasaServiceUnitsElement(Document jbiDocument)
            throws SAXException, IOException, ParserConfigurationException {
        
        Element casaSUs =
                (Element) newCasaDocument.createElement(CASA_SERVICE_UNITS_ELEM_NAME);
        
        // 1. Copy service units from jbi document over
        NodeList jbiSUs = 
                jbiDocument.getElementsByTagName(JBI_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < jbiSUs.getLength(); i++) {
            Element jbiSU = (Element) jbiSUs.item(i);
            String componentID = getJBIServiceUnitComponentName(jbiSU);
            
            Element casaSU = bcNamespace2NameMap.values().contains(componentID) ?
                createCasaBindingServiceUnitElement(jbiSU) :
                createCasaServiceEngineServiceUnitElement(jbiSU);
            
            casaSUs.appendChild(casaSU);
        }
                
        if (oldCasaDocument != null) {        
            // 2. Add ghost binding component service units (BC SUs with all its
            // ports marked as "deleted") from old casa
            try {
                for (Element oldBCSU : getGhostBindingComponentServiceUnitElements()) {
                    Element newBCSU = deepCloneCasaNodeWithEndpointConversion(
                            oldBCSU);                    
                    casaSUs.appendChild(newBCSU);
                }
            } catch (Exception e) {
                log("ERROR: Problem merging \"deleted\" binding component service units from old casa: " + e +
                        ". This does not affect regular compapp build." );
            }
            
            // 3. Add external service units from old casa
            try {
                NodeList oldCasaSUs = oldCasaDocument.getElementsByTagName(
                        CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
                for (int i = 0; i < oldCasaSUs.getLength(); i++) {
                    Element oldCasaSU = (Element) oldCasaSUs.item(i);
                    String type = oldCasaSU.getAttribute(CASA_INTERNAL_ATTR_NAME);
                    if (type != null && type.equals("false")) {
                        Node newCasaSU = deepCloneCasaNodeWithEndpointConversion(
                                oldCasaSU);                        
                        casaSUs.appendChild(newCasaSU);
                    }
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
     */
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

    /**
     * Creates a new binding-component-service-unit element in the 
     * new casa document.
     */
    private Element createCasaBindingServiceUnitElement(Element jbiSU)
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

        Element casaPorts = createCasaPortsElement(suName);
        bcSU.appendChild(casaPorts);

        return bcSU;
    }

    /**
     * Creates a new service-engine-service-unit element in the 
     * new casa document.
     */
    private Element createCasaServiceEngineServiceUnitElement(Element jbiSU)
            throws SAXException, IOException, ParserConfigurationException {

        Element seSU = newCasaDocument.createElement(CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
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

        List<Endpoint> suEndpointList = suEndpointsMap.get(suName); //loadSUEndpoints(suName);
        if (suEndpointList == null) {
            log("ERROR: Invalid service unit name: " + suName);
        } else {
            for (Endpoint endpoint : suEndpointList) {
                Element endpointRef = endpoint.isConsumes() ?
                    (Element) newCasaDocument.createElement(CASA_CONSUMES_ELEM_NAME) :
                    (Element) newCasaDocument.createElement(CASA_PROVIDES_ELEM_NAME);
                String endpointID = getEndpointID(endpoint);
                endpointRef.setAttribute(CASA_ENDPOINT_ATTR_NAME, endpointID);
                seSU.appendChild(endpointRef);
            }
        }

        return seSU;
    }

    /**
     * Creates the connections element in the new casa document.
     */
    private Element createCasaConnectionsElement(Document jbiDocument) {

        Element casaConnections =
                (Element) newCasaDocument.createElement(CASA_CONNECTIONS_ELEM_NAME);

        // 1. Copy connections from jbi document over.
        NodeList jbiConnections =
                jbiDocument.getElementsByTagName(JBI_CONNECTION_ELEM_NAME);

        for (int i = 0; i < jbiConnections.getLength(); i++) {
            Element jbiConnection = (Element) jbiConnections.item(i);

            Element casaConnection =
                    (Element) newCasaDocument.createElement(CASA_CONNECTION_ELEM_NAME);
            casaConnection.setAttribute(CASA_STATE_ATTR_NAME, CASA_UNCHANGED_ATTR_VALUE);

            Element consumer =
                    (Element) jbiConnection.getElementsByTagName(JBI_CONSUMER_ELEM_NAME).item(0);
            String consumerEndpointName =
                    consumer.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
            QName consumerServiceQName = XmlUtil.getAttributeNSName(
                    consumer, JBI_SERVICE_NAME_ATTR_NAME);
            String consumerID = getEndpointID(consumerServiceQName, consumerEndpointName);
            casaConnection.setAttribute(CASA_CONSUMER_ATTR_NAME, consumerID);

            Element provider =
                    (Element) jbiConnection.getElementsByTagName(JBI_PROVIDER_ELEM_NAME).item(0);
            String providerEndpointName =
                    provider.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
            QName providerServiceQName = XmlUtil.getAttributeNSName(
                    provider, JBI_SERVICE_NAME_ATTR_NAME);
            String providerID = getEndpointID(providerServiceQName, providerEndpointName);
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
                        String newProviderID =
                                findNewEndpointID(oldCasaDocument, newCasaDocument, oldConnection, false);
                        Element newConnection =
                                findNewConnection(casaConnections, newConsumerID, newProviderID);
                        
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

    private List<Element> createCasaWSDLReferenceElements()
    throws SAXException, DOMException, IOException, ParserConfigurationException {

        List<Element> ret = new ArrayList<Element>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        Element casaPortTypes =
                (Element) newCasaDocument.createElement(CASA_PORTTYPES_ELEM_NAME);

        Element casaBindings =
                (Element) newCasaDocument.createElement(CASA_BINDINGS_ELEM_NAME);

        Element casaServices =
                (Element) newCasaDocument.createElement(CASA_SERVICES_ELEM_NAME);

        for (WSDLModel model : wsdlRepository.getWsdlCollection()) {
            String relativePath =
                    MyFileUtil.getRelativePath(new File(confDirLoc), getFile(model));

            Definitions defs = model.getDefinitions();

            // Add casa:porttypes
            for (PortType pt : defs.getPortTypes()) {
                String ptName = pt.getName();
                Element linkElement = createCasaLinkElement(relativePath, 
                        "/definitions/portType" + "[@name='" + ptName + "']");
                casaPortTypes.appendChild(linkElement);
            }

            // Add casa:bindings
            for (Binding b : defs.getBindings()) {
                String bName = b.getName();
                Element linkElement = createCasaLinkElement(relativePath,
                        "/definitions/binding" + "[@name='" + bName + "']");
                casaBindings.appendChild(linkElement);
            }

            // Add casa:services
            for (Service s : defs.getServices()) {
                String sName = s.getName();
                Element linkElement = createCasaLinkElement(relativePath,
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
     * Creates the regions element in the new casa document.
     */
    private Element createCasaRegionsElement() {

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

        Element regions =
                (Element) newCasaDocument.createElement(CASA_REGIONS_ELEM_NAME);

        Element region = (Element) newCasaDocument.createElement(CASA_REGION_ELEM_NAME);
        region.setAttribute(CASA_NAME_ATTR_NAME, WSDL_ENDPOINTS_REGION_NAME);
        region.setAttribute(CASA_WIDTH_ATTR_NAME, DEFAULT_WSDL_ENDPOINTS_REGION_WIDTH);
        regions.appendChild(region);

        region = (Element) newCasaDocument.createElement(CASA_REGION_ELEM_NAME);
        region.setAttribute(CASA_NAME_ATTR_NAME, JBI_MODULES_REGION_NAME);
        region.setAttribute(CASA_WIDTH_ATTR_NAME, DEFAULT_JBI_MODULES_REGION_WIDTH);
        regions.appendChild(region);

        region = (Element) newCasaDocument.createElement(CASA_REGION_ELEM_NAME);
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

    private File getFile(WSDLModel model) {
        Lookup lookup = model.getModelSource().getLookup();
        File f = (File) lookup.lookup(File.class);
        if (f == null) {
            FileObject fo = (FileObject) lookup.lookup(FileObject.class);
            f = FileUtil.toFile(fo);
        }
        return f;
    }

    private String getFilePath(WSDLModel model) {
        File f = getFile(model);
        return f.getPath().replaceAll("\\\\", "/");
    }

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
        
    private List<Endpoint> getDeletedBCEndpoints()
            throws DOMException {
         
         List<Endpoint> endpointList = new ArrayList<Endpoint>();
         
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
                         endpointList.add(endpoint);
                     }
                 }
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
    
    public void mergeCasaChanges(Document jbiDocument)
            throws SAXException, ParserConfigurationException, IOException {
        
        if (oldCasaDocument == null) {
            return;
        }
        
        try {
            // 3. Merge SE SU locations (only if there is no SE SU change)
            NodeList oldSESUs = oldCasaDocument.getElementsByTagName(
                    CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
            NodeList newSESUs = newCasaDocument.getElementsByTagName(
                    CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
            
            for (int i = 0; i < oldSESUs.getLength(); i++) {
                Element oldSESU = (Element) oldSESUs.item(i);
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
            
            // 4. Merge BC SU port locations (only if there is no port change)
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
                }
            }          
        } catch (Exception e) {
            log("ERROR: Problem merging old casa document: " + e +
                    " The CASA file will be regenerated. This should not affect regular compapp build." );
        }
    }
    
    private static Element getCasaEndpoint(Document casaDocument, String endpointID) {
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

        Element oldEndpoint = getCasaEndpoint(oldCasaDocument, oldEndpointID);
        if (oldEndpoint != null) {
            String oldEndpointName =
                    oldEndpoint.getAttribute(CASA_ENDPOINT_NAME_ATTR_NAME);
            QName oldServiceQName =
                    XmlUtil.getAttributeNSName(oldEndpoint, CASA_SERVICE_NAME_ATTR_NAME);
            return getEndpointID(oldServiceQName, oldEndpointName);
        }

        return null;
    }

    private Element findNewConnection(Element newCasaConnections,
            String newConsumerID, String newProviderID) {

        NodeList newConnections =
                newCasaConnections.getElementsByTagName(CASA_CONNECTION_ELEM_NAME);

        for (int j = 0; j < newConnections.getLength(); j++) {
            Element newConnection = (Element) newConnections.item(j);
            if (newConnection.getAttribute(CASA_CONSUMER_ATTR_NAME).equals(newConsumerID) &&
                    newConnection.getAttribute(CASA_PROVIDER_ATTR_NAME).equals(newProviderID)) {
                return newConnection;
            }
        }

        return null;
    }

    private Element getConnectionEndpoint(Element connection, boolean consumer) {
        String elementName = consumer? JBI_CONSUMER_ELEM_NAME : JBI_PROVIDER_ELEM_NAME;
        NodeList endpoints = connection.getElementsByTagName(elementName);
        assert endpoints != null && endpoints.getLength() == 1;
        return (Element) endpoints.item(0);
    }

    /**
     * Deep clones a node.
     */
    /*
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
     *
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


    /**
     * Finds a link container element from a list of link container elements.
     */
    private Element findLinkContainerElement(NodeList nodeList, Element connection) {

        Element link = ((Element)connection.getElementsByTagName(CASA_LINK_ELEM_NAME).item(0));
        String linkHref = link.getAttributeNS(XLINK_NAMESPACE_URI, XLINK_HREF_ATTR_NAME);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element myElement = (Element) nodeList.item(i);
            Element myLink = (Element) myElement.getElementsByTagName(CASA_LINK_ELEM_NAME).item(0);
            // String myLinkHref = myLink.getAttributeNS(XLINK_NAMESPACE_URI, XLINK_HREF_ATTR_NAME); // FIXME ?????
            String myLinkHref = myLink.getAttribute(XLINK_NAMESPACE_PREFIX + ":" + XLINK_HREF_ATTR_NAME);
            //log("    " + myLinkHref);
            if (myLinkHref.equals(linkHref)) {
                return myElement;
            }
        }

        return null;
    }

    private void buildServiceUnitEndpointsMap(Document jbiDocument)
    throws SAXException, IOException, ParserConfigurationException {

        NodeList jbiSUs =
                jbiDocument.getElementsByTagName(JBI_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < jbiSUs.getLength(); i++) {
            Element jbiSU = (Element) jbiSUs.item(i);
            String suName = getJBIServiceUnitName(jbiSU);
            List<Endpoint> suEndpoints = loadSUEndpoints(suName);
            suEndpointsMap.put(suName, suEndpoints);
        }
    }

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
     * Gets all the endpoints defined in a casa document.
     */
    private static List<Endpoint> getEndpoints(Document casaDocument) {
        List<Endpoint> ret = new ArrayList<Endpoint>();
        
        NodeList endpointList = 
                casaDocument.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
        for (int i = 0; i < endpointList.getLength(); i++) {
            Element endpointElement = (Element) endpointList.item(i);
            Endpoint endpoint = getEndpoint(endpointElement);
            ret.add(endpoint);
        }
        
        return ret;
    }

    /**
     * Creates the endpoints element in the new casa document.
     */
    private Element createCasaEndpointsElement(Document jbiDocument)
            throws SAXException, IOException, ParserConfigurationException{

        Element casaEndpoints =
                (Element) newCasaDocument.createElement(CASA_ENDPOINTS_ELEM_NAME);
        Element casaRoot = (Element) newCasaDocument.getElementsByTagName("casa").item(0);
        casaRoot.appendChild(casaEndpoints);        
        
        // 1. Add all the endpoints from the old casa document. 
        // (Do this first to preserve the old endpoint IDs. Not that this is 
        // necessary, but it is nice to have fewer changes after rebuild.)
        if (oldCasaDocument != null) {            
            try {
                for (Endpoint endpoint : getEndpoints(oldCasaDocument)) {
                    addEndpoint(endpoint);
                }
            } catch (Exception e) {
                log("ERROR: Problem merging endpoints from old casa: " + e +
                        ". This does not affect regular compapp build." );
            }
        }

        // 2. Add endpoints from (internal) SESUs and visible endpoints from BCSUs
        List<Endpoint> endpointList = new ArrayList<Endpoint>();
        for (String key : suEndpointsMap.keySet()) {
            List<Endpoint> list = suEndpointsMap.get(key);
            if (list != null) {
                for (Endpoint endpoint : list) {                    
                    addEndpoint(endpoint);                    
                }
            }
        }

        return casaEndpoints;
    }

    // index used for creating endpoint IDs in the new casa document
    private int endpointIndex = 1;

    /**
     * Adds an endpoint to the new casa document. Do nothing if the given 
     * endpoint already exists.
     *
     * @return the ID of the endpoint
     */
    private String addEndpoint(Endpoint endpoint) {

        String key = endpoint.getFullyQualifiedName();
        String endpointID = endpointMap.get(key);

        if (endpointID == null) {
            endpointID = "endpoint" + endpointIndex;
            endpointIndex++;
            endpointMap.put(key, endpointID);

            Element casaEndpoints =
                    (Element) newCasaDocument.getElementsByTagName("endpoints").item(0);

            Element casaEndpoint =
                    (Element) newCasaDocument.createElement(CASA_ENDPOINT_ELEM_NAME);
            casaEndpoint.setAttribute(CASA_NAME_ATTR_NAME, endpointID);
            casaEndpoint.setAttribute(CASA_ENDPOINT_NAME_ATTR_NAME,
                    endpoint.getEndpointName());

            setAttributeQName(casaEndpoint, CASA_SERVICE_NAME_ATTR_NAME,
                    endpoint.getServiceQName());
            setAttributeQName(casaEndpoint, CASA_INTERFACE_NAME_ATTR_NAME,
                    endpoint.getInterfaceQName());

            casaEndpoints.appendChild(casaEndpoint);
        }

        return endpointID;
    }

    /** 
     * Gets the endpoint ID of an endpoint in the new casa document.
     * @deprecated
     */
    private String getEndpointID(QName serviceQName, String endpointName) {
        String key = serviceQName.toString() + "." + endpointName;
        return endpointMap.get(key);
    }
    private String getEndpointID(Endpoint endpoint) {
        String key = endpoint.getFullyQualifiedName();
        return endpointMap.get(key);
    }

    /**
     * A Map mapping fully qualified endpoint name to endpoint ID for all 
     * the endpoints in the new casa document.
     */
    private Map<String, String> endpointMap = new HashMap<String, String>();

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
    // Mapping namespace URI to namespace prefix
     private Map<String, String> jbiNSMap = null;
     
     private void setJBIAttributeQName(Document jbiDocument, Element element,
             String attrName, String attrQValue) {
        String attrValueNamespace;
        String attrValue;

        if (attrQValue == null || attrQValue.trim().equals("")) {
            attrValueNamespace = "";
            attrValue = "";
        } else {
            attrValueNamespace = attrQValue.substring(1, attrQValue.indexOf("}"));
            attrValue = attrQValue.substring(attrQValue.indexOf("}") + 1);
        }

        setJBIAttributeQName(jbiDocument, element, attrName, attrValueNamespace, attrValue);
    }

    private void setJBIAttributeQName(Document jbiDocument,
            Element element, String attrName,
            String attrValueNamespace, String attrValue) {

        String attrQValue;
        if (jbiNSMap == null) {
            jbiNSMap = XmlUtil.getNamespaceMap(jbiDocument);
        }

        if (attrValueNamespace.equals("") && attrValue.equals("")) {
            attrQValue = "";
        } else {
            String prefix = jbiNSMap.get(attrValueNamespace);
            if (prefix == null) {
                prefix = "ns" + prefixID;
                prefixID++;
                element.getOwnerDocument().getDocumentElement().setAttribute(
                        "xmlns:" + prefix, attrValueNamespace);
                jbiNSMap.put(attrValueNamespace, prefix);
            }
            attrQValue = prefix + ":" + attrValue;
        }

        element.setAttribute(attrName, attrQValue);
    }
    */
    
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

    private List<Element> createCasaPortEndpointsElement(String suName,
            QName serviceQName, String portName)
            throws SAXException, IOException, ParserConfigurationException {

        List<Element> ret = new ArrayList<Element>();

        List<Endpoint> suEndpointList = suEndpointsMap.get(suName); 
        for (Endpoint endpoint : suEndpointList) {
            if (portName.equals(endpoint.getEndpointName()) &&
                    serviceQName.equals(endpoint.getServiceQName())) {
                String endpointID = getEndpointID(serviceQName, portName);

                Element cEndpointRef =
                    (Element) newCasaDocument.createElement(CASA_CONSUMES_ELEM_NAME);
                cEndpointRef.setAttribute(CASA_ENDPOINT_ATTR_NAME, endpointID);
                ret.add(cEndpointRef);

                Element pEndpointRef =
                    (Element) newCasaDocument.createElement(CASA_PROVIDES_ELEM_NAME);
                pEndpointRef.setAttribute(CASA_ENDPOINT_ATTR_NAME, endpointID);
                ret.add(pEndpointRef);
            }
        }
        return ret;
    }

    private List<Endpoint> loadSUEndpoints(String suName)
    throws SAXException, IOException, ParserConfigurationException {
        List<Endpoint> suEndpointList = new ArrayList<Endpoint>();

        File suJbiFile = new File(confDirLoc + "/../jbiServiceUnits/" + suName + "/jbi.xml"); // NOI18N
        if (suJbiFile.exists()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document suJbiDocument = documentBuilder.parse(suJbiFile);
            NodeList servicesElements = suJbiDocument.getElementsByTagNameNS(JBI_NAMESPACE_URI, JBI_SERVICES_ELEM_NAME);
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

                        Endpoint endpoint = new Endpoint(endpointName,
                                new QName(serviceNS, serviceName),
                                new QName(interfaceNS, interfaceName),
                                e.getLocalName().equals("consumes"));
                        suEndpointList.add(endpoint);
                    }
                }
            }
            return suEndpointList;

        }
        return null;
    }

    private boolean isSUPort(List<Endpoint> suEndpointList,
            String portName, String serviceName, String tns) { // FIXME
        //System.out.println("Check: "+portName+", "+serviceName+", "+tns);
        for (Endpoint endpoint : suEndpointList) {
            if (endpoint.getEndpointName().equals(portName) &&
                    endpoint.getServiceQName().getLocalPart().equals(serviceName)) {
                String ns = endpoint.getServiceQName().getNamespaceURI();
                if ((ns != null) && (ns.length() > 0) && (ns.equals(tns))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates a ports element for a binding component service unit in the 
     * new casa document.
     */
    private Element createCasaPortsElement(String bcsuName)
            throws SAXException, IOException, ParserConfigurationException {

        List<Endpoint> endpointList; 

        // Create casa ports element.
        Element casaPortsElement = (Element) newCasaDocument.createElement(
                CASA_PORTS_ELEM_NAME);
        
        String casaWSDLFileName = getCompAppWSDLFileName();

        // Loop through all WSDLs and add casa port elements of the given binding component type.
        for (WSDLModel model : wsdlRepository.getWsdlCollection()) {
            /*
            if (wsdlRepository.isJavaEEWsdl(model)) {
                continue;
            }
            */
            
            String relativePath = MyFileUtil.getRelativePath(new File(confDirLoc), getFile(model));
            String tns = model.getRootComponent().getPeer().getAttribute("targetNamespace");
                        //wsdlDocument.getDocumentElement().getAttribute("targetNamespace");

            if (relativePath.endsWith(casaWSDLFileName)) {
                endpointList = new ArrayList<Endpoint>();
                List<Endpoint> suEndpoints = suEndpointsMap.get(bcsuName);
                if (suEndpoints != null) {
                    endpointList.addAll(suEndpoints);
                }
            } else {
                endpointList = suEndpointsMap.get(bcsuName);
            }

            // Add casa port
            for (Service s : model.getDefinitions().getServices()) {
                String serviceName = s.getName();
                QName serviceQName = new QName(tns, serviceName);
                
                for (Port p : s.getPorts()) {
                    String portName = p.getName();
                    
                    if (isSUPort(endpointList, portName, serviceName, tns)) { // FIXME
                        String fullyQualifiedPortName =
                                serviceQName.toString() + "." + portName;
                        
                        // Instead of always creating a new casa port, we check
                        // if a casa port has already been created.
                        Element casaPortElement =
                                casaPortMap.get(fullyQualifiedPortName);
                        if (casaPortElement == null) {
                            // Create new casa port
                            casaPortElement =
                                    createCasaPortElement(relativePath, portName,
                                    serviceQName, bcsuName);
                            
                            // Add casa port to casa ports
                            casaPortsElement.appendChild(casaPortElement);
                            casaPortMap.put(fullyQualifiedPortName, casaPortElement);
                        } else {
                            // Reuse old casa port
                            Element casaLinkElement = createCasaLinkElement(
                                    relativePath,
                                    "/definitions/service[@name='" + serviceName +
                                    "']/port[@name='" + portName + "']");
                            casaPortElement.appendChild(casaLinkElement);
                        }
                    }
                }
            }
        }
        
        // Merge deleted port elements from the old casa document under the given
        // binding component service unit back into the new casa document.
        List<Element> deletedPortElements = 
                getDeletedPortElements(oldCasaDocument, bcsuName);        
        for (Element oldPort : deletedPortElements) {
            Node newPort = deepCloneCasaNodeWithEndpointConversion(oldPort);            
            casaPortsElement.appendChild(newPort);
        }

        return casaPortsElement;
    }
              
    /**
     * Gets all the deleted Port elements in a casa document under the given
     * binding component service unit.
     */
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
    
    /**
     * Creates a port element for a binding component service unit in the 
     * new casa document.
     */
    private Element createCasaPortElement(String relativePath,
            String portName, QName serviceQName,
            String suName)
            throws DOMException, SAXException, IOException, ParserConfigurationException {
        // Create casa port
        Element casaPortElement =
                (Element) newCasaDocument.createElement(CASA_PORT_ELEM_NAME);
        casaPortElement.setAttribute(CASA_X_ATTR_NAME, "0");
        casaPortElement.setAttribute(CASA_Y_ATTR_NAME, "-1");

        Element linkElement = createCasaLinkElement(relativePath,
                "/definitions/service[@name='" + serviceQName.getLocalPart() +
                "']/port[@name='" + portName + "']");
        casaPortElement.appendChild(linkElement);
        
        // Mark the casa port as "deleted" if the old casa document says so.
        // (Since the casa port element has not been added to the casa document,
        // can't use getAttributeNS() yet.)
        String linkHref = 
                linkElement.getAttribute(XLINK_NAMESPACE_PREFIX + ":" + XLINK_HREF_ATTR_NAME);
        if (deletedCasaPortLinkHrefs != null &&     
                deletedCasaPortLinkHrefs.contains(linkHref)) {
            casaPortElement.setAttribute(CASA_STATE_ATTR_NAME, CASA_DELETED_ATTR_VALUE);
        }

        // Add casa endpoints to casa port
        List<Element> endpointElements = createCasaPortEndpointsElement(
                suName, serviceQName, portName);
        for (Element element : endpointElements) {
            casaPortElement.appendChild(element);
        }
        return casaPortElement;
    }
    
    /**
     * Creates a link element in the new casa document.
     */
    private Element createCasaLinkElement(String relativePath, String uri) {
        Element linkElement = (Element) newCasaDocument.createElement(
                CASA_LINK_ELEM_NAME);
        linkElement.setAttribute(
                XLINK_NAMESPACE_PREFIX + ":type", "simple");
        linkElement.setAttribute(
                XLINK_NAMESPACE_PREFIX + ":href",
                relativePath + "#xpointer(" + uri + ")");
        return linkElement;
    }
      
    /*
    private static String getQName(String namespace, String name) {
        if (namespace == null) {
            return name;
        }
        return "{" + namespace + "}" + name;
    }*/    
    
    private String getCompAppWSDLFileName() {        
        String projName = project.getProperty(JbiProjectProperties.ASSEMBLY_UNIT_UUID);
        return projName + ".wsdl";
    }
    
    private void log(String msg) {
        task.log(msg);
    }
}
