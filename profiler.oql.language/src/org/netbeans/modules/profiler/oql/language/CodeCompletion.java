/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.oql.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.languages.CompletionItem;
import org.netbeans.api.languages.Context;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.profiler.heapwalk.oql.OQLEngine;
import org.netbeans.modules.profiler.heapwalk.oql.OQLException;
import org.netbeans.modules.profiler.heapwalk.oql.ObjectVisitor;

/**
 *
 * @author Jaroslav Bachorik
 */
public class CodeCompletion {

    public static final List<CompletionItem> getClasses(Context context) {
        AbstractDocument document = (AbstractDocument) context.getDocument();
        OQLEngine engine = (OQLEngine) document.getProperty(OQLEngine.class);
        if (engine == null) {
            return Collections.EMPTY_LIST;
        }

        final List<CompletionItem> completionItems = new ArrayList<CompletionItem>();
        document.readLock();
        try {
            TokenSequence ts = getTokenSequence(context);
            String tokenText = previousToken(ts).text().toString();
            engine.executeQuery("select map(filter(heap.classes(), \"/" + tokenText + "/(it.name)\"), \"it.name\")", new ObjectVisitor() { // NOI18N

                public boolean visit(Object o) {
                    completionItems.add(new CompletionItem(o.toString(), null, "Heap", CompletionItem.Type.CLASS, 1));
                    return false;
                }
            });
        } catch (OQLException oQLException) {
        } finally {
            document.readUnlock();
        }
        return completionItems;
    }

    private static TokenSequence getTokenSequence(Context context) {
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(context.getDocument());
        TokenSequence ts = tokenHierarchy.tokenSequence();
        while (true) {
            ts.move(context.getOffset());
            if (!ts.moveNext()) {
                return ts;
            }
            TokenSequence ts2 = ts.embedded();
            if (ts2 == null) {
                return ts;
            }
            ts = ts2;
        }
    }

    private static Token previousToken(TokenSequence ts) {
        do {
            if (!ts.movePrevious()) {
                return ts.token();
            }
        } while (ts.token().id().name().endsWith("whitespace") ||
                ts.token().id().name().endsWith("comment"));
        return ts.token();
    }
}
