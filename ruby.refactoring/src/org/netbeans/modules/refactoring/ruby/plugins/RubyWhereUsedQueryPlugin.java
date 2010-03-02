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
package org.netbeans.modules.refactoring.ruby.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.text.Document;
import org.jrubyparser.ast.AliasNode;
import org.jrubyparser.ast.ArgumentNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.DAsgnNode;
import org.jrubyparser.ast.DVarNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.LocalVarNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.SymbolNode;
import org.jrubyparser.ast.INameNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.ruby.RetoucheUtils;
import org.netbeans.modules.refactoring.ruby.RubyElementCtx;
import org.netbeans.modules.refactoring.ruby.WhereUsedElement;
import org.netbeans.modules.refactoring.ruby.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.Element;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Actual implementation of Find Usages query search for Ruby
 * 
 * @todo Perform index lookups to determine the set of files to be checked!
 * @todo Scan comments!
 * @todo Do more prechecks of the elements we're trying to find usages for
 * 
 * @author  Jan Becicka
 * @author Tor Norbye
 */
public class RubyWhereUsedQueryPlugin extends RubyRefactoringPlugin {
    
    private final WhereUsedQuery refactoring;
    private final RubyElementCtx searchHandle;
    private Set<IndexedClass> subclasses;
    private final String targetName;
    
    /** Creates a new instance of WhereUsedQuery */
    public RubyWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
        this.searchHandle = refactoring.getRefactoringSource().lookup(RubyElementCtx.class);
        targetName = searchHandle.getSimpleName();
    }
    
//    protected Source getRubySource(Phase p) {
//        switch (p) {
//        default:
//            return RetoucheUtils.getSource(searchHandle.getFileObject());
//        }
//    }
    
    @Override
    public Problem preCheck() {
        if (!searchHandle.getFileObject().isValid()) {
            return new Problem(true, NbBundle.getMessage(RubyWhereUsedQueryPlugin.class, "DSC_ElNotAvail")); // NOI18N
        }
        
        return null;
    }
    
    private Set<FileObject> getRelevantFiles(final RubyElementCtx tph) {
        Set<FileObject> set = new HashSet<FileObject>();
                
        FileObject file = tph.getFileObject();
        if (file != null) {
            set.add(file);
//            source = RetoucheUtils.createSource(cpInfo, tph.getFileObject());
            if (isFindSubclasses() || isFindDirectSubclassesOnly()) {
                // No need to do any parsing, we'll be using the index to find these files!
                set.add(file);
                String name = tph.getName();

                // Find overrides of the class
                RubyIndex index = RubyIndex.get(Collections.singleton(file));
                String fqn = AstUtilities.getFqnName(tph.getPath());
                subclasses = index.getSubClasses(null, fqn, name, isFindDirectSubclassesOnly());

                if (subclasses.size() > 0) {
//                        for (IndexedClass clz : classes) {
//                            FileObject fo = clz.getFileObject();
//                            if (fo != null) {
//                                set.add(fo);
//                            }
//                        }
                    // For now just parse this particular file!
                    set.add(file);
                }
            }

            if (tph.getKind() == ElementKind.VARIABLE || tph.getKind() == ElementKind.PARAMETER) {
                // For local variables, only look in the current file!
                set.add(file);
            } else {
                set.addAll(RetoucheUtils.getRubyFilesInProject(file));
            }
        }
        return set;
//                final Element el = tph.resolveElement(info);
//                if (el.getKind().isField()) {
//                    //get field references from index
//                    set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//                } else if (el.getKind().isClass() || el.getKind().isInterface()) {
//                    if (isFindSubclasses()||isFindDirectSubclassesOnly()) {
//                        if (isFindDirectSubclassesOnly()) {
//                            //get direct implementors from index
//                            EnumSet searchKind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
//                            set.addAll(idx.getResources(ElementHandle.create((TypeElement)el), searchKind,EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//                        } else {
//                            //itererate implementors recursively
//                            set.addAll(getImplementorsRecursive(idx, cpInfo, (TypeElement)el));
//                        }
//                    } else {
//                        //get type references from index
//                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//                    }
//                } else if (el.getKind() == ElementKind.METHOD && isFindOverridingMethods()) {
//                    //Find overriding methods
//                    TypeElement type = (TypeElement) el.getEnclosingElement();
//                    set.addAll(getImplementorsRecursive(idx, cpInfo, type));
//                } 
//                if (el.getKind() == ElementKind.METHOD && isFindUsages()) {
//                    //get method references for method and for all it's overriders
//                    Set<ElementHandle<TypeElement>> s = idx.getElements(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE));
//                    for (ElementHandle<TypeElement> eh:s) {
//                        TypeElement te = eh.resolve(info);
//                        if (te==null) {
//                            continue;
//                        }
//                        for (Element e:te.getEnclosedElements()) {
//                            if (e instanceof ExecutableElement) {
//                                if (info.getElements().overrides((ExecutableElement)e, (ExecutableElement)el, te)) {
//                                    set.addAll(idx.getResources(ElementHandle.create(te), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//                                }
//                            }
//                        }
//                    }
//                    set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
//                } else if (el.getKind() == ElementKind.CONSTRUCTOR) {
//                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//                }
//                    
    }
    
//    private Set<FileObject> getImplementorsRecursive(ClassIndex idx, ClasspathInfo cpInfo, TypeElement el) {
//        Set<FileObject> set = new HashSet<FileObject>();
//        LinkedList<ElementHandle<TypeElement>> elements = new LinkedList(idx.getElements(ElementHandle.create(el),
//                EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
//                EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//        HashSet<ElementHandle> result = new HashSet();
//        while(!elements.isEmpty()) {
//            ElementHandle<TypeElement> next = elements.removeFirst();
//            result.add(next);
//            elements.addAll(idx.getElements(next,
//                    EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
//                    EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//        }
//        for (ElementHandle<TypeElement> e : result) {
//            FileObject fo = SourceUtils.getFile(e, cpInfo);
//            assert fo != null: "issue 90196, Cannot find file for " + e + ". cpInfo=" + cpInfo ;
//            set.add(fo);
//        }
//        return set;
//    }
    
    //@Override
    public Problem prepare(final RefactoringElementsBag elements) {
        Set<FileObject> a = getRelevantFiles(searchHandle);
        fireProgressListenerStart(ProgressEvent.START, a.size());
        processFiles(a, new FindTask(elements));
        fireProgressListenerStop();
        return null;
    }
    
    public Problem fastCheckParameters() {
        if (targetName == null) {
            return new Problem(true, "Cannot determine target name. Please file a bug with detailed information on how to reproduce (preferably including the current source file and the cursor position)");
        }
        if (searchHandle.getKind() == ElementKind.METHOD) {
            return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
        } 
        return null;
    }
    
    public Problem checkParameters() {
        return null;
    }
    
    private Problem checkParametersForMethod(boolean overriders, boolean usages) {
        if (!(usages || overriders)) {
            return new Problem(true, NbBundle.getMessage(RubyWhereUsedQueryPlugin.class, "MSG_NothingToFind"));
        } else
            return null;
    }
        
    private boolean isFindSubclasses() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES);
    }
    private boolean isFindUsages() {
        return refactoring.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
    }
    private boolean isFindDirectSubclassesOnly() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
    }
    
    private boolean isFindOverridingMethods() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
    }

    private boolean isSearchFromBaseClass() {
        return false;
    }

    private boolean isSearchInComments() {
        return refactoring.getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS);
    }
    
    private class FindTask extends TransformTask {

        private RefactoringElementsBag elements;

        public FindTask(RefactoringElementsBag elements) {
            super();
            this.elements = elements;
        }

        protected Collection<ModificationResult> process(ParserResult parserResult) {
            if (isCancelled()) {
                return Collections.<ModificationResult>emptySet();
            }

            Error error = null;
            
            RubyElementCtx searchCtx = searchHandle;
            
            Node root = AstUtilities.getRoot(parserResult);
            
            if (root == null) {
                //System.out.println("Skipping file " + workingCopy.getFileObject());
                // See if the document contains references to this symbol and if so, put a warning in
                String sourceText = parserResult.getSnapshot().getText().toString();
                if (sourceText != null && sourceText.indexOf(targetName) != -1) {
                    int start = 0;
                    int end = 0;
                    String desc = "Parse error in file which contains " + targetName + " reference - skipping it"; 
                    List<? extends Error> errors = parserResult.getDiagnostics();
                    if (errors.size() > 0) {
                        for (Error e : errors) {
                            if (e.getSeverity() == Severity.ERROR) {
                                error = e;
                                break;
                            }
                        }
                        if (error == null) {
                            error = errors.get(0);
                        }
                        
                        String errorMsg = error.getDisplayName();
                        
                        if (errorMsg.length() > 80) {
                            errorMsg = errorMsg.substring(0, 77) + "..."; // NOI18N
                        }

                        desc = desc + "; " + errorMsg;
                        start = error.getStartPosition();
                        start = LexUtilities.getLexerOffset(parserResult, start);
                        if (start == -1) {
                            start = 0;
                        }
                        end = start;
                    }
                    
                    Set<Modifier> modifiers = Collections.emptySet();
                    Icon icon = UiUtils.getElementIcon(ElementKind.ERROR, modifiers);
                    OffsetRange range = new OffsetRange(start, end);
                    WhereUsedElement element = WhereUsedElement.create(parserResult, targetName, desc, range, icon);
                    elements.add(refactoring, element);
                }
            }

            if (error == null && isSearchInComments()) {
                Document doc = RetoucheUtils.getDocument(parserResult, RubyUtils.getFileObject(parserResult));
                if (doc != null) {
                    //force open
                    TokenHierarchy<Document> th = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = th.tokenSequence();

                    ts.move(0);

                    searchTokenSequence(parserResult, ts);
                }
            }
            
            if (root == null) {
                // TODO - warn that this file isn't compileable and is skipped?
                return Collections.<ModificationResult>emptySet();
            }
            
            Element element = AstElement.create(parserResult, root);
            Node node = searchCtx.getNode();
            RubyElementCtx fileCtx = new RubyElementCtx(root, node, element, RubyUtils.getFileObject(parserResult), parserResult);

            // If it's a local search, use a simpler search routine
            // TODO: ArgumentNode - look to see if we're in a parameter list, and if so its a localvar
            // (if not, it's a method)
            
            if (isFindSubclasses() || isFindDirectSubclassesOnly()) {
                // I'm only looking for the specific classes
                assert subclasses != null;
                // Look in these files for the given classes
                //findSubClass(root);
                for (IndexedClass clz : subclasses) {
                    RubyElementCtx matchCtx = new RubyElementCtx(clz);
                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
                }
            } else if (isFindUsages()) {
                Node method = null;
                if (node instanceof ArgumentNode) {
                    AstPath path = searchCtx.getPath();
                    assert path.leaf() == node;
                    Node parent = path.leafParent();

                    if (!(parent instanceof MethodDefNode)) {
                        method = AstUtilities.findLocalScope(node, path);
                    }
                } else if (node instanceof LocalVarNode || node instanceof LocalAsgnNode || node instanceof DAsgnNode || 
                        node instanceof DVarNode) {
                    // A local variable read or a parameter read, or an assignment to one of these
                    AstPath path = searchCtx.getPath();
                    method = AstUtilities.findLocalScope(node, path);
                }

                if (method != null) {
                    findLocal(searchCtx, fileCtx, method, targetName);
                } else {
                    // Full AST search
                    // TODO: If it's a local variable, parameter or dynamic variable, limit search to the current scope!
                    AstPath path = new AstPath();
                    path.descend(root);
                    find(path, searchCtx, fileCtx, root, targetName, Character.isUpperCase(targetName.charAt(0)));
                    path.ascend();
                }
            
                // TODO: Comment search
                // TODO: ClassSearch: If looking for subtypes only, do something special here...
               // (in fact, I should be able to ONLY use the index, correct?)
                
                // TODO
                
            } else if (isFindOverridingMethods()) {
                // TODO
                
            } else if (isSearchFromBaseClass()) {
                // TODO
            }
            return Collections.<ModificationResult>emptySet();
        }

        private void searchTokenSequence(ParserResult info, TokenSequence<?> ts) {
            if (ts.moveNext()) {
                do {
                    Token<?> token = ts.token();
                    TokenId id = token.id();

                    String primaryCategory = id.primaryCategory();
                    if ("comment".equals(primaryCategory) || "block-comment".equals(primaryCategory)) { // NOI18N
                        // search this comment
                        assert targetName != null;
                        CharSequence tokenText = token.text();
                        if (tokenText == null || targetName == null) {
                            continue;
                        }
                        int index = TokenUtilities.indexOf(tokenText, targetName);
                        if (index != -1) {
                            String text = tokenText.toString();
                            // TODO make sure it's its own word. Technically I could
                            // look at identifier chars like "_" here but since they are
                            // used for other purposes in comments, consider letters
                            // and numbers as enough
                            if ((index == 0 || !Character.isLetterOrDigit(text.charAt(index-1))) &&
                                    (index+targetName.length() >= text.length() || 
                                    !Character.isLetterOrDigit(text.charAt(index+targetName.length())))) {
                                int start = ts.offset() + index;
                                int end = start + targetName.length();
                                
                                // TODO - get a comment-reference icon? For now, just use the icon type
                                // of the search target
                                Set<Modifier> modifiers = Collections.emptySet();
                                if (searchHandle.getElement() != null) {
                                    modifiers = searchHandle.getElement().getModifiers();
                                }
                                Icon icon = UiUtils.getElementIcon(searchHandle.getKind(), modifiers);
                                OffsetRange range = new OffsetRange(start, end);
                                WhereUsedElement element = WhereUsedElement.create(info, targetName, range, icon); 
                                elements.add(refactoring, element);
                            }
                        }
                    } else {
                        TokenSequence<?> embedded = ts.embedded();
                        if (embedded != null) {
                            searchTokenSequence(info, embedded);
                        }                                    
                    }
                } while (ts.moveNext());
            }
        }

        /**
         * @todo P1: This is matching method names on classes that have nothing to do with the class we're searching for
         *   - I've gotta filter fields, methods etc. that are not in the current class
         *  (but I also have to search for methods that are OVERRIDING the class... so I've gotta work a little harder!)
         * @todo Arity matching on the methods to preclude methods that aren't overriding or aliasing!
         */
        @SuppressWarnings("fallthrough")
        private void find(AstPath path, RubyElementCtx searchCtx, RubyElementCtx fileCtx, Node node, String name, boolean upperCase) {
            /*if (node instanceof ArgumentNode) {
                if (((ArgumentNode)node).getName().equals(name)) {
                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
                }
            } else*/ if (node.getNodeType() == NodeType.ALIASNODE) {
                AliasNode an = (AliasNode)node;
                if (an.getNewName().equals(name) || an.getOldName().equals(name)) {
                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
                }
            } else if (!upperCase) {
                // Local variables - I can be smarter about context searches here!
                
                // Methods, attributes, etc.
                // TODO - be more discriminating on the filetype
                switch (node.getNodeType()) {
                case DEFNNODE:
                case DEFSNODE:
                    if (((MethodDefNode)node).getName().equals(name)) {
                                                
                        boolean skip = false;

                        // Check that we're in a class or module we're interested in
                        String fqn = AstUtilities.getFqnName(path);
                        if (fqn == null || fqn.length() == 0) {
                            fqn = RubyIndex.OBJECT;
                        }
                        
                        if (!fqn.equals(searchCtx.getDefClass())) {
                            // XXX THE ABOVE IS NOT RIGHT - I shouldn't
                            // use equals on the class names, I should use the
                            // index and see if one derives from includes the other
//                            skip = true;
                        }

                        // Check arity
                        if (!skip && AstUtilities.isCall(searchCtx.getNode())) {
                            // The reference is a call and this is a definition; see if
                            // this looks like a match
                            // TODO - enforce that this method is also in the desired
                            // target class!!!
                            if (!AstUtilities.isCallFor(searchCtx.getNode(), searchCtx.getArity(), node)) {
                                skip = true;
                            }
                        } else {
                            // The search handle is a method def, as is this, with the same name.
                            // Now I need to go and see if this is an override (e.g. compatible
                            // arglist...)
                            // XXX TODO
                        }
                        
                        if (!skip) {
                            node = AstUtilities.getDefNameNode((MethodDefNode)node);
                            // Found a method match
                            // TODO - check arity - see OccurrencesFinder
                            RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                            elements.add(refactoring, WhereUsedElement.create(matchCtx));
                        }
                    }
                    break;
                case FCALLNODE:
                    if (AstUtilities.isAttr(node)) {
                        SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);
                        for (SymbolNode symbol : symbols) {
                            if (symbol.getName().equals(name)) {
                                RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                                elements.add(refactoring, WhereUsedElement.create(matchCtx));
                            }
                        }                    
                    }
                    // Fall through for other call checking
                case VCALLNODE:
                case CALLNODE:
                     if (((INameNode)node).getName().equals(name)) {
                         // TODO - if it's a call without a lhs (e.g. Call.LOCAL),
                         // make sure that we're referring to the same method call
                        // Found a method call match
                        // TODO - make a node on the same line
                        // TODO - check arity - see OccurrencesFinder
                        RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                        elements.add(refactoring, WhereUsedElement.create(matchCtx));
                     }
                     break;
                case SYMBOLNODE:
                    if (((SymbolNode)node).getName().equals(name)) {
                        RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                        elements.add(refactoring, WhereUsedElement.create(matchCtx));
                    }
                    break;
                case GLOBALVARNODE:
                case GLOBALASGNNODE:
                case INSTVARNODE:
                case INSTASGNNODE:
                case CLASSVARNODE:
                case CLASSVARASGNNODE:
                case CLASSVARDECLNODE:
                    if (((INameNode)node).getName().equals(name)) {
                        RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                        elements.add(refactoring, WhereUsedElement.create(matchCtx));
                    }
                    break;
                }
            } else {
                // Classes, modules, constants, etc.
                switch (node.getNodeType()) {
                case COLON2NODE: {
                    Colon2Node c2n = (Colon2Node)node;
                    if (c2n.getName().equals(name)) {
                        RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                        elements.add(refactoring, WhereUsedElement.create(matchCtx));
                    }
                    break;
                }
                case CONSTNODE:
                case CONSTDECLNODE:
                    if (((INameNode)node).getName().equals(name)) {
                        RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                        elements.add(refactoring, WhereUsedElement.create(matchCtx));
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
                find(path, searchCtx, fileCtx, child, name, upperCase);
                path.ascend();
            }
        }
        
        /** Search for local variables in local scope */
        private void findLocal(RubyElementCtx searchCtx, RubyElementCtx fileCtx, Node node, String name) {
            switch (node.getNodeType()) {
            case ARGUMENTNODE:
                // TODO - check parent and make sure it's not a method of the same name?
                // e.g. if I have "def foo(foo)" and I'm searching for "foo" (the parameter),
                // I don't want to pick up the ArgumentNode under def foo that corresponds to the
                // "foo" method name!
                if (((ArgumentNode)node).getName().equals(name)) {
                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
                }
                break;
// I don't have alias nodes within a method, do I?                
//            } else if (node instanceof AliasNode) { 
//                AliasNode an = (AliasNode)node;
//                if (an.getNewName().equals(name) || an.getOldName().equals(name)) {
//                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
//                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
//                }
//                break;
            case LOCALVARNODE:
            case LOCALASGNNODE:
                if (((INameNode)node).getName().equals(name)) {
                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
                }
                break;
            case DVARNODE:
            case DASGNNODE:
                 if (((INameNode)node).getName().equals(name)) {
                    // Found a method call match
                    // TODO - make a node on the same line
                    // TODO - check arity - see OccurrencesFinder
                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
                 }                 
                 break;
            case SYMBOLNODE:
                // XXX Can I have symbols to local variables? Try it!!!
                if (((SymbolNode)node).getName().equals(name)) {
                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
                }
                break;
            }

            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.isInvisible()) {
                    continue;
                }
                findLocal(searchCtx, fileCtx, child, name);
            }
        }

//        private void findSubClass(Node node) {
//            @SuppressWarnings("unchecked")
//            List<Node> list = node.childNodes();
//
//            for (Node child : list) {
//                findSubClass(child);
//            }
//        }
    }
}
