/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.w3c.dom.Element;

/**
 * Handle classpaths for the freeform project.
 * Keeps three caches:
 * <ol>
 * <li>Classpaths registered when the project is opened. The same are unregistered
 *     when it is closed again, regardless of what might have changed since.
 * <li>A map from abstract compilation units (keyed by the literal text of the
 *     <code>&lt;package-root&gt;</code> elements) to implementations which do the
 *     actual listening and (re-)computation of roots.
 * <li>A map from actual package roots to the matching classpath.
 * </ol>
 * The complexity here is needed because
 * <ol>
 * <li>It is necessary to always unregister the exact same set of ClassPath objects
 *     you initially registered (even if some have since become invalid, etc.).
 *     Ideally, adding or removing whole paths would dynamically register or unregister
 *     them (if the project is currently open); the current code does not do this.
 * <li>A given ClassPath object must fire changes if its list of roots changes.
 * <li>It is necessary to return the same ClassPath object for the same FileObject.
 * </ol>
 * @author Jesse Glick
 */
final class Classpaths implements ClassPathProvider, AntProjectListener, PropertyChangeListener {
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance(Classpaths.class.getName());
    
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private AuxiliaryConfiguration aux;
    
    /**
     * Map from classpath types to maps from package roots to classpaths.
     */
    private final Map/*<String,Map<FileObject,Classpath>>*/ classpaths = new HashMap();
    
    /**
     * Map from classpath types to maps from lists of package root names to classpath impls.
     */
    private final Map/*<String,Map<List<String>,MutableClassPathImplementation>>*/ mutablePathImpls = new HashMap();
    
    private final Map/*<MutableClassPathImplementation, Classpath>*/ mutableClassPathImpl2ClassPath = new HashMap();
    
    /**
     * Map from classpath types to sets of classpaths we last registered to GlobalPathRegistry.
     */
    private Map/*<String,Set<Classpath>>*/ registeredClasspaths = null;
    
    public Classpaths(AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
        helper.addAntProjectListener(this);
        evaluator.addPropertyChangeListener(this);
    }
    
    public synchronized ClassPath findClassPath(FileObject file, String type) {
        Map/*<FileObject,ClassPath>*/ classpathsByType = (Map)classpaths.get(type);
        if (classpathsByType == null) {
            classpathsByType = new WeakHashMap();
            classpaths.put(type, classpathsByType);
        }
        // Check for cached value.
        Iterator it = classpathsByType.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            FileObject root = (FileObject)entry.getKey();
            if (root == file || FileUtil.isParentOf(root, file)) {
                // Already have it.
                return (ClassPath)entry.getValue();
            }
        }
        // Need to create it.
        Element java = aux.getConfigurationFragment("java-data", JavaProjectNature.NS_JAVA, true); // NOI18N
        if (java == null) {
            return null;
        }
        List/*<Element>*/ compilationUnits = Util.findSubElements(java);
        it = compilationUnits.iterator();
        while (it.hasNext()) {
            Element compilationUnitEl = (Element)it.next();
            assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
            List/*<FileObject>*/ packageRoots = findPackageRoots(helper, evaluator, compilationUnitEl);
            Iterator it2 = packageRoots.iterator();
            while (it2.hasNext()) {
                FileObject root = (FileObject)it2.next();
                if (root == file || FileUtil.isParentOf(root, file)) {
                    // Got it. Compute classpath and cache it (for each root).
                    ClassPath cp = getPath(compilationUnitEl, packageRoots, type);
                    it2 = packageRoots.iterator();
                    while (it2.hasNext()) {
                        FileObject root2 = (FileObject)it2.next();
                        classpathsByType.put(root2, cp);
                    }
                    return cp;
                }
            }
        }
        return null;
    }
    
    /** All classpath types we handle. */
    private static final String[] TYPES = {
        ClassPath.SOURCE,
        ClassPath.BOOT,
        ClassPath.EXECUTE,
        ClassPath.COMPILE,
    };

    /**
     * Called when project is opened.
     * Tries to find all compilation units and calculate all the paths needed
     * for each of them and register them all.
     */
    public synchronized void opened() {
        if (registeredClasspaths != null) {
            return;
        }
        Map/*<String,Set<Classpath>>*/ _registeredClasspaths = new HashMap();
        for (int i = 0; i < TYPES.length; i++) {
            String type = TYPES[i];
            _registeredClasspaths.put(type, new HashSet());
        }
        Element java = aux.getConfigurationFragment("java-data", JavaProjectNature.NS_JAVA, true); // NOI18N
        if (java == null) {
            return;
        }
        List/*<Element>*/ compilationUnits = Util.findSubElements(java);
        Iterator it = compilationUnits.iterator();
        while (it.hasNext()) {
            Element compilationUnitEl = (Element)it.next();
            assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
            // For each compilation unit, find the package roots first.
            List/*<FileObject>*/ packageRoots = findPackageRoots(helper, evaluator, compilationUnitEl);
            for (int i = 0; i < TYPES.length; i++) {
                String type = TYPES[i];
                // Then for each type, collect the classpath (creating it as needed).
                Map/*<FileObject,ClassPath>*/ classpathsByType = (Map)classpaths.get(type);
                if (classpathsByType == null) {
                    classpathsByType = new WeakHashMap();
                    classpaths.put(type, classpathsByType);
                }
                Set/*<ClassPath>*/ registeredClasspathsOfType = (Set) _registeredClasspaths.get(type);
                assert registeredClasspathsOfType != null;
                // Check if there is already a ClassPath registered to one of these roots.
                ClassPath cp = null;
                Iterator it2 = packageRoots.iterator();
                while (cp == null && it2.hasNext()) {
                    FileObject root = (FileObject)it2.next();
                    cp = (ClassPath)classpathsByType.get(root);
                }
                if (cp == null) {
                    // Nope. Calculate and register it now.
                    cp = getPath(compilationUnitEl, packageRoots, type);
                    it2 = packageRoots.iterator();
                    while (it2.hasNext()) {
                        FileObject root = (FileObject)it2.next();
                        classpathsByType.put(root, cp);
                    }
                }
                assert cp != null;
                registeredClasspathsOfType.add(cp);
            }
        }
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("classpaths for " + helper.getProjectDirectory() + ": " + classpaths);
        }
        // Don't do this before it is calculated, or a runtime error above might corrupt state:
        this.registeredClasspaths = _registeredClasspaths;
        // Register all of the classpaths we found.
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        for (int i = 0; i < TYPES.length; i++) {
            String type = TYPES[i];
            Set/*<ClassPath>*/ registeredClasspathsOfType = (Set)registeredClasspaths.get(type);
            gpr.register(type, (ClassPath[])registeredClasspathsOfType.toArray(new ClassPath[registeredClasspathsOfType.size()]));
        }
    }
    
    private synchronized void registerNewClasspath(String type, ClassPath cp) {
        if (registeredClasspaths == null) {
            return;
        }
        Set s = (Set)registeredClasspaths.get(type);
        s.add(cp);
        GlobalPathRegistry.getDefault().register(type, (ClassPath[])new ClassPath[]{cp});
    }
    
    /**
     * Called when project is closed.
     * Unregisters any previously registered classpaths.
     */
    public synchronized void closed() {
        if (registeredClasspaths == null) {
            return;
        }
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        for (int i = 0; i < TYPES.length; i++) {
            String type = TYPES[i];
            Set/*<ClassPath>*/ registeredClasspathsOfType = (Set)registeredClasspaths.get(type);
            gpr.unregister(type, (ClassPath[])registeredClasspathsOfType.toArray(new ClassPath[registeredClasspathsOfType.size()]));
        }
        registeredClasspaths = null;
    }
    
    private static List/*<String>*/ findPackageRootNames(Element compilationUnitEl) {
        List/*<String>*/ names = new ArrayList();
        Iterator it = Util.findSubElements(compilationUnitEl).iterator();
        while (it.hasNext()) {
            Element e = (Element) it.next();
            if (!e.getLocalName().equals("package-root")) { // NOI18N
                continue;
            }
            String location = Util.findText(e);
            names.add(location);
        }
        return names;
    }
    
    private static List/*<FileObject>*/ findPackageRoots(AntProjectHelper helper, PropertyEvaluator evaluator, List/*<String>*/ packageRootNames) {
        List/*<FileObject>*/ roots = new ArrayList(packageRootNames.size());
        Iterator it = packageRootNames.iterator();
        while (it.hasNext()) {
            String location = (String) it.next();
            String locationEval = evaluator.evaluate(location);
            if (locationEval != null) {
                File locationFile = helper.resolveFile(locationEval);
                FileObject locationFileObject = FileUtil.toFileObject(locationFile);
                if (locationFileObject != null) {
                    if (FileUtil.isArchiveFile(locationFileObject)) {
                        locationFileObject = FileUtil.getArchiveRoot(locationFileObject);
                    }
                    roots.add(locationFileObject);
                }
            }
        }
        return roots;
    }
    
    public static List/*<FileObject>*/ findPackageRoots(AntProjectHelper helper, PropertyEvaluator evaluator, Element compilationUnitEl) {
        return findPackageRoots(helper, evaluator, findPackageRootNames(compilationUnitEl));
    }
    
    private ClassPath getPath(Element compilationUnitEl, List/*<FileObject>*/ packageRoots, String type) {
        if (type.equals(ClassPath.SOURCE) || type.equals(ClassPath.COMPILE) ||
                type.equals(ClassPath.EXECUTE) || type.equals(ClassPath.BOOT)) {
            List/*<String>*/ packageRootNames = findPackageRootNames(compilationUnitEl);
            Map/*<List<String>,MutableClassPathImplementation>*/ mutablePathImplsByType = (Map) mutablePathImpls.get(type);
            if (mutablePathImplsByType == null) {
                mutablePathImplsByType = new HashMap();
                mutablePathImpls.put(type, mutablePathImplsByType);
            }
            MutableClassPathImplementation impl = (MutableClassPathImplementation) mutablePathImplsByType.get(packageRootNames);
            if (impl == null) {
                // XXX will it ever not be null?
                impl = new MutableClassPathImplementation(packageRootNames, type, compilationUnitEl);
                mutablePathImplsByType.put(packageRootNames, impl);
            }
            ClassPath cp = (ClassPath)mutableClassPathImpl2ClassPath.get(impl);
            if (cp == null) {
                cp = ClassPathFactory.createClassPath(impl);
                mutableClassPathImpl2ClassPath.put(impl, cp);
                registerNewClasspath(type, cp);
            }
            return cp;
        } else {
            // Unknown.
            return null;
        }
    }
    
    private List/*<URL>*/ createSourcePath(List/*<String>*/ packageRootNames) {
        List/*<URL>*/ roots = new ArrayList(packageRootNames.size());
        Iterator it = packageRootNames.iterator();
        while (it.hasNext()) {
            String location = (String) it.next();
            String locationEval = evaluator.evaluate(location);
            if (locationEval != null) {
                roots.add(createClasspathEntry(locationEval));
            }
        }
        return roots;
    }
    
    private List/*<URL>*/ createCompileClasspath(Element compilationUnitEl) {
        Iterator/*<Element>*/ it = Util.findSubElements(compilationUnitEl).iterator();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e.getLocalName().equals("classpath") && e.getAttribute("mode").equals("compile")) { // NOI18N
                return createClasspath(e);
            }
        }
        // None specified; assume it is empty.
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Create a classpath from a &lt;classpath&gt; element.
     */
    private List/*<URL>*/ createClasspath(Element classpathEl) {
        String cp = Util.findText(classpathEl);
        if (cp == null) {
            cp = "";
        }
        String cpEval = evaluator.evaluate(cp);
        if (cpEval == null) {
            return null;
        }
        String[] path = PropertyUtils.tokenizePath(cpEval);
        URL[] pathURL = new URL[path.length];
        for (int i = 0; i < path.length; i++) {
            pathURL[i] = createClasspathEntry(path[i]);
        }
        return Arrays.asList(pathURL);
    }
    
    private URL createClasspathEntry(String text) {
        File entryFile = helper.resolveFile(text);
        URL entry;
        try {
            entry = entryFile.toURI().toURL();
        } catch (MalformedURLException x) {
            throw new AssertionError(x);
        }
        if (FileUtil.isArchiveFile(entry)) {
            return FileUtil.getArchiveRoot(entry);
        } else {
            String entryS = entry.toExternalForm();
            if (!entryS.endsWith("/")) { // NOI18N
                // A nonexistent dir. Have to add trailing slash ourselves.
                try {
                    return new URL(entryS + '/');
                } catch (MalformedURLException x) {
                    throw new AssertionError(x);
                }
            } else {
                return entry;
            }
        }
    }
    
    private List/*<URL>*/ createExecuteClasspath(List/*<String>*/ packageRoots, Element compilationUnitEl) {
        Iterator/*<Element>*/ it = Util.findSubElements(compilationUnitEl).iterator();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e.getLocalName().equals("classpath") && e.getAttribute("mode").equals("execute")) { // NOI18N
                return createClasspath(e);
            }
        }
        // None specified; assume it is same as compile classpath plus (cf. #49113) <built-to> dirs/JARs
        // if there are any (else include the source dir(s) as a fallback for the I18N wizard to work).
        List/*<URL>*/ urls = new ArrayList();
        urls.addAll(createCompileClasspath(compilationUnitEl));
        boolean foundBuiltTos = false;
        Iterator/*<Element>*/ builtTos = Util.findSubElements(compilationUnitEl).iterator();
        while (builtTos.hasNext()) {
            Element builtTo = (Element) builtTos.next();
            if (!builtTo.getLocalName().equals("built-to")) { // NOI18N
                continue;
            }
            foundBuiltTos = true;
            String rawtext = Util.findText(builtTo);
            assert rawtext != null : "Must have nonempty text inside <built-to>";
            String text = evaluator.evaluate(rawtext);
            if (text == null) {
                continue;
            }
            urls.add(createClasspathEntry(text));
        }
        if (!foundBuiltTos) {
            urls.addAll(createSourcePath(packageRoots));
        }
        return urls;
    }
    
    private List/*<URL>*/ createBootClasspath(Element compilationUnitEl) {
        Iterator/*<Element>*/ it = Util.findSubElements(compilationUnitEl).iterator();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e.getLocalName().equals("classpath") && e.getAttribute("mode").equals("boot")) { // NOI18N
                return createClasspath(e);
            }
        }
        // None specified; try to find a matching Java platform.
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform platform = jpm.getDefaultPlatform(); // fallback
        it = Util.findSubElements(compilationUnitEl).iterator();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e.getLocalName().equals("source-level")) { // NOI18N
                String level = Util.findText(e);
                Specification spec = new Specification("j2se", new SpecificationVersion(level)); // NOI18N
                JavaPlatform[] matchingPlatforms = jpm.getPlatforms(null, spec);
                if (matchingPlatforms.length > 0) {
                    // Pick one.
                    platform = matchingPlatforms[0];
                }
                break;
            }
        }
        if (platform != null) {
            // XXX this is not ideal; should try to reuse the ClassPath as is?
            // The current impl will not listen to changes in the platform classpath correctly.
            List/*<ClassPath.Entry>*/ entries = platform.getBootstrapLibraries().entries();
            List/*<URL>*/ urls = new ArrayList(entries.size());
            Iterator it2 = entries.iterator();
            while (it2.hasNext()) {
                ClassPath.Entry entry = (ClassPath.Entry) it2.next();
                urls.add(entry.getURL());
            }
            return urls;
        } else {
            assert false : "JavaPlatformManager has no default platform";
            return Collections.EMPTY_LIST;
        }
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        pathsChanged();
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }

    public void propertyChange(PropertyChangeEvent evt) {
        pathsChanged();
    }
    
    private void pathsChanged() {
        synchronized (this) {
            classpaths.clear();
        }
        //System.err.println("pathsChanged: " + mutablePathImpls);
        Iterator/*<Map<List<String>,MutableClassPathImplementation>>*/ it1 = mutablePathImpls.values().iterator();
        while (it1.hasNext()) {
            Map/*<List<String>,MutableClassPathImplementation>*/ m = (Map) it1.next();
            Iterator/*<MutableClassPathImplementation>*/ it2 = m.values().iterator();
            while (it2.hasNext()) {
                MutableClassPathImplementation impl = (MutableClassPathImplementation) it2.next();
                impl.change();
            }
        }
    }
    
    /**
     * Representation of one path.
     * Listens to changes in project.xml and/or evaluator and responds.
     */
    private final class MutableClassPathImplementation implements ClassPathImplementation {
        
        private final List/*<String>*/ packageRootNames;
        private final String type;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private List/*<URL>*/ roots; // should always be non-null
        
        public MutableClassPathImplementation(List/*<String>*/ packageRootNames, String type, Element initialCompilationUnit) {
            this.packageRootNames = packageRootNames;
            this.type = type;
            initRoots(initialCompilationUnit);
        }
        
        private Element findCompilationUnit() {
            Element java = aux.getConfigurationFragment("java-data", JavaProjectNature.NS_JAVA, true); // NOI18N
            if (java == null) {
                return null;
            }
            List/*<Element>*/ compilationUnits = Util.findSubElements(java);
            Iterator it = compilationUnits.iterator();
            while (it.hasNext()) {
                Element compilationUnitEl = (Element)it.next();
                assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
                if (packageRootNames.equals(findPackageRootNames(compilationUnitEl))) {
                    // Found a matching compilation unit.
                    return compilationUnitEl;
                }
            }
            // Did not find it.
            return null;
        }
        
        /**
         * Initialize list of URL roots.
         */
        private void initRoots(Element compilationUnitEl) {
            if (compilationUnitEl == null) {
                // Dead.
                roots = Collections.EMPTY_LIST;
                return;
            }
            if (type.equals(ClassPath.SOURCE)) {
                roots = createSourcePath(packageRootNames);
            } else if (type.equals(ClassPath.COMPILE)) {
                roots = createCompileClasspath(compilationUnitEl);
            } else if (type.equals(ClassPath.EXECUTE)) {
                roots = createExecuteClasspath(packageRootNames, compilationUnitEl);
            } else {
                assert type.equals(ClassPath.BOOT) : type;
                roots = createBootClasspath(compilationUnitEl);
            }
            assert roots != null;
        }

        public List/*<PathResourceImplementation>*/ getResources() {
            List/*<PathResourceImplementation>*/ impls = new ArrayList(roots.size());
            Iterator it = roots.iterator();
            while (it.hasNext()) {
                URL root = (URL) it.next();
                assert root.toExternalForm().endsWith("/") : "Had bogus roots " + roots + " for type " + type + " in " + helper.getProjectDirectory();
                impls.add(ClassPathSupport.createResource(root));
            }
            return impls;
        }
        
        /**
         * Notify impl of a possible change in data.
         */
        public void change() {
            List/*<URL>*/ oldRoots = roots;
            initRoots(findCompilationUnit());
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("MutableClassPathImplementation.change: packageRootNames=" + packageRootNames + " type=" + type + " oldRoots=" + oldRoots + " roots=" + roots);
            }
            if (!roots.equals(oldRoots)) {
                pcs.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
}
