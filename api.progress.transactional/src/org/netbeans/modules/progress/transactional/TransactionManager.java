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

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.transactional.Transaction;
import org.netbeans.api.progress.transactional.TransactionController;
import org.netbeans.api.progress.transactional.TransactionException;

/**
 * Object which can run a transaction, retains state about the previous run
 * and can roll back the transaction.
 * <p/>
 * Transactions are stateless;  when a transaction is run, it is run by a
 * TransactionRunner which remembers the argument passed and the value returned.
 * <p/>
 * Hold an instance of TransactionRunner to provide undo support for a transaction.
 *
 * @author Tim Boudreau
 */
public abstract class TransactionManager<TransactionType extends Transaction<ArgType, ResultType>, ArgType, ResultType> {
    protected final AtomicBoolean hasRolledBack = new AtomicBoolean(false);
    protected final AtomicBoolean hasRun = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isRollingBack = new AtomicBoolean(false);
    private final TransactionType transaction;

    protected TransactionManager(TransactionType actual) {
        this.transaction = actual;
    }

    protected abstract ResultType run(TransactionController controller, ArgType argument) throws TransactionException;
    protected abstract boolean rollback(TransactionController controller) throws TransactionException;

    public final ResultType doRun(TransactionController controller, ArgType argument) throws TransactionException {
        try {
            if (hasRun()) {
                throw new IllegalStateException ("Already run"); //NOI18N
            }
            isRunning.set(true);
            ControllerAccessor.DEFAULT.checkCancelled(controller);
            ControllerAccessor.DEFAULT.ui(controller).onBeginTransaction(transaction());
            try {
                ResultType res = run(controller, argument);
                hasRun.set(true);
                hasRolledBack.set(false);
                return res;
            } finally {
                controller.checkCancelled();
                ControllerAccessor.DEFAULT.checkCancelled(controller);
                if (!ControllerAccessor.DEFAULT.failed(controller)) {
                    ControllerAccessor.DEFAULT.ui(controller).onEndTransaction(transaction());
                }
            }
        } finally {
            isRunning.set(false);
        }
    }

    public final boolean doRollback(TransactionController controller) throws TransactionException {
        try {
            if (!hasRun()) {
                return true;
            }
            if (hasRolledBack()) {
                throw new IllegalStateException ("Already rolled back"); //NOI18N
            }
            isRollingBack.set(true);
            ControllerAccessor.DEFAULT.enterRollback(controller);
            ControllerAccessor.DEFAULT.ui(controller).onBeginRollback(transaction());
            try {
                rollback(controller);
            } finally {
                if (!ControllerAccessor.DEFAULT.failed(controller)) {
                    ControllerAccessor.DEFAULT.ui(controller).onEndRollback(transaction());
                }
            }
            hasRun.set(false);
            return !ControllerAccessor.DEFAULT.failed(controller);
        } finally {
            isRollingBack.set(false);
        }
    }

    public boolean hasRolledBack() {
        return hasRolledBack.get();
    }

    public boolean hasRun() {
        return hasRun.get();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean isRollingBack() {
        return isRollingBack.get();
    }

    public final TransactionType transaction() {
        return transaction;
    }

    public void reset() {
        if (isRunning() || isRollingBack()) {
            throw new IllegalStateException("Cannot clear while running " + //NOI18N
                    "or rolling back"); //NOI18N
        }
        hasRun.set(false);
        hasRolledBack.set(false);
    }
}
