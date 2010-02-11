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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.progress.transactional.TransactionHandlerAccessor;
import org.netbeans.modules.progress.transactional.TransactionManager;
import org.netbeans.spi.progress.transactional.TransactionRunner;

/**
 * Transaction manager which runs each of its target transactions on
 * separate threads, with separate UIs, and returns an aggregate result.
 *
 * @author Tim Boudreau
 */
final class ParallelTransactionManager<AArgType, BArgType, AResultType, BResultType> extends TransactionManager<ParallelTransaction<AArgType, BArgType, AResultType, BResultType>, ParallelValue<AArgType, BArgType>, ParallelValue<AResultType, BResultType>> {
    private final TransactionManager<? extends Transaction<AArgType, AResultType>, AArgType, AResultType> aManager;
    private final TransactionManager<? extends Transaction<BArgType, BResultType>,BArgType, BResultType> bManager;

    ParallelTransactionManager(ParallelTransaction<AArgType, BArgType, AResultType, BResultType> t) {
        super (t);
        aManager = t.a().createRunner();
        bManager = t.b().createRunner();
    }

    @Override
    protected ParallelValue<AResultType, BResultType> run(TransactionController controller, ParallelValue<AArgType, BArgType> argument) throws TransactionException {
        TransactionRunner<BArgType, BResultType> runner = TransactionHandlerAccessor.DEFAULT.createRunner(bManager);
        TransactionController bController = controller.cloneWithNewUI();
        Future<BResultType> f  = TransactionHandlerAccessor.DEFAULT.start(runner, bController, argument == null ? null : argument.b(), controller.ui().getMode(), null);
        Exception x = null;
        try {
            AResultType a = null;
//            try {
                a = aManager.doRun(controller, argument == null ? null : argument.a());
//            } catch (TransactionException e) {
//                if (e.isCancellation()) {
//                    f.cancel(true);
//                }
//                throw e;
//            }
            try {
                BResultType b = f.get();
                if (controller.failed() || bController.failed()) {
                    throw new TransactionException ("One thread failed");
                }
                return a == null && b == null ? null : new ParallelValue<AResultType, BResultType> (a, b);
            } catch (InterruptedException ex) {
                throw new TransactionException(ex);
            } catch (ExecutionException ex) {
                TransactionException te = ex.getCause() instanceof TransactionException ? (TransactionException) ex.getCause() : null;
                if (te != null) {
                    throw te;
                } else {
                    throw new IllegalStateException (ex);
                }
            }
        } catch (TransactionException e) {
            x = e;
        } catch (RuntimeException e) {
            x = e;
        } finally {
            if (x != null) {
                //try parallel rollback?  probably not worth it
                runner.rollback(bController);
            }
        }
        return null;
    }

    @Override
    protected boolean rollback(TransactionController controller) throws TransactionException {
        TransactionRunner<BArgType, BResultType> runner = TransactionHandlerAccessor.DEFAULT.createRunner(bManager);
        TransactionController bController = controller.cloneWithNewUI();
        Future<Boolean> f  = TransactionHandlerAccessor.DEFAULT.startRollback(runner, bController, bController.ui().getMode());
        boolean result = aManager.doRollback(controller);
        try {
            return f.get() & result;
        } catch (InterruptedException ex) {
            bController.failed(bManager, ex, null);
        } catch (ExecutionException ex) {
            bController.failed(bManager, ex, null);
        }
        return false;
    }
}
