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
package org.netbeans.modules.web.livehtml.ui;

import java.text.DateFormat;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.filter.FilteredAnalysis;

/**
 *
 * @author petr-podzimek
 */
public class AnalysisItem implements Comparable<AnalysisItem> {
    
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    
    private Analysis analysis;
    private FilteredAnalysis filteredAnalysis;

    public AnalysisItem(Analysis analysis) {
        this.analysis = analysis;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public Analysis resolveAnalysis() {
        return isFilteredItem() ? filteredAnalysis : analysis;
    }
    
    public boolean isFilteredItem() {
        return filteredAnalysis != null;
    }

    public FilteredAnalysis getFilteredAnalysis() {
        return filteredAnalysis;
    }

    public void setFilteredAnalysis(FilteredAnalysis filteredAnalysis) {
        this.filteredAnalysis = filteredAnalysis;
    }
    
    public String getRevisionLabel(int selectedRevisionIndex) {
        StringBuilder revisionsLabel = new StringBuilder(String.valueOf(selectedRevisionIndex));
        revisionsLabel.append(" / ");
        revisionsLabel.append(resolveAnalysis().getRevisionsCount());
        
        if (isFilteredItem()) {
            revisionsLabel.append(" (");
            revisionsLabel.append(toParentAnalysisRevisionIndex(selectedRevisionIndex));
            revisionsLabel.append("/");
            revisionsLabel.append(analysis.getRevisionsCount());
            revisionsLabel.append(")");
        }
        
        return revisionsLabel.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (analysis.getFinished() == null) {
            sb.append(" * ");
        }
        
//        if (filteredAnalysis != null) {
//            sb.append(" F ");
//        }
        
        if (analysis.getSourceUrl() != null) {
            String sourceUrl = analysis.getSourceUrl().toExternalForm();
            if (sourceUrl.length() > 25) {
                sourceUrl = "..." + sourceUrl.substring(25);
            }
            sb.append(sourceUrl);
        }
        
        if (analysis.getCreated() != null) {
            sb.append(" - ");
            sb.append(DATE_FORMAT.format(analysis.getCreated()));
        }
        
        return sb.toString();
    }

    @Override
    public int compareTo(AnalysisItem o) {
        if (o == null || resolveAnalysis() == null) {
            return -1;
        }
        return -resolveAnalysis().compareTo(o.resolveAnalysis());
    }

    private Integer toParentAnalysisRevisionIndex(int revisionIndex) {
        if (revisionIndex < 0 || getFilteredAnalysis().getTimeStampsCount() <= revisionIndex) {
            return null;
        }
        final String timeStamp = getFilteredAnalysis().getTimeStamps().get(revisionIndex);
        
        if (timeStamp == null) {
            return null;
        }
        
        for (int i = 0; i < getAnalysis().getTimeStamps().size(); i++) {
            final String parentTimeStamp = getAnalysis().getTimeStamps().get(i);
            if (timeStamp.equals(parentTimeStamp)) {
                return i;
            }
        }
        
        return null;
    }
    
}
