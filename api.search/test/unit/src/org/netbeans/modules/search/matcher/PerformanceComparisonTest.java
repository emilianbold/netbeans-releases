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
package org.netbeans.modules.search.matcher;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.search.SearchInfoDefinitionFactory;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.provider.TerminationFlag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class PerformanceComparisonTest extends NbTestCase {

    public PerformanceComparisonTest(String name) {
        super(name);
    }

    public void testPerformance() {

        if (true) {
            System.out.println("Skipping long-running local test.");
            return;
        }

        String directory = "c:\\users\\jhavlin\\documents\\NetBeansProjects\\JavaApplication\\files\\testSearch\\textFiles\\text_3000000.txt";
        SearchPattern sp = SearchPattern.create("private", false, false, false);

        long def, fst;

        //def = search("Default", createSearcher(directory, "*.java"), new DefaultMatcher(sp));
        fst = search("FastSrc", createSearchInfo(directory), new MultiLineMappedMatcherBig(sp));

        //System.out.println("Faster ratio: " + ((double) fst / (double) def) * 100 + " %");

        //fst = search("FastSrc", createSearcher(directory, "*.java"), new FastMatcher(sp));
        //def = search("Default", createSearcher(directory, "*.java"), new DefaultMatcher(sp));

        //System.out.println("Faster ratio: " + ((double) fst / (double) def) * 100 + " %");
    }

    private long search(String name, SearchInfo searchInfo,
            AbstractMatcher matcher) {

        CountingListener cl = new CountingListener();
        SearchScopeOptions sso = SearchScopeOptions.create("*.txt", false);

        TerminationFlag tf = new TerminationFlag() {

            @Override
            public boolean isTerminated() {
                return false;
            }
        };

        for (FileObject fo : searchInfo.iterateFilesToSearch(sso, cl, tf)) {
            Def result = matcher.check(fo, cl);
            if (result != null) {
                cl.objectFound(result);
            }
        }

        long time = 0;

        if (matcher instanceof DefaultMatcher) {
            time = ((DefaultMatcher) matcher).getTotalTime();
        } else if (matcher instanceof FastMatcher) {
            time = ((FastMatcher) matcher).getTotalTime();
        }

        System.out.println(name + " runned " + time + " ms, count: " + cl.count);
        return time;
    }

    private SearchInfo createSearchInfo(String dir) {
        SearchInfo searchInfo = new MockSearchInfo(
                FileUtil.toFileObject(new File(dir)));
        return searchInfo;
    }

    private class CountingListener extends SearchListener {

        private int count = 0;

        public void objectFound(MatchingObject.Def object) {
            count++;
        }
    }

    private static class MockSearchInfo extends SearchInfo {

        private FileObject root;

        public MockSearchInfo(FileObject root) {
            this.root = root;
        }

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public List<SearchRoot> getSearchRoots() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<FileObject> getFilesToSearch(SearchScopeOptions options,
                SearchListener listener, TerminationFlag terminationFlag) {

            SearchInfoDefinition si;
            si = SearchInfoDefinitionFactory.createSearchInfo(
                    root, new SearchFilterDefinition[0]);
            return si.filesToSearch(options, listener, terminationFlag);
        }
    }
}
