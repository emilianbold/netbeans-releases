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
package org.netbeans.modules.compapp.casaeditor.model.casa;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaAttribute;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JarCatalogModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaModelImpl;
import org.netbeans.modules.compapp.casaeditor.model.jbi.impl.JBIAttributes;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBindingInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.DeleteModuleAction;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.TemplateGroup;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.ui.wizard.BindingGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.WizardBindingConfigurationStep;
import org.netbeans.modules.xml.wsdl.ui.spi.ValidationInfo;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author jqian
 */
public class CasaWrapperModel extends CasaModelImpl {
    
    public static final String PROPERTY_PREFIX               = "model_property_";                           // NOI18N
    public static final String PROPERTY_MODEL_RELOAD         = PROPERTY_PREFIX + "reload";                  // NOI18N
    public static final String PROPERTY_CONNECTION_REMOVED   = PROPERTY_PREFIX + "connection_removed";      // NOI18N
    public static final String PROPERTY_CONNECTION_ADDED     = PROPERTY_PREFIX + "connection_added";        // NOI18N
    public static final String PROPERTY_CASA_PORT_REMOVED    = PROPERTY_PREFIX + "casa_port_removed";       // NOI18N
    public static final String PROPERTY_CASA_PORT_ADDED      = PROPERTY_PREFIX + "casa_port_added";         // NOI18N
    public static final String PROPERTY_ENDPOINT_REMOVED     = PROPERTY_PREFIX + "endpoint_removed";        // NOI18N
    public static final String PROPERTY_ENDPOINT_ADDED       = PROPERTY_PREFIX + "endpoint_added";          // NOI18N
    public static final String PROPERTY_ENDPOINT_RENAMED     = PROPERTY_PREFIX + "endpoint_renamed";        // NOI18N
    public static final String PROPERTY_SERVICE_UNIT_RENAMED = PROPERTY_PREFIX + "service_unit_renamed";    // NOI18N
    public static final String PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_ADDED   = PROPERTY_PREFIX + "service_unit_added";   // NOI18N
    public static final String PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_REMOVED = PROPERTY_PREFIX + "service_unit_removed"; // NOI18N
    
    private static final String CASA_WSDL_RELATIVE_LOCATION = "../jbiasa/";                         // NOI18N
    private static final String CASA_WSDL_FILENAME = "casa.wsdl";                                   // NOI18N
    private static final String JBI_SERVICE_UNITS_DIR = "jbiServiceUnits";                          // NOI18N
    private static final String DUMMY_PORTTYPE_NAME = "dummyCasaPortType";                          // NOI18N
    
    private static JarCatalogModel acm = new JarCatalogModel();
    private static CatalogWriteModel catalogModel;
    
    private PropertyChangeSupport mSupport = new PropertyChangeSupport(this);
    
    // 01/24/07, added to support add/del SU projects
    private Map<String, Project> mAddProjects = new HashMap<String, Project>();
    private Map<String, String> mDeleteProjects = new HashMap<String, String>();
    
    
    /** Creates a new instance of CasaWrapperModel */
    public CasaWrapperModel(ModelSource source) {
        super(source);
        
        // ?
        try {
            sync();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        buildCatalogModel();
    }
    
    private void buildCatalogModel() {
        
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        super.removePropertyChangeListener(pcl);
        mSupport.removePropertyChangeListener(pcl);
    }
    
    /**
     * Add property change listener which will receive events for any element
     * in the underlying sa jbi model.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        super.addPropertyChangeListener(pcl);
        mSupport.addPropertyChangeListener(pcl);
    }
    
    /**
     * Gets a list of binding component service units.
     */
    public List<CasaBindingComponentServiceUnit> getBindingComponentServiceUnits() {
        CasaServiceUnits serviceUnits = getRootComponent().getServiceUnits();
        return serviceUnits.getBindingComponentServiceUnits();
    }
    
    /**
     * Gets a list of service engine service units.
     */
    public List<CasaServiceEngineServiceUnit> getServiceEngineServiceUnits() {
        CasaServiceUnits serviceUnits = getRootComponent().getServiceUnits();
        return serviceUnits.getServiceEngineServiceUnits();
    }
    
    // sesu only ?
    public boolean existingServiceUnit(String id) {
        for (CasaServiceEngineServiceUnit serviceUnit : getServiceEngineServiceUnits()) {
            String myID = serviceUnit.getUnitName();
            if (id.equalsIgnoreCase(myID)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets all the WSDL endpoints in the CASA model.
     */
    public List<CasaPort> getCasaPorts() {
        List<CasaPort> ret = new ArrayList<CasaPort>();
        
        for (CasaBindingComponentServiceUnit bcSU : getBindingComponentServiceUnits()) {
            for (CasaPort casaPort : bcSU.getPorts().getPorts()) {
                ret.add(casaPort);
            }
        }
        
        return ret;
    }
    
    public String getEndpointName(CasaPort casaPort) { // Move me to CasaPort
        CasaEndpointRef endpointRef = casaPort.getConsumes();
        if (endpointRef == null) {
            endpointRef = casaPort.getProvides();
        }
        //assert endpoint != null;
        if (endpointRef == null) // TMP
            return "getEndpointName=null, Uh?";         // NOI18N
        return endpointRef.getEndpoint().get().getEndpointName();
    }

    public void setEndpointName(CasaPort casaPort, String endpointName) { // Move me to CasaPort
        CasaEndpointRef endpointRef = casaPort.getConsumes();
        if (endpointRef == null) {
            endpointRef = casaPort.getProvides();
        }
        assert endpointRef != null;
        setEndpointName(casaPort, endpointRef, endpointName);
    }

    public void setEndpointName(CasaEndpointRef endpointRef, String endpointName) {
        setEndpointName(endpointRef, endpointRef, endpointName);
    }
    
    public String getBindingComponentName(CasaPort casaPort) { // Move me to CasaPort
        CasaBindingComponentServiceUnit bcSU =
                (CasaBindingComponentServiceUnit) casaPort.getParent().getParent();
        return bcSU.getComponentName();
    }
    
    /**
     * Gets the WSDL port linked by the casaport
     *
     * @param port  the casaport
     * @return  the wsdl port linked by the casaport
     */
    public Port getLinkedWSDLPort(CasaPort casaPort) { // TODO: refactor to CasaPort?
        CasaLink link = casaPort.getLink();
        String linkHref = link.getHref();
        
        try {
            Port port = (Port) getWSDLComponentFromXLinkHref(linkHref);
            return port;
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets a single <code>WSDLComponent</code> from a xlink href that looks like
     * "su-name/foo.wsdl#xpointer(some-xpath-for-a-single-wsdl-element)"
     *
     * @exception IllegalArgumentException  if the given href doesn't match the
     *      expected pattern.
     */
    private WSDLComponent getWSDLComponentFromXLinkHref(String linkHref)
    throws URISyntaxException, CatalogModelException, XPathExpressionException {
        
        WSDLComponent wsdlComponent = (WSDLComponent) cachedReferences.get(linkHref);
        
        if (wsdlComponent == null) {
            String regex = "(.*)#xpointer\\((.*)\\)";       // NOI18N
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(linkHref);
            
            if (! matcher.matches()) {
                throw new IllegalArgumentException("Invalid xlink href: " + linkHref);
            }
            
            String uriString = matcher.group(1);
            String xpathString = matcher.group(2);
            
            WSDLModel wsdlModel = getWSDLModel(uriString);
//            System.out.println("uriString: " + uriString + "=>" + wsdlModel);
            
            if (wsdlModel != null) {
                WSDLComponent root = wsdlModel.getRootComponent();
                wsdlComponent =
                        new FindWSDLComponent().findComponent(root, xpathString);
                cachedReferences.put(linkHref, wsdlComponent);
            } else {
                System.out.println("WARNING: WSDL model for " + linkHref + " is (temporarily) unavailable.");
            }
        }
        
        return wsdlComponent;
    }
    
    private Map<String, Object> cachedReferences = new HashMap<String, Object>();
    
    
    /**
     * Gets the casa provides or consumes endpoint from a casa connection.
     */
    public CasaEndpointRef getCasaEndpoint(CasaConnection casaConnection,
            boolean isConsumes) {
        CasaEndpoint casaEndpoint = isConsumes ?
            casaConnection.getConsumer().get() :
            casaConnection.getProvider().get();
        return getCasaEndpointRef(casaEndpoint);
    }
    
    private CasaEndpointRef getCasaEndpointRef(CasaEndpoint casaEndpoint) {
        
        for (CasaServiceEngineServiceUnit seSU : getServiceEngineServiceUnits()) {
            for (CasaEndpointRef endpointRef : seSU.getEndpoints()) {
                if (endpointRef.getEndpoint().get() == casaEndpoint) {
                    return endpointRef;
                }
            }
        }
        
        for (CasaBindingComponentServiceUnit bcSU : getBindingComponentServiceUnits()) {
            for (CasaPort casaPort : bcSU.getPorts().getPorts()) {
                CasaEndpointRef endpointRef = casaPort.getConsumes();
                if (endpointRef != null &&
                        endpointRef.getEndpoint().get() == casaEndpoint) {
                    return endpointRef;
                }
                endpointRef = casaPort.getProvides();
                if (endpointRef != null &&
                        endpointRef.getEndpoint().get() == casaEndpoint) {
                    return endpointRef;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Sets the location of a casa service engine service unit.
     */
    public void setServiceEngineServiceUnitLocation(
            final CasaServiceEngineServiceUnit casaServiceUnit,
            int x, int y) {
        
        if (casaServiceUnit.getX() == x && casaServiceUnit.getY() == y) {
            return;
        }
        
        startTransaction();
        try {
            casaServiceUnit.setX(x);
            casaServiceUnit.setY(y);
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
    }
    
    /**
     * Sets the location of a casa port.
     */
    public void setCasaPortLocation(final CasaPort casaPort, int x, int y) {
        
        if (casaPort.getX() == x && casaPort.getY() == y) {
            return;
        }
        
        startTransaction();
        try {
            casaPort.setX(x);
            casaPort.setY(y);
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
    }
    
    /**
     * Adds a brand new connection or mark a previously deleted connection
     * as unchanged.
     */
    public CasaConnection addConnection(final CasaConsumes consumesRef,
            final CasaProvides providesRef) {
        
        
        if (!canConnect(consumesRef, providesRef)) {
            throw new RuntimeException(
                    "The two endpoints cannot be connected because of their incompatible interfaces.");
        }
        
        CasaConnection casaConnection = getCasaConnection(consumesRef, providesRef);
        
        if (casaConnection == null) {
            casaConnection = addNewConnection(consumesRef, providesRef);
        } else {
            String state = casaConnection.getState();
            if (!ConnectionState.DELETED.getState().equals(state)) {
                throw new RuntimeException(
                        "Cannot add a connection that is marked as \"new\" or \"unchanged\"");
            }
            setConnectionState(casaConnection, ConnectionState.UNCHANGED);
        }
        
        return casaConnection;
    }
    
    /**
     * Adds a brand new connection between the given two endpoints.
     */
    private CasaConnection addNewConnection(final CasaConsumes consumesRef,
            final CasaProvides providesRef) {
        
        CasaEndpointRef definedEndpointRef = null;
        CasaEndpointRef undefinedEndpointRef = null;
        
        if (isEndpointDefined(consumesRef)) {
            if (!isEndpointDefined(providesRef)) {
                definedEndpointRef = consumesRef;
                undefinedEndpointRef = providesRef;
            }
        } else {
            if (isEndpointDefined(providesRef)) {
                definedEndpointRef = providesRef;
                undefinedEndpointRef = consumesRef;
            }
        }
        
        if (definedEndpointRef != null && undefinedEndpointRef != null) {
            // Populate the interface name of the undefined endpoint
            startTransaction();
            try {
                QName interfaceQName =
                        definedEndpointRef.getEndpoint().get().getInterfaceQName();
                CasaPort casaPort = getCasaPort(undefinedEndpointRef);
                if (casaPort != null) {
                    // Update both Consumes and Provides in the same WSDL endpoint
                    CasaEndpointRef p = casaPort.getProvides();
                    if (p != null) {
                        p.getEndpoint().get().setInterfaceQName(interfaceQName);
                    }
                    CasaEndpointRef c = casaPort.getConsumes();
                    if (c != null) {
                        c.getEndpoint().get().setInterfaceQName(interfaceQName);
                    }
                    
                    //todo: 12/22 temp method to add portType name...
                    casaPort.setPortType(interfaceQName.toString());
                    
                    Port port = getLinkedWSDLPort(casaPort);
                    populateBindingAndPort(casaPort, port, interfaceQName,
                            casaPort.getBindingType());
                    
                } else {  // endpoint is in SE
                    CasaEndpoint endpoint = undefinedEndpointRef.getEndpoint().get();
                    endpoint.setInterfaceQName(interfaceQName);
                }
            } finally {
                if (isIntransaction()) {
                    endTransaction();
                }
            }
        }
        
        // Create a new casa connection
        CasaComponentFactory casaFactory = getFactory();
        CasaConnection casaConnection = casaFactory.createCasaConnection();
        casaConnection.setState(ConnectionState.NEW.getState());
        casaConnection.setConsumer(
                casaConnection.createReferenceTo(consumesRef.getEndpoint().get(), CasaEndpoint.class));
                //consumesRef.getEndpoint());
        casaConnection.setProvider(
                casaConnection.createReferenceTo(providesRef.getEndpoint().get(), CasaEndpoint.class));
                //providesRef.getEndpoint());
        
        CasaConnections casaConnections = getRootComponent().getConnections();
        
        // Add the new casa connection into casa file
        startTransaction();
        try {
            casaConnections.addConnection(-1, casaConnection);
        } finally {
            if (isIntransaction()) {
                fireConnectionAdded(casaConnection);
                endTransaction();
            }
        }
        
        return casaConnection;
    }
    
    private void populateBindingAndPort(CasaPort casaPort, Port port,
            QName qName, String bType) {
        
        CasaWrapperModel casaWrapperModel = (CasaWrapperModel) casaPort.getModel();
        PortType portType = casaWrapperModel.getCasaPortType(casaPort);
        System.out.println("Got WSDLEndpoint Action.. Pt: " + portType);
        
        String wsdlLocation = casaWrapperModel.getWSDLLocation(casaPort);
        System.out.println("Got WSDL location: " + wsdlLocation);
        
        ExtensibilityElementTemplateFactory factory = new ExtensibilityElementTemplateFactory();
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
        LocalizedTemplateGroup ltg = null;
        LocalizedTemplateGroup bindingType = null;
        for (TemplateGroup group : groups) {
            ltg = factory.getLocalizedTemplateGroup(group);
            protocols.add(ltg);
            System.out.println(bType+" Add Potocol: "+ltg.getName());
            if (ltg.getName().equalsIgnoreCase(bType)) {
                bindingType = ltg;
            }
        }
        if (bindingType == null) {
            // missing the binding type, use the default for now...
            bindingType = ltg;
        }
        
        Binding binding = port.getBinding().get();
        String bindingName = binding.getName();
        Service service = (Service) port.getParent();
        String serviceName = service.getName();
        String servicePortName = port.getName();
        LocalizedTemplate bindingSubType = null;
        LocalizedTemplate[] templates = bindingType.getTemplate();
        
        WSDLModel wsdlModel = (WSDLModel) port.getModel();
        Definitions definitions = wsdlModel.getDefinitions();
        Collection<Import> imports = definitions.getImports();
        
        boolean foundImport = false;
        for (Import imprt : imports) {
            if (imprt.getNamespace().equals(qName.getNamespaceURI()) &&
                    imprt.getLocation().equals(wsdlLocation)) {
                foundImport = true;
                break;
            }
        }
        Import newImport = null;
        if (!foundImport) {
            newImport = wsdlModel.getFactory().createImport();
            newImport.setNamespace(qName.getNamespaceURI());
            newImport.setLocation(wsdlLocation);
        }
        
        Map configurationMap = new HashMap();
        configurationMap.put(WizardBindingConfigurationStep.BINDING_NAME, bindingName);
        configurationMap.put(WizardBindingConfigurationStep.BINDING_TYPE, bindingType);
        configurationMap.put(WizardBindingConfigurationStep.SERVICE_NAME, serviceName);
        configurationMap.put(WizardBindingConfigurationStep.SERVICEPORT_NAME, servicePortName);
        
        wsdlModel.startTransaction();
        try {
            if (newImport != null) {
                definitions.addImport(newImport);
            }
            
            boolean invalidBinding = true;
            
            for (int k = 0; (k < templates.length) && invalidBinding; k++) {
                bindingSubType = templates[k];
                configurationMap.put(WizardBindingConfigurationStep.BINDING_SUBTYPE, bindingSubType);
                
                if (binding != null) {
                    definitions.removeBinding(binding);
                }
                
                if (port != null) {
                    List<WSDLComponent> children = new ArrayList<WSDLComponent>(port.getChildren());
                    for (int i = children.size() - 1; i >= 0; i--) {
                        WSDLComponent child = children.get(i);
                        if (child instanceof org.netbeans.modules.xml.wsdl.model.ExtensibilityElement) {
                            port.removeExtensibilityElement((org.netbeans.modules.xml.wsdl.model.ExtensibilityElement)child);
                        }
                    }
                }
                
                BindingGenerator bGen = new BindingGenerator(wsdlModel, portType, configurationMap);
                bGen.execute();
                
                binding = bGen.getBinding();
                List<ValidationInfo> vBindingInfos = bindingSubType.getMProvider().validate(binding);
                invalidBinding = vBindingInfos.size() > 0;
                System.out.println(invalidBinding+" subType: "+bindingSubType.getName());
            }
            
        } finally {
            if (wsdlModel.isIntransaction()) {
                wsdlModel.endTransaction();
            }
        }
    }
    
    /**
     * Removes a connection or marks it as deleted depending on the current
     * state of the connection.
     */
    public void removeConnection(final CasaConnection casaConnection) {
        removeConnection(casaConnection, false);
    }
    
    /**
     * Removes a connection or marks it as deleted (depending on the current
     * state of the connection AND the overwriting hard delete flag).
     *
     * @param hardDelete    if true, the connection will be deleted; otherwise,
     *                      the connection will be deleted or marked as deleted
     *                      depending on the current state of the connection.
     */
    // FIXME: do we need the hardDelete flag?
    private void removeConnection(final CasaConnection casaConnection,
            boolean hardDelete) {
        String state = casaConnection.getState();
        
        if (ConnectionState.DELETED.getState().equals(state)) {
            throw new RuntimeException(
                    "Trying to remove a connection that has already been marked as \"deleted\"");
        }
        
        // 1. Clear user-created WSDL endpoint's Consumes/Provides interface name
        // if there is no visible connections left after the connection deletion.
        // Existing WSDL endpoints are left alone because there might be "deleted"
        // connections hanging around.
        CasaEndpointRef casaConsumes = getCasaEndpointRef(casaConnection.getConsumer().get());
        CasaPort casaPort = getCasaPort(casaConsumes);
        if (casaPort != null) {
            if (isDefinedInCompApp(casaPort)) {
                if (getConnections(casaPort, false).size() == 0) { // no visible connection left
                    CasaConsumes casaPortConsumes = casaPort.getConsumes();
                    if (casaPortConsumes != null) {
                        clearCasaEndpointInterfaceName(casaPortConsumes);
                    }
                    CasaProvides casaPortProvides = casaPort.getProvides();
                    if (casaPortProvides != null) {
                        clearCasaEndpointInterfaceName(casaPortProvides);
                    }
                }
            }
        } else {
            CasaServiceEngineServiceUnit sesu = getCasaEngineServiceUnit(casaConsumes);
            if (sesu != null && !sesu.isInternal()) { // endpoint belongs to external SESU
                if (getConnections(casaConsumes, false).size() == 0) { // no visible connection left
                    clearCasaEndpointInterfaceName(casaConsumes);
                }
            }
        }
        
        CasaEndpointRef casaProvides = getCasaEndpointRef(casaConnection.getProvider().get());
        casaPort = getCasaPort(casaProvides);
        if (casaPort != null) {
            if (isDefinedInCompApp(casaPort)) {
                if (getConnections(casaPort, false).size() == 0) {
                    CasaConsumes casaPortConsumes = casaPort.getConsumes();
                    if (casaPortConsumes != null) {
                        clearCasaEndpointInterfaceName(casaPortConsumes);
                    }
                    CasaProvides casaPortProvides = casaPort.getProvides();
                    if (casaPortProvides != null) {
                        clearCasaEndpointInterfaceName(casaPortProvides);
                    }
                }
            }
        } else {
            CasaServiceEngineServiceUnit sesu = getCasaEngineServiceUnit(casaConsumes);
            if (sesu != null && !sesu.isInternal()) { // endpoint belongs to external SESU
                if (getConnections(casaProvides, false).size() == 0) {
                    clearCasaEndpointInterfaceName(casaProvides);
                }
            }
        }
        
        // 2. Delete connection
        if (ConnectionState.NEW.getState().equals(state) || hardDelete) {
            // Remove casa connection
            CasaConnections casaConnections = (CasaConnections) casaConnection.getParent();
            startTransaction();
            try {
                casaConnections.removeConnection(casaConnection);
            } finally {
                if (isIntransaction()) {
                    fireConnectionRemoved(casaConnection);
                    endTransaction();
                }
            }
        } else if (ConnectionState.UNCHANGED.getState().equals(state)) {
            setConnectionState(casaConnection, ConnectionState.DELETED);
        }
    }
    
    private void setConnectionState(final CasaConnection casaConnection,
            final ConnectionState state) {
        String initialState = casaConnection.getState();
        startTransaction();
        try {
            casaConnection.setState(state.getState());
        } finally {
            if (isIntransaction()) {
                if (ConnectionState.UNCHANGED.getState().equals(initialState) &&
                        ConnectionState.DELETED.equals(state)) { // FIXME
                    fireConnectionRemoved(casaConnection);
                } else if (ConnectionState.DELETED.getState().equals(initialState) &&
                        ConnectionState.UNCHANGED.equals(state)) {
                    fireConnectionAdded(casaConnection);
                } else {
                    assert false : "Uh? setConnectionState: " + initialState + "->" + state.getState();
                }
                endTransaction();
            }
        }
    }
    
    private void fireConnectionRemoved(CasaConnection casaConnection) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaConnection,
                PROPERTY_CONNECTION_REMOVED,
                null, null));
    }
    
    private void fireConnectionAdded(CasaConnection casaConnection) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaConnection,
                PROPERTY_CONNECTION_ADDED,
                null, null));
    }
    
    private void fireCasaPortAdded(CasaPort casaPort) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaPort,
                PROPERTY_CASA_PORT_ADDED,
                null, null));
    }
    
    private void fireCasaPortRemoved(CasaPort casaPort) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaPort,
                PROPERTY_CASA_PORT_REMOVED,
                null, null));
    }
    
    private void fireCasaEndpointAdded(CasaEndpointRef casaEndpointRef) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaEndpointRef,
                PROPERTY_ENDPOINT_ADDED,
                null, null));
    }
    
    private void fireCasaEndpointRemoved(CasaEndpointRef casaEndpointRef) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaEndpointRef,
                PROPERTY_ENDPOINT_REMOVED,
                null, null));
    }
    
    private void fireCasaEndpointRenamed(CasaComponent casaComponent) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaComponent,
                PROPERTY_ENDPOINT_RENAMED,
                null, null));
    }
    
    
    private void fireServiceUnitRenamed(CasaServiceUnit su) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                su,
                PROPERTY_SERVICE_UNIT_RENAMED,
                null, null));
    }
    
    private void fireServiceEngineServiceUnitAdded(CasaServiceEngineServiceUnit casaSESU) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaSESU,
                PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_ADDED,
                null, null));
    }
    
    private void fireServiceEngineServiceUnitRemoved(CasaServiceEngineServiceUnit casaSESU) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaSESU,
                PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_REMOVED,
                null, null));
    }
    
    /**
     * Tests if the two given endpoints can be connected or not.
     * Two endpoints can be connected if and only if all of the following
     * conditions hold true:
     * <UL>
     * <LI> one endpoint is a Consumes and the other is a Provides;
     * <LI> there is no existing visible connection involving the Consumes
     *      endpoint (there could be existing visible connections involving
     *      the Provides endpoint though);
     * <LI> the two endpoints don't have incompatible defined interfaces;
     * <LI> at least one of the two endpoints is from a service engine
     *      service unit.
     * </UL>
     */
    public boolean canConnect(CasaEndpointRef endpoint1,
            CasaEndpointRef endpoint2) {
        
        if ((endpoint1 instanceof CasaConsumes &&
                endpoint2 instanceof CasaConsumes) ||
                (endpoint1 instanceof CasaProvides &&
                endpoint2 instanceof CasaProvides)) {
            return false;
        }
        
        CasaEndpointRef consumes = (endpoint1 instanceof CasaConsumes) ?
            endpoint1 : endpoint2;
        
        if (getConnections(consumes, false).size() > 0) {
            return false;
        }
        
        if (isBindingComponentCasaEndpoint(endpoint1) &&
                isBindingComponentCasaEndpoint(endpoint2)) {
            return false;
        }
        
        if (isEndpointDefined(endpoint1) && isEndpointDefined(endpoint2)) {
            QName consumesInterfaceQName =
                    endpoint1.getEndpoint().get().getInterfaceQName();
            QName providesInterfaceQName =
                    endpoint2.getEndpoint().get().getInterfaceQName();
            return consumesInterfaceQName.equals(providesInterfaceQName);
        }
        
        // Do we allow both endpoints to be undefined?
        return true;
    }
    
    private boolean isEndpointDefined(CasaEndpointRef endpointRef) {
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        QName interfaceQName = endpoint.getInterfaceQName();
        return interfaceQName.getLocalPart().trim().length() > 0
                && !interfaceQName.equals(new QName(CASA_WSDL_TNS, DUMMY_PORTTYPE_NAME)); // FIXME
    }
    
    /**
     * Gets the casa connection between the two given casa endpoints, including
     * connection that is marked as deleted.
     */
    private CasaConnection getCasaConnection(CasaConsumes casaConsumes,
            CasaProvides casaProvides) {
        // Get all the connections, including deleted ones
        List<CasaConnection> casaConnectionList = getCasaConnectionList(true);
        
        CasaEndpoint consumesEndpoint = casaConsumes.getEndpoint().get();
        CasaEndpoint providesEndpoint = casaProvides.getEndpoint().get();
        
        for (CasaConnection casaConnection : casaConnectionList) {
            if (casaConnection.getConsumer().get() == consumesEndpoint &&
                    casaConnection.getProvider().get() == providesEndpoint) {
                return casaConnection;
            }
        }
        
        return null;
    }
    
    private Map<String, String> bcNameMap;
    
    // FIXME: should we get bcs from design time or run time?
    public Map<String, String> getDefaultBindingComponents() {
        if (bcNameMap == null) {
            bcNameMap = new HashMap<String, String>();
            
            JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
            assert bcinfo != null;
            
            List<JbiBindingInfo> bcList = bcinfo.getBindingInfoList();
            for (JbiBindingInfo bi : bcList) {
                bcNameMap.put(bi.getBcName(), bi.getBindingName());
            }
        }
        return bcNameMap;
    }
    
    /**
     * Gets a list of <code>CasaConnection</code>s in the CASA model.
     */
    // RENAME ME
    public List<CasaConnection> getCasaConnectionList(boolean includeDeleted) {
        List<CasaConnection> ret = new ArrayList<CasaConnection>();
        
        CasaConnections casaConnections = getRootComponent().getConnections();
        for (CasaConnection casaConnection : casaConnections.getConnections()) {
            String state = casaConnection.getState();
            if (includeDeleted ||
                    !state.equals(ConnectionState.DELETED.getState())) {
                ret.add(casaConnection);
            }
        }
        
        return ret;
    }
    
    /**
     * Gets the WSDL PortType of a given CasaPort.
     */
    public PortType getCasaPortType(CasaPort casaPort) {
        String portType = casaPort.getPortType();
        if ((portType == null) || (portType.length() < 1)) {
            return null;
        }
        CasaPortTypes casaPortTypes = getRootComponent().getPortTypes();
        for (CasaLink link : casaPortTypes.getLinks()) {
            String slink = link.getHref();
            try {
                PortType pt = (PortType) this.getWSDLComponentFromXLinkHref(slink);
                System.out.println("Got PortType: "+pt.getName());
                if (portType.equals(
                        "{" + pt.getModel().getDefinitions().getTargetNamespace() + "}" + pt.getName())) {
                    return pt;
                }
            } catch (Exception ex) {
                System.out.println("Failed to Fetch: "+slink);
            }
        }
        return null;
    }
    
    private String getWSDLLocation(CasaPort casaPort) {
        String portType = casaPort.getPortType();
        if ((portType == null) || (portType.length() < 1)) {
            return null;
        }
        CasaPortTypes casaPortTypes = getRootComponent().getPortTypes();
        for (CasaLink link : casaPortTypes.getLinks()) {
            String slink = link.getHref();
            try {
                PortType pt = (PortType) this.getWSDLComponentFromXLinkHref(slink);
                System.out.println("Got PortType: "+pt.getName());
                if (portType.equals(
                        "{" + pt.getModel().getDefinitions().getTargetNamespace() + "}" + pt.getName())) {
                    return slink.substring(0, slink.indexOf("#xpointer"));
                }
            } catch (Exception ex) {
                System.out.println("Failed to Fetch: "+slink);
            }
        }
        return null;
    }
    
    /**
     * Adds an internal or external unknown service engine service unit
     * (from the palette).
     */
    public CasaServiceEngineServiceUnit addUnknownEngineServiceUnit(
            boolean internal, int x, int y) {
        String projName = getUniqueUnknownEngineServiceUnitName();
        
        CasaServiceEngineServiceUnit seSU =
                addEngineServiceUnit(projName, "", internal, true, x, y);
        
        return seSU;
    }
    
    private String getUniqueUnknownEngineServiceUnitName() {
        List<String> existingUnknownNames = new ArrayList<String>();
        
        for (CasaServiceEngineServiceUnit seSU : getServiceEngineServiceUnits()) {
            if (seSU.isUnknown()) {
                existingUnknownNames.add(seSU.getUnitName());
            }
        }
        
        String ret = null;
        
        for (int i = 1; ; i++) {
            ret = "Unknown" + i;
            if (!existingUnknownNames.contains(ret)) {
                break;
            }
        }
        
        return ret;
    }
    
    private CasaServiceEngineServiceUnit addEngineServiceUnit(String projName,
            String compName, boolean internal, boolean unknown,
            int x, int y) {
        
        CasaComponentFactory casaFactory = getFactory();
        CasaServiceEngineServiceUnit casaSESU =
                casaFactory.createCasaEngineServiceUnit();
        casaSESU.setX(x);
        casaSESU.setY(y);
        casaSESU.setInternal(internal);
        casaSESU.setUnknown(unknown);
        casaSESU.setDefined(false);
        casaSESU.setName(projName);
        casaSESU.setUnitName(projName);
        casaSESU.setDescription("some description");
        casaSESU.setComponentName(compName);
        casaSESU.setArtifactsZip(projName + ".jar");
        
        CasaServiceUnits casaSUs = getRootComponent().getServiceUnits();
        startTransaction();
        try {
            casaSUs.addServiceEngineServiceUnit(-1, casaSESU);
        } finally {
            if (isIntransaction()) {
                fireServiceEngineServiceUnitAdded(casaSESU);
                endTransaction();
            }
        }
        
        return casaSESU;
    }
    
    /**
     * Adds a new WSDL endpoint.
     */
    public CasaPort addCasaPort(String componentType, String componentName, int x, int y) {
        
        // 1. Update casa.wsdl
        WSDLModel casaWSDLModel = getCasaWSDLModel(true); 
        WSDLComponentFactory wsdlFactory = casaWSDLModel.getFactory();
        Definitions definitions = casaWSDLModel.getDefinitions();
        
        // 1.0. create dummy porttype (need this to avoid NPE when building compapp)
        PortType portType = null;
        boolean dummyPortTypeExists = false;
        for (PortType pt : definitions.getPortTypes()) {
            if (pt.getName().equals(DUMMY_PORTTYPE_NAME)) {
                dummyPortTypeExists = true;
                portType = pt;
                break;
            }
        }
        if (!dummyPortTypeExists) {
            portType = wsdlFactory.createPortType();
            String newPortTypeName = DUMMY_PORTTYPE_NAME; //getUniquePortTypeName(casaWSDLModel);
            portType.setName(newPortTypeName);
        }
        
        // 1.1. create new binding
        Binding binding = wsdlFactory.createBinding();
        String newBindingName = getUniqueBindingName(casaWSDLModel);
        binding.setName(newBindingName);
        
        // 1.2. create new port
        Port port = wsdlFactory.createPort();
        String newPortName = getUniquePortName(casaWSDLModel);
        port.setName(newPortName);
        
        // 1.3. create new service
        int serviceCount = definitions.getServices().size();
        Service service = wsdlFactory.createService(); // FIXME: use existing one?
        String newServiceName = getUniqueServiceName(casaWSDLModel);
        service.setName(newServiceName);
        service.addPort(port);
        
        casaWSDLModel.startTransaction();
        try {
            port.setBinding(port.createReferenceTo(binding, Binding.class));
            binding.setType(binding.createReferenceTo(portType, PortType.class));
            if (!dummyPortTypeExists) {
                definitions.addPortType(portType);
            }
            definitions.addBinding(binding);
            definitions.addService(service);
        } finally {
            if (casaWSDLModel.isIntransaction()) {
                casaWSDLModel.endTransaction();
            }
        }
        
        String portHref = CASA_WSDL_RELATIVE_LOCATION + CASA_WSDL_FILENAME +
                "#xpointer(/definitions/service[@name='" + newServiceName + "']/port[@name='" + newPortName + "'])";
                
        return addCasaPortToModel(componentType, componentName, 
                 newServiceName, newPortName, portHref, null, x, y);
    }
    
    private JbiBindingInfo getBindingInfo(Port port) {
        JbiDefaultComponentInfo bcinfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        if (bcinfo == null) {
            return null;
        }
        
        List<JbiBindingInfo> bclist = bcinfo.getBindingInfoList();
        List<ExtensibilityElement> xts = port.getExtensibilityElements();
        if (xts.size() > 0) {
            ExtensibilityElement ex = xts.get(0);
            String qns = ex.getQName().getNamespaceURI();
            if (qns != null) {
                for (JbiBindingInfo bi : bclist) {
                    String[] ns = bi.getNameSpaces();
                    if (ns != null) {
                        for (String n : ns) {
                            if (n.equalsIgnoreCase(qns)) {
                                return bi;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Add a port from wsdl file into CASA
     *
     * @param port  selected port to add
     * @param wsdlFile the source wsdl file
     * @return the casa port or null if failed to add
     */
    public CasaPort addCasaPortFromWsdlPort(Port port, File wsdlFile) {
        try {
            JbiBindingInfo bi = getBindingInfo(port);
            if (bi == null) {
                return null;
            }
            String componentType = bi.getBindingName();
            String componentName = bi.getBcName();
            String newServiceName = ((Service) (port.getParent())).getName();
            String newPortName = port.getName();
            int x = 0;
            int y = 0;
            String fname = wsdlFile.getCanonicalPath();
            String href = fname.substring(fname.indexOf("jbiServiceUnits")).replace('\\', '/');
            String portHref = "../" + href + "#xpointer(/definitions/service[@name='"
                    + newServiceName + "']/port[@name='" + newPortName + "'])";
            
            return addCasaPortToModel(componentType, componentName, newServiceName, newPortName, portHref, port, x, y);
        } catch (Exception ex) {
            // add failed...
        }
        
        return null;
    }
    
    private CasaPort addCasaPortToModel(String componentType, String componentName,
            String newServiceName, String newPortName,
            String portHref, Port port, int x, int y) {
        CasaComponentFactory casaFactory = getFactory();
        CasaEndpoint consumes, provides;
        
        String newServiceNamespace = 
                getCasaWSDLModel(false).getDefinitions().getTargetNamespace(); // ???
        
        CasaEndpoints endpoints = getRootComponent().getEndpoints();
        startTransaction();
        try {
            // 1. Create and add a dummy consumes endpoint
            consumes = casaFactory.createCasaEndpoint();
            String consumesID = getUniqueEndpointID();
            consumes.setName(consumesID);
            consumes.setEndpointName(newPortName); 
            consumes.setInterfaceQName(new QName(""));
            consumes.setServiceQName(new QName(newServiceNamespace, newServiceName));
            
            endpoints.addEndpoint(-1, consumes);
            
            // 2. Create and add a dummy provides endpoint
            provides = casaFactory.createCasaEndpoint();
            String providesID = getUniqueEndpointID();
            provides.setName(providesID);
            provides.setEndpointName(newPortName); 
            provides.setInterfaceQName(new QName(""));
            provides.setServiceQName(new QName(newServiceNamespace, newServiceName));
            
            endpoints.addEndpoint(-1, provides);
            
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
        
        // 3. Create new casa port
        CasaPort casaPort = casaFactory.createCasaPort();
        casaPort.setX(x);
        casaPort.setY(y);
//        casaPort.setBindingState("unbound");
        casaPort.setPortType("");
        casaPort.setBindingType(componentType);
        
        // 4. Add casa port link
        CasaLink link = casaFactory.createCasaLink();
        link.setHref(portHref);
        link.setType("simple");
        casaPort.setLink(link);
        
        // 5. Create and add consumes reference
        CasaConsumes casaConsumes = casaFactory.createCasaConsumes();
        casaConsumes.setEndpoint(
                casaConsumes.createReferenceTo(consumes, CasaEndpoint.class));
        casaPort.setConsumes(casaConsumes);
        
        // 4.5. create and add dummy Provides endpoint reference
        
        CasaProvides casaProvides = casaFactory.createCasaProvides();
        casaProvides.setEndpoint(
                casaProvides.createReferenceTo(provides, CasaEndpoint.class));
        casaPort.setProvides(casaProvides);
        
        startTransaction();
        try {
            CasaBindingComponentServiceUnit casaBCSU = null;
            for(CasaBindingComponentServiceUnit bcSUs : getBindingComponentServiceUnits()) {
                if (bcSUs.getComponentName().equals(componentName)) {
                    casaBCSU = bcSUs;
                    break;
                }
            }
            
            if (casaBCSU == null) {
                // create a new casa binding component service unit in *.casa
                casaBCSU = casaFactory.createCasaBindingServiceUnit();
                casaBCSU.setUnitName(componentName);
                casaBCSU.setComponentName(componentName);
                casaBCSU.setName(componentName); // FIXME
                casaBCSU.setDescription("some description");
                casaBCSU.setArtifactsZip(componentName + ".jar");
                
                CasaPorts casaPorts = casaFactory.createCasaPorts();
                casaBCSU.setPorts(casaPorts);
                
                CasaServiceUnits casaSUs = getRootComponent().getServiceUnits();
                casaSUs.addBindingComponentServiceUnit(-1, casaBCSU);
            }
            
            // add the new WSDL endpoint
            CasaPorts casaPorts = casaBCSU.getPorts();
            casaPorts.addPort(-1, casaPort);
        
        } finally {
            if (isIntransaction()) {
                fireCasaPortAdded(casaPort);
                endTransaction();
            }
        }
        
        return casaPort;
    }
    
    /**
     * Removes a WSDL endpoint or marks it as deleted depending on the current
     * state of the WSDL endpoint. A WSDL endpoint can only be removed or marked
     * as deleted successfully when there is no active connection
     * connecting its Consumes or Provides endpoint.
     *
     * @param casaPort          an existing casa port
     * @param deleteConnection  delete any connection that involves the consumes
     *                          or provides endpoint in the WSDL endpoint
     */
    public void removeCasaPort(final CasaPort casaPort, boolean deleteConnection) {
        removeCasaPort(casaPort, deleteConnection, true);
    }
    
    /**
     * Removes a WSDL endpoint or marks it as deleted (depending on the current
     * state of the WSDL endpoint AND the overwriting hard delete flag).
     * A WSDL endpoint can only be removed or marked as deleted when there is
     * no active connection connecting its Consumes or Provides endpoint.
     *
     * @param casaPort          an existing casa port
     * @param deleteConnection  delete any visible connection that involves the
     *                          consumes or provides endpoint in the WSDL endpoint
     * @param hardDelete        if true, the connection will be deleted;
     *                          otherwise, the connection will be deleted or
     *                          marked as deleted depending on the current state
     *                          of the connection.
     */
    private void removeCasaPort(final CasaPort casaPort,
            boolean deleteConnection, boolean hardDelete) {
        assert deleteConnection == true; // FIXME
        
        // 1. Delete connections
        CasaConsumes consumes = casaPort.getConsumes();
        if (consumes != null) {
            List<CasaConnection> visibleConnections =
                    getConnections(consumes, false); // don't include "deleted" connection
            if (visibleConnections.size() > 0) {
                if (deleteConnection) {
                    for (CasaConnection visibleConnection : visibleConnections) {
                        removeConnection(visibleConnection);
                    }
                } else {
                    throw new RuntimeException("There are still connections involving the Consumes endpoint in the WSDL endpoint. "
                            + "Either remove the connections manually or hard-delete this WSDL endpoint.");
                }
            }
        }
        
        CasaProvides provides = casaPort.getProvides();
        if (provides != null) {
            List<CasaConnection> visibleConnections =
                    getConnections(provides, false); // don't include "deleted" connection
            if (visibleConnections.size() > 0) {
                if (deleteConnection) {
                    for (CasaConnection visibleConnection : visibleConnections) {
                        removeConnection(visibleConnection);
                    }
                } else {
                    throw new RuntimeException("There are still connections involving the Provides endpoint in the WSDL endpoint. "
                            + "Either remove the connections manually or hard-delete this WSDL endpoint.");
                }
            }
        }
        
        // 2. Remember the two endpoints, we might have some cleanup work to do.
        CasaEndpoint c = casaPort.getConsumes() == null ? null :
            casaPort.getConsumes().getEndpoint().get();
        CasaEndpoint p = casaPort.getProvides() == null ? null :
            casaPort.getProvides().getEndpoint().get();
        
        // 3. Delete the casa port (which deletes enpoint refs too)
        CasaPorts casaPorts = (CasaPorts) casaPort.getParent();
        startTransaction();
        try {
            casaPorts.removePort(casaPort);
        } finally {
            if (isIntransaction()) {
                fireCasaPortRemoved(casaPort);
                endTransaction();
            }
        }
        
        // 4. Delete dangling endpoint
        if (c != null) {
            removeDanglingEndpoint(c);
        }
        if (p != null) {
            removeDanglingEndpoint(p);
        }
        
        // Deleting a casaport could leave an empty casa binding service unit
        // in the casa file but that's probably OK.
    }
    
    /**
     * Removes a Consumes/Provides endpoint in an external service unit.
     *
     * @param endpoint      a Consumes or Provides endpoint
     * @param deleteConnection    delete any connection that connects the endpoint
     */
    public void removeEndpoint(final CasaEndpointRef endpointRef, boolean deleteConnection) {
        assert deleteConnection == true; // FIXME
        
        // Delete any visible connections
        List<CasaConnection> visibleConnections = getConnections(endpointRef, false);
        for (CasaConnection visibleConnection : visibleConnections) {
            removeConnection(visibleConnection);
        }
        
        CasaPort casaPort = getCasaPort(endpointRef);
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        
        // Delete the endpoint reference
        CasaServiceEngineServiceUnit seSU =
                (CasaServiceEngineServiceUnit) endpointRef.getParent();
        startTransaction();
        try {
            if (endpointRef instanceof CasaConsumes) {
                seSU.removeConsumes((CasaConsumes) endpointRef);
            } else {
                seSU.removeProvides((CasaProvides) endpointRef);
            }
        } finally {
            if (isIntransaction()) {
                fireCasaEndpointRemoved(endpointRef);
                endTransaction();
            }
        }
        
        // Delete dangling endpoint
        boolean endpointRemoved = removeDanglingEndpoint(endpoint);
        
        // Delete the corresponding Port, Binding and possibly Service element
        // from casa.wsdl if applicable.
        if (endpointRemoved && casaPort != null && isDefinedInCompApp(casaPort)) {
            Port port = getLinkedWSDLPort(casaPort);
            
            WSDLModel casaWSDLModel = port.getModel();
            if (casaWSDLModel != null) {
                // FIXME: when deleting a user-defined casaport, we delete
                // the consumes and provides endpoint, both of which try to
                // delete the same port/binding/service now! Maybe we should
                // hold on deleting port/... until there is really no reference
                // left?
                Definitions definitions = casaWSDLModel.getDefinitions();
                Binding binding = port.getBinding().get();
                Service service = (Service) port.getParent();
                
                casaWSDLModel.startTransaction();
                try {
                    definitions.removeBinding(binding);
                    if (service.getPorts().size() == 1) {
                        definitions.removeService(service);
                    } else {
                        service.removePort(port);
                    }
                } finally {
                    if (casaWSDLModel.isIntransaction()) {
                        casaWSDLModel.endTransaction();
                    }
                }
            }
        }
    }
    
    private boolean removeDanglingEndpoint(CasaEndpoint endpoint) {
        // For invisible connections, we still need to keep the endpoints around
        
        for (CasaServiceEngineServiceUnit seSU : getServiceEngineServiceUnits()) {
            for (CasaEndpointRef endpointRef : seSU.getEndpoints()) {
                if (endpointRef.getEndpoint().get() == endpoint) {
                    return false;
                }
            }
        }
        
        for (CasaBindingComponentServiceUnit bcSU : getBindingComponentServiceUnits()) {
            for (CasaPort port : bcSU.getPorts().getPorts()) {
                CasaEndpointRef consumes = port.getConsumes();
                if (consumes != null) {
                    if (consumes.getEndpoint().get() == endpoint) {
                        return false;
                    }
                }
                CasaEndpointRef provides = port.getProvides();
                if (provides != null) {
                    if (provides.getEndpoint().get() == endpoint) {
                        return false;
                    }
                }
            }
        }
        
        // Actually, we only care about connections that are marked as "deleted"        
        for (CasaConnection connection : getCasaConnectionList(true)) { // include deleted
            if (connection.getConsumer().get() == endpoint ||
                    connection.getProvider().get() == endpoint) {
                return false;
            }
        }
        
        // The endpoint is not referenced by anyone. We can safely delete it now.
        CasaEndpoints endpoints = getRootComponent().getEndpoints();
        startTransaction();
        try {
            endpoints.removeEndpoint(endpoint);
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
        
        return true;
    }
    
    public CasaPort getCasaPort(CasaEndpointRef endpoint) {
        CasaComponent comp = endpoint.getParent();
        if (comp instanceof CasaPort) {
            return (CasaPort) comp;
        } else {
            return null;
        }
    }
    
    public CasaServiceEngineServiceUnit getCasaEngineServiceUnit(
            CasaEndpointRef endpointRef) {
        CasaComponent comp = endpointRef.getParent();
        if (comp instanceof CasaServiceEngineServiceUnit) {
            return (CasaServiceEngineServiceUnit) comp;
        } else {
            return null;
        }
    }
    
    private boolean isBindingComponentCasaEndpoint(CasaEndpointRef casaEndpointRef) {
        return getCasaPort(casaEndpointRef) != null;
    }
    
    /**
     * Saves all the related models.
     * This should be called when the CasaWrapperModel is saved.
     */
    public void saveRelatedDataObjects() {
        
        List<DataObject> dataObjects = getRelatedDataObjects();
        
        for (DataObject dataObject : dataObjects) {
            SaveCookie saveCookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);
            if (saveCookie != null) {
                try {
                    saveCookie.save();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Discards changes to all the related models.
     * This should be called when the CasaWrapperModel changes are discarded.
     */
    public void discardRelatedDataObjects() {
        
        List<DataObject> dataObjects = getRelatedDataObjects();
        
        for (DataObject dataObject : dataObjects) {
            dataObject.setModified(false);
            try {
                dataObject.setValid(false);
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Gets a list of related data objects.
     */
    private List<DataObject> getRelatedDataObjects() {
        
        List<DataObject> ret = new ArrayList<DataObject>();
        
        // Get casa.wsdl
        WSDLModel casaWsdlModel = getCasaWSDLModel(false);
        if (casaWsdlModel != null) {
            DataObject dataObject = getDataObject(casaWsdlModel);
            if (dataObject != null) {
                ret.add(dataObject);
            }
        }
        
        return ret;
    }
    
    private DataObject getDataObject(Model model) {
        Lookup lookup = model.getModelSource().getLookup();
        FileObject fileObject = (FileObject) lookup.lookup(FileObject.class);
        try {
            DataObject dataObject = DataObject.find(fileObject);
            return dataObject;
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Adds a new endpoint to an external service engine service unit.
     */
    public CasaEndpointRef addEndpointToExternalServiceUnit(
            final CasaServiceEngineServiceUnit casaSESU, boolean isConsumes) {
        
        assert !casaSESU.isInternal() : "Not supportting adding new endpoints to internal service units for now.";
        
        CasaComponentFactory casaFactory = getFactory();
        
        String endpointID = getUniqueEndpointID();
        String endpointName = endpointID; // FIXME
        
        // Create casa endpoint
        CasaEndpoint endpoint = casaFactory.createCasaEndpoint();
        endpoint.setName(endpointID);
        endpoint.setEndpointName(endpointName);        
        
        // Create casa endpoint reference
        CasaEndpointRef endpointRef = isConsumes ?
            casaFactory.createCasaConsumes() :
            casaFactory.createCasaProvides();
        
        CasaEndpoints endpoints = getRootComponent().getEndpoints();
        
        startTransaction();
        try {
            endpoint.setInterfaceQName(new QName("")); 
            endpoint.setServiceQName(new QName(""));
            endpoints.addEndpoint(-1, endpoint);
            
            endpointRef.setEndpoint(
                    endpointRef.createReferenceTo(endpoint, CasaEndpoint.class));
            
            if (isConsumes) {
                casaSESU.addConsumes(-1, (CasaConsumes)endpointRef);
            } else {
                casaSESU.addProvides(-1, (CasaProvides)endpointRef);
            }
        } finally {
            if (isIntransaction()) {
                fireCasaEndpointAdded(endpointRef);
                endTransaction();
            }
        }
        
        return endpointRef;
    }
    
    /**
     * Add all paneding SE projects from queue to the CompApp project
     */
    public void addPendingProjects() {
        try {
            Project jbiProject = getJBIProject();
            
            for (Iterator it=mAddProjects.keySet().iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                Project project = mAddProjects.get(key);
                AntArtifactProvider antArtifactProvider =
                        (AntArtifactProvider) project.getLookup().
                        lookup(AntArtifactProvider.class);
                assert antArtifactProvider != null;
                
                AntArtifact[] artifacts =
                        antArtifactProvider.getBuildArtifacts();
                boolean success =
                        new AddProjectAction().addProject(jbiProject, artifacts[0]);
                
                if (success) {
                    mAddProjects.remove(key);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Delete all paneding SE projects from queue to the CompApp project
     */
    public void deletePendingProjects() {
        try {
            Project jbiProject = getJBIProject();
            
            for (Iterator it = mDeleteProjects.keySet().iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                String artifactName = mDeleteProjects.get(key);
                
                DeleteModuleAction deleteModuleAction =
                        (DeleteModuleAction) SystemAction.get(DeleteModuleAction.class);
                
                boolean success =
                        deleteModuleAction.removeProject(jbiProject, artifactName);
                
                if (success) {
                    mDeleteProjects.remove(key);
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Adds a component project into compapp project.
     *
     * @param project   a component project
     * @param x         x location of the new service unit in the editor
     * @param y         y location of the new service unit in the editor
     */
    public void addInternalJBIModule(Project project, String type, int x, int y) {
        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        
        if (mDeleteProjects.containsKey(projectName)) {
            mDeleteProjects.remove(projectName);
        } else {
            mAddProjects.put(projectName, project); // todo: needs to fix duplicate proj names..
        }
        
        addEngineServiceUnit(projectName, type, // trimVersion(type),
                true,  // is internal SESU
                false, // is known
                x, y);
    }
    
    private String trimVersion(String type) {
        if (type == null) {
            return type;
        }
        int i = type.indexOf('-');
        if (i < 0) {
            return type;
        }
        return type.substring(0, i);
    }
    
    /**
     * Deletes a service engine service unit, all the connections connecting
     * the service unit and possibly WSDL endpoints.
     *
     * @param serviceUnit       an existing service engine service unit
     */
    public void removeServiceEngineServiceUnit(CasaServiceEngineServiceUnit seSU) {
        
        if (seSU.isInternal() && !seSU.isUnknown()) {
            String projectName = seSU.getUnitName();
            if (mAddProjects.containsKey(projectName)) {
                mAddProjects.remove(projectName);
            } else {
                mDeleteProjects.put(projectName, seSU.getArtifactsZip());
            }
        }
        
        CasaServiceUnits sus = getRootComponent().getServiceUnits();
        
        Set<CasaPort> connectingCasaPorts = new HashSet<CasaPort>();
        
        // 1. Delete connections
        for (CasaEndpointRef endpointRef : seSU.getEndpoints()) {
            List<CasaConnection> visibleConnections =
                    getConnections(endpointRef, false); // don't include "deleted" connection
            for (CasaConnection visibleConnection : visibleConnections) {
                
                // find connecting casa ports
                CasaEndpointRef casaConsumes =
                        getCasaEndpointRef(visibleConnection.getConsumer().get());
                CasaPort casaPort = getCasaPort(casaConsumes);
                if (casaPort != null) {
                    connectingCasaPorts.add(casaPort);
                }
                
                CasaEndpointRef casaProvides =
                        getCasaEndpointRef(visibleConnection.getProvider().get());
                casaPort = getCasaPort(casaProvides);
                if (casaPort != null) {
                    connectingCasaPorts.add(casaPort);
                }
                
                removeConnection(visibleConnection, true); // hard delete connections?
            }
        }
        
        // 2. Delete WSDL endpoints
        for (CasaPort casaPort : connectingCasaPorts) {
            // Here we only want to delete those casa ports that come from the
            // component projects.
            if (!isDefinedInCompApp(casaPort)) {
                // Delete the casa port if there is no (even deleted) connections
                // connecting it any more.
                if (getConnections(casaPort, true).size() == 0) { // casaport not connected
                    removeCasaPort(casaPort, true, //false, // the flag doesn't matter, there should be no connections
                            true); // hard delete the casa port
                }
            }
        }
        
        // 3. Remember the endpoints inside the service unit. We might have some
        // cleanup work to do.
        List<CasaEndpointRef> endpointRefs = seSU.getEndpoints();
        List<CasaEndpoint> endpoints = new ArrayList<CasaEndpoint>();
        for (CasaEndpointRef endpointRef : endpointRefs) {
            endpoints.add(endpointRef.getEndpoint().get());
        }
        
        // 4. Delete the casa service unit
        CasaServiceUnits casaSUs = getRootComponent().getServiceUnits();
        startTransaction();
        try {
            casaSUs.removeServiceEngineServiceUnit(seSU);
        } finally {
            if (isIntransaction()) {
                fireServiceEngineServiceUnitRemoved(seSU);
                endTransaction();
            }
        }
        
        // 5. Clean up dangling endpoints
        for (CasaEndpoint endpoint : endpoints) {
            removeDanglingEndpoint(endpoint);
        }
    }
    
    // TMP: remove me
    public String getServiceUnitName(CasaServiceEngineServiceUnit casaSESU) {
        return casaSESU.getName();
    }
    
    // TMP: remove me
    public String getServiceUnitName(CasaBindingComponentServiceUnit casaBCSU) {
        return casaBCSU.getName();
    }
    
    // TMP: remove me
    public String getServiceUnitComponentName(CasaBindingComponentServiceUnit casaBCSU) {
        return casaBCSU.getComponentName();
    }
    
    // TMP: remove me
    public String getServiceUnitComponentName(CasaServiceEngineServiceUnit casaSESU) {
        return casaSESU.getComponentName();
    }
    
    public String getServiceUnitComponentName(ServiceUnit su) {
        if (su == null) {
            return "FIXME: getServiceUnitComponentName"; // TMP
        }
        String componentName = su.getTarget().getComponentName();
        /*
        int javaPackageNameIndex = componentName.lastIndexOf('.') + 1;
        if (javaPackageNameIndex > 0 && javaPackageNameIndex < componentName.length()) {
            // Assume java package naming scheme: com.foo.myse-version
            // Just use the last part (before the version) as the actual name.
            int versionIndex = componentName.indexOf('-');
            if (versionIndex >= 0) {
                // Strip version information.
                componentName = componentName.substring(0, versionIndex).toUpperCase();
                javaPackageNameIndex = componentName.lastIndexOf('.') + 1;
                if (javaPackageNameIndex > 0 && javaPackageNameIndex < componentName.length()) {
                    componentName = componentName.substring(javaPackageNameIndex).toUpperCase();
                }
            } else {
                componentName = componentName.substring(javaPackageNameIndex).toUpperCase();
            }
            componentName = componentName.substring(javaPackageNameIndex).toUpperCase();
        }
        */
        return componentName.toUpperCase();
    }
    
    // TMP: remove me
    public String getEndpointName(CasaEndpointRef casaEndpointRef) {
        return casaEndpointRef.getEndpoint().get().getEndpointName();
    }
    public QName getInterfaceQName(CasaEndpointRef casaEndpointRef) {
        return casaEndpointRef.getEndpoint().get().getInterfaceQName();
    }
    public QName getServiceQName(CasaEndpointRef casaEndpointRef) {
        return casaEndpointRef.getEndpoint().get().getServiceQName();
    }
    
    /**
     * Gets the owning compapp project.
     */
    public Project getJBIProject() throws IOException {
        ModelSource modelSource = getModelSource();
        Lookup lookup = modelSource.getLookup();
        FileObject casaFO = (FileObject) lookup.lookup(FileObject.class);
        FileObject projectFO = casaFO.getParent().getParent().getParent();
        return ProjectManager.getDefault().findProject(projectFO);
    }
    
    public void buildCompApp() {
        try {
            Project jbiProject = getJBIProject();
            ActionProvider actionProvider =
                    (ActionProvider) jbiProject.getLookup().lookup(ActionProvider.class);
            actionProvider.invokeAction(ActionProvider.COMMAND_BUILD, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Gets a list of casa connections that connecting the given endpoint.
     *
     * @param endpoint          an endpoint
     * @param includeDeleted    whether to include connections that are marked as deleted
     */
    private List<CasaConnection> getConnections(CasaEndpointRef endpointRef,
            boolean includeDeleted) {
        List<CasaConnection> ret = new ArrayList<CasaConnection>();
        
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        for (CasaConnection connection : getCasaConnectionList(includeDeleted)) {
            if (connection.getConsumer().get() == endpoint ||
                    connection.getProvider().get() == endpoint) {
                ret.add(connection);
            }
        }
        
        return ret;
    }
    
    private String getUniqueEndpointID() {
        List<String> existingNames = new ArrayList<String>();
        
        for (CasaEndpoint casaEndpoint : getRootComponent().getEndpoints().getEndpoints()) {
            String name = casaEndpoint.getName();
            existingNames.add(name);
        }
        
        String newName = null;
        for (int i = 1; i < 1000; i++) {
            newName = "endpoint" + i;
            if (!existingNames.contains(newName)) {
                break;
            }
        }
        
        return newName;
    }
    
    private String getUniquePortTypeName(WSDLModel wsdlModel) {
        List<String> existingNames = new ArrayList<String>();
        
        for (PortType portType : wsdlModel.getDefinitions().getPortTypes()) {
            String name = portType.getName();
            existingNames.add(name);
        }
        
        String newName = null;
        for (int i = 1; i < 1000; i++) {
            newName = "casaPortType" + i;
            if (!existingNames.contains(newName)) {
                break;
            }
        }
        
        return newName;
    }
    
    private String getUniqueBindingName(WSDLModel wsdlModel) {
        List<String> existingNames = new ArrayList<String>();
        
        for (Binding binding : wsdlModel.getDefinitions().getBindings()) {
            String name = binding.getName();
            existingNames.add(name);
        }
        
        String newName = null;
        for (int i = 1; i < 1000; i++) {
            newName = "casaBinding" + i;
            if (!existingNames.contains(newName)) {
                break;
            }
        }
        
        return newName;
    }
    
    private String getUniqueServiceName(WSDLModel wsdlModel) {
        List<String> existingNames = new ArrayList<String>();
        
        for (Service service : wsdlModel.getDefinitions().getServices()) {
            String name = service.getName();
            existingNames.add(name);
        }
        
        String newName = null;
        for (int i = 1; i < 1000; i++) {
            newName = "casaService" + i;
            if (!existingNames.contains(newName)) {
                break;
            }
        }
        
        return newName;
    }
    
    private String getUniquePortName(WSDLModel wsdlModel) {
        List<String> existingNames = new ArrayList<String>();
        
        for (Service service : wsdlModel.getDefinitions().getServices()) {
            for (Port port : service.getPorts()) {
                String name = port.getName();
                existingNames.add(name);
            }
        }
        
        String newName = null;
        for (int i = 1; i < 1000; i++) {
            newName = "casaPort" + i;
            if (!existingNames.contains(newName)) {
                break;
            }
        }
        
        return newName;
    }
    
    /**
     * Gets the CASA WSDL Model.
     */
    private WSDLModel getWSDLModel(final String uriString) {
        
        ModelSource modelSource = getModelSource();
        Lookup lookup = modelSource.getLookup();
        CatalogModel catalogModel = (CatalogModel) lookup.lookup(CatalogModel.class);
        
        ModelSource wsdlModelSource = null;
        try {
            wsdlModelSource =
                    catalogModel.getModelSource(new URI(uriString), modelSource);
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        
        if (wsdlModelSource != null) {
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(wsdlModelSource);
            assert wsdlModel.getDefinitions() != null;
            return wsdlModel;
        } else {
            System.out.println("WARNING: getWSDLModel() == null, uriString is " + uriString);
            return null;
        }
    }
    
    private WSDLModel getCasaWSDLModel(boolean create) {
        ModelSource modelSource = getModelSource();
        Lookup lookup = modelSource.getLookup();
        CatalogModel catalogModel = (CatalogModel) lookup.lookup(CatalogModel.class);
        FileObject fo = (FileObject) lookup.lookup(FileObject.class);
        URI uri = null;
        try {
            uri = new URI(CASA_WSDL_RELATIVE_LOCATION + CASA_WSDL_FILENAME);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        
        ModelSource wsdlModelSource = null;
        try {
            wsdlModelSource = catalogModel.getModelSource(uri, modelSource);
        } catch (CatalogModelException ex) {
            ; // ex.printStackTrace();
        }
        
        if (wsdlModelSource == null && create) {
            FileObject casaWSDLDirFO = //fo.getFileObject(CASA_WSDL_RELATIVE_LOCATION);
                    fo.getParent().getParent().getFileObject("jbiasa");
            File file = new File(FileUtil.toFile(casaWSDLDirFO), CASA_WSDL_FILENAME);
            createEmptyCasaWSDLFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);
            // try again
            try {
                wsdlModelSource = catalogModel.getModelSource(uri, modelSource);
            } catch (CatalogModelException ex) {
                ex.printStackTrace();
            }
        }
        
        if (wsdlModelSource != null) {
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(wsdlModelSource);
            /*
            if (firstTimeLoadingCasaWSDLModel) {
                firstTimeLoadingCasaWSDLModel = false;
                wsdlModel.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        markDirty();
                    }
                });
                wsdlModel.addComponentListener(new ComponentListener() {
                    public void valueChanged(ComponentEvent componentEvent) {
                        markDirty();
                    }
                    
                    public void childrenAdded(ComponentEvent componentEvent) {
                        markDirty();
                    }
                    
                    public void childrenDeleted(ComponentEvent componentEvent) {
                        markDirty();
                    }
                });
            }
            */
            return wsdlModel;
        } else {
            return null;
        }
    }
    /*
    private boolean firstTimeLoadingCasaWSDLModel = true;
    
    private void markDirty() {
        DataObject dataObject = getDataObject(this);
        if (!dataObject.isModified()) {
            dataObject.setModified(true);
        }
    }*/
    
    /*public Map<String, String> getCasaPortAttributes(final CasaPort casaPort) {
        Map<String, String> ret = new HashMap<String, String>();
     
        CasaLink link = casaPort.getLink();
        String href = link.getHref();
        Port port = null;
        try {
            port = (Port) getWSDLComponentFromXLinkHref(href);
     
            // port name
            ret.put("name", port.getName());
     
            // port children attributes
            for (org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
                    extElement : port.getExtensibilityElements()) {
                        Map<QName, String> attributes = extElement.getAttributeMap();
                        for (QName qname : attributes.keySet()) {
                            String attrValue = attributes.get(qname);
                            ret.put(qname.getLocalPart(), attrValue);
                        }
                    }
     
                    // porttype name
                    Binding binding = port.getBinding().get();
                    PortType porttype = binding.getType().get();
                    ret.put("porttype", porttype.getName());
     
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
     
        return ret;
    }
     
    public void setCasaPortAttribute(final CasaPort casaPort, String name, String value) {
     
        CasaLink link = casaPort.getLink();
        String href = link.getHref();
        Port port = null;
        try {
            port = (Port) getWSDLComponentFromXLinkHref(href);
     
            if (name.equals("name")) { // port name
                port.setName(value);
            } else if (name.equals("porttype")) { // porttype name
                Binding binding = port.getBinding().get();
                PortType porttype = binding.getType().get();
                porttype.setName(value);
            } else { // port children attributes
                for (org.netbeans.modules.xml.wsdl.model.ExtensibilityElement
                        extElement : port.getExtensibilityElements()) {
                            Map<QName, String> attributes = extElement.getAttributeMap();
                            for (QName qname : attributes.keySet()) {
                                String attrValue = attributes.get(qname);
                                if (qname.getLocalPart().equals(name)) {
                                    extElement.setAnyAttribute(qname, value);
                                    return;
                                }
                            }
                        }}
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
    }*/
    
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String CASA_WSDL_TNS = "http://whatever"; //http://localhost/SynchronousSample/SynchronousSample";
    
    private void createEmptyCasaWSDLFile(File file) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.write(NEWLINE);
            out.write("<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\"");
            out.write(NEWLINE);
            out.write("             xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"");
            out.write(NEWLINE);
            out.write("             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"");
            out.write(NEWLINE);
            out.write("             targetNamespace=\"" + CASA_WSDL_TNS + "\"");
            
            
            out.write(NEWLINE);
            out.write("             xmlns:myns=\"" + CASA_WSDL_TNS + "\"");
            out.write(NEWLINE);
            out.write("             name=\"casawsdl\">");
            out.write(NEWLINE);
            out.write("</definitions>");
            out.write(NEWLINE);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Gets a list of Connections that involve the given WSDL endpoint.
     */
    private List<CasaConnection> getConnections(CasaPort casaPort,
            boolean includeDeleted) {
        List<CasaConnection> ret = new ArrayList<CasaConnection>();
        
        CasaConsumes consumes = casaPort.getConsumes();
        if (consumes != null) {
            ret.addAll(getConnections(consumes, includeDeleted));
        }
        
        CasaProvides provides = casaPort.getProvides();
        if (provides != null) {
            ret.addAll(getConnections(provides, includeDeleted));
        }
        
        return ret;
    }
    
    /**
     * Checks whether a service unit is editable.
     * For now only external service units are editable.
     */
    public boolean isEditable(CasaServiceEngineServiceUnit su) {
        return !su.isInternal();
    }
    
    public boolean isEditable(CasaServiceEngineServiceUnit su, String propertyName) {
        if (!isEditable(su)) {
            return false;
        }
        
        if (CasaAttribute.UNIT_NAME.getName().equals(propertyName) ||
                CasaAttribute.DESCRIPTION.getName().equals(propertyName)) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isEditable(CasaBindingComponentServiceUnit su) {
        return false;
    }
    
    /**
     * Checks whether a CasaPort is editable.
     * Only user-created ones are editable. CasaPorts that come from component
     * projects are not editable.
     */
    public boolean isEditable(CasaPort casaPort) {
        return isDefinedInCompApp(casaPort);
    }
    
    // TODO
    public boolean isEditable(CasaPort casaPort, String propertyName) {
        return isEditable(casaPort);
    }
    
    /**
     * Checks whether an endpoint is editable.
     * Only endpoints in external service units are editable.
     * @deprecated use #isEditable(CasaEndpoint, String) instead
     */
    public boolean isEditable(CasaEndpointRef endpointRef) {
        CasaServiceEngineServiceUnit seSU =
                getCasaEngineServiceUnit(endpointRef);
        return seSU != null && !seSU.isInternal();
    }
    
    /**
     * Checks whether a particular property of an endpoint is editable.
     */
    public boolean isEditable(CasaEndpointRef endpointRef, String propertyName) {
        if (!isEditable(endpointRef)) {
            return false;
        }
        
        // Can't edit internface name when there is visible connection
        if (getConnections(endpointRef, false).size() > 0 &&
                JBIAttributes.INTERFACE_NAME.getName().equals(propertyName)) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Checks whether a connection is editable.
     * Connections are never editable.
     */
    public boolean isEditable(CasaConnection connection) {
        return false;
    }
    
    private boolean isDefinedInCompApp(CasaPort casaPort) {
        CasaLink link = casaPort.getLink();
        String linkHref = link.getHref();
        return linkHref.startsWith("../jbiasa/casa.wsdl#xpointer");
    }
    
    /**
     * Checks whether a service unit is deletable. For now a service unit is
     * always deletable.
     */
    public boolean isDeletable(CasaServiceEngineServiceUnit su) {
        return true;
    }
    
    public boolean isDeletable(CasaBindingComponentServiceUnit su) {
        return true;
    }
    
    /**
     * Checks whether a WSDL endpoint is deletable.
     */
    public boolean isDeletable(CasaPort casaPort) {
        return true;
    }
    
    /**
     * Checks whether an endpoint is deletable. An endpoint is deletable if
     * and only if it belongs to an external service engine service unit.
     */
    public boolean isDeletable(CasaEndpointRef endpointRef) {
        CasaServiceEngineServiceUnit casaSESU =
                getCasaEngineServiceUnit(endpointRef);
        return casaSESU != null && !casaSESU.isInternal();
    }
    
    /**
     * Checks whether a connection is deletable. A connection is always deletable,
     * although for auto-generated connections, a delete is actually a hide.
     */
    public boolean isDeletable(CasaConnection connection) {
        return true;
    }
    
    /**
     * Sets name of an endpoint.
     */
    private void setEndpointName(CasaComponent component, CasaEndpointRef endpointRef, String endpointName) {
        CasaEndpoint casaEndpoint = endpointRef.getEndpoint().get();
       
        if (!casaEndpoint.getEndpointName().equals(endpointName)) {
            startTransaction();
            try {
                casaEndpoint.setEndpointName(endpointName);
            } finally {
                if (isIntransaction()) {
                    fireCasaEndpointRenamed(component);
                    endTransaction();
                }
            }
        }
    }
    
    // TMP
    private void clearCasaEndpointInterfaceName(CasaEndpointRef endpointRef) {
        setEndpointInterfaceQName(endpointRef, new QName(""));
    }
    
    public void setEndpointInterfaceQName(CasaEndpointRef endpointRef,
            QName interfaceQName) {
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        
        startTransaction();
        try {
            endpoint.setInterfaceQName(interfaceQName);
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
    }
    
    public void setEndpointServiceQName(CasaEndpointRef endpointRef,
            QName serviceQName) {
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        
        startTransaction();
        try {
            endpoint.setServiceQName(serviceQName);
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
    }
    
    public CasaRegion getCasaRegion(CasaRegion.Name regionName) {
        String name = regionName.getName();
        List<CasaRegion> regions = getRootComponent().getRegions().getRegions();
        for (CasaRegion region : regions) {
            if (region.getName().equals(name)) {
                return region;
            }
        }
        
        assert false : "Unknown casa region: " + name;
        return null;
    }
    
    public void setCasaRegionWidth(CasaRegion casaRegion, int width) {        
        startTransaction();
        try {
            casaRegion.setWidth(width);
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
    }
    
    public void setUnitName(CasaServiceUnit su, String unitName) {           
        startTransaction();
        try {
            su.setUnitName(unitName);
        } finally {
            if (isIntransaction()) {
                fireServiceUnitRenamed(su);
                endTransaction();
            }
        }
    }
    /**
     * Gets all the namespaces defined in the SA jbi model.
     *
     * @return a map mapping namespace prefix to namespace URI
     */
    public Map<String, String> getNamespaces() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.putAll(getDocumentModelNamespaces(this));
        return ret;
    }
    
    /**
     * Gets all the namespaces defined in any document model.
     *
     * @return a map mapping namespace prefix to namespace URI
     */
    private static Map<String, String> getDocumentModelNamespaces(
            AbstractDocumentModel model) {
        Map<String, String> ret = new HashMap<String, String>();
        
        NamedNodeMap map = model.getRootComponent().getPeer().getAttributes();
        for (int j = 0; j < map.getLength(); j++) {
            Node node = map.item(j);
            
            String prefix = node.getPrefix();
            if (prefix != null && prefix.equals("xmlns")) {
                String localName = node.getLocalName();
                if (! localName.equals("xsi")) { // TMP
                    String namespaceURI = node.getNodeValue();
//                    System.out.println(
//                            " prefix=" + prefix +
//                            " localName: " + localName +
//                            " namespaceURI: " + namespaceURI);
                    ret.put(localName, namespaceURI);
                }
            }
        }
        
        return ret;
    }
    
// TODO This is only an example of how a sync might be done.
    public void sync() throws IOException {
        // This should reset the internal fields/state of saJBIModel
        // as well as make a call to saJBIModel.super.sync()
        super.sync();
        
        WSDLModel casaWSDLModel = getCasaWSDLModel(false);
        
        if (casaWSDLModel != null) {
            casaWSDLModel.sync();
        }
        
        cachedReferences.clear();
        
        // Probably need to also reset all internal fields/state
        // of the su jbi models.
        // cachedReferences.clear();
        
        // This seems necessary to update the extensibility elements
        // when switching from source -> design view.
        // Perhaps this introduces a performance hit?
        super.refresh();
        
        // At the very end, we need to fire a property change event
        // so that the view can update itself.
        mSupport.firePropertyChange(new PropertyChangeEvent(
                this,
                PROPERTY_MODEL_RELOAD,
                null,
                null));
    }
}


