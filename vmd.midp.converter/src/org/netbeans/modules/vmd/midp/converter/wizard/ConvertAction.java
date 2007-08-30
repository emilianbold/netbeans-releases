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
 *
 */
package org.netbeans.modules.vmd.midp.converter.wizard;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.DialogDisplayer;

public final class ConvertAction extends CookieAction {
    
    protected void performAction(Node[] activatedNodes) {
        FileObject primary = activatedNodes[0].getLookup().lookup(FileObject.class);
        FileObject secondary = FileUtil.findBrother(primary, "mvd"); // NOI18N
        if (primary == null  ||  secondary == null)
            return;
        ConvertPanel panel = new ConvertPanel ();
        panel.switchToShown (primary, secondary, primary.getParent ());
        DialogDisplayer.getDefault ().createDialog (panel.getDialogDescriptor ()).setVisible (true);
    }
    
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public String getName() {
        return NbBundle.getMessage(ConvertAction.class, "CTL_ConvertAction"); // NOI18N
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            FileObject.class
        };
    }
    
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}
