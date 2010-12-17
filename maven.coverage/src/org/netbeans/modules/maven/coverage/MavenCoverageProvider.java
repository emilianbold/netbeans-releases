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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.coverage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@ProjectServiceProvider(service=CoverageProvider.class, projectType="org-netbeans-modules-maven") // not limited to a packaging
public final class MavenCoverageProvider implements CoverageProvider {

    private static final String GROUP_COBERTURA = "org.codehaus.mojo"; // NOI18N
    private static final String ARTIFACT_COBERTURA = "cobertura-maven-plugin"; // NOI18N
    private static final String GROUP_SITE = "org.apache.maven.plugins"; // NOI18N
    private static final String ARTIFACT_SITE = "maven-site-plugin"; // NOI18N

    private final Project p;

    public MavenCoverageProvider(Project p) {
        this.p = p;
    }

    public @Override boolean supportsHitCounts() {
        return true;
    }

    public @Override boolean supportsAggregation() {
        return false;
    }

    @SuppressWarnings("deprecation")
    public @Override boolean isEnabled() {
        NbMavenProject prj = p.getLookup().lookup(NbMavenProject.class);
        if (prj == null) {
            return false;
        }
        if (PluginPropertyUtils.getPluginVersion(prj.getMavenProject(), GROUP_COBERTURA, ARTIFACT_COBERTURA) != null) {
            // For whatever reason, was configured as a direct build plugin... fine.
            return true;
        }
        // Maven 3.x configuration:
        for (Plugin plug : prj.getMavenProject().getBuildPlugins()) {
            if (GROUP_SITE.equals(plug.getGroupId()) && ARTIFACT_SITE.equals(plug.getArtifactId())) {
                Xpp3Dom cfg = (Xpp3Dom) plug.getConfiguration(); // MNG-4862
                if (cfg == null) {
                    continue;
                }
                Xpp3Dom reportPlugins = cfg.getChild("reportPlugins"); // NOI18N
                if (reportPlugins == null) {
                    continue;
                }
                for (Xpp3Dom plugin : reportPlugins.getChildren("plugin")) { // NOI18N
                    Xpp3Dom groupId = plugin.getChild("groupId"); // NOI18N
                    if (groupId == null) {
                        continue;
                    }
                    Xpp3Dom artifactId = plugin.getChild("artifactId"); // NOI18N
                    if (artifactId == null) {
                        continue;
                    }
                    if (GROUP_COBERTURA.equals(groupId.getValue()) && ARTIFACT_COBERTURA.equals(artifactId.getValue())) {
                        return true;
                    }
                }
            }
        }
        // Maven 2.x configuration:
        for (ReportPlugin plug : prj.getMavenProject().getReportPlugins()) {
            if (GROUP_COBERTURA.equals(plug.getGroupId()) && ARTIFACT_COBERTURA.equals(plug.getArtifactId())) {
                return true;
            }
        }
        // In fact you _could_ just run the plugin directly here... but perhaps the user did not want to do so.
        return false;
    }

    public @Override boolean isAggregating() {
        throw new UnsupportedOperationException();
    }

    public @Override void setAggregating(boolean aggregating) {
        throw new UnsupportedOperationException();
    }

    public @Override Set<String> getMimeTypes() {
        return Collections.singleton("text/x-java"); // NOI18N
    }

    public @Override void setEnabled(boolean enabled) {
        // XXX add plugin configuration here if not already present
    }

    private @CheckForNull FileObject report() {
        // XXX read overridden location acc. to http://mojo.codehaus.org/cobertura-maven-plugin/cobertura-mojo.html#outputDirectory
        return p.getProjectDirectory().getFileObject("target/site/cobertura/coverage.xml"); // NOI18N
    }

    private @NullAllowed org.w3c.dom.Document report;

    public @Override synchronized void clear() {
        FileObject r = report();
        if (r != null) {
            try {
                r.delete();
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
        }
        report = null;
    }

    private FileChangeListener listener;

    private @CheckForNull synchronized org.w3c.dom.Document parse() {
        if (report != null) {
            return report;
        }
        FileObject r = report();
        if (r == null) {
            return null;
        }
        CoverageManager.INSTANCE.setEnabled(p, true); // XXX otherwise it defaults to disabled?? not clear where to call this
        if (listener == null) {
            listener = new FileChangeAdapter() {
                public @Override void fileChanged(FileEvent fe) {
                    fire();
                }
                public @Override void fileDataCreated(FileEvent fe) {
                    fire();
                }
                public @Override void fileDeleted(FileEvent fe) {
                    fire();
                }
                private void fire() {
                    report = null;
                    CoverageManager.INSTANCE.resultsUpdated(p, MavenCoverageProvider.this);
                }
            };
            FileUtil.addFileChangeListener(listener, FileUtil.toFile(r));
        }
        try {
            return report = XMLUtil.parse(new InputSource(r.getURL().toString()), true, false, XMLUtil.defaultErrorHandler(), new EntityResolver() {
                public @Override InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if (systemId.equals("http://cobertura.sourceforge.net/xml/coverage-04.dtd")) {
                        return new InputSource(MavenCoverageProvider.class.getResourceAsStream("coverage-04.dtd")); // NOI18N
                    } else {
                        return null;
                    }
                }
            });
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
            return null;
        }
    }

    private @CheckForNull FileObject src() {
        // XXX getOriginalMavenProject().getCompileSourceRoots()
        return p.getProjectDirectory().getFileObject("src/main/java"); // NOI18N
    }
    
    public @Override FileCoverageDetails getDetails(final FileObject fo, final Document doc) {
        org.w3c.dom.Document r = parse();
        if (r == null) {
            return null;
        }
        FileObject src = src();
        if (src == null) {
            return null;
        }
        String path = FileUtil.getRelativePath(src, fo);
        if (path == null) {
            return null;
        }
        final List<Element> lines = new ArrayList<Element>();
        String name = null;
        NodeList nl = r.getElementsByTagName("class"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            final Element clazz = (Element) nl.item(i);
            if (clazz.getAttribute("filename").equals(path)) { // NOI18N
                Element linesE = XMLUtil.findElement(clazz, "lines", null); // NOI18
                if (linesE != null) {
                    lines.addAll(XMLUtil.findSubElements(linesE));
                }
                if (name == null) {
                    name = clazz.getAttribute("name");
                }
            }
        }
        if (name == null) {
            return null; // no match
        }
        final String _name = name;
        return new FileCoverageDetails() {
            public @Override FileObject getFile() {
                return fo;
            }
            public @Override int getLineCount() {
                return doc.getDefaultRootElement().getElementCount();
            }
            public @Override boolean hasHitCounts() {
                return true;
            }
            public @Override long lastUpdated() {
                FileObject r = report();
                return r != null ? r.lastModified().getTime() : 0L;
            }
            public @Override FileCoverageSummary getSummary() {
                return summaryOf(fo, _name, lines);
            }
            private Integer find(int lineNo) {
                for (Element line : lines) {
                    if (line.getAttribute("number").equals(String.valueOf(lineNo + 1))) { // NOI18N
                        return Integer.valueOf(line.getAttribute("hits")); // NOI18N
                    }
                }
                return null;
            }
            public @Override CoverageType getType(int lineNo) {
                Integer count = find(lineNo);
                return count == null ? CoverageType.INFERRED : count == 0 ? CoverageType.NOT_COVERED : CoverageType.COVERED;
            }
            public @Override int getHitCount(int lineNo) {
                Integer count = find(lineNo);
                return count == null ? 0 : count;
            }
        };
    }

    public @Override List<FileCoverageSummary> getResults() {
        org.w3c.dom.Document r = parse();
        if (r == null) {
            return null;
        }
        FileObject src = src();
        if (src == null) {
            return null;
        }
        List<FileCoverageSummary> summs = new ArrayList<FileCoverageSummary>();
        NodeList nl = r.getElementsByTagName("class"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            Element clazz = (Element) nl.item(i);
            FileObject java = src.getFileObject(clazz.getAttribute("filename")); // NOI18N
            if (java == null) {
                continue;
            }
            // XXX nicer to collect together nested classes in same compilation unit
            Element linesE = XMLUtil.findElement(clazz, "lines", null); // NOI18N
            List<Element> lines = linesE != null ? XMLUtil.findSubElements(linesE) : Collections.<Element>emptyList();
            summs.add(summaryOf(java, clazz.getAttribute("name").replace('$', '.'), lines));
        }
        return summs;
    }
    
    private FileCoverageSummary summaryOf(FileObject java, String name, List<Element> lines) {
        // Not really the total number of lines in the file at all, but close enough - the ones Cobertura recorded.
        int lineCount = 0;
        int executedLineCount = 0;
        for (Element line : lines) {
            lineCount++;
            if (!line.getAttribute("hits").equals("0")) {
                executedLineCount++;
            }
        }
        return new FileCoverageSummary(java, name, lineCount, executedLineCount, 0, 0); // NOI18N
    }

    public @Override String getTestAllAction() {
        return "cobertura"; // NOI18N
        // XXX and Test button runs COMMAND_TEST_SINGLE on file, which is not good here; cf. CoverageSideBar.testOne
    }

}
