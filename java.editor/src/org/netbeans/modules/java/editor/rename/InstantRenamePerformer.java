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
package org.netbeans.modules.java.editor.rename;

import com.sun.source.util.TreePath;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.SyncDocumentRegion;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.netbeans.modules.java.editor.semantic.ColoringAttributes;
import org.netbeans.modules.java.editor.semantic.FindLocalUsagesQuery;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Lahoda
 */
public class InstantRenamePerformer implements DocumentListener, KeyListener {
    
    private SyncDocumentRegion region;
    private Document doc;
    private JTextComponent target;
    private MutablePositionRegion mainRegion;
    
    /** Creates a new instance of InstantRenamePerformer */
    private InstantRenamePerformer(JTextComponent target, Set<Highlight> highlights, int caretOffset) throws BadLocationException {
	this.target = target;
	doc = target.getDocument();
	
	MutablePositionRegion mainRegion = null;
	List<MutablePositionRegion> regions = new ArrayList<MutablePositionRegion>();
	List<Highlight> newHighlights = new ArrayList<Highlight>();
	
	for (Highlight h : highlights) {
	    Position start = NbDocument.createPosition(doc, h.getStart(), Bias.Backward);
	    Position end = NbDocument.createPosition(doc, h.getEnd(), Bias.Forward);
	    MutablePositionRegion current = new MutablePositionRegion(start, end);
	    
	    if (isIn(current, caretOffset)) {
		mainRegion = current;
	    } else {
		regions.add(current);
	    }
	    
	    newHighlights.add(new RenameHighlight(current));
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
	
	Highlighter.getDefault().setHighlights(getFileObject(), "instant-rename", newHighlights);
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
    }
    
    private FileObject getFileObject() {
	DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
	
	if (od == null)
	    return null;
	
	return od.getPrimaryFile();
    }
    
    public static void invokeInstantRename(JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();
            String ident = Utilities.getIdentifier(Utilities.getDocument(target), caret);
            
            if (ident == null) {
                Utilities.setStatusBoldText(target, "Cannot perform instant rename here.");
                return;
            }
            
            DataObject od = (DataObject) target.getDocument().getProperty(Document.StreamDescriptionProperty);
            JavaSource js = JavaSource.forFileObject(od.getPrimaryFile());
            final boolean[] wasResolved = new boolean[1];
            final Set<Highlight>[] changePoints = new Set[1];
            
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController controller) throws Exception {
                    if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0)
                        return;
                    
                    changePoints[0] = computeChangePoints(controller, caret, wasResolved);
                }
            }, true);
            
            if (wasResolved[0]) {
                if (changePoints[0] != null) {
                    doInstantRename(changePoints[0], target, caret, ident);
                } else {
                    doFullRename(od.getCookie(EditorCookie.class), od.getNodeDelegate());
                }
            } else {
                Utilities.setStatusBoldText(target, "Cannot perform instant rename here.");
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
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
    
    private static void doInstantRename(Set<Highlight> changePoints, JTextComponent target, int caret, String ident) throws BadLocationException {
        InstantRenamePerformer.performInstantRename(target, changePoints, caret);
    }
    
    static Set<Highlight> computeChangePoints(CompilationInfo info, final int caret, final boolean[] wasResolved) throws IOException {
        TreePath path = info.getTreeUtilities().pathFor(caret);
        Element el = info.getTrees().getElement(path);
        
        if (el == null) {
            wasResolved[0] = false;
            return null;
        }
        
        final Document doc = info.getDocument();
        
        //#89736: if the caret is not in the resolved element's name, no rename:
        final Highlight name = org.netbeans.modules.java.editor.semantic.Utilities.createHighlight(info, doc, path, EnumSet.of(ColoringAttributes.MARK_OCCURRENCES), null);
        
        doc.render(new Runnable() {
            public void run() {
                wasResolved[0] = name.getStart() <= caret && caret <= name.getEnd();
            }
        });
        
        if (!wasResolved[0])
            return null;
        
        if (el.getKind() == ElementKind.CONSTRUCTOR) {
            //for constructor, work over the enclosing class:
            el = el.getEnclosingElement();
        }
        
        if (allowInstantRename(el)) {
            final Set<Highlight> points = new HashSet<Highlight>(new FindLocalUsagesQuery().findUsages(el, info, doc));
            
            if (el.getKind().isClass()) {
                //rename also the constructors:
                for (ExecutableElement c : ElementFilter.constructorsIn(el.getEnclosedElements())) {
                    TreePath t = info.getTrees().getPath(c);
                    
                    if (t != null) {
                        Highlight h = org.netbeans.modules.java.editor.semantic.Utilities.createHighlight(info, doc, t, EnumSet.of(ColoringAttributes.MARK_OCCURRENCES), null);
                        
                        if (h != null) {
                            points.add(h);
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
        }
        
        return null;
    }
    
    private static boolean allowInstantRename(Element e) {
        if (org.netbeans.modules.java.editor.semantic.Utilities.isPrivateElement(e)) {
            return true;
        }
        
        //#92160: check for local classes:
        if (e.getKind() == ElementKind.CLASS) {//only classes can be local
            Element enclosing = e.getEnclosingElement();
            
            return LOCAL_CLASS_PARENTS.contains(enclosing.getKind());
        }
        
        return false;
    }
    
    private static boolean overlapsWithGuardedBlocks(Document doc, Set<Highlight> highlights) {
        if (!(doc instanceof GuardedDocument))
            return false;
        
        GuardedDocument gd = (GuardedDocument) doc;
        MarkBlock current = gd.getGuardedBlockChain().getChain();
        
        while (current != null) {
            for (Highlight h : highlights) {
                if ((current.compare(h.getStart(), h.getEnd()) & MarkBlock.OVERLAP) != 0) {
                    return true;
                }
            }
            
            current = current.getNext();
        }
        
        return false;
    }
    
    private static final Set<ElementKind> LOCAL_CLASS_PARENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT, ElementKind.METHOD, ElementKind.STATIC_INIT);
    
    
    public static void performInstantRename(JTextComponent target, Set<Highlight> highlights, int caretOffset) throws BadLocationException {
	new InstantRenamePerformer(target, highlights, caretOffset);
    }

    private boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private boolean inSync;
    
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
	inSync = true;
	region.sync(0);
	inSync = false;
	target.repaint();
    }

    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null) {
            return ;
        }
        
	inSync = true;
	region.sync(0);
	inSync = false;
	target.repaint();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void caretUpdate(CaretEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
	if (   (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0) 
            || (e.getKeyCode() == KeyEvent.VK_ENTER  && e.getModifiers() == 0)) {
	    release();
	    e.consume();
	}
    }

    public void keyReleased(KeyEvent e) {
    }

    private void release() {
	target.putClientProperty(InstantRenamePerformer.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(null);
        }
	target.removeKeyListener(this);
	Highlighter.getDefault().setHighlights(getFileObject(), "instant-rename", Collections.emptyList());

	region = null;
	doc = null;
	target = null;
	mainRegion = null;
    }

    private static final class RenameHighlight implements Highlight {

	private PositionRegion region;
	private static final Coloring COLORING = new Coloring(null, null, new Color(138, 191, 236));
	
	public RenameHighlight(PositionRegion region) {
	    this.region = region;
	}
	
        public int getStart() {
	    return region.getStartOffset();
        }

        public int getEnd() {
	    return region.getEndOffset();
        }

        public Coloring getColoring() {
	    return COLORING;
        }
    }
    
}
