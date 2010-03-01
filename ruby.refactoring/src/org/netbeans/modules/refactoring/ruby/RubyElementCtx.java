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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.ruby;

import java.util.Iterator;
import java.util.Set;
import javax.swing.text.Document;
import org.jrubyparser.ast.ArgumentNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.ClassVarAsgnNode;
import org.jrubyparser.ast.ClassVarDeclNode;
import org.jrubyparser.ast.ClassVarNode;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.GlobalAsgnNode;
import org.jrubyparser.ast.GlobalVarNode;
import org.jrubyparser.ast.IScopingNode;
import org.jrubyparser.ast.InstAsgnNode;
import org.jrubyparser.ast.InstVarNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.SClassNode;
import org.jrubyparser.ast.SymbolNode;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.ruby.Arity;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.ContextKnowledge;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyType;
import org.netbeans.modules.ruby.RubyTypeInferencer;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.Element;
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
 * Element and ElementHandle classes (plus some friends like ParserResult
 * and FileObject) passed around in the equivalent Java refactoring code.
 *
 * @author Tor Norbye
 */
public class RubyElementCtx {
    
    private Node node;
    private Node root;
    private ParserResult info;
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
        ParserResult info) {
        initialize(root, node, element, fileObject, info);
    }

    /** Create a new element holder representing the node closest to the given caret offset in the given compilation job */
    public RubyElementCtx(ParserResult parserResult, int caret) {
        Node _root = AstUtilities.getRoot(parserResult);

        int astOffset = AstUtilities.getAstOffset(parserResult, caret);
        path = new AstPath(_root, astOffset);

        Node leaf = path.leaf();
        if (leaf == null) {
            return;
        }

        Iterator<Node> it = path.leafToRoot();
        FindNode:
        while (it.hasNext()) {
            leaf = it.next();
            switch (leaf.getNodeType()) {
                case ARGUMENTNODE:
                case LOCALVARNODE:
                case LOCALASGNNODE:
                case DVARNODE:
                case DASGNNODE:
                case SYMBOLNODE:
                case FCALLNODE:
                case VCALLNODE:
                case CALLNODE:
                case GLOBALVARNODE:
                case GLOBALASGNNODE:
                case INSTVARNODE:
                case INSTASGNNODE:
                case CLASSVARNODE:
                case CLASSVARASGNNODE:
                case CLASSVARDECLNODE:
                case COLON2NODE:
                case CONSTNODE:
                case CONSTDECLNODE:
                    break FindNode;
            }
            if (!it.hasNext()) {
                leaf = path.leaf();
                break;
            }
        }
        Element _element = AstElement.create(parserResult, leaf);

        initialize(_root, leaf, _element, RubyUtils.getFileObject(parserResult), parserResult);
    }

    /** Create a new element holder representing the given node in the same context as the given existing context */
    public RubyElementCtx(RubyElementCtx ctx, Node node) {
        Element _element = AstElement.create(info, node);

        initialize(ctx.getRoot(), node, _element, ctx.getFileObject(), ctx.getInfo());
    }

    public RubyElementCtx(IndexedElement element) {
        ParserResult[] infoHolder = new ParserResult[1];
        Node _node = AstUtilities.getForeignNode(element, infoHolder);
        ParserResult _info = infoHolder[0];

        Element e = AstElement.create(_info, _node);

        FileObject fo = element.getFileObject();
        document = RetoucheUtils.getDocument(null, fo);

        initialize(root, _node, e, fo, _info);
    }

    private void initialize(Node root, Node node, Element element, FileObject fileObject,
        ParserResult info) {
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
            switch (node.getNodeType()) {
            case DEFNNODE:
            case DEFSNODE:
                kind = AstUtilities.isConstructorMethod((MethodDefNode)node)
                    ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
                break;
            case FCALLNODE:
            case VCALLNODE:
            case CALLNODE:
                kind = ElementKind.METHOD;
                break;
            case CLASSNODE:
            case SCLASSNODE:
                kind = ElementKind.CLASS;
                break;
            case MODULENODE:
                kind = ElementKind.MODULE;
                break;
            case LOCALVARNODE:
            case LOCALASGNNODE:
            case DVARNODE:
            case DASGNNODE:
                kind = ElementKind.VARIABLE;
                break;
            case ARGUMENTNODE: {
                AstPath _path = getPath();

                if (_path.leafParent() instanceof MethodDefNode) {
                    kind = AstUtilities.isConstructorMethod((MethodDefNode)_path.leafParent())
                        ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
                } else {
                    // TODO - are ArgumentNodes used anywhere else?
                    kind = ElementKind.PARAMETER;
                }
                break;
            }
            case SYMBOLNODE:
                // Ugh - how do I know what it's referring to - a method? a class? a constant? etc.
                if (Character.isUpperCase(((SymbolNode)node).getName().charAt(0))) {
                    kind = ElementKind.CLASS; // Or module? Or constants? How do we know?
                } else {
                    // Or method
                    kind = ElementKind.METHOD;
                }
                break;
            case ALIASNODE:
                // XXX ugh - how do I know what the alias is referring to? For now just guess METHOD, the most common usage
                kind = ElementKind.METHOD;
                break;
            case COLON2NODE:
            case CONSTNODE: {
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
                break;
            }
            case CONSTDECLNODE:
                kind = ElementKind.CONSTANT;
                break;
            case GLOBALVARNODE:
            case GLOBALASGNNODE:
                kind = ElementKind.GLOBAL;
                break;
            case INSTVARNODE:
            case INSTASGNNODE:
            case CLASSVARNODE:
            case CLASSVARASGNNODE:
            case CLASSVARDECLNODE:
                kind = ElementKind.FIELD;
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

    public ParserResult getInfo() {
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

    public void setNames(String name, String simpleName) {
        this.name = name;
        this.simpleName = simpleName;
    }

    public Arity getArity() {
        if (arity == null) {
            if (node instanceof MethodDefNode) {
                arity = Arity.getDefArity(node);
            } else if (AstUtilities.isCall(node)) {
                arity = Arity.getCallArity(node);
            } else if (node instanceof ArgumentNode) {
                AstPath _path = getPath();

                if (_path.leafParent() instanceof MethodDefNode) {
                    arity = Arity.getDefArity(_path.leafParent());
                }
            }
        }

        return arity;
    }

    public BaseDocument getDocument() {
        if (document == null) {
            document = RetoucheUtils.getDocument(info, RubyUtils.getFileObject(info));
        }

        return document;
    }
    
    private String getViewControllerRequire(FileObject view) {
        return null;
    }

    /**
     * If the node is a method call, return the class of the method we're
     * looking for (if any)
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

                RubyType types = RubyType.unknown();
                final RubyType callType = call.getType();
                if (callType.isKnown() && !call.isLHSConstant()) {
                    types = callType;
                }
                String lhs = call.getLhs();

                if (!types.isKnown() && lhs != null && node != null && call.isSimpleIdentifier()) {
                    Node method = AstUtilities.findLocalScope(node, getPath());

                    if (method != null) {
                        // TODO - if the lhs is "foo.bar." I need to split this
                        // up and do it a bit more cleverly
                        ContextKnowledge knowledge =
                            new ContextKnowledge(null, method, node, astOffset, lexOffset, info);
                        RubyTypeInferencer rti = RubyTypeInferencer.create(knowledge, false);
                        types = rti.inferType(lhs);
                    }
                } else if (call == Call.LOCAL) {
                    // Look in the index to see which method it's coming from... 
                    RubyIndex index = RubyIndex.get(info);
                    String fqn = AstUtilities.getFqnName(getPath());

                    if ((fqn == null) || (fqn.length() == 0)) {
                        fqn = RubyIndex.OBJECT;
                    }

                    Set<IndexedMethod> methods = index.getMethods(getName(), fqn, Kind.EXACT);
                    IndexedMethod method = !methods.isEmpty() 
                            ? methods.iterator().next()
                            : index.getSuperMethod(fqn, getName(), false);

                    if (method != null) {
                        defClass = method.getIn();
                    } // else: It's some unqualified method call we don't recognize - perhaps an attribute?
                      // For now just assume it's a method on this class
                }

                if (defClass == null) {
                    // Just an inherited method call?
                    if (!types.isKnown() && (lhs == null || "self".equals(lhs))) {
                        defClass = AstUtilities.getFqnName(getPath());
                    } else if (types.isKnown()) {
                        // TODO handle all types - types.getRealTypes();
                        defClass = types.isSingleton() ? types.first() : null;
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
