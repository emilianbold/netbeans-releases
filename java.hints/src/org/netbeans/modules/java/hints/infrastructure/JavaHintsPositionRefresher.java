/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.java.hints.jackpot.impl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.jackpot.impl.hints.HintsTask;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.PositionRefresher;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Refreshes all Java Hints on current line upon Alt-Enter or mouseclick
 * @author Max Sauer
 */
public class JavaHintsPositionRefresher implements PositionRefresher {

    private static final Logger LOG = Logger.getLogger(JavaHintsPositionRefresher.class.getName());

    @Override
    public Map<String, List<ErrorDescription>> getErrorDescriptionsAt(final Context context, final Document doc) {
        final JavaSource js = JavaSource.forDocument(doc);
        final Map<String, List<ErrorDescription>> eds = new HashMap<String, List<ErrorDescription>>();

        Runnable r = new Runnable() {

            public void run() {
                try {
                    js.runUserActionTask(new RefreshTask(eds, context, doc), true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        
        ProgressUtils.runOffEventDispatchThread(r, NbBundle.getMessage(JavaHintsPositionRefresher.class, "Refresh_hints"), context.getCancel(), false); // NOI18N

        return eds;
    }


    private class RefreshTask implements Task<CompilationController> {

        private final Map<String, List<ErrorDescription>> eds;
        private final Context ctx;
        private final Document doc;

        public RefreshTask( Map<String, List<ErrorDescription>> eds, Context ctx, Document doc) {
            this.eds = eds;
            this.ctx = ctx;
            this.doc = doc;
        }

        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.RESOLVED);

            Document doc = controller.getDocument();

            if (doc == null) {
                return;
            }
            
            long version = DocumentUtilities.getDocumentVersion(doc);
            int position = ctx.getPosition();

            //TODO: better cancel handling (propagate into tasks?)
            if (ctx.isCanceled()) {
                return;
            }

            LastUpdatedHolder holder = getHolder(doc);

            //SuggestionsTask
            if ((version > 0 && holder.suggestions < version) || holder.suggestionsCaret != position) {
                LOG.fine("Computing suggestions");
                eds.put(HintsTask.KEY_SUGGESTIONS, new HintsInvoker(controller, position, new AtomicBoolean()).computeHints(controller));
            } else {
                LOG.fine("Suggestions already computed");
            }

            if (ctx.isCanceled()) {
                return;
            }

            //HintsTask
            if (version > 0 && holder.hints < version) {
                LOG.fine("Computing hints");
                
                int rowStart = Utilities.getRowStart((BaseDocument) doc, position);
                int rowEnd = Utilities.getRowEnd((BaseDocument) doc, position);

                eds.put(HintsTask.KEY_HINTS, new HintsInvoker(controller, rowStart, rowEnd, new AtomicBoolean()).computeHints(controller));
            } else {
                LOG.fine("Hints already computed");
            }

            if (ctx.isCanceled()) {
                return;
            }

            //ErrorHints
            if (version > 0 && holder.errors < version) {
                LOG.fine("Computing errors");

                final List<ErrorDescription> errors = new ErrorHintsProvider().computeErrors(controller, doc, position, org.netbeans.modules.java.hints.errors.Utilities.JAVA_MIME_TYPE);
                for (ErrorDescription ed : errors) {
                    LazyFixList fixes = ed.getFixes();
                    if (fixes instanceof CreatorBasedLazyFixList) { //compute fixes, since they're lazy computed
                        ((CreatorBasedLazyFixList) ed.getFixes()).compute(controller, ctx.getCancel());
                    }
                }
                eds.put(ErrorHintsProvider.class.getName(), errors);
            } else {
                LOG.fine("Errors already computed, computing fixes");
                
                for (ErrorDescription ed : holder.errorsContent) {
                    if (ed.getRange().getBegin().getOffset() <= position && position <= ed.getRange().getEnd().getOffset()) {
                        if (!ed.getFixes().isComputed()) {
                            ((CreatorBasedLazyFixList) ed.getFixes()).compute(controller, ctx.getCancel());
                        }
                    }
                }
            }
        }
        
    }

    public static void hintsUpdated(Document doc, long version) {
        if (doc == null) return;
        getHolder(doc).hints = version;
    }

    public static void suggestionsUpdated(Document doc, long version, int caret) {
        if (doc == null) return;
        getHolder(doc).suggestions = version;
        getHolder(doc).suggestionsCaret = caret;
    }

    public static void errorsUpdated(Document doc, long version, List<ErrorDescription> errors) {
        if (doc == null) return;
        getHolder(doc).errors = version;
        getHolder(doc).errorsContent = errors;
    }

    private static LastUpdatedHolder getHolder(Document doc) {
        LastUpdatedHolder holder = (LastUpdatedHolder) doc.getProperty(LastUpdatedHolder.class);

        if (holder == null) {
            doc.putProperty(LastUpdatedHolder.class, holder = new LastUpdatedHolder());
        }

        return holder;
    }

    private static final class LastUpdatedHolder {
        private long suggestions;
        private int suggestionsCaret;
        private long hints;
        private long errors;
        private List<ErrorDescription> errorsContent;
    }
}
