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

package org.netbeans.modules.websvc.registry.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;

import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.websvc.registry.wsdl.*;
////import org.netbeans.modules.websvc.registry.NotFoundException;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Date;
import org.netbeans.modules.websvc.registry.WebServicePersistenceManager;
/**
 * A model to keep track of web service data and their group
 * Nodes are created using this model
 * @author Winston Prakash
 */
public class WebServiceListModel {

    private static Random serviceRandom = new Random(System.currentTimeMillis());
    private static Random serviceGroupRandom = new Random(System.currentTimeMillis());

    Set listeners = new HashSet();
    /**
     * Fix for Bug#: 5039378
     * Netbeans can potentially use multiple threads to maintain a Node's data model.
     *- David Botterill 5/6/2004
     */
    private Map webservices = Collections.synchronizedMap(new HashMap());
    private Map webserviceGroups = Collections.synchronizedMap(new HashMap());

    private static int websvcCounter=0;
    private static int groupCounter=0;

    public static String MODEL_SERVICE_ADDED = "ServiceAdded";
    public static String MODEL_SERVICE_REMOVED = "ServiceRemoved";

    private static String EVENT_TYPE_ADD = "ADD";
    private static String EVENT_TYPE_REMOVE = "REMOVE";

    private static WebServiceListModel websvcNodeModel = new WebServiceListModel();
    private static boolean initialized=false;
    


    private WebServiceListModel() {
    }

    public  static WebServiceListModel getInstance() {
        if (initialized==false){
            initialized=true;//avoid conflicts in threads.
            WebServicePersistenceManager pm = new WebServicePersistenceManager();
            pm.load(pm.getClass().getClassLoader());
        }
        return websvcNodeModel;
    }


    public void addWebServiceListModelListener(WebServiceListModelListener listener){
        listeners.add(listener);
    }

    public void removeWebServiceListModelListener(WebServiceListModelListener listener){
        listeners.remove(listener);
    }
    /** Get a unique Id for the webservice data
     *  Unique Id is "webservice" + a random number.
     */
    public String getUniqueWebServiceId(){
        String uniqueId = "webservice" + serviceRandom.nextLong();
        while(webservices.containsKey(uniqueId)){
            uniqueId = "webservice" + serviceRandom.nextLong();
        }
        return uniqueId;
    }

    /** Get a unique Id for the webservice data group
     *  Unique Id is "webserviceGroup" + a random number.
     */
    public String getUniqueWebServiceGroupId(){
        String uniqueId = "webserviceGroup" + serviceGroupRandom.nextLong();
        while(webserviceGroups.containsKey(uniqueId)){
            uniqueId = "webserviceGroup" + serviceGroupRandom.nextLong();
        }
        return uniqueId;
    }


    /** Add the webservice data with a unique Id */
    public void addWebService(WebServiceData webService) {
        //System.out.println("WebServiceNodeModel Webservice add called - " + webService.getId());
        if (webService != null && !webservices.containsKey(webService.getId())) {
            webservices.put(webService.getId(), webService);
            fireServiceAdded(webService);
        }
    }

    /** Get the webservice data based on unique Id */
    public WebServiceData getWebService(String webServiceId){
        return (WebServiceData) webservices.get(webServiceId);
    }

    /** Get the webservice data from the model */
    public void removeWebService(String webServiceId) {
        //System.out.println("WebServiceNodeModel Webservice remove called - " + webServiceId);
        WebServiceData wsData = (WebServiceData) webservices.get(webServiceId);
        if(wsData != null) {
            WebServiceGroup group = getWebServiceGroup(getWebService(webServiceId).getGroupId());
            if(group != null) {
                group.remove(webServiceId);
            }
            webservices.remove(webServiceId);
            fireServiceRemoved(wsData);
        }
    }

    public void removeWebService(WebServiceData ws) {
        // !PW Note the web service passed in is not necessarily the same data object
        // as the service to be removed.  It is merely a key to find a match in the list model
        Iterator iter = webservices.keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next().toString();
            WebServiceData wsData = (WebServiceData) webservices.get(key);
            if(wsData.equals(ws)) {
                WebServiceGroup group = getWebServiceGroup(wsData.getGroupId());
                if(group != null) {
                    group.remove(key);
                }
                webservices.remove(key);
                fireServiceRemoved(wsData);
                break;
            }
        }
    }

    /** Check if the model contains the webservice data*/
    public boolean webServiceExists(WebServiceData webService){
        Iterator iter = webservices.keySet().iterator();
        while(iter.hasNext()){
            WebServiceData wsData = (WebServiceData) webservices.get(iter.next());
            if(wsData.equals(webService)) {
                return true;
            }
        }
        return false;
    }
    
    public WebServiceData findService(WebServiceData webService) {
        Iterator iter = webservices.keySet().iterator();
        while(iter.hasNext()){
            WebServiceData wsData = (WebServiceData) webservices.get(iter.next());
            if(wsData.equals(webService)) {
                return wsData;
            }
        }
        return null;
    }

    /** Check if the model contains this service by name */
    public boolean webServiceExists(String serviceName) {
        // Create key from serviceName in form of WebServiceData
        WebServiceData wsKey = new WebServiceData();
        wsKey.setDisplayName(serviceName);

        Iterator iter = webservices.keySet().iterator();
        while(iter.hasNext()){
            WebServiceData wsData = (WebServiceData) webservices.get(iter.next());
            if(wsData.equals(wsKey)) {
                return true;
            }
        }

        return false;
    }

    /** Get all the webservice data added to this model*/
    public Set getWebServiceSet() {
        Set websvcs = new HashSet();
        Iterator iter = webservices.keySet().iterator();
        while(iter.hasNext()){
            WebServiceData wsData = (WebServiceData) webservices.get(iter.next());
            websvcs.add(wsData);
        }
        return websvcs;
    }

    /** Add a webservice group to the model*/
    public void addWebServiceGroup(WebServiceGroup group){
        //System.out.println("WebServiceNodeModel add group called - " + group.getId());
        if(webserviceGroups == null || group == null) return;
        if (!webserviceGroups.containsKey(group.getId())) {
            webserviceGroups.put(group.getId(), group);
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceListModelEvent evt = new  WebServiceListModelEvent(group.getId());
                ((WebServiceListModelListener)iter.next()).webServiceGroupAdded(evt);
            }
        }
    }

    /** Remove the webservice group from the model*/
    public void removeWebServiceGroup(String groupId){
        //System.out.println("WebServiceNodeModel remove group called - " + groupId);
        if (webserviceGroups.containsKey(groupId)) {
            /**
             * Fix bug: 
             * We need to get an array of the web services instead of using the Iterator because a 
             * Set iterator is fail-safe and will throw a ConcurrentModificationException if you're using
             * it and the set is modified.
             * - David Botterill 5/6/2004.
             */
            String [] webserviceIds = (String [])getWebServiceGroup(groupId).getWebServiceIds().toArray(new String[0]);
           for(int ii=0; null != webserviceIds && ii < webserviceIds.length; ii++) {
                removeWebService(webserviceIds[ii]);
            }
            webserviceGroups.remove(groupId);
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                WebServiceListModelEvent evt = new  WebServiceListModelEvent(groupId);
                ((WebServiceListModelListener)iter.next()).webServiceGroupRemoved(evt);
            }
        }
    }

    /** Get a webservice group by its Id*/
    public WebServiceGroup getWebServiceGroup(String groupId){
        WebServiceGroup group = (WebServiceGroup) webserviceGroups.get(groupId);
        // !PW IZ 57542 - deferred initialization changes have made default group initialization
        // difficult so move it here.  The default group should always exist.  The only time
        // it does not is generally when the user is running the IDE for the first time with
        // a new user directory.
        if(group == null && "default".equals(groupId)) {
            synchronized (this) {
                group = (WebServiceGroup) webserviceGroups.get(groupId);
                if(group == null) {
                    group = new WebServiceGroup("default");
                    addWebServiceGroup(group);
                }
            }
        }
        return group;
    }

    public Set getWebServiceGroupSet() {
        Set wsGroups = new HashSet();
        Iterator iter = webserviceGroups.keySet().iterator();
        while(iter.hasNext()){
            WebServiceGroup group = (WebServiceGroup) webserviceGroups.get(iter.next());
            wsGroups.add(group);
        }
        return wsGroups;
    }
    
    public WebServiceGroup findWebServiceGroup(String groupName) {
        Iterator iter = webserviceGroups.keySet().iterator();
        while(iter.hasNext()) {
            WebServiceGroup group = (WebServiceGroup) webserviceGroups.get(iter.next());
            if (groupName.equals(group.getName()))
                return group;
        }
        return null;
    }

    /** Using property change object to signify service adds and removes.
     *  !PW FIXME Should flesh out the ListModel events and move them to the API
     *  in lieu of this code.  EA2 item.  
     *  Note also, should provide ability for listener to only listen for adds or
     *  removes of a particular name before sending out event.  Otherwise, this
     *  won't scale.
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void fireServiceAdded(final WebServiceData wsData) {
        // Make sure these events are fired on the Swing event thread in case the
        // respondants update UI (which they likely will because this event is
        // intended for node updates.)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                propertyChangeSupport.firePropertyChange(MODEL_SERVICE_ADDED, null, wsData);
            }
        });
    }

    private void fireServiceRemoved(final WebServiceData wsData) {
        // Make sure these events are fired on the Swing event thread in case the
        // respondants update UI (which they likely will because this event is
        // intended for node updates.)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                propertyChangeSupport.firePropertyChange(MODEL_SERVICE_REMOVED, wsData, null);
            }
        });
    }

//    public static class WebServiceEvent {
//        private final WebServiceData webServiceData;
//
//        private WebServiceEvent(final WebServiceData wsData) {
//            this.webServiceData = wsData;
//        }
//
//        public WebServiceData getWebServiceData() {
//            return webServiceData;
//        }
//    };
}
