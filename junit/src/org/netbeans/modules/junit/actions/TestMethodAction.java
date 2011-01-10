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
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.junit.output.OutputUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
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
        if (activatedNodes.length != 1) {
            return false;
        }
        SingleMethod sm = getTestMethod(activatedNodes[0].getLookup());
        if (sm != null){
            ActionProvider ap = OutputUtils.getActionProvider(sm.getFile());
            if (ap != null){
                return Arrays.asList(ap.getSupportedActions()).contains(command) && ap.isActionEnabled(command, Lookups.singleton(sm));
            }
        }
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        SingleMethod sm = getTestMethod(activatedNodes[0].getLookup());
        if (sm != null){
            ActionProvider ap = OutputUtils.getActionProvider(sm.getFile());
            if (ap != null){
                ap.invokeAction(command, Lookups.singleton(sm));
            }
        }
    }

    private SingleMethod getTestMethod(Lookup lkp){
        SingleMethod sm = lkp.lookup(SingleMethod.class);
        if (sm == null){
            EditorCookie ec = lkp.lookup(EditorCookie.class);
            if (ec != null){
                JEditorPane[] panes = ec.getOpenedPanes();
                if (panes.length > 0) {
                    final int cursor = panes[0].getCaret().getDot();
                    JavaSource js = JavaSource.forDocument(panes[0].getDocument());
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

