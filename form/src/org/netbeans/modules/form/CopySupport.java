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
import org.netbeans.modules.form.layoutsupport.LayoutSupport;

class CopySupport {

    public static DataFlavor COMPONENT_COPY_FLAVOR = new RADDataFlavor(
        RADComponent.class,
        "COMPONENT_COPY_FLAVOR"); // NOI18N

    public static DataFlavor COMPONENT_CUT_FLAVOR = new RADDataFlavor(
        RADComponent.class,
        "COMPONENT_CUT_FLAVOR"); // NOI18N

    static class RADDataFlavor extends DataFlavor {
        RADDataFlavor(Class representationClass, String name) {
            super(representationClass, name);
        }
    }

    // -----------

    public static class RADTransferable implements Transferable {
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
            if (flavor instanceof RADDataFlavor) {
                return radComponent;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    // -----------

    /** Method for checking whether a component can be moved to a container
     * (the component should not be pasted to its own sub-container
     * or even to itself). */
    public static boolean canPasteCut(RADComponent sourceComponent,
                                      FormModel targetForm,
                                      ComponentContainer targetContainer) {
        if (sourceComponent.getFormModel() != targetForm)
            return true;

        if (targetContainer == null
                || targetContainer == targetForm.getModelContainer())
            return targetForm.getModelContainer().getIndexOf(sourceComponent) < 0;

        RADComponent targetComponent = (RADComponent) targetContainer;

        return sourceComponent != targetComponent
               && sourceComponent.getParentComponent() != targetComponent
               && !sourceComponent.isParentComponent(targetComponent);
    }

    // -----------

    /** Paste type for meta components.
     */
    public static class RADPaste extends PasteType {
        private Transferable transferable;
        private ComponentContainer targetContainer;
        private FormModel targetForm;

        public RADPaste(Transferable t,
                        ComponentContainer targetContainer,
                        FormModel targetForm) {
            this.transferable = t;
            this.targetContainer = targetContainer;
            this.targetForm = targetForm;
        }

        public Transferable paste() throws java.io.IOException {
            boolean fromCut =
                transferable.isDataFlavorSupported(COMPONENT_CUT_FLAVOR);

            RADComponent sourceComponent = null;
            try {
                sourceComponent = (RADComponent)
                    transferable.getTransferData(fromCut ?
                        COMPONENT_CUT_FLAVOR : COMPONENT_COPY_FLAVOR);
            }
            catch (java.io.IOException e) { } // ignore - should not happen
            catch (UnsupportedFlavorException e) { } // ignore - should not happen

            if (sourceComponent == null)
                return null;

            if (!fromCut) { // pasting copy of RADComponent
                targetForm.getComponentCreator()
                    .copyComponent(sourceComponent, targetContainer);
                return null;
            }
            else { // pasting cut RADComponent (same instance)
                FormModel sourceForm = sourceComponent.getFormModel();
                if (sourceForm != targetForm) { // taken from another form
                    Node sourceNode = sourceComponent.getNodeReference();
                    // delete component in the source
                    if (sourceNode != null)
                        sourceNode.destroy();
                    else throw new IllegalStateException();

                    sourceComponent.initialize(targetForm);
                }
                else { // moving within the same form
                    if (!canPasteCut(sourceComponent, targetForm, targetContainer))
                        return transferable; // ignore paste

                    // remove source component from its parent
                    sourceForm.removeComponent(sourceComponent);
                }

                if (sourceComponent instanceof RADVisualComponent
                        && targetContainer instanceof RADVisualContainer) {
                    RADVisualContainer visualCont = (RADVisualContainer)
                                                    targetContainer;
                    LayoutSupport laysup = visualCont.getLayoutSupport();
                    if (laysup != null) {
                        RADVisualComponent sourceComp = (RADVisualComponent)
                                                        sourceComponent;
                        LayoutSupport.ConstraintsDesc cd =
                                            laysup.getConstraints(sourceComp);
                        targetForm.addVisualComponent(sourceComp, visualCont,
                                                      laysup.fixConstraints(cd));
                    }
                }
                else {
                    targetForm.addComponent(sourceComponent, targetContainer);
                }

                // return new copy flavor, as the first one was used already
                return new RADTransferable(COMPONENT_COPY_FLAVOR, sourceComponent);
            }
        }
    }

    // ------------

    /** Paste type for InstanceCookie.
     */
    public static class InstancePaste extends PasteType {
        private Transferable transferable;
        private RADComponent targetComponent;
        private FormModel targetForm;

        public InstancePaste(Transferable t,
                             RADComponent targetComponent,
                             FormModel targetForm) {
            this.transferable = t;
            this.targetComponent = targetComponent;
            this.targetForm = targetForm;
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
