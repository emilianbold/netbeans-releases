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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.WebServicePersistenceManager;

/**
 * A model to keep track of web service data and their group
 * Nodes are created using this model
 * @author Winston Prakash
 */
public class WebServiceListModel {
    
    public static final String DEFAULT_GROUP = "default"; // NOI18N
    
    private static Random serviceRandom = new Random(System.currentTimeMillis());
    private static Random serviceGroupRandom = new Random(System.currentTimeMillis());
    public static boolean MODEL_DIRTY_FLAG = false;
    
    Set<WebServiceListModelListener> listeners = new HashSet<WebServiceListModelListener>();
    
    /**
     * Fix for Bug#: 5039378
     * Netbeans can potentially use multiple threads to maintain a Node's data model.
     *- David Botterill 5/6/2004
     */
    private List<WebServiceData> webServices = Collections.synchronizedList(new ArrayList<WebServiceData>());
    private List<WebServiceGroup> webServiceGroups = Collections.synchronizedList(new ArrayList<WebServiceGroup>());
    
    // To maintain the display names for the webservice/port
    private Set uniqueDisplayNames = Collections.synchronizedSet(new HashSet());
    private List<String> partnerServices = new ArrayList<String>();
    
    private static WebServiceListModel websvcNodeModel = new WebServiceListModel();

    private boolean initialized = false;
    
    private WebServiceListModel() {
    }
    
    public static WebServiceListModel getInstance() {
        return websvcNodeModel;
    }
    
    public void addWebServiceListModelListener(WebServiceListModelListener listener){
        listeners.add(listener);
    }
    
    public void removeWebServiceListModelListener(WebServiceListModelListener listener){
        listeners.remove(listener);
    }
    
    public List<String> getPartnerServices() {
        return partnerServices;
    }
    
    private boolean containsKey(List list, String key) {
        synchronized (list) {
            for (Object o : list) {
                if (o instanceof WebServiceData) {
                    WebServiceData wsData = (WebServiceData)o;
                    
                    if (wsData.getId().equals(key)) {
                        return true;
                    }
                }else if (o instanceof WebServiceGroup) {
                    WebServiceGroup wsGroup = (WebServiceGroup)o;
                    
                    if (wsGroup.getId().equals(key)) {
                        return true;
                    }                    
                }
            }
            return false;
        }
    }
    
    /** Get a unique Id for the webservice data
     *  Unique Id is "webservice" + a random number.
     */
    public String getUniqueWebServiceId(){
        initialize();
        String uniqueId = "webservice" + serviceRandom.nextLong();
        
        while(containsKey(webServices, uniqueId)) {
            uniqueId = "webservice" + serviceRandom.nextLong();
        }
        return uniqueId;
    }
    
    /** Get a unique Id for the webservice data group
     *  Unique Id is "webserviceGroup" + a random number.
     */
    public String getUniqueWebServiceGroupId(){
        initialize();
        String uniqueId = "webserviceGroup" + serviceGroupRandom.nextLong();
        while(containsKey(webServiceGroups, uniqueId)){
            uniqueId = "webserviceGroup" + serviceGroupRandom.nextLong();
        }
        return uniqueId;
    }
    
    /** Add the webservice data with a unique Id */
    public void addWebService(WebServiceData webService) {
        initialize();
        if (!webServices.contains(webService)) {
            WebServiceListModel.setDirty(true);
            webServices.add(webService);
        }
    }
    
    /** Get the webservice data based on unique Id */
    public WebServiceData getWebService(String webServiceId){
        synchronized (webServices) {
            initialize();
            for (WebServiceData wsData : webServices) {
                if (wsData.getId().equals(webServiceId)) {
                    return wsData;
                }
            }
        }
        return null;
    }
    
    /** Get the webservice data from the model */
    public void removeWebService(String webServiceId) {
        initialize();
        WebServiceData wsData = getWebService(webServiceId);
        if (wsData == null) return;
        WebServiceGroup group = getWebServiceGroup(wsData.getGroupId());
        WebServiceListModel.setDirty(true);
        if(group != null) group.remove(webServiceId);
        webServices.remove(wsData);
    }
 
    
    /** Check if the model contains the webservice data*/
    public boolean webServiceExists(WebServiceData webService){
        initialize();
        return containsKey(webServices, webService.getId());
    }
    
    /** Get a unique display name */
    public String getUniqueDisplayName( String name ) {
        initialize();
        String displayName = name;
        for(int i = 1; uniqueDisplayNames.contains(displayName); i++) {
            displayName = name + Integer.toString(i);
        }
        
        return displayName;
    }
    
    public boolean isDisplayNameUnique( String name ) {
        return !uniqueDisplayNames.contains( name );
    }
    
    /** Get all the webservice data added to this model*/
    public List<WebServiceData> getWebServiceSet() {
        initialize();
        return webServices;
    }
    
    /** Add a webservice group to the model*/
    public void addWebServiceGroup(WebServiceGroup group){
        initialize();
        if (!webServiceGroups.contains(group)) {
            WebServiceListModel.setDirty(true);
            webServiceGroups.add(group);
            
            for (WebServiceListModelListener listener : listeners) {
                WebServiceListModelEvent evt = new  WebServiceListModelEvent(group.getId());
                listener.webServiceGroupAdded(evt);
            }
        }
    }
    
    /** Remove the webservice group from the model*/
    public void removeWebServiceGroup(String groupId){
        initialize();
        WebServiceGroup group = getWebServiceGroup(groupId);
        if (group != null) {
            WebServiceListModel.setDirty(true);
            /**
             * Fix bug:
             * We need to get an array of the web services instead of using the Iterator because a
             * Set iterator is fail-safe and will throw a ConcurrentModificationException if you're using
             * it and the set is modified.
             * - David Botterill 5/6/2004.
             */
            String [] webserviceIds = (String [])getWebServiceGroup(groupId).getWebServiceIds().toArray(new String[0]);
            for(int ii=0; null != webserviceIds && ii < webserviceIds.length; ii++) {
                WebServiceManager.getInstance().removeWebService(getWebService(webserviceIds[ii]));
            }
            webServiceGroups.remove(group);
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceListModelEvent evt = new  WebServiceListModelEvent(groupId);
                ((WebServiceListModelListener)iter.next()).webServiceGroupRemoved(evt);
            }
        }
    }
    
    /** Get a webservice group by its Id*/
    public WebServiceGroup getWebServiceGroup(String groupId) {
        synchronized (webServiceGroups) {
            initialize();
            for (WebServiceGroup wsGroup : webServiceGroups) {
                if (wsGroup.getId().equals(groupId)) {
                    return wsGroup;
                }
            }
        }
        return null;
    }
    
    public List<WebServiceGroup> getWebServiceGroupSet() {
        initialize();
        return webServiceGroups;
    }
    
    public static void setDirty(boolean inDirtyFlag) {
        WebServiceListModel.MODEL_DIRTY_FLAG = inDirtyFlag;
    }
    
    public static boolean isDirty() {
        return WebServiceListModel.MODEL_DIRTY_FLAG;
    }
    
    private synchronized void initialize() {
        if (!initialized) {
            initialized = true;
            WebServicePersistenceManager manager = new WebServicePersistenceManager();
            manager.load();
            
            for (WebServiceGroup wsGroup : webServiceGroups) {
                if (wsGroup.getId().equals(DEFAULT_GROUP)) {
                    for (WebServiceGroupListener defaultGroupListener : defaultGroupListeners) {
                        wsGroup.addWebServiceGroupListener(defaultGroupListener);
                    }
                    defaultGroupListeners = null;
                    return;
                }
            }
            
            // Generate the default group on initialization if it doesn't exist
            WebServiceGroup defaultGroup = new WebServiceGroup(WebServiceListModel.DEFAULT_GROUP);
            webServiceGroups.add(defaultGroup);
            for (WebServiceGroupListener defaultGroupListener : defaultGroupListeners) {
                defaultGroup.addWebServiceGroupListener(defaultGroupListener);
            }
            defaultGroupListeners = null;
        }
    }
    
    private List<WebServiceGroupListener> defaultGroupListeners = new ArrayList<WebServiceGroupListener>();
    
    public void addDefaultGroupListener(WebServiceGroupListener listener) {
        synchronized (webServiceGroups) {
            for (WebServiceGroup wsGroup : webServiceGroups) {
                if (wsGroup.getId().equals(DEFAULT_GROUP)) {
                    wsGroup.addWebServiceGroupListener(listener);
                    return;
                }
            }
            
            if (!defaultGroupListeners.contains(listener)) {
                defaultGroupListeners.add(listener);
            }
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
}
