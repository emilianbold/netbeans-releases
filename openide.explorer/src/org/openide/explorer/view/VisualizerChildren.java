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
package org.openide.explorer.view;

import org.openide.nodes.*;
import java.util.*;


/** List of Visualizers. This is holded by parent visualizer by a
* weak reference,
*
* @author Jaroslav Tulach
*/
final class VisualizerChildren extends Object {
    /** empty visualizer children for any leaf */
    public static final VisualizerChildren EMPTY = new VisualizerChildren();   
    
    /** parent visualizer */
    public final VisualizerNode parent;

    /** visualizer nodes (children) */
    private final List<VisualizerNode> visNodes;
    
    private List<Node> snapshot;
    
    /** Empty VisualizerChildren. */
    private VisualizerChildren () {
        visNodes = Collections.EMPTY_LIST;
        snapshot = Collections.EMPTY_LIST;
        parent = null;
    }    
    
    /** Creates new VisualizerChildren.
     * Can be called only from EventQueue.
     */
    public VisualizerChildren(VisualizerNode parent, List<Node> snapshot) {
        this.parent = parent;
        int size = snapshot.size();
        visNodes = new ArrayList<VisualizerNode>(size);
        for (int i = 0; i < size; i++) {
            visNodes.add(null);
        }
        this.snapshot = snapshot;
    }

    /** recomputes indexes for all nodes.
     * @param tn tree node that we are looking for
     */
    private final void recomputeIndexes(VisualizerNode tn) {
        assert visNodes.size() == snapshot.size() : "visnodes.size()=" + visNodes.size()
                + " snapshot.size()=" + snapshot.size();

        for (int i = 0; i < visNodes.size(); i++) {
            VisualizerNode node = visNodes.get(i);
            if (node != null) {
                node.indexOf = i;
            }
        }

        if (tn != null && tn.indexOf == -1) {
            // not computed => force computation
            for (int i = 0; i < visNodes.size(); i++) {
                VisualizerNode visNode = (VisualizerNode) getChildAt(i);
                visNode.indexOf = i;
                if (visNode == tn) {
                    return;
                }
            }
        }
    }
    
    public javax.swing.tree.TreeNode getChildAt(int pos) {
        if (pos >= visNodes.size()) {
            return VisualizerNode.EMPTY;
        }
        VisualizerNode visNode = visNodes.get(pos);
        if (visNode == null) {
            Node node = snapshot.get(pos);
            visNode = VisualizerNode.getVisualizer(this, node);
            visNode.indexOf = pos;
            visNodes.set(pos, visNode);
            parent.notifyVisualizerChildrenChange(false, this);
        }
        return visNode;
    }
    
    public int getChildCount() {
        return visNodes.size();
    }

    public java.util.Enumeration<VisualizerNode> children(final boolean create) {
        return new java.util.Enumeration<VisualizerNode>() {

            private int index;

            public boolean hasMoreElements() {
                return index < visNodes.size();
            }

            public VisualizerNode nextElement() {
                return create ? (VisualizerNode) getChildAt(index++) : visNodes.get(index++);
            }
        };
    }

    /** Delegated to us from VisualizerNode
     * 
     */
    public int getIndex(final javax.swing.tree.TreeNode p1) {
        VisualizerNode visNode = (VisualizerNode) p1;
        if (visNode.indexOf != -1) {
            if (visNode.indexOf >= visNodes.size() || visNodes.get(visNode.indexOf) != visNode) {
                return -1;
            }
        } else {
            recomputeIndexes(visNode);
        }
        return visNode.indexOf;
    }

    final String dumpIndexes(VisualizerNode visNode) {
        StringBuilder sb = new StringBuilder();
        sb.append("EMPTY: " + (visNode == VisualizerNode.EMPTY) + ", Lazy: " // NOI18N
                + snapshot.getClass().getName().endsWith("LazySnapshot")); // NOI18N
        sb.append("\nSeeking for: ").append(visNode.toId()); // NOI18N
        sb.append("\nwith parent: ").append(((VisualizerNode)visNode.getParent()) != null // NOI18N
                ? ((VisualizerNode)visNode.getParent()).toId() : "null"); // NOI18N
        sb.append("\nSeeking in : ").append(parent != null ? parent.toId() : "null").append("\n"); // NOI18N
        addVisNodesInfo(sb);
        return sb.toString();
    }
    
    private void addVisNodesInfo(StringBuilder sb) {
        for (int i = 0; i < visNodes.size(); i++) {
            VisualizerNode node = visNodes.get(i);
            sb.append("  ").append(i); // NOI18N
            if (node != null) {
                sb.append(" = ").append(node.toId()); // NOI18N
            } else {
                sb.append(" = null"); // NOI18N
            }
            sb.append('\n'); // NOI18N
        }        
    }
    
    final String dumpEventInfo(VisualizerEvent ev) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nEvent: " + ev.getClass().getName()); // NOI18N
        sb.append("\nOriginal event: " + ev.originalEvent.getClass().getName()); // NOI18N
        sb.append("\ncurrent vis. nodes:"); // NOI18N
        addVisNodesInfo(sb);
        sb.append("\nIndexes: "); // NOI18N
        int[] arr = ev.getArray();
        for (int i = 0; i < arr.length; i++) {
            sb.append(Integer.toString(arr[i]));
            sb.append(" "); // NOI18N
        }
        sb.append("\n"); // NOI18N
        sb.append(ev.originalEvent.toString());
        return sb.toString();
    }
    
    /** Notification of children addded event. Modifies the list of nodes
     * and fires info to all listeners.
     */
    public void added(VisualizerEvent.Added ev) {
        if (this != parent.getChildren()) {
            // children were replaced, quit processing event
            return;
        }        
        snapshot = ev.getSnapshot();
        ListIterator<VisualizerNode> it = visNodes.listIterator();

        int[] indxs = ev.getArray();
        int current = 0;
        int inIndxs = 0;

        while (inIndxs < indxs.length) {
            while (current++ < indxs[inIndxs]) {
                it.next();
            }
            it.add(null);
            inIndxs++;
        }

        recomputeIndexes(null);

        VisualizerNode parent = this.parent;
        while (parent != null) {
            Object[] listeners = parent.getListenerList();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).added(ev);
            }
            parent = (VisualizerNode) parent.getParent();
        }
    }

    /** Notification that children has been removed. Modifies the list of nodes
     * and fires info to all listeners.
     */
   public void removed(VisualizerEvent.Removed ev) {
        if (this != parent.getChildren()) {
            // children were replaced, quit processing event
            return;
        }
        snapshot = ev.getSnapshot();
        int[] idxs = ev.getArray();
        if (idxs.length == 0) {
            return;
        }
        
        assert visNodes.size() > idxs[idxs.length - 1] : dumpEventInfo(ev);

        for (int i = idxs.length - 1; i >= 0; i--) {
            VisualizerNode visNode = visNodes.remove(idxs[i]);
            ev.removed.add(visNode != null ? visNode : VisualizerNode.EMPTY);
        }

        recomputeIndexes(null);

        VisualizerNode parent = this.parent;
        while (parent != null) {
            Object[] listeners = parent.getListenerList();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).removed(ev);
            }
            parent = (VisualizerNode) parent.getParent();
        }

        if (visNodes.isEmpty()) {
            // now is empty
            this.parent.notifyVisualizerChildrenChange(true, this);
        }
    }

    /** Notification that children has been reordered. Modifies the list of nodes
     * and fires info to all listeners.
     */
    public void reordered(VisualizerEvent.Reordered ev) {
        if (this != parent.getChildren()) {
            // children were replaced, quit processing event
            return;
        }
        snapshot = ev.getSnapshot();

        int[] indxs = ev.getArray();
        VisualizerNode[] old = visNodes.toArray(new VisualizerNode[visNodes.size()]);
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

                    for (int j = 0; i < indxs.length; j++) {
                        System.err.println("\t" + indxs[j]); // NOI18N
                    }
                    Thread.dumpStack();

                    return;
                }

                arr[indxs_i] = old_i;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.err.println("Length of actual array: " + old.length); // NOI18N
            System.err.println("Indices of reorder event:"); // NOI18N

            for (int i = 0; i < indxs.length; i++) {
                System.err.println("\t" + indxs[i]); // NOI18N
            }
            return;
        }

        visNodes.clear();
        visNodes.addAll(Arrays.asList(arr));
        recomputeIndexes(null);

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
