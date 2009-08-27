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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.java.editor.rename;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.editor.java.JavaKit.JavaDeleteCharAction;
import org.netbeans.modules.editor.java.RunOffAWT;
import org.netbeans.modules.editor.java.RunOffAWT.Worker;
import org.netbeans.modules.java.editor.javadoc.JavadocImports;
import org.netbeans.modules.java.editor.semantic.FindLocalUsagesQuery;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Lahoda
 */
public class InstantRenamePerformer implements DocumentListener, KeyListener {
    
    private static final Logger LOG = Logger.getLogger(InstantRenamePerformer.class.getName());
    
    private SyncDocumentRegion region;
    private int span;
    private Document doc;
    private JTextComponent target;
    
    private AttributeSet attribs = null;
    private AttributeSet attribsLeft = null;
    private AttributeSet attribsRight = null;
    private AttributeSet attribsMiddle = null;
    private AttributeSet attribsAll = null;

    private AttributeSet attribsSlave = null;
    private AttributeSet attribsSlaveLeft = null;
    private AttributeSet attribsSlaveRight = null;
    private AttributeSet attribsSlaveMiddle = null;
    private AttributeSet attribsSlaveAll = null;
    
    /** Creates a new instance of InstantRenamePerformer */
    private InstantRenamePerformer(JTextComponent target, Set<Token> highlights, int caretOffset) throws BadLocationException {
	this.target = target;
	doc = target.getDocument();
	
	MutablePositionRegion mainRegion = null;
	List<MutablePositionRegion> regions = new ArrayList<MutablePositionRegion>();
        
	for (Token h : highlights) {
	    Position start = NbDocument.createPosition(doc, h.offset(null), Bias.Backward);
	    Position end = NbDocument.createPosition(doc, h.offset(null) + h.length(), Bias.Forward);
	    MutablePositionRegion current = new MutablePositionRegion(start, end);
	    
	    if (isIn(current, caretOffset)) {
		mainRegion = current;
	    } else {
		regions.add(current);
	    }
	}
	
	if (mainRegion == null) {
	    throw new IllegalArgumentException("No highlight contains the caret.");
	}
	
	regions.add(0, mainRegion);
	
	region = new SyncDocumentRegion(doc, regions);
	
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(this);
        }
        
	target.addKeyListener(this);
	
	target.putClientProperty(InstantRenamePerformer.class, this);
	
        requestRepaint();
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
        
        span = region.getFirstRegionLength();
    }
    
    public static void invokeInstantRename(JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();
            String ident = Utilities.getIdentifier(Utilities.getDocument(target), caret);
            
            if (ident == null) {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenamePerformer.class, "WARN_CannotPerformHere"));
                return;
            }
            
            DataObject od = (DataObject) target.getDocument().getProperty(Document.StreamDescriptionProperty);
            JavaSource js = od != null ? JavaSource.forFileObject(od.getPrimaryFile()) : null;

            if (js == null) {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenamePerformer.class, "WARN_CannotPerformHere"));
                return ;
            }
            
            final boolean[] wasResolved = new boolean[1];

            Set<Token> changePoints = RunOffAWT.computeOffAWT(new Worker<Set<Token>>() {
                public Set<Token> process(CompilationInfo info) {
                    try {
                        return computeChangePoints(info, caret, wasResolved);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        return null;
                    }
                }
            }, "Instant Rename", js, Phase.RESOLVED);
            
            if (changePoints != null && wasResolved[0]) {
                if (changePoints != null) {
                    doInstantRename(changePoints, target, caret, ident);
                } else {
                    doFullRename(od.getCookie(EditorCookie.class), od.getNodeDelegate());
                }
            } else {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenamePerformer.class, "WARN_CannotPerformHere"));
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
    }
    private static void doFullRename(EditorCookie ec, Node n) {
        
        InstanceContent ic = new InstanceContent();
        ic.add(ec);
        ic.add(n);
        Lookup actionContext = new AbstractLookup(ic);
        
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
    
    private static void doInstantRename(Set<Token> changePoints, JTextComponent target, int caret, String ident) throws BadLocationException {
        InstantRenamePerformer.performInstantRename(target, changePoints, caret);
    }
    
    static Set<Token> computeChangePoints(final CompilationInfo info, final int caret, final boolean[] wasResolved) throws IOException {
        final Document doc = info.getDocument();
        
        if (doc == null)
            return null;
        
        final int[] adjustedCaret = new int[] {caret};
        final boolean[] insideJavadoc = {false};
        
        doc.render(new Runnable() {
            public void run() {
                TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(info.getTokenHierarchy(), caret);
                
                ts.move(caret);
                
                if (ts.moveNext() && ts.token()!=null) {
                    if (ts.token().id() == JavaTokenId.IDENTIFIER) {
                        adjustedCaret[0] = ts.offset() + ts.token().length() / 2 + 1;
                    } else if (ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
                        TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
                        if (jdts != null && JavadocImports.isInsideReference(jdts, caret)) {
                            jdts.move(caret);
                            if (jdts.moveNext() && jdts.token().id() == JavadocTokenId.IDENT) {
                                adjustedCaret[0] = jdts.offset();
                                insideJavadoc[0] = true;
                            }
                        }
                    }
                }
            }
        });
        
        TreePath path = insideJavadoc[0]? null: info.getTreeUtilities().pathFor(adjustedCaret[0]);
        
        //correction for int something[]:
        if (path != null && path.getParentPath() != null) {
            Kind leafKind = path.getLeaf().getKind();
            Kind parentKind = path.getParentPath().getLeaf().getKind();
            
            if (leafKind == Kind.ARRAY_TYPE && parentKind == Kind.VARIABLE) {
                long typeEnd = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), path.getLeaf());
                long variableEnd = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), path.getLeaf());
                
                if (typeEnd == variableEnd) {
                    path = path.getParentPath();
                }
            }
        }
        
        Element el = insideJavadoc[0]
                ? JavadocImports.findReferencedElement(info, adjustedCaret[0])
                : info.getTrees().getElement(path);
        
        if (el == null) {
            wasResolved[0] = false;
            return null;
        }
        
        //#89736: if the caret is not in the resolved element's name, no rename:
        final Token name = insideJavadoc[0]
                ? JavadocImports.findNameTokenOfReferencedElement(info, adjustedCaret[0])
                : org.netbeans.modules.java.editor.semantic.Utilities.getToken(info, doc, path);
        
        if (name == null)
            return null;
        
        doc.render(new Runnable() {
            public void run() {
                wasResolved[0] = name.offset(null) <= caret && caret <= (name.offset(null) + name.length());
            }
        });
        
        if (!wasResolved[0])
            return null;
        
        if (el.getKind() == ElementKind.CONSTRUCTOR) {
            //for constructor, work over the enclosing class:
            el = el.getEnclosingElement();
        }
        
        if (allowInstantRename(el)) {
            final Set<Token> points = new HashSet<Token>(new FindLocalUsagesQuery(true).findUsages(el, info, doc));
            
            if (el.getKind().isClass()) {
                //rename also the constructors:
                for (ExecutableElement c : ElementFilter.constructorsIn(el.getEnclosedElements())) {
                    TreePath t = info.getTrees().getPath(c);
                    
                    if (t != null) {
                        Token token = org.netbeans.modules.java.editor.semantic.Utilities.getToken(info, doc, t);
                        
                        if (token != null) {
                            points.add(token);
                        }
                    }
                }
            }
            
            final boolean[] overlapsWithGuardedBlocks = new boolean[1];
            
            doc.render(new Runnable() {
                public void run() {
                    overlapsWithGuardedBlocks[0] = overlapsWithGuardedBlocks(doc, points);
                }
            });
            
            if (overlapsWithGuardedBlocks[0]) {
                return null;
            }
            
            return points;
        } else if (insideJavadoc[0]) {
            // java refactoring does not support javadoc
            wasResolved[0] = false;
        }
        
        return null;
    }

    private static boolean allowInstantRename(Element e) {
        if (org.netbeans.modules.java.editor.semantic.Utilities.isPrivateElement(e)) {
            return true;
        }
        
        if (isInaccessibleOutsideOuterClass(e)) {
            return true;
        }
        
        //#92160: check for local classes:
        if (e.getKind() == ElementKind.CLASS) {//only classes can be local
            Element enclosing = e.getEnclosingElement();
            final ElementKind enclosingKind = enclosing.getKind();

            //#150352: parent is annonymous class
            if (enclosingKind == ElementKind.CLASS) {
                final Set<ElementKind> fm = EnumSet.of(ElementKind.METHOD, ElementKind.FIELD);
                if (enclosing.getSimpleName().length() == 0 || fm.contains(enclosing.getEnclosingElement().getKind())) {
                    return true;
                }
            }


            return LOCAL_CLASS_PARENTS.contains(enclosingKind);
        }

        if (e.getKind() == ElementKind.TYPE_PARAMETER) {
            return true;
        }
        
        return false;
    }

    /**
     * computes accessibility of members of nested classes
     * @param e member
     * @return {@code true} if the member cannot be accessed outside the outer class
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=169377">169377</a>
     */
    private static boolean isInaccessibleOutsideOuterClass(Element e) {
        Element enclosing = e.getEnclosingElement();
        boolean isStatic = e.getModifiers().contains(Modifier.STATIC);
        ElementKind kind = e.getKind();
        if (isStatic || kind == ElementKind.CLASS) {
            // static declaration of nested class, interface, enum, ann type, method, field
            // or inner class
            return isAnyEncloserPrivate(e);
        } else if (enclosing != null) {
            // final is enum, ann type and some classes
            ElementKind enclosingKind = enclosing.getKind();
            boolean isEnclosingFinal = enclosing.getModifiers().contains(Modifier.FINAL)
                    // ann type is not final even if it cannot be subclassed
                    || enclosingKind == ElementKind.ANNOTATION_TYPE
                    // enum implementing an interface is not final even if it cannot be subclassed
                    || enclosingKind == ElementKind.ENUM;
            // do not try to rename members of class that extends or implements anything
            // it would require deeper analyze
            boolean isSubclass = enclosingKind == ElementKind.CLASS
                    && ( !"java.lang.Object".equals(((TypeElement) enclosing).getSuperclass().toString())
                            || !((TypeElement) enclosing).getInterfaces().isEmpty() );
            return isEnclosingFinal && !isSubclass && isAnyEncloserPrivate(e);
        }
        return false;
    }

    private static boolean isAnyEncloserPrivate(Element e) {
        Element enclosing = e.getEnclosingElement();
        while (enclosing != null && (enclosing.getKind().isClass() || enclosing.getKind().isInterface())) {
            boolean isPrivateClass = enclosing.getModifiers().contains(Modifier.PRIVATE);
            if (isPrivateClass) {
                return true;
            }
            enclosing = enclosing.getEnclosingElement();
        }
        return false;
    }
    
    private static boolean overlapsWithGuardedBlocks(Document doc, Set<Token> highlights) {
        if (!(doc instanceof GuardedDocument))
            return false;
        
        GuardedDocument gd = (GuardedDocument) doc;
        MarkBlock current = gd.getGuardedBlockChain().getChain();
        
        while (current != null) {
            for (Token h : highlights) {
                if ((current.compare(h.offset(null), h.offset(null) + h.length()) & MarkBlock.OVERLAP) != 0) {
                    return true;
                }
            }
            
            current = current.getNext();
        }
        
        return false;
    }
    
    private static final Set<ElementKind> LOCAL_CLASS_PARENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT, ElementKind.METHOD, ElementKind.STATIC_INIT);
    
    
    public static void performInstantRename(JTextComponent target, Set<Token> highlights, int caretOffset) throws BadLocationException {
	new InstantRenamePerformer(target, highlights, caretOffset);
    }

    private boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private boolean inSync;
    
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        //check for modifications outside the first region:
        if (e.getOffset() < region.getFirstRegionStartOffset() || (e.getOffset() + e.getLength()) > region.getFirstRegionEndOffset()) {
            release();
            return;
        }
        
	inSync = true;
	region.sync(0);
        span = region.getFirstRegionLength();
	inSync = false;
        
	requestRepaint();
    }

    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        if (e.getLength() == 1) {
            if ((e.getOffset() < region.getFirstRegionStartOffset() || e.getOffset() > region.getFirstRegionEndOffset())) {
                release();
                return;
            }

            if (e.getOffset() == region.getFirstRegionStartOffset() && region.getFirstRegionLength() > 0 && region.getFirstRegionLength() == span) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("e.getOffset()=" + e.getOffset());
                    LOG.fine("region.getFirstRegionStartOffset()=" + region.getFirstRegionStartOffset());
                    LOG.fine("region.getFirstRegionEndOffset()=" + region.getFirstRegionEndOffset());
                    LOG.fine("span= " + span);
                }
                JavaDeleteCharAction jdca = (JavaDeleteCharAction) target.getClientProperty(JavaDeleteCharAction.class);
                
                if (jdca != null && !jdca.getNextChar()) {
                    undo();
                } else {
                    release();
                }
                
                return;
            }
            
            if (e.getOffset() == region.getFirstRegionEndOffset() && region.getFirstRegionLength() > 0 && region.getFirstRegionLength() == span) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("e.getOffset()=" + e.getOffset());
                    LOG.fine("region.getFirstRegionStartOffset()=" + region.getFirstRegionStartOffset());
                    LOG.fine("region.getFirstRegionEndOffset()=" + region.getFirstRegionEndOffset());
                    LOG.fine("span= " + span);
                }
            //XXX: moves the caret anyway:
//                JavaDeleteCharAction jdca = (JavaDeleteCharAction) target.getClientProperty(JavaDeleteCharAction.class);
//
//                if (jdca != null && jdca.getNextChar()) {
//                    undo();
//                } else {
                    release();
//                }

                return;
            }
        } else {
            //selection/multiple characters removed:
            int removeSpan = e.getLength() + region.getFirstRegionLength();
            
            if (span < removeSpan) {
                release();
                return;
            }
        }
        
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null) {
            return ;
        }
        
	inSync = true;
	region.sync(0);
        span = region.getFirstRegionLength();
	inSync = false;
        
	requestRepaint();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void caretUpdate(CaretEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public synchronized void keyPressed(KeyEvent e) {
	if (   (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0) 
            || (e.getKeyCode() == KeyEvent.VK_ENTER  && e.getModifiers() == 0)) {
	    release();
	    e.consume();
	}
    }

    public void keyReleased(KeyEvent e) {
    }

    private void release() {
        if (target == null) {
            //already released
            return ;
        }
        
	target.putClientProperty(InstantRenamePerformer.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(null);
        }
	target.removeKeyListener(this);
	target = null;

	region = null;
        attribs = null;
        
        requestRepaint();

	doc = null;
    }

    private void undo() {
        if (doc instanceof BaseDocument && ((BaseDocument) doc).isAtomicLock()) {
            ((BaseDocument) doc).atomicUndo();
        } else {
            UndoableEdit undoMgr = (UndoableEdit) doc.getProperty(BaseDocument.UNDO_MANAGER_PROP);
            if (target != null && undoMgr != null) {
                try {
                    undoMgr.undo();
                } catch (CannotUndoException e) {
                    Logger.getLogger(InstantRenamePerformer.class.getName()).log(Level.WARNING, null, e);
                }
            }
        }
    }
    
    private void requestRepaint() {
        if (region == null) {
            OffsetsBag bag = getHighlightsBag(doc);
            bag.clear();
        } else {
            // Compute attributes
            if (attribs == null) {
                // read the attributes for the master region
                attribs = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext"); //NOI18N
                Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
                Color background = (Color) attribs.getAttribute(StyleConstants.Background);
                attribsLeft = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsRight = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.RightBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsMiddle = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsAll = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.RightBorderLineColor, foreground,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );

                // read the attributes for the slave regions
                attribsSlave = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext-slave"); //NOI18N
                Color slaveForeground = (Color) attribsSlave.getAttribute(StyleConstants.Foreground);
                Color slaveBackground = (Color) attribsSlave.getAttribute(StyleConstants.Background);
                attribsSlaveLeft = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.LeftBorderLineColor, slaveForeground, 
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveRight = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.RightBorderLineColor, slaveForeground, 
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveMiddle = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveAll = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.LeftBorderLineColor, slaveForeground, 
                        EditorStyleConstants.RightBorderLineColor, slaveForeground,
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
            }
            
            OffsetsBag nue = new OffsetsBag(doc);
            for(int i = 0; i < region.getRegionCount(); i++) {
                int startOffset = region.getRegion(i).getStartOffset();
                int endOffset = region.getRegion(i).getEndOffset();
                int size = region.getRegion(i).getLength();
                if (size == 1) {
                    nue.addHighlight(startOffset, endOffset, i == 0 ? attribsAll : attribsSlaveAll);
                } else if (size > 1) {
                    nue.addHighlight(startOffset, startOffset + 1, i == 0 ? attribsLeft : attribsSlaveLeft);
                    nue.addHighlight(endOffset - 1, endOffset, i == 0 ? attribsRight : attribsSlaveRight);
                    if (size > 2) {
                        nue.addHighlight(startOffset + 1, endOffset - 1, i == 0 ? attribsMiddle : attribsSlaveMiddle);
                    }
                }
            }
            
            OffsetsBag bag = getHighlightsBag(doc);
            bag.setHighlights(nue);
        }
    }
    
//    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(138, 191, 236));
    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.red);
    
    private static AttributeSet getSyncedTextBlocksHighlight(String name) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs != null ? fcs.getFontColors(name) : null;
        return as == null ? defaultSyncedTextBlocksHighlight : as;
    }
    
    private static AttributeSet createAttribs(Object... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
            "They are key-value pairs of attributes that will be inserted into the set.";

        List<Object> list = new ArrayList<Object>();
        
        for(int i = keyValuePairs.length / 2 - 1; i >= 0 ; i--) {
            Object attrKey = keyValuePairs[2 * i];
            Object attrValue = keyValuePairs[2 * i + 1];

            if (attrKey != null && attrValue != null) {
                list.add(attrKey);
                list.add(attrValue);
            }
        }
        
        return AttributesUtilities.createImmutable(list.toArray());
    }
    
    public static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(InstantRenamePerformer.class);
        
        if (bag == null) {
            doc.putProperty(InstantRenamePerformer.class, bag = new OffsetsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "Instant Rename Highlights Bag", new Object[] {((DataObject) stream).getPrimaryFile(), bag}); //NOI18N
            }
        }
        
        return bag;
    }
    
}
