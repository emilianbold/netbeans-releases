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
package org.netbeans.modules.ruby;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.ruby.RubyCompletionItem.MethodItem;
import org.netbeans.modules.ruby.elements.IndexedMethod;

/**
 *
 * @author Erno Mononen
 */
final class RubyDynamicFindersCompleter {

    private final CompletionRequest request;
    private final int anchor;
    private final List<? super CompletionProposal> proposals;

    private RubyDynamicFindersCompleter(List<? super CompletionProposal> proposals, CompletionRequest request, int anchor) {
        this.request = request;
        this.anchor = anchor;
        this.proposals = proposals;
    }

    static Set<IndexedMethod> proposeDynamicMethods(Set<IndexedMethod> methods, List<? super CompletionProposal> proposals, CompletionRequest request, int anchor) {
        RubyDynamicFindersCompleter completer = new RubyDynamicFindersCompleter(proposals, request, anchor);
        return completer.filterAndHandleDynamicMethods(methods);
    }

    /**
     * Filters out dynamic finder methods from the given <code>methods</code> and
     * creates completion proposals for them as needed.
     *
     * @param methods
     * @return
     */
    private Set<IndexedMethod> filterAndHandleDynamicMethods(Set<IndexedMethod> methods) {

        Set<IndexedMethod> result = new HashSet<IndexedMethod>(methods);
        Set<IndexedMethod> finders = new HashSet<IndexedMethod>();

        for (Iterator<IndexedMethod> it = methods.iterator(); it.hasNext();) {
            IndexedMethod indexedMethod = it.next();
            if (IndexedMethod.MethodType.DYNAMIC_FINDER == indexedMethod.getMethodType()) {
                finders.add(indexedMethod);
                result.remove(indexedMethod);
            }
        }

        proposeDynamicMethods(finders);
        return result;
    }

    private void proposeDynamicMethods(Set<IndexedMethod> finders) {
        // handles creating completion items for dynamic methods, including "find_by_something_and..."
        // kind of items. a bit hacky solution...
        Map<String, IndexedMethod> virtualMethods = new HashMap<String, IndexedMethod>();
        for (Iterator<IndexedMethod> it = finders.iterator(); it.hasNext();) {
            IndexedMethod method = it.next();
            String name = method.getName();
            int nextAnd = FindersHelper.nextAttributeLocation(name, request.prefix.length());
            if (nextAnd != -1) {
                // store as a key, format "find_by_name_and" (without the trailing underscore)
                String key = FindersHelper.subToNextAttribute(name, nextAnd);
                if (virtualMethods.get(key) == null) {
                    virtualMethods.put(key, method);
                    MethodItem methodItem = new RubyCompletionItem.VirtualFinderMethodItem(method, anchor, request, key);
                    methodItem.setSmart(method.isSmart());
                    proposals.add(methodItem);
                }
                it.remove();
            } else {
                MethodItem methodItem = new RubyCompletionItem.FinderMethodItem(method, anchor, request);
                methodItem.setSmart(method.isSmart());
                proposals.add(methodItem);
            }
        }

    }
}
