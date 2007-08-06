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

package org.netbeans.modules.xml.jaxb.actions;

import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author lgao
 */
public class JAXBWizardOpenXSDIntoEditorAction extends NodeAction {
    
    public JAXBWizardOpenXSDIntoEditorAction() {
    }

    protected void performAction(Node[] nodes) {
        Node node = nodes[ 0 ];
        
        FileObject fo = node.getLookup().lookup(FileObject.class );
        try {
        if ( fo != null ) {
            DataObject dataObject = DataObject.find( fo );
            if ( dataObject != null ) {
                EditCookie ec = dataObject.getCookie(EditCookie.class );
                if ( ec != null ) {
                    ec.edit();
                }
            }
        }
        } catch ( DataObjectNotFoundException donfe ) {
            donfe.printStackTrace();
        }
    }

    public String getName() {
        return NbBundle.getMessage(this.getClass(), 
                                    "LBL_OpenSchemaFile"); // No I18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
}
