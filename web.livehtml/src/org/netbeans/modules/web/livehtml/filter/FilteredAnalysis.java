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
package org.netbeans.modules.web.livehtml.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.AnalysisListener;
import org.netbeans.modules.web.domdiff.Change;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.StackTrace;

/**
 * 
 * @author petr-podzimek
 */
public class FilteredAnalysis extends Analysis {
    
    private final Analysis parentAnalysis;
    
    private final GroupedRevisions whiteSpaceGroupedRevisions = new GroupedRevisions();
    private final GroupedRevisions stackTraceGroupedRevisions = new GroupedRevisions();
    private final GroupedRevisions scriptLocationGroupedRevisions = new GroupedRevisions();

    private AnalysisListener analysisListener = new FilteredAnalysis.PrivateAnalystListener();
    
    private final boolean groupWhiteSpaces;
    private final boolean groupIdenticalStackTraces;
    private final List<String> groupScriptLocations;

    public FilteredAnalysis(List<String> groupScriptLocations, boolean groupIdenticalStackTraces, boolean groupWhiteSpaces, Analysis parentAnalysis) {
        super();
        assert parentAnalysis != null : "Parent analysis could not be null";
        this.parentAnalysis = parentAnalysis;
        this.groupScriptLocations = groupScriptLocations;
        this.groupWhiteSpaces = groupWhiteSpaces;
        this.groupIdenticalStackTraces = groupIdenticalStackTraces;

        setSourceUrl(parentAnalysis.getSourceUrl());
        parentAnalysis.addAnalysisListener(analysisListener);
        
        applyFilter();
    }

    /**
     * Reference to source {@link Analysis} for this filtered analysis.
     * @return Source {@link Analysis}. Can not be null.
     */
    public Analysis getParentAnalysis() {
        return parentAnalysis;
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
        
        // This code is specific for this class.
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
        
        final List<Integer> revisionIndexes = new ArrayList<Integer>();
        for (int i = 1; i < getParentAnalysis().getTimeStampsCount(); i++) {
            revisionIndexes.add(i);
        }
        
        List<GroupedRevisions> groupedRevisionsList = new ArrayList<GroupedRevisions>();
        
        // Update this instance GroupedRevisions
        groupedRevisionsList.add(getWhiteSpaceGroupedRevisions(revisionIndexes));
        groupedRevisionsList.add(getIdenticalStackTraceGroupedRevisions(revisionIndexes));
        groupedRevisionsList.add(getStackTraceGroupedRevisions(revisionIndexes));
        
        // GroupedRevisions can contain key values removed later during process - this will 
        FilteredAnalysis.fixRemovedRevisions(revisionIndexes, groupedRevisionsList);
        
        // revision with index 0 must be added from parent Analysis
        Revision revision0 = getParentAnalysis().getRevision(0);
        storeDocumentVersion(revision0, true);
        
        for (Integer revisionIndex : revisionIndexes) {
            final int timeStampsCount = getTimeStampsCount();
            int maxRevisionIndex = revisionIndex;
            for (GroupedRevisions groupedRevisions : groupedRevisionsList) {
                final Set<Integer> revisions = groupedRevisions.get(revisionIndex);
                
                // remove original indexes
                groupedRevisions.remove(revisionIndex);
                groupedRevisions.putRevisionIndexes(timeStampsCount, revisions);
                
                maxRevisionIndex = Math.max(maxRevisionIndex, FilterUtilities.safeMax(revisions));
            }
            
            Revision revision = getParentAnalysis().getRevision(maxRevisionIndex);
            storeDocumentVersion(revision, true);
        }
        
        makeFinished();
    }
    
    /**
     * Gets detail information about specified revision for UI.
     * @param revisionIndex index of revision to process.
     * @return Specified revision detail information.
     */
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

//TODO: In final code this could be removed.
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
        return scriptLocationGroupedRevisions.get(revisionIndex);
    }

    private boolean hasWhiteSpaceGroupedRevisions() {
        return whiteSpaceGroupedRevisions != null && !whiteSpaceGroupedRevisions.isEmpty();
    }

    private boolean hasStackTraceGroupedRevisions() {
        return stackTraceGroupedRevisions != null && !stackTraceGroupedRevisions.isEmpty();
    }

    private boolean hasScriptGroupedRevisions() {
        return scriptLocationGroupedRevisions != null && !scriptLocationGroupedRevisions.isEmpty();
    }

    public boolean isGroupWhiteSpaces() {
        return groupWhiteSpaces;
    }

    public boolean isGroupIdenticalStackTraces() {
        return groupIdenticalStackTraces;
    }

    public List<String> getGroupScriptLocations() {
        return groupScriptLocations;
    }

    /**
     * Gets {@link #whiteSpaceGroupedRevisions} grouped by White Spaces when value of {@link #isGroupWhiteSpaces()} property is <b>true</b>. 
     * Cleared {@link #whiteSpaceGroupedRevisions} is returned when value of {@link #isGroupWhiteSpaces()} property is <b>false</b>
     * @param revisionIndexes revision indexes of parent {@link Analysis} to process.
     * @return {@link #whiteSpaceGroupedRevisions}
     */
    private GroupedRevisions getWhiteSpaceGroupedRevisions(List<Integer> revisionIndexes) {
        whiteSpaceGroupedRevisions.clear();
        if (revisionIndexes == null || revisionIndexes.isEmpty() || !isGroupWhiteSpaces()) {
            return whiteSpaceGroupedRevisions;
        }
        
        Integer lastWhiteSpaceRevisionIndex = null;
        for (Iterator<Integer> it = revisionIndexes.iterator(); it.hasNext();) {
            Integer revisionIndex = it.next();
            final String timeStamp = getParentAnalysis().getTimeStamps().get(revisionIndex);
            final StringBuilder diffStr = getParentAnalysis().read(Analysis.DIFF, timeStamp);
            final List<Change> diff = Change.decodeFromJSON(diffStr == null ? null : diffStr.toString());
            
            if (isEmptyChanges(diff)) {
                if (lastWhiteSpaceRevisionIndex == null) {
                    lastWhiteSpaceRevisionIndex = revisionIndex;
                } else {
                    whiteSpaceGroupedRevisions.putRevisionIndex(lastWhiteSpaceRevisionIndex, revisionIndex);
                    it.remove();
                }
            } else {
                if (lastWhiteSpaceRevisionIndex != null) {
                    lastWhiteSpaceRevisionIndex = revisionIndex;
                }
            }

        }
        
        return whiteSpaceGroupedRevisions;
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
    
    /**
     * Gets {@link #stackTraceGroupedRevisions} grouped by identical JavaScript Stack Trace when value of {@link #isGroupIdenticalStackTraces()} property is <b>true</b>. 
     * Cleared {@link #stackTraceGroupedRevisions} is returned when value of {@link #isGroupIdenticalStackTraces()} property is <b>false</b>
     * @param revisionIndexes revision indexes of parent {@link Analysis} to process.
     * @return {@link #stackTraceGroupedRevisions}
     */
    private GroupedRevisions getIdenticalStackTraceGroupedRevisions(List<Integer> revisionIndexes) {
        stackTraceGroupedRevisions.clear();
        if (revisionIndexes == null || revisionIndexes.isEmpty() || !isGroupIdenticalStackTraces()) {
            return stackTraceGroupedRevisions;
        }
        return filterAndGroupByStackTrace(revisionIndexes, null, stackTraceGroupedRevisions);
    }

    /**
     * Gets {@link #scriptLocationGroupedRevisions} filtered by script location and grouped by identical JavaScript Stack Trace when value of {@link #getGroupScriptLocations()} is not null or empty. 
     * Gets cleared {@link #scriptLocationGroupedRevisions} when value of {@link #getGroupScriptLocations()} is null or empty. 
     * @param revisionIndexes revision indexes of parent {@link Analysis} to process.
     * @return {@link #scriptLocationGroupedRevisions}
     */
    private GroupedRevisions getStackTraceGroupedRevisions(List<Integer> revisionIndexes) {
        scriptLocationGroupedRevisions.clear();
        if (revisionIndexes == null || revisionIndexes.isEmpty() || getGroupScriptLocations() == null || getGroupScriptLocations().isEmpty()) {
            return scriptLocationGroupedRevisions;
        }
        return filterAndGroupByStackTrace(revisionIndexes, groupScriptLocations, scriptLocationGroupedRevisions);
    }

    /**
     * Gets groupedRevisions parameter filtered by script location and grouped by identical JavaScript Stack Trace when value of {@link #getGroupScriptLocations()} is not null or empty. 
     * Gets cleared {@link #scriptLocationGroupedRevisions} when value of {@link #getGroupScriptLocations()} is null or empty. 
     * @param revisionIndexes revision indexes of parent {@link Analysis} to process.
     * @param ignoreScriptLocations list of JavaScript locations to be ignored. 
     * @param groupedRevisions groupedRevisions to process.
     * @return 
     */
    private GroupedRevisions filterAndGroupByStackTrace(List<Integer> revisionIndexes, List<String> ignoreScriptLocations, GroupedRevisions groupedRevisions) {
        if (revisionIndexes == null || revisionIndexes.isEmpty()) {
            return groupedRevisions;
        }
        Integer lastGroupIndex = null;
        JSONArray lastStackTrace = null;
        for (Iterator<Integer> it = revisionIndexes.iterator(); it.hasNext();) {
            Integer revisionIndex = it.next();
            
            // previous revison Time Stamp from must be taken from Analysis class.
            final String timeStamp = getParentAnalysis().getTimeStamps().get(revisionIndex - 1); 
            final StringBuilder stackTraceStr = getParentAnalysis().read(Analysis.STACKTRACE, timeStamp);
            if (stackTraceStr == null) {
                continue;
            }
            
            JSONArray stacktrace = (JSONArray) JSONValue.parse(stackTraceStr.toString());
            JSONArray filteredStacktrace = stacktrace;
            if (ignoreScriptLocations != null) {
                filteredStacktrace = filterStackTrace(ignoreScriptLocations, stacktrace);
            }
            
            if (FilterUtilities.match(lastStackTrace, filteredStacktrace)) {
                groupedRevisions.putRevisionIndex(lastGroupIndex, revisionIndex);
                it.remove();
            } else {
                lastGroupIndex = revisionIndex;
                lastStackTrace = filteredStacktrace;
            }

        }
        return groupedRevisions;
    }

    private JSONArray filterStackTrace(List<String> ignoreScriptLocations, JSONArray jsonArray) {
        JSONArray filteredJSONArray = new JSONArray();
        
        boolean addAll = false;
        for (Object object : jsonArray) {
            if (object instanceof JSONObject) {
                JSONObject jSONObject = (JSONObject) object;
                final Object script = jSONObject.get(StackTrace.SCRIPT);
                String scriptLocation = null;
                if (script instanceof String) {
                    scriptLocation = (String) script;
                }
                
                if (!addAll && scriptLocation != null && !ignoreScriptLocations.contains(scriptLocation)) {
                    addAll = true;
                }
                if (addAll) {
                    filteredJSONArray.add(object);
                }
            }
        }
        return filteredJSONArray;
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

    private static void fixRemovedRevisions(List<Integer> indexes, Collection<GroupedRevisions> sources) {
        for (GroupedRevisions map : sources) {
            for (GroupedRevisions map1 : sources) {
                if (map != map1) {
                    map.fixRemovedRevisions(indexes, map1);
                }
            }
        }
    }

    /**
     * Map of grouped revisions where key is revision index and value is Set of revision index.
     */
    private class GroupedRevisions extends HashMap<Integer, Set<Integer>> {

        protected void putRevisionIndex(Integer groupIndex, Integer revisionIndex) {
            final Set<Integer> indexes = safeGetRevisionIndexes(groupIndex);
            indexes.add(revisionIndex);
        }

        protected void putRevisionIndexes(Integer groupIndex, Set<Integer> revisionIndexes) {
            if (revisionIndexes != null) {
                final Set<Integer> indexes = safeGetRevisionIndexes(groupIndex);
                indexes.addAll(revisionIndexes);
            }
        }

        private Set<Integer> safeGetRevisionIndexes(Integer groupIndex) {
            Set<Integer> indexes = get(groupIndex);
            if (indexes == null) {
                indexes = new HashSet<Integer>();
                put(groupIndex, indexes);
            }
            return indexes;
        }

        protected void fixRemovedRevisions(List<Integer> indexes, GroupedRevisions source) {
            Set<Integer> indexesToRemove = new HashSet<Integer>();
            for (Map.Entry<Integer, Set<Integer>> entry : entrySet()) {
                final Integer key = entry.getKey();
                if (!indexes.contains(key)) {
                    final Set<Integer> values = entry.getValue();
                    final Integer indexReplacement = source.getIndexReplacement(indexes, key);
                    if (indexReplacement != null) {
                        for (Integer value : values) {
                            putRevisionIndex(indexReplacement, value);
                        }
                        indexesToRemove.add(key);
                    }
                }
            }
            keySet().removeAll(indexesToRemove);
        }

        protected Integer getIndexReplacement(List<Integer> indexes, Integer index) {
            for (Map.Entry<Integer, Set<Integer>> entry : entrySet()) {
                final Integer key = entry.getKey();
                final Set<Integer> values = entry.getValue();
                if (values.contains(index) && indexes.contains(key)) {
                    return key;
                }
            }
            return null;
        }

    }

}
