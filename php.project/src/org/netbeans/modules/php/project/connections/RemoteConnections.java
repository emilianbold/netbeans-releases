/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections;

import java.awt.Dialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class RemoteConnections {

    public static enum ConnectionType {
        FTP ("LBL_Ftp"); // NOI18N

        private final String label;

        private ConnectionType(String labelKey) {
            label = NbBundle.getMessage(RemoteConnections.class, labelKey);
        }

        public String getLabel() {
            return label;
        }
    }

    private final RemoteConnectionsPanel remoteConnectionsPanel;

    public static RemoteConnections get() {
        return new RemoteConnections();
    }

    private RemoteConnections() {
        remoteConnectionsPanel = new RemoteConnectionsPanel();
    }

    public void open() {
        String title = NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_ManageRemoteConnections");
        DialogDescriptor descriptor = new DialogDescriptor(remoteConnectionsPanel, title, true, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        if (descriptor.getValue() == NotifyDescriptor.OK_OPTION) {
            saveRemoteConnections();
        }
    }

    private void saveRemoteConnections() {
        // XXX
    }
}
