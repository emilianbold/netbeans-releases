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

package org.netbeans.modules.form;

import java.awt.event.*;
import java.beans.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.DefaultEditorKit;

import org.openide.*;
import org.openide.actions.PasteAction;
import org.openide.nodes.*;
import org.openide.explorer.*;
import org.openide.awt.UndoRedo;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.datatransfer.*;

import org.netbeans.modules.form.actions.TestAction;
import org.netbeans.modules.form.palette.PaletteUtils;

/**
 * The ComponentInspector - special explorer for form editor.
 *
 * @author Tomas Pavek
 */

public class ComponentInspector extends TopComponent
                                implements ExplorerManager.Provider
{
    private ExplorerManager explorerManager;

    private TestAction testAction = SystemAction.findObject(TestAction.class, true);

    private PasteAction pasteAction = SystemAction.findObject(PasteAction.class, true);

    private CopyCutActionPerformer copyActionPerformer = new CopyCutActionPerformer(true);
    private CopyCutActionPerformer cutActionPerformer = new CopyCutActionPerformer(false);
    private DeleteActionPerformer deleteActionPerformer = new DeleteActionPerformer();

    private ClipboardListener clipboardListener;

    /** Currently focused form or null if no form is opened/focused */
    private FormEditor focusedForm;

    private EmptyInspectorNode emptyInspectorNode;
    
    private BeanTreeView treeView;

    /** Default icon base for control panel. */
    private static final String EMPTY_INSPECTOR_ICON_BASE =
        "org/netbeans/modules/form/resources/emptyInspector.gif"; // NOI18N

    /** The icon for ComponentInspector */
    private static final String iconURL =
        "org/netbeans/modules/form/resources/inspector.png"; // NOI18N

    private static ComponentInspector instance;

    // ------------
    // construction (ComponentInspector is a singleton)

    /** Gets default instance. Don't use directly, it reserved for '.settings' file only,
     * i.e. deserialization routines, otherwise you can get non-deserialized instance.
     * 
     * @return ComponentInspector singleton.
     */
    public static synchronized ComponentInspector getDefault() {
        if (instance == null)
            instance = new ComponentInspector();
        return instance;
    }

    /** Finds default instance. Use in client code instead of {@link #getDefault()}.
     * 
     * @return ComponentInspector singleton.
     */
    public static synchronized ComponentInspector getInstance() {
        if (instance == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("ComponentInspector"); // NOI18N
            if (instance == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                    "Can not find ComponentInspector component for its ID. Returned " + tc)); // NOI18N
                instance = new ComponentInspector();
            }
        }
        return instance;
    }

    static boolean exists() {
        return instance != null;
    }

    /** Overriden to explicitely set persistence type of ComponentInspector
     * to PERSISTENCE_ALWAYS */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    private ComponentInspector() {
        explorerManager = new ExplorerManager();

        associateLookup(
            ExplorerUtils.createLookup(explorerManager, setupActionMap(getActionMap()))
        );

        emptyInspectorNode = new EmptyInspectorNode();
        explorerManager.setRootContext(emptyInspectorNode);

        explorerManager.addPropertyChangeListener(new NodeSelectionListener());

        setLayout(new java.awt.BorderLayout());
        createComponents();

        setIcon(ImageUtilities.loadImage(iconURL));
        setName(FormUtils.getBundleString("CTL_InspectorTitle")); // NOI18N
        setToolTipText(FormUtils.getBundleString("HINT_ComponentInspector")); // NOI18N
    }

    javax.swing.ActionMap setupActionMap(javax.swing.ActionMap map) {
        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer);
        //map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", deleteActionPerformer); // NOI18N

        return map;
    }

    private void createComponents() {
        treeView = new BeanTreeView();
        treeView.setDragSource(true);
        treeView.setDropTarget(true);
        treeView.getAccessibleContext().setAccessibleName(
            FormUtils.getBundleString("ACS_ComponentTree")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_ComponentTree")); // NOI18N
        add(java.awt.BorderLayout.CENTER, treeView);
    }

    // --------------
    // overriding superclasses, implementing interfaces

    // ExplorerManager.Provider
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public UndoRedo getUndoRedo() {
        UndoRedo ur = focusedForm != null ?
                          focusedForm.getFormUndoRedoManager() : null;
        return ur != null ? ur : super.getUndoRedo();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.component-inspector"); // NOI18N
    }

    /** Replaces this in object stream.
     * 
     * @return ResolvableHelper
     */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected void componentActivated() {
        attachActions();
    }

    @Override
    protected void componentDeactivated() {
        detachActions();
    }

    // ------------
    // activating and focusing

    synchronized void attachActions() {
        ExplorerUtils.activateActions(explorerManager, true);
        updatePasteAction();

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            if (clipboardListener == null)
                clipboardListener = new ClipboardChangesListener();
            clip.addClipboardListener(clipboardListener);
        }
    }

    synchronized void detachActions() {
        ExplorerUtils.activateActions(explorerManager, false);

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            clip.removeClipboardListener(clipboardListener);
        }
    }

    /** This method focuses the ComponentInspector on given form.
     * @param form the form to focus on
     */
    public void focusForm(final FormEditor form) {
        if (focusedForm != form)
            focusFormInAwtThread(form, 0);
    }

    /** This method focuses the ComponentInspector on given form.
     * @param form the form to focus on
     * @param visible true to open inspector, false to close
     */
    public void focusForm(final FormEditor form, boolean visible) {
        if (focusedForm != form)
            focusFormInAwtThread(form, visible ? 1 : -1);
    }

    private void focusFormInAwtThread(final FormEditor form,
                                      final int visibility) {
        if (java.awt.EventQueue.isDispatchThread()) {
            focusFormImpl(form, visibility);
        }
        else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    focusFormImpl(form, visibility);
                }
            });
        }
    }

    private void focusFormImpl(FormEditor form, int visibility) {
        focusedForm = form;

        if ((form == null) || (form.getFormDesigner() == null)) {
            testAction.setFormDesigner(null);
            PaletteUtils.setContext(null);

            // swing memory leak workaround
            removeAll();
            createComponents();
            revalidate();

            getExplorerManager().setRootContext(emptyInspectorNode);
        }
        else {
            Node[] selectedNodes = form.getFormDesigner().getSelectedComponentNodes();

            testAction.setFormDesigner(form.getFormDesigner());
            PaletteUtils.setContext(form.getFormDataObject().getPrimaryFile());

            Node formNode = form.getFormRootNode();
            if (formNode == null) { // form not loaded yet, should not happen
                System.err.println("Warning: FormEditorSupport.getFormRootNode() returns null"); // NOI18N
                getExplorerManager().setRootContext(emptyInspectorNode);
            }
            else
                getExplorerManager().setRootContext(formNode);
            
            try {
                getExplorerManager().setSelectedNodes(selectedNodes);
            } catch (PropertyVetoException ex) {                
                ex.printStackTrace();   // should not happen
            }
                        
        }

        if (visibility > 0)
            open();
        else if (visibility < 0)
            close();
    }

    public FormEditor getFocusedForm() {
        return focusedForm;
    }

    /** Called to synchronize with FormDesigner. Invokes NodeSelectionListener.
     */
    void setSelectedNodes(Node[] nodes, FormEditor form)
        throws PropertyVetoException
    {
        if (form == focusedForm)
            getExplorerManager().setSelectedNodes(nodes);
    }

    public Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    private Node[] getSelectedRootNodes() {
        // exclude nodes that are under other selected nodes
        Node[] selected = getExplorerManager().getSelectedNodes();
        if (selected != null && selected.length > 1) {
            List<Node> list = new ArrayList<Node>(selected.length);
            for (int i=0; i < selected.length; i++) {
                Node node = selected[i];
                boolean subcontained = false;
                for (int j=0; j < selected.length; j++) {
                    if (j != i && isSubcontainedNode(node, selected[j])) {
                        subcontained = true;
                        break;
                    }
                }
                if (!subcontained) {
                    list.add(node);
                }
            }
            if (list.size() < selected.length) {
                selected = list.toArray(new Node[list.size()]);
            }
        }
        return selected;
    }

    private static boolean isSubcontainedNode(Node node, Node maybeParent) {
        RADComponentCookie cookie = node.getCookie(RADComponentCookie.class);
        RADComponent comp = (cookie != null) ? cookie.getRADComponent() : null;
        if (comp != null) {
            cookie = maybeParent.getCookie(RADComponentCookie.class);
            RADComponent parentComp = (cookie != null) ? cookie.getRADComponent() : null;
            if (parentComp != null && parentComp.isParentComponent(comp)) {
                return true;
            }
        }
        return false;
    }

    // ---------------
    // actions
    
    // fix of issue 42082
    private void updatePasteAction() {
        if(java.awt.EventQueue.isDispatchThread()) {
            updatePasteActionInAwtThread();
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updatePasteActionInAwtThread();
                }
            });
        }
    }

    private void updatePasteActionInAwtThread() {
        Node[] selected = getExplorerManager().getSelectedNodes();
        if (selected != null && selected.length >= 1) {
            // pasting considered only on the first selected node
            Clipboard clipboard = getClipboard();
            Transferable trans = clipboard.getContents(this); // [this??]
            if (trans != null) {
                Node node = selected[0];
                PasteType[] pasteTypes = node.getPasteTypes(trans);
                if (pasteTypes.length != 0) {
                    // transfer accepted by the node, we are done
                    pasteAction.setPasteTypes(pasteTypes);
                    return;
                }

                boolean multiFlavor = false;
                try {
                    multiFlavor = trans.isDataFlavorSupported(
                                    ExTransferable.multiFlavor);
                }
                catch (Exception e) {} // ignore, should not happen

                if (multiFlavor) {
                    // The node did not accept whole multitransfer as is - try
                    // to break it into individual transfers and paste them in
                    // sequence instead.
                    try {
                        MultiTransferObject mto = (MultiTransferObject)
                            trans.getTransferData(ExTransferable.multiFlavor);

                        int n = mto.getCount(), i;
                        Transferable[] t = new Transferable[n];
                        PasteType[] p = new PasteType[n];

                        for (i=0; i < n; i++) {
                            t[i] = mto.getTransferableAt(i);
                            pasteTypes = node.getPasteTypes(t[i]);
                            if (pasteTypes.length == 0)
                                break;

                            p[i] = pasteTypes[0]; // ??
                        }

                        if (i == n) { // all individual transfers accepted
                            pasteAction.setPasteTypes(
                                new PasteType[] { new MultiPasteType(t, p) });
                            return;
                        }
                    }
                    catch (Exception ex) {
                        org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }

        pasteAction.setPasteTypes(null);
    }

    private Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(java.awt.datatransfer.Clipboard.class);
        if (c == null)
            c = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        return c;
    }
    
    @Override
    protected String preferredID() {
        return getClass().getName();
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return treeView.requestFocusInWindow();
    }

    // ---------------
    // innerclasses

    // listener on nodes selection (ExplorerManager)
    private class NodeSelectionListener implements PropertyChangeListener,
                                                   ActionListener, Runnable
    {
        private javax.swing.Timer timer;

        NodeSelectionListener() {
            timer = new javax.swing.Timer(150, this);
            timer.setCoalesce(true);
            timer.setRepeats(false);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;            
            
            FormDesigner designer;
            if (focusedForm == null
                    || (designer = focusedForm.getFormDesigner()) == null)
                return;

            Node[] selectedNodes = getExplorerManager().getSelectedNodes();

            if (designer.getDesignerMode() == FormDesigner.MODE_CONNECT) {
                // specially handle node selection in connection mode
                if (selectedNodes.length > 0) {
                    RADComponentCookie cookie = selectedNodes[0].getCookie(RADComponentCookie.class);
                    if (cookie != null
                        && cookie.getRADComponent() == designer.getConnectionSource()
                        && selectedNodes.length > 1)
                    {
                        cookie = selectedNodes[selectedNodes.length-1]
                                .getCookie(RADComponentCookie.class);
                    }
                    if (cookie != null)
                        designer.connectBean(cookie.getRADComponent(), true);
                }
            }
            else if (evt.getSource() == ComponentInspector.this.getExplorerManager())
            {   // the change comes from ComponentInspector => synchronize FormDesigner
                designer.clearSelectionImpl();
                for (int i=0; i < selectedNodes.length; i++) {
                    FormCookie formCookie = selectedNodes[i].getCookie(FormCookie.class);
                    if (formCookie != null) {
                        Node node = formCookie.getOriginalNode();
                        if (node instanceof RADComponentNode)
                            designer.addComponentToSelectionImpl(
                                ((RADComponentNode)node).getRADComponent());
                    }
                }
                designer.repaintSelection();
            }

            // refresh nodes' lookup with current set of cookies
            for (int i=0; i < selectedNodes.length; i++)
                ((FormNode)selectedNodes[i]).updateCookies();

            // restart waiting for expensive part of the update
            timer.restart();
        }

        @Override
        public void actionPerformed(ActionEvent evt) { // invoked by Timer
            java.awt.EventQueue.invokeLater(this); // replan to EventQueue thread
        }

        /** Updates activated nodes and actions. It is executed via timer 
         * restarted each time a new selection change appears - if they come
         * quickly e.g. due to the user is holding a cursor key, this
         * (relatively time expensive update) is done only at the end.
         */
        @Override
        public void run() {
            Node[] selectedNodes = getExplorerManager().getSelectedNodes();
            setActivatedNodes(selectedNodes);
            // set activated nodes also on FormDesigner - to keep it in sync
            FormDesigner designer = focusedForm != null ?
                                    focusedForm.getFormDesigner() : null;
            if (designer != null)
                designer.setActivatedNodes(selectedNodes);

            updatePasteAction();

            timer.stop();
        }
    }

    // listener on clipboard changes
    private class ClipboardChangesListener implements ClipboardListener {
        @Override
        public void clipboardChanged(ClipboardEvent ev) {
            if (!ev.isConsumed())
                updatePasteAction();
        }
    }

    // performer for DeleteAction
    private class DeleteActionPerformer extends javax.swing.AbstractAction
                                        implements ActionPerformer, Mutex.Action<Object>
    {
        private Node[] nodesToDestroy;

        @Override
        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        @Override
        public void performAction(SystemAction action) {
            Node[] selected = getSelectedRootNodes();

            if (selected == null || selected.length == 0)
                return;

            for (int i=0; i < selected.length; i++)
                if (!selected[i].canDestroy())
                    return;

            try { // clear nodes selection first
                getExplorerManager().setSelectedNodes(new Node[0]);
            }
            catch (PropertyVetoException e) {} // cannot be vetoed

            nodesToDestroy = selected;
            if (java.awt.EventQueue.isDispatchThread())
                doDelete();
            else // reinvoke synchronously in AWT thread
                Mutex.EVENT.readAccess(this);
        }

        @Override
        public Object run() {
            doDelete();
            return null;
        }

        private void doDelete() {
            if (nodesToDestroy != null) {
                for (int i=0; i < nodesToDestroy.length; i++) {
                    try {
                        nodesToDestroy[i].destroy();
                    }
                    catch (java.io.IOException ex) { // should not happen
                        ex.printStackTrace();
                    }
                }
                nodesToDestroy = null;
            }
        }
    }
    
    // performer for CopyAction and CutAction
    private class CopyCutActionPerformer extends javax.swing.AbstractAction
                                         implements ActionPerformer
    {
        private boolean copy;

        public CopyCutActionPerformer(boolean copy) {
            this.copy = copy;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        @Override
        public void performAction(SystemAction action) {
            Transferable trans;
            Node[] selected = getSelectedRootNodes();

            if (selected == null || selected.length == 0)
                trans = null;
            else if (selected.length == 1)
                trans = getTransferableOwner(selected[0]);
            else {
                Transferable[] transArray = new Transferable[selected.length];
                for (int i=0; i < selected.length; i++)
                    if ((transArray[i] = getTransferableOwner(selected[i]))
                                                                     == null)
                        return;

                trans = new ExTransferable.Multi(transArray);
            }

            if (trans != null) {
                Clipboard clipboard = getClipboard();
                clipboard.setContents(trans, new StringSelection("")); // NOI18N
            }
        }

        private Transferable getTransferableOwner(Node node) {
            try {
                return copy ? node.clipboardCopy() : node.clipboardCut();
            }
            catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return null;
            }
        }
    }

    // paste type used for ExTransferable.Multi
    private static class MultiPasteType extends PasteType
                                 implements Mutex.ExceptionAction<Transferable>
    {
        private Transferable[] transIn;
        private PasteType[] pasteTypes;

        MultiPasteType(Transferable[] t, PasteType[] p) {
            transIn = t;
            pasteTypes = p;
        }

        // performs the paste action
        @Override
        public Transferable paste() throws java.io.IOException {
            if (java.awt.EventQueue.isDispatchThread())
                return doPaste();
            else { // reinvoke synchronously in AWT thread
                try {
                    return Mutex.EVENT.readAccess(this);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof java.io.IOException)
                        throw (java.io.IOException) e;
                    else { // should not happen, ignore
                        e.printStackTrace();
                        return ExTransferable.EMPTY;
                    }
                }
            }
        }

        @Override
        public Transferable run() throws Exception {
            return doPaste();
        }

        private Transferable doPaste() throws java.io.IOException {
            Transferable[] transOut = new Transferable[transIn.length];
            for (int i=0; i < pasteTypes.length; i++) {
                Transferable newTrans = pasteTypes[i].paste();
                transOut[i] = newTrans != null ? newTrans : transIn[i];
            }
            return new ExTransferable.Multi(transOut);
        }
    }

    // -----------

    // node for empty ComponentInspector
    private static class EmptyInspectorNode extends AbstractNode {
        public EmptyInspectorNode() {
            super(Children.LEAF);
            setIconBaseWithExtension(EMPTY_INSPECTOR_ICON_BASE);
        }
        @Override
        public boolean canRename() {
            return false;
        }
    }

    final public static class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7424646018839457544L;
        public Object readResolve() {
            return ComponentInspector.getDefault();
        }
    }
}
