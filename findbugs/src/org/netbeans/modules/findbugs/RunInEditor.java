/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.findbugs;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.findbugs.RunFindBugs.SigFilesValidator;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class RunInEditor implements CancellableTask<CompilationInfo> {

    private static final Logger LOG = Logger.getLogger(RunInEditor.class.getName());
    public static final String RUN_IN_EDITOR = "run-in-editor";
    public static final boolean RUN_IN_EDITOR_DEFAULT = false;
    public static final String HINTS_KEY = RunInEditor.class.getName();
    
    private final AtomicBoolean cancel = new AtomicBoolean();
    private long cancelledAt;

    private String previousTimeStamps;
    
    @Override
    public void run(final CompilationInfo parameter) throws Exception {
        cancel.set(false);

        try {
            doRun(parameter);
        } finally {
            if (cancel.get()) {
                LOG.log(Level.FINE, "Cancelling RunInEditor took: {0}ms", System.currentTimeMillis() - cancelledAt);
            }
        }
    }

    private void doRun(final CompilationInfo parameter) throws Exception {
        LOG.log(Level.FINE, "RunInEditor");

        if (!NbPreferences.forModule(RunInEditor.class).getBoolean(RUN_IN_EDITOR, RUN_IN_EDITOR_DEFAULT)) return;

        if (ErrorsCache.isInError(parameter.getFileObject(), true)) {
            LOG.log(Level.FINE, "Not running FindBugs in editor, as the current file has been compiled with errors.");
            return ;
        }

        ClassPath sourceCP = parameter.getClasspathInfo().getClassPath(PathKind.SOURCE);
        FileObject sourceRoot = sourceCP.findOwnerRoot(parameter.getFileObject());

        if (sourceRoot == null) {
            HintsController.setErrors(parameter.getFileObject(), RunInEditor.class.getName(), Collections.<ErrorDescription>emptyList());
            return;
        }

        final Set<String> classNames = new HashSet<String>();

        new TreePathScanner<Void, Void>() {
            @Override public Void visitClass(ClassTree node, Void p) {
                Element el = parameter.getTrees().getElement(getCurrentPath());

                if (el != null && (el.getKind().isClass() || el.getKind().isClass())) {
                    classNames.add(parameter.getElements().getBinaryName((TypeElement) el).toString());
                }

                return super.visitClass(node, p);
            }
        }.scan(parameter.getCompilationUnit(), null);

        final AtomicLong latest = new AtomicLong(-1);
        final AtomicReference<String> newTimeStampsString = new AtomicReference<String>();
        
        List<ErrorDescription> bugs = RunFindBugs.runFindBugs(parameter, null, null, sourceRoot, classNames, null, new SigFilesValidator() {
            @Override public boolean validate(Iterable<? extends FileObject> files) {
                StringBuilder timeStamps = new StringBuilder();

                for (FileObject f : files) {
                    if (timeStamps.length() != 0) timeStamps.append("-");
                    long ts = f.lastModified().getTime();
                    timeStamps.append(ts);
                    latest.set(Math.max(latest.get(), ts));
                }
                
                String timeStampsString = timeStamps.toString();

                if (timeStampsString.equals(previousTimeStamps)) {
                    LOG.log(Level.FINE, "Classfiles did not change, skipping FindBugs in editor");
                    return false;
                }

                newTimeStampsString.set(timeStampsString);
                return true;
            }
        });

        if (cancel.get() || bugs == null) return;
        
        long documentTimeStamp;
        
        DataObject d = DataObject.find(parameter.getFileObject());
        
        if (d.isModified()) {
            Document doc = parameter.getDocument();
            
            documentTimeStamp = doc != null ? DocumentUtilities.getDocumentTimestamp(doc) : 0;
        } else {
            documentTimeStamp = parameter.getFileObject().lastModified().getTime();
        }
        
        if (documentTimeStamp > 0 && latest.get() > 0 && documentTimeStamp > latest.get()) {
            LOG.log(Level.FINE, "Document is too new for the classfiles, skipping FindBugs in editor (classfiles timestamp {0}, document timestamp {1})", new Object[] {latest.get(), documentTimeStamp});
            return ;
        }
        
        HintsController.setErrors(parameter.getFileObject(), HINTS_KEY, bugs);
        
        previousTimeStamps = newTimeStampsString.get();
    }

    @Override public void cancel() {
        cancel.set(true);
        cancelledAt = System.currentTimeMillis();
    }

    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class FactoryImpl extends EditorAwareJavaSourceTaskFactory {

        public FactoryImpl() {
            super(Phase.RESOLVED, Priority.MIN);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new RunInEditor();
        }

    }
}
