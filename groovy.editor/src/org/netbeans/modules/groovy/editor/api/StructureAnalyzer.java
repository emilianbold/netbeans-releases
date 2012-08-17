/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.groovy.editor.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTClass;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTField;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTMethod;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.openide.util.Exceptions;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.StructureScanner.Configuration;
import org.netbeans.modules.csl.spi.ParserResult;


/**
 * @author Martin Adamek
 */
public class StructureAnalyzer implements StructureScanner {

    private List<ASTElement> structure;
    private Map<ASTClass, Set<FieldNode>> fields;
    private Map<ASTClass, Set<PropertyNode>> properties;
    private List<ASTMethod> methods;
    
    private static final Logger LOG = Logger.getLogger(StructureAnalyzer.class.getName());

    public AnalysisResult analyze(GroovyParserResult result) {
        return scan(result);
    }

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        GroovyParserResult result = ASTUtils.getParseResult(info);

        AnalysisResult ar = result.getStructure();
        List<? extends ASTElement> elements = ar.getElements();
        List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        for (ASTElement e : elements) {
            if (isVisible(e)) {
                itemList.add(new GroovyStructureItem(e, info));
            }
        }

        return itemList;
    }

    private AnalysisResult scan(GroovyParserResult result) {
        AnalysisResult analysisResult = new AnalysisResult();

        ASTNode root = ASTUtils.getRoot(result);

        if (root == null) {
            return analysisResult;
        }

        structure = new ArrayList<ASTElement>();
        fields = new HashMap<ASTClass, Set<FieldNode>>();
        methods = new ArrayList<ASTMethod>();
        properties = new HashMap<ASTClass, Set<PropertyNode>>();

        AstPath path = new AstPath();
        path.descend(root);
        // TODO: I should pass in a "default" context here to stash methods etc. outside of modules and classes
        scan(result, root, path, null, null, null);
        path.ascend();

        // Process fields
        Map<String, FieldNode> names = new HashMap<String, FieldNode>();

        for (ASTClass clz : fields.keySet()) {
            Set<FieldNode> assignments = fields.get(clz);

            // Find unique variables
            if (assignments != null) {
                for (FieldNode assignment : assignments) {
                    names.put(assignment.getName(), assignment);
                }

                // Add unique fields
                for (FieldNode field : names.values()) {
                    ASTField co = new ASTField(result, field);
                    //co.setIn(AstUtilities.getClassOrModuleName(clz));
                    co.setIn(clz.getFqn());

                    // Make sure I don't already have an entry for this field as an
                    // attr_accessor or writer
                    String fieldName = field.getName();

                    boolean found = false;

                    Set<PropertyNode> nodes = properties.get(clz);
                    if (nodes != null) {
                        for (PropertyNode node : nodes) {
                            if (fieldName.equals(node.getName())) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (found) {
                        co.setProperty(true);
                    }
                    clz.addChild(co);
                }

                names.clear();
            }
        }

        analysisResult.setElements(structure);

        return analysisResult;
    }

    private void scan(GroovyParserResult result, ASTNode node, AstPath path, String in, Set<String> includes, ASTElement parent) {
        if (node instanceof AnnotatedNode
                && !((AnnotatedNode) node).hasNoRealSourcePosition()) {

            if (node instanceof ClassNode) {
                ASTClass co = new ASTClass(result, node);
                co.setFqn(((ClassNode) node).getName());

                if (parent != null) {
                    parent.addChild(co);
                } else {
                    structure.add(co);
                }

                parent = co;
            } else if (node instanceof FieldNode) {
                if (parent instanceof ASTClass) {
                    // We don't have unique declarations, only assignments (possibly many)
                    // so stash these in a map and extract unique fields when we're done
                    Set<FieldNode> assignments = fields.get(parent);

                    if (assignments == null) {
                        assignments = new HashSet<FieldNode>();
                        fields.put((ASTClass) parent, assignments);
                    }

                    assignments.add((FieldNode) node);
                }
            } else if (node instanceof MethodNode) {
                ASTMethod co = new ASTMethod(result, node);
                methods.add(co);
                co.setIn(in);

                // TODO - don't add this to the top level! Make a nested list
                if (parent != null) {
                    parent.addChild(co);
                } else {
                    structure.add(co);
                }
            } else if (node instanceof PropertyNode) {
                Set<PropertyNode> declarations = properties.get(parent);

                if (declarations == null) {
                    declarations = new HashSet<PropertyNode>();
                    properties.put((ASTClass) parent, declarations);
                }

                declarations.add((PropertyNode) node);
            }
        }

        @SuppressWarnings("unchecked")
        List<ASTNode> list = ASTUtils.children(node);

        for (ASTNode child : list) {
            path.descend(child);
            scan(result, child, path, in, includes, parent);
            path.ascend();
        }
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        ASTNode root = ASTUtils.getRoot(info);

        if (root == null) {
            return Collections.emptyMap();
        }

        GroovyParserResult rpr = ASTUtils.getParseResult(info);
        AnalysisResult analysisResult = rpr.getStructure();

        Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        List<OffsetRange> codefolds = new ArrayList<OffsetRange>();
        folds.put("codeblocks", codefolds); // NOI18N

        final BaseDocument doc = LexUtilities.getDocument(rpr, false);
        if (doc == null) {
            return Collections.emptyMap();
        }

        final OffsetRange[] importsRange = new OffsetRange[1];
        final List<OffsetRange> commentsRanges = new ArrayList<OffsetRange>();

        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(doc, 1);

                int importStart = 0;
                int importEnd = 0;

                boolean startSet = false;

                while (ts != null && ts.isValid() && ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == GroovyTokenId.LITERAL_import) {
                        int offset = ts.offset();
                        if (!startSet) {
                            importStart = offset;
                            startSet = true;
                        }
                        importEnd = offset;
                    } else if (t.id() == GroovyTokenId.BLOCK_COMMENT) {
                        // does this Block comment (GSF_BLOCK_COMMENT) span
                        // multiple lines? E.g. includes \n ?
                        StringBuffer sb = new StringBuffer(t.text());

                        if (sb.indexOf("\n") != -1) {
                            int offset = ts.offset();
                            commentsRanges.add(new OffsetRange(offset, offset + t.length()));
                        }
                    }
                }
                try {
                    importEnd = Utilities.getRowEnd(doc, importEnd);
                    importsRange[0]  = new OffsetRange(importStart, importEnd);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        });

        if (!commentsRanges.isEmpty()) {
            folds.put("comments", commentsRanges); // NOI18N
        }

        try {
            if (importsRange[0] != null && Utilities.getRowCount(doc, importsRange[0].getStart(), importsRange[0].getEnd()) > 1) {
                folds.put("imports", Collections.singletonList(importsRange[0])); // NOI18N
            }
            addFolds(doc, analysisResult.getElements(), folds, codefolds);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return folds;
    }

    private void addFolds(BaseDocument doc, List<? extends ASTElement> elements,
            Map<String,List<OffsetRange>> folds, List<OffsetRange> codeblocks) throws BadLocationException {
        for (ASTElement element : elements) {
            ElementKind kind = element.getKind();
            switch (kind) {
            case FIELD:
            case METHOD:
            case CONSTRUCTOR:
            case CLASS:
            case MODULE:
                ASTNode node = element.getNode();
                OffsetRange range = ASTUtils.getRangeFull(node, doc);

                // beware of synthetic elements
                if ((kind == ElementKind.METHOD && !((MethodNode) node).isSynthetic())
                        || (kind == ElementKind.CONSTRUCTOR && !((ConstructorNode) node).isSynthetic())
                        || (kind == ElementKind.FIELD
                            && ((FieldNode) node).getInitialExpression() instanceof ClosureExpression)
                        // Only make nested classes/modules foldable, similar to what the java editor is doing
                        || (range.getStart() > Utilities.getRowStart(doc, range.getStart())) && kind != ElementKind.FIELD) {

                    int start = range.getStart();
                    // Start the fold at the END of the line behind last non-whitespace, remove curly brace, if any
                    start = Utilities.getRowLastNonWhite(doc, start);
                    if (start >= 0 && doc.getChars(start, 1)[0] != '{') {
                        start++;
                    }
                    int end = range.getEnd();
                    if (start != (-1) && end != (-1) && start < end && end <= doc.getLength()) {
                        range = new OffsetRange(start, end);
                        codeblocks.add(range);
                    }
                }
                break;
            }

            List<? extends ASTElement> children = element.getChildren();

            if (children != null && children.size() > 0) {
                addFolds(doc, children, folds, codeblocks);
            }
        }
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    private static boolean isVisible(ASTElement element) {
        // FIXME perhaps we should store synthetic atributte in AstElement
        if ((element.getKind() == ElementKind.METHOD)) {
            ASTMethod method = (ASTMethod) element;
            ASTNode node = method.getNode();
            return !(node instanceof MethodNode) 
                    || (!((MethodNode) node).isSynthetic() && ((MethodNode) node).getLineNumber() >= 0);
        }
        return true;
    }

    public static final class AnalysisResult {

        private List<ASTElement> elements;

        Set<String> getRequires() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        List<ASTElement> getElements() {
            if (elements == null) {
                return Collections.emptyList();
            }
            return elements;
        }

        private void setElements(List<ASTElement> elements) {
            this.elements = elements;
        }

    }

    private static class GroovyStructureItem implements StructureItem {

        private final ASTElement node;

        private final ElementKind kind;

        private final ParserResult info;

        @NullAllowed
        private final BaseDocument doc;

        private GroovyStructureItem(ASTElement node, ParserResult info) {
            this.node = node;
            this.kind = node.getKind();
            this.info = info;
            // FIXME true or false ?
            this.doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
        }

        @Override
        public String getName() {
            return node.getName();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.appendText(node.getName());

            if ((kind == ElementKind.METHOD) || (kind == ElementKind.CONSTRUCTOR)) {
                // Append parameters
                ASTMethod jn = (ASTMethod) node;

                Collection<String> parameters = jn.getParameters();

                if ((parameters != null) && (parameters.size() > 0)) {
                    formatter.appendHtml("(");
                    formatter.parameters(true);

                    for (Iterator<String> it = parameters.iterator(); it.hasNext();) {
                        String ve = it.next();
                        // TODO - if I know types, list the type here instead. For now, just use the parameter name instead
                        formatter.appendText(ve);

                        if (it.hasNext()) {
                            formatter.appendHtml(", ");
                        }
                    }

                    formatter.parameters(false);
                    formatter.appendHtml(")");
                } else {
                    formatter.appendHtml("()");
                }
            }

            return formatter.getText();
        }

        @Override
        public ElementHandle getElementHandle() {
            return node;
        }

        @Override
        public ElementKind getKind() {
            return kind;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return node.getModifiers();
        }

        @Override
        public boolean isLeaf() {
            switch (kind) {
            case ATTRIBUTE:
            case PROPERTY:
            case CONSTANT:
            case CONSTRUCTOR:
            case METHOD:
            case FIELD:
            case KEYWORD:
            case VARIABLE:
            case OTHER:
                return true;

            case MODULE:
            case CLASS:
                return false;

            default:
                throw new RuntimeException("Unhandled kind: " + kind);
            }
        }

        @Override
        public List<?extends StructureItem> getNestedItems() {
            List<ASTElement> nested = node.getChildren();

            if ((nested != null) && (nested.size() > 0)) {
                List<GroovyStructureItem> children = new ArrayList<GroovyStructureItem>(nested.size());

                // FIXME: the same old problem: AstElement != ElementHandle.

                for (ASTElement co : nested) {
                    if (isVisible(co)) {
                        children.add(new GroovyStructureItem(co, info));
                    }
                }

                return children;
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public long getPosition() {
            if (doc != null) {
                OffsetRange range = ASTUtils.getRangeFull(node.getNode(), doc);
                LOG.log(Level.FINEST, "getPosition(), start: {0}", range.getStart());
                return (long) range.getStart();
            }
            return 0;
        }

        @Override
        public long getEndPosition() {
            if (doc != null) {
                OffsetRange range = ASTUtils.getRangeFull(node.getNode(), doc);
                LOG.log(Level.FINEST, "getEndPosition(), end: {0}", range.getEnd());
                return (long) range.getEnd();
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof GroovyStructureItem)) {
                // System.out.println("- not a desc");
                return false;
            }

            GroovyStructureItem d = (GroovyStructureItem)o;

            if (kind != d.kind) {
                // System.out.println("- kind");
                return false;
            }

            if (!getName().equals(d.getName())) {
                // System.out.println("- name");
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = (29 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);
            hash = (29 * hash) + ((this.kind != null) ? this.kind.hashCode() : 0);

            return hash;
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public String getSortText() {
            return getName();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }

    }

}
