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

package org.netbeans.modules.remote.ui;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.dlight.api.terminal.TerminalSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Vladimir Voskresensky
 */
public class OpenTerminalAction extends SingleHostAction {
    private JMenu remotePopupMenu;
    private JMenu localPopupMenu;

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "OpenTerminalMenuItem"); // NOI18N
    }

    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
    }

    @Override
    public boolean isVisible(Node node) {
        return true;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        JMenuItem out = localPopupMenu;
        Node[] activatedNodes = getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1 && isRemote(activatedNodes[0])) {
            out = remotePopupMenu;
        }
        return out;
    }

    private void createSubMenu() {
        if (remotePopupMenu == null) {
            remotePopupMenu = new JMenu(getName());
            remotePopupMenu.add(SystemAction.get(AddHome.class).getPopupPresenter());
//            popupMenu.add(new AddProjects().getPopupPresenter());
            remotePopupMenu.add(SystemAction.get(AddMirror.class).getPopupPresenter());
            remotePopupMenu.add(SystemAction.get(AddRoot.class).getPopupPresenter());
        }
        if (localPopupMenu == null) {
            localPopupMenu = new JMenu(getName());
            localPopupMenu.add(SystemAction.get(AddHome.class).getPopupPresenter());
//            popupMenu.add(new AddProjects().getPopupPresenter());
            localPopupMenu.add(SystemAction.get(AddRoot.class).getPopupPresenter());
        }
    }

    private enum PLACE {

        ROOT("OpenRoot"),// NOI18N
        HOME("OpenHome"),// NOI18N
        PROJECTS("OpenProjects"),// NOI18N
        MIRROR("OpenMirror");// NOI18N
        private final String name;

        PLACE(String nameKey) {
            this.name = NbBundle.getMessage(OpenTerminalAction.class, nameKey);
        }

        private String getName() {
            return name;
        }
    }

    private static abstract class AddPlace extends SingleHostAction {

        private final PLACE place;

        private AddPlace(PLACE place) {
            this.place = place;
            putProperty("noIconInMenu", Boolean.TRUE);// NOI18N
        }

        protected abstract String getPath(ExecutionEnvironment env);

        @Override
        protected void performAction(final ExecutionEnvironment env, Node node) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OpenTerminalAction.class, "OpenTerminalAction.opening"));
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        ConnectionManager.getInstance().connectTo(env);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (CancellationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    final String path = getPath(env);
                    if (path != null) {
                        Runnable openFavorites = new Runnable() {

                            @Override
                            public void run() {
                                TerminalSupport.openTerminal(env.getDisplayName(), env, path);
                            }
                        };
                        SwingUtilities.invokeLater(openFavorites);
                    } else {
                        String msg;
                        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                            msg = NbBundle.getMessage(AddToFavoritesAction.class, "NotConnected", getPath(env), env.getDisplayName());
                        } else {
                            msg = NbBundle.getMessage(AddToFavoritesAction.class, "NoRemotePath", getPath(env));
                        }
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                    }
                }
            };
            RequestProcessor.getDefault().post(runnable);
            
        }

        @Override
        public String getName() {
            return place.getName();
        }
    }

    private static final class AddRoot extends AddPlace {

        public AddRoot() {
            super(PLACE.ROOT);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            return "/"; // NOI18N
        }
    }

    private static final class AddHome extends AddPlace {

        public AddHome() {
            super(PLACE.HOME);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                if (hostInfo != null) {
                    String userDir = hostInfo.getUserDir();
                    return userDir;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }

    private static final class AddProjects extends AddPlace {

        public AddProjects() {
            super(PLACE.PROJECTS);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            return "/"; // NOI18N
        }
    }

    private static final class AddMirror extends AddPlace {

        public AddMirror() {
            super(PLACE.MIRROR);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            String remoteSyncRoot = RemotePathMap.getRemoteSyncRoot(env);
            return remoteSyncRoot;
        }
    }
}
