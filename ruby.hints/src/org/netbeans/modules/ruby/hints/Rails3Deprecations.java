/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.Deprecations.Deprecation;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

import static org.netbeans.modules.ruby.hints.RailsDeprecations.*;

/**
 * Hints for methods/fields deprecated in Rails 3.
 *
 * @author Erno Mononen
 */
public class Rails3Deprecations extends RubyAstRule {

    private static final Set<Deprecation> DEPRECATED_CONSTANTS = new HashSet<Deprecation>();
    private static final Set<Deprecation> DEPRECATED_CONTROLLER_METHODS = new HashSet<Deprecation>();
    private static final Set<Deprecation> DEPRECATED_AR_METHODS = new HashSet<Deprecation>();

    static {
        DEPRECATED_CONSTANTS.add(new Deprecation("RAILS_ROOT", "Rails.root", null, null)); // NOI18N
        DEPRECATED_CONSTANTS.add(new Deprecation("RAILS_ENV", "Rails.env", null, null)); // NOI18N
        DEPRECATED_CONSTANTS.add(new Deprecation("RAILS_DEFAULT_LOGGER", "Rails.logger", null, null)); // NOI18N

        DEPRECATED_CONTROLLER_METHODS.add(new Deprecation("filter_parameter_logging", "config.filter_parameters", null, null)); // NOI18N

        DEPRECATED_AR_METHODS.add(new Deprecation("named_scope", "scope", null, null)); // NOI18N
        DEPRECATED_AR_METHODS.add(new Deprecation("validates_presence_of", "validates...:presence => true", null, null, false)); // NOI18N
    }

    private RubyRuleContext context;

    public Rails3Deprecations() {
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        ParserResult info = context.parserResult;
        // Only perform these checks in Rails 3 projects
        return RubyHints.isInRails3Project(RubyUtils.getFileObject(info));
    }

    @Override
    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.ROOTNODE);
    }

    @Override
    public void run(RubyRuleContext context, List<Hint> result) {
        this.context = context;
        Node root = context.node;
        ParserResult info = context.parserResult;
        AstPath path = context.path;

        if (root == null) {
            return;
        }

        // This rule should only be called on the root node itself
        assert path.leaf() == root;

        scan(info, root, result);
    }

    public void cancel() {
        // Does nothing
    }

    @Override
    public String getId() {
        return "Rails3_Deprecations"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RailsDeprecations.class, "Rails3Deprecation");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(RailsDeprecations.class, "Rails3DeprecationDesc");
    }

    private void scan(ParserResult info, Node node, List<Hint> result) {
        // Look for use of deprecated constants
        if (node.getNodeType() == NodeType.CONSTNODE) {
            String name = AstUtilities.getName(node);
            // Skip matches in _test files, since the standard code generator still
            // spits out code which violates the deprecations
            // (such as    @request    = ActionController::TestRequest.new )
            Deprecation deprecation = findMatching(name, DEPRECATED_CONSTANTS);
            if (deprecation != null) {
                String message = NbBundle.getMessage(Rails3Deprecations.class, "DeprecatedRailsConstant", deprecation.oldName, deprecation.newName);
                addHintAndFix(node, result, message, deprecation);
            }
        } else if (AstUtilities.isCall(node)) {
            Deprecation deprecation = null;
            String name = AstUtilities.getName(node);
            if (inActionController(info, node, this)) {
                deprecation = findMatching(name, DEPRECATED_CONTROLLER_METHODS);
            } else if (inActiveRecordModel(info, node, this)) {
                deprecation = findMatching(name, DEPRECATED_AR_METHODS);
            }
            if (deprecation != null) {
                String message = NbBundle.getMessage(Rails3Deprecations.class, "DeprecatedRailsMethodUse", deprecation.oldName, deprecation.newName);
                addHintAndFix(node, result, message, deprecation);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            scan(info, child, result);
        }
    }

    private Deprecation findMatching(String name, Collection<Deprecation> deprecations) {
        for (Deprecation deprecation : deprecations) {
            if (deprecation.oldName.equals(name)) {
                return deprecation;
            }
        }
        return null;

    }

    private void addHintAndFix(Node node, List<Hint> result, String displayName, Deprecation deprecation) {
        OffsetRange range = AstUtilities.getNameRange(node);
        ParserResult info = context.parserResult;
        range = LexUtilities.getLexerOffsets(info, range);
        if (range != OffsetRange.NONE) {
            List<HintFix> fixes;
            if (deprecation.enableFix) {
                HintFix fix = new Deprecations.DeprecationCallFix(context, node, deprecation, false);
                fixes = Collections.singletonList(fix);
            } else {
                fixes = Collections.emptyList();
            }
            Hint desc = new Hint(this, displayName, RubyUtils.getFileObject(info), range, fixes, 100);
            result.add(desc);
        }
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }
}
