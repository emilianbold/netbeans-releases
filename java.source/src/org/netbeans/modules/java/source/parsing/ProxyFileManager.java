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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.modules.java.source.util.Iterators;

/**
 *
 * @author Tomas Zezula
 */
public class ProxyFileManager implements JavaFileManager {

    private static final Location ALL = new Location () {
        public String getName() { return "ALL";}   //NOI18N        

        public boolean isOutputLocation() { return false; }
    };
    
    private final JavaFileManager bootPath;
    private final JavaFileManager classPath;
    private final JavaFileManager sourcePath;
    private final JavaFileManager outputhPath;       
    
    private JavaFileObject lastInfered;
    private String lastInferedResult;
    
    /** Creates a new instance of ProxyFileManager */
    public ProxyFileManager(JavaFileManager bootPath, JavaFileManager classPath, JavaFileManager sourcePath, JavaFileManager outputhPath) {
        assert bootPath != null;
        assert classPath != null;
        this.bootPath = bootPath;
        this.classPath = classPath;
        this.sourcePath = sourcePath;
        this.outputhPath = outputhPath;
    }
    
    private JavaFileManager[] getFileManager (Location location) {
        if (location == StandardLocation.CLASS_PATH) {            
            return this.outputhPath == null ? 
                new JavaFileManager[] {this.classPath} :
                new JavaFileManager[] {this.classPath, this.outputhPath};
        }
        else if (location == StandardLocation.PLATFORM_CLASS_PATH) {
            return new JavaFileManager[] {this.bootPath};
        }
        else if (location == StandardLocation.SOURCE_PATH && this.sourcePath != null) {
            return new JavaFileManager[] {this.sourcePath};
        }
        else if (location == StandardLocation.CLASS_OUTPUT && this.outputhPath != null) {
            return new JavaFileManager[] {this.outputhPath};
        }
        else if (location == ALL) {
            return this.outputhPath == null ?
                new JavaFileManager[] {
                    this.sourcePath,
                    this.bootPath,
                    this.classPath
                }:
                new JavaFileManager[] {
                    this.sourcePath,
                    this.bootPath,
                    this.classPath,
                    this.outputhPath
                };
        }
        else {
            return new JavaFileManager[0];
        }
    }   
    
    private JavaFileManager[] getAllFileManagers () {
        List<JavaFileManager> result = new ArrayList<JavaFileManager> (4);
        result.add(this.bootPath);
        result.add (this.classPath);
        if (this.sourcePath!=null) {
            result.add (this.sourcePath);
        }
        if (this.outputhPath!=null) {
            result.add (this.outputhPath);
        }
        return result.toArray(new JavaFileManager[result.size()]);
    }
    
    public Iterable<JavaFileObject> list(Location l, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        List<Iterable<JavaFileObject>> iterables = new LinkedList<Iterable<JavaFileObject>>();
        JavaFileManager[] fms = getFileManager (l);
        for (JavaFileManager fm : fms) {
            iterables.add( fm.list(l, packageName, kinds, recurse)); 
        }        
        return Iterators.chained(iterables);
    }

    public FileObject getFileForInput(Location l, String packageName, String relativeName) throws IOException {
        JavaFileManager[] fms = getFileManager(l);        
        for (JavaFileManager fm : fms) {
            FileObject result = fm.getFileForInput(l, packageName, relativeName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    public FileObject getFileForOutput(Location l, String packageName, String relativeName, FileObject sibling) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        JavaFileManager[] fms = getFileManager (l);
        assert fms.length <=1;
        if (fms.length == 0) {
            return null;
        }
        else {
            return fms[0].getFileForOutput(l, packageName, relativeName, sibling);
        }
    }
    
    public ClassLoader getClassLoader (Location l) {
        return null;
    }
    
    public void flush() throws IOException {
        JavaFileManager[] fms = getAllFileManagers ();
        for (JavaFileManager fm : fms) {
            fm.flush();
        }
    }

    public void close() throws IOException {
        JavaFileManager[] fms = getAllFileManagers ();
        for (JavaFileManager fm : fms) {
            fm.close();
        }
    }

    public int isSupportedOption(String string) {
        return -1;
    }
    
    public boolean handleOption (String current, Iterator<String> remains) {
        return false;
    }

    public boolean hasLocation(JavaFileManager.Location location) {
        return location == StandardLocation.CLASS_PATH ||
               location == StandardLocation.PLATFORM_CLASS_PATH ||
               location == StandardLocation.SOURCE_PATH ||
               location == StandardLocation.CLASS_OUTPUT;
    }
    
    public JavaFileObject getJavaFileForInput (Location l, String className, JavaFileObject.Kind kind) throws IOException {
        JavaFileManager[] fms = getFileManager (l);
        for (JavaFileManager fm : fms) {
            JavaFileObject result = fm.getJavaFileForInput(l,className,kind);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public JavaFileObject getJavaFileForOutput(Location l, String className, JavaFileObject.Kind kind, FileObject sibling) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        JavaFileManager[] fms = getFileManager (l);
        assert fms.length <=1;
        if (fms.length == 0) {
            return null;
        }
        else {
            return fms[0].getJavaFileForOutput (l, className, kind, sibling);
        }
    }   
    
    
    public String inferBinaryName(JavaFileManager.Location location, JavaFileObject javaFileObject) {
        assert javaFileObject != null;
        //If cached return it dirrectly
        if (javaFileObject == lastInfered) {
            return lastInferedResult;
        }
        String result;
        //If instanceof FileObject.Base no need to delegate it
        if (javaFileObject instanceof FileObjects.Base) {
            final FileObjects.Base base = (FileObjects.Base) javaFileObject;
            final StringBuilder sb = new StringBuilder ();
            sb.append (base.getPackage());
            if (sb.length()>0) {
                sb.append('.'); //NOI18N
            }
            sb.append(base.getNameWithoutExtension());
            result = sb.toString();
            this.lastInfered = javaFileObject;
            this.lastInferedResult = result;
            return result;
        }
        //Ask delegates to infer the binary name
        JavaFileManager[] fms = getFileManager (location);
        for (JavaFileManager fm : fms) {
            result = fm.inferBinaryName (location, javaFileObject);
            if (result != null && result.length() > 0) {
                this.lastInfered = javaFileObject;
                this.lastInferedResult = result;
                return result;
            }
        }        
        return null;
    }                

    public boolean isSameFile(FileObject fileObject, FileObject fileObject0) {
        final JavaFileManager[] fms = getFileManager(ALL);
        for (JavaFileManager fm : fms) {
            if (fm.isSameFile(fileObject, fileObject0)) {
                return true;
            }
        }
        return fileObject.toUri().equals (fileObject0.toUri());
    }
    
}
