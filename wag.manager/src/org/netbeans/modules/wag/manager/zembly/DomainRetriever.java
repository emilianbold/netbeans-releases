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
import org.netbeans.modules.wag.manager.model.WagDomain;

/**
 *
 * @author peterliu
 */
public class DomainRetriever {

    private static final String LIST_DOMAIN_URI = "platform.api.ListDomain";  //NOI18N
    private static final String GET_USER_OWNED_ITEMS_URI = "platform.user.GetUserOwnedItems"; // NOI18N
    private static final String ITEM_TYPE_PARAM = "itemType";     //NOI18N
    private static final String ITEM_TYPE_VALUE = "[\"DOMAIN\"]";    //NOI18N
    private static final String SHOW_DRAFTS_PARAM = "showDrafts";   //NOI18N
    private static final String SHOW_DRAFTS_VALUE = "false";        //NOI18N
    private static final String USERID_PARAM = "userid";        //NOI18N
    private static final String NAME_ATTR = "name";     //NOI18N
    private static final String PATH_ATTR = "path";     //NOI18N
    private static final String ITEMS_ATTR = "items";   //NOI18N
    private Zembly zembly;

    public DomainRetriever(Zembly zembly) {
        this.zembly = zembly;
    }

    public Collection<WagDomain> getAllDomains() {

        try {
            String result = zembly.callService(LIST_DOMAIN_URI, new String[][]{});
            System.out.println("all domains: " + result);
            return parseAllDomains(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }

    public Collection<WagDomain> getYourDomains() {
        try {
            String userid = ZemblySession.getInstance().getUserInfo().getUserid();
            String result = zembly.callService(GET_USER_OWNED_ITEMS_URI,
                    new String[][]{
                        {USERID_PARAM, userid},
                        {ITEM_TYPE_PARAM, ITEM_TYPE_VALUE},
                        {SHOW_DRAFTS_PARAM, SHOW_DRAFTS_VALUE}
                    });
            System.out.println("your domains: " + result);
            return parseYourDomains(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }

    private Collection<WagDomain> parseAllDomains(String data) {
        try {
            JSONTokener parser = new JSONTokener(data);
            Collection<WagDomain> domains = new ArrayList<WagDomain>();

            while (parser.more()) {
                JSONArray array = (JSONArray) parser.nextValue();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    WagDomain domain = new WagDomain(obj.getString(NAME_ATTR), obj.getString(PATH_ATTR));
                    System.out.println("domain: " + domain);
                    domains.add(domain);
                }
            }

            return domains;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }

    private Collection<WagDomain> parseYourDomains(String data) {
        try {
            Collection<WagDomain> domains = new ArrayList<WagDomain>();

            JSONTokener parser = new JSONTokener(data);
            JSONObject obj = (JSONObject) parser.nextValue();
            JSONArray items = (JSONArray) obj.getJSONArray(ITEMS_ATTR);

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                WagDomain domain = new WagDomain(item.getString(NAME_ATTR), item.getString(PATH_ATTR));
                System.out.println("your domain: " + domain);
                domains.add(domain);
            }

            return domains;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }
}
