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
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNode.Attribute;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class LibraryDeclarationChecker extends HintsProvider {

    @Override
    public List<Hint> compute(RuleContext context) {
        List<Hint> hints = new ArrayList<Hint>();

        checkLibraryDeclarations(hints, context);

        return hints;
    }

    //check the namespaces declaration:
    //1. if the declared library is available
    //2. if the declared library is used + remove unused declaration hint
    //3. if there are usages of undeclared library 
    //    + hint to add the declaration (if library available)
    //        - by default prefix
    //        - or search all the libraries for such component and offer the match/es
    //
    private void checkLibraryDeclarations(final List<Hint> hints, final RuleContext context) {
        final HtmlParserResult result = (HtmlParserResult) context.parserResult;
        final Snapshot snapshot = result.getSnapshot();

        //find all usages of composite components tags for this page
        Collection<String> declaredNamespaces = result.getNamespaces().keySet();
        final Collection<FaceletsLibrary> declaredLibraries = new ArrayList<FaceletsLibrary>();
        JsfSupport jsfSupport = JsfSupport.findFor(context.doc);
        Map<String, FaceletsLibrary> libs = Collections.EMPTY_MAP;
        if (jsfSupport != null) {
            libs = jsfSupport.getFaceletsLibraries();
        }

        //Find the namespaces declarations itself
        //a.take the html AST
        //b.search for nodes with xmlns attribute
        //ugly, grr, the whole namespace support needs to be fixed
        final Map<String, AstNode.Attribute> namespace2Attribute = new HashMap<String, Attribute>();
        AstNode root = result.root();
        AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

            public void visit(AstNode node) {
                if (node.type() == AstNode.NodeType.OPEN_TAG) {
                    //put all NS attributes to the namespace2Attribute map for #1.
                    Collection<AstNode.Attribute> nsAttrs = node.getAttributes(new AstNode.AttributeFilter() {

                        public boolean accepts(Attribute attribute) {
                            return "xmlns".equals(attribute.namespacePrefix()); //NOI18N
                        }
                    });
                    for (AstNode.Attribute attr : nsAttrs) {
                        namespace2Attribute.put(attr.unquotedValue(), attr);
                    }
                } else if (node.type() == AstNode.NodeType.UNKNOWN_TAG && node.getNamespacePrefix() != null) {
                    //3. check for undeclared components

                    List<HintFix> fixes = new ArrayList<HintFix>();
                    List<FaceletsLibrary> libs = FixLibDeclaration.getLibsByPrefix(context.doc, node.getNamespacePrefix());

                    for (FaceletsLibrary lib : libs){
                        FixLibDeclaration fix = new FixLibDeclaration(context.doc, node.getNamespacePrefix(), lib);
                        fixes.add(fix);
                    }

                    //this itself means that the node is undeclared since
                    //otherwise it wouldn't appear in the pure html parse tree
                    Hint hint = new Hint(DEFAULT_ERROR_RULE,
                            NbBundle.getMessage(HintsProvider.class, "MSG_UNDECLARED_COMPONENT"), //NOI18N
                            context.parserResult.getSnapshot().getSource().getFileObject(),
                            JsfUtils.createOffsetRange(snapshot, node.startOffset(), node.startOffset() + node.name().length() + 1 /* "<".length */),
                            fixes, DEFAULT_ERROR_HINT_PRIORITY);
                    hints.add(hint);
                }
            }
        });

        for (String namespace : declaredNamespaces) {
            FaceletsLibrary lib = libs.get(namespace);
            if (lib != null) {
                declaredLibraries.add(lib);
            } else {
                //1. report error - missing library for the declaration
                Attribute attr = namespace2Attribute.get(namespace);
                if (attr != null) {
                    //found the declaration, mark as error
                    Hint hint = new Hint(DEFAULT_ERROR_RULE,
                            NbBundle.getMessage(HintsProvider.class, "MSG_MISSING_LIBRARY"), //NOI18N
                            context.parserResult.getSnapshot().getSource().getFileObject(),
                            JsfUtils.createOffsetRange(snapshot, attr.nameOffset(), attr.valueOffset() + attr.value().length()),
                            Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                    hints.add(hint);
                }
            }
        }

        //2. find for unused declarations
        final Collection<PositionRange> ranges = new ArrayList<PositionRange>();
        context.doc.render(new Runnable() { //isFunctionLibraryPrefixUsadInEL accesses the document's token hierarchy
            @Override
            public void run() {
                for (FaceletsLibrary lib : declaredLibraries) {
                    AstNode rootNode = result.root(lib.getNamespace());
                    if (rootNode == null) {
                        continue; //no parse result for this namespace, the namespace is not declared
                    }
                    final int[] usages = new int[1];
                    AstNodeUtils.visitChildren(rootNode, new AstNodeVisitor() {

                        public void visit(AstNode node) {
                            usages[0]++;
                        }
                    }, AstNode.NodeType.OPEN_TAG);

                    usages[0] += isFunctionLibraryPrefixUsadInEL(context, lib) ? 1 : 0;

                    if (usages[0] == 0) {
                        //unused declaration
                        Attribute declAttr = namespace2Attribute.get(lib.getNamespace());
                        if (declAttr != null) {
                            int from = declAttr.nameOffset();
                            int to = declAttr.valueOffset() + declAttr.value().length();
                            try {
                                ranges.add(new PositionRange(context, from, to));
                            } catch (BadLocationException ex) {
                                //just ignore
                            }
                        }

                    }
                }
            }
        });
        

        //generate remove all unused declarations
        for (PositionRange range : ranges) {
            int from = range.getFrom();
            int to = range.getTo();

            List<HintFix> fixes = Arrays.asList(new HintFix[]{
                new RemoveUnusedLibraryDeclarationHintFix(context.doc, range), //the only occurance
                new RemoveUnusedLibrariesDeclarationHintFix(context.doc, ranges) //remove all
            });
            Hint hint = new Hint(DEFAULT_WARNING_RULE,
                    NbBundle.getMessage(HintsProvider.class, "MSG_UNUSED_LIBRARY_DECLARATION"), //NOI18N
                    context.parserResult.getSnapshot().getSource().getFileObject(),
                    JsfUtils.createOffsetRange(snapshot, from, to),
                    fixes, DEFAULT_ERROR_HINT_PRIORITY);

            hints.add(hint);
        }

    }

    private boolean isFunctionLibraryPrefixUsadInEL(RuleContext context, FaceletsLibrary lib) {
        String libraryPrefix = ((HtmlParserResult)context.parserResult).getNamespaces().get(lib.getNamespace());
        Document doc = context.doc;

        //lets suppose we operate on the xhtml document which has a top level lexer and the EL
        //is always embedded on the first embedding level
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        TokenSequence<?> ts = th.tokenSequence();
        ts.moveStart();
        while(ts.moveNext()) {
            TokenSequence<ELTokenId> elts = ts.embeddedJoined(ELTokenId.language());
            if(elts != null) {
                //check the EL expression for the function library prefix usages
                elts.moveStart();
                while(elts.moveNext()) {
                    if(elts.token().id() == ELTokenId.TAG_LIB_PREFIX && CharSequenceUtilities.equals(libraryPrefix, elts.token().text())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static class RemoveUnusedLibraryDeclarationHintFix extends RemoveUnusedLibrariesDeclarationHintFix {

        public RemoveUnusedLibraryDeclarationHintFix(BaseDocument document, PositionRange range) {
            super(document, Collections.<PositionRange>singletonList(range));
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HintsProvider.class, "MSG_HINTFIX_REMOVE_UNUSED_LIBRARY_DECLARATION");
        }

    }

    private static class RemoveUnusedLibrariesDeclarationHintFix implements HintFix {

        protected Collection<PositionRange> ranges = new ArrayList<PositionRange>();
        protected BaseDocument document;

        public RemoveUnusedLibrariesDeclarationHintFix(BaseDocument document, Collection<PositionRange> ranges) {
            this.document = document;
            this.ranges = ranges;
        }

        public String getDescription() {
            return NbBundle.getMessage(HintsProvider.class, "MSG_HINTFIX_REMOVE_ALL_UNUSED_LIBRARIES_DECLARATION");
        }

        public void implement() throws Exception {
            document.runAtomic(new Runnable() {

                public void run() {
                    try {
                        for (PositionRange range : ranges) {
                            int from = range.getFrom();
                            int to = range.getTo();
                            //check if the line before the area is white
                            int lineBeginning = Utilities.getRowStart(document, from);
                            int firstNonWhite = Utilities.getFirstNonWhiteBwd(document, from);
                            if (lineBeginning > firstNonWhite) {
                                //delete the white content before the area inclusing the newline
                                from = lineBeginning - 1; // (-1 => includes the line end)
                            }
                            document.remove(from, to - from);
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            });
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }

    private static final class PositionRange {

        private Position from, to;

        public PositionRange(RuleContext context, int from, int to) throws BadLocationException {
            Snapshot snapshot = context.parserResult.getSnapshot();
            //the constructor will simply throw BLE when the embedded to source offset conversion fails (returns -1)
            this.from = context.doc.createPosition(snapshot.getOriginalOffset(from));
            this.to = context.doc.createPosition(snapshot.getOriginalOffset(to));
        }

        public int getFrom() {
            return from.getOffset();
        }

        public int getTo() {
            return to.getOffset();
        }
    }
}
