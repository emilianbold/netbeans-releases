/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.javascript;


import java.util.Iterator;

import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.javascript.editing.AstPath;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.Element;
import org.netbeans.modules.javascript.editing.IndexedFunction;
import org.netbeans.modules.javascript.editing.AstElement;
import org.openide.filesystems.FileObject;


/**
 * This is a holder class for a Ruby element as well as its
 * context - used in various places in the refactoring classes.
 * These need to be able to be mapped from one AST to another,
 * and correspond (roughly) to the TreePath, JsElementCtx,
 * Element and ElementHandle classes (plus some friends like CompilationInfo
 * and FileObject) passed around in the equivalent Java refactoring code.
 *
 * @author Tor Norbye
 */
public class JsElementCtx {
    private Node node;
    private Node root;
    private CompilationInfo info;
    private FileObject fileObject;
    private AstPath path;
    private int caret;
    private BaseDocument document;

    // TODO - get rid of this, the refactoring code should be completely rewritten to use AST nodes directly
    private Element element;

    // Lazily computed
    private ElementKind kind;
    private String name;
    private String simpleName;
    //private Arity arity;
    private String defClass;

    public JsElementCtx(Node root, Node node, Element element, FileObject fileObject,
        CompilationInfo info) {
        initialize(root, node, element, fileObject, info);
    }

    /** Create a new element holder representing the node closest to the given caret offset in the given compilation job */
    public JsElementCtx(CompilationInfo info, int caret) {
        Node root = AstUtilities.getRoot(info);

        int astOffset = AstUtilities.getAstOffset(info, caret);
        path = new AstPath(root, astOffset);

        Node leaf = path.leaf();

        Iterator<Node> it = path.leafToRoot();
    FindNode:
        while (it.hasNext()) {
            leaf = it.next();
            switch (leaf.getType()) {
                case Token.OBJLITNAME:
                    if (AstUtilities.isLabelledFunction(leaf)) {
                        break FindNode;
                    }
                    break;
                case Token.FUNCNAME:
                case Token.NAME:
                case Token.BINDNAME:
                case Token.PARAMETER:
                case Token.CALL:
                    break FindNode;
            }
            if (!it.hasNext()) {
                leaf = path.leaf();
                break;
            }
        }
        Element element = AstElement.getElement(info, leaf);

        initialize(root, leaf, element, info.getFileObject(), info);
    }

    /** Create a new element holder representing the given node in the same context as the given existing context */
    public JsElementCtx(JsElementCtx ctx, Node node) {
        Element element = AstElement.getElement(info, node);

        initialize(ctx.getRoot(), node, element, ctx.getFileObject(), ctx.getInfo());
    }

    private void initialize(Node root, Node node, Element element, FileObject fileObject,
        CompilationInfo info) {
        this.root = root;
        this.node = node;
        this.element = element;
        this.fileObject = fileObject;
        this.info = info;
    }

    public Node getRoot() {
        return root;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public void setFileObject(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public ElementKind getKind() {
        if (kind == null) {
            switch (node.getType()) {
            case Token.OBJLITNAME: {
                if (AstUtilities.isLabelledFunction(node)) {
                    kind = ElementKind.METHOD;
                }
                break;
            }
            case Token.FUNCNAME:
                //case Token.FUNCTION:
                kind = Character.isUpperCase(node.getString().charAt(0)) ?
                    ElementKind.CONSTRUCTOR : ElementKind.METHOD;
                break;
            case Token.CALL:
                kind = ElementKind.METHOD;
                break;
            case Token.PARAMETER:
                kind = ElementKind.PARAMETER;
                break;
            case Token.STRING: // actually, it's a property...
            case Token.NAME:
            case Token.BINDNAME:
                // TODO - look up scope and see if it's a global or a local var
                //kind = ElementKind.GLOBAL;
                kind = ElementKind.VARIABLE;
                break;
            case Token.CONST:
            case Token.SETCONST:
                kind = ElementKind.CONSTANT;
                break;
            }
        }

        return kind;
    }

    public void setKind(ElementKind kind) {
        this.kind = kind;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public CompilationInfo getInfo() {
        return info;
    }

    public AstPath getPath() {
        if (path == null) {
            path = new AstPath(root, node);
        }

        return path;
    }

    public int getCaret() {
        return caret;
    }

    public String getName() {
        if (name == null) {
            String[] names = RetoucheUtils.getNodeNames(node);
            name = names[0];
            simpleName = names[1];
        }

        return name;
    }
    
    public void setNames(String name, String simpleName) {
        this.name = name;
        this.simpleName = simpleName;
    }

    @CheckForNull
    public String getSimpleName() {
        if (name == null) {
            getName();
        }

        return simpleName;
    }

//    public Arity getArity() {
//        if (arity == null) {
//            if (node instanceof MethodDefNode) {
//                arity = Arity.getDefArity(node);
//            } else if (AstUtilities.isCall(node)) {
//                arity = Arity.getCallArity(node);
//            } else if (node instanceof ArgumentNode) {
//                AstPath path = getPath();
//
//                if (path.leafParent() instanceof MethodDefNode) {
//                    arity = Arity.getDefArity(path.leafParent());
//                }
//            }
//        }
//
//        return arity;
//    }

    public BaseDocument getDocument() {
        if (document == null) {
            document = RetoucheUtils.getDocument(info, info.getFileObject());
        }

        return document;
    }
    
    private String getViewControllerRequire(FileObject view) {
        return null;
    }

//    /** If the node is a method call, return the class of the method we're looking
//     * for (if any)
//     */
//    public String getDefClass() {
//        if (defClass == null) {
//            if (RubyUtils.isRhtmlFile(fileObject)) {
//                // TODO - look in the Helper class as well to see if the method is coming from there!
//                // In fact that's probably a more likely home!
//                defClass = "ActionView::Base";
//            } else if (AstUtilities.isCall(node)) {
//                // Try to figure out the call type from the call
//                BaseDocument doc = getDocument();
//                TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
//                int astOffset = AstUtilities.getCallRange(node).getStart();
//                Call call = Call.getCallType(doc, th, astOffset);
//                int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
//
//                String type = call.getType();
//                String lhs = call.getLhs();
//
//                if ((type == null) && (lhs != null) && (node != null) && call.isSimpleIdentifier()) {
//                    Node method = AstUtilities.findLocalScope(node, getPath());
//
//                    if (method != null) {
//                        // TODO - if the lhs is "foo.bar." I need to split this
//                        // up and do it a bit more cleverly
//                        TypeAnalyzer analyzer =
//                            new TypeAnalyzer(null, method, node, astOffset, lexOffset, doc, null);
//                        type = analyzer.getType(lhs);
//                    }
//                } else if (call == Call.LOCAL) {
//                    // Look in the index to see which method it's coming from... 
//                    RubyIndex index = RubyIndex.get(info.getIndex());
//                    String fqn = AstUtilities.getFqnName(getPath());
//
//                    if ((fqn == null) || (fqn.length() == 0)) {
//                        fqn = RubyIndex.OBJECT;
//                    }
//
//                    IndexedMethod method = index.getOverridingMethod(fqn, getName());
//
//                    if (method != null) {
//                        defClass = method.getIn();
//                    } // else: It's some unqualified method call we don't recognize - perhaps an attribute?
//                      // For now just assume it's a method on this class
//                }
//
//                if (defClass == null) {
//                    // Just an inherited method call?
//                    if ((type == null) && (lhs == null)) {
//                        defClass = AstUtilities.getFqnName(getPath());
//                    } else if (type != null) {
//                        defClass = type;
//                    } else {
//                        defClass = RubyIndex.UNKNOWN_CLASS;
//                    }
//                }
//            } else {
//                if (getPath() != null) {
//                    IScopingNode clz = AstUtilities.findClassOrModule(getPath());
//
//                    if (clz != null) {
//                        defClass = AstUtilities.getClassOrModuleName(clz);
//                    }
//                }
//
//                if ((defClass == null) && (element != null)) {
//                    defClass = element.getIn();
//                }
//
//                if (defClass == null) {
//                    defClass = RubyIndex.OBJECT; // NOI18N
//                }
//            }
//        }
//
//        return defClass;
//    }

    @Override
    public String toString() {
        return "node= " + node + ";kind=" + getKind() + ";" + super.toString();
    }

    /**
     * Get the prefix of the name which should be "stripped" before letting the user edit the variable,
     * and put back in when done. For globals for example, it's "$"
     */
    public String getStripPrefix() {
//        if (node instanceof GlobalVarNode || node instanceof GlobalAsgnNode) {
//            return "$";
//        } else if (node instanceof InstVarNode || node instanceof InstAsgnNode) {
//            return "@";
//        } else if (node instanceof ClassVarNode || node instanceof ClassVarDeclNode ||
//                node instanceof ClassVarAsgnNode) {
//            return "@@";            
//        //} else if (node instanceof SymbolNode) {
//        // Symbols don't include these in their names
//        //    return ":";
//        }
//
//        // TODO: Blocks - "&" ?
//        // Restargs - "*" ?
        return null;
    }
}
