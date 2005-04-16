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

import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceGroupListener;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.model.WebServiceGroupEvent;

import java.util.*;

import org.openide.nodes.*;
import org.netbeans.modules.websvc.registry.util.*;

/** List of children of a webservices group node.
 *
 * @author Winston Prakash
 */
public class WebServiceGroupNodeChildren extends Children.Keys implements WebServiceGroupListener{
    
    WebServiceGroup webserviceGroup;
    WebServiceListModel websvcListModel = WebServiceListModel.getInstance();
    
    public WebServiceGroupNodeChildren(WebServiceGroup websvcGroup) {
        webserviceGroup = websvcGroup;
        webserviceGroup.addWebServiceGroupListener(this);
    }
    
    protected void addNotify() {
        super.addNotify();
        updateKeys();
    }
    
    private void updateKeys() {
        setKeys(webserviceGroup.getWebServiceIds());
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        Node node = null;
        if (key instanceof String) {
            node = new WebServicesNode(websvcListModel.getWebService((String)key));
            return new Node[]{node};
        }
        return new Node[]{node};
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
