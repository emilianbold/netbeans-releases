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
package org.netbeans.modules.csl.api;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.annotations.CheckForNull;
import org.netbeans.modules.csl.api.annotations.NonNull;

/** 
 * The CompletionResult object returns a list of proposals along with some
 * information about the result. You should subclass this class
 * yourself (or use the default implementation, 
 * {@link org.netbeans.modules.gsf.spi.DefaultCompletionResult}
 * and return an instance of it from the {@link CodeCompletionHandler#complete} method.
 * object is provided by the language implementation
 * 
 * @author Tor Norbye
 */
public abstract class CodeCompletionResult {
    /**
     * Special code completion result which means that there are no proposals
     */
    public static final CodeCompletionResult NONE = new CodeCompletionResult() {

        @Override
        public List<CompletionProposal> getItems() {
            return Collections.emptyList();
        }

        @Override
        public boolean isTruncated() {
            return false;
        }

        @Override
        public boolean isFilterable() {
            return false;
        }

        @Override
        public Set<String> embeddedTypes() {
            return Collections.emptySet();
        }
    };

    /**
     * Return the list of completion proposals that should be presented to
     * the user.
     * 
     * @return A list of items to show the user
     */
    @NonNull
    public abstract List<CompletionProposal> getItems();

    /**
     * This method is called when the user has chosen to insert a code completion item.
     * The method is called BEFORE the actual item is inserted into the document.
     * 
     * @param item The proposal that is about to be inserted into the document.
     */
    public void beforeInsert(@NonNull CompletionProposal item) {
    }

    /**
     * <p>Insert the given item into the document. (The document and other context
     * was passed in as part of the {@link CodeCompletionContext} passed to the completion
     * handler.)
     * </p>
     * <p><b>NOTE</b>: Most implementation should just return false from this
     * method. False means that you have not handled the insertion, and the framework
     * will do it for you. Return true if you want to have custom handling here.
     * In that case, the infrastructure will not do anything else.
     * </p>
     * 
     * @param item The item to be inserted into the document.
     * @return true if you want to handle the insertion yourself, or false to get the
     *   infrastructure to do it on your behalf.
     */
    public boolean insert(@NonNull CompletionProposal item) {
        return false;
    }

    /**
     * This method is called when the user has chosen to insert a code completion item.
     * The method is called AFTER the actual item is inserted into the document.
     * 
     * @param item The proposal that has been inserted into the document.
     */
    public void afterInsert(@NonNull CompletionProposal item) {
    }

    /**
     * Return true if you have truncated the items that are returned. For example,
     * it is probably pointless to return a list of 5,000 methods to the user.
     * This just means a slow response time, so implementations may choose to abort
     * when the set is probably too large to be used without further filtering.
     * In this case, you should return "true" from this method, which will cause
     * the infrastructure to (a) insert an item at the bottom of the list stating
     * that the list has been truncated, and (b) it will NOT do its normal optimization
     * or simply filtering the first result set as the user types additional characters;
     * it will repeat the full search whenever the list has been truncated.
     * @return true if and only if the {@link #getItems()} method returned a truncated
     * result.
     */
    public abstract boolean isTruncated();

    /**
     * Return true iff the code completion result can be "filtered" (narrowed down)
     * by the infrastructure without repeating the search. In other words,
     * if the prefix was "f", and your search returned {"foo", "fuu"}, then
     * if your result is filterable (by default true), and the user types "u"
     * then the infrastructure will not repeat the search it will just filter
     * your original result down from {"foo", "fuu"} to just {"fuu"}.
     *
     * @return true iff the result can be filtered (by default, true).
     */
    public abstract boolean isFilterable();
    
    /**
     * <p>Return extra mimetypes that might be embedded here and should be checked
     * for code completion.  Normally null or an empty set.
     * </p>
     * <p>
     * In embedded files, code completion normally gets invoked on the 
     * CodeCompletionHandler for the innermost (most specific) mime language at
     * the caret offset. In some cases this isn't adequate; for example, inside
     * an empty "onclick" handler in HTML, you expect JavaScript code completion,
     * but because the attribute is empty, there aren't actually any tokens
     * for JavaScript there, so no JavaScript embedding - and you end up with
     * just HTML completion.
     * </p>
     * <p>
     * This method lets an outer language coordinate other languages that should
     * be included for code completion. For example, in the above example, the
     * HTML code completion can look at the tokens, and decide that inside
     * style="" it will include CSS completion, and inside onclick="" it will include
     * JavaScript completion.
     * </p>
     * <p>
     * <b>Note: It is illegal for these result sets to contain their own mimetypes,
     * or a set of circular references.</b>
     * </p>
     * 
     * @return A set of other mimetypes to also check at this location, or null.
     */
    @CheckForNull
    public abstract Set<String> embeddedTypes();
}
