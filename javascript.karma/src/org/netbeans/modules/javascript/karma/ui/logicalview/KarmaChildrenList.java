/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.karma.ui.logicalview;

import java.awt.Image;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.exec.KarmaServers;
import org.netbeans.modules.javascript.karma.exec.KarmaServersListener;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferencesValidator;
import org.netbeans.modules.javascript.karma.ui.customizer.KarmaCustomizer;
import org.netbeans.modules.javascript.karma.util.ValidationResult;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class KarmaChildrenList implements NodeList<Node>, PreferenceChangeListener {

    static final Logger LOGGER = Logger.getLogger(KarmaChildrenList.class.getName());

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final PreferenceChangeListener preferenceChangeListener = WeakListeners.create(PreferenceChangeListener.class, this, KarmaPreferences.class);


    public KarmaChildrenList(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public List<Node> keys() {
        return Collections.<Node>singletonList(KarmaNode.create(project));
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public Node node(Node key) {
        return key;
    }

    @Override
    public void addNotify() {
        KarmaPreferences.addPreferenceChangeListener(project, preferenceChangeListener);
    }

    @Override
    public void removeNotify() {
        KarmaPreferences.removePreferenceChangeListener(project, preferenceChangeListener);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        // possibly restart server
        if (KarmaServers.getInstance().isServerRunning(project)) {
            KarmaServers.getInstance().stopServer(project, false);
            ValidationResult result = new KarmaPreferencesValidator()
                    .validate(project)
                    .getResult();
            if (result.isFaultless()) {
                KarmaServers.getInstance().startServer(project);
            }
        }
    }

    //~ Inner classes

    private static final class KarmaNode extends AbstractNode implements KarmaServersListener {

        @StaticResource
        private static final String KARMA_ICON = "org/netbeans/modules/javascript/karma/ui/resources/karma.png"; // NOI18N
        @StaticResource
        private static final String WAITING_BADGE = "org/netbeans/modules/javascript/karma/ui/resources/waiting.png"; // NOI18N
        @StaticResource
        private static final String RUNNING_BADGE = "org/netbeans/modules/javascript/karma/ui/resources/running.png"; // NOI18N

        private final Project project;


        @NbBundle.Messages({
            "KarmaNode.displayName=Karma",
            "KarmaNode.description=Test Runner for JavaScript",
        })
        private KarmaNode(Project project) {
            super(Children.LEAF, Lookups.fixed(project));

            assert project != null;
            this.project = project;

            setName("Karma"); // NOI18N
            setDisplayName(Bundle.KarmaNode_displayName());
            setShortDescription(Bundle.KarmaNode_description());
            setIconBaseWithExtension(KARMA_ICON);
        }

        static KarmaNode create(Project project) {
            KarmaNode karmaNode = new KarmaNode(project);
            KarmaServers.getInstance().addKarmaServersListener(karmaNode);
            return karmaNode;
        }

        @Override
        public void destroy() throws IOException {
            KarmaServers.getInstance().removeKarmaServersListener(this);
            super.destroy();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                SystemAction.get(StartKarmaServerAction.class),
                SystemAction.get(StopKarmaServerAction.class),
                SystemAction.get(RestartKarmaServerAction.class),
                null,
                SystemAction.get(CustomizeKarmaAction.class),
            };
        }

        @Override
        public Image getIcon(int type) {
            return badgeIcon(super.getIcon(type));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return badgeIcon(super.getOpenedIcon(type));
        }

        private Image badgeIcon(Image origImg) {
            Image badge = null;
            if (KarmaServers.getInstance().isServerStarting(project)) {
                badge = ImageUtilities.loadImage(WAITING_BADGE);
            } else if (KarmaServers.getInstance().isServerStarted(project)) {
                badge = ImageUtilities.loadImage(RUNNING_BADGE);
            }
            return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
        }

        @Override
        public void serverStateChanged(Project project) {
            if (this.project.equals(project)) {
                fireIconChange();
                fireOpenedIconChange();
            }
        }

    }

    private static final class StartKarmaServerAction extends BaseNodeAction {

        @Override
        protected void performAction(Project project) {
            KarmaServers.getInstance().startServer(project);
        }

        @Override
        protected boolean enable(Project project) {
            return !KarmaServers.getInstance().isServerRunning(project);
        }

        @NbBundle.Messages("StartKarmaServerAction.name=Start")
        @Override
        public String getName() {
            return Bundle.StartKarmaServerAction_name();
        }

    }

    private static final class StopKarmaServerAction extends BaseNodeAction {

        @Override
        protected void performAction(Project project) {
            KarmaServers.getInstance().stopServer(project, false);
        }

        @Override
        protected boolean enable(Project project) {
            return KarmaServers.getInstance().isServerRunning(project);
        }

        @NbBundle.Messages("StopKarmaServerAction.name=Stop")
        @Override
        public String getName() {
            return Bundle.StopKarmaServerAction_name();
        }

    }

    private static final class RestartKarmaServerAction extends BaseNodeAction {

        @Override
        protected void performAction(Project project) {
            KarmaServers.getInstance().restartServer(project);
        }

        @Override
        protected boolean enable(Project project) {
            return KarmaServers.getInstance().isServerRunning(project);
        }

        @NbBundle.Messages("RestartKarmaServerAction.name=Restart")
        @Override
        public String getName() {
            return Bundle.RestartKarmaServerAction_name();
        }

    }

    private static final class CustomizeKarmaAction extends BaseNodeAction {

        @Override
        protected void performAction(Project project) {
            project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(KarmaCustomizer.IDENTIFIER, null);
        }

        @Override
        protected boolean enable(Project project) {
            return true;
        }

        @NbBundle.Messages("CustomizeKarmaAction.name=Properties")
        @Override
        public String getName() {
            return Bundle.CustomizeKarmaAction_name();
        }

    }

    private abstract static class BaseNodeAction extends NodeAction {

        protected abstract void performAction(Project project);

        protected abstract boolean enable(Project project);

        @Override
        protected final void performAction(Node[] activatedNodes) {
            Project project = getProject(activatedNodes);
            if (project == null) {
                LOGGER.fine("No project found -> no karma action performed");
                return;
            }
            performAction(project);
        }

        @Override
        protected final boolean enable(Node[] activatedNodes) {
            Project project = getProject(activatedNodes);
            if (project == null) {
                LOGGER.fine("No project found -> no karma action enabled");
                return false;
            }
            return enable(project);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        @CheckForNull
        private Project getProject(Node[] activatedNodes) {
            if (activatedNodes.length != 1) {
                return null;
            }
            Node node = activatedNodes[0];
            return node.getLookup().lookup(Project.class);
        }

    }

}
