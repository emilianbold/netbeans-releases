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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.search.ui.BasicAbstractResultsPanel;
import org.netbeans.modules.search.ui.BasicReplaceResultsPanel;
import org.netbeans.modules.search.ui.BasicSearchResultsPanel;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
class ResultDisplayer extends SearchResultsDisplayer<MatchingObject.Def> {

    private ResultModel resultModel;
    private BasicSearchCriteria criteria;
    private BasicComposition composition;
    private BasicAbstractResultsPanel resultPanel;
    private Node infoNode;

    public ResultDisplayer(BasicSearchCriteria criteria,
            BasicComposition composition) {
        this.criteria = criteria;
        this.resultModel = new ResultModel(criteria,
                criteria.getReplaceString());
        this.composition = composition;

        resultModel.setCommonSearchFolder(
                CommonSearchRoot.find(composition.getRootFiles()));
    }

    @Override
    public synchronized JComponent getVisualComponent() {
        if (resultPanel != null) {
            return resultPanel;
        }
        if (criteria.isSearchAndReplace()) {
            resultPanel = new BasicReplaceResultsPanel(resultModel, composition,
                    composition.getRootFiles(), infoNode);
        } else {
            resultPanel = new BasicSearchResultsPanel(resultModel, composition,
                    criteria.isFullText(), composition.getRootFiles(),
                    infoNode);
        }
        resultPanel.setToolTipText(composition.getScopeDisplayName()
                + ": " + getTitle());                                   //NOI18N
        return resultPanel;
    }

    @Override
    public void addMatchingObject(MatchingObject.Def object) {
        if (resultModel.objectFound(object.getFileObject(), object.getCharset(),
                object.getTextDetails())) {
            resultPanel.update();
            resultPanel.addMatchingObject(
                    resultModel.getMatchingObjects().get(
                    resultModel.size() - 1));
        }
        if (resultModel.wasLimitReached()) {
            composition.terminate();
        }
    }

    ResultModel getResultModel() {
        return resultModel;
    }

    @Override
    public String getTitle() {
        if (criteria.getTextPattern() == null) {
            if (criteria.getFileNamePattern() == null) {
                return NbBundle.getMessage(ResultView.class,
                        "TEXT_MSG_RESULTS_FOR_FILE_PATTERN"); //NOI18N
            } else {
                return criteria.getFileNamePatternExpr();
            }
        } else {
            return criteria.getTextPatternExpr();
        }
    }

    @Override
    public void searchStarted() {
        resultModel.setStartTime();
        resultPanel.searchStarted();
    }

    @Override
    public void searchFinished() {
        resultPanel.searchFinished();
    }

    /**
     * Class for holding a updating common search root of searched files.
     */
    static class CommonSearchRoot {

        /**
         * Minimal number of folders shown in relative path to a matching file.
         */
        private static final int MIN_REL_PATH_LEN = 4;
        private boolean exists = true;
        private List<FileObject> path;
        private FileObject file = null;

        /**
         * Update path to folder that is common parent of all searched files.
         */
        synchronized void update(FileObject fo) {

            if (!exists) {
                // It is clear that no common path does exist.
            } else if (exists && file == null) {
                // Common path has not been initialized yet.
                initCommonPath(fo);
            } else if ((FileUtil.isParentOf(file, fo))) {
                // No need to update, file is under common path.
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

        /**
         * Find common part of two file paths.
         *
         * @return Longest common sub-path. If p1 and p2 are equal paths, p1 is
         * returned.
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

        /**
         * Get list describing file path from root (first item) to a file.
         */
        static List<FileObject> filePathAsList(FileObject fo) {
            List<FileObject> path = new LinkedList<FileObject>();
            for (FileObject p = fo; p != null; p = p.getParent()) {
                path.add(0, p);
            }
            return path;
        }

        /**
         * Create initial common path for the first searched file.
         */
        private void initCommonPath(FileObject fo) {

            for (FileObject p = fo; p != null; p = p.getParent()) {
                if (isLikeProjectFolder(p)) {
                    FileObject projectParent = p.getParent();
                    if (projectParent != null) {
                        file = projectParent;
                        path = filePathAsList(projectParent);
                        return;
                    }
                }
            }
            List<FileObject> p = filePathAsList(fo);
            if (p.size() > MIN_REL_PATH_LEN) {
                path = p.subList(0, p.size() - MIN_REL_PATH_LEN);
                file = path.get(path.size() - 1);
            } else {
                exists = false;
            }
        }

        /**
         * Return true if folder seems to be a project folder
         */
        private boolean isLikeProjectFolder(FileObject folder) {
            if (folder.getFileObject("src") != null) {
                return true;
            } else if (folder.getFileObject("nbproject") != null) {
                return true;
            } else {
                return false;
            }
        }

        synchronized FileObject getFileObject() {
            if (exists) {
                return file;
            } else {
                return null;
            }
        }

        private static FileObject find(List<FileObject> roots) {
            CommonSearchRoot csr = new CommonSearchRoot();
            for (FileObject f : roots) {
                csr.update(f);
            }
            return csr.getFileObject();
        }
    }

    @Override
    public void setInfoNode(Node infoNode) {
        this.infoNode = infoNode;
    }

    @Override
    public void closed() {
        resultPanel.closed();
    }
}
