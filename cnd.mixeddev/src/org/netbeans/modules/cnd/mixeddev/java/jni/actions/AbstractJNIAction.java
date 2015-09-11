/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.mixeddev.java.jni.actions;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.mixeddev.Triple;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.cnd.mixeddev.java.JavaContextSupport;
import org.netbeans.modules.cnd.mixeddev.java.ResolveJavaEntityTask;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public abstract class AbstractJNIAction extends NodeAction {
    
    public AbstractJNIAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        Triple<DataObject, Document, Integer> context = extractContext(activatedNodes);
        if (context != null) {
            return isEnabledAtPosition(context.second, context.third);
        }
        return false;
    }
    
    protected final Triple<DataObject, Document, Integer> extractContext(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            final Node activeNode = activatedNodes[0];
            final DataObject dobj = activeNode.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                final EditorCookie ec = activeNode.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    JEditorPane pane = Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
                        @Override
                        public JEditorPane run() {
                            return NbDocument.findRecentEditorPane(ec);
                        }
                    });
                    if (pane != null) {
                        return Triple.of(dobj, pane.getDocument(), pane.getCaret().getDot());
                    }
                }
            }
        }
        return null;
    }
    
    protected JavaEntityInfo resolveJavaEntity(Document doc, int caret) {
        return JavaContextSupport.resolveContext(doc, new ResolveJavaEntityTask(caret));
    }
    
    protected abstract boolean isEnabledAtPosition(Document doc, int caret);
}
