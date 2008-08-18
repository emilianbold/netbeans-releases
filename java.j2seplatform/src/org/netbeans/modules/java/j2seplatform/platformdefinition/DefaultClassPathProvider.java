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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author  tom
 */
public class DefaultClassPathProvider implements ClassPathProvider {
    
    /**Java file extension */
    private static final String JAVA_EXT = "java";                      //NOI18N
    /**Class file extension*/
    private static final String CLASS_EXT = "class";                    //NOI18N

    private /*WeakHash*/Map<FileObject,WeakReference<FileObject>> sourceRootsCache = new WeakHashMap<FileObject,WeakReference<FileObject>>();
    private /*WeakHash*/Map<FileObject,WeakReference<ClassPath>> sourceClasPathsCache = new WeakHashMap<FileObject,WeakReference<ClassPath>>();
    private Reference<ClassPath> compiledClassPath;    
    
    /** Creates a new instance of DefaultClassPathProvider */
    public DefaultClassPathProvider() {
    }
    
    public synchronized ClassPath findClassPath(FileObject file, String type) {
        if (!file.isValid ()) {
            return null;
        }
        // #47099 - PVCS: Externally deleted file causes Exception        
        if (file.isVirtual()) {
            //Can't do more
            return null;
        }
        // #49013 - do not return classpath for files which do 
        // not have EXTERNAL URL, e.g. files from DefaultFS
        // The modified template has an external URL (file) as well as an internal (nbfs)
        // the original check externalURL == null does not work, the classpath with nbfs root
        // is returned. Also it's not possible to create classpath with external URLs  
        // (ClassPathSupport.createClasspath(URLMapper.getURL(root,EXTERNAL))) for these templates
        // since the the returned classpath WILL NOT work correctly (ClassPath.getClassPath(file,SOURCE).findRoot(file)
        // returns null).
        try {
            URL externalURL = URLMapper.findURL(file, URLMapper.EXTERNAL);
            if ( externalURL == null || !externalURL.equals(file.getURL())) {
                return null;
            }
        } catch (FileStateInvalidException fsi) {
            return null;
        }
        if (JAVA_EXT.equalsIgnoreCase(file.getExt()) || file.isFolder()) {  //Workaround: Editor asks for package root
            if (ClassPath.BOOT.equals (type)) {
                JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
                if (defaultPlatform != null) {
                    return defaultPlatform.getBootstrapLibraries();
                }
            }
            else if (ClassPath.COMPILE.equals(type)) {
                synchronized (this) {
                    ClassPath cp = null;
                    if (this.compiledClassPath == null || (cp = this.compiledClassPath.get()) == null) {
                        cp = ClassPathFactory.createClassPath(new CompileClassPathImpl ());
                        this.compiledClassPath = new WeakReference<ClassPath> (cp);
                    }
                    return cp;
                }
            }
            else if (ClassPath.SOURCE.equals(type)) {
//                synchronized (this) {
//                    ClassPath cp = null;
//                    if (file.isFolder()) {
//                        Reference ref = (Reference) this.sourceClasPathsCache.get (file);
//                        if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
//                            cp = ClassPathSupport.createClassPath(new FileObject[] {file});
//                            this.sourceClasPathsCache.put (file, new WeakReference(cp));
//                        }
//                    }
//                    else {
//                        Reference ref = (Reference) this.sourceRootsCache.get (file);
//                        FileObject sourceRoot = null;
//                        if (ref == null || (sourceRoot = (FileObject)ref.get()) == null ) {
//                            sourceRoot = getRootForFile (file, TYPE_JAVA);
//                            if (sourceRoot == null) {
//                                return null;
//                            }
//                            this.sourceRootsCache.put (file, new WeakReference(sourceRoot));
//                        }
//                        if (!sourceRoot.isValid()) {
//                            this.sourceClasPathsCache.remove(sourceRoot);
//                        }
//                        else {
//                            ref = (Reference) this.sourceClasPathsCache.get(sourceRoot);
//                            if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
//                                cp = ClassPathSupport.createClassPath(new FileObject[] {sourceRoot});
//                                this.sourceClasPathsCache.put (sourceRoot, new WeakReference(cp));
//                            }
//                        }
//                    }
//                    return cp;                                        
//                }         
                //XXX: Needed by refactoring of the javaws generated files,
                //anyway it's better to return no source path for files with no project.
                //It has to be ignored by java model anyway otherwise a single java
                //file inside home folder may cause a scan of the whole home folder.
                //see issue #75410
                return null;
            }
        }
        else if (CLASS_EXT.equals(file.getExt())) {
            if (ClassPath.BOOT.equals (type)) {
                JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
                if (defaultPlatform != null) {
                    return defaultPlatform.getBootstrapLibraries();
                }
            }
            else if (ClassPath.EXECUTE.equals(type)) {
                ClassPath cp = null;
                Reference<FileObject> foRef = this.sourceRootsCache.get (file);
                FileObject execRoot = null;
                if (foRef == null || (execRoot = foRef.get()) == null ) {
                    execRoot = JavadocAndSourceRootDetection.findPackageRoot(file);
                    if (execRoot == null || !execRoot.isFolder()) {
                        return null;
                    }
                    this.sourceRootsCache.put (file, new WeakReference<FileObject>(execRoot));
                }
                if (!execRoot.isValid()) {
                    this.sourceClasPathsCache.remove (execRoot);
                }
                else {
                    try {
                        Reference<ClassPath> cpRef = this.sourceClasPathsCache.get(execRoot);
                        if (cpRef == null || (cp = cpRef.get()) == null ) {
                            final URL url = execRoot.getURL();
                            if (!execRoot.isValid()) {
                                //The root is not valid, URL may be broken
                                return null;
                            }
                            cp = ClassPathSupport.createClassPath(url);
                            this.sourceClasPathsCache.put (execRoot, new WeakReference<ClassPath>(cp));
                        }
                        return cp;
                    } catch (FileStateInvalidException e) {
                        //Handled by return null;
                    }
                }
            }
        }
        return null;
    }            
    
    private static class RecursionException extends IllegalStateException {}
    
    private static class CompileClassPathImpl implements ClassPathImplementation, GlobalPathRegistryListener {
        
        private List<? extends PathResourceImplementation> cachedCompiledClassPath;
        private PropertyChangeSupport support;
        private final ThreadLocal<Boolean> active = new ThreadLocal<Boolean> ();
        
        public CompileClassPathImpl () {
            this.support = new PropertyChangeSupport (this);
        }
        
        public synchronized List<? extends PathResourceImplementation> getResources () {
            
            if (this.cachedCompiledClassPath == null) {
                Boolean _active = active.get();
                if (_active == Boolean.TRUE) {
                    throw new RecursionException ();
                }
                active.set(true);
                try {
                    GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
                    regs.addGlobalPathRegistryListener(this);
                    Set<URL> roots = new HashSet<URL> ();
                    //Add compile classpath
                    Set<ClassPath> paths = regs.getPaths (ClassPath.COMPILE);
                    for (Iterator<ClassPath> it = paths.iterator(); it.hasNext();) {
                        ClassPath cp =  it.next();
                        try {
                            for (ClassPath.Entry entry : cp.entries()) {
                                roots.add (entry.getURL());
                            }                    
                        } catch (RecursionException e) {/*Recover from recursion*/}
                    }
                    //Add entries from Exec CP which has sources on Sources CP and are not on the Compile CP
                    Set<ClassPath> sources = regs.getPaths(ClassPath.SOURCE);
                    Set<URL> sroots = new HashSet<URL> ();
                    for (Iterator<ClassPath> it = sources.iterator(); it.hasNext();) {
                        ClassPath cp = it.next();
                        try {
                            for (Iterator<ClassPath.Entry> eit = cp.entries().iterator(); eit.hasNext();) {
                                ClassPath.Entry entry = eit.next();
                                sroots.add (entry.getURL());
                            }                    
                        } catch (RecursionException e) {/*Recover from recursion*/}
                    }                
                    Set<ClassPath> exec = regs.getPaths(ClassPath.EXECUTE);
                    for (Iterator<ClassPath> it = exec.iterator(); it.hasNext();) {
                        ClassPath cp = it.next ();
                        try {
                            for (Iterator<ClassPath.Entry> eit = cp.entries().iterator(); eit.hasNext();) {
                                ClassPath.Entry entry = eit.next ();
                                FileObject[] fos = SourceForBinaryQuery.findSourceRoots(entry.getURL()).getRoots();
                                for (int i=0; i< fos.length; i++) {
                                    try {
                                        if (sroots.contains(fos[i].getURL())) {
                                            roots.add (entry.getURL());
                                        }
                                    } catch (FileStateInvalidException e) {
                                        ErrorManager.getDefault().notify(e);
                                    }                                
                                }
                            }
                        } catch (RecursionException e) {/*Recover from recursion*/}
                    }
                    List<PathResourceImplementation> l =  new ArrayList<PathResourceImplementation> ();
                    for (Iterator it = roots.iterator(); it.hasNext();) {
                        l.add (ClassPathSupport.createResource((URL)it.next()));
                    }
                    this.cachedCompiledClassPath = Collections.unmodifiableList(l);
                } finally {
                    active.remove();
                }
            }
            return this.cachedCompiledClassPath;
            
        }
        
        public void addPropertyChangeListener (PropertyChangeListener l) {
            this.support.addPropertyChangeListener (l);
        }
        
        public void removePropertyChangeListener (PropertyChangeListener l) {
            this.support.removePropertyChangeListener (l);
        }
        
        public void pathsAdded(org.netbeans.api.java.classpath.GlobalPathRegistryEvent event) {
            synchronized (this) {
                if (ClassPath.COMPILE.equals(event.getId()) || ClassPath.SOURCE.equals(event.getId())) {
                    GlobalPathRegistry.getDefault().removeGlobalPathRegistryListener(this);
                    this.cachedCompiledClassPath = null;
                }
            }
            this.support.firePropertyChange(PROP_RESOURCES,null,null);
        }    
    
        public void pathsRemoved(org.netbeans.api.java.classpath.GlobalPathRegistryEvent event) {
            synchronized (this) {
                if (ClassPath.COMPILE.equals(event.getId()) || ClassPath.SOURCE.equals(event.getId())) {
                    GlobalPathRegistry.getDefault().removeGlobalPathRegistryListener(this);
                    this.cachedCompiledClassPath = null;
                }
            }
            this.support.firePropertyChange(PROP_RESOURCES,null,null);
        }
        
    }
    
}
