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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
