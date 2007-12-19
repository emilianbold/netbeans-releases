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
package org.netbeans.modules.php.rt.providers.impl.ftp.nodes;

import javax.swing.Action;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpFileInfo;
import org.netbeans.modules.php.rt.providers.impl.ftp.nodes.actions.RefreshAction;
import org.netbeans.modules.php.rt.resources.ResourceMarker;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author avk
 */
public class FtpDirectoryObjectNode extends FtpBaseObjectNode {

    
    public FtpDirectoryObjectNode(FtpHostImpl ftpHost, FtpFileInfo file){
        super(ftpHost, file);
        setName(file.getName());
        setDisplayName(file.getName()+"/");
        setIcon();
        setShortDescription(file.getFullName());
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context){
            return super.getActions(context);
        } else {
            return ACTIONS;
        }
    }

    public void setIcon() {
        setIconBaseWithExtension(ResourceMarker.getLocation() + ResourceMarker.DIR_ICON);
    }

    public void setErrorIcon() {
        setIconBaseWithExtension(ResourceMarker.getLocation() + ResourceMarker.DIR_ERROR_ICON);
    }

    private static final Action[] ACTIONS = new Action[]{
        SystemAction.get(RefreshAction.class)
    };
}
