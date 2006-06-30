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

import org.openide.filesystems.FileObject;

/**
 * Permits providers to return specification source level of Java source file.
 * <p>
 * A default implementation is registered by the
 * <code>org.netbeans.modules.java.project</code> module which looks up the
 * project corresponding to the file (if any) and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Java Project module and uses this style.
 * </p>
 * @see org.netbeans.api.java.queries.SourceLevelQuery
 * @see org.netbeans.api.queries.FileOwnerQuery
 * @see org.netbeans.api.project.Project#getLookup
 * @see org.netbeans.api.java.classpath.ClassPath#BOOT
 * @author David Konecny
 * @since org.netbeans.api.java/1 1.5
 */
public interface SourceLevelQueryImplementation {

    /**
     * Returns source level of the given Java file. For acceptable return values
     * see the documentation of <code>-source</code> command line switch of 
     * <code>javac</code> compiler .
     * @param javaFile Java source file in question
     * @return source level of the Java file, e.g. "1.3", "1.4" or "1.5", or
     *    null if it is not known
     */
    public String getSourceLevel(FileObject javaFile);

}
