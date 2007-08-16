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
package org.netbeans.modules.websvc.manager.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A model to keep track of web service group
 * Group Nodes are created using this model.
 * Default group does not a corresponding node
 * Its webservices are displayed directly under
 * Web Service root Node.
 * Group node has only the webservice Id List
 * @author Winston Prakash
 */

public class WebServiceGroup {
    
    Set listeners = new HashSet();
    String groupId = null;
    String groupName = null;
    
    Set<String> webserviceIds = new HashSet<String>();
    
    public WebServiceGroup() {
        this(WebServiceListModel.getInstance().getUniqueWebServiceGroupId());
    }
    
    public WebServiceGroup(String id) {
        setId(id);
    }
    
    public void addWebServiceGroupListener(WebServiceGroupListener listener){
        listeners.add(listener);
    }
    
    public void removeWebServiceGroupListener(WebServiceGroupListener listener){
        listeners.remove(listener);
    }
    
    public void setId(String id){
        groupId = id;
    }
    
    public String getId(){
        return groupId;
    }
    
    public String getName() {
        return groupName;
    }
    
    public void setName(String name) {
        modelDirty();
        groupName = name;
    }
    
    public void add(String webServiceId) {
        add(webServiceId, false);
    }
    
    public void remove(String webServiceId) {
        remove(webServiceId, false);
    }
    
    public void add(String webServiceId, boolean quietly) {
        if (!webserviceIds.contains(webServiceId)) {
            WebServiceData wsData = WebServiceListModel.getInstance().getWebService(webServiceId);
            wsData.setGroupId(getId());
            webserviceIds.add(webServiceId);
            
            if (quietly) return;
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId, getId());
                ((WebServiceGroupListener)iter.next()).webServiceAdded(evt);
            }
        }else if (!quietly) {
            // This is a hack to make the nodes to appear while restoring 
            // the W/S meta data at IDE start (lag due to WSDL parsing)
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId, getId());
                ((WebServiceGroupListener)iter.next()).webServiceAdded(evt);
            }
        }
    }
    
    public void remove(String webServiceId, boolean quietly){
        //System.out.println("WebServiceGroup remove called - " + webServiceId);
        if (webserviceIds.contains(webServiceId)) {
            webserviceIds.remove(webServiceId);
            if (quietly) return;
            
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId, getId());
                ((WebServiceGroupListener)iter.next()).webServiceRemoved(evt);
            }
        }
    }
    
    public void modify(String webServiceId) {
        // It is here solely to notify the listners
        Iterator iter = listeners.iterator();
        while(iter.hasNext()) {
            WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId, getId());
            ((WebServiceGroupListener)iter.next()).webServiceRemoved(evt);
        }
    }
    
    public void setWebServiceIds(Set ids){
        webserviceIds = ids;
        Iterator iter = webserviceIds.iterator();
        while(iter.hasNext()) {
            WebServiceData wsData = WebServiceListModel.getInstance().getWebService((String)iter.next());
            wsData.setGroupId(getId());
        }
    }
    
    public Set<String> getWebServiceIds(){
        return webserviceIds;
    }
    /**
     * Partial Fix for Bug: 5107518
     * Changed so the web services will only be persisted if there is a change.
     * - David Botterill 9/30/2004
     */
    private void modelDirty() {
        WebServiceListModel.setDirty(true);
    }
    
    @Override
    public boolean equals(Object o) {
        try {
            WebServiceGroup g2 = (WebServiceGroup)o;
            return g2.getId().equals(getId());
        }catch (Exception ex) {
            return false;
        }
    }
}
