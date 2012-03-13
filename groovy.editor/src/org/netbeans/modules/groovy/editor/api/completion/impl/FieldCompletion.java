/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.api.completion.impl;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionRequest;
import org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.completion.CompleteElementHandler;

/**
 * Complete the fields for a class. There are two principal completions for fields:
 *
 * 1.) We are invoked right behind a dot. Then we have to retrieve the type in front of this dot.
 * 2.) We are located inside a type. Then we gotta get the fields for this class.
 *
 * @author Martin Janicek
 */
public class FieldCompletion extends BaseCompletion {

    @Override
    public boolean complete(List<CompletionProposal> proposals, CompletionRequest request, int anchor) {
        LOG.log(Level.FINEST, "-> completeFields"); // NOI18N

        if (request.location == CaretLocation.INSIDE_PARAMETERS && request.isBehindDot() == false) {
            LOG.log(Level.FINEST, "no fields completion inside of parameters-list"); // NOI18N
            return false;
        }

        if (request.dotContext != null && request.dotContext.isMethodsOnly()) {
            return false;
        }

        // We are after either implements or extends keyword
        if ((request.ctx.beforeLiteral != null && request.ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_implements) ||
            (request.ctx.beforeLiteral != null && request.ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_extends)) {
            return false;
        }
        
        if (request.ctx.beforeLiteral != null && request.ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_class) {
            return false;
        }
        
        if (request.isBehindDot()) {
            LOG.log(Level.FINEST, "We are invoked right behind a dot."); // NOI18N

            PackageCompletionRequest packageRequest = getPackageRequest(request);

            if (packageRequest.basePackage.length() > 0) {
                ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);

                if (isValidPackage(pathInfo, packageRequest.basePackage)) {
                    LOG.log(Level.FINEST, "The string before the dot seems to be a valid package"); // NOI18N
                    return false;
                }
            }
        }
        ClassNode declaringClass = request.declaringClass;

        if (declaringClass == null) {
            LOG.log(Level.FINEST, "No declaring class found"); // NOI18N
            return false;
        }

        // If we are dealing with GStrings, the prefix is prefixed ;-)
        // ... with the dollar sign $ See # 143295
        int anchorShift = 0;
        String fieldName = request.prefix;

        if (request.prefix.startsWith("$")) {
            fieldName = request.prefix.substring(1);
            anchorShift = 1;
        }

        Map<FieldSignature, ? extends CompletionItem> result = CompleteElementHandler
                .forCompilationInfo(request.info)
                    .getFields(ContextHelper.getSurroundingClassNode(request), declaringClass, fieldName, anchor + anchorShift);
        
        FieldSignature prefixFieldSignature = new FieldSignature(request.prefix);
        if (result.containsKey(prefixFieldSignature)) {
            result.remove(prefixFieldSignature);
        }
        proposals.addAll(result.values());

        return true;
    }
}
