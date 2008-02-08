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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.netbeans.modules.websvc.core.WebServiceTransferable;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.netbeans.modules.websvc.saas.ui.actions.DeleteServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.RefreshServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.ViewApiDocAction;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WsdlSaasNode extends AbstractNode {
    WsdlSaas saas;

    public WsdlSaasNode(WsdlSaas saas) {
        this(saas, new InstanceContent());
    }
    
    protected WsdlSaasNode(WsdlSaas saas, InstanceContent content) {
        super(new WsdlSaasNodeChildren(saas));
        this.saas = saas;
        content.add(saas);
    }

    public String getDisplayName() {
        return saas.getDisplayName();
    }
    
    public String getShortDescription() {
        return saas.getDescription();
    }
    
    private static final java.awt.Image ICON =
       org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/webservice.png" ); //NOI18N
    
    @Override
    public java.awt.Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (SaasNodeActionsProvider ext : SaasUtil.getSaasNodeActionsProviders()) {
            for (Action a : ext.getSaasActions(this.getLookup())) {
                actions.add(a);
            }
        }
        actions.add(SystemAction.get(ViewApiDocAction.class));
        actions.add(SystemAction.get(DeleteServiceAction.class));
        actions.add(SystemAction.get(RefreshServiceAction.class));

        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public void destroy() throws IOException{
        WebServiceListModel.getInstance().removeWebService(saas.getWsdlData().getId());
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
            ss.setDisplayName(NbBundle.getMessage(WsdlSaasNode.class, "WS_INFO"));
            ss.setShortDescription(NbBundle.getMessage(WsdlSaasNode.class, "WS_INFO"));
            sheet.put(ss);
        }
        
        // Service name (from the wsdl)
        ss.put( new PropertySupport.ReadOnly( "name", // NOI18N
                String.class,
                NbBundle.getMessage(WsdlSaasNode.class, "PORT_NAME_IN_WSDL"),
                NbBundle.getMessage(WsdlSaasNode.class, "PORT_NAME_IN_WSDL") ) {
            public Object getValue() {
                return getName();
            }
        });
        
        // URL for the wsdl file (entered by the user)
        ss.put( new PropertySupport.ReadOnly( "URL", // NOI18N
                String.class,
                NbBundle.getMessage(WsdlSaasNode.class, "WS_URL"),
                NbBundle.getMessage(WsdlSaasNode.class, "WS_URL") ) {
            public Object getValue() {
                return saas.getUrl();
            }
        });
        
        return sheet;
    }
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        if (!saas.getWsdlData().isResolved()) {
            return super.clipboardCopy();
        }
        
        // enable drag-and-drop of a single port if only one is available
        Node[] children = getChildren().getNodes();
        if (children != null && children.length == 1) {
            final Transferable portTransferable = children[0].clipboardCopy();
            final Transferable wsTransferable = super.clipboardCopy();
            final Transferable webserviceTransferable = ExTransferable.create(
                    new WebServiceTransferable(new WebServiceReference(
                    getWsdlURL(), saas.getWsdlModel().getName(), "")));
            
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
        java.lang.String wsdlURL = saas.getUrl();
        try {
            url = new URL(wsdlURL);
        } catch (MalformedURLException ex) {
            //attempt to recover
            File f = new File(wsdlURL);
            try{
                url = f.getCanonicalFile().toURI().normalize().toURL();
            } catch (IOException exc) {
                Exceptions.printStackTrace(exc);
            }
        }
        return url;
    }
    
}
