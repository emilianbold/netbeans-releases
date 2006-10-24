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

package org.netbeans.modules.form.actions;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.form.palette.BeanInstaller;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * InstallToPalette action - installs selected classes as beans to palette.
 *
 * @author   Ian Formanek
 */

public class InstallToPaletteAction extends NodeAction {

    private static String name;

    public InstallToPaletteAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(InstallToPaletteAction.class)
                     .getString("ACT_InstallToPalette"); // NOI18N
        return name;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("beans.adding"); // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }

    protected void performAction(Node[] activatedNodes) {
        BeanInstaller.installBeans(activatedNodes);
    }

    protected boolean enable(Node[] activatedNodes) {
        for (Node n: activatedNodes) {
            FileObject fobj = n.getLookup().lookup(FileObject.class);
            if (fobj == null || JavaSource.forFileObject(fobj) == null) {
                return false;
            }
        }
        
        return true;
    }

}
