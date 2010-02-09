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

import java.util.List;
import org.netbeans.modules.progress.transactional.TransactionManager;

/**
 * A transaction which contains two other transactions, and will run them in
 * parallel.
 *
 * @author Tim Boudreau
 */
final class ParallelTransaction <AArgType, BArgType, AResultType, BResultType> extends Transaction <ParallelValue<AArgType, BArgType>, ParallelValue<AResultType, BResultType>> {
    private final Transaction<AArgType, AResultType> a;
    private final Transaction<BArgType, BResultType> b;
    ParallelTransaction(String name, Transaction<AArgType, AResultType> a, Transaction<BArgType, BResultType> b) {
        super (name == null ? a.getName() == null ? b.getName() : a.getName() : name, ParallelValue.class, ParallelValue.class);
        this.a = a;
        this.b = b;
    }

    Transaction<AArgType, AResultType> a() {
        return a;
    }

    Transaction<BArgType, BResultType> b() {
        return b;
    }

    @Override
    boolean contains(Transaction<?, ?> x) {
        return x == this || x == a || x == b || a.contains(x) || b.contains(x);
    }

    @Override
    protected int size() {
        return a.size();
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    protected int indexOf(Transaction<?,?> x) {
        int result = x == this || x == a || x == b ? 0 : -1;
        if (result < 0) {
            result = a.indexOf(x);
            if (result < 0) {
                result = b.indexOf(x);
            }
        }
        assert (result == -1) == (!contents().contains(x)) : "Result " + result + " but contents contains " + x + " a is a " + a.getClass().getName() + " b is a " + b.getClass().getName();
        return result;
    }

    @Override
    protected void listContents(List<? super Transaction<?,?>> l) {
//        l.add (a);
//        l.add (b);
        a.listContents(l);
        b.listContents(l);
    }

    @Override
    protected ParallelValue<AResultType, BResultType> run(TransactionController controller, ParallelValue<AArgType, BArgType> argument) throws TransactionException {
        //PENDING: allow synchronous execution?
        throw new UnsupportedOperationException("Should never be called"); //NOI18N
    }

    @Override
    protected boolean rollback(TransactionController controller, ParallelValue<AArgType, BArgType> argument, ParallelValue<AResultType, BResultType> prevResult) throws TransactionException {
        //PENDING: allow synchronous rollback?
        throw new UnsupportedOperationException("Should never be called"); //NOI18N
    }

    @Override
    TransactionManager<? extends Transaction<ParallelValue<AArgType, BArgType>, ParallelValue<AResultType, BResultType>>, ParallelValue<AArgType, BArgType>, ParallelValue<AResultType, BResultType>> createRunner() {
        return new ParallelTransactionManager<AArgType, BArgType, AResultType, BResultType>(this);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + a + "," + b +"]";
    }
}
