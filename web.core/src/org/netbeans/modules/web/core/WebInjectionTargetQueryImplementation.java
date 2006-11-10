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

package org.netbeans.modules.web.core;

import java.io.IOException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
        
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;

import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 *
 * @author Martin Adamek
 */
public class WebInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public WebInjectionTargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(final FileObject fileObject, final String fqn) {
        if (fileObject == null) {
            throw new NullPointerException("Passed null FileObject to WebInjectionTargetQueryImplementation.isInjectionTarget(FileObject, String)"); // NOI18N
        }
        
        final boolean[] ret = new boolean[] {false};
        WebModule webModule = WebModule.getWebModule(fileObject);
        if (webModule != null &&
                !webModule.getJ2eePlatformVersion().equals("1.3") &&
                !webModule.getJ2eePlatformVersion().equals("1.4")) {
            
            JavaSource src = JavaSource.forFileObject(fileObject);
                        
            CancellableTask task = new CancellableTask<CompilationController>() {
                public void run(CompilationController compilationController) throws IOException {
                    compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    final Elements elements = compilationController.getElements();
                    final TypeElement servletElement = elements.getTypeElement("javax.servlet.Servlet");
                    final TypeElement thisElement = elements.getTypeElement(fqn);
                    ret[0] = compilationController.getTypes().isSubtype(thisElement.asType(),servletElement.asType());
                }

                public void cancel() {
                }
            };
            try {
                src.runUserActionTask(task, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return ret[0];
    }

    public boolean isStaticReferenceRequired(FileObject fileObject, String fqn) {
        return false;
    }

}
