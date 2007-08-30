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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.GlobalAsgnNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.HashNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.DeclarationFinder.DeclarationLocation;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.Call;
import org.netbeans.modules.ruby.lexer.RubyCommentTokenId;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 * Find a declaration from an element in the JRuby AST.
 *
 * @todo Look at the target to see which method to choose. For example, if
 *  you do Foo.new, I should locate "initialize" in Foo, not somewhere else.
 * @todo Don't include inexact matches like alias nodes when searching first;
 *   only if a search for actual declaration nodes fail should I revert to looking
 *   for aliases!
 * @todo If you're looking for a local class, such as a Rails model, I should
 *   find those first!
 * 
 * @author Tor Norbye
 */
public class DeclarationFinder implements org.netbeans.api.gsf.DeclarationFinder {
    /** An increasing number; I will be using this number modulo the  */
    private static int methodSelector = 0;

    /** When true, don't match alias nodes as reads. Used during traversal of the AST. */
    private boolean ignoreAlias;

    /** Creates a new instance of DeclarationFinder */
    public DeclarationFinder() {
    }

    public OffsetRange getReferenceSpan(Document document, int lexOffset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        
        BaseDocument doc = (BaseDocument)document;
        if (RubyUtils.isRhtmlDocument(doc)) {
            RailsTarget target = findRailsTarget(doc, th, lexOffset);
            if (target != null) {
                return target.range;
            }
        }
        
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

        if (ts == null) {
            return OffsetRange.NONE;
        }

        ts.move(lexOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return OffsetRange.NONE;
        }

        // Determine whether the caret position is right between two tokens
        boolean isBetween = (lexOffset == ts.offset());

        OffsetRange range = getReferenceSpan(ts, th, lexOffset);

        if ((range == OffsetRange.NONE) && isBetween) {
            // The caret is between two tokens, and the token on the right
            // wasn't linkable. Try on the left instead.
            if (ts.movePrevious()) {
                range = getReferenceSpan(ts, th, lexOffset);
            }
        }

        return range;
    }

    private OffsetRange getReferenceSpan(TokenSequence<?extends TokenId> ts,
        TokenHierarchy<Document> th, int lexOffset) {
        Token<?extends TokenId> token = ts.token();
        TokenId id = token.id();

        // TODO: Tokens.SUPER, Tokens.THIS, Tokens.SELF ...
        if ((id == GsfTokenId.IDENTIFIER) || (id == GsfTokenId.CLASS_VAR) ||
                (id == GsfTokenId.GLOBAL_VAR) || (id == GsfTokenId.CONSTANT) ||
                (id == GsfTokenId.TYPE_SYMBOL) || (id == GsfTokenId.INSTANCE_VAR)) {
            return new OffsetRange(ts.offset(), ts.offset() + token.length());
        }

        // Look for embedded RDoc comments:
        TokenSequence<?extends TokenId> embedded = ts.embedded();

        if (embedded != null) {
            ts = embedded;
            embedded.move(lexOffset);

            if (embedded.moveNext()) {
                Token<?extends TokenId> embeddedToken = embedded.token();

                if (embeddedToken.id() == RubyCommentTokenId.COMMENT_LINK) {
                    return new OffsetRange(embedded.offset(),
                        embedded.offset() + embeddedToken.length());
                }
                // Recurse into the range - perhaps there is Ruby code (identifiers

                // etc.) to follow there
                OffsetRange range = getReferenceSpan(embedded, th, lexOffset);

                if (range != OffsetRange.NONE) {
                    return range;
                }
            }
        }

        // Allow hyperlinking of some literal strings too, such as require strings
        if ((id == RubyTokenId.QUOTED_STRING_LITERAL) || (id == RubyTokenId.STRING_LITERAL)) {
            int requireStart = LexUtilities.getRequireStringOffset(lexOffset, th);

            if (requireStart != -1) {
                String require = LexUtilities.getStringAt(lexOffset, th);

                if (require != null) {
                    return new OffsetRange(requireStart, requireStart + require.length());
                }
            }
        }

        return OffsetRange.NONE;
    }

    public DeclarationLocation findDeclaration(CompilationInfo info, int lexOffset) {
        // Is this a require-statement? If so, jump to the required file
        try {
            Document document = info.getDocument();
            TokenHierarchy<Document> th = TokenHierarchy.get(document);
            BaseDocument doc = (BaseDocument)document;

            int astOffset = AstUtilities.getAstOffset(info, lexOffset);
            if (astOffset == -1) {
                return DeclarationLocation.NONE;
            }

            if (RubyUtils.isRhtmlFile(info.getFileObject())) {
                DeclarationLocation loc = findRailsFile(info, doc, th, lexOffset, astOffset);

                if (loc != DeclarationLocation.NONE) {
                    return loc;
                }
            }

            OffsetRange range = getReferenceSpan(doc, lexOffset);

            if (range == OffsetRange.NONE) {
                return DeclarationLocation.NONE;
            }

            // Determine the bias (if the caret is between two tokens, did we
            // click on a link for the left or the right?
            boolean leftSide = range.getEnd() <= lexOffset;

            Node root = AstUtilities.getRoot(info);

            if (root == null) {
                // No parse tree - try to just use the syntax info to do a simple index lookup
                // for methods and classes
                String text = doc.getText(range.getStart(), range.getLength());
                RubyIndex index = RubyIndex.get(info.getIndex());

                if ((index == null) || (text.length() == 0)) {
                    return DeclarationLocation.NONE;
                }

                if (Character.isUpperCase(text.charAt(0))) {
                    // A Class or Constant?
                    Set<IndexedClass> classes =
                        index.getClasses(text, NameKind.EXACT_NAME, true, false, false);

                    if (classes.size() == 0) {
                        return DeclarationLocation.NONE;
                    }

                    try {
                        IndexedClass candidate =
                            findBestClassMatch(classes, (BaseDocument)info.getDocument(), null,
                                null, index);

                        if (candidate != null) {
                            IndexedElement com = candidate;
                            Node node = AstUtilities.getForeignNode(com, null);

                            return new DeclarationLocation(com.getFile().getFileObject(),
                                node.getPosition().getStartOffset());
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                } else {
                    // A method?
                    Set<IndexedMethod> methods =
                        index.getMethods(text, null, NameKind.EXACT_NAME, RubyIndex.SOURCE_SCOPE);

                    if (methods.size() == 0) {
                        methods = index.getMethods(text, null, NameKind.EXACT_NAME);
                    }

                    try {
                        IndexedMethod candidate =
                            findBestMethodMatch(text, methods, (BaseDocument)info.getDocument(),
                                astOffset, lexOffset, null, null, index);

                        if (candidate != null) {
                            IndexedElement com = candidate;
                            Node node = AstUtilities.getForeignNode(com, null);

                            return new DeclarationLocation(com.getFile().getFileObject(),
                                node.getPosition().getStartOffset());
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                } // TODO: @ - field?

                return DeclarationLocation.NONE;
            }

            RubyIndex index = RubyIndex.get(info.getIndex());

            int tokenOffset = lexOffset;

            if (leftSide && (tokenOffset > 0)) {
                tokenOffset--;
            }

            // See if the hyperlink is for the string in a require statement
            int requireStart = LexUtilities.getRequireStringOffset(tokenOffset, th);

            if (requireStart != -1) {
                String require = LexUtilities.getStringAt(tokenOffset, th);

                if (require != null) {
                    String file = index.getRequiredFileUrl(require);

                    if (file != null) {
                        FileObject fo = RubyIndex.getFileObject(file);

                        return new DeclarationLocation(fo, 0);
                    }
                }

                // It's in a require string so no possible other matches
                return DeclarationLocation.NONE;
            }

            AstPath path = new AstPath(root, astOffset);
            Node closest = path.leaf();

            // See if the hyperlink is over a method reference in an rdoc comment
            DeclarationLocation rdoc = findRDocMethod(info, doc, astOffset, lexOffset, root, path, closest, index);

            if (rdoc != DeclarationLocation.NONE) {
                return fix(rdoc, info);
            }

            // Look at the parse tree; find the closest node and jump based on the context
            if (closest instanceof LocalVarNode || closest instanceof LocalAsgnNode) {
                // A local variable read or a parameter read, or an assignment to one of these
                String name = ((INameNode)closest).getName();
                Node method = AstUtilities.findLocalScope(closest, path);

                return fix(findLocal(info, method, name), info);
            } else if (closest instanceof DVarNode) {
                // A dynamic variable read or assignment
                String name = ((DVarNode)closest).getName(); // Does not implement INameNode
                Node block = AstUtilities.findDynamicScope(closest, path);

                return fix(findDynamic(info, block, name), info);
            } else if (closest instanceof DAsgnNode) {
                // A dynamic variable read or assignment
                String name = ((INameNode)closest).getName();
                Node block = AstUtilities.findDynamicScope(closest, path);

                return fix(findDynamic(info, block, name), info);
            } else if (closest instanceof InstVarNode) {
                // A field variable read
                return fix(findInstance(info, root, ((INameNode)closest).getName()), info);
            } else if (closest instanceof ClassVarNode) {
                // A class variable read
                return fix(findClassVar(info, root, ((ClassVarNode)closest).getName()), info);
            } else if (closest instanceof GlobalVarNode) {
                // A global variable read
                String name = ((GlobalVarNode)closest).getName(); // GlobalVarNode does not implement INameNode

                return fix(findGlobal(info, root, name), info);
            } else if (closest instanceof FCallNode || closest instanceof VCallNode ||
                    closest instanceof CallNode) {
                // A method call
                String name = ((INameNode)closest).getName();

                Call call = Call.getCallType(doc, th, lexOffset);

                String type = call.getType();
                String lhs = call.getLhs();

                if ((type == null) && (lhs != null) && (closest != null) &&
                        call.isSimpleIdentifier()) {
                    Node method = AstUtilities.findLocalScope(closest, path);

                    if (method != null) {
                        // TODO - if the lhs is "foo.bar." I need to split this
                        // up and do it a bit more cleverly
                        TypeAnalyzer analyzer = new TypeAnalyzer(method, closest, astOffset, lexOffset, doc, info.getFileObject());
                        type = analyzer.getType(lhs);
                    }
                }

                // Constructors: "new" ends up calling "initialize".
                // Actually, it's more complicated than this: a method CAN override new
                // in which case I should show it, but that is discouraged and people
                // SHOULD override initialize, which is what the default new method will
                // call for initialization.
                if (type == null) { // unknown type - search locally

                    if (name.equals("new")) { // NOI18N
                        name = "initialize"; // NOI18N
                    }

                    Arity arity = Arity.getCallArity(closest);

                    DeclarationLocation loc = fix(findMethod(info, root, name, arity), info);

                    if (loc != DeclarationLocation.NONE) {
                        return loc;
                    }
                }

                String fqn = AstUtilities.getFqnName(path);

                return findMethod(name, fqn, type, info, astOffset, lexOffset, path, closest, index);
            } else if (closest instanceof ConstNode || closest instanceof Colon2Node) {
                // POSSIBLY a class usage.
                String name = ((INameNode)closest).getName();
                Node localClass = findClass(root, name);

                if (localClass != null) {
                    // Ensure that we have the right FQN if specific
                    if (closest instanceof Colon2Node) {
                        AstPath classPath = new AstPath(root, localClass);

                        if (classPath.leaf() != null) {
                            String fqn1 = AstUtilities.getFqn((Colon2Node)closest);
                            String fqn2 = AstUtilities.getFqnName(classPath);

                            if (fqn1.equals(fqn2)) {
                                return fix(getLocation(info, localClass), info);
                            }
                        } else {
                            assert false : localClass.toString();
                        }
                    } else {
                        return fix(getLocation(info, localClass), info);
                    }
                }

                if (closest instanceof Colon2Node) {
                    name = AstUtilities.getFqn((Colon2Node)closest);
                }

                // E.g. for "include Assertions" within Test::Unit::TestCase, try looking
                // for Test::Unit::TestCase::Assertions, Test::Unit:Assertions, Test::Assertions.
                // And for "include Util::Backtracefilter" try Test::Unit::Util::Backtracefilter etc.
                String fqn = AstUtilities.getFqnName(path);

                return findClass(name, fqn, info, path, closest, index);
            } else if (closest instanceof SymbolNode) {
                String name = ((SymbolNode)closest).getName();

                // Search for methods, fields, etc.
                Arity arity = Arity.UNKNOWN;
                DeclarationLocation location = findMethod(info, root, name, arity);

                if (location == DeclarationLocation.NONE) {
                    location = findInstance(info, root, name);
                }

                if (location == DeclarationLocation.NONE) {
                    location = findClassVar(info, root, name);
                }

                if (location == DeclarationLocation.NONE) {
                    location = findGlobal(info, root, name);
                }

                if (location == DeclarationLocation.NONE) {
                    Node clz = findClass(root, ((INameNode)closest).getName());

                    if (clz != null) {
                        location = getLocation(info, clz);
                    }
                }

                return fix(location, info);
            } else if (closest instanceof AliasNode) {
                AliasNode an = (AliasNode)closest;

                // TODO - determine if the click is over the new name or the old name
                String newName = an.getNewName();

                // XXX I don't know where the old and new names are since the user COULD
                // have used more than one whitespace character for separation. For now I'll
                // just have to assume it's the normal case with one space:  alias new old. 
                // I -could- use the getPosition.getEndOffset() to see if this looks like it's
                // the case (e.g. node length != "alias ".length + old.length+new.length+1).
                // In this case I could go peeking in the source buffer to see where the
                // spaces are - between alias and the first word or between old and new. XXX.
                int newLength = newName.length();
                int aliasPos = an.getPosition().getStartOffset();

                if (astOffset > aliasPos+6) { // 6: "alias ".length()

                    if (astOffset > (aliasPos + 6 + newLength)) {
                        // It's over the old word: this counts as a usage.
                        // The problem is that we don't know if it's a local, a dynamic, an instance
                        // variable, etc. (The $ and @ parts are not included in the alias statement).
                        // First see if it's a local variable.
                        String name = an.getOldName();
                        ignoreAlias = true;

                        try {
                            DeclarationLocation location =
                                findLocal(info, AstUtilities.findLocalScope(closest, path), name);

                            if (location == DeclarationLocation.NONE) {
                                location = findDynamic(info, AstUtilities.findDynamicScope(closest, path),
                                        name);
                            }

                            if (location == DeclarationLocation.NONE) {
                                location = findMethod(info, root, name, Arity.UNKNOWN);
                            }

                            if (location == DeclarationLocation.NONE) {
                                location = findInstance(info, root, name);
                            }

                            if (location == DeclarationLocation.NONE) {
                                location = findClassVar(info, root, name);
                            }

                            if (location == DeclarationLocation.NONE) {
                                location = findGlobal(info, root, name);
                            }

                            if (location == DeclarationLocation.NONE) {
                                Node clz = findClass(root, name);

                                if (clz != null) {
                                    location = getLocation(info, clz);
                                }
                            }

                            // TODO - what if we're aliasing another alias? I think that should show up in the various
                            // other nodes
                            if (location == DeclarationLocation.NONE) {
                                return location;
                            } else {
                                return fix(location, info);
                            }
                        } finally {
                            ignoreAlias = false;
                        }
                    } else {
                        // It's over the new word: this counts as a declaration. Nothing to do here except
                        // maybe jump right back to the beginning.
                        return new DeclarationLocation(info.getFileObject(), aliasPos + 4);
                    }
                }
            } else if (closest instanceof ArgumentNode) {
                // A method name (if under a DefnNode or DefsNode) or a parameter (if indirectly under an ArgsNode)
                String name = ((ArgumentNode)closest).getName(); // ArgumentNode doesn't implement INameNode

                Node parent = path.leafParent();

                if (parent != null) {
                    if (parent instanceof MethodDefNode) {
                        // It's a method name
                        return DeclarationLocation.NONE;
                    } else {
                        // Parameter (check to see if its under ArgumentNode)
                        Node method = AstUtilities.findLocalScope(closest, path);

                        return fix(findLocal(info, method, name), info);
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return DeclarationLocation.NONE;
    }
    
    private DeclarationLocation findRailsFile(CompilationInfo info, BaseDocument doc, TokenHierarchy<Document> th, int lexOffset, int astOffset) {
        RailsTarget target = findRailsTarget(doc, th, lexOffset);
        if (target != null) {
            String type = target.type;
            if (type.indexOf("partial") != -1) { // NOI18N
                
                FileObject dir;
                String name;
                int slashIndex = target.name.lastIndexOf('/');
                if (slashIndex != -1) {
                    
                    // Find app dir, and build up a relative path to the view file in the process
                    FileObject app = info.getFileObject().getParent();

                    while (app != null) {
                        if (app.getName().equals("views") && // NOI18N
                                ((app.getParent() == null) || app.getParent().getName().equals("app"))) { // NOI18N
                            app = app.getParent();

                            break;
                        }

                        app = app.getParent();
                    }
                    
                    if (app == null) {
                        return DeclarationLocation.NONE;
                    }
                    
                    String relativePath = target.name.substring(0, slashIndex);
                    dir = app.getFileObject("views/" + relativePath); // NOI18N
                    if (dir == null) {
                        return DeclarationLocation.NONE;
                    }
                    name = "_" + target.name.substring(slashIndex+1); // NOI18N
                    
                } else {
                    dir = info.getFileObject().getParent();
                    name = "_" + target.name; // NOI18N
                }
                
                // Try to find the partial file
                // TODO - any other filetypes I should check
                String[] extensions = { ".rhtml", ".erb", ".html.erb" }; // NOI18N
                FileObject partial = null;
                for (String ext : extensions) {
                    partial = dir.getFileObject(name + ext);
                    if (partial != null) {
                        break;
                    }
                }
                if (partial == null) {
                    // Handle some other file types for the partials
                    for (FileObject child : dir.getChildren()) {
                        if (child.isValid() && !child.isFolder() && child.getName().equals(name)) {
                            partial = child;
                            break;
                        }
                    }
                }

                if (partial != null) {
                    return new DeclarationLocation(partial, 0);
                }
                
            } else if (type.indexOf("controller") != -1 || type.indexOf("action") != -1) { // NOI18N
                // Look for the controller file in the corresponding directory
                FileObject file = info.getFileObject();
                file = file.getParent();
                //FileObject dir = file.getParent();

                String action = null;
                String fileName = file.getName();
                boolean isController = type.indexOf("controller") != -1; // NOI18N
                String path = ""; // NOI18N
                if (isController) {
                    path = target.name;
                } else {
                    if (!fileName.startsWith("_")) { // NOI18N
                                                     // For partials like "_foo", just use the surrounding view
                        path = fileName;
                        action = info.getFileObject().getName();
                    }
                }
                
                // The hyperlink has either the controller or the action, but I should
                // look at the AST to find the other such that the navigation works
                // better. E.g. if you click on :controller=>'foo', and the statement
                // also has an :action=>'bar', we not only jump to FooController we go to
                // the "def bar" in it as well (and vice versa if you click on just :action=>'bar';
                // this normally assumes its the controller associated with the RHTML file unless
                // a different controller is specified
                int delta = target.range.getStart() - lexOffset;
                String[] controllerAction = findControllerAction(info, lexOffset+delta, astOffset+delta);
                if (controllerAction[0] != null) {
                    path = controllerAction[0];
                }
                if (controllerAction[1] != null) {
                    action = controllerAction[1];
                }

                // Find app dir, and build up a relative path to the view file in the process
                FileObject app = file.getParent();

                while (app != null) {
                    if (app.getName().equals("views") && // NOI18N
                            ((app.getParent() == null) || app.getParent().getName().equals("app"))) { // NOI18N
                        app = app.getParent();

                        break;
                    }

                    path = app.getNameExt() + "/" + path; // NOI18N
                    app = app.getParent();
                }

                if (app != null) {
                    FileObject controllerFile = app.getFileObject("controllers/" + path + "_controller.rb"); // NOI18N
                    if (controllerFile != null) {
                        int offset = 0;
                        if (action != null) {
                            offset = AstUtilities.findOffset(controllerFile, action);
                            if (offset < 0) {
                                offset = 0;
                            }
                        }
                        
                        return new DeclarationLocation(controllerFile, offset);
                    }
                }
            }
        }
        
        return DeclarationLocation.NONE;
    }
    
    /** Locate the :action and :controller strings in the hash list that is under the
     * given offsets
     * @return A string[2] where string[0] is the controller or null, and string[1] is the
     *   action or null
     */
    @SuppressWarnings("unchecked")
    private String[] findControllerAction(CompilationInfo info, int lexOffset, int astOffset) {
        String[] result = new String[2];
        
        Node root = AstUtilities.getRoot(info);
        AstPath path = new AstPath(root, astOffset);
        Iterator<Node> it = path.leafToRoot();
        Node prev = null;
        while (it.hasNext()) {
            Node n = it.next();
            
            if (n instanceof HashNode) {
                if (prev instanceof ListNode) { // uhm... why am I going back to prev?
                    List<Node> hashItems = (List<Node>)prev.childNodes();

                    Iterator<Node> hi = hashItems.iterator();
                    while (hi.hasNext()) {
                        String from = null;
                        String to = null;
                        
                        Node f = hi.next();
                        if (f instanceof SymbolNode) {
                            from = ((SymbolNode)f).getName();
                        }
                        
                        if (hi.hasNext()) {
                            Node t = hi.next();
                            if (t instanceof StrNode) {
                                to = ((StrNode)t).getValue().toString();
                            }
                        }
                        
                        if ("controller".equals(from)) { // NOI18N
                            result[0] = to;
                        } else if ("action".equals(from)) { // NOI18N
                            result[1] = to;
                        }
                    }
                    
                    break;
                }
            }
            
            prev = n;
        }
        return result;
    }

    /** A result from findRailsTarget which computes sections that have special
     * hyperlink semantics - like link_to, render :partial, render :action, :controller etc.
     */
    private static class RailsTarget {
        RailsTarget(String type, String name, OffsetRange range) {
            this.type = type;
            this.range = range;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return "RailsTarget(" + type + ", " + name + ", " + range + ")";
        }

        String name;
        OffsetRange range;
        String type;
    }

    private RailsTarget findRailsTarget(BaseDocument doc, TokenHierarchy<Document> th, int lexOffset) {
        try {
            // TODO - limit this to RHTML files only?
            int begin = Utilities.getRowStart(doc, lexOffset);
            if (begin != -1) {
                int end = Utilities.getRowEnd(doc, lexOffset);
                String s = doc.getText(begin, end-begin); // TODO - limit to a narrower region around the caret?
                String[] targets = new String[] { ":partial => ", ":controller => ", ":action => " }; // NOI18N
                for (String target : targets) {
                    int index = s.indexOf(target);
                    if (index != -1) {
                        // Find string
                        int nameOffset = begin+index+target.length();
                        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(th, nameOffset);
                        ts.move(nameOffset);

                        StringBuilder sb = new StringBuilder();
                        while (ts.moveNext() && ts.offset() < end) {
                            Token<?extends TokenId> token = ts.token();
                            TokenId id = token.id();
                            if (id == RubyTokenId.STRING_LITERAL || id == RubyTokenId.QUOTED_STRING_LITERAL) {
                                sb.append(token.text().toString());
                            }

                            if (!"string".equals(id.primaryCategory())) {
                                break;
                            }
                        }
                        int rangeEnd = ts.offset();

                        String name = sb.toString();

                        if (lexOffset <= rangeEnd && lexOffset >= begin+index) {
                            OffsetRange range = new OffsetRange(begin+index, rangeEnd);
                            return new RailsTarget(target, name, range);
                        }
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return null;
    }
    
    // TODO - rewrite to use getInheritedMethods!
    private DeclarationLocation findMethod(String name, String possibleFqn, String type,
        CompilationInfo info, int caretOffset, int lexOffset, AstPath path, Node closest, RubyIndex index) {
        Set<IndexedMethod> methods = Collections.emptySet();

        String fqn = possibleFqn;

        if (type != null) {
            fqn = possibleFqn;

            // Try looking at the libraries too
            while ((methods.size() == 0) && (fqn.length() > 0)) {
                methods = index.getMethods(name, fqn + "::" + type, NameKind.EXACT_NAME);

                int f = fqn.lastIndexOf("::");

                if (f == -1) {
                    break;
                } else {
                    fqn = fqn.substring(0, f);
                }
            }
        }

        if (methods.size() == 0) {
            methods = index.getMethods(name, type, NameKind.EXACT_NAME);
            if (methods.size() == 0 && type != null) {
                methods = index.getMethods(name, null, NameKind.EXACT_NAME);
            }
        }

        try {
            IndexedMethod candidate =
                findBestMethodMatch(name, methods, (BaseDocument)info.getDocument(), caretOffset,
                    lexOffset, path, closest, index);

            if (candidate != null) {
                IndexedElement com = candidate;
                Node node = AstUtilities.getForeignNode(com, null);

                return new DeclarationLocation(com.getFile().getFileObject(),
                    node.getPosition().getStartOffset());
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findClass(String name, String possibleFqn, CompilationInfo info,
        AstPath path, Node closest, RubyIndex index) {
        // Try searching by qualified name by context first, if it's not qualified
        Set<IndexedClass> classes = Collections.emptySet();
        String fqn = possibleFqn;

        // First try looking only at the local scope
        while ((classes.size() == 0) && (fqn.length() > 0)) {
            classes =
                index.getClasses(fqn + "::" + name, NameKind.EXACT_NAME, true, false, // NOI18N
                    false, RubyIndex.SOURCE_SCOPE);

            int f = fqn.lastIndexOf("::"); // NOI18N

            if (f == -1) {
                break;
            } else {
                fqn = fqn.substring(0, f);
            }
        }

        if (classes.size() == 0) {
            classes = index.getClasses(name, NameKind.EXACT_NAME, true, false, false,
                    RubyIndex.SOURCE_SCOPE);
        }

        // If no success with looking only at the source scope, look in libraries as well
        if (classes.size() == 0) {
            fqn = possibleFqn;

            // Try looking at the libraries too
            while ((classes.size() == 0) && (fqn.length() > 0)) {
                classes =
                    index.getClasses(fqn + "::" + name, NameKind.EXACT_NAME, true, false, // NOI18N
                        false);

                int f = fqn.lastIndexOf("::");

                if (f == -1) {
                    break;
                } else {
                    fqn = fqn.substring(0, f);
                }
            }

            if (classes.size() == 0) {
                classes = index.getClasses(name, NameKind.EXACT_NAME, true, false, false);
            }
        }

        try {
            IndexedClass candidate =
                findBestClassMatch(classes, (BaseDocument)info.getDocument(), path, closest, index);

            if (candidate != null) {
                IndexedElement com = candidate;
                Node node = AstUtilities.getForeignNode(com, null);

                return new DeclarationLocation(com.getFile().getFileObject(),
                    node.getPosition().getStartOffset());
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        return DeclarationLocation.NONE;
    }

    /** Locate the method declaration for the given method call */
    public IndexedMethod findMethodDeclaration(CompilationInfo info, Node callNode, AstPath path) {
        int caretOffset = AstUtilities.getCallRange(callNode).getStart();

        // Is this a require-statement? If so, jump to the required file
        try {
            Document doc = info.getDocument();

            // Determine the bias (if the caret is between two tokens, did we
            // click on a link for the left or the right?
            int lexOffset = LexUtilities.getLexerOffset(info, caretOffset);
            if (lexOffset == -1) {
                return null;
            }
            OffsetRange range = getReferenceSpan(doc, lexOffset);

            if (range == OffsetRange.NONE) {
                return null;
            }

            boolean leftSide = range.getEnd() <= caretOffset;

            Node root = AstUtilities.getRoot(info);

            if (root == null) {
                // No parse tree - try to just use the syntax info to do a simple index lookup
                // for methods and classes
                String text = doc.getText(range.getStart(), range.getLength());
                RubyIndex index = RubyIndex.get(info.getIndex());

                if ((index == null) || (text.length() == 0)) {
                    return null;
                }

                if (Character.isUpperCase(text.charAt(0))) {
                    // A Class or Constant?
                    // Not a method call
                    return null;
                } else {
                    // A method?
                    Set<IndexedMethod> methods = index.getMethods(text, null, NameKind.EXACT_NAME);

                    try {
                        IndexedMethod candidate =
                            findBestMethodMatch(text, methods, (BaseDocument)info.getDocument(),
                                caretOffset, lexOffset, null, null, index);

                        return candidate;
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                } // TODO: @ - field?

                return null;
            }

            RubyIndex index = RubyIndex.get(info.getIndex());

            TokenHierarchy<Document> th = TokenHierarchy.get(doc);

            int tokenOffset = caretOffset;

            if (leftSide && (tokenOffset > 0)) {
                tokenOffset--;
            }

            // A method call
            String name = ((INameNode)callNode).getName();

            //Call call = LexUtilities.getCallType(doc, th, caretOffset);
            Set<IndexedMethod> methods = index.getMethods(name, null, NameKind.EXACT_NAME);

            if (name.equals("new")) { // NOI18N
                                      // Also look for initialize

                Set<IndexedMethod> initializeMethods =
                    index.getMethods("initialize", null, NameKind.EXACT_NAME);
                //initializeMethods.size(); methods.size()
                methods.addAll(initializeMethods);
            }

            try {
                IndexedMethod candidate =
                    findBestMethodMatch(name, methods, (BaseDocument)info.getDocument(),
                        caretOffset, lexOffset, path, callNode, index);

                return candidate;
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return null;
    }

    private DeclarationLocation fix(DeclarationLocation location, CompilationInfo info) {
        if ((location != DeclarationLocation.NONE) && (location.getFileObject() == null) &&
                (location.getUrl() == null)) {
            return new DeclarationLocation(info.getFileObject(), location.getOffset());
        }

        return location;
    }

    private DeclarationLocation getLocation(CompilationInfo info, Node node) {
        return new DeclarationLocation(null, LexUtilities.getLexerOffset(info, node.getPosition().getStartOffset()));
    }

    private DeclarationLocation findRDocMethod(CompilationInfo info, Document doc, int astOffset, int lexOffset, 
            Node root, AstPath path, Node closest, RubyIndex index) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        TokenSequence<?extends TokenId> ts = LexUtilities.getRubyTokenSequence((BaseDocument)doc, lexOffset);

        if (ts == null) {
            return DeclarationLocation.NONE;
        }

        ts.move(lexOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return DeclarationLocation.NONE;
        }

        Token<?extends TokenId> token = ts.token();

        TokenSequence<?extends TokenId> embedded = ts.embedded();

        if (embedded != null) {
            ts = embedded;

            embedded.move(lexOffset);

            if (!embedded.moveNext() && !embedded.movePrevious()) {
                return DeclarationLocation.NONE;
            }

            token = embedded.token();
        }

        // Is this a comment? If so, possibly do rdoc-method reference jump
        if ((token != null) && (token.id() == RubyCommentTokenId.COMMENT_LINK)) {
            String method = token.text().toString();

            if (method.startsWith("#")) {
                method = method.substring(1);

                DeclarationLocation loc = findMethod(info, root, method, Arity.UNKNOWN);

                // It looks like "#foo" can refer not just to methods (as rdoc suggested)
                // but to attributes as well - in Rails' initializer.rb this is used
                // in a number of places.
                if (loc == DeclarationLocation.NONE) {
                    loc = findInstance(info, root, "@" + method);
                }

                return loc;
            } else {
                // A URL such as http://netbeans.org - try to open it in a browser!
                try {
                    URL url = new URL(method);

                    return new DeclarationLocation(url);
                } catch (MalformedURLException mue) {
                    // URL is from user source... don't complain with exception dialogs etc.
                    ;
                }
            }
            
            // Probably a Class#method
            int methodIndex = method.indexOf("#");
            if (methodIndex != -1 && methodIndex < method.length()-1) {
                String clz = method.substring(0, methodIndex);
                method = method.substring(methodIndex+1);

                return findMethod(method, clz, null, info, astOffset, lexOffset, path, closest, index);
            }
        }

        return DeclarationLocation.NONE;
    }

    IndexedClass findBestClassMatch(Set<IndexedClass> classes, BaseDocument doc,
        AstPath path, Node reference, RubyIndex index) {
        // Make sure that the best fit method actually has a corresponding valid source location
        // and parse tree
        while (!classes.isEmpty()) {
            IndexedClass clz = findBestClassMatchHelper(classes, doc, path, reference, index);
            Node node = AstUtilities.getForeignNode(clz, null);

            if (node != null) {
                return clz;
            }

            // TODO: Sort results, then pick candidate number modulo methodSelector
            if (!classes.contains(clz)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best class and we keep trying it
                classes.remove(classes.iterator().next());
            } else {
                classes.remove(clz);
            }
        }

        return null;
    }

    // Now that I have a common RubyObject superclass, can I combine this and findBestMethodMatchHelper
    // since there's a lot of code duplication that could be shared by just operating on RubyObjects ?
    private IndexedClass findBestClassMatchHelper(Set<IndexedClass> classes, BaseDocument doc,
        AstPath path, Node reference, RubyIndex index) {
        // 1. First see if the reference is fully qualified. If so the job should
        //   be easier: prune the result set down
        // If I have the fqn, I can also call RubyIndex.getRDocLocation to pick the
        // best candidate
        Set<IndexedClass> candidates = new HashSet<IndexedClass>();

        if (reference instanceof Colon2Node) {
            String fqn = AstUtilities.getFqn((Colon2Node)reference);

            while ((fqn != null) && (fqn.length() > 0)) {
                for (IndexedClass clz : classes) {
                    if (fqn.equals(clz.getSignature())) {
                        candidates.add(clz);
                    }
                }

                // TODO: Use the fqn to check if the class is documented: if so, prefer it

                // Check inherited methods; for example, if we've determined
                // that you're looking for Integer::foo, I should happily match
                // Numeric::foo.
                IndexedClass superClass = index.getSuperclass(fqn);

                if (superClass != null) {
                    fqn = superClass.getSignature();
                } else {
                    break;
                }
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            classes = candidates;
        }

        // 2. See if the reference is followed by a method call - if so, that may
        //   help disambiguate which reference we're after.
        // TODO

        // 3. See which of the class references are defined in files directly
        //   required by this file.
        Set<String> requires = null;

        if (path != null) {
            candidates = new HashSet<IndexedClass>();

            requires = AstUtilities.getRequires(path.root());

            for (IndexedClass clz : classes) {
                String require = clz.getRequire();

                if (requires.contains(require)) {
                    candidates.add(clz);
                }
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (!candidates.isEmpty()) {
                classes = candidates;
            }
        }

        // 4. See if any of the classes are "kernel" classes (builtins) and for these
        //   go to the known locations
        candidates = new HashSet<IndexedClass>();

        for (IndexedClass clz : classes) {
            String url = clz.getFileUrl();

            if (url != null && url.indexOf("rubystubs") != -1) { // NOI18N
                candidates.add(clz);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            classes = candidates;
        }

        // 5. See which classes are documented, and prefer those over undocumented classes
        candidates = new HashSet<IndexedClass>();

        int longestDocLength = 0;

        for (IndexedClass clz : classes) {
            int length = clz.getDocumentationLength();

            if (length > longestDocLength) {
                candidates.clear();
                candidates.add(clz);
                longestDocLength = length;
            } else if ((length > 0) && (length == longestDocLength)) {
                candidates.add(clz);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            classes = candidates;
        }

        // 6. Look at transitive closure of require statements and see which files
        //  are most likely candidates
        if ((index != null) && (requires != null)) {
            candidates = new HashSet<IndexedClass>();

            Set<String> allRequires = index.getRequiresTransitively(requires);

            for (IndexedClass clz : classes) {
                String require = clz.getRequire();

                if (allRequires.contains(require)) {
                    candidates.add(clz);
                }
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (!candidates.isEmpty()) {
                classes = candidates;
            }
        }

        // 7. Other heuristics: Look at the method definition with the
        //   most methods associated with it. Look at other uses of this
        //   class in this parse tree, look at the methods and see if we
        //   can rule out candidates based on that

        // 8. Look at superclasses and consider -their- requires to figure out
        //   which class we're supposed to use
        // TODO
        candidates = new HashSet<IndexedClass>();

        // Pick one arbitrarily
        if (classes.size() > 0) {
            return classes.iterator().next();
        } else {
            return null;
        }
    }

    IndexedMethod findBestMethodMatch(String name, Set<IndexedMethod> methods,
        BaseDocument doc, int astOffset, int lexOffset, AstPath path, Node call, RubyIndex index) {
        // Make sure that the best fit method actually has a corresponding valid source location
        // and parse tree
        while (!methods.isEmpty()) {
            IndexedMethod method =
                findBestMethodMatchHelper(name, methods, doc, astOffset, lexOffset, path, call, index);
            Node node = AstUtilities.getForeignNode(method, null);

            if (node != null) {
                return method;
            }

            if (!methods.contains(method)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best method and we keep trying it
                methods.remove(methods.iterator().next());
            } else {
                methods.remove(method);
            }
        }

        return null;
    }

    private IndexedMethod findBestMethodMatchHelper(String name, Set<IndexedMethod> methods,
        BaseDocument doc, int astOffset, int lexOffset, AstPath path, Node callNode, RubyIndex index) {
        Set<IndexedMethod> candidates = new HashSet<IndexedMethod>();

        // 1. First see if the reference is fully qualified. If so the job should
        //   be easier: prune the result set down
        // If I have the fqn, I can also call RubyIndex.getRDocLocation to pick the
        // best candidate
        if (callNode instanceof CallNode) {
            Node node = ((CallNode)callNode).getReceiverNode();
            String fqn = null;

            if (node instanceof Colon2Node) {
                fqn = AstUtilities.getFqn((Colon2Node)node);
            } else if (node instanceof ConstNode) {
                fqn = ((ConstNode)node).getName();
            }

            if (fqn != null) {
                while ((fqn != null) && (fqn.length() > 0)) {
                    for (IndexedMethod method : methods) {
                        if (fqn.equals(method.getClz())) {
                            candidates.add(method);
                        }
                    }

                    // Check inherited methods; for example, if we've determined
                    // that you're looking for Integer::foo, I should happily match
                    // Numeric::foo.
                    IndexedClass superClass = index.getSuperclass(fqn);

                    if (superClass != null) {
                        fqn = superClass.getSignature();
                    } else {
                        break;
                    }
                }
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            methods = candidates;
        }

        // 2. See if the reference is not qualified (no :: or . prior to
        // the method call; if so it must be an inherited method (or a local
        // method, but we've already checked that possibility before getting
        // into the index search)
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);

        Call call = Call.getCallType(doc, th, lexOffset);
        boolean skipPrivate = true;

        if ((path != null) && (callNode != null) && (call != Call.LOCAL) && (call != Call.NONE)) {
            boolean skipInstanceMethods = call.isStatic();

            candidates = new HashSet<IndexedMethod>();

            String type = call.getType();

            // I'm not doing any data flow analysis at this point, so
            // I can't do anything with a LHS like "foo.". Only actual types.
            if ((type != null) && (type.length() > 0)) {
                String lhs = call.getLhs();

                String fqn = AstUtilities.getFqnName(path);

                // TODO for self and super, rather than computing ALL inherited methods
                // (and picking just one of them), I should use the FIRST match as the
                // one to show! (closest super class or include definition)
                if ("self".equals(lhs)) {
                    type = fqn;
                    skipPrivate = false;
                } else if ("super".equals(lhs)) {
                    skipPrivate = false;

                    IndexedClass sc = index.getSuperclass(fqn);

                    if (sc != null) {
                        type = sc.getFqn();
                    } else {
                        ClassNode cls = AstUtilities.findClass(path);

                        if (cls != null) {
                            type = AstUtilities.getSuperclass(cls);
                        }
                    }
                }

                if ((type != null) && (type.length() > 0)) {
                    // Possibly a class on the left hand side: try searching with the class as a qualifier.
                    // Try with the LHS + current FQN recursively. E.g. if we're in
                    // Test::Unit when there's a call to Foo.x, we'll try
                    // Test::Unit::Foo, and Test::Foo
                    while (candidates.size() == 0) {
                        candidates = index.getInheritedMethods(fqn + "::" + type, name,
                                NameKind.EXACT_NAME);

                        int f = fqn.lastIndexOf("::");

                        if (f == -1) {
                            break;
                        } else {
                            fqn = fqn.substring(0, f);
                        }
                    }

                    // Add methods in the class (without an FQN)
                    if (candidates.size() == 0) {
                        candidates = index.getInheritedMethods(type, name, NameKind.EXACT_NAME);
                    }
                }
            }

            if (skipPrivate || skipInstanceMethods) {
                Set<IndexedMethod> m = new HashSet<IndexedMethod>();

                for (IndexedMethod method : candidates) {
                    // Don't include private or protected methods on other objects
                    if (skipPrivate && (method.isPrivate() && !"new".equals(method.getName()))) {
                        // TODO - "initialize" removal here should not be necessary since they should
                        // be marked as private, but index doesn't contain that yet
                        continue;
                    }

                    // We can only call static methods
                    if (skipInstanceMethods && !method.isStatic()) {
                        continue;
                    }

                    m.add(method);
                }

                candidates = m;
            }

            // First try to limit the candidates down to the ones that match the lhs type, if we
            // are calling new or initialize
            if (type != null /* && ("new".equals(name) || "initialize".equals(name))*/) { // NOI18N

                Set<IndexedMethod> cs = new HashSet<IndexedMethod>();

                for (IndexedMethod m : candidates) {
                    // AppendIO might be the lhs - e.g. AppendIO.new, yet its FQN is Shell::AppendIO
                    // so do suffix comparison
                    if ((m.getIn() != null) && m.getIn().endsWith(type)) {
                        cs.add(m);
                    }
                }

                if (cs.size() < candidates.size()) {
                    candidates = cs;
                }
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            methods = candidates;
        }

        // 3. Use method arity to rule out mismatches
        // TODO - this is tricky since Ruby lets you specify more or fewer
        // parameters with some reasonable behavior...
        // Possibly I should do this check further down since the
        // other heuristics may work better as a first-level disambiguation

        // 4. Check to see which classes are required directly from this file, and
        // prefer matches that are in this set of classes
        Set<String> requires = null;

        if (path != null) {
            candidates = new HashSet<IndexedMethod>();

            requires = AstUtilities.getRequires(path.root());

            for (IndexedMethod method : methods) {
                String require = method.getRequire();

                if (requires.contains(require)) {
                    candidates.add(method);
                }
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (!candidates.isEmpty()) {
                methods = candidates;
            }
        }

        // 3. See if any of the methods are in "kernel" classes (builtins) and for these
        //   go to the known locations
        candidates = new HashSet<IndexedMethod>();

        for (IndexedMethod method : methods) {
            String url = method.getFileUrl();

            if (url != null && url.indexOf("rubystubs") != -1) { // NOI18N
                candidates.add(method);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            methods = candidates;
        }

        // 4. See which methods are documented, and prefer those over undocumented methods
        candidates = new HashSet<IndexedMethod>();

        int longestDocLength = 0;

        for (IndexedMethod method : methods) {
            int length = method.getDocumentationLength();

            if (length > longestDocLength) {
                candidates.clear();
                candidates.add(method);
                longestDocLength = length;
            } else if ((length > 0) && (length == longestDocLength)) {
                candidates.add(method);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            methods = candidates;
        }

        // 5. Look at transitive closure of require statements and see which files
        //  are most likely candidates
        if ((index != null) && (requires != null)) {
            candidates = new HashSet<IndexedMethod>();

            Set<String> allRequires = index.getRequiresTransitively(requires);

            for (IndexedMethod method : methods) {
                String require = method.getRequire();

                if (allRequires.contains(require)) {
                    candidates.add(method);
                }
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (!candidates.isEmpty()) {
                methods = candidates;
            }
        }

        // 6. Other heuristics: Look at the method definition with the
        //   class with most methods associated with it. Look at other uses of this
        //   method in this parse tree and see if I can figure out the containing class
        //   or rule out other candidates based on that

        // 7. Look at superclasses and consider -their- requires to figure out
        //   which class we're looking for methods in
        // TODO

        // Pick one arbitrarily
        if (methods.size() > 0) {
            return methods.iterator().next();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private DeclarationLocation findLocal(CompilationInfo info, Node node, String name) {
        if (node instanceof LocalAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            if (((AliasNode)node).getNewName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (node instanceof ArgsNode) {
            ArgsNode an = (ArgsNode)node;

            if (an.getArgsCount() > 0) {
                List<Node> args = (List<Node>)an.childNodes();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = (List<Node>)arg.childNodes();

                        for (Node arg2 : args2) {
                            if (arg2 instanceof ArgumentNode) {
                                if (((ArgumentNode)arg2).getName().equals(name)) {
                                    return getLocation(info, arg2);
                                }
                            } else if (arg2 instanceof LocalAsgnNode) {
                                if (((LocalAsgnNode)arg2).getName().equals(name)) {
                                    return getLocation(info, arg2);
                                }
                            }
                        }
                    }
                }
            }

            // Rest args
            if (an.getRestArgNode() != null) {
                ArgumentNode bn = an.getRestArgNode();

                if (bn.getName().equals(name)) {
                    return getLocation(info, bn);
                }
            }

            // Block args
            if (an.getBlockArgNode() != null) {
                BlockArgNode bn = an.getBlockArgNode();

                if (bn.getName().equals(name)) {
                    return getLocation(info, bn);
                }
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            DeclarationLocation location = findLocal(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findDynamic(CompilationInfo info, Node node, String name) {
        if (node instanceof DAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            if (((AliasNode)node).getNewName().equals(name)) {
                return getLocation(info, node);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            DeclarationLocation location = findDynamic(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findInstance(CompilationInfo info, Node node, String name) {
        if (node instanceof InstAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            if (((AliasNode)node).getNewName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (AstUtilities.isAttr(node)) {
            // TODO: Compute the symbols and check for equality
            // attr_reader, attr_accessor, attr_writer
            SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);

            for (int i = 0; i < symbols.length; i++) {
                if (name.equals("@" + symbols[i].getName())) {
                    return getLocation(info, symbols[i]);
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            DeclarationLocation location = findInstance(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findClassVar(CompilationInfo info, Node node, String name) {
        if (node instanceof ClassVarDeclNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            if (((AliasNode)node).getNewName().equals(name)) {
                return getLocation(info, node);
            }

            // TODO: Are there attr readers and writers for class variables?
            //        } else if (AstUtilities.isAttrReader(node) || AstUtilities.isAttrWriter(node)) {
            //            // TODO: Compute the symbols and check for equality
            //            // attr_reader, attr_accessor, attr_writer
            //            SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);
            //
            //            for (int i = 0; i < symbols.length; i++) {
            //                if (name.equals("@" + symbols[i].getName())) {
            //                    return getLocation(info, symbols[i]);
            //                }
            //            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            DeclarationLocation location = findClassVar(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findGlobal(CompilationInfo info, Node node, String name) {
        if (node instanceof GlobalAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            if (((AliasNode)node).getNewName().equals(name)) {
                return getLocation(info, node);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            DeclarationLocation location = findGlobal(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findMethod(CompilationInfo info, Node node, String name, Arity arity) {
        // Recursively search for methods or method calls that match the name and arity
        if (node instanceof MethodDefNode) {
            if (((MethodDefNode)node).getName().equals(name) &&
                    Arity.matches(arity, Arity.getDefArity(node))) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            if (((AliasNode)node).getNewName().equals(name)) {
                // No obvious way to check arity
                return getLocation(info, node);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            DeclarationLocation location = findMethod(info, child, name, arity);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private Node findClass(Node node, String name) {
        if (node instanceof ClassNode) {
            String n = AstUtilities.getClassOrModuleName((ClassNode)node);

            if (n.equals(name)) {
                return node;
            }
        } else if (node instanceof ConstDeclNode) {
            if (((INameNode)node).getName().equals(name)) {
                return node;
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            if (((AliasNode)node).getNewName().equals(name)) {
                return node;
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            Node match = findClass(child, name);

            if (match != null) {
                return match;
            }
        }

        return null;
    }
}
