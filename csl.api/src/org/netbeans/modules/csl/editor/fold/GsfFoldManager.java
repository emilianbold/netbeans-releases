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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.csl.editor.fold;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.*;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * 
 * Copied from both JavaFoldManager and JavaElementFoldManager
 * 
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 */
public class GsfFoldManager implements FoldManager {

    private static final Logger LOG = Logger.getLogger(GsfFoldManager.class.getName());
    
    public static final FoldType CODE_BLOCK_FOLD_TYPE = new FoldType("code-block"); // NOI18N
    public static final FoldType INITIAL_COMMENT_FOLD_TYPE = new FoldType("initial-comment"); // NOI18N
    public static final FoldType IMPORTS_FOLD_TYPE = new FoldType("imports"); // NOI18N
    public static final FoldType JAVADOC_FOLD_TYPE = new FoldType("javadoc"); // NOI18N
    public static final FoldType TAG_FOLD_TYPE = new FoldType("tag"); // NOI18N
    public static final FoldType INNER_CLASS_FOLD_TYPE = new FoldType("inner-class"); // NOI18N

    
    private static final String IMPORTS_FOLD_DESCRIPTION = "..."; // NOI18N

    private static final String COMMENT_FOLD_DESCRIPTION = "..."; // NOI18N

    private static final String JAVADOC_FOLD_DESCRIPTION = "..."; // NOI18N
    
    private static final String CODE_BLOCK_FOLD_DESCRIPTION = "{...}"; // NOI18N

    private static final String TAG_FOLD_DESCRIPTION = "<.../>"; // NOI18N


    public static final FoldTemplate CODE_BLOCK_FOLD_TEMPLATE
        = new FoldTemplate(CODE_BLOCK_FOLD_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 1, 1);
    
    public static final FoldTemplate INITIAL_COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(INITIAL_COMMENT_FOLD_TYPE, COMMENT_FOLD_DESCRIPTION, 2, 2);

    public static final FoldTemplate IMPORTS_FOLD_TEMPLATE
        = new FoldTemplate(IMPORTS_FOLD_TYPE, IMPORTS_FOLD_DESCRIPTION, 0, 0);

    public static final FoldTemplate JAVADOC_FOLD_TEMPLATE
        = new FoldTemplate(JAVADOC_FOLD_TYPE, JAVADOC_FOLD_DESCRIPTION, 3, 2);

    public static final FoldTemplate TAG_FOLD_TEMPLATE
        = new FoldTemplate(TAG_FOLD_TYPE, TAG_FOLD_DESCRIPTION, 0, 0);

    public static final FoldTemplate INNER_CLASS_FOLD_TEMPLATE
        = new FoldTemplate(INNER_CLASS_FOLD_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 0, 0);

    
    public static final String CODE_FOLDING_ENABLE = "code-folding-enable"; //NOI18N
    
    /** Collapse methods by default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_METHOD = "code-folding-collapse-method"; //NOI18N
    
    /**
     * Collapse inner classes by default 
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_INNERCLASS = "code-folding-collapse-innerclass"; //NOI18N
    
    /**
     * Collapse import section default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_IMPORT = "code-folding-collapse-import"; //NOI18N
    
    /**
     * Collapse javadoc comment by default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_JAVADOC = "code-folding-collapse-javadoc"; //NOI18N

    /**
     * Collapse initial comment by default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_INITIAL_COMMENT = "code-folding-collapse-initial-comment"; //NOI18N
    
     /**
     * Collapse tags by default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_TAGS = "code-folding-collapse-tags"; //NOI18N

    
    protected static final class FoldTemplate {

        private FoldType type;

        private String description;

        private int startGuardedLength;

        private int endGuardedLength;

        protected FoldTemplate(FoldType type, String description,
        int startGuardedLength, int endGuardedLength) {
            this.type = type;
            this.description = description;
            this.startGuardedLength = startGuardedLength;
            this.endGuardedLength = endGuardedLength;
        }

        public FoldType getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public int getStartGuardedLength() {
            return startGuardedLength;
        }

        public int getEndGuardedLength() {
            return endGuardedLength;
        }

    }
    
    private FoldOperation operation;
    private FileObject    file;
    private JavaElementFoldTask task;
    
//    // Folding presets
//    private boolean foldImportsPreset;
//    private boolean foldInnerClassesPreset;
//    private boolean foldJavadocsPreset;
//    private boolean foldCodeBlocksPreset;
//    private boolean foldInitialCommentsPreset;
    private static volatile Preferences prefs;
    
    /** Creates a new instance of GsfFoldManager */
    public GsfFoldManager() {
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
        
        String mimeType = DocumentUtilities.getMimeType(operation.getHierarchy().getComponent());
        if (prefs == null) {
            prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        }
    }

    @Override
    public synchronized void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        file = DataLoadersBridge.getDefault().getFileObject(doc);
        
        if (file != null) {
            currentFolds = new HashMap<FoldInfo, Fold>();
            task = JavaElementFoldTask.getTask(file);
            task.setGsfFoldManager(GsfFoldManager.this, file);
        }
    }
    
    @Override
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeEmptyNotify(Fold emptyFold) {
        removeDamagedNotify(emptyFold);
    }

    @Override
    public void removeDamagedNotify(Fold damagedFold) {
        currentFolds.remove(operation.getExtraInfo(damagedFold));
        if (importsFold == damagedFold) {
            importsFold = null;//not sure if this is correct...
        }
        if (initialCommentFold == damagedFold) {
            initialCommentFold = null;//not sure if this is correct...
        }
    }

    public void expandNotify(Fold expandedFold) {
    }

    public synchronized void release() {
        if (task != null) {
            task.setGsfFoldManager(this, null);
        }
        
        task         = null;
        file         = null;
        currentFolds = null;
        importsFold  = null;
        initialCommentFold = null;
    }
    
//    public void settingsChange(SettingsChangeEvent evt) {
//        // Get folding presets
//        foldInitialCommentsPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT);
//        foldImportsPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_IMPORT);
//        foldCodeBlocksPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_METHOD);
//        foldInnerClassesPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_INNERCLASS);
//        foldJavadocsPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_JAVADOC);
//    }
    
    private static boolean getSetting(String settingName) {
        return prefs.getBoolean(settingName, false);
    }
    
    static final class JavaElementFoldTask extends IndexingAwareParserResultTask<ParserResult> {

        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        
        public JavaElementFoldTask() {
            super(TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        //XXX: this will hold JavaElementFoldTask as long as the FileObject exists:
        private static Map<FileObject, JavaElementFoldTask> file2Task = new WeakHashMap<FileObject, JavaElementFoldTask>();

        static JavaElementFoldTask getTask(FileObject file) {
            JavaElementFoldTask task = file2Task.get(file);

            if (task == null) {
                file2Task.put(file, task = new JavaElementFoldTask());
            }

            return task;
        }
        
        private Collection<Reference<GsfFoldManager>> managers = new ArrayList<Reference<GsfFoldManager>>(2);
        
        synchronized void setGsfFoldManager(GsfFoldManager manager, FileObject file) {
            if (file == null) {
                for (Iterator<Reference<GsfFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                    Reference<GsfFoldManager> ref = it.next();
                    GsfFoldManager fm = ref.get();
                    if (fm == null || fm == manager) {
                        it.remove();
                        break;
                    }
                }
            } else {
                managers.add(new WeakReference<GsfFoldManager>(manager));
                GsfFoldScheduler.reschedule();
            }
        }
        
        private synchronized Object findLiveManagers() {
            GsfFoldManager oneMgr = null;
            List<GsfFoldManager> result = null;
            
            for (Iterator<Reference<GsfFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                Reference<GsfFoldManager> ref = it.next();
                GsfFoldManager fm = ref.get();
                if (fm == null) {
                    it.remove();
                    continue;
                }
                if (result != null) {
                    result.add(fm);
                } else if (oneMgr != null) {
                    result = new ArrayList<GsfFoldManager>(2);
                    result.add(oneMgr);
                    result.add(fm);
                } else {
                    oneMgr = fm;
                }
            }
            return result != null ? result : oneMgr;
        }
        
        
        public void run(final ParserResult info, SchedulerEvent event) {
            cancelled.set(false);
            
            final Object mgrs = findLiveManagers();
            
            if (mgrs == null) {
                return;
            }
            
            long startTime = System.currentTimeMillis();

            // Don't update folds, if there is an invalid result
            // It should be solved per lenguages, but then there has to be remembered
            // lates folds and transformed to the new possition.
            if (hasErrors(info)) {
                return;
            }

            final TreeSet<FoldInfo> folds = new TreeSet<FoldInfo>();
            Document doc = info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return;
            }
            boolean success = gsfFoldScan(doc, info, folds);
            if (!success || cancelled.get()) {
                return;
            }
            
            if (mgrs instanceof GsfFoldManager) {
                SwingUtilities.invokeLater(((GsfFoldManager)mgrs).new CommitFolds(folds));
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    Collection<GsfFoldManager> jefms = (Collection<GsfFoldManager>)mgrs;
                    public void run() {
                        for (GsfFoldManager jefm : jefms) {
                            jefm.new CommitFolds(folds).run();
                        }
                }});
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 1", //NOI18N
                    new Object[] {info.getSnapshot().getSource().getFileObject(), endTime - startTime});
        }
        
        /**
         * Ask the language plugin to scan for folds. 
         * 
         * @return true If folds were found, false if cancelled
         */
        private boolean gsfFoldScan(final Document doc, ParserResult info, final TreeSet<FoldInfo> folds) {
            final boolean [] success = new boolean [] { false };
            Source source = info.getSnapshot().getSource();

            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    public @Override void run(ResultIterator resultIterator) throws Exception {
                        String mimeType = resultIterator.getSnapshot().getMimeType();
                        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
                        if (language == null) {
                            return;
                        }

                        StructureScanner scanner = language.getStructure();
                        if (scanner == null) {
                            return;
                        }

                        Parser.Result r = resultIterator.getParserResult();
                        if (!(r instanceof ParserResult)) {
                            return;
                        }

                        scan((ParserResult) r, folds, doc, scanner);

                        if (cancelled.get()) {
                            return;
                        }

                        for(Embedding e : resultIterator.getEmbeddings()) {
                            run(resultIterator.getResultIterator(e));

                            if (cancelled.get()) {
                                return;
                            }
                        }

                        success[0] = true;
                    }
                });
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }

            if (success[0]) {
                //check for initial fold:
                success[0] = checkInitialFold(doc, folds);
            }

            return success[0];
        }

        private boolean checkInitialFold(final Document doc, final TreeSet<FoldInfo> folds) {
            final boolean[] ret = new boolean[1];
            ret[0] = true;
            final TokenHierarchy<?> th = TokenHierarchy.get(doc);
            if (th == null) {
                return false;
            }
            doc.render(new Runnable() {
                @Override
                public void run() {
                    try {
                        TokenSequence<?> ts = th.tokenSequence();
                        if (ts == null) {
                            return;
                        }
                        while (ts.moveNext()) {
                            Token<?> token = ts.token();
                            String category = token.id().primaryCategory();
                            if ("comment".equals(category)) { // NOI18N
                                int startOffset = ts.offset();
                                int endOffset = startOffset + token.length();
                                boolean collapsed = getSetting(CODE_FOLDING_COLLAPSE_INITIAL_COMMENT); //foldInitialCommentsPreset;


                                // Find end - could be a block of single-line statements

                                while (ts.moveNext()) {
                                    token = ts.token();
                                    category = token.id().primaryCategory();
                                    if ("comment".equals(category)) { // NOI18N
                                        endOffset = ts.offset() + token.length();
                                    } else if (!"whitespace".equals(category)) { // NOI18N
                                        break;
                                    }
                                }

                                try {
                                    // Start the fold at the END of the line
                                    startOffset = org.netbeans.editor.Utilities.getRowEnd((BaseDocument) doc, startOffset);
                                    if (startOffset >= endOffset) {
                                        return;
                                    }
                                } catch (BadLocationException ex) {
                                    LOG.log(Level.WARNING, null, ex);
                                }

                                folds.add(new FoldInfo(doc, startOffset, endOffset, INITIAL_COMMENT_FOLD_TEMPLATE, collapsed));
                                return;
                            }
                            if (!"whitespace".equals(category)) { // NOI18N
                                break;
                            }
                            
                        }
                    } catch (BadLocationException e) {
                        ret[0] = false;
                    }
                }
            });
            return ret[0];
        }
        
        private void scan(final ParserResult info,
            final TreeSet<FoldInfo> folds, final Document doc, final
            StructureScanner scanner) {
            
            doc.render(new Runnable() {
                @Override
                public void run() {
                    addTree(folds, info, doc, scanner);
                }
            });
        }
        
        private void addFoldsOfType(
                    StructureScanner scanner,
                    String type, Map<String,List<OffsetRange>> folds,
                    TreeSet<FoldInfo> result,
                    Document doc, 
                    String collapsedOptionName,
                    FoldTemplate template) {
            
            List<OffsetRange> ranges = folds.get(type); //NOI18N
            if (ranges != null) {
                boolean collapseByDefault = getSetting(collapsedOptionName);
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Creating folds {0}, collapsed: {1}", new Object[] {
                        type, collapseByDefault
                    });
                }
                for (OffsetRange range : ranges) {
                    try {
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, "Fold: {0}", range);
                        }
                        addFold(range, result, doc, collapseByDefault, template);
                    } catch (BadLocationException ble) {
                        LOG.log(Level.WARNING, "StructureScanner " + scanner + " supplied invalid fold " + range, ble); //NOI18N
                    }
                }
            } else {
                LOG.log(Level.FINEST, "No folds of type {0}", type);
            }
        }
        
        private void addTree(TreeSet<FoldInfo> result, ParserResult info, Document doc, StructureScanner scanner) {
            // #217322, disabled folding -> no folds will be created
            if (!getSetting(CODE_FOLDING_ENABLE)) {
                return;
            }
            Map<String,List<OffsetRange>> folds = scanner.folds(info);
            if (cancelled.get()) {
                return;
            }
            addFoldsOfType(scanner, "codeblocks", folds, result, doc, 
                    CODE_FOLDING_COLLAPSE_METHOD, CODE_BLOCK_FOLD_TEMPLATE);
            addFoldsOfType(scanner, "comments", folds, result, doc, 
                    CODE_FOLDING_COLLAPSE_JAVADOC, JAVADOC_FOLD_TEMPLATE);
            addFoldsOfType(scanner, "initial-comment", folds, result, doc, 
                    CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, INITIAL_COMMENT_FOLD_TEMPLATE);
            addFoldsOfType(scanner, "imports", folds, result, doc, 
                    CODE_FOLDING_COLLAPSE_IMPORT, IMPORTS_FOLD_TEMPLATE);
            addFoldsOfType(scanner, "tags", folds, result, doc, 
                    CODE_FOLDING_COLLAPSE_TAGS, TAG_FOLD_TEMPLATE);
            addFoldsOfType(scanner, "othercodeblocks", folds, result, doc, 
                    CODE_FOLDING_COLLAPSE_TAGS, CODE_BLOCK_FOLD_TEMPLATE);
            addFoldsOfType(scanner, "inner-classes", folds, result, doc, 
                    CODE_FOLDING_COLLAPSE_INNERCLASS, INNER_CLASS_FOLD_TEMPLATE);
        }
        
        private void addFold(OffsetRange range, TreeSet<FoldInfo> folds, Document doc, boolean collapseByDefault, FoldTemplate template) throws BadLocationException {
            if (range != OffsetRange.NONE) {
                int start = range.getStart();
                int end = range.getEnd();
                if (start != (-1) && end != (-1) && end <= doc.getLength()) {
                    folds.add(new FoldInfo(doc, start, end, template, collapseByDefault));
                }
            }
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return GsfFoldScheduler.class;
        }

        @Override
        public void cancel() {
            cancelled.set(true);
        }

    }
    
    private class CommitFolds implements Runnable {
        
        private boolean insideRender;
        private TreeSet<FoldInfo> infos;
        private long startTime;
        
        public CommitFolds(TreeSet<FoldInfo> infos) {
            this.infos = infos;
        }
        
        /**
         * For singular folds, if they exist in the FoldManager already
         * ignores the default state, and takes it from the actual state of
         * existing fold.
         */
        private boolean mergeSpecialFoldState(FoldInfo fi) {
            if (fi.template == IMPORTS_FOLD_TEMPLATE) {
                if (importsFold != null) {
                    return importsFold.isCollapsed();
                }
            } else if (fi.template == INITIAL_COMMENT_FOLD_TEMPLATE) {
                if (initialCommentFold != null) {
                    return initialCommentFold.isCollapsed();
                }
            }
            return fi.collapseByDefault;
        }

        public void run() {
            Document document = operation.getHierarchy().getComponent().getDocument();
            if (!insideRender) {
                startTime = System.currentTimeMillis();
                insideRender = true;
                document.render(this);
                
                return;
            }
            
            operation.getHierarchy().lock();
            
            try {
                FoldHierarchyTransaction tr = operation.openTransaction();
                
                try {
                    if (currentFolds == null) {
                        return;
                    }
                    
                    Map<FoldInfo, Fold> added   = new TreeMap<FoldInfo, Fold>();
                    // TODO - use map duplication here instead?
                    TreeSet<FoldInfo> removed = new TreeSet<FoldInfo>(currentFolds.keySet());
                    int documentLength = document.getLength();
                    
                    for (FoldInfo i : infos) {
                        if (removed.remove(i)) {
                            continue ;
                        }
                        
                        int start = i.start.getOffset();
                        int end   = i.end.getOffset();
                        
                        if (end > documentLength) {
                            continue;
                        }
                        
                        if (end > start && (end - start) > (i.template.getStartGuardedLength() + i.template.getEndGuardedLength())) {
                            Fold f    = operation.addToHierarchy(i.template.getType(),
                                                                 i.template.getDescription(),
                                                                 mergeSpecialFoldState(i),
                                                                 start,
                                                                 end,
                                                                 i.template.getStartGuardedLength(),
                                                                 i.template.getEndGuardedLength(),
                                                                 i,
                                                                 tr);
                            
                            added.put(i, f);
                            
                            if (i.template == IMPORTS_FOLD_TEMPLATE) {
                                importsFold = f;
                            }
                            if (i.template == INITIAL_COMMENT_FOLD_TEMPLATE) {
                                initialCommentFold = f;
                            }
                        }
                    }
                    
                    for (FoldInfo i : removed) {
                        Fold f = currentFolds.remove(i);
                        
                        operation.removeFromHierarchy(f, tr);
                        
                        if (importsFold == f ) {
                            importsFold = null;
                        }
                        
                        if (initialCommentFold == f) {
                            initialCommentFold = f;
                        }
                    }
                    
                    currentFolds.putAll(added);
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, null, e);
                } finally {
                    tr.commit();
                }
            } finally {
                operation.getHierarchy().unlock();
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 2",
                    new Object[] {file, endTime - startTime});
        }
    }
    
    private Map<FoldInfo, Fold> currentFolds;
    private Fold initialCommentFold;
    private Fold importsFold;
    
    protected static final class FoldInfo implements Comparable {
        
        private Position start;
        private Position end;
        private FoldTemplate template;
        private boolean collapseByDefault;
        
        public FoldInfo(Document doc, int start, int end, FoldTemplate template, boolean collapseByDefault) throws BadLocationException {
            this.start = doc.createPosition(start);
            this.end   = doc.createPosition(end);
            this.template = template;
            this.collapseByDefault = collapseByDefault;
        }
        
        @Override
        public int hashCode() {
            return 1;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FoldInfo)) {
                return false;
            }
            
            return compareTo(o) == 0;
        }
        
        public int compareTo(Object o) {
            FoldInfo remote = (FoldInfo) o;
            
            if (start.getOffset() < remote.start.getOffset()) {
                return -1;
            }
            
            if (start.getOffset() > remote.start.getOffset()) {
                return 1;
            }
            
            if (end.getOffset() < remote.end.getOffset()) {
                return -1;
            }
            
            if (end.getOffset() > remote.end.getOffset()) {
                return 1;
            }
            
            return 0;
        }
    }

    private static boolean hasErrors(ParserResult r) {
        for(org.netbeans.modules.csl.api.Error e : r.getDiagnostics()) {
            if (e.getSeverity() == Severity.FATAL) {
                return true;
            }
        }
        return false;
    }
}
