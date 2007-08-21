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

package org.netbeans.modules.websvc.manager.nodes;

import org.netbeans.modules.websvc.manager.util.WebServiceLibReferenceHelper;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Collection;

import org.netbeans.modules.websvc.manager.api.WebServiceManager;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer;
import org.netbeans.modules.websvc.manager.spi.WebServiceTransferManager;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.nodes.Node;

import org.openide.nodes.Sheet;
import org.openide.nodes.AbstractNode;

import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet.Set;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;

import java.awt.Image;

import org.netbeans.modules.websvc.manager.model.WebServiceData;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.netbeans.modules.websvc.manager.actions.ViewWSDLAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.openide.util.datatransfer.ExTransferable;

/**
 * The node displayed in the Server Navigator for a web service port
 *
 * @author  david, cao
 */
public class WebServicesPortNode  extends AbstractNode implements Node.Cookie {
    protected static final DataFlavor PORT_NODE_FLAVOR;
    
    static {
        try {
            PORT_NODE_FLAVOR = new java.awt.datatransfer.DataFlavor("application/x-java-netbeans-websvcmgr-port;class=org.openide.nodes.Node");
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    
    }
    
    private WebServiceData wsData;
    private WsdlPort port;
    private final Transferable transferable;
    
    public WebServicesPortNode() {
        this(null,null);
    }
    
    public WebServicesPortNode(WebServiceData inData, WsdlPort  inPort) {
        super(new WebServicesPortNodeChildren(inData, inPort));
        
        wsData = inData;
        
        if(null == inPort) {
            throw new NullPointerException("Cannot instantiate WebServicesPortNode with null port");
        }
        port = inPort;
        setName(port.getName());
        transferable = ExTransferable.create(new PortTransferable(new WebServiceMetaDataTransfer.Port(inData, inPort.getName())));
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (WebServiceManagerExt ext : ManagerUtil.getExtensions()) {
            for (Action a : ext.getPortActions()) {
                actions.add(a);
            }
        }
        actions.add(SystemAction.get(ViewWSDLAction.class));
        return actions.toArray(new Action[actions.size()]);
    }
    
    public Action getPreferredAction() {
        Action[] actions = getActions(true);
        return actions.length > 0 ? actions[0] : null;
    }
    
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/wsport-closed.png"); // NOI18N
    }
    
    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/wsport-open.png"); // NOI18N
    }
    
    public void destroy() throws IOException{
        WebServiceManager.getInstance().removeWebService(wsData);
        super.destroy();
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /**
     * Create a property sheet for the individual W/S port node. The properties sheet contains the
     * the following properties:
     *  - Name of the port
     *  - WSDL URL
     *  - Endpoint Address
     *
     * @return property sheet for the data source nodes
     */
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("data"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("data");  // NOI18N
            ss.setDisplayName(NbBundle.getMessage(WebServicesPortNode.class, "WS_INFO"));
            ss.setShortDescription(NbBundle.getMessage(WebServicesPortNode.class, "WS_INFO"));
            sheet.put(ss);
        }
        
        // Port name (from the wsdl)
        ss.put( new PropertySupport.ReadOnly( "port", // NOI18N
                String.class,
                NbBundle.getMessage(WebServicesPortNode.class, "PORT_NAME_IN_WSDL"),
                NbBundle.getMessage(WebServicesPortNode.class, "PORT_NAME_IN_WSDL") ) {
            public Object getValue() {
                String portName = port.getName();
                return portName;
            }
        });
        
        // URL for the wsdl file (entered by the user)
        ss.put( new PropertySupport.ReadOnly( "URL", // NOI18N
                String.class,
                NbBundle.getMessage(WebServicesPortNode.class, "WS_URL"),
                NbBundle.getMessage(WebServicesPortNode.class, "WS_URL") ) {
            public Object getValue() {
                return wsData.getURL();
            }
        });
        
        return sheet;
    }
    
    public WsdlPort getPort() {
        return port;
    }
      
    // Handle copying and cutting specially:    
    public boolean canCopy() {
        return isMethodDroppable();
    }
    public boolean canCut() {
        return isMethodDroppable();
    }
    
    public boolean isMethodDroppable() {
        boolean isJ2EE_15 = ManagerUtil.isJavaEE5Project(WebServiceLibReferenceHelper.getActiveProject());
        if (wsData != null && isJ2EE_15) {
            return wsData.isJaxWsEnabled();
        } else if (wsData != null && !isJ2EE_15) {
            return wsData.isJaxRpcEnabled();
        } else {
            return true;
        }
    }
    
    public Transferable clipboardCopy() throws IOException {
        if (!isMethodDroppable()) {
            return super.clipboardCopy();
        }
        
        Collection<? extends WebServiceTransferManager> managers = Lookup.getDefault().lookupAll(WebServiceTransferManager.class);
        Transferable result = transferable;
        
        for (WebServiceTransferManager m : managers) {
            result = m.addDataFlavors(result);
        }
        
        return result;
    }
    
    static final class PortTransferable implements Transferable {
        private static final DataFlavor[] SUPPORTED_FLAVORS = { WebServiceMetaDataTransfer.PORT_FLAVOR, PORT_NODE_FLAVOR };
        private final WebServiceMetaDataTransfer.Port transferData;
        
        public PortTransferable(WebServiceMetaDataTransfer.Port transferData) {
            this.transferData = transferData;
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return SUPPORTED_FLAVORS;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == SUPPORTED_FLAVORS[0] || flavor == SUPPORTED_FLAVORS[1];
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }else {
                return transferData;
            }
        }
    }
    
    
    public WebServiceData getWebServiceData() {
        return this.wsData;
    }
}
