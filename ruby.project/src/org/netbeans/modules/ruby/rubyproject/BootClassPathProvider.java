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

package org.netbeans.modules.ruby.rubyproject;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathProvider;
import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
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
        for (RubyPlatform platform : RubyPlatformManager.getPlatforms()) {
            if (!platform.isValid()) {
                continue;
            }
            FileObject systemRoot = platform.getSystemRoot(file);
            if (systemRoot != null) {
                return getRubyClassPaths(file, type, systemRoot);
            }
        }
        
        return null;
    }
    
    private ClassPath getRubyClassPaths(FileObject file, String type, FileObject systemRoot) {
        // Default provider - do this for things like Ruby library files
        synchronized (this) {
            ClassPath cp = null;
            if (!file.isFolder()) {
                file = systemRoot;
            }
            if (file.isFolder()) {
                Reference ref = (Reference) this.sourceClassPathsCache.get (file);
                if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
                    cp = ClassPathSupport.createClassPath(new FileObject[] {file});
                    this.sourceClassPathsCache.put(file, new WeakReference<ClassPath>(cp));
                }
            }
            return cp;                                        
        }
    }
}
