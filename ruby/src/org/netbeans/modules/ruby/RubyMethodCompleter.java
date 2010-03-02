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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.IScopingNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.RubyCompletionItem.DbItem;
import org.netbeans.modules.ruby.RubyCompletionItem.MethodItem;
import org.netbeans.modules.ruby.RubyParser.Sanitize;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedConstant;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.lexer.Call;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;

final class RubyMethodCompleter extends RubyBaseCompleter {

    private static int callLineStart = -1;
    private static IndexedMethod callMethod;
    private final String fqn;
    private final Call call;

    static boolean complete(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final String fqn,
            final Call call,
            final int anchor,
            final boolean caseSensitive) {
        RubyMethodCompleter rsc = new RubyMethodCompleter(proposals, request, fqn, call, anchor, caseSensitive);
        return rsc.complete();
    }

    private RubyMethodCompleter(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final String fqn,
            final Call call,
            final int anchor,
            final boolean caseSensitive) {
        super(proposals, request, anchor, caseSensitive);
        this.fqn = fqn;
        this.call = call;
    }

    /**
     * Determine if we're trying to complete the name of a method on another
     * object rather than an inherited or local one. These should list ALL known
     * methods, unless of course we know the type of the method we're operating
     * on (such as strings or regexps), or types inferred through data flow
     * analysis
     *
     * @todo Look for self or this or super; these should be limited to
     *       inherited.
     */
    private boolean complete() {

        final String prefix = request.prefix;
        final int lexOffset = request.lexOffset;
        final TokenHierarchy<Document> th = request.th;
        final AstPath path = request.path;
        final QuerySupport.Kind kind = request.kind;
        final Node target = request.target != null ? AstUtilities.findNextNonNewLineNode(request.target) : null;

        TokenSequence<? extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

        // Look in the token stream for constructs of the type
        //   foo.x^
        // or
        //   foo.^
        // and if found, add all methods
        // (no keywords etc. are possible matches)
        if ((getIndex() == null) || (ts == null)) {
            return false;
        }
        boolean skipPrivate = true;

        if ((call == Call.LOCAL) || (call == Call.NONE)) {
            return false;
        }

        // If we're not sure we're only looking for a method, don't abort after this
        boolean done = call.isMethodExpected();

        String lhs = call.getLhs();
        boolean skipInstanceMethods = call.isStatic();

        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();

        RubyType type = RubyType.unknown();
        final RubyType callType = call.getType();
        if (callType.isKnown() && !call.isLHSConstant()) {
            type = callType;
        }

        // Target might be null somehow, since AST is not always available when
        // the source is in the incosistent state
        if (!type.isKnown() && lhs != null && target != null) {
            if (call.isSimpleIdentifier() || call.isLHSConstant()) {
                Node method = AstUtilities.findLocalScope(target, path);

                String _lhs = lhs;
                if (call.isLHSConstant()) {
                    // TODO: curently constants are class/module insensitive, cf. #154098
                    int lastColon2 = lhs.lastIndexOf("::"); // NOI18N
                    if (lastColon2 != -1) {
                        _lhs = lhs.substring(lastColon2 + 2);
                    }
                }
                if (method != null) {
                    // TODO - if the lhs is "foo.bar." I need to split this
                    // up and do it a bit more cleverly
                    type = getTypesForConstant(lhs);
                    if (!type.isKnown()) {
                        // try fqn
                        type = getTypesForConstant(AstUtilities.getFqnName(path, lhs));
                    }
                    if (!type.isKnown()) {
                        type = createTypeInferencer(request, method).inferType(_lhs);
                    }
                    if (type.isKnown() && call.isLHSConstant()) {
                        // lhs is not a class or module, is a constant for which we have
                        // type-inference. Clumsy -> polish infrastructure..
                        skipInstanceMethods = false;
                    }
                }
                if (!type.isKnown() && call.isLHSConstant() && callType != null) {
                    type = callType;
                }
            } else if (AstUtilities.isAssignmentNode(target)) {
                if (!target.childNodes().isEmpty()) {
                    Node child = target.childNodes().get(0);
                    if (AstUtilities.isCall(child)) {
                        type = RubyTypeInferencer.create(request.createContextKnowledge(), false).inferType(child);
                    }
                }
            }
        }

        if (!type.isKnown() && target != null && AstUtilities.isCall(target)) {
            type = getTypeForCall(target);
        }

        // I'm not doing any data flow analysis at this point, so
        // I can't do anything with a LHS like "foo.". Only actual types.
        if (type.isKnown()) {
            if ("self".equals(lhs)) { // NOI18N
                type = RubyType.create(fqn);
                skipPrivate = true;
            } else if ("super".equals(lhs)) { // NOI18N
                skipPrivate = true;

                IndexedClass sc = getIndex().getSuperclass(fqn);

                if (sc != null) {
                    type = RubyType.create(sc.getFqn());
                } else {
                    ClassNode cls = AstUtilities.findClass(path);

                    if (cls != null) {
                        type = RubyType.create(AstUtilities.getSuperclass(cls));
                    }
                }

                if (!type.isKnown()) {
                    type = RubyType.OBJECT;
                }
            }

            if (type.isKnown()) {
                // Possibly a class on the left hand side: try searching with the class as a qualifier.
                // Try with the LHS + current FQN recursively. E.g. if we're in
                // Test::Unit when there's a call to Foo.x, we'll try
                // Test::Unit::Foo, and Test::Foo
                String _fqn = fqn;
                while (methods.isEmpty()) {
                    for (String realType : type.getRealTypes()) {
                        methods.addAll(getIndex().getInheritedMethods(_fqn + "::" + realType, prefix, kind));
                    }

                    int f = _fqn.lastIndexOf("::");

                    if (f == -1) {
                        break;
                    } else {
                        _fqn = _fqn.substring(0, f);
                    }
                }

                // Add methods in the class (without an FQN)
                for (String realType : type.getRealTypes()) {
                    methods.addAll(getIndex().getInheritedMethods(realType, prefix, kind));
                }
            }
        }

        // Try just the method call (e.g. across all classes). This is ignoring the
        // left hand side because we can't resolve it.
        if (methods.isEmpty() || type.hasUnknownMember()) {
            methods.addAll(getIndex().getMethods(prefix, kind));
        }

        for (IndexedMethod method : RubyDynamicFindersCompleter.proposeDynamicMethods(methods, proposals, request, anchor)) {
            // Don't include private or protected methods on other objects
            if (skipPrivate && (method.isPrivate() && !"new".equals(method.getName()))) {
                // TODO - "initialize" removal here should not be necessary since they should
                // be marked as private, but index doesn't contain that yet
                continue;
            }

            // We can only call static methods. And module class is a special case (#110267)
            if (skipInstanceMethods && !method.isStatic() && !method.doesBelongToModule()) {
                continue;
            }

            // Do not offer instance methods of Module class as instance methods (issue #110267)
            if (!skipInstanceMethods && method.doesBelongToModule()) {
                continue;
            }

            // do not offer static methods for instances
            if (!skipInstanceMethods && method.isStatic()) {
                continue;
            }

            if (method.isNoDoc()) {
                continue;
            }

            if (method.getMethodType() == IndexedMethod.MethodType.DBCOLUMN) {
                DbItem item = new DbItem(method, method.getName(), method.getIn(), anchor, request);
                propose(item);
                continue;
            }

            MethodItem methodItem = new MethodItem(method, anchor, request);
            // Exact matches
            methodItem.setSmart(method.isSmart());
            propose(methodItem);
        }

        return done;
    }

    private RubyType getTypeForCall(Node target) {
        if ("".equals(request.prefix)) {
            // we often have broken AST here, try to handle one commmon case
            Node realTarget = findClosestMatchingNode(target);
            if (realTarget != null) {
                target = realTarget;
            }
            return RubyTypeInferencer.create(request.createContextKnowledge(), false).inferType(target);
        } else {
            if (target instanceof CallNode) {
                Node receiver = ((CallNode) target).getReceiverNode();
                return RubyTypeInferencer.create(request.createContextKnowledge(), false).inferType(receiver);
            } else { // receiver is self
                IScopingNode clazz = AstUtilities.findClassOrModule(request.path);
                if (clazz != null) {
                    return RubyType.create(AstUtilities.getClassOrModuleName(clazz));
                }
            }
        }
        return RubyType.unknown();
    }

    private Node findClosestMatchingNode(Node target) {
        // when we have e.g.
        // a_method().anotherMethod.^ (<= invoke CC here)
        // Foo.new
        //
        // the target is Foo and anotherMethod is its receiver (since the AST is broken)
        // this method tries to find the real target based on the lhs.
        String name = AstUtilities.getCallName(target);
        String lhs = call.getLhs();
        if (lhs == null) {
            return target;
        }
        if (lhs.equals(name)) {
            return target;
        }
        int lastDot = lhs.lastIndexOf(".");
        if (lastDot != -1) {
            lhs = lhs.substring(lastDot + 1, lhs.length());
            int lastLeftParen = lhs.lastIndexOf("(");
            if (lastLeftParen != -1) {
                lhs = lhs.substring(lastLeftParen + 1, lhs.length());
            }
        }
        if (name.equals(lhs)) {
            return target;
        }
        for (Node child : target.childNodes()) {
            if (AstUtilities.isCall(child) && lhs.equals(AstUtilities.getCallName(child))) {
                return child;
            }
        }
        return null;
    }
    /**
     * Compute the current method call at the given offset. Returns false if
     * we're not in a method call. The argument index is returned in
     * parameterIndexHolder[0] and the method being called in methodHolder[0].
     */
    static boolean computeMethodCall(Parser.Result parserResult, int lexOffset, int astOffset,
            IndexedMethod[] methodHolder, int[] parameterIndexHolder, int[] anchorOffsetHolder,
            Set<IndexedMethod>[] alternativesHolder, QuerySupport.Kind kind) {
        try {
            Node root = AstUtilities.getRoot(parserResult);

            if (root == null) {
                return false;
            }

            IndexedMethod targetMethod = null;
            int index = -1;

            AstPath path = null;
            // Account for input sanitation
            // TODO - also back up over whitespace, and if I hit the method
            // I'm parameter number 0
            int originalAstOffset = astOffset;

            // Adjust offset to the left
            BaseDocument doc = RubyUtils.getDocument(parserResult, true);
            if (doc == null) {
                return false;
            }
            int newLexOffset = LexUtilities.findSpaceBegin(doc, lexOffset);
            if (newLexOffset < lexOffset) {
                astOffset -= (lexOffset - newLexOffset);
            }

            RubyParseResult rpr = AstUtilities.getParseResult(parserResult);
            OffsetRange range = rpr.getSanitizedRange();
            if (range != OffsetRange.NONE && range.containsInclusive(astOffset)) {
                if (astOffset != range.getStart()) {
                    astOffset = range.getStart() - 1;
                    if (astOffset < 0) {
                        astOffset = 0;
                    }
                    path = new AstPath(root, astOffset);
                }
            }

            if (path == null) {
                path = new AstPath(root, astOffset);
            }

            int currentLineStart = Utilities.getRowStart(doc, lexOffset);
            if (callLineStart != -1 && currentLineStart == callLineStart) {
                // We know the method call
                targetMethod = callMethod;
            // if (targetMethod != null) {
            // Somehow figure out the argument index
            // Perhaps I can keep the target tree around and look in it
            // (This is all trying to deal with temporarily broken
            // or ambiguous calls.
            // }
            }
            // Compute the argument index

            Node call = null;
            int anchorOffset = -1;

            if (targetMethod != null) {
                Iterator<Node> it = path.leafToRoot();
                String name = targetMethod.getName();
                while (it.hasNext()) {
                    Node node = it.next();
                    if (AstUtilities.isCall(node) &&
                            name.equals(AstUtilities.getCallName(node))) {
                        if (node.getNodeType() == NodeType.CALLNODE) {
                            Node argsNode = ((CallNode) node).getArgsNode();

                            if (argsNode != null) {
                                index = AstUtilities.findArgumentIndex(argsNode, astOffset);

                                if (index == -1 && astOffset < originalAstOffset) {
                                    index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
                                }

                                if (index != -1) {
                                    call = node;
                                    anchorOffset = argsNode.getPosition().getStartOffset();
                                }
                            }
                        } else if (node.getNodeType() == NodeType.FCALLNODE) {
                            Node argsNode = ((FCallNode) node).getArgsNode();

                            if (argsNode != null) {
                                index = AstUtilities.findArgumentIndex(argsNode, astOffset);

                                if (index == -1 && astOffset < originalAstOffset) {
                                    index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
                                }

                                if (index != -1) {
                                    call = node;
                                    anchorOffset = argsNode.getPosition().getStartOffset();
                                }
                            }
                        } else if (node.getNodeType() == NodeType.VCALLNODE) {
                            // We might be completing at the end of a method call
                            // and we don't have parameters yet so it just looks like
                            // a vcall, e.g.
                            //   create_table |
                            // This is okay as long as the caret is outside and to
                            // the right of this call. However
                            final OffsetRange callRange = AstUtilities.getCallRange(node);
                            AstUtilities.getCallName(node);
                            if (originalAstOffset > callRange.getEnd()) {
                                index = 0;
                                call = node;
                                anchorOffset = callRange.getEnd() + 1;
                            }
                        }

                        break;
                    }
                }
            }

            boolean haveSanitizedComma = rpr.getSanitized() == Sanitize.EDITED_DOT ||
                    rpr.getSanitized() == Sanitize.ERROR_DOT;
            if (haveSanitizedComma) {
                // We only care about removed commas since that
                // affects the parameter count
                if (rpr.getSanitizedContents().indexOf(',') == -1) {
                    haveSanitizedComma = false;
                }
            }

            if (call == null) {
                // Find the call in around the caret. Beware of
                // input sanitization which could have completely
                // removed the current parameter (e.g. with just
                // a comma, or something like ", @" or ", :")
                // where we accidentally end up in the previous
                // parameter.
                ListIterator<Node> it = path.leafToRoot();
                nodesearch:
                while (it.hasNext()) {
                    Node node = it.next();

                    if (kind == QuerySupport.Kind.EXACT) {
                        // For documentation popups, don't go up through blocks
                        if (node.getNodeType() == NodeType.ITERNODE || node.getNodeType() == NodeType.DEFNNODE || node.getNodeType() == NodeType.DEFSNODE) {
                            // Don't consider calls outside the current block or method (149540)
                            break;
                        }
                    }

                    if (node.getNodeType() == NodeType.CALLNODE) {
                        final OffsetRange callRange = AstUtilities.getCallRange(node);
                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
                            for (int i = 0; i < 3 && it.hasNext(); i++) {
                                // It's not really a peek in the sense
                                // that there's no reason to retry these
                                // nodes later
                                Node peek = it.next();
                                if (AstUtilities.isCall(peek) &&
                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(parserResult, peek.getPosition().getStartOffset())) ==
                                        Utilities.getRowStart(doc, lexOffset)) {
                                    // Use the outer method call instead
                                    if (it.hasPrevious()) {
                                        it.previous();
                                    }
                                    continue nodesearch;
                                }
                            }
                        }

                        Node argsNode = ((CallNode) node).getArgsNode();

                        if (argsNode != null) {
                            index = AstUtilities.findArgumentIndex(argsNode, astOffset);

                            if (index == -1 && astOffset < originalAstOffset) {
                                index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
                            }

                            if (index != -1) {
                                call = node;
                                anchorOffset = argsNode.getPosition().getStartOffset();

                                break;
                            }
                        } else {
                            if (originalAstOffset > callRange.getEnd()) {
                                index = 0;
                                call = node;
                                anchorOffset = callRange.getEnd() + 1;
                                break;
                            }
                        }
                    } else if (node.getNodeType() == NodeType.FCALLNODE) {
                        final OffsetRange callRange = AstUtilities.getCallRange(node);
                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
                            for (int i = 0; i < 3 && it.hasNext(); i++) {
                                // It's not really a peek in the sense
                                // that there's no reason to retry these
                                // nodes later
                                Node peek = it.next();
                                if (AstUtilities.isCall(peek) &&
                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(parserResult, peek.getPosition().getStartOffset())) ==
                                        Utilities.getRowStart(doc, lexOffset)) {
                                    // Use the outer method call instead
                                    if (it.hasPrevious()) {
                                        it.previous();
                                    }
                                    continue nodesearch;
                                }
                            }
                        }

                        Node argsNode = ((FCallNode) node).getArgsNode();

                        if (argsNode != null) {
                            index = AstUtilities.findArgumentIndex(argsNode, astOffset);

                            if (index == -1 && astOffset < originalAstOffset) {
                                index = AstUtilities.findArgumentIndex(argsNode, originalAstOffset);
                            }

                            if (index != -1) {
                                call = node;
                                anchorOffset = argsNode.getPosition().getStartOffset();

                                break;
                            }
                        }
                    } else if (node.getNodeType() == NodeType.VCALLNODE) {
                        // We might be completing at the end of a method call
                        // and we don't have parameters yet so it just looks like
                        // a vcall, e.g.
                        //   create_table |
                        // This is okay as long as the caret is outside and to
                        // the right of this call.

                        final OffsetRange callRange = AstUtilities.getCallRange(node);
                        if (haveSanitizedComma && originalAstOffset > callRange.getEnd() && it.hasNext()) {
                            for (int i = 0; i < 3 && it.hasNext(); i++) {
                                // It's not really a peek in the sense
                                // that there's no reason to retry these
                                // nodes later
                                Node peek = it.next();
                                if (AstUtilities.isCall(peek) &&
                                        Utilities.getRowStart(doc, LexUtilities.getLexerOffset(parserResult, peek.getPosition().getStartOffset())) ==
                                        Utilities.getRowStart(doc, lexOffset)) {
                                    // Use the outer method call instead
                                    if (it.hasPrevious()) {
                                        it.previous();
                                    }
                                    continue nodesearch;
                                }
                            }
                        }

                        if (originalAstOffset > callRange.getEnd()) {
                            index = 0;
                            call = node;
                            anchorOffset = callRange.getEnd() + 1;
                            break;
                        }
                    }
                }
            }

            if (index != -1 && haveSanitizedComma && call != null) {
                Node an = null;
                if (call.getNodeType() == NodeType.FCALLNODE) {
                    an = ((FCallNode) call).getArgsNode();
                } else if (call.getNodeType() == NodeType.CALLNODE) {
                    an = ((CallNode) call).getArgsNode();
                }
                if (an != null && index < an.childNodes().size() &&
                        an.childNodes().get(index).getNodeType() == NodeType.HASHNODE) {
                    // We should stay within the hashnode, so counteract the
                    // index++ which follows this if-block
                    index--;
                }

                // Adjust the index to account for our removed
                // comma
                index++;
            }

            if ((call == null) || (index == -1)) {
                callLineStart = -1;
                callMethod = null;
                return false;
            } else if (targetMethod == null) {
                // Look up the
                // See if we can find the method corresponding to this call
                targetMethod = new RubyDeclarationFinder().findMethodDeclaration(parserResult, call, path,
                        alternativesHolder);
                if (targetMethod == null) {
                    return false;
                }
            }

            callLineStart = currentLineStart;
            callMethod = targetMethod;

            methodHolder[0] = callMethod;
            parameterIndexHolder[0] = index;

            // TODO - if you're in a splat target, I should be highlighting the splat target!!
            if (anchorOffset == -1) {
                anchorOffset = call.getPosition().getStartOffset(); // TODO - compute
            }
            anchorOffsetHolder[0] = anchorOffset;
        } catch (BadLocationException ble) {
            return false;
        }

        return true;
    }

    private static RubyTypeInferencer createTypeInferencer(final CompletionRequest request, final Node target) {
        ContextKnowledge knowledge = request.createContextKnowledge();
        request.target = target;
        return RubyTypeInferencer.create(knowledge, false);
    }

    private RubyType getTypesForConstant(final String constantFqn) {
        String module = RubyUtils.parseConstantName(constantFqn)[0];
        Set<? extends IndexedConstant> constants = getIndex().getConstants(constantFqn);
        for (IndexedConstant indexedConstant : constants) {
            if (module.equals(indexedConstant.getFqn())) {
                RubyType type = indexedConstant.getType();
                if (type.isKnown()) {
                    return type;
                }
            }
        }
        return RubyType.unknown();

    }
}
