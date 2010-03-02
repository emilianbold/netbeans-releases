/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.railsprojects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.ruby.railsprojects.ui.FoldersListSettings;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectEvent;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectListener;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;

/**
 * This class represents a project source roots. It is used to obtain roots as Ant properties, FileObject's
 * or URLs.
 * @author Tomas Zezula
 */
public final class SourceRoots {
    
    public static final String PROP_ROOT_PROPERTIES = "rootProperties";    //NOI18N
    public static final String PROP_ROOTS = "roots";   //NOI18N

    public static final String DEFAULT_SOURCE_LABEL = NbBundle.getMessage(SourceRoots.class, "NAME_src.dir");
    public static final String DEFAULT_TEST_LABEL = NbBundle.getMessage(SourceRoots.class, "NAME_test.src.dir");

    private boolean showRSpec;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    private final String elementName;
    private final String newRootNameTemplate;
    private List<String> sourceRootProperties;
    private List<String> sourceRootNames;
    private List<FileObject> sourceRoots;
    private List<FileObject> plainFiles;
    private List<URL> sourceRootURLs;
    private final PropertyChangeSupport support;
    private final ProjectMetadataListener listener;
    private final boolean isTest;
    private final File projectDir;

    /**
     * Creates new SourceRoots
     * @param helper
     * @param evaluator
     * @param elementName the name of XML element under which are declared the roots
     * @param newRootNameTemplate template for new property name of source root
     */
    SourceRoots (UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper, String elementName, boolean isTest, String newRootNameTemplate) {
        assert helper != null && evaluator != null && refHelper != null && elementName != null && newRootNameTemplate != null;
        this.helper = helper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.elementName = elementName;
        this.isTest = isTest;
        this.newRootNameTemplate = newRootNameTemplate;        
        this.projectDir = FileUtil.toFile(this.helper.getRakeProjectHelper().getProjectDirectory());
        this.support = new PropertyChangeSupport(this);
        this.listener = new ProjectMetadataListener();
        this.evaluator.addPropertyChangeListener (WeakListeners.propertyChange(this.listener,this.evaluator));
        this.helper.getRakeProjectHelper().addRakeProjectListener (WeakListeners.create(RakeProjectListener.class, this.listener,this.helper));
        //if (helper != null && helper.getRakeProjectHelper() != null) {
        //    showRSpec = new RSpecSupport(/*helper.getRakeProjectHelper().getProjectDirectory(),*/ null).isRSpecInstalled();
        //}
        showRSpec = true;
    }
    
    private String getNodeDescription(String key) {
        return NbBundle.getMessage(SourceRoots.class, key); // NOI18N
    }
    
    private void initializeRoots() {
        synchronized (this) {
            if (sourceRoots == null) {
                if (isTest) {
                    initializeTestRoots();
                } else if (FoldersListSettings.getDefault().getLogicalView()) {
                    initializeRootsLogical();
                } else {
                    initializeRootsFiles();
                }
            }
        }
    }

    private void initializeTestRoots() {
        sourceRootNames = new ArrayList<String>();
        sourceRootProperties = new ArrayList<String>();
        if (showRSpec) {
            sourceRootNames.add(getNodeDescription("rspec")); // NOI18N
            sourceRootProperties.add("spec"); // NOI18N
        }

        sourceRootNames.add(getNodeDescription("test")); // NOI18N
        sourceRootProperties.add("test"); // NOI18N
        List<FileObject> result = new ArrayList<FileObject>();
        for (String p : sourceRootProperties) {
            FileObject f = helper.getRakeProjectHelper().resolveFileObject(p);
            if (f == null) {
                continue;
            }
            if (FileUtil.isArchiveFile(f)) {
                f = FileUtil.getArchiveRoot(f);
            }
            result.add(f);
        }
        sourceRoots = Collections.unmodifiableList(result);
    }

    private void addPlainFiles(FileObject dir, String... fileNames) {
        plainFiles = new ArrayList<FileObject>(20);
        for (String fileName : fileNames) {
            FileObject toAdd = dir.getFileObject(fileName);
            if (toAdd != null) {
                plainFiles.add(toAdd);
            }
        }
    }

    /** Create a logical view of the project: flatten app/ and test/
     * and substitute logical names instead of the directory names
     */
    private void initializeRootsLogical() {

        FileObject fo = helper.getRakeProjectHelper().getProjectDirectory();
        addPlainFiles(fo, "Capfile", "Gemfile", "Rakefile", "README");

        // show app/metal for Rack applications, but only if the folder already exists
        boolean metal = fo.getFileObject("app/metal") != null;//NOI18N

        sourceRootNames = new ArrayList<String>(20);
        sourceRootProperties = new ArrayList<String>(20);
        // Note Keep list in sync with root properties list below
        sourceRootNames.add(getNodeDescription("app_controllers")); // NOI18N
        sourceRootNames.add(getNodeDescription("app_helpers")); // NOI18N
        if (metal) {
            sourceRootNames.add(getNodeDescription("app_metal")); // NOI18N
        }
        sourceRootNames.add(getNodeDescription("app_models")); // NOI18N
        sourceRootNames.add(getNodeDescription("app_views")); // NOI18N
        sourceRootProperties.add("app/controllers"); // NOI18N
        sourceRootProperties.add("app/helpers"); // NOI18N
        if (metal) {
            sourceRootProperties.add("app/metal"); // NOI18N
        }
        sourceRootProperties.add("app/models"); // NOI18N
        sourceRootProperties.add("app/views"); // NOI18N

        // Add in other dirs we don't know about
        FileObject app = fo.getFileObject("app"); // NOI18N
        if (app != null) {
            Set<String> knownAppDirs = new HashSet<String>();
            knownAppDirs.add("controllers"); // NOI18N
            knownAppDirs.add("helpers"); // NOI18N
            knownAppDirs.add("models"); // NOI18N
            knownAppDirs.add("views"); // NOI18N
            List<String> missing = findUnknownFolders(app, knownAppDirs);
            if (missing != null) {
                for (String name : missing) {
                    String combinedName = "app/" + name; // NOI18N
                    sourceRootNames.add(combinedName);
                    sourceRootProperties.add(combinedName);
                }
            }
        }

        sourceRootNames.add(getNodeDescription("components")); // NOI18N
        sourceRootProperties.add("components"); // NOI18N
        sourceRootNames.add(getNodeDescription("config")); // NOI18N
        sourceRootProperties.add("config"); // NOI18N
        sourceRootNames.add(getNodeDescription("db")); // NOI18N
        sourceRootProperties.add("db"); // NOI18N
        sourceRootNames.add(getNodeDescription("lib")); // NOI18N
        sourceRootProperties.add("lib"); // NOI18N
        sourceRootNames.add(getNodeDescription("log")); // NOI18N
        sourceRootProperties.add("log"); // NOI18N
        sourceRootNames.add(getNodeDescription("public")); // NOI18N
        sourceRootProperties.add("public"); // NOI18N
        if (showRSpec) {
            sourceRootNames.add(getNodeDescription("rspec")); // NOI18N
            sourceRootProperties.add("spec"); // NOI18N
        }

        sourceRootNames.add(getNodeDescription("test")); // NOI18N
        sourceRootProperties.add("test"); // NOI18N
        sourceRootNames.add(getNodeDescription("script")); // NOI18N
        sourceRootProperties.add("script"); // NOI18N
        sourceRootNames.add(getNodeDescription("doc")); // NOI18N
        sourceRootProperties.add("doc"); // NOI18N
        
        // Vendor is treated specially. 
        // It should be split up into multiple roots that are indexed
        // as platform (not as sources, thus not rescanned on subsequent startups,
        // and possibly pulling in preindexed libraries).
        sourceRootNames.add(getNodeDescription("vendor")); // NOI18N
        sourceRootProperties.add("vendor"); // NOI18N

        // Add in other top-level dirs we don't know about
        if (fo != null) {
            Set<String> knownTopDirs = new HashSet<String>();
            // Deliberately hidden
            knownTopDirs.add("nbproject"); // NOI18N
            knownTopDirs.add("tmp"); // NOI18N

            knownTopDirs.add("app"); // NOI18N
            knownTopDirs.add("components"); // NOI18N
            knownTopDirs.add("config"); // NOI18N
            knownTopDirs.add("db"); // NOI18N
            knownTopDirs.add("lib"); // NOI18N
            knownTopDirs.add("log"); // NOI18N
            knownTopDirs.add("public"); // NOI18N
            knownTopDirs.add("spec"); // NOI18N
            knownTopDirs.add("lib"); // NOI18N
            knownTopDirs.add("test"); // NOI18N
            knownTopDirs.add("doc"); // NOI18N
            knownTopDirs.add("script"); // NOI18N
            knownTopDirs.add("vendor"); // NOI18N

            List<String> missing = findUnknownFolders(fo, knownTopDirs);
            if (missing != null) {
                for (String name : missing) {
                    sourceRootNames.add(name);
                    sourceRootProperties.add(name);
                }
            }
        }
        
        //Local caching
        assert sourceRoots == null;
        List<FileObject> result = new ArrayList<FileObject>();
        for (String p : sourceRootProperties) {
            FileObject f = helper.getRakeProjectHelper().resolveFileObject(p);
            if (f == null) {
                continue;
            }
            if (FileUtil.isArchiveFile(f)) {
                f = FileUtil.getArchiveRoot(f);
            }
            result.add(f);
        }
        sourceRoots = Collections.unmodifiableList(result);
        
//        assert sourceRootNames.size() == sourceRootProperties.size() && 
//                sourceRootNames.size() == sourceRoots.size();
    }
    
    /** Look in the given directory and identify any folders we don't "know" about yet */
    private static List<String> findUnknownFolders(FileObject folder, Set<String> known) {
        List<String> result = null;
        for (FileObject child : folder.getChildren()) {
            if (child.isFolder()) {
                String name = child.getNameExt();
                if (!known.contains(name) && isVisible(child)) {
                    if (result == null) {
                        result = new ArrayList<String>();
                    }
                    
                    result.add(name);
                }
            }
        }
        
        if (result != null) {
            Collections.sort(result);
        }
        
        return result;
    }

    /**
     * XXX - copy-pasted from o.n.core.ui.options.filetypes.IgnoredFilesPreferences.
     * Take a look into {@link #isVisible()}.
     * <p/>
     * Default ignored files pattern. Pattern \.(cvsignore|svn|DS_Store) is covered by ^\..*$
     */
    private static final String DEFAULT_IGNORED_FILES = "^(CVS|SCCS|vssver.?\\.scc|#.*#|%.*%|_svn)$|~$|^\\.(?!htaccess$).*$"; //NOI18N

    private static boolean isVisible(final FileObject child) {
        // XXX should use VisibilityQuery#isVisible, but can't in this way.
        // See http://www.netbeans.org/nonav/issues/show_bug.cgi?id=119244
        return !child.getNameExt().matches(DEFAULT_IGNORED_FILES);
    }

    /**
     * Initialize source roots to just match the Rails view.
     * Note that my load path will be way wrong for unit test execution and such - and
     * possibly for require-indexing (for require completion)
     */
    private void initializeRootsFiles() {
        FileObject fo = helper.getRakeProjectHelper().getProjectDirectory();
        if (fo == null) {
            initializeRootsLogical();
            return;
        }

        assert sourceRoots == null;
        List<FileObject> result = new ArrayList<FileObject>(20);
        sourceRootNames = new ArrayList<String>(20);
        sourceRootProperties = new ArrayList<String>(20);
        plainFiles = new ArrayList<FileObject>(20);

        FileObject[] children = fo.getChildren();
        for (FileObject f : children) {
            if (!VisibilityQuery.getDefault().isVisible(f)) {
                continue;
            }
            if (FileUtil.isArchiveFile(f)) {
                f = FileUtil.getArchiveRoot(f);
            }
            if (f.isFolder()) {
                String name = f.getName();
                // Deliberately skipped
                if (name.equals("nbproject") || name.equals("tmp")) { // NOI18N
                    continue;
                }
                result.add(f);
            } else {
                plainFiles.add(f);
            }
        }

        // Sort files alphabetically
        Collections.sort(result, new Comparator<FileObject>() {
            public int compare(FileObject f1, FileObject f2) {
                return f1.getNameExt().compareTo(f2.getNameExt());
            }
        });

        for (FileObject f : result) {
                String name = f.getNameExt();
                sourceRootNames.add(name);
                sourceRootProperties.add(name);
        }
        sourceRoots = Collections.unmodifiableList(result);
    }
    
    /**
     * Returns the display names of soruce roots
     * The returned array has the same length as an array returned by the getRootProperties.
     * It may contain empty strings but not null.
     * @return an array of String
     */
    public String[] getRootNames () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {
            public String[] run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootNames == null) {
                        initializeRoots();
                    }
                    assert sourceRootNames != null;
                    return sourceRootNames.toArray(new String[sourceRootNames.size()]);
                }
            }
        });
                
    }

    /**
     * Returns names of Ant properties in the project.properties file holding the source roots.
     * @return an array of String
     */
    public String[] getRootProperties () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {
            public String[] run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootProperties == null) {
                        initializeRoots();
                    }
                    assert sourceRootProperties != null;
                    return sourceRootProperties.toArray(new String[sourceRootProperties.size()]);
                }
            }
        });
    }

    /**
     * Returns the source roots
     * @return an array of FileObject
     */
    public FileObject[] getRoots () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject[]>() {
                public FileObject[] run () {
                    synchronized (SourceRoots.this) {
                        initializeRoots();
                        assert sourceRoots != null;
                        return sourceRoots.toArray(new FileObject[sourceRoots.size()]);
                    }
                }
        });                
    }

    /**
     * Returns the extra files in the root dir (not corresponding to source folders)
     * @return an array of FileObject
     */
    public FileObject[] getExtraFiles () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject[]>() {
                public FileObject[] run () {
                    synchronized (SourceRoots.this) {
                        initializeRoots();
                        assert plainFiles != null;
                        return plainFiles.toArray(new FileObject[plainFiles.size()]);
                    }
                }
        });                
    }

    /**
     * Returns the source roots as URLs.
     * @return an array of URL
     */
    public URL[] getRootURLs() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<URL[]>() {
            public URL[] run () {
                synchronized (this) {
                    //Local caching
                    if (sourceRootURLs == null) {
                        String[] srcProps = getRootProperties();
                        List<URL> result = new ArrayList<URL>();
                        for (int i = 0; i<srcProps.length; i++) {
                            String prop = srcProps[i];
                            if (prop != null) {
                                File f = helper.getRakeProjectHelper().resolveFile(prop);
                                try {                                    
                                    URL url = f.toURI().toURL();
                                    if (!f.exists()) {
                                        url = new URL(url.toExternalForm() + "/"); // NOI18N
                                    }
                                    result.add(url);
                                } catch (MalformedURLException e) {
                                    ErrorManager.getDefault().notify(e);
                                }
                            }
                        }
                        sourceRootURLs = Collections.unmodifiableList(result);
                    }
                }
                return sourceRootURLs.toArray(new URL[sourceRootURLs.size()]);
            }
        });                
    }

    /**
     * Adds PropertyChangeListener
     * @param listener
     */
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    /**
     * Translates root name into display name of source/test root
     * @param rootName the name of root got from {@link SourceRoots#getRootNames}
     * @param propName the name of property the root is stored in
     * @return the label to be displayed
     */
    public String getRootDisplayName (String rootName, String propName) {
        if (rootName == null || rootName.length() ==0) {
//            //If the prop is src.dir use the default name
//            if (isTest && RailsProjectGenerator.DEFAULT_TEST_SRC_NAME.equals(propName)) {    //NOI18N
//                rootName = DEFAULT_TEST_LABEL;
//            }
//            else if (!isTest && RailsProjectGenerator.DEFAULT_SRC_NAME.equals(propName)) {   //NOI18N
//                rootName = DEFAULT_SOURCE_LABEL;
//            }
//            else {
                //If the name is not given, it should be either a relative path in the project dir
                //or absolute path when the root is not under the project dir
                String propValue = evaluator.getProperty(propName);
                File sourceRoot = propValue == null ? null : helper.getRakeProjectHelper().resolveFile(propValue);
                rootName = createInitialDisplayName(sourceRoot);                
            }
//        }
        return rootName;
    }
    
    /**
     * Creates initial display name of source/test root
     * @param sourceRoot the source root
     * @return the label to be displayed
     */
    public String createInitialDisplayName (File sourceRoot) {
        String rootName;
        if (sourceRoot != null) {
        String srPath = sourceRoot.getAbsolutePath();
        String pdPath = projectDir.getAbsolutePath() + File.separatorChar;
        if (srPath.startsWith(pdPath)) {
            rootName = srPath.substring(pdPath.length());
        }
        else {
            rootName = sourceRoot.getAbsolutePath();
        }
        }
        else {
            rootName = isTest ? DEFAULT_TEST_LABEL : DEFAULT_SOURCE_LABEL;
        }
        return rootName;
    }
    
    /** 
     * Returns true if this SourceRoots instance represents source roots belonging to
     * the tests compilation unit.
     * @return boolean
     */
    public boolean isTest () {
        return this.isTest;
    }

    private void resetCache (boolean isXMLChange, String propName) {
        boolean fire = false;
        synchronized (this) {
            //In case of change reset local cache
            if (isXMLChange) {
                this.sourceRootProperties = null;
                this.sourceRootNames = null;
                this.sourceRoots = null;
                this.sourceRootURLs = null;
                fire = true;
            } else if (propName == null || (sourceRootProperties != null && sourceRootProperties.contains(propName))) {
                this.sourceRoots = null;
                this.sourceRootURLs = null;
                fire = true;
            }
        }
        if (fire) {
            if (isXMLChange) {
                this.support.firePropertyChange (PROP_ROOT_PROPERTIES,null,null);
            }
            this.support.firePropertyChange (PROP_ROOTS,null,null);
        }
    }

//    private void readProjectMetadata () {
//        Element cfgEl = helper.getPrimaryConfigurationData(true);
//        NodeList nl = cfgEl.getElementsByTagNameNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);
//        assert nl.getLength() == 0 || nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
//        List<String> rootProps = new ArrayList<String>();
//        List<String> rootNames = new ArrayList<String>();
//        // It can be 0 in the case when the project is created by RailsProjectGenerator and not yet customized
//        if (nl.getLength()==1) {
//            NodeList roots = ((Element)nl.item(0)).getElementsByTagNameNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");    //NOI18N
//            for (int i=0; i<roots.getLength(); i++) {
//                Element root = (Element) roots.item(i);
//                String value = root.getAttribute("id");  //NOI18N
//                assert value.length() > 0 : "Illegal project.xml";
//                rootProps.add(value);
//                value = root.getAttribute("name");  //NOI18N
//                rootNames.add (value);
//            }
//        }
//        this.sourceRootProperties = Collections.unmodifiableList(rootProps);
//        this.sourceRootNames = Collections.unmodifiableList(rootNames);
//    }

    private class ProjectMetadataListener implements PropertyChangeListener,RakeProjectListener {

        public void propertyChange(PropertyChangeEvent evt) {
            resetCache (false,evt.getPropertyName());
        }

        public void configurationXmlChanged(RakeProjectEvent ev) {
            resetCache (true,null);
        }

        public void propertiesChanged(RakeProjectEvent ev) {
            //Handled by propertyChange
        }
    }

}
