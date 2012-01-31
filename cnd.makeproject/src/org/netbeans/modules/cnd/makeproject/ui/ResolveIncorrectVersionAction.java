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
package org.netbeans.modules.cnd.makeproject.ui;

import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

final class ResolveIncorrectVersionAction extends AbstractAction {
    
    private MakeLogicalViewRootNode node;
    private Runnable negativeAction;

    ResolveIncorrectVersionAction(MakeLogicalViewRootNode node, Runnable negativeAction) {
        super(NbBundle.getMessage(ResolveIncorrectVersionAction.class, "MSG_version_resolve"), null); //NOI18N
        this.node = node;
        this.negativeAction = negativeAction;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
            String title = NbBundle.getMessage(ResolveIncorrectVersionAction.class, "MSG_version_ignore_title"); //NOI18N
            String message = NbBundle.getMessage(ResolveIncorrectVersionAction.class, "MSG_version_ignore"); //NOI18N
            NotifyDescriptor nd = new NotifyDescriptor(message,
                    title, NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null, NotifyDescriptor.YES_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.YES_OPTION) {
                node.reInitWithUnsupportedVersion();
            } else {
                if (negativeAction != null) {
                    negativeAction.run();
                }
            }
    }    
    
}
