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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.web.livehtml.filter.FilteredAnalysis;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author petr-podzimek
 */
public class AnalysisStorage {
    
    private static AnalysisStorage instance;
    
    /**
     * NOTE: Current code works only when true.
     */
    public static boolean isUnitTesting = true;
    
    private List<Analysis> storedAnalyses = new CopyOnWriteArrayList<Analysis>();
    private File storageRoot;
    private List<AnalysisStorageListener> analysisStorageListeners = new CopyOnWriteArrayList<AnalysisStorageListener>();

    public static synchronized AnalysisStorage getInstance() {
        if (instance == null) {
            instance = new AnalysisStorage();
        }
        return instance;
    }

    public final void addAnalysisStorageListener(AnalysisStorageListener analysisStorageListener) {
        if (analysisStorageListener != null && !analysisStorageListeners.contains(analysisStorageListener)) {
            analysisStorageListeners.add(analysisStorageListener);
        }
    }
    
    public final void removeAnalysisStorageListener(AnalysisStorageListener analysisStorageListener) {
        if (analysisStorageListener != null) {
            analysisStorageListeners.remove(analysisStorageListener);
        }
    }
    
    private void fireAnalysisAdded(Analysis analysis) {
        for (AnalysisStorageListener analysisStorageListener : analysisStorageListeners) {
            analysisStorageListener.analysisAdded(analysis);
        }
    }
    
    private void fireAnalysisRemoved(Analysis analysis) {
        for (AnalysisStorageListener analysisStorageListener : analysisStorageListeners) {
            analysisStorageListener.analysisRemoved(analysis);
        }
    }
    
    public static Lookup getParserLookup() {
        Properties p = new Properties();
        p.setProperty("add_text_nodes", "true");
        return Lookups.fixed(p);
    }
    
    public File getChangesStorageRoot(File liveHTMLRoot) {
        long fileHash = System.currentTimeMillis();
        File f = new File(liveHTMLRoot, Long.toString(fileHash % 173 + 172));   // NOI18N
        if (!f.exists()) {
            f.mkdirs();
        }
        while (true) {
            File ff = new File(f, Long.toString(fileHash));
            if (ff.exists()) {
                fileHash++;
            } else {
                ff.mkdirs();
                return ff;
            }
        }
    }

    private File getTempDirectory() {
        File f;
        try {
            f = File.createTempFile("livehtml", "test");
            f.deleteOnExit();
        } catch (IOException ex) {
            throw new RuntimeException("cannot create temp file", ex);
        }
        final File file = new File(f.getParentFile(), "livehtml");
        file.deleteOnExit();
        return file;
    }

    public synchronized File getStorageRoot() {
        if (storageRoot == null) {
            storageRoot = getTempDirectory();
        }
        return storageRoot;
    }

    private void addAnalysis(Analysis analysis) {
        if (analysis != null) {
            getStoredAnalyses().add(analysis);
            fireAnalysisAdded(analysis);
        }
    }
    
    private void removeAnalysis(Analysis analysis) {
        if (analysis != null) {
            getStoredAnalyses().remove(analysis);
            fireAnalysisRemoved(analysis);
        }
    }
    
    public synchronized Analysis resolveAnalysis(URL url) {
        if (url == null) {
            return null;
        }
        
        Analysis analysis = getAnalysis(url);
        if (analysis == null || analysis.getFinished() != null) {
            
            makeAllAsFinished();
            
            analysis = new Analysis();
            analysis.setSourceUrl(url);

            addAnalysis(analysis);
        }
        return analysis;
    }
    
    public synchronized Analysis addFilteredAnalysis(FilteredAnalysis filteredAnalysis) {
        removeAnalysis(getFilteredAnalysis(filteredAnalysis.getParentAnalysis()));
        
        addAnalysis(filteredAnalysis);
        
        return filteredAnalysis;
    }

    public List<Analysis> getStoredAnalyses() {
        return storedAnalyses;
    }
    
    private Analysis getAnalysis(URL url) {
        if (url == null) {
            return null;
        }
        
        Analysis selectedAnalysis = null;
        for (Analysis analysis : getStoredAnalyses()) {
            if (analysis.getClass() == Analysis.class && 
                    url.equals(analysis.getSourceUrl())) {
                selectedAnalysis = analysis;
            }
        }
        
        return selectedAnalysis;
    }
    
    private void makeAllAsFinished() {
        for (Analysis analysis : getStoredAnalyses()) {
            if (analysis.getFinished() == null) {
                analysis.makeFinished();
            }
        }
    }
    
    public FilteredAnalysis getFilteredAnalysis(Analysis parentAnalysis) {
        if (parentAnalysis == null) {
            return null;
        }
        for (Analysis analysis : getStoredAnalyses()) {
            if (analysis instanceof FilteredAnalysis) {
                FilteredAnalysis filteredAnalysis = (FilteredAnalysis) analysis;
                if (parentAnalysis == filteredAnalysis.getParentAnalysis()) {
                    return filteredAnalysis;
                }
            }
        }
        return null;
    }

}
