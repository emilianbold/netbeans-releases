/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.modules.ruby.rubyproject.RSpecSupport;

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
        if (helper != null && helper.getRakeProjectHelper() != null) {
            showRSpec = new RSpecSupport(helper.getRakeProjectHelper().getProjectDirectory(), null).isRSpecInstalled();
        }
    }
    
    private String getNodeDescription(String key) {
        return NbBundle.getMessage(SourceRoots.class, key); // NOI18N
    }
    
    private void initializeRoots() {
        synchronized (SourceRoots.this) {
            if (sourceRootNames == null) {
                if (FoldersListSettings.getDefault().getLogicalView()) {
                    initializeRootsLogical();
                } else {
                    initializeRootsFiles();
                }
            }
        }
    }
    
    /** Create a logical view of the project: flatten app/ and test/
     * and substitute logical names instead of the directory names
     */
    private void initializeRootsLogical() {
        plainFiles = new ArrayList<FileObject>(20);

        FileObject fo = helper.getRakeProjectHelper().getProjectDirectory();
        
        FileObject rakefile = fo.getFileObject("Rakefile");
        if (rakefile != null) {
            plainFiles.add(rakefile);
        }
        FileObject readme = fo.getFileObject("README");
        if (readme != null) {
            plainFiles.add(readme);
        }
        
        assert sourceRootNames == null;
        assert sourceRootProperties == null;
        sourceRootNames = new ArrayList<String>(20);
        sourceRootProperties = new ArrayList<String>(20);
        // Note Keep list in sync with root properties list below
        sourceRootNames.add(getNodeDescription("app_controllers")); // NOI18N
        sourceRootNames.add(getNodeDescription("app_helpers")); // NOI18N
        sourceRootNames.add(getNodeDescription("app_models")); // NOI18N
        sourceRootNames.add(getNodeDescription("app_views")); // NOI18N
        sourceRootProperties.add("app/controllers"); // NOI18N
        sourceRootProperties.add("app/helpers"); // NOI18N
        sourceRootProperties.add("app/models"); // NOI18N
        sourceRootProperties.add("app/views"); // NOI18N

        // Add in other dirs we don't know about
        FileObject app = fo.getFileObject("app");
        if (app != null) {
            Set<String> knownAppDirs = new HashSet<String>();
            knownAppDirs.add("controllers");
            knownAppDirs.add("helpers");
            knownAppDirs.add("models");
            knownAppDirs.add("views");
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
        sourceRootNames.add(getNodeDescription("test_unit")); // NOI18N
        sourceRootNames.add(getNodeDescription("test_functional")); // NOI18N
        sourceRootNames.add(getNodeDescription("test_fixtures")); // NOI18N
        sourceRootNames.add(getNodeDescription("test_mocks")); // NOI18N
        sourceRootNames.add(getNodeDescription("test_integration")); // NOI18N
        sourceRootProperties.add("test/unit"); // NOI18N
        sourceRootProperties.add("test/functional"); // NOI18N
        sourceRootProperties.add("test/fixtures"); // NOI18N
        sourceRootProperties.add("test/mocks"); // NOI18N
        sourceRootProperties.add("test/integration"); // NOI18N

        sourceRootNames.add(getNodeDescription("script")); // NOI18N
        sourceRootProperties.add("script"); // NOI18N
        sourceRootNames.add(getNodeDescription("doc")); // NOI18N
        sourceRootProperties.add("doc"); // NOI18N
        
        // Add in other test dirs we don't know about
        FileObject test = fo.getFileObject("test");
        if (test != null) {
            Set<String> knownTestDirs = new HashSet<String>();
            knownTestDirs.add("unit");
            knownTestDirs.add("functional");
            knownTestDirs.add("fixtures");
            knownTestDirs.add("mocks");
            knownTestDirs.add("integration");
            List<String> missing = findUnknownFolders(test, knownTestDirs);
            if (missing != null) {
                for (String name : missing) {
                    String combinedName = "test/" + name; // NOI18N
                    sourceRootNames.add(combinedName);
                    sourceRootProperties.add(combinedName);
                }
            }
        }

        sourceRootNames.add(getNodeDescription("vendor")); // NOI18N
        sourceRootProperties.add("vendor"); // NOI18N

        // Add in other top-level dirs we don't know about
        if (fo != null) {
            Set<String> knownTopDirs = new HashSet<String>();
            // Deliberately hidden
            knownTopDirs.add("nbproject");
            knownTopDirs.add("tmp");

            knownTopDirs.add("app");
            knownTopDirs.add("components");
            knownTopDirs.add("config");
            knownTopDirs.add("db");
            knownTopDirs.add("lib");
            knownTopDirs.add("log");
            knownTopDirs.add("public");
            knownTopDirs.add("spec");
            knownTopDirs.add("lib");
            knownTopDirs.add("test");
            knownTopDirs.add("doc");
            knownTopDirs.add("script");
            knownTopDirs.add("vendor");

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
    private List<String> findUnknownFolders(FileObject folder, Set<String> known) {
        List<String> result = null;
        for (FileObject child : folder.getChildren()) {
            if (child.isFolder()) {
                String name = child.getNameExt();
                if (!known.contains(name) && VisibilityQuery.getDefault().isVisible(child)) {
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
    
    /** Initialize source roots to just match the Rails view.
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
        assert sourceRootNames == null;
        sourceRootNames = new ArrayList<String>(20);
        assert sourceRootProperties == null;
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
                    initializeRoots();
                }
                assert sourceRootNames != null;
                return sourceRootNames.toArray (new String[sourceRootNames.size()]);
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
                    initializeRoots();
                }
                assert sourceRootProperties != null;
                return sourceRootProperties.toArray (new String[sourceRootProperties.size()]);
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
                    }
                    assert sourceRoots != null;
                    return sourceRoots.toArray(new FileObject[sourceRoots.size()]);
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
                    }
                    assert plainFiles != null;
                    return plainFiles.toArray(new FileObject[plainFiles.size()]);
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


//    /**
//     * Replaces the current roots by the new ones
//     * @param roots the URLs of new roots
//     * @param labels the names of roots
//     */
//    public void putRoots (final URL[] roots, final String[] labels) {
//        ProjectManager.mutex().writeAccess(
//                new Mutex.Action<Void>() {
//                    public Void run() {
//                        String[] originalProps = getRootProperties();
//                        URL[] originalRoots = getRootURLs();
//                        Map<URL,String> oldRoots2props = new HashMap<URL,String>();
//                        for (int i=0; i<originalProps.length;i++) {
//                            oldRoots2props.put (originalRoots[i],originalProps[i]);
//                        }
//                        Map<URL,String> newRoots2lab = new HashMap<URL,String>();
//                        for (int i=0; i<roots.length;i++) {
//                            newRoots2lab.put (roots[i],labels[i]);
//                        }
//                        Element cfgEl = helper.getPrimaryConfigurationData(true);
//                        NodeList nl = cfgEl.getElementsByTagNameNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);
//                        assert nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
//                        Element ownerElement = (Element) nl.item(0);
//                        //Remove all old roots
//                        NodeList rootsNodes = ownerElement.getElementsByTagNameNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");    //NOI18N
//                        while (rootsNodes.getLength()>0) {
//                            Element root = (Element) rootsNodes.item(0);
//                            ownerElement.removeChild(root);
//                        }
//                        //Remove all unused root properties
//                        List<URL> newRoots = Arrays.asList(roots);
//                        Map<URL,String> propsToRemove = new HashMap<URL,String>(oldRoots2props);
//                        propsToRemove.keySet().removeAll(newRoots);
//                        EditableProperties props = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
//                        props.keySet().removeAll(propsToRemove.values());
//                        helper.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH,props);
//                        //Add the new roots
//                        Document doc = ownerElement.getOwnerDocument();
//                        oldRoots2props.keySet().retainAll(newRoots);
//                        for (URL newRoot : newRoots) {
//                            String rootName = oldRoots2props.get(newRoot);
//                            if (rootName == null) {
//                                //Root is new generate property for it
//                                props = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
//                                String[] names = newRoot.getPath().split("/");  //NOI18N
//                                rootName = MessageFormat.format(newRootNameTemplate, new Object[] {names[names.length - 1], ""}); // NOI18N
//                                int rootIndex = 1;
//                                while (props.containsKey(rootName)) {
//                                    rootIndex++;
//                                    rootName = MessageFormat.format(newRootNameTemplate, new Object[] {names[names.length - 1], rootIndex});
//                                }
//                                File f = FileUtil.normalizeFile(new File(URI.create(newRoot.toExternalForm())));
//                                File projDir = FileUtil.toFile(helper.getRakeProjectHelper().getProjectDirectory());
//                                String path = f.getAbsolutePath();
//                                String prjPath = projDir.getAbsolutePath()+File.separatorChar;
//                                if (path.startsWith(prjPath)) {
//                                    path = path.substring(prjPath.length());
//                                }
//                                else {
//                                    path = refHelper.createForeignFileReference(f, RailsProject.SOURCES_TYPE_RUBY);
//                                    props = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
//                                }
//                                props.put(rootName,path);
//                                helper.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH,props);
//                            }
//                            Element newRootNode = doc.createElementNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); //NOI18N
//                            newRootNode.setAttribute("id",rootName);    //NOI18N
//                            String label = (String) newRoots2lab.get (newRoot);
//                            if (label != null && label.length()>0 && !label.equals (getRootDisplayName(null,rootName))) { //NOI18N
//                                newRootNode.setAttribute("name",label); //NOI18N
//                            }
//                            ownerElement.appendChild (newRootNode);
//                        }
//                        helper.putPrimaryConfigurationData(cfgEl,true);
//                        return null;
//                    }
//                }
//        );
//    }
    
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
