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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.StringTokenizer;
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author  tom
 */
public class DefaultClassPathProvider implements ClassPathProvider {
    
    /** Name of package keyword. */
    private static final String PACKAGE = "package";                    //NOI18N
    /**Java file extension */
    private static final String JAVA_EXT = "java";                      //NOI18N
        
    private /*WeakHash*/Map/*<FileObject,WeakReference<FileObject>>*/ sourceRootsCache = new WeakHashMap ();
    private /*WeakHash*/Map/*<FileObject,WeakReference<ClassPath>>*/ sourceClasPathsCache = new WeakHashMap();
    private Reference/*<ClassPath>*/ compiledClassPath;
    
    /** Creates a new instance of DefaultClassPathProvider */
    public DefaultClassPathProvider() {
    }
    
    public synchronized ClassPath findClassPath(FileObject file, String type) {
        if (!file.isValid ()) {
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
                    if (this.compiledClassPath == null || (cp = (ClassPath)this.compiledClassPath.get()) == null) {
                        cp = ClassPathFactory.createClassPath(new CompileClassPathImpl ());
                        this.compiledClassPath = new WeakReference (cp);
                    }
                    return cp;
                }
            }
            else if (ClassPath.SOURCE.equals(type)) {
                synchronized (this) {
                    ClassPath cp = null;
                    if (file.isFolder()) {
                        Reference ref = (Reference) this.sourceClasPathsCache.get (file);
                        if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
                            cp = ClassPathSupport.createClassPath(new FileObject[] {file});
                            this.sourceClasPathsCache.put (file, new WeakReference(cp));
                        }
                    }
                    else {
                        Reference ref = (Reference) this.sourceRootsCache.get (file);
                        FileObject sourceRoot = null;
                        if (ref == null || (sourceRoot = (FileObject)ref.get()) == null ) {
                            sourceRoot = getRootForFile (file);
                            this.sourceRootsCache.put (file, new WeakReference(sourceRoot));
                        }
                        ref = (Reference) this.sourceClasPathsCache.get(sourceRoot);
                        if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
                            cp = ClassPathSupport.createClassPath(new FileObject[] {sourceRoot});
                            this.sourceClasPathsCache.put (sourceRoot, new WeakReference(cp));
                        }
                    }
                    return cp;                                        
                }                    
            }
        }
        return null;
    }            
    
    private static FileObject getRootForFile (final FileObject fo) {
        String pkg = findJavaPackage (fo);
        FileObject sourceRoot = null;
        if (pkg == null) {
            sourceRoot = fo.getParent();
        }
        else {
            List elements = new ArrayList ();
            for (StringTokenizer tk = new StringTokenizer(pkg,"."); tk.hasMoreTokens();) {
                elements.add(tk.nextElement());
            }
            FileObject tmp = fo;
            for (int i=elements.size()-1; i>=0; i--) {
                String name = (String)elements.get(i);
                tmp = tmp.getParent();
                if (!tmp.getName().equals(name)) {
                    tmp = fo;
                    break;
                }                
            }
            sourceRoot = tmp.getParent();
        }
        return sourceRoot;
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
                        } else {
                            // Keep on looking for valid package statement.
                            gotPackage = false;
                            continue;
                        }
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
        } catch (IOException e1) {
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
        
        private List cachedCompiledClassPath;
        private PropertyChangeSupport support;
        
        public CompileClassPathImpl () {
            this.support = new PropertyChangeSupport (this);
        }
        
        public synchronized List getResources () {
            if (this.cachedCompiledClassPath == null) {
                GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
                regs.addGlobalPathRegistryListener(this);
                Set roots = new HashSet ();
                //Add compile classpath
                Set paths = regs.getPaths (ClassPath.COMPILE);
                for (Iterator it = paths.iterator(); it.hasNext();) {
                    ClassPath cp = (ClassPath) it.next();
                    for (Iterator eit = cp.entries().iterator(); eit.hasNext();) {
                        ClassPath.Entry entry = (ClassPath.Entry) eit.next();
                        roots.add (entry.getURL());
                    }                    
                }
                //Add entries from Exec CP which has sources on Sources CP and are not on the Compile CP
                Set sources = regs.getPaths(ClassPath.SOURCE);
                Set sroots = new HashSet ();
                for (Iterator it = sources.iterator(); it.hasNext();) {
                    ClassPath cp = (ClassPath) it.next();
                    for (Iterator eit = cp.entries().iterator(); eit.hasNext();) {
                        ClassPath.Entry entry = (ClassPath.Entry) eit.next();
                        sroots.add (entry.getURL());
                    }                    
                }                
                Set exec = regs.getPaths(ClassPath.EXECUTE);
                for (Iterator it = exec.iterator(); it.hasNext();) {
                    ClassPath cp = (ClassPath) it.next ();
                    for (Iterator eit = cp.entries().iterator(); eit.hasNext();) {
                        ClassPath.Entry entry = (ClassPath.Entry) eit.next ();
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
                List l =  new ArrayList ();
                for (Iterator it = roots.iterator(); it.hasNext();) {
                    l.add (ClassPathSupport.createResource((URL)it.next()));
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
        
        public synchronized void pathsAdded(org.netbeans.api.java.classpath.GlobalPathRegistryEvent event) {
            if (ClassPath.COMPILE.equals(event.getId()) || ClassPath.SOURCE.equals(event.getId())) {
                GlobalPathRegistry.getDefault().removeGlobalPathRegistryListener(this);
                this.cachedCompiledClassPath = null;
            }
            this.support.firePropertyChange(PROP_RESOURCES,null,null);
        }    
    
        public synchronized void pathsRemoved(org.netbeans.api.java.classpath.GlobalPathRegistryEvent event) {
            if (ClassPath.COMPILE.equals(event.getId()) || ClassPath.SOURCE.equals(event.getId())) {
                GlobalPathRegistry.getDefault().removeGlobalPathRegistryListener(this);
                this.cachedCompiledClassPath = null;
            }
            this.support.firePropertyChange(PROP_RESOURCES,null,null);
        }
        
    }
    
}
