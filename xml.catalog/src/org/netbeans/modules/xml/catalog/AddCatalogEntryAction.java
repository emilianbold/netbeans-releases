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

package org.netbeans.modules.xml.catalog;

import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.xml.catalog.spi.CatalogWriter;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;

/**
 * AddCatalogEntryAction.java
 *
 * Created on May 31, 2005
 * @author mkuchtiak
 */
public class AddCatalogEntryAction extends NodeAction {

    /** Creates a new instance of AddCatalogEntryAction */
    public AddCatalogEntryAction() {}

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        CatalogNode node = (CatalogNode) activatedNodes[0].getCookie(CatalogNode.class);
        CatalogWriter catalog = (CatalogWriter)node.getCatalogReader();
        CatalogEntryPanel panel = new CatalogEntryPanel();
        DialogDescriptor dd = new DialogDescriptor(panel,
                              Util.THIS.getString ("TITLE_addCatalogEntry")); //NOI18N
        //dd.setHelpCtx(new HelpCtx(CatalogMounterPanel.class));
        panel.setEnclosingDesc(dd);
        java.awt.Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dd.setValid(false);
        dialog.setVisible(true);
        if (dd.getValue().equals(DialogDescriptor.OK_OPTION)) {
            if (panel.isPublic())
                catalog.registerCatalogEntry("PUBLIC:"+panel.getPublicId(), panel.getUri()); //NOI18N
            else
                catalog.registerCatalogEntry("SYSTEM:"+panel.getSystemId(), panel.getUri()); //NOI18N
        }
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length>0)  {
            Object node = activatedNodes[0].getCookie(CatalogNode.class);
            if (node instanceof CatalogNode && ((CatalogNode)node).getCatalogReader() instanceof CatalogWriter) return true;
        }
        return false;
    }

    protected boolean asynchronous() {
        return false;
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }

    public String getName() {
        return NbBundle.getMessage(AddCatalogEntryAction.class,"TXT_AddCatalogEntry");
    }
    
}
