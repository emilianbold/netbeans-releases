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

package org.netbeans.modules.cnd.makeproject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 */
@ServiceProvider(service=ProjectFactory.class, position=144)
public final class MakeBasedProjectFactorySingleton implements ProjectFactory2 {

    public static final String PROJECT_XML_PATH = "nbproject/project.xml"; // NOI18N

    public static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1"; // NOI18N

    public static final Logger LOG = Logger.getLogger(MakeBasedProjectFactorySingleton.class.getName());

    public static final MakeProjectTypeImpl TYPE_INSTANCE = new MakeProjectTypeImpl();

    /** Construct the singleton. */
    public MakeBasedProjectFactorySingleton() {}

    private static final Map<Project,Reference<MakeProjectHelperImpl>> project2Helper = new WeakHashMap<>();
    private static final Map<MakeProjectHelperImpl,Reference<Project>> helper2Project = new WeakHashMap<>();

    private static MakeProjectTypeImpl findMakeProjectType(String type) {
        if (MakeProjectTypeImpl.TYPE.equals(type)) {
            return TYPE_INSTANCE;
        }
        return null;
    }

    @Override
  public boolean isProject(FileObject dir) {
        FileObject projectFile = dir.getFileObject(PROJECT_XML_PATH);
        return projectFile != null && projectFile.isValid() && projectFile.isData();
    }

    @Override
    public Result isProject2(FileObject projectDirectory) {
        FileObject projectFile = projectDirectory.getFileObject(PROJECT_XML_PATH);
        //#54488: Added check for virtual
        if (projectFile == null || !projectFile.isData() || projectFile.isVirtual()) {
            return null;
        }
        try {
            Document projectXml = loadProjectXml(projectFile);
            if (projectXml != null) {
                Element typeEl = XMLUtil.findElement(projectXml.getDocumentElement(), "type", PROJECT_NS); // NOI18N
                if (typeEl != null) {
                    String type = XMLUtil.findText(typeEl);
                    if (type != null) {
                        MakeProjectTypeImpl provider = findMakeProjectType(type);
                        if (provider != null) {
                            return new ProjectManager.Result(provider.getIcon(projectXml.getDocumentElement()));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MakeProjectTypeImpl.class.getName()).log(Level.FINE, "Failed to load the project.xml file.", ex);
        }
        // better have false positives than false negatives (according to the ProjectManager.isProject/isProject2 javadoc.
        return new ProjectManager.Result(null);
    }


    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        FileObject projectFile = projectDirectory.getFileObject(PROJECT_XML_PATH);
        //#54488: Added check for virtual
        if (projectFile == null) {
            LOG.log(Level.FINE, "not found data file {0}/nbproject/project.xml", projectDirectory.getPath()); // NOI18N
            return null;
        }
        if (!projectFile.isData()) {
            LOG.log(Level.FINE, "not found plain file {0}/nbproject/project.xml", projectDirectory.getPath()); // NOI18N
            return null;
        }
        if (projectFile.isVirtual()) {
            LOG.log(Level.FINE, "not concrete data file {0}/nbproject/project.xml", projectDirectory.getPath()); // NOI18N
            return null;
        }
        Document projectXml = loadProjectXml(projectFile);
        if (projectXml == null) {
            LOG.log(Level.FINE, "could not load {0}", projectFile);
            return null;
        }
        Element typeEl = XMLUtil.findElement(projectXml.getDocumentElement(), "type", PROJECT_NS); // NOI18N
        if (typeEl == null) {
            LOG.log(Level.FINE, "no <type> in {0}", projectFile);
            return null;
        }
        String type = XMLUtil.findText(typeEl);
        if (type == null) {
            LOG.log(Level.FINE, "no <type> text in {0}", projectFile);
            return null;
        }
        MakeProjectTypeImpl provider = findMakeProjectType(type);
        if (provider == null) {
            LOG.log(Level.FINE, "no provider for {0}", type);
            return null;
        }
        MakeProjectHelperImpl helper = MakeProjectHelperImpl.create(projectDirectory, projectXml, state, provider);
        Project project = provider.createProject(helper);
        project2Helper.put(project, new WeakReference<>(helper));
        synchronized (helper2Project) {
            helper2Project.put(helper, new WeakReference<>(project));
        }

        return project;
    }

    private Document loadProjectXml(FileObject projectDiskFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = projectDiskFile.getInputStream();
        try {
            FileUtil.copy(is, baos);
        } finally {
            is.close();
        }
        byte[] data = baos.toByteArray();
        InputSource src = new InputSource(new ByteArrayInputStream(data));
        try {
//            Document projectXml = XMLUtil.parse(src, false, true, Util.defaultErrorHandler(), null);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException x) {
                throw new SAXException(x);
            }
            builder.setErrorHandler(XMLUtil.defaultErrorHandler());
            Document projectXml = builder.parse(src);
            LOG.fine("parsed document");
//            dumpFields(projectXml);
            Element projectEl = projectXml.getDocumentElement();
            LOG.fine("got document element");
//            dumpFields(projectXml);
//            dumpFields(projectEl);
            String namespace = projectEl.getNamespaceURI();
            LOG.log(Level.FINE, "got namespace {0}", namespace);
            if (!PROJECT_NS.equals(namespace)) {
                LOG.log(Level.FINE, "{0} had wrong root element namespace {1} when parsed from {2}",
                        new Object[] {projectDiskFile, namespace, baos});
                return null;
            }
            if (!"project".equals(projectEl.getLocalName())) { // NOI18N
                LOG.log(Level.FINE, "{0} had wrong root element name {1} when parsed from {2}",
                        new Object[] {projectDiskFile, projectEl.getLocalName(), baos});
                return null;
            }
            return projectXml;
        } catch (SAXException e) {
            IOException ioe = new IOException(projectDiskFile + ": " + e, e); // NOI18N
            throw ioe;
        }
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
        Reference<MakeProjectHelperImpl> helperRef = project2Helper.get(project);
        if (helperRef == null) {
            StringBuilder sBuff = new StringBuilder("#191029: no project helper for a "); // NOI18N
            sBuff.append(project.getClass().getName()).append('\n'); // NOI18N
            sBuff.append("argument project: ").append(project).append(" => ").append(project.hashCode()).append('\n'); // NOI18N
            sBuff.append("project2Helper keys: " + "\n"); // NOI18N
            project2Helper.keySet().forEach((prj) -> {
                sBuff.append("    project: ").append(prj).append(" => ").append(prj.hashCode()).append('\n'); // NOI18N
            });
            // Happens occasionally, no clue why. Maybe someone saving project before ctor has finished?
            LOG.warning(sBuff.toString());
            return;
        }
        MakeProjectHelperImpl helper = helperRef.get();
        assert helper != null : "MakeProjectHelper collected for " + project;
        helper.save();
    }

    /**
     * Get the helper corresponding to a project.
     * For use from {@link ProjectGenerator}.
     * @param project an make-based project
     * @return the corresponding Make project helper object, or null if it is unknown
     */
    public static MakeProjectHelperImpl getHelperFor(Project p) {
        Reference<MakeProjectHelperImpl> helperRef = project2Helper.get(p);
        return helperRef != null ? helperRef.get() : null;
    }
    
    public static Project getProjectFor(MakeProjectHelper helper) {
        Reference<Project> ref = helper2Project.get(helper);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }
}
