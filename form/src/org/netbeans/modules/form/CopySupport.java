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

import java.awt.datatransfer.*;
import org.openide.nodes.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.datatransfer.PasteType;
import org.netbeans.modules.form.layoutsupport.*;

/** Support class for copy/cut/paste operations on form.
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
                if (flavors[i] == flavor) { // comparison based on exact instances, as these are static in this node
                    return true;
                }
            }
            return false;
        }

        public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, java.io.IOException
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
    static class RADPaste extends PasteType {
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

        public Transferable paste() throws java.io.IOException {
            boolean fromCut =
                transferable.isDataFlavorSupported(getComponentCutFlavor());

            RADComponent sourceComponent = null;
            try {
                sourceComponent = (RADComponent) transferable.getTransferData(
                                     fromCut ? getComponentCutFlavor() :
                                               getComponentCopyFlavor());
            }
            catch (java.io.IOException e) { } // ignore - should not happen
            catch (UnsupportedFlavorException e) { } // ignore - should not happen

            if (sourceComponent == null)
                return null;

            if (!fromCut) { // pasting copy of RADComponent
                targetForm.getComponentCreator()
                              .copyComponent(sourceComponent, targetComponent);
                return null;
//                return new RADTransferable(getComponentCopyFalvor(),
//                                           sourceComponent);
            }

            // pasting cut
            FormModel sourceForm = sourceComponent.getFormModel();
            if (sourceForm != targetForm) { // cut from another form
                targetForm.getComponentCreator()
                    .copyComponent(sourceComponent, targetComponent);

                Node sourceNode = sourceComponent.getNodeReference();
                // delete component in the source form
                if (sourceNode != null)
                    sourceNode.destroy();
                else throw new IllegalStateException();
            }
            else { // moving component within the same form
                if (!canPasteCut(sourceComponent, targetForm, targetComponent)
                    || !MetaComponentCreator.canAddComponent(
                                               sourceComponent.getBeanClass(),
                                               targetComponent))
                    return transferable; // ignore paste

                // remove source component from its parent
                sourceForm.removeComponentFromContainer(sourceComponent);

                if (sourceComponent instanceof RADVisualComponent
                    && targetComponent instanceof RADVisualContainer)
                {
                    RADVisualComponent visualComp = (RADVisualComponent)
                                                    sourceComponent;
                    RADVisualContainer visualCont = (RADVisualContainer)
                                                    targetComponent;
                    LayoutConstraints constr = visualCont.getLayoutSupport()
                                             .getStoredConstraints(visualComp);

                    targetForm.addVisualComponent(visualComp, visualCont, constr);
                }
                else {
                    ComponentContainer targetContainer =
                        targetComponent instanceof ComponentContainer ?
                            (ComponentContainer) targetComponent : null;

                    targetForm.addComponent(sourceComponent, targetContainer);
                }
            }

            // return new copy flavor, as the first one was used already
            return new RADTransferable(getComponentCopyFlavor(),
                                       sourceComponent);
        }
    }

    // ------------

    /** Paste type for InstanceCookie.
     */
    static class InstancePaste extends PasteType {
        private Transferable transferable;
        private FormModel targetForm;
        private RADComponent targetComponent;

        public InstancePaste(Transferable t,
                             FormModel targetForm,
                             RADComponent targetComponent)
        {
            this.transferable = t;
            this.targetForm = targetForm;
            this.targetComponent = targetComponent;
        }

        public final Transferable paste() throws java.io.IOException {
            InstanceCookie ic =
                (InstanceCookie) NodeTransfer.cookie(transferable,
                                                     NodeTransfer.COPY,
                                                     InstanceCookie.class);
            if (ic != null)
                targetForm.getComponentCreator().createComponent(
                                                    ic, targetComponent, null);
            return transferable;
        }
    }
}
