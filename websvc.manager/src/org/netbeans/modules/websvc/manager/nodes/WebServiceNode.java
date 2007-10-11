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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.nodes;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.netbeans.modules.websvc.core.WebServiceTransferable;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.actions.DeleteWebServiceAction;
import org.netbeans.modules.websvc.manager.actions.RefreshWebServiceAction;
import org.netbeans.modules.websvc.manager.actions.ViewWSDLAction;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author quynguyen
 */
public class WebServiceNode extends AbstractNode implements Node.Cookie, PropertyChangeListener {
    protected static final DataFlavor WEBSERVICE_NODE_FLAVOR;
    
    static {
        try {
            WEBSERVICE_NODE_FLAVOR = new DataFlavor("application/x-java-netbeans-websvcmgr-webservice;class=org.openide.nodes.Node");
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
        
    }
    
    private final WebServiceData wsData;
    
    public WebServiceNode() {
        this(null);
    }
    
    public WebServiceNode(WebServiceData wsData) {
        this(wsData, new InstanceContent());
    }
    
    private WebServiceNode(WebServiceData wsData, InstanceContent content) {
        super(new WebServiceNodeChildren(wsData), new AbstractLookup(content));
        this.wsData = wsData;
        content.add(wsData);
        content.add(this);
        if (wsData.getWsdlService() != null) {
            setName(wsData.getWsdlService().getName());
        }else {
            setName(getUnresolvedName());
        }
        wsData.addPropertyChangeListener(WeakListeners.create(PropertyChangeListener.class, this, wsData));
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (WebServiceManagerExt ext : ManagerUtil.getExtensions()) {
            for (Action a : ext.getWebServiceActions(this)) {
                actions.add(a);
            }
        }
        actions.add(SystemAction.get(ViewWSDLAction.class));
        actions.add(SystemAction.get(RefreshWebServiceAction.class));
        actions.add(SystemAction.get(DeleteWebServiceAction.class));
        return actions.toArray(new Action[actions.size()]);
    }
    
    @Override
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/webservice.png");
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/webservice.png");
    }
    
    @Override
    public void destroy() throws IOException{
        WebServiceManager.getInstance().removeWebService(wsData);
        super.destroy();
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    /**
     * Create a property sheet for the individual W/S port node. The properties sheet contains the
     * the following properties:
     *  - WSDL URL
     *  - Endpoint Address
     *
     * @return property sheet for the data source nodes
     */
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("data"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("data");  // NOI18N
            ss.setDisplayName(NbBundle.getMessage(WebServiceNode.class, "WS_INFO"));
            ss.setShortDescription(NbBundle.getMessage(WebServiceNode.class, "WS_INFO"));
            sheet.put(ss);
        }
        
        // Service name (from the wsdl)
        ss.put( new PropertySupport.ReadOnly( "name", // NOI18N
                String.class,
                NbBundle.getMessage(WebServiceNode.class, "PORT_NAME_IN_WSDL"),
                NbBundle.getMessage(WebServiceNode.class, "PORT_NAME_IN_WSDL") ) {
            public Object getValue() {
                return getName();
            }
        });
        
        // URL for the wsdl file (entered by the user)
        ss.put( new PropertySupport.ReadOnly( "URL", // NOI18N
                String.class,
                NbBundle.getMessage(WebServiceNode.class, "WS_URL"),
                NbBundle.getMessage(WebServiceNode.class, "WS_URL") ) {
            public Object getValue() {
                return wsData.getURL();
            }
        });
        
        return sheet;
    }
    
    public WebServiceData getWebServiceData() {
        return wsData;
    }
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        if (!wsData.isResolved()) {
            return super.clipboardCopy();
        }
        
        // enable drag-and-drop of a single port if only one is available
        Node[] children = getChildren().getNodes();
        if (children != null && children.length == 1) {
            final Transferable portTransferable = children[0].clipboardCopy();
            final Transferable wsTransferable = super.clipboardCopy();
            final Transferable webserviceTransferable = ExTransferable.create(new WebServiceTransferable(new WebServiceReference(getWsdlURL(), wsData.getName(), "")));
            
            DataFlavor[] portFlavors = portTransferable.getTransferDataFlavors();
            DataFlavor[] wsFlavors = wsTransferable.getTransferDataFlavors();
            DataFlavor[] webserviceFlavors = webserviceTransferable.getTransferDataFlavors();
            
            final DataFlavor[] flavors =
                    new DataFlavor[portFlavors.length + wsFlavors.length + webserviceFlavors.length];
            
            int j = 0;
            for(int i = 0; i <webserviceFlavors.length; i++){
                flavors[j++] = webserviceFlavors[i];
            }
            for (int i = 0; i < portFlavors.length; i++) {
                flavors[j++] = portFlavors[i];
            }
            for (int i = 0; i < wsFlavors.length; i++) {
                flavors[j++] = wsFlavors[i];
            }
            
            return new Transferable() {
                public DataFlavor[] getTransferDataFlavors() {
                    return flavors;
                }
                
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    for (int i = 0; i < flavors.length; i++) {
                        if (flavors[i].equals(flavor)) {
                            return true;
                        }
                    }
                    return false;
                }
                
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    if (webserviceTransferable.isDataFlavorSupported(flavor)){
                        return webserviceTransferable.getTransferData(flavor);
                    } else if (portTransferable.isDataFlavorSupported(flavor)) {
                        return portTransferable.getTransferData(flavor);
                    }else if (wsTransferable.isDataFlavorSupported(flavor)) {
                        return wsTransferable.getTransferData(flavor);
                    } else {
                        throw new UnsupportedFlavorException(flavor);
                    }
                }
            };
        }else {
            return super.clipboardCopy();
        }
    }
    
    private URL getWsdlURL(){
        URL url = null;
        java.lang.String wsdlURL = wsData.getURL();
        try {
            url = new URL(wsdlURL);
        } catch (MalformedURLException ex) {
            //attempt to recover
            File f = new File(wsdlURL);
            try{
                url = f.getCanonicalFile().toURI().normalize().toURL();
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
        }
        return url;
    }
    
    private String getUnresolvedName() {
        String wsdlName = "";
        try {
            URL wsdl = new URL(wsData.getOriginalWsdl());
            String path = wsdl.getPath();
            int i = path.lastIndexOf("/"); // NOI1N

            if (i >= 0 && i < path.length() - 1) {
                wsdlName = path.substring(i + 1);
            }
        } catch (IOException ex) {
        }
        
        wsdlName += "(" + NbBundle.getMessage(WebServiceNode.class, "WS_UNRESOLVED") + ")"; // NOI18N
        return wsdlName;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("resolved")) { // NOI18N
            Object newValue = evt.getNewValue();
            if (newValue instanceof Boolean) {
                boolean resolved = ((Boolean)newValue).booleanValue();
                if (resolved) {
                    setName(wsData.getWsdlService().getName());
                }else {
                    setName(getUnresolvedName());
                }
                
            }
        }
    }
    
}
