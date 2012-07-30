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
package org.netbeans.modules.web.livehtml.filter.groupscripts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.AnalysisListener;
import org.netbeans.modules.web.livehtml.AnalysisStorage;
import org.netbeans.modules.web.livehtml.Change;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.filter.FilteredAnalysis;

/**
 *
 * @author petr-podzimek
 */
public class GroupScriptsFilteredAnalysis extends FilteredAnalysis {

    private final GroupScriptsRevisionFilter revisionFilter;
    
    private Map<Integer, Integer> groupedRevision = new HashMap<Integer, Integer>();
    private List<Integer> whitespaceRevision = new ArrayList<Integer>();
    
    private AnalysisListener analysisListener = new PrivateAnalystListener();

    public GroupScriptsFilteredAnalysis(GroupScriptsRevisionFilter revisionFilter, Analysis parentAnalysis) {
        super(parentAnalysis);
        this.revisionFilter = revisionFilter;
        
        if (parentAnalysis != null) {
            setSourceUrl(parentAnalysis.getSourceUrl());
            parentAnalysis.addAnalysisListener(analysisListener);
        }
    }

    public GroupScriptsRevisionFilter getRevisionFilter() {
        return revisionFilter;
    }

    @Override
    public Revision getRevision(int changeIndex) {
        if (changeIndex >= getTimeStampsCount()) {
            return null;
        }
        final String timeStamp = getTimeStamps().get(changeIndex);
        StringBuilder content = read(CONTENT, timeStamp);
        
        StringBuilder diff = read(DIFF, timeStamp);
        StringBuilder beautifiedDiff = read(BDIFF, timeStamp);
        StringBuilder beautifiedContent = read(BCONTENT, timeStamp);
        
        StringBuilder stackTrace = read(STACKTRACE, getTimeStamps().get(changeIndex));
        StringBuilder data = read(DATA, getTimeStamps().get(changeIndex));
        
        Revision revision = new Revision(
                changeIndex,
                timeStamp,
                content,
                beautifiedContent,
                Change.decodeFromJSON(diff == null ? null : diff.toString()), 
                Change.decodeFromJSON(beautifiedDiff == null ? null : beautifiedDiff.toString()), 
                stackTrace,
                data);
        
        return revision;
    }
    
    @Override
    public synchronized void applyFilter() {
        
        groupedRevision.clear();
        whitespaceRevision.clear();
        
        List<Revision> revisionsGroup = new ArrayList<Revision>();
        List<Object> lastObjects = null;
        
        final int FIRST_INDEX = 1;
        int whiteSpaceDetected = 0;
        
        for (int i = 0; i <= getParentAnalysis().getTimeStampsCount(); i++) {
            final Revision revision = getParentAnalysis().getRevision(i);
            
            if (revision == null) {
                continue;
            }
            
            if (i <= FIRST_INDEX || getTimeStampsCount() == 0) {
                storeDocumentVersion(revision, true);
                continue;
            }

            if (revisionFilter.isIgnoreWhiteSpaces()) {
                if (revision.hasEmptyChanges()) {
                    if (whiteSpaceDetected == 0) {
                        whitespaceRevision.add(getTimeStampsCount() - 1);
                    }
                    whiteSpaceDetected += 1;
                    continue;
                }

                if (whiteSpaceDetected > 0) {
                    groupedRevision.put(getTimeStampsCount() - 1, whiteSpaceDetected);
                    storeDocumentVersion(revision, true);
                    whiteSpaceDetected = 0;
                    continue;
                }
                
            }
            
            List<Object> objects = revision.getStacktrace();
            if (revisionFilter.getStackTraceFilter() != null) {
                objects = revisionFilter.filter(objects);
            }
            
            if (revisionFilter.isGroupRevisions() || revisionFilter.getStackTraceFilter() != null) {
                if (lastObjects != null && !lastObjects.isEmpty()) {
                    if (revisionFilter.match(lastObjects, objects)) {
                        revisionsGroup.add(revision);
                        lastObjects = objects;
                    } else {
                        groupedRevision.put(getTimeStampsCount(), revisionsGroup.size() - 1);
                        final Revision lastRevision = revisionsGroup.get(revisionsGroup.size() - 1);
                        
                        storeDocumentVersion(lastRevision, true);
                        storeDocumentVersion(revision, true);

                        revisionsGroup.clear();
                        lastObjects = null;
                    }
                } else {
                        revisionsGroup.add(revision);
                        lastObjects = objects;
                }
            } else {
                storeDocumentVersion(revision, true);
            }
            
        }
        
        if (whiteSpaceDetected > 0) {
            groupedRevision.put(getTimeStampsCount() - 1, whiteSpaceDetected);
            whiteSpaceDetected = 0;
        }
                
        if (lastObjects != null && !revisionsGroup.isEmpty()) {
            final Revision lastRevision = revisionsGroup.get(revisionsGroup.size() - 1);
            groupedRevision.put(getTimeStampsCount(), revisionsGroup.size() - 1);
            storeDocumentVersion(lastRevision, true);
        } else {
            storeDocumentVersions(revisionsGroup);
        }

        revisionsGroup.clear();
        
        makeFinished();
    }
    
    public Integer getRevisionCountForGroup(Integer revisionIndex) {
        return groupedRevision.get(revisionIndex);
    }

    public Map<Integer, Integer> getGroupedRevision() {
        return groupedRevision;
    }

    @Override
    public String getFilteredRevisionLabel(int selectedRevisionIndex) {
        final Integer revisionCountForGroup = getRevisionCountForGroup(selectedRevisionIndex);
        if (revisionCountForGroup != null) {
            if (isWhiteSpacedRevision(selectedRevisionIndex)) {
                return "+" + revisionCountForGroup + " white spaces Revisions";
            } else {
                return "+" + revisionCountForGroup + " grouped Revisions";
            }
        }
        return "";
    }

    public boolean isWhiteSpacedRevision(Integer selectedRevisionIndex) {
        return whitespaceRevision.contains(selectedRevisionIndex);
    }

    private void storeDocumentVersion(Revision revision, boolean realChange) {
        for (String fileName : getRootDirectory().list()) {
            if (fileName.startsWith(revision.getTimeStamp())) {
                File file = new File(getRootDirectory(), fileName);
                file.delete();
            }
        }
        
        storeDocumentVersion(
                revision.getTimeStamp(),// + "-" + generation, 
//                String.valueOf(System.currentTimeMillis()),
                revision.getContent() == null ? "null" : revision.getContent(), 
                revision.getStacktrace() == null ? "null" : revision.getStacktrace().toJSONString(),
                realChange);
    }

    private void storeDocumentVersions(List<Revision> revisions) {
        for (Revision revision : revisions) {
            storeDocumentVersion(revision, true);
        }
    }

    private class PrivateAnalystListener implements AnalysisListener {
        
        @Override
        public void revisionAdded(Analysis analysis, String timeStamp) {

            clearLastChangeWasNotReal();
            clearLastDiff();
            clearTimeStamps();
            cleatDataToStore();

//            Runnable r = new Runnable() {
//                @Override
//                public void run() {
                    applyFilter();
//                }
//            };
//
//            if (AnalysisStorage.isUnitTesting) {
//                r.run();
//            } else {
//                getRequestProcessor().post(r);
//            }
        }

    }
    
}
