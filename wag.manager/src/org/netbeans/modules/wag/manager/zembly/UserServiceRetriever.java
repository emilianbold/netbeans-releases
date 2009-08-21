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
import java.util.Collection;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.modules.wag.manager.model.WagService;

/**
 *
 * @author peterliu
 */
public class UserServiceRetriever {

    private static final String GET_USER_OWNED_ITEMS_URI = "platform.user.GetUserOwnedItems"; // NOI18N
    private static final String ITEM_TYPE_PARAM = "itemType";     //NOI18N
    private static final String ITEM_TYPE_VALUE = "[\"SERVICE\"]";    //NOI18N
    private static final String SHOW_DRAFTS_PARAM = "showDrafts";   //NOI18N
    private static final String SHOW_DRAFTS_VALUE = "true";        //NOI18N
    private static final String USERID_PARAM = "userid";        //NOI18N
    private static final String ITEMS_ATTR = "items";   //NOI18N
 
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
          
            JSONTokener parser = new JSONTokener(data);
            JSONObject obj = (JSONObject) parser.nextValue();
            JSONArray items = (JSONArray) obj.getJSONArray(ITEMS_ATTR);

            return ZemblySession.getInstance().getItemInfoRetriever().getServices(items);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }
}
