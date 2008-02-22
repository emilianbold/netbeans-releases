/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.gsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

/**
 * Task provider which provides tasks for the tasklist corresponding
 * to hints in files.
 * 
 * @todo Caching
 * @todo Register via instanceCreate to ensure this is a singleton
 *   (Didn't work - see uncommented code below; try to fix.)
 * @todo Exclude tasks that are not Rule#showInTaskList==true
 * 
 * Much of this class is based on the similar JavaTaskProvider in
 * java/source by Stanislav Aubrecht and Jan Lahoda
 * 
 * @author Tor Norbye
 */
public class GsfTaskProvider extends PushTaskScanner  {
    private TaskScanningScope scope;
    private Callback callback;
    // Registered by core/tasklist's layer
    private static final String TASKLIST_ERROR = "nb-tasklist-error"; //NOI18N
    private static final String TASKLIST_WARNING = "nb-tasklist-warning"; //NOI18N
    private static final String TASKLIST_ERROR_HINT = "nb-tasklist-errorhint"; //NOI18N
    private static final String TASKLIST_WARNING_HINT = "nb-tasklist-warninghint"; //NOI18N
    private static final Set<RequestProcessor.Task> TASKS = new HashSet<RequestProcessor.Task>();
    private static boolean clearing;
    private static final RequestProcessor WORKER = new RequestProcessor("GSF Task Provider");
    
    //private static final GsfTaskProvider INSTANCE = new GsfTaskProvider(LanguageRegistry.getInstance().getLanguagesDisplayName());
    //public static GsfTaskProvider getInstance() {
    //    return INSTANCE;
    //}
    //
    // For some reason, the instanceCreate method didn't work here, so use a lame setup instead.
    // If you set up instanceCreate again, make sure the refresh() call doesn't do work before
    // the tasklist is open...
    private static GsfTaskProvider INSTANCE;
    public GsfTaskProvider() {
        this(LanguageRegistry.getInstance().getLanguagesDisplayName());
        INSTANCE = this;
    }
    
    private GsfTaskProvider(String languageList) {
        super(NbBundle.getMessage(GsfTaskProvider.class, "GsfTasks", languageList),
              NbBundle.getMessage(GsfTaskProvider.class, "GsfTasksDesc", languageList), null);
    }

    @Override
    public synchronized void setScope(TaskScanningScope scope, Callback callback) {
        //cancel all current operations:
        cancelAllCurrent();
        
        this.scope = scope;
        this.callback = callback;
        
        if (scope == null || callback == null) {
            return;
        }
        
        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            enqueue(new Work(file, callback));
        }
        
        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            // TODO - find out which subgroups to use
            for (SourceGroup sg : ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)) {
                enqueue(new Work(sg.getRootFolder(), callback));
            }
        }
    }

    public static void refresh(FileObject file) {
        if (INSTANCE != null) {
            INSTANCE.refreshImpl(file);
        }
    }
    
    private synchronized void refreshImpl(FileObject file) {
        if (scope == null || callback == null) {
            return;  //nothing to refresh
        }
        if (!scope.isInScope(file)) {
            if (!file.isFolder()) {
                return;
            }
            
            //the given file may be a parent of some file that is in the scope:
            for (FileObject inScope : scope.getLookup().lookupAll(FileObject.class)) {
                if (FileUtil.isParentOf(file, inScope)) {
                    enqueue(new Work(inScope, callback));
                }
            }
            
            return ;
        }
        
        enqueue(new Work(file, callback));
    }
    
    private static void enqueue(Work w) {
        synchronized (TASKS) {
            if (INSTANCE != null && TASKS.size() == 0) {
               INSTANCE.callback.started();
            }
            final RequestProcessor.Task task = WORKER.post(w);
            
            TASKS.add(task);
            task.addTaskListener(new TaskListener() {
                public void taskFinished(org.openide.util.Task task) {
                    synchronized (TASKS) {
                        if (!clearing) {
                            TASKS.remove(task);
                            if (INSTANCE != null && TASKS.size() == 0) {
                               INSTANCE.callback.finished();
                            }
                        }
                    }
                }
            });
            if (task.isFinished()) {
                TASKS.remove(task);
                if (INSTANCE != null && TASKS.size() == 0) {
                   INSTANCE.callback.finished();
                }
            }
        }
    }
    
    private static void cancelAllCurrent() {
        synchronized (TASKS) {
            clearing = true;
            try {
                for (RequestProcessor.Task t : TASKS) {
                    t.cancel();
                }
                TASKS.clear();
            } finally {
                clearing = false;
            }
        }
    }
    
    
    private static final class Work implements Runnable {
        private FileObject fileOrRoot;
        private Callback callback;

        public Work(FileObject fileOrRoot, Callback callback) {
            this.fileOrRoot = fileOrRoot;
            this.callback = callback;
        }
        
        public FileObject getFileOrRoot() {
            return fileOrRoot;
        }

        public Callback getCallback() {
            return callback;
        }
        
        public void run() {
            FileObject file = getFileOrRoot();
            refreshFile(file);
        }
        
        private void refreshFile(final FileObject file) {
            if (file.isFolder()) {
                // HACK Bypass all the libraries in Rails projects
                // TODO FIXME The hints providers need to pass in relevant directories
                if (file.getName().equals("vendor") && file.getParent().getFileObject("nbproject") != null) { // NOI18N
                    return;
                }
                for (FileObject child : file.getChildren()) {
                    refreshFile(child);
                }
                return;
            }
            final LanguageRegistry registry = LanguageRegistry.getInstance();
            final List<Language> applicableLanguages = registry.getApplicableLanguages(file.getMIMEType());
            boolean applicable = false;
            for (Language language : applicableLanguages) {
                HintsProvider provider = language.getHintsProvider();
                if (provider != null) {
                    applicable = true;
                }
            }
            if (!applicable) {
                // No point compiling the file if there are no hintsproviders
                return;
            }

            final List<ErrorDescription> result = new ArrayList<ErrorDescription>();
            
            Source source = Source.forFileObject(file);
            if (source == null) {
                return;
            }

            final List<Task> tasks = new ArrayList<Task>();

            CancellableTask<CompilationController> runner = new CancellableTask<CompilationController>() {
                public void cancel() {
                }

                public void run(CompilationController info) throws Exception {
                    // Ensure document is forced open
                    UiUtils.getDocument(info.getFileObject(), true);

                    info.toPhase(Phase.RESOLVED);

                    for (String mimeType : info.getEmbeddedMimeTypes()) {
                        Collection<? extends ParserResult> embeddedResults = info.getEmbeddedResults(mimeType);
                        for (ParserResult parserResult : embeddedResults) {
                            Language language = registry.getLanguageByMimeType(mimeType);
                            HintsProvider provider = language.getHintsProvider();
                            if (provider == null) {
                                continue;
                            }

                            List<Error> errors = provider.computeErrors(info, result);
                            provider.computeHints(info, result);
                            for (Error error : errors) {
                                try {
                                    int astOffset = error.getStartPosition();
                                    int lexOffset;
                                    if (parserResult.getTranslatedSource() != null) {
                                        lexOffset = parserResult.getTranslatedSource().getLexicalOffset(astOffset);
                                        if (lexOffset == -1) {
                                            continue;
                                        }
                                    } else {
                                        lexOffset = astOffset;
                                    }


                                    int lineno = NbDocument.findLineNumber((StyledDocument)info.getDocument(), lexOffset)+1;
                                    Task task = Task.create(file, 
                                            error.getSeverity() == org.netbeans.modules.gsf.api.Severity.ERROR ? TASKLIST_ERROR : TASKLIST_WARNING,
                                            error.getDisplayName(),
                                            lineno);
                                    tasks.add(task);
                                } catch (IOException ioe) {
                                    Exceptions.printStackTrace(ioe);
                                }
                            }
                        }
                    }
                }
            };
            
            try {
                source.runUserActionTask(runner, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            for (final ErrorDescription hint : result) {
                try {
                    Task task = Task.create(file,
                            severityToTaskListString(hint.getSeverity()),
                            hint.getDescription(),
                            hint.getRange().getBegin().getLine()+1);
                    tasks.add(task);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }

            callback.setTasks(file, tasks);
        }
    }
    
    private static String severityToTaskListString(Severity severity){
        return (severity == Severity.ERROR) ? TASKLIST_ERROR_HINT : TASKLIST_WARNING_HINT;
    }
}
