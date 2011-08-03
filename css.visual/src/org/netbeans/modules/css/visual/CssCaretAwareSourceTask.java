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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.visual.ui.preview.CssTCController;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 * 
 * @author mfukala@netbeans.org
 */
public final class CssCaretAwareSourceTask extends ParserResultTask<CssCslParserResult> {

    //static, will hold the singleton reference forever but I cannot reasonably
    //hook to gsf to be able to free this once last css component closes
    private static CssTCController windowController;

    private static final String CSS_MIMETYPE = "text/x-css"; //NOI18N
    
    private boolean cancelled;

    private static synchronized void initializeWindowController() {
        if(windowController == null) {
            windowController = CssTCController.getDefault();
        }
    }

    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            initializeWindowController();

            String mimeType = snapshot.getMimeType();
            String sourceMimeType = snapshot.getSource().getMimeType();

            //allow to run only on .css files
            if(sourceMimeType.equals(CSS_MIMETYPE) && mimeType.equals(CSS_MIMETYPE)) { //NOI18N
                return Collections.singletonList(new CssCaretAwareSourceTask());
            } else {
                return Collections.emptyList();
            }
        }
    }

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
    public void run(CssCslParserResult result, SchedulerEvent event) {
        cancelled = false;
        
        if(event == null) {
            return ;
        }

        if(!(event instanceof CursorMovedSchedulerEvent)) {
            return ;
        }

        int caretOffset = ((CursorMovedSchedulerEvent)event).getCaretOffset();

        Node root = result.getParseTree();
        if(root != null) {
            //find the rule scope and check if there is an error inside it
            Node leaf = NodeUtil.findNodeAtOffset(root, caretOffset);
            if(leaf != null) {
                Node ruleNode = leaf.type() == NodeType.ruleSet ?
                    leaf :
                    NodeUtil.getAncestorByType(leaf, NodeType.ruleSet);
                if(ruleNode != null) {
                    //filter out warnings
                    List<? extends Error> errors = result.getDiagnostics();
                    for(Error e : errors) {

                        if(e.getSeverity() == Severity.ERROR) {
                            if(ruleNode.from() <= e.getStartPosition() &&
                                    ruleNode.to() >= e.getEndPosition()) {
                                //there is an error in the selected rule
                                CssEditorSupport.getDefault().parsedWithError(result);
                                return ;
                            }
                        }
                    }
                    
                    if(cancelled) {
                        return ;
                    }

                    //no errors found in the node
                    CssEditorSupport.getDefault().parsed(result, ((CursorMovedSchedulerEvent)event).getCaretOffset());
                    return ;
                }
            }
        }
        
        if(cancelled) {
            return ;
        }

        //out of rule, lets notify the editor support anyway
        CssEditorSupport.getDefault().parsed(result, ((CursorMovedSchedulerEvent)event).getCaretOffset());

    }
}

