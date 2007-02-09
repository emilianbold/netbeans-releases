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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.java.queries;

import java.net.URL;
import org.netbeans.api.java.queries.BinaryForSourceQuery;

/**
 * Information about where binaries (classfiles) corresponding to 
 * Java sources can be found, this is intended to be the inverse of 
 * the SourceForBinaryQueryImplementation.
 * @see BinaryForSourceQuery
 * @see SourceForBinaryQuery
 * @see SourceForBinaryQueryImplementation
 * @since org.netbeans.api.java/1 1.12
 * @author Tomas Zezula
 */
public interface BinaryForSourceQueryImplementation {
    
    /**
     * Returns the binary root(s) for a given source root.
     * <p>
     * The returned BinaryForSourceQuery.Result must be a singleton. It means that for
     * repeated calling of this method with the same recognized root the method has to
     * return the same instance of BinaryForSourceQuery.Result.<br>
     * The typical implemantation of the findBinaryRoots contains 3 steps:
     * <ol>
     * <li>Look into the cache if there is already a result for the root, if so return it</li>
     * <li>Check if the sourceRoot is recognized, if not return null</li>
     * <li>Create a new BinaryForSourceQuery.Result for the sourceRoot, put it into the cache
     * and return it.</li>
     * </ol>
     * </p>
     * <p>
     * Any absolute URL may be used but typically it will use the <code>file</code>
     * protocol for directory entries and <code>jar</code> protocol for JAR entries
     * (e.g. <samp>jar:file:/tmp/foo.jar!/</samp>).
     * </p>
     * @param sourceRoot the source path root
     * @return a result object encapsulating the answer or null if the sourceRoot is not recognized
     */
    public BinaryForSourceQuery.Result findBinaryRoots(URL sourceRoot);
    
}
