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

package org.netbeans.modules.websvc.registry.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.util.Util;

/**
 *
 * @author  Winston Prakash
 */
public class WebServiceGroup {
    
    Set listeners = new HashSet();
    String groupId = null;
    String groupName = null;
    
    Set webserviceIds = new HashSet();
    
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
        groupName = name;
    }
    
    public void add(String webServiceId) {
        //System.out.println("WebServiceGroup add called - " + webServiceId);
        if (!webserviceIds.contains(webServiceId)) {
            WebServiceData wsData = WebServiceListModel.getInstance().getWebService(webServiceId);
            wsData.setGroupId(getId());
            webserviceIds.add(webServiceId);
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId);
                ((WebServiceGroupListener)iter.next()).webServiceAdded(evt);
            }
        }
    }
    
    public void remove(String webServiceId){
        //System.out.println("WebServiceGroup remove called - " + webServiceId);
        if (webserviceIds.contains(webServiceId)) {
            webserviceIds.remove(webServiceId);
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceGroupEvent evt = new  WebServiceGroupEvent(webServiceId);
                ((WebServiceGroupListener)iter.next()).webServiceRemoved(evt);
            }
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
    
    public Set getWebServiceIds(){
        return webserviceIds;
    }
    
}
