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
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.modules.wag.manager.model.WagApi;
import org.netbeans.modules.wag.manager.model.WagRankedServices.RankingType;
import org.netbeans.modules.wag.manager.model.WagService;
import org.netbeans.modules.wag.manager.util.Utilities;

/**
 *
 * @author peterliu
 */
public class RankingRetriever {

    private static final String GET_RANKINGS_URL = "platform.analytic.GetRankings";        //NOI18N
    private static final String CATEGORIES_PARAM = "categories";    //NOI18N
    private static final String MAX_RESULTS_PARAM = "maxResults";    //NOI18N
    private static final String RANKING_PERIODS_PARAM = "rankingPeriods"; //NOI18N
    private static final String MAX_RESULTS = "20"; //NOI18N
    private static final String HIGHEST_RATED_SERVICES = "HIGHEST_RATED_SERVICES";  //NOI18N
    private static final String MOST_USED_JS_SERVICES = "MOST_USED_JS_SERVICES";    //NOI18N
    private static final String NEWEST_APIS = "NEWEST_APIS";        //NOI18N
    private static final String ALL_TIME = "ALL_TIME";      //NOI18N
    private static final String LAST_24HR = "LAST_24HR";    //NOI18N
    private static final String RANKS_ATTR = "ranks";       //NOI18N
    private static final String NAME_ATTR = "name";     //NOI18N
    private static final String PATH_ATTR = "path";     //NOI18N
    private Zembly zembly;

    public RankingRetriever(Zembly zembly) {
        this.zembly = zembly;
    }

    public Collection<WagService> getRankedServices(RankingType type) {
        String category = HIGHEST_RATED_SERVICES;
        String rankingPeriod = ALL_TIME;

        if (type == RankingType.HIGHEST_RATED) {
            category = MOST_USED_JS_SERVICES;
            rankingPeriod = LAST_24HR;
        }

        try {
            String result = zembly.callService(GET_RANKINGS_URL,
                    new String[][]{{CATEGORIES_PARAM, category},
                        {RANKING_PERIODS_PARAM, rankingPeriod},
                        {MAX_RESULTS_PARAM, MAX_RESULTS}});
            return parseRankings(result);
        } catch (Exception ex) {
            Utilities.handleException(ex);
        }

        return Collections.emptyList();
    }

    public Collection<WagApi> getNewestApis() {
        try {
            String result = zembly.callService(GET_RANKINGS_URL,
                    new String[][]{{CATEGORIES_PARAM, NEWEST_APIS},
                        {RANKING_PERIODS_PARAM, ALL_TIME},
                        {MAX_RESULTS_PARAM, MAX_RESULTS}});
            return parseNewestApis(result);
        } catch (Exception ex) {
            Utilities.handleException(ex);
        }

        return Collections.emptyList();
    }

    private Collection<WagService> parseRankings(String data) {
        try {
            JSONTokener parser = new JSONTokener(data);
            JSONArray array = (JSONArray) parser.nextValue();
            JSONObject obj = array.getJSONObject(0);
            JSONArray items = (JSONArray) obj.getJSONArray(RANKS_ATTR);

            return ZemblySession.getInstance().getItemInfoRetriever().getServices(items);
        } catch (Exception ex) {
            Utilities.handleException(ex);
        }

        return Collections.emptyList();
    }

    private Collection<WagApi> parseNewestApis(String data) {
        try {
            JSONTokener parser = new JSONTokener(data);
            JSONArray array = (JSONArray) parser.nextValue();
            JSONObject obj = array.getJSONObject(0);
            JSONArray items = (JSONArray) obj.getJSONArray(RANKS_ATTR);
            Collection<WagApi> apis = new ArrayList<WagApi>();

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                WagApi api = new WagApi(item.getString(NAME_ATTR), item.getString(PATH_ATTR));
                System.out.println("api: " + api);
                apis.add(api);
            }

            return apis;

        } catch (Exception ex) {
            Utilities.handleException(ex);
        }

        return Collections.emptyList();
    }
}
