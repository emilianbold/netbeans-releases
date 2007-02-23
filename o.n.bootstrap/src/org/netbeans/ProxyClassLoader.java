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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;

/**
 * A class loader that has multiple parents and uses them for loading
 * classes and resources. It can be used in tree hierarchy, where it
 * can exploit its capability to not throw ClassNotFoundException when
 * communicating with other ProxyClassLoader.
 * It itself doesn't load classes or resources, but allows subclasses
 * to add such functionality.
 *
 * @author  Petr Nejedly, Jesse Glick
 */
public class ProxyClassLoader extends ClassLoader implements Util.PackageAccessibleClassLoader {

    private static final Logger LOGGER = Logger.getLogger(ProxyClassLoader.class.getName());
    
    /**
     * All known package owners.
     * Packages are given in format <samp>org/netbeans/modules/foo/</samp>.
     */
    private final Map<String, ClassLoader> domainsByPackage = new HashMap<String, ClassLoader>();
    /** All known packages */
    private final Map<String, Package> packages = new HashMap<String, Package>();

    /** All parents of this classloader, including their parents recursively */
    private ClassLoader[] parents;

    /** if true, we have been destroyed */
    private boolean dead = false;
    
    private final boolean transitive;
    
    /** Create a multi-parented classloader.
     * Loads recursively from parents.
     * @param parents list of parent classloaders. 
     * @throws IllegalArgumentException if there are any nulls or duplicate
     * parent loaders or cycles.
     */
    public ProxyClassLoader( ClassLoader[] parents ) {
        this(parents, true);
    }
    
    /** Create a multi-parented classloader.
     * @param parents list of parent classloaders. 
     * @param transitive whether other PCLs depending on this one will
     *                   automatically search through its parent list
     * @throws IllegalArgumentException if there are any nulls or duplicate
     * parent loaders or cycles.
     * @since org.netbeans.core/1 > 1.6
     */
    public ProxyClassLoader(ClassLoader[] parents, boolean transitive) {
        if (parents.length == 0) {
            throw new IllegalArgumentException ("ProxyClassLoader must have a parent"); // NOI18N
        }
        
        this.transitive = transitive;
        
        Set<ClassLoader> check = new HashSet<ClassLoader>(Arrays.asList(parents));
        if (check.size() < parents.length) throw new IllegalArgumentException("duplicate parents"); // NOI18N
        if (check.contains(null)) throw new IllegalArgumentException("null parent in " + check); // NOI18N

        this.parents = coalesceParents(parents);
    }
    
    // this is used only by system classloader, maybe we can redesign it a bit
    // to live without this functionality, then destroy may also go away
    /** Add new parents dynamically.
     * @param nueparents the new parents to add (append to list)
     * @throws IllegalArgumentException in case of a null or cyclic parent (duplicate OK)
     */
    public void append(ClassLoader[] nueparents) throws IllegalArgumentException {
        if (nueparents == null) throw new IllegalArgumentException("null parents array"); // NOI18N
        for (int i = 0; i < nueparents.length; i++) {
            if (nueparents[i] == null) throw new IllegalArgumentException("null parent"); // NOI18N
        }
        ClassLoader[] resParents = null;
        ModuleFactory moduleFactory = Lookup.getDefault().lookup(ModuleFactory.class);
        if (moduleFactory != null && moduleFactory.removeBaseClassLoader()) {
            // this hack is here to prevent having the application classloader
            // as parent to all module classloaders.
            resParents = coalesceAppend(new ClassLoader[0], nueparents);
        } else {
            resParents = coalesceAppend(parents, nueparents);
        }
        synchronized (this) {
            // synchronized because we don't want to mess up potentially running
            // classloading
            parents = resParents;
        }
    }
    
    /** Try to destroy this classloader.
     * Subsequent attempts to use it will log an error (at most one though).
     */
    public void destroy() {
        dead = true;
    }

    private void zombieCheck(String hint) {
        if (dead) {
            /* suppress this warning for now, more confusing than useful
            LOGGER.log(Level.WARNING, "WARNING - attempting to use a zombie classloader " + this + " on " + hint + ". This means classes from a disabled module are still active. May or may not be a problem.", new IllegalStateException());
             */
            // don't warn again for same loader... this was enough
            dead = false;
        }
    }

    /**
     * Loads the class with the specified name.  The implementation of
     * this method searches for classes in the following order:<p>
     * <ol>
     * <li> Calls {@link #findLoadedClass(String)} to check if the class has
     *      already been loaded.
     * <li> Checks the caches whether another class from the same package
     *      was already loaded and uses the same classloader
     * <li> Tries to find the class using parent loaders in their order.
     * <li> Calls the {@link #simpleFindClass} method to find
     *      the class using this class loader.
     * </ol>
     *
     * @param     name the name of the class
     * @param     resolve if <code>true</code> then resolve the class
     * @return	  the resulting <code>Class</code> object
     * @exception ClassNotFoundException if the class could not be found
     */
    protected synchronized final Class<?> loadClass(String name, boolean resolve)
                                            throws ClassNotFoundException {
        zombieCheck(name);
        String filename = name.replace('.', '/').concat(".class"); // NOI18N
        int idx = filename.lastIndexOf('/'); // NOI18N
        if (idx == -1) {
            throw new ClassNotFoundException("Will not load classes from default package (" + name + ")"); // NOI18N
        }
        String pkg = filename.substring(0, idx + 1); // "org/netbeans/modules/foo/"
        Class c = smartLoadClass(name, filename, pkg);
        if(c == null) {
            throw new ClassNotFoundException(name + " from " + this);
        }
        if (resolve) resolveClass(c);
        return c;
    }
    
    /** This ClassLoader can't load anything itself. Subclasses
     * may override this method to do some class loading themselves. The
     * implementation should not throw any exception, just return
     * <CODE>null</CODE> if it can't load required class.
     *
     * @param  name the name of the class
     * @param  fileName the expected filename of the classfile, like
     *      <CODE>java/lang/Object.class</CODE> for <CODE>java.lang.Object</CODE>
     *      The ClassLoader implementation may or may not use it, depending
     *      whether it is usefull to it.
     * @param pkg the package name, in the format org/netbeans/modules/foo/
     * @return the resulting <code>Class</code> object or <code>null</code>
     */
    protected Class simpleFindClass(String name, String fileName, String pkg) {
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
     * Finds the resource with the given name. The implementation of
     * this method searches for resources in the following order:<p>
     * <ol>
     * <li> Checks the caches whether another resource or class from the same
     *      package was already loaded and uses the same classloader.
     * <li> Tries to find the resources using parent loaders in their order.
     * <li> Calls the {@link #findResource(String)} method to find
     *      the resources using this class loader.
     * </ol>
     *
     * @param  name a "/"-separated path name that identifies the resource.
     * @return a URL for reading the resource, or <code>null</code> if
     *      the resource could not be found.
     * @see #findResource(String)
     */
    public final URL getResource(String name) {
        zombieCheck(name);
        
        name = stripInitialSlash(name);
        
        final int slashIdx = name.lastIndexOf('/');
        if (slashIdx == -1) {
            printDefaultPackageWarning(name);
        }
        final String pkg = name.substring(0, slashIdx + 1);

        if (isSpecialResource(pkg)) {
            // Disable domain cache for this one, do a simple check.
            for (int i = 0; i < parents.length; i++) {
                if (!shouldDelegateResource(pkg, parents[i])) continue;
                URL u;
                if (parents[i] instanceof ProxyClassLoader) {
                    u = ((ProxyClassLoader)parents[i]).findResource(name);
                } else {
                    u = parents[i].getResource(name);
                }
                if (u != null) return u;
            }
            return findResource(name);
        }
        
        ClassLoader owner = domainsByPackage.get(pkg);

        if (owner != null) { // known package
            // Note that shouldDelegateResource should already be true for this!
            if (owner instanceof ProxyClassLoader) {
                return ((ProxyClassLoader)owner).findResource(name); // we have its parents, skip them
	    } else {
                return owner.getResource(name);     // know nothing about this loader and his structure
            }
        } 
        
        // virgin package
        URL retVal = null;
        for (int i = 0; i < parents.length; i++) {
            owner = parents[i];
            if (!shouldDelegateResource(pkg, owner)) continue;
            if (owner instanceof ProxyClassLoader) {
                retVal = ((ProxyClassLoader)owner).findResource(name); // skip parents (checked already)
            } else {
                retVal = owner.getResource(name); // know nothing about this loader and his structure
            }
            if (retVal != null) {
                String p = new String(pkg).intern(); // NOPMD
                domainsByPackage.put(p, owner);
		if (owner instanceof ProxyClassLoader) {
                    ((ProxyClassLoader)owner).domainsByPackage.put(p, owner);
		}
                return retVal;
            }
        }
        
        // try it ourself
        retVal = findResource(name);
        if (retVal != null) {
            domainsByPackage.put(new String(pkg).intern(), this); // NOPMD
        }
        return retVal;
    }

    /** This ClassLoader can't load anything itself. Subclasses
     * may override this method to do some resource loading themselves.
     *
     * @param  name the resource name
     * @return a URL for reading the resource, or <code>null</code>
     *      if the resource could not be found.
     */
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
    protected final synchronized Enumeration<URL> findResources(String name) throws IOException {
        zombieCheck(name);
        name = stripInitialSlash(name);
        final int slashIdx = name.lastIndexOf('/');
        if (slashIdx == -1) {
            printDefaultPackageWarning(name);
        }
        final String pkg = name.substring(0, slashIdx + 1);

        // Don't bother optimizing this call by domains.
        // It is mostly used for resources for which isSpecialResource would be true anyway.
        List<Enumeration<URL>> es = new ArrayList<Enumeration<URL>>(parents.length + 1);
        for (ClassLoader parent : parents) {
            if (!shouldDelegateResource(pkg, parent)) {
                continue;
            }
            if (parent instanceof ProxyClassLoader) {
                es.add(((ProxyClassLoader) parent).simpleFindResources(name));
            } else {
                es.add(parent.getResources(name));
            }
        }
        es.add(simpleFindResources(name));
        // Should not be duplicates, assuming the parent loaders are properly distinct
        // from one another and do not overlap in JAR usage, which they ought not.
        // Anyway MetaInfServicesLookup, the most important client of this method, does
        // its own duplicate filtering already.
        return Enumerations.concat(Collections.enumeration(es));
    }

    /** This ClassLoader can't load anything itself. Subclasses
     * may override this method to do some resource loading themselves, this
     * implementation simply delegates to findResources method of the superclass
     * that should return empty Enumeration.
     *
     * @param  name the resource name
     * @return an Enumeration of URLs for the resources
     * @throws IOException if I/O errors occur
     */
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
    protected Package getPackage(String name) {
        zombieCheck(name);
        return getPackageFast(name, name.replace('.', '/') + '/', true);
    }
    
    /**
     * Faster way to find a package.
     * @param name package name in org.netbeans.modules.foo format
     * @param sname package name in org/netbeans/modules/foo/ format
     * @param recurse whether to also ask parents
     * @return located package, or null
     */
    protected Package getPackageFast(String name, String sname, boolean recurse) {
        synchronized (packages) {
            Package pkg = packages.get(name);
            if (pkg != null) {
                return pkg;
            }
            if (!recurse) {
                return null;
            }
            for (int i = 0; i < parents.length; i++) {
                ClassLoader par = parents[i];
                if (par instanceof ProxyClassLoader && shouldDelegateResource(sname, par)) {
                    pkg = ((ProxyClassLoader)par).getPackageFast(name, sname, false);
                    if (pkg != null) {
                        break;
                    }
                }
            }
            if (pkg == null && /* #30093 */shouldDelegateResource(sname, getParent())) {
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
    protected Package definePackage(String name, String specTitle,
                String specVersion, String specVendor, String implTitle,
		String implVersion, String implVendor, URL sealBase )
		throws IllegalArgumentException {
	synchronized (packages) {
            Package pkg = super.definePackage (name, specTitle, specVersion, specVendor, implTitle,
			implVersion, implVendor, sealBase);
            packages.put(name, pkg);
            return pkg;
	}
    }

    /**
     * Returns all of the Packages defined by this class loader and its parents.
     *
     * @return the array of <code>Package</code> objects defined by this
     * <code>ClassLoader</code>
     */
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
        zombieCheck(null);
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
        if (!"commons-logging.properties".equals(name)) { // NOI18N
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
    private ClassLoader[] coalesceParents(ClassLoader[] loaders) throws IllegalArgumentException {
        int likelySize = loaders.length * 5 + 10;
        Set<ClassLoader> resultingUnique = new HashSet<ClassLoader>(likelySize);
        List<ClassLoader> resulting = new ArrayList<ClassLoader>(likelySize);
        for (int i = 0; i < loaders.length; i++) {
            addRec(resultingUnique, resulting, loaders[i]);
        }
        ClassLoader[] ret = resulting.toArray(new ClassLoader[resulting.size()]);
        return ret;
    }
    
    /** Coalesce a new set of loaders into the existing ones.
     */
    private ClassLoader[] coalesceAppend(ClassLoader[] existing, ClassLoader[] appended) throws IllegalArgumentException {
        int likelySize = existing.length + 3;
        Set<ClassLoader> resultingUnique = new HashSet<ClassLoader>(likelySize);
        List<ClassLoader> existingL = Arrays.asList(existing);
        resultingUnique.addAll(existingL);
        if (resultingUnique.containsAll(Arrays.asList(appended))) {
            // No change required.
            return existing;
        }
        List<ClassLoader> resulting = new ArrayList<ClassLoader>(likelySize);
        resulting.addAll(existingL);
        for (int i = 0; i < appended.length; i++) {
            addRec(resultingUnique, resulting, appended[i]);
        }
        ClassLoader[] ret = resulting.toArray(new ClassLoader[resulting.size()]);
        return ret;
    }
    
    private void addRec(Set<ClassLoader> resultingUnique, List<ClassLoader> resulting, ClassLoader loader) throws IllegalArgumentException {
        if (loader == this) throw new IllegalArgumentException("cycle in parents"); // NOI18N
        if (resultingUnique.contains(loader)) return;
        if (loader instanceof ProxyClassLoader && ((ProxyClassLoader)loader).transitive) {
            ClassLoader[] parents = ((ProxyClassLoader)loader).parents;
            for (int i = 0; i < parents.length; i++) {
                addRec(resultingUnique, resulting, parents[i]);
            }
        }
        resultingUnique.add(loader);
        resulting.add(loader);
    }

    /** A method that finds a class either in itself or in parents.
     * It uses dual signaling for class not found: it can either return null
     * or throw CNFE itself.
     * @param name class name, e.g. "org.netbeans.modules.foo.Clazz"
     * @param fileName resource name, e.g. "org/netbeans/modules/foo/Clazz.class"
     * @param pkg package component, e.g. "org/netbeans/modules/foo/"
     * @return a class or null if not found. It can also throw an exception.
     * @throws ClassNotFoundException in case it doesn't found a class
     * and a parent eglible for loading it thrown it already.
     */
    private final Class smartLoadClass(String name, String fileName, String pkg) throws ClassNotFoundException {
	// First, check if the class has already been loaded
	Class c = findLoadedClass(name);
	if(c != null) return c;
        
        final ClassLoader owner = isSpecialResource(pkg) ? null : domainsByPackage.get(pkg);
        if (owner == this) {
            return simpleFindClass(name, fileName, pkg);
        }
        if (owner != null) {
            // Note that shouldDelegateResource should already be true as we hit this pkg before.
            if (owner instanceof ProxyClassLoader) {
                return ((ProxyClassLoader)owner).fullFindClass(name, fileName, pkg);
            } else {
                return owner.loadClass(name); // May throw CNFE, will be propagated
            }
        }
        
        // Virgin package, do the parent scan 
        c = loadInOrder(name, fileName, pkg);

        if (c != null) {
            final ClassLoader owner2 = getClassClassLoader(c); // who got it?
            domainsByPackage.put(new String(pkg).intern(), owner2); // NOPMD
        }
        return c;
    }

    // #29844 run as privileged as it may get called by loadClassInternal() used
    // during class resolving by JVM with arbitrary ProtectionDomain context stack
    private static ClassLoader getClassClassLoader(final Class c) {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return c.getClassLoader();
            }
        });
    }

    private final Class loadInOrder( String name, String fileName, String pkg ) throws ClassNotFoundException {
        ClassNotFoundException cached = null;
        for (int i = 0; i < parents.length; i++) {
	    ClassLoader par = parents[i];
            if (!shouldDelegateResource(pkg, par)) continue;
	    if ((par instanceof ProxyClassLoader) && 
                ((ProxyClassLoader)par).shouldBeCheckedAsParentProxyClassLoader()) {
                ProxyClassLoader pcl = (ProxyClassLoader)par;
		Class c = pcl.fullFindClass(name, fileName, pkg);
                // pcl might have have c in its already-loaded classes even though
                // it was not the defining class loader. In that case, if pcl was
                // not transitive (should not expose its own parents), reject this.
                if (c != null && (pcl.transitive || getClassClassLoader(c) == pcl)) return c;
	    } else {
                // The following is an optimization, it should not affect semantics:
                boolean skip = false;
                if (optimizeNBLoading()) {
                    if (name.startsWith("org.netbeans.") || // NOI18N
                            name.startsWith("org.openide.") || // NOI18N
                            name.endsWith(".Bundle") || // NOI18N
                            name.endsWith("BeanInfo") || // NOI18N
                            name.endsWith("Editor")) { // NOI18N
                        if (par.getResource(fileName) == null) {
                            // We would just throw CNFE anyway, don't bother!
                            // Avg. (over ten runs after primer, w/ netbeans.close):
                            // before: 13.87s after: 13.40s saved: 1.3%
                            skip = true;
                        }
                    }
                }
                if (!skip) {
                    try {
                        return par.loadClass(name);
                    } catch( ClassNotFoundException cnfe ) {
                        cached = cnfe;
                    }
                }
	    }
	}

        Class c = simpleFindClass(name, fileName, pkg); // Try it ourselves
        if (c != null) return c;
        if (cached != null) throw cached;
	return null;
    }

    private synchronized Class fullFindClass(String name, String fileName, String pkg) {
	Class c = findLoadedClass(name);
        if (c == null) {
            c = simpleFindClass(name, fileName, pkg);
            if (c != null) {
                domainsByPackage.put(new String(pkg).intern(), this); // NOPMD
            }
        }
	return c;
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
    
    /** Test whether a given resource name is something that any JAR might
     * have, and for which the domain cache should be disabled.
     * The result must not change from one call to the next with the same argument.
     * By default the domain cache is disabled only for META-INF/* JAR information.
     * @param pkg the package component of the resource path ending with a slash,
     *        e.g. "org/netbeans/modules/foo/"
     * @return true if it is a special resource, false for normal domain-cached resource
     * @since org.netbeans.core/1 1.3
     */
    protected boolean isSpecialResource(String pkg) {
        if (pkg.startsWith("META-INF/")) return true; // NOI18N
        
        // #38368: do not cache the default package
        if (pkg.length() == 0) return true;
        
        return false;
    }
    
    /** Test whether a given resource request (for a class or not) should be
     * searched for in the specified parent classloader or not.
     * The result must not change from one call to the next with the same arguments.
     * By default, always true. Subclasses may override to "mask" certain
     * packages from view, possibly according to the classloader chain.
     * @param pkg the package component of the resource path ending with a slash,
     *        e.g. "org/netbeans/modules/foo/"
     * @param parent a classloader which is a direct or indirect parent of this one
     * @return true if the request should be delegated to this parent; false to
     *         only search elsewhere (other parents, this loader's own namespace)
     * @since org.netbeans.core/1 1.3
     */
    protected boolean shouldDelegateResource(String pkg, ClassLoader parent) {
        return true;
    }
    
}
