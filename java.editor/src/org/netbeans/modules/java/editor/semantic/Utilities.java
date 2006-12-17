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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.openide.ErrorManager;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {
    
    private static final boolean DEBUG = false;
    
    private static final int[] NO_SPAN = {-1, -1};
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }
    
    private static int[] findIdentifierSpanImpl(Tree decl, Tree lastLeft, String name, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        return findIdentifierSpanImpl(decl, lastLeft, Collections.<Tree>emptyList(), name, cu, positions, doc);
    }

    private static int[] findIdentifierSpanImpl(Tree decl, Tree lastLeft, List<? extends Tree> firstRight, String name, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int declStart = (int) positions.getStartPosition(cu, decl);
        int start = lastLeft != null ? (int)positions.getEndPosition(cu, lastLeft) : declStart;
        
        if (start == (-1)) {
            start = declStart;
            if (start == (-1)) {
                return NO_SPAN;
            }
        }
        
        try {
            int end = (int)positions.getEndPosition(cu, decl);

            for (Tree t : firstRight) {
                if (t == null)
                    continue;

                int proposedEnd = (int)positions.getStartPosition(cu, t);

                if (proposedEnd != (-1) && proposedEnd < end)
                    end = proposedEnd;
            }

            if (start > end) {
                //may happend in case:
                //public static String s() [] {}
                //(meaning: method returning array of Strings)
                //use a conservative start value:
                start = (int) positions.getStartPosition(cu, decl);
            }
            
            if (start > doc.getLength() || end > doc.getLength()) {
                if (DEBUG) {
                    System.err.println("Log: position outside document: ");
                    System.err.println("lastLeft = " + lastLeft );
                    System.err.println("decl = " + decl);
                    System.err.println("startOffset = " + start);
                    System.err.println("endOffset = " + end);
                    Thread.dumpStack();
                }
                
                return NO_SPAN;
            }
            
            String text = doc.getText(start, end - start);
            
            int index = text.lastIndexOf(name.toString());
            
            if (index != (-1)) {
                return new int[] {start + index, start + index + name.length()};
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        return NO_SPAN;
    }
    
    private static int[] findIdentifierSpanImpl(MemberSelectTree tree, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int start = (int)positions.getStartPosition(cu, tree);
        
        int endPosition = (int)positions.getEndPosition(cu, tree);
        
        if (start == (-1) || endPosition == (-1))
            return NO_SPAN;

        try {
            String member = tree.getIdentifier().toString();
            int end = endPosition - start;
            
            if (start > doc.getLength() || end > doc.getLength()) {
                if (DEBUG) {
                    System.err.println("Log: position outside document: ");
                    System.err.println("tree = " + tree );
                    System.err.println("member = " + member);
                    System.err.println("startOffset = " + start);
                    System.err.println("endOffset = " + end);
                    Thread.dumpStack();
                }
                
                return NO_SPAN;
            }
            
            String text = doc.getText(start, end);
            
            int index = text.lastIndexOf(member);
            
            if (index != (-1)) {
                return new int[] {start + index, start + index + member.length()};
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().annotate(e, ErrorManager.UNKNOWN, "start=" + start + ", endPosition=" + endPosition, null, null, null);
            ErrorManager.getDefault().notify(e);
        }
        
        return NO_SPAN;
    }
    
    private static final Map<Class, List<Kind>> class2Kind;
    
    static {
        class2Kind = new HashMap<Class, List<Kind>>();
        
        for (Kind k : Kind.values()) {
            Class c = k.asInterface();
            List<Kind> kinds = class2Kind.get(c);
            
            if (kinds == null) {
                class2Kind.put(c, kinds = new ArrayList<Kind>());
            }
            
            kinds.add(k);
        }
    }
    
    private static int[] findIdentifierSpanImpl(TreePath decl, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        if (doc == null) {
            throw new NullPointerException();
        }
        
        Tree leaf = decl.getLeaf();
        
        if (class2Kind.get(MethodTree.class).contains(leaf.getKind())) {
            if (positions.getStartPosition(cu, leaf) == (-1) || positions.getEndPosition(cu, leaf) == (-1))
                return NO_SPAN; //syntetic methods
            
            MethodTree method = (MethodTree) leaf;
            List<Tree> rightTrees = new ArrayList<Tree>();

            rightTrees.addAll(method.getParameters());
            rightTrees.addAll(method.getThrows());
            rightTrees.add(method.getBody());

            Name name = method.getName();
            
            if (method.getReturnType() == null)
                name = ((ClassTree) decl.getParentPath().getLeaf()).getSimpleName();
            
            return findIdentifierSpanImpl(leaf, method.getReturnType(), rightTrees, name.toString(), cu, positions, doc);
        }
        if (class2Kind.get(VariableTree.class).contains(leaf.getKind())) {
            VariableTree var = (VariableTree) leaf;

            return findIdentifierSpanImpl(leaf, var.getType(), Collections.singletonList(var.getInitializer()), var.getName().toString(), cu, positions, doc);
        }
        if (class2Kind.get(MemberSelectTree.class).contains(leaf.getKind())) {
            return findIdentifierSpanImpl((MemberSelectTree) leaf, cu, positions, doc);
        }
        if (class2Kind.get(ClassTree.class).contains(leaf.getKind())) {
            //very inefficient:
            int start = (int)positions.getStartPosition(cu, leaf);
            int end   = (int)positions.getEndPosition(cu, leaf);
            
            if (start == (-1) || end == (-1)) {
                return NO_SPAN;
            }
                
            if (start > doc.getLength() || end > doc.getLength()) {
                if (DEBUG) {
                    System.err.println("Log: position outside document: ");
                    System.err.println("decl = " + decl);
                    System.err.println("startOffset = " + start);
                    System.err.println("endOffset = " + end);
                    Thread.dumpStack();
                }
                
                return NO_SPAN;
            }
            
            String name = ((ClassTree) leaf).getSimpleName().toString();
            
            if (name.length() == 0)
                return NO_SPAN;
            
            try {
                String text = doc.getText(start, end - start);
                
                int index = text.indexOf(name);
                
                if (index == (-1)) {
                    return NO_SPAN;
//                    throw new IllegalStateException("Should NEVER happen.");
                }
                
                start += index;
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            int exactEnd   = start + name.length();
            
            return new int[] {start, exactEnd};
        }
        throw new IllegalArgumentException("Only MethodDecl, VariableDecl and ClassDecl are accepted by this method.");
    }

    public static int[] findIdentifierSpan(final TreePath decl, final CompilationUnitTree cu, final SourcePositions positions, final Document doc) {
        final int[][] result = new int[1][];
        doc.render(new Runnable() {
            public void run() {
                result[0] = findIdentifierSpanImpl(decl, cu, positions, doc);
            }
        });
        
        return result[0];
    }
    
    private static int findBodyStartImpl(Tree cltree, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int start = (int)positions.getStartPosition(cu, cltree);
        int end   = (int)positions.getEndPosition(cu, cltree);
        
        if (start == (-1) || end == (-1)) {
            return -1;
        }
        
        if (start > doc.getLength() || end > doc.getLength()) {
            if (DEBUG) {
                System.err.println("Log: position outside document: ");
                System.err.println("decl = " + cltree);
                System.err.println("startOffset = " + start);
                System.err.println("endOffset = " + end);
                Thread.dumpStack();
            }
            
            return (-1);
        }
        
        try {
            String text = doc.getText(start, end - start);
            
            int index = text.indexOf('{');
            
            if (index == (-1)) {
                return -1;
//                throw new IllegalStateException("Should NEVER happen.");
            }
            
            return start + index;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        return (-1);
    }
    
    public static int findBodyStart(final Tree cltree, final CompilationUnitTree cu, final SourcePositions positions, final Document doc) {
        Kind kind = cltree.getKind();
        if (kind != Kind.CLASS && kind != Kind.METHOD)
            throw new IllegalArgumentException("Unsupported kind: "+ kind);
        final int[] result = new int[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = findBodyStartImpl(cltree, cu, positions, doc);
            }
        });
        
        return result[0];
    }
    
    private static int findLastBracketImpl(MethodTree tree, CompilationUnitTree cu, SourcePositions positions, Document doc) {
        int start = (int)positions.getStartPosition(cu, tree);
        int end   = (int)positions.getEndPosition(cu, tree);
        
        if (start == (-1) || end == (-1)) {
            return -1;
        }
        
        if (start > doc.getLength() || end > doc.getLength()) {
            if (DEBUG) {
                System.err.println("Log: position outside document: ");
                System.err.println("decl = " + tree);
                System.err.println("startOffset = " + start);
                System.err.println("endOffset = " + end);
                Thread.dumpStack();
            }
            
            return (-1);
        }
        
        try {
            String text = doc.getText(end - 1, 1);
            
            if (text.charAt(0) == '}')
                return end - 1;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        return (-1);
    }
    
    public static int findLastBracket(final MethodTree tree, final CompilationUnitTree cu, final SourcePositions positions, final Document doc) {
        final int[] result = new int[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = findLastBracketImpl(tree, cu, positions, doc);
            }
        });
        
        return result[0];
    }
    
    private static Highlight createHighlightImpl(CompilationUnitTree cu, SourcePositions positions, Document doc, int startOffset, int endOffset, Collection<ColoringAttributes> c, Color es) {
        try {
            if (startOffset > doc.getLength() || endOffset > doc.getLength()) {
                if (DEBUG) {
                    System.err.println("Log: position outside document: ");
//                  System.err.println("tree = " + tree );
                    System.err.println("startOffset = " + startOffset );
                    System.err.println("endOffset = " + endOffset );
                    Thread.dumpStack();
                }
                
                return null;
            }
            
            return new HighlightImpl(doc, startOffset, endOffset, c, es);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static Highlight createHighlightImpl(CompilationUnitTree cu, SourcePositions positions, Document doc, TreePath tree, Collection<ColoringAttributes> c, Color es) {
        Tree leaf = tree.getLeaf();
        int startOffset = (int)positions.getStartPosition(cu, leaf);
        int endOffset   = (int)positions.getEndPosition(cu, leaf);
        
        //XXX: do not use instanceof:
        if (leaf instanceof MethodTree || leaf instanceof VariableTree || leaf instanceof ClassTree || leaf instanceof MemberSelectTree) {
            int[] span = findIdentifierSpan(tree, cu, positions, doc);
            
            startOffset = span[0];
            endOffset   = span[1];
        }
        
        if (startOffset == (-1) || endOffset == (-1))
            return null;
        
        return createHighlightImpl(cu, positions, doc, startOffset, endOffset, c, es);
    }
    
    public static Highlight createHighlight(final CompilationUnitTree cu, final SourcePositions positions, final Document doc, final TreePath tree, final Collection<ColoringAttributes> c, final Color es) {
        final Highlight[] result = new Highlight[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = createHighlightImpl(cu, positions, doc, tree, c, es);
            }
        });
        
        return result[0];
    }
    
    public static Highlight createHighlight(final CompilationUnitTree cu, final SourcePositions positions, final Document doc, final int startOffset, final int endOffset, final Collection<ColoringAttributes> c, final Color es) {
        final Highlight[] result = new Highlight[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = createHighlightImpl(cu, positions, doc, startOffset, endOffset, c, es);
            }
        });
        
        return result[0];
    }
    
    private static final Set<String> keywords;
    
    static {
        keywords = new HashSet();
        
        keywords.add("true");
        keywords.add("false");
        keywords.add("null");
        keywords.add("this");
        keywords.add("super");
        keywords.add("class");
    }
    
    public static boolean isKeyword(Tree tree) {
        if (tree.getKind() == Kind.IDENTIFIER) {
            return keywords.contains(((IdentifierTree) tree).getName().toString());
        }
        if (tree.getKind() == Kind.MEMBER_SELECT) {
            return keywords.contains(((MemberSelectTree) tree).getIdentifier().toString());
        }
        
        return false;
    }
    
    public static boolean isPrivateElement(Element el) {
        if (el.getKind() == ElementKind.PARAMETER)
            return true;
        
        if (el.getKind() == ElementKind.LOCAL_VARIABLE)
            return true;
        
        if (el.getKind() == ElementKind.EXCEPTION_PARAMETER)
            return true;
        
        return el.getModifiers().contains(Modifier.PRIVATE);
    }
    

}
