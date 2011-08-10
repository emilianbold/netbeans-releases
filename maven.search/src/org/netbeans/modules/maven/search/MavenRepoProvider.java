/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;
import org.apache.lucene.search.BooleanQuery;
import org.netbeans.modules.maven.indexer.api.NBArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.QueryRequest;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.RequestProcessor;

public class MavenRepoProvider implements SearchProvider {

    private static final RequestProcessor RP = new RequestProcessor(MavenRepoProvider.class.getName(), 10);

    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with
     * appropriate results
     *
     * @param request Search request object that contains information what to search for
     * @param response Search response object that stores search results. Note that it's important to react to return value of SearchResponse.addResult(...) method and stop computation if false value is returned.
     */
    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        
        List<RepositoryInfo> loadedRepos = RepositoryQueries.getLoadedContexts();
        if (loadedRepos.isEmpty()) {
            return;
        }

        List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
        final List<NBVersionInfo> tempInfos = new ArrayList<NBVersionInfo>();
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (null == o || !(o instanceof QueryRequest)) {
                    return;
                }
                synchronized (tempInfos) {
                    tempInfos.addAll(((QueryRequest) o).getResults());
                }
            }
        };
        final QueryRequest queryRequest = new QueryRequest(getQuery(request), loadedRepos, observer);
        final RequestProcessor.Task searchTask = RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    RepositoryQueries.find(queryRequest);
                } catch (BooleanQuery.TooManyClauses exc) {
                    // query too general, just ignore it
                    synchronized (tempInfos) {
                        tempInfos.clear();
                    }
                } catch (OutOfMemoryError oome) {
                    // running into OOME may still happen in Lucene despite the fact that
                    // we are trying hard to prevent it in NexusRepositoryIndexerImpl
                    // (see #190265)
                    // in the bad circumstances theoretically any thread may encounter OOME
                    // but most probably this thread will be it
                    synchronized (tempInfos) {
                        tempInfos.clear();
                    }
                }
            }
        });
        try {
            // wait maximum 5 seconds for the repository search to complete
            // after the timeout tempInfos should contain at least partial results
            // we are not waiting longer, repository index download may be running on the background
            // because NexusRepositoryIndexerImpl.getLoaded() might have returned also repos
            // which are not available for the search yet
            searchTask.waitFinished(5000);
        } catch (InterruptedException ex) {
        }
        queryRequest.deleteObserver(observer);
        synchronized (tempInfos) {
            infos.addAll(tempInfos);
        }
        searchTask.cancel();

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
            nbai.addAllVersionInfos(entry.getValue());
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
//        fields.add(QueryField.FIELD_VERSION);
        fields.add(QueryField.FIELD_NAME);
        fields.add(QueryField.FIELD_DESCRIPTION);
//        fields.add(QueryField.FIELD_CLASSES);

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
                return s1.compareTo(s2);
            } else {
                return s1.compareTo(s2);
            }
        }

    }
}
