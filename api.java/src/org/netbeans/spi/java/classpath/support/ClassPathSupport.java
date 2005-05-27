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
package org.netbeans.spi.java.classpath.support;

import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.modules.java.classpath.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Convenience factory for creating classpaths of common sorts.
 * @since org.netbeans.api.java/1 1.4
 */
public class ClassPathSupport {

    private ClassPathSupport () {
    }


    /** Creates leaf PathResourceImplementation.
     * The created PathResourceImplementation has exactly one immutable root.
     * @param url the root of the resource. The URL must refer to folder. In the case of archive file
     * the jar protocol URL must be used.
     * @return PathResourceImplementation
     */
    public static PathResourceImplementation createResource (URL url) {
        if (url == null) {
            throw new NullPointerException("Cannot pass null URL to ClassPathSupport.createResource"); // NOI18N
        }
        // FU.iAF is a bit slow, so don't call it except when assertions are on:
        boolean assertions = false;
        assert assertions = true;
        if (assertions && FileUtil.isArchiveFile(url)) {
            throw new IllegalArgumentException("File URL pointing to " + // NOI18N
                "JAR is not valid classpath entry. Use jar: URL. Was: "+url); // NOI18N
        }
        if (!url.toExternalForm().endsWith("/")) { // NOI18N
            throw new IllegalArgumentException("URL must be a folder URL (append '/' if necessary): " + url); // NOI18N
        }
        return new SimplePathResourceImplementation (url);
    }


    /**
     * Create ClassPathImplementation for the given list of
     * {@link PathResourceImplementation} entries.
     * @param entries list of {@link PathResourceImplementation} instances;
     *     cannot be null; can be empty
     * @return SPI classpath
     */
    public static ClassPathImplementation createClassPathImplementation(List/*<PathResourceImplementation>*/ entries) {
        if (entries == null) {
            throw new NullPointerException("Cannot pass null entries"); // NOI18N
        }
        return new SimpleClassPathImplementation(entries);
    }


    /**
     * Create ClassPath for the given list of
     * {@link PathResourceImplementation} entries.
     * @param entries list of {@link PathResourceImplementation} instances;
     *     cannot be null; can be empty
     * @return API classpath
     */
    public static ClassPath createClassPath(List/*<PathResourceImplementation>*/ entries) {
        if (entries == null) {
            throw new NullPointerException("Cannot pass null entries"); // NOI18N
        }
        return ClassPathFactory.createClassPath(createClassPathImplementation(entries));
    }


    /**
     * Create ClassPath for the given array of class path roots
     * @param roots array of fileobjects which must correspond to directory.
     * In the case of archive file, the FileObject representing the root of the
     * archive must be used.  Cannot be null; can be empty array; array can contain nulls.
     * @return API classpath
     */
    public static ClassPath createClassPath (FileObject[] roots) {
        assert roots != null;
        List l = new ArrayList ();
        for (int i =0; i < roots.length; i++) {
            if (roots[i] == null) {
                continue;
            }
            try {
                URL u = roots[i].getURL();            
                l.add(createResource(u));
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify (e);
            }
        }
        return createClassPath (l);
    }


    /**
     * Create ClassPath for the given array of class path roots
     * @param roots array of URLs which must correspond to directory.
     * In the case of archive file, the jar protocol URL must be used.
     *   Cannot be null; can be empty array; array can contain nulls.
     * @return API classpath
     */
    public static ClassPath createClassPath (URL[] roots) {
        assert roots != null;
        List l = new ArrayList ();
        for (int i=0; i< roots.length; i++) {
            if (roots[i] == null)
                continue;
            l.add (createResource(roots[i]));
        }
        return createClassPath(l);
    }


    /**
     * Creates read only proxy ClassPathImplementation for given delegates.
     * The order of resources is given by the order of the delegates
     * @param delegates ClassPathImplementations to delegate to.
     * @return SPI classpath
     */
    public static ClassPathImplementation createProxyClassPathImplementation (ClassPathImplementation[] delegates) {
        return new ProxyClassPathImplementation (delegates);
    }


    /**
     * Creates read only proxy ClassPath for given delegates.
     * The order of resources is given by the order of the delegates
     * @param delegates ClassPaths to delegate to.
     * @return API classpath
     */
    public static ClassPath createProxyClassPath (ClassPath[] delegates) {
        assert delegates != null;
        ClassPathImplementation[] impls = new ClassPathImplementation [delegates.length];
        for (int i = 0; i < delegates.length; i++) {
             impls[i] = ClassPathAccessor.DEFAULT.getClassPathImpl (delegates[i]);
        }
        return ClassPathFactory.createClassPath (createProxyClassPathImplementation(impls));
    }

}
