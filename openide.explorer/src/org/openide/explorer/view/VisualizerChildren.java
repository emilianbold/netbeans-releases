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
package org.openide.explorer.view;

import org.openide.nodes.*;

import java.util.*;


/** List of Visualizers. This is holded by parent visualizer by a
* weak reference,
*
* @author Jaroslav Tulach
*/
final class VisualizerChildren extends Object {
    /** parent visualizer */
    public final VisualizerNode parent;

    /** list of all objects here (VisualizerNode) */
public final List<VisualizerNode> list = new ArrayList<VisualizerNode>();

    /** Creates new VisualizerChildren.
    * Can be called only from EventQueue.
    */
    public VisualizerChildren(VisualizerNode parent, Node[] nodes) {
        this.parent = parent;

        int s = nodes.length;

        for (int i = 0; i < s; i++) {
            VisualizerNode v = VisualizerNode.getVisualizer(this, nodes[i]);
            list.add(v);
        }
    }

    /** Notification of children addded event. Modifies the list of nodes
    * and fires info to all listeners.
    */
    public void added(VisualizerEvent.Added ev) {
        ListIterator<VisualizerNode> it = list.listIterator();
        boolean empty = !it.hasNext();

        int[] indxs = ev.getArray();
        Node[] nodes = ev.getAdded();

        int current = 0;
        int inIndxs = 0;

        while (inIndxs < indxs.length) {
            while (current++ < indxs[inIndxs]) {
                it.next();
            }

            it.add(VisualizerNode.getVisualizer(this, nodes[inIndxs]));
            inIndxs++;
        }

        VisualizerNode parent = this.parent;

        while (parent != null) {
            Object[] listeners = parent.getListenerList();

            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).added(ev);
            }

            parent = (VisualizerNode) parent.getParent();
        }

        if (empty) {
            // change of state
            this.parent.notifyVisualizerChildrenChange(list.size(), this);
        }
    }

    private static boolean sameContains(List l, Object elem) {
        for (Iterator it = l.iterator(); it.hasNext();) {
            if (it.next() == elem) {
                return true;
            }
        }

        return false;
    }

    /** Notification that children has been removed. Modifies the list of nodes
    * and fires info to all listeners.
    */
    public void removed(VisualizerEvent.Removed ev) {
        List remList = Arrays.asList(ev.getRemovedNodes());

        Iterator it = list.iterator();

        VisualizerNode vis;

        int[] indx = new int[remList.size()];
        int count = 0;
        int remSize = 0;

        while (it.hasNext()) {
            // take visualizer node
            vis = (VisualizerNode) it.next();

            // check if it will removed
            if (sameContains(remList, vis.node)) {
                indx[remSize++] = count;

                // remove this VisualizerNode from children
                it.remove();

                // bugfix #36389, add the removed node to VisualizerEvent
                ev.removed.add(vis);
            }

            count++;
        }

        // notify event about changed indexes
        ev.setRemovedIndicies(indx);

        VisualizerNode parent = this.parent;

        while (parent != null) {
            Object[] listeners = parent.getListenerList();

            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).removed(ev);
            }

            parent = (VisualizerNode) parent.getParent();
        }

        if (list.isEmpty()) {
            // now is empty
            this.parent.notifyVisualizerChildrenChange(0, this);
        }
    }

    /**
     * Issue 37802, sort the actual list of children with the comparator,
     * rather than expecting it to match the current children of the node,
     * which may be in an inconsistent state.
     */
    private int[] reorderByComparator(Comparator<VisualizerNode> c) {
        VisualizerNode[] old = list.toArray(new VisualizerNode[list.size()]);
        Arrays.sort(old, c);

        int[] idxs = new int[old.length];

        for (int i = 0; i < idxs.length; i++) {
            idxs[i] = list.indexOf(old[i]);
        }

        list.clear();
        list.addAll(Arrays.asList(old));

        return idxs;
    }

    /** Notification that children has been reordered. Modifies the list of nodes
    * and fires info to all listeners.
    */
    public void reordered(VisualizerEvent.Reordered ev) {
        if (ev.getComparator() != null) {
            //#37802
            ev.array = reorderByComparator(ev.getComparator());
        } else {
            int[] indxs = ev.getArray();
            VisualizerNode[] old = list.toArray(new VisualizerNode[list.size()]);
            VisualizerNode[] arr = new VisualizerNode[old.length];

            int s = indxs.length;

            try {
                for (int i = 0; i < s; i++) {
                    // arr[indxs[i]] = old[i];
                    VisualizerNode old_i = old[i];
                    int indxs_i = indxs[i];

                    if (arr[indxs_i] != null) {
                        // this is bad <-- we are rewriting some old value --> there will remain some null somewhere
                        System.err.println("Writing to this index for the second time: " + indxs_i); // NOI18N
                        System.err.println("Length of indxs array: " + indxs.length); // NOI18N
                        System.err.println("Length of actual array: " + old.length); // NOI18N
                        System.err.println("Indices of reorder event:"); // NOI18N

                        for (int j = 0; i < indxs.length; j++)
                            System.err.println("\t" + indxs[j]); // NOI18N

                        Thread.dumpStack();

                        return;
                    }

                    arr[indxs_i] = old_i;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                System.err.println("Length of actual array: " + old.length); // NOI18N
                System.err.println("Indices of reorder event:"); // NOI18N

                for (int i = 0; i < indxs.length; i++)
                    System.err.println("\t" + indxs[i]); // NOI18N

                return;
            }

            assert !Arrays.asList(arr).contains(null) : "Null element in reorderer list " + Arrays.asList(arr) +
            "; list=" + list + " indxs=" + Arrays.asList(org.openide.util.Utilities.toObjectArray(indxs));
            list.clear();
            list.addAll(Arrays.asList(arr));
            assert !list.contains(null);
        }

        VisualizerNode parent = this.parent;

        while (parent != null) {
            Object[] listeners = parent.getListenerList();

            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).reordered(ev);
            }

            parent = (VisualizerNode) parent.getParent();
        }
    }
}
