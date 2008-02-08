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
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer;
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer.PortTransferable;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.netbeans.modules.websvc.manager.spi.WebServiceTransferManager;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.ui.actions.ViewWSDLAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WsdlPortNode extends AbstractNode {
    private WsdlSaas saas;
    private WsdlPort port;
    private Transferable transferable;

    public WsdlPortNode(WsdlSaas saas, WsdlPort port) {
        this(saas, port, new InstanceContent());
    }
    
    private WsdlPortNode(WsdlSaas saas, WsdlPort port, InstanceContent content) {
        super(new WsdlPortNodeChildren(saas, port), new AbstractLookup(content));
        this.port = port;
        this.saas = saas;
        content.add(saas);
        content.add(port);
        transferable = ExTransferable.create(new PortTransferable(new WebServiceMetaDataTransfer.Port(saas.getWsdlData(), port.getName())));
    }    
    
    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (WebServiceManagerExt ext : ManagerUtil.getExtensions()) {
            for (Action a : ext.getPortActions(this)) {
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
    
    private static final Image icon = 
        Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/wsport-closed.png"); // NOI18N
    private static final Image iconOpened = 
        Utilities.loadImage("org/netbeans/modules/websvc/manager/resources/wsport-open.png"); // NOI18N

    @Override
    public Image getIcon(int type){
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return iconOpened;
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
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("data"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("data");  // NOI18N
            ss.setDisplayName(NbBundle.getMessage(WsdlPortNode.class, "WS_INFO"));
            ss.setShortDescription(NbBundle.getMessage(WsdlPortNode.class, "WS_INFO"));
            sheet.put(ss);
        }
        
        // Port name (from the wsdl)
        ss.put( new PropertySupport.ReadOnly( "port", // NOI18N
                String.class,
                NbBundle.getMessage(WsdlPortNode.class, "PORT_NAME_IN_WSDL"),
                NbBundle.getMessage(WsdlPortNode.class, "PORT_NAME_IN_WSDL") ) {
            public Object getValue() {
                String portName = port.getName();
                return portName;
            }
        });
        
        // URL for the wsdl file (entered by the user)
        ss.put( new PropertySupport.ReadOnly( "URL", // NOI18N
                String.class,
                NbBundle.getMessage(WsdlPortNode.class, "WS_URL"),
                NbBundle.getMessage(WsdlPortNode.class, "WS_URL") ) {
            public Object getValue() {
                return saas.getUrl();
            }
        });
        
        return sheet;
    }
    
    public WsdlPort getPort() {
        return port;
    }
      
    // Handle copying and cutting specially:    
    @Override
    public boolean canCopy() {
        return true;
    }
    @Override
    public boolean canCut() {
        return false;
    }
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        Collection<? extends WebServiceTransferManager> managers = Lookup.getDefault().lookupAll(WebServiceTransferManager.class);
        Transferable result = transferable;
        
        for (WebServiceTransferManager m : managers) {
            result = m.addDataFlavors(result);
        }
        
        return result;
    }
}
