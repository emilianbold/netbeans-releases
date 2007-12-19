/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.utils;


import org.netbeans.modules.php.rt.providers.impl.actions.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class ActionsDialogs {

    private static final String CONFIRM_REWRITE_TITLE = "LBL_Confirm_Rewrite_Title"; // NOI18N
    private static final String CONFIRM_REWRITE_MESSAGE = "LBL_Confirm_Rewrite_Msg"; // NOI18N
    private static final String CONFIRM_REWRITE_WITH_DIR_MESSAGE = "LBL_Confirm_Rewrite_with_dir_Msg"; // NOI18N

    public static boolean userConfirmOkCancel(String title, String msg) {
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION;
    }

    public static boolean userConfirmYesNo(String title, String msg) {
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION;
    }

    public static boolean userConfirmRewrite(String file, boolean[] dontShowAgain) {
        String title = NbBundle.getMessage(ActionsDialogs.class, CONFIRM_REWRITE_TITLE, file);
        String msg = NbBundle.getMessage(ActionsDialogs.class, CONFIRM_REWRITE_MESSAGE, file);
        return DontShowAgainPanel.showDialog(title, msg, dontShowAgain);
    }

    public static boolean userConfirmRewrite(String file, String dir, boolean[] dontShowAgain) {
        String title = NbBundle.getMessage(ActionsDialogs.class, CONFIRM_REWRITE_TITLE, file);
        String msg = NbBundle.getMessage(ActionsDialogs.class, CONFIRM_REWRITE_WITH_DIR_MESSAGE, file, dir);
        return DontShowAgainPanel.showDialog(title, msg, dontShowAgain);
    }

}
