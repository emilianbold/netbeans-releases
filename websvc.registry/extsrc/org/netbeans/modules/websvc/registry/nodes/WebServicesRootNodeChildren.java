/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.websvc.registry.nodes;

import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceGroupListener;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.model.WebServiceListModelListener;
import java.util.*;

import org.openide.nodes.*;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.websvc.registry.util.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** List of children of a containing node.
 * Remember to document what your permitted keys are!
 *
 * @author octav, Winston Prakash
 */
public class WebServicesRootNodeChildren extends Children.Keys implements WebServiceGroupListener, WebServiceListModelListener{
    
    WebServiceGroup defaultGroup = null;
    
    public WebServicesRootNodeChildren() {
        
    }
    private WebServiceGroup getDefaultGroup(){
        if (defaultGroup==null){
            WebServiceListModel websvcListModel = WebServiceListModel.getInstance();
            defaultGroup = websvcListModel.getWebServiceGroup("default");
            if(defaultGroup == null) defaultGroup = new WebServiceGroup("default");
            websvcListModel.addWebServiceGroup(defaultGroup);
            defaultGroup.addWebServiceGroupListener(this);
            websvcListModel.addWebServiceListModelListener(this);
            
        }
        return defaultGroup;
        
    }
    protected void addNotify() {
        super.addNotify();
        getDefaultGroup();
        updateKeys();
    }
    
    private void updateKeys() {
        WebServiceListModel websvcListModel = WebServiceListModel.getInstance();
        WebServiceGroup[] keys = new WebServiceGroup[websvcListModel.getWebServiceGroupSet().size()];
        Iterator iter = websvcListModel.getWebServiceGroupSet().iterator();
//        System.out.println("updateKeys websvcListModel"+websvcListModel);
        int counter =0;
        while(iter.hasNext()){
            keys[counter++]= (WebServiceGroup)iter.next();
//            System.out.println("updateKeys "+ keys[counter-1]);
        }
        setKeys(keys);
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        Set nodes = new HashSet();
        if (key instanceof WebServiceGroup) {
            WebServiceGroup wsGroup = (WebServiceGroup)key;
            if(wsGroup.getId().equals(getDefaultGroup().getId())){
                WebServiceListModel websvcListModel = WebServiceListModel.getInstance();
                Iterator iter = getDefaultGroup().getWebServiceIds().iterator();
                while(iter.hasNext()){
                    WebServiceData wsData = websvcListModel.getWebService((String)iter.next());
//                System.out.println("Adding  nodes.add( new WebServicesNode(data));\n\n");
                    nodes.add(new WebServicesNode(wsData));
                }
            }else{
//                System.out.println("Adding  nodes.add( new WebServiceGroupNode(wsGroup));\n\n");
                nodes.add( new WebServiceGroupNode(wsGroup));
            }
        }
        return (Node[])nodes.toArray(new Node[nodes.size()]);
    }
    
    public void webServiceGroupAdded(org.netbeans.modules.websvc.registry.model.WebServiceListModelEvent modelEvent) {
//        System.out.println("!in children nodes: webServiceGroupAdded is called!!!!"+this);
        updateKeys();
        //refreshKey(websvcListModel.getWebServiceGroup(modelEvent.getWebServiceGroupId()));
    }
    
    public void webServiceGroupRemoved(org.netbeans.modules.websvc.registry.model.WebServiceListModelEvent modelEvent) {
        updateKeys();
        //refreshKey(websvcListModel.getWebServiceGroup(modelEvent.getWebServiceGroupId()));
    }
    
    public void webServiceAdded(org.netbeans.modules.websvc.registry.model.WebServiceGroupEvent groupEvent) {
//        System.out.println("!in children nodes: webServiceAdded is called!!!!"+this);
        //System.out.println("parent"+this.g);
        updateKeys();
        refreshKey(getDefaultGroup());
    }
    
    public void webServiceRemoved(org.netbeans.modules.websvc.registry.model.WebServiceGroupEvent groupEvent) {
        //updateKeys();
        refreshKey(getDefaultGroup());
    }
    
}
