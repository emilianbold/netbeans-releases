/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.actions;

import org.netbeans.modules.cnd.remote.actions.base.RemoteOpenActionBase;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vladimir Kvashin
 */
@ActionID(id = "org.netbeans.modules.cnd.remote.actions.OpenRemoteProjectAction", category = "Project")
@ActionRegistration(iconInMenu = true, displayName = "#OpenRemoteProjectAction.submenu.title")
@ActionReferences({
    //@ActionReference(path = "Menu/File", position = 520),
    @ActionReference(path = "Toolbars/Remote", position = 2000)
})
public class OpenRemoteProjectAction extends RemoteOpenActionBase implements PropertyChangeListener {

    private ImageIcon icon;
    private static final boolean ALLOW_LOCAL = false;
    
    public OpenRemoteProjectAction() {
        super(NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.submenu.title"), ALLOW_LOCAL);
        icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/remote/resources/openProject.png", false); //NOI18N
        putValue("iconBase","org/netbeans/modules/cnd/remote/resources/openProject.png"); //NOI18N
        if (!ALLOW_LOCAL) {
            ServerList.addPropertyChangeListener(WeakListeners.propertyChange(this, this));
        }
    }

    @Override
    protected Icon getIcon() {
        return icon;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (actionCommand != null && !actionCommand.isEmpty()) {
            super.actionPerformed(e);
        } else {
            ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
            if (!ALLOW_LOCAL && executionEnvironment.isLocal()) {
                return;
            }
            actionPerformed(executionEnvironment);
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ServerList.PROP_DEFAULT_RECORD.equals(evt.getPropertyName())){
            setEnabled(!ServerList.getDefaultRecord().getExecutionEnvironment().isLocal());
        }
    }

    @Override
    protected void actionPerformed(ExecutionEnvironment env) {
        RemoteOpenHelper.openProject(env);
    }

    @Override
    protected String getSubmenuTitle() {
        return NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.submenu.title");
    }

    @Override
    protected String getItemTitle(ServerRecord record) {
        return NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.item.title", record.getDisplayName());
    }        
}
