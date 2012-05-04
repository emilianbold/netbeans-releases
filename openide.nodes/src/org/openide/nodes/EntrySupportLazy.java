/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.nodes;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Children.Entry;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class EntrySupportLazy extends EntrySupport {
    private static final int prefetchCount = Math.max(Integer.getInteger("org.openide.explorer.VisualizerNode.prefetchCount", 50), 0); // NOI18N
    private static final Logger LOGGER = Logger.getLogger(EntrySupportLazy.class.getName());
        
    /** represents state of this object. The state itself should not 
     * mutate, the reference to different states, however may -
     * in future.
     */
    private EntrySupportLazyState state = new EntrySupportLazyState();
    
    //private static final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);

    public EntrySupportLazy(Children ch) {
        super(ch);
    }
    /** lock to guard creation of snapshots */
    protected final Object LOCK = new Object();
    /** @GuardedBy("LOCK")*/
    private int snapshotCount;

    public boolean checkInit() {
        if (state.inited) {
            return true;
        }
        boolean doInit = false;
        synchronized (LOCK) {
            if (!state.initInProgress) {
                doInit = true;
                state.initInProgress = true;
                state.initThread = Thread.currentThread();
            }
        }
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (doInit) {
            if (LOG_ENABLED) {
                LOGGER.finer("Initialize " + this + " on " + Thread.currentThread());
                LOGGER.finer("    callAddNotify()"); // NOI18N
            }
            try {
                children.callAddNotify();
            } finally {
                class Notify implements Runnable {

                    public void run() {
                        synchronized (LOCK) {
                            state.initThread = null;
                            LOCK.notifyAll();
                        }
                    }
                }
                Notify notify = new Notify();
                state.inited = true;
                if (Children.MUTEX.isReadAccess()) {
                    Children.MUTEX.postWriteRequest(notify);
                } else {
                    notify.run();
                }
            }
        } else {
            if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess() || (state.initThread == Thread.currentThread())) {
                if (LOG_ENABLED) {
                    LOGGER.log(Level.FINER, "Cannot wait for finished initialization " + this + " on " + Thread.currentThread() + " read access: " + Children.MUTEX.isReadAccess() + " write access: " + Children.MUTEX.isWriteAccess() + " initThread: " + state.initThread);
                }
                // we cannot wait
                notifySetEntries();
                return false;
            }
            // otherwise we can wait
            synchronized (LOCK) {
                while (state.initThread != null) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        return true;
    }

    final int getSnapshotCount() {
        assert Thread.holdsLock(LOCK);
        return snapshotCount;
    }

    final void incrementCount() {
        assert Thread.holdsLock(LOCK);
        snapshotCount++;
    }

    final void decrementCount() {
        assert Thread.holdsLock(LOCK);
        snapshotCount++;
    }

    @Override
    List<Node> snapshot() {
        checkInit();
        try {
            Children.PR.enterReadAccess();
            return createSnapshot();
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    final void registerNode(int delta, EntryInfo who) {
        if (delta == -1) {
            try {
                Children.PR.enterWriteAccess();
                boolean zero = false;
                LOGGER.finer("register node"); // NOI18N
                synchronized (EntrySupportLazy.this.LOCK) {
                    int cnt = 0;
                    boolean found = false;
                    cnt += getSnapshotCount();
                    if (cnt == 0) {
                        for (Entry entry : notNull(state.visibleEntries)) {
                            EntryInfo info = state.entryToInfo.get(entry);
                            if (info.currentNode() != null) {
                                cnt++;
                                break;
                            }
                            if (info == who) {
                                found = true;
                            }
                        }
                    }
                    zero = cnt == 0 && (found || who == null);
                    if (zero) {
                        state.inited = false;
                        state.initThread = null;
                        state.initInProgress = false;
                        if (children.getEntrySupport() == this) {
                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("callRemoveNotify() " + this); // NOI18N
                            }
                            children.callRemoveNotify();
                        }
                    }
                }
            } finally {
                Children.PR.exitWriteAccess();
            }
        }
    }

    @Override
    public Node getNodeAt(int index) {
        if (!checkInit()) {
            return null;
        }
        Node node = null;
        while (true) {
            try {
                Children.PR.enterReadAccess();
                List<Entry> e = notNull(state.visibleEntries);
                if (index >= e.size()) {
                    return node;
                }
                Entry entry = e.get(index);
                EntryInfo info = state.entryToInfo.get(entry);
                node = info.getNode();
                if (!isDummyNode(node)) {
                    return node;
                }
                hideEmpty(null, entry);
            } finally {
                Children.PR.exitReadAccess();
            }
            if (Children.MUTEX.isReadAccess()) {
                return node;
            }
        }
    }

    @Override
    public Node[] getNodes(boolean optimalResult) {
        if (!checkInit()) {
            return new Node[0];
        }
        Node holder = null;
        if (optimalResult) {
            holder = children.findChild(null);
        }
        Children.LOG.log(Level.FINEST, "findChild returns: {0}", holder); // NOI18N
        Children.LOG.log(Level.FINEST, "after findChild: {0}", optimalResult); // NOI18N
        while (true) {
            Set<Entry> invalidEntries = null;
            Node[] tmpNodes = null;
            try {
                Children.PR.enterReadAccess();
                List<Entry> e = notNull(state.visibleEntries);
                List<Node> toReturn = new ArrayList<Node>(e.size());
                for (Entry entry : e) {
                    EntryInfo info = state.entryToInfo.get(entry);
                    assert !info.isHidden();
                    Node node = info.getNode();
                    if (isDummyNode(node)) {
                        if (invalidEntries == null) {
                            invalidEntries = new HashSet<Entry>();
                        }
                        invalidEntries.add(entry);
                    }
                    toReturn.add(node);
                }
                tmpNodes = toReturn.toArray(new Node[0]);
                if (invalidEntries == null) {
                    return tmpNodes;
                }
                hideEmpty(invalidEntries, null);
            } finally {
                Children.PR.exitReadAccess();
            }
            if (Children.MUTEX.isReadAccess()) {
                return tmpNodes;
            }
        }
    }

    @Override
    public Node[] testNodes() {
        if (!state.inited) {
            return null;
        }
        List<Node> created = new ArrayList<Node>();
        try {
            Children.PR.enterReadAccess();
            for (Entry entry : notNull(state.visibleEntries)) {
                EntryInfo info = state.entryToInfo.get(entry);
                Node node = info.currentNode();
                if (node != null) {
                    created.add(node);
                }
            }
        } finally {
            Children.PR.exitReadAccess();
        }
        return created.isEmpty() ? null : created.toArray(new Node[created.size()]);
    }

    @Override
    public int getNodesCount(boolean optimalResult) {
        checkInit();
        try {
            Children.PR.enterReadAccess();
            return notNull(state.visibleEntries).size();
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    @Override
    public boolean isInitialized() {
        return state.inited;
    }

    Entry entryForNode(Node key) {
        for (Map.Entry<Entry, EntryInfo> entry : state.entryToInfo.entrySet()) {
            if (entry.getValue().currentNode() == key) {
                return entry.getKey();
            }
        }
        return null;
    }

    static boolean isDummyNode(Node node) {
        return node.getClass() == DummyNode.class;
    }

    @Override
    void refreshEntry(Entry entry) {
        assert Children.MUTEX.isWriteAccess();
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (LOG_ENABLED) {
            LOGGER.finer("refreshEntry() " + this);
            LOGGER.finer("    entry: " + entry); // NOI18N
        }
        if (!state.inited) {
            return;
        }
        EntryInfo info = state.entryToInfo.get(entry);
        if (info == null) {
            if (LOG_ENABLED) {
                LOGGER.finer("    no such entry: " + entry); // NOI18N
            }
            // no such entry
            return;
        }
        Node oldNode = info.currentNode();
        EntryInfo newInfo = null;
        Node newNode = null;
        if (info.isHidden()) {
            newNode = info.getNode(true, null);
        } else {
            newInfo = info.duplicate(null);
            newNode = newInfo.getNode(true, null);
        }
        boolean newIsDummy = isDummyNode(newNode);
        if (newIsDummy && info.isHidden()) {
            // dummy is already hidden
            return;
        }
        if (newNode.equals(oldNode)) {
            // same node =>
            return;
        }
        if (!info.isHidden() || newIsDummy) {
            removeEntries(null, entry, newInfo, true, true);
        }
        if (newInfo != null) {
            info = newInfo;
            state.entryToInfo.put(entry, info);
        }
        if (newIsDummy) {
            return;
        }
        // recompute indexes
        int index = 0;
        info.setIndex(-1);
        List<Entry> arr = new ArrayList<Entry>();
        for (Entry tmpEntry : state.entries) {
            EntryInfo tmpInfo = state.entryToInfo.get(tmpEntry);
            if (tmpInfo.isHidden()) {
                continue;
            }
            tmpInfo.setIndex(index++);
            arr.add(tmpEntry);
        }
        state.visibleEntries = arr;
        fireSubNodesChangeIdx(true, new int[]{info.getIndex()}, entry, createSnapshot(), null);
    }

    void notifySetEntries() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("notifySetEntries() " + this); // NOI18N
        }
        state.mustNotifySetEntries = true;
    }

    @Override
    void setEntries(Collection<? extends Entry> newEntries, boolean noCheck) {
        assert Children.MUTEX.isWriteAccess();
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (LOG_ENABLED) {
            LOGGER.finer("setEntries(): " + this); // NOI18N
            LOGGER.finer("    inited: " + state.inited); // NOI18N
            LOGGER.finer("    mustNotifySetEnties: " + state.mustNotifySetEntries); // NOI18N
            LOGGER.finer("    newEntries size: " + newEntries.size() + " data:" + newEntries); // NOI18N
            LOGGER.finer("    entries size: " + state.entries.size() + " data:" + state.entries); // NOI18N
            LOGGER.finer("    visibleEntries size: " + notNull(state.visibleEntries).size() + " data:" + state.visibleEntries); // NOI18N
            LOGGER.finer("    entryToInfo size: " + state.entryToInfo.size()); // NOI18N
        }
        int entriesSize = 0;
        int entryToInfoSize = 0;
        assert (entriesSize = state.entries.size()) >= 0;
        assert (entryToInfoSize = state.entryToInfo.size()) >= 0;
        assert state.entries.size() == state.entryToInfo.size() : "Entries: " + state.entries.size() + "; vis. entries: " + notNull(state.visibleEntries).size() + "; Infos: " + state.entryToInfo.size() + "; entriesSize: " + entriesSize + "; entryToInfoSize: " + entryToInfoSize + dumpEntriesInfos(state.entries, state.entryToInfo); // NOI18N
        if (!state.mustNotifySetEntries && !state.inited) {
            state.entries = new ArrayList<Entry>(newEntries);
            state.visibleEntries = new ArrayList<Entry>(newEntries);
            state.entryToInfo.keySet().retainAll(state.entries);
            for (int i = 0; i < state.entries.size(); i++) {
                Entry entry = state.entries.get(i);
                EntryInfo info = state.entryToInfo.get(entry);
                if (info == null) {
                    info = new EntryInfo(entry);
                    state.entryToInfo.put(entry, info);
                }
                info.setIndex(i);
            }
            return;
        }
        Set<Entry> entriesToRemove = new HashSet<Entry>(state.entries);
        entriesToRemove.removeAll(newEntries);
        if (!entriesToRemove.isEmpty()) {
            removeEntries(entriesToRemove, null, null, false, false);
        }
        // change the order of entries, notifies
        // it and again brings children to up-to-date state, recomputes indexes
        Collection<Entry> toAdd = updateOrder(newEntries);
        if (!toAdd.isEmpty()) {
            state.entries = new ArrayList<Entry>(newEntries);
            int[] idxs = new int[toAdd.size()];
            int addIdx = 0;
            int inx = 0;
            boolean createNodes = toAdd.size() == 2 && prefetchCount > 0;
            state.visibleEntries = new ArrayList<Entry>();
            for (int i = 0; i < state.entries.size(); i++) {
                Entry entry = state.entries.get(i);
                EntryInfo info = state.entryToInfo.get(entry);
                if (info == null) {
                    info = new EntryInfo(entry);
                    state.entryToInfo.put(entry, info);
                    if (createNodes) {
                        Node n = info.getNode();
                        if (isDummyNode(n)) {
                            // mark as hidden
                            info.setIndex(-2);
                            continue;
                        }
                    }
                    idxs[addIdx++] = inx;
                }
                if (info.isHidden()) {
                    continue;
                }
                info.setIndex(inx++);
                state.visibleEntries.add(entry);
            }
            if (addIdx == 0) {
                return;
            }
            if (idxs.length != addIdx) {
                int[] tmp = new int[addIdx];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = idxs[i];
                }
                idxs = tmp;
            }
            fireSubNodesChangeIdx(true, idxs, null, createSnapshot(), null);
        }
    }

    /** Updates the order of entries.
     * @param current current state of nodes
     * @param entries new set of entries
     * @return list of infos that should be added
     */
    private List<Entry> updateOrder(Collection<? extends Entry> newEntries) {
        assert Children.MUTEX.isWriteAccess();
        List<Entry> toAdd = new LinkedList<Entry>();
        int[] perm = new int[state.visibleEntries.size()];
        int currentPos = 0;
        int permSize = 0;
        List<Entry> reorderedEntries = null;
        List<Entry> newVisible = null;
        for (Entry entry : newEntries) {
            EntryInfo info = state.entryToInfo.get(entry);
            if (info == null) {
                // this entry has to be added
                toAdd.add(entry);
            } else {
                if (reorderedEntries == null) {
                    reorderedEntries = new LinkedList<Entry>();
                    newVisible = new ArrayList<Entry>();
                }
                reorderedEntries.add(entry);
                if (info.isHidden()) {
                    continue;
                }
                newVisible.add(entry);
                int oldPos = info.getIndex();
                // already there => test if it should not be reordered
                if (currentPos != oldPos) {
                    info.setIndex(currentPos);
                    perm[oldPos] = 1 + currentPos;
                    permSize++;
                }
                currentPos++;
            }
        }
        if (permSize > 0) {
            // now the perm array contains numbers 1 to ... and
            // 0 one places where no permutation occures =>
            // decrease numbers, replace zeros
            for (int i = 0; i < perm.length; i++) {
                if (perm[i] == 0) {
                    // fixed point
                    perm[i] = i;
                } else {
                    // decrease
                    perm[i]--;
                }
            }
            // reorderedEntries are not null
            state.entries = reorderedEntries;
            state.visibleEntries = newVisible;
            Node p = children.parent;
            if (p != null) {
                p.fireReorderChange(perm);
            }
        }
        return toAdd;
    }

    Node getNode(Entry entry) {
        checkInit();
        try {
            Children.PR.enterReadAccess();
            EntryInfo info = state.entryToInfo.get(entry);
            if (info == null) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("getNode() " + this);
                    LOGGER.finer("    no such entry: " + entry); // NOI18N
                }
                return null;
            }
            Node node = info.getNode();
            return isDummyNode(node) ? null : node;
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    /** @param added added or removed
     *  @param indices list of integers with indexes that changed
     */
    protected void fireSubNodesChangeIdx(boolean added, int[] idxs, Entry sourceEntry, List<Node> current, List<Node> previous) {
        if (children.parent != null && children.getEntrySupport() == this) {
            children.parent.fireSubNodesChangeIdx(added, idxs, sourceEntry, current, previous);
        }
    }

    private static <T> List<T> notNull(List<T> it) {
        if (it == null) {
            return Collections.emptyList();
        } else {
            return it;
        }
    }

    private static String dumpEntriesInfos(List<Entry> entries, Map<Entry, EntryInfo> entryToInfo) {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for (Entry entry : entries) {
            sb.append("\n").append(++cnt).append(" entry ").append(entry).append(" -> ").append(entryToInfo.get(entry)); // NOI18N
        }
        sb.append("\n\n"); // NOI18N
        for (Map.Entry<Entry, EntryInfo> e : entryToInfo.entrySet()) {
            if (entries.contains(e.getKey())) {
                sb.append("\n").append(" contained ").append(e.getValue()); // NOI18N
            } else {
                sb.append("\n").append(" missing   ").append(e.getValue()).append(" for ").append(e.getKey()); // NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    protected List<Entry> getEntries() {
        return new ArrayList<Entry>(state.entries);
    }

    /** holds node for entry; 1:1 mapping */
    final class EntryInfo {

        /** corresponding entry */
        final Entry entry;
        /** cached node for this entry */
        private NodeRef refNode;
        /** my index in list of entries */
        private int index = -1;

        public EntryInfo(Entry entry) {
            this.entry = entry;
        }

        final EntryInfo duplicate(Node node) {
            EntryInfo ei = new EntryInfo(entry);
            ei.index = index;
            ei.refNode = node != null ? new NodeRef(node, ei) : refNode;
            return ei;
        }

        final EntrySupportLazy lazy() {
            return EntrySupportLazy.this;
        }

        /** Gets or computes the nodes. It holds them using weak reference
         * so they can get garbage collected.
         */
        public final Node getNode() {
            return getNode(false, null);
        }
        private Thread creatingNodeThread = null;

        public final Node getNode(boolean refresh, Object source) {
            while (true) {
                Node node = null;
                boolean creating = false;
                synchronized (LOCK) {
                    if (refresh) {
                        refNode = null;
                    }
                    if (refNode != null) {
                        node = refNode.get();
                        if (node != null) {
                            return node;
                        }
                    }
                    if (creatingNodeThread != null) {
                        if (creatingNodeThread == Thread.currentThread()) {
                            return new DummyNode();
                        }
                        try {
                            LOCK.wait();
                        } catch (InterruptedException ex) {
                        }
                    } else {
                        creatingNodeThread = Thread.currentThread();
                        creating = true;
                    }
                }
                Collection<Node> nodes = Collections.emptyList();
                if (creating) {
                    try {
                        nodes = entry.nodes(source);
                    } catch (RuntimeException ex) {
                        NodeOp.warning(ex);
                    }
                }
                synchronized (LOCK) {
                    if (!creating) {
                        if (refNode != null) {
                            node = refNode.get();
                            if (node != null) {
                                return node;
                            }
                        }
                        // node created by other thread was GCed meanwhile, try once again
                        continue;
                    }
                    if (nodes.size() == 0) {
                        node = new DummyNode();
                    } else {
                        if (nodes.size() > 1) {
                            LOGGER.fine("Number of nodes for Entry: " + entry + " is " + nodes.size() + " instead of 1"); // NOI18N
                        }
                        node = nodes.iterator().next();
                    }
                    refNode = new NodeRef(node, this);
                    if (creating) {
                        creatingNodeThread = null;
                        LOCK.notifyAll();
                    }
                }
                // assign node to the new children
                node.assignTo(children, -1);
                node.fireParentNodeChange(null, children.parent);
                return node;
            }
        }

        /** extract current node (if was already created) */
        Node currentNode() {
            synchronized (LOCK) {
                return refNode == null ? null : refNode.get();
            }
        }

        final boolean isHidden() {
            return this.index == -2;
        }

        /** Sets the index of the entry. */
        final void setIndex(int i) {
            this.index = i;
        }

        /** Get index. */
        final int getIndex() {
            assert index >= 0 : "When first asked for it has to be set: " + index; // NOI18N
            return index;
        }

        @Override
        public String toString() {
            return "EntryInfo for entry: " + entry + ", node: " + (refNode == null ? null : refNode.get()); // NOI18N
        }
    }

    private static final class NodeRef extends WeakReference<Node> implements Runnable {

        private final EntryInfo info;

        public NodeRef(Node node, EntryInfo info) {
            super(node, Utilities.activeReferenceQueue());
            info.lazy().registerNode(1, info);
            this.info = info;
        }

        @Override
        public void run() {
            info.lazy().registerNode(-1, info);
        }
    }

    /** Dummy node class for entries without any node */
    static class DummyNode extends AbstractNode {
        public DummyNode() {
            super(Children.LEAF);
        }
    }

    void hideEmpty(final Set<Entry> entries, final Entry entry) {
        Children.MUTEX.postWriteRequest(new Runnable() {
            public void run() {
                removeEntries(entries, entry, null, true, true);
            }
        });
    }

    private void removeEntries(Set<Entry> entriesToRemove, Entry entryToRemove, EntryInfo newEntryInfo, boolean justHide, boolean delayed) {
        assert Children.MUTEX.isWriteAccess();
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (LOG_ENABLED) {
            LOGGER.finer("removeEntries(): " + this); // NOI18N
            LOGGER.finer("    entriesToRemove: " + entriesToRemove); // NOI18N
            LOGGER.finer("    entryToRemove: " + entryToRemove); // NOI18N
            LOGGER.finer("    newEntryInfo: " + newEntryInfo); // NOI18N
            LOGGER.finer("    justHide: " + justHide); // NOI18N
            LOGGER.finer("    delayed: " + delayed); // NOI18N
        }
        int index = 0;
        int removedIdx = 0;
        int removedNodesIdx = 0;
        int expectedSize = entriesToRemove != null ? entriesToRemove.size() : 1;
        int[] idxs = new int[expectedSize];
        List<Entry> previousEntries = state.visibleEntries;
        Map<Entry, EntryInfo> previousInfos = null;
        List<Entry> newEntries = justHide ? null : new ArrayList<Entry>();
        Node[] removedNodes = null;
        state.visibleEntries = new ArrayList<Entry>();
        for (Entry entry : state.entries) {
            EntryInfo info = state.entryToInfo.get(entry);
            if (info == null) {
                continue;
            }
            boolean remove;
            if (entriesToRemove != null) {
                remove = entriesToRemove.remove(entry);
            } else {
                remove = entryToRemove.equals(entry);
            }
            if (remove) {
                if (info.isHidden()) {
                    if (!justHide) {
                        state.entryToInfo.remove(entry);
                    }
                    continue;
                }
                idxs[removedIdx++] = info.getIndex();
                if (previousInfos == null) {
                    previousInfos = new HashMap<Entry, EntryInfo>(state.entryToInfo);
                }
                Node node = info.currentNode();
                if (!info.isHidden() && node != null && !isDummyNode(node)) {
                    if (removedNodes == null) {
                        removedNodes = new Node[expectedSize];
                    }
                    removedNodes[removedNodesIdx++] = node;
                }
                if (justHide) {
                    EntryInfo dup = newEntryInfo != null ? newEntryInfo : info.duplicate(null);
                    state.entryToInfo.put(info.entry, dup);
                    // mark as hidden
                    dup.setIndex(-2);
                } else {
                    state.entryToInfo.remove(entry);
                }
            } else {
                if (!info.isHidden()) {
                    state.visibleEntries.add(info.entry);
                    info.setIndex(index++);
                }
                if (!justHide) {
                    newEntries.add(info.entry);
                }
            }
        }
        if (!justHide) {
            state.entries = newEntries;
        }
        if (removedIdx == 0) {
            return;
        }
        if (removedIdx < idxs.length) {
            idxs = (int[]) resizeArray(idxs, removedIdx);
        }
        List<Node> curSnapshot = createSnapshot(state.visibleEntries, new HashMap<Entry, EntryInfo>(state.entryToInfo), delayed);
        List<Node> prevSnapshot = createSnapshot(previousEntries, previousInfos, false);
        fireSubNodesChangeIdx(false, idxs, entryToRemove, curSnapshot, prevSnapshot);
        if (removedNodesIdx > 0) {
            if (removedNodesIdx < removedNodes.length) {
                removedNodes = (Node[]) resizeArray(removedNodes, removedNodesIdx);
            }
            if (children.parent != null) {
                for (Node node : removedNodes) {
                    node.deassignFrom(children);
                    node.fireParentNodeChange(children.parent, null);
                }
            }
            children.destroyNodes(removedNodes);
        }
    }

    private static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) {
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        }
        return newArray;
    }

    LazySnapshot createSnapshot() {
        return createSnapshot(state.visibleEntries, new HashMap<Entry, EntryInfo>(state.entryToInfo), false);
    }

    protected LazySnapshot createSnapshot(List<Entry> entries, Map<Entry, EntryInfo> e2i, boolean delayed) {
        synchronized (LOCK) {
            return delayed ? new DelayedLazySnapshot(entries, e2i) : new LazySnapshot(entries, e2i);
        }
    }

    class LazySnapshot extends AbstractList<Node> {

        final List<Entry> entries;
        final Map<Entry, EntryInfo> entryToInfo;

        public LazySnapshot(List<Entry> entries, Map<Entry, EntryInfo> e2i) {
            incrementCount();
            this.entries = entries;
            assert entries != null;
            this.entryToInfo = e2i != null ? e2i : Collections.<Entry, EntryInfo>emptyMap();
        }

        public Node get(int index) {
            Entry entry = entries.get(index);
            return get(entry);
        }

        Node get(Entry entry) {
            EntryInfo info = entryToInfo.get(entry);
            Node node = info.getNode();
            if (isDummyNode(node)) {
                // force new snapshot
                hideEmpty(null, entry);
            }
            return node;
        }

        @Override
        public String toString() {
            return entries.toString();
        }

        public int size() {
            return entries.size();
        }

        @Override
        protected void finalize() throws Throwable {
            boolean unregister = false;
            synchronized (LOCK) {
                decrementCount();
                if (getSnapshotCount() == 0) {
                    unregister = true;
                }
            }
            if (unregister) {
                registerNode(-1, null);
            }
        }
    }

    final class DelayedLazySnapshot extends LazySnapshot {

        public DelayedLazySnapshot(List<Entry> entries, Map<Entry, EntryInfo> e2i) {
            super(entries, e2i);
        }
    }
    
}
