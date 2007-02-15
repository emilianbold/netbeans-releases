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

package org.netbeans.modules.refactoring.spi;

import java.util.Collection;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;

/**
 * Interface for factory classes which allows to create Problem, which ProblemDetails
 * can handle read only files. Typically VCS can turn read only files into read write.
 * Implementation of ReadOnlyFilesHandler is required to be registered in Lookup.
 * More then one instance is not allowed to be registered.
 * @author Jan Becicka
 * @since 1.5.0
 */
public interface ReadOnlyFilesHandler {
    
    /**
     * Create a Problem, which ProblemDetails 
     * can handle read only files. Typically VCS can turn read only files into
     * read write.
     * Typical implementation will be following:
     * <pre>
     * new Problem(false,
     *    "Some files needs to be checked out for editing",
     *     ProblemDetailsFactory.createProblemDetails(new VCSDetailsImpl(files))
     * );
     * </pre>
     * @param files Collection of FileObjects
     * @param session current refactoring session
     * @return Problem with ProblemDetails, which can handle read only files.
     * @see ProblemDetailsImplementation
     */
    public Problem createProblem(RefactoringSession session, Collection files); 
}
