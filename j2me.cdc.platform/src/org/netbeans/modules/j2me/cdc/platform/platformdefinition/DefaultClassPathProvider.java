/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2me.cdc.platform.platformdefinition;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author  tom
 */
public class DefaultClassPathProvider implements ClassPathProvider {
    
    /** Name of package keyword. */
    private static final String PACKAGE = "package";                    //NOI18N
    /**Java file extension */
    private static final String JAVA_EXT = "java";                      //NOI18N
    /**Class file extension*/
    private static final String CLASS_EXT = "class";                    //NOI18N

    private static final int TYPE_JAVA = 1;

    private static final int TYPE_CLASS = 2;

    private Map<FileObject,WeakReference<FileObject>> sourceRootsCache = new WeakHashMap<FileObject,WeakReference<FileObject>> ();
    private Map<FileObject,WeakReference<ClassPath>> sourceClasPathsCache = new WeakHashMap<FileObject,WeakReference<ClassPath>>();
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
        if (URLMapper.findURL(file, URLMapper.EXTERNAL) == null) {
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
                synchronized (this) {
                    ClassPath cp = null;
                    if (file.isFolder()) {
                        Reference ref = this.sourceClasPathsCache.get (file);
                        if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
                            cp = ClassPathSupport.createClassPath(new FileObject[] {file});
                            this.sourceClasPathsCache.put (file, new WeakReference<ClassPath>(cp));
                        }
                    }
                    else {
                        Reference ref = this.sourceRootsCache.get (file);
                        FileObject sourceRoot = null;
                        if (ref == null || (sourceRoot = (FileObject)ref.get()) == null ) {
                            sourceRoot = getRootForFile (file, TYPE_JAVA);
                            if (sourceRoot == null) {
                                return null;
                            }
                            this.sourceRootsCache.put (file, new WeakReference<FileObject>(sourceRoot));
                        }
                        if (!sourceRoot.isValid()) {
                            this.sourceClasPathsCache.remove(sourceRoot);
                        }
                        else {
                            ref = this.sourceClasPathsCache.get(sourceRoot);
                            if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
                                cp = ClassPathSupport.createClassPath(new FileObject[] {sourceRoot});
                                this.sourceClasPathsCache.put (sourceRoot, new WeakReference<ClassPath>(cp));
                            }
                        }
                    }
                    return cp;                                        
                }                    
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
                Reference<FileObject> ref = this.sourceRootsCache.get (file);
                FileObject execRoot = null;
                if (ref == null || (execRoot = ref.get()) == null ) {
                    execRoot = getRootForFile (file, TYPE_CLASS);
                    if (execRoot == null) {
                        return null;
                    }
                    this.sourceRootsCache.put (file, new WeakReference<FileObject>(execRoot));
                }
                if (!execRoot.isValid()) {
                    this.sourceClasPathsCache.remove (execRoot);
                }
                else {
                    Reference<ClassPath> cpRef = this.sourceClasPathsCache.get(execRoot);
                    if (ref == null || (cp = cpRef.get()) == null ) {
                        cp = ClassPathSupport.createClassPath(new FileObject[] {execRoot});
                        this.sourceClasPathsCache.put (execRoot, new WeakReference<ClassPath>(cp));
                    }
                    return cp;
                }
            }
        }
        return null;
    }            
    
    private static FileObject getRootForFile (final FileObject fo, int type) {
        String pkg;
        if (type == TYPE_JAVA) {
            pkg = findJavaPackage (fo);
        }
        else  {
            pkg = findClassPackage (fo);
        }
        FileObject packageRoot = null;
        if (pkg == null) {
            packageRoot = fo.getParent();
        }
        else {
            List<String> elements = new ArrayList<String> ();
            for (StringTokenizer tk = new StringTokenizer(pkg,"."); tk.hasMoreTokens();) {
                elements.add(tk.nextToken());
            }
            FileObject tmp = fo;
            for (String name : elements ) {
                tmp = tmp.getParent();
                if (tmp == null || !tmp.getName().equals(name)) {
                    tmp = fo;
                    break;
                }                
            }
            packageRoot = tmp.getParent();
        }
        return packageRoot;
    }


    /**
     * Find java package in side .class file.
     *
     * @return package or null if not found
     */
    private static final String findClassPackage (FileObject file) {
        try {
            InputStream in = file.getInputStream();
            try {
                ClassFile cf = new ClassFile(in,false);
                ClassName cn = cf.getName();
                return cn.getPackage();
            } finally {
                in.close ();
            }
        } catch (FileNotFoundException fnf) {
            //Ignore it
            // The file was removed after checking it for isValid
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }

    /**
     * Find java package in side .java file. 
     *
     * @return package or null if not found
     */
    private static String findJavaPackage(FileObject file) {
        String pkg = ""; // NOI18N
        boolean packageKnown = false;
        
        // Try to find the package name and then infer a directory to mount.
        BufferedReader rd = null;

        try {
            int pckgPos; // found package position

            rd = new BufferedReader(new SourceReader(file.getInputStream()));

            // Check for unicode byte watermarks.
            rd.mark(2);
            char[] cbuf = new char[2];
            rd.read(cbuf, 0, 2);
            
            if (cbuf[0] == 255 && cbuf[1] == 254) {
                rd.close();
                rd = new BufferedReader(new SourceReader(file.getInputStream(), "Unicode")); // NOI18N
            } else {
                rd.reset();
            }

            while (!packageKnown) {
                String line = rd.readLine();
                if (line == null) {
                    packageKnown = true; // i.e. valid termination of search, default pkg
                    //break;
                    return pkg;
                }

                pckgPos = line.indexOf(PACKAGE);
                if (pckgPos == -1) {
                    continue;
                }
                StringTokenizer tok = new StringTokenizer(line, " \t;"); // NOI18N
                boolean gotPackage = false;
                while (tok.hasMoreTokens()) {
                    String theTok = tok.nextToken ();
                    if (gotPackage) {
                        // Hopefully the package name, but first a sanity check...
                        StringTokenizer ptok = new StringTokenizer(theTok, "."); // NOI18N
                        boolean ok = ptok.hasMoreTokens();
                        while (ptok.hasMoreTokens()) {
                            String component = ptok.nextToken();
                            if (component.length() == 0) {
                                ok = false;
                                break;
                            }
                            if (!Character.isJavaIdentifierStart(component.charAt(0))) {
                                ok = false;
                                break;
                            }
                            for (int pos = 1; pos < component.length(); pos++) {
                                if (!Character.isJavaIdentifierPart(component.charAt(pos))) {
                                    ok = false;
                                    break;
                                }
                            }
                        }
                        if (ok) {
                            pkg = theTok;
                            packageKnown = true;
                            //break; 
                            return pkg;
                        } 
                        // Keep on looking for valid package statement.
                        gotPackage = false;
                        continue;
                    } else if (theTok.equals (PACKAGE)) {
                        gotPackage = true;
                    } else if (theTok.equals ("{")) { // NOI18N
                        // Most likely we can stop if hit opening brace of class def.
                        // Usually people leave spaces around it.
                        packageKnown = true; // valid end of search, default pkg
                        // break; 
                        return pkg;
                    }
                }
            }
        } catch (FileNotFoundException fnf) {
            //Ignore it
            //The file was probably removed after it was checked for isValid
        }
        catch (IOException e1) {
            ErrorManager.getDefault().notify(e1);
        } finally {
            try {
                if (rd != null) {
                    rd.close();
                }
            } catch (IOException e2) {
                ErrorManager.getDefault().notify(e2);
            }
        }
        
        return null;
    }
    
    /**
     * Filtered reader for Java sources - it simply excludes
     * comments and some useless whitespaces from the original stream.
     */
    public static class SourceReader extends InputStreamReader {
        private int preRead = -1;
        private boolean inString = false;
        private boolean backslashLast = false;
        private boolean separatorLast = false;
        static private final char separators[] = {'.'}; // dot is enough here...
        static private final char whitespaces[] = {' ', '\t', '\r', '\n'};
        
        public SourceReader(InputStream in) {
            super(in);
        }
        
        public SourceReader(InputStream in, String encoding) throws UnsupportedEncodingException {
            super(in, encoding);
        }

        /** Reads chars from input reader and filters them. */
        public int read(char[] data, int pos, int len) throws IOException {
            int numRead = 0;
            int c;
            char[] onechar = new char[1];
            
            while (numRead < len) {
                if (preRead != -1) {
                    c = preRead;
                    preRead = -1;
                } else {
                    c = super.read(onechar, 0, 1);
                    if (c == -1) {   // end of stream reached
                        return (numRead > 0) ? numRead : -1;
                    }
                    c = onechar[0];
                }
                
                if (c == '/' && !inString) { // a comment could start here
                    preRead = super.read(onechar, 0, 1);
                    if (preRead == 1) {
                        preRead = onechar[0];
                    }
                    if (preRead != '*' && preRead != '/') { // it's not a comment
                        data[pos++] = (char) c;
                        numRead++;
                        if (preRead == -1) {   // end of stream reached
                            return numRead;
                        }
                    } else { // we have run into the comment - skip it
                        if (preRead == '*') { // comment started with /*
                            preRead = -1;
                            do {
                                c = moveToChar('*');
                                if (c == 0) {
                                    c = super.read(onechar, 0, 1);
                                    if (c == 1) {
                                        c = onechar[0];
                                    }
                                    if (c == '*') {
                                        preRead = c;
                                    }
                                }
                            } while (c != '/' && c != -1);
                        } else { // comment started with //
                            preRead = -1;
                            c = moveToChar('\n');
                            if (c == 0) {
                                preRead = '\n';
                            }
                        }
                        if (c == -1) {   // end of stream reached
                            return -1;
                        }
                    }
                } else { // normal valid character
                    if (!inString) { // not inside a string " ... "
                        if (isWhitespace(c)) { // reduce some whitespaces
                            while (true) {
                                preRead = super.read(onechar, 0, 1);
                                if (preRead == -1) {   // end of stream reached
                                    return (numRead > 0) ? numRead : -1;
                                }
                                preRead = onechar[0];

                                if (isSeparator(preRead)) {
                                    c = preRead;
                                    preRead = -1;
                                    break;
                                } else if (!isWhitespace(preRead)) {
                                    if (separatorLast) {
                                        c = preRead;
                                        preRead = -1;
                                    }
                                    break;
                                }
                            }
                        }
                        
                        if (c == '\"' || c == '\'') {
                            inString = true;
                            separatorLast = false;
                        } else {
                            separatorLast = isSeparator(c);
                        }
                    } else { // we are just in a string
                        if (c == '\"' || c == '\'') {
                            if (!backslashLast) {
                                inString = false;
                            } else {
                                backslashLast = false;
                            }
                        } else {
                            backslashLast = (c == '\\');
                        }
                    }

                    data[pos++] = (char) c;
                    numRead++;
                }
            }
            return numRead;
        }
        
        private int moveToChar(int c) throws IOException {
            int cc;
            char[] onechar = new char[1];

            if (preRead != -1) {
                cc = preRead;
                preRead = -1;
            } else {
                cc = super.read(onechar, 0, 1);
                if (cc == 1) {
                    cc = onechar[0];
                }
            }

            while (cc != -1 && cc != c) {
                cc = super.read(onechar, 0, 1);
                if (cc == 1) {
                    cc = onechar[0];
                }
            }

            return (cc == -1) ? -1 : 0;
        }

        static private boolean isSeparator(int c) {
            for (int i=0; i < separators.length; i++) {
                if (c == separators[i]) {
                    return true;
                }
            }
            return false;
        }

        static private boolean isWhitespace(int c) {
            for (int i=0; i < whitespaces.length; i++) {
                if (c == whitespaces[i]) {
                    return true;
                }
            }
            return false;
        }
    } // End of class SourceReader.
    
    private static class CompileClassPathImpl implements ClassPathImplementation, GlobalPathRegistryListener {
        
        private List<PathResourceImplementation> cachedCompiledClassPath;
        private PropertyChangeSupport support;
        
        public CompileClassPathImpl () {
            this.support = new PropertyChangeSupport (this);
        }
        
        public synchronized List<PathResourceImplementation> getResources () {
            if (this.cachedCompiledClassPath == null) {
                GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
                regs.addGlobalPathRegistryListener(this);
                Set<URL> roots = new HashSet<URL> ();
                //Add compile classpath
                Set<ClassPath> paths = regs.getPaths (ClassPath.COMPILE);
                for (ClassPath cp : paths) {
                    for (ClassPath.Entry entry : (java.util.List<ClassPath.Entry>)cp.entries()) {
                        roots.add (entry.getURL());
                    }                    
                }
                //Add entries from Exec CP which has sources on Sources CP and are not on the Compile CP
                Set<ClassPath> sources = regs.getPaths(ClassPath.SOURCE);
                Set<URL> sroots = new HashSet<URL> ();
                for (ClassPath cp : sources ) {
                    for (ClassPath.Entry entry : (java.util.List<ClassPath.Entry>)cp.entries()) {
                        sroots.add (entry.getURL());
                    }                    
                }                
                Set<ClassPath> exec = regs.getPaths(ClassPath.EXECUTE);
                for (ClassPath cp : exec) {
                    for (ClassPath.Entry entry : (java.util.List<ClassPath.Entry>)cp.entries()) {
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
                }
                List<PathResourceImplementation> l =  new ArrayList<PathResourceImplementation> ();
                for (URL url : roots) {
                    l.add (ClassPathSupport.createResource(url));
                }
                this.cachedCompiledClassPath = Collections.unmodifiableList(l);
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
