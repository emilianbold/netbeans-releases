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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.hints.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class SelectionHintsTask extends ParserResultTask<ParserResult> {
    
    private static final Logger LOG = Logger.getLogger(SelectionHintsTask.class.getName());
    private boolean cancelled = false;
    
    public SelectionHintsTask() {
    }
    
    public @Override void run(ParserResult result, SchedulerEvent event) {
        resume();
        
        Document doc = result.getSnapshot().getSource().getDocument();
        if (doc == null) {
            return;
        }

        FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return;
        }

        if (!(event instanceof CursorMovedSchedulerEvent)) {
            return;
        }
        
        CursorMovedSchedulerEvent evt = (CursorMovedSchedulerEvent) event;
        int[] range = new int [] {
            Math.min(evt.getMarkOffset(), evt.getCaretOffset()),
            Math.max(evt.getMarkOffset(), evt.getCaretOffset())
        };
        if (range == null || range.length != 2 || range[0] == -1 || range[1] == -1) {
            return;
        }

        int start = range[0];
        int end = range[1];
        
        Language language = SuggestionsTask.getHintsProviderLanguage(doc, start);
        if (language == null) {
            return;
        }

        HintsProvider provider = language.getHintsProvider();
        assert provider != null; // getHintsProviderLanguage will return null if there's no provider
        GsfHintsManager manager = language.getHintsManager();
        if (manager == null) {
            return;
        }

        List<ErrorDescription> description = new ArrayList<ErrorDescription>();
        List<Hint> hints = new ArrayList<Hint>();
        
        if (start != end) {
            RuleContext ruleContext = manager.createRuleContext(result, language, -1, start, end);
            if (ruleContext != null) {
                provider.computeSelectionHints(manager, ruleContext, hints, Math.min(start,end), Math.max(start,end));
                for (Hint hint : hints) {
                    ErrorDescription desc = manager.createDescription(hint, ruleContext, false);
                    description.add(desc);
                }
            }
        }
        
        if (isCancelled()) {
            return;
        }
        
        HintsController.setErrors(fileObject, SelectionHintsTask.class.getName(), description);
    }

    public @Override int getPriority() {
        return Integer.MAX_VALUE;
    }

    public @Override Class<? extends Scheduler> getSchedulerClass() {
        // XXX: this should be in fact EDITOR_SELECTION_TASK_SCHEDULER, but we dont' have this
        // in Parsing API yet
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    public @Override synchronized void cancel() {
        cancelled = true;
    }

    private synchronized void resume() {
        cancelled = false;
    }

    private synchronized boolean isCancelled() {
        return cancelled;
    }
}
