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
package org.netbeans.modules.gsfret.editor.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.swing.text.Document;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.modules.editor.highlights.spi.Highlight;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public class MethodExitDetector { // extends CancellableTreePathScanner<Boolean, Stack<Tree>> {
    
    /** Creates a new instance of MethodExitDetector */
    public MethodExitDetector() {
    }
    
    private CompilationInfo info;
//    private Document doc;
//    private Set<Highlight> highlights;
//    private Collection<TypeMirror> exceptions;
//    private Stack<Map<TypeMirror, List<Highlight>>> exceptions2HighlightsStack;
    
    public Set<Highlight> process(CompilationInfo info, Document document/*, MethodTree methoddecl, Collection<Tree> excs*/) {
        this.info = info;
        System.err.println("MethodExitDetecter is Lobotomized");
//        this.doc  = document;
//        this.highlights = new HashSet<Highlight>();
//        this.exceptions2HighlightsStack = new Stack<Map<TypeMirror, List<Highlight>>>();
//        this.exceptions2HighlightsStack.push(null);
//        
//        try {
            Set<Highlight> result = new HashSet<Highlight>();
//            
//            CompilationUnitTree cu = info.getCompilationUnit();
//            
//            Boolean wasReturn = scan(TreePath.getPath(cu, methoddecl), null);
//            
//            if (isCanceled())
//                return Collections.emptySet();
//            
//            if (excs == null) {
//                //"return" exit point only if not searching for exceptions:
//                result.addAll(highlights);
//                
//                if (wasReturn != Boolean.TRUE) {
//                    int lastBracket = Utilities.findLastBracket(methoddecl, cu, info.getTrees().getSourcePositions(), document);
//                    
//                    if (lastBracket != (-1)) {
//                        //highlight the "fall over" exitpoint:
//                        result.add(Utilities.createHighlight(cu, info.getTrees().getSourcePositions(), document, lastBracket, lastBracket + 1, EnumSet.of(ColoringAttributes.MARK_OCCURRENCES), MarkOccurencesHighlighter.ES_COLOR));
//                    }
//                }
//            }
//            
//            List<TypeMirror> exceptions = null;
//            
//            if (excs != null) {
//                exceptions = new ArrayList<TypeMirror>();
//                
//                for (Tree t : excs) {
//                    if (isCanceled())
//                        return Collections.emptySet();
//                    
//                    TypeMirror m = info.getTrees().getTypeMirror(TreePath.getPath(cu, t));
//                    
//                    if (m != null) {
//                        exceptions.add(m);
//                    }
//                }
//            }
//            
//            Types t = info.getTypes();
//            
//            assert exceptions2HighlightsStack.size() == 1 : exceptions2HighlightsStack.size();
//            
//            Map<TypeMirror, List<Highlight>> exceptions2Highlights = exceptions2HighlightsStack.peek();
//            
//            //exceptions2Highlights may be null if the method is empty (or not finished, like "public void")
//            //see ExitPointsEmptyMethod and ExitPointsStartedMethod tests:
//            if (exceptions2Highlights != null) {
//                for (TypeMirror type1 : exceptions2Highlights.keySet()) {
//                    if (isCanceled())
//                        return Collections.emptySet();
//                    
//                    boolean add = true;
//                    
//                    if (exceptions != null) {
//                        add = false;
//                        
//                        for (TypeMirror type2 : exceptions) {
//                            add |= t.isAssignable(type1, type2);
//                        }
//                    }
//                    
//                    if (add) {
//                        result.addAll(exceptions2Highlights.get(type1));
//                    }
//                }
//            }
//            
            return result;
//        } finally {
//            //clean-up:
//            this.info = null;
//            this.doc  = null;
//            this.highlights = null;
//            this.exceptions2HighlightsStack = null;
//        }
    }
    
//    private void addToExceptionsMap(TypeMirror key, Highlight value) {
//        if (value == null)
//            return ;
//        
//        Map<TypeMirror, List<Highlight>> map = exceptions2HighlightsStack.peek();
//        
//        if (map == null) {
//            map = new HashMap<TypeMirror, List<Highlight>>();
//            exceptions2HighlightsStack.pop();
//            exceptions2HighlightsStack.push(map);
//        }
//        
//        List<Highlight> l = map.get(key);
//        
//        if (l == null) {
//            map.put(key, l = new ArrayList<Highlight>());
//        }
//        
//        l.add(value);
//    }
//    
//    private void doPopup() {
//        Map<TypeMirror, List<Highlight>> top = exceptions2HighlightsStack.pop();
//        
//        if (top == null)
//            return ;
//        
//        Map<TypeMirror, List<Highlight>> result = exceptions2HighlightsStack.pop();
//        
//        if (result == null) {
//            exceptions2HighlightsStack.push(top);
//            return ;
//        }
//        
//        for (TypeMirror key : top.keySet()) {
//            List<Highlight> topKey    = top.get(key);
//            List<Highlight> resultKey = result.get(key);
//            
//            if (topKey == null)
//                continue;
//            
//            if (resultKey == null) {
//                result.put(key, topKey);
//                continue;
//            }
//            
//            resultKey.addAll(topKey);
//        }
//        
//        exceptions2HighlightsStack.push(result);
//    }
//    
//    private Highlight createHighlight(TreePath tree) {
//        return Utilities.createHighlight(info.getCompilationUnit(), info.getTrees().getSourcePositions(), doc, tree, EnumSet.of(ColoringAttributes.MARK_OCCURRENCES), MarkOccurencesHighlighter.ES_COLOR);
//    }
//    
//    @Override
//    public Boolean visitTry(TryTree tree, Stack<Tree> d) {
//        exceptions2HighlightsStack.push(null);
//        
//        Boolean returnInTryBlock = scan(tree.getBlock(), d);
//        
//        boolean returnInCatchBlock = true;
//        
//        for (Tree t : tree.getCatches()) {
//            Boolean b = scan(t, d);
//            
//            returnInCatchBlock &= b == Boolean.TRUE;
//        }
//        
//        Boolean returnInFinallyBlock = scan(tree.getFinallyBlock(), d);
//        
//        doPopup();
//        
//        if (returnInTryBlock == Boolean.TRUE && returnInCatchBlock)
//            return Boolean.TRUE;
//        
//        return returnInFinallyBlock;
//    }
//    
//    @Override
//    public Boolean visitReturn(ReturnTree tree, Stack<Tree> d) {
//        if (exceptions == null) {
//            Highlight h = createHighlight(getCurrentPath());
//            
//            if (h != null) {
//                highlights.add(h);
//            }
//        }
//        
//        super.visitReturn(tree, d);
//        return Boolean.TRUE;
//    }
//    
//    @Override
//    public Boolean visitCatch(CatchTree tree, Stack<Tree> d) {
//        TypeMirror type1 = info.getTrees().getTypeMirror(new TreePath(new TreePath(getCurrentPath(), tree.getParameter()), tree.getParameter().getType()));
//        Types t = info.getTypes();
//        
//        if (type1 != null) {
//            Set<TypeMirror> toRemove = new HashSet<TypeMirror>();
//            Map<TypeMirror, List<Highlight>> exceptions2Highlights = exceptions2HighlightsStack.peek();
//            
//            if (exceptions2Highlights != null) {
//                for (TypeMirror type2 : exceptions2Highlights.keySet()) {
//                    if (t.isAssignable(type2, type1)) {
//                        toRemove.add(type2);
//                    }
//                }
//                
//                for (TypeMirror type : toRemove) {
//                    exceptions2Highlights.remove(type);
//                }
//            }
//            
//        }
//        
//        scan(tree.getParameter(), d);
//        return scan(tree.getBlock(), d);
//    }
//    
//    @Override
//    public Boolean visitMethodInvocation(MethodInvocationTree tree, Stack<Tree> d) {
//        Element el = info.getTrees().getElement(new TreePath(getCurrentPath(), tree.getMethodSelect()));
//        
//        if (el == null) {
//            System.err.println("Warning: decl == null");
//            System.err.println("tree=" + tree);
//        }
//        
//        if (el != null && el.getKind() == ElementKind.METHOD) {
//            for (TypeMirror m : ((ExecutableElement) el).getThrownTypes()) {
//                addToExceptionsMap(m, createHighlight(getCurrentPath()));
//            }
//        }
//        
//        super.visitMethodInvocation(tree, d);
//        return null;
//    }
//    
//    @Override
//    public Boolean visitThrow(ThrowTree tree, Stack<Tree> d) {
//        addToExceptionsMap(info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), tree.getExpression())), createHighlight(getCurrentPath()));
//        
//        super.visitThrow(tree, d);
//        
//        return Boolean.TRUE;
//    }
//    
//    @Override
//    public Boolean visitNewClass(NewClassTree tree, Stack<Tree> d) {
//        Element el = info.getTrees().getElement(getCurrentPath());
//        
//        if (el != null && el.getKind() == ElementKind.CONSTRUCTOR) {
//            for (TypeMirror m : ((ExecutableElement) el).getThrownTypes()) {
//                addToExceptionsMap(m, createHighlight(getCurrentPath()));
//            }
//        }
//        
//        super.visitNewClass(tree, d);
//        return null;
//    }
//    
//    @Override
//    public Boolean visitMethod(MethodTree node, Stack<Tree> p) {
//        scan(node.getModifiers(), p);
//        scan(node.getReturnType(), p);
//        scan(node.getTypeParameters(), p);
//        scan(node.getParameters(), p);
//        scan(node.getThrows(), p);
//        return scan(node.getBody(), p);
//    }
//    
//    @Override
//    public Boolean visitIf(IfTree node, Stack<Tree> p) {
//        scan(node.getCondition(), p);
//        Boolean thenResult = scan(node.getThenStatement(), p);
//        Boolean elseResult = scan(node.getElseStatement(), p);
//        
//        if (thenResult == Boolean.TRUE && elseResult == Boolean.TRUE)
//            return Boolean.TRUE;
//        
//        return null;
//    }
    
}
