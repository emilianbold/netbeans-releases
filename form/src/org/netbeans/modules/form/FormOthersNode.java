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

import java.util.ArrayList;
import java.awt.datatransfer.*;

import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.actions.AddAction;
import org.netbeans.modules.form.project.ClassSource;

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
        setName(FormUtils.getBundleString("CTL_NonVisualComponents")); // NOI18N
    }

    public javax.swing.Action[] getActions(boolean context) {
        if (systemActions == null) { // from AbstractNode
            ArrayList actions = new ArrayList(10);

            if (!getFormModel().isReadOnly()) {
                actions.add(SystemAction.get(AddAction.class));
                actions.add(null);
                actions.add(SystemAction.get(PasteAction.class));
                actions.add(null);
                actions.add(SystemAction.get(ReorderAction.class));
                actions.add(null);
            }

            javax.swing.Action[] superActions = super.getActions(context);
            for (int i=0; i < superActions.length; i++)
                actions.add(superActions[i]);

            systemActions = new SystemAction[actions.size()];
            actions.toArray(systemActions);
        }

        return systemActions;
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
                Object data = t.getTransferData(t.getTransferDataFlavors()[0]);
                if (data instanceof RADComponent)
                    transComp = (RADComponent) data;
            }
            catch (UnsupportedFlavorException e) {} // should not happen
            catch (java.io.IOException e) {} // should not happen

            if (transComp != null
                && (!cut || CopySupport.canPasteCut(transComp, formModel, null)))
            {
                s.add(new CopySupport.RADPaste(t, formModel, null));
            }
        }
        else { // java or class node could be copied
            ClassSource classSource = CopySupport.getCopiedBeanClassSource(t);
            if (classSource != null) {
                s.add(new CopySupport.ClassPaste(t, classSource, formModel, null));
            }
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
            setKeys(formModel.getOtherComponents().toArray());
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
            children.getFormModel().fireComponentsReordered(cont, perm);
//            children.updateKeys();
        }
    }
}
