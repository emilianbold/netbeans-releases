/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.diff;

import java.io.File;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.status.DiffAction", category = "Git")
@ActionRegistration(displayName = "#LBL_DiffAction_Name")
public class DiffAction extends GitAction {

    @Override
    protected void performContextAction (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        diff(context);
    }

    @Override
    protected String iconResource () {
        return "org/netbeans/modules/git/resources/icons/diff.png"; // NOI18N
    }

    public void diff (VCSContext context) {
        String contextName = Utils.getContextDisplayName(context);
        MultiDiffPanelController controller = new MultiDiffPanelController(context);
        DiffTopComponent tc = new DiffTopComponent(controller);
        controller.setActions(tc);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName)); //NOI18N
        tc.open();
        tc.requestActive();
    }

    public void diff (File file, String rev1, String rev2) {
        MultiDiffPanelController controller = new MultiDiffPanelController(file, rev1, rev2);
        DiffTopComponent tc = new DiffTopComponent(controller);
        controller.setActions(tc);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", file.getName())); // NOI18N
        tc.open();
        tc.requestActive();
    }
}
