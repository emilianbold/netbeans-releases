/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet.Set;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import org.netbeans.modules.websvc.spi.support.ConfigureHandlerAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.jaxrpc.ServiceInformation;
import org.netbeans.modules.websvc.wsdl.config.ServiceInformationImpl;
import org.netbeans.modules.websvc.wsdl.config.WsCompileConfigDataObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


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
        if (registerNode != null) {
            setValue("wsdl-url",registerNode.getValue("wsdl-url")); //NOI18N
        }
        
        WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
        registryView.addPropertyChangeListener(this);
    }
    
    @Override
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
            FileObject parentFO = f.getParent();
            if(parentFO != null && parentFO.isFolder()) {
                FileObject configFO = parentFO.getFileObject(f.getName() + WsCompileConfigDataObject.WSCOMPILE_CONFIG_FILENAME_SUFFIX, "xml");
                if(configFO!=null&&configFO.isValid())
                    configFO.delete();
            }
        }
        WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
        registryView.removePropertyChangeListener(this);
    }
    
    @Override
    public String getName() {
        return (registerNode != null) ? registerNode.getName() : super.getName();
    }
    
    @Override
    public String getDisplayName() {
        return (registerNode != null) ? registerNode.getDisplayName() : NbBundle.getMessage(ServiceClientNode.class, "MSG_parsingWSDL");
    }
    
    @Override
    public String getShortDescription() {
        // !PW FIXME what should the short description of this node really be?
        // If the service is registered, it's a formatted string with the attributes
        // of the service (name, ports, etc.)
        
        // show short description of registry node instead of WSDL node
        return registerNode != null ? registerNode.getShortDescription() : "Unregistered service";
    }
    
    public String getURL() {
        WebServicesClientSupport wsc = WebServicesClientSupport.
                getWebServicesClientSupport(dobj.getPrimaryFile());
        if(wsc != null){
            return wsc.getWsdlSource(dobj.getName());
        }
        return null;
    }
    
    @Override
    public Node.PropertySet[] getPropertySets() {
        // !PW FIXME should do minimal property set for WSDL node (not the WSDL properties though.)
        // should also massage properties retrieved from registry, as some of them may
        // not apply (such as path to wsdl file.)
        
        // show property sheet for registry node instead of WSDL node
        //return registerNode != null ? registerNode.getPropertySets() : new Node.PropertySet [0];
        if(registerNode!= null) {
            Node.PropertySet[] registerNodePropSets = registerNode.getPropertySets();
            if(getURL()!=null) {
                for(Node.PropertySet registerNodePropSet:registerNodePropSets) {
                    if(registerNodePropSet instanceof Set && 
                            "data".equals(((Set)registerNodePropSet).getName())) {
                        Set propSet = (Set) registerNodePropSet;
                        try {
                            Node.Property<String> p = new Reflection<String>(this, String.class, "getURL", null); // NOI18N
                            Node.Property registerNodeProp = propSet.get("URL");
                            p.setName(registerNodeProp.getName());
                            p.setDisplayName(registerNodeProp.getDisplayName());
                            p.setShortDescription(registerNodeProp.getShortDescription());
                            propSet.put(p);
                            break;
                        } catch (NoSuchMethodException ex) {
                        }
                    }
                }
            }
            return registerNodePropSets;
        }
        else {
            return new Node.PropertySet [0];
        }
    }
    
    @Override
    public Image getIcon(int type) {
        // show icon for registry node instead of WSDL node
        return registerNode != null ? registerNode.getIcon(type) : getWaitIcon();
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        // show opened icon for registry node instead of WSDL node
        return registerNode != null ? registerNode.getOpenedIcon(type) : getWaitIcon();
    }

    private static Image waitIcon;
    private static synchronized Image getWaitIcon() {
        if (waitIcon == null) {
            waitIcon = ImageUtilities.loadImage("org/netbeans/modules/websvc/jaxrpc/nodes/resources/wait.gif"); //NOI18N
        }
        return waitIcon;
    }
    
    @Override
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
    
    @Override
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
                    setValue("wsdl-url",registerNode.getValue("wsdl-url")); //NOI18N
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

