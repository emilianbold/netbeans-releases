/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl.providers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.modules.profiler.api.java.ProfilerTypeUtils;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx;
import org.netbeans.modules.profiler.spi.java.GoToSourceProvider;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ServiceProvider(service = GoToSourceProvider.class)
public final class GoToJavaSourceProvider extends GoToSourceProvider {
    @Override
    public boolean openSource(final Lookup.Provider project, final String className, final String methodName, final String signature, final int line) {
        final AtomicBoolean result = new AtomicBoolean(false);

        SourceClassInfo ci = ProfilerTypeUtils.resolveClass(className, project);
        FileObject sourceFile = ci != null ? ci.getFile() : null;

        if (sourceFile == null) {
            return false;
        }

        JavaSource js = JavaSource.forFileObject(sourceFile);
        if (js != null) {

            try {

                js.runWhenScanFinished(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws Exception {
                        if (!controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).equals(JavaSource.Phase.ELEMENTS_RESOLVED)) {
                            return;
                        }
                        TypeElement parentClass = ElementUtilitiesEx.resolveClassByName(className, controller, true);
                        if (ElementOpen.open(controller.getClasspathInfo(), parentClass)) {
                            Document doc = controller.getDocument();
                            if (doc != null && doc instanceof StyledDocument) {
                                if (openAtLine(controller, doc, methodName, line)) {
                                    result.set(true);
                                    return;
                                }
                                if (methodName != null) {
                                    ExecutableElement methodElement = ElementUtilitiesEx.resolveMethodByName(controller, parentClass, methodName, signature);
                                    if (methodElement != null && ElementOpen.open(controller.getClasspathInfo(), methodElement)) {
                                        result.set(true);
                                        return;
                                    }
                                }
                            }
                            result.set(true);
                        }
                    }
                }, true);
            } catch (IOException e) {
            }
            return result.get();
        }
        return false;
    }

    @Override
    public boolean openFile(final FileObject srcFile, final int offset) {
        final boolean[] rslt = new boolean[] {false};
        Runnable action = new Runnable() {

            @Override
            public void run() {
                rslt[0] = UiUtils.open(srcFile, offset);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(action);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (InvocationTargetException e) {
                ProfilerLogger.log(e);
            }
        }
        return rslt[0];
    }

    private static boolean openAtLine(CompilationController controller, Document doc, String methodName, int line) {
        try {
            if (line > -1) {
                DataObject od = DataObject.find(controller.getFileObject());
                int offset = NbDocument.findLineOffset((StyledDocument) doc, line);
                ExecutableElement parentMethod = controller.getTreeUtilities().scopeFor(offset).getEnclosingMethod();
                if (parentMethod != null) {
                    String offsetMethodName = parentMethod.getSimpleName().toString();
                    if (methodName.equals(offsetMethodName)) {
                        LineCookie lc = od.getCookie(LineCookie.class);
                        if (lc != null) {
                            Line l = lc.getLineSet().getCurrent(line - 1);

                            if (l != null) {
                                l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(GoToJavaSourceProvider.class.getName()).log(Level.WARNING, "Error accessing dataobject", e);
        }
        return false;
    }
}
