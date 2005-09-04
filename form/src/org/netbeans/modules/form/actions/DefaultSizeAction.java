/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;
import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.actions.NodeAction;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.layoutdesign.LayoutModel;

public class DefaultSizeAction extends NodeAction {

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] nodes) {
        for (int i=0; i < nodes.length; i++) {
            if (getValidComponent(nodes[i]) == null)
                return false; // all components must be valid
        }
        return true;
    }

    protected void performAction(Node[] nodes) {
        FormModel formModel = null;
        FormDesigner formDesigner = null;
        LayoutDesigner layoutDesigner = null;
        RADVisualComponent topDesignComponent = null;
        LayoutModel layoutModel = null;
        Object layoutUndoMark = null;
        javax.swing.undo.UndoableEdit layoutUE = null;

        for (int i=0; i < nodes.length; i++) {
            RADVisualComponent metacomp = getValidComponent(nodes[i]);
            if (metacomp == null)
                return; // all components must be valid

            if (layoutDesigner == null) {
                formModel = metacomp.getFormModel();
                formDesigner = FormEditor.getFormDesigner(formModel);
                layoutDesigner = formDesigner.getLayoutDesigner();
                layoutModel = formModel.getLayoutModel();
                layoutUndoMark = layoutModel.getChangeMark();
                layoutUE = layoutModel.getUndoableEdit();
            }
            layoutDesigner.setDefaultSize(metacomp.getId());
            if (metacomp instanceof RADVisualContainer) {
                formModel.fireContainerLayoutChanged((RADVisualContainer)metacomp, null, null, null);
                // [should be recursive]
            }
            if (topDesignComponent == null && metacomp == formDesigner.getTopDesignComponent()) {
                topDesignComponent = metacomp;
            }
            else { // update container the component is in
                formModel.fireContainerLayoutChanged(metacomp.getParentContainer(), null, null, null);
            }
        }

        if (topDesignComponent != null) {
            formDesigner.resetDesignerSize();
        }
        if (layoutUE != null && !layoutUndoMark.equals(layoutModel.getChangeMark())) {
            formModel.addUndoableEdit(layoutUE);
        }
    }

    public String getName() {
        return org.openide.util.NbBundle.getBundle(DefaultSizeAction.class)
                .getString("ACT_DefaultSize"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private static RADVisualComponent getValidComponent(Node node) {
        RADComponentCookie radCookie = (RADComponentCookie)
                node.getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualComponent) {
                RADVisualComponent visualMetaComp = (RADVisualComponent)metacomp;
                RADVisualContainer parent = visualMetaComp.getParentContainer();
                if ((parent != null && parent.getLayoutSupport() == null)
                    || (visualMetaComp instanceof RADVisualContainer
                        && ((RADVisualContainer)visualMetaComp).getLayoutSupport() == null
                        && (!(visualMetaComp instanceof RADVisualFormContainer)
                            || ((RADVisualFormContainer)visualMetaComp).getFormSizePolicy()
                                 != RADVisualFormContainer.GEN_BOUNDS)))
                    return visualMetaComp;
            }
        }
        return null;
    }
}
