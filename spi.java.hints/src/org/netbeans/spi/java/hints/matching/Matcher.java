/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.spi.java.hints.matching;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.java.hints.matching.CopyFinder.Options;
import org.netbeans.spi.java.hints.matching.CopyFinder.State;
import org.netbeans.spi.java.hints.matching.CopyFinder.VariableAssignments;

/**Searches for occurrences of a {@link Pattern}.
 *
 * @author lahvac
 */
public class Matcher {

    /**Create the Matcher for the given {@link CompilationInfo}. See the set methods for default
     * settings of the Matcher.
     *
     * @param info for which the Matcher should be created
     * @return newly created Matcher
     */
    public static Matcher create(CompilationInfo info) {
        return new Matcher(info);
    }

    private final CompilationInfo info;
    private       AtomicBoolean cancel;
    private       TreePath root;
    private Set<Options> options = EnumSet.noneOf(Options.class);
    private Map<String, TreePath> variables;
    private Map<String, Collection<? extends TreePath>> multiVariables;
    private Map<String, String> variables2Names;

    private Matcher(CompilationInfo info) {
        this.info = info;
        this.root = new TreePath(info.getCompilationUnit());
        this.options.add(Options.ALLOW_GO_DEEPER);
    }

    /**Search for the occurrences only under the given tree. By default, the full {@link CompilationUnitTree}
     * of the given {@link CompilationInfo} is used.
     *
     * @param root where to start the search
     * @return the matcher itself
     */
    public Matcher setSearchRoot(TreePath root) {
        this.root = root;
        return this;
    }

    /**The matcher should perform the tree-top search: the occurrence of the pattern must correspond directly
     * to the search root. By default, tree-top search is not performed, and all occurrences of the
     * pattern under the specified root will be reported.
     *
     * @return the matcher itself
     */
    public Matcher setTreeTopSearch() {
        this.options.remove(Options.ALLOW_GO_DEEPER);
        return this;
    }

    /**The matching should ignore type attributes on the trees. By default, types
     * specified in the pattern must match the actual types.
     *
     * @return the matcher itself
     */
    public Matcher setUntypedMatching() {
        this.options.add(Options.NO_ELEMENT_VERIFY);
        return this;
    }

    /**Preset values of free variables in the pattern (see {@link Pattern#createPatternWithFreeVariables(com.sun.source.util.TreePath, java.util.Map)}).
     * A tree node will be marked as an occurrence of the pattern only if the subtree
     * corresponding to a specified free variable will match the given value.
     *
     * By default, no values are preset for any variables.
     * 
     * @param variables
     * @param multiVariables
     * @param variables2Names
     * @return the matcher itself
     */
    public Matcher setPresetVariable(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names) {
        this.variables = variables;
        this.multiVariables = multiVariables;
        this.variables2Names = variables2Names;
        return this;
    }

    /**Make the matching cancelable by setting {@code true} to the given {@link AtomicBoolean}.
     *
     * @param cancel {@link AtomicBoolean} which should be set to {@code true} to stop the matching as soon as possible
     * @return the matcher itself
     */
    public Matcher setCancel(AtomicBoolean cancel) {
        this.cancel = cancel;
        return this;
    }

    /**Search for occurrences of the given pattern in the given subject tree.
     * 
     * TODO: "multipattern" matching
     * @param pattern for which the search should be performed
     * @return descriptions of found pattern occurrences
     */
    public @NonNull Collection<? extends OccurrenceDescription> match(Pattern pattern) {
        Set<Options> opts = EnumSet.noneOf(Options.class);

        opts.addAll(options);

        if (pattern.variable2Type != null) {
            opts.add(Options.ALLOW_VARIABLES_IN_PATTERN);
        }

        if (pattern.allowRemapToTrees) {
            opts.add(Options.ALLOW_REMAP_VARIABLE_TO_EXPRESSION);
        }

        List<OccurrenceDescription> result = new ArrayList<OccurrenceDescription>();
        State preinitializeState;

        if (variables != null) {
            preinitializeState = State.from(variables, multiVariables, variables2Names);
        } else {
            preinitializeState = null;
        }

        for (Entry<TreePath, VariableAssignments> e : CopyFinder.internalComputeDuplicates(info, pattern.pattern, root, preinitializeState, pattern.remappable, cancel, pattern.variable2Type, opts.toArray(new Options[opts.size()])).entrySet()) {
            result.add(new OccurrenceDescription(e.getKey(), e.getValue().variables, e.getValue().multiVariables, e.getValue().variables2Names, e.getValue().variablesRemapToElement, e.getValue().variablesRemapToTrees));
        }

        return Collections.unmodifiableCollection(result);
    }

    /**A found occurrence of a pattern.
     */
    public static final class OccurrenceDescription {
        private final TreePath occurrenceRoot;
        private final Map<String, TreePath> variables;
        private final Map<String, Collection<? extends TreePath>> multiVariables;
        private final Map<String, String> variables2Names;
        private final Map<Element, Element> variablesRemapToElement;
        private final Map<Element, TreePath> variablesRemapToTrees;

        private OccurrenceDescription(TreePath occurrenceRoot, Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
            this.occurrenceRoot = occurrenceRoot;
            this.variables = variables;
            this.multiVariables = multiVariables;
            this.variables2Names = variables2Names;
            this.variablesRemapToElement = variablesRemapToElement;
            this.variablesRemapToTrees = variablesRemapToTrees;
        }

        /**The tree node that represents the occurrence. For multi-part patterns {@link Pattern#createSimplePattern(java.lang.Iterable) },
         * returns the {@link TreePath} corresponding to the first part (TODO: verify!).
         * 
         * @return root tree of the occurrence
         */
        public TreePath getOccurrenceRoot() {
            return occurrenceRoot;
        }

        /**For patterns with free variables, contains mapping of single-variable
         * names to the tree node that was bound to the variable. That means,
         * the tree node that corresponds to the first occurrence of the variable
         * in the pattern.
         *
         * @return mapping of free variable names to the tree nodes that were bound to them
         */
        public Map<String, TreePath> getVariables() {
            return variables;
        }

        /**For patterns with free variables, contains mapping of multi-variable
         * names to the tree nodes that were bound to the variable. That means,
         * the tree nodes that corresponds to the first occurrence of the variable
         * in the pattern.
         *
         * @return mapping of free variable names to the tree nodes that were bound to them
         */
        public Map<String, Collection<? extends TreePath>> getMultiVariables() {
            return multiVariables;
        }

        /**For patterns with free variables, contains mapping of variable
         * names to a name that was bound to the variable.
         *
         * @return mapping of free variable names to names that were bound to them
         */
        public Map<String, String> getVariables2Names() {
            return variables2Names;
        }

        /**For patterns which allow variable remap, contains mapping of the element
         * in the pattern to the corresponding element in this occurrence.
         *
         * @return mapping of elements in the pattern to the elements in this occurrence
         */
        public Map<Element, Element> getVariablesRemapToElement() {
            return variablesRemapToElement;
        }

        /**For patterns which allow variable remap to trees, contains mapping of the element
         * in the pattern to the corresponding tree node in this occurrence.
         *
         * @return mapping of elements in the pattern to the elements in this occurrence
         */
        public Map<Element, TreePath> getVariablesRemapToTrees() {
            return variablesRemapToTrees;
        }

    }

}
