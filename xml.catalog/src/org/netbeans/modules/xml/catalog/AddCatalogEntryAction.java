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
        dialog.show();
        if (dd.getValue().equals(DialogDescriptor.OK_OPTION)) {
            if (panel.isPublic())
                catalog.registerCatalogEntry("PUBLIC:"+panel.getPublicId(), panel.getUri()); //NOI18N
            else
                catalog.registerCatalogEntry("SYSTEM:"+panel.getSystemId(), panel.getUri()); //NOI18N
        }
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length>0)  {
            CatalogNode node = (CatalogNode) activatedNodes[0].getCookie(CatalogNode.class);
            if (node!=null && node.getCatalogReader() instanceof CatalogWriter) return true;
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
