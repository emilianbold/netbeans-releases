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

package org.netbeans;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * A class loader that has multiple parents and uses them for loading
 * classes and resources. It is optimized for working in the enviroment 
 * of a deeply nested classloader hierarchy. It uses shared knowledge 
 * about package population to route the loading request directly 
 * to the correct classloader. 
 * It doesn't load classes or resources itself, but allows subclasses
 * to add such functionality.
 * 
 * @author  Petr Nejedly, Jesse Glick
 */
public class ProxyClassLoader extends ClassLoader implements Util.PackageAccessibleClassLoader {

    private static final Logger LOGGER = Logger.getLogger(ProxyClassLoader.class.getName());
    
    /** All known packages */
    private final Map<String, Package> packages = new HashMap<String, Package>();

    /** All parents of this classloader, including their parents recursively */
    private ProxyClassLoader[] parents;

    private final boolean transitive;

    /** The base class loader that is before all ProxyClassLoaders. */
    private ClassLoader systemCL = ClassLoader.getSystemClassLoader();
 
    /** A shared map of all packages known by all classloaders. Also covers META-INF based resources.
     * It contains two kinds of keys: dot-separated package names and slash-separated
     * META-INF resource names, e.g. {"org.foobar", "/services/org.foobar.Foo"} 
     */
    private static Map<String,Set<ProxyClassLoader>> packageCoverage = new HashMap<String,Set<ProxyClassLoader>>(); 
    
     
    private Set<ProxyClassLoader> parentSet = new HashSet<ProxyClassLoader>(); 
     
    private static Map<String,Boolean> sclPackages = new HashMap<String,Boolean>();  
    
   
    /** Create a multi-parented classloader.
     * @param parents all direct parents of this classloader, except system one.
     * @param coveredPackages Enumeration of Strings if format "org.something" 
+     *   containing all packages to be covered by this classloader. 
     * @param transitive whether other PCLs depending on this one will
     *                   automatically search through its parent list
     */
    public ProxyClassLoader(ClassLoader[] parents, boolean transitive) {
        this.transitive = transitive;
        
        this.parents = coalesceParents(parents);
        parentSet.addAll(Arrays.asList(this.parents));
    }
    
    protected final void addCoveredPackages(Iterable<String> coveredPackages) {
        synchronized(packageCoverage) {
            for (String pkg : coveredPackages) {
                Set<ProxyClassLoader> delegates = packageCoverage.get(pkg); 
                if (delegates == null) { 
                    delegates = new HashSet<ProxyClassLoader>();
                    packageCoverage.put(pkg, delegates); 
                } 
                delegates.add(this); 
            }
        }
        
    }
    
    // this is used only by system classloader, maybe we can redesign it a bit
    // to live without this functionality, then destroy may also go away
    /** Add new parents dynamically.
     * @param nueparents the new parents to add (append to list)
     * @throws IllegalArgumentException in case of a null or cyclic parent (duplicate OK)
     */
    public void append(ClassLoader[] nueparents) throws IllegalArgumentException {
        if (nueparents == null) throw new IllegalArgumentException("null parents array"); // NOI18N
        
        for (ClassLoader cl : nueparents) {
            if (cl == null) throw new IllegalArgumentException("null parent: " + Arrays.asList(nueparents)); // NOI18N
        }
        
        ProxyClassLoader[] resParents = null;
        ModuleFactory moduleFactory = Lookup.getDefault().lookup(ModuleFactory.class);
        if (moduleFactory != null && moduleFactory.removeBaseClassLoader()) {
            // this hack is here to prevent having the application classloader
            // as parent to all module classloaders.
            resParents = coalesceAppend(new ProxyClassLoader[0], nueparents);
        } else {
            resParents = coalesceAppend(parents, nueparents);
        }
        synchronized (this) {
            // synchronized because we don't want to mess up potentially running
            // classloading
            parents = resParents;
            parentSet.clear();
            parentSet.addAll(Arrays.asList(parents));
        }
    }
         
    /**
     * Loads the class with the specified name.  The implementation of
     * this method searches for classes in the following order:<p>
     * <ol>
     * <li> Looks for a known package and pass the loading to the ClassLoader 
            for that package. 
     * <li> For unknown packages passes the call directly 
     *      already been loaded.
     * </ol>
     *
     * @param     name the name of the class
     * @param     resolve if <code>true</code> then resolve the class
     * @return	  the resulting <code>Class</code> object
     * @exception ClassNotFoundException if the class could not be found
     */
    @Override
    protected synchronized Class loadClass(String name, boolean resolve)
                                            throws ClassNotFoundException {
        Class cls = null;

        int last = name.lastIndexOf('.');
        String pkg = (last >= 0) ? name.substring(0, last) : "";

        final String path = pkg.replace('.', '/') + "/";

        Set<ProxyClassLoader> del = packageCoverage.get(pkg);
 
        Boolean boo = sclPackages.get(pkg);
        if ((boo == null || boo.booleanValue()) && shouldDelegateResource(path, null)) {
            try {
                cls = systemCL.loadClass(name);
                if (boo == null) sclPackages.put(pkg, true);
                return cls; // try SCL first
            } catch (ClassNotFoundException e) {
                // No dissaster, try other loaders
            }
        }

        if (del == null) {
            // uncovered package, go directly to SCL (may throw the CNFE for us)
            //if (shouldDelegateResource(path, null)) cls = systemCL.loadClass(name);
        } else if (del.size() == 1) {
            // simple package coverage
            ProxyClassLoader pcl = del.iterator().next();
            if (pcl == this || (parentSet.contains(pcl) && shouldDelegateResource(path, pcl))) {
                cls = pcl.selfLoadClass(name);
                if (cls != null) sclPackages.put(pkg, false);
            }/* else { // maybe it is also covered by SCL
                if (shouldDelegateResource(path, null)) cls = systemCL.loadClass(name);
            }*/
        } else {
            // multicovered package, search in order
            for (ProxyClassLoader pcl : parents) { // all our accessible parents
                if (del.contains(pcl) && shouldDelegateResource(path, pcl)) { // that cover given package
                    cls = pcl.selfLoadClass(name);
                    if (cls != null) break;
                } 
            } 
            if (cls == null && del.contains(this)) cls = selfLoadClass(name); 
            if (cls != null) sclPackages.put(pkg, false); 
        } 
        if (cls == null && shouldDelegateResource(path, null)) cls = systemCL.loadClass(name); // may throw CNFE
        if (cls == null) throw new ClassNotFoundException(name); 
        if (resolve) resolveClass(cls); 
        return cls; 
    }       

    /** May return null */ 
    private synchronized Class selfLoadClass(String name) { 
        Class cls = findLoadedClass(name); 
        if (cls == null) cls = doLoadClass(name); 
        return cls; 
    }

    
    /** This ClassLoader can't load anything itself. Subclasses
     * may override this method to do some class loading themselves. The
     * implementation should not throw any exception, just return
     * <CODE>null</CODE> if it can't load required class.
     *
     * @param  name the name of the class
     * @return the resulting <code>Class</code> object or <code>null</code>
     */
    protected Class doLoadClass(String name) {
        return null;
    }
    
    private String stripInitialSlash(String resource) { // #90310
        if (resource.startsWith("/")) {
            LOGGER.log(Level.WARNING, "Should not use initial '/' in calls to ClassLoader.getResource(s): {0}", resource);
            return resource.substring(1);
        } else {
            return resource;
        }
    }

    /**
     * Finds the resource with the given name.
     * @param  name a "/"-separated path name that identifies the resource.
     * @return a URL for reading the resource, or <code>null</code> if
     *      the resource could not be found.
     * @see #findResource(String)
     */
    @Override
    public final URL getResource(String name) {
        URL url = null;
        name = stripInitialSlash(name);

        int last = name.lastIndexOf('/');
        String pkg = (last >= 0) ? name.substring(0, last).replace('/', '.') : "";
        String path = name.substring(0, last+1);
        
        Set<ProxyClassLoader> del = packageCoverage.get(pkg);

        if (del == null) {
            // uncovered package, go directly to SCL
            if (shouldDelegateResource(path, null)) url = systemCL.getResource(name);
        } else if (del.size() == 1) {
            // simple package coverage
            ProxyClassLoader pcl = del.iterator().next();
            if (pcl == this || (parentSet.contains(pcl) && shouldDelegateResource(path, pcl)))
                url = pcl.findResource(name);
        } else {
            // multicovered package, search in order
            for (ProxyClassLoader pcl : parents) { // all our accessible parents
                if (del.contains(pcl) && shouldDelegateResource(path, pcl)) { // that cover given package
                    url = pcl.findResource(name);
                    if (url != null) break;
                }
            }
            if (url == null && del.contains(this)) url = findResource(name); 
        }

        // uncovered package, go directly to SCL
        if (url == null && shouldDelegateResource(path, null)) url = systemCL.getResource(name);
        
        return url;
    }

    /** This ClassLoader can't load anything itself. Subclasses
     * may override this method to do some resource loading themselves.
     *
     * @param  name the resource name
     * @return a URL for reading the resource, or <code>null</code>
     *      if the resource could not be found.
     */
    @Override
    protected URL findResource(String name) {
	return null;
    }
    
    /**
     * Finds all the resource with the given name. The implementation of
     * this method uses the {@link #simpleFindResources(String)} method to find
     * all the resources available from this classloader and adds all the 
     * resources from all the parents.
     *
     * @param  name the resource name
     * @return an Enumeration of URLs for the resources
     * @throws IOException if I/O errors occur
     */    
    @Override
    protected final synchronized Enumeration<URL> findResources(String name) throws IOException {
        final int slashIdx = name.lastIndexOf('/');
        final String path = name.substring(0, slashIdx + 1);
        List<Enumeration<URL>> sub = new ArrayList<Enumeration<URL>>();

        // always consult SCL first
        if (shouldDelegateResource(path, null)) sub.add(systemCL.getResources(name));
        
        if(name.startsWith("META-INF/")) { // common but expensive resources

            String relName = name.substring(8);
            Set<ProxyClassLoader> del = packageCoverage.get(relName);

            if (del != null) { // unclaimed resource, go directly to SCL
                for (ProxyClassLoader pcl : parents) { // all our accessible parents
                    if (del.contains(pcl) && shouldDelegateResource(path, pcl)) { // that cover given package
                        sub.add(pcl.simpleFindResources(name));
                    }
                }
                if (del.contains(this)) sub.add(simpleFindResources(name)); 
            }
        } else { // Don't bother optimizing this call by domains.
            for (ProxyClassLoader pcl : parents) { 
                if (shouldDelegateResource(path, pcl)) sub.add(pcl.simpleFindResources(name)); 
            }
            sub.add(simpleFindResources(name));
        }
        // Should not be duplicates, assuming the parent loaders are properly distinct
        // from one another and do not overlap in JAR usage, which they ought not.
        // Anyway MetaInfServicesLookup, the most important client of this method, does
        // its own duplicate filtering already.
        return Enumerations.concat(Collections.enumeration(sub));
    }

    protected Enumeration<URL> simpleFindResources(String name) throws IOException {
        return super.findResources(name);
    }

    
    /**
     * Returns a Package that has been defined by this class loader or any
     * of its parents.
     *
     * @param  name the package name
     * @return the Package corresponding to the given name, or null if not found
     */
    @Override
    protected Package getPackage(String name) {
        return getPackageFast(name, true);
    }
    
    /**
     * Faster way to find a package.
     * @param name package name in org.netbeans.modules.foo format
     * @param sname package name in org/netbeans/modules/foo/ format
     * @param recurse whether to also ask parents
     * @return located package, or null
     */
    protected Package getPackageFast(String name, boolean recurse) {
        synchronized (packages) {
            Package pkg = packages.get(name);
            if (pkg != null) {
                return pkg;
            }
            if (!recurse) {
                return null;
            }
            for (int i = 0; i < parents.length; i++) {
                ProxyClassLoader par = parents[i];
                pkg = par.getPackageFast(name, false);
                if (pkg != null) break;
            }
            if (pkg == null) {
                // Cannot access either Package.getSystemPackage nor ClassLoader.getPackage
                // from here, so do the best we can though it will cause unnecessary
                // duplication of the package cache (PCL.packages vs. CL.packages):
                pkg = super.getPackage(name);
            }
            if (pkg != null) {
                packages.put(name, pkg);
            }
            return pkg;
        }
    }

    /** This is here just for locking serialization purposes.
     * Delegates to super.definePackage with proper locking.
     * Also tracks the package in our private cache, since
     * getPackageFast(...,...,false) will not call super.getPackage.
     */
    @Override
    protected Package definePackage(String name, String specTitle,
                String specVersion, String specVendor, String implTitle,
		String implVersion, String implVendor, URL sealBase )
		throws IllegalArgumentException {
	synchronized (packages) {
            try {
                Package pkg = super.definePackage(name, specTitle, specVersion, specVendor, implTitle,
                        implVersion, implVendor, sealBase);
                packages.put(name, pkg);
                return pkg;
            } catch (IllegalArgumentException x) {
                Exceptions.attachMessage(x, "If you are getting this, probably it is because you have several modules trying to load from the same package. This is not supported (#71524).");
                throw x;
            }
	}
    }

    /**
     * Returns all of the Packages defined by this class loader and its parents.
     *
     * @return the array of <code>Package</code> objects defined by this
     * <code>ClassLoader</code>
     */
    @Override
    protected synchronized Package[] getPackages() {
        return getPackages(new HashSet<ClassLoader>());
    }
    
    /**
     * Returns all of the Packages defined by this class loader and its parents.
     * Do not recurse to parents in addedParents set. It speeds up execution
     * time significantly.
     * @return the array of <code>Package</code> objects defined by this
     * <code>ClassLoader</code>
     */
    private synchronized Package[] getPackages(Set<ClassLoader> addedParents) {
        Map<String,Package> all = new HashMap<String, Package>();
        // XXX call shouldDelegateResource on each?
        addPackages(all, super.getPackages());
        for (int i = 0; i < parents.length; i++) {
            ClassLoader par = parents[i];
            if (par instanceof ProxyClassLoader && addedParents.add(par)) {
                // XXX should ideally use shouldDelegateResource here...
                addPackages(all, ((ProxyClassLoader)par).getPackages(addedParents));
            }
        }
        synchronized (packages) {
            all.keySet().removeAll(packages.keySet());
            packages.putAll(all);
        }
        return packages.values().toArray(new Package[packages.size()]);
    }
    
    public Package getPackageAccessibly(String name) {
        return getPackage(name);
    }
    
    public Package[] getPackagesAccessibly() {
        return getPackages();
    }

    /**
     * #38368: Warn that the default package should not be used.
     */
    private static void printDefaultPackageWarning(String name) {
        // #42201 - commons-logging lib tries to read its config from this file, ignore
        if (!"commons-logging.properties".equals(name) &&
            !"jndi.properties".equals(name)) { // NOI18N
            LOGGER.log(Level.INFO, null, new IllegalStateException("You are trying to access file: " + name + " from the default package. Please see http://www.netbeans.org/download/dev/javadoc/org-openide-modules/org/openide/modules/doc-files/classpath.html#default_package"));
        }
    }
    
    /** Coalesce parent classloaders into an optimized set.
     * This means that all parents of the specified classloaders
     * are also added recursively, removing duplicates along the way.
     * Search order should be preserved (parents before children, stable w.r.t. inputs).
     * @param loaders list of suggested parents (no nulls or duplicates permitted)
     * @return optimized list of parents (no nulls or duplicates)
     * @throws IllegalArgumentException if there are cycles
     */
    private ProxyClassLoader[] coalesceParents(ClassLoader[] loaders) throws IllegalArgumentException {
        return coalesceAppend(new ProxyClassLoader[0], loaders);
    }
    
    /** Coalesce a new set of loaders into the existing ones.
     */
    private ProxyClassLoader[] coalesceAppend(ProxyClassLoader[] existing, ClassLoader[] appended) throws IllegalArgumentException {
        int likelySize = existing.length + appended.length;
        
        LinkedHashSet<ClassLoader> uniq = new LinkedHashSet<ClassLoader>(likelySize);
        uniq.addAll(Arrays.asList(existing));

        if (uniq.containsAll(Arrays.asList(appended))) return existing; // No change required.

        for (ClassLoader l : appended) addRec(uniq, l); // add all loaders (maybe recursively)
        
        // validate the configuration
        // it is valid if all heading non-ProxyClassLoaders are parents of the last one
        boolean head = true;
        List<ProxyClassLoader> pcls = new ArrayList<ProxyClassLoader>(uniq.size());
        for (ClassLoader l : uniq) {
            if (head) {
                if (l instanceof ProxyClassLoader) {
                    // only PCLs after this point
                    head = false; 
                    pcls.add((ProxyClassLoader)l);
                } else {
                    if (isParentOf(systemCL, l)) {
                        systemCL = l;
                    } else {
                        throw new IllegalArgumentException("Bad ClassLoader ordering: " + Arrays.asList(appended));
                    }
                }
            } else {
                if (l instanceof ProxyClassLoader) {
                    pcls.add((ProxyClassLoader)l);
                } else {
                        throw new IllegalArgumentException("Bad ClassLoader ordering: " + Arrays.asList(appended));
                    
                }
            }
        }
         
        ProxyClassLoader[] ret = pcls.toArray(new ProxyClassLoader[pcls.size()]);
        return ret;
    }
    
    private static boolean isParentOf(ClassLoader parent, ClassLoader child) {
        while (child != null) {
            if (child == parent) return true;
            child = child.getParent();
        }
        return false;
    }
    
    private void addRec(Set<ClassLoader> resultingUnique, ClassLoader loader) throws IllegalArgumentException {
        if (loader == this) throw new IllegalArgumentException("cycle in parents"); // NOI18N
        if (resultingUnique.contains(loader)) return;
        if (loader instanceof ProxyClassLoader && ((ProxyClassLoader)loader).transitive) {
            for (ProxyClassLoader lpar : ((ProxyClassLoader)loader).parents) {
                addRec(resultingUnique, lpar);
            }
        }
        resultingUnique.add(loader);
    }


    private void addPackages(Map<String,Package> all, Package[] pkgs) {
        // Would be easier if Package.equals() was just defined sensibly...
        for (int i = 0; i < pkgs.length; i++) {
            all.put(pkgs[i].getName(), pkgs[i]);
        }
    }
    
    /**
     * Can be overridden by special classloaders
     * (see project installer/jnlp/modules).
     * @see #loadInOrder
     */
    protected boolean shouldBeCheckedAsParentProxyClassLoader() {
        return true;
    }

    /**
     * Allows turning off the optimization in {@link #loadInOrder}.
     */
    protected boolean optimizeNBLoading() {
        return true;
    }
    
    protected boolean shouldDelegateResource(String pkg, ClassLoader parent) {
         return true;
    }

    /** Called before releasing the classloader so it can itself unregister
     * from the global ClassLoader pool */
    public void destroy() {
        synchronized(packageCoverage) {
            for (Iterator<String> it = packageCoverage.keySet().iterator(); it.hasNext(); ) {
                String pkg = it.next();
                Set<ProxyClassLoader> set = packageCoverage.get(pkg);
                if (set.remove(this)) {
                    if (set.isEmpty()) it.remove();
                }
            }
        }
    }

}
