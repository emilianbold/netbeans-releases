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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.NbUtilities;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.util.CamelCaseUtil;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionRequest;
import org.netbeans.modules.groovy.editor.api.completion.util.RequestHelper;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;

/**
 * This should complete constructor generation.
 *  We are processing e.g. SB, StrB both results in:
 *
 *      StringBuilder() {
 *      }
 * 
 * @author Martin Janicek
 */
public class ConstructorGenerationCompletion extends BaseCompletion {

    @Override
    public boolean complete(List<CompletionProposal> proposals, CompletionRequest request, int anchor) {
        LOG.log(Level.FINEST, "-> constructor generation completion"); // NOI18N

        if (!isValidLocation(request)) {
            return false;
        }

        ClassNode requestedClass = RequestHelper.getSurroundingClassNode(request);
        if (requestedClass == null) {
            LOG.log(Level.FINEST, "No surrounding class found, bail out ..."); // NOI18N
            return false;
        }
        String className = NbUtilities.stripPackage(requestedClass.getName());

        boolean camelCaseMatch = CamelCaseUtil.compareCamelCase(className, request.prefix);
        if (camelCaseMatch) {
            LOG.log(Level.FINEST, "Prefix matches Class's CamelCase signature. Adding."); // NOI18N
            proposals.add(new CompletionItem.ConstructorItem(className, "", Collections.EMPTY_LIST, anchor, true));
        }

        return camelCaseMatch;
    }

    private boolean isValidLocation(CompletionRequest request) {
        if (!(request.location == CaretLocation.INSIDE_CLASS)) {
            LOG.log(Level.FINEST, "Not inside a class"); // NOI18N
            return false;
        }

        // We don't want to offer costructor generation when creating new instance
        if (request.ctx.before1 != null && request.ctx.before1.text().toString().equals("new") && request.prefix.length() > 0) {
            return false;
        }

        // We are after implements keyword
        if (request.ctx.beforeLiteral != null && request.ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_implements) {
            return false;
        }

        if (request.prefix == null || request.prefix.length() < 0) {
            return false;
        }

        // We can be either in 'String ^' or in 'NoSE^' situation
        if (request.ctx.before1 != null && request.ctx.before1.id() == GroovyTokenId.IDENTIFIER) {
            request.ctx.ts.movePrevious();
            Token<?> caretToken = request.ctx.ts.token();

            if (" ".equals(caretToken.text().toString())) {
                // 'String ^' situation --> No constructor generation proposals
                return false;
            }
        }

        // We are after class definition
        if (request.ctx.beforeLiteral != null && request.ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_class) {
            return false;
        }

        return true;
    }
}
