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
package org.netbeans.api.gsf;

import java.util.List;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.gsf.OccurrencesFinder;
import org.netbeans.api.gsf.SemanticAnalyzer;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementHandle;


/**
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public interface Parser {
    /** Parse the given set of files, and notify the parse listener for each transition 
     * (compilation results are attached to the events). The SourceFileReader can be used
     * to get the contents of the files to be parsed.
     */
    void parseFiles(@NonNull List<ParserFile> files,/* @NonNull ErrorHandler errorHandler, */
            @NonNull ParseListener listener, @NonNull SourceFileReader reader);
    
    /**
     * Return an object capable of providing source offsets for objects produced by the parser
     */
    PositionManager getPositionManager();

    /**
     *  HACK HACK HACK
     *  This should be done through a source task factory
     *
     *  TODO: Document specific conventions here: highlighting unused vars, parameters, etc. Point
     *  to ColoringAttributes etc.
     */
    SemanticAnalyzer getSemanticAnalysisTask();

    /**
     *  HACK HACK HACK
     *  This should be done through a source task factory
     *
     *  TODO: Document expected conventions here, like placing the caret over a return value
     *  should highlight all exit points, over a throws clause should highlight all points
     *  throwing or catching that particular exception, etc.
     */
    OccurrencesFinder getMarkOccurrencesTask(int caretPosition);

    /**
     * Create a "handle" for the given element. The handle should be able to be
     * mapped to an equivalent element in a separate parse tree through the
     * {@link #resolveHandle} method.
     */
    <T extends Element> ElementHandle<T> createHandle(CompilationInfo info, final T element);

    /**
     * For the given handle, resolve it to an equivalent element in the new
     * parse tree pointed to by the updated CompilationInfo.
     */
    <T extends Element> T resolveHandle(CompilationInfo info, ElementHandle<T> handle);
}
