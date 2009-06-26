/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wag.manager.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.modules.wag.manager.model.WagService;
import org.netbeans.modules.wag.manager.model.WagServiceParameter;
import com.zembly.oauth.api.Parameter;
import com.zembly.gateway.client.Zembly;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author peterliu
 */
public class SearchEngine {

    private static final String BASE_URL = "http://zembly.com/things/";
    private static final String HTTP_METHOD = "POST";
    private static final String SEARCH_ITEM_URI = "platform/repository/SearchItems;exec";
    private static final String GET_ITEM_INFO_URI = "platform/repository/GetItemInfo;exec";
    private static final String SEARCH_STRING_PARAM = "searchString";
    private static final String SEARCH_FOR = "searchFor";
    private static final String START_INDEX = "startIndex";
    private static final String MAX_RESULTS_PARAM = "maxResults";
    private static final String ITEM_URI_PARAM = "itemURI";
    private static final String VERSION_PARAM = "version";
    private static final String CONTENT_TYPE_ATTR = "contentType";
    private static final String ITEMS_ATTR = "items";
    private static final String PATH_ATTR = "path";
    private static final String NAME_ATTR = "name";
    private static final String URL_ATTR = "url";
    private static final String PARAMETERS_ATTR = "parameters";
    private static final String TYPE_ATTR = "type";
    private static final String WADL_CONTENT_TYPE = "application/vnd.sun.wadl+xml";
    private static final String JAVA_SERVICE_CONTENT_TYPE = "application/java";
    private static SearchEngine instance;
    private Zembly zembly;

    private SearchEngine() {
        try {
            zembly = Zembly.getInstance("org/netbeans/modules/wag/manager/resources/zcl.properties");
        } catch (Exception ex) {
            // ignore
        }
    }

    public static SearchEngine getInstance() {
        if (instance == null) {
            instance = new SearchEngine();
        }

        return instance;
    }

    public List<WagService> search(String query, int maxResults, int startIndex) {
        try {
            List<Parameter> params = new ArrayList<Parameter>();
            params.add(Parameter.create(SEARCH_STRING_PARAM, query));
            params.add(Parameter.create(MAX_RESULTS_PARAM, Integer.toString(maxResults)));
            params.add(Parameter.create(SEARCH_FOR, "SERVICE"));
            params.add(Parameter.create(START_INDEX, Integer.toString(startIndex)));
            String result = zembly.callService(SEARCH_ITEM_URI, params);

            return parse(result);
        } catch (Exception ex) {
            handleException(ex);
        }

        return Collections.emptyList();
    }

    private List<WagService> parse(String data) {
        try {
            //System.out.println("data = " + data);
            List<WagService> services = new ArrayList<WagService>();
            List<Parameter> params = new ArrayList<Parameter>();

            JSONTokener parser = new JSONTokener(data);
            JSONObject obj = (JSONObject) parser.nextValue();
            JSONArray items = obj.getJSONArray(ITEMS_ATTR);

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                WagService svc = new WagService();
                services.add(svc);
                svc.setName(item.getString(NAME_ATTR));
                String uri = item.getString(PATH_ATTR);
                svc.setPath(uri);

                String contentType = item.getString(CONTENT_TYPE_ATTR);

                if (contentType.equals(WADL_CONTENT_TYPE)) { // || contentType.equals(JAVA_SERVICE_CONTENT_TYPE)) {
                    svc.setPrependWeb(true);
                }

                params.clear();
                params.add(Parameter.create(ITEM_URI_PARAM, uri));
                params.add(Parameter.create(VERSION_PARAM, "latest"));
                String result = zembly.callService(GET_ITEM_INFO_URI, params);
                //System.out.println("result = " + result);
                parser = new JSONTokener(result);

                JSONObject info = (JSONObject) parser.nextValue();
                svc.setUrl(info.getString(URL_ATTR));
                JSONArray svcParams = info.getJSONArray(PARAMETERS_ATTR);
                List<WagServiceParameter> wagParams = new ArrayList<WagServiceParameter>();
                svc.setParameters(wagParams);
                for (int j = 0; j < svcParams.length(); j++) {
                    JSONObject p = svcParams.getJSONObject(j);
                    WagServiceParameter wp = new WagServiceParameter();
                    wp.setName(p.getString(NAME_ATTR));
                    wp.setType(p.getString(TYPE_ATTR));
                    wagParams.add(wp);
                }
            }

            return services;
        } catch (Exception ex) {
            handleException(ex);
        }

        return Collections.emptyList();
    }

    private void handleException(Exception ex) {
        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getMessage());
        DialogDisplayer.getDefault().notify(msg);
    }
}
