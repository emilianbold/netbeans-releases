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
import java.util.Set;
import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.java.editor.semantic.FindLocalUsagesQuery;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

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
                    
                    TreePath path = controller.getTreeUtilities().pathFor(caret);
                    Element el = controller.getTrees().getElement(path);
                    
                    if (el == null) {
                        wasResolved[0] = false;
                        return ;
                    }
                    
                    wasResolved[0] = true;
                    
                    if (org.netbeans.modules.java.editor.semantic.Utilities.isPrivateElement(el)) {
                        changePoints[0] = new FindLocalUsagesQuery().findUsages(el, controller, target.getDocument());
                    }
                }
            }, true);
            
            if (wasResolved[0]) {
                if (changePoints[0] != null) {
                    doInstantRename(changePoints[0], target, caret, ident);
                } else {
                    doFullRename();
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
    
    private void doInstantRename(Set<Highlight> changePoints, JTextComponent target, int caret, String ident) throws BadLocationException {
        InstantRenamePerformer.performInstantRename(target, changePoints, caret);
    }
    
    private void doFullRename() {
        final NotifyDescriptor nd = new NotifyDescriptor.Message("Placeholder: Cannot perform instant rename, starting full rename instead");
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                DialogDisplayer.getDefault().notify(nd);
            }
        });
    }
    
}
