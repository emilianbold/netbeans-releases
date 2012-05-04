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
    
    private void setState(EntrySupportLazyState s) {
        assert Thread.holdsLock(LOCK);
        state = s;
    }

    public boolean checkInit() {
        if (state.isInited()) {
            return true;
        }
        boolean doInit = false;
        synchronized (LOCK) {
            if (!state.isInitInProgress()) {
                doInit = true;
                setState(state.changeProgress(true).changeThread(Thread.currentThread()));
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
                synchronized (LOCK) {
                    class Notify implements Runnable {
                        @Override
                        public void run() {
                            synchronized (LOCK) {
                                setState(state.changeThread(null));
                                LOCK.notifyAll();
                            }
                        }
                    }
                    Notify notify = new Notify();
                    setState(state.changeInited(true));
                    if (Children.MUTEX.isReadAccess()) {
                        Children.MUTEX.postWriteRequest(notify);
                    } else {
                        notify.run();
                    }
                }
            }
        } else {
            if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess() || (state.initThread() == Thread.currentThread())) {
                if (LOG_ENABLED) {
                    LOGGER.log(Level.FINER, "Cannot wait for finished initialization " + this + " on " + Thread.currentThread() + " read access: " + Children.MUTEX.isReadAccess() + " write access: " + Children.MUTEX.isWriteAccess() + " initThread: " + state.initThread());
                }
                // we cannot wait
                notifySetEntries();
                return false;
            }
            // otherwise we can wait
            synchronized (LOCK) {
                while (state.initThread() != null) {
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
                        for (Entry entry : notNull(state.getVisibleEntries())) {
                            EntryInfo info = state.getEntryToInfo().get(entry);
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
                        setState(state.changeInited(false).changeThread(null).changeProgress(false));
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
                List<Entry> e = notNull(state.getVisibleEntries());
                if (index >= e.size()) {
                    return node;
                }
                Entry entry = e.get(index);
                EntryInfo info = state.getEntryToInfo().get(entry);
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
                List<Entry> e = notNull(state.getVisibleEntries());
                List<Node> toReturn = new ArrayList<Node>(e.size());
                for (Entry entry : e) {
                    EntryInfo info = state.getEntryToInfo().get(entry);
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
        if (!state.isInited()) {
            return null;
        }
        List<Node> created = new ArrayList<Node>();
        try {
            Children.PR.enterReadAccess();
            for (Entry entry : notNull(state.getVisibleEntries())) {
                EntryInfo info = state.getEntryToInfo().get(entry);
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
            return notNull(state.getVisibleEntries()).size();
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    @Override
    public boolean isInitialized() {
        return state.isInited();
    }

    Entry entryForNode(Node key) {
        for (Map.Entry<Entry, EntryInfo> entry : state.getEntryToInfo().entrySet()) {
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
        if (!state.isInited()) {
            return;
        }
        EntryInfo info = state.getEntryToInfo().get(entry);
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
        Map<Entry,EntryInfo> new2Info = null;
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
            new2Info = new HashMap<Entry, EntryInfo>(state.getEntryToInfo());
            new2Info.put(entry, info);
        }
        if (newIsDummy) {
            return;
        }
        // recompute indexes
        int index = 0;
        info.setIndex(-1);
        List<Entry> arr = new ArrayList<Entry>();
        for (Entry tmpEntry : state.getEntries()) {
            EntryInfo tmpInfo = state.getEntryToInfo().get(tmpEntry);
            if (tmpInfo.isHidden()) {
                continue;
            }
            tmpInfo.setIndex(index++);
            arr.add(tmpEntry);
        }
        synchronized (LOCK) {
            setState(state.changeEntries(null, arr, new2Info));
        }
        fireSubNodesChangeIdx(true, new int[]{info.getIndex()}, entry, createSnapshot(), null);
    }

    void notifySetEntries() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("notifySetEntries() " + this); // NOI18N
        }
        synchronized (LOCK) {
            setState(state.changeMustNotify(true));
        }
    }

    @Override
    void setEntries(Collection<? extends Entry> newEntries, boolean noCheck) {
        assert Children.MUTEX.isWriteAccess();
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (LOG_ENABLED) {
            LOGGER.finer("setEntries(): " + this); // NOI18N
            LOGGER.finer("    inited: " + state.isInited()); // NOI18N
            LOGGER.finer("    mustNotifySetEnties: " + state.isMustNotify()); // NOI18N
            LOGGER.finer("    newEntries size: " + newEntries.size() + " data:" + newEntries); // NOI18N
            LOGGER.finer("    entries size: " + state.getEntries().size() + " data:" + state.getEntries()); // NOI18N
            LOGGER.finer("    visibleEntries size: " + notNull(state.getVisibleEntries()).size() + " data:" + state.getVisibleEntries()); // NOI18N
            LOGGER.finer("    entryToInfo size: " + state.getEntryToInfo().size()); // NOI18N
        }
        int entriesSize = 0;
        int entryToInfoSize = 0;
        assert (entriesSize = state.getEntries().size()) >= 0;
        assert (entryToInfoSize = state.getEntryToInfo().size()) >= 0;
        assert state.getEntries().size() == state.getEntryToInfo().size() : "Entries: " + state.getEntries().size() + "; vis. entries: " + notNull(state.getVisibleEntries()).size() + "; Infos: " + state.getEntryToInfo().size() + "; entriesSize: " + entriesSize + "; entryToInfoSize: " + entryToInfoSize + dumpEntriesInfos(state.getEntries(), state.getEntryToInfo()); // NOI18N
        if (!state.isMustNotify() && !state.isInited()) {
            ArrayList<Entry> newStateEntries = new ArrayList<Entry>(newEntries);
            ArrayList<Entry> newStateVisibleEntries = new ArrayList<Entry>(newEntries);
            Map<Entry, EntryInfo> newState2Info = new HashMap<Entry, EntryInfo>();
            {
                Map<Entry, EntryInfo> oldState2Info = state.getEntryToInfo();
                for (Entry entry : newEntries) {
                    final EntryInfo prev = oldState2Info.get(entry);
                    if (prev != null) {
                        newState2Info.put(entry, prev);
                    }
                }
            }
            for (int i = 0; i < newStateEntries.size(); i++) {
                Entry entry = newStateEntries.get(i);
                EntryInfo info = newState2Info.get(entry);
                if (info == null) {
                    info = new EntryInfo(entry);
                    newState2Info.put(entry, info);
                }
                info.setIndex(i);
            }
            synchronized (LOCK) {
                setState(state.changeEntries(newStateEntries, newStateVisibleEntries, newState2Info));
            }
            return;
        }
        Set<Entry> entriesToRemove = new HashSet<Entry>(state.getEntries());
        entriesToRemove.removeAll(newEntries);
        if (!entriesToRemove.isEmpty()) {
            removeEntries(entriesToRemove, null, null, false, false);
        }
        // change the order of entries, notifies
        // it and again brings children to up-to-date state, recomputes indexes
        Collection<Entry> toAdd = updateOrder(newEntries);
        if (!toAdd.isEmpty()) {
            ArrayList<Entry> newStateEntries = new ArrayList<Entry>(newEntries);
            int[] idxs = new int[toAdd.size()];
            int addIdx = 0;
            int inx = 0;
            boolean createNodes = toAdd.size() == 2 && prefetchCount > 0;
            ArrayList<Entry> newStateVisibleEntries = new ArrayList<Entry>();
            Map<Entry, EntryInfo> newState2Info = new HashMap<Entry, EntryInfo>(state.getEntryToInfo());
            for (int i = 0; i < newStateEntries.size(); i++) {
                Entry entry = newStateEntries.get(i);
                EntryInfo info = newState2Info.get(entry);
                if (info == null) {
                    info = new EntryInfo(entry);
                    newState2Info.put(entry, info);
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
                newStateVisibleEntries.add(entry);
            }
            synchronized (LOCK) {
                setState(state.changeEntries(newStateEntries, newStateVisibleEntries, newState2Info));
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
        int[] perm = new int[state.getVisibleEntries().size()];
        int currentPos = 0;
        int permSize = 0;
        List<Entry> reorderedEntries = null;
        List<Entry> newVisible = null;
        for (Entry entry : newEntries) {
            EntryInfo info = state.getEntryToInfo().get(entry);
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
            synchronized (LOCK) {
                setState(state.changeEntries(reorderedEntries, newVisible, null));
            }
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
            EntryInfo info = state.getEntryToInfo().get(entry);
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

    static <T> List<T> notNull(List<T> it) {
        if (it == null) {
            return Collections.emptyList();
        } else {
            return it;
        }
    }

    static String dumpEntriesInfos(List<Entry> entries, Map<Entry, EntryInfo> entryToInfo) {
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
        return state.getEntries();
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
        List<Entry> previousEntries = state.getVisibleEntries();
        Map<Entry, EntryInfo> previousInfos = null;
        Map<Entry, EntryInfo> new2Infos = null;
        List<Entry> newEntries = justHide ? null : new ArrayList<Entry>();
        Node[] removedNodes = null;
        ArrayList<Entry> newStateVisibleEntries = new ArrayList<Entry>();
        Map<Entry, EntryInfo> oldState2Info = state.getEntryToInfo();
        for (Entry entry : state.getEntries()) {
            EntryInfo info = oldState2Info.get(entry);
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
                        if (new2Infos == null) {
                            new2Infos = new HashMap<Entry, EntryInfo>(oldState2Info);
                        }
                        new2Infos.remove(entry);
                    }
                    continue;
                }
                idxs[removedIdx++] = info.getIndex();
                if (previousInfos == null) {
                    previousInfos = new HashMap<Entry, EntryInfo>(oldState2Info);
                }
                Node node = info.currentNode();
                if (!info.isHidden() && node != null && !isDummyNode(node)) {
                    if (removedNodes == null) {
                        removedNodes = new Node[expectedSize];
                    }
                    removedNodes[removedNodesIdx++] = node;
                }
                if (new2Infos == null) {
                    new2Infos = new HashMap<Entry, EntryInfo>(oldState2Info);
                }
                if (justHide) {
                    EntryInfo dup = newEntryInfo != null ? newEntryInfo : info.duplicate(null);
                    new2Infos.put(info.entry, dup);
                    // mark as hidden
                    dup.setIndex(-2);
                } else {
                    new2Infos.remove(entry);
                }
            } else {
                if (!info.isHidden()) {
                    newStateVisibleEntries.add(info.entry);
                    info.setIndex(index++);
                }
                if (!justHide) {
                    newEntries.add(info.entry);
                }
            }
        }
        if (!justHide) {
            //state.entries = newEntries;
        }
        synchronized (LOCK) {
            setState(state.changeEntries(newEntries, newStateVisibleEntries, new2Infos));
        }
        if (removedIdx == 0) {
            return;
        }
        if (removedIdx < idxs.length) {
            idxs = (int[]) resizeArray(idxs, removedIdx);
        }
        List<Node> curSnapshot = createSnapshot(state.getVisibleEntries(), new HashMap<Entry, EntryInfo>(state.getEntryToInfo()), delayed);
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
        return createSnapshot(state.getVisibleEntries(), new HashMap<Entry, EntryInfo>(state.getEntryToInfo()), false);
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
            assert entries.size() <= entryToInfo.size();
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
