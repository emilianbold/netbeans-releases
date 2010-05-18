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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.insync;

import java.io.PrintWriter;

/**
 * An abstract representation of a logical compilation unit. The implementation may be directly
 * associated with a source file/buffer, or may be an abstraction on top of one or more other units.
 *
 * @author Carl Quinn
 */
public interface Unit {

    //---------------------------------------------------------------------------------------- State

    /**
     * Tracks the state of the unit.<p/>
     *
     * When everything is Insync, we're in the CLEAN state.<p/>
     *
     * A programmatic modification of the unit (e.g. setting a DOM attribute, or java property)
     * within a writeLock() takes us into the MODELDIRTY state temporarily, and then to a
     * SOURCEDIRTY state on writeUnlock().<p/>
     *
     * A buffer edit takes us from the CLEAN state to the SOURCEDIRTY state. <p/>
     *
     * A sync() takes us back from SOURCEDIRTY to CLEAN, but if there is an error during sync(), we
     * go to the BUSTED state.<p/>
     *
     * An edit in the BUSTED state takes us back to the SOURCEDIRTY state from which a new sync()
     * can be attempted.
     */
    public static class State {
        private final String name;
        private State(String name) { this.name = name; }

        public String toString() { return name; }

        /**
         * Report whether the source is in an invalid state. This typically means
         * that the source file cannot be parsed due to some source error.
         * You can get the errors causing the invalid state by calling
         * @see{getErrors}.
         * @return true iff the source is invalid
         */
        public boolean isBusted() {
            return this == BUSTED;
        }

        /**
         * Report whether the model is available to be written to
         * @return true iff the state is clean or modeldirty
         */
        public boolean isModelAvailable() {
            return this == CLEAN || this == MODELDIRTY;
        }

        public static final State CLEAN = new State("Clean");  //NOI18N
        public static final State MODELDIRTY = new State("ModelDirty");  //NOI18N
        public static final State SOURCEDIRTY = new State("SourceDirty");  //NOI18N
        public static final State BUSTED = new State("Busted");  //NOI18N
    }

    /**
     * Get the current state of this unit.
     *
     * @return This unit's current state.
     */
    public abstract State getState();

    /**
     * Return the list of errors if this unit does not compile. If there are no errors it returns an
     * empty array - never null.
     *
     * @return An array of ParserAnnotations.
     */
    public abstract ParserAnnotation[] getErrors();

    //---------------------------------------------------------------------------------------- Input

    /**
     * Acquires a lock to begin reading some state from the unit.  There can be multiple readers at
     * the same time.  Writing blocks the readers until notification of the change to the listeners
     * has been completed.  This method should be used very carefully to avoid unintended compromise
     * of the unit.  It should always be balanced with a <code>readUnlock</code>.
     *
     * @see #readUnlock
     */
    public abstract void readLock();

    /**
     * Does a read unlock. This signals that one of the readers is done. If there are no more
     * readers then writing can begin again. This should be balanced with a readLock, and should
     * occur in a finally statement so that the balance is guaranteed. The following is an example.
     *
     * <pre><code>
     *    readLock();
     *    try {   // do something   }
     *    finally {  readUnlock();   }
     * </code></pre>
     *
     * @see #readLock
     */
    public abstract void readUnlock();

    /**
     * Sync this unit's contents to its document or underlying unit, reading changes as needed and
     * updating flags.
     * 
     * @see #writeLock
     * @return true iff the model was modified by the operation
     */
    public abstract boolean sync();

    //--------------------------------------------------------------------------------------- Output

    /**
     * Acquires a lock to begin mutating the unit this lock protects. There can be no writing,
     * notification of changes, or reading going on in order to gain the lock. Additionally a thread
     * is allowed to gain more than one <code>writeLock</code>, as long as it doesn't attempt to
     * gain additional <code>writeLock</code> s from within unit notification. Attempting to gain
     * a *<code>writeLock</code> from within a UnitListener notification will result in an
     * <code>IllegalStateException</code>. The ability to obtain more than one
     * <code>writeLock</code> per thread allows subclasses to gain a writeLock, perform a number
     * of operations, then release the lock.
     * <p>
     * Calls to <code>writeLock</code> must be balanced with calls to <code>writeUnlock</code>,
     * else the <code>Unit</code> will be left in a locked state so that no reading or writing can
     * be done.
     * 
     * @exception IllegalStateException thrown on illegal lock attempt. If the unit is implemented
     *                properly, this can only happen if a unit listener attempts to mutate the unit.
     *                This situation violates the bean event model where order of delivery is not
     *                guaranteed and all listeners should be notified before further mutations are
     *                allowed.
     */
    public abstract void writeLock(UndoEvent event);

    /**
     * Releases a write lock previously obtained via <code>writeLock</code>. After decrementing
     * the lock count if there are no oustanding locks this will allow a new writer, or readers.
     * 
     * @return true iff the last pending write lock was completely release.
     * @see #writeLock
     */
    public abstract boolean writeUnlock(UndoEvent event);

    /**
     * Return true if and only if the unit is currently locked by a write lock
     * 
     * @see #writeLock
     */
    public abstract boolean isWriteLocked();

    /**
     * Destroy this unit & cleanup any resources or registrations that this unit may have.
     */
    public abstract void destroy();

    //--------------------------------------------------------------------------------------- Events
    /*

    protected ArrayList listeners = new ArrayList();
    boolean notifyingListeners;

    public void addNodeChangeListener(Node node, NodeChangeListener l) {
        listeners.add(new NodeChangeListener.Pair(node, l));
    }

    public void removeNodeChangeListener(NodeChangeListener l) {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            NodeChangeListener.Pair p = (NodeChangeListener.Pair)i.next();
            if (p.listener == l)
                i.remove();
        }
    }

    public NodeChangeListener.Pair[] getNodeChangeListeners() {
        return (NodeChangeListener.Pair[])listeners.toArray(NodeChangeListener.Pair.EMPTY_ARRAY);
    }

    protected synchronized void fireChangeEvent(NodeChangeEvent e) {
        notifyingListeners = true;
        // fire e to every listener associated with owner or changed node...
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            NodeChangeListener.Pair p = (NodeChangeListener.Pair)i.next();
            if (p.node == null || p.node == e.owner || p.node == e.target) {
                switch (e.change) {
                case NodeChangeEvent.MODIFIED:
                    p.listener.nodeModified(e);
                    break;
                case NodeChangeEvent.ADDED:
                    p.listener.nodeAdded(e);
                    break;
                case NodeChangeEvent.REMOVED:
                    p.listener.nodeRemoved(e);
                    break;
                }
            }
        }
        notifyingListeners = false;
    }

    protected synchronized void fireChangeEvent(Node owner, Node changed, int change) {
        if (!listeners.isEmpty())
            fireChangeEvent(new NodeChangeEvent(this, owner, changed, change));
    }
    */

    //---------------------------------------------------------------------------------------- Debug

    /**
     * Debug method to dump diagnostic info of this unit to a PrintWriter
     * 
     * @param w The PrintWriter to dump debug info to
     */
    public abstract void dumpTo(PrintWriter w);
}
