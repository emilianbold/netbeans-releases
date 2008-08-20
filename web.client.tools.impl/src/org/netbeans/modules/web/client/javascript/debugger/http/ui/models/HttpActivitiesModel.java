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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.Action;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpActivity;
import org.netbeans.modules.web.client.javascript.debugger.http.ui.HttpMonitorPreferences;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessage;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessageEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessageEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpProgress;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpRequest;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpResponse;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent.TreeChanged;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author joelle
 */
public class HttpActivitiesModel implements TreeModel, TableModel, NodeModel, NodeActionsProvider {

    private final List<ModelListener> listeners;
    public final static String METHOD_COLUMN = "METHOD_COLUMN";
    public final static String SENT_COLUMN = "SENT_COLUMN";
    public final static String RESPONSE_COLUMN = "RESPONSE_COLUMN";
    private static final String HTTP_RESPONSE =
            "org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/GreenArrow"; // NOI18N

    private static final HttpMonitorPreferences httpMonitorPreferences = HttpMonitorPreferences.getInstance();
    private NbJSDebugger debugger;
    private final JSHttpMessageEventListener httpMessageEventListener = new JSHttpMesageEventListenerImpl();
    private final PreferenceChangeListenerImpl preferenceChangeListener = new PreferenceChangeListenerImpl();

    private  final static Logger LOG = Logger.getLogger(HttpActivitiesModel.class.getName());
    
    public HttpActivitiesModel(NbJSDebugger debugger) {
        this.listeners = new CopyOnWriteArrayList<ModelListener>();
        this.debugger = debugger;
        debugger.addJSHttpMessageEventListener(
                WeakListeners.create( JSHttpMessageEventListener.class, httpMessageEventListener, debugger));
        httpMonitorPreferences.addPreferenceChangeListener(
                WeakListeners.create( PreferenceChangeListener.class,   preferenceChangeListener, httpMonitorPreferences));
    }

    private final Map<String, HttpActivity> id2ActivityMap = new HashMap<String, HttpActivity>();
    private class JSHttpMesageEventListenerImpl implements JSHttpMessageEventListener {

        public void onHttpMessageEvent(JSHttpMessageEvent jsHttpMessageEvent) {
            JSHttpMessage message = jsHttpMessageEvent.getHttpMessage();
            assert message != null;

            if (message instanceof JSHttpRequest) {
                JSHttpRequest req = (JSHttpRequest) message;
                HttpActivity activity = new HttpActivity(req);
                synchronized (lock){
                    if ( req.isLoadTriggeredByUser() ) {
                        activityList.clear();
                        id2ActivityMap.clear();;
                    }
                    id2ActivityMap.put(message.getId(), activity);
                    activityList.add(activity);
                }
            } else {
                HttpActivity activity = id2ActivityMap.get(message.getId());
                if ( activity == null ){
                        LOG.warning("Activity should not be null for response:" + message);
                        return;
                }
                if (message instanceof JSHttpResponse) {
                    activity.setResponse((JSHttpResponse) message);
                } else if (message instanceof JSHttpProgress) {
                    activity.updateProgress((JSHttpProgress) message);
                }

            }
            fireModelChange();
        }
    }
    final List<HttpActivity> activityList = Collections.synchronizedList(new LinkedList<HttpActivity>());
    private List<HttpActivity> filteredActivites;
    public List<HttpActivity> getHttpActivities() {
         synchronized ( lock ) {
            if ( filteredActivites == null ) {
                filteredActivites = filterActivities(Collections.unmodifiableList(activityList));
            }
            return Collections.unmodifiableList(filteredActivites);
        }
    }


    private final Object lock = new Object();
    private final List<HttpActivity> filterActivities(List<HttpActivity> activities){
        List<HttpActivity> filterList = new LinkedList<HttpActivity>();

        if ( httpMonitorPreferences.isShowAll() ) {
            filterList = activities;
        } else {
            for( HttpActivity activity : activities ){
                String category = activity.getCategory();
                if ( category != null && !filterOutCategory(category)){
                    filterList.add(activity);
                }
            }
        }
        return filterList;
    }

    public void clearActivities() {
        synchronized (lock){
            activityList.clear();
            id2ActivityMap.clear();
            filteredActivites = null;
        }
        fireModelChange();
    }

    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return getHttpActivities();
        }
        if (node instanceof HttpActivity) {
            HttpActivity activity = (HttpActivity) node;

            if (METHOD_COLUMN.equals(columnID)) {
                return activity.getMethod();
            } else if (SENT_COLUMN.equals(columnID)) {
                Date startTime = activity.getStartTime();
                return startTime != null ? startTime.toString() : "";
            } else if (RESPONSE_COLUMN.equals(columnID)) {
                Date endTime = activity.getEndTime();
                return endTime != null ? endTime.toString() : "";
            }
            throw new UnknownTypeException("Column type not recognized: " + columnID);

        }
        throw new UnknownTypeException("Type not recognized:" + node);
    }


    public static final List<String> HTML_CONTENT_TYPES = Arrays.asList("text/plain", "application/octet-stream", "text/html", "text/xml" );
    public static final List<String> JS_CONTENT_TYPES = Arrays.asList("application/x-javascript", "text/javascript", "application/javascript");
    public static final List<String> CSS_CONTENT_TYPES = Arrays.asList("text/css");
    public static final List<String> IMAGES_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif", "image/png", "image/bmp");
    public static final List<String> FLASH_CONTENT_TYPES = Arrays.asList("application/x-shockwave-flash");
    
    private static List<String> editorMimeType  = new ArrayList<String>();
    static {
        editorMimeType.addAll(HTML_CONTENT_TYPES);
        editorMimeType.addAll(JS_CONTENT_TYPES);
        editorMimeType.addAll(CSS_CONTENT_TYPES);
    }
    public static final List getEditorMimeTypes () {
        return editorMimeType;
    }

    private static final String HTML_CATEGORY = "html";
    private static final String JS_CATEGORY = "js";
    private static final String CSS_CATEGORY = "css";
    private static final String IMAGE_CATEGORY = "image";
    private static final String FLASH_CATEGORY ="flash";
    private static final String XHR_CATEGORY = "xhr";
    private static final String BIN_CATEGORY = "bin";
    private static final String TEXT_CATEGORY = "text";

    private boolean filterOutCategory(String category){
        if ( !httpMonitorPreferences.isShowHTML() && HTML_CATEGORY.equals(category)){
                return true;
        } else if ( !httpMonitorPreferences.isShowJS() && JS_CATEGORY.equals(category) ){
                return true;
        } else if ( !httpMonitorPreferences.isShowCSS() && CSS_CATEGORY.equals(category)){
                return true;
        } else if ( !httpMonitorPreferences.isShowImages() && IMAGE_CATEGORY.equals(category)){
                return true;
        } else if ( !httpMonitorPreferences.isShowFlash() && FLASH_CATEGORY.equals(category)){
                return true;
        } else if ( !httpMonitorPreferences.isShowXHR() && XHR_CATEGORY.equals(category)){
                return true;
        }
        return false;
    }
    
//    private boolean filterOutContentType(String contentType){
//        if ( !httpMonitorPreferences.isShowHTML() && HTML_CONTENT_TYPES.contains(contentType) ){
//                return true;
//        } else if ( !httpMonitorPreferences.isShowJS() && JS_CONTENT_TYPES.contains(contentType) ){
//                return true;
//        } else if ( !httpMonitorPreferences.isShowCSS() && CSS_CONTENT_TYPES.contains(contentType)){
//                return true;
//        } else if ( !httpMonitorPreferences.isShowImages() && IMAGES_CONTENT_TYPES.contains(contentType)){
//                return true;
//        } else if ( !httpMonitorPreferences.isShowFlash() && FLASH_CONTENT_TYPES.contains(contentType)){
//                return true;
//        }
//        return false;
//    }


    public Object[] getChildren(Object parent, int from, int to) {
        if (ROOT.equals(parent)) {
            return getHttpActivities().toArray();
        }
        return new Object[0];
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return getHttpActivities().size(); //Hmm, I don't want to refilter but not sure if this is safe.
        }
        return 0;
    }

    public Object getRoot() {
        return ROOT;
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return false;
        }
        return true;
    }

    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }

    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    private void fireModelChange() {
        for (ModelListener l : listeners) {
            l.modelChanged(new TreeChanged(this));
        }
        synchronized ( lock ) {
            filteredActivites = null;
        }
    }

    public String getDisplayName(Object node) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return NbBundle.getMessage(HttpActivitiesModel.class, "URL_COLUMN");
        }
        if (node instanceof HttpActivity) {
            HttpActivity activity = ((HttpActivity) node);
            return activity.toString();
//            String displayName = activity.getRequest().toString();  //url
//            return displayName;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        if (ROOT.equals(node)) {
            return null;
        } else {
            return HTTP_RESPONSE;
        }
    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof HttpActivity) {
            HttpActivity activity = ((HttpActivity) node);
            String displayName = activity.getRequest().toString();
            return displayName;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public void performDefaultAction(Object node) throws UnknownTypeException {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action[] getActions(Object node) throws UnknownTypeException {
        return new Action[]{};
    }
    
    private static final MethodColumn methodColumn = new MethodColumn();
    private static final SentColumn sentColumn = new SentColumn();
    private static final ResponseColumn resColumn = new ResponseColumn();

    public static ColumnModel getColumnModel(String columnID) {
        if (METHOD_COLUMN.equals(columnID)) {
            return methodColumn;
        } else if (SENT_COLUMN.equals(columnID)) {
            return sentColumn;
        } else if (RESPONSE_COLUMN.equals(columnID)) {
            return resColumn;
        }
        return null;
    }

    private static final class MethodColumn extends AbstractColumnModel {        

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

        private static final HttpMonitorPreferences httpMonitorPreferences = HttpMonitorPreferences.getInstance();
        @Override

        public int getColumnWidth () {
            return properties.getInt (getID () + ".columnWidth", HttpMonitorPreferences.DEFAULT_METHOD_COLUMN_WIDTH);
        }

    }

    private static final class SentColumn extends AbstractColumnModel {

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

    private static final class ResponseColumn extends AbstractColumnModel {

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

    private class PreferenceChangeListenerImpl implements PreferenceChangeListener {

            public void preferenceChange(PreferenceChangeEvent evt) {
                if ( HttpMonitorPreferences.isPreference(evt.getKey())) {
                    fireModelChange();
                }
            }
        }
}
