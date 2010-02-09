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

package org.netbeans.api.progress.transactional;

import org.netbeans.modules.progress.transactional.TransactionManager;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.progress.transactional.TransactionException.NotificationStyle;

/**
 *
 * @author Tim Boudreau
 */
final class SingleTransactionManager<ArgType, ResultType> extends TransactionManager<Transaction<ArgType, ResultType>, ArgType, ResultType> {
    protected final AtomicReference<ArgType> argument = new AtomicReference<ArgType>();
    protected final AtomicReference<ResultType> result = new AtomicReference<ResultType>();
    SingleTransactionManager(Transaction<ArgType, ResultType> actual) {
        super (actual);
    }

    @Override
    protected ResultType run(TransactionController controller, ArgType argument) throws TransactionException {
        this.argument.set(argument);
        try{
            run (transaction(), controller, argument, hasRun, result);
        } catch (Exception e) {
            handleException (e, true, this, controller, true);
        }
        return result.get();
    }

    @Override
    protected boolean rollback(TransactionController controller) throws TransactionException {
        try {
            return rollback (transaction(), controller, argument.get(), result.get(), hasRolledBack);
        } catch (Exception e) {
            handleException (e, true, this, controller, true);
            return false;
        }
    }

    <T, R> boolean rollback(Transaction<T,R> t, TransactionController controller, T arg, R res, AtomicBoolean toSet) throws TransactionException {
        boolean ress = t.rollback(controller, arg, res);
        toSet.set(true);
        return ress;
    }

    <T, R> void run (Transaction<T,R> t, TransactionController controller, T arg, AtomicBoolean toSet, AtomicReference<R> dest) throws TransactionException {
        R res = t.run(controller, arg);
        toSet.set(true);
        dest.set(res);
    }

    static <T,R> void handleException(Exception e, boolean inRollback, TransactionManager<? extends Transaction<T,R>, T,R> t, TransactionController c, boolean rethrow) throws TransactionException {
        boolean shouldThrow = rethrow && inRollback ? c.rollbackFailed(t, e, null) : c.failed(t, e, null);
        if (!inRollback) {
            c.failed(t, e, null);
            c.enterRollback();
            try {
                t.doRollback(c);
            } catch (RuntimeException ex) {
                handleException (ex, true, t, c, false);
            }
        }
        if (shouldThrow) {
            if (e instanceof TransactionException) {
                throw (TransactionException) e;
            } else {
                throw (RuntimeException) e;
            }
        } else {
            throw new TransactionException (e.getLocalizedMessage(), NotificationStyle.NONE);
        }
    }

    @Override
    public void reset() {
        super.reset();
        argument.set(null);
        result.set(null);
    }
}
