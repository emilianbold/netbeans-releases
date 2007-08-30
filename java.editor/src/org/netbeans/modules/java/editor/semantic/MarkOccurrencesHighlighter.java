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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.java.editor.semantic.ColoringAttributes.Coloring;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class MarkOccurrencesHighlighter implements CancellableTask<CompilationInfo> {
    
    private FileObject file;
    
    /** Creates a new instance of SemanticHighlighter */
    MarkOccurrencesHighlighter(FileObject file) {
        this.file = file;
    }
    
    public static final Color ES_COLOR = new Color( 175, 172, 102 ); // new Color(244, 164, 113);
    
    public Document getDocument() {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = d.getCookie(EditorCookie.class);
            
            if (ec == null)
                return null;
            
            return ec.getDocument();
        } catch (IOException e) {
            Logger.getLogger(MarkOccurrencesHighlighter.class.getName()).log(Level.INFO, "SemanticHighlighter: Cannot find DataObject for file: " + FileUtil.getFileDisplayName(file), e);
            return null;
        }
    }
    
    public void run(CompilationInfo info) {
        resume();
        
        Document doc = getDocument();
        
        if (doc == null) {
            Logger.getLogger(MarkOccurrencesHighlighter.class.getName()).log(Level.INFO, "SemanticHighlighter: Cannot get document!");
            return ;
        }
        
        Preferences node = MarkOccurencesSettings.getCurrentNode();
        
        if (!node.getBoolean(MarkOccurencesSettings.ON_OFF, true)) {
            getHighlightsBag(doc).clear();
            OccurrencesMarkProvider.get(doc).setOccurrences(Collections.<Mark>emptySet());
            return ;
        }
        
        long start = System.currentTimeMillis();
        
        int caretPosition = MarkOccurrencesHighlighterFactory.getLastPosition(file);
        
        if (isCancelled())
            return;
        
        List<int[]> bag = processImpl(info, node, doc, caretPosition);
        
        if (isCancelled())
            return;
        
        Logger.getLogger("TIMER").log(Level.FINE, "Occurrences",
            new Object[] {((DataObject) doc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile(), (System.currentTimeMillis() - start)});
        
        if (bag == null) {
            if (node.getBoolean(MarkOccurencesSettings.KEEP_MARKS, true)) {
                return ;
            }
            
            bag = new ArrayList<int[]>();
        }
        
        Collections.sort(bag, new Comparator<int[]>() {
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });
        
        Iterator<int[]> it = bag.iterator();
        int[] last = it.hasNext() ? it.next() : null;
        List<int[]> result = new ArrayList<int[]>(bag.size());
        
        while (it.hasNext()) {
            int[] current = it.next();
            
            if (current[0] < last[1]) {
                //merge the highlights:
                last[1] = Math.max(current[1], last[1]);
            } else {
                result.add(last);
                last = current;
            }
        }
        
        if (last != null) {
            result.add(last);
        }
        
        OffsetsBag obag = new OffsetsBag(doc);
        
        obag.clear();
        
        AttributeSet attributes = ColoringManager.getColoringImpl(MO);
        
        for (int[] span : result) {
            obag.addHighlight(span[0], span[1], attributes);
        }
        
        getHighlightsBag(doc).setHighlights(obag);
        OccurrencesMarkProvider.get(doc).setOccurrences(OccurrencesMarkProvider.createMarks(doc, bag, ES_COLOR, "Mark Occurrences"));
    }
    
    private boolean isIn(CompilationUnitTree cu, SourcePositions sp, Tree tree, int position) {
        return sp.getStartPosition(cu, tree) <= position && position <= sp.getEndPosition(cu, tree);
    }
    
    private boolean isIn(int caretPosition, Token span) {
//        System.err.println("caretPosition = " + caretPosition );
//        System.err.println("span[0]= " + span[0]);
//        System.err.println("span[1]= " + span[1]);
        if (span == null)
            return false;
        
        return span.offset(null) <= caretPosition && caretPosition <= span.offset(null) + span.length();
    }
    
    List<int[]> processImpl(CompilationInfo info, Preferences node, Document doc, int caretPosition) {
        CompilationUnitTree cu = info.getCompilationUnit();
        TreePath tp = info.getTreeUtilities().pathFor(caretPosition);
        TreePath typePath = findTypePath(tp);
        
        if (isCancelled())
            return null;
        
        //detect caret inside the return type or throws clause:
        if (typePath != null && typePath.getParentPath().getLeaf().getKind() == Kind.METHOD) {
            //hopefully found something, check:
            MethodTree decl = (MethodTree) typePath.getParentPath().getLeaf();
            Tree type = decl.getReturnType();
            
            if (   node.getBoolean(MarkOccurencesSettings.EXIT, true)
                && isIn(cu, info.getTrees().getSourcePositions(), type, caretPosition)) {
                MethodExitDetector med = new MethodExitDetector();
                
                setExitDetector(med);
                
                try {
                    return med.process(info, doc, decl, null);
                } finally {
                    setExitDetector(null);
                }
            }
            
            for (Tree exc : decl.getThrows()) {
                if (   node.getBoolean(MarkOccurencesSettings.EXCEPTIONS, true)
                    && isIn(cu, info.getTrees().getSourcePositions(), exc, caretPosition)) {
                    MethodExitDetector med = new MethodExitDetector();
                    
                    setExitDetector(med);
                    
                    try {
                        return med.process(info, doc, decl, Collections.singletonList(exc));
                    } finally {
                        setExitDetector(null);
                    }
                }
            }
        }
        
        if (isCancelled())
            return null;
        
        if (node.getBoolean(MarkOccurencesSettings.IMPLEMENTS, true)) {
            //detect caret inside the extends/implements clause:
            if (typePath != null && typePath.getParentPath().getLeaf().getKind() == Kind.CLASS) {
                ClassTree ctree = (ClassTree) typePath.getParentPath().getLeaf();
                int bodyStart = Utilities.findBodyStart(ctree, cu, info.getTrees().getSourcePositions(), doc);
                
                boolean isExtends = ctree.getExtendsClause() == typePath.getLeaf();
                boolean isImplements = false;
                
                for (Tree t : ctree.getImplementsClause()) {
                    if (t == typePath.getLeaf()) {
                        isImplements = true;
                        break;
                    }
                }
                
                if (   (isExtends && node.getBoolean(MarkOccurencesSettings.OVERRIDES, true))
                    || (isImplements && node.getBoolean(MarkOccurencesSettings.IMPLEMENTS, true))) {
                    Element superType = info.getTrees().getElement(typePath);
                    Element thisType  = info.getTrees().getElement(typePath.getParentPath());
                    
                    if (isClass(superType) && isClass(thisType))
                        return detectMethodsForClass(info, doc, typePath.getParentPath(), (TypeElement) superType, (TypeElement) thisType);
                }
            }
            
            if (isCancelled())
                return null;
            
            TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            
            if (ts != null && tp.getLeaf().getKind() == Kind.CLASS) {
                int bodyStart = Utilities.findBodyStart(tp.getLeaf(), cu, info.getTrees().getSourcePositions(), doc);
                
                if (caretPosition < bodyStart) {
                    ts.move(caretPosition);
                    
                    if (ts.moveNext()) {
                        if (node.getBoolean(MarkOccurencesSettings.OVERRIDES, true) && ts.token().id() == JavaTokenId.EXTENDS) {
                            Tree superClass = ((ClassTree) tp.getLeaf()).getExtendsClause();
                            
                            if (superClass != null) {
                                Element superType = info.getTrees().getElement(new TreePath(tp, superClass));
                                Element thisType  = info.getTrees().getElement(tp);
                                
                                if (isClass(superType) && isClass(thisType))
                                    return detectMethodsForClass(info, doc, tp, (TypeElement) superType, (TypeElement) thisType);
                            }
                        }
                        
                        if (node.getBoolean(MarkOccurencesSettings.IMPLEMENTS, true) && ts.token().id() == JavaTokenId.IMPLEMENTS) {
                            List<? extends Tree> superClasses = ((ClassTree) tp.getLeaf()).getImplementsClause();
                            
                            if (superClasses != null) {
                                List<TypeElement> superTypes = new ArrayList<TypeElement>();
                                
                                for (Tree superTypeTree : superClasses) {
                                    if (superTypeTree != null) {
                                        Element superType = info.getTrees().getElement(new TreePath(tp, superTypeTree));
                                        
                                        if (isClass(superType))
                                            superTypes.add((TypeElement) superType);
                                    }
                                }
                                
                                Element thisType  = info.getTrees().getElement(tp);
                                
                                if (!superTypes.isEmpty() && isClass(thisType))
                                    return detectMethodsForClass(info, doc, tp, superTypes, (TypeElement) thisType);
                            }
                            
                        }
                    }
                }
            }
        }
        
        if (isCancelled())
            return null;
        
        Tree tree =tp.getLeaf();
        
        if (node.getBoolean(MarkOccurencesSettings.BREAK_CONTINUE, true) && (tree.getKind() == Kind.BREAK || tree.getKind() == Kind.CONTINUE)) {
            return detectBreakOrContinueTarget(info, doc, tp);
        }
        
        //variable declaration:
        Element el = info.getTrees().getElement(tp);
        if (   el != null
                && (!(tree.getKind() == Kind.CLASS) || isIn(caretPosition, Utilities.findIdentifierSpan(info, doc, tp)))
                && !Utilities.isKeyword(tree)
                && (!(tree.getKind() == Kind.METHOD) || isIn(caretPosition, Utilities.findIdentifierSpan(info, doc, tp)))
                && isEnabled(node, el)) {
            FindLocalUsagesQuery fluq = new FindLocalUsagesQuery();
            
            setLocalUsages(fluq);
            
            try {
                List<int[]> bag = new ArrayList<int[]>();
                for (Token t : fluq.findUsages(el, info, doc)) {
                    bag.add(new int[] {t.offset(null), t.offset(null) + t.length()});
                }
                
                return bag;
            } finally {
                setLocalUsages(null);
            }
        }
        
        return null;
    }
    
    private static final Set<Kind> TYPE_PATH_ELEMENT = EnumSet.of(Kind.IDENTIFIER, Kind.PRIMITIVE_TYPE, Kind.PARAMETERIZED_TYPE, Kind.MEMBER_SELECT, Kind.ARRAY_TYPE);
    
    private static TreePath findTypePath(TreePath tp) {
        if (!TYPE_PATH_ELEMENT.contains(tp.getLeaf().getKind()))
            return null;
        
        while (TYPE_PATH_ELEMENT.contains(tp.getParentPath().getLeaf().getKind())) {
            tp = tp.getParentPath();
        }
        
        return tp;
    }
    
    private static boolean isClass(Element el) {
        return el != null && (el.getKind().isClass() || el.getKind().isInterface());
    }
    
    private static boolean isEnabled(Preferences node, Element el) {
        switch (el.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
            case TYPE_PARAMETER: //???
                return node.getBoolean(MarkOccurencesSettings.TYPES, true);
            case CONSTRUCTOR:
            case METHOD:
                return node.getBoolean(MarkOccurencesSettings.METHODS, true);
            case ENUM_CONSTANT:
                return node.getBoolean(MarkOccurencesSettings.CONSTANTS, true);
            case FIELD:
                if (el.getModifiers().containsAll(EnumSet.of(Modifier.STATIC, Modifier.FINAL))) {
                    return node.getBoolean(MarkOccurencesSettings.CONSTANTS, true);
                } else {
                    return node.getBoolean(MarkOccurencesSettings.FIELDS, true);
                }
            case LOCAL_VARIABLE:
            case PARAMETER:
            case EXCEPTION_PARAMETER:
                return node.getBoolean(MarkOccurencesSettings.LOCAL_VARIABLES, true);
            case PACKAGE:
                return false; //never mark occurrence packages
            default:
                Logger.getLogger(MarkOccurrencesHighlighter.class.getName()).log(Level.INFO, "Unknow element type: {0}.", el.getKind());
                return true;
        }
    }
    
    private boolean canceled;
    private MethodExitDetector exitDetector;
    private FindLocalUsagesQuery localUsages;
    
    private final synchronized void setExitDetector(MethodExitDetector detector) {
        this.exitDetector = detector;
    }
    
    private final synchronized void setLocalUsages(FindLocalUsagesQuery localUsages) {
        this.localUsages = localUsages;
    }
    
    public final synchronized void cancel() {
        canceled = true;
        
        if (exitDetector != null) {
            exitDetector.cancel();
        }
        if (localUsages != null) {
            localUsages.cancel();
        }
    }
    
    protected final synchronized boolean isCancelled() {
        return canceled;
    }
    
    protected final synchronized void resume() {
        canceled = false;
    }
    
    private List<int[]> detectMethodsForClass(CompilationInfo info, Document document, TreePath clazz, TypeElement superType, TypeElement thisType) {
        return detectMethodsForClass(info, document, clazz, Collections.singletonList(superType), thisType);
    }
    
    static Coloring MO = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.MARK_OCCURRENCES);
    
    private List<int[]> detectMethodsForClass(CompilationInfo info, Document document, TreePath clazz, List<TypeElement> superTypes, TypeElement thisType) {
        List<int[]> highlights = new ArrayList<int[]>();
        ClassTree clazzTree = (ClassTree) clazz.getLeaf();
        TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");
        
        OUTER: for (Tree member: clazzTree.getMembers()) {
            if (isCancelled()) {
                return null;
            }
            
            if (member.getKind() == Kind.METHOD) {
                TreePath path = new TreePath(clazz, member);
                Element el = info.getTrees().getElement(path);
                
                if (el.getKind() == ElementKind.METHOD) {
                    for (TypeElement superType : superTypes) {
                        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(superType))) {
                            if (info.getElements().overrides((ExecutableElement) el, ee, thisType) && (superType.getKind().isClass() || !ee.getEnclosingElement().equals(jlObject))) {
                                Token t = Utilities.getToken(info, document, path);
                                
                                if (t != null) {
                                    highlights.add(new int[] {t.offset(null), t.offset(null) + t.length()});
                                }
                                continue OUTER;
                            }
                        }
                    }
                }
            }
        }
        
        return highlights;
    }
    
    private List<int[]> detectBreakOrContinueTarget(CompilationInfo info, Document document, TreePath breakOrContinue) {
        List<int[]> result = new ArrayList<int[]>();
        StatementTree target = info.getTreeUtilities().getBreakContinueTarget(breakOrContinue);
        
        if (target == null)
            return null;
        
        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        
        ts.move((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), target));
        
        if (ts.moveNext()) {
            result.add(new int[] {ts.offset(), ts.offset() + ts.token().length()});
        }
        
        StatementTree statement = target.getKind() == Kind.LABELED_STATEMENT ? ((LabeledStatementTree) target).getStatement() : target;
        Tree block = null;
        
        switch (statement.getKind()) {
            case SWITCH:
                block = statement;
                break;
            case WHILE_LOOP:
                if (((WhileLoopTree) statement).getStatement().getKind() == Kind.BLOCK)
                    block = ((WhileLoopTree) statement).getStatement();
                break;
            case FOR_LOOP:
                if (((ForLoopTree) statement).getStatement().getKind() == Kind.BLOCK)
                    block = ((ForLoopTree) statement).getStatement();
                break;
            case DO_WHILE_LOOP:
                if (((DoWhileLoopTree) statement).getStatement().getKind() == Kind.BLOCK)
                    block = ((DoWhileLoopTree) statement).getStatement();
                break;
        }
        
        if (block != null) {
            ts.move((int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), block));
            
            if (ts.movePrevious() && ts.token().id() == JavaTokenId.RBRACE) {
                result.add(new int[] {ts.offset(), ts.offset() + ts.token().length()});
            }
        }
        
        return result;
    }
    
    static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(MarkOccurrencesHighlighter.class);
        
        if (bag == null) {
            doc.putProperty(MarkOccurrencesHighlighter.class, bag = new OffsetsBag(doc, false));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            final OffsetsBag bagFin = bag;
            DocumentListener l = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }
                public void removeUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }
                public void changedUpdate(DocumentEvent e) {}
            };
            
            doc.addDocumentListener(l);
            
            if (stream instanceof DataObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "MarkOccurrences Highlights Bag", new Object[] {((DataObject) stream).getPrimaryFile(), bag}); //NOI18N
                Logger.getLogger("TIMER").log(Level.FINE, "MarkOccurrences Highlights Bag Listener", new Object[] {((DataObject) stream).getPrimaryFile(), l}); //NOI18N
            }
        }
        
        return bag;
    }
    
}

