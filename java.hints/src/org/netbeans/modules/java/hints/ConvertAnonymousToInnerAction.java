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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.hints.infrastructure.HintAction;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertAnonymousToInnerAction extends HintAction {

    public ConvertAnonymousToInnerAction() {
        putValue(NAME, NbBundle.getMessage(ConvertAnonymousToInnerAction.class, "CTL_ConvertAnonymousToInner"));
    }

    protected void perform(JavaSource js,final int[] selection) {
        final Fix[] f = new Fix[1];
        String error = null;
        
        if (selection[0] == selection[1]) {
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {}
                    public void run(CompilationController parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.RESOLVED);
                        TreePath path = parameter.getTreeUtilities().pathFor(selection[0]);
                        
                        while (path != null && path.getLeaf().getKind() != Kind.NEW_CLASS)
                            path = path.getParentPath();
                        
                        if (path == null)
                            return ;
                        
                        f[0] = ConvertAnonymousToInner.computeFix(parameter, path, -1);
                    }
                }, true);
                
                if (f[0] == null) {
                    error = "ERR_CaretNotInAnonymousInnerclass";
                }
            } catch (IOException e) {
                error = "ERR_SelectionNotSupported";
                Exceptions.printStackTrace(e);
            }
        } else {
            error = "ERR_SelectionNotSupported";
        }
        
        if (f[0] != null) {
            try {
                f[0].implement();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            
            return ;
        }
        
        if (error != null) {
            String errorText = NbBundle.getMessage(ConvertAnonymousToInnerAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    @Override
    protected boolean requiresSelection() {
        return false;
    }

}
