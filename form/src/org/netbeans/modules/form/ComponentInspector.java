/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.MessageFormat;
import java.awt.datatransfer.*;
import javax.swing.undo.*;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.explorer.*;
//import org.openide.explorer.propertysheet.*; // TEMP
//import org.openide.awt.SplittedPanel; // TEMP
import org.openide.awt.UndoRedo;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.*;

import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.palette.*;

/**
 * The ComponentInspector - special explorer.
 *
 * @author Tomas Pavek
 */

/* [ComponentInspector uses its own (simplified) implementation of action
 *  performes - like org.openide.explorer.ExplorerActions (rev. 1.47) - to be
 *  able to deal with multiple components operations - like delete and paste.
 *  This is necessary for correct undo/redo behavior (compound changes).]
 */

public class ComponentInspector extends ExplorerPanel implements Serializable
{
    private TestAction testAction = (TestAction)
                SystemAction.findObject(TestAction.class, true);
    private FormEditorAction inspectorAction = (FormEditorAction)
                SystemAction.findObject(FormEditorAction.class, true);
    private ReloadAction reloadAction = (ReloadAction)
                SystemAction.findObject(ReloadAction.class, true);

    private DeleteAction deleteAction = (DeleteAction)
                SystemAction.findObject(DeleteAction.class, true);
    private CopyAction copyAction = (CopyAction)
                SystemAction.findObject(CopyAction.class, true);
    private CutAction cutAction = (CutAction)
                SystemAction.findObject(CutAction.class, true);
    private PasteAction pasteAction = (PasteAction)
                SystemAction.findObject(PasteAction.class, true);

    private ActionPerformer copyActionPerformer = new CopyCutActionPerformer(true);
    private ActionPerformer cutActionPerformer = new CopyCutActionPerformer(false);
    private ActionPerformer deleteActionPerformer = new DeleteActionPerformer();

    private boolean actionsAttached = false;

    private ClipboardListener clipboardListener;

    /** Currently focused form or null if no form is opened/focused */
    private FormEditorSupport focusedForm;

    private boolean dontSynchronizeSelectedNodes = false;

//    private SplittedPanel split; // TEMP
//    private PropertySheetView sheet; // TEMP

    private static EmptyInspectorNode emptyInspectorNode;

    /** Default icon base for control panel. */
    private static final String EMPTY_INSPECTOR_ICON_BASE =
        "org/netbeans/modules/form/resources/emptyInspector"; // NOI18N

    /** The icon for ComponentInspector */
    private static final String iconURL =
        "org/netbeans/modules/form/resources/inspector.gif"; // NOI18N

    /** The name "Component Inspector" */
    private static String INSPECTOR_TITLE;

    private static ComponentInspector instance;

    // ------------
    // construction

    public static ComponentInspector getInstance() {
        if (instance == null)
            instance = new ComponentInspector();
        return instance;
    }

    private ComponentInspector() {
        ExplorerManager manager = getExplorerManager();
        emptyInspectorNode = new EmptyInspectorNode();
        manager.setRootContext(emptyInspectorNode);

        setLayout(new BorderLayout());

//        createSplit(); 
        createComponents(); // TEMP

        setIcon(Utilities.loadImage(iconURL));
        if (INSPECTOR_TITLE == null)
            INSPECTOR_TITLE = FormUtils.getBundleString("CTL_InspectorTitle"); // NOI18N
        setName(INSPECTOR_TITLE);

        // force window system to not show tab when this comp is alone
        putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N
        setToolTipText(FormUtils.getBundleString("HINT_ComponentInspector")); // NOI18N

        manager.addPropertyChangeListener(new NodeSelectionListener());
    }

//    private void createSplit() {
    private void createComponents() { // TEMP
//        split = new SplittedPanel();
        BeanTreeView treeView = new BeanTreeView();
//        sheet = new PropertySheetView();
//        split.add(treeView, SplittedPanel.ADD_FIRST);
//        split.add(sheet, SplittedPanel.ADD_SECOND);
//        split.setSplitType(SplittedPanel.VERTICAL);
//        split.setSplitPosition(30);

//        sheet.setDisplayWritableOnly(
//            FormEditor.getFormSettings().getDisplayWritableOnly());
//        sheet.addPropertyChangeListener(new PropertiesDisplayListener()); // TEMP

//        add(BorderLayout.CENTER, split);
        add(BorderLayout.CENTER, treeView); // TEMP

        treeView.getAccessibleContext().setAccessibleName(
            FormUtils.getBundleString("ACS_ComponentTree")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_ComponentTree")); // NOI18N
    }

    

    public void open() {
        // #37141 Rough workaround.
        WindowManager wm = WindowManager.getDefault();
        Mode mode = wm.findMode(this);
        if(mode == null) {
            mode = wm.findMode("inspector"); // NOI18N
            if(mode != null) {
                mode.dockInto(this);
            }
        }
        
        super.open();
    }
    
    // ------------
    // activating and focusing

    protected void componentActivated() {
        attachActions();
    }

    protected void componentDeactivated() {
        detachActions();
    }

    synchronized void attachActions() {
        actionsAttached = true;
        updateActions();

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            if (clipboardListener == null)
                clipboardListener = new ClipboardChangesListener();
            clip.addClipboardListener(clipboardListener);
        }
    }

    synchronized void detachActions() {
        actionsAttached = false;

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            clip.removeClipboardListener(clipboardListener);
        }

        if (deleteActionPerformer == deleteAction.getActionPerformer())
            deleteAction.setActionPerformer(null);
        if (copyActionPerformer == copyAction.getActionPerformer()) {
            copyAction.setActionPerformer(null);
            pasteAction.setPasteTypes(null);
        }
        if (cutActionPerformer == cutAction.getActionPerformer())
            cutAction.setActionPerformer(null);
    }

    boolean getActionsAttached() {
        return actionsAttached;
    }

    /** This method focuses the ComponentInspector on given form.
     * @param form the form to focus on
     */
    public void focusForm(final FormEditorSupport form) {
        if (focusedForm != form)
            focusFormInAwtThread(form, 0);
    }

    /** This method focuses the ComponentInspector on given form.
     * @param form the form to focus on
     * @param visible true to open inspector, false to close
     */
    public void focusForm(final FormEditorSupport form, boolean visible) {
        if (focusedForm != form)
            focusFormInAwtThread(form, visible ? 1 : -1);
    }

    private void focusFormInAwtThread(final FormEditorSupport form,
                                      final int visibility) {
        if (java.awt.EventQueue.isDispatchThread()) {
            focusFormImpl(form, visibility);
        }
        else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    focusFormImpl(form, visibility);
                }
            });
        }
    }

    private void focusFormImpl(FormEditorSupport form, int visibility) {
        focusedForm = form;

        if (form == null) {
            testAction.setFormModel(null);
            inspectorAction.setEnabled(false);
            reloadAction.setForm(null);

            // swing memory leak workaround
//            remove(split);
//            createSplit();
            removeAll();
            createComponents(); // TEMP

            getExplorerManager().setRootContext(emptyInspectorNode);
        }
        else {
            testAction.setFormModel(form.getFormModel());
            inspectorAction.setEnabled(true);
            reloadAction.setForm(form);

            Node formNode = form.getFormRootNode();
            // XXX how can it be null?
            if (formNode == null) {
                System.err.println("Warning: FormEditorSupport.getFormRootNode() returns null"); // NOI18N
                getExplorerManager().setRootContext(emptyInspectorNode);
            }
            else {
//                sheet.setDisplayWritableOnly(!form.getFormModel().isReadOnly()
//                     && FormEditor.getFormSettings().getDisplayWritableOnly()); // TEMP

                dontSynchronizeSelectedNodes = true;
                getExplorerManager().setRootContext(formNode);
                dontSynchronizeSelectedNodes = false;
            }
        }
        setName(INSPECTOR_TITLE);

        if (visibility > 0) {
            open();
            setCloseOperation(TopComponent.CLOSE_LAST);
        }
        else if (visibility < 0) {
            setCloseOperation(TopComponent.CLOSE_EACH);
            close();
        }
    }

    // from ExplorerPanel
    protected void updateTitle() {
        setName(INSPECTOR_TITLE);
    }

    public FormEditorSupport getFocusedForm() {
        return focusedForm;
    }

    void setSelectedNodes(Node[] nodes, FormEditorSupport form)
        throws PropertyVetoException
    {
        if (form == focusedForm) {
            dontSynchronizeSelectedNodes = true;
            getExplorerManager().setSelectedNodes(nodes);
            dontSynchronizeSelectedNodes = false;
        }
    }

    Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    // ---------------
    // actions

    private void updateActions() {
        boolean noPerformers = false;
        Node[] selected = getExplorerManager().getSelectedNodes();
        int n = selected != null ? selected.length : 0;
        int i;

        if (n > 0) {
            if (n > 1) {
                HashMap allNodes = new HashMap(101);
                for (i=0; i < n; i++)
                    if (!checkNodeParents(selected[i], allNodes)) {
                        noPerformers = true;
                        break;
                    }
            }

            if (!noPerformers) {
                for (i=0; i < n; i++)
                    if (!selected[i].canCopy()) {
                        copyAction.setActionPerformer(null);
                        break;
                    }
                if (i == n)
                    copyAction.setActionPerformer(copyActionPerformer);

                for (i=0; i < n; i++)
                    if (!selected[i].canCut()) {
                        cutAction.setActionPerformer(null);
                        break;
                    }
                if (i == n)
                    cutAction.setActionPerformer(cutActionPerformer);

                for (i=0; i < n; i++)
                    if (!selected[i].canDestroy()) {
                        deleteAction.setActionPerformer(null);
                        break;
                    }
                if (i == n)
                    deleteAction.setActionPerformer(deleteActionPerformer);
            }
        }
        else noPerformers = true; // no selected nodes

        if (noPerformers) {
            deleteAction.setActionPerformer(null);
            copyAction.setActionPerformer(null);
            cutAction.setActionPerformer(null);
        }

        updatePasteAction();
    }

    private void updatePasteAction() {
        Node[] selected = getExplorerManager().getSelectedNodes();
        if (selected != null && selected.length == 1) {
            // exactly one node must be selected
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

    private boolean checkNodeParents(Node node, Map set) {
        if (set.get(node) != null)
            return false; // the node is in the set (as parent of another node)
        
        // 'this' means the original node (not parent)
        set.put(node, this);
 
        node = node.getParentNode();
        while (node != null) {
            if (set.put(node, node) == this)
                return false; // the parent is also a node already in the set
            node = node.getParentNode();
        }

        return true;
    }

    // --------------

    public UndoRedo getUndoRedo() {
        UndoRedo ur = focusedForm != null ?
                          focusedForm.getFormUndoRedoManager() : null;
        return ur != null ? ur : super.getUndoRedo();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.component-inspector"); // NOI18N
    }

    /** Fixed preferred size (as the inherited preferred size is too big). */
    public Dimension getPreferredSize() {
        return new Dimension(250, 400);
    }

    /** Replaces this in object stream. */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    // -------------

    private Clipboard getClipboard() {
        Clipboard c = (java.awt.datatransfer.Clipboard)
            Lookup.getDefault().lookup(java.awt.datatransfer.Clipboard.class);
        if (c == null)
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        return c;
    }

    // ---------------
    // innerclasses

    // listener on nodes selection (ExplorerManager)
    private class NodeSelectionListener implements PropertyChangeListener,
                                                   ActionListener
    {
        private javax.swing.Timer timer;

        NodeSelectionListener() {
            timer = new javax.swing.Timer(150, this);
            timer.setCoalesce(true);
            timer.setRepeats(false);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;

            timer.start();
        }

        public void actionPerformed(ActionEvent evt) {
            if (actionsAttached)
                updateActions();

            if (focusedForm == null)
                return;

            FormDesigner designer = focusedForm.getFormDesigner();
            if (designer == null)
                return;

            Node[] selected = getExplorerManager().getSelectedNodes();

            if (designer.getDesignerMode() == FormDesigner.MODE_CONNECT) {
                // handle connection mode
                if (selected.length == 0)
                    return;

                RADComponentCookie cookie = (RADComponentCookie)
                    selected[0].getCookie(RADComponentCookie.class);
                if (cookie != null
                    && cookie.getRADComponent() == designer.getConnectionSource()
                    && selected.length > 1)
                {
                    cookie = (RADComponentCookie)
                        selected[selected.length-1].getCookie(RADComponentCookie.class);
                }
                if (cookie != null)
                    designer.connectBean(cookie.getRADComponent(), true);
            }
            else if (!dontSynchronizeSelectedNodes) {
                // synchronize FormDesigner with ComponentInspector
                designer.clearSelectionImpl();

                for (int i=0; i < selected.length; i++) {
                    FormCookie formCookie = (FormCookie)
                        selected[i].getCookie(FormCookie.class);
                    if (formCookie != null) {
                        Node node = formCookie.getOriginalNode();
                        if (node instanceof RADComponentNode)
                            designer.addComponentToSelectionImpl(
                                ((RADComponentNode)node).getRADComponent());
                    }
                }

                designer.repaintSelection();
            }

            timer.stop();
        }
    }

    // listener on clipboard changes
    private class ClipboardChangesListener implements ClipboardListener {
        public void clipboardChanged(ClipboardEvent ev) {
            if (!ev.isConsumed())
                updatePasteAction();
        }
    }

//    // listener on PropertySheet.PROPERTY_DISPLAY_WRITABLE_ONLY flag
//    private class PropertiesDisplayListener implements PropertyChangeListener {
//        public void propertyChange(PropertyChangeEvent evt) {
//            if (PropertySheet.PROPERTY_DISPLAY_WRITABLE_ONLY.equals(
//                                              evt.getPropertyName()))
//            {
//                FormEditor.getFormSettings().setDisplayWritableOnly(
//                                               sheet.getDisplayWritableOnly());
//            }
//        }
//    } // TEMP

    // performer for DeleteAction
    private class DeleteActionPerformer implements ActionPerformer,
                                                   Mutex.Action
    {
        private Node[] nodesToDestroy;

        public void performAction(SystemAction action) {
            Node[] selected = getExplorerManager().getSelectedNodes();
            if (selected == null || selected.length == 0)
                return;
            
            if (ExplorerPanel.isConfirmDelete() && !confirmDelete(selected))
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
                    catch (IOException ex) { // should not happen
                        ex.printStackTrace();
                    }
                }
                nodesToDestroy = null;
            }
        }

        private boolean confirmDelete(Node[] selected) {
            String message;
            String title;
            if (selected.length == 1) {
                message = NbBundle.getMessage(ExplorerActions.class,
                                              "MSG_ConfirmDeleteObject", // NOI18N
                                              selected[0].getDisplayName());
                title = NbBundle.getMessage(ExplorerActions.class,
                                            "MSG_ConfirmDeleteObjectTitle"); // NOI18N
            }
            else {
                message = NbBundle.getMessage(ExplorerActions.class,
                                              "MSG_ConfirmDeleteObjects", // NOI18N
                                               new Integer(selected.length));
                title = NbBundle.getMessage(ExplorerActions.class,
                                            "MSG_ConfirmDeleteObjectsTitle"); // NOI18N
            }

            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                        message, title, NotifyDescriptor.YES_NO_OPTION);

            return NotifyDescriptor.YES_OPTION.equals (
                   DialogDisplayer.getDefault().notify(desc));
        }
    }

    // performer for CopyAction and CutAction
    private class CopyCutActionPerformer implements ActionPerformer {
        private boolean copy;

        public CopyCutActionPerformer(boolean copy) {
            this.copy = copy;
        }

        public void performAction(SystemAction action) {
            Transferable trans;
            Node[] selected = getExplorerManager().getSelectedNodes();

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
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return null;
            }
        }
    }

    // paste type used for ExTransferable.Multi
    private class MultiPasteType extends PasteType
                                 implements Mutex.ExceptionAction
    {
        private Transferable[] transIn;
        private PasteType[] pasteTypes;

        MultiPasteType(Transferable[] t, PasteType[] p) {
            transIn = t;
            pasteTypes = p;
        }

        // performs the paste action
        public Transferable paste() throws IOException {
            if (java.awt.EventQueue.isDispatchThread())
                return doPaste();
            else { // reinvoke synchronously in AWT thread
                try {
                    return (Transferable) Mutex.EVENT.readAccess(this);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof IOException)
                        throw (IOException) e;
                    else { // should not happen, ignore
                        e.printStackTrace();
                        return ExTransferable.EMPTY;
                    }
                }
            }
        }

        public Object run() throws Exception {
            return doPaste();
        }

        private Transferable doPaste() throws IOException {
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
            setIconBase(EMPTY_INSPECTOR_ICON_BASE);
        }
        public boolean canRename() {
            return false;
        }
    }

    final public static class ResolvableHelper implements Serializable {
        static final long serialVersionUID = 7424646018839457544L;
        public Object readResolve() {
            return ComponentInspector.getInstance();
        }
    }
}
