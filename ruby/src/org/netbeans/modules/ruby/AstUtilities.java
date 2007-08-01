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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgsCatNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.AssignableNode;
import org.jruby.ast.BlockNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.Colon3Node;
import org.jruby.ast.ConstNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.ForNode;
import org.jruby.ast.IScopingNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.util.ByteList;
import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.SourceFileReader;
import org.netbeans.api.gsf.SourceModel;
import org.netbeans.api.gsf.SourceModelFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.spi.gsf.DefaultParseListener;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;


/**
 *
 * @todo Create a NodePath abstraction, wrapping my ArrayList operation
 *   that I'm using here?
 *
 * @author Tor Norbye
 */
public class AstUtilities {
    /** Whether or not the prefixes for defs should be highlighted, e.g. in
     *   def HTTP.foo
     * Should "HTTP." be highlighted, or just the foo portion?
     */
    private static final boolean INCLUDE_DEFS_PREFIX = false;

    static int getAstOffset(CompilationInfo info, int lexOffset) {
        if (info.getEmbeddingModel() != null) {
            return info.getEmbeddingModel().sourceToGeneratedPos(info.getFileObject(), lexOffset);
        }
        
        return lexOffset;
    }

    /** This is a utility class only, not instantiatiable */
    private AstUtilities() {
    }

    /** Move to a generic (non-AST-oriented) utility class? */
    public static BaseDocument getBaseDocument(FileObject fileObject, boolean forceOpen) {
        DataObject dobj;

        try {
            dobj = DataObject.find(fileObject);

            EditorCookie ec = dobj.getCookie(EditorCookie.class);

            if (ec == null) {
                throw new IOException("Can't open " + fileObject.getNameExt());
            }

            Document document;

            if (forceOpen) {
                document = ec.openDocument();
            } else {
                document = ec.getDocument();
            }

            if (document instanceof BaseDocument) {
                return ((BaseDocument)document);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        return null;
    }

    /**
     * Get the rdoc documentation associated with the given node in the given document.
     * The node must have position information that matches the source in the document.
     */
    public static List<String> gatherDocumentation(CompilationInfo info, BaseDocument baseDoc, Node node) {
        LinkedList<String> comments = new LinkedList<String>();
        int elementBegin = node.getPosition().getStartOffset();
        if (info != null) {
            elementBegin = LexUtilities.getLexerOffset(info, elementBegin);
        }

        try {
            if (elementBegin >= baseDoc.getLength()) {
                return null;
            }

            // Search to previous lines, locate comments. Once we have a non-whitespace line that isn't
            // a comment, we're done

            int offset = Utilities.getRowStart(baseDoc, elementBegin);
            offset--;

            // Skip empty and whitespace lines
            while (offset >= 0) {
                // Find beginning of line
                offset = Utilities.getRowStart(baseDoc, offset);

                if (!Utilities.isRowEmpty(baseDoc, offset) &&
                        !Utilities.isRowWhite(baseDoc, offset)) {
                    break;
                }

                offset--;
            }

            if (offset < 0) {
                return null;
            }

            while (offset >= 0) {
                // Find beginning of line
                offset = Utilities.getRowStart(baseDoc, offset);

                if (Utilities.isRowEmpty(baseDoc, offset) || Utilities.isRowWhite(baseDoc, offset)) {
                    // Empty lines not allowed within an rdoc
                    break;
                }

                // This is a comment line we should include
                int lineBegin = Utilities.getRowFirstNonWhite(baseDoc, offset);
                int lineEnd = Utilities.getRowLastNonWhite(baseDoc, offset) + 1;
                String line = baseDoc.getText(lineBegin, lineEnd - lineBegin);

                // Tolerate "public", "private" and "protected" here --
                // Test::Unit::Assertions likes to put these in front of each
                // method.
                if (line.startsWith("#")) {
                    comments.addFirst(line);
                } else if ((comments.size() == 0) && line.startsWith("=end") &&
                        (lineBegin == Utilities.getRowStart(baseDoc, offset))) {
                    // It could be a =begin,=end document - see scanf.rb in Ruby lib for example. Treat this differently.
                    gatherInlineDocumentation(comments, baseDoc, offset);

                    return comments;
                } else if (line.equals("public") || line.equals("private") ||
                        line.equals("protected")) { // NOI18N
                                                    // Skip newlines back up to the comment
                    offset--;

                    while (offset >= 0) {
                        // Find beginning of line
                        offset = Utilities.getRowStart(baseDoc, offset);

                        if (!Utilities.isRowEmpty(baseDoc, offset) &&
                                !Utilities.isRowWhite(baseDoc, offset)) {
                            break;
                        }

                        offset--;
                    }

                    continue;
                } else {
                    // No longer in a comment
                    break;
                }

                // Previous line
                offset--;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return comments;
    }

    private static void gatherInlineDocumentation(LinkedList<String> comments,
        BaseDocument baseDoc, int offset) throws BadLocationException {
        // offset points to a line containing =end
        // Skip the =end list
        offset = Utilities.getRowStart(baseDoc, offset);
        offset--;

        // Search backwards in the document for the =begin (if any) and add all lines in reverse
        // order in between.
        while (offset >= 0) {
            // Find beginning of line
            offset = Utilities.getRowStart(baseDoc, offset);

            // This is a comment line we should include
            int lineBegin = offset;
            int lineEnd = Utilities.getRowEnd(baseDoc, offset);
            String line = baseDoc.getText(lineBegin, lineEnd - lineBegin);

            if (line.startsWith("=begin")) {
                // We're done!
                return;
            }

            comments.addFirst(line);

            // Previous line
            offset--;
        }
    }

    public static Node getForeignNode(final IndexedElement o, Node[] foreignRootRet) {
        ParserFile file = o.getFile();

        if (file == null) {
            return null;
        }

        List<ParserFile> files = Collections.singletonList(file);
        SourceFileReader reader =
            new SourceFileReader() {
                public CharSequence read(ParserFile file)
                    throws IOException {
                    Document doc = o.getDocument();

                    if (doc == null) {
                        return "";
                    }

                    try {
                        return doc.getText(0, doc.getLength());
                    } catch (BadLocationException ble) {
                        IOException ioe = new IOException();
                        ioe.initCause(ble);
                        throw ioe;
                    }
                }

                public int getCaretOffset(ParserFile fileObject) {
                    return -1;
                }
            };

        DefaultParseListener listener = new DefaultParseListener();
        new RubyParser().parseFiles(files, listener, reader);

        ParserResult result = listener.getParserResult();

        if (result == null) {
            return null;
        }

        Node root = AstUtilities.getRoot(result);

        if (root == null) {
            return null;
        } else if (foreignRootRet != null) {
            foreignRootRet[0] = root;
        }

        String signature = o.getSignature();

        if (signature == null) {
            return null;
        }

        Node node = AstUtilities.findBySignature(root, signature);

        // Special handling for "new" - these are synthesized from "initialize" methods
        if ((node == null) && "new".equals(o.getName())) {
            signature = signature.replaceFirst("new", "initialize");
            node = AstUtilities.findBySignature(root, signature);
        }

        return node;
    }

    public static int boundCaretOffset(CompilationInfo info, int caretOffset) {
        Document doc = null;

        try {
            doc = info.getDocument();
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

        // If you invoke code completion while indexing is in progress, the
        // completion job (which stores the caret offset) will be delayed until
        // indexing is complete - potentially minutes later. When the job
        // is finally run we need to make sure the caret position is still valid.
        int length = doc.getLength();

        if (caretOffset > length) {
            caretOffset = length;
        }

        return caretOffset;
    }

    /**
     * Return the set of requires that are defined in this AST
     * (no transitive closure though).
     */
    public static Set<String> getRequires(Node root) {
        Set<String> requires = new HashSet<String>();
        addRequires(root, requires);

        return requires;
    }

    private static void addRequires(Node node, Set<String> requires) {
        if (node instanceof FCallNode) {
            // A method call
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
            }
        } else if (node instanceof ModuleNode || node instanceof ClassNode ||
                node instanceof MethodDefNode) {
            // Only look for require statements at the top level
            return;
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            addRequires(child, requires);
        }
    }

    /** Locate the method of the given name and arity */
    public static MethodDefNode findMethod(Node node, String name, Arity arity) {
        // Recursively search for methods or method calls that match the name and arity
        if (node instanceof MethodDefNode && ((MethodDefNode)node).getName().equals(name)) {
            Arity defArity = Arity.getDefArity(node);

            if (Arity.matches(arity, defArity)) {
                return (MethodDefNode)node;
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            MethodDefNode match = findMethod(child, name, arity);

            if (match != null) {
                return match;
            }
        }

        return null;
    }

    public static MethodDefNode findMethodAtOffset(Node root, int offset) {
        AstPath path = new AstPath(root, offset);
        Iterator<Node> it = path.leafToRoot();

        while (it.hasNext()) {
            Node node = it.next();

            if (node instanceof MethodDefNode) {
                return (MethodDefNode)node;
            }
        }

        return null;
    }

    public static ClassNode findClassAtOffset(Node root, int offset) {
        AstPath path = new AstPath(root, offset);
        Iterator<Node> it = path.leafToRoot();

        while (it.hasNext()) {
            Node node = it.next();

            if (node instanceof ClassNode) {
                return (ClassNode)node;
            }
        }

        return null;
    }

    public static Node findLocalScope(Node node, AstPath path) {
        Node method = findMethod(path);

        if (method == null) {
            if (path.root() != null) {
                return path.root();
            }

            method = findBlock(path);
        }

        if (method == null) {
            method = path.leafParent();

            if (method instanceof NewlineNode) {
                method = path.leafGrandParent();
            }

            if (method == null) {
                method = node;
            }
        }

        return method;
    }

    public static Node findDynamicScope(Node node, AstPath path) {
        Node block = findBlock(path);

        if (block == null) {
            // Use parent
            block = path.leafParent();

            if (block == null) {
                block = node;
            }
        }

        return block;
    }

    public static Node findBlock(AstPath path) {
        // Find the closest block node enclosing the given node
        for (Node curr : path) {
            if (curr instanceof BlockNode || (curr instanceof IterNode && !(curr instanceof ForNode))) {
                return curr;
            }
        }

        return null;
    }

    public static MethodDefNode findMethod(AstPath path) {
        // Find the closest block node enclosing the given node
        for (Node curr : path) {
            if (curr instanceof MethodDefNode) {
                return (MethodDefNode)curr;
            }
        }

        return null;
    }

    // XXX Shouldn't this go in the REVERSE direction? I might find
    // a superclass here!
    // XXX What about SClassNode?
    public static ClassNode findClass(AstPath path) {
        // Find the closest block node enclosing the given node
        for (Node curr : path) {
            if (curr instanceof ClassNode) {
                return (ClassNode)curr;
            }
        }

        return null;
    }

    public static IScopingNode findClassOrModule(AstPath path) {
        // Find the closest block node enclosing the given node
        for (Node curr : path) {
            // XXX What about SClassNodes?
            if (curr instanceof ClassNode || curr instanceof ModuleNode) {
                return (IScopingNode)curr;
            }
        }

        return null;
    }

    public static boolean isCall(Node node) {
        return node instanceof FCallNode || node instanceof VCallNode || node instanceof CallNode;
    }
    
    public static String getCallName(Node node) {
        assert isCall(node);

        if (node instanceof INameNode) {
            return ((INameNode)node).getName();
        }
        assert false : node;

        return null;
    }

    public static String getDefName(Node node) {
        if (node instanceof MethodDefNode) {
            return ((MethodDefNode)node).getName();
        }
        assert false : node;

        return null;
    }
    
    public static ArgumentNode getDefNameNode(MethodDefNode node) {
        return node.getNameNode();
    }
    
    public static boolean isConstructorMethod(MethodDefNode node) {
        String name = node.getName();
        if (name.equals("new") || name.equals("initialize")) { // NOI18N
            return true;
        }
        
        return false;
    }

    /** Find the direct child which is an ArgsNode, and pick out the argument names
     * @param node The method definition node
     * @param namesOnly If true, return only the parameter names for rest args and
     *  blocks. If false, include "*" and "&".
     */
    @SuppressWarnings("unchecked")
    public static List<String> getDefArgs(MethodDefNode node, boolean namesOnly) {
        // TODO - do anything special about (&), blocks, argument lists (*), etc?
        List<Node> nodes = (List<Node>)node.childNodes();

        // TODO - use AstElement.getParameters?
        for (Node c : nodes) {
            if (c instanceof ArgsNode) {
                ArgsNode an = (ArgsNode)c;

                List<Node> args = (List<Node>)an.childNodes();
                List<String> parameters = new ArrayList<String>();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = (List<Node>)arg.childNodes();

                        for (Node arg2 : args2) {
                            if (arg2 instanceof ArgumentNode) {
                                String name = ((ArgumentNode)arg2).getName();
                                parameters.add(name);
                            } else if (arg2 instanceof LocalAsgnNode) {
                                String name = ((LocalAsgnNode)arg2).getName();
                                parameters.add(name);
                            }
                        }
                    }
                }

                // Rest args
                if (an.getRestArgNode() != null) {
                    String name = an.getRestArgNode().getName();

                    if (!namesOnly) {
                        name = "*" + name;
                    }

                    parameters.add(name);
                }
                

                // Block args
                if (an.getBlockArgNode() != null) {
                    String name = an.getBlockArgNode().getName();

                    if (!namesOnly) {
                        name = "&" + name;
                    }

                    parameters.add(name);
                }

                return parameters;
            }
        }

        return null;
    }

    public static String getDefSignature(MethodDefNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getDefName(node));

        List<String> args = getDefArgs(node, false);

        if ((args != null) && (args.size() > 0)) {
            sb.append('(');

            Iterator<String> it = args.iterator();
            sb.append(it.next());

            while (it.hasNext()) {
                sb.append(',');
                sb.append(it.next());
            }

            sb.append(')');
        }

        return sb.toString();
    }

    /**
     * Look for the caret offset in the parameter list; return the
     * index of the parameter that contains it.
     */
    @SuppressWarnings("unchecked")
    public static int findArgumentIndex(Node node, int offset) {
        if (node instanceof FCallNode) {
            Node argsNode = ((FCallNode)node).getArgsNode();

            return findArgumentIndex(argsNode, offset);
        } else if (node instanceof CallNode) {
            Node argsNode = ((CallNode)node).getArgsNode();

            return findArgumentIndex(argsNode, offset);
        } else if (node instanceof ArgsCatNode) {
            ArgsCatNode acn = (ArgsCatNode)node;

            int index = findArgumentIndex(acn.getFirstNode(), offset);

            if (index != -1) {
                return index;
            }

            index = findArgumentIndex(acn.getSecondNode(), offset);

            if (index != -1) {
                // Add in arg count on the left
                return getConstantArgs(acn) + index;
            }

            ISourcePosition pos = node.getPosition();

            if ((offset >= pos.getStartOffset()) && (offset <= pos.getEndOffset())) {
                return getConstantArgs(acn);
            }
        } else if (node instanceof ListNode) {
            List<Node> children = node.childNodes();

            int prevEnd = Integer.MAX_VALUE;

            for (int index = 0; index < children.size(); index++) {
                Node child = children.get(index);
                ISourcePosition pos = child.getPosition();

                if ((offset <= pos.getEndOffset()) &&
                        ((offset >= prevEnd) || (offset >= pos.getStartOffset()))) {
                    return index;
                }

                prevEnd = pos.getEndOffset();
            }

            // Caret -inside- empty parentheses?
            ISourcePosition pos = node.getPosition();

            if ((offset > pos.getStartOffset()) && (offset < pos.getEndOffset())) {
                return 0;
            }
        } else {
            ISourcePosition pos = node.getPosition();

            if ((offset >= pos.getStartOffset()) && (offset <= pos.getEndOffset())) {
                return 0;
            }
        }

        return -1;
    }

    /** Utility method used by findArgumentIndex: count the constant number of
     * arguments in a parameter list before the argscatnode */
    private static int getConstantArgs(ArgsCatNode acn) {
        Node node = acn.getFirstNode();

        if (node instanceof ListNode) {
            List children = node.childNodes();

            return children.size();
        } else {
            return 1;
        }
    }

    /**
     * Return true iff the given call note can be considered a valid call of the given method.
     */
    public static boolean isCallFor(Node call, Arity callArity, Node method) {
        assert isCall(call);
        assert method instanceof MethodDefNode;

        // Simple call today...
        return getDefName(method).equals(getCallName(call)) &&
        Arity.matches(callArity, Arity.getDefArity(method));
    }
    
    // TODO: use the structure analyzer data for more accurate traversal?
    /** For the given signature, locating the corresponding Node within the tree that
     * it corresponds to */
    public static Node findBySignature(Node root, String signature) {
        //String name = signature.split("(::)")
        // Find next name we're looking for
        String name = getNextSigComponent(signature);
        signature = signature.substring(name.length());

        return findBySignature(root, signature, name);
    }

    // For a signature of the form Foo::Bar#baz(arg1,arg2,...)
    // pull out the next component; in the above, successively return
    // "Foo", "Bar", "baz", etc.
    private static String getNextSigComponent(String signature) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int n = signature.length();

        // Skip leading separators
        for (; i < n; i++) {
            char c = signature.charAt(i);

            if ((c == '#') || (c == ':') || (c == '(')) {
                continue;
            }

            break;
        }

        // Add the name
        for (; i < n; i++) {
            char c = signature.charAt(i);

            if ((c == '#') || (c == ':') || (c == '(')) {
                break;
            }

            sb.append(c);
        }

        return sb.toString();
    }

    private static Node findBySignature(Node node, String signature, String name) {
        boolean lookingForMethod = (Character.isLowerCase(name.charAt(0)));

        if (lookingForMethod) {
            if ((node instanceof MethodDefNode) && name.equals(AstUtilities.getDefName(node))) {
                // See if the parameter list matches
                // XXX TODO
                List<String> parameters = getDefArgs((MethodDefNode)node, false);

                if ((signature.length() == 0) &&
                        ((parameters == null) || (parameters.size() == 0))) {
                    // No args
                    return node;
                } else if (signature.length() != 0) {
                    assert signature.charAt(0) == '(';

                    String argList = signature.substring(1, signature.length() - 1);
                    String[] args = argList.split(",");

                    if (args.length == parameters.size()) {
                        // Should I enforce equality here?
                        boolean equal = true;

                        for (int i = 0; i < args.length; i++) {
                            if (!args[i].equals(parameters.get(i))) {
                                equal = false;

                                break;
                            }
                        }

                        if (equal) {
                            return node;
                        }
                    }
                }
            } else if (isAttr(node)) {
                SymbolNode[] symbols = getAttrSymbols(node);
                for (SymbolNode sym : symbols) {
                    if (name.equals(sym.getName())) {
                        return node;
                    }
                }
            }
        } else if ((node instanceof ModuleNode || node instanceof ClassNode)) {
            Colon3Node c3n = ((IScopingNode)node).getCPath();

            if (c3n instanceof Colon2Node) {
                String fqn = getFqn((Colon2Node)c3n);

                if (fqn.startsWith(name) && signature.startsWith(fqn.substring(name.length()))) {
                    signature = signature.substring(fqn.substring(name.length()).length());
                    name = getNextSigComponent(signature);

                    if (name.length() == 0) {
                        // The signature points to a class (or module) - just return it
                        return node;
                    }

                    int index = signature.indexOf(name);
                    assert index != -1;
                    signature = signature.substring(index + name.length());
                }
            } else if (name.equals(AstUtilities.getClassOrModuleName(((IScopingNode)node)))) {
                name = getNextSigComponent(signature);

                if (name.length() == 0) {
                    // The signature points to a class (or module) - just return it
                    return node;
                }

                int index = signature.indexOf(name);
                assert index != -1;
                signature = signature.substring(index + name.length());
            }
        } else if (node instanceof SClassNode) {
            Node receiver = ((SClassNode)node).getReceiverNode();
            String rn = null;

            if (receiver instanceof Colon2Node) {
                // TODO - check to see if we qualify
                rn = ((Colon2Node)receiver).getName();
            } else if (receiver instanceof ConstNode) {
                rn = ((ConstNode)receiver).getName();
            } // else: some other type of singleton class definition, like class << foo

            if (rn != null) {
                if (name.equals(rn)) {
                    name = getNextSigComponent(signature);

                    if (name.length() == 0) {
                        // The signature points to a class (or module) - just return it
                        return node;
                    }

                    int index = signature.indexOf(name);
                    assert index != -1;
                    signature = signature.substring(index + name.length());
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            Node match = findBySignature(child, signature, name);

            if (match != null) {
                return match;
            }
        }

        return null;
    }

    /** Return true iff the given node contains the given offset */
    public static boolean containsOffset(Node node, int offset) {
        ISourcePosition pos = node.getPosition();

        return ((offset >= pos.getStartOffset()) && (offset <= pos.getEndOffset()));
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    public static OffsetRange getRange(Node node) {
        ISourcePosition pos = node.getPosition();
        OffsetRange range = new OffsetRange(pos.getStartOffset(), pos.getEndOffset());

        return range;
    }

    /**
     * Return a range that matches the lvalue for an assignment. The node must be namable.
     */
    public static OffsetRange getLValueRange(AssignableNode node) {
        assert node instanceof INameNode;

        ISourcePosition pos = node.getPosition();
        OffsetRange range =
            new OffsetRange(pos.getStartOffset(),
                pos.getStartOffset() + ((INameNode)node).getName().length());

        return range;
    }

    public static OffsetRange getNameRange(Node node) {
        if (node instanceof AssignableNode) {
            return getLValueRange((AssignableNode)node);
        } else if (node instanceof MethodDefNode) {
            return getFunctionNameRange(node);
        } else if (isCall(node)) {
            return getCallRange(node);
        } else if (node instanceof ClassNode) {
            // TODO - try to pull out the constnode or colon2node holding the class name,
            // and return it!
            Colon3Node c3n = ((ClassNode)node).getCPath();
            if (c3n != null) {
                return getRange(c3n);
            } else {
                return getRange(node);
            }
        } else if (node instanceof ModuleNode) {
            // TODO - try to pull out the constnode or colon2node holding the class name,
            // and return it!
            Colon3Node c3n = ((ModuleNode)node).getCPath();
            if (c3n != null) {
                return getRange(c3n);
            } else {
                return getRange(node);
            }
//        } else if (node instanceof SClassNode) {
//            // TODO - try to pull out the constnode or colon2node holding the class name,
//            // and return it!
//            Colon3Node c3n = ((SClassNode)node).getCPath();
//            if (c3n != null) {
//                return getRange(c3n);
//            } else {
//                return getRange(node);
//            }
        } else {
            return getRange(node);
        }
    }
    
    /** For CallNodes, the offset range for the AST node includes the entire parameter list.
     *  We want ONLY the actual call/operator name. So compute that on our own.
     */
    public static OffsetRange getCallRange(Node node) {
        ISourcePosition pos = node.getPosition();
        int start = pos.getStartOffset();
        int end = pos.getEndOffset();
        assert isCall(node);
        assert node instanceof INameNode;

        if (node instanceof CallNode) {
            // A call of the form Foo.bar. "bar" is the CallNode, "Foo" is the ReceiverNode.
            // Here I'm only handling named nodes; there may be others
            Node receiver = ((CallNode)node).getReceiverNode();

            if (receiver != null) {
                start = receiver.getPosition().getEndOffset() + 1; // end of "Foo::bar" + "."
            }
        }

        if (node instanceof INameNode) {
            end = start + ((INameNode)node).getName().length();
        }

        return new OffsetRange(start, end);
    }

    @SuppressWarnings("unchecked")
    public static OffsetRange getFunctionNameRange(Node node) {
        // TODO - enforce MethodDefNode and call getNameNode on it!
        for (Node child : (List<Node>)node.childNodes()) {
            if (child instanceof ArgumentNode) {
                OffsetRange range = AstUtilities.getRange(child);

                return range;
            }
        }

        if (node instanceof MethodDefNode) {
            for (Node child : (List<Node>)node.childNodes()) {
                if (child instanceof ConstNode) {
                    ISourcePosition pos = child.getPosition();
                    int end = pos.getEndOffset();
                    int start;

                    if (INCLUDE_DEFS_PREFIX) {
                        start = pos.getStartOffset();
                    } else {
                        start = end + 1;
                    }

                    // TODO - look at the source buffer and tweak offset if it's wrong
                    // This assumes we have a single constant node, followed by a dot, followed by the name
                    end = end + 1 + AstUtilities.getDefName(node).length(); // +1: "."

                    OffsetRange range = new OffsetRange(start, end);

                    return range;
                }
            }
        }

        return OffsetRange.NONE;
    }

    /**
     * Return the OffsetRange for an AliasNode that represents the new name portion.
     */
    public static OffsetRange getAliasNewRange(AliasNode node) {
        // XXX I don't know where the old and new names are since the user COULD
        // have used more than one whitespace character for separation. For now I'll
        // just have to assume it's the normal case with one space:  alias new old.
        // I -could- use the getPosition.getEndOffset() to see if this looks like it's
        // the case (e.g. node length != "alias ".length + old.length+new.length+1).
        // In this case I could go peeking in the source buffer to see where the
        // spaces are - between alias and the first word or between old and new. XXX.
        ISourcePosition pos = node.getPosition();

        int newStart = pos.getStartOffset() + 6; // 6: "alias ".length()

        return new OffsetRange(newStart, newStart + node.getNewName().length());
    }

    /**
     * Return the OffsetRange for an AliasNode that represents the old name portion.
     */
    public static OffsetRange getAliasOldRange(AliasNode node) {
        // XXX I don't know where the old and new names are since the user COULD
        // have used more than one whitespace character for separation. For now I'll
        // just have to assume it's the normal case with one space:  alias new old.
        // I -could- use the getPosition.getEndOffset() to see if this looks like it's
        // the case (e.g. node length != "alias ".length + old.length+new.length+1).
        // In this case I could go peeking in the source buffer to see where the
        // spaces are - between alias and the first word or between old and new. XXX.
        ISourcePosition pos = node.getPosition();

        int oldStart = pos.getStartOffset() + 6 + node.getNewName().length() + 1; // 6: "alias ".length; 1: " ".length

        return new OffsetRange(oldStart, oldStart + node.getOldName().length());
    }

    public static String getClassOrModuleName(IScopingNode node) {
        return ((INameNode)node.getCPath()).getName();
    }

    public static List<ClassNode> getClasses(Node root) {
        // I would like to use a visitor for this, but it's not
        // working - I get NPE's within DefaultIteratorVisitor
        // on valid ASTs, and I see it's not used heavily in JRuby,
        // so I'm not doing it this way for now.
        //final List<ClassNode> classes = new ArrayList<ClassNode>();
        //// There could be multiple Class definitions for this
        //// same class, and (empirically) rdoc shows the documentation
        //// for the last declaration.
        //NodeVisitor findClasses = new AbstractVisitor() {
        //    public Instruction visitClassNode(ClassNode node) {
        //        classes.add(node);
        //        return visitNode(node);
        //    }
        //
        //    protected Instruction visitNode(Node iVisited) {
        //        return null;
        //    }
        //};
        //new DefaultIteratorVisitor(findClasses).visitRootNode((RootNode)parseResult.getRootNode());
        List<ClassNode> classes = new ArrayList<ClassNode>();
        addClasses(root, classes);

        return classes;
    }

    private static void addClasses(Node node, List<ClassNode> classes) {
        if (node instanceof ClassNode) {
            classes.add((ClassNode)node);
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            addClasses(child, classes);
        }
    }

    private static void addAncestorParents(Node node, StringBuilder sb) {
        if (node instanceof Colon2Node) {
            Colon2Node c2n = (Colon2Node)node;
            addAncestorParents(c2n.getLeftNode(), sb);

            if ((sb.length() > 0) && (sb.charAt(sb.length() - 1) != ':')) {
                sb.append("::");
            }

            sb.append(c2n.getName());
        } else if (node instanceof INameNode) {
            if ((sb.length() > 0) && (sb.charAt(sb.length() - 1) != ':')) {
                sb.append("::");
            }

            sb.append(((INameNode)node).getName());
        }
    }

    public static String getFqn(Colon2Node c2n) {
        StringBuilder sb = new StringBuilder();

        addAncestorParents(c2n, sb);

        return sb.toString();
    }

    public static String getSuperclass(ClassNode clz) {
        StringBuilder sb = new StringBuilder();

        if (clz.getSuperNode() != null) {
            addAncestorParents(clz.getSuperNode(), sb);

            return sb.toString();
        }

        return null;
    }

    /** Compute the module/class name for the given node path */
    public static String getFqnName(AstPath path) {
        StringBuilder sb = new StringBuilder();

        Iterator<Node> it = path.rootToLeaf();

        while (it.hasNext()) {
            Node node = it.next();

            if (node instanceof ModuleNode || node instanceof ClassNode) {
                Colon3Node cpath = ((IScopingNode)node).getCPath();

                if (cpath == null) {
                    continue;
                }

                if (sb.length() > 0) {
                    sb.append("::"); // NOI18N
                }

                if (cpath instanceof Colon2Node) {
                    sb.append(getFqn((Colon2Node)cpath));
                } else {
                    sb.append(cpath.getName());
                }
            }
        }

        return sb.toString();
    }

    public static boolean isAttr(Node node) {
        if (!(node instanceof FCallNode)) {
            return false;
        }

        String name = ((INameNode)node).getName();

        if (name.startsWith("attr")) { // NOI18N

            if ("attr".equals(name) || "attr_reader".equals(name) || // NOI18N
                    "attr_accessor".equals(name) || "attr_writer".equals(name) || // NOI18N
                                                                                      // Rails: Special definitions which builds methods that have actual fields
                                                                                      // backing the attribute. Important to include these since they're
                                                                                      // used for key Rails members like headers, session, etc.
                    "attr_internal".equals(name) || "attr_internal_reader".equals(name) ||
                    "attr_internal_writer".equals(name) || // NOI18N
                    "attr_internal_accessor".equals(name)) { // NOI18N

                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static SymbolNode[] getAttrSymbols(Node node) {
        assert isAttr(node);

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child instanceof ListNode) {
                List<Node> symbols = (List<Node>)child.childNodes();
                List<SymbolNode> symbolList = new ArrayList<SymbolNode>(symbols.size());

                for (Node symbol : symbols) {
                    if (symbol instanceof SymbolNode) {
                        symbolList.add((SymbolNode)symbol);
                    }
                }

                return symbolList.toArray(new SymbolNode[symbolList.size()]);
            }
        }

        return new SymbolNode[0];
    }

    // TODO use this from all the various places that have this inlined...
    public static Node getRoot(CompilationInfo info) {
        ParserResult result = info.getParserResult();

        if (result == null) {
            return null;
        }

        return getRoot(result);
    }

    public static Node getRoot(ParserResult r) {
        assert r instanceof RubyParseResult;

        RubyParseResult result = (RubyParseResult)r;

        // TODO - just call result.getRoot()
        // but I might have to compensate for the new RootNode behavior in JRuby
        ParserResult.AstTreeNode ast = result.getAst();

        if (ast == null) {
            return null;
        }

        Node root = (Node)ast.getAstNode();

        return root;
    }

    /**
     * Get the private and protected methods in the given class
     */
    public static void findPrivateMethods(Node clz, Set<Node> protectedMethods,
        Set<Node> privateMethods) {
        Set<String> publicMethodSymbols = new HashSet<String>();
        Set<String> protectedMethodSymbols = new HashSet<String>();
        Set<String> privateMethodSymbols = new HashSet<String>();
        Set<Node> publicMethods = new HashSet<Node>();

        @SuppressWarnings("unchecked")
        List<Node> list = clz.childNodes();

        Modifier access = Modifier.PUBLIC;

        for (Node child : list) {
            access = getMethodAccess(child, access, publicMethodSymbols, protectedMethodSymbols,
                    privateMethodSymbols, publicMethods, protectedMethods, privateMethods);
        }

        // Can't just return private methods directly, since sometimes you can
        // specify that a particular method is public before we know about it,
        // so I can't just remove it from the known private list when I see the
        // access modifier
        privateMethodSymbols.removeAll(publicMethodSymbols);
        protectedMethodSymbols.removeAll(publicMethodSymbols);

        // Should I worry about private foo;  protected :foo ?
        // Seems unlikely somebody would do that... I guess
        // I could do privateMethodSymbols.removeAll(protectedMethodSymbols) etc.
        //privateMethodSymbols.removeAll(protectedMethodSymbols);
        //protectedMethodSymbols.removeAll(privateMethodSymbols);

        // Add all methods known to be private into the private node set
        for (String name : privateMethodSymbols) {
            for (Node n : publicMethods) {
                if (name.equals(AstUtilities.getDefName(n))) {
                    privateMethods.add(n);
                }
            }
        }

        for (String name : protectedMethodSymbols) {
            for (Node n : publicMethods) {
                if (name.equals(AstUtilities.getDefName(n))) {
                    protectedMethods.add(n);
                }
            }
        }
    }

    /**
     * @todo Should I really recurse into classes? If I have nested classes private
     *  methods ther shouldn't be included for the parent!
     *
     * @param access The "current" known access level (PUBLIC, PROTECTED or PRIVATE)
     * @return the access level to continue with at this syntactic level
     */
    @SuppressWarnings("unchecked")
    private static Modifier getMethodAccess(Node node, Modifier access,
        Set<String> publicMethodSymbols, Set<String> protectedMethodSymbols,
        Set<String> privateMethodSymbols, Set<Node> publicMethods, Set<Node> protectedMethods,
        Set<Node> privateMethods) {
        if (node instanceof MethodDefNode) {
            if (access == Modifier.PRIVATE) {
                privateMethods.add(node);
            } else if (access == Modifier.PUBLIC) {
                publicMethods.add(node);
            } else if (access == Modifier.PROTECTED) {
                protectedMethods.add(node);
            }

            // XXX Can I have nested method definitions? If so I may have to continue here
            return access;
        } else if (node instanceof VCallNode || node instanceof FCallNode) {
            String name = ((INameNode)node).getName();

            if ("private".equals(name)) {
                // TODO - see if it has arguments, if it does - it's just a single
                // method defined to be private
                // Iterate over arguments and add symbols...
                if (Arity.callHasArguments(node)) {
                    List<Node> params = (List<Node>)node.childNodes();

                    for (Node param : params) {
                        if (param instanceof ListNode) {
                            List<Node> params2 = (List<Node>)param.childNodes();

                            for (Node param2 : params2) {
                                if (param2 instanceof SymbolNode) {
                                    String symbol = ((SymbolNode)param2).getName();
                                    privateMethodSymbols.add(symbol);
                                }
                            }
                        }
                    }
                } else {
                    access = Modifier.PRIVATE;
                }

                return access;
            } else if ("protected".equals(name)) {
                // TODO - see if it has arguments, if it does - it's just a single
                // method defined to be private
                // Iterate over arguments and add symbols...
                if (Arity.callHasArguments(node)) {
                    List<Node> params = (List<Node>)node.childNodes();

                    for (Node param : params) {
                        if (param instanceof ListNode) {
                            List<Node> params2 = (List<Node>)param.childNodes();

                            for (Node param2 : params2) {
                                if (param2 instanceof SymbolNode) {
                                    String symbol = ((SymbolNode)param2).getName();
                                    protectedMethodSymbols.add(symbol);
                                }
                            }
                        }
                    }
                } else {
                    access = Modifier.PROTECTED;
                }

                return access;
            } else if ("public".equals(name)) {
                if (!Arity.callHasArguments(node)) {
                    access = Modifier.PUBLIC;

                    return access;
                } else {
                    List<Node> params = (List<Node>)node.childNodes();

                    for (Node param : params) {
                        if (param instanceof ListNode) {
                            List<Node> params2 = (List<Node>)param.childNodes();

                            for (Node param2 : params2) {
                                if (param2 instanceof SymbolNode) {
                                    String symbol = ((SymbolNode)param2).getName();
                                    publicMethodSymbols.add(symbol);
                                }
                            }
                        }
                    }
                }
            }

            return access;
        } else if (node instanceof ClassNode || node instanceof ModuleNode) {
            return access;
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            access = getMethodAccess(child, access, publicMethodSymbols, protectedMethodSymbols,
                    privateMethodSymbols, publicMethods, protectedMethods, privateMethods);
        }

        return access;
    }
    
    /**
     * Get the method name for the given offset - or null if it cannot be found. This
     * will initiate a new parse job if necessary.
     */
    public static String getMethodName(FileObject fo, final int offset) {
        SourceModel js = SourceModelFactory.getInstance().getModel(fo);

        if (js == null) {
            return null;
        }
        
        if (js.isScanInProgress()) {
            return null;
        }

        final String[] result = new String[1];

        try {
            js.runUserActionTask(new CancellableTask<CompilationInfo>() {
                    public void cancel() {
                    }

                    public void run(CompilationInfo info) {
                        org.jruby.ast.Node root = AstUtilities.getRoot(info);

                        if (root == null) {
                            return;
                        }

                        org.jruby.ast.MethodDefNode method =
                            AstUtilities.findMethodAtOffset(root, offset);

                        if (method == null) {
                            // It's possible the user had the caret on a line
                            // that includes a method that isn't actually inside
                            // the method block - such as the beginning of the
                            // "def" line, or the end of a line after "end".
                            // The latter isn't very likely, but the former can
                            // happen, so let's check the method bodies at the
                            // end of the current line
                            try {
                                BaseDocument doc = (BaseDocument)info.getDocument();
                                int endOffset = Utilities.getRowEnd(doc, offset);

                                if (endOffset != offset) {
                                    method = AstUtilities.findMethodAtOffset(root, endOffset);
                                }
                            } catch (BadLocationException ble) {
                                Exceptions.printStackTrace(ble);
                            } catch (IOException ioe) {
                                Exceptions.printStackTrace(ioe);
                            }
                        }

                        if (method != null) {
                            result[0] = method.getName();
                        }
                    }
                }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        return result[0];
    }

    public static int findOffset(FileObject fo, final String methodName) {
        SourceModel js = SourceModelFactory.getInstance().getModel(fo);

        if (js == null) {
            return -1;
        }
        
        if (js.isScanInProgress()) {
            return -1;
        }

        final int[] result = new int[1];
        result[0] = -1;

        try {
            js.runUserActionTask(new CancellableTask<CompilationInfo>() {
                    public void cancel() {
                    }

                    public void run(CompilationInfo info) {
                        org.jruby.ast.Node root = AstUtilities.getRoot(info);

                        if (root == null) {
                            return;
                        }

                        org.jruby.ast.Node method =
                            AstUtilities.findMethod(root, methodName, Arity.UNKNOWN);

                        if (method != null) {
                            int startOffset = method.getPosition().getStartOffset();
                            result[0] = startOffset;
                        }
                    }
                }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        return result[0];
    }
}
