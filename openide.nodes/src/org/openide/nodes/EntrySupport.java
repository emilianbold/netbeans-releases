/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Children.Entry;

/**
 *
 * @author t_h
 */
abstract class EntrySupport {

    /** children we are attached to */
    public final Children children;
    
    /** array of children Reference (ChildrenArray) */
    Reference<ChildrenArray> array = new WeakReference<ChildrenArray>(null);
    
    /** collection of all entries */
    protected List<? extends Entry> entries = Collections.emptyList();

    /** Creates a new instance of EntrySupport */
    protected EntrySupport(Children children) {
        this.children = children;
    }

    //
    // API methods to be called from Children
    //
    public abstract int getNodesCount();

    public abstract Node[] getNodes(boolean optimalResult);

    /** Getter for a node at given position. If node with such index
     * does not exists it should return null.*/
    public abstract Node getNodeAt(int index);

    public abstract Node[] testNodes();

    public abstract boolean isInitialized();
    
    abstract void setEntries(Collection<? extends Entry> entries);
    
    /** Access to copy of current entries.
     * @return copy of entries in the objects
     */
    protected final List<Entry> getEntries() {
        return new ArrayList<Entry>(this.entries);
    }
    
    /** Refreshes content of one entry. Updates the state of children appropriately. */
    abstract void refreshEntry(Entry entry);

        
    /** Default support that just fires changes directly to children and is suitable
     * for simple mappings.
     */
    static final class Default extends EntrySupport {

        /** mapping from entries to info about them */
        private Map<Entry, Info> map;
        private static final Object LOCK = new Object();
        private static final Logger LOG_GET_ARRAY = Logger.getLogger("org.openide.nodes.Children.getArray"); // NOI18N        
        private Thread initThread;


        public Default(Children ch) {
            super(ch);
        }

        public boolean isInitialized() {
            ChildrenArray arr = array.get();
            return (arr != null) && arr.isInitialized();
        }

        public final Node[] getNodes() {
            //Thread.dumpStack();
            //System.err.println(off + "getNodes: " + getNode ());
            boolean[] results = new boolean[2];

            for (;;) {
                results[1] = isInitialized();

                // initializes the ChildrenArray possibly calls 
                // addNotify if this is for the first time
                ChildrenArray array = getArray(results); // fils results[0]

                Node[] nodes;

                try {
                    Children.PR.enterReadAccess();
                    nodes = array.nodes();
                } finally {
                    Children.PR.exitReadAccess();
                }

                final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("  length     : " + (nodes == null ? "nodes is null" : nodes.length)); // NOI18N
                    LOG_GET_ARRAY.fine("  entries    : " + entries); // NOI18N
                    LOG_GET_ARRAY.fine("  init now   : " + isInitialized()); // NOI18N

                }
                // if not initialized that means that after
                // we computed the nodes, somebody changed them (as a
                // result of addNotify) => we have to compute them
                // again
                if (results[1]) {
                    // otherwise it is ok.
                    return nodes;
                }

                if (results[0]) {
                    // looks like the result cannot be computed, just give empty one
                    return (nodes == null) ? new Node[0] : nodes;
                }
            }
        }

        public Node[] getNodes(boolean optimalResult) {
            ChildrenArray hold;
            Node find;
            if (optimalResult) {
                final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("computing optimal result");// NOI18N

                }
                hold = getArray(null);
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("optimal result is here: " + hold);// NOI18N

                }
                find = children.findChild(null);
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("Find child got: " + find); // NOI18N

                }
            }

            return getNodes();
        }

        public final int getNodesCount() {
            return getNodes().length;
        }

        @Override
        public Node getNodeAt(int index) {
            Node[] nodes = getNodes();
            return index < nodes.length ? nodes[index] : null;
        }
        

        /** Computes the nodes now.
         */
        final Node[] justComputeNodes() {
            if (map == null) {
                map = Collections.synchronizedMap(new HashMap<Entry, Info>(17));

            //      debug.append ("Map initialized\n"); // NOI18N
            //      printStackTrace();
            }

            List<Node> l = new LinkedList<Node>();
            for (Entry entry : entries) {
                Info info = findInfo(entry);

                try {
                    l.addAll(info.nodes());
                } catch (RuntimeException ex) {
                    NodeOp.warning(ex);
                }
            }

            Node[] arr = l.toArray(new Node[l.size()]);

            // initialize parent nodes
            for (int i = 0; i < arr.length; i++) {
                Node n = arr[i];
                n.assignTo(children, i);
                n.fireParentNodeChange(null, children.parent);
            }

            return arr;
        }

        /** Finds info for given entry, or registers
         * it, if not registered yet.
         */
        private Info findInfo(Entry entry) {
            synchronized (map) {
                Info info = map.get(entry);

                if (info == null) {
                    info = new Info(entry);
                    map.put(entry, info);

                //      debug.append ("Put: " + entry + " info: " + info); // NOI18N
                //      debug.append ('\n');
                //      printStackTrace();
                }
                return info;
            }
        }

        //
        // Entries
        //

        protected void setEntries(Collection<? extends Entry> entries) {
            final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
            // current list of nodes
            ChildrenArray holder = array.get();

            if (IS_LOG_GET_ARRAY) {
                LOG_GET_ARRAY.fine("setEntries for " + this + " on " + Thread.currentThread()); // NOI18N

                LOG_GET_ARRAY.fine("       values: " + entries); // NOI18N

                LOG_GET_ARRAY.fine("       holder: " + holder); // NOI18N

            }
            if (holder == null) {
                //      debug.append ("Set1: " + entries); // NOI18N
                //      printStackTrace();
                this.entries = new ArrayList<Entry>(entries);

                if (map != null) {
                    map.keySet().retainAll(new HashSet<Entry>(entries));
                }

                return;
            }

            Node[] current = holder.nodes();

            if (current == null) {
                // the initialization is not finished yet =>
                //      debug.append ("Set2: " + entries); // NOI18N
                //      printStackTrace();
                this.entries = new ArrayList<Entry>(entries);

                if (map != null) {
                    map.keySet().retainAll(new HashSet<Entry>(entries));
                }

                return;
            }

            // if there are old items in the map, remove them to
            // reflect current state
            map.keySet().retainAll(new HashSet<Entry>(this.entries));

            // what should be removed
            Set<Entry> toRemove = new HashSet<Entry>(map.keySet());
            Set<Entry> entriesSet = new HashSet<Entry>(entries);
            toRemove.removeAll(entriesSet);

            if (!toRemove.isEmpty()) {
                // notify removing, the set must be ready for
                // callbacks with questions
                updateRemove(current, toRemove);
                current = holder.nodes();
            }

            // change the order of entries, notifies
            // it and again brings children to up-to-date state
            Collection<Info> toAdd = updateOrder(current, entries);

            if (!toAdd.isEmpty()) {
                // toAdd contains Info objects that should
                // be added
                updateAdd(toAdd, new ArrayList<Entry>(entries));
            }
        }

        private void checkInfo(Info info, Entry entry, Collection<? extends Entry> entries, java.util.Map<Entry, Info> map) {
            if (info == null) {
                throw new IllegalStateException(
                        "Error in " + getClass().getName() + " with entry " + entry + " from among " + entries + " in " + map + // NOI18N
                        " probably caused by faulty key implementation." + // NOI18N
                        " The key hashCode() and equals() methods must behave as for an IMMUTABLE object" + // NOI18N
                        " and the hashCode() must return the same value for equals() keys."); // NOI18N

            }
        }

        /** Removes the objects from the children.
         */
        private void updateRemove(Node[] current, Set<Entry> toRemove) {
            List<Node> nodes = new LinkedList<Node>();

            for (Entry en : toRemove) {
                Info info = map.remove(en);

                //debug.append ("Removed: " + en + " info: " + info); // NOI18N
                //debug.append ('\n');
                //printStackTrace();
                checkInfo(info, en, null, map);

                nodes.addAll(info.nodes());
            }

            // modify the current set of entries and empty the list of nodes
            // so it has to be recreated again
            //debug.append ("Current : " + this.entries + '\n'); // NOI18N
            this.entries.removeAll(toRemove);

            //debug.append ("Removing: " + toRemove + '\n'); // NOI18N
            //debug.append ("New     : " + this.entries + '\n'); // NOI18N
            //printStackTrace();
            clearNodes();

            children.notifyRemove(nodes, current);
        }

        /** Updates the order of entries.
         * @param current current state of nodes
         * @param entries new set of entries
         * @return list of infos that should be added
         */
        private List<Info> updateOrder(Node[] current, Collection<? extends Entry> newEntries) {
            List<Info> toAdd = new LinkedList<Info>();

            // that assignes entries their begining position in the array
            // of nodes
            java.util.Map<Info, Integer> offsets = new HashMap<Info, Integer>();

            {
                int previousPos = 0;

                for (Entry entry : entries) {
                    Info info = map.get(entry);
                    checkInfo(info, entry, entries, map);

                    offsets.put(info, previousPos);

                    previousPos += info.length();
                }
            }

            // because map can contain some additional items,
            // that has not been garbage collected yet,
            // retain only those that are in current list of
            // entries
            map.keySet().retainAll(new HashSet<Entry>(entries));

            int[] perm = new int[current.length];
            int currentPos = 0;
            int permSize = 0;
            List<Entry> reorderedEntries = null;

            for (Entry entry : newEntries) {
                Info info = map.get(entry);

                if (info == null) {
                    // this info has to be added
                    info = new Info(entry);
                    toAdd.add(info);
                } else {
                    int len = info.length();

                    if (reorderedEntries == null) {
                        reorderedEntries = new LinkedList<Entry>();
                    }

                    reorderedEntries.add(entry);

                    // already there => test if it should not be reordered
                    Integer previousInt = offsets.get(info);

                    /*
                    if (previousInt == null) {
                    System.err.println("Offsets: " + offsets);
                    System.err.println("Info: " + info);
                    System.err.println("Entry: " + info.entry);
                    System.err.println("This entries: " + this.entries);
                    System.err.println("Entries: " + entries);
                    System.err.println("Map: " + map);
                    
                    System.err.println("---------vvvvv");
                    System.err.println(debug);
                    System.err.println("---------^^^^^");
                    
                    }
                     */
                    int previousPos = previousInt;

                    if (currentPos != previousPos) {
                        for (int i = 0; i < len; i++) {
                            perm[previousPos + i] = 1 + currentPos + i;
                        }

                        permSize += len;
                    }
                }

                currentPos += info.length();
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
                this.entries = reorderedEntries;

                //      debug.append ("Set3: " + this.entries); // NOI18N
                //      printStackTrace();
                // notify the permutation to the parent
                clearNodes();

                //System.err.println("Paremutaiton! " + getNode ());
                Node p = children.parent;

                if (p != null) {
                    p.fireReorderChange(perm);
                }
            }

            return toAdd;
        }

        /** Updates the state of children by adding given Infos.
         * @param infos list of Info objects to add
         * @param entries the final state of entries that should occur
         */
        private void updateAdd(Collection<Info> infos, List<? extends Entry> entries) {
            List<Node> nodes = new LinkedList<Node>();
            for (Info info : infos) {
                nodes.addAll(info.nodes());
                map.put(info.entry, info);

            //      debug.append ("updateadd: " + info.entry + " info: " + info + '\n'); // NOI18N
            //      printStackTrace();
            }

            this.entries = entries;

            //      debug.append ("Set4: " + entries); // NOI18N
            //      printStackTrace();
            clearNodes();

            children.notifyAdd(nodes);
        }

        /** Refreshes content of one entry. Updates the state of children
         * appropriately.
         */
        final void refreshEntry(Entry entry) {
            // current list of nodes
            ChildrenArray holder = array.get();

            if (holder == null) {
                return;
            }

            Node[] current = holder.nodes();

            if (current == null) {
                // the initialization is not finished yet =>
                return;
            }

            // because map can contain some additional items,
            // that has not been garbage collected yet,
            // retain only those that are in current list of
            // entries
            map.keySet().retainAll(new HashSet<Entry>(this.entries));

            Info info = map.get(entry);

            if (info == null) {
                // refresh of entry that is not present =>
                return;
            }

            Collection<Node> oldNodes = info.nodes();
            Collection<Node> newNodes = info.entry.nodes();

            if (oldNodes.equals(newNodes)) {
                // nodes are the same =>
                return;
            }

            Set<Node> toRemove = new HashSet<Node>(oldNodes);
            toRemove.removeAll(new HashSet<Node>(newNodes));

            if (!toRemove.isEmpty()) {
                // notify removing, the set must be ready for
                // callbacks with questions
                // modifies the list associated with the info
                oldNodes.removeAll(toRemove);
                clearNodes();

                // now everything should be consistent => notify the remove
                children.notifyRemove(toRemove, current);

                current = holder.nodes();
            }

            List<Node> toAdd = refreshOrder(entry, oldNodes, newNodes);
            info.useNodes(newNodes);

            if (!toAdd.isEmpty()) {
                // modifies the list associated with the info
                clearNodes();
                children.notifyAdd(toAdd);
            }
        }

        /** Updates the order of nodes after a refresh.
         * @param entry the refreshed entry
         * @param oldNodes nodes that are currently in the list
         * @param newNodes new nodes (defining the order of oldNodes and some more)
         * @return list of infos that should be added
         */
        private List<Node> refreshOrder(Entry entry, Collection<Node> oldNodes, Collection<Node> newNodes) {
            List<Node> toAdd = new LinkedList<Node>();

            int currentPos = 0;

            // cycle thru all entries to find index of the entry
            Iterator<? extends Entry> it1 = this.entries.iterator();

            for (;;) {
                Entry e = it1.next();

                if (e.equals(entry)) {
                    break;
                }

                Info info = findInfo(e);
                currentPos += info.length();
            }

            Set<Node> oldNodesSet = new HashSet<Node>(oldNodes);
            Set<Node> toProcess = new HashSet<Node>(oldNodesSet);

            Node[] permArray = new Node[oldNodes.size()];
            Iterator<Node> it2 = newNodes.iterator();

            int pos = 0;

            while (it2.hasNext()) {
                Node n = it2.next();

                if (oldNodesSet.remove(n)) {
                    // the node is in the old set => test for permuation
                    permArray[pos++] = n;
                } else {
                    if (!toProcess.contains(n)) {
                        // if the node has not been processed yet
                        toAdd.add(n);
                    } else {
                        it2.remove();
                    }
                }
            }

            // JST: If you get IllegalArgumentException in following code
            // then it can be cause by wrong synchronization between
            // equals and hashCode methods. First of all check them!
            int[] perm = NodeOp.computePermutation(oldNodes.toArray(new Node[oldNodes.size()]), permArray);

            if (perm != null) {
                // apply the permutation
                clearNodes();

                // temporarily change the nodes the entry should use
                findInfo(entry).useNodes(Arrays.asList(permArray));
                Node p = children.parent;
                if (p != null) {
                    p.fireReorderChange(perm);
                }
            }

            return toAdd;
        }

        //
        // ChildrenArray operations call only under lock
        //
        /** @return either nodes associated with this children or null if
         * they are not created
         */
        public Node[] testNodes() {
            ChildrenArray arr = array.get();
            return (arr == null) ? null : arr.nodes();
        }

        /** Obtains references to array holder. If it does not exist, it is created.
         *
         * @param cannotWorkBetter array of size 1 or null, will contain true, if
         *    the getArray cannot be initialized (we are under read access
         *    and another thread is responsible for initialization, in such case
         *    give up on computation of best result
         */
        private ChildrenArray getArray(boolean[] cannotWorkBetter) {
            final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);

            ChildrenArray arr;
            boolean doInitialize = false;
            synchronized (LOCK) {
                arr = array.get();

                if (arr == null) {
                    arr = new ChildrenArray();

                    // register the array with the children
                    registerChildrenArray(arr, true);
                    doInitialize = true;
                    initThread = Thread.currentThread();
                }
            }

            if (doInitialize) {
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("Initialize " + this + " on " + Thread.currentThread()); // NOI18N

                }

                // this call can cause a lot of callbacks => be prepared
                // to handle them as clean as possible
                try {
                    children.callAddNotify();

                    if (IS_LOG_GET_ARRAY) {
                        LOG_GET_ARRAY.fine("addNotify successfully called for " + this + " on " + Thread.currentThread()); // NOI18N

                    }
                } finally {
                    boolean notifyLater;
                    notifyLater = Children.MUTEX.isReadAccess();

                    if (IS_LOG_GET_ARRAY) {
                        LOG_GET_ARRAY.fine(
                                "notifyAll for " + this + " on " + Thread.currentThread() + "  notifyLater: " + notifyLater); // NOI18N

                    }

                    // now attach to entrySupport, so when entrySupport == null => we are
                    // not fully initialized!!!!
                    arr.entrySupport = this;
                    class SetAndNotify implements Runnable {

                        public ChildrenArray toSet;
                        public Children whatSet;

                        public void run() {
                            synchronized (LOCK) {
                                initThread = null;
                                LOCK.notifyAll();
                            }

                            if (IS_LOG_GET_ARRAY) {
                                LOG_GET_ARRAY.fine(
                                        "notifyAll done"); // NOI18N

                            }

                        }
                    }

                    SetAndNotify setAndNotify = new SetAndNotify();
                    setAndNotify.toSet = arr;
                    setAndNotify.whatSet = children;

                    if (notifyLater) {
                        // the notify to the lock has to be done later than
                        // setKeys is executed, otherwise the result of addNotify
                        // might not be visible to other threads
                        // fix for issue 50308
                        Children.MUTEX.postWriteRequest(setAndNotify);
                    } else {
                        setAndNotify.run();
                    }
                }
            } else {
                // otherwise, if not initialize yet (arr.children) wait 
                // for the initialization to finish, but only if we can wait
                if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess() || (initThread == Thread.currentThread())) {
                    // fail, we are in read access
                    if (IS_LOG_GET_ARRAY) {
                        LOG_GET_ARRAY.log(Level.FINE,
                                "cannot initialize better " + this + // NOI18N
                                " on " + Thread.currentThread() + // NOI18N
                                " read access: " + Children.MUTEX.isReadAccess() + // NOI18N
                                " initThread: " + initThread, // NOI18N
                                new Exception("StackTrace") // NOI18N
                                );
                    }

                    if (cannotWorkBetter != null) {
                        cannotWorkBetter[0] = true;
                    }

                    return arr;
                }

                // otherwise we can wait
                synchronized (LOCK) {
                    while (initThread != null) {
                        if (IS_LOG_GET_ARRAY) {
                            LOG_GET_ARRAY.fine(
                                    "waiting for children for " + this + // NOI18N
                                    " on " + Thread.currentThread() // NOI18N
                                    );
                        }

                        try {
                            LOCK.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine(
                            " children are here for " + this + // NOI18N
                            " on " + Thread.currentThread() + // NOI18N
                            " children " + children);
                }
            }

            return arr;
        }

        /** Clears the nodes
         */
        private void clearNodes() {
            ChildrenArray arr = array.get();

            //System.err.println(off + "  clearNodes: " + getNode ());
            if (arr != null) {
                // clear the array
                arr.clear();
            }
        }


        /** Registration of ChildrenArray.
         * @param chArr the associated ChildrenArray
         * @param weak use weak or hard reference
         */
        final void registerChildrenArray(final ChildrenArray chArr, boolean weak) {
            final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
            if (IS_LOG_GET_ARRAY) {
                LOG_GET_ARRAY.fine("registerChildrenArray: " + chArr + " weak: " + weak); // NOI18N

            }
            if (weak) {
                this.array = new WeakReference<ChildrenArray>(chArr);
            } else {
                // hold the children hard
                this.array = new WeakReference<ChildrenArray>(chArr) {

                    @Override
                    public ChildrenArray get() {
                        return chArr;
                    }
                };
            }

            chArr.pointedBy(this.array);
            if (IS_LOG_GET_ARRAY) {
                LOG_GET_ARRAY.fine("pointed by: " + chArr + " to: " + this.array); // NOI18N

            }
        }

        /** Finalized.
         */
        final void finalizedChildrenArray(Reference caller) {
            final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
            // usually in removeNotify setKeys is called => better require write access
            try {
                Children.PR.enterWriteAccess();

                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("previous array: " + array + " caller: " + caller);
                }
                if (array == caller) {
                    // really finalized and not reconstructed
                    children.removeNotify();
                }

            /*
            else {
            System.out.println("Strange removeNotify " + caller + " : " + value );
            }
             */
            } finally {
                Children.PR.exitWriteAccess();
            }
        }
        /** Forces finalization of nodes for given info.
         * Called from finalizer of Info.
         */
        final void finalizeNodes() {
            ChildrenArray arr = array.get();

            if (arr != null) {
                arr.finalizeNodes();
            }
        }

        /** Information about an entry. Contains number of nodes,
         * position in the array of nodes, etc.
         */
        final class Info extends Object {

            int length;
            final Entry entry;

            public Info(Entry entry) {
                this.entry = entry;
            }

            /** Finalizes the content of ChildrenArray.
             */
            @Override
            protected void finalize() {
                finalizeNodes();
            }

            public Collection<Node> nodes() {
                // forces creation of the array
                ChildrenArray arr = getArray(null);
                return arr.nodesFor(this);
            }

            public void useNodes(Collection<Node> nodes) {
                // forces creation of the array
                ChildrenArray arr = getArray(null);
                arr.useNodes(this, nodes);

                // assign all there nodes the new children
                for (Node n : nodes) {
                    n.assignTo(EntrySupport.Default.this.children, -1);
                    n.fireParentNodeChange(null, children.parent);
                }
            }

            public int length() {
                return length;
            }

            @Override
            public String toString() {
                return "Children.Info[" + entry + ",length=" + length + "]"; // NOI18N
            }
        }
    }
    
    static final class Lazy extends EntrySupport {
        private Map<Entry, EntryInfo> entryToInfo = new HashMap<Entry, EntryInfo>();
        private List<WeakReference<Node>> childrenNodes = new ArrayList<WeakReference<Node>>();
        /** Computed size of nodes in this support or -1 if the size
         * needs to be recomputed once again. Clear to -1 if you do some
         * changes in nodes count.
         */
        private int nodesCount = -1;

        public Lazy(Children ch) {
            super(ch);
        }

        @Override
        public Node getNodeAt(int index) {
            try {
                Children.PR.enterReadAccess();
                if (childrenNodes != null) {
                    if (index >= childrenNodes.size()) {
                        return null;
                    }
                    Node node = childrenNodes.get(index).get();
                    if (node != null) {
                        return node;
                    }
                }
            } finally {
                Children.PR.exitReadAccess();
            }
            return computeAt(index);
        }
        
        final Node computeAt(final int index) {
            int low = 0;
            int high = entries.size() - 1;
            while (low <= high) {
                int mid = (low + high) / 2;
                Entry e = entries.get(mid);
                EntryInfo info = entryToInfo.get(e);
                if (info.getIndex() > index) {
                    high = mid - 1;
                    continue;
                }
                int above = info.getIndex() + info.size();
                if (above > index) {
                    List<Node> list = info.getNodes();
                    int size = info.getIndex();

                    /*if (list.size() <= index - size) {
                        return NO_NODE;
                    }*/
                    Node n = (Node) list.get(index - size);
                    n.assignTo(children, index);
                    return n;
                }
                low = mid + 1;
            }
            return null;
        }

        @Override
        public Node[] getNodes(boolean optimalResult) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getNodesCount() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isInitialized() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        void refreshEntry(Entry entry) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        /** Gets info for given entry, or create one if not registered yet. */
        private EntryInfo getInfo(Entry entry) {
            synchronized (entryToInfo) {
                EntryInfo info = entryToInfo.get(entry);
                if (info == null) {
                    info = new EntryInfo(entry);
                    entryToInfo.put(entry, info);
                }
                return info;
            }
        }        

        @Override
        void setEntries(Collection<? extends Entry> entries) {
            entries = new ArrayList(entries);
            nodesCount = -1;

            assert this.entries.size() == entryToInfo.size();

            HashSet<Entry> retain = new HashSet<Entry>(entries);
            Iterator<? extends Entry> it = this.entries.iterator();
            int index = 0;
            ArrayList<Integer> removedIdxs = new ArrayList<Integer>();
            ArrayList<Node> removedNodes = new ArrayList<Node>();
            while (it.hasNext()) {
                EntryInfo info = entryToInfo.get(it.next());
                int size = info.size();
                if (!retain.contains(info.entry)) {
                    for (int i = 0; i < size; i++) {
                        removedIdxs.add(new Integer(index + i));
                    }
                    // unassign from parent
                    Collection<Node> nodes = info.currentNodes(null);
                    if (nodes != null) {
                        removedNodes = new ArrayList<Node>(nodes.size());
                        Iterator nodeIt = nodes.iterator();
                        while (nodeIt.hasNext()) {
                            Node n = (Node) nodeIt.next();
                            if (n != null) {
                                n.deassignFrom(children);
                                removedNodes.add(n);
                            }
                        }
                    }
                    // remove the entry from collection
                    it.remove();
                    entryToInfo.remove(info.entry);
                }
                info.setIndex(index);
                index += size;
            }

            if (!removedIdxs.isEmpty()) {
                int[] idxs = new int[removedIdxs.size()];
                for (int i = 0; i < idxs.length; i++) {
                    idxs[i] = ((Integer) removedIdxs.get(i)).intValue();
                }
                childrenNodes.clear();
                //fireIndexesAddedOrRemoved(false, idxs);
                children.destroyNodes(removedNodes.toArray(new Node[removedNodes.size()]));
            }

            // change the order of entries, notifies
            // it and again brings children to up-to-date state, recomputes indexes
            Collection<EntryInfo> toAdd = updateOrder(entries);

            if (!toAdd.isEmpty()) {
                // now we know that this.entries are subset of entries and
                // are also properly sorted. So we can just iterate over 
                // entries and whenever there is a different, just add once
                ArrayList addedIndixes = new ArrayList(toAdd.size());
                for (EntryInfo info : toAdd) {
                    final int size = info.size();
                    final int idx = info.getIndex();
                    Collection<Node> nodes = info.currentNodes(null);
                    Iterator nodeIt = nodes == null ? null : nodes.iterator();
                    for (int i = 0; i < size; i++) {
                        addedIndixes.add(new Integer(idx + i));
                        if (nodeIt != null) {
                            // assign to new parent
                            Node n = (Node) nodeIt.next();
                            if (n != null) {
                                n.assignTo(children, i);
                            }
                        }                     
                    }
                }
                if (!addedIndixes.isEmpty()) {
                    int[] idxs = new int[addedIndixes.size()];
                    for (int i = 0; i < idxs.length; i++) {
                        idxs[i] = ((Integer) addedIndixes.get(i)).intValue();
                    }
                    childrenNodes.clear();
                    //fireIndexesAddedOrRemoved(true, idxs);
                }
            }
        }

        /** Updates the order of entries.
         * @param current current state of nodes
         * @param entries new set of entries
         * @return list of infos that should be added
         */
        private List<EntryInfo> updateOrder(Collection<? extends Entry> newEntries) {
            List<EntryInfo> toAdd = new LinkedList<EntryInfo>();

            // that assignes entries their begining position in the array of nodes
            Map<EntryInfo, Integer> offsets = new HashMap<EntryInfo, Integer>();
            int previousPos = 0;
            for (Entry entry : entries) {
                EntryInfo info = entryToInfo.get(entry);
                offsets.put(info, previousPos);
                previousPos += info.size();
            }

            // because map can contain some additional items,
            // that has not been garbage collected yet,
            // retain only those that are in current list of
            // entries
            entryToInfo.keySet().retainAll(new HashSet<Entry>(entries));

            int[] perm = new int[previousPos];
            int currentPos = 0;
            int permSize = 0;
            List<Entry> reorderedEntries = null;

            for (Entry entry : newEntries) {
                EntryInfo info = entryToInfo.get(entry);

                if (info == null) {
                    // this info has to be added
                    info = new EntryInfo(entry);
                    info.setIndex(currentPos);
                    entryToInfo.put(entry, info);
                    toAdd.add(info);
                } else {
                    int len = info.size();

                    if (reorderedEntries == null) {
                        reorderedEntries = new LinkedList<Entry>();
                    }

                    reorderedEntries.add(entry);
                    info.setIndex(currentPos);

                    // already there => test if it should not be reordered
                    Integer previousInt = offsets.get(info);
                    previousPos = previousInt;

                    if (currentPos != previousPos) {
                        for (int i = 0; i < len; i++) {
                            perm[previousPos + i] = 1 + currentPos + i;
                        }
                        permSize += len;
                    }
                }
                currentPos += info.size();
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
                this.entries = reorderedEntries;
                childrenNodes.clear();

                //System.err.println("Paremutaiton! " + getNode ());
                Node p = children.parent;

                if (p != null) {
                    p.fireReorderChange(perm);
                }
            }
            return toAdd;
        }

        @Override
        public Node[] testNodes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
      
        final class EntryInfo {
            
            /** corresponding entry */
            final Entry entry;

            /** my length, -1 means uninitialized */
            private int length = -1;

            /** cached nodes for this entry */
            private List<WeakReference<Node>> nodesCache;

            /** my index (including sizes) in list of entries */
            private int index = -1;
            
            public EntryInfo(Entry entry) {
                this.entry = entry;
            }

            /** Returns size of this entry */
            public final int size() {
                if (length < 0) {
                    length = getNodes().size();
                }
                return length;
            }

            /** Gets or computes the nodes. It holds them using weak reference
             * so they can get garbage collected.
             */
            public final synchronized List<Node> getNodes() {
                boolean[] containsNulls = new boolean[1];
                List<Node> curNodes = currentNodes(containsNulls);
                if (curNodes != null && containsNulls[0] == false) {
                    return curNodes;
                }
                Collection<Node> nodes = entry.nodes();
                useNodes(nodes);
                return new ArrayList<Node>(nodes);
            }

            /** extract current nodes */
            synchronized List<Node> currentNodes(boolean[] containsNulls) {
                if (nodesCache == null) {
                    return null;
                }
                ArrayList<Node> arr = new ArrayList<Node>(nodesCache.size());
                for (int i = 0; i < nodesCache.size(); i++) {
                    Node n = nodesCache.get(i).get();
                    if (n == null && containsNulls != null) {
                        containsNulls[0] = true;
                    }
                    arr.add(n);
                }
                return arr;
            }

            /** Assignes new set of nodes to this entry. */
            public final synchronized void useNodes(Collection<Node> nodes) {
                nodesCache = new ArrayList<WeakReference<Node>>(nodes.size());
                for (Node n : nodes) {
                    nodesCache.add(new WeakReference<Node>(n));
                }
                length = nodes.size();
                /*
                // assign all there nodes the new children
                for (Node n : list) {
                n.assignTo(Children.this, -1);
                n.fireParentNodeChange(null, parent);
                }*/
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
                String clazz = super.toString();
                int in = clazz.lastIndexOf('$');
                if (in >= 0) {
                    clazz = clazz.substring(in + 1);
                }
                return clazz + "[index: " + index + ",length:" + length + "]"; // NOI18N
            }
        }        
    }
}
