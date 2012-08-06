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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.AnalysisListener;
import org.netbeans.modules.web.livehtml.Change;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.Utilities;
import org.netbeans.modules.web.livehtml.filter.FilteredAnalysis;
import org.netbeans.modules.web.livehtml.filter.RevisionFilter;

/**
 *
 * @author petr-podzimek
 */
public class GroupScriptsFilteredAnalysis extends FilteredAnalysis {
    
    private final ScriptRevisionFilter scriptRevisionFilter;
    private final StackTraceRevisionFilter stackTraceRevisionFilter;
    
    private final boolean ignoreWhiteSpaces;
    
    private Map<Integer, Set<Integer>> whiteSpaceGroupedRevisions = new HashMap<Integer, Set<Integer>>();
    private Map<Integer, Set<Integer>> stackTraceGroupedRevisions = new HashMap<Integer, Set<Integer>>();
    private Map<Integer, Set<Integer>> scriptGroupedRevisions = new HashMap<Integer, Set<Integer>>();

    private AnalysisListener analysisListener = new PrivateAnalystListener();

    public GroupScriptsFilteredAnalysis(ScriptRevisionFilter scriptRevisionFilter, StackTraceRevisionFilter stackTraceRevisionFilter, boolean ignoreWhiteSpaces, Analysis parentAnalysis) {
        super(parentAnalysis);
        this.scriptRevisionFilter = scriptRevisionFilter;
        this.stackTraceRevisionFilter = stackTraceRevisionFilter;
        this.ignoreWhiteSpaces = ignoreWhiteSpaces;

        setSourceUrl(parentAnalysis.getSourceUrl());
        parentAnalysis.addAnalysisListener(analysisListener);
        
        applyFilter();
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
        
        StringBuilder stackTrace = read(STACKTRACE, timeStamp);
        StringBuilder data = read(DATA, timeStamp);
        
        Revision revision = new Revision(
                changeIndex,
                timeStamp,
                content,
                beautifiedContent,
                Change.decodeFromJSON(diff == null ? null : diff.toString()), 
                Change.decodeFromJSON(beautifiedDiff == null ? null : beautifiedDiff.toString()), 
                stackTrace,
                data);
        revision.setPreviewContent(read(NO_JS_CONTENT, timeStamp));
        revision.setReformattedPreviewContent(read(FORMATTED_NO_JS_CONTENT, timeStamp));
        
        return revision;
    }

    private void applyFilter() {
        getTimeStamps().clear();
        
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 1; i < getParentAnalysis().getTimeStampsCount(); i++) {
            indexes.add(i);
        }
        
        final Map<Integer, Set<Integer>> whiteSpaceGroupedRevisions1 = getWhiteSpaceGroupedRevisions(indexes);
        final Map<Integer, Set<Integer>> stackTraceGroupRevisions1 = getStackTraceGroupRevisions(indexes);
        final Map<Integer, Set<Integer>> scriptGroupRevisions1 = getScriptGroupRevisions(indexes);

        Utilities.fixRemovedRevisions(indexes, whiteSpaceGroupedRevisions1, scriptGroupRevisions1, stackTraceGroupRevisions1);
        
        Revision revision0 = getParentAnalysis().getRevision(0);
        storeDocumentVersion(revision0, true);
        
        for (Integer index : indexes) {
            
            final int timeStampsCount = getTimeStampsCount();
            final Set<Integer> whiteSpaceRevisions = whiteSpaceGroupedRevisions1.get(index);
            final Set<Integer> stackTraceRevisions = stackTraceGroupRevisions1.get(index);
            final Set<Integer> scriptRevisions = scriptGroupRevisions1.get(index);
            
            Utilities.putRevisions(timeStampsCount, whiteSpaceRevisions, whiteSpaceGroupedRevisions);
            Utilities.putRevisions(timeStampsCount, stackTraceRevisions, stackTraceGroupedRevisions);
            Utilities.putRevisions(timeStampsCount, scriptRevisions, scriptGroupedRevisions);
            
            int revisionIndex = index;
            
            // 
            revisionIndex = Math.max(revisionIndex, Utilities.max(whiteSpaceRevisions));
            revisionIndex = Math.max(revisionIndex, Utilities.max(stackTraceRevisions));
            revisionIndex = Math.max(revisionIndex, Utilities.max(scriptRevisions));
            
            Revision revision = getParentAnalysis().getRevision(revisionIndex);
            storeDocumentVersion(revision, true);
        }
        
        makeFinished();
    }
    
    public Map<Integer, Set<Integer>> getWhiteSpaceGroupedRevisions(List<Integer> indexes) {
        Map<Integer, Set<Integer>> whiteSpaceRevisions = new HashMap<Integer, Set<Integer>>();
        if (!ignoreWhiteSpaces || indexes == null || indexes.isEmpty()) {
            return whiteSpaceRevisions;
        }
        Integer lastWhiteSpaceIndex = null;
        for (Iterator<Integer> it = indexes.iterator(); it.hasNext();) {
            Integer index = it.next();
            final String timeStamp = getParentAnalysis().getTimeStamps().get(index);
            final StringBuilder diffStr = getParentAnalysis().read(DIFF, timeStamp);
            final List<Change> diff = Change.decodeFromJSON(diffStr == null ? null : diffStr.toString());
            
            if (isEmptyChanges(diff)) {
                if (lastWhiteSpaceIndex == null) {
                    lastWhiteSpaceIndex = index;
                } else {
                    Utilities.putRevision(whiteSpaceRevisions, lastWhiteSpaceIndex, index);
                    it.remove();
                }
            } else {
                if (lastWhiteSpaceIndex != null) {
//                    put(whiteSpaceRevisions, lastWhiteSpaceIndex, index);
                    lastWhiteSpaceIndex = index;
                }
            }

        }
//        if (lastWhiteSpaceIndex != null) {
////            put(whiteSpaceRevisions, lastWhiteSpaceIndex, indexes.size());
////            indexes.remove(lastWhiteSpaceIndex);
//            lastWhiteSpaceIndex = null;
//        }
        return whiteSpaceRevisions;
    }
    
    private static boolean isEmptyChanges(List<Change> changes) {
        if (changes== null || changes.isEmpty()) {
            return true;
        }
        for (Change change : changes) {
            if (!change.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public Map<Integer, Set<Integer>> getStackTraceGroupRevisions(List<Integer> indexes) {
        return getRevisionFilterRevisions(indexes, stackTraceRevisionFilter);
    }
    
    public Map<Integer, Set<Integer>> getScriptGroupRevisions(List<Integer> indexes) {
        return getRevisionFilterRevisions(indexes, scriptRevisionFilter);
    }
    
    private Map<Integer, Set<Integer>> getRevisionFilterRevisions(List<Integer> indexes, RevisionFilter<JSONArray> revisionFilter) {
        Map<Integer, Set<Integer>>  revisionFilterRevisions = new HashMap<Integer, Set<Integer>> ();
        if (indexes == null || indexes.isEmpty() || revisionFilter == null) {
            return revisionFilterRevisions;
        }
        Integer lastGroupIndex = null;
        JSONArray lastStackTrace = null;
        for (Iterator<Integer> it = indexes.iterator(); it.hasNext();) {
            Integer index = it.next();
            final String timeStamp = getParentAnalysis().getTimeStamps().get(index - 1);
            final StringBuilder stackTraceStr = getParentAnalysis().read(STACKTRACE, timeStamp);
            if (stackTraceStr == null) {
                continue;
            }
            
            JSONArray stacktrace = (JSONArray) JSONValue.parse(stackTraceStr.toString());
            JSONArray filteredStacktrace = revisionFilter.filter(stacktrace);
            
            if (revisionFilter.match(lastStackTrace, filteredStacktrace)) {
                Utilities.putRevision(revisionFilterRevisions, lastGroupIndex, index);
                it.remove();
            } else {
                lastGroupIndex = index;
                lastStackTrace = filteredStacktrace;
            }

        }
//        if (lastGroupIndex != null) {
//            indexes.remove(lastGroupIndex);
//        }
        return revisionFilterRevisions;
    }
    
    @Override
    public String getRevisionDetailLabel(int selectedRevisionIndex) {
        StringBuilder label = new StringBuilder();
        
        final Set<Integer> scriptGroupRevisions1 = getScriptGroupedRevisions(selectedRevisionIndex);
        if (scriptGroupRevisions1 != null) {
            label.append(" + Grouped by script");
            label.append(scriptGroupRevisions1);
        }
        final Set<Integer> stackTraceGroupRevisions1 = getStackTraceGroupedRevisions(selectedRevisionIndex);
        if (stackTraceGroupRevisions1 != null) {
            label.append(" + Grouped by Stack Trace");
            label.append(stackTraceGroupRevisions1);
        }
        
        final Set<Integer> whiteSpaceGroupedRevisions1 = getWhiteSpaceGroupedRevisions(selectedRevisionIndex);
        if (whiteSpaceGroupedRevisions1 != null) {
            label.append(" + Grouped by White spaces");
            label.append(whiteSpaceGroupedRevisions1);
        }
        
        return label.toString();
    }

    private void storeDocumentVersion(Revision revision, boolean realChange) {
        if (revision == null) {
            return;
        }
        
        for (String fileName : getRootDirectory().list()) {
            if (fileName.startsWith(revision.getTimeStamp())) {
                File file = new File(getRootDirectory(), fileName);
                file.delete();
            }
        }
        
        storeDocumentVersion(
                revision.getTimeStamp(),// + "-" + generation, 
//                String.valueOf(System.currentTimeMillis()),
                revision.getContent() == null ? null : revision.getContent(), 
                revision.getStacktrace() == null ? null : revision.getStacktrace().toJSONString(),
                revision.getData() == null ? null : revision.getData().toString(),
                realChange);
    }

    public Set<Integer> getWhiteSpaceGroupedRevisions(Integer revisionIndex) {
        if (!hasWhiteSpaceGroupedRevisions()) {
            return null;
        }
        return whiteSpaceGroupedRevisions.get(revisionIndex);
    }

    public Set<Integer> getStackTraceGroupedRevisions(Integer revisionIndex) {
        if (!hasStackTraceGroupedRevisions()) {
            return null;
        }
        return stackTraceGroupedRevisions.get(revisionIndex);
    }

    public Set<Integer> getScriptGroupedRevisions(Integer revisionIndex) {
        if (!hasScriptGroupedRevisions()) {
            return null;
        }
        return scriptGroupedRevisions.get(revisionIndex);
    }

    private boolean hasWhiteSpaceGroupedRevisions() {
        return whiteSpaceGroupedRevisions != null && !whiteSpaceGroupedRevisions.isEmpty();
    }

    private boolean hasStackTraceGroupedRevisions() {
        return stackTraceGroupedRevisions != null && !stackTraceGroupedRevisions.isEmpty();
    }

    private boolean hasScriptGroupedRevisions() {
        return scriptGroupedRevisions != null && !scriptGroupedRevisions.isEmpty();
    }

    public Map<Integer, Set<Integer>> getWhiteSpaceGroupedRevisions() {
        return whiteSpaceGroupedRevisions;
    }

    public Map<Integer, Set<Integer>> getStackTraceGroupedRevisions() {
        return stackTraceGroupedRevisions;
    }

    public Map<Integer, Set<Integer>> getScriptGroupedRevisions() {
        return scriptGroupedRevisions;
    }

    public ScriptRevisionFilter getScriptRevisionFilter() {
        return scriptRevisionFilter;
    }

    public StackTraceRevisionFilter getStackTraceRevisionFilter() {
        return stackTraceRevisionFilter;
    }

    public boolean isIgnoreWhiteSpaces() {
        return ignoreWhiteSpaces;
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
