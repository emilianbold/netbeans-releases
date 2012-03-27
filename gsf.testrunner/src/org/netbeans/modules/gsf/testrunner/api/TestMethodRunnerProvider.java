/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.testrunner.api;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author theofanis
 */
public abstract class TestMethodRunnerProvider {

    private final String command = SingleMethod.COMMAND_RUN_SINGLE_METHOD;

    public abstract boolean canHandle(Node activatedNode);

    public abstract SingleMethod getTestMethod(Document doc, int caret);

    public final void runTestMethod(Node activatedNode) {
        final Node activeNode = activatedNode;
        final Document doc;
        final int caret;

        EditorCookie ec = activeNode.getLookup().lookup(EditorCookie.class);
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
                SingleMethod sm = activeNode.getLookup().lookup(SingleMethod.class);
                if (sm == null) {
                    sm = getTestMethod(doc, caret);
                }
                if (sm != null) {
                    ActionProvider ap = getActionProvider(sm.getFile());
                    if (ap != null) {
                        if (Arrays.asList(ap.getSupportedActions()).contains(command) && ap.isActionEnabled(command, Lookups.singleton(sm))) {
                            ap.invokeAction(command, Lookups.singleton(sm));
                        }
                    }
                }
            }
        },
        NbBundle.getMessage(TestMethodRunnerProvider.class, "LBL_Action_RunTestMethod"), new AtomicBoolean(), false);
    }

    static ActionProvider getActionProvider(FileObject fileObject) {
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if (owner == null) { // #183586
            return null;
        }
        return owner.getLookup().lookup(ActionProvider.class);
    }
}
