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
    private Node.Property[] codeGenProperties;
    private Node.Property[] resourceProperties;
    private Node.Property[] allProperties;

    public FormRootNode(FormModel formModel) {
        super(new RootChildren(formModel), formModel);
        setName("Form Root Node"); // NOI18N
        setIconBaseWithExtension("org/netbeans/modules/form/resources/formDesigner.gif"); // NOI18N
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
        Node.PropertySet codeSet = new Node.PropertySet(
                "codeGeneration", // NOI18N
                FormUtils.getBundleString("CTL_SyntheticTab"), // NOI18N
                FormUtils.getBundleString("CTL_SyntheticTabHint")) // NOI18N
        {
            public Node.Property[] getProperties() {
                return getCodeGenProperties();
            }
        };
        Node.PropertySet resourceSet = new Node.PropertySet(
                "resources", // NOI18N
                FormUtils.getBundleString("CTL_ResourceTab"), // NOI18N
                FormUtils.getBundleString("CTL_ResourceTabHint")) // NOI18N
        {
            public Node.Property[] getProperties() {
                return getResourceProperties();
            }
        };
        return new Node.PropertySet[] { codeSet, resourceSet };
    }

    Node.Property[] getCodeGenProperties() {
        if (codeGenProperties == null)
            codeGenProperties = createCodeGenProperties();
        return codeGenProperties;
    }
    
    private Node.Property[] createCodeGenProperties() {
        return FormEditor.getCodeGenerator(getFormModel()).getSyntheticProperties(null);
    }

    Node.Property[] getResourceProperties() {
        if (resourceProperties == null)
            resourceProperties = createResourceProperties();
        return resourceProperties;
    }
    
    private Node.Property[] createResourceProperties() {
        return FormEditor.getResourceSupport(getFormModel()).createFormProperties();
    }

    Node.Property[] getAllProperties() {
        if (allProperties == null) {
            int codeGenCount = getCodeGenProperties().length;
            int resCount = getResourceProperties().length;
            allProperties = new Node.Property[codeGenCount + resCount];
            System.arraycopy(codeGenProperties, 0, allProperties, 0, codeGenCount);
            System.arraycopy(resourceProperties, 0, allProperties, codeGenCount, resCount);
        }
        return allProperties;
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
