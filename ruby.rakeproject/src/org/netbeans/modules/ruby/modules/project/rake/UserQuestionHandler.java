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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.ruby.modules.project.rake;

import java.io.IOException;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/**
 * Deals with {@link UserQuestionException}s.
 * @see "#46089"
 * @author Jesse Glick
 */
public final class UserQuestionHandler {

    private UserQuestionHandler() {}

    /**
     * Handle a user question exception later (in the event thread).
     * Displays a dialog and invokes the appropriate method on the callback.
     * The callback will be notified in the event thread.
     * Use when catching {@link UserQuestionException} during {@link FileObject#lock}.
     */
    public static void handle(final UserQuestionException e, final Callback callback) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(
                    e.getLocalizedMessage(),
                    NbBundle.getMessage(UserQuestionHandler.class, "TITLE_CannotWriteFile"),
                    NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION)) {
                    try {
                        e.confirmed();
                        callback.accepted();
                    } catch (IOException x) {
                        callback.error(x);
                    }
                } else {
                    callback.denied();
                }
            }
        });
    }
    
    /**
     * Intended behavior.
     */
    public interface Callback {
        
        /**
         * Called later if the user accepted the question.
         */
        void accepted();
        
        /**
         * Called later if the user denied the question.
         */
        void denied();
        
        /**
         * Called later if the user accepted the question but there was in fact a problem.
         */
        void error(IOException e);
        
    }
    
}
