/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.java.RunOffAWT;
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
        
        RunOffAWT.runOffAWT(r, NbBundle.getMessage(JavaHintsPositionRefresher.class, "Refresh_hints"), context.getCancel()); // NOI18N

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
            int position = ctx.getPosition();

            //TODO: better cancel handling (propagate into tasks?)
            if (ctx.isCanceled()) {
                return;
            }

            //SuggestionsTask
            SuggestionsTask suggestionsTask = new SuggestionsTask();
            suggestionsTask.run(controller);
            eds.put(SuggestionsTask.class.getName(), suggestionsTask.getSuggestions());

            if (ctx.isCanceled()) {
                return;
            }

            //HintsTask
            int rowStart = Utilities.getRowStart((BaseDocument) doc, position);
            int rowEnd = Utilities.getRowEnd((BaseDocument) doc, position);
            Set<ErrorDescription> errs = new TreeSet<ErrorDescription>(new Comparator<ErrorDescription>() {
                public int compare(ErrorDescription arg0, ErrorDescription arg1) {
                    return arg0.toString().equals(arg1.toString()) ? 0 : 1;
                }
            });

            Set<Tree> encounteredLeafs = new HashSet<Tree>();
            HintsTask task = new HintsTask();
            for (int i = rowStart; i <= rowEnd; i++) {
                TreePath path = controller.getTreeUtilities().pathFor(i);
                Tree leaf = path.getLeaf();
                if (!encounteredLeafs.contains(leaf)) {
                    errs.addAll(task.computeHints(controller, path));
                    encounteredLeafs.add(leaf);
                }
            }

            eds.put(HintsTask.class.getName(), new ArrayList<ErrorDescription>(errs));

            if (ctx.isCanceled()) {
                return;
            }

            //ErrorHints
            final List<ErrorDescription> errors = new ErrorHintsProvider().computeErrors(controller, doc, position);
            for (ErrorDescription ed : errors) {
                LazyFixList fixes = ed.getFixes();
                if (fixes instanceof CreatorBasedLazyFixList) { //compute fixes, since they're lazy computed
                    ((CreatorBasedLazyFixList) ed.getFixes()).compute(controller, ctx.getCancel());
                }
            }
            eds.put(ErrorHintsProvider.class.getName(), errors);
        }

    }
}
