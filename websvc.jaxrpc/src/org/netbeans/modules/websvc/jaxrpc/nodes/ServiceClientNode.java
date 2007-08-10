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
 */

package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import org.netbeans.modules.websvc.core.webservices.action.ConfigureHandlerAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.core.webservices.action.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.jaxrpc.ServiceInformation;
import org.netbeans.modules.websvc.wsdl.config.ServiceInformationImpl;
import org.openide.loaders.DataObject;


/** Wrap wsdl node from wsdl directory in a filter, but display children from
 *  corresponding registry node, along with registry properties and short
 *  description.
 *
 * @author Peter Williams
 */

public class ServiceClientNode extends FilterNode implements PropertyChangeListener {
    
    private String serviceName;
    private Node registerNode;
    private DataObject dobj;
    
    public ServiceClientNode(Node wsdlNode, Node registerNode) {
        super(wsdlNode, (registerNode != null) ? new FilterNode.Children(registerNode) : Children.LEAF);
        
        this.registerNode = registerNode;
        this.serviceName = null;
        
        // !PW FIXME this should be reworked when probably to be passed in as a parameter
        // from ClientViewChildren when we enhance to handle multiple services per WSDL.
        dobj = (DataObject) wsdlNode.getCookie(DataObject.class);
        if(dobj != null) {
            ServiceInformationImpl wsdlModel = new ServiceInformationImpl(dobj);
            String [] serviceNames = wsdlModel.getServiceNames(); //dobj.getServiceNames();
            if(serviceNames != null && serviceNames.length > 0) {
                this.serviceName = serviceNames[0];
            }
        }
        
        WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
        registryView.addPropertyChangeListener(this);
    }
    
    public void destroy() throws java.io.IOException {
        super.destroy();
        
        /**
         * This code has been moved from the old WSDLDataObject which used to call removeServiceClient
         * Since we are now using the WsdlDataObject of the xml module, it has become necessary to call this here.
         */ 
        if(dobj != null){
            FileObject f = dobj.getPrimaryFile();
            WebServicesClientSupport wsc = WebServicesClientSupport.getWebServicesClientSupport(f);
            if(wsc != null){
                wsc.removeServiceClient(f.getName());
            }
        }
        WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
        registryView.removePropertyChangeListener(this);
    }
    
    public String getName() {
        return (registerNode != null) ? registerNode.getName() : super.getName();
    }
    
    public String getDisplayName() {
        return (registerNode != null) ? registerNode.getDisplayName() : super.getDisplayName();
    }
    
    public String getShortDescription() {
        // !PW FIXME what should the short description of this node really be?
        // If the service is registered, it's a formatted string with the attributes
        // of the service (name, ports, etc.)
        
        // show short description of registry node instead of WSDL node
        return registerNode != null ? registerNode.getShortDescription() : "Unregistered service";
    }
    
    public Node.PropertySet[] getPropertySets() {
        // !PW FIXME should do minimal property set for WSDL node (not the WSDL properties though.)
        // should also massage properties retrieved from registry, as some of them may
        // not apply (such as path to wsdl file.)
        
        // show property sheet for registry node instead of WSDL node
        return registerNode != null ? registerNode.getPropertySets() : new Node.PropertySet [0];
    }
    
    public Image getIcon(int type) {
        // !PW FIXME me need static source for Web Service Icon
        Image wsdlIcon = super.getIcon(type);
        
        // show icon for registry node instead of WSDL node
        return registerNode != null ? registerNode.getIcon(type) : wsdlIcon;
    }
    
    public Image getOpenedIcon(int type) {
        // !PW FIXME me need static source for Web Service Opened Icon
        Image wsdlOpenedIcon = super.getOpenedIcon(type);
        
        // show opened icon for registry node instead of WSDL node
        return registerNode != null ? registerNode.getOpenedIcon(type) : wsdlOpenedIcon;
    }
    
    public Action[] getActions(boolean context) {
        FileObject fo = dobj.getPrimaryFile();
        WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(fo);
        // TODO - this is a hack so that we don't need a new api method - will have to resolve it once I know what's the best way, maybe this is enough
        if (clientSupport.getDeploymentDescriptor() == null) {
            return new Action[] {
                org.openide.util.actions.SystemAction.get( org.netbeans.modules.websvc.jaxrpc.actions.RefreshServiceAction.class ),
                org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.DeleteAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.ToolsAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.PropertiesAction.class )
            };
        }
        return new Action[] {
            org.openide.util.actions.SystemAction.get( org.netbeans.modules.websvc.jaxrpc.actions.RefreshServiceAction.class ),
            org.openide.util.actions.SystemAction.get(ConfigureHandlerAction.class),
            org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class ),
            null,
            //SystemAction.get(WSEditAttributesAction.class),
            //null,
            org.openide.util.actions.SystemAction.get( org.openide.actions.DeleteAction.class ),
            null,
            org.openide.util.actions.SystemAction.get( org.openide.actions.ToolsAction.class ),
            null,
            org.openide.util.actions.SystemAction.get( org.openide.actions.PropertiesAction.class )
        };
    }
    
    public Node.Cookie getCookie(Class type) {
        if (type == ConfigureHandlerCookie.class) {
            FileObject fo = dobj.getPrimaryFile();
            Project project = FileOwnerQuery.getOwner(fo);
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(fo);
            if (clientSupport.getDeploymentDescriptor() != null) {   // TODO - see above, this is a same hack
                return new ConfigureHandlerCookieImpl(serviceName, project, clientSupport, fo );
            }
        }
        //        else if(type == EditWSAttributesCookie.class){
        //            return new EditWSAttributesCookieImpl(this, null);
        //        }
        else if (type == ServiceInformation.class){
            return new ServiceInformationImpl(dobj);
        }
        
        Node.Cookie result = super.getCookie(type);
        
        if(result == null && registerNode != null) {
            result = registerNode.getCookie(type);
        }
        
        return result;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(WebServicesRegistryView.WEB_SERVICE_ADDED.equals(evt.getPropertyName())) {
            if(registerNode == null && serviceName != null) {
                Node addedServiceNode = (Node) evt.getNewValue();
                // !PW FIXME when named services in registry are changed to be case sensitive,
                // change these comparisons also.
                if(serviceName.equalsIgnoreCase(addedServiceNode.getName())) {
                    registerNode = addedServiceNode;
                    setChildren(new FilterNode.Children(registerNode));
                    fireIconChange();
                }
            }
        } else if(WebServicesRegistryView.WEB_SERVICE_REMOVED.equals(evt.getPropertyName())) {
            if(registerNode != null && serviceName != null) {
                String removedServiceName = (String) evt.getOldValue();
                // !PW FIXME when named services in registry are changed to be case sensitive,
                // change these comparisons also.
                if(serviceName.equalsIgnoreCase(removedServiceName)) {
                    registerNode = null;
                    setChildren(Children.LEAF);
                    fireIconChange();
                }
            }
        }
    }
}

