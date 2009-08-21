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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.modules.wag.manager.model.WagApi;
import org.netbeans.modules.wag.manager.model.WagService;

/**
 *
 * @author peterliu
 */
public class ContentRetriever {
    private static final String LIST_CONTENTS_URI = "platform.repository.ListContents"; //NOI18N
    private static final String ITEM_URI_PARAM = "itemURI";     //NOI18N
    private static final String APIS_ATTR = "apis";     //NOI18N
    private static final String SERVICES_ATTR = "services";     //NOI18N
    private static final String NAME_ATTR = "name";     //NOI18N
    private static final String PATH_ATTR = "path";     //NOI18N
  
    private Zembly zembly;

    public ContentRetriever(Zembly zembly) {
        this.zembly = zembly;
    }

    private String retrieveContent(String path) {
        try {
            String result = zembly.callService(LIST_CONTENTS_URI,
                    new String[][]{{ITEM_URI_PARAM, path}});

            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }

    public Collection<WagApi> getApis(String path) {
        return parseApis(retrieveContent(path));
    }

    public Collection<WagService> getServices(String path) {
        return parseServices(retrieveContent(path), path);
    }

    private Collection<WagApi> parseApis(String data) {
        try {
            JSONTokener parser = new JSONTokener(data);
            JSONObject obj = (JSONObject) parser.nextValue();
            JSONArray array = obj.optJSONArray(APIS_ATTR);
            Collection<WagApi> apis = new ArrayList<WagApi>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                WagApi api = new WagApi(item.getString(NAME_ATTR), item.getString(PATH_ATTR));
                System.out.println("api: " + api);
                apis.add(api);
            }


            return apis;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }

    private Collection<WagService> parseServices(String data, String parentPath) {
        try {
            JSONTokener parser = new JSONTokener(data);
            JSONObject obj = (JSONObject) parser.nextValue();
            JSONArray items = obj.getJSONArray(SERVICES_ATTR);

            return ZemblySession.getInstance().getItemInfoRetriever().getServices(items);
        } catch (Exception ex) {
             ex.printStackTrace();
            // TODO: need to handle exception
        }

        return Collections.emptyList();
    }
}
