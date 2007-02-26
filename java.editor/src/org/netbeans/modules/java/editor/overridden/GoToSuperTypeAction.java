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
package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.java.JavaKit;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class GoToSuperTypeAction extends BaseAction {
    
    public GoToSuperTypeAction() {
        super(JavaKit.gotoSuperImplementationAction, SAVE_POSITION | ABBREV_RESET);
        putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("goto-super-implementation"));
        String name = NbBundle.getBundle(JavaKit.class).getString("goto-super-implementation-trimmed");
        putValue(ExtKit.TRIMMED_TEXT,name);
        putValue(POPUP_MENU_TEXT, name);
    }
    
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        JavaSource js = JavaSource.forDocument(target.getDocument());
        
        if (js == null) {
            Toolkit.getDefaultToolkit().beep();
        }
        
        final List<ElementDescription> result = new ArrayList<ElementDescription>();
        final AnnotationType[] type  = new AnnotationType[1];
        
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {}
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED); //!!!
                    
                    TreePath path = parameter.getTreeUtilities().pathFor(target.getCaretPosition());
                    
                    while (path != null && path.getLeaf().getKind() != Kind.METHOD) {
                        path = path.getParentPath();
                    }
                    
                    if (path == null) {
                        Toolkit.getDefaultToolkit().beep();
                        return ;
                    }
                    
                    Element resolved = parameter.getTrees().getElement(path);
                    
                    if (resolved == null || resolved.getKind() != ElementKind.METHOD) {
                        Toolkit.getDefaultToolkit().beep();
                        return ;
                    }
                    
                    ExecutableElement ee = (ExecutableElement) resolved;
                    
                    type[0] = IsOverriddenAnnotationHandler.detectOverrides(parameter, (TypeElement) ee.getEnclosingElement(), ee, result);
                }
                
            }, true);
            
            if (type[0] == null) {
                Toolkit.getDefaultToolkit().beep();
                return ;
            }
            
            Point p = new Point(target.modelToView(target.getCaretPosition()).getLocation());
            
            SwingUtilities.convertPointToScreen(p, target);
            
            IsOverriddenAnnotation.performGoToAction(type[0], result, p, "");
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
    }

}
