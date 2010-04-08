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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jrubyparser.ast.AliasNode;
import org.jrubyparser.ast.ArgsNode;
import org.jrubyparser.ast.ArgumentNode;
import org.jrubyparser.ast.BlockArgNode;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.ClassVarDeclNode;
import org.jrubyparser.ast.ClassVarNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.DAsgnNode;
import org.jrubyparser.ast.DVarNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.GlobalAsgnNode;
import org.jrubyparser.ast.GlobalVarNode;
import org.jrubyparser.ast.HashNode;
import org.jrubyparser.ast.InstAsgnNode;
import org.jrubyparser.ast.InstVarNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.LocalVarNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.SymbolNode;
import org.jrubyparser.ast.VCallNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.SuperNode;
import org.jrubyparser.ast.ZSuperNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedField;
import org.netbeans.modules.ruby.elements.IndexedMethod;
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
 * @todo Within a gem, prefer other matches within the same gem or gem cluster
 * @todo Prefer files named after the class! (e.g. SchemaStatements in schema_statements.rb)
 * 
 * @author Tor Norbye
 */
public class RubyDeclarationFinder extends RubyDeclarationFinderHelper implements DeclarationFinder {

    /** An increasing number; I will be using this number modulo the  */
    private static int methodSelector = 0;

    /** When true, don't match alias nodes as reads. Used during traversal of the AST. */
    private boolean ignoreAlias;

    private RubyIndex rubyIndex;

    private static final String PARTIAL = "partial"; //NOI18N
    private static final String CONTROLLER = "controller"; //NOI18N
    private static final String ACTION =  "action";//NOI18N
    private static final String TEMPLATE = "template";//NOI18N
    private static final String[] RAILS_TARGET_RAW_NAMES = new String[] {PARTIAL, CONTROLLER, ACTION, TEMPLATE};

    private static final List<String> RAILS_TARGETS = initRailsTargets();

    private static List<String> initRailsTargets() {
        List<String> result = new ArrayList<String>(RAILS_TARGET_RAW_NAMES.length * 4);
        for (String target : RAILS_TARGET_RAW_NAMES) {
            result.add(":" + target + " => ");
            result.add(":" + target + "=> ");
            result.add(":" + target + " =>");
            result.add(":" + target + "=>");
        }
        return result;
    }
    /** Creates a new instance of RubyDeclarationFinder */
    public RubyDeclarationFinder() {
    }

    private RubyIndex getIndex(ParserResult result) {
        if (rubyIndex == null) {
            rubyIndex = RubyIndex.get(result);
        }
        return rubyIndex;
    }

    public OffsetRange getReferenceSpan(Document document, int lexOffset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        
        BaseDocument doc = (BaseDocument)document;
        FileObject fo = RubyUtils.getFileObject(document);
        if (RubyUtils.isRhtmlDocument(doc) || (fo != null && RubyUtils.isRailsProject(fo))) {
            RailsTarget target = findRailsTarget(doc, th, lexOffset);
            if (target != null) {
                return target.range;
            }
        }
        
        TokenSequence<?extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(th, lexOffset);

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

    private OffsetRange getReferenceSpan(TokenSequence<?> ts,
        TokenHierarchy<Document> th, int lexOffset) {
        Token<?> token = ts.token();
        TokenId id = token.id();

        if (id == RubyTokenId.IDENTIFIER) {
            if (token.length() == 1 && id == RubyTokenId.IDENTIFIER && token.text().toString().equals(",")) {
                return OffsetRange.NONE;
            }
        }

        // TODO: Tokens.THIS, Tokens.SELF ...
        if ((id == RubyTokenId.IDENTIFIER) || (id == RubyTokenId.CLASS_VAR) ||
                (id == RubyTokenId.GLOBAL_VAR) || (id == RubyTokenId.CONSTANT) ||
                (id == RubyTokenId.TYPE_SYMBOL) || (id == RubyTokenId.INSTANCE_VAR) ||
                (id == RubyTokenId.SUPER)) {
            return new OffsetRange(ts.offset(), ts.offset() + token.length());
        }

        // Look for embedded RDoc comments:
        TokenSequence<?> embedded = ts.embedded();

        if (embedded != null) {
            ts = embedded;
            embedded.move(lexOffset);

            if (embedded.moveNext()) {
                Token<?> embeddedToken = embedded.token();

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
            
            int classNameStart = LexUtilities.getClassNameStringOffset(lexOffset, th);
            if (classNameStart != -1) {
                String className = LexUtilities.getStringAt(lexOffset, th);
                if (className != null) {
                    return new OffsetRange(classNameStart, classNameStart + className.length());
                }
            }
        }

        return OffsetRange.NONE;
    }

    public DeclarationLocation findDeclaration(ParserResult parserResult, int lexOffset) {
        // Is this a require-statement? If so, jump to the required file
        try {
            Document document = RubyUtils.getDocument(parserResult, true);
            if (document == null) {
                return DeclarationLocation.NONE;
            }
            TokenHierarchy<Document> th = TokenHierarchy.get(document);
            BaseDocument doc = (BaseDocument)document;

            int astOffset = AstUtilities.getAstOffset(parserResult, lexOffset);
            if (astOffset == -1) {
                return DeclarationLocation.NONE;
            }

            boolean view = RubyUtils.isRhtmlFile(RubyUtils.getFileObject(parserResult));
            if (view || RubyUtils.isRailsProject(RubyUtils.getFileObject(parserResult))) {
                DeclarationLocation loc = findRailsFile(parserResult, doc, th, lexOffset, astOffset, view);

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

            Node root = AstUtilities.getRoot(parserResult);

            RubyIndex index = getIndex(parserResult);
            if (root == null) {
                // No parse tree - try to just use the syntax info to do a simple index lookup
                // for methods and classes
                String text = doc.getText(range.getStart(), range.getLength());

                if ((index == null) || (text.length() == 0)) {
                    return DeclarationLocation.NONE;
                }

                if (Character.isUpperCase(text.charAt(0))) {
                    // A Class or Constant?
                    Set<IndexedClass> classes =
                        index.getClasses(text, QuerySupport.Kind.EXACT, true, false, false);

                    if (classes.size() == 0) {
                        return DeclarationLocation.NONE;
                    }

                    RubyClassDeclarationFinder cdf = new RubyClassDeclarationFinder(null, null, null, index, null);
                    DeclarationLocation l = cdf.getElementDeclaration(classes, null);

                    if (l != null) {
                        return l;
                    }
                } else {
                    // A method?
                    Set<IndexedMethod> methods =
                        index.getMethods(text, (String) null, QuerySupport.Kind.EXACT);

                    if (methods.size() == 0) {
                        methods = index.getMethods(text, QuerySupport.Kind.EXACT);
                    }

                    DeclarationLocation l = getMethodDeclaration(parserResult, text, methods,
                         null, null, index, astOffset, lexOffset);

                    if (l != null) {
                        return l;
                    }
                } // TODO: @ - field?

                return DeclarationLocation.NONE;
            }

            int tokenOffset = lexOffset;

            if (leftSide && (tokenOffset > 0)) {
                tokenOffset--;
            }

            // See if the hyperlink is for the string in a require statement
            int requireStart = LexUtilities.getRequireStringOffset(tokenOffset, th);

            if (requireStart != -1) {
                String require = LexUtilities.getStringAt(tokenOffset, th);

                if (require != null) {
                    FileObject fo = index.getRequiredFile(require);

                    if (fo != null) {
                        return new DeclarationLocation(fo, 0);
                    }
                }

                // It's in a require string so no possible other matches
                return DeclarationLocation.NONE;
            }

            AstPath path = new AstPath(root, astOffset);
            Node closest = path.leaf();
            if (closest == null) {
                return DeclarationLocation.NONE;
            }

            // See if the hyperlink is over a method reference in an rdoc comment
            DeclarationLocation rdoc = findRDocMethod(parserResult, doc, astOffset, lexOffset, root, path, closest, index);

            if (rdoc != DeclarationLocation.NONE) {
                return fix(rdoc, parserResult);
            }

            // Look at the parse tree; find the closest node and jump based on the context
            if (closest instanceof LocalVarNode || closest instanceof LocalAsgnNode) {
                // A local variable read or a parameter read, or an assignment to one of these
                String name = ((INameNode)closest).getName();
                Node method = AstUtilities.findLocalScope(closest, path);

                return fix(findLocal(parserResult, method, name), parserResult);
            } else if (closest instanceof DVarNode) {
                // A dynamic variable read or assignment
                String name = ((DVarNode)closest).getName(); // Does not implement INameNode
                Node block = AstUtilities.findDynamicScope(closest, path);

                return fix(findDynamic(parserResult, block, name), parserResult);
            } else if (closest instanceof DAsgnNode) {
                // A dynamic variable read or assignment
                String name = ((INameNode)closest).getName();
                Node block = AstUtilities.findDynamicScope(closest, path);

                return fix(findDynamic(parserResult, block, name), parserResult);
            } else if (closest instanceof InstVarNode) {
                // A field variable read
                String name = ((INameNode)closest).getName();
                return findInstanceFromIndex(parserResult, name, path, index, false);
            } else if (closest instanceof ClassVarNode) {
                // A class variable read
                String name = ((INameNode)closest).getName();
                return findInstanceFromIndex(parserResult, name, path, index, false);
            } else if (closest instanceof GlobalVarNode) {
                // A global variable read
                String name = ((GlobalVarNode)closest).getName(); // GlobalVarNode does not implement INameNode

                return fix(findGlobal(parserResult, root, name), parserResult);
            } else if (closest instanceof FCallNode || closest instanceof VCallNode ||
                    closest instanceof CallNode) {
                // A method call
                String name = ((INameNode)closest).getName();

                Call call = Call.getCallType(doc, th, lexOffset);

                RubyType type = call.getType();
                String lhs = call.getLhs();

                if (!type.isKnown() && lhs != null && closest != null &&
                        call.isSimpleIdentifier()) {
                    Node method = AstUtilities.findLocalScope(closest, path);

                    if (method != null) {
                        // TODO - if the lhs is "foo.bar." I need to split this
                        // up and do it a bit more cleverly
                        ContextKnowledge knowledge = new ContextKnowledge(
                                index, root, method, astOffset, lexOffset, parserResult);
                        RubyTypeInferencer inferencer = RubyTypeInferencer.create(knowledge);
                        type = inferencer.inferType(lhs);
                    }
                }

                // Constructors: "new" ends up calling "initialize".
                // Actually, it's more complicated than this: a method CAN override new
                // in which case I should show it, but that is discouraged and people
                // SHOULD override initialize, which is what the default new method will
                // call for initialization.
                if (!type.isKnown()) { // search locally

                    if (name.equals("new")) { // NOI18N
                        name = "initialize"; // NOI18N
                    }

                    Arity arity = Arity.getCallArity(closest);

                    DeclarationLocation loc = fix(findMethod(parserResult, root, name, arity), parserResult);

                    if (loc != DeclarationLocation.NONE) {
                        return loc;
                    }
                }

                String fqn = AstUtilities.getFqnName(path);
                if (call == Call.LOCAL && fqn != null && fqn.length() == 0) {
                    fqn = "Object";
                }

                return findMethod(name, fqn, type, call, parserResult, astOffset, lexOffset, path, closest, index);
            } else if (closest instanceof ConstNode || closest instanceof Colon2Node) {
                // try Class usage
                RubyClassDeclarationFinder classDF = new RubyClassDeclarationFinder(parserResult, root, path, index, closest);
                DeclarationLocation decl = classDF.findClassDeclaration();
                if (decl != DeclarationLocation.NONE) {
                    return decl;
                }
                // try Constant usage
                RubyConstantDeclarationFinder constantDF = new RubyConstantDeclarationFinder(parserResult, root, path, index, closest);
                return constantDF.findConstantDeclaration();
            } else if (closest instanceof SymbolNode) {
                String name = ((SymbolNode)closest).getName();

                // Search for methods, fields, etc.
                Arity arity = Arity.UNKNOWN;
                DeclarationLocation location = findMethod(parserResult, root, name, arity);

                // search for AR associations
                if (location == DeclarationLocation.NONE) {
                    location = new ActiveRecordAssociationFinder(index, (SymbolNode) closest, root, path).findAssociationLocation();
                }

                // search for helpers
                if (location == DeclarationLocation.NONE) {
                    location = new HelpersFinder(index, (SymbolNode) closest, root, path).findHelperLocation();
                }

                if (location == DeclarationLocation.NONE) {
                    location = findInstance(parserResult, root, name, index);
                }

                if (location == DeclarationLocation.NONE) {
                    location = findClassVar(parserResult, root, name);
                }

                if (location == DeclarationLocation.NONE) {
                    location = findGlobal(parserResult, root, name);
                }

                if (location == DeclarationLocation.NONE) {
                    RubyClassDeclarationFinder cdf = new RubyClassDeclarationFinder();
                    Node clz = cdf.findClass(root, ((INameNode)closest).getName(), ignoreAlias);

                    if (clz != null) {
                        location = getLocation(parserResult, clz);
                    }
                }

                // methods
                if (location == DeclarationLocation.NONE) {
                    location = findInstanceMethodsFromIndex(parserResult, name, path, index);
                }
                // fields
                if (location == DeclarationLocation.NONE) {
                    location = findInstanceFromIndex(parserResult, name, path, index, true);
                }

                return fix(location, parserResult);
            } else if (closest instanceof AliasNode) {
                AliasNode an = (AliasNode)closest;

                // TODO - determine if the click is over the new name or the old name
                String newName = AstUtilities.getNameOrValue(an.getNewName());
                if (newName == null) {
                    return DeclarationLocation.NONE;
                }

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
                        String name = AstUtilities.getNameOrValue(an.getOldName());
                        if (name == null) {
                            return DeclarationLocation.NONE;
                        }
                        ignoreAlias = true;

                        try {
                            DeclarationLocation location =
                                findLocal(parserResult, AstUtilities.findLocalScope(closest, path), name);

                            if (location == DeclarationLocation.NONE) {
                                location = findDynamic(parserResult, AstUtilities.findDynamicScope(closest, path),
                                        name);
                            }

                            if (location == DeclarationLocation.NONE) {
                                location = findMethod(parserResult, root, name, Arity.UNKNOWN);
                            }

                            if (location == DeclarationLocation.NONE) {
                                location = findInstance(parserResult, root, name, index);
                            }

                            if (location == DeclarationLocation.NONE) {
                                location = findClassVar(parserResult, root, name);
                            }

                            if (location == DeclarationLocation.NONE) {
                                location = findGlobal(parserResult, root, name);
                            }

                            if (location == DeclarationLocation.NONE) {
                                RubyClassDeclarationFinder cdf = new RubyClassDeclarationFinder();
                                Node clz = cdf.findClass(root, name, ignoreAlias);

                                if (clz != null) {
                                    location = getLocation(parserResult, clz);
                                }
                            }

                            // TODO - what if we're aliasing another alias? I think that should show up in the various
                            // other nodes
                            if (location == DeclarationLocation.NONE) {
                                return location;
                            } else {
                                return fix(location, parserResult);
                            }
                        } finally {
                            ignoreAlias = false;
                        }
                    } else {
                        // It's over the new word: this counts as a declaration. Nothing to do here except
                        // maybe jump right back to the beginning.
                        return new DeclarationLocation(RubyUtils.getFileObject(parserResult), aliasPos + 4);
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

                        return fix(findLocal(parserResult, method, name), parserResult);
                    }
                } 
            } else if (closest instanceof StrNode) {
                    // See if the hyperlink is for the string that is the value for :class_name =>
                    int classNameStart = LexUtilities.getClassNameStringOffset(astOffset, th);
                    if (classNameStart != -1) {
                        String className = LexUtilities.getStringAt(tokenOffset, th);
                        if (className != null) {
                            return getLocation(index.getClasses(className, QuerySupport.Kind.EXACT, true, false, false));
                        }
                    }
            } else if (closest instanceof SuperNode || closest instanceof ZSuperNode) {
                Node scope = AstUtilities.findLocalScope(closest, path);
                String fqn = AstUtilities.getFqnName(path);
                switch (scope.getNodeType()) {
                    case SCLASSNODE:
                    case MODULENODE:
                    case CLASSNODE: {
                        IndexedClass superClass = index.getSuperclass(fqn);
                        if (superClass != null) {
                            return getLocation(Collections.singleton(superClass));
                        }
                        break;
                    }
                    case DEFNNODE:
                    case DEFSNODE: {
                        MethodDefNode methodDef = AstUtilities.findMethod(path);
                        IndexedMethod superMethod = index.getSuperMethod(fqn, methodDef.getName(), true);
                        if (superMethod != null) {
                            return getLocation(Collections.singleton(superMethod));
                        }
                        break;
                    }
                }
            }
        } catch (BadLocationException ble) {
            // do nothing - see #154991
        }

        return DeclarationLocation.NONE;
    }

    /** 
     * Compute the declaration location for a test string (such as MosModule::TestBaz/test_qux).
     * 
     * @param fileInProject a file in the project where to perform the search
     * @param testString a string represening a test class and method, such as TestFoo/test_bar
     * @param classLocation if true, returns the location of the class rather then the method.
     */
    public static DeclarationLocation getTestDeclaration(FileObject fileInProject, String testString, boolean classLocation) {
        return getTestDeclaration(fileInProject, testString, classLocation, true);
    }

    public static DeclarationLocation getTestDeclaration(FileObject fileInProject, String testString, 
            boolean classLocation, boolean requireDeclaredClass) {
        
        int methodIndex = testString.indexOf('/'); //NOI18N
        if (methodIndex == -1) {
            return DeclarationLocation.NONE;
        }

        RubyIndex index = RubyIndex.get(QuerySupport.findRoots(fileInProject,
                Collections.singleton(RubyLanguage.SOURCE),
                Collections.singleton(RubyLanguage.BOOT),
                Collections.<String>emptySet()));

        if (index == null) {
            return DeclarationLocation.NONE;
        }

        String className = testString.substring(0, methodIndex);
        String methodName = testString.substring(methodIndex+1);

        Set<IndexedMethod> methods = index.getMethods(methodName, className, QuerySupport.Kind.EXACT);
        DeclarationLocation methodLocation = getLocation(methods);
        if (!classLocation) {
            if (DeclarationLocation.NONE == methodLocation && !requireDeclaredClass) {
                // the test method is not defined in the class
                methodLocation = getLocation(index.getMethods(methodName, QuerySupport.Kind.EXACT));
            }
            return methodLocation;
        }
        Set<IndexedClass> classes =
                index.getClasses(className, QuerySupport.Kind.EXACT, false, false, true, null);
        DeclarationLocation classDeclarationLocation = getLocation(classes);
        
        if (DeclarationLocation.NONE == methodLocation && classLocation) {
            return classDeclarationLocation;
        }
        if (methodLocation.getFileObject().equals(classDeclarationLocation.getFileObject())) {
            return classDeclarationLocation;
        }

        for (AlternativeLocation alt : classDeclarationLocation.getAlternativeLocations()) {
            if (methodLocation.getFileObject().equals(alt.getLocation().getFileObject())) {
                return alt.getLocation();
            }
        }

        return classDeclarationLocation;
    }

    static DeclarationLocation getLocation(Set<? extends IndexedElement> elements) {
        DeclarationLocation loc = DeclarationLocation.NONE;
        for (IndexedElement element : elements) {
            FileObject fo = element.getFileObject();
            if (fo == null) {
                continue;
            }
            if (loc == DeclarationLocation.NONE) {
                int offset = -1;
                Node node = AstUtilities.getForeignNode(element);
                if (node != null) {
                    offset = AstUtilities.getRange(node).getStart();
                }
                loc = new DeclarationLocation(fo, offset, element);
                loc.addAlternative(new RubyAltLocation(element, false));
            } else {
                AlternativeLocation alternate = new RubyAltLocation(element, false);
                loc.addAlternative(alternate);
            }
        }
        return loc;
    }

    private DeclarationLocation findRailsFile(ParserResult info, BaseDocument doc, 
            TokenHierarchy<Document> th, int lexOffset, int astOffset, boolean fromView) {
        RailsTarget target = findRailsTarget(doc, th, lexOffset);
        if (target != null) {
            String type = target.type;
            if (type.indexOf(PARTIAL) != -1 || type.indexOf(TEMPLATE) != -1) { // NOI18N

                boolean template = type.indexOf(TEMPLATE) != -1;
                FileObject dir;
                String name;
                int slashIndex = target.name.lastIndexOf('/');
                if (slashIndex != -1) {
                    
                    FileObject app = RubyUtils.getAppDir(RubyUtils.getFileObject(info));
                    if (app == null) {
                        return DeclarationLocation.NONE;
                    }
                    
                    String relativePath = target.name.substring(0, slashIndex);
                    dir = app.getFileObject("views/" + relativePath); // NOI18N
                    if (dir == null) {
                        return DeclarationLocation.NONE;
                    }
                    name = target.name.substring(slashIndex+1); // NOI18N
                    
                } else {
                    dir = RubyUtils.getFileObject(info).getParent();
                    name = target.name; // NOI18N
                }

                if (!template) {
                    name = "_" + name;
                }
                
                DeclarationLocation partialLocation = findPartial(name, dir);
                if (partialLocation != DeclarationLocation.NONE) {
                    return partialLocation;
                }
            } else if (type.indexOf(CONTROLLER) != -1 || type.indexOf(ACTION) != -1) { // NOI18N
                // Look for the controller file in the corresponding directory
                FileObject file = RubyUtils.getFileObject(info);
                file = file.getParent();
                //FileObject dir = file.getParent();

                String action = null;
                String fileName = file.getName();
                boolean isController = type.indexOf(CONTROLLER) != -1; // NOI18N
                String path = ""; // NOI18N
                if (isController) {
                    path = target.name;
                } else {
                    if (!fileName.startsWith("_")) { // NOI18N
                                                     // For partials like "_foo", just use the surrounding view
                        path = fileName;
                        action = RubyUtils.getFileObject(info).getName();
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

                if (!fromView) {
                    // uh, this is getting really messy - hard to add funtionality here
                    // without breaking existing functionality. this an attempt to fix
                    // IZ 172679 w/o affect navigation from views. the class is in
                    // need of serious refactoring.
                    String controllerName = null;
                    if (controllerAction[0] != null) {
                        controllerName = controllerAction[0];
                    } else if (isController) {
                        controllerName = target.name;
                    } else {
                        controllerName = RubyUtils.getFileObject(info).getName();
                    }
                    return findActionLocation(asControllerClass(controllerName), action, info);
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

    private static String asControllerClass(String controllerName) {
        String suffix = controllerName.endsWith("_controller") ? "" : "_controller";//NOI18N
        return RubyUtils.underlinedNameToCamel(controllerName + suffix);
    }

    private DeclarationLocation findActionLocation(String controllerName, String actionName, ParserResult result) {
        RubyIndex index = getIndex(result);
        Set<IndexedMethod> methods = index.getMethods(actionName, controllerName, QuerySupport.Kind.EXACT);
        return getLocation(methods);
    }

    /**
     * Finds the location of the partial matching the given <code>name</code> in the
     * given <code>dir</code>.
     * 
     * @param name
     * @param dir
     * @return
     */
    private DeclarationLocation findPartial(String name, FileObject dir) {
        // Try to find the partial file
        FileObject partial = dir.getFileObject(name);
        if (partial != null) {
            return new DeclarationLocation(partial, 0);
        }
        // try extensions
        for (String ext : RubyUtils.RUBY_VIEW_EXTS) {
            partial = dir.getFileObject(name + ext);
            if (partial != null) {
                return new DeclarationLocation(partial, 0);

            }
        }
        // Handle some other file types for the partials
        for (FileObject child : dir.getChildren()) {
            if (child.isValid() && !child.isFolder() && child.getName().equals(name)) {
                return new DeclarationLocation(child, 0);
            }
        }

        // finally, try matching just the first part of the file name
        for (FileObject child : dir.getChildren()) {
            if (child.isValid() && !child.isFolder()) {
                String fileName = child.getName();
                int firstDot = fileName.indexOf('.');
                if (firstDot != -1 && name.equals(fileName.substring(0, firstDot))) {
                    return new DeclarationLocation(child, 0);
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
    private String[] findControllerAction(ParserResult info, int lexOffset, int astOffset) {
        String[] result = new String[2];
        
        Node root = AstUtilities.getRoot(info);
        if (root == null) {
            return result;
        }
        AstPath path = new AstPath(root, astOffset);
        Iterator<Node> it = path.leafToRoot();
        Node prev = null;
        while (it.hasNext()) {
            Node n = it.next();
            
            if (n instanceof HashNode) {
                if (prev instanceof ListNode) { // uhm... why am I going back to prev?
                    List<Node> hashItems = prev.childNodes();

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

    private boolean fastCheckIsRailsTarget(String s) {
        for (String targetName : RAILS_TARGET_RAW_NAMES) {
            if (s.indexOf(targetName) != -1) {
                return true;
            }
        }
        return false;

    }

    private RailsTarget findRailsTarget(BaseDocument doc, TokenHierarchy<Document> th, int lexOffset) {
        try {
            doc.readLock();
            // TODO - limit this to RHTML files only?
            int begin = Utilities.getRowStart(doc, lexOffset);
            if (begin != -1) {
                int end = Utilities.getRowEnd(doc, lexOffset);
                String s = doc.getText(begin, end-begin); // TODO - limit to a narrower region around the caret?
                if (!fastCheckIsRailsTarget(s)) {
                    return null;
                }
                for (String target : RAILS_TARGETS) {
                    int index = s.indexOf(target);
                    if (index != -1) {
                        // Find string
                        int nameOffset = begin+index+target.length();
                        TokenSequence<?extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(th, nameOffset);
                        if (ts == null) {
                            return null;
                        }

                        ts.move(nameOffset);

                        StringBuilder sb = new StringBuilder();
                        boolean started = false;
                        while (ts.moveNext() && ts.offset() < end) {
                            started = true;
                            Token<?> token = ts.token();
                            TokenId id = token.id();
                            if (id == RubyTokenId.STRING_LITERAL || id == RubyTokenId.QUOTED_STRING_LITERAL) {
                                sb.append(token.text().toString());
                            }

                            if (!"string".equals(id.primaryCategory())) {
                                break;
                            }
                        }
                        if (!started) {
                            return null;
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
        } finally {
            doc.readUnlock();
        }

        return null;
    }
    
    private DeclarationLocation findMethod(String name, String possibleFqn, RubyType type, Call call,
        ParserResult info, int caretOffset, int lexOffset, AstPath path, Node closest, RubyIndex index) {
        Set<IndexedMethod> methods = getApplicableMethods(name, possibleFqn, type, call, index);

        int astOffset = caretOffset;
        DeclarationLocation l = getMethodDeclaration(info, name, methods, 
             path, closest, index, astOffset, lexOffset);

        return l;
    }

    private Set<IndexedMethod> getApplicableMethods(String name, String possibleFqn, 
            RubyType type, Call call, RubyIndex index) {
        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        String fqn = possibleFqn;
        if (!type.isKnown() && possibleFqn != null && call.getLhs() == null && call != Call.UNKNOWN) {
            fqn = possibleFqn;

            // Possibly a class on the left hand side: try searching with the class as a qualifier.
            // Try with the LHS + current FQN recursively. E.g. if we're in
            // Test::Unit when there's a call to Foo.x, we'll try
            // Test::Unit::Foo, and Test::Foo
            while (methods.size() == 0 && (fqn.length() > 0)) {
                methods = index.getInheritedMethods(fqn, name, QuerySupport.Kind.EXACT);

                int f = fqn.lastIndexOf("::");

                if (f == -1) {
                    break;
                } else {
                    fqn = fqn.substring(0, f);
                }
            }
        }

        if (type.isKnown() && methods.size() == 0) {
            fqn = possibleFqn;

            // Possibly a class on the left hand side: try searching with the class as a qualifier.
            // Try with the LHS + current FQN recursively. E.g. if we're in
            // Test::Unit when there's a call to Foo.x, we'll try
            // Test::Unit::Foo, and Test::Foo
            while (methods.size() == 0 && fqn != null && (fqn.length() > 0)) {
                for (String realType : type.getRealTypes()) {
                    methods.addAll(index.getInheritedMethods(fqn + "::" + realType, name, QuerySupport.Kind.EXACT));
                }

                int f = fqn.lastIndexOf("::");

                if (f == -1) {
                    break;
                } else {
                    fqn = fqn.substring(0, f);
                }
            }

            if (methods.size() == 0) {
                // Add methods in the class (without an FQN)
                for (String realType : type.getRealTypes()) {
                    methods.addAll(index.getInheritedMethods(realType, name, QuerySupport.Kind.EXACT));
                }
                
                if (methods.size() == 0) {
                    for (String realType : type.getRealTypes()) {
                        assert realType != null : "Should not be null";
                        if (realType.indexOf("::") == -1) {
                            // Perhaps we specified a class without its FQN, such as "TableDefinition"
                            // -- go and look for the full FQN and add in all the matches from there
                            Set<IndexedClass> classes = index.getClasses(realType, QuerySupport.Kind.EXACT, false, false, false);
                            Set<String> fqns = new HashSet<String>();
                            for (IndexedClass cls : classes) {
                                String f = cls.getFqn();
                                if (f != null) {
                                    fqns.add(f);
                                }
                            }
                            for (String f : fqns) {
                                if (!f.equals(realType)) {
                                    methods.addAll(index.getInheritedMethods(f, name, QuerySupport.Kind.EXACT));
                                }
                            }
                        }
                    }
                }
            }
            
            // Fall back to ALL methods across classes
            // Try looking at the libraries too
            if (methods.size() == 0) {
                methods.addAll(index.getMethods(name, QuerySupport.Kind.EXACT));
            }
        }

        if (methods.size() == 0) {
            if (!type.isKnown()) {
                methods.addAll(index.getMethods(name, QuerySupport.Kind.EXACT));
            } else {
                methods.addAll(index.getMethods(name, type.getRealTypes(), QuerySupport.Kind.EXACT));
            }
            if (methods.size() == 0 && type.isKnown()) {
                methods = index.getMethods(name, QuerySupport.Kind.EXACT);
            }
        }
        
        return methods;
    }

    private DeclarationLocation getMethodDeclaration(ParserResult info, String name, Set<IndexedMethod> methods,
            AstPath path, Node closest, RubyIndex index, int astOffset, int lexOffset) {
        BaseDocument doc = RubyUtils.getDocument(info);
        if (doc == null) {
            return DeclarationLocation.NONE;
        }

        IndexedMethod candidate =
            findBestMethodMatch(name, methods, doc,
                astOffset, lexOffset, path, closest, index);

        if (candidate != null) {
            FileObject fileObject = candidate.getFileObject();
            if (fileObject == null) {
                return DeclarationLocation.NONE;
            }

            Node node = AstUtilities.getForeignNode(candidate);
            int nodeOffset = 0;
            if (node != null) {
                nodeOffset = node.getPosition().getStartOffset();
                if (node.getNodeType() == NodeType.ALIASNODE) {
                    nodeOffset += 6; // 6 = lenght of 'alias '
                }
            }

            DeclarationLocation loc = new DeclarationLocation(
                fileObject, nodeOffset, candidate);

            if (!CHOOSE_ONE_DECLARATION && methods.size() > 1) {
                // Could the :nodoc: alternatives: if there is only one nodoc'ed alternative
                // don't ask user!
                int not_nodoced = 0;
                for (final IndexedMethod mtd : methods) {
                    if (!mtd.isNoDoc()) {
                        not_nodoced++;
                    }
                }
                if (not_nodoced >= 2) {
                    for (final IndexedMethod mtd : methods) {
                        loc.addAlternative(new RubyAltLocation(mtd, mtd == candidate));
                    }
                }
            }

            return loc;
        }
     
        return DeclarationLocation.NONE;
    }
    
    /** Locate the method declaration for the given method call */
    public IndexedMethod findMethodDeclaration(Parser.Result parserResult, Node callNode, AstPath path,
            Set<IndexedMethod>[] alternativesHolder) {
        int astOffset = AstUtilities.getCallRange(callNode).getStart();

        // Is this a require-statement? If so, jump to the required file
        try {
            Document doc = RubyUtils.getDocument(parserResult);
            if (doc == null) {
                return null;
            }

            // Determine the bias (if the caret is between two tokens, did we
            // click on a link for the left or the right?
            int lexOffset = LexUtilities.getLexerOffset(parserResult, astOffset);
            if (lexOffset == -1) {
                return null;
            }
            OffsetRange range = getReferenceSpan(doc, lexOffset);

            if (range == OffsetRange.NONE) {
                return null;
            }

            boolean leftSide = range.getEnd() <= astOffset;

            Node root = AstUtilities.getRoot(parserResult);

            RubyIndex index = RubyIndex.get(parserResult);
            if (root == null) {
                // No parse tree - try to just use the syntax info to do a simple index lookup
                // for methods and classes
                String text = doc.getText(range.getStart(), range.getLength());


                if ((index == null) || (text.length() == 0)) {
                    return null;
                }

                if (Character.isUpperCase(text.charAt(0))) {
                    // A Class or Constant?
                    // Not a method call
                    return null;
                } else {
                    // A method?
                    Set<IndexedMethod> methods = index.getMethods(text, QuerySupport.Kind.EXACT);

                    BaseDocument bdoc = (BaseDocument)doc;
                    IndexedMethod candidate =
                        findBestMethodMatch(text, methods, bdoc,
                            astOffset, lexOffset, null, null, index);

                    return candidate;
                } // TODO: @ - field?
            }

            TokenHierarchy<Document> th = TokenHierarchy.get(doc);

            int tokenOffset = astOffset;

            if (leftSide && (tokenOffset > 0)) {
                tokenOffset--;
            }

            // A method call
            String name = ((INameNode)callNode).getName();
            String fqn = AstUtilities.getFqnName(path);

            if ((fqn == null) || (fqn.length() == 0)) {
                fqn = "Object"; // NOI18N
            }
            
            Call call = Call.getCallType((BaseDocument)doc, th, lexOffset);
            boolean skipPrivate = true;
            boolean done = call.isMethodExpected();
            boolean skipInstanceMethods = call.isStatic();

            RubyType type = call.getType();
            String lhs = call.getLhs();
            QuerySupport.Kind kind = QuerySupport.Kind.EXACT;

            Node node = callNode;
            if ((!type.isKnown()) && (lhs != null) && (node != null) && call.isSimpleIdentifier()) {
                Node method = AstUtilities.findLocalScope(node, path);

                if (method != null) {
                    // TODO - if the lhs is "foo.bar." I need to split this
                    // up and do it a bit more cleverly
                    ContextKnowledge knowledge = new ContextKnowledge(
                            index, root, method, astOffset, lexOffset, AstUtilities.getParseResult(parserResult));
                    RubyTypeInferencer inferencer = RubyTypeInferencer.create(knowledge);
                    type = inferencer.inferType(lhs);
                }
            }

            // I'm not doing any data flow analysis at this point, so
            // I can't do anything with a LHS like "foo.". Only actual types.
            if (type.isKnown()) {
                if ("self".equals(lhs)) {
                    type = RubyType.create(fqn);
                    skipPrivate = false;
                } else if ("super".equals(lhs)) {
                    skipPrivate = false;

                    IndexedClass sc = index.getSuperclass(fqn);

                    if (sc != null) {
                        type = RubyType.create(sc.getFqn());
                    } else {
                        ClassNode cls = AstUtilities.findClass(path);

                        if (cls != null) {
                            type = RubyType.create(AstUtilities.getSuperclass(cls));
                        }
                    }

                    if (!type.isKnown()) {
                        type = RubyType.OBJECT; // NOI18N
                    }
                }
            }
            if (call == Call.LOCAL && fqn != null && fqn.length() == 0) {
                fqn = "Object";
            }

            Set<IndexedMethod> methods = getApplicableMethods(name, fqn, type, call, index);
            
            if (name.equals("new")) { // NOI18N
                // Also look for initialize
                Set<IndexedMethod> initializeMethods = getApplicableMethods("initialize", fqn, type, call, index);
                methods.addAll(initializeMethods);
            }

            IndexedMethod candidate =
                findBestMethodMatch(name, methods, (BaseDocument)doc,
                    astOffset, lexOffset, path, callNode, index);

            if (alternativesHolder != null) {
                alternativesHolder[0] = methods;
            }
            return candidate;
        } catch (BadLocationException ble) {
            // do nothing - see #154991
        }

        return null;
    }

    @SuppressWarnings("empty-statement")
    private DeclarationLocation findRDocMethod(ParserResult info, Document doc, int astOffset, int lexOffset,
            Node root, AstPath path, Node closest, RubyIndex index) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        TokenSequence<?> ts = LexUtilities.getRubyTokenSequence((BaseDocument)doc, lexOffset);

        if (ts == null) {
            return DeclarationLocation.NONE;
        }

        ts.move(lexOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return DeclarationLocation.NONE;
        }

        Token<?> token = ts.token();

        TokenSequence<?> embedded = ts.embedded();

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
            // TODO - use findLinkedMethod
            String method = token.text().toString();

            if (method.startsWith("#")) {
                method = method.substring(1);

                DeclarationLocation loc = findMethod(info, root, method, Arity.UNKNOWN);

                // It looks like "#foo" can refer not just to methods (as rdoc suggested)
                // but to attributes as well - in Rails' initializer.rb this is used
                // in a number of places.
                if (loc == DeclarationLocation.NONE) {
                    loc = findInstance(info, root, "@" + method, index);
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

                return findMethod(method, null, RubyType.create(clz), Call.UNKNOWN,
                        info, astOffset, lexOffset, path, closest, index);
            }
        }

        return DeclarationLocation.NONE;
    }
    
    @SuppressWarnings("empty-statement")
    DeclarationLocation findLinkedMethod(ParserResult info, String method) {
        Node root = AstUtilities.getRoot(info);
        AstPath path = new AstPath();
        path.descend(root);
        Node closest = root;
        int astOffset = 0;
        int lexOffset = 0;
        RubyIndex index = getIndex(info);

        if (root == null) {
            return DeclarationLocation.NONE;
        }

        if (method.startsWith("#")) {
            method = method.substring(1);

            DeclarationLocation loc = findMethod(info, root, method, Arity.UNKNOWN);

            // It looks like "#foo" can refer not just to methods (as rdoc suggested)
            // but to attributes as well - in Rails' initializer.rb this is used
            // in a number of places.
            if (loc == DeclarationLocation.NONE) {
                loc = findInstance(info, root, "@" + method, index);
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

            return findMethod(method, null, RubyType.create(clz), Call.UNKNOWN, info, astOffset, lexOffset, path, closest, index);
        }
        
        return DeclarationLocation.NONE;
    }

    IndexedMethod findBestMethodMatch(String name, Set<IndexedMethod> methodSet,
        BaseDocument doc, int astOffset, int lexOffset, AstPath path, Node call, RubyIndex index) {
        // Make sure that the best fit method actually has a corresponding valid source location
        // and parse tree

        Set<IndexedMethod> methods = new HashSet<IndexedMethod>(methodSet);
        
        while (!methods.isEmpty()) {
            IndexedMethod method =
                findBestMethodMatchHelper(name, methods, doc, astOffset, lexOffset, path, call, index);
            Node node = AstUtilities.getForeignNode(method);

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
        
        // Dynamic methods that don't have source (such as the TableDefinition methods "binary", "boolean", etc.
        if (methodSet.size() > 0) {
            return methodSet.iterator().next();
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

            RubyType type = call.getType();

            // I'm not doing any data flow analysis at this point, so
            // I can't do anything with a LHS like "foo.". Only actual types.
            if (type.isKnown()) {
                String lhs = call.getLhs();

                String fqn = AstUtilities.getFqnName(path);

                // TODO for self and super, rather than computing ALL inherited methods
                // (and picking just one of them), I should use the FIRST match as the
                // one to show! (closest super class or include definition)
                if ("self".equals(lhs)) {
                    type = RubyType.create(fqn);
                    skipPrivate = false;
                } else if ("super".equals(lhs)) {
                    skipPrivate = false;

                    IndexedClass sc = index.getSuperclass(fqn);

                    if (sc != null) {
                        type = RubyType.create(sc.getFqn());
                    } else {
                        ClassNode cls = AstUtilities.findClass(path);

                        if (cls != null) {
                            type = RubyType.create(AstUtilities.getSuperclass(cls));
                        }
                    }
                }

                if (type.isKnown()) {
                    // Possibly a class on the left hand side: try searching with the class as a qualifier.
                    // Try with the LHS + current FQN recursively. E.g. if we're in
                    // Test::Unit when there's a call to Foo.x, we'll try
                    // Test::Unit::Foo, and Test::Foo
                    while (candidates.size() == 0) {
                        candidates = index.getInheritedMethods(fqn + "::" + type, name,
                                QuerySupport.Kind.EXACT);

                        int f = fqn.lastIndexOf("::");

                        if (f == -1) {
                            break;
                        } else {
                            fqn = fqn.substring(0, f);
                        }
                    }

                    // Add methods in the class (without an FQN)
                    if (candidates.size() == 0) {
                        candidates = index.getInheritedMethods(type, name, QuerySupport.Kind.EXACT);
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
                    if ((m.getIn() != null) && type.isSingleton() && m.getIn().endsWith(type.first())) {
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
        
        // 3. Prefer methods with extra index attributes since these tend to be important
        // methods (e.g. pick ActiveRecord::ConnectionAdapters::SchemaStatements instead
        // of the many overrides of that method
        // (A more general solution would be to prefer ancestor classes' implementations
        // over superclasses' implementations
        candidates = new HashSet<IndexedMethod>();

        for (IndexedMethod method : methods) {
            String attributes = method.getEncodedAttributes();
            if (attributes != null && attributes.length() > 3) {
                candidates.add(method);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            methods = candidates;
        }

        // 4. Use method arity to rule out mismatches
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

            if (RubyUtils.isRubyStubsURL(url)) {
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

    private DeclarationLocation findLocal(ParserResult info, Node node, String name) {
        if (node instanceof LocalAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            String newName = AstUtilities.getNameOrValue(((AliasNode)node).getNewName());
            if (name.equals(newName)) {
                return getLocation(info, node);
            }
        } else if (node instanceof ArgsNode) {
            ArgsNode an = (ArgsNode)node;

            if (an.getRequiredCount() > 0) {
                List<Node> args = an.childNodes();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = arg.childNodes();

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
            if (an.getRest() != null) {
                ArgumentNode bn = an.getRest();

                if (bn.getName().equals(name)) {
                    return getLocation(info, bn);
                }
            }

            // Block args
            if (an.getBlock() != null) {
                BlockArgNode bn = an.getBlock();

                if (bn.getName().equals(name)) {
                    return getLocation(info, bn);
                }
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            DeclarationLocation location = findLocal(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findDynamic(ParserResult info, Node node, String name) {
        if (node instanceof DAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            String newName = AstUtilities.getNameOrValue(((AliasNode)node).getNewName());
            if (name.equals(newName)) {
                return getLocation(info, node);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            DeclarationLocation location = findDynamic(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findInstance(ParserResult info, Node node, String name, RubyIndex index) {
        if (node instanceof InstAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            String newName = AstUtilities.getNameOrValue(((AliasNode)node).getNewName());
            if (name.equals(newName)) {
                return getLocation(info, node);
            }
        } else if (AstUtilities.isAttr(node)) {
            // TODO: Compute the symbols and check for equality
            // attr_reader, attr_accessor, attr_writer
            SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);

            for (int i = 0; i < symbols.length; i++) {
                // possibly an instance variable referred by attr_accessor and like
                if (name.equals(symbols[i].getName())) {
                    Node root = AstUtilities.getRoot(info);
                    DeclarationLocation location =
                            findInstanceFromIndex(info, name, new AstPath(root, node), index, true);
                    if (location != DeclarationLocation.NONE) {
                        return location;
                    }
                    return getLocation(info, symbols[i]);
                }
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            DeclarationLocation location = findInstance(info, child, name, index);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findClassVar(ParserResult info, Node node, String name) {
        if (node instanceof ClassVarDeclNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            String newName = AstUtilities.getNameOrValue(((AliasNode)node).getNewName());
            if (name.equals(newName)) {
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

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            DeclarationLocation location = findClassVar(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findInstanceFromIndex(ParserResult info, String name, AstPath path, RubyIndex index, boolean inherited) {
        String fqn = AstUtilities.getFqnName(path);

        // TODO - if fqn has multiple ::'s, try various combinations? or is 
        // add inherited already doing that?
        Set<IndexedField> f = index.getInheritedFields(fqn, name, QuerySupport.Kind.EXACT, inherited);
        for (IndexedField field : f) {
            // How do we choose one?
            // For now, just pick the first one
            
            Node node = AstUtilities.getForeignNode(field);

            if (node != null) {
                return new DeclarationLocation(field.getFileObject(),
                    node.getPosition().getStartOffset(), field);
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findInstanceMethodsFromIndex(ParserResult info, String name, AstPath path, RubyIndex index) {
        String fqn = AstUtilities.getFqnName(path);
        Set<IndexedMethod> methods = index.getInheritedMethods(fqn, name, QuerySupport.Kind.EXACT);
        return getLocation(methods);
    }

    private DeclarationLocation findGlobal(ParserResult info, Node node, String name) {
        if (node instanceof GlobalAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            String newName = AstUtilities.getNameOrValue(((AliasNode)node).getNewName());
            if (name.equals(newName)) {
                return getLocation(info, node);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            DeclarationLocation location = findGlobal(info, child, name);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation findMethod(ParserResult info, Node node, String name, Arity arity) {
        // Recursively search for methods or method calls that match the name and arity
        if (node instanceof MethodDefNode) {
            if (((MethodDefNode)node).getName().equals(name) &&
                    Arity.matches(arity, Arity.getDefArity(node))) {
                return getLocation(info, node);
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            String newName = AstUtilities.getNameOrValue(((AliasNode)node).getNewName());
            if (name.equals(newName)) {
                // No obvious way to check arity
                return getLocation(info, node);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            DeclarationLocation location = findMethod(info, child, name, arity);

            if (location != DeclarationLocation.NONE) {
                return location;
            }
        }

        return DeclarationLocation.NONE;
    }

}
