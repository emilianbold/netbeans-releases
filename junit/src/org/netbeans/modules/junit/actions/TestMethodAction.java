/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.junit.actions;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.junit.output.OutputUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;

public abstract class TestMethodAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(TestMethodAction.class.getName());
    private String command;

    protected TestMethodAction(String command){
        this.command = command;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes.length == 1);
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        final Document doc;
        final int caret;

        EditorCookie ec = activatedNodes[0].getLookup().lookup(EditorCookie.class);
        if (ec != null) {
            JEditorPane pane = NbDocument.findRecentEditorPane(ec);
            if (pane != null) {
                doc = pane.getDocument();
                caret = pane.getCaret().getDot();
            } else {
                doc = null;
                caret = -1;
            }
        } else {
            doc = null;
            caret = -1;
        }

        ProgressUtils.runOffEventDispatchThread(new Runnable() {

            @Override
            public void run() {
                SingleMethod sm = getTestMethod(activatedNodes[0].getLookup(), doc, caret);
                if (sm != null) {
                    ActionProvider ap = OutputUtils.getActionProvider(sm.getFile());
                    if (ap != null) {
                        ap.invokeAction(command, Lookups.singleton(sm));
                    }
                }
            }
        },
        getName(), new AtomicBoolean(), false);
    }

    
    private SingleMethod getTestMethod(Lookup lkp, Document doc, int cursor){
        SingleMethod sm = lkp.lookup(SingleMethod.class);
        if (sm == null && doc != null){
            JavaSource js = JavaSource.forDocument(doc);
            TestClassInfoTask task = new TestClassInfoTask(cursor);
            try {
                Future<Void> f = js.runWhenScanFinished(task, true);
                if (f.isDone() && task.getFileObject() != null && task.getMethodName() != null){
                    sm = new SingleMethod(task.getFileObject(), task.getMethodName());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return sm;
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

