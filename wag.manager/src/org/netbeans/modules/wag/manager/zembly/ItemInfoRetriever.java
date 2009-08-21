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
public class ItemInfoRetriever {

    private static final String GET_ITEM_INFO_URI = "platform/repository/GetItemInfo;exec";
    private static final String NAME_ATTR = "name";     //NOI18N
    private static final String PATH_ATTR = "path";     //NOI18N
    private static final String ITEM_URI_PARAM = "itemURI"; //NOI18N
    private static final String VERSION_PARAM = "version";  //NOI18N
    private static final String UUID_ATTR = "uuid";         //NOI18N
    private static final String PARAMETERS_ATTR = "parameters"; //NOI18N
    private static final String TYPE_ATTR = "type";         //NOI18N
    private static final String URL_ATTR = "url";           //NOI18N
    private static final String MIME_TYPE_ATTR = "mimeType";    //NOI18N
    private static final String WADL_CONTENT_TYPE = "application/vnd.sun.wadl+xml";     //NOI18N
    private static final String LATEST = "latest";      //NO118N

    private Zembly zembly;

    public ItemInfoRetriever(Zembly zembly) {
        this.zembly = zembly;
    }

    public Collection<WagService> getServices(JSONArray items) {
        
        try {
            Collection<WagService> services = new ArrayList<WagService>();
            Collection<Parameter> params = new ArrayList<Parameter>();

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
         
                params.clear();
                params.add(Parameter.create(ITEM_URI_PARAM, item.getString(PATH_ATTR)));
                params.add(Parameter.create(VERSION_PARAM, LATEST));
                String result = zembly.callService(GET_ITEM_INFO_URI, params);
                //System.out.println("result = " + result);
                JSONTokener parser = new JSONTokener(result);

                JSONObject info = (JSONObject) parser.nextValue();
                WagService svc = new WagService();
                services.add(svc);

                svc.setDisplayName(item.getString(NAME_ATTR));

                String callableName = info.getString(NAME_ATTR);

                String contentType = info.getString(MIME_TYPE_ATTR);
                if (contentType.equals(WADL_CONTENT_TYPE)) { // || contentType.equals(JAVA_SERVICE_CONTENT_TYPE)) {
                    callableName = "web." + callableName;
                }

                svc.setCallableName(callableName);
                svc.setUuid(info.getString(UUID_ATTR));
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
