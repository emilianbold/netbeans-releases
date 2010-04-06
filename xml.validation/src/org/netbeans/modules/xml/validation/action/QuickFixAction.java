/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.xml.validation.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openide.nodes.Node;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.xml.validation.core.QuickFix;
import org.netbeans.modules.xml.validation.core.QuickFixable;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.12.03
 */
public final class QuickFixAction extends IconAction {

    public QuickFixAction() {
        super(i18n(QuickFixAction.class, "CTL_Quick_Fix_Action"), i18n(QuickFixAction.class, "TLT_Quick_Fix_Action"), icon(QuickFixAction.class, "quickfix")); // NOI18N
    }

    public void actionPerformed(ActionEvent event) {
        InputOutput io = IOProvider.getDefault().getIO(i18n(QuickFixAction.class, "LBL_Quick_Fix_Window"), false); // NOI18N
        OutputWriter out = io.getOut();

        try {
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        io.select();

        out.println(i18n(QuickFixAction.class, "MSG_Quick_Fix_started")); // NOI18N
        doQuickFix(getQuickFixes(getSelectedNode()), out);
        out.println();
        out.print(i18n(QuickFixAction.class, "MSG_Quick_Fix_finished")); // NOI18N
    }

    private void doQuickFix(List<QuickFix> quickFixes, OutputWriter out) {
        if (quickFixes.size() == 0) {
            out.println();
            out.println(i18n(QuickFixAction.class, "MSG_Nothing_to_do")); // NOI18N
            return;
        }
        for (QuickFix quickFix : quickFixes) {
            String description = quickFix.doFix();

            if (description != null) {
                out.println();
                out.println(i18n(QuickFixAction.class, "MSG_Quick_Fix", description)); // NOI18N
            }
        }
    }

    private List<QuickFix> getQuickFixes(Node node) {
        List<QuickFix> quickFixes = new ArrayList<QuickFix>();

        if (node == null) {
            return quickFixes;
        }
//out();
//out("NODE: " + node);
        Controller controller = node.getLookup().lookup(Controller.class);
//out("CONTROLLER: " + controller);

        if (controller == null) {
//out("CONTROLLER is NULL");
            return quickFixes;
        }
        List<ResultItem> result = controller.getResult();

        for (ResultItem item : result) {
            if (!(item instanceof QuickFixable)) {
                continue;
            }
            QuickFix quickFix = ((QuickFixable) item).getQuickFix();

            if (quickFix != null) {
                quickFixes.add(quickFix);
            }
        }
        return quickFixes;
    }
}
