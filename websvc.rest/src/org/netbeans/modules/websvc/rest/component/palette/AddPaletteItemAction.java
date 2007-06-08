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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Owner
 */
public class AddPaletteItemAction extends AbstractAction {
    
    private Lookup item;
    
    public AddPaletteItemAction() {
    }

    public AddPaletteItemAction(Lookup item) {
        this.item = item;
    }
    
    public void actionPerformed(ActionEvent e) {
        AddPaletteItemPanel panel = new AddPaletteItemPanel();
        DialogDescriptor desc = new DialogDescriptor(panel,
                NbBundle.getMessage(AddPaletteItemAction.class, "TTL_AddPaletteItemPanel"));
        Object res = DialogDisplayer.getDefault().notify(desc);
        if (res.equals(NotifyDescriptor.YES_OPTION)) {
            String name = panel.getPaletteItemName();
            String description = panel.getPaletteItemDescription();
            String iconUrl = panel.getPaletteItemIconUrl();
        };        
    }
}
