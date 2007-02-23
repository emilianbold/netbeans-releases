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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.java.editor.semantic.ColoringAttributes;
import org.netbeans.modules.java.editor.semantic.FindLocalUsagesQuery;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Lahoda
 */
public class InstantRenameAction extends BaseAction {
    
    /** Creates a new instance of InstantRenameAction */
    public InstantRenameAction() {
        super("in-place-refactoring", ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET
        | SAVE_POSITION);
    }
    
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
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
            
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
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
            ErrorManager.getDefault().notify(e);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    @Override
    protected Class getShortDescriptionBundleClass() {
        return InstantRenameAction.class;
    }
    
    private void doInstantRename(Set<Highlight> changePoints, JTextComponent target, int caret, String ident) throws BadLocationException {
        InstantRenamePerformer.performInstantRename(target, changePoints, caret);
    }
    
    private void doFullRename(EditorCookie ec, Node n) {
        
        InstanceContent ic = new InstanceContent();
        ic.add(ec);
        ic.add(n);
        Lookup actionContext = new AbstractLookup(ic);
        
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
    
    static Set<Highlight> computeChangePoints(CompilationInfo info, final int caret, final boolean[] wasResolved) throws IOException {
        TreePath path = info.getTreeUtilities().pathFor(caret);
        Element el = info.getTrees().getElement(path);
        
        if (el == null) {
            wasResolved[0] = false;
            return null;
        }
        
        //#89736: if the caret is not in the resolved element's name, no rename:
        final Highlight name = org.netbeans.modules.java.editor.semantic.Utilities.createHighlight(info.getCompilationUnit(), info.getTrees().getSourcePositions(), info.getDocument(), path, EnumSet.of(ColoringAttributes.MARK_OCCURRENCES), null);
        
        info.getDocument().render(new Runnable() {
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
            Set<Highlight> points = new HashSet<Highlight>(new FindLocalUsagesQuery().findUsages(el, info, info.getDocument()));
            
            if (el.getKind().isClass()) {
                //rename also the constructors:
                for (ExecutableElement c : ElementFilter.constructorsIn(el.getEnclosedElements())) {
                    TreePath t = info.getTrees().getPath(c);
                    
                    if (t != null) {
                        Highlight h = org.netbeans.modules.java.editor.semantic.Utilities.createHighlight(info.getCompilationUnit(), info.getTrees().getSourcePositions(), info.getDocument(), t, EnumSet.of(ColoringAttributes.MARK_OCCURRENCES), null);
                        
                        if (h != null) {
                            points.add(h);
                        }
                    }
                }
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
    
    private static final Set<ElementKind> LOCAL_CLASS_PARENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT, ElementKind.METHOD, ElementKind.STATIC_INIT);
    
}
