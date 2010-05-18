/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbrefactoring;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;

/**
 * A plugin for EJB refactorings, only displays a warning message.
 * 
 * @author Erno Mononen
 */
public class EjbRefactoringPlugin implements RefactoringPlugin{
    
    /**
     * The localized message to be displayed.
     */ 
    private final String message;
    private AbstractRefactoring refactoring;

    public EjbRefactoringPlugin(AbstractRefactoring refactoring, String message) {
        this.refactoring = refactoring;
        this.message = message;
    }
    
    public Problem preCheck() {
        return null;
    }
    
    public Problem checkParameters() {
        final Problem[] result = new Problem[]{new Problem(false, message)};
        final TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null){
            FileObject fo = tph.getFileObject();
            if (fo != null){
                try {
                    JavaSource js = JavaSource.forFileObject(fo);
                    js.runUserActionTask(new CancellableTask<CompilationController>() {
                        public void run(CompilationController info) throws Exception {
                            info.toPhase(JavaSource.Phase.RESOLVED);
                            Element el = tph.resolveElement(info);
                            if (el.getModifiers().contains(Modifier.PRIVATE)){
                                result[0] = null;
                            }
                        }

                        public void cancel() {
                            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                        }
                    }, true);
                } catch (IOException ex) {
                    Logger.global.log(Level.INFO, null, ex);
                }
            }
        }
        return result[0];
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    public void cancelRequest() {
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        return null;
    }
    
    
}
