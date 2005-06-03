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

import org.openide.nodes.*;
import org.openide.actions.PropertiesAction;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.actions.*;

/**
 * This class represents the root node of the form (displayed as root in
 * Component Inspector).
 *
 * @author Tomas Pavek
 */

class FormRootNode extends FormNode {
    private Node.Property[] syntheticProperties;

    public FormRootNode(FormModel formModel) {
        super(new RootChildren(formModel), formModel);
        setName("Form Root Node"); // NOI18N
        setIconBase("org/netbeans/modules/form/resources/formDesigner"); // NOI18N
        updateName(formModel.getName());
    }

    // TODO: icons for visual and non-visual forms
//    public Image getIcon(int iconType) {
//    }

    public boolean canRename() {
        return false;
    }

    public boolean canDestroy() {
        return false;
    }

    public javax.swing.Action[] getActions(boolean context) {
        if (systemActions == null) // from AbstractNode
            systemActions = new SystemAction[] {
                SystemAction.get(ReloadAction.class),
                null,
                SystemAction.get(PropertiesAction.class) 
            };
        return systemActions;
    }

    void updateName(String name) {
        setDisplayName(FormUtils.getFormattedBundleString("FMT_FormNodeName", // NOI18N
                                                          new Object[] { name }));
    }

    FormOthersNode getOthersNode() {
        return ((RootChildren)getChildren()).othersNode;
    }
    
    public Node.PropertySet[] getPropertySets() {
        Node.PropertySet ps = new Node.PropertySet(
                "synthetic", // NOI18N
                FormUtils.getBundleString("CTL_SyntheticTab"), // NOI18N
                FormUtils.getBundleString("CTL_SyntheticTabHint")) // NOI18N
        {
            public Node.Property[] getProperties() {
                return getSyntheticProperties();
            }
        };
        return new Node.PropertySet[] {ps};
    }
    
    Node.Property[] getSyntheticProperties() {
        if (syntheticProperties == null)
            syntheticProperties = createSyntheticProperties();
        return syntheticProperties;
    }
    
    private Node.Property[] createSyntheticProperties() {
        return FormEditor.getCodeGenerator(getFormModel()).getSyntheticProperties(null);
    }

    // ----------------

    static class RootChildren extends Children.Keys {

        static final Object OTHERS_ROOT = new Object();
        static final Object MAIN_VISUAL_ROOT = new Object();

        private FormModel formModel;
        private FormOthersNode othersNode;

        protected RootChildren(FormModel formModel) {
            this.formModel = formModel;
            setKeys(formModel.getTopRADComponent() != null ?
                        new Object[] { OTHERS_ROOT, MAIN_VISUAL_ROOT } :
                        new Object[] { OTHERS_ROOT } );
        }

        protected Node[] createNodes(Object key) {
            Node node;
            if (key == MAIN_VISUAL_ROOT)
                node = new RADComponentNode(formModel.getTopRADComponent());
            else // OTHERS_ROOT
                node = othersNode = new FormOthersNode(formModel);

            node.getChildren().getNodes(); // enforce subnodes creation
            return new Node[] { node };
        }

        protected final FormModel getFormModel() {
            return formModel;
        }
    }
    
}
