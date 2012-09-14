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

import org.netbeans.modules.web.domdiff.Change;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.netbeans.modules.html.editor.lib.api.HtmlParseResult;
import org.netbeans.modules.html.editor.lib.api.HtmlParser;
import org.netbeans.modules.html.editor.lib.api.HtmlParserFactory;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.web.domdiff.Diff;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author petr-podzimek
 */
public class Analysis implements Comparable<Analysis> {
    
    private static RequestProcessor REQUEST_PROCESSOR = new RequestProcessor("Live HTML - analysis", 1);
    
    public static final String CONTENT = "content";
    public static final String DIFF = "diff";
    public static final String BDIFF = "bdiff";
    public static final String BCONTENT = "bcontent";
    public static final String STACKTRACE = "stacktrace";
    public static final String DATA = "data";
    
    public static final String NO_JS_CONTENT = "no_js_content";
    
    private URL sourceUrl;
    private Date created = new Date();
    private Date finished = null;

    private File rootDirectory;
    private List<String> timeStamps = new ArrayList<String>();
    
    private String dataToStore = null;
    private Diff lastDiff;
    private boolean lastChangeWasNotReal = false;
    
    private List<AnalysisListener> analysisListeners = new CopyOnWriteArrayList<AnalysisListener>();
    
    public Analysis() {
        this(AnalysisStorage.getInstance().getChangesStorageRoot(AnalysisStorage.getInstance().getStorageRoot()));
    }

    protected Analysis(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void store(String type, String timestamp, String content) {
        if (content == null) {
            return;
        }
        File storeFile = new File(getRootDirectory(), timestamp + "." + type);
        storeFile.deleteOnExit();
        try {
            assert !storeFile.exists() : "File should not exist yet! storeFile=" + storeFile;
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(storeFile)));
            ZipEntry entry = new ZipEntry(type);
            zos.putNextEntry(entry);
            byte b[] = content.getBytes();
            zos.write(b, 0, b.length);
            zos.flush();
            zos.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public Revision getRevision(int changeIndex) {
        if (changeIndex >= getTimeStampsCount()) {
            return null;
        }
        final String timeStamp = getTimeStamps().get(changeIndex);
        StringBuilder content = read(CONTENT, timeStamp);
        
        StringBuilder stacktrace = null;
        StringBuilder data = null;
        StringBuilder diff = read(DIFF, timeStamp);
        StringBuilder beautifiedDiff = read(BDIFF, timeStamp);
        StringBuilder beautifiedContent = read(BCONTENT, timeStamp);
        StringBuilder previewContent = read(NO_JS_CONTENT, timeStamp);
        
        //TODO: Rewrite this part of code to read data of current timesamp only!
        if (changeIndex > 0) {
            stacktrace = read(STACKTRACE, getTimeStamps().get(changeIndex - 1));
            data = read(DATA, getTimeStamps().get(changeIndex - 1));
        }
        
        Revision revision = new Revision(
                changeIndex,
                timeStamp,
                content,
                beautifiedContent,
                Change.decodeFromJSON(diff == null ? null : diff.toString()), 
                Change.decodeFromJSON(beautifiedDiff == null ? null : beautifiedDiff.toString()), 
                stacktrace,
                data, 
                previewContent);
        
        return revision;
    }
    
    public StringBuilder read(String type, String timestamp) {
        File storeFile = new File(getRootDirectory(), timestamp + "." + type);
        if (!storeFile.exists()) {
            return null;
        }
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(storeFile)));
            ZipEntry entry;
            while ( (entry = zis.getNextEntry()) != null ) {
                if( entry.getName().equals(type) ) {
                    return Utilities.fetchFileContent(zis);
                }
            }
            throw new RuntimeException("Should never happen");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public URL getSourceUrl() {
        return sourceUrl;
    }

    protected void setSourceUrl(URL sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Date getCreated() {
        return created;
    }

    public Date getFinished() {
        return finished;
    }

    public void makeFinished() {
        this.finished = new Date();
    }

    @Override
    public int compareTo(Analysis o) {
        if (o == null || o.getSourceUrl() == null || o.getCreated() == null) {
            return -1;
        }
        
        if (getSourceUrl() == null || getCreated() == null) {
            return 1;
        }
        
        final int createdResult = getCreated().compareTo(o.getCreated());
        if (createdResult == 0) {
            final int sourceUrlResult = getSourceUrl().toExternalForm().compareTo(o.getSourceUrl().toExternalForm());
            return sourceUrlResult;
        }
        
        return createdResult;
    }
    
    public int getRevisionsCount() {
        if (getTimeStamps() == null) {
            return 0;
        }
        return getTimeStamps().size() - 1;
    }

    public int getTimeStampsCount() {
        if (getTimeStamps() == null) {
            return 0;
        }
        return getTimeStamps().size();
    }
    
    public void storeDataEvent(long timestamp, String data, String request, String mime) {
        String dd = "Request "+request +" produced "+mime+" data:\n" + data;
        if (dataToStore == null) {
            dataToStore = dd;
        } else {
            dataToStore += "\n\n"+dd;
        }
    }
    
    protected void addTimeStamp(String timestamp) {
        timeStamps.add(timestamp);
        fireRevisionAdded(timestamp);
    }
    
//TODO: remove comment when this method is needed.
//    public void deleteFiles() {
//        for (File f : root.listFiles()) {
//            f.delete();
//        }
//        root.delete();
//    }
    
    public List<String> getTimeStamps() {
        return timeStamps;
    }

    private String getDataToStore() {
        return dataToStore;
    }

    public void setDataToStore(String dataToStore) {
        this.dataToStore = dataToStore;
    }

    public boolean isLastChangeWasNotReal() {
        return lastChangeWasNotReal;
    }

    public void setLastChangeWasNotReal(boolean lastChangeWasNotReal) {
        this.lastChangeWasNotReal = lastChangeWasNotReal;
    }

    protected File getRootDirectory() {
        return rootDirectory;
    }

    private Diff getLastDiff() {
        return lastDiff;
    }

    private void setLastDiff(Diff lastDiff) {
        this.lastDiff = lastDiff;
    }

    public void addAnalysisListener(AnalysisListener analysisListener) {
        if (analysisListener != null && !analysisListeners.contains(analysisListener)) {
            this.analysisListeners.add(analysisListener);
        }
    }

    public void removeAnalysisListener(AnalysisListener analysisListener) {
        if (analysisListener != null) {
            this.analysisListeners.remove(analysisListener);
        }
    }
    
    private void fireRevisionAdded(String timeStamp) {
        for (AnalysisListener analysisListener : analysisListeners) {
            analysisListener.revisionAdded(this, timeStamp);
        }
    }

    protected void storeDocumentVersion(final String timestamp, final String content, final String stackTrace, final boolean realChange) {
        storeDocumentVersion(timestamp, content, stackTrace, getDataToStore(), realChange);
    }

    protected void storeDocumentVersion(final String timestamp, final String content, final String stackTrace, final String data, final boolean realChange) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                assert (realChange ? true : !isLastChangeWasNotReal()) : "reject multiple consequent changes which are not realChange";
                if (isLastChangeWasNotReal() && realChange) {
                    // forget about last change and replace it with the change coming:
                    getTimeStamps().remove(getTimeStamps().size() - 1);
                    setLastChangeWasNotReal(false);
                }
                if (realChange) {
                    setDataToStore(null);
                } else {
                    setLastChangeWasNotReal(true);
                }
                store(CONTENT, timestamp, content);
                if (realChange) {
                    store(STACKTRACE, timestamp, stackTrace);
                    if (data != null) {
                        store(DATA, timestamp, data);
                    }
                }
                int total = getTimeStampsCount();
                
                if (total > 0) {
                    parse(content, timestamp, realChange, total-1, getTimeStamps().get(getTimeStampsCount() - 1));
                }
                addTimeStamp(timestamp);
            }
        };
        if (AnalysisStorage.isUnitTesting) {
            r.run();
        } else {
            REQUEST_PROCESSOR.post(r);
        }
    }
    
    private void parse(String content, String timestamp, boolean realChange, int previousChangeIndex, String previousTimestamp) {
        StringBuilder previousContent = read(CONTENT, previousTimestamp);
        HtmlParser parser = HtmlParserFactory.findParser(HtmlVersion.getDefaultVersion());
        try {
            HtmlSource s1 = new HtmlSource(previousContent);
            HtmlSource s2 = new HtmlSource(content);
            HtmlParseResult previousResult = parser.parse(s1, HtmlVersion.getDefaultVersion(), AnalysisStorage.getParserLookup());
            HtmlParseResult currentResult = parser.parse(s2, HtmlVersion.getDefaultVersion(), AnalysisStorage.getParserLookup());
            Diff d = new Diff(s1, s2, 
                    (OpenTag)previousResult.root().children().iterator().next(), 
                    (OpenTag)currentResult.root().children().iterator().next());
            List<Change> changes = d.compare(getLastDiff(), previousChangeIndex);
            store(DIFF, timestamp, Change.encodeToJSON(changes));
            
            List<Change> beautifiedChanges = Change.decodeFromJSON(Change.encodeToJSON(changes));
            StringBuilder beautifiedContent = ReformatSupport.reformat(
                    new HtmlSource(content), 
                    (OpenTag)currentResult.root().children().iterator().next(), 
                    beautifiedChanges);
            store(BDIFF, timestamp, Change.encodeToJSON(beautifiedChanges));
            store(BCONTENT, timestamp, beautifiedContent.toString());

            // XXX should reuse already parsed tree instead of creating token hierarchy here:
            Map<Integer, Integer> indexesOfJavaScript = ReformatSupport.getIndexesOfJavaScript(content);
            StringBuilder replaceBySpaces = ReformatSupport.replaceBySpaces(indexesOfJavaScript, new StringBuilder(content));
            store(Analysis.NO_JS_CONTENT, timestamp, replaceBySpaces.toString());
            
            if (realChange) {
                setLastDiff(d);
            }
            
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot parse " + getRootDirectory().getAbsolutePath() + " [" + timestamp + "," + previousTimestamp + "]", t);
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.sourceUrl != null ? this.sourceUrl.hashCode() : 0);
        hash = 37 * hash + (this.created != null ? this.created.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Analysis other = (Analysis) obj;
        if (this.sourceUrl != other.sourceUrl && (this.sourceUrl == null || !this.sourceUrl.equals(other.sourceUrl))) {
            return false;
        }
        if (this.created != other.created && (this.created == null || !this.created.equals(other.created))) {
            return false;
        }
        return true;
    }
    
    protected void clearTimeStamps() {
        timeStamps.clear();
    }
    
    protected void cleatDataToStore() {
        dataToStore = null;
    }
    
    protected void clearLastDiff() {
        lastDiff = null;
    }
    
    protected void clearLastChangeWasNotReal() {
        lastChangeWasNotReal = false;
    }

    protected static RequestProcessor getRequestProcessor() {
        return REQUEST_PROCESSOR;
    }
    
    @Override
    public String toString() {
        return "Analysis{" + "created=" + created;
    }
    
}
