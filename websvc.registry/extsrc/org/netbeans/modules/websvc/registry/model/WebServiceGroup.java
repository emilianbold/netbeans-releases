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
