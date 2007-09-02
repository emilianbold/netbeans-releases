/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.jruby.ast.CallNode;

import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.CommentNode;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.SClassNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.parser.RubyParserResult;
import org.jruby.util.ByteList;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementHandle;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.HtmlFormatter;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.StructureItem;
import org.netbeans.api.gsf.StructureScanner;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.elements.AstAttributeElement;
import org.netbeans.modules.ruby.elements.AstClassElement;
import org.netbeans.modules.ruby.elements.AstConstantElement;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.AstFieldElement;
import org.netbeans.modules.ruby.elements.AstMethodElement;
import org.netbeans.modules.ruby.elements.AstModuleElement;
import org.openide.util.Exceptions;


/**
 * @todo Access modifiers
 * @todo Fields
 * @todo Statics
 * @todo Properties (attr_accessor etc.)
 * @todo Recognize specs in rspec files and list them separately?
 *
 * @todo Rewrite various other helper classes to use the scanned structure
 *   for the file instead of searching from scratch. For example, the code
 *   completion scanner should rely on the structure view to add local
 *   classes, fields and globals - it should only scan the current method
 *   for local variables. Similarly, the declaration finder should use it
 *   to locate local classes, method definitions and such. And obviously,
 *   the semantic analyzer should use it to find private methods.
 * @todo Scan rspec files and itemize it's and contexts
 *
 * @author Tor Norbye
 */
public class StructureAnalyzer implements StructureScanner {
    private Set<AstClassElement> haveAccessModifiers;
    private List<AstElement> structure;
    private Map<AstClassElement, Set<InstAsgnNode>> fields;
    private Set<String> requires;
    private List<AstMethodElement> methods;
    private Map<AstClassElement, Set<AstAttributeElement>> attributes;
    private HtmlFormatter formatter;
    private RubyParseResult result;

    public StructureAnalyzer() {
    }

    public List<?extends StructureItem> scan(CompilationInfo info, HtmlFormatter formatter) {
        this.result = (RubyParseResult)info.getParserResult();
        this.formatter = formatter;

        AnalysisResult ar = result.getStructure();
        List<?extends AstElement> elements = ar.getElements();
        List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        for (AstElement e : elements) {
            itemList.add(new RubyStructureItem(e, info));
        }

        return itemList;
    }
    
    public static class AnalysisResult {
        private List<?extends AstElement> elements;
        private Map<AstClassElement, Set<AstAttributeElement>> attributes;
        private Set<String> requires;
        
        private AnalysisResult() {
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

    private AnalysisResult scan(RubyParseResult result) {
        AnalysisResult analysisResult = new AnalysisResult();

        Node root = AstUtilities.getRoot(result);

        if (root == null) {
            return analysisResult;
        }

        structure = new ArrayList<AstElement>();
        fields = new HashMap<AstClassElement, Set<InstAsgnNode>>();
        attributes = new HashMap<AstClassElement, Set<AstAttributeElement>>();
        requires = new HashSet<String>();
        methods = new ArrayList<AstMethodElement>();
        haveAccessModifiers = new HashSet<AstClassElement>();

        
        AstPath path = new AstPath();
        path.descend(root);
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
                    AstFieldElement co = new AstFieldElement(field);
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

                    for (AstElement member : clz.getChildren()) {
                        if ((member.getKind() == ElementKind.ATTRIBUTE) &&
                                member.getName().equals(fieldName)) {
                            found = true;

                            break;
                        }
                    }

                    if (!found) {
                        clz.addChild(co);
                    }
                }

                names.clear();
            }
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

    public Map<String,List<OffsetRange>> folds(CompilationInfo info) {
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return Collections.emptyMap();
        }

        RubyParseResult rpr = (RubyParseResult)info.getParserResult();
        AnalysisResult analysisResult = rpr.getStructure();

        Map<String,List<OffsetRange>> folds = new HashMap<String,List<OffsetRange>>();
        List<OffsetRange> codefolds = new ArrayList<OffsetRange>();
        folds.put("codeblocks", codefolds); // NOI18N

        try {
            BaseDocument doc = (BaseDocument)info.getDocument();

            addFolds(doc, analysisResult.getElements(), folds, codefolds);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return folds;
    }
    
    private void addFolds(BaseDocument doc, List<? extends AstElement> elements, 
            Map<String,List<OffsetRange>> folds, List<OffsetRange> codeblocks) throws BadLocationException {
        for (AstElement element : elements) {
            switch (element.getKind()) {
            case CLASS:
            case MODULE:
                Node node = element.getNode();

                OffsetRange range = AstUtilities.getRange(node);
                // Only make nested classes/modules foldable, similar to what the java editor is doing
                if (range.getStart() > Utilities.getRowStart(doc, range.getStart())) {
                    codeblocks.add(range);
                }

                break;
            case METHOD:
            case CONSTRUCTOR:
                node = element.getNode();
                codeblocks.add(AstUtilities.getRange(node));

                break;
            }
            
            List<? extends AstElement> children = element.getChildren();
            if (children != null && children.size() > 0) {
                addFolds(doc, children, folds, codeblocks);
            }
        }
    }

    private void scan(Node node, AstPath path, String in, Set<String> includes, AstElement parent) {
        // Recursively search for methods or method calls that match the name and arity
        switch (node.nodeId) {
        case NodeTypes.CLASSNODE: {
            AstClassElement co = new AstClassElement(node);
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
        case NodeTypes.MODULENODE: {
            AstModuleElement co = new AstModuleElement(node);
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
        case NodeTypes.SCLASSNODE: {
            // Singleton class, e.g.   class << self, or class << File, etc.
            AstClassElement co = new AstClassElement(node);
            co.setIn(in);
            co.setFqn(AstUtilities.getFqnName(path));

            // Pass on to children
            Node receiver = ((SClassNode)node).getReceiverNode();

            if (receiver instanceof INameNode) {
                in = ((INameNode)receiver).getName();
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
        case NodeTypes.DEFNNODE:
        case NodeTypes.DEFSNODE: {
            AstMethodElement co = new AstMethodElement(node);
            methods.add(co);
            co.setIn(in);

            // "initialize" methods are private
            if ((node instanceof DefnNode) && "initialize".equals(((DefnNode)node).getName())) {
                co.setAccess(Modifier.PRIVATE);
            } else if ((parent != null) && parent.getNode() instanceof SClassNode) {
                // What about public/protected/private access?
                co.setModifiers(EnumSet.of(Modifier.STATIC));
            }
            
            // A module inclusion callback? These often (at least in Rails) call
            // extend() to insert the instance methods into the class
            if (node instanceof DefsNode &&
                    parent instanceof AstModuleElement &&
                    "included".equals(((DefsNode)node).getName())) { // NOI18N
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

            // TODO - don't add this to the top level! Make a nested list
            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }
            
            break;
        }
        case NodeTypes.CONSTDECLNODE: {
            AstConstantElement co = new AstConstantElement((ConstDeclNode)node);
            co.setIn(in);

            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }
            
            break;
        }
        case NodeTypes.CLASSVARDECLNODE: {
            AstFieldElement co = new AstFieldElement(node);
            co.setIn(in);

            if (parent != null) {
                parent.addChild(co);
            } else {
                structure.add(co);
            }
            
            break;
        }
        case NodeTypes.INSTASGNNODE: {
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
        case NodeTypes.VCALLNODE: {
            String name = ((INameNode)node).getName();

            if (("private".equals(name) || "protected".equals(name)) &&
                    parent instanceof AstClassElement) { // NOI18N
                haveAccessModifiers.add((AstClassElement)parent);
            }
            
            break;
        }
        case NodeTypes.FCALLNODE: {
            String name = ((INameNode)node).getName();

            if (name.equals("require")) { // XXX Load too?

                Node argsNode = ((FCallNode)node).getArgsNode();

                if (argsNode instanceof ListNode) {
                    ListNode args = (ListNode)argsNode;

                    if (args.size() > 0) {
                        Node n = args.get(0);

                        // For dynamically computed strings, we have n instanceof DStrNode
                        // but I can't handle these anyway
                        if (n instanceof StrNode) {
                            ByteList require = ((StrNode)n).getValue();

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

                    if (args.size() > 0) {
                        Node n = args.get(0);

                        if (n instanceof Colon2Node) {
                            includes.add(AstUtilities.getFqn((Colon2Node)n));
                        } else if (n instanceof INameNode) {
                            includes.add(((INameNode)n).getName());
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
                        AstAttributeElement co = new AstAttributeElement(s, node);
                        
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
                            String func = ((SymbolNode)n).getName();

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
                                    AstMethodElement co = new AstMethodElement(dupeNode);
                                    co.setIn(in);

                                    // "initialize" methods are private
                                    if ((dupeNode instanceof DefnNode) &&
                                            "initialize".equals(((DefnNode)dupeNode).getName())) { // NOI18N
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
            }
            
            break;
        }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        // This was a temporary bug in JRuby that has been fixed
        if (list == null) {
            return;
        }

        for (Node child : list) {
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
    private String getExtendWith(MethodDefNode node) {
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
        
        String receiverName = ((INameNode)receiver).getName();
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
                    rn = ((Colon2Node)n).getName();
                } else if (n instanceof ConstNode) {
                    rn = ((ConstNode)n).getName();
                }
                
                return rn;
            }
        }
        
        return null;
    }
    
    private CallNode findExtendCall(Node node) {
        if (node instanceof CallNode) {
            CallNode call = (CallNode)node;
            
            if ("extend".equals(call.getName())) { // NOI18N
                return call;
            }
        }
        
        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            CallNode call = findExtendCall(child);
            
            if (call != null) {
                return call;
            }
        }
        
        return null;
    }
    
    AnalysisResult analyze(RubyParseResult result) {
        return scan(result);
    }

    /** Look through the comment nodes and associate them with the AST nodes */
    public void addComments(RubyParseResult result) {
        Node root = result.getRootNode();

        if (root == null) {
            return;
        }

        RubyParserResult r = result.getJRubyResult();

        // REALLY slow implementation
        @SuppressWarnings("unchecked")
        List<CommentNode> comments = r.getCommentNodes();

        for (CommentNode comment : comments) {
            ISourcePosition pos = comment.getPosition();
            int start = pos.getStartOffset();
            int end = pos.getEndOffset();
            Node node = findClosest(root, start, end);
            assert node != null;

            node.addComment(comment);
        }
    }

    private Node findClosest(Node node, int start, int end) {
        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        ISourcePosition pos = node.getPosition();

        if (end < pos.getStartOffset()) {
            return node;
        }

        if (start > pos.getEndOffset()) {
            return null;
        }

        for (Node child : list) {
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
        CompilationInfo info;

        private RubyStructureItem(AstElement node, CompilationInfo info) {
            this.node = node;
            this.info = info;

            kind = node.getKind();
        }

        public String getName() {
            return node.getName();
        }

        public String getHtml() {
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

            return formatter.getText();
        }

        public ElementHandle<?extends Element> getElementHandle() {
            return info.getParser().createHandle(info, node);
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
            case OTHER:
                return true;

            case MODULE:
            case CLASS:
                return false;

            default:
                throw new RuntimeException("Unhandled kind: " + kind);
            }
        }

        public List<?extends StructureItem> getNestedItems() {
            List<AstElement> nested = node.getChildren();

            if ((nested != null) && (nested.size() > 0)) {
                List<RubyStructureItem> children = new ArrayList<RubyStructureItem>(nested.size());

                for (Element co : nested) {
                    children.add(new RubyStructureItem((AstElement)co, info));
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

            // hash = 29 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
