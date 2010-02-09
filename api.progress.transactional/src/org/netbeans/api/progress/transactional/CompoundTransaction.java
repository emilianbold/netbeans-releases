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

import java.util.Collections;
import java.util.HashMap;
import org.netbeans.modules.progress.transactional.TransactionManager;
import java.util.List;
import java.util.Map;
import org.openide.util.Parameters;

/**
 *
 * @author Tim Boudreau
 */
final class CompoundTransaction<ArgType, InterimArgType, ResultType> extends Transaction<ArgType, ResultType>  {
    private final Transaction<ArgType, InterimArgType> prev;
    private final Transaction<InterimArgType, ResultType> next;

    CompoundTransaction(Transaction<ArgType, InterimArgType> prev, Transaction<InterimArgType, ResultType> next) {
        super(prev.getName(), prev.argType(), next.resultType(), prev.canCancel() && next.canCancel());
        this.prev = prev;
        this.next = next;
    }

    public Class<? super InterimArgType> interimArgType() {
        return prev.resultType();
    }

    Transaction<ArgType, InterimArgType> previous() {
        return prev;
    }

    Transaction<InterimArgType, ResultType> next() {
        return next;
    }

    @Override
    protected ResultType run(TransactionController controller, ArgType argument) throws TransactionException {
        return next.run(controller, prev.run(controller, argument));
    }

    @Override
    protected boolean rollback(TransactionController controller, ArgType argument, ResultType prevResult) throws TransactionException {
        throw new AssertionError("Should never be called");
    }

    @Override
    protected void listContents(List<? super Transaction<?,?>> contents) {
        prev.listContents(contents);
        next.listContents(contents);
    }

    @Override
    boolean contains(Transaction<?, ?> x) {
        return x == this || prev.contains(x) || next.contains(x);
    }

    private final Map<Transaction<?,?>, Integer> indexCache = Collections.synchronizedMap(new HashMap<Transaction<?,?>, Integer> ());
    @Override
    @SuppressWarnings("element-type-mismatch")
    protected int indexOf(Transaction<?, ?> xaction) {
        //Avoid lots of recursion in multiple calls
        Integer res = indexCache.get(xaction);
        if (res != null) {
            return res.intValue();
        }
        int result = -1;
        if (xaction == this || xaction == prev) {
            result = 0;
        } else {
            if (next == xaction) {
                return prev.size();
            }
            int prevIx = prev.indexOf(xaction);
            if (prevIx >= 0) {
                result = prevIx;
            } else {
                int nextIx = next.indexOf(xaction);
                if (nextIx >= 0) {
                    result = nextIx + prev.size();
                }
            }
        }
//        assert (result == -1) == (!contents().contains(xaction)) : "Index of " + xaction + " is " + result + " contents claims" + this;
//        indexCache.put(xaction, result);
        return result;
    }

    @Override
    protected int size() {
        return prev.size() + next.size();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + prev.getClass().getName() + "," + next.getClass().getName() + " size=" + size() + "]";
//        return super.toString() + "[" + prev + "," + next + "]";
    }

    @Override
    TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> createRunner () {
        return new CompoundTransactionManager<ArgType, InterimArgType, ResultType> (this);
    }
}
