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
package org.netbeans.modules.maven.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import org.apache.lucene.search.BooleanQuery;
import org.netbeans.modules.maven.indexer.api.NBArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class MavenRepoProvider implements SearchProvider {

    private static final String NOT_SHOW_AGAIN = "notShowAgain";
    private static final String SEARCH_ENABLED = "searchEnabled";

    private static final SearchSetup sSetup = new SearchSetup();

    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with
     * apropriate results
     *
     * @param request Search request object that contains information what to search for
     * @param response Search response object that stores search results. Note that it's important to react to return value of SearchResponse.addResult(...) method and stop computation if false value is returned.
     */
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        List<RepositoryInfo> loadedRepos = RepositoryQueries.getLoadedContexts();
        Preferences prefs = NbPreferences.forModule(MavenRepoProvider.class);
        if (loadedRepos.size() == 0 && !prefs.getBoolean(SEARCH_ENABLED, false)) {
            if (!prefs.getBoolean(NOT_SHOW_AGAIN, false)) {
                response.addResult(sSetup, NbBundle.getMessage(MavenRepoProvider.class, "LBL_SearchSetup"),
                        NbBundle.getMessage(MavenRepoProvider.class, "TIP_SearchSetup"), null);
            }
            return;
        }

        List<NBVersionInfo> infos = null;
        try {
            infos = RepositoryQueries.find(getQuery(request));
        } catch (BooleanQuery.TooManyClauses e) {
            // query too general, just ignore it
            return;
        }
        Map<String, List<NBVersionInfo>> map = new TreeMap<String, List<NBVersionInfo>>(new Comp(request.getText()));
        for (NBVersionInfo nbvi : infos) {
            String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId(); //NOI18N
            List<NBVersionInfo> get = map.get(key);
            if (get == null) {
                get = new ArrayList<NBVersionInfo>();
                map.put(key, get);
            }
            get.add(nbvi);
        }
        Set<Entry<String, List<NBVersionInfo>>> entrySet = map.entrySet();
        for (Entry<String, List<NBVersionInfo>> entry : entrySet) {
            NBArtifactInfo nbai = new NBArtifactInfo(entry.getKey());
            nbai.addAlVersionInfos(entry.getValue());
            if (!response.addResult(new OpenArtifactInfo(nbai), nbai.getName())) {
                return;
            }
        }
    }

    List<QueryField> getQuery(SearchRequest request) {
        List<QueryField> fq = new ArrayList<QueryField>();
        String q = request.getText();
        String[] splits = q.split(" "); //NOI18N
        List<String> fields = new ArrayList<String>();
        fields.add(QueryField.FIELD_GROUPID);
        fields.add(QueryField.FIELD_ARTIFACTID);
        fields.add(QueryField.FIELD_NAME);
        fields.add(QueryField.FIELD_DESCRIPTION);
        fields.add(QueryField.FIELD_CLASSES);

        for (String one : splits) {
            for (String fld : fields) {
                QueryField f = new QueryField();
                f.setField(fld);
                f.setValue(one);
                fq.add(f);
            }
        }
        return fq;
    }

    //TODO copied from AddDependencyPanel.java, we shall somehow unify..
    private static class Comp implements Comparator<String> {
        private String query;

        public Comp(String q) {
            query = q;
        }

        /** Impl of comparator, sorts artifacts asfabetically with exception
         * of items that contain current query string, which take precedence.
         */
        public int compare(String s1, String s2) {

            int index1 = s1.indexOf(query);
            int index2 = s2.indexOf(query);

            if (index1 >= 0 || index2 >=0) {
                if (index1 < 0) {
                    return 1;
                } else if (index2 < 0) {
                    return -1;
                }
                return index1 - index2;
            } else {
                return s1.compareTo(s2);
            }
        }

    }

    private static class SearchSetup implements Runnable {

        public void run() {
            SearchSetupPanel ssPanel = new SearchSetupPanel();
            DialogDescriptor dd = new DialogDescriptor(ssPanel,
                    NbBundle.getBundle(MavenRepoProvider.class).getString("TIT_SearchSetup"));
            dd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(dd);
            if (ret == DialogDescriptor.YES_OPTION || ssPanel.isNotAgainChecked()) {
                Preferences prefs = NbPreferences.forModule(MavenRepoProvider.class);
                if (ret == DialogDescriptor.YES_OPTION) {
                    prefs.putBoolean(SEARCH_ENABLED, true);
                }
                if (ssPanel.isNotAgainChecked()) {
                    prefs.putBoolean(NOT_SHOW_AGAIN, true);
                }
            }
        }

    }

}
