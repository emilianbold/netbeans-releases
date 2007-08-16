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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.nodes;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.actions.DeleteWebServiceAction;
import org.netbeans.modules.websvc.manager.actions.ViewWSDLAction;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author quynguyen
 */
public class WebServiceNode extends AbstractNode implements Node.Cookie {
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
        super(new WebServiceNodeChildren(wsData));
        this.wsData = wsData;
        
        setName(wsData.getWsdlService().getName());
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(ViewWSDLAction.class),
            SystemAction.get(DeleteWebServiceAction.class)
        };
    }
    
    @Override
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/visualweb/websvcmgr/resources/webservice.png");
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/visualweb/websvcmgr/resources/webservice.png");
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
        // enable drag-and-drop of a single port if only one is available
        Node[] children = getChildren().getNodes();
        if (children != null && children.length == 1) {
            final Transferable portTransferable = children[0].clipboardCopy();
            final Transferable wsTransferable = super.clipboardCopy();
            
            DataFlavor[] portFlavors = portTransferable.getTransferDataFlavors();
            DataFlavor[] wsFlavors = wsTransferable.getTransferDataFlavors();
            
            final DataFlavor[] flavors = 
                    new DataFlavor[portFlavors.length + wsFlavors.length];
            
            int j = 0;
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
                    if (portTransferable.isDataFlavorSupported(flavor)) {
                        return portTransferable.getTransferData(flavor);
                    }else if (wsTransferable.isDataFlavorSupported(flavor)) {
                        return wsTransferable.getTransferData(flavor);
                    }else {
                        throw new UnsupportedFlavorException(flavor);
                    }
                }
            };
        }else {
            return super.clipboardCopy();
        }
    }
}
