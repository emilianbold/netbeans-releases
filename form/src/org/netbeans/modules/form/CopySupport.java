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

import java.io.IOException;
import java.awt.datatransfer.*;

import org.openide.nodes.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.ErrorManager;
import org.openide.cookies.SourceCookie;
import org.openide.src.ClassElement;
import org.openide.loaders.DataObject;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.project.*;
import org.netbeans.modules.form.layoutdesign.*;

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

    /** Paste type for meta components.
     */
    static class RADPaste extends PasteType implements Mutex.ExceptionAction {
        private Transferable transferable;
        private FormModel targetForm;
        private RADComponent targetComponent;

        public RADPaste(Transferable t,
                        FormModel targetForm,
                        RADComponent targetComponent)
        {
            this.transferable = t;
            this.targetForm = targetForm;
            this.targetComponent = targetComponent;
        }

        public String getName() {
            return FormUtils.getBundleString(isComponentCut() ?
                                             "CTL_CutPaste" : "CTL_CopyPaste"); // NOI18N
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
            boolean fromCut = isComponentCut();
            RADComponent sourceComponent = getSourceComponent(fromCut);

            if (sourceComponent == null)
                return null;

            if (!fromCut || sourceComponent.getCodeExpression() == null) {
                // pasting copy of RADComponent
                targetForm.getComponentCreator()
                              .copyComponent(sourceComponent, targetComponent);
                return null;
//                return new RADTransferable(getComponentCopyFalvor(),
//                                           sourceComponent);
            }

            // pasting cut
            FormModel sourceForm = sourceComponent.getFormModel();
            if (sourceForm != targetForm
                || (targetComponent != null
                    && !sourceComponent.getClass().isAssignableFrom(
                                         targetComponent.getClass())))
            {   // cut from another form or pasting to an incompatible container
                if (targetForm.getComponentCreator()
                                .copyComponent(sourceComponent, targetComponent)
                    != null)
                {
                    Node sourceNode = sourceComponent.getNodeReference();
                    // delete component in the source form
                    if (sourceNode != null)
                        sourceNode.destroy();
                    else throw new IllegalStateException();
                }
                else return null; // paste not performed
            }
            else { // moving component within the same form
                if (!canPasteCut(sourceComponent, targetForm, targetComponent)
                    || !MetaComponentCreator.canAddComponent(
                                               sourceComponent.getBeanClass(),
                                               targetComponent))
                    return null; // not allowed, ignore paste

                sourceForm.startCompoundEdit();
                boolean resetConstraintProperties = false;
                LayoutModel layoutModel = sourceForm.getLayoutModel();
                LayoutComponent layoutComponent = null;
                if (layoutModel != null) {
                    layoutComponent = layoutModel.getLayoutComponent(sourceComponent.getId());
                    if (layoutComponent != null) {
                        resetConstraintProperties = true;
                        Object layoutUndoMark = layoutModel.getChangeMark();
                        javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
                        layoutModel.removeComponentAndIntervals(sourceComponent.getId(), true);
                        if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                            sourceForm.addUndoableEdit(ue);
                        }
                    }
                }
                
                // remove source component from its parent
                sourceForm.removeComponent(sourceComponent, false);

                if (sourceComponent instanceof RADVisualComponent
                    && targetComponent instanceof RADVisualContainer)
                {
                    RADVisualContainer visualCont =
                        (RADVisualContainer) targetComponent;
                    LayoutSupportManager layoutSupport =
                        visualCont.getLayoutSupport();

                    if (layoutSupport == null) {
                        visualCont.add(sourceComponent);
                        LayoutComponent parent = layoutModel.getLayoutComponent(visualCont.getId());
                        Object layoutUndoMark = layoutModel.getChangeMark();
                        javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
                        if (layoutComponent == null) {
                            layoutComponent = new LayoutComponent(sourceComponent.getId(),
                                MetaComponentCreator.shouldBeLayoutContainer((RADVisualComponent)sourceComponent));
                        }
                        resetConstraintProperties = true;
                        layoutModel.addNewComponent(layoutComponent, parent, null);
                        if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                            sourceForm.addUndoableEdit(ue);
                        }
                    } else {
                        RADVisualComponent[] compArray = new RADVisualComponent[] {
                            (RADVisualComponent) sourceComponent };
                        LayoutConstraints[] constrArray = new LayoutConstraints[] {
                            layoutSupport.getStoredConstraints(compArray[0]) };

                        try {
                            layoutSupport.acceptNewComponents(compArray,
                                                              constrArray,
                                                              -1);
                        }
                        catch (RuntimeException ex) {
                            // layout support does not accept the component
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                            return transferable;
                        }

                        // add the component to the target container
                        visualCont.add(sourceComponent);
                        layoutSupport.addComponents(compArray, constrArray, -1);
                    }
                    targetForm.fireComponentAdded(sourceComponent, false);
                }
                else {
                    ComponentContainer targetContainer =
                        targetComponent instanceof ComponentContainer ?
                            (ComponentContainer) targetComponent : null;

                    // add the component to the target container
                    targetForm.addComponent(sourceComponent, targetContainer);
                }
                if (resetConstraintProperties) {
                    ((RADVisualComponent)sourceComponent).resetConstraintsProperties();
                }
                sourceForm.endCompoundEdit(true);
            }

            return ExTransferable.EMPTY;
        }

        boolean isComponentCut() {
            return transferable.isDataFlavorSupported(getComponentCutFlavor());
        }

        RADComponent getSourceComponent(boolean fromCut) throws IOException {
            RADComponent sourceComponent = null;
            try {
                Object obj = transferable.getTransferData(
                               fromCut ? getComponentCutFlavor() :
                                         getComponentCopyFlavor());
                if (obj instanceof RADComponent)
                    sourceComponent = (RADComponent) obj;
            }
            catch (UnsupportedFlavorException e) { // ignore - should not happen
            }

            return sourceComponent;
        }
    }

    // ------------

    /** Paste type for a class (java node).
     */
    static class ClassPaste extends PasteType implements Mutex.ExceptionAction {

        private Transferable transferable;
        private ClassSource classSource;
        private FormModel targetForm;
        private RADComponent targetComponent; // may be null if pasting to Other Components

        public ClassPaste(Transferable t,
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
            targetForm.getComponentCreator().createComponent(
                                        classSource, targetComponent, null);
            return transferable;
        }
    }

    static String getCopiedBeanClassName(Transferable t) {
        ClassElement clsElem = (ClassElement)
            NodeTransfer.cookie(t, NodeTransfer.COPY, ClassElement.class);

        if (clsElem == null) {
            SourceCookie source = (SourceCookie)
                NodeTransfer.cookie(t, NodeTransfer.COPY, SourceCookie.class);
            DataObject dobj = (DataObject)
                NodeTransfer.cookie(t, NodeTransfer.COPY, DataObject.class);
            if (source != null && dobj != null) {
                ClassElement[] classes = source.getSource().getClasses();
                for (int i=0; i < classes.length; i++) {
                    if (classes[i].getName().getName().equals(dobj.getName())
                        && classes[i].isDeclaredAsJavaBean())
                    {
                        clsElem = classes[i];
                        break;
                    }
                }
            }
        }

        return clsElem != null ? clsElem.getVMName() : null;
    }

    static ClassSource getCopiedBeanClassSource(Transferable t) {
        DataObject dobj = (DataObject)
            NodeTransfer.cookie(t, NodeTransfer.COPY, DataObject.class);
        FileObject fo = dobj != null ? dobj.getPrimaryFile() : null;
        if (fo == null)
            return null;

        String clsName = getCopiedBeanClassName(t);
        if (clsName == null)
            return null;

        return ClassPathUtils.getProjectClassSource(fo, clsName);
    }

}
