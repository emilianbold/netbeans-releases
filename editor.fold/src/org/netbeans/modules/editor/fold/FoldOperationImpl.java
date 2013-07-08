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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.openide.util.Exceptions;


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
        if (origEndOffset == endOffset) {
            return;
        }
        ApiPackageAccessor api = getAccessor();
        FoldStateChange state = transaction.getFoldStateChange(fold);
        if (state.getOriginalStartOffset() >= 0 && state.getOriginalStartOffset() > endOffset) {
            LOG.warning("Original start offset > end offset, dumping fold hierarchy: " + execution);
        }
        api.foldSetEndOffset(fold, getDocument(), endOffset);
        api.foldStateChangeEndOffsetChanged(transaction.getFoldStateChange(fold), origEndOffset);
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        if (!isReleased()) {
            manager.insertUpdate(evt, transaction.getTransaction());
        }
    }
    
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        if (!isReleased()) {
            manager.removeUpdate(evt, transaction.getTransaction());
        }
    }
    
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        if (!isReleased()) {
            manager.changedUpdate(evt, transaction.getTransaction());
        }
    }

    public void release() {
        released = true;
        manager.release();
    }
    
    public boolean isReleased() {
        return released;
    }
    
    /**
     * Enumerates all folds contributed by this Operation, whether blocked or active.
     * 
     * @return 
     */
    public Iterator<Fold>   foldIterator() {
        return new BI(new DFSI(execution.getRootFold()));
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
    
    /**
     * Compares two folds A, B. Fold "A" precedes "B", if and only if:
     * <ul>
     * <li>A fully encloses B, or
     * <li>A starts before B, or
     * <li>A and B occupy the same range, and A's priority is lower
     * </ul>
     * 
     * @param a fold to compare
     * @param b fold to compare
     * @return -1, 1, 0 as appropriate for a Comparator
     */
    private static final Comparator<Fold> FOLD_COMPARATOR = new Comparator<Fold>() {
        @Override
        public int compare(Fold a, Fold b) {
            int diff = a.getStartOffset() - b.getStartOffset();
            if (diff < 0) {
                return -1;
            }
            int diff2 = b.getEndOffset() - a.getEndOffset();
            if (diff2 != 0 || diff != 0) {
                return 1;
            }
            ApiPackageAccessor accessor = getAccessor();
            return accessor.foldGetOperation(a).getPriority() - accessor.foldGetOperation(b).getPriority();
        }
    };

    /**
     * Level of depth-first traversal
     */
    static class PS {
        private Fold    parent;
        private int     childIndex = -1;
        private PS      next;
        
        PS(Fold parent, PS next) {
            this.parent = parent;
            this.next = next;
        }
    }
    
    /**
     * Implmentation of depth-first pre-order traversal through Fold hierarchy.
     * Each level is iterated in the fold order = start offset order.
     */
    private class DFSI implements Iterator<Fold> {
        PS  level;
        
        private DFSI(Fold root) {
            level = new PS(root, null);
        }
        
        @Override
        public Fold next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (level.childIndex == -1) {
                level.childIndex++;
                return level.parent;
            }
            // note that hasNext also pops levels, as necessary, so there's a
            // level, which is not yet exhausted.
            Fold f = level.parent.getFold(level.childIndex++);
            if (f.getFoldCount() > 0) {
                level = new PS(f, level);
                level.childIndex++;
                return level.parent;
            }
            return f;
        }
        
        @Override
        public boolean hasNext() {
            while (level != null) {
                if (level.childIndex == -1) {
                    return true;
                } else if (level.childIndex >= level.parent.getFoldCount()) {
                    level = level.next;
                } else {
                    return true;
                }
            }
            return false;
        }
        
        public void remove() {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Iterator, which processes all blocked folds along with their blocker.
     * The blocker + blockers folds are ordered using the fold order. Results
     * are filtered to contain just Folds produced by this Operation. Folds 
     * owned by other operations/executions are skipped.
     * <p/>
     * Note that blocked folds do not form a hierarchy; they were removed from
     * the fold hierarchy when it was decided to block those folds. So prior to
     * iterating further in FoldHierarchy, all (recursively) blocked folds must
     * be processed.
     */
    private class BI implements Iterator<Fold> {
        private Iterator<Fold>  dfsi;
        private Iterator<Fold>  blockedFolds;
        private Fold ret;
        private Stack<Object[]> blockStack = new Stack<Object[]>();
        private Fold blocker;

        public BI(Iterator<Fold> dfsi) {
            this.dfsi = dfsi;
        }
        
        /**
         * If fold 'f' blocks some other folds, those blocked folds will b processed
         * instead of 'f'. f will be mixed among and ordered with its blocked folds, so the
         * entire chain will be processed in the document order.
         * 
         * @param f
         * @return true, if blocked folds should be processed.
         */
        private boolean processBlocked(Fold f) {
            if (f == blocker) {
                return false;
            }
            Collection<Fold> blocked = execution.getBlockedFolds(f);
            if (blocked != null && !blocked.isEmpty()) {
                List<Fold> blockedSorted = new ArrayList<Fold>(blocked.size() + 1);
                blockedSorted.addAll(blocked);
                // enumerate together with blocked ones
                blockedSorted.add(f);
                Collections.sort(blockedSorted, FOLD_COMPARATOR);
                blockStack.push(new Object[] { blockedFolds, blocker});
                blockedFolds = blockedSorted.iterator();
                blocker = f;
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Fold next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Fold f = ret;
            ret = null;
            return f;
        }
        
        @Override
        public boolean hasNext() {
            if (ret != null) {
                return true;
            }
            if (blockedFolds != null) {
                while (blockedFolds.hasNext()) {
                    Fold f = blockedFolds.next();
                    if (processBlocked(f)) {
                        // continue with a different level of blocking
                        continue;
                    }
                    if (operation.owns(f)) {
                        ret = f;
                        return true;
                    }
                }
                blockedFolds = null;
            }
            if (!blockStack.isEmpty()) {
                Object[] o = blockStack.pop();
                blocker = (Fold)o[1];
                blockedFolds = (Iterator<Fold>)o[0];
                return hasNext();
            }
            
            while (dfsi.hasNext()) {
                Fold f = dfsi.next();
                if (processBlocked(f)) {
                    return hasNext();
                }
                if (operation.owns(f)) {
                    ret = f;
                    return true;
                }
            }
            return false;
        }
    }
    
    public Map<FoldInfo, Fold> update(Collection<FoldInfo> fi, Collection<Fold> removed, Collection<FoldInfo> created) throws BadLocationException {
        Refresher r = new Refresher(fi);
        if (!isReleased()) {
            if (!execution.isLockedByCaller()) {
                throw new IllegalStateException("Update must run under FoldHierarchy lock");
            }
            r.run();
        } else {
            return null;
        }
        if (removed != null) {
            removed.addAll(r.toRemove);
        }
        if (created != null) {
            created.addAll(r.toAdd);
        }
        return r.currentFolds;
    }
    
    public boolean getInitialState(FoldType ft) {
        return execution.getInitialFoldState(ft);
    }

    private class Refresher implements Comparator<FoldInfo> {
        private Collection<FoldInfo>    foldInfos;
        private Collection<Fold>        toRemove = new ArrayList<Fold>();
        private Collection<FoldInfo>    toAdd = new ArrayList<FoldInfo>();
        private Map<FoldInfo, Fold>     currentFolds = new HashMap<FoldInfo, Fold>();

        /**
         * Transaction which covers the update
         */
        private FoldHierarchyTransactionImpl tran;

        public Refresher(Collection<FoldInfo> foldInfos) {
            this.foldInfos = foldInfos;
        }
        
        @Override
        public int compare(FoldInfo a, FoldInfo b) {
            int diff = a.getStart() - b.getStart();
            if (diff != 0) {
                return diff;
            }
            int diff2 = b.getEnd() - a.getEnd();
            return diff2;
        }
        
            
        private int compare(FoldInfo info, Fold f) {
            if (info == null) {
                if (f == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (f == null) {
                return -1;
            }

            int diff = info.getStart() - f.getStartOffset();
            if (diff != 0) {
                return diff;
            }
            diff = f.getEndOffset() - info.getEnd();
            if (diff != 0) {
                return diff;
            }
            if (info.getType() == f.getType()) {
                return 0;
            }
            return info.getType().code().compareToIgnoreCase(f.getType().code());
        }
        
        private Iterator<Fold>  foldIt;
        private Iterator<FoldInfo>  infoIt;
        private FoldInfo nextInfo;
        
        private FoldInfo ni() {
            if (nextInfo != null) {
                FoldInfo f = nextInfo;
                nextInfo = null;
                return f;
            }
            return infoIt.hasNext() ? infoIt.next() : null;
        }
        
        private FoldInfo peek() {
            FoldInfo f = ni();
            nextInfo = f;
            return f;
        }
        
        private boolean containsOneAnother(FoldInfo i, Fold f) {
            int s1 = i.getStart();
            int s2 = f.getStartOffset();
            int e1 = i.getEnd();
            int e2 = f.getEndOffset();
            
            return ((s1 >= s2 && e2 >= e1)  ||
                (s2 >= s1 && e1 >= e2));
        }
        
        private boolean nextSameRange(FoldInfo i, Fold f) {
            if (i == null || f == null) {
                return false;
            }
            if (i.getType() != f.getType() || !containsOneAnother(i, f)) {
                return false;
            }
            FoldInfo next = peek();
            if (next == null) {
                return true;
            }
            return next.getStart() != f.getStartOffset() || next.getEnd() != f.getEndOffset();
        }
        
        private boolean containsSame(FoldInfo i, Fold f) {
            if (i == null || f == null || i.getType() != f.getType()) {
                return false;
            }
            return containsOneAnother(i, f);
        }
        
        public void run() throws BadLocationException {
            // first order the supplied folds:
            List ll = new ArrayList<FoldInfo>(foldInfos);
            Collections.sort(ll, this);
            
            foldIt = foldIterator();
            infoIt = ll.iterator();
            
            Fold f = foldIt.hasNext() ? foldIt.next() : null;
            FoldInfo i = infoIt.hasNext() ? infoIt.next() : null;
            
            tran = openTransaction();

            
            try {
                while (f != null || i != null) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Fold = " + f + ", FoldInfo = " + i);
                    }
                    int action = compare(i, f);
                    if (action < 0 && !nextSameRange(i, f)) {
                        // create a new fold from the FoldInfo
                        toAdd.add(i);
                        i = ni();
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("Advanced info, next = " + i);
                        }
                        continue;
                    } else if (action > 0 && !containsSame(i, f)) {
                        toRemove.add(f);
                        f = foldIt.hasNext() ? foldIt.next() : null;
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("Advanced fold, next = " + f);
                        }
                        continue;
                    }

                    update(f, i);
                    currentFolds.put(i, f);
                    i = ni();
                    f = foldIt.hasNext() ? foldIt.next() : null;
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Advanced both info & fold");
                    }
                }
                for (Fold fold : toRemove) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Removing: " + f);
                    }
                    if (fold.getParent() != null) {
                        removeFromHierarchy(fold, tran);
                    }
                }
                for (FoldInfo info : toAdd) {
                    try {
                        currentFolds.put(info, getOperation().addToHierarchy(
                                info.getType(), 
                                info.getStart(), info.getEnd(),
                                info.getCollapsed(), 
                                info.getTemplate(),
                                info.getDescriptionOverride(),
                                info.getExtraInfo(),
                                tran.getTransaction()));
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("Adding: " + i);
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } finally {
                tran.commit();
            }
        }
        
        public Fold update(Fold f, FoldInfo info) throws BadLocationException {
            this.fsch = null;
            int soffs = f.getStartOffset();
            ApiPackageAccessor acc = getAccessor();
            if (info.getStart() != soffs) {
                acc.foldSetStartOffset(f, getDocument(), info.getStart());
                FoldStateChange state = getFSCH(f);
                if (state.getOriginalEndOffset() >= 0 && state.getOriginalEndOffset() < soffs) {
                    LOG.warning("Original start offset > end offset, dumping fold hierarchy: " + execution);
                    LOG.warning("FoldInfo: " + info + ", fold: " + f);
                }
                acc.foldStateChangeStartOffsetChanged(state, soffs);
                soffs = info.getStart();
            }
            int eoffs = f.getEndOffset();
            if (info.getEnd() != eoffs) {
                FoldStateChange state = getFSCH(f);
                if (state.getOriginalStartOffset()>= 0 && state.getOriginalStartOffset() > eoffs) {
                    LOG.warning("Original end offset < start offset, dumping fold hierarchy: " + execution);
                    LOG.warning("FoldInfo: " + info + ", fold: " + f);
                }
                acc.foldSetEndOffset(f, getDocument(), info.getEnd());
                acc.foldStateChangeEndOffsetChanged(state, eoffs);
                eoffs = info.getEnd();
            }
            if (soffs > eoffs) {
                LOG.warning("Updated end offset < start offset, dumping fold hierarchy: " + execution);
                LOG.warning("FoldInfo: " + info + ", fold: " + f);
            }
            String desc = info.getDescriptionOverride();
            if (desc == null) {
                desc = info.getTemplate().getDescription();
            }
            if (!f.getDescription().equals(desc)) {
                acc.foldSetDescription(f, desc);
                acc.foldStateChangeDescriptionChanged(getFSCH(f));
            }
            if (info.getCollapsed() != null && f.isCollapsed() != info.getCollapsed()) {
                getAccessor().foldSetCollapsed(f, info.getCollapsed());
                getAccessor().foldStateChangeCollapsedChanged(getFSCH(f));
            }
            return f;
        }

        /**
         * FoldStateChange for the current fold being updated;
         * just an optimization.
         */
        private FoldStateChange fsch;

        private FoldStateChange getFSCH(Fold f) {
            if (fsch != null) {
                return fsch;
            }
            return fsch = tran.getFoldStateChange(f);
        }

    }
    
    public String toString() {
        return "FoldOp[mgr = " + manager + ", rel = " + released + "]"; // NOI18N
    }
}
