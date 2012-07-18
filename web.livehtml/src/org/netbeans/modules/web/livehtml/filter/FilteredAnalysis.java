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
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.AnalysisListener;
import org.netbeans.modules.web.livehtml.Change;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.Utilities;
import org.netbeans.modules.web.livehtml.diff.Diff;

/**
 *
 * @author petr-podzimek
 */
public class FilteredAnalysis extends Analysis {
    
    private final Analysis parentAnalysis;
    private RevisionFilter revisionFilter;

    public FilteredAnalysis(Analysis parentAnalysis, File root, String initialContent) {
        super(root, initialContent);
        this.parentAnalysis = parentAnalysis;
    }

    @Override
    public Revision getRevision(int changeIndex, boolean reformatRevision) {
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
        
        StringBuilder editorContent;
        List<Change> changes;
        if (reformatRevision) {
            editorContent = beautifiedContent;
            changes = Change.decodeFromJSON(beautifiedDiff == null ? null : beautifiedDiff.toString());
        } else {
            editorContent = content;
            changes = Change.decodeFromJSON(diff == null ? null : diff.toString());
        }
        Revision rev = new Revision(editorContent, stackTrace, changes, data, timeStamp, changeIndex);
        
        return rev;
    }
    
    public Analysis getParentAnalysis() {
        return parentAnalysis;
    }

    public final void setRevisionFilter(RevisionFilter revisionFilter, boolean reformatRevision) {
        this.revisionFilter = revisionFilter;
        
        for (int i = 1; i < parentAnalysis.getTimeStampsCount(); i++) {
            final Revision revision = parentAnalysis.getRevision(i, reformatRevision);
            if (revision != null && (i == 0 || revisionFilter.match(revision))) {
                storeDocumentVersion(
                        revision.getTimeStamp(), 
                        revision.getContent() == null ? "null" : revision.getContent(), 
                        revision.getStacktrace() == null ? "null" : revision.getStacktrace().toJSONString(),
                        true);
            }
        }
        
        makeFinished();
    }

    public RevisionFilter getRevisionFilter() {
        return revisionFilter;
    }

//    @Override
//    public Revision getRevision(int changeIndex, boolean reformatRevision) {
//        final String filteredTimeStamp = getTimeStamps().get(changeIndex);
//        int timeStampIndex = -1;
//        for (int i = 0; i < parentAnalysis.getTimeStampsCount(); i++) {
//            String timeStamp = parentAnalysis.getTimeStamps().get(i);
//            if (filteredTimeStamp.equals(timeStamp)) {
//                timeStampIndex = i;
//            }
//        }
//        
//        if (timeStampIndex == -1) {
//            return null;
//        }
//        
//        return parentAnalysis.getRevision(timeStampIndex, reformatRevision);
//    }
//    
}
