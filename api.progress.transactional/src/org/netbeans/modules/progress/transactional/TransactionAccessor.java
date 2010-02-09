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

import java.util.List;
import org.netbeans.api.progress.transactional.Transaction;
import org.netbeans.api.progress.transactional.FailureHandler;
import org.netbeans.api.progress.transactional.TransactionController;
import org.netbeans.api.progress.transactional.TransactionException;
import org.netbeans.spi.progress.transactional.TransactionUI;

/**
 *
 * @author Tim Boudreau
 */
public abstract class TransactionAccessor {
    public static TransactionAccessor DEFAULT;
    public abstract boolean transactionHasRun (TransactionManager<?,?,?> runner);
    public abstract TransactionController createController(FailureHandler handler, UI ui);
    public abstract int transactionSize (Transaction<?,?> t);
    public abstract int indexOf (Transaction<?,?> parent, Transaction<?,?> child);
    public abstract String transactionName(Transaction<?,?> transaction);
    public abstract void cancel(TransactionController ctrller);
    public abstract boolean canCancel (Transaction<?,?> transaction);
    public abstract TransactionUI uiFor (TransactionController controller);
    public abstract boolean isCompoundTransaction (Transaction<?,?> transaction);
    public abstract List<Transaction<?,?>> getContents(Transaction<?,?> transaction);
    public abstract <ArgType, ResultType>TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> createState (Transaction<ArgType,ResultType> xaction);
    public abstract <ArgType, ResultType> ResultType run (TransactionManager<?, ArgType, ResultType> t, TransactionController c, ArgType arg) throws TransactionException;
    public abstract boolean rollback (TransactionManager<?, ?, ?> t, TransactionController c) throws TransactionException;
    public abstract boolean transactionHasRolledBack (TransactionManager<?,?,?> runner);
    public abstract boolean transactionIsRunning (TransactionManager<?,?,?> runner);
    public abstract List<? extends Transaction<?,?>> contents (Transaction<?,?> t);


}
