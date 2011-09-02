/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.search;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openidex.search.DataObjectSearchGroup;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchType;

/**
 *
 * @author  Marian Petras
 * @author  kaktus
 */
final class SpecialSearchGroup extends DataObjectSearchGroup {

    final BasicSearchCriteria basicCriteria;
    final boolean hasExtraSearchTypes;
    private final SearchScope searchScope;
    private SearchTask listeningSearchTask;
    private CommonSearchRoot commonSearchRoot = new CommonSearchRoot();

    private LinkedList searchItems;
    
    SpecialSearchGroup(BasicSearchCriteria basicCriteria,
                       Collection<SearchType> extraSearchTypes,
                       SearchScope searchScope) {
        super();
        this.basicCriteria = basicCriteria;
        hasExtraSearchTypes = !extraSearchTypes.isEmpty();
        this.searchScope = searchScope;
        
        if ((basicCriteria == null) && !hasExtraSearchTypes) {
            assert false;
            throw new IllegalArgumentException();
        }
        
        if (hasExtraSearchTypes) {
            for (SearchType searchType : extraSearchTypes) {
                add(searchType);
            }
        }
    }

    @Override
    protected void prepareSearch(){
        searchItems = new LinkedList();
        SearchInfo sInfo = searchScope.getSearchInfo();
        if (sInfo instanceof SearchInfo.Files){
            addSearchItems(((SearchInfo.Files)sInfo).filesToSearch());
        } else {
            addSearchItems(sInfo.objectsToSearch());
        }
    }

    private void addSearchItems(Iterator items) {
        for (Iterator j = items; j.hasNext(); ) {
            if (stopped) {
                return;
            }

            Object file = j.next();
            searchItems.add(file);

            if (file instanceof FileObject) {
                commonSearchRoot.update((FileObject) file);
            } else if (file instanceof DataObject) {
                commonSearchRoot.update(((DataObject) file).getPrimaryFile());
            }
        }
    }

    @Override
    public void doSearch() {
        notifyStarted(searchItems.size());
        int index = 0;
        while(!searchItems.isEmpty()) {
            if (stopped) {
                return;
            }
            processSearchObject(searchItems.poll());
            notifyProgress(index++);
        }
    }

    /**
     * Provides search on one search object instance. The object is added to
     * set of searched objects and passed to all search types encapsulated by
     * this search group. In the case the object passes all search types is added
     * to the result set and fired an event <code>PROP_FOUND</code> about successful
     * match to interested property change listeners. 
     *
     * @param searchObject object to provide actuall test on it. The actual instance
     * has to be of type returned by all <code>SearchKey.getSearchObjectType</code>
     * returned by <code>SearchType</code> of this <code>SearchGroup</code>
     */
    @Override
    protected void processSearchObject(Object searchObject) {
        if (!hasExtraSearchTypes) {
            assert basicCriteria != null;
            if (searchObject instanceof DataObject){
                DataObject dataObj = (DataObject) searchObject;
                if (basicCriteria.matches(dataObj)) {
                    notifyMatchingObjectFound(dataObj);
                }
            } else if (searchObject instanceof FileObject){
                FileObject fileObj = (FileObject) searchObject;
                if (basicCriteria.matches(fileObj)) {
//                    try {
//                        notifyMatchingObjectFound(DataObject.find(fileObj));
//                    } catch (DataObjectNotFoundException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
                    notifyMatchingObjectFound(fileObj);
                }
            }
            return;
        }
        
        if ((basicCriteria == null) || basicCriteria.matches((DataObject) searchObject)) {
            super.processSearchObject(searchObject);
        }
    }

    @Override
    protected void firePropertyChange(String name,
                                      Object oldValue,
                                      Object newValue) {
            notifyMatchingObjectFound((DataObject) newValue);
        }
    
    private void notifyMatchingObjectFound(Object obj) {
        if (listeningSearchTask != null) {
            Charset charset = (basicCriteria != null)
                              ? basicCriteria.getLastUsedCharset()
                              : null;
            listeningSearchTask.matchingObjectFound(obj, charset);
        }
    }

    private void notifyStarted(int unitsCount) {
        if (listeningSearchTask != null) {
            listeningSearchTask.searchStarted(unitsCount);
        }
    }

    private void notifyProgress(int progress) {
        if (listeningSearchTask != null) {
            listeningSearchTask.progress(progress);
        }
    }
    
    void setListeningSearchTask(SearchTask searchTask) {
        listeningSearchTask = searchTask;
    }

    SearchScope getSearchScope(){
        return searchScope;
    }

    /** Class for holding a updating common search root of searched files. */
    static class CommonSearchRoot {

        /** Minimal number of folders shown in relative path to a matching file.
         */
        private int minRelPathLen = 6;
        private boolean exists = true;
        private List<FileObject> path;
        private FileObject file = null;

        public CommonSearchRoot() {
        }

        public CommonSearchRoot(int minRelPathLen) {
            if (minRelPathLen < 1) {
                throw new IllegalArgumentException();
            }
            this.minRelPathLen = minRelPathLen;
        }

        /** Update path to folder that is common parent of all searched files. 
         */
        synchronized void update(FileObject fo) {

            if (!exists) {
                // It is clear that no common path does exist.
            } else if (exists && file == null) {
                // Common path has not been initialized yet.
                initCommonPath(fo);
            } else if ((FileUtil.isParentOf(file, fo))) {
                // No need to update, file under common path.
            } else {
                List<FileObject> p = filePathAsList(fo.getParent());
                path = findCommonPath(path, p);
                if (path.isEmpty()) {
                    path = null;
                    file = null;
                    exists = false;
                } else {
                    file = path.get(path.size() - 1);
                }
            }
        }

        /** Find common part of two file paths.
         *  
         * @return Longest common sub-path. If p1 and p2 are equal paths,
         * p1 is returned.
         */
        static List<FileObject> findCommonPath(List<FileObject> p1,
                List<FileObject> p2) {

            for (Iterator<FileObject> i1 = p1.iterator(),
                    i2 = p2.iterator(); i1.hasNext() && i2.hasNext();) {
                FileObject fo1 = i1.next();
                FileObject fo2 = i2.next();
                if (!fo1.equals(fo2)) {
                    return p1.subList(0, p1.indexOf(fo1));
                }
            }
            return p1;
        }

        /** Get list describing file path from root (first item) to a file. */
        static List<FileObject> filePathAsList(FileObject fo) {
            List<FileObject> path = new LinkedList<FileObject>();
            for (FileObject p = fo; p != null; p = p.getParent()) {
                path.add(0, p);
            }
            return path;
        }

        /** Create initial common path for the first searched file.  */
        private void initCommonPath(FileObject fo) {

            for (FileObject p = fo.getParent(); p != null; p = p.getParent()) {
                String name = p.getName();
                if ((name.equals("src") || name.equals("nbproject"))) {
                    FileObject projectDir = p.getParent();
                    if (projectDir != null) {
                        FileObject projectParent = projectDir.getParent();
                        if (projectParent != null) {
                            file = projectParent;
                            path = filePathAsList(projectParent);
                            return;
                        }
                    }
                }
            }
            List<FileObject> p = filePathAsList(fo);
            if (p.size() > minRelPathLen) {
                path = p.subList(0, p.size() - minRelPathLen);
                file = path.get(path.size() - 1);
            } else {
                exists = false;
            }
        }

        synchronized FileObject getFileObject() {
            if (exists) {
                return file;
            } else {
                return null;
            }
        }
    }
    
    /** Get folder that is common to all searched files. Can be null. */
    synchronized FileObject getCommonSearchFolder() {
        return commonSearchRoot.getFileObject();
    }
}
