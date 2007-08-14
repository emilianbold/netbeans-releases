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

package org.netbeans.modules.editor.fold;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.netbeans.lib.editor.util.PriorityMutex;
import org.openide.ErrorManager;

/**
 * Class backing the <code>FoldHierarchy</code> in one-to-one relationship.
 * <br>
 * The <code>FoldHierarchy</code> delegates all its operations
 * to this object.
 *
 * <p>
 * All the changes performed in to the folds are always done
 * in terms of a transaction represented by {@link FoldHierarchyTransactionImpl}.
 * The transaction can be opened by {@link #openTransaction()}.
 *
 * <p>
 * This class changes its state upon displayability change
 * of the associated component by listening on "ancestor" component property.
 * <br>
 * If the component is not displayable then the list of root folds becomes empty
 * while if the component gets displayable the root folds are created
 * according to registered managers.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldHierarchyExecution implements DocumentListener {
    
    private static final String PROPERTY_FOLD_HIERARCHY_MUTEX = "foldHierarchyMutex"; //NOI18N

    private static final String PROPERTY_FOLDING_ENABLED = "code-folding-enable"; //NOI18N

    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.fold"); //NOI18N
    
    private static final boolean debugFire
        = Boolean.getBoolean("netbeans.debug.editor.fold.fire"); //NOI18N
    
    private static final FoldOperationImpl[] EMPTY_FOLD_OPERTAION_IMPL_ARRAY
        = new FoldOperationImpl[0];
    
    static {
        // The following call will make sure that the SpiPackageAccessor gets initialized
        FoldOperation.isBoundsValid(0, 0, 0, 0);
    }
    
    private final JTextComponent component;
    
    private FoldHierarchy hierarchy;
    
    private Fold rootFold;
    
    private FoldOperationImpl[] operations = EMPTY_FOLD_OPERTAION_IMPL_ARRAY;
    
    /**
     * Map containing [blocked-fold, blocking-fold] pairs.
     */
    private Map blocked2block = new HashMap(4);
    
    /**
     * Map containing [blocking-fold, blocked-fold-set] pairs.
     */
    private Map block2blockedSet = new HashMap(4);
    
    /** Whether hierarchy is initialized (root fold etc.) */
    private boolean inited;
    
    private AbstractDocument lastDocument;
    
    private PriorityMutex mutex;
    
    private EventListenerList listenerList;
    
    private boolean foldingEnabled;
    
    private FoldHierarchyTransactionImpl activeTransaction;
    
    private PropertyChangeListener componentChangesListener;
    
    public static synchronized FoldHierarchy getOrCreateFoldHierarchy(JTextComponent component) {
        if (component == null) {
            throw new NullPointerException("component cannot be null"); // NOI18N
        }

        FoldHierarchyExecution execution
            = (FoldHierarchyExecution)component.getClientProperty(FoldHierarchyExecution.class);
        
        if (execution == null) {
            execution = new FoldHierarchyExecution(component);
            execution.init();

            component.putClientProperty(FoldHierarchyExecution.class, execution);
        }
        
        return execution.getHierarchy();
    }
    
    /**
     * Construct new fold hierarchy SPI
     *
     * @param hierarchy hierarchy for which this SPI gets created.
     * @param component comoponent for which this all happens.
     */
    private FoldHierarchyExecution(JTextComponent component) {
        this.component = component;
    }
    
    /**
     * Initialize this spi by existing hierarchy instance
     * (the one for which this spi was created).
     * <br>
     * This is called lazily upon first attempt to lock
     * the hierarchy.
     */
    private void init() {
        // Allow listeners to be added
        listenerList = new EventListenerList();

        // Assign mutex
        mutex = (PriorityMutex)component.getClientProperty(PROPERTY_FOLD_HIERARCHY_MUTEX);
        if (mutex == null) {
            mutex = new PriorityMutex();
            component.putClientProperty(PROPERTY_FOLD_HIERARCHY_MUTEX, mutex);
        }

        this.hierarchy = ApiPackageAccessor.get().createFoldHierarchy(this);
        
        Document doc = component.getDocument();
        try {
            rootFold = ApiPackageAccessor.get().createFold(
                new FoldOperationImpl(this, null, Integer.MAX_VALUE),
                FoldHierarchy.ROOT_FOLD_TYPE,
                "root", // NOI18N
                false,
                doc,
                0, doc.getEndPosition().getOffset(),
                0, 0,
                null
            );
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        foldingEnabled = getFoldingEnabledSetting();

        // Start listening on component changes
        startComponentChangesListening();

        rebuild();
    }
    
    /**
     * Get the fold hierarchy associated with this SPI
     * in one-to-one relationship.
     */
    public final FoldHierarchy getHierarchy() {
        return hierarchy;
    }
    
    /**
     * Lock the hierarchy for exclusive use. This method must only
     * be used together with {@link #unlock()} in <code>try..finally</code> block.
     * <br>
     * Prior using this method the document must be locked.
     * The document lock can be either readlock
     * e.g. by using {@link javax.swing.text.Document#render(Runnable)}
     * or writelock
     * e.g. when in {@link javax.swing.event.DocumentListener})
     * and must be obtained on component's document
     * i.e. {@link javax.swing.text.JTextComponent#getDocument()}
     * should be used.
     *
     * <p>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * <p>
     * <font color="red">
     * <b>Note:</b> The clients using this method must ensure that
     * they <b>always</b> use this method in the following pattern:<pre>
     *
     *     lock();
     *     try {
     *         ...
     *     } finally {
     *         unlock();
     *     }
     * </pre>
     * </font>
     */
    public final void lock() {
        mutex.lock();
    }
    
    /**
     * Unlock the hierarchy from exclusive use. This method must only
     * be used together with {@link #lock()} in <code>try..finally</code> block.
     */
    public void unlock() {
        mutex.unlock();
    }
    
    /**
     * Get the text component for which this fold hierarchy was created.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * @return non-null text component for which this fold hierarchy was created.
     */
    public JTextComponent getComponent() {
        return component;
    }

    /**
     * Get the root fold of this hierarchy.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * @return root fold of this hierarchy.
     *   The root fold covers the whole document and is uncollapsable.
     */
    public Fold getRootFold() {
        return rootFold;
    }
    
    /**
     * Add listener for changes done in the hierarchy.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * @param l non-null listener to be added.
     */
    public void addFoldHierarchyListener(FoldHierarchyListener l) {
        synchronized (listenerList) {
            listenerList.add(FoldHierarchyListener.class, l);
        }
    }
    
    /**
     * Remove previously added listener for changes done in the hierarchy.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * @param l non-null listener to be removed.
     */
    public void removeFoldHierarchyListener(FoldHierarchyListener l) {
        synchronized (listenerList) {
            listenerList.remove(FoldHierarchyListener.class, l);
        }
    }
    
    void fireFoldHierarchyListener(FoldHierarchyEvent evt) {
        if (debugFire) {
            /*DEBUG*/System.err.println("Firing FoldHierarchyEvent:\n" + evt); // NOI18N
        }

        Object[] listeners = listenerList.getListenerList(); // no need to sync
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == FoldHierarchyListener.class) {
                ((FoldHierarchyListener)listeners[i + 1]).foldHierarchyChanged(evt);
            }
        }
        
    }
    
    /**
     * Attempt to add the given fold to the code folding hierarchy.
     * The fold will either become part of the hierarchy or it will
     * become blocked by another fold present in the hierarchy.
     * <br>
     * Only folds created by the fold operations of this hierarchy
     * can be added.
     *
     * @param fold fold to be added
     * @param transaction transaction under which the fold should be added.
     * @return true if the fold was added successfully or false
     *  if it became blocked.
     */
    public boolean add(Fold fold, FoldHierarchyTransactionImpl transaction) {
        if (fold.getParent() != null || isBlocked(fold)) {
            throw new IllegalStateException("Fold already added: " + fold); // NOI18N
        }
        
        boolean added = transaction.addFold(fold);
        
//        checkConsistency();
        
        return added;
    }
    
    /**
     * Remove the fold that is either present in the hierarchy or blocked
     * by another fold.
     *
     * @param fold fold to be removed
     * @param transaction non-null transaction under which the fold should be removed.
     */
    public void remove(Fold fold, FoldHierarchyTransactionImpl transaction) {
        transaction.removeFold(fold);
        
//        checkConsistency();
    }
    
    /**
     * Check whether the fold is currently present in the hierarchy or blocked.
     *
     * @return true if the fold is currently present in the hierarchy or blocked
     *  or false otherwise.
     */
    public boolean isAddedOrBlocked(Fold fold) {
        return (fold.getParent() != null || isBlocked(fold));
    }
    
    /**
     * Is the given fold blocked by another fold?
     */
    public boolean isBlocked(Fold fold) {
        return (getBlock(fold) != null);
    }
    
    /**
     * Get the fold blocking the given fold or null
     * if the fold is not blocked.
     */
    Fold getBlock(Fold fold) {
        return (blocked2block.size() > 0)
            ? (Fold)blocked2block.get(fold)
            : null;
    }
    
    /**
     * Mark given fold as blocked by the block fold.
     */
    void markBlocked(Fold blocked, Fold block) {
        blocked2block.put(blocked, block);

        Set blockedSet = (Set)block2blockedSet.get(block);
        if (blockedSet == null) {
            blockedSet = new HashSet();
            block2blockedSet.put(block, blockedSet);
        }
        if (!blockedSet.add(blocked)) { // already added
            throw new IllegalStateException("fold " + blocked + " already blocked"); // NOI18N
        }
    }
    
    /**
     * Remove blocked fold from mappings.
     *
     * @param blocked fold
     * @return fold that blocked the blocked fold.
     * @throws IllegalArgumentException if the given blocked fold was not really blocked.
     */
    Fold unmarkBlocked(Fold blocked) {
        // Find block for the given blocked fold
        Fold block = (Fold)blocked2block.remove(blocked);
        if (block == null) { // not blocked
            throw new IllegalArgumentException("Not blocked: " + blocked); // NOI18N
        }

        // Remove the fold from set of blocked folds of the block
        Set blockedSet = (Set)block2blockedSet.get(block);
        if (!blockedSet.remove(blocked)) {
            throw new IllegalStateException("Not blocker for " + blocked); // NOI18N
        }
        if (blockedSet.size() == 0) { // Remove the blocker as well
            block2blockedSet.remove(block);
        }
        return block;
    }

    /**
     * Mark the given block fold to be no longer blocking
     * (and mark the folds blocked by the given block fold as not blocked).
     *
     * @param block the fold blocking others
     * @return set of folds blocked by the block or null if the given fold
     *  was not block.
     */
    Set unmarkBlock(Fold block) {
        Set blockedSet = (Set)block2blockedSet.remove(block);
        if (blockedSet != null) {
            // Remove all items of blocked set
            int size = blocked2block.size();
            blocked2block.keySet().removeAll(blockedSet);
            if (size - blocked2block.size() != blockedSet.size()) { // not all removed
                throw new IllegalStateException("Not all removed: " + blockedSet); // NOI18N
            }
        }
        return blockedSet;
    }

    /**
     * Collapse all folds in the given collection.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     */
    public void collapse(Collection c) {
        setCollapsed(c, true);
    }
    
    /**
     * Expand all folds in the given collection.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     */
    public void expand(Collection c) {
        setCollapsed(c, false);
    }
        
    private void setCollapsed(Collection c, boolean collapsed) {
        FoldHierarchyTransactionImpl transaction = openTransaction();
        try {
            for (Iterator it = c.iterator(); it.hasNext();) {
                Fold fold = (Fold)it.next();
                transaction.setCollapsed(fold, collapsed);
            }
        } finally {
            transaction.commit();
        }
    }
    
    /**
     * Open a new transaction on the fold hierarchy
     * to make a change in the hierarchy.
     * <br>
     * Transaction is active until commited
     * by calling <code>transaction.commit()</code>.
     * <br>
     * Only one transaction can be active at the time.
     * <br>
     * There is currently no way to rollback the transaction.
     *
     * <p>
     * <font color="red">
     * <b>Note:</b> The clients using this method must ensure that
     * they <b>always</b> use this method in the following pattern:<pre>
     *
     *     FoldHierarchyTransaction transaction = operation.openTransaction();
     *     try {
     *         ...
     *     } finally {
     *         transaction.commit();
     *     }
     * </pre>
     * </font>

     */
    public FoldHierarchyTransactionImpl openTransaction() {
        if (activeTransaction != null) {
            throw new IllegalStateException("Active transaction already exists."); // NOI18N
        }
        activeTransaction = new FoldHierarchyTransactionImpl(this);
        return activeTransaction;
    }
    
    void clearActiveTransaction() {
        if (activeTransaction == null) {
            throw new IllegalStateException("No transaction in progress"); // NOI18N
        }
        activeTransaction = null;
    }

    void createAndFireFoldHierarchyEvent(
    Fold[] removedFolds, Fold[] addedFolds,
    FoldStateChange[] foldStateChanges,
    int affectedStartOffset, int affectedEndOffset) {
        
        // Check correctness
        if (affectedStartOffset < 0) {
            throw new IllegalArgumentException("affectedStartOffset=" // NOI18N
                + affectedStartOffset + " < 0"); // NOI18N
        }
        
        if (affectedEndOffset < affectedStartOffset) {
            throw new IllegalArgumentException("affectedEndOffset=" // NOI18N
                + affectedEndOffset + " < affectedStartOffset=" + affectedStartOffset); // NOI18N
        }

        FoldHierarchyEvent evt = ApiPackageAccessor.get().createFoldHierarchyEvent(
            hierarchy,
            removedFolds, addedFolds, foldStateChanges,
            affectedStartOffset, affectedEndOffset
        );

        fireFoldHierarchyListener(evt);
    }

    /**
     * Rebuild the fold hierarchy - the fold managers will be recreated.
     */
    public void rebuild() {
        // Stop listening on the original document
        if (lastDocument != null) {
            // Remove document listener with specific priority
            DocumentUtilities.removeDocumentListener(lastDocument, this, DocumentListenerPriority.FOLD_UPDATE);
            lastDocument = null;
        }

        Document doc = getComponent().getDocument();
        AbstractDocument adoc;
        boolean releaseOnly; // only release the current hierarchy root folds
        if (doc instanceof AbstractDocument) {
            adoc = (AbstractDocument)doc;
            releaseOnly = false;
        } else { // doc is null or non-AbstractDocument => release the hierarchy
            adoc = null;
            releaseOnly = true;
        }
        
        if (!foldingEnabled) { // folding not enabled => release
            releaseOnly = true;
        }
        
        if (adoc != null) {
            adoc.readLock();
            
            // Start listening for changes
            if (!releaseOnly) {
                lastDocument = adoc;
                // Add document listener with specific priority
                DocumentUtilities.addDocumentListener(lastDocument, this, DocumentListenerPriority.FOLD_UPDATE);
            }
        }
        try {
            lock();
            try {
                rebuildManagers(releaseOnly);
            } finally {
                unlock();
            }
        } finally {
            if (adoc != null) {
                adoc.readUnlock();
            }
        }
    }
        
    /**
     * Rebuild (or release) the root folds of the hierarchy in the event dispatch thread.
     *
     * @param releaseOnly release the current root folds
     *  but make the new root folds array empty.
     */
    private void rebuildManagers(boolean releaseOnly) {
        for (int i = 0; i < operations.length; i++) {
            operations[i].release();
        }
        operations = EMPTY_FOLD_OPERTAION_IMPL_ARRAY; // really release
        
        // Call all the providers
        FoldManagerFactoryProvider provider = !releaseOnly
            ? FoldManagerFactoryProvider.getDefault()
            : FoldManagerFactoryProvider.getEmpty();

        List factoryList = provider.getFactoryList(getHierarchy());
        int factoryListLength = factoryList.size();

        if (debug) {
            /*DEBUG*/System.err.println("FoldHierarchy rebuild():" // NOI18N
                + " FoldManager factory count=" + factoryListLength // NOI18N
            );
        }
        
        // Create fold managers
        int priority = factoryListLength - 1; // highest priority (till lowest == 0)
        boolean ok = false;
        try {
            operations = new FoldOperationImpl[factoryListLength];
            for (int i = 0; i < factoryListLength; i++) {
                FoldManagerFactory factory = (FoldManagerFactory)factoryList.get(i);
                FoldManager manager = factory.createFoldManager();
                operations[i] = new FoldOperationImpl(this, manager, priority);
                priority--;
            }
            ok = true;
        } finally {
            if (!ok) {
                operations = EMPTY_FOLD_OPERTAION_IMPL_ARRAY;
            }
        }

        // Init managers under a local transaction
        FoldHierarchyTransactionImpl transaction = openTransaction();
        ok = false;
        try {
            // Remove all original folds - pass array of all blocked folds
            Fold[] allBlocked = new Fold[blocked2block.size()];
            blocked2block.keySet().toArray(allBlocked);
            transaction.removeAllFolds(allBlocked);

            // Init folds in all fold managers
            // Go from the manager with highest priority (index 0)
            for (int i = 0; i < factoryListLength; i++) {
                operations[i].initFolds(transaction);
            }
            ok = true; // inited successfully
        } finally {
            if (!ok) {
                // TODO - remove folds under root fold
                operations = EMPTY_FOLD_OPERTAION_IMPL_ARRAY;
            }
            transaction.commit();
        }
    }
    
    private void startComponentChangesListening() {
        if (componentChangesListener == null) {
            // Start listening on component changes
            componentChangesListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    String propName = evt.getPropertyName();
                    if ("document".equals(propName)) { //NOI18N
                        foldingEnabled = getFoldingEnabledSetting();
                        rebuild();
                    } else if (PROPERTY_FOLDING_ENABLED.equals(propName)) {
                        foldingEnabledSettingChange();
                    }
                }
            };

            // Start listening on the component.
            // As the hierarchy instance is stored as a property of the component
            // (and in fact the spi and the reference to the listener as well)
            // the listener does not need to be removed
            getComponent().addPropertyChangeListener(componentChangesListener);
        }
    }
    
    public void insertUpdate(DocumentEvent evt) {
        lock();
        try {
            FoldHierarchyTransactionImpl transaction = openTransaction();
            try {
                transaction.insertUpdate(evt);

                int operationsLength = operations.length;
                for (int i = 0; i < operationsLength; i++) {
                    operations[i].insertUpdate(evt, transaction);
                }
            } finally {
                transaction.commit();
            }
        } finally {
            unlock();
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        lock();
        try {
            FoldHierarchyTransactionImpl transaction = openTransaction();
            try {
                transaction.removeUpdate(evt);

                int operationsLength = operations.length;
                for (int i = 0; i < operationsLength; i++) {
                    operations[i].removeUpdate(evt, transaction);
                }
            } finally {
                transaction.commit();
            }
        } finally {
            unlock();
        }
    }

    public void changedUpdate(DocumentEvent evt) {
        lock();
        try {
            FoldHierarchyTransactionImpl transaction = openTransaction();
            try {
                transaction.changedUpdate(evt);
                
                int operationsLength = operations.length;
                for (int i = 0; i < operationsLength; i++) {
                    operations[i].changedUpdate(evt, transaction);
                }
            } finally {
                transaction.commit();
            }
        } finally {
            unlock();
        }
    }

    private boolean getFoldingEnabledSetting() {
        Boolean b = (Boolean)component.getClientProperty(PROPERTY_FOLDING_ENABLED);
        return (b != null) ? b.booleanValue() : true;
    }
    
    public void foldingEnabledSettingChange() {
        boolean origFoldingEnabled = foldingEnabled;
        foldingEnabled = getFoldingEnabledSetting();
        if (origFoldingEnabled != foldingEnabled) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    rebuild();
                }
            });
        }
    }
    
    /**
     * Check the internal consistency of the hierarchy
     * and its contained folds. This is useful for testing purposes.
     *
     * @throws IllegalStateException in case an inconsistency is found.
     */
    public void checkConsistency() {
        try {
            checkFoldConsistency(getRootFold());
        } catch (RuntimeException e) {
            /*DEBUG*/System.err.println("FOLD HIERARCHY INCONSISTENCY FOUND\n" + this); // NOI18N
            throw e; // rethrow the exception
        }
    }
    
    private static void checkFoldConsistency(Fold fold) {
        int startOffset = fold.getStartOffset();
        int endOffset = fold.getEndOffset();
        int lastEndOffset = startOffset;
        
        for (int i = 0; i < fold.getFoldCount(); i++) {
            Fold child = fold.getFold(i);
            if (child.getParent() != fold) {
                throw new IllegalStateException("Wrong parent of child=" // NOI18N
                    + child + ": " + child.getParent() // NOI18N
                    + " != " + fold); // NOI18N
            }
            int foldIndex = fold.getFoldIndex(child);
            if (foldIndex != i) {
                throw new IllegalStateException("Fold index " + foldIndex // NOI18N
                    + " instead of " + i); // NOI18N
            }
            
            int childStartOffset = child.getStartOffset();
            int childEndOffset = child.getEndOffset();
            if (childStartOffset < lastEndOffset) {
                throw new IllegalStateException("childStartOffset=" + childStartOffset // NOI18N
                    + " < lastEndOffset=" + lastEndOffset); // NOI18N
            }
            if (childStartOffset > childEndOffset) {
                throw new IllegalStateException("childStartOffset=" + childStartOffset // NOI18N
                    + " > childEndOffset=" + childEndOffset); // NOI18N
            }
            lastEndOffset = childEndOffset;
            
            checkFoldConsistency(child);
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("component="); // NOI18N
        sb.append(System.identityHashCode(getComponent()));
        sb.append('\n');

        // Append info about root folds
        sb.append(FoldUtilitiesImpl.foldToStringChildren(hierarchy.getRootFold(), 0));
        sb.append('\n');
        
        // Append info about blocked folds
        if (blocked2block != null) {
            sb.append("BLOCKED\n"); // NOI18N
            for (Iterator it = blocked2block.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                sb.append("    "); // NOI18N
                sb.append(entry.getKey());
                sb.append(" blocked-by "); // NOI18N
                sb.append(entry.getValue());
                sb.append('\n');
            }
        }
        
        // Append info about blockers
        if (block2blockedSet != null) {
            sb.append("BLOCKERS\n"); // NOI18N
            for (Iterator it = block2blockedSet.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                sb.append("    "); // NOI18N
                sb.append(entry.getKey());
                sb.append('\n');
                Set blockedSet = (Set)entry.getValue();
                for (Iterator it2 = blockedSet.iterator(); it2.hasNext();) {
                    sb.append("        blocks "); // NOI18N
                    sb.append(it2.next());
                    sb.append('\n');
                }
            }
        }
        
        int operationsLength = operations.length;
        if (operationsLength > 0) {
            sb.append("Fold Managers\n"); // NOI18N
            for (int i = 0; i < operationsLength; i++) {
                sb.append("FOLD MANAGER ["); // NOI18N
                sb.append(i);
                sb.append("]:\n"); // NOI18N
                sb.append(operations[i].getManager());
                sb.append("\n"); // NOI18N
            }
        }
        
        return sb.toString();
    }

}
