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
package org.netbeans.modules.wag.manager.zembly;

import com.zembly.gateway.client.Zembly;
import com.zembly.oauth.api.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.modules.wag.manager.model.WagService;
import org.netbeans.modules.wag.manager.model.WagServiceParameter;

/**
 *
 * @author peterliu
 */
public class UserServiceRetriever {

    private static final String GET_USER_OWNED_ITEMS_URI = "platform.user.GetUserOwnedItems"; // NOI18N
    private static final String GET_ITEM_INFO_URI = "platform/repository/GetItemInfo;exec";
    private static final String ITEM_TYPE_PARAM = "itemType";     //NOI18N
    private static final String ITEM_TYPE_VALUE = "[\"SERVICE\"]";    //NOI18N
    private static final String SHOW_DRAFTS_PARAM = "showDrafts";   //NOI18N
    private static final String SHOW_DRAFTS_VALUE = "true";        //NOI18N
    private static final String USERID_PARAM = "userid";        //NOI18N
    private static final String NAME_ATTR = "name";     //NOI18N
    private static final String PATH_ATTR = "path";     //NOI18N
    private static final String ITEMS_ATTR = "items";   //NOI18N
    private static final String ITEM_URI_PARAM = "itemURI";
    private static final String VERSION_PARAM = "version";
    private static final String UUID_ATTR = "uuid";
    private static final String PARAMETERS_ATTR = "parameters";
    private static final String TYPE_ATTR = "type";
    private static final String URL_ATTR = "url";
 
    private Zembly zembly;

    public UserServiceRetriever(Zembly zembly) {
        this.zembly = zembly;
    }

    public Collection<WagService> getYourServices() {
        try {
            String userid = ZemblySession.getInstance().getUserInfo().getUserid();
            String username = ZemblySession.getInstance().getUserInfo().getUsername();

            String result = zembly.callService(GET_USER_OWNED_ITEMS_URI,
                    new String[][]{
                        {USERID_PARAM, userid},
                        {ITEM_TYPE_PARAM, ITEM_TYPE_VALUE},
                        {SHOW_DRAFTS_PARAM, SHOW_DRAFTS_VALUE}
                    });
            System.out.println("your domains: " + result);

            return parseYourServices(result, username);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }

    private Collection<WagService> parseYourServices(String data, String username) {
        try {
            Collection<WagService> services = new ArrayList<WagService>();
            Collection<Parameter> params = new ArrayList<Parameter>();

            JSONTokener parser = new JSONTokener(data);
            JSONObject obj = (JSONObject) parser.nextValue();
            JSONArray items = (JSONArray) obj.getJSONArray(ITEMS_ATTR);

           for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                WagService svc = new WagService();
                services.add(svc);
                String name = item.getString(NAME_ATTR);
                svc.setDisplayName(name);
                svc.setCallableName(username + "." + name);
                String uri = item.getString(PATH_ATTR);
                svc.setUuid(item.getString(UUID_ATTR));

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

                System.out.println("service: " + svc);
            }


            return services;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }
}
