/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.collab.chat;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.kenai.api.KenaiActivity;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.Utilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * 
 */
public class CreateChatAction extends AbstractAction {

    private KenaiProject project;
    public CreateChatAction(KenaiProject project) {
        super();
        this.project = project;
        String name = project.getDisplayName();
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent e) {
        final JButton source = (e.getSource() instanceof JButton)?(JButton) e.getSource():null;
        final TopComponent mainWindow = WindowManager.getDefault().findTopComponent("TeamTopComponent"); //NOI18N
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(CreateChatAction.class, "LBL_CheckPermissions"));
        progress.setInitialDelay(0);
        progress.start();
        if (source!=null) source.setEnabled(true);
        Utilities.getRequestProcessor().post(new Runnable() {

            public void run() {
                try {
                    if (!project.getKenai().isAuthorized(project, KenaiActivity.PROJECTS_ADMIN)) {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                progress.finish();
                                JOptionPane.showMessageDialog(null, NbBundle.getMessage(CreateChatAction.class, "CTL_NotAuthorizedToCreateChat",getValue(NAME)));
                                mainWindow.setCursor(Cursor.getDefaultCursor());
                                if (source!=null) source.setEnabled(true);
                            }
                        });
                        return;
                    }
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        int value = JOptionPane.showOptionDialog(
                                null,
                                NbBundle.getMessage(CreateChatAction.class, "LBL_CreateChatQuestions"),
                                NbBundle.getMessage(CreateChatAction.class, "LBL_CreateChatTitle"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                null,
                                null);
                        if (value == JOptionPane.YES_OPTION) {
                            progress.setDisplayName(NbBundle.getMessage(CreateChatAction.class, "CTL_CreatingChatProgress"));
                            Utilities.getRequestProcessor().post(new Runnable() {

                                public void run() {
                                    try {
                                        final KenaiFeature f = project.createProjectFeature(
                                                project.getName(),
                                                NbBundle.getMessage(CreateChatAction.class, "CTL_ChatRoomName", project.getName()),
                                                NbBundle.getMessage(CreateChatAction.class, "CTL_ChatRoomDescription", project.getName()),
                                                KenaiService.Names.XMPP_CHAT,
                                                null,
                                                null,
                                                null);

                                        SwingUtilities.invokeLater(new Runnable() {

                                            public void run() {
                                                final ChatTopComponent chatTc = ChatTopComponent.findInstance();
                                                chatTc.open();
                                                chatTc.addChat(new ChatPanel(KenaiConnection.getDefault(project.getKenai()).getChat(f)));
                                                mainWindow.setCursor(Cursor.getDefaultCursor());
                                                progress.finish();
                                                if (source!=null) source.setEnabled(true);
                                                chatTc.requestActive();
                                            }
                                        });
                                    } catch (KenaiException kenaiException) {
                                        Exceptions.printStackTrace(kenaiException);
                                        mainWindow.setCursor(Cursor.getDefaultCursor());
                                        progress.finish();
                                        if (source!=null) source.setEnabled(true);
                                    }
                                }
                            });

                        } else {
                            mainWindow.setCursor(Cursor.getDefaultCursor());
                            progress.finish();
                            if (source!=null) source.setEnabled(true);
                        }
                    }
                });
            }
        });
    }
}
