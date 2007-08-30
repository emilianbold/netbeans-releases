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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaAttribute;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaModelImpl;
import org.netbeans.modules.compapp.casaeditor.model.jbi.impl.JBIAttributes;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBindingInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectHelper;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.DeleteModuleAction;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.ui.wizard.BindingGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.WizardBindingConfigurationStep;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ValidationInfo;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
    public static final String PROPERTY_ENDPOINT_NAME_CHANGED     = PROPERTY_PREFIX + "endpoint_renamed";        // NOI18N
    public static final String PROPERTY_ENDPOINT_INTERFACE_QNAME_CHANGED    = PROPERTY_PREFIX + "endpoint_interface_qname_changed"; // NOI18N
    public static final String PROPERTY_ENDPOINT_SERVICE_QNAME_CHANGED      = PROPERTY_PREFIX + "endpoint_service_qname_changed";   // NOI18N
    public static final String PROPERTY_SERVICE_UNIT_RENAMED = PROPERTY_PREFIX + "service_unit_renamed";    // NOI18N
    public static final String PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_ADDED   = PROPERTY_PREFIX + "service_unit_added";   // NOI18N
    public static final String PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_REMOVED = PROPERTY_PREFIX + "service_unit_removed"; // NOI18N
    
    private static final String CASA_WSDL_RELATIVE_LOCATION = "../jbiasa/";     // NOI18N
    private static final String JBI_SERVICE_UNITS_DIR = "jbiServiceUnits";      // NOI18N
    private static final String DUMMY_PORTTYPE_NAME = "dummyCasaPortType";      // NOI18N
               
    private PropertyChangeSupport mSupport = new PropertyChangeSupport(this);
    
    // 01/24/07, added to support add/del SU projects
    // mapping project names to projects for pending projects to be added
    private Map<String, Project> mAddProjects = new HashMap<String, Project>();
    // mapping project names to project artifact names for pending projects to be deleted.
    private Map<String, String> mDeleteProjects = new HashMap<String, String>();
        
    // mapping WSDL port link hrefs to WSDL components
    private Map<String, Object> cachedWSDLComponents = new HashMap<String, Object>();
    
    private List<String> artifactTypes = new ArrayList<String>();
    
//    // mapping binding component name to binding type, 
//    // e.x, "sun-smtp-binding" -> "smtp".
//    private Map<String, String> bcName2BindingType;
//    
//    // mapping binding type to binding component name, 
//    // e.x, "smtp" -> "sun-smtp-binding".
//    private Map<String, String> bindingType2BcName;
    
    
    /** Creates a new instance of CasaWrapperModel */
    public CasaWrapperModel(ModelSource source) {
        super(source);
        
        // ?
//        try {
//            sync();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
        
        artifactTypes.add(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA);
//        buildBindingComponentMaps();
    }
        
    public void removePropertyChangeListener(final PropertyChangeListener pcl) {
        super.removePropertyChangeListener(pcl);
        mSupport.removePropertyChangeListener(pcl);
    }
    
    /**
     * Add property change listener which will receive events for any element
     * in the underlying sa jbi model.
     */
    public void addPropertyChangeListener(final PropertyChangeListener pcl) {
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
    
    /**
     * Tests if a service engine service unit with the given unit name exists.
     */
    public boolean existingServiceEngineServiceUnit(String unitName) {
        for (CasaServiceEngineServiceUnit serviceUnit : getServiceEngineServiceUnits()) {
            if (unitName.equalsIgnoreCase(serviceUnit.getUnitName())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets all the visible WSDL endpoints in the CASA model.
     */
    public List<CasaPort> getCasaPorts() {
        List<CasaPort> ret = new ArrayList<CasaPort>();        
        for (CasaBindingComponentServiceUnit bcSU : getBindingComponentServiceUnits()) {
            for (CasaPort casaPort : bcSU.getPorts().getPorts()) {
                if (! CasaPortState.DELETED.getState().equals(casaPort.getState())) {
                    ret.add(casaPort);
                }
            }
        }        
        return ret;
    }
    
    /** 
     * Gets the target binding component name of a casa port.
     */ 
    public String getBindingComponentName(final CasaPort casaPort) { 
        CasaBindingComponentServiceUnit bcSU =
                (CasaBindingComponentServiceUnit) casaPort.getParent().getParent();
        return bcSU.getComponentName();
    }
    
    public String getBindingType(final CasaPort casaPort) {
        String bindingType = casaPort.getBindingType();
        if (bindingType == null) {
            //IZ: 113545
            try {
                JbiBindingInfo bi = getBindingInfo(getLinkedWSDLPort(casaPort));
                if (bi != null) {
                    return bi.getBindingName();
                }
            } catch (Exception ex) {
                // skip to use the default one..
            }
            String bcCompName = getBindingComponentName(casaPort);
            bindingType = getDefaultBindingComponents().get(bcCompName);
        }
        return bindingType;
    }

    /**
     * Sets the endpoint name of a casa port.
     */
    public void setEndpointName(final CasaPort casaPort, String endpointName) { 
        if (!isDefinedInCompApp(casaPort)) {
            throw new RuntimeException("Cannot change endpoint defined in component projects."); // NOI18N
        }
        
        CasaEndpointRef endpointRef = casaPort.getConsumes();
        if (endpointRef == null) {
            endpointRef = casaPort.getProvides();
        }
        assert endpointRef != null;
        setEndpointName(casaPort, endpointRef, endpointName);
    }

    /**
     * Sets the endpoint name of a casa consumes or casa provides.
     */
    public void setEndpointName(final CasaEndpointRef endpointRef, 
            String endpointName) {
        CasaPort casaPort = getCasaPort(endpointRef);
        if (casaPort == null) {
            CasaServiceEngineServiceUnit seSU = getCasaEngineServiceUnit(endpointRef);
            if (seSU.isInternal()) {
                throw new RuntimeException("Cannot change endpoint in internal JBI modules."); // NOI18N                
            }
        } else if (!isDefinedInCompApp(casaPort)) {
            throw new RuntimeException("Cannot change endpoint defined in component projects."); // NOI18N
        }
        
        setEndpointName(endpointRef, endpointRef, endpointName);
    }
        
    /**
     * Gets the WSDL port linked by the casaport
     *
     * @param port  the casaport
     * @return  the wsdl port linked by the casaport
     */
    public Port getLinkedWSDLPort(final CasaPort casaPort) { 
        String linkHref = casaPort.getLink().getHref();        
        try {
            Port port = (Port) getWSDLComponentFromXLinkHref(linkHref);
            return port;
        } catch (Exception ex) {
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
        
        WSDLComponent wsdlComponent = 
                (WSDLComponent) cachedWSDLComponents.get(linkHref);
        
        if (wsdlComponent == null) {
            String regex = "(.*)#xpointer\\((.*)\\)";       // NOI18N
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(linkHref);
            
            if (! matcher.matches()) {
                throw new IllegalArgumentException(
                        "Invalid xlink href: " + linkHref); // NOI18N
            }
            
            String uriString = matcher.group(1);
            String xpathString = matcher.group(2);
            
            WSDLModel wsdlModel = getWSDLModel(uriString);
            
            if (wsdlModel != null) {
                WSDLComponent root = wsdlModel.getRootComponent();
                wsdlComponent =
                        new FindWSDLComponent().findComponent(root, xpathString);
                cachedWSDLComponents.put(linkHref, wsdlComponent);
            } else {
                System.out.println("WARNING: WSDL model for " + linkHref + // NOI18N
                        " is (temporarily) unavailable.");   // NOI18N
            }
        }
        
        return wsdlComponent;
    }
        
    /**
     * Gets the casa provides or consumes endpoint from a casa connection.
     */
    public CasaEndpointRef getCasaEndpointRef(
            final CasaConnection casaConnection,
            boolean isConsumes) {
        CasaEndpoint casaEndpoint = isConsumes ?
            casaConnection.getConsumer().get() :
            casaConnection.getProvider().get();
        return getCasaEndpointRef(casaEndpoint, isConsumes);
    }
    
    /**
     * Gets the CasaConsumes or CasaProvides that links to the given endpoint.
     */
    private CasaEndpointRef getCasaEndpointRef(final CasaEndpoint endpoint, 
            boolean isConsumes) {
        
        for (CasaServiceEngineServiceUnit seSU : getServiceEngineServiceUnits()) {
            for (CasaEndpointRef endpointRef : seSU.getEndpoints()) {
                if (((isConsumes && endpointRef instanceof CasaConsumes) ||
                        (!isConsumes && endpointRef instanceof CasaProvides)) &&
                        endpointRef.getEndpoint().get() == endpoint) {
                    return endpointRef;
                }
            }
        }
        
        for (CasaBindingComponentServiceUnit bcSU : getBindingComponentServiceUnits()) {
            for (CasaPort casaPort : bcSU.getPorts().getPorts()) {
                CasaEndpointRef endpointRef = isConsumes ? 
                    casaPort.getConsumes() : casaPort.getProvides();
                if (endpointRef != null &&
                        endpointRef.getEndpoint().get() == endpoint) {
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
            final CasaServiceEngineServiceUnit seSU, int x, int y) {
        
        if (seSU.getX() == x && seSU.getY() == y) {
            return;
        }
        
        startTransaction();
        try {
            seSU.setX(x);
            seSU.setY(y);
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
    public CasaConnection addConnection(final CasaConsumes consumes,
            final CasaProvides provides) {
        return addConnection(consumes, provides, true);
    }
    
    /**
     * Adds a brand new connection or mark a previously deleted connection
     * as unchanged.
     * 
     * @param consumes  a casa consumes endpoint
     * @param provides  a casa provides endpoint
     * @param direction the (mouse) direction during the connection creation.
     *                  If <code>true</code>, the "source" endpoint is the 
     *                  consumes endpoint; if <code>false</code>, the "source" 
     *                  endpoint is the provides endpoint.
     */
    public CasaConnection addConnection(final CasaConsumes consumes,
            final CasaProvides provides, boolean direction) {
                
        if (!canConnect(consumes, provides)) {
            throw new RuntimeException(
                    "The two endpoints cannot be connected because of their incompatible interfaces."); 
        }
        
        // Adjust the incompatible BC interface first, if necessary.
        boolean endpoint1Defined = isEndpointDefined(consumes);
        boolean endpoint2Defined = isEndpointDefined(provides);
        if (endpoint1Defined && endpoint2Defined) {
            QName consumesInterfaceQName = consumes.getInterfaceQName();
            QName providesInterfaceQName = provides.getInterfaceQName();
            if (!consumesInterfaceQName.equals(providesInterfaceQName)) {
                CasaPort casaPort1 = getCasaPort(consumes);
                CasaPort casaPort2 = getCasaPort(provides);
                boolean isFreeEditablePort1 = casaPort1 != null && 
                        isEditable(casaPort1) &&
                        getConnections(casaPort1, false).size() == 0; // the flag here doesn't matter
                boolean isFreeEditablePort2 = casaPort2 != null && 
                        isEditable(casaPort2) &&
                        getConnections(casaPort2, false).size() == 0; // the flag here doesn't matter 
                
                 if (isFreeEditablePort2 && direction) {
                    setEndpointInterfaceQName(provides, null);
                } else {
                    setEndpointInterfaceQName(consumes, null);
                }
            }
        }
        
        CasaConnection connection = getCasaConnection(consumes, provides);
        
        if (connection == null) {
            connection = addNewConnection(consumes, provides);
        } else {
            String state = connection.getState();
            if (!ConnectionState.DELETED.getState().equals(state)) {
                throw new RuntimeException(
                        "Cannot add a connection that is marked as \"new\" or \"unchanged\""); 
            }
            setConnectionState(connection, ConnectionState.UNCHANGED);
        }
        
        return connection;
    }
    
    /**
     * Adds a brand new connection between the given two endpoints.
     */
    private CasaConnection addNewConnection(final CasaConsumes consumes,
            final CasaProvides provides) {
        
        CasaEndpointRef definedEndpointRef = null;
        CasaEndpointRef undefinedEndpointRef = null;
        
        if (isEndpointDefined(consumes)) {
            if (!isEndpointDefined(provides)) {
                definedEndpointRef = consumes;
                undefinedEndpointRef = provides;
            }
        } else {
            if (isEndpointDefined(provides)) {
                definedEndpointRef = provides;
                undefinedEndpointRef = consumes;
            }
        }
        
        if (definedEndpointRef != null && undefinedEndpointRef != null) {
            // Populate the interface name of the undefined endpoint
            QName interfaceQName = definedEndpointRef.getInterfaceQName();
            setEndpointInterfaceQName(undefinedEndpointRef, interfaceQName);
        }
        
        // Create a new casa connection
        CasaComponentFactory casaFactory = getFactory();
        CasaConnection casaConnection = casaFactory.createCasaConnection();
        casaConnection.setState(ConnectionState.NEW.getState());
        casaConnection.setConsumer(
                casaConnection.createReferenceTo(consumes.getEndpoint().get(), 
                CasaEndpoint.class));
        casaConnection.setProvider(
                casaConnection.createReferenceTo(provides.getEndpoint().get(), 
                CasaEndpoint.class));
        
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
    
    private static void populateBindingAndPort(final CasaPort casaPort, 
            final Port port, final QName interfaceQName, String bType) {
        
        CasaWrapperModel casaWrapperModel = (CasaWrapperModel) casaPort.getModel();
        PortType portType = casaWrapperModel.getPortType(interfaceQName); //casaWrapperModel.getCasaPortType(casaPort);
//        System.out.println("Got WSDLEndpoint Action.. Pt: " + portType);
        
        String wsdlLocation = casaWrapperModel.getWSDLLocation(interfaceQName);

//        System.out.println("Got WSDL location: " + wsdlLocation);
        
        ExtensibilityElementTemplateFactory factory = new ExtensibilityElementTemplateFactory();
        Collection<TemplateGroup> groups = factory.getExtensibilityElementTemplateGroups();
        Vector<LocalizedTemplateGroup> protocols = new Vector<LocalizedTemplateGroup>();
        LocalizedTemplateGroup ltg = null;
        LocalizedTemplateGroup bindingType = null;
        for (TemplateGroup group : groups) {
            ltg = factory.getLocalizedTemplateGroup(group);
            protocols.add(ltg);
            // System.out.println(bType+" Add Potocol: "+ltg.getName());
            if (ltg.getName().equalsIgnoreCase(bType)) {
                bindingType = ltg;
            }
        }
        if (bindingType == null) {      
            String msg = NbBundle.getMessage(CasaWrapperModel.class,
                    "MSG_FAIL_TO_POPULATE_BINDING", // NOI18N
                    bType == null ? "null" : bType.toUpperCase()); // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            throw new RuntimeException(msg);
        }
        
        Binding binding = port.getBinding().get();
        String bindingName = binding.getName();
        Service service = (Service) port.getParent();
        String serviceName = service.getName();
        String servicePortName = port.getName();
        LocalizedTemplate bindingSubType = null;
        LocalizedTemplate[] templates = bindingType.getTemplate();
        
        WSDLModel wsdlModel = port.getModel();
        Definitions definitions = wsdlModel.getDefinitions();
        
        boolean foundImport = false;
        if (wsdlLocation != null) {
            for (Import imprt : definitions.getImports()) {
                if (imprt.getNamespace().equals(interfaceQName.getNamespaceURI()) &&
                        imprt.getLocation().equals(wsdlLocation)) {
                    foundImport = true;
                    break;
                }
            }
        }
        Import newImport = null;
        if (wsdlLocation != null && !foundImport) {
            newImport = wsdlModel.getFactory().createImport();
            newImport.setNamespace(interfaceQName.getNamespaceURI());
            newImport.setLocation(wsdlLocation);
        }
        
        Map<String, Object> configurationMap = new HashMap<String, Object>();
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
                configurationMap.put(
                        WizardBindingConfigurationStep.BINDING_SUBTYPE, 
                        bindingSubType);
                
                if (binding != null) {
                    definitions.removeBinding(binding);
                }
                
                if (port != null) {
                    for (ExtensibilityElement ex : port.getExtensibilityElements()) {
                        port.removeExtensibilityElement(ex);
                    }
                }
                
                BindingGenerator bGen = 
                        new BindingGenerator(wsdlModel, portType, configurationMap);
                bGen.execute();
                
                binding = bGen.getBinding();
                List<ValidationInfo> vBindingInfos = 
                        bindingSubType.getMProvider().validate(binding);
                invalidBinding = vBindingInfos.size() > 0;
                //System.out.println(invalidBinding + " subType: " + // NOI18N
                //        bindingSubType.getName()); 
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
    public void removeConnection(final CasaConnection connection) {
        removeConnection(connection, false);
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
    private void removeConnection(final CasaConnection connection,
            boolean hardDelete) {
        String state = connection.getState();
        
        if (ConnectionState.DELETED.getState().equals(state)) {
            throw new RuntimeException(
                    "Trying to remove a connection that has already been marked as \"deleted\"");
        }
        
        // 1. Clear user-created WSDL endpoint's Consumes/Provides interface name
        // if there is no visible connections left after the connection deletion.
        // Existing WSDL endpoints are left alone because there might be "deleted"
        // connections hanging around.
        CasaEndpointRef casaConsumes = getCasaEndpointRef(connection, true);
        CasaEndpointRef casaProvides = getCasaEndpointRef(connection, false);
        
        for (CasaEndpointRef endpointRef : 
            new CasaEndpointRef[] {casaConsumes, casaProvides}) {
                
            CasaPort casaPort = getCasaPort(endpointRef);
            if (casaPort != null) {
//                if (isDefinedInCompApp(casaPort)) {
//                    if (getConnections(casaPort, false).size() == 1) { // this is the only visible connection left
//                        setEndpointInterfaceQName(endpointRef, null);
//                    }
//                }
            } else {
                CasaServiceEngineServiceUnit sesu = 
                        getCasaEngineServiceUnit(endpointRef);
                if (sesu != null && !sesu.isInternal()) { // endpoint belongs to external SESU
                    if (getConnections(endpointRef, false).size() == 1) { // this is the only visible connection left
                        setEndpointInterfaceQName(endpointRef, null);
                    }
                }
            }
        }
        
        // 2. Delete connection
        if (ConnectionState.NEW.getState().equals(state) || hardDelete) {
            // Remove casa connection
            CasaConnections casaConnections = 
                    (CasaConnections) connection.getParent();
            startTransaction();
            try {
                casaConnections.removeConnection(connection);
            } finally {
                if (isIntransaction()) {
                    fireConnectionRemoved(connection);
                    endTransaction();
                }
            }
        } else if (ConnectionState.UNCHANGED.getState().equals(state)) {
            setConnectionState(connection, ConnectionState.DELETED);
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
                    assert false : "Uh? setConnectionState: " + initialState + "->" + state.getState(); // NOI18N
                }
                endTransaction();
            }
        }
    }
    
    private void fireConnectionRemoved(final CasaConnection connection) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                connection,
                PROPERTY_CONNECTION_REMOVED,
                null, null));
    }
    
    private void fireConnectionAdded(final CasaConnection connection) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                connection,
                PROPERTY_CONNECTION_ADDED,
                null, null));
    }
    
    private void fireCasaPortAdded(final CasaPort casaPort) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                casaPort,
                PROPERTY_CASA_PORT_ADDED,
                null, null));
    }
    
    private void fireCasaPortRemoved(final CasaPort casaPort) {
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
    
    private void fireCasaEndpointRemoved(final CasaEndpointRef endpointRef) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                endpointRef,
                PROPERTY_ENDPOINT_REMOVED,
                null, null));
    }
    
    private void fireCasaEndpointRenamed(final CasaComponent component) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                component,
                PROPERTY_ENDPOINT_NAME_CHANGED,
                null, null));
    }
    
    private void fireCasaEndpointInterfaceQNameChanged(
            final CasaComponent component) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                component,
                PROPERTY_ENDPOINT_INTERFACE_QNAME_CHANGED,
                null, null));
    }
    
    private void fireCasaEndpointServiceQNameChanged(
            final CasaComponent component) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                component,
                PROPERTY_ENDPOINT_SERVICE_QNAME_CHANGED,
                null, null));
    }
    
    private void fireServiceUnitRenamed(final CasaServiceUnit su) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                su,
                PROPERTY_SERVICE_UNIT_RENAMED,
                null, null));
    }
    
    private void fireServiceEngineServiceUnitAdded(
            final CasaServiceEngineServiceUnit seSU) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                seSU,
                PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_ADDED,
                null, null));
    }
    
    private void fireServiceEngineServiceUnitRemoved(
            final CasaServiceEngineServiceUnit seSU) {
        firePropertyChangeEvent(new PropertyChangeEvent(
                seSU,
                PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_REMOVED,
                null, null));
    }
    
    /**
     * Tests if the two given endpoints can be connected or not.
     * Two endpoints can be connected if and only if ALL of the following
     * conditions hold true:
     * <UL>
     * <LI> one endpoint is a Consumes and the other is a Provides;
     * <LI> there is no existing visible connection involving the Consumes
     *      endpoint (there could be existing visible connections involving
     *      the Provides endpoint though);
     * <LI> at least one of the two endpoints has a defined interface;
     * <LI> if the two endpoints have incompatible defined interfaces, then
     *      one and only one endpoint must be a free editable BC port;     
     * <LI> the two endpoint cannot be both external endpoints;
     * <LI> the two endpoints don't belong to the same binding component;
     * </UL>
     */
    public boolean canConnect(final CasaEndpointRef endpointRef1,
            final CasaEndpointRef endpointRef2) {
        String unConnectableReason = 
                getUnConnectableReason(endpointRef1, endpointRef2);
        return unConnectableReason == null;
    }
    
    /**
     * Gets the reason why two given endpoints are not connectable.
     *
     * @return  a description why two endpoints are not connectable; null if
     *          the two endpoints are connectable.
     */
    public String getUnConnectableReason(final CasaEndpointRef endpointRef1,
            final CasaEndpointRef endpointRef2) {
        
        if (endpointRef1 instanceof CasaConsumes &&
                endpointRef2 instanceof CasaConsumes) {
            return NbBundle.getMessage(this.getClass(), 
                    "MSG_CANNOT_CONNECT_TWO_CONSUMES"); // NOI18N            
        }
                
        if (endpointRef1 instanceof CasaProvides &&
                endpointRef2 instanceof CasaProvides) {
            return NbBundle.getMessage(this.getClass(), 
                    "MSG_CANNOT_CONNECT_TWO_PROVIDES"); // NOI18N            
        }
        
        CasaEndpointRef consumes = (endpointRef1 instanceof CasaConsumes) ?
            endpointRef1 : endpointRef2;
        
        if (getConnections(consumes, false).size() > 0) {
            return NbBundle.getMessage(this.getClass(), 
                    "MSG_CANNOT_CONNECT_TWO_PROVIDES_WITH_ONE_CONSUMES"); // NOI18N            
        }
        
        CasaPort casaPort1 = getCasaPort(endpointRef1);
        CasaPort casaPort2 = getCasaPort(endpointRef2);
        if (casaPort1 != null && casaPort2 != null && casaPort1 == casaPort2) {
            return NbBundle.getMessage(this.getClass(), 
                    "MSG_CANNOT_CONNECT_SAME_BC_TO_BC"); // NOI18N            
        }
        
        CasaServiceEngineServiceUnit sesu1 = getCasaEngineServiceUnit(endpointRef1);
        CasaServiceEngineServiceUnit sesu2 = getCasaEngineServiceUnit(endpointRef2);
        if (sesu1 != null && sesu2 != null && 
                !sesu1.isInternal() && !sesu2.isInternal()) {
            return NbBundle.getMessage(this.getClass(), 
                        "MSG_CANNOT_CONNECT_TWO_EXTERNAL_ENDPOINTS"); // NOI18N
        }
        
        boolean endpoint1Defined = isEndpointDefined(endpointRef1);
        boolean endpoint2Defined = isEndpointDefined(endpointRef2);
        if (endpoint1Defined && endpoint2Defined) {
            QName consumesInterfaceQName = endpointRef1.getInterfaceQName();
            QName providesInterfaceQName = endpointRef2.getInterfaceQName();
            if (!consumesInterfaceQName.equals(providesInterfaceQName)) {
                boolean isFreeEditablePort1 = casaPort1 != null && 
                        isEditable(casaPort1) &&
                        getConnections(casaPort1, false).size() == 0; // the flag here doesn't matter
                boolean isFreeEditablePort2 = casaPort2 != null && 
                        isEditable(casaPort2) &&
                        getConnections(casaPort2, false).size() == 0; // the flag here doesn't matter
                // Two incompatible endpoints are connectable if and only if
                // at least one is a free editable BC port.
                if (!isFreeEditablePort1 && !isFreeEditablePort2) {
                    return NbBundle.getMessage(this.getClass(),
                            "MSG_CANNOT_CONNECT_INCOMPATIBLE_ENDPOINTS"); // NOI18N
                }
            }
        }
        
        if (!endpoint1Defined && !endpoint2Defined) {
            return NbBundle.getMessage(this.getClass(), 
                        "MSG_CANNOT_CONNECT_TWO_UNDEFINED_ENDPOINTS"); // NOI18N
        }
        
        return null;
    }
    
    private boolean isEndpointDefined(final CasaEndpointRef endpointRef) {
        QName interfaceQName = endpointRef.getInterfaceQName();
        String compAppWSDLTns = getCompAppWSDLTargetNamespace();
        return interfaceQName.getLocalPart().trim().length() > 0
                && !interfaceQName.equals(new QName(compAppWSDLTns, DUMMY_PORTTYPE_NAME)); 
    }
    
    /**
     * Gets the casa connection between the two given casa endpoints, including
     * connection that is marked as deleted.
     */
    private CasaConnection getCasaConnection(final CasaConsumes consumes,
            final CasaProvides provides) {
        // Get all the connections, including deleted ones
        List<CasaConnection> connectionList = getCasaConnectionList(true);
        
        CasaEndpoint consumesEndpoint = consumes.getEndpoint().get();
        CasaEndpoint providesEndpoint = provides.getEndpoint().get();
        
        for (CasaConnection connection : connectionList) {
            if (connection.getConsumer().get() == consumesEndpoint &&
                    connection.getProvider().get() == providesEndpoint) {
                return connection;
            }
        }
        
        return null;
    }
        
//    // FIXME: should we get bcs from design time or run time?
//    private void buildBindingComponentMaps() {
//        bcName2BindingType = new HashMap<String, String>();
//        bindingType2BcName = new HashMap<String, String>();
//        
//        JbiDefaultComponentInfo bcinfo =
//                JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
//        assert bcinfo != null;
//        
//        for (JbiBindingInfo bi : bcinfo.getBindingInfoList()) {
//            String bcName = bi.getBcName();
//            String bindingName = bi.getBindingName();
//            bcName2BindingType.put(bcName, bindingName);
//            bindingType2BcName.put(bindingName, bcName);
//        }
//    }
    
    // Mapping binding component name to binding type, 
    // e.x., sun-ftp-binding => ftp. FIXME THIS IS WRONG!
    private Map<String, String> bcNameMap;
    
    // FIXME: should we get bcs from design time or run time?
    public Map<String, String> getDefaultBindingComponents() {
        if (bcNameMap == null) {
            bcNameMap = new HashMap<String, String>();
            
            JbiDefaultComponentInfo bcinfo = 
                    JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
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
    public List<CasaConnection> getCasaConnectionList(boolean includeDeleted) {
        List<CasaConnection> ret = new ArrayList<CasaConnection>();
        
        CasaConnections connections = getRootComponent().getConnections();
        for (CasaConnection connection : connections.getConnections()) {
            String state = connection.getState();
            if (includeDeleted ||
                    !state.equals(ConnectionState.DELETED.getState())) {
                ret.add(connection);
            }
        }
        
        return ret;
    }
    
    /**
     * Gets a list of WSDL PortTypes in the composite application and its 
     * JBI modules.
     */
    public List<PortType> getPortTypes() {
        List<PortType> ret = new ArrayList<PortType>();
        
        CasaPortTypes portTypes = getRootComponent().getPortTypes();
        for (CasaLink link : portTypes.getLinks()) {
            String href = link.getHref();
            try {
                PortType pt = (PortType) getWSDLComponentFromXLinkHref(href);
//                if (pt.getName().equals(DUMMY_PORTTYPE_NAME)) {
//                    ret.add(null);
//                } else {
                    ret.add(pt);
//                }
            } catch (Exception ex) {
                System.out.println("Failed to Fetch: " + href);
            }
        }
        
        return ret;
    }
    
    /**
     * Gets the WSDL PortType of a given CasaPort.
     */
    public PortType getCasaPortType(final CasaPort casaPort) {
        Port port = getLinkedWSDLPort(casaPort);
        if (port == null) {
            return null;
        } else {
            return port.getBinding().get().getType().get();
        }
    }
    
    public static boolean isDummyPortType(PortType portType) {
        return portType.getName().equals(DUMMY_PORTTYPE_NAME);
    }
    
    private PortType getPortType(final QName interfaceQName) {
        CasaPortTypes casaPortTypes = getRootComponent().getPortTypes();
        for (CasaLink link : casaPortTypes.getLinks()) {
            String href = link.getHref();
            try {
                PortType pt = (PortType) this.getWSDLComponentFromXLinkHref(href);
                if (interfaceQName.getNamespaceURI().equals(
                        pt.getModel().getDefinitions().getTargetNamespace()) &&
                        interfaceQName.getLocalPart().equals(pt.getName())) {
                    return pt;
                }
            } catch (Exception ex) {
                System.out.println("Failed to fetch portType: " + interfaceQName);
            }
        }
        return null;
    }    
    
    private String getWSDLLocation(final QName interfaceQName) {
        CasaPortTypes casaPortTypes = getRootComponent().getPortTypes();
        for (CasaLink link : casaPortTypes.getLinks()) {
            String href = link.getHref();
            try {
                PortType pt = (PortType) this.getWSDLComponentFromXLinkHref(href);
                if (interfaceQName.getNamespaceURI().equals( 
                        pt.getModel().getDefinitions().getTargetNamespace()) &&
                        interfaceQName.getLocalPart().equals(pt.getName())) {
                    return href.substring(0, href.indexOf("#xpointer")); // NOI18N
                }
            } catch (Exception ex) {
                System.out.println("Failed to fetch WSDL location: " + href);
            }
        }
        return null;
    }
    
    /**
     * Adds an internal or external unknown service engine service unit
     * (from the palette).
     */
    public CasaServiceEngineServiceUnit addServiceEngineServiceUnit(
            boolean internal, int x, int y) {
        String projName = getUniqueUnknownServiceEngineServiceUnitName();
        return addServiceEngineServiceUnit(projName, 
                "",   // component name unknown       // NOI18N
                null, // use default service unit description
                internal, 
                true, // is unknown?
                !internal, // is defined? Treat external SU as defined and internal SU as undefined.
                x, y); 
    }
    
    private String getUniqueUnknownServiceEngineServiceUnitName() {
        List<String> existingUnknownNames = new ArrayList<String>();
        
        for (CasaServiceEngineServiceUnit seSU : getServiceEngineServiceUnits()) {
            if (seSU.isUnknown()) {
                existingUnknownNames.add(seSU.getUnitName());
            }
        }
        
        String unknownSESUNamePrefix = 
                NbBundle.getMessage(this.getClass(), 
                    "TXT_UNKNOWN_EXTERNAL_SESU_PREFIX"); // NOI18N
        return getUniqueName(existingUnknownNames, unknownSESUNamePrefix);
                
    }
    
    public CasaServiceEngineServiceUnit addServiceEngineServiceUnit(
            String projName, String compName, String description,
            boolean internal, boolean unknown, boolean defined,
            int x, int y) {
        
        CasaComponentFactory factory = getFactory();
        CasaServiceEngineServiceUnit seSU =
                factory.createCasaEngineServiceUnit();
        seSU.setX(x);
        seSU.setY(y);
        seSU.setInternal(internal);
        seSU.setUnknown(unknown);
        seSU.setDefined(defined);
        seSU.setName(projName);
        seSU.setUnitName(projName);
        seSU.setDescription(description == null ? 
            NbBundle.getMessage(this.getClass(), "TXT_SERVICE_UNIT_DESCRIPTION") : // NOI18N
            description); 
        seSU.setComponentName(compName);
        seSU.setArtifactsZip(projName + ".jar"); // NOI18N
        
        CasaServiceUnits sus = getRootComponent().getServiceUnits();
        startTransaction();
        try {
            sus.addServiceEngineServiceUnit(-1, seSU);
        } finally {
            if (isIntransaction()) {
                fireServiceEngineServiceUnitAdded(seSU);
                endTransaction();
            }
        }
        
        return seSU;
    }
    
    private PortType getDummyPortType(final WSDLModel compAppWSDLModel, 
            boolean create) {
        
        Definitions definitions = compAppWSDLModel.getDefinitions();
        for (PortType pt : definitions.getPortTypes()) {
            if (pt.getName().equals(DUMMY_PORTTYPE_NAME)) {
                return pt;
            }
        }
        
        PortType pt = null;
        
        if (create) {
            WSDLComponentFactory wsdlFactory = compAppWSDLModel.getFactory();
            pt = wsdlFactory.createPortType();
            pt.setName(DUMMY_PORTTYPE_NAME);
            
            compAppWSDLModel.startTransaction();
            try {
                definitions.addPortType(pt);
            } finally {
                if (compAppWSDLModel.isIntransaction()) {
                    compAppWSDLModel.endTransaction();
                }
            }
        }
        
        return pt;
    }
    
    /**
     * Adds a new WSDL endpoint.
     */
    public CasaPort addCasaPort(String bindingType, String componentName, int x, int y) {
        
        // 1. Update casa wsdl
        WSDLModel compAppWSDLModel = getCompAppWSDLModel(true); 
        WSDLComponentFactory wsdlFactory = compAppWSDLModel.getFactory();
        Definitions definitions = compAppWSDLModel.getDefinitions();
        
        // 1.0. create dummy porttype (need this to avoid NPE when building compapp)
        PortType portType = getDummyPortType(compAppWSDLModel, true);
        
        // 1.1. create new binding
        Binding binding = wsdlFactory.createBinding();
        String newBindingName = getUniqueBindingName(compAppWSDLModel);
        binding.setName(newBindingName);
        
        // 1.2. create new port
        Port port = wsdlFactory.createPort();
        String newPortName = getUniquePortName(compAppWSDLModel);
        port.setName(newPortName);
        
        // 1.3. create new service
        int serviceCount = definitions.getServices().size();
        Service service = wsdlFactory.createService(); // FIXME: use existing one?
        String newServiceName = getUniqueServiceName(compAppWSDLModel);
        service.setName(newServiceName);
        service.addPort(port);
        
        compAppWSDLModel.startTransaction();
        try {
            port.setBinding(port.createReferenceTo(binding, Binding.class));
            binding.setType(binding.createReferenceTo(portType, PortType.class));
            definitions.addBinding(binding);
            definitions.addService(service);
        } finally {
            if (compAppWSDLModel.isIntransaction()) {
                compAppWSDLModel.endTransaction();
            }
        }
        
        String relativePath = getRelativePathForCompAppWSDL();
        String portHref = getPortHref(relativePath, newServiceName, newPortName);        
        String portTypeHref = getPortTypeHref(relativePath, DUMMY_PORTTYPE_NAME);
                
        String tns = compAppWSDLModel.getDefinitions().getTargetNamespace(); 

        return addCasaPortToModel(componentName, bindingType, 
                new QName(tns, DUMMY_PORTTYPE_NAME),  
                new QName(tns, newServiceName), 
                newPortName, portHref, portTypeHref, x, y);
    }
    
    private String getRelativePathForCompAppWSDL() {
        String compAppWSDLFileName = getCompAppWSDLFileName();
        return CASA_WSDL_RELATIVE_LOCATION + compAppWSDLFileName;
    }
    
    private String getPortHref(String relativePath, String serviceName, String portName) {
        return relativePath +
                "#xpointer(/definitions/service[@name='" + // NOI18N
                serviceName + "']/port[@name='" + portName + "'])"; // NOI18N
    }
    
    private String getPortTypeHref(String relativePath, String portTypeName) {
        return relativePath +
                "#xpointer(/definitions/portType[@name='" + portTypeName + "'])"; // NOI18N
    }
    
    private String getCompAppWSDLFileName() {
        Project jbiProject = getJBIProject(); 
        if (jbiProject != null) {
            return JbiProjectHelper.getJbiProjectName(jbiProject) + ".wsdl"; // NOI18N
        } else {
            return null;
        }
    }
    
    private JbiBindingInfo getBindingInfo(final Port port) {
        JbiDefaultComponentInfo bcinfo = 
                JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
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
     * Add a port from wsdl file into CASA. This is to restore deleted casa port
     * from component projects.
     *
     * @param port  selected port to add
     * @param wsdlFile the source wsdl file
     * @return the casa port or null if failed to add
     */
    public CasaPort addCasaPortFromWsdlPort(final Port port, 
            final File wsdlFile) {
        try {
            JbiBindingInfo bi = getBindingInfo(port);
            if (bi == null) {
                return null;
            }
            
            String newServiceName = ((Service) (port.getParent())).getName();
            String newPortName = port.getName(); 
            String fname = wsdlFile.getCanonicalPath();
            String relativePath = "../" + // NOI18N
                    fname.substring(fname.indexOf(JBI_SERVICE_UNITS_DIR)).replace('\\', '/'); // NOI18N
            String portHref = getPortHref(relativePath, newServiceName, newPortName); 
            
            for (CasaBindingComponentServiceUnit bcSU : getBindingComponentServiceUnits()) {
                for (CasaPort casaPort : bcSU.getPorts().getPorts()) {
                    if (portHref.equals(casaPort.getLink().getHref())) {
                        assert CasaPortState.DELETED.getState().equals(casaPort.getState());
                        setCasaPortState(casaPort, CasaPortState.NORMAL);
                        return casaPort;
                    }
                }
            }
            
            String tns = port.getModel().getDefinitions().getTargetNamespace();
            String newInterfaceName = port.getBinding().get().getType().get().getName();
            String newPortTypeHref = getPortTypeHref(relativePath, newInterfaceName);
            
            String bindingType = bi.getBindingName();
            String componentName = bi.getBcName();
            
            return addCasaPortToModel(componentName, bindingType,  
                    new QName(tns, newInterfaceName),
                    new QName(tns, newServiceName), 
                    newPortName, portHref, newPortTypeHref, 0, 0);
        } catch (Exception ex) {
            // add failed...
            ex.printStackTrace();
        }
        
        return null;
    }
    
    private CasaPort addCasaPortToModel(String componentName, 
            String bindingType,
            QName newInterfaceQName,
            QName newServiceQName, String newPortName,
            String portHref, String portTypeHref, int x, int y) {
        
        CasaEndpoints endpoints = getRootComponent().getEndpoints();
                
        // 1. Create and add a dummy endpoint
        CasaComponentFactory casaFactory = getFactory();
        CasaEndpoint newEndpoint = casaFactory.createCasaEndpoint();
        String newEndpointID = getUniqueEndpointID(this);
            
        startTransaction();
        try {            
            newEndpoint.setName(newEndpointID);
            newEndpoint.setEndpointName(newPortName); 
            newEndpoint.setInterfaceQName(newInterfaceQName);
            newEndpoint.setServiceQName(newServiceQName);
            
            endpoints.addEndpoint(-1, newEndpoint);
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
        
        // 2. Create new casa port
        CasaPort casaPort = casaFactory.createCasaPort();
        casaPort.setX(x);
        casaPort.setY(y);
//        casaPort.setBindingState("unbound");
//        casaPort.setPortType("");
        casaPort.setBindingType(bindingType);
        
        // 3. Add casa port link
        CasaLink link = casaFactory.createCasaLink();
        link.setHref(portHref);
        link.setType("simple");
        casaPort.setLink(link);
        
        // 4. Create and add consumes reference
        CasaConsumes casaConsumes = casaFactory.createCasaConsumes();
        casaConsumes.setEndpoint(
                casaConsumes.createReferenceTo(newEndpoint, CasaEndpoint.class));
        casaPort.setConsumes(casaConsumes);
        
        // 5. Create and add provides reference        
        CasaProvides casaProvides = casaFactory.createCasaProvides();
        casaProvides.setEndpoint(
                casaProvides.createReferenceTo(newEndpoint, CasaEndpoint.class));
        casaPort.setProvides(casaProvides);
                
        // 6. Create a new portType link, if the exsiting portType list doens't
        // contains the new portType.
        boolean portTypeExists = false;
        CasaPortTypes portTypes = getRootComponent().getPortTypes();
        for (CasaLink portTypeLink : portTypes.getLinks()) {
            if (portTypeLink.getHref().equals(portTypeHref)) {
                portTypeExists = true;
                break;
            }
        }
        CasaLink portTypeLink = null;
        if (!portTypeExists) {
            portTypeLink = casaFactory.createCasaLink();
            portTypeLink.setHref(portTypeHref);
            portTypeLink.setType("simple"); // NOI18N
        }

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
                casaBCSU.setDescription(NbBundle.getMessage(this.getClass(), 
                    "TXT_SERVICE_UNIT_DESCRIPTION")); // NOI18N
                casaBCSU.setArtifactsZip(componentName + ".jar"); // NOI18N
                
                CasaPorts casaPorts = casaFactory.createCasaPorts();
                casaBCSU.setPorts(casaPorts);
                
                CasaServiceUnits casaSUs = getRootComponent().getServiceUnits();
                casaSUs.addBindingComponentServiceUnit(-1, casaBCSU);
            }
            
            // add the new WSDL endpoint
            CasaPorts casaPorts = casaBCSU.getPorts();
            casaPorts.addPort(-1, casaPort);
            
            // add the new portType link, if applicable
            if (portTypeLink != null) {                
                portTypes.addLink(-1, portTypeLink);
            }
            
            // TODO: we are not using Bindings and Services. Those are currently
            // not updated.
        
        } finally {
            if (isIntransaction()) {
                fireCasaPortAdded(casaPort);
                endTransaction();
            }
        }
        
        return casaPort;
    }
    
    /**
     * Deletes a casa port or marks it as deleted depending on where the casa
     * port is defined. All the visible connections connecting the casa port's
     * Consumes and Provides endpoint are deleted or marked as deleted as a
     * side effect. 
     *
     * If the casa port is defined in compapp, then the corresponding WSDL port 
     * and binding in casa wsdl are cleaned up.
     *
     * @param casaPort          a casa port
     */
    public void removeCasaPort(final CasaPort casaPort) {
        
        String linkHref = casaPort.getLink().getHref();
        final Port port = getLinkedWSDLPort(casaPort);
        
        CasaEndpoint endpoint = casaPort.getConsumes() != null ? 
            casaPort.getConsumes().getEndpoint().get() :
            casaPort.getProvides().getEndpoint().get();
        
        // 1. Delete connections
        CasaConsumes consumes = casaPort.getConsumes();
        if (consumes != null) {
            for (CasaConnection connection : getConnections(consumes, false)) { // don't include "deleted" connection
                removeConnection(connection);
            }
        }
        
        CasaProvides provides = casaPort.getProvides();
        if (provides != null) {
            for (CasaConnection connection : getConnections(provides, false)) { // don't include "deleted" connection
                removeConnection(connection);
            }
        }
        
        if (!isDefinedInCompApp(casaPort)) {
            // 2. Simply mark the casa port as deleted and that's it.
            setCasaPortState(casaPort, CasaPortState.DELETED);
            
        } else {
            // 2. Delete the casa port itself or delete the binding component
            // service unit if there is no casa port left behind.
            CasaPorts casaPorts = (CasaPorts) casaPort.getParent();
            startTransaction();
            try {
                if (casaPorts.getPorts().size() > 1) {
                    casaPorts.removePort(casaPort);
                } else {
                    CasaBindingComponentServiceUnit bcSU =
                            (CasaBindingComponentServiceUnit) casaPorts.getParent();
                    CasaServiceUnits sus = (CasaServiceUnits) bcSU.getParent();
                    sus.removeBindingComponentServiceUnit(bcSU);
                }
            } finally {
                if (isIntransaction()) {
                    fireCasaPortRemoved(casaPort);
                    endTransaction();
                }
            }
            
            // 3. Delete dangling endpoint
            removeDanglingEndpoint(endpoint);
            
            // 4. Clean up casa wsdl
            // Added invokeLater to fix a IllegalStateException from WSDL UI
            // temporarily.
            // To reproduce the problem:
            // (1) Use SynchSample
            // (2) Open CASA editor
            // (3) Open casa wsdl (this is the key step)
            // (4) Drop a WSDL port into CASA
            // (5) Make a connection from the WSDL port to BPEL SU's endpoint
            // (6) Delete the dropped WSDL port
            //     => IllegalStateException: Referencing component not part of model
            //
            // If we delete the new connection first then delete the WSDL port,
            // then everything is OK.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Binding binding = port.getBinding().get();
                    Service service = (Service) port.getParent();
                    Definitions definitions = (Definitions) service.getParent();                                     
                    
                    WSDLModel compAppWSDLModel = port.getModel();
                    compAppWSDLModel.startTransaction();
                    try {
                        service.removePort(port);
                        definitions.removeService(service);
                        definitions.removeBinding(binding);
                    } finally {
                        if (compAppWSDLModel.isIntransaction()) {
                            compAppWSDLModel.endTransaction();
                        }
                    }
                    
//                    checkAndCleanUpDummyPortType(compAppWSDLModel);
                }
            });
            
            // 5. Clear the cached WSDL component reference.
            cachedWSDLComponents.remove(linkHref);
        }
    }
    
    /**
     * Clean up the dummy portType if it is no longer used in casa wsdl.
     */ /*
    private void checkAndCleanUpDummyPortType(WSDLModel compAppWSDLModel) {
        PortType dummyPortType = getDummyPortType(compAppWSDLModel, false);
        
        if (dummyPortType != null) {                                  
            boolean dummyPortTypeUsed = false;
            
            Definitions definitions = compAppWSDLModel.getDefinitions();  
            for (Binding b : definitions.getBindings()) {
                if (b.getType().get() == dummyPortType) {
                    dummyPortTypeUsed = true;
                    break;
                }
            }
            
            if (!dummyPortTypeUsed) {
                compAppWSDLModel.startTransaction();
                try {
                    definitions.removePortType(dummyPortType);
                } finally {
                    if (compAppWSDLModel.isIntransaction()) {
                        compAppWSDLModel.endTransaction();
                    }
                }
            }
        }
    } */
    
    private void setCasaPortState(final CasaPort casaPort,
            final CasaPortState state) {
        String initialState = casaPort.getState();
        startTransaction();
        try {
            casaPort.setState(state.getState());
        } finally {
            if (isIntransaction()) {
                if (//CasaPortState.UNCHANGED.getState().equals(initialState) &&
                        CasaPortState.DELETED.equals(state)) { // FIXME
                    fireCasaPortRemoved(casaPort);
                } else if (CasaPortState.DELETED.getState().equals(initialState) //&&
                        //ConnectionState.UNCHANGED.equals(state)
                        ) {
                    fireCasaPortAdded(casaPort);
                } else {
                    assert false : "Uh? setCasaPortState: " + initialState + "->" + state.getState(); // NOI18N
                }
                endTransaction();
            }
        }
    }
    
    /**
     * Removes a Consumes/Provides endpoint in an (external) service engine
     * service unit. All the visible connections connecting the endpoint 
     * are deleted as well.
     *
     * @param endpoint      a CasaConsumes or CasaProvides endpoint
     */
    public void removeEndpoint(final CasaEndpointRef endpointRef) {
        
        // 1. Delete any visible connections
        List<CasaConnection> visibleConnections = 
                getConnections(endpointRef, false);
        for (CasaConnection visibleConnection : visibleConnections) {
            removeConnection(visibleConnection);
        }
        
        CasaPort casaPort = getCasaPort(endpointRef);
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        
        // 2. Delete the endpoint reference itself
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
        // from casa wsdl if applicable.
        if (endpointRemoved && casaPort != null && isDefinedInCompApp(casaPort)) {
            Port port = getLinkedWSDLPort(casaPort);
            
            WSDLModel compAppWSDLModel = port.getModel();
            if (compAppWSDLModel != null) {
                // FIXME: when deleting a user-defined casaport, we delete
                // the consumes and provides endpoint, both of which try to
                // delete the same port/binding/service now! Maybe we should
                // hold on deleting port/... until there is really no reference
                // left?
                Definitions definitions = compAppWSDLModel.getDefinitions();
                Binding binding = port.getBinding().get();
                Service service = (Service) port.getParent();
                
                compAppWSDLModel.startTransaction();
                try {
                    definitions.removeBinding(binding);
                    if (service.getPorts().size() == 1) {
                        definitions.removeService(service);
                    } else {
                        service.removePort(port);
                    }
                } finally {
                    if (compAppWSDLModel.isIntransaction()) {
                        compAppWSDLModel.endTransaction();
                    }
                }
            }
        }
    }
    
    /**
     * Removes a casa endpoint if it is not referenced anywhere, that it, it is
     * not reference by any service engine service unit, binding component 
     * service unit, or invisible connections.
     */
    private boolean removeDanglingEndpoint(final CasaEndpoint endpoint) {
        // For invisible connections, we still need to keep the endpoints around.
        
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
                // The following is not really necessary now that casaport
                // always contains a Consumes and a Provides endpoint.
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
    
    /**
     * Gets the containing casa port for an endpoint.
     * 
     * @param endpoint  a casa endpoint reference
     * @return  the containing casa port, or <code>null</code> if the given  
     *          endpoint belongs to a service engine service unit.
     */
    public CasaPort getCasaPort(final CasaEndpointRef endpointRef) {
        CasaComponent comp = endpointRef.getParent();
        if (comp instanceof CasaPort) {
            return (CasaPort) comp;
        } else {
            return null;
        }
    }
    
    /**
     * Gets the containing service engine service unit for an endpoint.
     * 
     * @param endpoint  a casa endpoint reference
     * @return  the containing service engine service unit, or <code>null</null>
     *          if the given endpoint belongs to a casa port.
     */
    public CasaServiceEngineServiceUnit getCasaEngineServiceUnit(
            final CasaEndpointRef endpointRef) {
        CasaComponent comp = endpointRef.getParent();
        if (comp instanceof CasaServiceEngineServiceUnit) {
            return (CasaServiceEngineServiceUnit) comp;
        } else {
            return null;
        }
    }
    
    private boolean isBindingComponentEndpoint(final CasaEndpointRef endpointRef) {
        return getCasaPort(endpointRef) != null;
    }
    
    /**
     * Saves all the related models.
     * This should be called when the CasaWrapperModel is saved.
     */
    public void saveDocument() {
        
        saveRelatedDataObjects();
        
        addPendingProjects();
        
        deletePendingProjects();        
    }
    
    /**
     * Saves all the related models.
     */
    private void saveRelatedDataObjects() {
        
        List<DataObject> dataObjects = getRelatedDataObjects();
        
        for (DataObject dataObject : dataObjects) {
            SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);
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
     * This should be called when the CasaWrapperModel's changes are discarded.
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
        
        // Get casa wsdl
        WSDLModel compAppWSDLModel = getCompAppWSDLModel(false);
        if (compAppWSDLModel != null) {
            DataObject dataObject = getDataObject(compAppWSDLModel);
            if (dataObject != null) {
                ret.add(dataObject);
            }
        }
        
        return ret;
    }
    
    private static DataObject getDataObject(final Model model) {
        Lookup lookup = model.getModelSource().getLookup();
        FileObject fileObject = lookup.lookup(FileObject.class);
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
     * Adds a new endpoint to an (external) service engine service unit.
     */
    public CasaEndpointRef addEndpointToServiceEngineServiceUnit(
            final CasaServiceEngineServiceUnit seSU, boolean isConsumes) {
        
        assert !seSU.isInternal() : "Not supporting adding new endpoints to internal service units for now.";
        
        CasaComponentFactory casaFactory = getFactory();
        
        String endpointID = getUniqueEndpointID(this);
        String endpointName = getUniqueExternalEndpointName(this);
        
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
            endpoint.setInterfaceQName(new QName("")); // NOI18N
            endpoint.setServiceQName(new QName("")); // NOI18N
            endpoints.addEndpoint(-1, endpoint);
            
            endpointRef.setEndpoint(
                    endpointRef.createReferenceTo(endpoint, CasaEndpoint.class));
            
            if (isConsumes) {
                seSU.addConsumes(-1, (CasaConsumes)endpointRef);
            } else {
                seSU.addProvides(-1, (CasaProvides)endpointRef);
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
     * Adds a list of endpoints (from JBI Manager DnD transfer) to an
     * (external) service engine service unit.
     */
    public void addEndpointsToServiceEngineServiceUnit(
            final JBIServiceUnitTransferObject suTransfer,
            final CasaServiceEngineServiceUnit seSU) {
        
        Model model = seSU.getModel();
        List<JBIServiceUnitTransferObject.Endpoint> pList = 
                suTransfer.getProvidesEndpoints();
        for (JBIServiceUnitTransferObject.Endpoint p : pList) {
            CasaEndpointRef endpointRef =
                    addEndpointToServiceEngineServiceUnit(seSU, false);
            CasaEndpoint endpoint = endpointRef.getEndpoint().get();
            
            model.startTransaction();
            try {
                endpoint.setInterfaceQName(p.getInterfaceQName());
                endpoint.setServiceQName(p.getServiceQName());
                endpoint.setEndpointName(p.getEndpointName());
            } finally {
                if (model.isIntransaction()) {
                    model.endTransaction();
                }
            }
        }
        List<JBIServiceUnitTransferObject.Endpoint> cList = 
                suTransfer.getConsumesEndpoints();
        for (JBIServiceUnitTransferObject.Endpoint c : cList) {
            CasaEndpointRef endpointRef =
                    addEndpointToServiceEngineServiceUnit(seSU, true);
            CasaEndpoint endpoint = endpointRef.getEndpoint().get();
            
            model.startTransaction();
            try {
                endpoint.setInterfaceQName(c.getInterfaceQName());
                endpoint.setServiceQName(c.getServiceQName());
                endpoint.setEndpointName(c.getEndpointName());
            } finally {
                if (model.isIntransaction()) {
                    model.endTransaction();
                }
            }
        }
    }
    
    /**
     * Adds all the pending JBI module projects from queue to the CompApp project.
     */
    private void addPendingProjects() {
        try {
            Project jbiProject = getJBIProject();
            
            AddProjectAction addProjectAction = new AddProjectAction();
            
            for (String key : new HashSet<String>(mAddProjects.keySet())) {
                Project project = mAddProjects.get(key);
                AntArtifactProvider antArtifactProvider =
                        project.getLookup().lookup(AntArtifactProvider.class);
                assert antArtifactProvider != null;
                
                AntArtifact[] artifacts =
                        antArtifactProvider.getBuildArtifacts();
                boolean success =
                        addProjectAction.addProject(jbiProject, artifacts[0]);                
                if (success) {
                    mAddProjects.remove(key);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Deletes all the pending JBI module projects from queue to the CompApp project.
     */
    private void deletePendingProjects() {
        try {
            Project jbiProject = getJBIProject();
            
            DeleteModuleAction deleteModuleAction =
                    SystemAction.get(DeleteModuleAction.class);
                
            for (String key : new HashSet<String>(mDeleteProjects.keySet())) {
                String artifactName = mDeleteProjects.get(key);
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
     * Adds a component project into compapp project (from project explorer).
     *
     * @param project   a component project
     * @param x         x location of the new service unit in the editor
     * @param y         y location of the new service unit in the editor
     */
    public void addInternalJBIModule(final Project project, 
            String type, int x, int y) {
        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        
        if (mDeleteProjects.containsKey(projectName)) {
            mDeleteProjects.remove(projectName);
        } else {
            mAddProjects.put(projectName, project); // todo: needs to fix duplicate proj names..
        }
        
        addServiceEngineServiceUnit(projectName, type, 
                null,  // use default description
                true,  // is internal SESU?
                false, // is known?
                false, // is defined?
                x, y);
    }
    
    /**
     * Deletes a service engine service unit. All the connections connecting
     * the service unit, and possibly WSDL endpoints, will be deleted as well.
     *
     * @param seSU       an existing service engine service unit
     */
    public void removeServiceEngineServiceUnit(
            final CasaServiceEngineServiceUnit seSU) {
        
        if (seSU.isInternal() && !seSU.isUnknown()) {
            String projectName = seSU.getUnitName();
            if (mAddProjects.containsKey(projectName)) {
                mAddProjects.remove(projectName);
            } else {
                mDeleteProjects.put(projectName, seSU.getArtifactsZip());
            }
        }
        
        Set<CasaPort> connectingCasaPorts = new HashSet<CasaPort>();
        
        // 1. Delete connections
        for (CasaEndpointRef endpointRef : seSU.getEndpoints()) {
            List<CasaConnection> visibleConnections =
                    getConnections(endpointRef, false); // don't include "deleted" connection
            for (CasaConnection visibleConnection : visibleConnections) {
                
                // find connecting casa ports
                CasaEndpointRef casaConsumes =
                        getCasaEndpointRef(visibleConnection.getConsumer().get(), true);
                CasaPort casaPort = getCasaPort(casaConsumes);
                if (casaPort != null) {
                    connectingCasaPorts.add(casaPort);
                }
                
                CasaEndpointRef casaProvides =
                        getCasaEndpointRef(visibleConnection.getProvider().get(), false);
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
                    removeCasaPort(casaPort);                    
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
                    
    /**
     * Gets the owning compapp project.
     */
    public Project getJBIProject() {
        try {
            Lookup lookup = getModelSource().getLookup();
            FileObject casaFO = lookup.lookup(FileObject.class);
            FileObject projectFO = casaFO.getParent().getParent().getParent();
            return ProjectManager.getDefault().findProject(projectFO);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }      
    
    /**
     * Gets a (non-null) list of casa connections connecting the given endpoint.
     *
     * @param endpointRef       an endpoint reference
     * @param includeDeleted    whether to include connections that are marked as deleted
     */
    private List<CasaConnection> getConnections(
            final CasaEndpointRef endpointRef,
            boolean includeDeleted) {
        List<CasaConnection> ret = new ArrayList<CasaConnection>();
        
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        for (CasaConnection connection : getCasaConnectionList(includeDeleted)) {
            if ((endpointRef instanceof CasaConsumes &&
                    connection.getConsumer().get() == endpoint) ||
                    (endpointRef instanceof CasaProvides &&
                    connection.getProvider().get() == endpoint)) {
                ret.add(connection);
            }
        }
        
        return ret;
    }
    
    private static String getUniqueEndpointID(final CasaWrapperModel model) {
        List<String> existingNames = new ArrayList<String>();
        
        for (CasaEndpoint casaEndpoint : 
                model.getRootComponent().getEndpoints().getEndpoints()) {
            String name = casaEndpoint.getName();
            existingNames.add(name);
        }
        
        return getUniqueName(existingNames, "endpoint"); // NOI18N
    }
    
    private static String getUniqueExternalEndpointName(
            final CasaWrapperModel model) {
        List<String> existingNames = new ArrayList<String>();
        
        for (CasaEndpoint casaEndpoint : 
                model.getRootComponent().getEndpoints().getEndpoints()) {
            String name = casaEndpoint.getEndpointName();
            existingNames.add(name);
        }
        
        return getUniqueName(existingNames, "extEndpoint"); // NOI18N
    }
    
    private static String getUniquePortTypeName(final WSDLModel wsdlModel) {
        List<String> existingNames = new ArrayList<String>();
        
        for (PortType portType : wsdlModel.getDefinitions().getPortTypes()) {
            String name = portType.getName();
            existingNames.add(name);
        }
        
        return getUniqueName(existingNames, "casaPortType"); // NOI18N
    }
    
    private static String getUniqueBindingName(final WSDLModel wsdlModel) {
        List<String> existingNames = new ArrayList<String>();
        
        for (Binding binding : wsdlModel.getDefinitions().getBindings()) {
            String name = binding.getName();
            existingNames.add(name);
        }
        
        return getUniqueName(existingNames, "casaBinding"); // NOI18N
    }
    
    private static String getUniqueServiceName(final WSDLModel wsdlModel) {
        List<String> existingNames = new ArrayList<String>();
        
        for (Service service : wsdlModel.getDefinitions().getServices()) {
            String name = service.getName();
            existingNames.add(name);
        }
        
        return getUniqueName(existingNames, "casaService"); // NOI18N
    }
    
    private static String getUniquePortName(final WSDLModel wsdlModel) {
        List<String> existingNames = new ArrayList<String>();
        
        for (Service service : wsdlModel.getDefinitions().getServices()) {
            for (Port port : service.getPorts()) {
                String name = port.getName();
                existingNames.add(name);
            }
        }
        
        return getUniqueName(existingNames, "casaPort"); // NOI18N
    }
    
    private static String getUniqueName(final List<String> existingNames, 
            String prefix) {
        String newName = null;
        
        for (int i = 1; ; i++) {
            newName = prefix + i; 
            if (!existingNames.contains(newName)) {
                break;
            }
        }
        
        return newName;
    }
    
    /**
     * Gets the CASA WSDL Model.
     */
    private WSDLModel getWSDLModel(String uriString) {
        
        ModelSource modelSource = getModelSource();
        Lookup lookup = modelSource.getLookup();
        CatalogModel catalogModel = lookup.lookup(CatalogModel.class);
        
        ModelSource wsdlModelSource = null;
        try {
            wsdlModelSource =
                    catalogModel.getModelSource(new URI(uriString), modelSource);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if (wsdlModelSource != null) {
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(wsdlModelSource);
            assert wsdlModel.getDefinitions() != null;
            return wsdlModel;
        } else {
            System.out.println("WARNING: getWSDLModel() == null, uriString is " + uriString); // NOI18N
            return null;
        }
    } 
    
//    WSDLModel compappWSDLModel = null;
    
    private WSDLModel getCompAppWSDLModel(boolean create) {
        // TODO: use cache?
//        if (compappWSDLModel != null) {
//            return compappWSDLModel;
//        }
        String compAppWSDLFileName = getCompAppWSDLFileName();
        
        ModelSource modelSource = getModelSource();
        Lookup lookup = modelSource.getLookup();
        CatalogModel catalogModel = lookup.lookup(CatalogModel.class);
        FileObject casaFO = lookup.lookup(FileObject.class);
        URI uri = null;
        try {
            uri = new URI(CASA_WSDL_RELATIVE_LOCATION + compAppWSDLFileName);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        
        ModelSource wsdlModelSource = null;
        
        FileObject compAppWSDLDirFO = 
                    casaFO.getParent().getParent().getFileObject("jbiasa"); // NOI18N
        File compAppWSDLFile = 
                new File(FileUtil.toFile(compAppWSDLDirFO), compAppWSDLFileName); 
        try {
            if (!compAppWSDLFile.exists()) {
                if (create) {
                    createEmptyCompAppWSDLFile(compAppWSDLFile);
                    wsdlModelSource = catalogModel.getModelSource(uri, modelSource);
                }
            } else {
                wsdlModelSource = catalogModel.getModelSource(uri, modelSource);
            }
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }

        
        if (wsdlModelSource != null) {
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(wsdlModelSource);
            /*
//            if (firstTimeLoadingCompAppWSDLModel) {
//                firstTimeLoadingCompAppWSDLModel = false;
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
//            }
             */
//            compappWSDLModel = wsdlModel;
            return wsdlModel;
        } else {
            return null;
        }
    }
    /*
    private boolean firstTimeLoadingCompAppWSDLModel = true;
    */
    private void markDirty() {
        DataObject dataObject = getDataObject(this);
        if (!dataObject.isModified()) {
            dataObject.setModified(true);
        }
    }
    
    private String getCompAppWSDLTargetNamespace() {
        Project jbiProject = getJBIProject();
        return JbiProjectHelper.getJbiProjectName(jbiProject);
    }
    
    private void createEmptyCompAppWSDLFile(File file) {
        
        String NEWLINE = System.getProperty("line.separator"); // NOI18N
        
        BufferedWriter out = null;
                
        try {
            String tns = getCompAppWSDLTargetNamespace();
            
            FileWriter fileWriter = new FileWriter(file);
            out = new BufferedWriter(fileWriter);
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18M
            out.write(NEWLINE);
            out.write("<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\""); // NOI18M
            out.write(NEWLINE);
            out.write("             xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\""); // NOI18M
            out.write(NEWLINE);
            out.write("             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""); // NOI18M
            out.write(NEWLINE);
            out.write("             targetNamespace=\"" + tns + "\""); // NOI18M
            out.write(NEWLINE);
            out.write("             xmlns:myns=\"" + tns + "\">"); // NOI18M
            out.write(NEWLINE);
            out.write("</definitions>"); // NOI18M
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Gets a list of Connections that involve the given WSDL endpoint.
     */
    private List<CasaConnection> getConnections(final CasaPort casaPort,
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
    public boolean isEditable(final CasaServiceEngineServiceUnit seSU) {
        return !seSU.isInternal();
    }
    
    public boolean isEditable(final CasaServiceEngineServiceUnit seSU, 
            String propertyName) {
        if (!isEditable(seSU)) {
            return false;
        }
        
        if (CasaAttribute.UNIT_NAME.getName().equals(propertyName) ||
                CasaAttribute.DESCRIPTION.getName().equals(propertyName)) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isEditable(final CasaBindingComponentServiceUnit bcSU) {
        return false;
    }
    
    /**
     * Checks whether a CasaPort is editable.
     * Only CasaPorts coming from compapp are editable. CasaPorts that come from 
     * component projects are not editable.
     */
    public boolean isEditable(final CasaPort casaPort) {
        return isDefinedInCompApp(casaPort);
    }
    
    /**
     * Checks whether a particular property of a CasaPort is editable.
     */
    public boolean isEditable(final CasaPort casaPort, String propertyName) {
        if (!isEditable(casaPort)) {
            return false;
        }
        
        // Can't edit internface name when there is visible connection
        if (JBIAttributes.INTERFACE_NAME.getName().equals(propertyName) &&
                getConnections(casaPort, false).size() > 0) {
            return false;
        }
        
        return true;
    }
        
    /**
     * Checks whether a particular property of an endpoint is editable.
     *
     * Endpoints in internal service engine service unit and WSDL endpoints 
     * casa port that come from component projects are not editable.
     */
    public boolean isEditable(final CasaEndpointRef endpointRef, 
            String propertyName) {
        
        CasaServiceEngineServiceUnit seSU =
                getCasaEngineServiceUnit(endpointRef);
        if (seSU != null) {
            if (seSU.isInternal()) {
                return false;
            }
        } else {
            CasaPort casaPort = getCasaPort(endpointRef);
            if (!isEditable(casaPort)) {
                return false;
            }
        }    
        
        // Can't edit internface name when there is visible connection
        if (JBIAttributes.INTERFACE_NAME.getName().equals(propertyName)) {
            /* Issue: 113870 : The consumes and provides of wsdl end points shouldn't be editable */
            CasaPort casaPort = getCasaPort(endpointRef);
            if(casaPort != null) {
                return false;
            }
            /*
            if (getConnections(endpointRef, false).size() > 0) {
                return false;
            } else {
                // For WSDL port, we need to check both consumes and provides.
                CasaPort casaPort = getCasaPort(endpointRef);
                if (casaPort != null 
                        && getConnections(casaPort, false).size() > 0) {
                    return false;
                }
            }
            */
        }
        
        return true;
    }
        
    /**
     * Check whether the given casa port is defined in the composite application.
     */
    private boolean isDefinedInCompApp(final CasaPort casaPort) {
        CasaLink link = casaPort.getLink();
        String linkHref = link.getHref();
        return linkHref.startsWith("../jbiasa/"); // NOI18N
    }
    
    private boolean isDefinedInCompAppWSDL(final CasaPort casaPort) {
        CasaLink link = casaPort.getLink();
        String linkHref = link.getHref();
        return linkHref.startsWith("../jbiasa/" + getCompAppWSDLFileName() + "#xpointer"); // NOI18N
    }
    
    /**
     * Checks whether a service unit is deletable. For now a service unit is
     * always deletable.
     */
    public boolean isDeletable(final CasaServiceEngineServiceUnit seSU) {
        return true;
    }
    
    public boolean isDeletable(final CasaBindingComponentServiceUnit bcSU) {
        return true;
    }
    
    /**
     * Checks whether a WSDL endpoint is deletable.
     */
    public boolean isDeletable(final CasaPort casaPort) {
        return true;
    }
    
    /**
     * Checks whether an endpoint is deletable. An endpoint is deletable if
     * and only if it belongs to an external service engine service unit.
     */
    public boolean isDeletable(final CasaEndpointRef endpointRef) {
        CasaServiceEngineServiceUnit casaSESU =
                getCasaEngineServiceUnit(endpointRef);
        return casaSESU != null && !casaSESU.isInternal();
    }
    
    /**
     * Checks whether a connection is deletable. A connection is always deletable,
     * although for auto-generated connections, a delete is actually a hide.
     */
    public boolean isDeletable(final CasaConnection connection) {
        return true;
    }
    
    /**
     * Sets name of an endpoint.
     * To keep model integrity, the following side effects of this change 
     * could occur, if applicable: 
     * <UL>
     * <LI> The name of the corresponding WSDL port in WSDL file (casa wsdl) 
     * is updated;
     * <LI> The containing casa port's link href is updated.
     * </UL>
     */
    private void setEndpointName(final CasaComponent component, 
            CasaEndpointRef endpointRef, String endpointName) {
        CasaEndpoint casaEndpoint = endpointRef.getEndpoint().get();
       
        String oldEndpointName = casaEndpoint.getEndpointName();        
        if (oldEndpointName.equals(endpointName)) {
            return;
        }
        
        startTransaction();
        try {
            casaEndpoint.setEndpointName(endpointName);
        } finally {
            if (isIntransaction()) {
                fireCasaEndpointRenamed(component);
                endTransaction();
            }
        }
        
        // If the corresponding port is defined in casa wsdl, 
        // update the port name in casa wsdl and also the xlink in casa.
        CasaPort casaPort = null;
        if (component instanceof CasaPort) {
            casaPort = (CasaPort) component;
        } else {
            casaPort = getCasaPort((CasaEndpointRef) component);            
        }
        if (casaPort != null) {
            Port port = getLinkedWSDLPort(casaPort);
            // The port must be defined in casa wsdl because otherwise 
            // the endpoint is not editable.
            WSDLModel compAppWSDLModel = port.getModel();
            compAppWSDLModel.startTransaction();
            try {
                port.setName(endpointName);
            } finally {
                if (compAppWSDLModel.isIntransaction()) {
                    compAppWSDLModel.endTransaction();
                }
            }
            
            CasaLink link = casaPort.getLink();
            String linkHref = link.getHref();
            linkHref = linkHref.replaceAll(
                    "/port\\[@name='" + oldEndpointName + "'\\]", // NOI18N
                    "/port[@name='" + endpointName + "']"); // NOI18N
            startTransaction();
            try {
                link.setHref(linkHref);
            } finally {
                if (isIntransaction()) {
                    endTransaction();
                }
            }
        }
    }
      
    /**
     * Sets the interface qname of an endpoint. Updates the corresponding WSDL
     * Port, Binding and PortType if the endpoint is defined in casa wsdl.
     * 
     * @param endpointRef  a casa consumes or provides endpoint reference
     * @param interfaceQName    the new interface QName for the referenced endpoint
     */
    public void setEndpointInterfaceQName(final CasaEndpointRef endpointRef,
            final QName interfaceQName) {
        
        if (endpointRef.getInterfaceQName().equals(interfaceQName)) {
            return; // need this to avoid infinite loop
        }
                       
        // 1. Update endpoint interface qname in casa.
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        QName oldInterfaceQName = endpoint.getInterfaceQName();
        startTransaction();
        try {
            if (interfaceQName == null || interfaceQName.equals(new QName(""))) { // NOI18N
                CasaPort casaPort = getCasaPort(endpointRef);
                if (casaPort != null && isDefinedInCompAppWSDL(casaPort)) { 
                    Port port = getLinkedWSDLPort(casaPort);
                    WSDLModel compAppWSDLModel = port.getModel();
                    String tns = compAppWSDLModel.getDefinitions().getTargetNamespace();
                    endpoint.setInterfaceQName(new QName(tns, DUMMY_PORTTYPE_NAME));                
                } else {
                    endpoint.setInterfaceQName(interfaceQName);
                }
            } else {
                endpoint.setInterfaceQName(interfaceQName);
            }
        } finally {
            if (isIntransaction()) {
                fireCasaEndpointInterfaceQNameChanged(endpointRef);
                endTransaction();
            }
        }
        
        // 2. Update casa wsdl and casacade the interface change, if applicable.
        CasaPort casaPort = getCasaPort(endpointRef);
        if (casaPort == null) {
            return;
        }     
        Port port = getLinkedWSDLPort(casaPort);
        
        if (isDefinedInCompAppWSDL(casaPort)) {
            WSDLModel compAppWSDLModel = port.getModel();
            String tns = compAppWSDLModel.getDefinitions().getTargetNamespace();
            
            if (interfaceQName == null || interfaceQName.equals(new QName("")) || // NOI18N
                    interfaceQName.equals(new QName(tns, DUMMY_PORTTYPE_NAME))) {
                PortType dummyPT = getDummyPortType(compAppWSDLModel, true);
                // Here we also need to clean up casa wsdl:
                compAppWSDLModel.startTransaction();
                try {
                    // (1) remove wsdl port children
                    for (ExtensibilityElement ex : port.getExtensibilityElements()) {
                        port.removeExtensibilityElement(ex);
                    }
                    // (2) remove binding children
                    Binding binding = port.getBinding().get();
                    for (BindingOperation op : binding.getBindingOperations()) {
                        binding.removeBindingOperation(op);
                    }
                    for (ExtensibilityElement ex : binding.getExtensibilityElements()) {
                        binding.removeExtensibilityElement(ex);
                    }
                    // (3) change binding type to dummy porttype
                    binding.setType(binding.createReferenceTo(dummyPT, PortType.class));
                } finally {
                    if (compAppWSDLModel.isIntransaction()) {
                        compAppWSDLModel.endTransaction();
                    }
                }                
            } else {                
                try {
                    populateBindingAndPort(casaPort, port, interfaceQName,
                            casaPort.getBindingType());
                } catch (RuntimeException e) {
                    // TODO: we need better transaction rollback handling.
                    startTransaction();
                    try {
                        endpoint.setInterfaceQName(oldInterfaceQName);
                    } finally {
                        if (isIntransaction()) {
                            fireCasaEndpointInterfaceQNameChanged(endpointRef);
                            endTransaction();
                        }
                    }
                    throw e;
                }
            }
        } else {
            // TODO
        }
    }
    
    public void setEndpointServiceQName(final CasaEndpointRef endpointRef,
            final QName serviceQName) {
        CasaEndpoint endpoint = endpointRef.getEndpoint().get();
        
        startTransaction();
        try {
            endpoint.setServiceQName(serviceQName);
        } finally {
            if (isIntransaction()) {
                fireCasaEndpointServiceQNameChanged(endpointRef);
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
        
        assert false : "Unknown casa region: " + name; // NOI18N
        return null;
    }
    
    public void setCasaRegionWidth(final CasaRegion casaRegion, int width) {        
        startTransaction();
        try {
            casaRegion.setWidth(width);
        } finally {
            if (isIntransaction()) {
                endTransaction();
            }
        }
    }
    
    public void setUnitName(final CasaServiceUnit su, String unitName) {   
        if (su instanceof CasaBindingComponentServiceUnit) {
            throw new RuntimeException("Cannot change unit-name of binding component service unit.");
        }
        
        if (((CasaServiceEngineServiceUnit) su).isInternal()) {
            throw new RuntimeException("Cannot change unit-name of internal service engine service unit.");            
        }
        
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
        return getDocumentModelNamespaces(this);
    }
    
    /**
     * Gets the namespaces defined in any document model. Note that certain 
     * well-known namespaces are not included.
     *
     * @return a map mapping namespace prefix to namespace URI
     */
    private static Map<String, String> getDocumentModelNamespaces(
            AbstractDocumentModel model) {
        Map<String, String> ret = new TreeMap<String, String>();
        
        NamedNodeMap map = model.getRootComponent().getPeer().getAttributes();
        for (int j = 0; j < map.getLength(); j++) {
            Node node = map.item(j);
            
            String prefix = node.getPrefix();
            if (prefix != null && prefix.equals("xmlns")) {
                String localName = node.getLocalName();
                if (! localName.equals("xsi") // NOI18N         // TMP
                    && ! localName.equals("xlink")) { // NOI18N // TMP
                    String namespaceURI = node.getNodeValue();
                    ret.put(localName, namespaceURI);
                }
            }
        }
        
        return ret;
    }

    public String getJbiProjectType(Project p) {
        if (p == null) {
            return null;
        }

        // allow JavaEE project types
        if (JbiDefaultComponentInfo.isJavaEEProject(p)) {
            return JbiProjectConstants.JAVA_EE_SE_COMPONENT_NAME;
        }
        
        AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
        if (prov != null) {
            AntArtifact[] artifacts = prov.getBuildArtifacts();
            Iterator<String> artifactTypeItr = null;
            String artifactType = null;
            if (artifacts != null) {
                for (int i = 0; i < artifacts.length; i++) {
                    artifactTypeItr = this.artifactTypes.iterator();
                    while (artifactTypeItr.hasNext()){
                        artifactType = artifactTypeItr.next();
                        String arts = artifacts[i].getType();
                        if (arts.startsWith(artifactType)) {
                            int idx = arts.indexOf(':') + 1;
                            return arts.substring(idx);
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
// TODO This is only an example of how a sync might be done.
    public void sync() throws IOException {
        // This should reset the internal fields/state of saJBIModel
        // as well as make a call to saJBIModel.super.sync()
        super.sync();
        
        WSDLModel compAppWSDLModel = getCompAppWSDLModel(false);
        
        if (compAppWSDLModel != null) {
            compAppWSDLModel.sync();
        }

        cachedWSDLComponents.clear();
        
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


