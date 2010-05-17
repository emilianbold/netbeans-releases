/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.editor.fold;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.netbeans.api.editor.fold.FoldType;


/**
 * This is SPI (Service Provider Interface) object corresponding
 * to the <code>FoldHierarchy</code> in one-to-one relationship.
 * <br>
 * The <code>FoldHierarchy</code> delegates all its operations
 * to this object.
 *
 * <p>
 * All the changes performed in to the folds are always done
 * in terms of a transaction represented by {@link FoldHierarchyTransaction}.
 * The transaction can be opened by {@link #openTransaction()}.
 *
 * <p>
 * This class changes its state upon displayability change
 * of the associated copmonent by listening on "ancestor" component property.
 * <br>
 * If the component is not displayable then the list of root folds becomes empty
 * while if the component gets displayable the root folds are created
 * according to registered managers.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldOperationImpl {

    // -J-Dorg.netbeans.api.editor.fold.FoldHierarchy.level=FINEST
    private static final Logger LOG = Logger.getLogger(FoldHierarchy.class.getName());
    
    private FoldOperation operation;
    
    private FoldHierarchyExecution execution;
    
    private FoldManager manager;
    
    private int priority;
    
    private boolean released;
    
    public FoldOperationImpl(FoldHierarchyExecution execution,
    FoldManager manager, int priority) {
        this.execution = execution;
        this.manager = manager;
        this.priority = priority;

        this.operation = SpiPackageAccessor.get().createFoldOperation(this);
        if (manager != null) { // manager for root-fold is null
            manager.init(getOperation());
        }
    }
    
    public FoldOperation getOperation() {
        return operation;
    }
    
    public void initFolds(FoldHierarchyTransactionImpl transaction) {
        manager.initFolds(transaction.getTransaction());
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Fold Hierarchy after initFolds():\n" + execution + '\n');
            execution.checkConsistency();
        }
    }
    
    public FoldHierarchy getHierarchy() {
        return execution.getHierarchy();
    }
    
    public FoldManager getManager() {
        return manager;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public Document getDocument() {
        return execution.getComponent().getDocument();
    }
    
    public Fold createFold(FoldType type, String description, boolean collapsed,
    int startOffset, int endOffset, int startGuardedLength, int endGuardedLength,
    Object extraInfo)
    throws BadLocationException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Creating fold: type=" + type // NOI18N
                + ", description='" + description + "', collapsed=" + collapsed // NOI18N
                + ", startOffset=" + startOffset + ", endOffset=" + endOffset // NOI18N
                + ", startGuardedLength=" + startGuardedLength // NOI18N
                + ", endGuardedLength=" + endGuardedLength // NOI18N
                + ", extraInfo=" + extraInfo + '\n' // NOI18N
            );
            
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "Fold creation stack", new Exception());
            }
        }

        return getAccessor().createFold(this,
            type, description, collapsed,
            getDocument(), startOffset, endOffset,
            startGuardedLength, endGuardedLength,
            extraInfo
        );
    }
    
    public Object getExtraInfo(Fold fold) {
        checkFoldOperation(fold);
        return getAccessor().foldGetExtraInfo(fold);
    }
    
    public boolean isStartDamaged(Fold fold) {
        checkFoldOperation(fold);
        return getAccessor().foldIsStartDamaged(fold);
    }
    
    public boolean isEndDamaged(Fold fold) {
        checkFoldOperation(fold);
        return getAccessor().foldIsEndDamaged(fold);
    }
    
    public FoldHierarchyTransactionImpl openTransaction() {
        return execution.openTransaction();
    }
    
    public boolean addToHierarchy(Fold fold, FoldHierarchyTransactionImpl transaction) {
        checkFoldOperation(fold);
        return execution.add(fold, transaction);
    }

    public void removeFromHierarchy(Fold fold, FoldHierarchyTransactionImpl transaction) {
        checkFoldOperation(fold);
        execution.remove(fold, transaction);
    }
    
    public boolean isAddedOrBlocked(Fold fold) {
        checkFoldOperation(fold);
        return execution.isAddedOrBlocked(fold);
    }
    
    public boolean isBlocked(Fold fold) {
        checkFoldOperation(fold);
        return execution.isBlocked(fold);
    }
    
    public void setEndOffset(Fold fold, int endOffset, FoldHierarchyTransactionImpl transaction)
    throws BadLocationException {
        checkFoldOperation(fold);
        int origEndOffset = fold.getEndOffset();
        ApiPackageAccessor api = getAccessor();
        api.foldSetEndOffset(fold, getDocument(), endOffset);
        api.foldStateChangeEndOffsetChanged(transaction.getFoldStateChange(fold), origEndOffset);
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        manager.insertUpdate(evt, transaction.getTransaction());
    }
    
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        manager.removeUpdate(evt, transaction.getTransaction());
    }
    
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        manager.changedUpdate(evt, transaction.getTransaction());
    }

    public void release() {
        released = true;
        manager.release();
    }
    
    public boolean isReleased() {
        return released;
    }

    private void checkFoldOperation(Fold fold) {
        FoldOperationImpl foldOperation = getAccessor().foldGetOperation(fold);
        if (foldOperation != this) {
            throw new IllegalStateException(
                "Attempt to use the fold " + fold // NOI18N
                + " with invalid fold operation " // NOI18N
                + foldOperation + " instead of " + this // NOI18N
            );
        }
    }
    
    private static ApiPackageAccessor getAccessor() {
        return ApiPackageAccessor.get();
    }

}
