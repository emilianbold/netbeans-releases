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
package org.netbeans.modules.ruby;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.StrNode;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.openide.filesystems.FileObject;

import static org.netbeans.modules.ruby.RubyIndexer.*;

/**
 * A helper class for doing Rails specific indexing.
 *
 * <i>Refactored out from RubyIndexer</i>.
 */
final class RailsIndexer {

    private final ContextKnowledge knowledge;
    private final RubyIndexer.TreeAnalyzer analyzer;
    private static final String INCLUDE = "include";
    private static final String REQUIRE = "require";
    private static final String EXTEND = "extend";

    RailsIndexer(ContextKnowledge knowledge, TreeAnalyzer analyzer) {
        this.knowledge = knowledge;
        this.analyzer = analyzer;
    }

    private static Map<String, Set<String>> createResultMap() {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        result.put(INCLUDE, new HashSet<String>());
        result.put(REQUIRE, new HashSet<String>());
        result.put(EXTEND, new HashSet<String>());
        return result;
    }

    /**
     * Performs rails specific indexing.
     *
     * @return true if normal indexing should also be performed; false otherwise.
     */
    boolean index() {
        String fileName = analyzer.getFile().getNameExt();
        String path = analyzer.getFile().getPath();
        // Rails special case
        // in case of 2.3.2 fall through to do normal indexing as well, these special cases
        // are needed for rails < 2.3.2, normal indexing handles 2.3.2 classes.
        // if rails is in vendor/rails, we can't tell the version from
        // the path, so playing safe and falling through to do also normal
        // indexing in that case
        boolean fallThrough = RubyUtils.isRails23OrHigher(analyzer.getFile().getPath())
                || analyzer.getFile().getPath().contains("vendor/rails"); //NOI18N

        if ("action_controller.rb".equals(fileName)) { // NOI18N
            // Locate "ActionController::Base.class_eval do"
            // and take those include statements and stick them into ActionController::Base
            handleRailsBase("ActionController"); // NOI18N
            if (!fallThrough) {
                return false;
            }
        } else if ("active_record.rb".equals(fileName) || path.endsWith("active_record/base.rb")) { // NOI18N
            handleRailsBase("ActiveRecord"); // NOI18N
//                    handleRailsClass("ActiveRecord", "ActiveRecord" + "::Migration", "Migration", "migration");
            // HACK
            analyzer.getMigrationIndexer().handleMigrations();
            if (!fallThrough) {
                return false;
            }
        } else if ("action_mailer.rb".equals(fileName)) { // NOI18N
            handleRailsBase("ActionMailer"); // NOI18N
            if (!fallThrough) {
                return false;
            }
        } else if ("action_view.rb".equals(fileName)) { // NOI18N
            handleRailsBase("ActionView"); // NOI18N

            // HACK
            handleActionViewHelpers();
            if (!fallThrough) {
                return false;
            }

            //} else if ("action_web_service.rb".equals(fileName)) { // NOI18N
            // Uh oh - we have two different kinds of class eval here - one for ActionWebService, one for ActionController!
            // Gotta make this shiznit smarter!
            //handleRailsBase("ActionWebService::Base", "Base", "ActionWebService"); // NOI18N
            //handleRailsBase("ActionController:Base", "Base", "ActionController"); // NOI18N
        } else if (fileName.equals("assertions.rb") && analyzer.getUrl().endsWith("lib/action_controller/assertions.rb")) { // NOI18N
            handleRailsClass("Test::Unit", "Test::Unit::TestCase", "TestCase", "TestCase"); // NOI18N
            if (!fallThrough) {
                return false;
            }
        } else if (fileName.equals("schema_definitions.rb")) {
            handleSchemaDefinitions();
            // Fall through - also do normal indexing on the file
        }

        return true;
    }

    private void handleRailsBase(String classIn) {
        handleRailsClass(classIn, classIn + "::Base", "Base", "base"); // NOI18N
    }

    /** There's some pretty complicated dynamic behavior in Rails in how
     * the ActionController::Base class is decorated with module mixins;
     * my code cannot handle this directly, but it's a really important
     * special case to handle such that code completion works in the very
     * key controller classes edited by users. (This logic is replicated
     * in several other classes too - ActiveRecord etc.)
     */
    private void handleRailsClass(String classIn, String classFqn, String clz, String clzNoCase) {
        Node root = knowledge.getRoot();

        IndexDocument document = analyzer.getSupport().createDocument(analyzer.getIndexable());
        analyzer.getDocuments().add(document);

        Map<String, Set<String>> result = createResultMap();
        scan(root, result);

        addResults(document, result);
        // TODO:
        //addIncluded(indexed);
        int flags = 0;
        document.addPair(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags), false, true);

        document.addPair(FIELD_FQN_NAME, classFqn, true, true);
        document.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, clzNoCase, true, true);
        document.addPair(FIELD_CLASS_NAME, clz, true, true);
        document.addPair(FIELD_IN, classIn, false, true);
    }

    /**
     * Action view has some special loading behavior of helpers - see actionview's
     * "load_helpers" method.
     * @todo Make sure that the Partials loading is working too
     */
    private void handleActionViewHelpers() {
        if (analyzer.getFile() == null || analyzer.getFile().getParent() == null) {
            return;
        }
        assert analyzer.getFile().getName().equals("action_view");

        FileObject helpers = analyzer.getFile().getParent().getFileObject("action_view/helpers"); // NOI18N
        if (helpers == null) {
            return;
        }

        StringBuilder include = new StringBuilder();

        for (FileObject helper : helpers.getChildren()) {
            String name = helper.getName();
            if (name.endsWith("_helper")) { // NOI18N
                String className = RubyUtils.underlinedNameToCamel(name);
                String fqn = "ActionView::Helpers::" + className; // NOI18N
                if (include.length() > 0) {
                    include.append(",");
                }
                include.append(fqn);
            }
        }

        if (include.length() > 0) {
            int flags = 0;
            analyzer.addClassIncludes("Base", "ActionView::Base", "ActionView", flags,
                    include.toString());
        }
    }

    private boolean scan(Node node, Map<String, Set<String>> result) {
        boolean found = false;

        if (node instanceof FCallNode) {
            String name = ((INameNode) node).getName();

            if (name.equals(REQUIRE)) { // XXX Load too?

                Node argsNode = ((FCallNode) node).getArgsNode();

                if (argsNode instanceof ListNode) {
                    ListNode args = (ListNode) argsNode;

                    if (args.size() > 0) {
                        Node n = args.get(0);

                        // For dynamically computed strings, we have n instanceof DStrNode
                        // but I can't handle these anyway

                        if (n instanceof StrNode) {
                            String require = ((StrNode) n).getValue();

                            if (require != null && require.length() > 0) {
                                result.get(REQUIRE).add(require);
                            }
                        }
                    }
                }
            } else if (name.equals(INCLUDE) || name.equals(EXTEND)) {
                final String key = name.equals(INCLUDE) ? INCLUDE : EXTEND;
                Node argsNode = ((FCallNode) node).getArgsNode();
                if (argsNode instanceof ListNode) {
                    result.get(key).addAll(AstUtilities.getValuesAsFqn((ListNode) argsNode));
                }
            }
        } else if (node instanceof CallNode) {
            // Look for ActionController::Base.class_eval do block to make
            // sure we have the right special case
            CallNode call = (CallNode) node;

            if (call.getName().equals("class_eval")) { // NOI18N
                Node receiver = call.getReceiverNode();
                if ("Base".equals(AstUtilities.safeGetName(receiver))) {
                    found = true;
                }
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            if (scan(child, result)) {
                found = true;
            }
        }

        return found;
    }

    private void handleSchemaDefinitions() {
        // Make sure we're in Rails 2.0...
        if (analyzer.getUrl().indexOf("activerecord-2") == -1) { // NOI18N
            return;
        }

        Node root = AstUtilities.getRoot(analyzer.getResult());

        if (root == null) {
            return;
        }

        Map<String, Set<String>> result = createResultMap();
        scan(root, result);

        IndexDocument document = analyzer.getSupport().createDocument(analyzer.getIndexable());
        analyzer.getDocuments().add(document);

        addResults(document, result);

        // TODO:
        int flags = 0;
        document.addPair(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags), false, true);

        String clz = "TableDefinition";
        String classIn = "ActiveRecord::ConnectionAdapters";
        String classFqn = classIn + "::" + clz;
        String clzNoCase = clz.toLowerCase();

        document.addPair(FIELD_FQN_NAME, classFqn, true, true);
        document.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, clzNoCase, true, true);
        document.addPair(FIELD_CLASS_NAME, clz, true, true);
        document.addPair(FIELD_IN, classIn, false, true);

        // Insert methods:
        for (String type : new String[]{"string", "text", "integer", "float", "decimal", "datetime", "timestamp", "time", "date", "binary", "boolean"}) { // NOI18N
            Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);

            int mflags = getModifiersFlag(modifiers);
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append("(names,options);"); // NOI18N
            sb.append(IndexedElement.flagToFirstChar(mflags));
            sb.append(IndexedElement.flagToSecondChar(mflags));
            sb.append(";;;options(=>limit|default:nil|null:bool|precision|scale)"); // NOI18N

            String signature = sb.toString();

            document.addPair(FIELD_METHOD_NAME, signature, true, true);
        }
    }

    private void addResults(IndexDocument document, Map<String, Set<String>> result) {
        String r = analyzer.getRequireString(result.get(REQUIRE));
        if (r != null) {
            document.addPair(FIELD_REQUIRES, r, false, true);
        }
        analyzer.addRequire(document);

        String includes = analyzer.getIncludedString(result.get(INCLUDE));
        if (includes != null) {
            document.addPair(FIELD_INCLUDES, includes, false, true);
        }

        String extendz = analyzer.getIncludedString(result.get(EXTEND));
        if (extendz != null) {
            document.addPair(FIELD_EXTEND_WITH, extendz, false, true);
        }
    }
}
