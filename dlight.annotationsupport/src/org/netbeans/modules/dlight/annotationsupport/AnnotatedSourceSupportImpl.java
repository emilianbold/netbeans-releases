/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.annotationsupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionMetricFormatter;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author thp
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport.class)
public class AnnotatedSourceSupportImpl implements AnnotatedSourceSupport {

    private final static Logger log = Logger.getLogger("dlight.annotationsupport"); // NOI18N
    private static boolean checkedLogging = checkLogging();
    private static boolean logginIsOn;
    private HashMap<String, FileAnnotationInfo> activeAnnotations = new HashMap<String, FileAnnotationInfo>();
    private static AnnotatedSourceSupportImpl instance = null;

    public AnnotatedSourceSupportImpl() {
//        WindowManager.getDefault().getRegistry().addPropertyChangeListener(new EditorFileChangeListener());
        AnnotationSupport.getInstance().addPropertyChangeListener(new ProfilerPropertyChangeListener());
        EditorRegistry.addPropertyChangeListener(new EditorFileChangeListener());
    }

    protected static AnnotatedSourceSupportImpl getInstance() {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(AnnotatedSourceSupportImpl.class);
        }
        return instance;
    }
    
    private String getCacheKey(final FileObject fileObject) {
        if (fileObject == null) {
            return null;
        }
        
        CharSequence url = FileSystemProvider.fileObjectToUrl(fileObject);
        return url == null ? null : url.toString();
    }
    
    private String getCacheKey(final String filePath) {
        FileObject fo = FileSystemProvider.urlToFileObject(filePath);
        String result = getCacheKey(fo);
        if (result != null) {
            return result;
        }
        return filePath.toString();
    }

    private synchronized void preProcessAnnotations(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> list, boolean lineAnnotations) {
        if (list == null || list.size() == 0) {
            return;
        }
        for (FunctionCallWithMetric functionCall : list) {
            SourceFileInfo sourceFileInfo = sourceFileInfoProvider.getSourceFileInfo(functionCall);
            if (sourceFileInfo != null) {
                if (sourceFileInfo.isSourceKnown()) {
                    String filePath = sourceFileInfo.getFileName();
                    String key = getCacheKey(filePath);
                    FileAnnotationInfo fileAnnotationInfo = activeAnnotations.get(key);
                    if (fileAnnotationInfo == null) {
                        fileAnnotationInfo = new FileAnnotationInfo();
                        fileAnnotationInfo.setFilePath(filePath);
                        fileAnnotationInfo.setColumnNames(new String[metrics.size()]);
                        fileAnnotationInfo.setMaxColumnWidth(new int[metrics.size()]);
                        activeAnnotations.put(key, fileAnnotationInfo);
                    }
                    LineAnnotationInfo lineAnnotationInfo = new LineAnnotationInfo(fileAnnotationInfo);
                    lineAnnotationInfo.setLine(sourceFileInfo.getLine());
                    lineAnnotationInfo.setOffset(sourceFileInfo.getOffset());
                    lineAnnotationInfo.setColumns(new String[metrics.size()]);
                    lineAnnotationInfo.setNotFormattedColumns(new String[metrics.size()]);
                    boolean below = true;
                    int col = 0;
                    for (Column column : metrics) {
                        String metricId = column.getColumnName();
                        Object metricVal = functionCall.getMetricValue(metricId);
                        String longFormattedMetricString = FunctionMetricFormatter.getLongFormattedValue(functionCall, metricId);
                        String metricValString = FunctionMetricFormatter.getFormattedValue(functionCall, metricId);
                        if (!metricValString.equals("0.0")) { // NOI18N
                            below = false;
                        }
                        lineAnnotationInfo.getColumns()[col] = metricValString;
                        lineAnnotationInfo.getNotFormattedColumns()[col] = longFormattedMetricString;
                        int metricValLength = metricValString.length();
                        if (fileAnnotationInfo.getMaxColumnWidth()[col] < metricValLength) {
                            fileAnnotationInfo.getMaxColumnWidth()[col] = metricValLength;
                        }

                        String metricUName = column.getColumnUName();
                        fileAnnotationInfo.getColumnNames()[col] = metricUName;

                        col++;
                    }
                    if (lineAnnotations && !below) {
                        // line annotation (none zero)
                        fileAnnotationInfo.addLineAnnotationInfo(lineAnnotationInfo);
                    }
                    if (!lineAnnotations) {
                        // block annotation
                        fileAnnotationInfo.addBlockAnnotationInfo(lineAnnotationInfo);
                    }
                }
            }
        }
    }

    public synchronized FileAnnotationInfo getFileAnnotationInfo(String filePath) {
        return activeAnnotations.get(getCacheKey(filePath));
    }

    public synchronized void updateSource(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> list, List<FunctionCallWithMetric> functionCalls) {
        // log(sourceFileInfoProvider, metrics, list, functionCalls);
        // Remember list of annotated panes
        HashSet<JEditorPane> previousAnnotatedPanes = new HashSet<JEditorPane>();
        if (activeAnnotations != null) {
            for (FileAnnotationInfo fileAnnotationInfo : activeAnnotations.values()) {
                if (fileAnnotationInfo.isAnnotated()) {
                    previousAnnotatedPanes.add(fileAnnotationInfo.getEditorPane());
                }
            }
        }

        activeAnnotations = new HashMap<String, FileAnnotationInfo>();
        preProcessAnnotations(sourceFileInfoProvider, metrics, list, true);
        preProcessAnnotations(sourceFileInfoProvider, metrics, functionCalls, false);
        // Check current focused file in editor whether it should be annotated
        annotateCurrentFocusedFiles();

        // Now un-annotate all previous annotated panes except for the ones that that just have been annotated
        for (FileAnnotationInfo fileAnnotationInfo : activeAnnotations.values()) {
            if (fileAnnotationInfo.isAnnotated()) {
                previousAnnotatedPanes.remove(fileAnnotationInfo.getEditorPane());
            }
        }
        for (JEditorPane pane : previousAnnotatedPanes) {
//            SwingUtilities.invokeLater(new UnAnnotate(pane));
            new UnAnnotate(pane).run();
        }
    }

    private FileObject getFileObjectFromEditorPane(JTextComponent jEditorPane) {
        if (jEditorPane != null) {
            Document doc = jEditorPane.getDocument();
            if (doc != null) {
                Object source = doc.getProperty(Document.StreamDescriptionProperty);

                if (source instanceof DataObject) {
                    FileObject fo = ((DataObject) source).getPrimaryFile();
                    return fo;
                }
            }
        }
        return null;
    }
    
    private synchronized void annotateCurrentFocusedFiles() {
        // FIXUP: could there be more than one file in view?
        if (activeAnnotations.size() == 0) {
            return;
        }
        JTextComponent jEditorPane = EditorRegistry.focusedComponent();
        if (jEditorPane == null) {
            jEditorPane = EditorRegistry.lastFocusedComponent();
        }
        if (jEditorPane != null) {
            FileObject fileObject = getFileObjectFromEditorPane(jEditorPane);
            String key = getCacheKey(fileObject);
            if (key != null) {
                FileAnnotationInfo fileAnnotationInfo = activeAnnotations.get(key);

                if (fileAnnotationInfo != null) {
//                    if (!fileAnnotationInfo.isAnnotated()) {
                    fileAnnotationInfo.setEditorPane((JEditorPane) jEditorPane);
                    fileAnnotationInfo.setAnnotated(true);
//                    }
//                    SwingUtilities.invokeLater(new Annotate(jEditorPane, fileAnnotationInfo));
                    new Annotate(jEditorPane, fileAnnotationInfo).run();
                }
            }
        }
    }

    private static class HideAnnotate implements Runnable {
        JTextComponent jEditorPane;

        public HideAnnotate(JTextComponent jEditorPane) {
            this.jEditorPane = jEditorPane;
        }

        public void run() {
            AnnotationBarManager.hideAnnotationBar(jEditorPane);
        }
    }

    private static class UnAnnotate implements Runnable {
        JTextComponent jEditorPane;

        public UnAnnotate(JTextComponent jEditorPane) {
            this.jEditorPane = jEditorPane;
        }

        public void run() {
            AnnotationBarManager.unAnnotate(jEditorPane);
        }
    }

    private static class Annotate implements Runnable {
        JTextComponent jEditorPane;
        FileAnnotationInfo fileAnnotationInfo;

        public Annotate(JTextComponent jEditorPane, FileAnnotationInfo fileAnnotationInfo) {
            this.jEditorPane = jEditorPane;
            this.fileAnnotationInfo = fileAnnotationInfo;
        }

        public void run() {
            AnnotationBarManager.showAnnotationBar(jEditorPane, fileAnnotationInfo);
        }
    }

    private class EditorFileChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
                DLightExecutorService.submit(new Runnable() {

                    public void run() {
                        annotateCurrentFocusedFiles();
                    }
                }, "Annotate current focused file");//NOI18N
            }
        }
    }

    private class ProfilerPropertyChangeListener implements PropertyChangeListener {

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if (prop.equals(AnnotationSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE)) {
                boolean annotate = AnnotationSupport.getInstance().getTextAnnotationVisible();
                if (annotate) {
                    for (FileAnnotationInfo fileAnnotationInfo : activeAnnotations.values()) {
                        if (fileAnnotationInfo.isAnnotated()) {
//                            SwingUtilities.invokeLater(new Annotate(fileAnnotationInfo.getEditorPane(), fileAnnotationInfo));
                            new Annotate(fileAnnotationInfo.getEditorPane(), fileAnnotationInfo).run();
                        }
                    }
                } else {
                    for (FileAnnotationInfo fileAnnotationInfo : activeAnnotations.values()) {
                        if (fileAnnotationInfo.isAnnotated()) {
//                            SwingUtilities.invokeLater(new UnAnnotate(fileAnnotationInfo.getEditorPane()));
                            new HideAnnotate(fileAnnotationInfo.getEditorPane()).run();
                        }
                    }
                }
            }
        }
    }

    private void log(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> functionCalls, boolean lineAnnotations) {
        if (!logginIsOn) {
            return;
        }
        log.fine("AnnotatedSourceSupportImpl.updateSource");
        log.finest("metrics:");
        for (Column column : metrics) {
            log.finest("  getColumnLongUName " + column.getColumnLongUName());
            log.finest("  getColumnName " + column.getColumnName());
            log.finest("  getColumnUName " + column.getColumnUName());
            log.finest("  getExpression = " + column.getExpression());
            log.finest("");
        }
        log.finest("functionCalls:");
        for (FunctionCallWithMetric functionCall : functionCalls) {
            log.finest("  getDisplayedName " + functionCall.getDisplayedName());
            log.finest("  getFunction " + functionCall.getFunction());
            log.finest("  getFunction().getName() " + functionCall.getFunction().getName());
            log.finest("  getFunction().getQuilifiedName() " + functionCall.getFunction().getSignature());

            SourceFileInfo sourceFileInfo = sourceFileInfoProvider.getSourceFileInfo(functionCall);
            if (sourceFileInfo != null) {
                if (sourceFileInfo.isSourceKnown()) {
                    log.finer(sourceFileInfo.getFileName() + "\n"); // NOI18N
                }
                log.finer("  type=" + (lineAnnotations ? "Line" : "Block") + "\n"); // NOI18N);
                log.finer("  line=" + sourceFileInfo.getLine() + "\n"); // NOI18N);
                log.finer("  column=" + sourceFileInfo.getColumn() + "\n"); // NOI18N););
                log.finer("  offset=" + sourceFileInfo.getOffset() + "\n"); // NOI18N););
                for (Column column : metrics) {
                    String metricId = column.getColumnName();
                    Object metricVal = functionCall.getMetricValue(metricId);
                    String metricUName = column.getColumnUName();
                    log.finer("  " + metricUName + "=" + metricVal + "\n"); // NOI18N
                }
            }
            log.finest("  " + functionCall);
        }
    }

    private static boolean checkLogging() {
        if (checkedLogging) {
            return true;
        }
        logginIsOn = false;
        String logProp = System.getProperty("dlight.annotationsupport"); // NOI18N
        if (logProp != null) {
            logginIsOn = true;
            if (logProp.equals("FINE")) { // NOI18N
                log.setLevel(Level.FINE);
            } else if (logProp.equals("FINER")) { // NOI18N
                log.setLevel(Level.FINER);
            } else if (logProp.equals("FINEST")) { // NOI18N
                log.setLevel(Level.FINEST);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for(Map.Entry<String, FileAnnotationInfo> entry : activeAnnotations.entrySet()) {
            buf.append(entry.getKey()).append('=').append(entry.getValue()).append('\n'); // NOI18N
        }
        return buf.toString();
    }
}
