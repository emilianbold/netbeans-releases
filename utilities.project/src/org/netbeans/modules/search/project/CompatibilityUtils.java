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
package org.netbeans.modules.search.project;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.search.SearchInfoDefinitionFactory;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.FileNameMatcher;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.provider.TerminationFlag;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openidex.search.FileObjectFilter;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;
import org.openidex.search.Utils;

/**
 *
 * @author jhavlin
 */
class CompatibilityUtils {

    /**
     * Convert an old SearchInfo object to SearchInfoDefinition object.
     */
    static SearchInfoDefinition searchInfoToSearchInfoDefinition(
            final SearchInfo searchInfo) {

        return new WrappingSearchInfoDefinition(searchInfo);
    }

    /**
     * Convert list of FileObjectFilters to list of SearchFilterDefinitions.
     */
    static List<SearchFilterDefinition> fileObjectFiltersToSearchFilters(
            List<FileObjectFilter> filters) {

        List<SearchFilterDefinition> l = new LinkedList<SearchFilterDefinition>();
        for (FileObjectFilter fof : filters) {
            if (fof == SearchInfoFactory.SHARABILITY_FILTER) {
                l.add(SearchInfoDefinitionFactory.SHARABILITY_FILTER);
            } else if (fof == SearchInfoFactory.VISIBILITY_FILTER) {
                l.add(SearchInfoDefinitionFactory.VISIBILITY_FILTER);
            } else {
                l.add(new WrappingSearchFilter(fof));
            }
        }
        return l;
    }

    /**
     * Class wrapping FileObjectFilter to SearchFilter.
     */
    private static class WrappingSearchFilter extends SearchFilterDefinition {

        private FileObjectFilter delegate;

        public WrappingSearchFilter(FileObjectFilter delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean searchFile(FileObject file)
                throws IllegalArgumentException {

            return delegate.searchFile(file);
        }

        @Override
        public SearchFilterDefinition.FolderResult traverseFolder(
                FileObject folder) throws IllegalArgumentException {

            switch (delegate.traverseFolder(folder)) {
                case FileObjectFilter.DO_NOT_TRAVERSE:
                    return SearchFilterDefinition.FolderResult.DO_NOT_TRAVERSE;
                case FileObjectFilter.TRAVERSE_ALL_SUBFOLDERS:
                    return SearchFilterDefinition.FolderResult.TRAVERSE_ALL_SUBFOLDERS;
                case FileObjectFilter.TRAVERSE:
                    return SearchFilterDefinition.FolderResult.TRAVERSE;
            }
            throw new IllegalArgumentException();
        }
    }

    private static class WrappingSearchInfoDefinition
            extends SearchInfoDefinition {

        private SearchInfo searchInfo;

        public WrappingSearchInfoDefinition(SearchInfo searchInfo) {
            this.searchInfo = searchInfo;
        }

        @Override
        public boolean canSearch() {
            return searchInfo.canSearch();
        }

        @Override
        public Iterator<FileObject> filesToSearch(SearchScopeOptions options,
                SearchListener listener, TerminationFlag terminationFlag) {

            return new WrappingIterator(options,
                    Utils.getFileObjectsIterator(searchInfo),
                    listener, terminationFlag);
        }

        @Override
        public List<SearchRoot> getSearchRoots() {

            return Collections.emptyList(); // TODO could be obtained by simpleSearchIterator.
        }
    }

    /**
     * Iterator that wraps original iterator and filters only files that are
     * relevant for searching.
     */
    private static class WrappingIterator implements Iterator<FileObject> {

        private FileNameMatcher fileNameMatcher;
        private Iterator<FileObject> originalIterator;
        private SearchListener listener;
        private boolean upToDate = false;
        private FileObject next = null;
        private TerminationFlag terminationFlag;
        private List<SearchFilterDefinition> filters;

        public WrappingIterator(SearchScopeOptions searchScopeOptions,
                Iterator<FileObject> originalIterator,
                SearchListener listener, TerminationFlag terminationFlag) {

            this.fileNameMatcher = FileNameMatcher.create(searchScopeOptions);

            this.originalIterator = originalIterator;
            this.listener = listener;
            this.terminationFlag = terminationFlag;
            this.filters = searchScopeOptions.getFilters();
        }

        private void update() {
            assert !upToDate;
            itLoop:
            while (originalIterator.hasNext()) {
                FileObject fo = originalIterator.next();
                if (fo.isFolder()) {
                    continue;
                } else if (fileNameMatcher.pathMatches(fo)) {
                    for (SearchFilterDefinition filter : filters) {
                        if (!filter.searchFile(fo)) {
                            listener.fileSkipped(fo, filter, null);
                            continue itLoop;
                        }
                    }
                    next = fo;
                    upToDate = true;
                    return;
                }
            }
            next = null;
            upToDate = true;
        }

        @Override
        public boolean hasNext() {
            if (!upToDate) {
                update();
            }
            return next != null && !terminationFlag.isTerminated();
        }

        @Override
        public FileObject next() {

            if (!upToDate) {
                update();
            }

            if (next != null) {
                FileObject toReturn = next;
                upToDate = false;
                next = null;
                return toReturn;
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * Get search info for legacy or current definition in a lookup.
     */
    static org.netbeans.api.search.provider.SearchInfo getSearchInfoForLookup(
            Lookup lookup) {

        SearchInfoDefinition sid = lookup.lookup(SearchInfoDefinition.class);
        if (sid != null) {
            return SearchInfoUtils.createForDefinition(sid);
        }
        SearchInfo si = lookup.lookup(SearchInfo.class);
        if (si != null) {
            return legacyToCurrentSearchInfo(si);
        }
        return null;
    }

    /**
     * Get search info for a node. If there is no explicit search info
     * definition, try to create default search info.
     */
    static org.netbeans.api.search.provider.SearchInfo getSearchInfoForNode(
            Node node) {

        org.netbeans.api.search.provider.SearchInfo currentSearchInfo =
                SearchInfoUtils.findDefinedSearchInfo(node);
        if (currentSearchInfo != null) {
            return currentSearchInfo;
        }
        SearchInfo legacySearchInfo = node.getLookup().lookup(SearchInfo.class);
        if (legacySearchInfo != null) {
            return legacyToCurrentSearchInfo(legacySearchInfo);
        }
        Project p = node.getLookup().lookup(Project.class);
        if (p != null && !hasAncestorProjectNode(node.getParentNode())) {
            return AbstractProjectSearchScope.createDefaultProjectSearchInfo(p);
        }
        return SearchInfoUtils.getSearchInfoForNode(node);
    }

    /**
     * Check whether there is a project node among ancestors of a node.
     */
    private static boolean hasAncestorProjectNode(Node node) {
        if (node == null) {
            return false;
        }
        Project p = node.getLookup().lookup(Project.class);
        if (p != null) {
            return true;
        } else {
            return hasAncestorProjectNode(node.getParentNode());
        }
    }

    /**
     * Create SearchInfo for a legacy definition.
     */
    private static org.netbeans.api.search.provider.SearchInfo legacyToCurrentSearchInfo(SearchInfo legacyInfo) {
        return SearchInfoUtils.createForDefinition(
                new WrappingSearchInfoDefinition(legacyInfo));
    }
}
