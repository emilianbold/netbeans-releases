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
package org.netbeans.modules.xslt.tmap.ui.editors;

import javax.swing.JPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.nodes.TransformNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public class TransformCustomEditor extends JPanel implements CustomNodeEditor<Transform> {

    static final long serialVersionUID = 1L;

    private TransformPanel transformPanel;
    
    public TransformCustomEditor(TransformNode transformNode, EditingMode mode) {
        if (mode != null) {
            setEditingMode(mode);
        }
        createContent();
        initControls();
        subscribeListeners();
    }
    
    public void createContent() {
//        this.setLayout(new BorderLayout());
//        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        //
        transformPanel = new TransformPanel(this);
////        TransformCustomEditor.this.add(transformPanel);
        //
//        getAccessibleContext().setAccessibleDescription(
//                NbBundle.getMessage(TransformCustomEditor.class, "ACSD_LBL_Transform_Editor")); // NOI18N
         //
////        SoaUtil.activateInlineMnemonics(this);
   }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public Node getEditedNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transform getEditedObject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean doValidateAndSave() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EditingMode getEditingMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEditingMode(EditingMode newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean applyNewValues() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean initControls() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean subscribeListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean unsubscribeListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean afterClose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Lookup getLookup() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ValidStateManager getValidStateManager(boolean isFast) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
