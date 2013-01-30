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
import java.lang.String;
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
        StringBuilder previewContent = read(NO_JS_CONTENT, timeStamp);
        
        Revision revision = new Revision(
                changeIndex,
                timeStamp,
                content,
                beautifiedContent,
                Change.decodeFromJSON(diff == null ? null : diff.toString()), 
                Change.decodeFromJSON(beautifiedDiff == null ? null : beautifiedDiff.toString()), 
                stackTrace,
                data,
                previewContent);
        
        return revision;
    }

    private void applyFilter() {
        getTimeStamps().clear();
        final List<Integer> revisionIndexes = new ArrayList<Integer>();

        if (isGroupWhiteSpaces()) {
            getWhiteSpaceGroupedRevisions(revisionIndexes);
        }
        if (isGroupIdenticalStackTraces()) {
            getGroupByStackTraceRevisions(revisionIndexes, Collections.<String>emptyList());
        }
        if (getGroupScriptLocations() != null && getGroupScriptLocations().size() > 0) {
            getGroupByStackTraceRevisions(revisionIndexes, groupScriptLocations);
        }
        
        Analysis a = getParentAnalysis();
        for (int i=0; i < a.getTimeStampsCount(); i++) {
            if (revisionIndexes.contains(i) && i+1 != a.getTimeStampsCount()) {
                assert i != 0 : "first revision should never be removed";
                continue;
            }
            Revision revision = getParentAnalysis().getRevision(i);
            storeDocumentVersion(revision, true);
        }
        makeFinished();
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

    public boolean isGroupWhiteSpaces() {
        return groupWhiteSpaces;
    }

    public boolean isGroupIdenticalStackTraces() {
        return groupIdenticalStackTraces;
    }

    public List<String> getGroupScriptLocations() {
        return groupScriptLocations;
    }

    public void getWhiteSpaceGroupedRevisions(List<Integer> revisionIndexes) {
        Analysis a = getParentAnalysis();
        // first and second revision will never have any diff:
        for (int i=2; i < a.getRevisionsCount(); i++) {
            if (revisionIndexes.contains(i)) {
                continue;
            }
            String timeStamp = a.getTimeStamps().get(i);
            StringBuilder diffStr = getParentAnalysis().read(Analysis.DIFF, timeStamp);
            List<Change> diff = Change.decodeFromJSON(diffStr == null ? null : diffStr.toString());
            if (isEmptyChanges(diff)) {
                revisionIndexes.add(i);
            }
        }
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
    
    public void getGroupByStackTraceRevisions(List<Integer> revisionIndexes, List<String> ignoreScriptLocations) {
        Analysis a = getParentAnalysis();
        JSONArray previousStacktrace = null;
        for (int i=1; i < a.getRevisionsCount(); i++) {
            if (revisionIndexes.contains(i)) {
                continue;
            }
            String timeStamp = a.getTimeStamps().get(i);
            StringBuilder stackTraceStr = a.read(Analysis.STACKTRACE, timeStamp);
            JSONArray stacktrace = (JSONArray) JSONValue.parse(stackTraceStr.toString());
            stacktrace = filterStackTrace(ignoreScriptLocations, stacktrace);
            if (previousStacktrace == null) {
                previousStacktrace = stacktrace;
                continue;
            }
            if (FilterUtilities.match(previousStacktrace, stacktrace)) {
                revisionIndexes.add(i);
            }
            previousStacktrace = stacktrace;
        }
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
            applyFilter();
        }

    }

}
