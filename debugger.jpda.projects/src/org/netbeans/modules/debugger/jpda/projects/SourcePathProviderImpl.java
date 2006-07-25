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

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.netbeans.api.java.platform.JavaPlatformManager;

import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;


/**
 *
 * @author Jan Jancura
 */
public class SourcePathProviderImpl extends SourcePathProvider {
    
    private static boolean          verbose = 
        System.getProperty ("netbeans.debugger.sourcepathproviderimpl") != null;
    
    private static final Pattern thisDirectoryPattern = Pattern.compile("(/|\\A)\\./");
    private static final Pattern parentDirectoryPattern = Pattern.compile("(/|\\A)([^/]+?)/\\.\\./");

    private Session                 session;
    // contains source path + jdk source path for JPDAStart task
    private ClassPath               originalSourcePath;
    private ClassPath               smartSteppingSourcePath;
    private String[]                sourceRoots;
    private PropertyChangeSupport   pcs;
    

    public SourcePathProviderImpl (ContextProvider contextProvider) {
        pcs = new PropertyChangeSupport (this);
        this.session = (Session) contextProvider.lookupFirst 
            (null, Session.class);
        Map properties = (Map) contextProvider.lookupFirst 
            (null, Map.class);
        
        // 2) get default allSourceRoots of source roots used for stepping
        if (properties != null) {
            smartSteppingSourcePath = (ClassPath) properties.get ("sourcepath");
            ClassPath jdkCP = (ClassPath) properties.get ("jdksources");
            if ( (jdkCP == null) && (JavaPlatform.getDefault () != null) )
                jdkCP = JavaPlatform.getDefault ().getSourceFolders ();
            originalSourcePath = jdkCP == null ? 
                smartSteppingSourcePath :
                ClassPathSupport.createProxyClassPath (
                    new ClassPath[] {
                        smartSteppingSourcePath,
                        jdkCP
                    }
            );
        } else {
            Set allSourceRoots = new HashSet (
                GlobalPathRegistry.getDefault ().getSourceRoots ()
            );
            originalSourcePath = ClassPathSupport.createClassPath (
                (FileObject[]) allSourceRoots.toArray 
                    (new FileObject [allSourceRoots.size()])
            );
            
            JavaPlatform[] platforms = JavaPlatformManager.getDefault ().
                getInstalledPlatforms ();
            int i, k = platforms.length;
            for (i = 0; i < k; i++) {
                FileObject[] roots = platforms [i].getSourceFolders ().
                    getRoots ();
                int j, jj = roots.length;
                for (j = 0; j < jj; j++)
                    allSourceRoots.remove (roots [j]);
            }
            smartSteppingSourcePath = ClassPathSupport.createClassPath (
                (FileObject[]) allSourceRoots.toArray 
                    (new FileObject [allSourceRoots.size()])
            );
        }
        sourceRoots = getRoots (
            Arrays.asList (smartSteppingSourcePath.getRoots ()).iterator ()
        );
        
        if (verbose) 
            System.out.println 
                ("SPPI: init originalSourcePath " + originalSourcePath);    
        if (verbose) 
            System.out.println (
                "SPPI: init smartSteppingSourcePath " + smartSteppingSourcePath
            );    
    }

    /**
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    public String getURL (String relativePath, boolean global) {    if (verbose) System.out.println ("SPPI: getURL " + relativePath + " global " + global);
        FileObject fo = null;
        relativePath = normalize(relativePath);
        if (!global) {
            fo = smartSteppingSourcePath.findResource 
                (relativePath);                                         if (verbose) System.out.println ("SPPI:   fo " + fo);
        } else {
            fo = originalSourcePath.findResource 
                (relativePath);                                         if (verbose) System.out.println ("SPPI:   fo " + fo);
            if (fo == null)
                fo = GlobalPathRegistry.getDefault ().findResource (relativePath);      if (verbose) System.out.println ("SPPI:   fo2 " + fo);
        }
        if (fo == null) return null;
        try {
            return fo.getURL ().toString ();
        } catch (FileStateInvalidException e) {                     if (verbose) System.out.println ("SPPI:   FileStateInvalidException");
            return null;
        }
    }
    
    /**
     * Returns relative path for given url.
     *
     * @param url a url of resource file
     * @param directorySeparator a directory separator character
     * @param includeExtension whether the file extension should be included 
     *        in the result
     *
     * @return relative path
     */
    public String getRelativePath (
        String url, 
        char directorySeparator, 
        boolean includeExtension
    ) {
        // 1) url -> FileObject
        FileObject fo = null;                                       if (verbose) System.out.println ("SPPI: getRelativePath " + url);
        try {
            fo = URLMapper.findFileObject (new URL (url));          if (verbose) System.out.println ("SPPI:   fo " + fo);
        } catch (MalformedURLException e) {
            //e.printStackTrace ();
            return null;
        }
        ClassPath cp = ClassPath.getClassPath (fo, ClassPath.SOURCE);
        if (cp == null)
            cp = ClassPath.getClassPath (fo, ClassPath.COMPILE);
        if (cp == null) return null;
        return cp.getResourceName (
            fo, 
            directorySeparator,
            includeExtension
        );
    }
    
    /**
     * Returns the source root (if any) for given url.
     *
     * @param url a url of resource file
     *
     * @return the source root or <code>null</code> when no source root was found.
     */
    public String getSourceRoot(String url) {
        Iterator it = GlobalPathRegistry.getDefault().getSourceRoots().iterator();
        while (it.hasNext()) {
            FileObject fileObject = (FileObject) it.next ();
            try {
                String rootURL = fileObject.getURL().toString();
                if (url.startsWith(rootURL)) {
                    File f = null;
                    if (fileObject.getFileSystem () instanceof JarFileSystem)
                        f = ((JarFileSystem) fileObject.getFileSystem ()).
                            getJarFile ();
                    else
                        f = FileUtil.toFile (fileObject);
                    if (f != null) {
                        return f.getAbsolutePath ();
                    }
                }
            } catch (FileStateInvalidException ex) {
                // Invalid source root - skip
            }
        }
        return null; // not found
    }
    
    /**
     * Returns allSourceRoots of original source roots.
     *
     * @return allSourceRoots of original source roots
     */
    public String[] getOriginalSourceRoots () {
        return getRoots (GlobalPathRegistry.getDefault ().getSourceRoots ().
            iterator ());
    }
    
    /**
     * Returns array of source roots.
     *
     * @return array of source roots
     */
    public String[] getSourceRoots () {
        return sourceRoots;
    }
    
    /**
     * Sets array of source roots.
     *
     * @param sourceRoots a new array of sourceRoots
     */
    public void setSourceRoots (String[] sourceRoots) {
        int i, k = sourceRoots.length;
        FileObject[] fos = new FileObject [k];
        for (i = 0; i < k; i++)
            fos [i] = getFileObject (sourceRoots [i]);
        Object old = smartSteppingSourcePath;
        smartSteppingSourcePath = ClassPathSupport.createClassPath (fos);
        this.sourceRoots = sourceRoots;
        pcs.firePropertyChange (
            PROP_SOURCE_ROOTS, old, smartSteppingSourcePath
        );
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (l);
    }
    
    
    // helper methods ..........................................................
    
    /**
     * Normalizes the given path by removing unnecessary "." and ".." sequences.
     * This normalization is needed because the compiler stores source paths like "foo/../inc.jsp" into .class files. 
     * Such paths are not supported by our ClassPath API.
     * TODO: compiler bug? report to JDK?
     * 
     * @param path path to normalize
     * @return normalized path without "." and ".." elements
     */ 
    private static String normalize(String path) {
      for (Matcher m = thisDirectoryPattern.matcher(path); m.find(); )
      {
        path = m.replaceAll("$1");
        m = thisDirectoryPattern.matcher(path);
      }
      for (Matcher m = parentDirectoryPattern.matcher(path); m.find(); )
      {
        if (!m.group(2).equals("..")) {
          path = path.substring(0, m.start()) + m.group(1) + path.substring(m.end());
          m = parentDirectoryPattern.matcher(path);        
        }
      }
      return path;
    }
    
    /**
     * Returns array of source roots for given ClassPath as Strings.
     */
    private static String[] getRoots (Iterator it) {
        Set roots = new TreeSet ();
        while (it.hasNext ()) {
            FileObject fileObject = (FileObject) it.next ();
            File f = null;
            try {
                if (fileObject.getFileSystem () instanceof JarFileSystem)
                    f = ((JarFileSystem) fileObject.getFileSystem ()).
                        getJarFile ();
                else
                    f = FileUtil.toFile (fileObject);
            } catch (FileStateInvalidException ex) {
            }
            if (f != null)
                roots.add (f.getAbsolutePath ());
        }
        String[] fs = new String [roots.size ()];
        return (String[]) roots.toArray (fs);
    }

    /**
     * Returns FileObject for given String.
     */
    private FileObject getFileObject (String file) {
        File f = new File (file);
        FileObject fo = FileUtil.toFileObject (f);
        if (FileUtil.isArchiveFile (fo))
            fo = FileUtil.getArchiveRoot (fo);
        return fo;
    }
}
