/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.Image;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ruby.rubyproject.RubyActionProvider;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.rubyproject.AutoTestSupport;
import org.netbeans.modules.ruby.rubyproject.RSpecSupport;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.rake.RakeRunnerAction;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class RubyLogicalViewProvider implements LogicalViewProvider {
    
    /** Add an IRB console action to Ruby projects, like the Rails console for Rails projects */
    private static final boolean INCLUDE_IRB_CONSOLE = Boolean.getBoolean("ruby.irbconsole"); // NOI18N
    
    //private static final RequestProcessor BROKEN_LINKS_RP = new RequestProcessor("RubyPhysicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    
    private final RubyProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper resolver;
    private List<ChangeListener> changeListeners;
    
    public RubyLogicalViewProvider(RubyProject project, UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.resolver = resolver;
    }
    
    public Node createLogicalView() {
        return new RubyLogicalViewRootNode();
    }
    
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    public ReferenceHelper getRefHelper() {
        return resolver;
    }
    
    public UpdateHelper getUpdateHelper() {
        return helper;
    }
    
    public Node findPath(Node root, Object target) {
        Project project = root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
        }
        
        if (target instanceof FileObject) {
            FileObject targetFO = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(targetFO);
            if (!project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }
            
            Node[] rootChildren = root.getChildren().getNodes(true);
            for (int i = 0; i < rootChildren.length; i++) {
                TreeRootNode.PathFinder pf2 = rootChildren[i].getLookup().lookup(TreeRootNode.PathFinder.class);
                if (pf2 != null) {
                    Node n =  pf2.findPath(rootChildren[i], target);
                    if (n != null) {
                        return n;
                    }
                }
                FileObject childFO = rootChildren[i].getLookup().lookup(DataObject.class).getPrimaryFile();
                if (targetFO.equals(childFO)) {
                    return rootChildren[i];
                }
            }
        }
        
        return null;
    }
    
    public synchronized void addChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            this.changeListeners = new ArrayList<ChangeListener>();
        }
        this.changeListeners.add(l);
    }
    
    public synchronized void removeChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            return;
        }
        this.changeListeners.remove(l);
    }
    
    /**
     * Used by RubyProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advices him to use BrokenLinksAction to correct it.
     *
     */
    public void testBroken() {
        ChangeListener[] _listeners;
        synchronized (this) {
            if (this.changeListeners == null) {
                return;
            }
            _listeners = this.changeListeners.toArray(new ChangeListener[this.changeListeners.size()]);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (int i=0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(event);
        }
    }
    
//    private static Lookup createLookup( Project project ) {
//        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
//        // XXX Remove root folder after FindAction rewrite
//        return Lookups.fixed(new Object[] {project, rootFolder});
//    }
    
    
    // Private innerclasses ----------------------------------------------------------------
    
    public boolean hasBrokenLinks () {
//        return BrokenReferencesSupport.isBroken(helper.getRakeProjectHelper(), resolver, getBreakableProperties(),
//                new String[] {RubyProjectProperties.JAVA_PLATFORM});
        return false;
    }
    
    public boolean hasInvalidJdkVersion () {
//        String javaSource = this.evaluator.getProperty("javac.source");     //NOI18N
//        String javaTarget = this.evaluator.getProperty("javac.target");    //NOI18N
//        if (javaSource == null && javaTarget == null) {
//            //No need to check anything
//            return false;
//        }
//        
//        final String platformId = this.evaluator.getProperty("platform.active");  //NOI18N
//        final JavaPlatform activePlatform = RubyProjectUtil.getActivePlatform (platformId);
//        if (activePlatform == null) {
//            return true;
//        }        
//        SpecificationVersion platformVersion = activePlatform.getSpecification().getVersion();
//        try {
//            return (javaSource != null && new SpecificationVersion (javaSource).compareTo(platformVersion)>0)
//                   || (javaTarget != null && new SpecificationVersion (javaTarget).compareTo(platformVersion)>0);
//        } catch (NumberFormatException nfe) {
//            ErrorManager.getDefault().log("Invalid javac.source: "+javaSource+" or javac.target: "+javaTarget+" of project:"
//                +this.project.getProjectDirectory().getPath());
//            return true;
//        }
        return false;
    }
    
    private static Image brokenProjectBadge = ImageUtilities.loadImage("org/netbeans/modules/ruby/rubyproject/ui/resources/brokenProjectBadge.gif", true);
    
    /** Filter node containin additional features for the Ruby physical
     */
    private final class RubyLogicalViewRootNode extends AbstractNode {
        
        //private Action brokenLinksAction;
        private boolean broken;         //Represents a state where project has a broken reference repairable by broken reference support
        private boolean illegalState;   //Represents a state where project is not in legal state, eg invalid source/target level
        
        private final RSpecSupport rspecSupport;
        
        public RubyLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-ruby-rubyproject/Nodes"), 
                  Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/ruby/rubyproject/ui/resources/jruby.png");
            super.setName(ProjectUtils.getInformation(project).getDisplayName());
            if (hasBrokenLinks()) {
                broken = true;
            }
            else if (hasInvalidJdkVersion ()) {
                illegalState = true;
            }
            //brokenLinksAction = new BrokenLinksAction();
            this.rspecSupport = new RSpecSupport(project);
        }

        public @Override String getShortDescription() {
            String platformDesc = RubyPlatform.platformDescriptionFor(project);
            if (platformDesc == null) {
                platformDesc = NbBundle.getMessage(RubyLogicalViewProvider.class, "RubyLogicalViewProvider.PlatformNotFound");
            }
            String dirName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(RubyLogicalViewProvider.class, "RubyLogicalViewProvider.ProjectTooltipDescription", dirName, platformDesc);
        }
        
        public @Override String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            // XXX text colors should be taken from UIManager, not hard-coded!
            return broken || illegalState ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }
        
        public @Override Image getIcon(int type) {
            Image original = super.getIcon(type);
            return broken || illegalState ? ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        }
        
        public @Override Image getOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            return broken || illegalState ? ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0) : original;
        }
        
        public @Override Action[] getActions( boolean context ) {
            return getAdditionalActions();
        }
        
        public @Override boolean canRename() {
            return true;
        }
        
        public @Override void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        public @Override HelpCtx getHelpCtx() {
            return new HelpCtx(RubyLogicalViewRootNode.class);
        }
        
        // Private methods -------------------------------------------------------------
        
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle(RubyLogicalViewProvider.class);
            
            List<Action> actions = new ArrayList<Action>();
            
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildGemAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
            actions.add(null);
            actions.add(SystemAction.get(RakeRunnerAction.class));
            actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_RDOC, bundle.getString("LBL_RDocAction_Name"), null)); // NOI18N
            actions.add(null);
            if (INCLUDE_IRB_CONSOLE) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_IRB_CONSOLE, "IRB" /*bundle.getString("LBL_ConsoleAction_Name")*/, null)); // NOI18N
                actions.add(null);
            }
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null)); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null)); // NOI18N
            if (AutoTestSupport.isInstalled(project)) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_AUTOTEST, bundle.getString("LBL_AutoTest"), null)); // NOI18N
            }
            if (rspecSupport.isRSpecInstalled()) {
                actions.add(ProjectSensitiveActions.projectCommandAction(RubyActionProvider.COMMAND_RSPEC, bundle.getString("LBL_RSpec"), null)); // NOI18N
            }
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
//            if (broken) {
//                actions.add(brokenLinksAction);
//            }
            actions.add(CommonProjectActions.customizeProjectAction());
            
            return actions.toArray(new Action[actions.size()]);
        }
        
//        private void setBroken(boolean broken) {
//            this.broken = broken;
//            //brokenLinksAction.setEnabled(broken);
//            fireIconChange();
//            fireOpenedIconChange();
//            fireDisplayNameChange(null, null);
//        }
//        
//        private void setIllegalState (boolean illegalState) {
//            this.illegalState = illegalState;
//            fireIconChange();
//            fireOpenedIconChange();
//            fireDisplayNameChange(null, null);
//        }
//        
//        /** This action is created only when project has broken references.
//         * Once these are resolved the action is disabled.
//         */
//        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, ChangeListener, Runnable {
//            
//            private RequestProcessor.Task task = null;
//            
//            private PropertyChangeListener weakPCL;
//            
//            public BrokenLinksAction() {
//                putValue(Action.NAME, NbBundle.getMessage(RubyLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
//                setEnabled(broken);
//                evaluator.addPropertyChangeListener(this);
//                // When evaluator fires changes that platform properties were
//                // removed the platform still exists in JavaPlatformManager.
//                // That's why I have to listen here also on JPM:
//                weakPCL = WeakListeners.propertyChange(this, JavaPlatformManager.getDefault());
//                JavaPlatformManager.getDefault().addPropertyChangeListener(weakPCL);
//                RubyLogicalViewProvider.this.addChangeListener((ChangeListener) WeakListeners.change(this, RubyLogicalViewProvider.this));
//            }
//            
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    helper.requestSave();
//                    BrokenReferencesSupport.showCustomizer(helper.getRakeProjectHelper(), resolver, getBreakableProperties(), new String[] {RubyProjectProperties.JAVA_PLATFORM});
//                    run();
//                } catch (IOException ioe) {
//                    ErrorManager.getDefault().notify(ioe);
//                }
//            }
//            
//            public void propertyChange(PropertyChangeEvent evt) {
//                refsMayChanged();
//            }
//            
//            
//            public void stateChanged(ChangeEvent evt) {
//                refsMayChanged();
//            }
//            
//            public synchronized void run() {
//                boolean old = RubyLogicalViewRootNode.this.broken;
//                boolean broken = hasBrokenLinks();
//                if (old != broken) {
//                    setBroken(broken);
//                }
//                
//                old = RubyLogicalViewRootNode.this.illegalState;
//                broken = hasInvalidJdkVersion ();
//                if (old != broken) {
//                    setIllegalState(broken);
//                }
//            }
//            
//            private void refsMayChanged() {
//                // check project state whenever there was a property change
//                // or change in list of platforms.
//                // Coalesce changes since they can come quickly:
//                if (task == null) {
//                    task = BROKEN_LINKS_RP.create(this);
//                }
//                task.schedule(100);
//            }
//            
//        }
//        
    }
    
}
