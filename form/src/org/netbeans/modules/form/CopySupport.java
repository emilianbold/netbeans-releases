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

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.undo.UndoableEdit;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.netbeans.modules.form.project.*;
import org.netbeans.modules.form.layoutdesign.*;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.palette.BeanInstaller;

/**
 * Support class for copy/cut/paste operations in form editor.
 *
 * @author Tomas Pavek
 */

class CopySupport {

    private static final String flavorMimeType =
        "application/x-form-metacomponent;class=java.lang.Object"; // NOI18N

    private static DataFlavor copyFlavor;
    private static DataFlavor cutFlavor;

    static DataFlavor getComponentCopyFlavor() {
        if (copyFlavor == null) {
            copyFlavor = new DataFlavor(flavorMimeType,
                                        "COMPONENT_COPY_FLAVOR"); // NOI18N
        }
        return copyFlavor;
    }

    static DataFlavor getComponentCutFlavor() {
        if (cutFlavor == null) {
            cutFlavor = new DataFlavor(flavorMimeType,
                                       "COMPONENT_CUT_FLAVOR"); // NOI18N
        }
        return cutFlavor;
    }

    // -----------

    static class RADTransferable implements Transferable {
        private RADComponent radComponent;
        private DataFlavor[] flavors;

        RADTransferable(DataFlavor flavor, RADComponent radComponent) {
            this(new DataFlavor[] { flavor }, radComponent);
        }

        RADTransferable(DataFlavor[] flavors, RADComponent radComponent) {
            this.flavors = flavors;
            this.radComponent = radComponent;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i] == flavor) {
                    return true;
                }
            }
            return false;
        }

        public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException
        {
            if ("x-form-metacomponent".equals(flavor.getSubType())) // NOI18N
                return radComponent;

            throw new UnsupportedFlavorException(flavor);
        }
    }

    // -----------

    /** Checks whether a component can be moved to a container (the component
     * cannot be pasted to its own sub-container or even to itself). */
    public static boolean canPasteCut(RADComponent sourceComponent,
                                      FormModel targetForm,
                                      RADComponent targetComponent)
    {
        if (!sourceComponent.isInModel())
            return false;
        if (sourceComponent.getFormModel() != targetForm)
            return true;

        if (targetComponent == null)
            return targetForm.getModelContainer().getIndexOf(sourceComponent) < 0;

        return sourceComponent != targetComponent
               && sourceComponent.getParentComponent() != targetComponent
               && !sourceComponent.isParentComponent(targetComponent);
    }

    // -----------

    static void createPasteTypes(Transferable trans, java.util.List s,
                                 FormModel targetForm, RADComponent targetComponent) {
        if (targetForm.isReadOnly()) {
            return;
        }

        Transferable[] allTrans;
        if (trans.isDataFlavorSupported(ExTransferable.multiFlavor)) {
            try {
                MultiTransferObject transObj = (MultiTransferObject)
                        trans.getTransferData(ExTransferable.multiFlavor);
                allTrans = new Transferable[transObj.getCount()];
                for (int i=0; i < allTrans.length; i++) {
                    allTrans[i] = transObj.getTransferableAt(i);
                }
            } catch (UnsupportedFlavorException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return;
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return;
            }
        } else {
            allTrans = new Transferable[] { trans };
        }

        boolean canPaste = false;
        boolean cut = false; // true - cut, false - copy
        List<RADComponent> sourceComponents = null;

        for (int i=0; i < allTrans.length; i++) {
            Transferable t = allTrans[i];
            boolean metaCompTransfer;
            if (t.isDataFlavorSupported(getComponentCopyFlavor())) {
                assert !cut;
                metaCompTransfer = true;
            } else if (t.isDataFlavorSupported(getComponentCutFlavor())) {
                assert cut || sourceComponents == null;
                metaCompTransfer = true;
                cut = true;
            } else {
                metaCompTransfer = false;
            }
            if (metaCompTransfer) {
                RADComponent transComp = null;
                try {
                    Object data = t.getTransferData(t.getTransferDataFlavors()[0]);
                    if (data instanceof RADComponent) {
                        transComp = (RADComponent) data;
                    }
                }
                catch (UnsupportedFlavorException e) {} // should not happen
                catch (java.io.IOException e) {} // should not happen

                if (transComp != null
                    // only cut to another container
                    && (!cut || canPasteCut(transComp, targetForm, targetComponent))
                    // must be a valid source/target combination
                    && (MetaComponentCreator.canAddComponent(transComp.getBeanClass(),
                                                             targetComponent)
                        || (!cut && MetaComponentCreator.canApplyComponent(transComp.getBeanClass(),
                                                                           targetComponent)))
                    // hack needed due to screwed design of menu metacomponents
                    && (!(targetComponent instanceof RADMenuComponent)
                          || transComp instanceof RADMenuItemComponent))
                {   // pasting this meta component is allowed
                    if (sourceComponents == null) {
                        sourceComponents = new LinkedList<RADComponent>();
                    }
                    sourceComponents.add(getComponentToCopy(transComp, targetComponent, cut));
                    canPaste = true;
                }
            } else { // java node (compiled class) could be copied
                ClassSource classSource = CopySupport.getCopiedBeanClassSource(t);
                if (classSource != null) {
                //                && (MetaComponentCreator.canAddComponent(cls, component)
                //                   || MetaComponentCreator.canApplyComponent(cls, component)))
                    s.add(new ClassPaste(t, classSource, targetForm, targetComponent));
                    canPaste = true;
                }
            }
        }

        if (sourceComponents != null) {
            s.add(new RADPaste(sourceComponents, targetForm, targetComponent, cut));
        }

        if (!canPaste && targetComponent != null
                && (!(targetComponent instanceof ComponentContainer)
                    || MetaComponentCreator.isTransparentLayoutComponent(targetComponent))
                && targetComponent.getParentComponent() != null) {
            // allow paste on non-container component - try its parent
            createPasteTypes(trans, s, targetForm, targetComponent.getParentComponent());
        }
    }

    private static RADComponent getComponentToCopy(RADComponent metacomp, RADComponent targetComp, boolean cut) {
        RADComponent parent = metacomp.getParentComponent();
        if (MetaComponentCreator.isTransparentLayoutComponent(parent)
                && (!cut || parent.getParentComponent() != targetComp)) {
            return parent;
        }
        return metacomp;
    }

    /**
     * Paste type for meta components.
     */
    private static class RADPaste extends PasteType implements Mutex.ExceptionAction {
        private List<RADComponent> sourceComponents;
        private FormModel targetForm;
        private RADComponent targetComponent;
        private boolean fromCut;

        RADPaste(List<RADComponent> sourceComponents,
                 FormModel targetForm, RADComponent targetComponent,
                 boolean cut)
        {
            this.sourceComponents = sourceComponents;
            this.targetForm = targetForm;
            this.targetComponent = targetComponent;
            this.fromCut = cut;
        }

        public String getName() {
            return FormUtils.getBundleString(fromCut ? "CTL_CutPaste" : "CTL_CopyPaste"); // NOI18N
        }

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
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        return ExTransferable.EMPTY;
                    }
                }
            }
        }

        public Object run() throws Exception {
            return doPaste();
        }

        private Transferable doPaste() throws IOException {
            if (sourceComponents == null || sourceComponents.isEmpty()) {
                return null;
            }

            FormModel sourceForm = sourceComponents.get(0).getFormModel();
            boolean move = fromCut && sourceForm == targetForm;
            boolean autoUndo = true; // in case of unexpected error, for robustness
            LayoutModel sourceLayout = sourceForm.getLayoutModel();
            LayoutModel targetLayout = targetForm.getLayoutModel();
            List<RADComponent> copiedComponents = null; // only for new-to-new layout copy

            // is the target container a visual container with new layout?
            boolean targetNewLayout = targetComponent instanceof RADVisualContainer
                    && ((RADVisualContainer)targetComponent).getLayoutSupport() == null;
            RADVisualContainer sourceContainer = null;
            Object layoutUndoMark = null;
            UndoableEdit layoutEdit = null;
            Map<String, String> sourceToTargetId = null; // for new-to-new layout copy
            Map<String, Rectangle> idToBounds = null; // for old-to-new layout copy

            if (targetNewLayout) {
                // do all source components come from a visual container from which
                // we can copy the layout to the new layout?
                for (RADComponent sourceComp : sourceComponents) {
                    if (sourceComp instanceof RADVisualComponent) {
                        RADVisualComponent sourceCompVisual = (RADVisualComponent) sourceComp;
                        if (!sourceCompVisual.isMenuComponent()) {
                            RADVisualContainer parent = sourceCompVisual.getParentContainer();
                            if (sourceContainer == null) {
                                sourceContainer = parent;
                            } else if (parent != sourceContainer) {
                                sourceContainer = null;
                                break;
                            }
                        }
                    } else {
                        sourceContainer = null;
                        break;
                    }
                }
                if (sourceContainer != null && sourceContainer.getLayoutSupport() != null
                    && (getComponentBounds(sourceComponents.get(0)) == null
                        || !isConvertibleLayout(sourceContainer))) {
                    sourceContainer = null; // old layout not suitable for conversion
                }
                // take care about undo in target layout model
                layoutUndoMark = targetLayout.getChangeMark();
                layoutEdit = targetLayout.getUndoableEdit();
            }

            try {
                // copy or move the components
                for (RADComponent sourceComp : sourceComponents) {
                    RADComponent copiedComp;
                    if (!move) {
                        copiedComp = targetForm.getComponentCreator().copyComponent(sourceComp, targetComponent);
                        if (copiedComp == null) {
                            return null; // copy failed...
                        }
                    } else { // move within the same form
                        targetForm.getComponentCreator().moveComponent(sourceComp, targetComponent);
                        copiedComp = sourceComp;
                    }

                    if (copiedComp instanceof RADVisualComponent && !((RADVisualComponent)copiedComp).isMenuComponent()) {
                        // for visual components we must care about the layout model (new layout)
                        if (targetNewLayout) {
                            RADVisualContainer targetContainer = (RADVisualContainer) targetComponent;
                            if (sourceContainer != null) { // source is one visual container, we can copy the layout
                                if (sourceContainer.getLayoutSupport() == null) { // copying from new layout
                                    if (sourceToTargetId == null) {
                                        sourceToTargetId = new HashMap<String, String>();
                                    }
                                    sourceToTargetId.put(sourceComp.getId(), copiedComp.getId());
                                    // remember the copied component - for next paste operation
                                    if (copiedComp != sourceComp) {
                                        if (copiedComponents == null) {
                                            copiedComponents = new ArrayList<RADComponent>(sourceComponents.size());
                                        }
                                        copiedComponents.add(copiedComp);
                                    }
                                } else { // copying from old layout - requires conversion
                                    if (idToBounds == null) {
                                        idToBounds = new HashMap<String, Rectangle>();
                                    }
                                    idToBounds.put(copiedComp.getId(), getComponentBounds(sourceComp));
                                }
                            } else { // layout can't be copied, place component on a default location
                                getLayoutDesigner().addUnspecifiedComponent(copiedComp.getId(),
                                        sourceComp.getId(), getComponentSize(sourceComp),
                                        targetComponent.getId());
                            }
                        } else if (move && sourceLayout != null) { // new-to-old layout copy
                            // need to remove layout component
                            LayoutComponent layoutComp = sourceLayout.getLayoutComponent(sourceComp.getId());
                            if (layoutComp != null) {
                                sourceLayout.removeComponent(sourceComp.getId(), !layoutComp.isLayoutContainer());
                            }
                        } // old-to-old layout copy is fully handled by MetaComponentCreator
                    } // also copying menu component needs no additional treatment
                }

                // copy the layout for all visual components together
                if (sourceToTargetId != null) { // new layout to new layout
                    getLayoutDesigner().copyLayout(sourceLayout, sourceToTargetId, targetComponent.getId());
                } else if (idToBounds != null) { // old layout to new layout
                    getLayoutDesigner().copyLayoutFromOutside(idToBounds, targetComponent.getId(), true);
                }

                autoUndo = false;
            } finally {
                if (layoutUndoMark != null && !layoutUndoMark.equals(targetLayout.getChangeMark())) {
                    sourceForm.addUndoableEdit(layoutEdit);
                }
                if (autoUndo) {
                    targetForm.forceUndoOfCompoundEdit();
                    // [don't expect problems in source form...]
                }
            }

            // remove components if cut from another form (the components have been copied)
            if (fromCut && sourceForm != targetForm) {
                for (RADComponent sourceComp : sourceComponents) {
                    Node sourceNode = sourceComp.getNodeReference();
                    if (sourceNode != null) {
                        sourceNode.destroy();
                    }
                }
            }

            // return Transferable object for the next paste operation
            if (fromCut) { // cut - can't be pasted again
                return ExTransferable.EMPTY;
            } else if (copiedComponents != null) {
                // make the newly copied components the source for the next paste
                if (copiedComponents.size() == 1) {
                    return new RADTransferable(getComponentCopyFlavor(), copiedComponents.get(0));
                } else {
                    Transferable[] trans = new Transferable[copiedComponents.size()];
                    int i = 0;
                    for (RADComponent comp : copiedComponents) {
                        trans[i++] = new RADTransferable(getComponentCopyFlavor(), comp);
                    }
                    return new ExTransferable.Multi(trans);
                }
            } else { // keep the original clipboard content
                return null;
            }
            // TODO: menu components edge cases
        }

        private static Rectangle getComponentBounds(RADComponent sourceComp) {
            FormDesigner designer = FormEditor.getFormDesigner(sourceComp.getFormModel());
            Component comp = (Component) designer.getComponent(sourceComp);
            return comp != null ? comp.getBounds() : null;
        }

        private static Dimension getComponentSize(RADComponent sourceComp) {
            FormDesigner designer = FormEditor.getFormDesigner(sourceComp.getFormModel());
            Component comp = (Component) designer.getComponent(sourceComp);
            return comp != null ? comp.getSize() : null;
        }

        private static boolean isConvertibleLayout(RADVisualContainer metaCont) {
            LayoutSupportManager ls = metaCont.getLayoutSupport();
            return !ls.isDedicated() && ls.getSupportedClass() != java.awt.CardLayout.class;
        }

        private LayoutDesigner getLayoutDesigner() {
            return FormEditor.getFormDesigner(targetForm).getLayoutDesigner();
        }
    }

    // ------------

    /**
     * Paste type for a class (java node).
     */
    private static class ClassPaste extends PasteType implements Mutex.ExceptionAction {
        private Transferable transferable;
        private ClassSource classSource;
        private FormModel targetForm;
        private RADComponent targetComponent; // may be null if pasting to Other Components

        ClassPaste(Transferable t,
                   ClassSource classSource,
                   FormModel targetForm,
                   RADComponent targetComponent)
        {
            this.transferable = t;
            this.classSource = classSource;
            this.targetForm = targetForm;
            this.targetComponent = targetComponent;
        }

        public Transferable paste() throws IOException {
            if (java.awt.EventQueue.isDispatchThread()) {
                return doPaste();
            }
            else { // reinvoke synchronously in AWT thread
                try {
                    return (Transferable) Mutex.EVENT.readAccess(this);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof IOException)
                        throw (IOException) e;
                    else { // should not happen, ignore
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        return transferable;
                    }
                }
            }
        }

        public Object run() throws Exception {
            return doPaste();
        }

        private Transferable doPaste() throws IOException {
            if ((classSource.getClassName().indexOf('.') == -1) // Issue 79573
                && !FormJavaSource.isInDefaultPackage(targetForm)) {
                String message = FormUtils.getBundleString("MSG_DefaultPackageBean"); // NOI18N
                NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } else {
                targetForm.getComponentCreator().createComponent(
                                        classSource, targetComponent, null);
            }
            return transferable;
        }
    }

    static String getCopiedBeanClassName(FileObject fo) {
        return BeanInstaller.findJavaBeanName(fo);
    }

    static ClassSource getCopiedBeanClassSource(Transferable t) {
        DataObject dobj = (DataObject)
            NodeTransfer.cookie(t, NodeTransfer.COPY, DataObject.class);
        FileObject fo = dobj != null ? dobj.getPrimaryFile() : null;
        if (fo == null)
            return null;

        String clsName = getCopiedBeanClassName(fo);
        if (clsName == null)
            return null;

        return ClassPathUtils.getProjectClassSource(fo, clsName);
    }

}
