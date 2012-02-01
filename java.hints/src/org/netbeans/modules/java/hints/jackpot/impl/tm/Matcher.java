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
package org.netbeans.modules.java.hints.jackpot.impl.tm;

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
import org.netbeans.modules.java.hints.jackpot.impl.tm.CopyFinder.Options;
import org.netbeans.modules.java.hints.jackpot.impl.tm.CopyFinder.State;
import org.netbeans.modules.java.hints.jackpot.impl.tm.CopyFinder.VariableAssignments;

/**
 *
 * @author lahvac
 */
public class Matcher {

    public static Matcher create(CompilationInfo info, /*XXX*/AtomicBoolean cancel) {
        return new Matcher(info, cancel);
    }

    private final CompilationInfo info;
    private final AtomicBoolean cancel;
    private       TreePath root;
    private Set<Options> options = EnumSet.noneOf(Options.class);
    private Map<String, TreePath> variables;
    private Map<String, Collection<? extends TreePath>> multiVariables;
    private Map<String, String> variables2Names;

    private Matcher(CompilationInfo info, AtomicBoolean cancel) {
        this.info = info;
        this.cancel = cancel;
        this.root = new TreePath(info.getCompilationUnit())
                ;
        this.options.add(Options.ALLOW_GO_DEEPER);
    }

    public Matcher setSearchRoot(TreePath root) {
        this.root = root;
        return this;
    }

    public Matcher setTreeTopSearch() {
        this.options.remove(Options.ALLOW_GO_DEEPER);
        return this;
    }

    public Matcher setUntypedMatching() {
        this.options.add(Options.NO_ELEMMENT_VERIFY);
        return this;
    }

    public Matcher setPresetVariable(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names) {
        this.variables = variables;
        this.multiVariables = multiVariables;
        this.variables2Names = variables2Names;
        return this;
    }

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

    public static final class OccurrenceDescription {
        private final TreePath occurrenceRoot;
        private final Map<String, TreePath> variables;
        private final Map<String, Collection<? extends TreePath>> multiVariables;
        private final Map<String, String> variables2Names;
        private final Map<Element, Element> variablesRemapToElement;
        private final Map<Element, TreePath> variablesRemapToTrees;

        public OccurrenceDescription(TreePath occurrenceRoot, Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
            this.occurrenceRoot = occurrenceRoot;
            this.variables = variables;
            this.multiVariables = multiVariables;
            this.variables2Names = variables2Names;
            this.variablesRemapToElement = variablesRemapToElement;
            this.variablesRemapToTrees = variablesRemapToTrees;
        }

        public Map<String, Collection<? extends TreePath>> getMultiVariables() {
            return multiVariables;
        }

        public TreePath getOccurrenceRoot() {
            return occurrenceRoot;
        }

        public Map<String, TreePath> getVariables() {
            return variables;
        }

        public Map<String, String> getVariables2Names() {
            return variables2Names;
        }

        public Map<Element, Element> getVariablesRemapToElement() {
            return variablesRemapToElement;
        }

        public Map<Element, TreePath> getVariablesRemapToTrees() {
            return variablesRemapToTrees;
        }

    }


}
