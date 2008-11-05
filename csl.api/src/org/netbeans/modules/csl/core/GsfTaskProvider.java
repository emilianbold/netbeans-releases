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

package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.MultiLanguageUserTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.classpath.ClassPathProvider;
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
        
        synchronized (TASKS) {
            this.scope = scope;
            this.callback = callback;
        }
        
        if (scope == null || callback == null) {
            return;
        }
        
        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            enqueue(new Work(file, callback));
        }
        
        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            // Performance: Only do project scanning for GSF-enabled projects! (See issue 141514)
            ClassPathProvider provider = p.getLookup().lookup(ClassPathProvider.class);
            if (provider == null) {
                continue;
            }

            ClassPath cp = provider.findClassPath(p.getProjectDirectory(), ClassPath.SOURCE);
            if (cp != null) {
                for (FileObject root : cp.getRoots()) {
                    enqueue(new Work(root, callback));
                }
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
            if (INSTANCE != null && TASKS.size() == 0 && INSTANCE.callback != null) {
               INSTANCE.callback.started();
            }
            final RequestProcessor.Task task = WORKER.post(w);
            
            TASKS.add(task);
            task.addTaskListener(new TaskListener() {
                public void taskFinished(org.openide.util.Task task) {
                    synchronized (TASKS) {
                        if (!clearing) {
                            TASKS.remove(task);
                            if (INSTANCE != null && TASKS.size() == 0 && INSTANCE.callback != null) {
                               INSTANCE.callback.finished();
                            }
                        }
                    }
                }
            });
            if (task.isFinished()) {
                TASKS.remove(task);
                if (INSTANCE != null && TASKS.size() == 0 && INSTANCE.callback != null) {
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
            if (!file.isValid()) {
                return;
            }
            if (file.isFolder()) {
                if (!VisibilityQuery.getDefault().isVisible(file)) {
                    return;
                }
                final String name = file.getName();
                if (name.equals("vendor")) { // NOI18N
                    if (file.getParent().getFileObject("nbproject") != null) { // NOI18N
                        return;
                    }
                }

                // TODO FIXME The hints providers need to pass in relevant directories
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
                    break;
                }
                //HANZ
//                if (language.getParser() != null) {
//                    applicable = true;
//                    break;
//                }
            }
            if (!applicable) {
                // No point compiling the file if there are no hintsproviders
                return;
            }

            // Make sure we're not dealing with a huge file!
            // Causes issues like 132306
            // openide.loaders/src/org/openide/text/DataEditorSupport.java
            // has an Env#inputStream method which posts a warning to the user
            // if the file is greater than 1Mb...
            //SG_ObjectIsTooBig=The file {1} seems to be too large ({2,choice,0#{2}b|1024#{3} Kb|1100000#{4} Mb|1100000000#{5} Gb}) to safely open. \n\
            //  Opening the file could cause OutOfMemoryError, which would make the IDE unusable. Do you really want to open it?
            // I don't want to try indexing these files... (you get an interactive
            // warning during indexing
            if (file.getSize () > 1024 * 1024) {
                return;
            }
            
            final List<ErrorDescription> result = new ArrayList<ErrorDescription>();
            
            Source source = Source.create(file);
            if (source == null) {
                return;
            }

            final List<Task> tasks = new ArrayList<Task>();

            MultiLanguageUserTask task = new MultiLanguageUserTask () {

                public void run (ResultIterator resultIterator) throws Exception {
                    // Ensure document is forced open
                    GsfUtilities.getDocument(resultIterator.getSnapshot ().getSource ().getFileObject (), true);
                    refreshEmbedding (file, resultIterator, result, tasks);
                }
            };
            
            try {
                ParserManager.parse (Collections.singleton (source), task);
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }

            for (final ErrorDescription hint : result) {
                try {
                    Task tasklistTask = Task.create(file,
                            severityToTaskListString(hint.getSeverity()),
                            hint.getDescription(),
                            hint.getRange().getBegin().getLine()+1);
                    tasks.add(tasklistTask);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }

            callback.setTasks(file, tasks);
        }
    }

    private static void refreshEmbedding (
        FileObject              file, 
        ResultIterator          resultIterator,
        List<ErrorDescription>  result,
        List<Task>              tasks
    ) throws ParseException {
        Snapshot snapshot = resultIterator.getSnapshot ();
        ParserResult parserResult = (ParserResult) resultIterator.getParserResult ();
        refreshEmbedding (file, parserResult, snapshot, result, tasks);
        for (Embedding embedding : resultIterator.getEmbeddings ())
            refreshEmbedding (file, resultIterator.getResultIterator (embedding), result, tasks);
    }
    
    private static void refreshEmbedding (
        FileObject              file, 
        ParserResult            parserResult,
        Snapshot                snapshot,
        List<ErrorDescription>  result,
        List<Task>              tasks
    ) throws ParseException {
        String mimeType = snapshot.getMimeType ();
        final LanguageRegistry registry = LanguageRegistry.getInstance ();
        Language language = registry.getLanguageByMimeType (mimeType);
        HintsProvider provider = language.getHintsProvider();
        List<Error> errors = new ArrayList<Error>();

        if (provider == null) {
            // Just parser errors, no hints
            List<? extends Error> parserErrors = parserResult.getDiagnostics();
            if (parserErrors != null) {
                errors.addAll(parserErrors);
            }
        } else {
            GsfHintsManager manager = language.getHintsManager();
            if (manager == null) {
                return;
            }
            RuleContext ruleContext = manager.createRuleContext(parserResult, language, -1, -1, -1);
            if (ruleContext == null) {
                return;
            }
            final List<Hint> hints = new ArrayList<Hint>();
            provider.computeErrors(manager, ruleContext, hints, errors);
            provider.computeHints(manager, ruleContext, hints);

            if (!file.isValid()) {
                return;
            }
            for (Hint desc : hints) {
                ErrorDescription errorDesc = manager.createDescription(desc, ruleContext, false);
                if (errorDesc != null) {
                    result.add(errorDesc);
                }
            }
        }

        for (Error error : errors) {
            StyledDocument doc = (StyledDocument) snapshot.getSource ().getDocument ();
            if (doc == null) {
                continue;
            }

            int astOffset = error.getStartPosition();
            int lexOffset = snapshot.getOriginalOffset (astOffset);
            if (lexOffset == -1) {
                continue;
            }

            int lineno = NbDocument.findLineNumber(doc, lexOffset) + 1;
            Task task = Task.create(file, 
                    error.getSeverity() == org.netbeans.modules.csl.api.Severity.ERROR ? TASKLIST_ERROR : TASKLIST_WARNING,
                    error.getDisplayName(),
                    lineno);
            tasks.add(task);
        }
    }
    
    private static String severityToTaskListString(Severity severity){
        return (severity == Severity.ERROR) ? TASKLIST_ERROR_HINT : TASKLIST_WARNING_HINT;
    }
}
