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

package org.netbeans.spi.java.classpath;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * Provider interface for classpaths.
 * <p>
 * The <code>org.netbeans.modules.java.project</code> module registers an
 * implementation of this interface to global lookup which looks for the
 * project which owns a file (if any) and checks its lookup for this interface,
 * and if it finds an instance, delegates to it. Therefore it is not normally
 * necessary for a project type provider to register its own instance just to
 * define the classpaths for files it owns, assuming it depends on the Java
 * Project module.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Note that to make editor code completion working for a source file the
 *  following classpath must be available for the source file:
 * </p>
 * <ol>
 * <li>the {@link ClassPath#BOOT} type of classpath
 *     is required otherwise the source file is not parseable and 
 *     code completion will be disabled</li>
 * <li>the {@link ClassPath#SOURCE} type of classpath
 *     is required otherwise the code completion will be disabled.
 *     Providing this classpath will enable code completion, but only elements
 *     defined on this classpath will be offered. See below point for how to
 *     make it fully working.</li>
 * <li>the {@link ClassPath#COMPILE} type of classpath
 *     is recommended to be provide to make code completion fully 
 *     working and suggesting also all the elements against which the source 
 *     are developed.</li>
 * </ol>
 * </div>
 * @see ClassPath#getClassPath
 * @author Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public interface ClassPathProvider {
    
    /**
     * Find some kind of a classpath for a given file.
     * @param file a file somewhere
     * @param type a classpath type such as {@link ClassPath#COMPILE}
     * @return an appropriate classpath, or null for no answer
     * @see ClassPathFactory
     * @see org.netbeans.spi.java.classpath.support.ClassPathSupport
     */
    ClassPath findClassPath(FileObject file, String type);
    
}
