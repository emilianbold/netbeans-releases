/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.openide.explorer;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Timer;
import org.netbeans.modules.openide.explorer.ExternalDragAndDrop;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.util.datatransfer.PasteType;

/**
 * This class contains the default implementation of reactions to the standard
 * explorer actions. It can be attached to any {@link ExplorerManager}. Then
 * this class will listen to changes of selected nodes or the explored context
 * of that manager, and update the state of cut/copy/paste/delete actions.  <P>
 * An instance of this class can only be attached to one manager at a time. Use
 * {@link #attach} and {@link #detach} to make the connection.
 *
 * @author Jan Jancura, Petr Hamernik, Ian Formanek, Jaroslav Tulach
 */
final class ExplorerActionsImpl {
    /** copy action performer */
    private final CopyCutActionPerformer copyActionPerformer = new CopyCutActionPerformer(true);

    /** cut action performer */
    private final CopyCutActionPerformer cutActionPerformer = new CopyCutActionPerformer(false);

    /** delete action performer */
    private final DeleteActionPerformer deleteActionPerformerConfirm = new DeleteActionPerformer(true);

    /** delete action performer no confirm */
    private final DeleteActionPerformer deleteActionPerformerNoConfirm = new DeleteActionPerformer(false);

    /** own paste action */
    private final OwnPaste pasteActionPerformer = new OwnPaste();
    private ActionStateUpdater actionStateUpdater;

    /** the manager we are listening on */
    private ExplorerManager manager;

    /** Creates new instance with a decision whether the action should update
     * performers (the old behaviour) or only set the state of cut,copy,delete,
     * and paste actions.
     */
    ExplorerActionsImpl() {
    }

    //
    // Implementation
    //

    /** Getter for the copy action.
     */
    final Action copyAction() {
        return copyActionPerformer;
    }

    /** The cut action */
    final Action cutAction() {
        return cutActionPerformer;
    }

    /** The delete action
     */
    final Action deleteAction(boolean confirm) {
        return confirm ? deleteActionPerformerConfirm : deleteActionPerformerNoConfirm;
    }

    /** Own paste action
     */
    final Action pasteAction() {
        return pasteActionPerformer;
    }

    /** Attach to new manager.
     * @param m the manager to listen on
     */
    public synchronized void attach(ExplorerManager m) {
        if (manager != null) {
            // first of all detach
            detach();
        }

        manager = m;

        // Sets action state updater and registers listening on manager and
        // exclipboard.
        actionStateUpdater = new ActionStateUpdater(manager);

        Clipboard c = getClipboard();

        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            clip.addClipboardListener(
                WeakListeners.create(
                    ClipboardListener.class, actionStateUpdater, clip
                )
            );
        }

        updateActions(true);
    }

    /** Detach from manager currently being listened on. */
    public synchronized void detach() {
        if (manager == null || actionStateUpdater == null) {
            return;
        }

        // Unregisters (weak) listening on manager and exclipboard (see attach).
        actionStateUpdater.unlisten(manager);
        actionStateUpdater = null;

        stopActions();

        manager = null;
    }

    /** Stops listening on all actions */
    private void stopActions() {
        if (copyActionPerformer != null) {
            copyActionPerformer.setEnabled(false);
            cutActionPerformer.setEnabled(false);
            deleteActionPerformerConfirm.setEnabled(false);
            deleteActionPerformerNoConfirm.setEnabled(false);
            pasteActionPerformer.setEnabled(false);
        }
    }

    /** Updates the state of all actions.
     * @param path list of selected nodes
     */
    private void updateActions(boolean updatePasteAction) {
        if (manager == null) {
            return;
        }

        Node[] path = manager.getSelectedNodes();

        int i;
        int k = (path != null) ? path.length : 0;

        if (k > 0) {
            boolean incest = false;

            if (k > 1) {
                // Do a special check for parenthood. Affects delete (for a long time),
                // copy (#13418), cut (#13426). If one node is a parent of another,
                // assume that the situation is sketchy and prevent it.
                // For k==1 it is impossible so do not waste time on it.
                HashMap<Node, Object> allNodes = new HashMap<Node, Object>(101);

                for (i = 0; i < k; i++) {
                    if (!checkParents(path[i], allNodes)) {
                        incest = true;

                        break;
                    }
                }
            }

            for (i = 0; i < k; i++) {
                if (incest || !path[i].canCopy()) {
                    copyActionPerformer.setEnabled(false);

                    break;
                }
            }

            if (i == k) {
                copyActionPerformer.setEnabled(true);
            }

            for (i = 0; i < k; i++) {
                if (incest || !path[i].canCut()) {
                    cutActionPerformer.setEnabled(false);

                    break;
                }
            }

            if (i == k) {
                cutActionPerformer.setEnabled(true);
            }

            for (i = 0; i < k; i++) {
                if (incest || !path[i].canDestroy()) {
                    deleteActionPerformerConfirm.setEnabled(false);
                    deleteActionPerformerNoConfirm.setEnabled(false);

                    break;
                }
            }

            if (i == k) {
                deleteActionPerformerConfirm.setEnabled(true);
                deleteActionPerformerNoConfirm.setEnabled(true);
            }
        } else { // k==0, i.e. no nodes selected
            copyActionPerformer.setEnabled(false);
            cutActionPerformer.setEnabled(false);
            deleteActionPerformerConfirm.setEnabled(false);
            deleteActionPerformerNoConfirm.setEnabled(false);
        }

        if (updatePasteAction) {
            updatePasteAction(path);
        }
    }

    /** Adds all parent nodes into the set.
     * @param set set of all nodes
     * @param node the node to check
     * @return false if one of the nodes is parent of another
     */
    private boolean checkParents(Node node, HashMap<Node, Object> set) {
        if (set.get(node) != null) {
            return false;
        }

        // this signals that this node is the original one
        set.put(node, this);

        for (;;) {
            node = node.getParentNode();

            if (node == null) {
                return true;
            }

            if (set.put(node, node) == this) {
                // our parent is a node that is also in the set
                return false;
            }
        }
    }

    /** Updates paste action.
    * @param path selected nodes
    */
    private void updatePasteAction(Node[] path) {
        ExplorerManager man = manager;

        if (man == null) {
            pasteActionPerformer.setPasteTypes(null);

            return;
        }

        if ((path != null) && (path.length > 1)) {
            pasteActionPerformer.setPasteTypes(null);

            return;
        } else {
            Node node = man.getExploredContext();
            Node[] selectedNodes = man.getSelectedNodes();

            if ((selectedNodes != null) && (selectedNodes.length == 1)) {
                node = selectedNodes[0];
            }

            if (node != null) {
                try {
                    Transferable trans = getClipboard().getContents(this);
                    updatePasteTypes(trans, node);
                } catch (NullPointerException npe) {
                    Logger.getLogger (ExplorerActionsImpl.class.getName ()).
                        log (Level.INFO, "Caused by http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6322854", npe);
                }
            }
        }
    }

    /** Actually updates paste types. */
    private void updatePasteTypes(Transferable trans, Node pan) {
        if (trans != null) {
            // First, just ask the node if it likes this transferable, whatever it may be.
            // If it does, then fine.
            PasteType[] pasteTypes = (pan == null) ? new PasteType[] {  } : pan.getPasteTypes(trans);

            if (pasteTypes.length != 0) {
                pasteActionPerformer.setPasteTypes(pasteTypes);

                return;
            }

            if (trans.isDataFlavorSupported(ExTransferable.multiFlavor)) {
                // The node did not accept this multitransfer as is--try to break it into
                // individual transfers and paste them in sequence instead.
                try {
                    MultiTransferObject obj = (MultiTransferObject) trans.getTransferData(ExTransferable.multiFlavor);
                    int count = obj.getCount();
                    boolean ok = true;
                    Transferable[] t = new Transferable[count];
                    PasteType[] p = new PasteType[count];

                    for (int i = 0; i < count; i++) {
                        t[i] = obj.getTransferableAt(i);
                        pasteTypes = (pan == null) ? new PasteType[] {  } : pan.getPasteTypes(t[i]);

                        if (pasteTypes.length == 0) {
                            ok = false;

                            break;
                        }

                        // [PENDING] this is ugly! ideally should be some way of comparing PasteType's for similarity?
                        p[i] = pasteTypes[0];
                    }

                    if (ok) {
                        PasteType[] arrOfPaste = new PasteType[] { new MultiPasteType(t, p) };
                        pasteActionPerformer.setPasteTypes(arrOfPaste);

                        return;
                    }
                } catch (UnsupportedFlavorException e) {
                    // [PENDING] notify?!
                } catch (IOException e) {
                    // [PENDING] notify?!
                }
            }
        }

        pasteActionPerformer.setPasteTypes(null);
    }

    /** If our clipboard is not found return the default system clipboard. */
    private static Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(Clipboard.class);

        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        return c;
    }

    /** Updates actions state via updater (if the updater is present). */
    private void updateActionsState() {
        ActionStateUpdater asu;

        synchronized (this) {
            asu = actionStateUpdater;
        }

        if (asu != null) {
            asu.update();
        }
    }

    private boolean actionsUpdateScheduled() {
        ActionStateUpdater asu = actionStateUpdater;
        return asu != null ? asu.updateScheduled() : false;
    }

    /** Paste type used when in clipbopard is MultiTransferable */
    private static class MultiPasteType extends PasteType {
        /** Array of transferables */
        Transferable[] t;

        /** Array of paste types */
        PasteType[] p;

        /** Constructs new MultiPasteType for the given content of the clipboard */
        MultiPasteType(Transferable[] t, PasteType[] p) {
            this.t = t;
            this.p = p;
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should be cleared.
        */
        public Transferable paste() throws IOException {
            int size = p.length;
            Transferable[] arr = new Transferable[size];

            for (int i = 0; i < size; i++) {
                Transferable newTransferable = p[i].paste();

                if (newTransferable != null) {
                    arr[i] = newTransferable;
                } else {
                    // keep the orginal
                    arr[i] = t[i];
                }
            }

            return new ExTransferable.Multi(arr);
        }
    }

    /** Own implementation of paste action
     */
    private class OwnPaste extends AbstractAction {
        private PasteType[] pasteTypes;

        OwnPaste() {
        }

        @Override
        public boolean isEnabled() {
            updateActionsState();

            return super.isEnabled();
        }

        public void setPasteTypes(PasteType[] arr) {
            synchronized (this) {
                this.pasteTypes = arr;
            }

            setEnabled(arr != null);
        }

        public void actionPerformed(ActionEvent e) {
            PasteType[] arr = this.pasteTypes;
            throw new IllegalStateException(
                "Should not be invoked at all. Paste types: " + (arr == null ? null : Arrays.asList(arr)) // NOI18N
            );
        }

        @Override
        public Object getValue(String s) {
            updateActionsState();

            if ("delegates".equals(s)) { // NOI18N

                return pasteTypes;
            }

            return super.getValue(s);
        }
    }

    /** Class which performs copy and cut actions */
    private class CopyCutActionPerformer extends AbstractAction {
        /** determine if adapter is used for copy or cut action. */
        private boolean copyCut;

        /** Create new adapter */
        public CopyCutActionPerformer(boolean b) {
            copyCut = b;
        }

        @Override
        public boolean isEnabled() {
            if (actionsUpdateScheduled()) {
                updateActions(false);
            }
            return super.isEnabled();
        }

        public void actionPerformed(ActionEvent ev) {
            Transferable trans = null;
            ExplorerManager em = manager;
            if (em == null) {
                return;
            }
            Node[] sel = em.getSelectedNodes();

            if (sel.length != 1) {
                Transferable[] arrayTrans = new Transferable[sel.length];

                for (int i = 0; i < sel.length; i++) {
                    if ((arrayTrans[i] = getTransferableOwner(sel[i])) == null) {
                        return;
                    }
                }

                trans = ExternalDragAndDrop.maybeAddExternalFileDnd( new ExTransferable.Multi(arrayTrans) );
            } else {
                trans = getTransferableOwner(sel[0]);
            }

            if (trans != null) {
                Clipboard clipboard = getClipboard();

                clipboard.setContents(trans, new StringSelection("")); // NOI18N
            }
        }

        private Transferable getTransferableOwner(Node node) {
            try {
                return copyCut ? node.clipboardCopy() : node.clipboardCut();
            } catch (IOException e) {
                Logger.getLogger(ExplorerActionsImpl.class.getName()).log(Level.WARNING, null, e);

                return null;
            }
        }
    }

    /** Class which performs delete action */
    private class DeleteActionPerformer extends AbstractAction implements Runnable {
        private boolean confirmDelete;

        DeleteActionPerformer(boolean confirmDelete) {
            this.confirmDelete = confirmDelete;
        }

        @Override
        public boolean isEnabled() {
            if (actionsUpdateScheduled()) {
                updateActions(false);
            }
            return super.isEnabled();
        }

        public void actionPerformed(ActionEvent ev) {
            ExplorerManager em = manager;
            if (em == null) {
                return;
            }

            final Node[] sel = em.getSelectedNodes();
            if ((sel == null) || (sel.length == 0)) {
                return;
            }

            for (ExtendedDelete del : Lookup.getDefault().lookupAll(ExtendedDelete.class)) {
                try {
                    if (del.delete(sel)) return;
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                    return;
                }
            }
            
            // perform action if confirmed
            if (!confirmDelete || doConfirm(sel)) {
                // clear selected nodes
                try {
                    em.setSelectedNodes(new Node[]{});
                } catch (PropertyVetoException e) {
                // never thrown, setting empty selected nodes cannot be vetoed
                }

                doDestroy(sel);

                // disables the action in AWT thread
                Mutex.EVENT.readAccess(this);
            }
        }

        /** Disables the action.
         */
        public void run() {
            setEnabled(false);
        }

        private boolean doConfirm(Node[] sel) {
            String message;
            String title;
            boolean customDelete = true;

            for (int i = 0; i < sel.length; i++) {
                if (!Boolean.TRUE.equals(sel[i].getValue("customDelete"))) { // NOI18N
                    customDelete = false;

                    break;
                }
            }

            if (customDelete) {
                return true;
            }

            if (sel.length == 1) {
                message = NbBundle.getMessage(
                        ExplorerActionsImpl.class, "MSG_ConfirmDeleteObject", sel[0].getDisplayName()
                    );
                title = NbBundle.getMessage(ExplorerActionsImpl.class, "MSG_ConfirmDeleteObjectTitle");
            } else {
                message = NbBundle.getMessage(
                        ExplorerActionsImpl.class, "MSG_ConfirmDeleteObjects", Integer.valueOf(sel.length)
                    );
                title = NbBundle.getMessage(ExplorerActionsImpl.class, "MSG_ConfirmDeleteObjectsTitle");
            }

            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);

            return NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc));
        }

        private void doDestroy(final Node[] sel) {
            for (int i = 0; i < sel.length; i++) {
                try {
                    sel[i].destroy();
                }
                catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }

    }

    /** Class which register changes in manager, and clipboard, coalesces
     * them if they are frequent and performs the update of actions state. */
    private class ActionStateUpdater implements PropertyChangeListener, ClipboardListener, ActionListener, Runnable {
        private final Timer timer;
        private final PropertyChangeListener weakL;

        ActionStateUpdater(ExplorerManager m) {
            timer = new Timer(200, this);
            timer.setCoalesce(true);
            timer.setRepeats(false);
            weakL = WeakListeners.propertyChange(this, m);
            m.addPropertyChangeListener(weakL);
        }

        void unlisten(ExplorerManager m) {
            m.removePropertyChangeListener(weakL);
        }

        boolean updateScheduled() {
            return timer.isRunning();
        }

        @Override
        public synchronized void propertyChange(PropertyChangeEvent e) {
            timer.restart();
        }

        @Override
        public void clipboardChanged(ClipboardEvent ev) {
            if (!ev.isConsumed()) {
                Mutex.EVENT.readAccess(this);
            }
        }

        @Override
        public void run() {
            ExplorerManager em = manager;

            if (em != null) {
                updatePasteAction(em.getSelectedNodes());
            }
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            updateActions(true);
        }

        /** Updates actions states now if there is pending event. */
        public void update() {
            if (timer.isRunning()) {
                timer.stop();
                updateActions(true);
            }
        }
    }
}
