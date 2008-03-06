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

package org.netbeans.spi.editor.fold;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.modules.editor.fold.ApiPackageAccessor;
import org.netbeans.modules.editor.fold.FoldHierarchyTransactionImpl;
import org.netbeans.modules.editor.fold.FoldOperationImpl;
import org.netbeans.modules.editor.fold.SpiPackageAccessor;


/**
 * Fold operation represents services
 * provided to an individual fold manager.
 * <br>
 * Each manager has its own dedicated instance
 * of fold operation.
 *
 * <p>
 * There are three main services - creation of a new fold
 * and adding or removing it from the hierarchy.
 * <br>
 * Adding and removing of the folds requires a valid transaction
 * that can be obtained by {@link #openTransaction()}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldOperation {
    
    private static boolean spiPackageAccessorRegistered;

    static {
        ensureSpiAccessorRegistered();
    }
    
    private static void ensureSpiAccessorRegistered() {
        if (!spiPackageAccessorRegistered) {
            spiPackageAccessorRegistered = true;
            SpiPackageAccessor.register(new SpiPackageAccessorImpl());
        }
    }
    
    private final FoldOperationImpl impl;
    
    private FoldOperation(FoldOperationImpl impl) {
        this.impl = impl;
    }
 
    
    /**
     * Create new fold instance and add it to the hierarchy.
     * <br>
     * The fold will either become part of the hierarchy directly or it will
     * become blocked by another fold already present in the hierarchy.
     * <br>
     * Once the blocking fold gets removed this fold will be phyiscally added
     * to the hierarchy automatically.
     *
     * <p>
     * The fold is logically bound to the fold manager that uses this fold operation.
     * <br>
     * The fold can only be removed by this fold operation.
     *
     * @param type type of the fold to be assigned to the fold.
     * @param description textual description of the fold that will be displayed
     *  once the fold becomes collapsed.
     * @param collapsed whether the fold should initially be collapsed or expanded.
     * @param startOffset starting offset of the fold. The fold creates swing position
     *  for the offset.
     * @param endOffset ending offset of the fold. The fold creates swing position
     *  for the offset.
     * @param startGuardedLength &gt;=0 initial guarded area of the fold (starting at the start offset).
     *  If the guarded area is modified the fold will be removed automatically.
     * @param endGuardedLength &gt;=0 ending guarded area of the fold (ending at the end offset).
     *  If the guarded area is modified the fold will be removed automatically.
     * @param extraInfo arbitrary extra information specific for the fold being created.
     *  It's not touched or used by the folding infrastructure in any way.
     *  <code>null<code> can be passed if there is no extra information.
     *  <br>
     *  The extra info of the existing fold can be obtained by
     *  {@link #getExtraInfo(org.netbeans.api.editor.fold.Fold)}.
     *
     * @return new fold instance that was added to the hierarchy.
     */
    public Fold addToHierarchy(FoldType type, String description, boolean collapsed,
    int startOffset, int endOffset, int startGuardedLength, int endGuardedLength,
    Object extraInfo, FoldHierarchyTransaction transaction)
    throws BadLocationException {
        Fold fold = impl.createFold(type, description, collapsed,
            startOffset, endOffset, startGuardedLength, endGuardedLength,
            extraInfo
        );
        impl.addToHierarchy(fold, transaction.getImpl());
        return fold;
    }
    
    /**
     * This static method can be used to check whether the bounds
     * of the fold that is planned to be added are valid.
     * <br>
     * The conditions are:<pre>
     *  startOffset &lt; endOffset
     * </pre>
     *
     * <pre>
     *  startGuardedLength &gt;= 0
     * </pre>
     *
     * <pre>
     *  endGuardedLength &gt;= 0
     * </pre>
     *
     * <pre>
     *  startOffset + startGuardedLength &lt;= endOffset - endGuardedLength
     * </pre>
     *
     * @return true if the bounds are OK or false otherwise.
     */
    public static boolean isBoundsValid(int startOffset, int endOffset,
    int startGuardedLength, int endGuardedLength) {
        return (startOffset < endOffset)
            && (startGuardedLength >= 0)
            && (endGuardedLength >= 0)
            && ((startOffset + startGuardedLength) <= (endOffset -endGuardedLength));
    }
    
    /**
     * Remove the fold that is either present in the hierarchy or blocked
     * by another fold.
     *
     * @param fold fold to be removed
     * @param transaction non-null transaction under which the fold should be removed.
     */
    public void removeFromHierarchy(Fold fold, FoldHierarchyTransaction transaction) {
        impl.removeFromHierarchy(fold, transaction.getImpl());
    }
    
    /**
     * Check whether this fold operation has produced the given fold.
     * 
     * @param fold non-null fold.
     * @return true if this fold operation produced the given fold (by its <code>addToHierarchy()</code> method)
     *   or false otherwise.
     */
    public boolean owns(Fold fold) {
        return (ApiPackageAccessor.get().foldGetOperation(fold) == impl);
    }
    
    /**
     * Return extra info object passed to fold at time of its creation.
     *
     * @return extra information object specific for the fold
     *  or null if there was no extra info.
     */
    public Object getExtraInfo(Fold fold) {
        return impl.getExtraInfo(fold);
    }
    
    /**
     * Check whether the starting guarded area of the fold
     * is damaged by a document modification.
     *
     * @param fold fold to check. The fold must be managed by this fold operation.
     * @return true if the starting area of the fold was damaged by the modification
     *  or false otherwise.
     */
    public boolean isStartDamaged(Fold fold) {
        return impl.isStartDamaged(fold);
    }

    /**
     * Check whether the ending guarded area of the fold
     * is damaged by a document modification.
     *
     * @param fold fold to check. The fold must be managed by this fold operation.
     * @return true if the ending area of the fold was damaged by the modification
     *  or false otherwise.
     */
    public boolean isEndDamaged(Fold fold) {
        return impl.isEndDamaged(fold);
    }

    /**
     * Open a new transaction over the fold hierarchy.
     * <br>
     * <b>Note:</b> Always use the following pattern:
     * <pre>
     *     FoldHierarchyTransaction transaction = operation.openTransaction();
     *     try {
     *         ...
     *     } finally {
     *         transaction.commit();
     *     }
     * </pre>
     *
     * @return opened transaction for further use.
     */
    public FoldHierarchyTransaction openTransaction() {
        return impl.openTransaction().getTransaction();
    }
    
    /**
     * Check whether the fold is currently present in the hierarchy or blocked.
     *
     * @return true if the fold is currently present in the hierarchy or blocked
     *  or false otherwise.
     */
    public boolean isAddedOrBlocked(Fold fold) {
        return impl.isAddedOrBlocked(fold);
    }
    
    /**
     * Is the given fold blocked by another fold?
     */
    public boolean isBlocked(Fold fold) {
        return impl.isBlocked(fold);
    }
    
    /**
     * Get the hierarchy for which this fold operations works.
     */
    public FoldHierarchy getHierarchy() {
        return impl.getHierarchy();
    }

    public boolean isReleased() {
        return impl.isReleased();
    }
    
    
    private static final class SpiPackageAccessorImpl extends SpiPackageAccessor {

        public FoldHierarchyTransaction createFoldHierarchyTransaction(
        FoldHierarchyTransactionImpl impl) {
            return new FoldHierarchyTransaction(impl);
        }
        
        public FoldHierarchyTransactionImpl getImpl(FoldHierarchyTransaction transaction) {
            return transaction.getImpl();
        }
        
        public FoldOperation createFoldOperation(FoldOperationImpl impl) {
            return new FoldOperation(impl);
        }
        
    }
}
