/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.ftp.nodes.actions;

import org.netbeans.modules.php.rt.providers.impl.ftp.nodes.FtpBaseObjectNodeChildren;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author avk
 */
public class RefreshAction extends NodeAction {

    private static final String LBL_REFRESH_FTP_NODE = "LBL_Refresh_ftp_node";       // NOI18N
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        Children children = node.getLookup().lookup(Children.class);
        if (children instanceof FtpBaseObjectNodeChildren){
            FtpBaseObjectNodeChildren ftpChildren = (FtpBaseObjectNodeChildren)children;
            ftpChildren.updateKeys();
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RefreshAction.class, LBL_REFRESH_FTP_NODE);
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
}
