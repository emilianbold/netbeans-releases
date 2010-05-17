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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.codecoverage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Manage code coverage collection; delegate to providers, etc.
 *
 * @author Tor Norbye
 */
public class CoverageManagerImpl implements CoverageManager {
    public static final String COVERAGE_INSTANCE_FILE = "coverage.instance"; // NOI18N
    private static final String MIME_TYPE = "mimeType"; // NOI18N
    private static final String COVERAGE_DOC_PROPERTY = "coverage"; // NOI18N
    private final static String PREF_EDITOR_BAR = "editorBar"; // NOI18N
    private Set<String> enabledMimeTypes = new HashSet<String>();
    private Map<Project, CoverageReportTopComponent> showingReports = new HashMap<Project, CoverageReportTopComponent>();
    private Boolean showEditorBar;

    static CoverageManagerImpl getInstance() {
        return (CoverageManagerImpl) CoverageManager.INSTANCE;
    }

    public void setEnabled(final Project project, final boolean enabled) {
        final CoverageProvider provider = getProvider(project);
        if (provider == null) {
            return;
        }

        final Set<String> mimeTypes = provider.getMimeTypes();
        if (enabled) {
            enabledMimeTypes.addAll(mimeTypes);
        } else {
            enabledMimeTypes.removeAll(mimeTypes);
        }

        provider.setEnabled(enabled);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                resultsUpdated(project, provider);

                if (!enabled) {
                    for (JTextComponent target : EditorRegistry.componentList()) {
                        Document document = target.getDocument();
                        CoverageSideBar sb = CoverageSideBar.getSideBar(document);
                        if (sb != null) {
                            sb.showCoveragePanel(false);
                        }
                    }
                }
            }
        });
    }

    public boolean isAggregating(Project project) {
        CoverageProvider provider = getProvider(project);
        if (provider != null) {
            return provider.isAggregating();
        }

        return false;
    }

    public void setAggregating(Project project, boolean aggregating) {
        CoverageProvider provider = getProvider(project);
        if (provider == null) {
            return;
        }

        provider.setAggregating(aggregating);
    }

    void focused(FileObject fo, JTextComponent target) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            CoverageProvider provider = getProvider(project);
            if (provider != null && provider.isEnabled()) {
                try {
                    EditorCookie ec = DataLoadersBridge.getDefault().getCookie(fo, EditorCookie.class);
                    if (ec != null) {
                        Document doc = ec.getDocument();
                        if (doc == null) {
                            return;
                        }

                        doc.putProperty(COVERAGE_DOC_PROPERTY, null);
                        CoverageHighlightsContainer container = CoverageHighlightsLayerFactory.getContainer(doc);
                        if (container != null) {
                            container.refresh();
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    void showFile(Project project, FileCoverageSummary result) {
        FileObject fo = result.getFile();
        if (fo == null) {
            String display = result.getDisplayName();
            File file = new File(display);
            if (file.exists()) {
                fo = FileUtil.toFileObject(file);
            } else {
                fo = project.getProjectDirectory().getFileObject(display.replace('\\', '/'));
            }
        }
        if (fo != null) {
            GsfUtilities.open(fo, -1, null);
        }
    }

    static CoverageProvider getProvider(Project project) {
        if (project != null) {
            return project.getLookup().lookup(CoverageProvider.class);
        } else {
            return null;
        }
    }

    public boolean isEnabled(Project project) {
        CoverageProvider provider = getProvider(project);
        if (provider != null) {
            return provider.isEnabled();
        }

        return false;
    }

    public boolean isEnabled(String mimeType) {
        return enabledMimeTypes.contains(mimeType);
    }

    FileCoverageDetails getDetails(Project project, FileObject fileObject, Document doc) {
        if (project != null) {
            CoverageProvider provider = getProvider(project);
            if (provider != null && provider.isEnabled()) {
                FileCoverageDetails hitCounts = (FileCoverageDetails) doc.getProperty(COVERAGE_DOC_PROPERTY);
                if (hitCounts == null) {
                    hitCounts = provider.getDetails(fileObject, doc);
                    doc.putProperty(COVERAGE_DOC_PROPERTY, hitCounts);

                    if (getShowEditorBar()) {
                        CoverageSideBar sb = CoverageSideBar.getSideBar(doc);
                        if (sb != null) {
                            sb.showCoveragePanel(true);
                            sb.setCoverage(hitCounts);
                        }
                    }

                    return hitCounts;
                }

                return hitCounts;
            }
        }

        return null;
    }

    public void resultsUpdated(Project project, CoverageProvider provider) {
        Set<String> mimeTypes = provider.getMimeTypes();
        for (JTextComponent target : EditorRegistry.componentList()) {
            Document document = target.getDocument();
            String mimeType = (String) document.getProperty(MIME_TYPE);
            if (mimeType != null && mimeTypes.contains(mimeType)) {
                FileObject fo = GsfUtilities.findFileObject(document);
                if (fo != null && FileOwnerQuery.getOwner(fo) == project) {
                    FileCoverageDetails hitCounts = (FileCoverageDetails) document.getProperty(COVERAGE_DOC_PROPERTY);
                    if (hitCounts == null) {
                        document.putProperty(COVERAGE_DOC_PROPERTY, null); // ehh... what?
                    }
                    if (isEnabled(project)) {
                        focused(fo, target);
                    } else {
                        CoverageHighlightsContainer container = CoverageHighlightsLayerFactory.getContainer(document);
                        if (container != null) {
                            container.refresh();
                        }
                    }
                }
            }
        }

        CoverageReportTopComponent report = showingReports.get(project);
        if (report != null) {
            List<FileCoverageSummary> coverage = provider.getResults();
            report.updateData(coverage);
        }
    }

    public void clear(Project project) {
        CoverageProvider provider = getProvider(project);
        if (provider != null) {
            provider.clear();
            resultsUpdated(project, provider);
        }
    }

    void closedReport(Project project) {
        showingReports.remove(project);
    }

    public void showReport(Project project) {
        // TODO - keep references to one per project and when open just show
        // the existing one?
        CoverageProvider provider = getProvider(project);
        if (provider != null) {
            List<FileCoverageSummary> results = provider.getResults();

            CoverageReportTopComponent report = showingReports.get(project);
            if (report == null) {
                report = new CoverageReportTopComponent(project, results);
                showingReports.put(project, report);
                report.open();
            }
            report.toFront();
            report.requestVisible();
        }
    }

    public boolean getShowEditorBar() {
        if (showEditorBar == null) {
            showEditorBar = Boolean.valueOf(NbPreferences.forModule(CoverageManager.class).getBoolean(PREF_EDITOR_BAR, true));
        }

        return showEditorBar == Boolean.TRUE;
    }

    public void setShowEditorBar(boolean on) {
        this.showEditorBar = Boolean.valueOf(on);
        NbPreferences.forModule(CoverageManager.class).putBoolean(PREF_EDITOR_BAR, on);

        // Update existing editors
        for (JTextComponent target : EditorRegistry.componentList()) {
            Document document = target.getDocument();
            CoverageSideBar sb = CoverageSideBar.getSideBar(document);
            if (sb != null) {
                sb.showCoveragePanel(on);
            }
        }
    }
}
