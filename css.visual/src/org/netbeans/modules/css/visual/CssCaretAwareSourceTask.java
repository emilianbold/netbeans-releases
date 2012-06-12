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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.css.visual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.*;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author mfukala@netbeans.org
 */
public final class CssCaretAwareSourceTask extends ParserResultTask<CssCslParserResult> {

    private static final String CSS_MIMETYPE = "text/x-css"; //NOI18N
    
    private boolean cancelled;

    @Override
    public int getPriority() {
        return 5000; //low priority
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public void run(final CssCslParserResult result, SchedulerEvent event) {
        cancelled = false;

        if (event == null) {
            return;
        }

        if (!(event instanceof CursorMovedSchedulerEvent)) {
            return;
        }

        final int caretOffset = ((CursorMovedSchedulerEvent) event).getCaretOffset();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateCssPropertiesWindow(result, caretOffset);
            }
            
        });

    }

    private void updateCssPropertiesWindow(final CssCslParserResult result, int documentOffset) {
        if(cancelled) {
            return ;
        }
        
        final RuleEditorTC cssPropertiesTC = (RuleEditorTC) WindowManager.getDefault().findTopComponent(RuleEditorTC.ID);
        if (cssPropertiesTC == null) {
            return;
        }
        
        
        final int astOffset = result.getSnapshot().getEmbeddedOffset(documentOffset);
        if(astOffset == -1) {
            //disable the rule editor
            cssPropertiesTC.setContext(null);
            return ;
        }
        
        Model model = result.getModelV2();
        
        model.runReadTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                final Collection<Rule> rules = new ArrayList<Rule>();
                styleSheet.accept(new ModelVisitor.Adapter() {

                    @Override
                    public void visitRule(Rule rule) {
                        rules.add(rule);
                    }
                });
                
                Rule match = null;
                for (Rule rule : rules) {
                    if (astOffset > rule.getStartOffset() && astOffset < rule.getEndOffset()) {
                        match = rule;
                        break;
                    }
                }
                if(cancelled) {
                    return ;
                }
                
                RuleContext context = match == null ? null : new RuleContext(match, result.getModelV2());
                cssPropertiesTC.setContext(context);
            }
        });

    }
    
    @MimeRegistration(mimeType="text/x-css", service=TaskFactory.class)
    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            String mimeType = snapshot.getMimeType();

            if (mimeType.equals(CSS_MIMETYPE)) { //NOI18N
                return Collections.singletonList(new CssCaretAwareSourceTask());
            } else {
                return Collections.emptyList();
            }
        }
    }

    
}
