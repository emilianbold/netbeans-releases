/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.ruby;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.CommentNode;
import org.jrubyparser.ast.ConstDeclNode;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.DefsNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.GlobalAsgnNode;
import org.jrubyparser.ast.InstAsgnNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.SClassNode;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.SymbolNode;
import org.jrubyparser.ast.INameNode;
import org.jruby.util.ByteList;
import org.jrubyparser.SourcePosition;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner.Configuration;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.elements.AstAttributeElement;
import org.netbeans.modules.ruby.elements.AstClassElement;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.AstFieldElement;
import org.netbeans.modules.ruby.elements.AstMethodElement;
import org.netbeans.modules.ruby.elements.AstModuleElement;
import org.netbeans.modules.ruby.elements.AstNameElement;
import org.netbeans.modules.ruby.elements.Element;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @todo Rewrite various other helper classes to use the scanned structure
 *   for the file instead of searching from scratch. For example, the code
 *   completion scanner should rely on the structure view to add local
 *   classes, fields and globals - it should only scan the current method
 *   for local variables. Similarly, the declaration finder should use it
 *   to locate local classes, method definitions and such. And obviously,
 *   the semantic analyzer should use it to find private methods.
 *
 * @author Tor Norbye
 */
public class RubyStructureAnalyzer implements StructureScanner {
    
    private Set<AstClassElement> haveAccessModifiers;
    private List<AstElement> structure;
    private Map<AstClassElement, Set<InstAsgnNode>> fields;
    private Map<String, GlobalAsgnNode> globals;
    private Set<String> requires;
    private List<AstMethodElement> methods;
    private Map<AstClassElement, Set<AstAttributeElement>> attributes;
    private RubyParseResult result;
    private RubyIndex index;
    private RubyTypeInferencer typeInferencer;
    private boolean isTestFile;

    private static final String RUBY_KEYWORD = "org/netbeans/modules/ruby/jruby.png"; //NOI18N
    private static ImageIcon keywordIcon;
    
    public RubyStructureAnalyzer() {
    }

    public List<?extends StructureItem> scan(final ParserResult result) {
        if (RubyUtils.isRhtmlOrYamlFile(RubyUtils.getFileObject(result))) {
            return scanRhtml(result);
        }

        this.result = AstUtilities.getParseResult(result);
        if (this.result == null) {
            return Collections.<StructureItem>emptyList();
        }

        AnalysisResult ar = this.result.getStructure();
        List<?extends AstElement> elements = ar.getElements();
        List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        for (AstElement e : elements) {
            itemList.add(new RubyStructureItem(e, result));
        }

        return itemList;
    }

    public static class AnalysisResult {
        
        private List<?extends AstElement> elements;
        private Map<AstClassElement, Set<AstAttributeElement>> attributes;
        private Set<String> requires;
        
        private AnalysisResult() {
        }

        public AstElement getElementFor(Node node) {
            for (AstElement element : getElements()) {
                AstElement result = findElement(element, node);
                if (result != null) {
                    return result;
                }
            }

            return null;
        }

        public AstElement findElement(AstElement element, Node node) {
            if (element.getNode() == node) {
                return element;
            }

            for (AstElement child : element.getChildren()) {
                if (child.getNode() == node) {
                    return child;
                }

                AstElement result = findElement(child, node);
                if (result != null) {
                    return result;
                }
            }

            return null;
        }
        
        public Set<String> getRequires() {
            return requires;
        }

        public void setRequires(Set<String> requires) {
            this.requires = requires;
        }

        private void setElements(List<?extends AstElement> elements) {
            this.elements = elements;
        }
        
        private void setAttributes(Map<AstClassElement, Set<AstAttributeElement>> attributes) {
            this.attributes = attributes;
        }
        
        public Map<AstClassElement, Set<AstAttributeElement>> getAttributes() {
            return attributes;
        }
        
        public List<?extends AstElement> getElements() {
            if (elements == null) {
                return Collections.emptyList();
            }
            return elements;
        }
    }

    private AnalysisResult scan(final RubyParseResult result) {
        AnalysisResult analysisResult = new AnalysisResult();

        Node root = AstUtilities.getRoot(result);

        if (root == null) {
            return analysisResult;
        }

        isTestFile = false;
        String name = RubyUtils.getFileObject(result).getNameExt();
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            name = name.substring(0, dot);
        }
        if (name.startsWith("test_") ||  // NOI18N
                name.endsWith("_test") || // NOI18N
                name.endsWith("_spec")) { // NOI18N
            isTestFile = true;
        }

        structure = new ArrayList<AstElement>();
        fields = new HashMap<AstClassElement, Set<InstAsgnNode>>();
        attributes = new HashMap<AstClassElement, Set<AstAttributeElement>>();
        requires = new HashSet<String>();
        methods = new ArrayList<AstMethodElement>();
        haveAccessModifiers = new HashSet<AstClassElement>();
        
        AstPath path = new AstPath();
        path.descend(root);
        ContextKnowledge knowledge = new ContextKnowledge(index, root, result);
        this.typeInferencer = RubyTypeInferencer.create(knowledge);
        // TODO: I should pass in a "default" context here to stash methods etc. outside of modules and classes
        scan(root, path, null, null, null);
        path.ascend();

        // Process fields
        Map<String, InstAsgnNode> names = new HashMap<String, InstAsgnNode>();

        for (AstClassElement clz : fields.keySet()) {
            Set<InstAsgnNode> assignments = fields.get(clz);

            // Find unique variables
            if (assignments != null) {
                for (InstAsgnNode assignment : assignments) {
                    names.put(assignment.getName(), assignment);
                }

                // Add unique fields
                for (InstAsgnNode field : names.values()) {
                    AstFieldElement co = new AstFieldElement(result, field);
                    //co.setIn(AstUtilities.getClassOrModuleName(clz));
                    co.setIn(clz.getFqn());

                    // Make sure I don't already have an entry for this field as an
                    // attr_accessor or writer
                    String fieldName = field.getName();

                    if (fieldName.startsWith("@@")) {
                        fieldName = fieldName.substring(2);
                    } else if (fieldName.startsWith("@")) {
                        fieldName = fieldName.substring(1);
                    }

                    boolean found = false;

                    // commented out to fix #168745 - the field needs
                    // to be added as a child to the class that contains it
                    // even if there is an attribute_accessor for it.
                    // (leaving this code here as i don't know what was the original
                    // reason for excluding it - possibly something i can't think of now)
                    /**
                    for (AstElement member : clz.getChildren()) {
                        if ((member.getKind() == ElementKind.ATTRIBUTE) &&
                                member.getName().equals(fieldName)) {
                            found = true;

                            break;
                        }
                    }
                    */

                    if (!found) {
                        clz.addChild(co);
                    }
                }

                names.clear();
            }
        }
        
        // Globals
        if (globals != null) {
            List<String> sortedNames = new ArrayList<String>(globals.keySet());
            Collections.sort(sortedNames);

            for (String globalName : sortedNames) {
                GlobalAsgnNode global = globals.get(globalName);
                AstElement co = new AstNameElement(result, global, globalName,
                        ElementKind.GLOBAL);
                structure.add(co);
            }
            names.clear();
        }

        // Process access modifiers
        for (AstClassElement clz : haveAccessModifiers) {
            // There are "public", "protected" or "private" modifiers in the
            // document; we should scan it more carefully for these and
            // annotate them properly
            Set<Node> protectedMethods = new HashSet<Node>();
            Set<Node> privateMethods = new HashSet<Node>();
            AstUtilities.findPrivateMethods(clz.getNode(), protectedMethods, privateMethods);

            if (privateMethods.size() > 0) {
                // TODO: Annotate my structure elements appropriately
                for (Element o : methods) {
                    if (o instanceof AstMethodElement) {
                        AstMethodElement jn = (AstMethodElement)o;

                        if (privateMethods.contains(jn.getNode())) {
                            jn.setAccess(Modifier.PRIVATE);
                        }
                    }
                }

                // TODO: Private fields!
            }

            if (protectedMethods.size() > 0) {
                // TODO: Annotate my structure elements appropriately
                for (Element o : methods) {
                    if (o instanceof AstMethodElement) {
                        AstMethodElement jn = (AstMethodElement)o;

                        if (protectedMethods.contains(jn.getNode())) {
                            jn.setAccess(Modifier.PROTECTED);
                        }
                    }
                }

                // TODO: Protected fields!
            }
        }

        analysisResult.setElements(structure);
        analysisResult.setAttributes(attributes);
        analysisResult.setRequires(requires);

        return analysisResult;
    }

    private static final Map<File, AnalysisResult> cache = new HashMap<File, AnalysisResult>();

    private AnalysisResult getCachedAnalysis(final RubyParseResult result) {
        // TODO: implement together with #cacheAnalysis
        return null;
//        File file = result.getFile().getFile();
//        AnalysisResult cachedRestul = cache.get(file);
//        if (cachedRestul == null) {
//            return null;
//        } else {
//            return cachedRestul;
//        }
    }

    private void cacheAnalysis(RubyParseResult result, AnalysisResult scan) {
        // TODO: store in the cache, prune old result and/or time-out old results, ...
//        File file = result.getFile().getFile();
//        cache.put(file, scan);
    }

    public Map<String, List<OffsetRange>> folds(final ParserResult result) {
        if (RubyUtils.isRhtmlFile(RubyUtils.getFileObject(result))) {
            return Collections.emptyMap();
        }

        Node root = AstUtilities.getRoot(result);

        if (root == null) {
            return Collections.emptyMap();
        }

        RubyParseResult rpr = AstUtilities.getParseResult(result);
        AnalysisResult analysisResult = rpr.getStructure();

        Map<String,List<OffsetRange>> folds = new HashMap<String,List<OffsetRange>>();
        List<OffsetRange> codefolds = new ArrayList<OffsetRange>();
        folds.put("codeblocks", codefolds); // NOI18N

        try {
            BaseDocument doc = RubyUtils.getDocument(result);
            if (doc != null) {
                try {
                    doc.readLock(); // For Utilities.getRowStart access
                    addFolds(doc, analysisResult.getElements(), folds, codefolds);
                } finally {
                    doc.readUnlock();
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return folds;
    }
    
    private void addFolds(
            final BaseDocument doc,
            final List<? extends AstElement> elements,
            final Map<String,List<OffsetRange>> folds,
            final List<OffsetRange> codeblocks) throws BadLocationException {
        for (AstElement element : elements) {
            ElementKind kind = element.getKind();
            switch (kind) {
            case METHOD:
            case CONSTRUCTOR:
            case CLASS:
            case MODULE: {
                Node node = element.getNode();
                OffsetRange range = AstUtilities.getRange(node);
                // note: unlike for java, allowing also folding of
                // top level classes and modules - in ruby it's fairly common
                // to have several top level classes in one file (#140247)
                int start = range.getStart();
                // Start the fold at the END of the line
                start = org.netbeans.editor.Utilities.getRowEnd(doc, start);
                int end = range.getEnd();
                if (start != (-1) && end != (-1) && start < end && end <= doc.getLength()) {
                    range = new OffsetRange(start, end);
                    codeblocks.add(range);
                }
                break;
            }
            case TEST: {
                Node node = element.getNode();
                OffsetRange range = AstUtilities.getRange(node);

                int start = range.getStart();
                // Start the fold at the END of the line
                start = org.netbeans.editor.Utilities.getRowEnd(doc, start);
                int end = range.getEnd();
                if (start != (-1) && end != (-1) && start < end && end <= doc.getLength()) {
                    range = new OffsetRange(start, end);
                    codeblocks.add(range);
                }
                break;
            }
            }
            
            List<? extends AstElement> children = element.getChildren();
            if (children != null && children.size() > 0) {
                addFolds(doc, children, folds, codeblocks);
            }
        }
    }

    private void scan(
            final Node node,
            final AstPath path,
            String in,
            Set<String> includes,
            AstElement parent) {
        // Recursively search for methods or method calls that match the name and arity
        switch (node.getNodeType()) {
        case CLASSNODE: {
            AstClassElement co = new AstClassElement(result, node);
            co.setIn(in);

            String fqn = AstUtilities.getFqnName(path);
            co.setFqn(fqn);
            // Pass on to children
            in = AstUtilities.getClassOrModuleName((ClassNode)node);
            includes = new HashSet<String>();
            co.setIncludes(includes);

            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }

            parent = co;
            break;
        }
        case MODULENODE: {
            AstModuleElement co = new AstModuleElement(result, node);
            co.setIn(in);
            co.setFqn(AstUtilities.getFqnName(path));
            in = AstUtilities.getClassOrModuleName((ModuleNode)node);

            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }

            parent = co;

            // XXX Can I have includes on modules?
            
            break;
        }
        case SCLASSNODE: {
            // Singleton class, e.g.   class << self, or class << File, etc.
            AstClassElement co = new AstClassElement(result, node);
            co.setIn(in);
            co.setFqn(AstUtilities.getFqnName(path));

            // Pass on to children
            Node receiver = ((SClassNode)node).getReceiverNode();

            if (receiver instanceof INameNode) {
                in = AstUtilities.getName(receiver);
            } else {
                in = null;
            }

            includes = new HashSet<String>();
            co.setIncludes(includes);

            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }

            parent = co;
            
            break;
        }
        case DEFNNODE:
        case DEFSNODE: {
            AstMethodElement co = new AstMethodElement(result, node);
            methods.add(co);
            co.setIn(in);

            // "initialize" methods are private
            if ((node instanceof DefnNode) && "initialize".equals(AstUtilities.getName(node))) {
                co.setAccess(Modifier.PRIVATE);
            } else if ((parent != null) && parent.getNode() instanceof SClassNode) {
                // What about public/protected/private access?
                co.setModifiers(EnumSet.of(Modifier.STATIC));
            }

            // A module inclusion callback? These often (at least in Rails) call
            // extend() to insert the instance methods into the class
            if (node instanceof DefsNode &&
                    parent instanceof AstModuleElement &&
                    "included".equals(AstUtilities.getName(node))) { // NOI18N
                // Analyze the given method to see if it's doing a simple
                // base.extend(Whatever) in the included method.
                String extendWith = getExtendWith((DefsNode)node);
                if (extendWith != null) {
                    if (extendWith.indexOf(':') == -1) {
                        String fqn = AstUtilities.getFqnName(path);
                        extendWith = fqn + "::" + extendWith; // NOI18N
                    }
                    ((AstModuleElement)parent).setExtendWith(extendWith);
                }
            }

            if (node instanceof DefnNode || node instanceof DefsNode) {
                RubyType type = new RubyType();
                type.append(typeInferencer.inferType(node));
                co.setType(type);
            }

            // TODO - don't add this to the top level! Make a nested list
            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }
            
            break;
        }
        case CONSTDECLNODE: {
            ConstDeclNode constNode = (ConstDeclNode) node;

            AstElement co = new AstNameElement(result, node, AstUtilities.getName(node),
                    ElementKind.CONSTANT);

            co.setType(typeInferencer.inferTypesOfRHS(constNode));
            co.setIn(in);

            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }
            
            break;
        }
        case CLASSVARDECLNODE: {
            AstFieldElement co = new AstFieldElement(result, node);
            co.setIn(in);

            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }
            
            break;
        }

        case GLOBALASGNNODE: {
            // We don't have unique declarations, only assignments (possibly many)
            // so stash these in a map and extract unique fields when we're done
            if (globals == null) {
                globals = new HashMap<String, GlobalAsgnNode>();
            }
            GlobalAsgnNode global = (GlobalAsgnNode)node;
            globals.put(global.getName(), global);

            break;
        }

        case INSTASGNNODE: {
            if (parent instanceof AstClassElement) {
                // We don't have unique declarations, only assignments (possibly many)
                // so stash these in a map and extract unique fields when we're done
                Set<InstAsgnNode> assignments = fields.get(parent);

                if (assignments == null) {
                    assignments = new HashSet<InstAsgnNode>();
                    fields.put((AstClassElement)parent, assignments);
                }

                assignments.add((InstAsgnNode)node);
                
            }

            break;
        }
        case VCALLNODE: {
            String name = AstUtilities.getName(node);

            if (("private".equals(name) || "protected".equals(name)) &&
                    parent instanceof AstClassElement) { // NOI18N
                haveAccessModifiers.add((AstClassElement)parent);
            }
            
            break;
        }
        case LOCALASGNNODE: {
            // Only include variables at the top level
            if (parent == null && AstUtilities.findMethod(path) == null) {
                // Make sure we're not inside a method
                // TODO - avoid duplicates?

                String name = AstUtilities.getName(node);
                boolean found = false;
                for (AstElement child : structure) {
                    if (child.getKind() == ElementKind.VARIABLE && name.equals(child.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    AstElement co = new AstNameElement(result, node, name,
                            ElementKind.VARIABLE);
                    assert node instanceof LocalAsgnNode : "LocalAsgnNode expected";
                    co.setType(typeInferencer.inferTypesOfRHS(node));
                    co.setIn(in);
                    structure.add(co);
                }
            }
            
            break;
        }
        case FCALLNODE: {
            String name = AstUtilities.getName(node);

            if (name.equals("require")) { // XXX Load too?

                Node argsNode = ((FCallNode)node).getArgsNode();

                if (argsNode instanceof ListNode) {
                    ListNode args = (ListNode)argsNode;

                    if (args.size() > 0) {
                        Node n = args.get(0);

                        // For dynamically computed strings, we have n instanceof DStrNode
                        // but I can't handle these anyway
                        if (n instanceof StrNode) {
                            String require = ((StrNode)n).getValue();

                            if ((require != null) && (require.length() > 0)) {
                                requires.add(require.toString());
                            }
                        }
                    }
                }
            } else if ((includes != null) && name.equals("include")) {
                Node argsNode = ((FCallNode)node).getArgsNode();

                if (argsNode instanceof ListNode) {
                    ListNode args = (ListNode)argsNode;
                    for (Node n : args.childNodes()) {
                        if (n instanceof Colon2Node) {
                            includes.add(AstUtilities.getFqn((Colon2Node) n));
                        } else if (n instanceof INameNode) {
                            includes.add(AstUtilities.getName(n));
                        }
                    }
                }
            } else if (("private".equals(name) || "protected".equals(name)) &&
                    parent instanceof AstClassElement) { // NOI18N
                haveAccessModifiers.add((AstClassElement)parent);
            } else if (AstUtilities.isAttr(node)) {
                // TODO: Compute the symbols and check for equality
                // attr_reader, attr_accessor, attr_writer
                SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);

                if ((symbols != null) && (symbols.length > 0)) {
                    for (SymbolNode s : symbols) {
                        AstAttributeElement co = new AstAttributeElement(result, s, node);
                        
                        if (parent instanceof AstClassElement) {
                            Set<AstAttributeElement> attrsInClass = attributes.get(parent);

                            if (attrsInClass == null) {
                                attrsInClass = new HashSet<AstAttributeElement>();
                                attributes.put((AstClassElement)parent, attrsInClass);
                            }

                            attrsInClass.add(co);
                        }
                        
                        if (parent != null) {
                            parent.addChild(co);
                        } else {
                            structure.add(co);
                        }
                    }
                }
            } else if (name.equals("module_function")) { // NOI18N
                                                         // TODO: module_function without arguments will make all the following methods
                                                         // module function - is this common?

                Node argsNode = ((FCallNode)node).getArgsNode();

                if (argsNode instanceof ListNode) {
                    ListNode args = (ListNode)argsNode;

                    for (int j = 0, m = args.size(); j < m; j++) {
                        Node n = args.get(j);

                        if (n instanceof SymbolNode) {
                            String func = AstUtilities.getName(n);

                            if ((func != null) && (func.length() > 0)) {
                                // Find existing method
                                AstMethodElement method = null;

                                for (Element o : methods) {
                                    if (o instanceof AstMethodElement) {
                                        AstMethodElement jn = (AstMethodElement)o;

                                        if (func.equals(jn.getName())) {
                                            // TODO - some kind of arity comparison?
                                            method = jn;

                                            break;
                                        }
                                    }
                                }

                                if (method != null) {
                                    // Make a new static version of the named function
                                    Node dupeNode = method.getNode();
                                    AstMethodElement co = new AstMethodElement(result, dupeNode);
                                    co.setIn(in);

                                    // "initialize" methods are private
                                    if ((dupeNode instanceof DefnNode) &&
                                            "initialize".equals(AstUtilities.getName(dupeNode))) { // NOI18N
                                        co.setAccess(Modifier.PRIVATE);
                                    }

                                    // module_functions are static
                                    // What about public/protected/private access?
                                    co.setModifiers(EnumSet.of(Modifier.STATIC));

                                    // TODO - don't add this to the top level! Make a nested list
                                    if (parent != null) {
                                        parent.addChild(co);
                                    } else {
                                        structure.add(co);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isTestFile) {
                if (name.equals("test") || name.equals("describe") || // NOI18N
                        name.equals("specify") || name.equals("context") || // NOI18N
                        name.equals("should") || name.equals("it")) { // NOI18N
                    String desc = name;
                    FCallNode fc = (FCallNode)node;
                    if (fc.getIterNode() != null || "it".equals(name)) { // NOI18N   // "it" without do/end: pending
                        Node argsNode = fc.getArgsNode();

                        if (argsNode instanceof ListNode) {
                            ListNode args = (ListNode)argsNode;

                            // TODO handle
                            //  describe  ThingsController, "GET #index" do
                            // e.g. where the desc string is not first
                            for (int i = 0, max = args.size(); i < max; i++) {
                                Node n = args.get(i);

                                // For dynamically computed strings, we have n instanceof DStrNode
                                // but I can't handle these anyway
                                if (n instanceof StrNode) {
                                    String descBl = ((StrNode)n).getValue();

                                    if ((descBl != null) && (descBl.length() > 0)) {
                                        // No truncation? See 138259
                                        //desc = RubyUtils.truncate(descBl.toString(), MAX_RUBY_LABEL_LENGTH);
                                        desc = descBl;
                                        // Prepend the function type (unless it's test - see 138260
                                        if (!name.equals("test")) { // NOI18N
                                            desc = name+": " + desc; // NOI18N
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        AstElement co = new AstNameElement(result, node, desc,
                                ElementKind.TEST);

                        if (parent != null) {
                            parent.addChild(co);
                        } else {
                            structure.add(co);
                        }
                        parent = co;
                    }
                }
            }
            
            break;
        }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            path.descend(child);
            scan(child, path, in, includes, parent);
            path.ascend();
        }
    }

    /** Analyze the given method and see if it looks like the following
     * common pattern (at least in Rails) :
     * <pre>
     *       def self.included(base)
     *         base.extend(ClassMethods)
     *       end
     * </pre>
     * If it does, return the class whose methods is added - e.g. "ClassMethods"
     * in the above example.
     */
    private String getExtendWith(final MethodDefNode node) {
        // TODO Check that we have a single parameter,
        // and that the same parameter is the name of a single method
        // call; a CallNode, whose name is extend and whose single
        // argument is a LocalVarNode (the parameter node).
        // The parameter list should be an ArrayNode containing just
        // a ConstNode (look for FQNs here, could be a Colon2Node).
        List<String> argList = AstUtilities.getDefArgs(node, true);
        
        if (argList == null || argList.size() != 1) {
            return null;
        }
        String param = argList.get(0);
        
        CallNode call = findExtendCall(node);
        if (call == null) {
            return null;
        }
        
        Node receiver = call.getReceiverNode();
        if (receiver == null || !(receiver instanceof INameNode)) {
            return null;
        }
        
        String receiverName = AstUtilities.getName(receiver);
        if (!param.equals(receiverName)) {
            return null;
        }
        
        Node argsNode = call.getArgsNode();

        if (argsNode instanceof ListNode) {
            ListNode args = (ListNode)argsNode;

            if (args.size() == 1) {
                Node n = args.get(0);
                String rn = null;

                if (n instanceof Colon2Node) {
                    // TODO - check to see if we qualify
                    rn = AstUtilities.getName(n);
                } else if (n instanceof ConstNode) {
                    rn = AstUtilities.getName(n);
                }
                
                return rn;
            }
        }
        
        return null;
    }
    
    private CallNode findExtendCall(final Node node) {
        if (node instanceof CallNode) {
            CallNode call = (CallNode)node;
            
            if ("extend".equals(call.getName())) { // NOI18N
                return call;
            }
        }
        
        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            CallNode call = findExtendCall(child);
            
            if (call != null) {
                return call;
            }
        }
        
        return null;
    }

    private static Set<FileObject> currentlyAnalyzingWithIndex = new HashSet<FileObject>();

    AnalysisResult analyze(final RubyParseResult result) {
        AnalysisResult scan = getCachedAnalysis(result);
        if (scan != null) {
            return scan;
        }
        boolean addedWithIndex = false; // prevent stack-overflow
        FileObject toAnalyze = RubyUtils.getFileObject(result);
        try {
            addedWithIndex = currentlyAnalyzingWithIndex.add(toAnalyze);
            if (addedWithIndex && result != null) {
                this.index = RubyIndex.get(result);
            }
            this.result = result;
            scan = scan(result);
            cacheAnalysis(result, scan);
            return scan;
        } finally {
            if (addedWithIndex) {
                boolean removed = currentlyAnalyzingWithIndex.remove(toAnalyze);
                assert removed : "consistent state";
            }
        }
    }

    /** Look through the comment nodes and associate them with the AST nodes */
    public void addComments(final RubyParseResult result) {
        Node root = result.getRootNode();

        if (root == null) {
            return;
        }

        org.jrubyparser.parser.ParserResult r = result.getJRubyResult();

        // REALLY slow implementation
        List<CommentNode> comments = r.getCommentNodes();

        for (CommentNode comment : comments) {
            SourcePosition pos = comment.getPosition();
            int start = pos.getStartOffset();
            int end = pos.getEndOffset();
            Node node = findClosest(root, start, end);
            assert node != null;

            node.addComment(comment);
        }
    }

    private Node findClosest(final Node node, final int start, final int end) {
        List<Node> list = node.childNodes();

        SourcePosition pos = node.getPosition();

        if (end < pos.getStartOffset()) {
            return node;
        }

        if (start > pos.getEndOffset()) {
            return null;
        }

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            Node closest = findClosest(child, start, end);

            if (closest != null) {
                return closest;
            }
        }

        return null;
    }

    private class RubyStructureItem implements StructureItem {
        
        AstElement node;
        ElementKind kind;
        ParserResult result;

        private RubyStructureItem(AstElement node, ParserResult result) {
            this.node = node;
            this.result = result;

            kind = node.getKind();
        }

        public String getName() {
            return node.getName();
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(node.getName());

            if ((kind == ElementKind.METHOD) || (kind == ElementKind.CONSTRUCTOR)) {
                // Append parameters
                AstMethodElement jn = (AstMethodElement)node;

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
                }
            }

            RubyType type = node.getType();
            if (type.isKnown()) {
                formatter.appendHtml("<font color='#777777'>"); // NOI18N
                formatter.appendHtml(" : "); // NOI18N
                formatter.appendText(typeAsString(type));
                formatter.appendHtml("</font>"); // NOI18N
            }

            return formatter.getText();
        }

        private String typeAsString(final RubyType type) {
            String types = type.asString(", "); // NOI18N
            // TODO, should we really show the information about the type we are
            // not able to fully infer? Does it bother users or do they like it?
            if (type.hasUnknownMember()) {
                NbBundle.getMessage(RubyStructureAnalyzer.class, "RubyUnknownType");
                types += ", " + NbBundle.getMessage(RubyStructureAnalyzer.class, "RubyUnknownType");
            }
            return types;
        }

        public ElementHandle getElementHandle() {
            return node;
        }

        public ElementKind getKind() {
            return kind;
        }

        public Set<Modifier> getModifiers() {
            return node.getModifiers();
        }

        public boolean isLeaf() {
            switch (kind) {
            case ATTRIBUTE:
            case CONSTANT:
            case CONSTRUCTOR:
            case METHOD:
            case FIELD:
            case KEYWORD:
            case VARIABLE:
            case GLOBAL:
            case OTHER:
                return true;

            case MODULE:
            case CLASS:
                return false;

            case TEST: {
                List<AstElement> nested = node.getChildren();
                return nested == null || nested.size() == 0;
            }

            default:
                throw new RuntimeException("Unhandled kind: " + kind);
            }
        }

        public List<? extends StructureItem> getNestedItems() {
            List<AstElement> nested = node.getChildren();

            if ((nested != null) && (nested.size() > 0)) {
                List<RubyStructureItem> children = new ArrayList<RubyStructureItem>(nested.size());

                for (Element co : nested) {
                    children.add(new RubyStructureItem((AstElement)co, result));
                }

                return children;
            } else {
                return Collections.emptyList();
            }
        }

        public long getPosition() {
            return node.getNode().getPosition().getStartOffset();
        }

        public long getEndPosition() {
            return node.getNode().getPosition().getEndOffset();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (!(o instanceof RubyStructureItem)) {
                // System.out.println("- not a desc");
                return false;
            }

            RubyStructureItem d = (RubyStructureItem)o;

            if (kind != d.kind) {
                // System.out.println("- kind");
                return false;
            }

            if (!getName().equals(d.getName())) {
                // System.out.println("- name");
                return false;
            }

            if ((kind == ElementKind.METHOD) || (kind == ElementKind.CONSTRUCTOR)) {
                // consider also arity (#131134)
                Arity arity = Arity.getDefArity(node.getNode());
                Arity darity = Arity.getDefArity(d.node.getNode());
                if (!arity.equals(darity)) {
                    return false;
                }
                
                if (!getModifiers().equals(d.getModifiers())) {
                    return false;
                }

                // consider parameters names and thus their arity (issue 101508)
                List<String> parameters = ((AstMethodElement) node).getParameters();
                List<String> dparameters = ((AstMethodElement) d.node).getParameters();
                if (parameters == null) {
                    return dparameters == null;
                } else {
                    return parameters.equals(dparameters);
                }
            }

            //            if ( !this.elementHandle.signatureEquals(d.elementHandle) ) {
            //                return false;
            //            }

            /*
            if ( !modifiers.equals(d.modifiers)) {
                // E.println("- modifiers");
                return false;
            }
            */

            // System.out.println("Equals called");            
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = (29 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);
            hash = (29 * hash) + ((this.kind != null) ? this.kind.hashCode() : 0);

            if ((kind == ElementKind.METHOD) || (kind == ElementKind.CONSTRUCTOR)) {
                // consider also arity
                Arity arity = Arity.getDefArity(node.getNode());
                hash = 37 * hash + arity.hashCode();
            }

            // hash = 29 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return getName() + " (kind: " + kind + ')';
        }

        public ImageIcon getCustomIcon() {
            return null;
        }

        public String getSortText() {
            return getName();
        }
    }
    
    private List<? extends StructureItem> scanRhtml(ParserResult result) {
        List<RhtmlStructureItem> items = new ArrayList<RhtmlStructureItem>();
        AbstractDocument doc = RubyUtils.getDocument(result);
        if (doc == null) {
            return Collections.emptyList();
        }
        doc.readLock ();
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence ts = th.tokenSequence();
            if (ts == null) {
                return items;
            }

            ts.moveStart();
            while (ts.moveNext()) {
                TokenId id = ts.token().id();
                if (id.name().equals("DELIMITER")) {
                    int start = ts.offset();
                    if (ts.moveNext()) {
                        Token token = ts.token();
                        int end = ts.offset() + token.length();
                        if (!token.id().name().equals("DELIMITER")) {
                            while (ts.moveNext()) {
                                if (ts.token().id().name().equals("DELIMITER")) {
                                    end = ts.offset() + token.length();
                                    break;
                                }
                            }
                        }

                        String name = navigatorName((Document)doc, th, start);
                        items.add(new RhtmlStructureItem(name, start, end));
                    }
                }
            }
        } finally {
            doc.readUnlock ();
        }
        
        return items;
    }
    
    public static String navigatorName(Document doc, TokenHierarchy th, int offset) {
        TokenSequence ts = th.tokenSequence();
        ts.move(offset);
        if (ts.moveNext()) {
            TokenId id = ts.token().id();
            if (id.name().equals("DELIMITER")) {
                if (ts.moveNext()) {
                    id = ts.token().id();
                    if (id.name().startsWith("RUBY")) {
                        TokenSequence t = ts.embedded();
                        if (t != null) {
                            t.moveStart();
                            if (!t.moveNext()) {
                                return DEFAULT_LABEL;
                            }
                            while (t.token().id() == RubyTokenId.WHITESPACE) {
                                if (!t.moveNext()) {
                                    break;
                                }
                            }
                            int begin = t.offset();
                            id = t.token().id();

                            if (id == RubyTokenId.WHITESPACE) {
                                // Empty tag
                                return DEFAULT_LABEL;
                            }

                            // Treat <%h specially!
                            if (id == RubyTokenId.IDENTIFIER && TokenUtilities.equals(t.token().text(), "h")) { // NOI18N
                                if (!t.moveNext()) {
                                    // Just a <%h%>
                                    int end = t.offset() + t.token().length();
                                    return createName(doc, begin, end);
                                }
                                // Skip any whitespace after this one
                                while (t.token().id() == RubyTokenId.WHITESPACE) {
                                    if (!t.moveNext()) {
                                        break;
                                    }
                                }
                                id = t.token().id();
                            }

                            if (id == RubyTokenId.STRING_BEGIN || id == RubyTokenId.QUOTED_STRING_BEGIN || id == RubyTokenId.REGEXP_BEGIN) {
                                while (t.moveNext()) {
                                    id = t.token().id();
                                    if (id == RubyTokenId.STRING_END || id == RubyTokenId.QUOTED_STRING_END || id == RubyTokenId.REGEXP_END) {
                                        int end = t.offset() + t.token().length();

                                        return createName(doc, begin, end);
                                    }
                                }
                            }

                            int end = t.offset() + t.token().length();

                            // See if this is a "foo.bar" expression and if so, include ".bar"
                            if (t.moveNext()) {
                                TokenId newId = t.token().id();
                                if (newId == RubyTokenId.DOT || id == RubyTokenId.LPAREN) { // Also handle (expr)
                                    if (t.moveNext()) {
                                        end = t.offset() + t.token().length();
                                    }
                                }
                            }

                            return createName(doc, begin, end);
                        }
                    }
                }
            }
        }
//
//        // Fallback mechanism - just pull text out of the document
//        String content = createName(doc, offset, offset + leaf.getLength());
//        if (content.startsWith("<%= ")) { // NOI18N
//            // NOI18N
//            if (content.startsWith("<%= ")) { // NOI18N
//                content = content.substring(4);
//            } else {
//                content = content.substring(3);
//            }
//        } else if (content.startsWith("<%")) { // NOI18N
//            // NOI18N
//            if (content.startsWith("<% ")) { // NOI18N
//                content = content.substring(3);
//            } else {
//                content = content.substring(2);
//            }
//        }
//        if (content.endsWith("-%>")) { // NOI18N
//            content = content.substring(0, content.length() - 3);
//        } else if (content.endsWith("%>")) { // NOI18N
//            content = content.substring(0, content.length() - 2);
//        }
//        return content;
////        }
        return DEFAULT_LABEL;
    }

    /** Create label for a navigator item */
    private static String createName(Document doc, int begin, int end) {
        try {
            boolean truncated = false;
            int length = end - begin;
            if (begin + length > doc.getLength()) {
                length = doc.getLength() - begin;
                truncated = true;
            }
            if (length > MAX_RUBY_LABEL_LENGTH) {
                length = MAX_RUBY_LABEL_LENGTH;
                truncated = true;
            }
            String content = doc.getText(begin, length);
            int newline = content.indexOf('\n');
            if (newline != -1) {
                if (content.startsWith("<%\n") || content.startsWith("<%#\n")) {
                    content = content.substring(newline+1);
                    newline = content.indexOf('\n');
                    if (newline != -1) {
                        content = content.substring(0, newline);
                    }
                } else {
                    boolean startsWithNewline = true;
                    for (int i = 0; i < newline; i++) {
                        if (!Character.isWhitespace((content.charAt(i)))) {
                            startsWithNewline = false;
                            break;
                        }
                    }
                    if (startsWithNewline) {
                        content = content.substring(newline+1);
                    } else {
                        content = content.substring(0, newline);
                    }
                }
            }
            if (truncated) {
                return content + "..."; // NOI18N
            } else {
                return content;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return DEFAULT_LABEL;
    }
    
    public Configuration getConfiguration() {
        return null;
    }

    private class RhtmlStructureItem implements StructureItem {
        
        private final String name;
        private final int start;
        private final int end;

        public RhtmlStructureItem(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
        
        public String getName() {
            return name;
        }

        public String getHtml(HtmlFormatter formatter) {
            formatter.appendText(name);
            return formatter.getText();
        }

        public ElementHandle getElementHandle() {
            return null;
        }

        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return true;
        }

        public List<? extends StructureItem> getNestedItems() {
            return Collections.emptyList();
        }

        public long getPosition() {
            return start;
        }

        public long getEndPosition() {
            return end;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;

            hash = (29 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);

            // hash = 29 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return getName();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (!(o instanceof RubyStructureItem)) {
                // System.out.println("- not a desc");
                return false;
            }

            RubyStructureItem d = (RubyStructureItem)o;

            if (!getName().equals(d.getName())) {
                // System.out.println("- name");
                return false;
            }

            return true;
        }

        public ImageIcon getCustomIcon() {
            if (keywordIcon == null) {
                keywordIcon = ImageUtilities.loadImageIcon(RUBY_KEYWORD, false);
            }
            
            return keywordIcon;
        }
        
        public String getSortText() {
            return Integer.toHexString(10000+(int)getPosition());
        }
    }
    
    /** Number of characters to display from the Ruby fragments in the navigator */
    private static final int MAX_RUBY_LABEL_LENGTH = 30;
    /** Default label to use on navigator items where we don't have more accurate
     * information */
    private static final String DEFAULT_LABEL = "<% %>"; // NOI18N
}
