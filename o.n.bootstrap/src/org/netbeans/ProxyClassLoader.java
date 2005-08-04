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

package org.netbeans;

import java.util.*;
import java.net.URL;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

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
public abstract class ProxyClassLoader extends ClassLoader {
    
    /** #38368: Allow disabling the default package warning */
    private static final boolean DO_NOT_WARN_DEFAULT_PACKAGE = Boolean.getBoolean("org.netbeans.do_not_warn_default_package"); // NOI18N
    
    /** All known packages, of type <code>Map&lt;String,Package&gt;</code> */
    private final Map packages = new HashMap();
    
    /** All known exact mappings to classes */
    private final Map classes = new HashMap();

    /** 
     * Including their parents recursively */
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
        
        Set check = new HashSet(Arrays.asList(parents)); // Set<ClassLoader>
        if (check.size() < parents.length) throw new IllegalArgumentException("duplicate parents"); // NOI18N
        if (check.contains(null)) throw new IllegalArgumentException("null parent in " + check); // NOI18N

        this.parents = coalesceParents(parents);
    }
    
    /** For testing purposes, allows access to the list of parents.
     */
    final ClassLoader[] getParents() {
        return this.parents;
    }
    
    /** Allows the subclasses to provide a set of ClassLoader that
     * are supposed to contain the specific packages. Implementation
     * can use whatever caching it wishes to provide the desired result.
     * 
     *
     * @param pkg the name of the package to query
     * @return null if this method is not implemented, or a set (which needs
     *   only a <code>contains</code> method implemented) that will answer
     *   true on all ClassLoader that could possibly contain the package
     */
    protected abstract Set/*<ClassLoader>*/ packageOwners(String pkg);
    
    /** Computes list of packages supported by this loader for optimizing
     * the resource location check.
     * @return array of strings or null, if this is not supported
     */
    public abstract String[] listPopulatedPackages();
    
    // this is used only by system classloader, maybe we can redesign it a bit
    // to live without this functionality, then destroy may also go away
    /** Add new parents dynamically.
     * @param parents the new parents to add (append to list)
     * @throws IllegalArgumentException in case of a null or cyclic parent (duplicate OK)
     */
    public synchronized void append(ClassLoader[] nueparents) throws IllegalArgumentException {
        // XXX should this be synchronized?
        if (nueparents == null) throw new IllegalArgumentException("null parents array"); // NOI18N
        for (int i = 0; i < nueparents.length; i++) {
            if (nueparents[i] == null) throw new IllegalArgumentException("null parent"); // NOI18N
        }
        
        ArrayList existing = new ArrayList();
        existing.addAll(Arrays.asList(parents));
        
        this.parents = coalesceAppend(
            existing, nueparents
        );
    }

    
    
    /** Try to destroy this classloader.
     * Subsequent attempts to use it will log an error (at most one though).
     */
    public void destroy() {
        dead = true;
    }

    private void zombieCheck(String hint) {
        if (/*dead*/false/* suppress this warning for now, more confusing than useful */) {
            IllegalStateException ise = new IllegalStateException("WARNING - attempting to use a zombie classloader " + this + " on " + hint + ". This means classes from a disabled module are still active. May or may not be a problem."); // NOI18N
            JarClassLoader.notify(0, ise);
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
    protected synchronized final Class loadClass(String name, boolean resolve)
                                            throws ClassNotFoundException {
        zombieCheck(name);
        String filename = name.replace('.', '/').concat(".class"); // NOI18N
        int idx = filename.lastIndexOf('/'); // NOI18N
        if (idx == -1) {
            throw new ClassNotFoundException("Will not load classes from default package (" + name + ")"); // NOI18N
        }
        String pkg = filename.substring(0, idx + 1); // "org/netbeans/modules/foo/"
        Class c = findLoadedClass(name);
        if(c != null) {
            return c;
        }
        
        Object obj = iterateInOrder(1, name, filename, pkg, null);
        if (obj instanceof ClassNotFoundException) {
            throw (ClassNotFoundException)obj;
        }

        if(obj == null) {
            throw new ClassNotFoundException(name);
        }
        
        c = (Class)obj;
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
    public final URL getResource(final String name) {
        zombieCheck(name);
        
        final int slashIdx = name.lastIndexOf('/');
        if (slashIdx == -1) {
            printDefaultPackageWarning(name);
        }
        final String pkg = name.substring(0, slashIdx + 1);
        
        return (URL)iterateInOrder(2, name, null, pkg, null);
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
    protected final synchronized Enumeration findResources(String name) throws IOException {
        zombieCheck(name);
        final int slashIdx = name.lastIndexOf('/');
        if (slashIdx == -1) {
            printDefaultPackageWarning(name);
        }
        final String pkg = name.substring(0, slashIdx + 1);
        
        Set query = packageOwners(pkg);

        // Don't bother optimizing this call by domains.
        // It is mostly used for resources for which isSpecialResource would be true anyway.
        Enumeration[] es = new Enumeration[parents.length + 1];
        IOException ex = (IOException)iterateInOrder(3, name, null, pkg, es);
        if (ex != null) {
            throw ex;
        }
        // Should not be duplicates (except null), assuming the parent loaders are properly distinct
        // from one another and do not overlap in JAR usage, which they ought not.
        // Anyway MetaInfServicesLookup, the most important client of this method, does
        // its own duplicate filtering already.
        return new AAEnum (es);
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
    protected Enumeration simpleFindResources(String name) throws IOException {
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
     */
    protected Package getPackageFast(String name, String sname, boolean recurse) {
        synchronized (packages) {
            Package pkg = (Package)packages.get(name);
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
		String implVersion, String implVendor, URL sealBase 
    ) throws IllegalArgumentException {
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
        return getPackages(new HashSet());
    }
    
    /**
     * Returns all of the Packages defined by this class loader and its parents.
     * Do not recurse to parents in addedParents set. It speeds up execution
     * time significantly.
     * @return the array of <code>Package</code> objects defined by this
     * <code>ClassLoader</code>
     */
    private synchronized Package[] getPackages(Set addedParents) {
        zombieCheck(null);
        Map all = new HashMap(); // Map<String,Package>
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
        return (Package[])packages.values().toArray(new Package[packages.size()]);
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
        if (! DO_NOT_WARN_DEFAULT_PACKAGE && !"commons-logging.properties".equals(name)) { // NOI18N
            System.err.println("You are trying to access file: " + name + " from the default package."); // NOI18N
            System.err.println("Please see http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/classpath.html#default_package"); // NOI18N
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
        Set resultingUnique = new HashSet(likelySize); // Set<ClassLoader>
        List resulting = new ArrayList(likelySize); // List<ClassLoader>
        for (int i = 0; i < loaders.length; i++) {
            addRec(resultingUnique, resulting, loaders[i]);
        }
        ClassLoader[] ret = (ClassLoader[])resulting.toArray(new ClassLoader[resulting.size()]);
        return ret;
    }
    
    /** Coalesce a new set of loaders into the existing ones.
     */
    private ClassLoader[] coalesceAppend(List existingL, ClassLoader[] appended) throws IllegalArgumentException {
        int likelySize = appended.length + existingL.size() + 3;
        Set resultingUnique = new HashSet(likelySize);
        resultingUnique.addAll(existingL);
        if (resultingUnique.containsAll(Arrays.asList(appended))) {
            // No change required.
            return (ClassLoader[])existingL.toArray(new ClassLoader[0]);
        }
        List resulting = new ArrayList(likelySize);
        resulting.addAll(existingL);
        for (int i = 0; i < appended.length; i++) {
            addRec(resultingUnique, resulting, appended[i]);
        }
        ClassLoader[] ret = (ClassLoader[])resulting.toArray(new ClassLoader[resulting.size()]);
        return ret;
    }
    
    private void addRec(Set resultingUnique, List resulting, ClassLoader loader) throws IllegalArgumentException {
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

    // #29844 run as privileged as it may get called by loadClassInternal() used
    // during class resolving by JVM with arbitrary ProtectionDomain context stack
    private static ClassLoader getClassClassLoader(final Class c) {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return c.getClassLoader();
            }
        });
    }
    
    private static final ClassLoader[] EMPTY = new ClassLoader[0];
    private static final Set ALL = new AbstractSet() {
        public boolean contains(Object o) {
            return true;
        }
        public int size() {
            return Integer.MAX_VALUE;
        }
        public Iterator iterator() {
            return Collections.EMPTY_LIST.iterator();
        }
    };

    /** @param operation 1 == loadClass, 2 == getResource, 3 == getResources 
     */
    private final Object iterateInOrder(
        int operation,
        String name, 
        String fileName, 
        String pkg,
        Enumeration[] out /* output or null */
    ) {
        Set askOnly;
        ClassLoader[] arr;
        ClassNotFoundException cached = null;
        
        ClassLoader checkLast = (ClassLoader)classes.get(pkg);
        if (checkLast != null) {
            askOnly = ALL;
            arr = EMPTY;
        } else {
            askOnly = packageOwners(pkg);
            arr = this.parents;
            checkLast = this;
        }
        
        int len = arr.length;
        for (int i = 0; i <= len; i++) {
            ClassLoader par = i == len ? checkLast : arr[i];
            
            if (par instanceof ProxyClassLoader) {
                ProxyClassLoader p = (ProxyClassLoader)par;
                
                if (askOnly == null) {
                    // ok, we know there is this package missing
                    // among ProxyClassLoaders
                    continue;
                }
                if (!askOnly.contains(p)) {
                    // ok, we know p does not have this package
                    continue;
                }
                
                if (p != this && !shouldDelegateResource(pkg, p)) {
                    // ok, usual check, is here for compatibility
                    // as I do not know what would happen if I removed it
                    continue;
                }
       
                switch (operation) {
                    case 1: {
                        Class c = p.fullFindClass(name, fileName, pkg);
                        // pcl might have have c in its already-loaded classes even though
                        // it was not the defining class loader. In that case, if pcl was
                        // not transitive (should not expose its own parents), reject this.
                        if (c != null) {
                            ClassLoader l = getClassClassLoader(c);
                            if (p.transitive || l == p) {
                                if (askOnly.size() == 1) {
                                    classes.put(pkg, l);
                                }
                            
                                return c;
                            }
                        }
                        break;
                    }
                    case 2: {
                        URL u = p.findResource(name);
                        if (u != null) {
                            return u;
                        }
                        break;
                    }
                    case 3: {
                        try {
                            out[i] = p.simpleFindResources(name);
                        } catch (IOException ex) {
                            return ex;
                        }
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Op: " + operation); // NOI18N
                }
            } else {
                // this is regular classloader, so either we know
                // nothing about the packages or the classloader is in
                // the set
                if (askOnly != null && !askOnly.contains(par)) {
                    continue;
                }
                if (par != this && !shouldDelegateResource(pkg, par)) {
                    continue;
                }
                
                switch (operation) {
                    case 1: {
                        // The following is an optimization, it should not affect semantics:
                        boolean skip = false;
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
                        if (!skip) {
                            try {
                                Class c = par.loadClass(name);
                                if (askOnly == null) {
                                    classes.put(pkg, par);
                                }
                                return c;
                            } catch( ClassNotFoundException cnfe ) {
                                cached = cnfe;
                            }
                        }
                        break;
                    }
                    case 2: {
                        URL u = par.getResource(name);
                        if (u != null) {
                            return u;
                        }
                        break;
                    }
                    case 3: {
                        try {
                            out[i] = par.getResources(name);
                        } catch (IOException ex) {
                            return ex;
                        }
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Op: " + operation); // NOI18N
                }
                
            }
        }
        
        return cached;
    }

    private synchronized Class fullFindClass(String name, String fileName, String pkg) {
        Class c = findLoadedClass(name);
        return (c == null) ? simpleFindClass(name, fileName, pkg) : c;
    }    

    private void addPackages(Map all, Package[] pkgs) {
        // Would be easier if Package.equals() was just defined sensibly...
        for (int i = 0; i < pkgs.length; i++) {
            all.put(pkgs[i].getName(), pkgs[i]);
        }
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
    
    private static final class AAEnum implements Enumeration {
        /** The array */
        private Enumeration[] array;
        /** Current index in the array */
        private int index = 0;

        /** Constructs a new ArrayEnumeration for specified array */
        public AAEnum (Enumeration[] array) {
            this.array = array;
        }

        /** Tests if this enumeration contains more elements.
        * @return  <code>true</code> if this enumeration contains more elements;
        *          <code>false</code> otherwise.
        */
        public boolean hasMoreElements() {
            for (;;) {
                if (index == array.length) {
                    return false;
                }

                if (array[index] != null && array[index].hasMoreElements ()) {
                    return true;
                }
                
                index++;
            }
        }

        /** Returns the next element of this enumeration.
        * @return     the next element of this enumeration.
        * @exception  NoSuchElementException  if no more elements exist.
        */
        public Object nextElement() {
            if (array[index] == null) {
                index++;
                return nextElement();
            }
            try {
                return array[index].nextElement ();
            } catch (NoSuchElementException ex) {
                if (hasMoreElements ()) {
                    // try once more
                    return nextElement ();
                }
                throw ex;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }
    }
}
