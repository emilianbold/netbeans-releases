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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import java.util.List;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.MetaComponentCreator;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 */
public class DuplicateAction  extends NodeAction {

    protected boolean enable(Node[] nodes) {
        List comps = FormUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && getParent(comps) != null);
    }
    
    public String getName() {
        return NbBundle.getMessage(AlignAction.class, "ACT_Duplicate"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(Node[] nodes) {
        List<RADComponent> comps = FormUtils.getSelectedLayoutComponents(nodes);
        RADVisualContainer parent = getParent(comps);
        if (parent != null) {
            String[] sourceIds = new String[comps.size()];
            String[] targetIds = new String[comps.size()];
            int i = 0;
            MetaComponentCreator creator = parent.getFormModel().getComponentCreator();
            for (RADComponent comp : comps) {
                RADComponent copiedComp = creator.copyComponent(comp, parent);
                if (copiedComp == null) {
                    return; // copy failed...
                }
                sourceIds[i] = comp.getId();
                targetIds[i] = copiedComp.getId();
                i++;
            }
            FormEditor.getFormDesigner(parent.getFormModel()).getLayoutDesigner()
                    .duplicateLayout(sourceIds, targetIds);
        }
    }

    private static RADVisualContainer getParent(List components) {
        RADVisualContainer commonParent = null;
        for (Object comp : components) {
            if (comp instanceof RADVisualComponent) {
                RADVisualContainer parent = ((RADVisualComponent)comp).getParentContainer();
                if (parent == null || (commonParent != null && parent != commonParent)) {
                    return null;
                }
                if (commonParent == null) {
                    commonParent = parent;
                }
            } else {
                return null;
            }
        }
        return commonParent != null && commonParent.getLayoutSupport() == null
                ? commonParent : null;
    }
}
