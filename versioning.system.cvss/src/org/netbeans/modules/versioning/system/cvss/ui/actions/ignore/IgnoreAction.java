/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.ignore;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.openide.nodes.Node;

import java.io.File;

/**
 * Adds files to .cvsignore file.
 *
 * @author Maros Sandor
 */
public class IgnoreAction extends AbstractSystemAction {
    
    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
        switch (actionStatus) {
        case UNDEFINED:
        case IGNORING:
            return "CTL_MenuItem_Ignore";  // NOI18N
        case UNIGNORING:
            return "CTL_MenuItem_Unignore"; // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }

    public int getActionStatus(Node [] nodes) {
        return getActionStatus(getContext(nodes).getFiles());
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    public int getActionStatus(File [] files) {
        int actionStatus = -1;
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            FileInformation info = cache.getStatus(files[i]);
            if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                actionStatus = (actionStatus == -1 || actionStatus == IGNORING) ? IGNORING : UNDEFINED;
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                actionStatus = (actionStatus == -1 || actionStatus == UNIGNORING) ? UNIGNORING : UNDEFINED;
            } else {
                actionStatus = UNDEFINED;
                break;
            }
        }
        return actionStatus == -1 ? UNDEFINED : actionStatus;
    }
    
    protected boolean enable(Node[] nodes) {
        return getActionStatus(nodes) != UNDEFINED;
    }

    public void performCvsAction(Node[] nodes) {
        int actionStatus = getActionStatus(nodes);
        if (actionStatus == IGNORING) {
            CvsVersioningSystem.getInstance().setIgnored(Utils.getCurrentContext(nodes).getFiles());
        } else if (actionStatus == UNIGNORING) {
            CvsVersioningSystem.getInstance().setNotignored(Utils.getCurrentContext(nodes).getFiles());
        } else {
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }

    protected boolean asynchronous() {
        return false;
    }

}
