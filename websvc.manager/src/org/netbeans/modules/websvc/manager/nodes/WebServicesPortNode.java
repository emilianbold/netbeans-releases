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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.manager.nodes;

import java.util.Collection;

import org.netbeans.modules.websvc.manager.WebServiceManager;
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
import org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer.PortTransferable;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * The node displayed in the Server Navigator for a web service port
 *
 * @author  david, cao
 */
public class WebServicesPortNode  extends AbstractNode implements Node.Cookie {
    
    private WebServiceData wsData;
    private WsdlPort port;
    private final Transferable transferable;
    
    public WebServicesPortNode() {
        this(null,null);
    }
    
    public WebServicesPortNode(WebServiceData inData, WsdlPort  inPort) {
        this(inData, inPort, new InstanceContent());
    }
    
    private WebServicesPortNode(WebServiceData inData, WsdlPort  inPort, InstanceContent content) {
        super(new WebServicesPortNodeChildren(inData, inPort), new AbstractLookup(content));
        
        wsData = inData;
        
        if(null == inPort) {
            throw new NullPointerException("Cannot instantiate WebServicesPortNode with null port");
        }
        port = inPort;
        content.add(wsData);
        content.add(port);
        setName(port.getName());
        transferable = ExTransferable.create(new PortTransferable(new WebServiceMetaDataTransfer.Port(inData, inPort.getName())));
    }
    
    // Create the popup menu:
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
        return true;
    }
    public boolean canCut() {
        return false;
    }
    
    public Transferable clipboardCopy() throws IOException {
        Collection<? extends WebServiceTransferManager> managers = Lookup.getDefault().lookupAll(WebServiceTransferManager.class);
        Transferable result = transferable;
        
        for (WebServiceTransferManager m : managers) {
            result = m.addDataFlavors(result);
        }
        
        return result;
    }
    
    public WebServiceData getWebServiceData() {
        return this.wsData;
    }
}
