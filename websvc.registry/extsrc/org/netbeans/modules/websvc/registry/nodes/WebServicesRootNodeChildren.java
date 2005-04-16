/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
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
