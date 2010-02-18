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

package org.netbeans.modules.progress.transactional;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.transactional.FailureHandler;
import org.netbeans.api.progress.transactional.Transaction;
import org.netbeans.api.progress.transactional.TransactionException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service=FailureHandler.class)
public class DefaultFailureHandler extends FailureHandler {

    @Override
    public boolean failed(Transaction<?, ?> xaction, Throwable e, String msg, boolean isRollback) {
        Logger logger = Logger.getLogger(DefaultFailureHandler.class.getName());
        if (e instanceof TransactionException) {
            TransactionException te = (TransactionException) e;
            if (te.isCancellation()) {
                logger.log(Level.FINER, "User cancelled {0}", xaction); //NOI18N
                return true;
            }
            logger.log(Level.FINE, "Transaction failed: {0}", xaction); //NOI18N
            logger.log(Level.FINER, null, e);
            switch (te.getStyle()) {
                case NONE :
                    break;
                case STATUS :
                    StatusDisplayer.getDefault().setStatusText(msg == null ? e.getLocalizedMessage() : msg);
                    break;
                case POPUP :
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg == null ? e.getLocalizedMessage() : msg));
                    break;
                default :
                    throw new AssertionError();

            }
        } else {
            Exceptions.printStackTrace(e);
        }
        return true;
    }

}
