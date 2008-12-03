/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.ruby.RubyCompletionItem.ClassItem;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.lexer.Call;

final class RubyClassCompleter extends RubyBaseCompleter {

    private final boolean showSymbols;
    private final Call call;

    static boolean complete(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final int anchor,
            final boolean caseSensitive,
            final Call call,
            final boolean showSymbols) {
        RubyClassCompleter rsc = new RubyClassCompleter(proposals, request, anchor, caseSensitive, call, showSymbols);
        return rsc.complete();
    }

    private RubyClassCompleter(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final int anchor,
            final boolean caseSensitive,
            final Call call,
            final boolean showSymbols) {
        super(proposals, request, anchor, caseSensitive);
        this.call = call;
        this.showSymbols = showSymbols;
    }

    /** Determine if we're trying to complete the name for a "def" (in which case
     * we'd show the inherited methods) */
    private boolean complete() {
        String prefix = request.prefix;
        NameKind kind = request.kind;

        int classAnchor = anchor;
        int fqnIndex = prefix.lastIndexOf("::");

        if (fqnIndex != -1) {
            classAnchor += (fqnIndex + 2);
        }

        String fullPrefix = prefix;

        // foo.| or foo.b|  -> we're expecting a method call. For Foo:: we don't know.
        if (call.isMethodExpected()) {
            return false;
        }

        String type = call.getType();
        String lhs = call.getLhs();

        if ((lhs != null) && lhs.equals(type)) {
            fullPrefix = type + "::" + prefix;
        }

        AstPath path = request.path;
        String ctx = AstUtilities.getFqnName(path);

        Set<String> uniqueClasses = new HashSet<String>();
        Set<IndexedClass> classes = getIndex().getClasses(fullPrefix, kind, false, false, false, RubyIndex.ALL_SCOPE, uniqueClasses);

        // Also try looking or classes scoped by the current class
        if ((ctx != null) && (ctx.length() > 0)) {
            Set<IndexedClass> extraClasses = getIndex().getClasses(ctx + "::" + fullPrefix, kind, false, false, false, RubyIndex.ALL_SCOPE, uniqueClasses);
            classes.addAll(extraClasses);
        }

        // Prefix the current class if necessary
        for (IndexedClass cls : classes) {
            if (cls.isNoDoc()) {
                continue;
            }

            ClassItem item = new ClassItem(cls, classAnchor, request);
            item.setSmart(true);

            if (showSymbols) {
                item.setSymbol(true);
            }

            propose(item);
        }

        return false;
    }
}