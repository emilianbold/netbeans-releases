/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.modules.java.freeform.JavaProjectNature;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;

/**
 * Memory model of project. Used for creation or customization of project.
 *
 * @author David Konecny
 */
public class ProjectModel  {
    
    /** Original project base folder */
    private File baseFolder;
    
    /** Freeform Project base folder */
    private File nbProjectFolder;

    private PropertyEvaluator evaluator;
    
    private String sourceLevel;
    
    /** List of JavaProjectGenerator.SourceFolders instances of type "java". */
    private List/*<JavaProjectGenerator.SourceFolder>*/ sourceFolders;
    
    /** List of <JavaProjectGenerator.JavaCompilationUnit> */
    public List/*<JavaProjectGenerator.JavaCompilationUnit>*/ javaCompilationUnitsList;

    private Set/*<String>*/ addedSourceFolders;
    private Set/*<String>*/ removedSourceFolders;
    
    public static final String TYPE_JAVA = "java"; // NOI18N
    public static final String CLASSPATH_MODE_COMPILE = "compile"; // NOI18N
    //Upper bound of sourse level supported by the java freeform project
    private static final SpecificationVersion JDK_MAX_SUPPORTED_VERSION = new SpecificationVersion ("1.5"); //NOI18N
    
    private ProjectModel(File baseFolder, File nbProjectFolder, PropertyEvaluator evaluator, List sourceFolders, List compUnits) {
        this.baseFolder = baseFolder;
        this.nbProjectFolder = nbProjectFolder;
        this.evaluator = evaluator;
        this.sourceFolders = sourceFolders;
        this.javaCompilationUnitsList = compUnits;
        if (javaCompilationUnitsList.size() > 0) {
            sourceLevel = ((JavaProjectGenerator.JavaCompilationUnit)javaCompilationUnitsList.get(0)).sourceLevel;
        }
        if (sourceLevel == null) {
            setSourceLevel(getDefaultSourceLevel());
        }
        resetState();
    }
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * Notifies only about change in source folders and compilation units.
     */
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    private void resetState() {
        addedSourceFolders = new HashSet();
        removedSourceFolders = new HashSet();
    }

    /** Create empty project model. Useful for new project creation. */
    public static ProjectModel createEmptyModel(File baseFolder, File nbProjectFolder, PropertyEvaluator evaluator) {
        return new ProjectModel(baseFolder, nbProjectFolder, evaluator, new ArrayList(), new ArrayList());
    }

    /** Create project model of existing project. Useful for project customization. */
    public static ProjectModel createModel(final File baseFolder, final File nbProjectFolder, final PropertyEvaluator evaluator, final AntProjectHelper helper) {
        return (ProjectModel) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                ProjectModel pm = new ProjectModel(
                        baseFolder, 
                        nbProjectFolder, 
                        evaluator,
                        // reads only "java" type because other types are not editable in UI
                        JavaProjectGenerator.getSourceFolders(helper, TYPE_JAVA),
                        JavaProjectGenerator.getJavaCompilationUnits(helper,
                            Util.getAuxiliaryConfiguration(helper))
                    );
                // only "java" type of sources was read so fix style to "pacakges" on all
                updateStyle(pm.sourceFolders);
                return pm;
            }
        });
    }

    /** Instantiate project model as new Java project. */
    public static void instantiateJavaProject(AntProjectHelper helper, ProjectModel model) throws IOException {
        List sourceFolders = model.updatePrincipalSourceFolders(model.sourceFolders, true);

        if (sourceFolders.size() > 0) {
            JavaProjectGenerator.putSourceFolders(helper, sourceFolders, null);
        }
        if (sourceFolders.size() > 0) {
            JavaProjectGenerator.putSourceViews(helper, sourceFolders, null);
        }
        JavaProjectGenerator.putJavaCompilationUnits(helper, Util.getAuxiliaryConfiguration(helper), model.javaCompilationUnitsList);        
        List exports = JavaProjectGenerator.guessExports(model.evaluator, JavaProjectGenerator.getTargetMappings(helper), model.javaCompilationUnitsList);
        if (exports.size() > 0) {
            JavaProjectGenerator.putExports(helper, exports);
        }
        List subprojects = JavaProjectGenerator.guessSubprojects(model.evaluator, model.javaCompilationUnitsList, model.baseFolder, model.nbProjectFolder);
        if (subprojects.size() > 0) {
            JavaProjectGenerator.putSubprojects(helper, subprojects);
        }
        
        model.resetState();
    }
    
    /** Persist modifications of project. */
    public static void saveProject(final AntProjectHelper helper, final ProjectModel model) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                // stores only "java" type because other types was not read
                JavaProjectGenerator.putSourceFolders(helper, model.sourceFolders, TYPE_JAVA);
                JavaProjectGenerator.putSourceViews(helper, model.sourceFolders, JavaProjectNature.STYLE_PACKAGES);

                List sourceFolders = JavaProjectGenerator.getSourceFolders(helper, null);
                sourceFolders = model.updatePrincipalSourceFolders(sourceFolders, false);
                JavaProjectGenerator.putSourceFolders(helper, sourceFolders, null);

                AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
                JavaProjectGenerator.putJavaCompilationUnits(helper, aux, model.javaCompilationUnitsList);
                model.resetState();

                List exports = JavaProjectGenerator.guessExports(model.getEvaluator(), 
                    JavaProjectGenerator.getTargetMappings(helper), model.javaCompilationUnitsList);
                JavaProjectGenerator.putExports(helper, exports);

                List/*<String>*/ subprojects = JavaProjectGenerator.guessSubprojects(model.getEvaluator(), 
                    model.javaCompilationUnitsList, model.baseFolder, model.nbProjectFolder);
                JavaProjectGenerator.putSubprojects(helper, subprojects);
                
                List/*<String>*/ buildFolders = JavaProjectGenerator.guessBuildFolders(model.getEvaluator(), 
                    model.javaCompilationUnitsList, model.baseFolder, model.nbProjectFolder);
                JavaProjectGenerator.putBuildFolders(helper, buildFolders);
                
                return null;
            }
        });
    }

    /**
     * This method according to the state of removed/added source folders
     * will ensure that all added typed external source roots will have
     * corresponding principal source folder and that principal source
     * folders of all removed typed external source roots are removed too.
     * In addition it can add project base folder as principal root 
     * if it is external.
     *
     * It is expected that this method will be called before project instantiation
     * or before update of project's data. 
     *
     * @param allSourceFolders list of all source folders, i.e. typed and untyped.
     * @param checkProjectDir should project base folder be checked
     * and added as principal source folder if needed or not
     * @return copy of allSourceFolders items plus added principal source folders
     */
    /*private*/ List/*<JavaProjectGenerator.SourceFolder>*/ updatePrincipalSourceFolders(
            List/*<JavaProjectGenerator.SourceFolder>*/ allSourceFolders, boolean checkProjectDir) {
        List allSF = new ArrayList(allSourceFolders);
        Iterator it = addedSourceFolders.iterator();
        while (it.hasNext()) {
            String location = (String)it.next();
            
            if (!isExternalSourceRoot(location)) {
                continue;
            }
            
            boolean exist = false;
            String label = ""; // NOI18N
            Iterator it2 = allSF.iterator();
            while (it2.hasNext()) {
                JavaProjectGenerator.SourceFolder _sf = (JavaProjectGenerator.SourceFolder)it2.next();
                if (_sf.location.equals(location) && _sf.type == null) {
                    exist = true;
                    break;
                }
                if (_sf.location.equals(location) && _sf.type != null) {
                    // find some label to use
                    label = _sf.label;
                }
            }
            
            if (!exist) {
                JavaProjectGenerator.SourceFolder _sf = new JavaProjectGenerator.SourceFolder();
                _sf.location = location;
                _sf.label = label;
                allSF.add(_sf);
            }
        }
        
        it = removedSourceFolders.iterator();
        while (it.hasNext()) {
            String location = (String)it.next();
            
            if (!isExternalSourceRoot(location)) {
                continue;
            }
            
            Iterator it2 = allSF.iterator();
            while (it2.hasNext()) {
                JavaProjectGenerator.SourceFolder _sf = (JavaProjectGenerator.SourceFolder)it2.next();
                if (_sf.location.equals(location) && _sf.type == null) {
                    it2.remove();
                }
            }
        }
        
        if (checkProjectDir && !baseFolder.equals(nbProjectFolder)) {
            JavaProjectGenerator.SourceFolder gen = new JavaProjectGenerator.SourceFolder();
            gen.location = "${"+ProjectConstants.PROP_PROJECT_LOCATION+"}"; // NOI18N
            // XXX: uniquefy label
            gen.label = baseFolder.getName();
            allSF.add(gen);
        }
        
        return allSF;
    }

    private boolean isExternalSourceRoot(String location) {
        String baseFolder_ = baseFolder.getAbsolutePath();
        if (!baseFolder_.endsWith(File.separator)) {
            baseFolder_ += File.separatorChar;
        }
        String nbProjectFolder_ = nbProjectFolder.getAbsolutePath();
        if (!nbProjectFolder_.endsWith(File.separator)) {
            nbProjectFolder_ += File.separatorChar;
        }
        File f = Util.resolveFile(evaluator, baseFolder, location);
        if (f == null) {
            return false;
        }
        location = f.getAbsolutePath();
        return (!location.startsWith(baseFolder_) &&
                    !location.startsWith(nbProjectFolder_));
    }
    
    /** Original project base folder. */
    public File getBaseFolder() {
        return baseFolder;
    }
    
    /** NetBeans project folder. */
    public File getNBProjectFolder() {
        return nbProjectFolder;
    }
    
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    public int getSourceFoldersCount() {
        return sourceFolders.size();
    }
    
    public JavaProjectGenerator.SourceFolder getSourceFolder(int index) {
        return (JavaProjectGenerator.SourceFolder)sourceFolders.get(index);
    }
    
    public void moveSourceFolder(int fromIndex, int toIndex) {
        JavaProjectGenerator.SourceFolder sf = (JavaProjectGenerator.SourceFolder)sourceFolders.remove(fromIndex);
        sourceFolders.add(toIndex, sf);
    }
    
    public void addSourceFolder(JavaProjectGenerator.SourceFolder sf, boolean isTests) {
        List keys = createCompilationUnitKeys();
        boolean singleCU = isSingleCompilationUnit(keys);
        if (singleCU) {
            // Check that source being added is part of the compilation unit.
            // If it is not then switch to multiple compilation unit mode.
            JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)javaCompilationUnitsList.get(0);
            if (cu.isTests != isTests) {
                updateCompilationUnits(true);
                singleCU = false;
            }
        }
        sourceFolders.add(sf);
        if (singleCU) {
            if (TYPE_JAVA.equals(sf.type)) {
                // update existing single compilation unit
                JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)javaCompilationUnitsList.get(0);
                cu.packageRoots.add(sf.location);
            }
        } else {
            // make sure new compilation unit is created for the source folder
            Iterator it = createCompilationUnitKeys().iterator();
            while (it.hasNext()) {
                CompilationUnitKey key = (CompilationUnitKey)it.next();
                getCompilationUnit(key, isTests);
            }
        }
        // remember all added locations
        if (removedSourceFolders.contains(sf.location)) {
            removedSourceFolders.remove(sf.location);
        } else {
            addedSourceFolders.add(sf.location);
        }
        fireChangeEvent();
    }

    public void removeSourceFolder(int index) {
        JavaProjectGenerator.SourceFolder sf = (JavaProjectGenerator.SourceFolder)sourceFolders.get(index);
        if (TYPE_JAVA.equals(sf.type)) {
            removeSourceLocation(sf.location);
        }
        sourceFolders.remove(index);
        // remember all removed locations
        if (addedSourceFolders.contains(sf.location)) {
            addedSourceFolders.remove(sf.location);
        } else {
            removedSourceFolders.add(sf.location);
        }
        fireChangeEvent();
    }
    
    public void clearSourceFolders() {
        sourceFolders.clear();
        javaCompilationUnitsList.clear();
        fireChangeEvent();
    }
    
    public String getSourceLevel() {
        return sourceLevel;
    }

    public void setSourceLevel(String sourceLevel) {
        if ((this.sourceLevel == null && sourceLevel == null) ||
            (this.sourceLevel != null && this.sourceLevel.equals(sourceLevel))) {
            return;
        }
        this.sourceLevel = sourceLevel;
        Iterator it = javaCompilationUnitsList.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)it.next();
            cu.sourceLevel = sourceLevel;
        }
    }
    
    public boolean canHaveSeparateClasspath() {
        // if there is more than one source root or more than one
        // compilation unit then enable checkbox "Separate Classpath".
        return (sourceFolders.size() > 1 || javaCompilationUnitsList.size() > 1);
    }

    public boolean canCreateSingleCompilationUnit() {
        // if there are sources and test sources I cannot create
        // single compilation unit for them:
        boolean testCU = false;
        boolean sourceCU = false;
        Iterator it = javaCompilationUnitsList.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)it.next();
            if (cu.isTests) {
                testCU = true;
            } else {
                sourceCU = true;
            }
        }
        return !(testCU && sourceCU);
    }

    public static boolean isSingleCompilationUnit(List/*<ProjectModel.CompilationUnitKey>*/ compilationUnitKeys) {
        return compilationUnitKeys.size() == 1 &&
            ((ProjectModel.CompilationUnitKey)compilationUnitKeys.get(0)).label == null;
    }

    /**
     * This methos checks Java source foldes and compilation units and returns 
     * list of CompilationUnitKey which represent them. The problem solved by
     * this method is that although usually there is 1:1 mapping between
     * source folders and copilation units, there can be also N:1 mapping when
     * one classpath is used for all source folders. Also user's customization
     * of project.xml can result in other combinations and they cannot be
     * clobbered by opening such a project in UI.
     */
    public List/*<CompilationUnitKey>*/ createCompilationUnitKeys() {
        // XXX: cache result of this method?
        ArrayList l = new ArrayList();
        Iterator it = javaCompilationUnitsList.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)it.next();
            CompilationUnitKey cul = new CompilationUnitKey();
            cul.locations = cu.packageRoots;
            cul.label = null;
            l.add(cul);
        }
        it = sourceFolders.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.SourceFolder sf = (JavaProjectGenerator.SourceFolder)it.next();
            if (!TYPE_JAVA.equals(sf.type)) {
                continue;
            }
            CompilationUnitKey cul = new CompilationUnitKey();
            cul.locations = new ArrayList();
            cul.locations.add(sf.location);
            cul.label = sf.label;
            // try to find corresponding JavaCompilationUnit
            int index = l.indexOf(cul);
            if (index != -1) {
                // use this key intead because it has label
                CompilationUnitKey cul_ = (CompilationUnitKey)l.get(index);
                cul_.label = sf.label;
                continue;
            }
            // check whether this SourceFolder.location is not part of an existing JavaCompilationUnit
            boolean found = false;
            Iterator it2 = javaCompilationUnitsList.iterator();
            while (it2.hasNext()) {
                JavaProjectGenerator.JavaCompilationUnit cu_ = (JavaProjectGenerator.JavaCompilationUnit)it2.next();
                if (cu_.packageRoots.contains(sf.location)) {
                    // found: skip it
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            // add this source folder then:
            l.add(cul);
        }
        return l;
    }

    /**
     * Update compilation units to 1:1 or 1:N mapping to source folders.
     * The separateClasspath attribute if true says that each source folder
     * will have its own compilation unit. In opposite case all source
     * folders will have one compilation unit.
     */
    public void updateCompilationUnits(boolean separateClasspath) {
        if (separateClasspath) {
            // This means that there was one compilation unit for all sources.
            // So create compilation unit per source folder.
            String classpath = null;
            List output = null;
            // Copy classpath and output from the first compilation unit
            // to all compilation units - should be easier to customize for user.
            if (javaCompilationUnitsList.size() > 0) {
                List classpaths = ((JavaProjectGenerator.JavaCompilationUnit)javaCompilationUnitsList.get(0)).classpath;
                if (classpaths != null) {
                    // find first "compile" mode classpath and use it
                    Iterator it = classpaths.iterator();
                    while (it.hasNext()) {
                        JavaProjectGenerator.JavaCompilationUnit.CP cp = (JavaProjectGenerator.JavaCompilationUnit.CP)it.next();
                        if (cp.mode.equals(CLASSPATH_MODE_COMPILE)) {
                            classpath = cp.classpath;
                            break;
                        }
                    }
                }
                output = ((JavaProjectGenerator.JavaCompilationUnit)javaCompilationUnitsList.get(0)).output;
            }
            javaCompilationUnitsList.clear();
            Iterator it = sourceFolders.iterator();
            while (it.hasNext()) {
                JavaProjectGenerator.SourceFolder sf = (JavaProjectGenerator.SourceFolder)it.next();
                JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
                cu.packageRoots = new ArrayList();
                cu.packageRoots.add(sf.location);
                if (classpath != null) {
                    JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
                    cp.mode = CLASSPATH_MODE_COMPILE;
                    cp.classpath = classpath;
                    cu.classpath = new ArrayList();
                    cu.classpath.add(cp);
                }
                if (output != null) {
                    cu.output = new ArrayList();
                    cu.output.addAll(output);
                }
                cu.sourceLevel = sourceLevel;
                javaCompilationUnitsList.add(cu);
            }
        } else {
            // This means that there are some compilation units which should be
            // merged into one which will be used for all sources.
            List packageRoots = new ArrayList();
            // First list of source roots
            Iterator it = sourceFolders.iterator();
            while (it.hasNext()) {
                JavaProjectGenerator.SourceFolder sf = (JavaProjectGenerator.SourceFolder)it.next();
                packageRoots.add(sf.location);
            }
            // Now try to merge all classpaths and outputs. Might be easier to customize
            Set classpath = new LinkedHashSet();
            Set output = new LinkedHashSet();
            it = javaCompilationUnitsList.iterator();
            while (it.hasNext()) {
                JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)it.next();
                if (cu.output != null) {
                    output.addAll(cu.output);
                }
                if (cu.classpath != null) {
                    Iterator it2 = cu.classpath.iterator();
                    while (it2.hasNext()) {
                        JavaProjectGenerator.JavaCompilationUnit.CP cp = (JavaProjectGenerator.JavaCompilationUnit.CP)it2.next();
                        if (cp.mode.equals(CLASSPATH_MODE_COMPILE)) {
                            String[] cpa = PropertyUtils.tokenizePath(cp.classpath);
                            for (int i=0; i<cpa.length; i++) {
                                classpath.add(cpa[i]);
                            }
                        }
                    }
                }
            }
            javaCompilationUnitsList.clear();
            JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
            cu.packageRoots = packageRoots;
            JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
            if (classpath.size() > 0) {
                StringBuffer cp_ = new StringBuffer();
                it = classpath.iterator();
                while (it.hasNext()) {
                    String item = (String)it.next();
                    cp_.append(item);
                    if (it.hasNext()) {
                        cp_.append(File.pathSeparatorChar);
                    }
                }
                cp.classpath = cp_.toString();
                cp.mode = CLASSPATH_MODE_COMPILE;
                cu.classpath = new ArrayList();
                cu.classpath.add(cp);
            }
            cu.output = new ArrayList(output);
            cu.sourceLevel = sourceLevel;
            javaCompilationUnitsList.add(cu);
        }
        fireChangeEvent();
    }

    /** Retrieve compilation unit or create empty one if it does not exist yet for the given 
     * key which is source package path(s).
     * The isTests is used only to initialize newly created compilation unit.
     */
    public JavaProjectGenerator.JavaCompilationUnit getCompilationUnit(CompilationUnitKey key, boolean isTests) {
        Iterator it = javaCompilationUnitsList.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)it.next();
            if (cu.packageRoots.equals(key.locations)) {
                return cu;
            }
        }
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = key.locations;
        cu.sourceLevel = sourceLevel;
        cu.isTests = isTests;
        javaCompilationUnitsList.add(cu);
        return cu;
    }

    private void removeSourceLocation(String location) {
        Iterator it = javaCompilationUnitsList.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)it.next();
            if (cu.packageRoots.contains(location)) {
                cu.packageRoots.remove(location);
            }
            if (cu.packageRoots.size() == 0) {
                it.remove();
            }
        }
    }

    /** Update style of loaded source folders of type "java" to packages. */
    private static void updateStyle(List/*<JavaProjectGenerator.SourceFolder>*/ sources) {
        Iterator it = sources.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.SourceFolder sf = (JavaProjectGenerator.SourceFolder)it.next();
            assert sf.type.equals(TYPE_JAVA);
            sf.style = JavaProjectNature.STYLE_PACKAGES;
        }
    }
    
    // only for unit testing
    void setSourceFolders(List list) {
        sourceFolders = list;
    }
    
    // only for unit testing
    List getSourceFolders() {
        return sourceFolders;
    }
    
    // only for unit testing
    void setJavaCompilationUnits(List list) {
        javaCompilationUnitsList = list;
    }
    
    // only for unit testing
    List getJavaCompilationUnits() {
        return javaCompilationUnitsList;
    }
    
    
    /**
     * Helper method returning source level of the current platform.
     */
    public static String getDefaultSourceLevel() {
        JavaPlatform platform = JavaPlatform.getDefault();
        SpecificationVersion sv = platform.getSpecification().getVersion();
        if (sv.compareTo(JDK_MAX_SUPPORTED_VERSION)>0) {
            sv = JDK_MAX_SUPPORTED_VERSION;
        }
        return sv.toString();
    }
    
    public boolean isTestSourceFolder(int index) {
        return isTestSourceFolder(getSourceFolder(index));
    }
    
    public boolean isTestSourceFolder(JavaProjectGenerator.SourceFolder sf) {
        Iterator it = javaCompilationUnitsList.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)it.next();
            if (cu.packageRoots.contains(sf.location)) {
                return cu.isTests;
            }
        }
        return false;
    }
    
    public static class CompilationUnitKey {
        public List locations;
        public String label;
        
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof CompilationUnitKey)) {
                return false;
            }
            CompilationUnitKey cul = (CompilationUnitKey)o;
            return this.locations.equals(cul.locations);
        }
        
        public int hashCode() {
            return locations.hashCode()*7;
        }

        public String toString() {
            return "PM.CUK:[label="+label+", locations="+locations+", this="+super.toString()+"]"; // NOI18N
        }
    }
    
    
}
