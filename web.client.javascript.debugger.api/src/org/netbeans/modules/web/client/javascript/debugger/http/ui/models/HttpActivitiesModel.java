/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.http.ui.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Action;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpActivity;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpResponse;
import org.netbeans.modules.web.client.javascript.debugger.models.AbstractColumnModel;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSCallStackModel;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public class HttpActivitiesModel implements TreeModel, TableModel, NodeModel, NodeActionsProvider {
    
    private final List<ModelListener> listeners;
    public final static String METHOD_COLUMN = "METHOD_COLUMN";
    public final static String SENT_COLUMN = "SENT_COLUMN";
    public final static String RESPONSE_COLUMN = "RESPONSE_COLUMN";
    

    private static final String HTTP_RESPONSE=
            "org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/GreenArrow"; // NOI18N
//    private final static String ROOT = "Root";

    public HttpActivitiesModel() {
        listeners = new CopyOnWriteArrayList<ModelListener>();
    }
    
    public List<HttpActivity> getHttpActivities() {
        List<HttpActivity> activityList = new ArrayList<HttpActivity>();
        activityList.add(HttpActivity.createDummyActivity());
        activityList.add(HttpActivity.createDummyActivity1());
        activityList.add(HttpActivity.createDummyActivity());
        return activityList;
    }

    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if ( ROOT.equals(node)){
            return getHttpActivities();
        }
        if( node instanceof HttpActivity ){
            HttpActivity activity = (HttpActivity)node;
            
            if ( METHOD_COLUMN.equals(columnID)){
                return activity.getRequest().getMethod();
            } else if ( SENT_COLUMN.equals(columnID) ) {
                return activity.getRequest().getSentDate();
            } else if ( RESPONSE_COLUMN.equals(columnID) ){
                HttpResponse response = activity.getResponse();
                if( response != null ){
                    return response.getResponseDate();
                } 
                return "";
            }
            throw new UnknownTypeException("Column type not recognized: " + columnID);
                
        }
        throw new UnknownTypeException("Type not recognized:" + node);
    }

    public Object[] getChildren(Object parent, int from, int to) {
        if( ROOT.equals(parent) ){
            return getHttpActivities().toArray();
        }
        return new Object[0];
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if( ROOT.equals(node)){
            return getHttpActivities().size();
        }
        return 0;
    }

    public Object getRoot() {
        return ROOT;
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if( ROOT.equals(node)){
            return false;
        }
        return true;
    }
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }

    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    

    public String getDisplayName(Object node) throws UnknownTypeException {
        if ( ROOT.equals(node)){
            return NbBundle.getMessage(HttpActivitiesModel.class, "URL_COLUMN");
        }
        if (node instanceof HttpActivity) {
            HttpActivity activity = ((HttpActivity) node);
            String displayName = activity.getRequest().getUrl().toString();
            return displayName;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        if( ROOT.equals(node)){
            return null;
        } else {
            return HTTP_RESPONSE;
        }
    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof HttpActivity) {
            HttpActivity activity = ((HttpActivity) node);
            String displayName = activity.getRequest().getUrl().toString();
            return displayName;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public void performDefaultAction(Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action[] getActions(Object node) throws UnknownTypeException {
        return new Action[]{};
    }
    
    private static final MethodColumn methodColumn = new MethodColumn();
    private static final SentColumn sentColumn = new SentColumn();
    private static final ResponseColumn resColumn = new ResponseColumn();
    
    public static ColumnModel getColumnModel(String columnID){
        if( METHOD_COLUMN.equals(columnID)){
            return methodColumn;
        } else if ( SENT_COLUMN.equals(columnID)){
            return sentColumn;
        } else if ( RESPONSE_COLUMN.equals(columnID)){
            return resColumn;
        }
        return null;
    }
    
    private static final class MethodColumn extends ColumnModel {

        @Override
        public String getID() {
            return METHOD_COLUMN;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HttpActivitiesModel.class, METHOD_COLUMN);
        }

        @Override
        public Class getType() {
            return String.class;
        }
        
    }
    
    private static final class SentColumn extends ColumnModel {

        @Override
        public String getID() {
            return SENT_COLUMN;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HttpActivitiesModel.class, SENT_COLUMN);
        }

        @Override
        public Class getType() {
            return String.class;
        }
        
    }
    
    private static final class ResponseColumn extends ColumnModel {

        @Override
        public String getID() {
            return RESPONSE_COLUMN;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HttpActivitiesModel.class, RESPONSE_COLUMN);
        }

        @Override
        public Class getType() {
            return String.class;
        }
        
    }


}
