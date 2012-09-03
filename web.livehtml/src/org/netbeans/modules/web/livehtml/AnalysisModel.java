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
package org.netbeans.modules.web.livehtml;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.web.livehtml.filter.FilteredAnalysis;

/**
 *
 * @author petr-podzimek
 */
public class AnalysisModel {
    
    private List<AnalysisModelListener> analysisModelListeners = new CopyOnWriteArrayList<AnalysisModelListener>();
    private URL sourceUrl = null;
    
    public AnalysisModel() {
        registerListeners();
    }

    public void addAnalysisModelListener(AnalysisModelListener analysisModelListener) {
        if (analysisModelListener != null && !analysisModelListeners.contains(analysisModelListener)) {
            analysisModelListeners.add(analysisModelListener);
        }
    }
    
    public void removeAnalysisModelListener(AnalysisModelListener analysisModelListener) {
        if (analysisModelListener != null) {
            analysisModelListeners.remove(analysisModelListener);
        }
    }
    
    private void fireAnalysisAdded(Analysis analysis) {
        for (AnalysisModelListener analysisModelListener : analysisModelListeners) {
            analysisModelListener.analysisAdded(analysis);
        }
    }
    
    private void fireAnalysisRemoved(Analysis analysis) {
        for (AnalysisModelListener analysisModelListener : analysisModelListeners) {
            analysisModelListener.analysisRemoved(analysis);
        }
    }
    
    public List<Analysis> getAnalyses() {
        List<Analysis> filteredAnalysises = new ArrayList<Analysis>();
        for (Analysis analysis : AnalysisStorage.getInstance().getStoredAnalyses()) {
            if (getSourceUrl() == null || getSourceUrl().equals(analysis.getSourceUrl())) {
                filteredAnalysises.add(analysis);
            }
        }
        return filteredAnalysises;
    }

    public Analysis resolveAnalysis(URL url) {
        return AnalysisStorage.getInstance().resolveAnalysis(url);
    }

    public FilteredAnalysis getFilteredAnalysis(Analysis parentAnalysis) {
        return AnalysisStorage.getInstance().getFilteredAnalysis(parentAnalysis);
    }
    
    public URL getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(URL sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    private void registerListeners() {
        AnalysisStorage.getInstance().addAnalysisStorageListener(new PrivateAnalysisStorageListener());
    }

    private class PrivateAnalysisStorageListener implements AnalysisStorageListener {
        
        @Override
        public void analysisAdded(Analysis analysis) {
            if (getSourceUrl() == null || getSourceUrl().equals(analysis.getSourceUrl())) {
                fireAnalysisAdded(analysis);
            }
        }

        @Override
        public void analysisRemoved(Analysis analysis) {
            if (getSourceUrl() == null || analysis.getSourceUrl().equals(getSourceUrl())) {
                fireAnalysisRemoved(analysis);
            }
        }

    }

}
