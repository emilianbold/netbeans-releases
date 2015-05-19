/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.spi.codemodel.completion;

import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMCursorKind;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public interface CMCompletionResultImplementation {
        /**
     * \brief The kind of entity that this completion refers to.
     *
     * The cursor kind will be a macro, keyword, or a declaration (one of the
     * *Decl cursor kinds), describing the entity that the completion is
     * referring to.
     *
     * \todo In the future, we would like to provide a full cursor, to allow the
     * client to extract additional information from declaration.
     */
    CMCursorKind getCursorKind();
    
    /**
     * \brief The code-completion string that describes how to insert this
     * code-completion result into the editing buffer. 
     */
    Iterable<CMCompletionChunkImplementation> getChunks();
    
    /**
     * \brief Determine the priority of this code completion.
     *
     * The priority of a code completion indicates how likely it is that this
     * particular completion is the completion that the user will select. The
     * priority is selected by various internal heuristics.
     *
     * @param completion_string The completion string to query.
     *
     * @return The priority of this completion string. Smaller values indicate
     * higher-priority (more likely) completions.
     */
    int getPriority();
    
    /**
     * \brief Retrieve the parent context of the given completion string.
     *
     * The parent context of a completion string is the semantic parent of the
     * declaration (if any) that the code completion represents. For example, a
     * code completion for an Objective-C/C++ method would have the method's class
     * or protocol as its context.
     *
     * @return The name of the completion parent, e.g., "NSObject" if the
     * completion string represents a method in the NSObject class.
     */
    CharSequence getCursorSemanticParentName();
    
    /**
     * \brief Retrieve the brief documentation comment attached to the
     * declaration that corresponds to the given completion string.
     */
    CharSequence getBriefComment();
    
    /**
     * \brief Determine the availability of the entity that this code-completion
     * string refers to.
     *
     * \param completion_string The completion string to query.
     *
     * \returns The availability of the completion string.
     */
    CMCursor.Availability getCompletionAvailability();    
    
    
    boolean equals(CMCompletionResultImplementation other);
    int hashCode();
}
