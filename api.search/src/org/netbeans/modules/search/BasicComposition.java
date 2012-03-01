/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.matcher.AbstractMatcher;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.netbeans.spi.search.provider.SearchProvider.Presenter;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.openide.filesystems.FileObject;

/**
 *
 * @author jhavlin
 */
public class BasicComposition extends SearchComposition<MatchingObject.Def> {

    private SearchInfo searchInfo;
    private AbstractMatcher matcher;
    private SearchResultsDisplayer<MatchingObject.Def> displayer = null;
    private BasicSearchCriteria basicSearchCriteria;
    private Presenter presenter;
    AtomicBoolean terminated = new AtomicBoolean(false);

    public BasicComposition(SearchInfo searchInfo, AbstractMatcher matcher,
            BasicSearchCriteria basicSearchCriteria, Presenter presenter) {

        this.searchInfo = searchInfo;
        this.matcher = matcher;
        this.basicSearchCriteria = basicSearchCriteria;
        this.presenter = presenter;
    }

    @Override
    public void start(SearchListener listener) {

        Iterable<FileObject> iterable = searchInfo.iterateFilesToSearch(
                basicSearchCriteria.getSearcherOptions(),
                listener, terminated);

        for (FileObject fo : iterable) {

            Def result = matcher.check(fo, listener);
            if (result != null) {
                getSearchResultsDisplayer().addMatchingObject(result);
            }
            if (terminated.get()) {
                break;
            }
        }
    }

    @Override
    public void terminate() {
        terminated.set(true);
        matcher.terminate();
    }

    @Override
    public boolean isTerminated() {
        return terminated.get();
    }

    @Override
    public synchronized SearchResultsDisplayer<Def> getSearchResultsDisplayer() {
        if (displayer == null) {
            displayer = new ResultDisplayer(basicSearchCriteria,
                    this);
        }
        return displayer;
    }

    public SearchResultsDisplayer<Def> getDisplayer() {
        return displayer;
    }

    List<FileObject> getRootFiles() {
        List<FileObject> list = new LinkedList<FileObject>();
        List<SearchRoot> searchRoots = searchInfo.getSearchRoots();
        if (searchRoots == null) {
            return Collections.emptyList();
        }
        for (SearchRoot sr : searchRoots) {
            list.add(sr.getFileObject());
        }
        return list;
    }

    public SearchProvider.Presenter getSearchProviderPresenter() {
        return presenter;
    }
}
