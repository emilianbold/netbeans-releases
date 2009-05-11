/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.jellytools.testutils;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * An extension to NodeUtils which adds support for safe delete dialog.
 *
 * @author Vojtech.Sigler@sun.com
 */
public class JavaNodeUtils extends NodeUtils {

    public static void closeSafeDeleteDialog()
    {
        String safeDeleteTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle",
                "LBL_SafeDel_Delete"); // NOI18N
        new JDialogOperator(safeDeleteTitle).close();
    }

    public static void performSafeDelete(Node node) {
        
        new DeleteAction().performAPI(node);
        // "Safe Delete"
        String safeDeleteTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle",
                "LBL_SafeDel_Delete"); // NOI18N
        // wait for Safe Delete dialog
        NbDialogOperator safeDeleteOper = new NbDialogOperator(safeDeleteTitle);
        try {
            // wait only 5 seconds
            safeDeleteOper.getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 5000);
            safeDeleteOper.ok();
        } catch (TimeoutExpiredException e) {
            // It is "classpath scanning in progress" dialog, wait until it dismiss,
            // and then wait for regular Safe Delete dialog
            safeDeleteOper.waitClosed();
            safeDeleteOper = new NbDialogOperator(safeDeleteTitle);
            safeDeleteOper.ok();
        }
        safeDeleteOper.waitClosed();
    }
}
