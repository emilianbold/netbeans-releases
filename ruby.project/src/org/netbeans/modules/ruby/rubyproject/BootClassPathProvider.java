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

package org.netbeans.modules.ruby.rubyproject;

import org.netbeans.api.ruby.platform.RubyInstallation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.spi.gsfpath.classpath.ClassPathProvider;
import org.netbeans.spi.gsfpath.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 * Supplies classpath information for Ruby installation files such as
 * gems, the standard library, corelibrary stubs, etc.  Based on the
 * Default provider in j2seplatform.
 * 
 * @author Tor Norbye
 */
public class BootClassPathProvider implements ClassPathProvider {
    
    private Map<FileObject, WeakReference<ClassPath>> sourceClassPathsCache =
            new WeakHashMap<FileObject, WeakReference<ClassPath>>();
    
    //private /*WeakHash*/Map/*<FileObject,WeakReference<FileObject>>*/ sourceRootsCache = new WeakHashMap ();
    //private Reference/*<ClassPath>*/ compiledClassPath;
    
    /** Default constructor for lookup. */
    public BootClassPathProvider() {}
    
    public ClassPath findClassPath(FileObject file, String type) {
        // See if the file is under the Ruby libraries
        RubyInstallation installation = RubyInstallation.getInstance();
        FileObject rubylib = installation.getRubyLibFo();
        FileObject rubyStubs = installation.getRubyStubs();
        while (file != null) {
            if (file == rubylib || file == rubyStubs) {
                return getRubyClassPaths(file, type);
            }
            
            file = file.getParent();
        }
        
        return null;
    }
    
    private ClassPath getRubyClassPaths(FileObject file, String type) {
            // Default provider - do this for things like Ruby library files
               synchronized (this) {
                    ClassPath cp = null;
                    if (file.isFolder()) {
                        Reference ref = (Reference) this.sourceClassPathsCache.get (file);
                        if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
                            cp = ClassPathSupport.createClassPath(new FileObject[] {file});
                            this.sourceClassPathsCache.put(file, new WeakReference<ClassPath>(cp));
                        }
                    }
                    else {
                        //Reference ref = (Reference) this.sourceRootsCache.get (file);
                        //FileObject sourceRoot = null;
                        //if (ref == null || (sourceRoot = (FileObject)ref.get()) == null ) {
                        //    sourceRoot = getRootForFile (file, TYPE_JAVA);
                        //    if (sourceRoot == null) {
                        //        return null;
                        //    }
                        //    this.sourceRootsCache.put (file, new WeakReference(sourceRoot));
                        //}
                        //if (!sourceRoot.isValid()) {
                        //    this.sourceClasPathsCache.remove(sourceRoot);
                        //}
                        //else {
                        //    ref = (Reference) this.sourceClasPathsCache.get(sourceRoot);
                        //    if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
                        //        cp = ClassPathSupport.createClassPath(new FileObject[] {sourceRoot});
                        //        this.sourceClasPathsCache.put (sourceRoot, new WeakReference(cp));
                        //    }
                        //}
                        return null;
                    }
                    return cp;                                        
                }
    }
    
}
