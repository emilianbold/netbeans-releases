/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jrubyparser.ast.AliasNode;
import org.jrubyparser.ast.ArgsCatNode;
import org.jrubyparser.ast.ArgsNode;
import org.jrubyparser.ast.ArgumentNode;
import org.jrubyparser.ast.AssignableNode;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.Colon3Node;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.IScopingNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.MultipleAsgnNode;
import org.jrubyparser.ast.NewlineNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.SClassNode;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.SymbolNode;
import org.jrubyparser.ast.VCallNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.SourcePosition;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedField;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Various utilities for operating on the JRuby ASTs that are used
 * elsewhere.
 * 
 * @todo Rewrite many of the custom recursion routines to simply 
 *  call {@link addNodesByType} and then iterate (without recursion) over
 *  the result set.
 *
 * @author Tor Norbye
 */
public class AstUtilities {

    private static final Logger LOGGER = Logger.getLogger(AstUtilities.class.getName());
    /**
     * Whether or not the prefixes for defs should be highlighted, e.g. in def
     * HTTP.foo should "HTTP." be highlighted, or just the foo portion?
     */
    private static final boolean INCLUDE_DEFS_PREFIX = false;

    /**
     * Tries to cast the given <code>result</code> to <code>RubyParseResult</code> 
     * and returns it. Returns <code>null</code> if it wasn't an instance of <code>RubyParseResult</code>.
     * 
     * @param result
     * @return
     */
    public static RubyParseResult getParseResult(Parser.Result result) {
        if (!(result instanceof RubyParseResult)) {
            LOGGER.log(Level.WARNING, "Expected RubyParseResult, but have {0}", result);
            return null;
        }
        return (RubyParseResult) result;
    }

    public static int getAstOffset(Parser.Result info, int lexOffset) {
        RubyParseResult result = getParseResult(info);
        if (result != null) {
            return result.getSnapshot().getEmbeddedOffset(lexOffset);
        }
        return lexOffset;
    }

    public static OffsetRange getAstOffsets(Parser.Result info, OffsetRange lexicalRange) {
        RubyParseResult result = getParseResult(info);
        if (result != null) {
            int rangeStart = lexicalRange.getStart();
            int start = result.getSnapshot().getEmbeddedOffset(rangeStart);
            if (start == rangeStart) {
                return lexicalRange;
            } else if (start == -1) {
                return OffsetRange.NONE;
            } else {
                // Assumes the translated range maintains size
                return new OffsetRange(start, start + lexicalRange.getLength());
            }
        }
        return lexicalRange;
    }

    /** This is a utility class only, not instantiatiable */
    private AstUtilities() {
    }

    /**
     * Get the rdoc documentation associated with the given node in the given document.
     * The node must have position information that matches the source in the document.
     */
    public static List<String> gatherDocumentation(Snapshot baseDoc, Node node) {
        LinkedList<String> comments = new LinkedList<String>();
        int elementBegin = node.getPosition().getStartOffset();

        try {
            if (elementBegin < 0 || elementBegin >= baseDoc.getText().length()) {
                return null;
            }

            // Search to previous lines, locate comments. Once we have a non-whitespace line that isn't
            // a comment, we're done

            int offset = GsfUtilities.getRowStart(baseDoc.getText(), elementBegin);
            offset--;

            // Skip empty and whitespace lines
            while (offset >= 0) {
                // Find beginning of line
                offset = GsfUtilities.getRowStart(baseDoc.getText(), offset);

                if (!GsfUtilities.isRowEmpty(baseDoc.getText(), offset) &&
                        !GsfUtilities.isRowWhite(baseDoc.getText(), offset)) {
                    break;
                }

                offset--;
            }

            if (offset < 0) {
                return null;
            }

            while (offset >= 0) {
                // Find beginning of line
                offset = GsfUtilities.getRowStart(baseDoc.getText(), offset);

                if (GsfUtilities.isRowEmpty(baseDoc.getText(), offset) || GsfUtilities.isRowWhite(baseDoc.getText(), offset)) {
                    // Empty lines not allowed within an rdoc
                    break;
                }

                // This is a comment line we should include
                int lineBegin = GsfUtilities.getRowFirstNonWhite(baseDoc.getText(), offset);
                int lineEnd = GsfUtilities.getRowLastNonWhite(baseDoc.getText(), offset) + 1;
                String line = baseDoc.getText().subSequence(lineBegin, lineEnd).toString();

                // Tolerate "public", "private" and "protected" here --
                // Test::Unit::Assertions likes to put these in front of each
                // method.
                if (line.startsWith("#")) {
                    comments.addFirst(line);
                } else if ((comments.size() == 0) && line.startsWith("=end") &&
                        (lineBegin == GsfUtilities.getRowStart(baseDoc.getText(), offset))) {
                    // It could be a =begin,=end document - see scanf.rb in Ruby lib for example. Treat this differently.
                    gatherInlineDocumentation(comments, baseDoc, offset);

                    return comments;
                } else if (line.equals("public") || line.equals("private") ||
                        line.equals("protected")) { // NOI18N
                                                    // Skip newlines back up to the comment
                    offset--;

                    while (offset >= 0) {
                        // Find beginning of line
                        offset = GsfUtilities.getRowStart(baseDoc.getText(), offset);

                        if (!GsfUtilities.isRowEmpty(baseDoc.getText(), offset) &&
                                !GsfUtilities.isRowWhite(baseDoc.getText(), offset)) {
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
        Snapshot baseDoc, int offset) throws BadLocationException {
        // offset points to a line containing =end
        // Skip the =end list
        offset = GsfUtilities.getRowStart(baseDoc.getText(), offset);
        offset--;

        // Search backwards in the document for the =begin (if any) and add all lines in reverse
        // order in between.
        while (offset >= 0) {
            // Find beginning of line
            offset = GsfUtilities.getRowStart(baseDoc.getText(), offset);

            // This is a comment line we should include
            int lineBegin = offset;
            int lineEnd = GsfUtilities.getRowEnd(baseDoc.getText(), offset);
            String line = baseDoc.getText().subSequence(lineBegin, lineEnd).toString();

            if (line.startsWith("=begin")) {
                // We're done!
                return;
            }

            comments.addFirst(line);

            // Previous line
            offset--;
        }
    }

    public static Node getForeignNode(final IndexedElement elem) {
        return getForeignNode(elem, null);
    }

    public static Node getForeignNode(final IndexedElement elem, final Parser.Result[] foreignInfoHolder) {
        FileObject fo = elem.getFileObject();
        if (fo == null) {
            return null;
        }

        Source source = Source.create(fo);
        final Node[] nodeHolder = new Node[1];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result result = resultIterator.getParserResult();
                    if (foreignInfoHolder != null) {
                        assert foreignInfoHolder.length == 1;
                        foreignInfoHolder[0] = result;
                    }

                    Node root = AstUtilities.getRoot(result);
                    if (root != null) {
                        String signature = elem.getSignature();

                        if (signature != null) {
                            Node node = AstUtilities.findBySignature(root, signature);

                            // Special handling for "new" - these are synthesized from "initialize" methods
                            if ((node == null) && "new".equals(elem.getName())) { // NOI18N
                                if (signature.indexOf("#new") != -1) {
                                    signature = signature.replaceFirst("#new", "#initialize"); //NOI18N
                                    } else {
                                    signature = signature.replaceFirst("new", "initialize"); //NOI18N
                                    }
                                node = AstUtilities.findBySignature(root, signature);
                            }

                            nodeHolder[0] = node;
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }


        return nodeHolder[0];
    }

//    public static Node getForeignNode(final IndexedElement o) {
//        FileObject fo = o.getFileObject();
//        if (fo == null) {
//            return null;
//        }
//
//        if (file == null) {
//            return null;
//        }
//
//        List<ParserFile> files = Collections.singletonList(file);
//        SourceFileReader reader =
//            new SourceFileReader() {
//                public CharSequence read(ParserFile file)
//                    throws IOException {
//                    Document doc = o.getDocument();
//
//                    if (doc == null) {
//                        return "";
//                    }
//
//                    try {
//                        return doc.getText(0, doc.getLength());
//                    } catch (BadLocationException ble) {
//                        IOException ioe = new IOException();
//                        ioe.initCause(ble);
//                        throw ioe;
//                    }
//                }
//
//                public int getCaretOffset(ParserFile fileObject) {
//                    return -1;
//                }
//            };
//
//        DefaultParseListener listener = new DefaultParseListener();
//        // TODO - embedding model?
//        TranslatedSource translatedSource = null; // TODO - determine this here?
//        Parser.Job job = new Parser.Job(files, listener, reader, translatedSource);
//        new RubyParser().parseFiles(job);
//
//        ParserResult result = listener.getParserResult();
//
//        if (result == null) {
//            return null;
//        }
//
//        Node root = AstUtilities.getRoot(result);
//
//        if (root == null) {
//            return null;
//        }
//
//        String signature = o.getSignature();
//
//        if (signature == null) {
//            return null;
//        }
//
//        Node node = AstUtilities.findBySignature(root, signature);
//
//        // Special handling for "new" - these are synthesized from "initialize" methods
//        if ((node == null) && "new".equals(o.getName())) { // NOI18N
//            signature = signature.replaceFirst("new", "initialize"); //NOI18N
//            node = AstUtilities.findBySignature(root, signature);
//        }
//
//        return node;
//    }

    public static int boundCaretOffset(ParserResult result, int caretOffset) {
        Document doc = RubyUtils.getDocument(result);
        if (doc != null) {
            // If you invoke code completion while indexing is in progress, the
            // completion job (which stores the caret offset) will be delayed until
            // indexing is complete - potentially minutes later. When the job
            // is finally run we need to make sure the caret position is still valid.
            int length = doc.getLength();

            if (caretOffset > length) {
                caretOffset = length;
            }
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
        if (node.getNodeType() == NodeType.FCALLNODE) {
            // A method call
            String name = getName(node);

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
            }
        } else if (node.getNodeType() == NodeType.MODULENODE || node.getNodeType() == NodeType.CLASSNODE ||
                node.getNodeType() == NodeType.DEFNNODE || node.getNodeType() == NodeType.DEFSNODE) {
            // Only look for require statements at the top level
            return;
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            addRequires(child, requires);
        }
    }

    /** Locate the method of the given name and arity */
    public static MethodDefNode findMethod(Node node, String name, Arity arity) {
        // Recursively search for methods or method calls that match the name and arity
        if ((node.getNodeType() == NodeType.DEFNNODE || node.getNodeType() == NodeType.DEFSNODE) &&
            getName(node).equals(name)) {
            Arity defArity = Arity.getDefArity(node);

            if (Arity.matches(arity, defArity)) {
                return (MethodDefNode)node;
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            MethodDefNode match = findMethod(child, name, arity);

            if (match != null) {
                return match;
            }
        }

        return null;
    }

    /**
     * Gets the closest node at the given offset.
     * 
     * @param root
     * @param offset
     * @return the closest node or <code>null</code>.
     */
    public static Node findNodeAtOffset(Node root, int offset) {
        AstPath path = new AstPath(root, offset);
        Iterator<Node> it = path.leafToRoot();
        return it.hasNext() ? it.next() : null;
    }

    public static MethodDefNode findMethodAtOffset(Node root, int offset) {
        AstPath path = new AstPath(root, offset);
        Iterator<Node> it = path.leafToRoot();

        while (it.hasNext()) {
            Node node = it.next();

            if (node.getNodeType() == NodeType.DEFNNODE || node.getNodeType() == NodeType.DEFSNODE) {
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
            Iterator<Node> it = path.leafToRoot();
            while (it.hasNext()) {
                Node n = it.next();
                switch (n.getNodeType()) {
                case DEFNNODE:
                case DEFSNODE:
                case CLASSNODE:
                case SCLASSNODE:
                case MODULENODE:
                    return n;
                }
            }
            
            if (path.root() != null) {
                return path.root();
            }

            method = findBlock(path);
        }

        if (method == null) {
            method = path.leafParent();

            if (method.getNodeType() == NodeType.NEWLINENODE) {
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
        // Find the most distant block node enclosing the given node (within
        // the current method/class/module
        Node candidate = null;
        for (Node curr : path) {
            switch (curr.getNodeType()) {
            //case BLOCKNODE:
            case ITERNODE:
                candidate = curr;
                break;
            case DEFNNODE:
            case DEFSNODE:
            case CLASSNODE:
            case SCLASSNODE:
            case MODULENODE:
                return candidate;
            }
        }

        return candidate;
    }

    public static MethodDefNode findMethod(AstPath path) {
        // Find the closest block node enclosing the given node
        for (Node curr : path) {
            if (curr.getNodeType() == NodeType.DEFNNODE || curr.getNodeType() == NodeType.DEFSNODE) {
                return (MethodDefNode)curr;
            }
            if (curr.getNodeType() == NodeType.CLASSNODE || curr.getNodeType() == NodeType.SCLASSNODE ||
                    curr.getNodeType() == NodeType.MODULENODE) {
                break;
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
            if (curr.getNodeType() == NodeType.CLASSNODE || curr.getNodeType() == NodeType.MODULENODE) {
                return (IScopingNode)curr;
            }
        }

        return null;
    }

    public static boolean isCall(Node node) {
        return node.getNodeType() == NodeType.FCALLNODE ||
                node.getNodeType() == NodeType.VCALLNODE ||
                node.getNodeType() == NodeType.CALLNODE;
    }
    
    public static String getCallName(Node node) {
        assert isCall(node);

        if (node instanceof INameNode) {
            return getName(node);
        }
        assert false : node;

        return null;
    }

    public static String getDefName(Node node) {
        if (node instanceof MethodDefNode) {
            return getName(node);
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
    public static List<String> getDefArgs(MethodDefNode node, boolean namesOnly) {
        // TODO - do anything special about (&), blocks, argument lists (*), etc?
        List<Node> nodes = node.childNodes();

        // TODO - use AstElement.getParameters?
        for (Node c : nodes) {
            if (c instanceof ArgsNode) {
                ArgsNode an = (ArgsNode)c;

                List<Node> args = an.childNodes();
                List<String> parameters = new ArrayList<String>();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = arg.childNodes();

                        for (Node arg2 : args2) {
                            if (arg2 instanceof ArgumentNode || arg2 instanceof LocalAsgnNode) {
                                String name = getName(arg2);
                                parameters.add(name);
                            }
                        }
                    }
                }

                // Rest args
                if (an.getRest() != null) {
                    String name = an.getRest().getName();

                    if (!namesOnly) {
                        name = "*" + name;
                    }

                    parameters.add(name);
                }
                

                // Block args
                if (an.getBlock() != null) {
                    String name = an.getBlock().getName();

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
    public static int findArgumentIndex(Node node, int offset) {
        switch (node.getNodeType()) {
        case FCALLNODE: {
            Node argsNode = ((FCallNode)node).getArgsNode();
            if (argsNode == null) {
                return -1;
            }

            return findArgumentIndex(argsNode, offset);
        }
        case CALLNODE: {
            Node argsNode = ((CallNode)node).getArgsNode();
            if (argsNode == null) {
                return -1;
            }

            return findArgumentIndex(argsNode, offset);
        }
        case ARGSCATNODE: {
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

            SourcePosition pos = node.getPosition();

            if ((offset >= pos.getStartOffset()) && (offset <= pos.getEndOffset())) {
                return getConstantArgs(acn);
            }
            
            return -1;
        }
        case HASHNODE: 
            // Everything gets glommed into the same hash parameter offset
            return offset;
        default:
            if (node instanceof ListNode) {
                List<Node> children = node.childNodes();

                int prevEnd = Integer.MAX_VALUE;

                for (int index = 0; index < children.size(); index++) {
                    Node child = children.get(index);
                    if (child.isInvisible()) {
                        continue;
                    }
                    if (child.getNodeType() == NodeType.HASHNODE) {
                        // Invalid offsets - the hashnode often has the wrong offset
                        OffsetRange range = AstUtilities.getRange(child);
                        if ((offset <= range.getEnd()) &&
                                ((offset >= prevEnd) || (offset >= range.getStart()))) {
                            return index;
                        }

                        prevEnd = range.getEnd();
                    } else {
                        SourcePosition pos = child.getPosition();
                        if ((offset <= pos.getEndOffset()) &&
                                ((offset >= prevEnd) || (offset >= pos.getStartOffset()))) {
                            return index;
                        }

                        prevEnd = pos.getEndOffset();
                    }

                }

                // Caret -inside- empty parentheses?
                SourcePosition pos = node.getPosition();

                if ((offset > pos.getStartOffset()) && (offset < pos.getEndOffset())) {
                    return 0;
                }
            } else {
                SourcePosition pos = node.getPosition();

                if ((offset >= pos.getStartOffset()) && (offset <= pos.getEndOffset())) {
                    return 0;
                }
            }

            return -1;
        }
    }

    /** Utility method used by findArgumentIndex: count the constant number of
     * arguments in a parameter list before the argscatnode */
    private static int getConstantArgs(ArgsCatNode acn) {
        Node node = acn.getFirstNode();

        if (node instanceof ListNode) {
            List children = node.childNodes();
// TODO - if one of the children is Node.INVALID_POSITION perhaps I need to reduce the count            

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
        String originalSig = signature;

        //String name = signature.split("(::)")
        // Find next name we're looking for
        boolean[] lookingForMethod = new boolean[1];
        String name = getNextSigComponent(signature, lookingForMethod);
        signature = signature.substring(name.length());

        Node node = findBySignature(root, signature, name, lookingForMethod);
        
        // Handle top level methods
        if (node == null && originalSig.startsWith("Object#")) {
            // Just look for top level method definitions instead
            originalSig = originalSig.substring(originalSig.indexOf('#')+1);
            name = getNextSigComponent(signature, lookingForMethod);
            signature = originalSig.substring(name.length());
            lookingForMethod[0] = true;
            
            node = findBySignature(root, signature, name, lookingForMethod);
        }

        return node;
    }

    // For a signature of the form Foo::Bar#baz(arg1,arg2,...)
    // pull out the next component; in the above, successively return
    // "Foo", "Bar", "baz", etc.
    private static String getNextSigComponent(String signature, boolean[] lookingForMethod) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int n = signature.length();

        // Skip leading separators
        for (; i < n; i++) {
            char c = signature.charAt(i);

            if (c == '#') {
                lookingForMethod[0] = true;
                continue;
            } else if ((c == ':') || (c == '(')) {
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

    private static Node findBySignature(Node node, String signature, String name, boolean[] lookingForMethod) {
        switch (node.getNodeType()) {
        case INSTASGNNODE:
            if (name.charAt(0) == '@') {
                String n = getName(node);
                //if (name.regionMatches(1, n, 0, n.length())) {
                if (name.equals(n)) {
                    return node;
                }
            }
            break;
        case CLASSVARDECLNODE:
        case CLASSVARASGNNODE:
            if (name.startsWith("@@")) {
                String n = getName(node);
                //if (name.regionMatches(2, n, 0, n.length())) {
                if (name.equals(n)) {
                    return node;
                }
            }
            break;

        case DEFNNODE:
        case DEFSNODE:
            if (lookingForMethod[0] && name.equals(AstUtilities.getDefName(node))) {
                // See if the parameter list matches
                // XXX TODO
                List<String> parameters = getDefArgs((MethodDefNode)node, false);

                if ((signature.length() == 0) &&
                        ((parameters == null) || (parameters.size() == 0))) {
                    // No args
                    return node;
                } else if (signature.length() != 0) {
                    assert signature.charAt(0) == '(' : signature;

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
            break;

        case FCALLNODE:
            if (isAttr(node)) {
                SymbolNode[] symbols = getAttrSymbols(node);
                for (SymbolNode sym : symbols) {
                    if (name.equals(sym.getName())) {
                        return sym;
                    }
                }
            }
            break;
        case CONSTDECLNODE:
            if (name.equals(getName(node))) {
                return node;
            }
        break;
        case CLASSNODE:
        case MODULENODE: {
                Colon3Node c3n = ((IScopingNode)node).getCPath();

                if (c3n instanceof Colon2Node) {
                    String fqn = getFqn((Colon2Node)c3n);

                    if (fqn.startsWith(name) && signature.startsWith(fqn.substring(name.length()))) {
                        signature = signature.substring(fqn.substring(name.length()).length());
                        name = getNextSigComponent(signature, lookingForMethod);

                        if (name.length() == 0) {
                            // The signature points to a class (or module) - just return it
                            return node;
                        }

                        int index = signature.indexOf(name);
                        assert index != -1;
                        signature = signature.substring(index + name.length());
                    }
                } else if (name.equals(AstUtilities.getClassOrModuleName(((IScopingNode)node)))) {
                    name = getNextSigComponent(signature, lookingForMethod);

                    if (name.length() == 0) {
                        // The signature points to a class (or module) - just return it
                        return node;
                    }

                    int index = signature.indexOf(name);
                    assert index != -1;
                    signature = signature.substring(index + name.length());
                }
            break;
        }
        case SCLASSNODE:
            Node receiver = ((SClassNode)node).getReceiverNode();
            String rn = null;

            if (receiver instanceof Colon2Node) {
                // TODO - check to see if we qualify
                rn = getName(receiver);
            } else if (receiver instanceof ConstNode) {
                rn = getName(receiver);
            } // else: some other type of singleton class definition, like class << foo

            if (rn != null) {
                if (name.equals(rn)) {
                    name = getNextSigComponent(signature, lookingForMethod);

                    if (name.length() == 0) {
                        // The signature points to a class (or module) - just return it
                        return node;
                    }

                    int index = signature.indexOf(name);
                    assert index != -1;
                    signature = signature.substring(index + name.length());
                }
            }
            break;
        }
        List<Node> list = node.childNodes();

        boolean old = lookingForMethod[0];

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            Node match = findBySignature(child, signature, name, lookingForMethod);

            if (match != null) {
                return match;
            }
        }
        lookingForMethod[0] = old;

        return null;
    }

    /** Return true iff the given node contains the given offset */
    public static boolean containsOffset(Node node, int offset) {
        SourcePosition pos = node.getPosition();

        return ((offset >= pos.getStartOffset()) && (offset <= pos.getEndOffset()));
    }

    /**
     * Return a range that matches the given node's source buffer range
     */
    public static OffsetRange getRange(Node node) {
        if (node.isInvisible()) {
            return OffsetRange.NONE;
        }

        if (node.getNodeType() == NodeType.NOTNODE) {
            SourcePosition pos = node.getPosition();
            // "unless !(x < 5)" gives a not-node with wrong offsets - starts
            // with ! but doesn't include the closing )
            List<Node> list = node.childNodes();
            if (list != null && list.size() > 0) {
                Node first = list.get(0);
                if (first.getNodeType() == NodeType.NEWLINENODE) {
                    OffsetRange range = getRange(first);
                    return new OffsetRange(pos.getStartOffset(), range.getEnd());
                }
            } 
            return new OffsetRange(pos.getStartOffset(), pos.getEndOffset());
        } else if (node.getNodeType() == NodeType.HASHNODE) {
            // Workaround for incorrect JRuby AST offsets for hashnodes :
            //   render :action => 'list'
            // has wrong argument offsets, which we want to correct.
            // Just adopt the start offset of its first child (if any) and
            // the end offset of its last child (if any)
            List<Node> list = node.childNodes();
            if (list != null && list.size() > 0) {
                int start = list.get(0).getPosition().getStartOffset();
                int end = list.get(list.size()-1).getPosition().getEndOffset();
                return new OffsetRange(start, end);
            } else {
                SourcePosition pos = node.getPosition();
                return new OffsetRange(pos.getStartOffset(), pos.getEndOffset());
            }
        } else if (node.getNodeType() == NodeType.NILNODE) {
            return OffsetRange.NONE;
        } else {
            SourcePosition pos = node.getPosition();
            try {
                return new OffsetRange(pos.getStartOffset(), pos.getEndOffset());
            } catch (Throwable t) {
                // ...because there are some problems -- see AstUtilities.testStress
                Exceptions.printStackTrace(t);
                return OffsetRange.NONE;
            }
        }
    }
    
    /**
     * Return a range that matches the lvalue for an assignment. The node must be namable.
     */
    public static OffsetRange getLValueRange(AssignableNode node) {
        if (node instanceof MultipleAsgnNode) {
            MultipleAsgnNode man = (MultipleAsgnNode)node;
            if (man.getHeadNode() != null) {
                return getNameRange(man.getHeadNode());
            } else {
                return getRange(node);
            }
        }
        assert node instanceof INameNode : node;

        SourcePosition pos = node.getPosition();
        OffsetRange range =
            new OffsetRange(pos.getStartOffset(),
                pos.getStartOffset() + getName(node).length());

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
        SourcePosition pos = node.getPosition();
        int start = pos.getStartOffset();
        int end = pos.getEndOffset();
        assert isCall(node);
        assert node instanceof INameNode;

        if (node instanceof CallNode) {
            // A call of the form Foo.bar. "bar" is the CallNode, "Foo" is the ReceiverNode.
            // Here I'm only handling named nodes; there may be others
            Node receiver = ((CallNode)node).getReceiverNode();

            if (receiver != null && !receiver.isInvisible()) {
                start = receiver.getPosition().getEndOffset() + 1; // end of "Foo::bar" + "."
            }
        }

        if (node instanceof INameNode) {
            end = start + getName(node).length();
        }

        return new OffsetRange(start, end);
    }

    public static OffsetRange getFunctionNameRange(Node node) {
        // TODO - enforce MethodDefNode and call getNameNode on it!
        for (Node child : node.childNodes()) {
            if (child instanceof ArgumentNode) {
                OffsetRange range = AstUtilities.getRange(child);

                return range;
            }
        }

        if (node instanceof MethodDefNode) {
            for (Node child : node.childNodes()) {
                if (child instanceof ConstNode) {
                    SourcePosition pos = child.getPosition();
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
        SourcePosition pos = node.getPosition();

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
        SourcePosition pos = node.getPosition();

        int oldStart = pos.getStartOffset() + 6 + node.getNewName().length() + 1; // 6: "alias ".length; 1: " ".length

        return new OffsetRange(oldStart, oldStart + node.getOldName().length());
    }

    public static String getClassOrModuleName(IScopingNode node) {
        return getName(node.getCPath());
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

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
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

            sb.append(getName(node));
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

        String name = getName(node);

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

    public static SymbolNode[] getAttrSymbols(Node node) {
        assert isAttr(node);

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child instanceof ListNode) {
                List<Node> symbols = child.childNodes();
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

    public static Node getRoot(final FileObject sourceFO) {
        Source source = Source.create(sourceFO);
        final Node[] rootHolder = new Node[1];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator ri) throws Exception {
                    Parser.Result result = ri.getParserResult();
                    rootHolder[0] = getRoot(result);
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return rootHolder[0];
    }

    /**
     * Gets the root node from the given <code>parserResult</code>. May return
     * <code>null</code> if <code>parserResult</code> was not a <code>RubyParserResult</code> or
     * did not have a root node.
     * @param parserResult 
     * @return the root node or <code>null</code>.
     */
    public static Node getRoot(Parser.Result parserResult) {
        if (!(parserResult instanceof RubyParseResult)) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                String msg = "Expected RubyParseResult, but got " + parserResult; //NOI18N
                // log an exception too see the stack trace
                LOGGER.log(Level.WARNING, msg, new Exception(msg));
            }
            return null;
        }
        RubyParseResult result = (RubyParseResult) parserResult;
        return result.getRootNode();
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

        List<Node> list = clz.childNodes();

        Modifier access = Modifier.PUBLIC;

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
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
            String name = getName(node);

            if ("private".equals(name)) {
                // TODO - see if it has arguments, if it does - it's just a single
                // method defined to be private
                // Iterate over arguments and add symbols...
                if (Arity.callHasArguments(node)) {
                    List<Node> params = node.childNodes();

                    for (Node param : params) {
                        if (param instanceof ListNode) {
                            List<Node> params2 = param.childNodes();

                            for (Node param2 : params2) {
                                if (param2 instanceof SymbolNode) {
                                    String symbol = getName(param2);
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
                    List<Node> params = node.childNodes();

                    for (Node param : params) {
                        if (param instanceof ListNode) {
                            List<Node> params2 = param.childNodes();

                            for (Node param2 : params2) {
                                if (param2 instanceof SymbolNode) {
                                    String symbol = getName(param2);
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
                    List<Node> params = node.childNodes();

                    for (Node param : params) {
                        if (param instanceof ListNode) {
                            List<Node> params2 = param.childNodes();

                            for (Node param2 : params2) {
                                if (param2 instanceof SymbolNode) {
                                    String symbol = getName(param2);
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

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            access = getMethodAccess(child, access, publicMethodSymbols, protectedMethodSymbols,
                    privateMethodSymbols, publicMethods, protectedMethods, privateMethods);
        }

        return access;
    }
    
    /**
     * Get the method name for the given offset - or null if it cannot be found. This
     * will initiate a new parse job if necessary.
     */
    public static String getMethodName(FileObject fo, final int lexOffset) {
        Source source = Source.create(fo);

        if (source == null) {
            return null;
        }

        final String[] methodName = new String[1];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result result = resultIterator.getParserResult();
                    Node root = AstUtilities.getRoot(result);
                    if (root == null) {
                        return;
                    }
                    int astOffset = AstUtilities.getAstOffset(result, lexOffset);
                    if (astOffset == -1) {
                        return;
                    }
                    org.jrubyparser.ast.MethodDefNode method = AstUtilities.findMethodAtOffset(root, astOffset);
                    if (method == null) {
                        // It's possible the user had the caret on a line
                        // that includes a method that isn't actually inside
                        // the method block - such as the beginning of the
                        // "def" line, or the end of a line after "end".
                        // The latter isn't very likely, but the former can
                        // happen, so let's check the method bodies at the
                        // end of the current line
                        BaseDocument doc = RubyUtils.getDocument(result);
                        if (doc != null) {
                            try {
                                int endOffset = Utilities.getRowEnd(doc, lexOffset);
                                if (endOffset != lexOffset) {
                                    astOffset = AstUtilities.getAstOffset(result, endOffset);
                                    if (astOffset == -1) {
                                        return;
                                    }
                                    method = AstUtilities.findMethodAtOffset(root, astOffset);
                                }
                            } catch (BadLocationException ble) {
                                Exceptions.printStackTrace(ble);
                            }
                        }
                    }
                    if (method != null) {
                        methodName[0] = method.getName();
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return methodName[0];
    }

    /**
     * Get the test name surrounding the given offset - or null if it cannot be found.
     * NOTE: This will initiate a new parse job if necessary. 
     */
    public static String getTestName(FileObject fo, final int caretOffset) {
        Source source = Source.create(fo);

        if (source == null) {
            return null;
        }
        
        final String[] testName = new String[1];

        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    try {
                        Parser.Result result = resultIterator.getParserResult();
                        Node root = AstUtilities.getRoot(result);
                        if (root == null) {
                            return;
                        }
                        // Make sure the offset isn't at the beginning of a line
                        BaseDocument doc = RubyUtils.getDocument(result, true);
                        if (doc == null) {
                            return;
                        }
                        int lexOffset = caretOffset;
                        int rowStart = Utilities.getRowFirstNonWhite(doc, lexOffset);
                        if (rowStart != -1 && lexOffset <= rowStart) {
                            lexOffset = rowStart + 1;
                        }
                        int astOffset = AstUtilities.getAstOffset(result, lexOffset);
                        if (astOffset == -1) {
                            return;
                        }
                        AstPath path = new AstPath(root, astOffset);
                        Iterator<Node> it = path.leafToRoot();
                        /*
                         * method names for shoulda tests need to be constructed from
                         * should and context nodes, e.g.
                         * context "An Instance" ...
                         *  should "respond to :something" ... (the method name here is "An Instance should respond to :something"
                         *    context "with a single element" ..
                         *        should "return that" ... (the name here is "An Instance with a single element should return that")
                         *
                         * the name for a should node without context uses the name of the tested class, e.g.
                         * class QueueTest
                         *  should "be empty" do ..
                         *  end
                         * end
                         * => the method name is "Queue should be empty"
                         */
                        List<String> shouldaMethodName = new ArrayList<String>();
                        // for shoulda tests without a context, the class name
                        // needs to be appended - see #151652 for details
                        boolean appendClassName = true;
                        while (it.hasNext()) {
                            Node node = it.next();
                            if (node.getNodeType() == NodeType.FCALLNODE) {
                                FCallNode fc = (FCallNode)node;
                                // Possibly a test node
                                // See http://github.com/rails/rails/commit/f74ba37f4e4175d5a1b31da59d161b0020b58e94
                                // test_name = "test_#{name.gsub(/[\s]/,'_')}".to_sym
                                if ("test".equals(fc.getName())) { // NOI18N
                                    String desc = getNodeDesc(fc);
                                    if (desc != null) {
                                        testName[0] = "test_" + desc.replace(' ', '_'); // NOI18N
                                    }
                                    return;
                                // possibly a shoulda test
                                } else if ("should".equals(fc.getName())) { //NOI18N
                                    buildShouldaMethod(" should " + getNodeDesc(fc), shouldaMethodName, false);
                                } else if ("context".equals(fc.getName())) { //NOI18N
                                    String desc = getNodeDesc(fc);
                                    if (desc != null) {
                                        appendClassName = false;
                                    }
                                    buildShouldaMethod(desc, shouldaMethodName, true);
                                }
                            } else if (node.getNodeType() == NodeType.CLASSNODE && appendClassName) {
                                String className = getClassNameForShoulda((IScopingNode) node);
                                buildShouldaMethod(className, shouldaMethodName, false);
                            } else if (node.getNodeType() == NodeType.DEFNNODE || node.getNodeType() == NodeType.DEFSNODE) {
                                testName[0] = getName(node);
                                return;
                            }
                        }
                        if (!shouldaMethodName.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (String each : shouldaMethodName) {
                                sb.append(each);
                            }
                            testName[0] = removeLeadingWhiteSpace(sb.toString());
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return testName[0];
    }

    private static String removeLeadingWhiteSpace(String str) {
        if (str.startsWith(" ")) { //NOI18N
            return str.substring(1);
        }
        return str;
    }

    private static String getClassNameForShoulda(IScopingNode classNode) {
        String testClassName = getClassOrModuleName(classNode);
        if (testClassName != null && testClassName.indexOf("Test") != -1) { //NOI18N
            return testClassName.substring(0, testClassName.indexOf("Test")); //NOI18N
        }
        return null;
    }

    private static void buildShouldaMethod(String desc, List<String> shouldaMethodName, boolean trim) {
        if (desc == null) {
            return;
        }
        // shoulda removes leading and trailing whitespaces for context nodes, but not
        // for should nodes
        if (trim) {
            desc = desc.trim();
        }
        if (shouldaMethodName.isEmpty()) {
            shouldaMethodName.add(desc);
        } else {
            shouldaMethodName.add(0, " " + desc); //NOI18N
        }
    }
    private static String getNodeDesc(FCallNode fc) {
        if (fc.getIterNode() == null) { // "it" without do/end: pending
            return null;
        }

        Node argsNode = fc.getArgsNode();

        if (argsNode instanceof ListNode) {
            ListNode args = (ListNode) argsNode;

            //  describe  ThingsController, "GET #index" do
            // e.g. where the desc string is not first
            for (int i = 0, max = args.size(); i < max; i++) {
                Node n = args.get(i);

                // For dynamically computed strings, we have n instanceof DStrNode
                // but I can't handle these anyway
                if (n instanceof StrNode) {
                    String descBl = ((StrNode) n).getValue();

                    if ((descBl != null) && (descBl.length() > 0)) {
                        // No truncation? See 138259
                        //desc = RubyUtils.truncate(descBl.toString(), MAX_RUBY_LABEL_LENGTH);
                        return descBl.toString();
                    }
                    break;
                }
            }
        }
        return null;

    }

    public static int findOffset(FileObject fo, final String methodName) {
        Source source = Source.create(fo);

        if (source == null) {
            return -1;
        }
        
        final int[] offset = new int[1];
        offset[0] = -1;

        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result result = resultIterator.getParserResult();
                    Node root = AstUtilities.getRoot(result);
                    if (root == null) {
                        return;
                    }

                    org.jrubyparser.ast.Node method =
                            AstUtilities.findMethod(root, methodName, Arity.UNKNOWN);

                    if (method != null) {
                        int startOffset = method.getPosition().getStartOffset();
                        offset[0] = startOffset;
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return offset[0];
    }
    
    /** Collect nodes of the given types (node.getNodeType()==NodeType.x) under the given root */
    public static void addNodesByType(Node root, NodeType[] nodeIds, List<Node> result) {
        for (int i = 0; i < nodeIds.length; i++) {
            if (root.getNodeType() == nodeIds[i]) {
                result.add(root);
                break;
            }
        }

        List<Node> list = root.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            addNodesByType(child, nodeIds, result);
        }
    }
    
    /** Return all the blocknodes that apply to the given node. The outermost block
     * is returned first.
     */
    public static List<Node> getApplicableBlocks(AstPath path, boolean includeNested) {
        Node block = AstUtilities.findBlock(path);

        if (block == null) {
            // Use parent
            block = path.leafParent();

            if (block == null) {
                return Collections.emptyList();
            }
        }
        
        List<Node> result = new ArrayList<Node>();
        Iterator<Node> it = path.leafToRoot();
        
        // Skip the leaf node, we're going to add it unconditionally afterwards
        if (includeNested) {
            if (it.hasNext()) {
                it.next();
            }
        }

        Node leaf = path.root();

      while_loop:
        while (it.hasNext()) {
            Node n = it.next();
            switch (n.getNodeType()) {
            //case BLOCKNODE:
            case ITERNODE:
                leaf = n;
                result.add(n);
                break;
            case DEFNNODE:
            case DEFSNODE:
            case CLASSNODE:
            case SCLASSNODE:
            case MODULENODE:
                leaf = n;
                break while_loop;
            }
        }

        if (includeNested) {
            addNodesByType(leaf, new NodeType[] { /*NodeType.BLOCKNODE,*/ NodeType.ITERNODE }, result);
        }
        
        return result;
    }
    
    public static String guessName(Parser.Result result, OffsetRange lexRange, OffsetRange astRange) {
        String guessedName = "";
        
        // Try to guess the name - see if it's in a method and if so name it after the parameter
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        @SuppressWarnings("unchecked")
        Set<IndexedMethod>[] alternatesHolder = new Set[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        if (!RubyMethodCompleter.computeMethodCall(result, lexRange.getStart(), astRange.getStart(),
                methodHolder, paramIndexHolder, anchorOffsetHolder, alternatesHolder, QuerySupport.Kind.PREFIX)) {

            return guessedName;
        }

        IndexedMethod targetMethod = methodHolder[0];
        int index = paramIndexHolder[0];

        List<String> params = targetMethod.getParameters();
        if (params == null || params.size() <= index) {
            return guessedName;
        }
        
        String s = params.get(index);
        if (s.startsWith("*") || s.startsWith("&")) { // Don't include * or & in variable name
            s = s.substring(1);
        }
        return s;
    }
    
    public static Set<String> getUsedFields(RubyIndex index, AstPath path) {
        String fqn = AstUtilities.getFqnName(path);
        if (fqn == null || fqn.length() == 0) {
            return Collections.emptySet();
        }
        Set<IndexedField> fields = index.getInheritedFields(fqn, "", QuerySupport.Kind.PREFIX, false);
        Set<String> fieldNames = new HashSet<String>();
        for (IndexedField f : fields) {
            fieldNames.add(f.getName());
        }
        
        return fieldNames;
    }
    
    public static Set<String> getUsedMethods(RubyIndex index, AstPath path) {
        String fqn = AstUtilities.getFqnName(path);
        if (fqn == null || fqn.length() == 0) {
            return Collections.emptySet();
        }
        Set<IndexedMethod> methods = index.getInheritedMethods(fqn, "", QuerySupport.Kind.PREFIX);
        Set<String> methodNames = new HashSet<String>();
        for (IndexedMethod m : methods) {
            methodNames.add(m.getName());
        }
        
        return methodNames;
    }
    
    /** @todo Implement properly */
    public static Set<String> getUsedConstants(RubyIndex index, AstPath path) {
        //String fqn = AstUtilities.getFqnName(path);
        //if (fqn == null || fqn.length() == 0) {
            return Collections.emptySet();
        //}
        //Set<IndexedConstant> constants = index.getInheritedConstants(fqn, "", QuerySupport.Kind.PREFIX);
        //Set<String> constantNames = new HashSet<String>();
        //for (IndexedConstant m : constants) {
        //    constantNames.add(m.getName());
        //}
        //
        //return constantNames;
    }
    
    public static Set<String> getUsedLocalNames(AstPath path, Node closest) {
        Node method = AstUtilities.findLocalScope(closest, path);
        Map<String, Node> variables = new HashMap<String, Node>();
        // Add locals
        RubyCodeCompleter.addLocals(method, variables);

        List<Node> applicableBlocks = AstUtilities.getApplicableBlocks(path, false);
        for (Node block : applicableBlocks) {
            RubyCodeCompleter.addDynamic(block, variables);
        }
        
        return variables.keySet();
    }

    /**
     * Throws {@link ClassCastException} if the given node is not instance of
     * {@link INameNode}.
     *
     * @param node instance of {@link INameNode}.
     * @return node's name
     */
    public static String getName(final Node node) {
        return ((INameNode) node).getName();
    }

    /**
     * Finds exit points of a method definition for the given node.
     *
     * @param defNode {@link MethodDefNode method definition node}
     * @param exits accumulator for found exit points
     */
    public static void findExitPoints(final MethodDefNode defNode, final Set<? super Node> exits) {
        Node body = defNode.getBodyNode();
        if (body != null) { // method with empty body
            findNonLastExitPoints(body, exits);
            Node last = findLastNode(body);
            if (last != null) {
                exits.add(last);
            }
        }
    }

    private static Node findLastNode(final Node node) {
        Node last = null;
        List<Node> list = node.childNodes();
        for (int i = list.size() - 1; i >= 0; i--) {
            last = list.get(i);

            if (last instanceof ArgsNode || last instanceof ArgumentNode) {
                // Done - no valid statement
                return null;
            }

            if (last instanceof ListNode) {
                last = last.childNodes().get(last.childNodes().size() - 1);
            }

            if (last instanceof NewlineNode && (last.childNodes().size() > 0)) {
                last = last.childNodes().get(last.childNodes().size() - 1);
                break;
            }
            break;
        }
        return last;
    }

    /** Helper for {@link #findExitPoints}. */
    private static void findNonLastExitPoints(final Node node, final Set<? super Node> exits) {
        switch (node.getNodeType()) {
            case RETURNNODE:
            case YIELDNODE:
                exits.add(node);
                break;
            case CLASSNODE:
            case SCLASSNODE:
            case MODULENODE:
                return; // Don't go into sub methods, classes, etc
            case FCALLNODE:
                FCallNode fc = (FCallNode) node;
                if ("fail".equals(fc.getName()) || "raise".equals(fc.getName())) { // NOI18N
                    exits.add(node);
                }
                break;
        }
        if (node instanceof MethodDefNode) {
            // Don't go into sub methods, classes, etc
            return;
        }

        List<Node> children = node.childNodes();

        for (Node child : children) {
            if (child.isInvisible()) {
                continue;
            }
            findNonLastExitPoints(child, exits);
        }
    }
}
