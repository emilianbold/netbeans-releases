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

import java.util.ArrayList;
import java.awt.datatransfer.*;

import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;

/**
 * This class represents the root node of "Other Components".
 *
 * @author Tomas Pavek
 */

class FormOthersNode extends FormNode {

    public FormOthersNode(FormModel formModel) {
        super(new OthersChildren(formModel), formModel);

        getCookieSet().add(new OthersIndex((OthersChildren)getChildren()));
        setIconBase("org/netbeans/modules/form/resources/formNonVisual"); // NOI18N
        setName("Others Node"); // NOI18N
        setName(FormEditor.getFormBundle().getString("CTL_NonVisualComponents")); // NOI18N
    }

    protected SystemAction[] createActions() {
        ArrayList actions = new ArrayList(10);

        if (!getFormModel().isReadOnly()) {
//            actions.add(SystemAction.get(AddComponentAction.class));
//            actions.add(null);
            actions.add(SystemAction.get(PasteAction.class));
            actions.add(null);
            actions.add(SystemAction.get(ReorderAction.class));
            actions.add(null);
        }

        SystemAction[] superActions = super.createActions();
        for (int i=0; i < superActions.length; i++)
            actions.add(superActions[i]);

        SystemAction[] array = new SystemAction[actions.size()];
        actions.toArray(array);
        return array;
    }

    protected void createPasteTypes(Transferable t, java.util.List s) {
        FormModel formModel = getFormModel();
        if (formModel.isReadOnly())
            return;

        boolean copy = t.isDataFlavorSupported(
                             CopySupport.getComponentCopyFlavor());
        boolean cut = t.isDataFlavorSupported(
                            CopySupport.getComponentCutFlavor());

        if (copy || cut) { // copy or cut some RADComponent
            RADComponent transComp = null;
            try {
                transComp = (RADComponent) t.getTransferData(
                                                t.getTransferDataFlavors()[0]);
            }
            catch (UnsupportedFlavorException e) {} // should not happen
            catch (java.io.IOException e) {} // should not happen

            if (transComp != null
                && (!cut || CopySupport.canPasteCut(transComp, formModel, null)))
            {
                s.add(new CopySupport.RADPaste(t, formModel, null));
            }
        }
        else { // if there is not a RADComponent in the clipboard,
               // try if it is not InstanceCookie
            InstanceCookie ic =
                (InstanceCookie) NodeTransfer.cookie(t,
                                                     NodeTransfer.COPY,
                                                     InstanceCookie.class);
            if (ic != null)
                s.add(new CopySupport.InstancePaste(t, formModel, null));
        }
    }

    // -------------

    static class OthersChildren extends FormNodeChildren {

        private FormModel formModel;

        protected OthersChildren(FormModel formModel) {
            this.formModel = formModel;
            updateKeys();
        }

        // FormNodeChildren implementation
        protected void updateKeys() {
            setKeys(formModel.getOtherComponents(false));
        }

        protected Node[] createNodes(Object key) {
            Node node = new RADComponentNode((RADComponent)key);
            node.getChildren().getNodes(); // enforce subnodes creation
            return new Node[] { node };
        }

        protected final FormModel getFormModel() {
            return formModel;
        }
    }

    // -------------

    static final class OthersIndex extends org.openide.nodes.Index.Support {
        private OthersChildren children;

        public OthersIndex(OthersChildren children) {
            this.children = children;
        }

        public Node[] getNodes() {
            return children.getNodes();
        }

        public int getNodesCount() {
            return getNodes().length;
        }

        public void reorder(int[] perm) {
            ComponentContainer cont = children.getFormModel().getModelContainer();
            cont.reorderSubComponents(perm);
            children.getFormModel().fireComponentsReordered(cont);
//            children.updateKeys();
        }
    }
}
