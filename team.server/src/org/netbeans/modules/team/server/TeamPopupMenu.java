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
package org.netbeans.modules.team.server;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.modules.team.server.api.TeamServerManager;
import org.netbeans.modules.team.server.ui.spi.PopupMenuProvider;
import org.netbeans.modules.team.server.ui.spi.TeamServerProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;


@Messages("KENAI_POPUP=Team")
public class TeamPopupMenu extends AbstractAction implements ContextAwareAction, PropertyChangeListener {

    private Map<Project, Data> menuProviders = Collections.synchronizedMap(new WeakHashMap<Project, Data>());

    private static TeamPopupMenu inst = null;

    private TeamPopupMenu() {
        putValue(NAME, Bundle.KENAI_POPUP());
    }

    @ActionID(id = "org.netbeans.modules.team.server.ui.TeamPopupMenu", category = "Team")
    @ActionRegistration(lazy = false, displayName = "#KENAI_POPUP")
    @ActionReference(path = "Projects/Actions", position = 151)
    public static synchronized TeamPopupMenu getDefault() {
        if (inst == null) {
            inst = new TeamPopupMenu();
            TeamServerManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(inst, TeamServerManager.getDefault()));
        }
        return inst;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new PopupMenuPresenter(actionContext);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (TeamServerManager.PROP_INSTANCES.equals(evt.getPropertyName())) {
            Utilities.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    menuProviders.clear();
                }
            });
        }
    }

    private final class PopupMenuPresenter extends AbstractAction implements Presenter.Popup {

        private final Project proj;

        private PopupMenuPresenter(Lookup actionContext) {
            proj = actionContext.lookup(Project.class);
        }

        @Override
        @Messages("LBL_CHECKING=Checking for Team Server support - wait...")
        public JMenuItem getPopupPresenter() {
            JMenu kenaiPopup = new JMenu(); //NOI18N
            final Node[] nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
            kenaiPopup.setVisible(false);
            if (proj != null && nodes.length == 1) {
                PopupMenuProvider provider = getMenuProvider(proj);
                if (provider == null) {
                    final JMenu dummy = new JMenu(Bundle.LBL_CHECKING());
                    dummy.setVisible(true);
                    dummy.setEnabled(false);
                    Utilities.getRequestProcessor().post(new Runnable() { // cache the results, update the popup menu
                        @Override
                        public void run() {
                            Data data = menuProviders.get(proj);
                            String repoUrl = data == null ? null : data.repoUrl;
                            if (repoUrl == null) {
                                repoUrl = VersioningQuery.getRemoteLocation(proj.getProjectDirectory().toURI());
                            }
                            if (repoUrl == null) {
                                menuProviders.put(proj, new Data("", DummyProvider.getDefault())); //NOI18N null cannot be used - project with no repo is null, "" is to indicate I already checked this one...
                                dummy.setVisible(false);
                            } else {
                                PopupMenuProvider popupProvider = null;
                                outer: for (TeamServerProvider serverProvider : TeamServerManager.getDefault().getProviders()) {
                                    for (String s : repoUrl.split(";")) {
                                        popupProvider = serverProvider.getPopupMenuProvider(repoUrl);
                                        if (popupProvider != null) {
                                            repoUrl = s;
                                            break outer;
                                        }
                                    }
                                }
                                if (popupProvider == null) {
                                    popupProvider = DummyProvider.getDefault();
                                }
                                menuProviders.put(proj, new Data(repoUrl, popupProvider));
                                final JMenu tmp = constructKenaiMenu();
                                final Component[] c = tmp.getMenuComponents();
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        tmp.revalidate();
                                        dummy.setText(Bundle.KENAI_POPUP());
                                        dummy.setEnabled(c.length > 0);
                                        for (int i = 0; i < c.length; i++) {
                                            Component item = c[i];
                                            dummy.add(item);
                                        }
                                        dummy.getParent().validate();
                                    }
                                });
                            }
                        }
                    });
                    return dummy;
                } else if (provider == DummyProvider.getDefault()) {
                     // hide for non-Kenai projects
                } else { // show for Kenai projects
                    kenaiPopup = constructKenaiMenu();
                }
            }
            return kenaiPopup;
        }

        private JMenu constructKenaiMenu() {
            // show for Kenai projects
            final JMenu teamPopup = new JMenu(Bundle.KENAI_POPUP());
            Data data = menuProviders.get(proj);
            teamPopup.setVisible(false);
            if (data != null) {
                Action[] actions = data.provider.getPopupMenuActions(proj, data.repoUrl);
                if (actions.length > 0) {
                    teamPopup.setVisible(true);
                    for (Action a : actions) {
                        if (a == null) {
                            teamPopup.addSeparator();
                        } else {
                            teamPopup.add(createmenuItem(a));
                        }
                    }
                }
            }
            return teamPopup;
        }

        public JSeparator createJSeparator() {
            JMenu menu = new JMenu();
            menu.addSeparator();
            return (JSeparator)menu.getPopupMenu().getComponent(0);
        }

        PopupMenuProvider getMenuProvider (Project proj) {
            assert proj != null;
            Data data = menuProviders.get(proj);
            if (data == null) { // repo is not cached - has to be cached on the background before
                return null;
            }
            return data.provider;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

    }
    
    private JMenuItem createmenuItem(Action action) {
        JMenuItem item;
        if (action instanceof Presenter.Menu) {
            item = ((Presenter.Menu) action).getMenuPresenter();
        } else {
            item = new JMenuItem();
            Actions.connect(item, action, true);
        }
        return item;
    }

    private static class DummyProvider implements PopupMenuProvider {
        private static DummyProvider instance;

        private DummyProvider () {
            
        }
        
        public static synchronized DummyProvider getDefault () {
            if (instance == null) {
                instance = new DummyProvider();
            }
            return instance;
        }

        @Override
        public Action[] getPopupMenuActions (Project proj, String repoUrl) {
            return new Action[0];
        }
    }
    
    private static class Data {
        private final String repoUrl;
        private final PopupMenuProvider provider;

        public Data (String repoUrl, PopupMenuProvider provider) {
            this.repoUrl = repoUrl;
            this.provider = provider;
        }
    }
}