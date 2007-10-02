/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
