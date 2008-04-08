/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe    //Currently not thread save - caller should hold java.source lock
public class MemoryFileManager implements JavaFileManager {
    
    //Todo: Can be mem optimzed by converting to packed UTF.
    private final Map<String,List<Integer>> packages = new HashMap<String, List<Integer>>();
    private final Map<Integer,Info> content = new HashMap<Integer,Info>(); 
    private final AtomicInteger currentId = new AtomicInteger ();

    public MemoryFileManager () {        
    }

    public ClassLoader getClassLoader(Location location) {
        //Don't support class loading
        throw new UnsupportedOperationException ();
    }

    //Covariant return type - used by unit test
    public List<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        if (recurse) {
            throw new UnsupportedEncodingException();
        }
        final List<JavaFileObject> result = new LinkedList<JavaFileObject> ();        
        if (location == StandardLocation.SOURCE_PATH) {
            final List<Integer> pkglst = packages.get(packageName);
            if (pkglst != null) {
                for (Integer foid : pkglst) {
                    Info data = content.get(foid);
                    assert data != null;
                    if (kinds.contains(FileObjects.getKind(FileObjects.getExtension((data.name instanceof String) ? (String) data.name : data.name.toString())))) {
                        result.add(FileObjects.memoryFileObject(packageName, data.name, data.mtime, data.content));
                    }
                }
            }
        }
        return result;
    }

    public String inferBinaryName(Location location, JavaFileObject file) {
        if (location == StandardLocation.SOURCE_PATH) {
            if (file instanceof FileObjects.InferableJavaFileObject) {
                return ((FileObjects.InferableJavaFileObject)file).inferBinaryName();
            }
        }
        return null;
    }

    public boolean isSameFile(FileObject a, FileObject b) {
        return a == null ? b == null : (b == null ? false : a.toUri().equals(b.toUri()));
    }

    public boolean handleOption(String current, Iterator<String> remaining) {
        return false;
    }

    public boolean hasLocation(Location location) {
        return location == StandardLocation.SOURCE_PATH;
    }

    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        if (location == StandardLocation.SOURCE_PATH) {            
            final String[] namePair = FileObjects.getPackageAndName (className);
            if (namePair != null) {
                final List<Integer> pkglst = this.packages.get(namePair[0]);
                if (pkglst != null) {
                    final String relativeName = namePair[1] + kind.extension;
                    for (Integer id : pkglst) {
                        final Info info = this.content.get (id);
                        assert info != null;
                        if (relativeName.equals(info.name)) {
                            return FileObjects.memoryFileObject(namePair[0], info.name, info.mtime, info.content);
                        }
                    }
                }
            }
        }
        return null;
    }

    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        throw new UnsupportedOperationException();
    }

    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        if (location == StandardLocation.SOURCE_PATH) {                   
            final List<Integer> pkglst = packages.get(packageName);
            if (pkglst != null) {
                for (Integer id : pkglst) {
                    final Info info = this.content.get (id);
                    assert info != null;
                    if (relativeName.equals(info.name)) {
                        return FileObjects.memoryFileObject(packageName, info.name, info.mtime, info.content);
                    }
                }
            }
        }
        return null;
    }

    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        throw new UnsupportedOperationException("");
    }

    public void flush() throws IOException {        
    }

    public void close() throws IOException {
        this.packages.clear();
        this.content.clear();
    }

    public int isSupportedOption(String option) {
        return -1;
    }
    
    public boolean register (final String pkg, final String name,
            final JavaFileObject.Kind kind,
            final long mtime,
            final CharSequence content) {
        Parameters.notNull("pkg", pkg);     //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        Parameters.notNull("kind", kind);
        Parameters.notNull("content", content); //NOI18N
        Parameters.notEmpty("name", name);  //NOI18N
        List<Integer> ids = this.packages.get(pkg);
        if (ids == null) {
            ids = new LinkedList<Integer>();
            this.packages.put(pkg, ids);
        }
        final String nameExt = name + kind.extension;
        //Check for duplicate
        for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
            final Integer id = it.next();
            final Info info = this.content.get(id);
            assert info != null;
            if (nameExt.equals(info.name)) {
                this.content.put(id, new Info(nameExt, mtime, content));
                return true;
            }
        }        
        //Todo: add
        final Integer id = currentId.getAndIncrement();
        this.content.put(id, new Info(nameExt, mtime, content));
        ids.add(id);
        return false;        
    }
    
    public boolean register (final String fqn, final long mtime, final CharSequence content) {
        Parameters.notNull("fqn", fqn);
        final String[] pkgName = FileObjects.getPackageAndName (fqn);
        return this.register(pkgName[0], pkgName[1], JavaFileObject.Kind.SOURCE, mtime, content);
    }
    
    public boolean unregister (final String pkg, final String name,
            final JavaFileObject.Kind kind) {
        Parameters.notNull("pkg", pkg);     //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        Parameters.notNull("kind", kind);   //NOI18N
        Parameters.notEmpty("name", name);  //NOI18N
        final List<Integer> ids = this.packages.get(pkg);
        final String nameExt = name + kind.extension;
        for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
            final Integer id = it.next();
            final Info info = this.content.get(id);
            assert info != null;
            if (nameExt.equals(info.name)) {
                it.remove();
                this.content.remove(id);
                return true;
            }
        }        
        return false;
    }
    
    public boolean unregister (final String fqn) {
        Parameters.notNull("fqn", fqn);
        final String[] pkgName = FileObjects.getPackageAndName (fqn);
        return this.unregister(pkgName[0], pkgName[1], JavaFileObject.Kind.SOURCE);
    }
    
    //Todo: Move to FileObjects?
    
    
    
    private static final class Info {
        public final String name;
        public final long mtime;
        public final CharSequence content;
        
        public Info (String name, long mtime, CharSequence content) {
            this.name = name;
            this.mtime = mtime;
            this.content = content;
        }
    }
}