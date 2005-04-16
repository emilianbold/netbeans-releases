/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.nodes.WebServicesNode;
import org.netbeans.modules.websvc.registry.nodes.WebServicesRootNode;
import org.netbeans.modules.websvc.registry.util.Util;
import org.netbeans.modules.websvc.registry.wsdl.WSDLInfo;

import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;

/**
 *
 * @author Peter Williams
 */
public class RegistryViewImpl implements WebServicesRegistryView, PropertyChangeListener {

    public RegistryViewImpl() {
        WebServiceListModel.getInstance().addPropertyChangeListener(this);
    }

    // !PW Does this have any use?
    public Node getRegistryRootNode() {
        Node rootNode = null;

        try {
            FileSystem defaultFileSystem = Repository.getDefault().getDefaultFileSystem();
            FileObject fo = defaultFileSystem.findResource("UI/Runtime");    //NOI18N
            DataFolder df = (DataFolder) DataObject.find(fo);
            Lookup l = new FolderLookup(df).getLookup();
            rootNode = (WebServicesRootNode) l.lookup(WebServicesRootNode.class);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            rootNode = null;
        }

        return rootNode;
    }

    public Node[] getWebServiceNodes(FileObject wsdlFile) {
        // locate all service nodes that came from this wsdl file.
        // I could turn WSDL file into blocks of WebServiceData, that I then search
        // for in the list model?

        Node [] result = null;
        ArrayList foundServices = new ArrayList();

        WebServiceListModel model = WebServiceListModel.getInstance();
        Set registeredServices = model.getWebServiceSet();
        List serviceNames = getServiceNames(wsdlFile);

        for(Iterator nameIter = serviceNames.iterator(); nameIter.hasNext(); ) {
            String searchName = (String) nameIter.next();

            for(Iterator iter = registeredServices.iterator(); iter.hasNext(); ) {
                WebServiceData wsData = (WebServiceData) iter.next();
                if(searchName.equalsIgnoreCase(wsData.getName())) {
                    foundServices.add(new WebServicesNode(wsData));
                    break;
                }
            }
        }

        if(foundServices.size() > 0) {
            result = (Node []) foundServices.toArray(new Node [foundServices.size()]);
        }

        return result;
    }

    public boolean isServiceRegistered(String serviceName) {
        return WebServiceListModel.getInstance().webServiceExists(serviceName);
    }

    public boolean registerService(FileObject wsdlFile, boolean replaceService) {
        boolean result;

        try {
            result = registerService(wsdlFile.getURL(), "default", replaceService);
        } catch(FileStateInvalidException ex) {
            result = false;
        }

        return result;
    }

    public boolean registerService(URL wsdlUrl, boolean replaceService) {
        return registerService(wsdlUrl, "default", replaceService);
    }

    private boolean registerService(URL wsdlUrl, String groupId, boolean replaceService) {
        boolean result = false;

        // Retrieve WSDL and convert into a Set of WebServiceData objects
        WSDLInfo wsdLInfo = new WSDLInfo();
        wsdLInfo.setWsdlUrl(wsdlUrl);
        wsdLInfo.setPackageName("webservice");	// NOI18N
        wsdLInfo.setRemoveGeneratedFiles(true);

        if(wsdLInfo.create()) {
            Set webServices = wsdLInfo.getWebServices();
            if(webServices != null && webServices.size() > 0) { 
                Iterator iter =  webServices.iterator();
                boolean duplicateFound = false;
                WebServiceListModel wsListModel = WebServiceListModel.getInstance();

                // Assume that all of the web services adds failed.  For each 
                // success, remove the web service from the failed set.
                HashSet addFailedWebServices = new HashSet();
                addFailedWebServices.addAll(webServices);

                while(iter.hasNext()){
                    WebServiceData wsData = (WebServiceData) iter.next();

                    if(wsListModel.webServiceExists(wsData)) {
                        if(replaceService) {
                            // remove old service first
//                            wsListModel.removeWebService(wsData.getDisplayName());
                            wsListModel.removeWebService(wsData);
                        } else {
                            // !PW Failed: Web service of that name already exists in model
                            continue;
                        }
                    }

                    // Now create the client code for the web service.
                    String jarFileName = System.getProperty("netbeans.user") +"/websvc/" + "webservice" + new Date().getTime() + ".jar";

                    if(!Util.createWSJar(wsData, null, jarFileName)) {
                        // !PW FIXME failed compilation
                        continue;
                    }

                    // Set the jar filename 
                    // !PW why isn't this done by Util.createWSJar?!
                    wsData.setProxyJarFileName(jarFileName);

                    // Add it to the list of web services for Server Navigator
                    wsListModel.addWebService(wsData);

                    // Put service in 'default'.
                    WebServiceGroup wsGroup = wsListModel.getWebServiceGroup(groupId);

                    // !PW Fix for 49717 - If this group does not exist yet, create it.
                    if(wsGroup == null) {
                        wsGroup = new WebServiceGroup(groupId);
                        wsListModel.addWebServiceGroup(wsGroup);
                    }

                    wsGroup.add(wsData.getId());
                    wsData.setGroupId(wsGroup.getId());

                    addFailedWebServices.remove(wsData);
                }

                // Return true if we added all services in this WSDL file, otherwise false.
                // !PW Need better error control and messaging.
                result = (addFailedWebServices.size() == 0);
            }
        } else {
            // !PW Failed to create WsdlInfo (bad URL, bad parse, etc.)
        }

        return result;
    }

    private List getServiceNames(FileObject wsdlFile) {
        List result = Collections.EMPTY_LIST;

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            ServiceNameParser handler= new ServiceNameParser();
            saxParser.parse(wsdlFile.getInputStream(), handler);
            result = handler.getServiceNameList();
        } catch(ParserConfigurationException ex) {
            // Bogus WSDL, return empty list.
        } catch(SAXException ex) {
            // Bogus WSDL, return empty list.
        } catch(IOException ex) {
            // Bogus WSDL, return empty list.
        }

        return result;
    }

    /** Property change support
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(WebServiceListModel.MODEL_SERVICE_ADDED.equals(evt.getPropertyName())) {
            WebServiceData wsData = (WebServiceData) evt.getNewValue();
            fireServiceAdded(new WebServicesNode(wsData));
        } else if(WebServiceListModel.MODEL_SERVICE_REMOVED.equals(evt.getPropertyName())) {
            WebServiceData wsData = (WebServiceData) evt.getOldValue();
            fireServiceRemoved(wsData.getName());
        }
    }

    private void fireServiceAdded(Node wsNode) {
        propertyChangeSupport.firePropertyChange(WEB_SERVICE_ADDED, null, wsNode);
    }

    private void fireServiceRemoved(String serviceName) {
        propertyChangeSupport.firePropertyChange(WEB_SERVICE_REMOVED, serviceName, null);
    }

    private static final class ServiceNameParser extends DefaultHandler {

        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl";
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/";

        private ArrayList serviceNameList;

        ServiceNameParser() {
            serviceNameList = new ArrayList();
        }

        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("service".equals(localname)) {
                    serviceNameList.add(attributes.getValue("name"));
                }
            }
        }

        public List/*String*/ getServiceNameList() {
            return serviceNameList;
        }
    }
}
