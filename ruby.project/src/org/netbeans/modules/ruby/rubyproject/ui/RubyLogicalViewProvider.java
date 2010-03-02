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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.rubyproject.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.modules.ruby.rubyproject.RubyActionProvider;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
import org.netbeans.modules.ruby.rubyproject.IrbAction;
import org.netbeans.modules.ruby.rubyproject.RSpecSupport;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.rake.RakeRunnerAction;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.ruby.codecoverage.RubyCoverageProvider;
import org.netbeans.modules.ruby.rubyproject.RubyBaseActionProvider;
import org.netbeans.modules.ruby.rubyproject.TestActionConfiguration;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner.TestType;

/**
 * Logical view provider for Ruby project.
 */
public final class RubyLogicalViewProvider extends RubyBaseLogicalViewProvider {
    
    /** Add an IRB console action to Ruby projects, like the Rails console for Rails projects */
    private static final boolean INCLUDE_IRB_CONSOLE = Boolean.getBoolean("ruby.irbconsole"); // NOI18N
    
    public RubyLogicalViewProvider(
            final RubyProject project,
            final UpdateHelper helper,
            final PropertyEvaluator evaluator,
            final ReferenceHelper resolver) {
        super(project, helper, evaluator, resolver);
    }
    
    public Node createLogicalView() {
        return new RubyLogicalViewRootNode();
    }

    @Override
    protected Node findWithPathFinder(final Node root, final FileObject target) {
        TreeRootNode.PathFinder pf2 = root.getLookup().lookup(TreeRootNode.PathFinder.class);
        if (pf2 != null) {
            Node n = pf2.findPath(root, target);
            if (n != null) {
                return n;
            }
        }
        return null;
    }

    /** Filter node containin additional features for the Ruby physical. */
    private final class RubyLogicalViewRootNode extends AbstractNode {
        
        private final RSpecSupport rspecSupport;
        
        public RubyLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(getProject(), "Projects/org-netbeans-modules-ruby-rubyproject/Nodes"), 
                  Lookups.singleton(getProject()));
            setIconBaseWithExtension("org/netbeans/modules/ruby/rubyproject/ui/resources/jruby.png");
            super.setName(ProjectUtils.getInformation(getProject()).getDisplayName());
            this.rspecSupport = new RSpecSupport(getProject());
        }

        public @Override String getShortDescription() {
            String platformDesc = RubyPlatform.platformDescriptionFor(getProject());
            if (platformDesc == null) {
                platformDesc = NbBundle.getMessage(RubyLogicalViewProvider.class, "RubyLogicalViewProvider.PlatformNotFound");
            }
            String dirName = FileUtil.getFileDisplayName(getProject().getProjectDirectory());
            return NbBundle.getMessage(RubyLogicalViewProvider.class, "RubyLogicalViewProvider.ProjectTooltipDescription", dirName, platformDesc);
        }
        
        public @Override Action[] getActions( boolean context ) {
            return getAdditionalActions();
        }
        
        public @Override boolean canRename() {
            return true;
        }
        
        public @Override void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(getProject(), s);
        }
        
        public @Override HelpCtx getHelpCtx() {
            return new HelpCtx(RubyLogicalViewRootNode.class);
        }
        
        // Private methods -------------------------------------------------------------
        
        private Action[] getAdditionalActions() {

            bundlerSupport.initialize();

            ResourceBundle bundle = NbBundle.getBundle(RubyLogicalViewProvider.class);
            
            List<Action> actions = new ArrayList<Action>();
            
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildGemAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(SystemAction.get(RakeRunnerAction.class));
            actions.add(SystemAction.get(IrbAction.class));
            actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_RDOC, bundle.getString("LBL_RDocAction_Name"), null)); // NOI18N
            actions.add(null);
            if (INCLUDE_IRB_CONSOLE) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_IRB_CONSOLE, "IRB" /*bundle.getString("LBL_ConsoleAction_Name")*/, null)); // NOI18N
                actions.add(null);
            }
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null)); // NOI18N
            if (TestActionConfiguration.enable(RubyBaseActionProvider.COMMAND_TEST, getProject())) {
                actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null)); // NOI18N
            }
            if (AutoTestSupport.isInstalled(getProject(), TestType.AUTOTEST)
                    && TestActionConfiguration.enable(RubyBaseActionProvider.COMMAND_AUTOTEST, getProject())) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_AUTOTEST, bundle.getString("LBL_AutoTest"), null)); // NOI18N
            }
            if (AutoTestSupport.isInstalled(getProject(), TestType.AUTOSPEC)
                    && TestActionConfiguration.enable(RubyBaseActionProvider.COMMAND_AUTOSPEC, getProject())) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_AUTOSPEC, bundle.getString("LBL_AutoSpec"), null)); // NOI18N
            }
            if (rspecSupport.isRSpecInstalled() 
                    && TestActionConfiguration.enable(RubyBaseActionProvider.COMMAND_RSPEC, getProject())) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_RSPEC, bundle.getString("LBL_RSpec"), null)); // NOI18N
            }
            if (bundlerSupport.installed()) {
                actions.add(bundlerSupport.createAction());
            }
            actions.add(RubyCoverageProvider.createCoverageAction(getProject()));
            actions.add(null);

            actions.add(CommonProjectActions.setProjectConfigurationAction());
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.openSubprojectsAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));
            
            // honor 57874 contact
            actions.add(null);
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N

            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            
            return actions.toArray(new Action[actions.size()]);
        }
    }
    
}
