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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.ruby;


import javax.swing.text.Document;

import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.GlobalAsgnNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.IScopingNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.jruby.ast.SymbolNode;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.retouche.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.Arity;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.TypeAnalyzer;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.lexer.Call;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.filesystems.FileObject;


/**
 * This is a holder class for a Ruby element as well as its
 * context - used in various places in the refactoring classes.
 * These need to be able to be mapped from one AST to another,
 * and correspond (roughly) to the TreePath, RubyElementCtx,
 * Element and ElementHandle classes (plus some friends like CompilationInfo
 * and FileObject) passed around in the equivalent Java refactoring code.
 *
 * @author Tor Norbye
 */
public class RubyElementCtx {
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
    private Arity arity;
    private String defClass;

    public RubyElementCtx(Node root, Node node, Element element, FileObject fileObject,
        CompilationInfo info) {
        initialize(root, node, element, fileObject, info);
    }

    /** Create a new element holder representing the node closest to the given caret offset in the given compilation job */
    public RubyElementCtx(CompilationInfo info, int caret) {
        Node root = AstUtilities.getRoot(info);

        if (info.getEmbeddingModel() != null) {
            caret = info.getEmbeddingModel().sourceToGeneratedPos(info.getFileObject(), caret);
        }

        path = new AstPath(root, caret);

        Node leaf = path.leaf();

        Element element = AstElement.create(leaf);

        initialize(root, leaf, element, info.getFileObject(), info);
    }

    /** Create a new element holder representing the given node in the same context as the given existing context */
    public RubyElementCtx(RubyElementCtx ctx, Node node) {
        Element element = AstElement.create(node);

        initialize(ctx.getRoot(), node, element, ctx.getFileObject(), ctx.getInfo());
    }

    public RubyElementCtx(IndexedElement element, CompilationInfo info) {
        Node[] rootRet = new Node[1];
        Node node = AstUtilities.getForeignNode(element, rootRet);
        Node root = rootRet[0];

        Element e = AstElement.create(node);

        FileObject fo = element.getFileObject();
        document = RetoucheUtils.getDocument(null, fo);

        initialize(root, node, e, fo, info);
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
            if (node instanceof MethodDefNode) {
                kind = AstUtilities.isConstructorMethod((MethodDefNode)node)
                    ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
            } else if (AstUtilities.isCall(node)) {
                kind = ElementKind.METHOD;
            } else if (node instanceof ClassNode || node instanceof SClassNode) {
                kind = ElementKind.CLASS;
            } else if (node instanceof ModuleNode) {
                kind = ElementKind.MODULE;
            } else if (node instanceof LocalVarNode || node instanceof LocalAsgnNode ||
                    node instanceof DVarNode || node instanceof DAsgnNode) {
                kind = ElementKind.VARIABLE;
            } else if (node instanceof ArgumentNode) {
                AstPath path = getPath();

                if (path.leafParent() instanceof MethodDefNode) {
                    kind = AstUtilities.isConstructorMethod((MethodDefNode)path.leafParent())
                        ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
                } else {
                    // TODO - are ArgumentNodes used anywhere else?
                    kind = ElementKind.PARAMETER;
                }
            } else if (node instanceof SymbolNode) {
                // Ugh - how do I know what it's referring to - a method? a class? a constant? etc.
                if (Character.isUpperCase(((SymbolNode)node).getName().charAt(0))) {
                    kind = ElementKind.CLASS; // Or module? Or constants? How do we know?
                } else {
                    // Or method
                    kind = ElementKind.METHOD;
                }
            } else if (node instanceof AliasNode) {
                // XXX ugh - how do I know what the alias is referring to? For now just guess METHOD, the most common usage
                kind = ElementKind.METHOD;
            } else if (node instanceof ConstNode || node instanceof Colon2Node) {
                Node n = getPath().leafParent();

                if (n instanceof ClassNode || n instanceof SClassNode) {
                    kind = ElementKind.CLASS;
                } else if (n instanceof ModuleNode) {
                    kind = ElementKind.MODULE;
                } else {
                    if (node instanceof ConstNode) {
                        // It might just be a reference to a class or a module (it probably is!)
                        // so I could look up in the index and see what it is -- a class, a module, or
                        // just some other constant - but that's expensive. For now, optimize for the
                        // common case - a class.
                        kind = ElementKind.CLASS;
                    } else {
                        kind = ElementKind.CONSTANT;
                    }
                }
            } else if (node instanceof GlobalVarNode || node instanceof GlobalAsgnNode) {
                kind = ElementKind.GLOBAL;
            } else if (node instanceof InstVarNode || node instanceof InstAsgnNode ||
                    node instanceof ClassVarNode || node instanceof ClassVarDeclNode ||
                    node instanceof ClassVarAsgnNode) {
                kind = ElementKind.FIELD;
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

    public String getSimpleName() {
        if (name == null) {
            getName();
        }

        return simpleName;
    }

    public Arity getArity() {
        if (arity == null) {
            if (node instanceof MethodDefNode) {
                arity = Arity.getDefArity(node);
            } else if (AstUtilities.isCall(node)) {
                arity = Arity.getCallArity(node);
            } else if (node instanceof ArgumentNode) {
                AstPath path = getPath();

                if (path.leafParent() instanceof MethodDefNode) {
                    arity = Arity.getDefArity(path.leafParent());
                }
            }
        }

        return arity;
    }

    public BaseDocument getDocument() {
        if (document == null) {
            document = RetoucheUtils.getDocument(info, info.getFileObject());
        }

        return document;
    }
    
    private String getViewControllerRequire(FileObject view) {
        return null;
    }

    /** If the node is a method call, return the class of the method we're looking
     * for (if any)
     */
    public String getDefClass() {
        if (defClass == null) {
            if (RubyUtils.isRhtmlFile(fileObject)) {
                // TODO - look in the Helper class as well to see if the method is coming from there!
                // In fact that's probably a more likely home!
                defClass = "ActionView::Base";
            } else if (AstUtilities.isCall(node)) {
                // Try to figure out the call type from the call
                BaseDocument doc = getDocument();
                TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
                int astOffset = AstUtilities.getCallRange(node).getStart();
                Call call = Call.getCallType(doc, th, astOffset);
                int lexOffset = LexUtilities.getLexerOffset(info, astOffset);

                String type = call.getType();
                String lhs = call.getLhs();

                if ((type == null) && (lhs != null) && (node != null) && call.isSimpleIdentifier()) {
                    Node method = AstUtilities.findLocalScope(node, getPath());

                    if (method != null) {
                        // TODO - if the lhs is "foo.bar." I need to split this
                        // up and do it a bit more cleverly
                        TypeAnalyzer analyzer =
                            new TypeAnalyzer(method, node, astOffset, lexOffset, doc, null);
                        type = analyzer.getType(lhs);
                    }
                } else if (call == Call.LOCAL) {
                    // Look in the index to see which method it's coming from... 
                    RubyIndex index = RubyIndex.get(info.getIndex());
                    String fqn = AstUtilities.getFqnName(getPath());

                    if ((fqn == null) || (fqn.length() == 0)) {
                        fqn = RubyIndex.OBJECT;
                    }

                    IndexedMethod method = index.getOverridingMethod(fqn, getName());

                    if (method != null) {
                        defClass = method.getIn();
                    } // else: It's some unqualified method call we don't recognize - perhaps an attribute?
                      // For now just assume it's a method on this class
                }

                if (defClass == null) {
                    // Just an inherited method call?
                    if ((type == null) && (lhs == null)) {
                        defClass = AstUtilities.getFqnName(getPath());
                    } else if (type != null) {
                        defClass = type;
                    } else {
                        defClass = RubyIndex.UNKNOWN_CLASS;
                    }
                }
            } else {
                if (getPath() != null) {
                    IScopingNode clz = AstUtilities.findClassOrModule(getPath());

                    if (clz != null) {
                        defClass = AstUtilities.getClassOrModuleName(clz);
                    }
                }

                if ((defClass == null) && (element != null)) {
                    defClass = element.getIn();
                }

                if (defClass == null) {
                    defClass = RubyIndex.OBJECT; // NOI18N
                }
            }
        }

        return defClass;
    }

    @Override
    public String toString() {
        return "node= " + node + ";kind=" + getKind() + ";" + super.toString();
    }

    /**
     * Get the prefix of the name which should be "stripped" before letting the user edit the variable,
     * and put back in when done. For globals for example, it's "$"
     */
    public String getStripPrefix() {
        if (node instanceof GlobalVarNode || node instanceof GlobalAsgnNode) {
            return "$";
        } else if (node instanceof InstVarNode || node instanceof InstAsgnNode) {
            return "@";
        } else if (node instanceof ClassVarNode || node instanceof ClassVarDeclNode ||
                node instanceof ClassVarAsgnNode) {
            return "@@";            
        //} else if (node instanceof SymbolNode) {
        // Symbols don't include these in their names
        //    return ":";
        }

        // TODO: Blocks - "&" ?
        // Restargs - "*" ?
        return null;
    }
}
