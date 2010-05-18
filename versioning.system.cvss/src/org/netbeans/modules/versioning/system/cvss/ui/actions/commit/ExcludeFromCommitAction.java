/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.io.File;
import java.awt.event.ActionEvent;

/**
 * Excludes selected nodes from commit.
 *
 * @author Maros Sandor
 */
public class ExcludeFromCommitAction extends AbstractAction implements Presenter.Menu {

    private final File [] files;
    private final int status;

    public ExcludeFromCommitAction(VCSContext ctx) {
        this(ctx.getRootFiles().toArray(new File[0]));
    }

    public ExcludeFromCommitAction(File[] files) {
        this.files = files;
        putValue(Action.NAME, NbBundle.getBundle(ExcludeFromCommitAction.class).getString("CTL_MenuItem_ExcludeFromCommit"));
        status = getActionStatus();
        if (status == 1) {
            putValue("BooleanState.Selected", Boolean.FALSE);  // NOI18N
        } else if (status == 2) {
            putValue("BooleanState.Selected", Boolean.TRUE);  // NOI18N
        }
        setEnabled(status != -1);
    }

    public boolean isEnabled() {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (File file : files) {
            if (CvsVersioningSystem.isManaged(file)) {
                FileInformation fi = cache.getCachedStatus(file);
                if (fi == null || (fi.getStatus() & FileInformation.STATUS_MANAGED) != 0) return true;
            }
        }
        return false;
    }
    
    public JMenuItem getMenuPresenter() {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(this);
        if (status != -1) item.setSelected(status == 2);
        Mnemonics.setLocalizedText(item, item.getText());
        return item;
    }

    private int getActionStatus() {
        CvsModuleConfig config = CvsModuleConfig.getDefault();
        int status = -1;
        for (File file : files) {
            if (config.isExcludedFromCommit(file)) {
                if (status == 1) return -1;
                status = 2;
            }
            else {
                if (status == 2) return -1;
                status = 1;
            }
        }
        return status;
    }

    public void actionPerformed(ActionEvent e) {
        CvsModuleConfig config = CvsModuleConfig.getDefault();
        for (File file : files) {
            if (status == 1) {
                config.addExclusion(file);
            } else if (status == 2) {
                config.removeExclusion(file);
            }
        }
    }
}
