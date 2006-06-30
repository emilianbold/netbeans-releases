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
package org.netbeans.spi.java.queries;

import java.net.URL;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;

// XXX add a listener to changes in result

/**
 * Information about where Java sources corresponding to binaries
 * (classfiles) can be found.
 * <p>
 * A default implementation is registered by the
 * <code>org.netbeans.modules.java.project</code> module which looks up the
 * project corresponding to the file (if any; <code>jar</code>-protocol URLs
 * actually check the owner of the JAR file itself) and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Java Project module and uses this style.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Note that if you supply a <code>SourceForBinaryQueryImplementation</code>
 * corresponding to an entry in a {@link org.netbeans.spi.java.classpath.ClassPathProvider} for some source
 * files, there needs to be a {@link org.netbeans.spi.java.classpath.ClassPathProvider} for the sources
 * used as dependencies as well. Otherwise code completion will not work well;
 * the current parser database creation strategy uses the following search order
 * when deciding what to parse for a binary classpath element:
 * </p>
 * <ol>
 * <li>The sources returned by <code>SourceForBinaryQueryImplementation</code>,
 *     <em>if</em> these have at least a bootclasspath specified as well by some
 *     {@link org.netbeans.spi.java.classpath.ClassPathProvider}.</li>
 * <li>Compiled classes mixed into the "source" directory, if there are any.</li>
 * <li>Compiled classes in the binary classpath element.</li>
 * </ol>
 * </div>
 * @see org.netbeans.api.java.queries.SourceForBinaryQuery
 * @see org.netbeans.api.queries.FileOwnerQuery
 * @see org.netbeans.api.project.Project#getLookup
 * @since org.netbeans.api.java/1 1.4
 */
public interface SourceForBinaryQueryImplementation {

    /**
     * Returns the source root(s) for a given binary root.
     * <p>
     * The returned SourceForBinaryQuery.Result must be a singleton. It means that for
     * repeated calling of this method with the same recognized root the method has to
     * return the same instance of SourceForBinaryQuery.Result.<br>
     * The typical implemantation of the findSourceRoots contains 3 steps:
     * <ol>
     * <li>Look into the cache if there is already a result for the root, if so return it</li>
     * <li>Check if the binaryRoot is recognized, if not return null</li>
     * <li>Create a new SourceForBinaryQuery.Result for the binaryRoot, put it into the cache
     * and return it.</li>
     * </ol>
     * </p>
     * <p>
     * Any absolute URL may be used but typically it will use the <code>file</code>
     * protocol for directory entries and <code>jar</code> protocol for JAR entries
     * (e.g. <samp>jar:file:/tmp/foo.jar!/</samp>).
     * </p>
     * @param binaryRoot the class path root of Java class files
     * @return a result object encapsulating the answer or null if the binaryRoot is not recognized
     */
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot);
    
}
