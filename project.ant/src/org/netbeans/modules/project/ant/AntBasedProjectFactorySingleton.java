/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.project.ant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.CRC32;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Singleton {@link ProjectFactory} implementation which handles all Ant-based
 * projects by delegating some functionality to registered Ant project types.
 * @author Jesse Glick
 */
@ServiceProvider(service=ProjectFactory.class, position=100)
public final class AntBasedProjectFactorySingleton implements ProjectFactory2 {
    
    public static final String PROJECT_XML_PATH = "nbproject/project.xml"; // NOI18N

    public static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1"; // NOI18N

    public static final Logger LOG = Logger.getLogger(AntBasedProjectFactorySingleton.class.getName());
    
    /** Construct the singleton. */
    public AntBasedProjectFactorySingleton() {}
    
    private static final Map<Project,Reference<AntProjectHelper>> project2Helper = new WeakHashMap<Project,Reference<AntProjectHelper>>();
    private static final Map<AntProjectHelper,Reference<Project>> helper2Project = new WeakHashMap<AntProjectHelper,Reference<Project>>();
    private static final Map<AntBasedProjectType,List<Reference<AntProjectHelper>>> type2Projects = new HashMap<AntBasedProjectType,List<Reference<AntProjectHelper>>>(); //for second part of #42738
    private static final Lookup.Result<AntBasedProjectType> antBasedProjectTypes;
    private static Map<String,AntBasedProjectType> antBasedProjectTypesByType = null;
    static {
        antBasedProjectTypes = Lookup.getDefault().lookupResult(AntBasedProjectType.class);
        antBasedProjectTypes.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                synchronized (AntBasedProjectFactorySingleton.class) {
                    Set<AntBasedProjectType> oldTypes = type2Projects.keySet();
                    Set<AntBasedProjectType> removed  = new HashSet<AntBasedProjectType>(oldTypes);
                    
                    removed.removeAll(antBasedProjectTypes.allInstances());
                    
                    antBasedProjectTypesRemoved(removed);
                    
                    antBasedProjectTypesByType = null;
                }
            }
        });
    }
    
    private static void antBasedProjectTypesRemoved(Set<AntBasedProjectType> removed) {
        for (AntBasedProjectType type : removed) {
            List<Reference<AntProjectHelper>> projects = type2Projects.get(type);
            if (projects != null) {
                for (Reference<AntProjectHelper> r : projects) {
                    AntProjectHelper helper = r.get();
                    if (helper != null) {
                        helper.notifyDeleted();
                    }
                }
            }
            type2Projects.remove(type);
        }
    }
    
    private static synchronized AntBasedProjectType findAntBasedProjectType(String type) {
        if (antBasedProjectTypesByType == null) {
            antBasedProjectTypesByType = new HashMap<String,AntBasedProjectType>();
            // No need to synchronize similar calls since this is called only inside
            // ProjectManager.mutex. However dkonecny says that allInstances can
            // trigger a LookupEvent which would clear antBasedProjectTypesByType,
            // so need to initialize that later; and who knows then Lookup changes
            // might be fired.
            for (AntBasedProjectType abpt : antBasedProjectTypes.allInstances()) {
                antBasedProjectTypesByType.put(abpt.getType(), abpt);
            }
        }
        return antBasedProjectTypesByType.get(type);
    }
    
    public boolean isProject(FileObject dir) {
        File dirF = FileUtil.toFile(dir);
        if (dirF == null) {
            return false;
        }
        // Just check whether project.xml exists. Do not attempt to parse it, etc.
        // Do not use FileObject.getFileObject since that may load other sister files.
        File projectXmlF = new File(new File(dirF, "nbproject"), "project.xml"); // NOI18N
        return projectXmlF.isFile();
    }

    public Result isProject2(FileObject projectDirectory) {
        if (FileUtil.toFile(projectDirectory) == null) {
            return null;
        }
        FileObject projectFile = projectDirectory.getFileObject(PROJECT_XML_PATH);
        //#54488: Added check for virtual
        if (projectFile == null || !projectFile.isData() || projectFile.isVirtual()) {
            return null;
        }
        File projectDiskFile = FileUtil.toFile(projectFile);
        //#63834: if projectFile exists and projectDiskFile does not, do nothing:
        if (projectDiskFile == null) {
            return null;
        }
        try {
            Document projectXml = loadProjectXml(projectDiskFile);
            if (projectXml != null) {
                Element typeEl = Util.findElement(projectXml.getDocumentElement(), "type", PROJECT_NS); // NOI18N
                if (typeEl != null) {
                    String type = Util.findText(typeEl);
                    if (type != null) {
                        AntBasedProjectType provider = findAntBasedProjectType(type);
                        if (provider != null) {
                            if (provider instanceof AntBasedGenericType) {
                                return new ProjectManager.Result(((AntBasedGenericType)provider).getIcon());
                            } else {
                                //put special icon?
                                return new ProjectManager.Result(null);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(AntBasedProjectFactorySingleton.class.getName()).log(Level.FINE, "Failed to load the project.xml file.", ex);
        }
        // better have false positives than false negatives (according to the ProjectManager.isProject/isProject2 javadoc.
        return new ProjectManager.Result(null);
    }

    
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (FileUtil.toFile(projectDirectory) == null) {
            LOG.log(Level.FINE, "no disk dir {0}", projectDirectory);
            return null;
        }
        FileObject projectFile = projectDirectory.getFileObject(PROJECT_XML_PATH);
        //#54488: Added check for virtual
        if (projectFile == null || !projectFile.isData() || projectFile.isVirtual()) {
            LOG.log(Level.FINE, "not concrete data file {0}/nbproject/project.xml", projectDirectory);
            return null;
        }
        File projectDiskFile = FileUtil.toFile(projectFile);
        //#63834: if projectFile exists and projectDiskFile does not, do nothing:
        if (projectDiskFile == null) {
            LOG.log(Level.FINE, "{0} not mappable to file", projectFile);
            return null;
        }
        Document projectXml = loadProjectXml(projectDiskFile);
        if (projectXml == null) {
            LOG.log(Level.FINE, "could not load {0}", projectDiskFile);
            return null;
        }
        Element typeEl = Util.findElement(projectXml.getDocumentElement(), "type", PROJECT_NS); // NOI18N
        if (typeEl == null) {
            LOG.log(Level.FINE, "no <type> in {0}", projectDiskFile);
            return null;
        }
        String type = Util.findText(typeEl);
        if (type == null) {
            LOG.log(Level.FINE, "no <type> text in {0}", projectDiskFile);
            return null;
        }
        AntBasedProjectType provider = findAntBasedProjectType(type);
        if (provider == null) {
            LOG.log(Level.FINE, "no provider for {0}", type);
            return null;
        }
        AntProjectHelper helper = HELPER_CALLBACK.createHelper(projectDirectory, projectXml, state, provider);
        Project project = provider.createProject(helper);
        project2Helper.put(project, new WeakReference<AntProjectHelper>(helper));
        synchronized (helper2Project) {
            helper2Project.put(helper, new WeakReference<Project>(project));
        }
        List<Reference<AntProjectHelper>> l = type2Projects.get(provider);
        
        if (l == null) {
            type2Projects.put(provider, l = new ArrayList<Reference<AntProjectHelper>>());
        }
        
        l.add(new WeakReference<AntProjectHelper>(helper));
        
        return project;
    }
    
    private Document loadProjectXml(File projectDiskFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = new FileInputStream(projectDiskFile);
        try {
            FileUtil.copy(is, baos);
        } finally {
            is.close();
        }
        byte[] data = baos.toByteArray();
        InputSource src = new InputSource(new ByteArrayInputStream(data));
        src.setSystemId(projectDiskFile.toURI().toString());
        try {
            Document projectXml = XMLUtil.parse(src, false, true, Util.defaultErrorHandler(), null);
            Element projectEl = projectXml.getDocumentElement();
            if (!PROJECT_NS.equals(projectEl.getNamespaceURI())) { // NOI18N
                LOG.log(Level.FINE, "{0} had wrong root element namespace {1} when parsed from {2}",
                        new Object[] {projectDiskFile, projectEl.getNamespaceURI(), baos});
                if (LOG.isLoggable(Level.FINE)) {
                    // XXX sometimes on deadlock get a bogus DeferredElementNSImpl;
                    // all fields null except fNodeIndex=1, ownerDocument/ownerNode, previousSibling=this
                    try {
                        for (Class c = projectEl.getClass(); c != null; c = c.getSuperclass()) {
                            for (Field f : c.getDeclaredFields()) {
                                if ((f.getModifiers() & Modifier.STATIC) > 0) {
                                    continue;
                                }
                                f.setAccessible(true);
                                LOG.fine(c.getName() + "." + f.getName() + "=" + f.get(projectEl));
                            }
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
                return null;
            }
            if (!"project".equals(projectEl.getLocalName())) { // NOI18N
                LOG.log(Level.FINE, "{0} had wrong root element name {1} when parsed from {2}",
                        new Object[] {projectDiskFile, projectEl.getLocalName(), baos});
                return null;
            }
            // #142680: try to cache CRC-32s of project.xml files known to be valid, since validation can be slow.
            Preferences prefs = NbPreferences.forModule(AntBasedProjectFactorySingleton.class);
            String key = "knownValidProjectXmlCRC32s"; // NOI18N
            List<Long> knownHashes = new ArrayList<Long>();
            String knownHashesS = prefs.get(key, null);
            if (knownHashesS != null) {
                for (String knownHash : knownHashesS.split(",")) { // NOI18N
                    try {
                        knownHashes.add(Long.valueOf(knownHash, 16));
                    } catch (NumberFormatException x) {/* forget it */}
                }
            }
            CRC32 crc = new CRC32();
            crc.update(data);
            long hash = crc.getValue();
            if (!knownHashes.contains(hash)) {
                Logger.getLogger(AntBasedProjectFactorySingleton.class.getName()).log(Level.FINE, "Validating: {0}", projectDiskFile);
                try {
                    ProjectXMLCatalogReader.validate(projectEl);
                    StringBuilder newKnownHashes = new StringBuilder(Long.toString(hash, 16));
                    for (int i = 0; i < knownHashes.size() && i < /* max size */100; i++) {
                        newKnownHashes.append(',');
                        newKnownHashes.append(Long.toString(knownHashes.get(i), 16));
                    }
                    prefs.put(key, newKnownHashes.toString());
                } catch (SAXException x) {
                    Element corrected = ProjectXMLCatalogReader.autocorrect(projectEl, x);
                    if (corrected != null) {
                        projectXml.replaceChild(corrected, projectEl);
                        projectEl = corrected;
                        // Try to correct on disk if possible.
                        // (If not, any changes from the IDE will write out a corrected file anyway.)
                        if (projectDiskFile.canWrite()) {
                            OutputStream os = new FileOutputStream(projectDiskFile);
                            try {
                                XMLUtil.write(projectXml, os, "UTF-8");
                            } finally {
                                os.close();
                            }
                        }
                    } else {
                        throw x;
                    }
                }
            }
            return projectXml;
        } catch (SAXException e) {
            IOException ioe = (IOException) new IOException(projectDiskFile + ": " + e.toString()).initCause(e);
            String msg = e.getMessage().
                    // org/apache/xerces/impl/msg/XMLSchemaMessages.properties validation (3.X.4)
                    replaceFirst("^cvc-[^:]+: ", ""). // NOI18N
                    replaceAll("http://www.netbeans.org/ns/", ".../"); // NOI18N
            Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(AntBasedProjectFactorySingleton.class,
                                                                        "AntBasedProjectFactorySingleton.parseError",
                                                                        projectDiskFile.getName(), msg));
            throw ioe;
        }
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
        Reference<AntProjectHelper> helperRef = project2Helper.get(project);
        if (helperRef == null) {
            StringBuilder sBuff = new StringBuilder();
            sBuff.append(project.getClass().getName() + "\n"); // NOI18N
            sBuff.append("argument project: " + project + " => " + project.hashCode() + "\n"); // NOI18N
            sBuff.append("project2Helper keys: " + "\n"); // NOI18N
            for (Project prj : project2Helper.keySet()) {
                sBuff.append("    project: " + prj + " => " + prj.hashCode() + "\n"); // NOI18N
            }
            throw new ClassCastException(sBuff.toString());
        }
        AntProjectHelper helper = helperRef.get();
        assert helper != null : "AntProjectHelper collected for " + project;
        HELPER_CALLBACK.save(helper);
    }
    
    /**
     * Get the project corresponding to a helper.
     * For use from {@link AntProjectHelper}.
     * @param helper an Ant project helper object
     * @return the corresponding project
     */
    public static Project getProjectFor(AntProjectHelper helper) {
        Reference<Project> projectRef;
        synchronized (helper2Project) {
            projectRef = helper2Project.get(helper);
        }
        assert projectRef != null : "Expecting a Project reference for " + helper;
        Project p = projectRef.get();
        assert p != null : "Expecting a non-null Project for " + helper;
        return p;
    }
    
    /**
     * Get the helper corresponding to a project.
     * For use from {@link ProjectGenerator}.
     * @param project an Ant-based project
     * @return the corresponding Ant project helper object, or null if it is unknown
     */
    public static AntProjectHelper getHelperFor(Project p) {
        Reference<AntProjectHelper> helperRef = project2Helper.get(p);
        return helperRef != null ? helperRef.get() : null;
    }


    /**
     * Callback to create and access AntProjectHelper objects from outside its package.
     */
    public interface AntProjectHelperCallback {
        AntProjectHelper createHelper(FileObject dir, Document projectXml, ProjectState state, AntBasedProjectType type);
        void save(AntProjectHelper helper) throws IOException;
    }
    /** Defined in AntProjectHelper's static initializer. */
    public static AntProjectHelperCallback HELPER_CALLBACK;
    static {
        Class<?> c = AntProjectHelper.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException e) {
            assert false : e;
        }
        assert HELPER_CALLBACK != null;
    }

    public static AntBasedProjectType create(Map map) {
        return new AntBasedGenericType(map);
    }


}
