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

import javax.swing.Action;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

import org.openide.util.actions.SystemAction;

/** The DataNode for Forms.
 *
 * @author Ian Formanek
 * @version 1.00, Jul 21, 1998
 */
public class FormDataNode extends FilterNode {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 1795549004166402392L;

    /** Icon base for form data objects. */
    private static final String FORM_ICON_BASE = "org/netbeans/modules/form/resources/form.gif"; // NOI18N

    /** Constructs a new FormDataObject for specified primary file */
    public FormDataNode(FormDataObject fdo) {
        this(JavaDataSupport.createJavaNode(fdo.getPrimaryFile()));
    }
    
    private FormDataNode(Node orig) {
        super(orig);
        ((AbstractNode) orig).setIconBaseWithExtension(FORM_ICON_BASE);
    }
    
    @Override
    public Action getPreferredAction() {
        // issue 56351
        return new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                FormEditorSupport supp = getCookie(FormEditorSupport.class);
                supp.openFormEditor(false);
            }
        };
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action[] javaActions = super.getActions(context);
        Action[] formActions = new Action[javaActions.length+2];
        formActions[0] = javaActions[0]; // OpenAction
        formActions[1] = SystemAction.get(org.openide.actions.EditAction.class);
        formActions[2] = null;
        System.arraycopy(javaActions, 1, formActions, 3, javaActions.length-1);
        return formActions;
    }

}
