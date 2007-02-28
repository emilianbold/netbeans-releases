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
 *
 */

package org.netbeans.modules.vmd.midp.codegen;

import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.vmd.api.codegen.CodeGlobalLevelPresenter;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.openide.util.Exceptions;

import javax.swing.text.StyledDocument;
import java.io.IOException;

/**
 * @author David Kaspar
 */
public class MidpCodePresenterSupport {

    public static Presenter createAddImportPresenter () {
        return new CodeGlobalLevelPresenter() {
            protected void performGlobalGeneration (StyledDocument styledDocument) {
                try {
                    JavaSource.forDocument (styledDocument).runModificationTask (new CancellableTask<WorkingCopy>() {
                        public void cancel () {
                        }

                        public void run (WorkingCopy parameter) throws Exception {
                            String fqn = getComponent ().getType ().getString ();
                            parameter.toPhase (JavaSource.Phase.PARSED);
                            SourceUtils.resolveImport (parameter, new TreePath (parameter.getCompilationUnit ()), fqn);
                        }
                    }).commit ();
                } catch (IOException e) {
                    Exceptions.printStackTrace (e);
                }
            }
        };

    }


}
