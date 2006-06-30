/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.editor.fold;

import org.netbeans.modules.editor.fold.FoldHierarchyTransactionImpl;

/**
 * Class encapsulating a modification
 * of the code folding hierarchy.
 * <br>
 * It's provided by {@link FoldOperation#openTransaction()}.
 * <br>
 * It can accumulate arbitrary number of changes of various folds
 * by being passed as argument to particular methods in the FoldOperation.
 * <br>
 * Only one transaction can be active at the time.
 * <br>
 * Once all the modifications are done the transaction must be
 * committed by {@link #commit()} which creates
 * a {@link org.netbeans.api.editor.fold.FoldHierarchyEvent}
 * and fires it to the listeners automatically.
 * <br>
 * Once the transaction is committed no additional
 * changes can be made to it.
 * <br>
 * There is currently no way to rollback the transaction.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldHierarchyTransaction {
    
    private final FoldHierarchyTransactionImpl impl;

    FoldHierarchyTransaction(FoldHierarchyTransactionImpl impl) {
        this.impl = impl;
    }

    /**
     * Commit this transaction.
     * <br>
     * Transaction can only be committed once.
     *
     * @throws IllegalStateException if the transaction is attempted
     *  to be commited more than once.
     */
    public void commit() {
        impl.commit();
    }
    
    FoldHierarchyTransactionImpl getImpl() {
        return impl;
    }

}
