/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
