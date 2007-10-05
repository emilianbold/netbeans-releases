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

import java.io.IOException;
import java.util.Collections;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceGroupListener;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.model.WebServiceGroupEvent;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/** List of children of a webservices group node.
 *
 * @author Winston Prakash
 */
public class WebServiceGroupNodeChildren extends Children.Keys<String> implements WebServiceGroupListener {
    
    WebServiceGroup webserviceGroup;
    WebServiceListModel websvcListModel = WebServiceListModel.getInstance();
    
    public WebServiceGroupNodeChildren(WebServiceGroup websvcGroup) {
        webserviceGroup = websvcGroup;
        webserviceGroup.addWebServiceGroupListener(this);
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        updateKeys();
    }
    
    private void updateKeys() {
        setKeys(webserviceGroup.getWebServiceIds());
    }
    
    @Override
    protected void removeNotify() {
        java.util.List<String> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }
    
    protected Node[] createNodes(String id) {
        WebServiceData wsData = websvcListModel.getWebService(id);
        if (wsData.getWsdlService() == null) {
            final WebServiceData data = wsData;
            Runnable modeller = new Runnable() {
                public void run() {
                    try {
                        WebServiceManager.getInstance().addWebService(data);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            };
            
            WebServiceManager.getInstance().getRequestProcessor().post(modeller);
            
            AbstractNode waitNode = new AbstractNode(Children.LEAF);
            waitNode.setName(NbBundle.getMessage(WebServiceGroupNodeChildren.class, "NODE_LOAD_MSG"));
            waitNode.setIconBaseWithExtension("org/netbeans/modules/websvc/manager/resources/wait.gif"); // NOI18N
            return new Node[] { waitNode };
        }else {
            assert wsData.getWsdlService() != null;
            return new Node[] { new WebServiceNode(wsData) };
        }
    }
    
    public void webServiceAdded(WebServiceGroupEvent groupEvent) {
        updateKeys();
        refreshKey(groupEvent.getWebServiceId());
    }
    
    public void webServiceRemoved(WebServiceGroupEvent groupEvent) {
        updateKeys();
        refreshKey(groupEvent.getWebServiceId());
    }
    
}
