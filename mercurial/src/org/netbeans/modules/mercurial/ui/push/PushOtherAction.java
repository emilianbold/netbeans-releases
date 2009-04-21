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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.push;

import java.net.URISyntaxException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.repository.Repository;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.wizards.CloneRepositoryWizardPanel;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;

/**
 * Push Other action for mercurial: 
 * hg push - push changes to the specified target
 * 
 * @author John Rice
 */
public class PushOtherAction extends ContextAction implements ChangeListener {
    
    private final VCSContext context;
    private Repository repository = null;
    private JButton pushButton = null;
    private JButton cancelButton = null;

    public PushOtherAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }

    public void performAction(ActionEvent e) {
        final File root = HgUtils.getRootFile(context);
        if (root == null) return;

        if (repository == null) {
            int repositoryModeMask = Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
            String title = org.openide.util.NbBundle.getMessage(CloneRepositoryWizardPanel.class, "CTL_Repository_Location");       // NOI18N
            repository = new Repository(repositoryModeMask, title, true);
            repository.addChangeListener(this);
        }
        pushButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(pushButton, org.openide.util.NbBundle.getMessage(PushOtherAction.class, "CTL_Push_Action_Push")); // NOI18N
        pushButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "ACSD_Push_Action_Push")); // NOI18N
        pushButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "ACSN_Push_Action_Push")); // NOI18N
        cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(PushOtherAction.class, "CTL_Push_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "ACSD_Push_Action_Cancel")); //NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "ACSN_Push_Action_Cancel")); // NOI18N

        pushButton.setEnabled(false);
        Object option = repository.show(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "CTL_PushDialog_Title"),
                                        new HelpCtx(PushOtherAction.class),
                                        new Object[] {pushButton, cancelButton},
                                        true,
                                        "hg.push.dialog");

        if (option == pushButton) {
            final HgURL pushPath;
            try {
                pushPath = repository.getUrl();
            } catch (URISyntaxException ex) {
                Mercurial.LOG.log(Level.SEVERE,
                                  this.getClass().getName()
                                          + ": Could not push because of invalid URI." //NOI18N
                                          + repository.getUrlString());
                Mercurial.LOG.log(Level.SEVERE,
                                  this.getClass().getName()
                                  + ": Invalid URI: "                   //NOI18N
                                  + repository.getUrlString());
                return;
            }

            push(context, root, pushPath);
        }
    }
    
    public void stateChanged(ChangeEvent evt) {
        pushButton.setEnabled(repository.isValid());
    }

    public static void push(final VCSContext ctx, final File root, final HgURL pushPath) {
        if (root == null || pushPath == null) return;
        final String fromPrjName = HgProjectUtils.getProjectName(root);
        final String toPrjName = NbBundle.getMessage(PushAction.class, "MSG_EXTERNAL_REPOSITORY"); // NOI18N
         
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() { 
               PushAction.performPush(root, pushPath, fromPrjName, toPrjName, this.getLogger()); 
            } 
        };

        support.start(rp, root,
                org.openide.util.NbBundle.getMessage(PushAction.class, "MSG_PUSH_PROGRESS")); // NOI18N
    }
    
    public boolean isEnabled() {
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(HgUtils.getRootFile(context) == null || ctxFiles == null || ctxFiles.size() == 0) 
            return false;
        return true; // #121293: Speed up menu display, warn user if not set when Push selected
    }
}
