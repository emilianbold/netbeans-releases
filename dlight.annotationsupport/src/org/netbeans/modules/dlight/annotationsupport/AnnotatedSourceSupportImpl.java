/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 *
 * @author thp
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport.class)
public class AnnotatedSourceSupportImpl implements AnnotatedSourceSupport {

    private final static Logger log = Logger.getLogger("dlight.annotationsupport"); // NOI18N
    private static boolean checkedLogging = checkLogging();
    private static boolean logginIsOn;
    private HashMap<String, FileAnnotationInfo> activeAnnotations = null;
    private static String SPACES = "            ";  // NOI18N

    public AnnotatedSourceSupportImpl() {
        enableSourceFileTracking();
    }

    public void updateSource(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> functionCalls) {
        log(sourceFileInfoProvider, metrics, functionCalls);
        if (activeAnnotations != null) {
            // un-annotate sources // FIXUP
        }
        activeAnnotations = new HashMap<String, FileAnnotationInfo>();
        for (FunctionCallWithMetric functionCall : functionCalls) {
            SourceFileInfo sourceFileInfo = sourceFileInfoProvider.getSourceFileInfo(functionCall);
            if (sourceFileInfo != null) {
                if (sourceFileInfo.isSourceKnown()) {
                    String filePath = sourceFileInfo.getFileName();
                    FileAnnotationInfo fileAnnotationInfo = activeAnnotations.get(filePath);
                    if (fileAnnotationInfo == null) {
                        fileAnnotationInfo = new FileAnnotationInfo();
                        fileAnnotationInfo.setFilePath(filePath);
                        fileAnnotationInfo.setColumnNames(new String[metrics.size()]);
                        fileAnnotationInfo.setMaxColumnWidth(new int[metrics.size()]);
                        activeAnnotations.put(filePath, fileAnnotationInfo);
                    }
                    LineAnnotationInfo lineAnnotationInfo = new LineAnnotationInfo(fileAnnotationInfo);
                    lineAnnotationInfo.setLine(sourceFileInfo.getLine());
                    lineAnnotationInfo.setOffset(sourceFileInfo.getOffset());
                    lineAnnotationInfo.setColumns(new String[metrics.size()]);
                    int col = 0;
                    for (Column column : metrics) {
                        String metricId = column.getColumnName();
                        Object metricVal = functionCall.getMetricValue(metricId);
                        String metricValString = metricVal.toString();
                        lineAnnotationInfo.getColumns()[col] = metricValString;
                        int metricValLength = metricValString.length();
                        if (fileAnnotationInfo.getMaxColumnWidth()[col] < metricValLength) {
                            fileAnnotationInfo.getMaxColumnWidth()[col] = metricValLength;
                        }

                        String metricUName = column.getColumnUName();
                        fileAnnotationInfo.getColumnNames()[col] = metricUName;
                        
                        col++;
                    }
                    fileAnnotationInfo.getLineAnnotationInfo().add(lineAnnotationInfo);
                }
            }
        }
        annotateCurrentSourceFiles();
    }

    private void enableSourceFileTracking() {
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(new MyPropertyChangeListener());
    }

    private File activatedFile(Node node) {
        DataObject dobj = node.getCookie(DataObject.class);
        if (dobj != null) {
            FileObject fo = dobj.getPrimaryFile();
            return FileUtil.toFile(fo);
        }
        return null;
    }

    private JEditorPane activatedEditorPane(Node node) {
        EditorCookie ec = node.getCookie(EditorCookie.class);
        if (ec == null) {
            return null;
        }

        JEditorPane[] panes = ec.getOpenedPanes();
        if (panes == null) {
            ec.open();
        }

        panes = ec.getOpenedPanes();
        if (panes == null) {
            return null;
        }
        JEditorPane currentPane = panes[0];
        return currentPane;
    }

    private void annotateCurrentSourceFiles() {
        Node[] nodes = WindowManager.getDefault().getRegistry().getCurrentNodes();
        if (nodes == null) {
            return;
        }
        for (final Node node : nodes) {
            File file = activatedFile(node);
            if (file != null) {
                final String filePath = file.getAbsolutePath();
                final FileAnnotationInfo fileAnnotationInfo = activeAnnotations.get(filePath);
                if (fileAnnotationInfo != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JEditorPane jEditorPane = activatedEditorPane(node);
                            if (!fileAnnotationInfo.isAnnotated()) {
                                fileAnnotationInfo.setEditorPane(jEditorPane);
                                // Calculate line numbers if only offset is known
                                log.fine("Annotating " + filePath + "\n"); // NOI18N)
                                List<LineAnnotationInfo> lines = fileAnnotationInfo.getLineAnnotationInfo();
                                for (LineAnnotationInfo line : lines) {
                                    //line.setLine(jEditorPane);
                                    log.fine("  " + line.getLine() + ":" + line.getAnnotation() + " [" + fileAnnotationInfo.getTooltip() + "]" + "\n"); // NOI18N)
                                }
                                fileAnnotationInfo.setAnnotated(true);
                            }
                            AnnotationBarManager.showAnnotationBar(jEditorPane, fileAnnotationInfo);
                        }
                    });
                }
            }
        }
    }

    class MyPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            annotateCurrentSourceFiles();
        }
    }

    private void log(SourceFileInfoDataProvider sourceFileInfoProvider, List<Column> metrics, List<FunctionCallWithMetric> functionCalls) {
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
            log.finest("  getFunction().getQuilifiedName() " + functionCall.getFunction().getQuilifiedName());

            SourceFileInfo sourceFileInfo = sourceFileInfoProvider.getSourceFileInfo(functionCall);
            if (sourceFileInfo != null) {
                if (sourceFileInfo.isSourceKnown()) {
                    log.finer(sourceFileInfo.getFileName() + "\n"); // NOI18N
                }
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

}
