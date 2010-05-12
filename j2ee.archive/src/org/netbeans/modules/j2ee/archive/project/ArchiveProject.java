/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.archive.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.archive.Util;
import org.netbeans.modules.j2ee.archive.customizer.ProvidesCustomizer;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NotImplementedException;
import org.openide.util.lookup.Lookups;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Result;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

@AntBasedProjectRegistration(
    iconResource="org/netbeans/modules/j2ee/archive/project/resources/packaged_archive_16.png",
    type=ArchiveProjectType.TYPE,
    sharedNamespace=ArchiveProjectType.PROJECT_CONFIGURATION_NS,
    privateNamespace=ArchiveProjectType.PRIVATE_CONFIGURATION_NS
)
public class ArchiveProject implements org.netbeans.api.project.Project {
    
    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private ArchiveProjectProperties projProperties;
    private final Icon ARCHIVE_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/j2ee/archive/project/resources/packaged_archive_16.png", false); // NOI18N
    private final UpdateHelper updateHelper;
    
    private HashMap<String,String> nameMap;
    
    public ArchiveProject(AntProjectHelper helper) {
        this.helper = helper;
        this.eval = helper.getStandardPropertyEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = new HelpfulLookup();
        this.updateHelper = new UpdateHelper(this, this.helper, this.aux, this.genFilesHelper,
                UpdateHelper.createDefaultNotifier());
        nameMap = new HashMap<String,String>(5);
    }
    
    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }
    
    public PropertyEvaluator  getPropertyEvaluator(){
        return eval;
        
    }
    
    public String getProjectProperty(String propName){
        
        return  (String) getArchiveProjectProperties().get(propName);
    }
    
    public String getEarPath(String key) {
        return (String) nameMap.get(key);
    }
    
    public void setEarPath(String key, String val) {
        nameMap.put(key,val);
    }
    
    final private static String NAME_LIT = "name"; // NOI18N
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
            @Override
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, NAME_LIT);
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, NAME_LIT);
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    /** Return configured project name. */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            @Override
            public String run() {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, NAME_LIT);
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "BINARCHIVE???"; // NOI18N
            }
        });
        
    }
    
    public ArchiveProjectProperties getArchiveProjectProperties() {
        projProperties= new ArchiveProjectProperties(this,getAntProjectHelper());
        return projProperties ;
    }
    
    // TODO automate the archive type discovery mechanism... see branch_ludo_changes_14_apr
    
    // having a Lookup that can be noisy is very useful during project
    // development
    private final class HelpfulLookup extends Lookup {
        
        private boolean verbose = Boolean.getBoolean("archiveproject.lookup.verbose");
        
        Lookup inner = LookupProviderSupport.createCompositeLookup(Lookups.fixed(new Object[] {
            new Info(),
            helper.createAuxiliaryConfiguration(),
            helper.createCacheDirectoryProvider(),
            helper.createGlobFileBuiltQuery(eval, new String[] {"${src.dir}/*.java"},
                    new String[] {"${build.classes.dir}/*.class"}),
            helper.createSharabilityQuery(eval, new String[] {"${src.dir}"},
                    new String[] {"${build.dir}","${dist.dir}", "${proxy.project.dir}"}),
            UILookupMergerSupport.createProjectOpenHookMerger(new OpenCloseHook()),
            new ProvidesAction(ArchiveProject.this),
            new ProvidesLogicalView(ArchiveProject.this),
            helper,
            new ProvidesJ2eeModule(helper, ArchiveProject.this),
            new J2eeModuleForAddModuleAction(J2eeModule.Type.EAR),
            new MyAntProvider(),
            new ProvidesCustomizer(ArchiveProject.this,helper),
            new RecommendedTemplatesImpl(),
            new ArchiveProjectOperations(ArchiveProject.this),
            ArchiveProject.this,
            new MyOpenHook(),
            new ProjectXmlSaved(),            
        }), "Projects/org-netbeans-modules-j2ee-archiveproject/Lookup");
                
        @Override
        public <T> Lookup.Item<T> lookupItem(Lookup.Template<T> template) {
            Item<T> retValue;
            
            retValue = inner.lookupItem(template);
            if (verbose && null == retValue && ErrorManager.getDefault().isNotifiable(ErrorManager.EXCEPTION)) {
                StackTraceElement[] sTEs = Thread.currentThread().getStackTrace();
                ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                        NbBundle.getMessage(ArchiveProject.class,"LOOKUP_MISS",template.toString(),sTEs[3],sTEs[4]));
    }
            return retValue;
        }
    
        @Override
        public <T> Lookup.Result<T> lookupResult(Class<T> clazz) {
            Result<T> retValue;
            
            retValue = inner.lookupResult(clazz);
            if (verbose && null == retValue && ErrorManager.getDefault().isNotifiable(ErrorManager.EXCEPTION)) {
                StackTraceElement[] sTEs = Thread.currentThread().getStackTrace();
                ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                        NbBundle.getMessage(ArchiveProject.class,"LOOKUP_MISS",clazz.toString(),sTEs[3],sTEs[4]));
            }
            return retValue;
        }

        @Override
        public <T> Collection<? extends T> lookupAll(Class<T> clazz) {
            Collection<? extends T> retValue;
            
            retValue = inner.lookupAll(clazz);
            if (verbose && null == retValue && ErrorManager.getDefault().isNotifiable(ErrorManager.EXCEPTION)) {
                StackTraceElement[] sTEs = Thread.currentThread().getStackTrace();
                ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                        NbBundle.getMessage(ArchiveProject.class,"LOOKUP_MISS",clazz.toString(),sTEs[3],sTEs[4]));
            }
            return retValue;
        }

        @Override
        public <T> T lookup(Class<T> clazz) {
            T ret = inner.lookup(clazz);
            if (verbose && null == ret && ErrorManager.getDefault().isNotifiable(ErrorManager.EXCEPTION)) {
                StackTraceElement[] sTEs = Thread.currentThread().getStackTrace();
                ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                        NbBundle.getMessage(ArchiveProject.class,"LOOKUP_MISS",clazz.getName(),sTEs[3],sTEs[4]));
            }
            return ret;
        }

        @Override
        public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
            Lookup.Result<T> ret = inner.lookup(template);
            if (verbose && null == ret && ErrorManager.getDefault().isNotifiable(ErrorManager.EXCEPTION)) {
                StackTraceElement[] sTEs = Thread.currentThread().getStackTrace();
                ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                        NbBundle.getMessage(ArchiveProject.class,"LOOKUP_MISS",template.toString(),sTEs[3],sTEs[4]));
            }
            return ret;
        }
    }
    //when #110886 gets implemented, this class is obsolete    
    private final class Info implements ProjectInformation {
        
        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private WeakReference<String> cachedName = null;
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
            synchronized (pcs) {
                cachedName = null;
            }
        }
        
        @Override
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }
        
        @Override
        public String getDisplayName() {
            synchronized (pcs) {
                if (cachedName != null) {
                    String dn = cachedName.get();
                    if (dn != null) {
                        return dn;
                    }
                }
            }
            String dn = ArchiveProject.this.getNamedProjectAttribute(NAME_LIT);
            synchronized (pcs) {
                cachedName = new WeakReference<String>(dn);
            }
            return dn;
        }
        
        @Override
        public Icon getIcon() {
            return ARCHIVE_PROJECT_ICON;
        }
        
        @Override
        public Project getProject() {
            return ArchiveProject.this;
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
    }
    
    private final class MyAntProvider implements AntArtifactProvider {
        // TODO - Need to fix for ejb-jar/app-client/resource-adapter cases
        @Override
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE,
                        "dist.archive", helper.getStandardPropertyEvaluator(), "dist",
                        "clean")
            };
        }
    }
    
    class OpenCloseHook extends ProjectOpenedHook {
        
        List<ClassPath[]> paths = new ArrayList<ClassPath[]>();
        
        @Override
        protected void projectOpened() {
            ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
                @Override
                public Object run() {
                    doRegeneration();
                    FileObject dir = helper.getProjectDirectory();
                    FileObject subDir;
                    ArchiveProjectProperties app = getArchiveProjectProperties();
                    
                    String type = (String) app.get(ArchiveProjectProperties.ARCHIVE_TYPE);
                    boolean isEar = ArchiveProjectProperties.PROJECT_TYPE_VALUE_EAR.equals(type);
                    subDir = dir.getFileObject(ArchiveProjectProperties.TMP_PROJ_DIR_VALUE);
                    if (subDir != null) {
                        Project tmpproj = null;
                        try {
                            tmpproj = ProjectManager.getDefault().findProject(subDir);
                        } catch (IllegalArgumentException ex) {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "" + ex);
                        } catch (IOException ex) {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "" + ex);
                        }
                        ProvidesJ2eeModule tmp = (ProvidesJ2eeModule) ArchiveProject.this.getLookup().lookup(ProvidesJ2eeModule.class);
                        if (null!=tmpproj) {
                            List<FileObject> roots = new ArrayList<FileObject>();
                            FileObject cpe = tmpproj.getProjectDirectory().getFileObject("src/java");
                            if (null != cpe) {
                                roots.add(cpe);
                            }
                            cpe = tmpproj.getProjectDirectory().getFileObject("src/conf");
                            if (null != cpe) {
                                roots.add(cpe);
                            }
                            cpe = tmpproj.getProjectDirectory().getFileObject("web/WEB-INF/classes");
                            if (null != cpe) {
                                roots.add(cpe);
                            }
                            if (roots.size() > 0) {
                                ClassPath cp = ClassPathSupport.createClassPath(roots.toArray(new FileObject[roots.size()])); // creatClassPath(roots);
                                ClassPath[] aofcp = new ClassPath[] {cp};
                                GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, aofcp);
                                GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, aofcp);
                                paths.add(aofcp);
                            }
                            J2eeModuleProvider jmp = (J2eeModuleProvider) tmpproj.getLookup().lookup(J2eeModuleProvider.class);
                            jmp.getConfigSupport().ensureConfigurationReady();
                            tmp.setInner(jmp);
                        } else {
                            J2eeModule.Type mt = isEar ? J2eeModule.Type.EAR : J2eeModule.Type.RAR;
                            tmp.setJ2eeModule(
                                    J2eeModuleFactory.createJ2eeModule(new J2eeModuleForAddModuleAction(mt)));
                            tmp.setServerInstanceID((String) app.get(ArchiveProjectProperties.J2EE_SERVER_INSTANCE));
                            if (isEar) {
                                tmp.getConfigSupport().ensureConfigurationReady();
                            }
                        }
                    }
                    
                    if (!isEar) {
                        return null;
                    }
                    
                    // This is an ear project
                    FileObject appXml = getProjectDirectory().getFileObject("nbproject").getFileObject("application.xml"); // NOI18N
                    if (null != appXml) {
                        try {
                            Application appDD = org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(appXml);
                            Module ms[] = appDD.getModule();
                            for (Module m : ms) {
                                String archivePath = m.getEjb();
                                if (null != archivePath) {
                                    openSubarchiveProject(archivePath);
                                } else {
                                    archivePath = m.getJava();
                                    if (null != archivePath) {
                                        openSubarchiveProject(archivePath);
                                    } else {
                                        archivePath = m.getConnector();
                                        if (null != archivePath) {
                                            openSubarchiveProject(archivePath);
                                        } else {
                                            Web w = m.getWeb();
                                            if (null != w) {
                                                archivePath = w.getWebUri();
                                                if (null != archivePath) {
                                                    openSubarchiveProject(archivePath);
                                                }
                                            }
                                        }
                                    }
                                }
                                
                            }
                        } catch (IOException ex) {
                            Logger.getLogger("global").log(Level.INFO, null, ex);
                        }
                    } else {
                        ErrorManager.getDefault().log(ErrorManager.WARNING, NbBundle.getMessage(ArchiveProject.class,"WARN_EAR_ARCH_MISSING_APPLICATION_XML",getName()));
                    }
                    return null;
                }
            });
        }
        
        private void openSubarchiveProject( String pathInEar ) throws IOException {
            FileObject root = getProjectDirectory();
            FileObject subprojRoot = root.getFileObject("subarchives");
            String subprojkey = Util.getKey(pathInEar);
            try {
                FileObject projDest = subprojRoot.getFileObject(subprojkey);
                nameMap.put(subprojkey, pathInEar);
                FileObject fo = (FileObject) projDest.getFileObject(ArchiveProjectProperties.TMP_PROJ_DIR_VALUE);
                Project tmpproj = ProjectManager.getDefault().findProject(fo);
                Project subproj = ProjectManager.getDefault().findProject(projDest);
                MyOpenHook eh = (MyOpenHook) subproj.getLookup().lookup(MyOpenHook.class);
                eh.regenerateBuildFiles();
                ProvidesJ2eeModule tmp = (ProvidesJ2eeModule) subproj.getLookup().lookup(ProvidesJ2eeModule.class);
                if (null!=tmpproj) {
                    J2eeModuleProvider jmp = (J2eeModuleProvider) tmpproj.getLookup().lookup(J2eeModuleProvider.class);
                    List<FileObject> roots = new ArrayList<FileObject>();
                    FileObject cpe = tmpproj.getProjectDirectory().getFileObject("src/java");
                    if (null != cpe) {
                        roots.add(cpe);
                    }
                    cpe = tmpproj.getProjectDirectory().getFileObject("src/conf");
                    if (null != cpe) {
                        roots.add(cpe);
                    }
                    cpe = tmpproj.getProjectDirectory().getFileObject("web/WEB-INF/classes");
                    if (null != cpe) {
                        roots.add(cpe);
                    }
                    if (roots.size() > 0) {
                        ClassPath cp = ClassPathSupport.createClassPath(roots.toArray(new FileObject[roots.size()])); // creatClassPath(roots);
                        ClassPath[] aofcp = new ClassPath[] {cp};
                        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, aofcp);
                        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, aofcp);
                        paths.add(aofcp);
                    }
                    try {
                        jmp.getConfigSupport().ensureConfigurationReady();
                    } catch (IllegalArgumentException iae) {
                        // I saw an IAE come out of this method once.  I want to
                        // find out this value if it happens again
                        FileObject tfo = tmpproj.getProjectDirectory();
                        if (null != tfo) {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                                    tmpproj.getProjectDirectory().toString());
                        } else {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                                    "null");
                        }
                        throw iae;
                    }
                    tmp.setInner(jmp);
                } else {
                    J2eeModule.Type mt = J2eeModule.Type.RAR;
                    tmp.setJ2eeModule(
                            J2eeModuleFactory.createJ2eeModule(new J2eeModuleForAddModuleAction(mt)));
                    tmp.setServerInstanceID((String) getArchiveProjectProperties().get(ArchiveProjectProperties.J2EE_SERVER_INSTANCE));
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "" + ex);
            }
        }
        
        @Override
        protected void projectClosed() {
            List<ClassPath[]> tmplist = new ArrayList<ClassPath[]>();
            for (ClassPath[] aofcp : paths) {
                GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, aofcp);
                GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, aofcp);
                tmplist.add(aofcp);
            }
            for (ClassPath[] aofcp : tmplist) {
                paths.remove(aofcp);
            }
        }
        
    }
    
    // TODO - implement when there is a customizer for the project.
    class ProjectXmlSaved extends org.netbeans.spi.project.support.ant.ProjectXmlSavedHook {
        
        @Override
        protected void projectXmlSaved() throws IOException {
            FileObject subDir = getProjectDirectory().getFileObject(ArchiveProjectProperties.TMP_PROJ_DIR_VALUE);
            if (subDir != null) {
                Project tmpproj = null;
                try {
                    tmpproj = ProjectManager.getDefault().findProject(subDir);
                } catch (IllegalArgumentException ex) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "" + ex);
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "" + ex);
                }
                ProvidesJ2eeModule tmp = (ProvidesJ2eeModule) ArchiveProject.this.getLookup().lookup(ProvidesJ2eeModule.class);
                if (null!=tmpproj) {
                    //J2eeModuleProvider jmp = (J2eeModuleProvider) tmpproj.getLookup().lookup(J2eeModuleProvider.class);
                    tmp.setServerInstanceID((String) getArchiveProjectProperties().get(ArchiveProjectProperties.J2EE_SERVER_INSTANCE));
                }
            }
        }
    }
    
    
    /** Return configured project name. */
    public String getNamedProjectAttribute(final String attr) {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            @Override
            public String run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, attr);
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }
    
    /** Store configured project name. */
    public void setNamedProjectAttribute(final String attr, final String value) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
            @Override
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, attr);
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, attr);
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(value));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    private class J2eeModuleForAddModuleAction implements J2eeModuleImplementation2 {
        
        private final J2eeModule.Type mt;
        
        J2eeModuleForAddModuleAction(J2eeModule.Type mt) {
            //super(null);
            this.mt = mt;
        }
        
        @Override
        public String getModuleVersion() {
            return J2eeModule.JAVA_EE_5; // throw new UnsupportedOperationException();
        }
        
        @Override
        public J2eeModule.Type getModuleType() {
            return mt;
        }
        
        @Override
        public String getUrl() {
            //throw new UnsupportedOperationException();
            return null;
        }
        
        public void setUrl(String url) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public FileObject getArchive() throws IOException {
            FileObject distDir = getProjectDirectory().getFileObject("dist");
            FileObject kids[] = distDir.getChildren();
            FileObject retVal = null;
            if (null != kids && kids.length == 1){
                retVal = kids[0];
            } else if (null != kids && kids.length > 1) {
                for (FileObject kid : kids) {
                    if (kid.isData() && kid.getNameExt().endsWith("ar")) {
                        retVal = kid;
                    }
                }
            }
            
            return retVal;
        }
        
        @Override
        public Iterator getArchiveContents() throws IOException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public FileObject getContentDirectory() throws IOException {
            return null;
        }
        
        // TODO MetadataModel:
        @Override
        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            throw new NotImplementedException();
        }
// TODO MetadataModel:
//        public RootInterface getDeploymentDescriptor(String location) {
//            RootInterface retVal = null;
//            if ("META-INF/application.xml".equals(location)) {
//                String dir = (String)getArchiveProjectProperties().get(ArchiveProjectProperties.PROXY_PROJECT_DIR);
//                FileObject appFile = getProjectDirectory().getFileObject(dir).getFileObject("src").getFileObject("conf").getFileObject("application.xml");
////                Application appBean;
//                try {
//                    retVal = org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(appFile);
//                } catch (IOException ex) {
//                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "" + ex);
//                }
//            } else if ("META-INF/ra.xml".equals(location)) {
//                retVal =  null;
//            } else {
//                throw new UnsupportedOperationException(location);
//            }
//            return retVal;
//        }
        
        @Override
        public File getResourceDirectory() {
            return new File(FileUtil.toFile(getProjectDirectory()),ArchiveProjectProperties.SETUP_DIR_VALUE);
        }

        @Override
        public File getDeploymentConfigurationFile(String name) {
            File retVal;
            
            String dir = (String)getArchiveProjectProperties().get(ArchiveProjectProperties.PROXY_PROJECT_DIR);
            FileObject parent = getProjectDirectory().getFileObject(dir).getFileObject("src").getFileObject("conf");
            int slashDex = name.lastIndexOf("/");
            String fname;
            if (-1 < slashDex) {
                fname = name.substring(slashDex);
            } else {
                fname = name;
            }
            FileObject appFile = parent.getFileObject(fname);
            if (null == appFile) {
                retVal = new File(FileUtil.toFile(parent), fname); // NOI18N
            } else {
                retVal = FileUtil.toFile(appFile);
            }
            return retVal;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
                throw new UnsupportedOperationException();
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
                throw new UnsupportedOperationException();
        }
    }
    
    
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        RecommendedTemplatesImpl() {
        }
        
        // List of primarily supported templates
        
        private static final String[] APPLICATION_TYPES = new String[] {
            "sunresource-types", // NOI18N
            "XML",                  // NOI18N
            "simple-files"          // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/SunResources/JDBC_Connection_Pool", // NOI18N
            "Templates/SunResources/JDBC_Resource", // NOI18N
            "Templates/SunResources/JMS_Resource", // NOI18N
            "Templates/SunResources/JavaMail_Resource", // NOI18N
            "Templates/Persistence/Schema.dbschema", // NOI18N
        };
        
        @Override
        public String[] getRecommendedTypes() {
            return APPLICATION_TYPES;
        }
        
        @Override
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
    class MyOpenHook {
        
        public void regenerateBuildFiles() {
            doRegeneration();
        }
    }
    
    private void doRegeneration() {
        
        GeneratedFilesHelper gFH = new GeneratedFilesHelper(helper);
        ArchiveProjectProperties app = getArchiveProjectProperties();
        String sourceArchive = eval.evaluate((String)app.get(ArchiveProjectProperties.SOURCE_ARCHIVE));
        try {
            if (sourceArchive.endsWith("war"))                              //NOI18N
                gFH.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        ArchiveProject.class.getResource("resources/build-impl-war.xsl"),
                        true);
            else
                // ejb-jar/connector/appclient
                gFH.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        ArchiveProject.class.getResource("resources/build-impl.xsl"),
                        true);
        } catch (IllegalStateException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "IllegalStateException while opening project: " + ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "IOException while opening project: " + ex);
        }
        try {
            gFH.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    ArchiveProject.class.getResource("resources/build.xsl"),
                    true);
        } catch (IllegalStateException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "IllegalStateException while opening project: " + ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "IOException while opening project: " + ex);
        }
        
        String servInstID = (String) app.get(ArchiveProjectProperties.J2EE_SERVER_INSTANCE);
        J2eePlatform platform = null;
        try {
            platform = Deployment.getDefault().getServerInstance(servInstID).getJ2eePlatform();
        } catch (InstanceRemovedException ex) {
            Logger.getLogger("global").log(Level.INFO, servInstID, ex);
        }
        if (platform != null) {
            // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
            ArchiveProjectProperties.setServerInstance(ArchiveProject.this,
                    ArchiveProject.this.helper, servInstID);
        } else {
            // if there is some server instance of the type which was used
            // previously do not ask and use it
            String serverType = (String) app.get(ArchiveProjectProperties.J2EE_SERVER_TYPE);
            if (serverType != null) {
                String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                if (servInstIDs.length > 0) {
                    ArchiveProjectProperties.setServerInstance(ArchiveProject.this, ArchiveProject.this.helper, servInstIDs[0]);
                    try {
                        platform = Deployment.getDefault().getServerInstance(servInstIDs[0]).getJ2eePlatform();
                    } catch (InstanceRemovedException ex) {
                        Logger.getLogger("global").log(Level.INFO, servInstIDs[0], ex);
                    }
                }
            }
            if (platform == null) {
                BrokenServerSupport.showAlert();
            }
        }
        
        // Force the tmpproj and subarchive/tmpproj's  to initialize
        
        // create the setupdirectory if ther isn't one in the project.
        FileObject dir = helper.getProjectDirectory();
        try {
            FileUtil.createFolder(dir,ArchiveProjectProperties.SETUP_DIR_VALUE);
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while opening project: " + ex);
        }
    }
}


