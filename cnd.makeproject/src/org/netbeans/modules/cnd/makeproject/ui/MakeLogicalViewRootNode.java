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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.project.BrokenIncludes;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectConfigurationProvider;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.actions.AddExistingFolderItemsAction;
import org.netbeans.modules.cnd.makeproject.api.actions.AddExistingItemAction;
import org.netbeans.modules.cnd.makeproject.api.actions.NewFolderAction;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.XMLUtil;

/**
 * Filter node contain additional features for the Make physical
 *
 * @author Alexander Simon
 */
final class MakeLogicalViewRootNode extends AnnotatedNode implements ChangeListener, LookupListener, PropertyChangeListener {

    private static final boolean SYNC_PROJECT_ACTION = Boolean.getBoolean("cnd.remote.sync.project.action"); // NOI18N

    private boolean brokenLinks;
    private boolean brokenIncludes;
    private boolean brokenProject;
    private boolean incorrectVersion;
    private boolean incorrectPlatform;
    private boolean checkVersion = true;
    private Folder folder;
    private final Lookup.Result<BrokenIncludes> brokenIncludesResult;
    private final MakeLogicalViewProvider provider;
    private final InstanceContent ic;

    public MakeLogicalViewRootNode(Folder folder, MakeLogicalViewProvider provider, InstanceContent ic) {
        this(new ProjectRootChildren(folder, provider),folder, provider, ic);
    }
    
    private MakeLogicalViewRootNode(ProjectRootChildren children, Folder folder, MakeLogicalViewProvider provider, InstanceContent ic) {
        super(children, new AbstractLookup(ic), MakeLogicalViewProvider.ANNOTATION_RP);
        children.setMakeLogicalViewRootNode(MakeLogicalViewRootNode.this);
        this.ic = ic;
        this.folder = folder;
        this.provider = provider;        
//        setChildren(new ProjectRootChildren(folder, provider));
        setIconBaseWithExtension(MakeConfigurationDescriptor.ICON);
        setName(ProjectUtils.getInformation(provider.getProject()).getDisplayName());

        brokenIncludesResult = Lookup.getDefault().lookup(new Lookup.Template<BrokenIncludes>(BrokenIncludes.class));
        brokenIncludesResult.addLookupListener(MakeLogicalViewRootNode.this);
        resultChanged(null);

        brokenLinks = provider.hasBrokenLinks();
        brokenIncludes = hasBrokenIncludes(provider.getProject());
        if (gotMakeConfigurationDescriptor()) {
            incorrectVersion = !isCorectVersion(provider.getMakeConfigurationDescriptor().getVersion());
            if (incorrectVersion) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        (new ResolveIncorrectVersionAction(MakeLogicalViewRootNode.this, new VisualUpdater())).actionPerformed(null);                
                    }
                });
            }
        }
        incorrectPlatform = isIncorrectPlatform();
        if (incorrectPlatform) {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    (new ResolveIncorrectPlatformAction(MakeLogicalViewRootNode.this)).actionPerformed(null);                
                }
            });
        }                    
        // Handle annotations
        setForceAnnotation(true);
        if (folder != null) {
            updateAnnotationFiles();
        }
        ProjectInformation pi = provider.getProject().getLookup().lookup(ProjectInformation.class);
        pi.addPropertyChangeListener(WeakListeners.propertyChange(MakeLogicalViewRootNode.this, pi));
        ToolsCacheManager.addChangeListener(WeakListeners.change(MakeLogicalViewRootNode.this, null));
        if (gotMakeConfigurationDescriptor()) {
            MakeProjectConfigurationProvider confProvider = provider.getProject().getLookup().lookup(MakeProjectConfigurationProvider.class);
            if (confProvider != null){
                confProvider.addPropertyChangeListener(WeakListeners.propertyChange(MakeLogicalViewRootNode.this, confProvider));
            }
            
            
        }

    }

    @Override
    public String getHtmlDisplayName() {
        String ret = getHtmlDisplayName2();
        ExecutionEnvironment env = provider.getProject().getRemoteFileSystemHost();
        if (env != null && env.isRemote()) {
            if (ret == null) {
                ret = getName();
            }
            ret = ret + " <font color=''!controlShadow''>[" + env.getDisplayName() + "]"; // NOI18N
        }
        return ret;
    }
    
    private String getHtmlDisplayName2() {
        String ret;
        if (brokenLinks || incorrectVersion || incorrectPlatform) {
            ret = getName();
        } else {
            ret = super.getHtmlDisplayName();
        }
        if (brokenLinks || incorrectVersion || incorrectPlatform) {
            try {
                ret = XMLUtil.toElementContent(ret);
            } catch (CharConversionException ex) {
                return ret;
            }
            return "<font color=\"#"+Integer.toHexString(getErrorForeground().getRGB() & 0xffffff) +"\">" + ret + "</font>"; //NOI18N
        }
        return ret;
    }

    private static Color getErrorForeground() {
        Color result = UIManager.getDefaults().getColor("nb.errorForeground");  //NOI18N
        if (result == null) {
            result = Color.RED;
        }
        return result;
    }

    public void reInit(MakeConfigurationDescriptor configurationDescriptor) {
        Folder logicalFolders = configurationDescriptor.getLogicalFolders();
        if (folder != null) {
            ic.remove(folder);
        }
        folder = logicalFolders;
        ic.add(logicalFolders);
        setChildren(new LogicalViewChildren(folder, provider));
        MakeProjectConfigurationProvider confProvider = provider.getProject().getLookup().lookup(MakeProjectConfigurationProvider.class);
        if (confProvider != null) {
            confProvider.addPropertyChangeListener(WeakListeners.propertyChange(MakeLogicalViewRootNode.this, confProvider));
        }
        stateChanged(null);
    }
    
    void reInitWithRemovedPrivate() {
        try {
            provider.getMakeConfigurationDescriptor().getNbPrivateProjectFileObject().delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }        
        reInit(provider.getMakeConfigurationDescriptor());
    }
    
    void reInitWithUnsupportedVersion() {
        checkVersion = false;
        reInit(provider.getMakeConfigurationDescriptor());
    }
    
    private void setRealProjectFolder(Folder folder) {
        assert folder != null;
        if (this.folder != null) {
            ic.remove(this.folder);
        }
        this.folder = folder;
        ic.add(folder);
        stateChanged(null);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // to reinvalidate Run/Debug and other toolbar buttons, we use the workaround with selection
                    // remember selection
                    Node[] selectedNodes = ProjectTabBridge.getInstance().getExplorerManager().getSelectedNodes();
                    // clear
                    ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(new Node[0]);
                    // restore
                    ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(selectedNodes);
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public Folder getFolder() {
        return folder;
    }

    private MakeProject getProject() {
        return provider.getProject();
    }

    private boolean gotMakeConfigurationDescriptor() {
        return provider.gotMakeConfigurationDescriptor();
    }

    MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        return provider.getMakeConfigurationDescriptor();
    }

    private void updateAnnotationFiles() {
        // Add project directory
        FileObject fo = getProject().getProjectDirectory();
        if (fo == null || !fo.isValid()) {
            // See IZ 125880
            Logger.getLogger("cnd.makeproject").log(Level.WARNING, "project.getProjectDirectory() == null - {0}", getProject());
        }
        if (!gotMakeConfigurationDescriptor()) {
            return;
        }
        // Add buildfolder from makefile projects to sources. See IZ 90190.
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return;
        }
        Configurations confs = makeConfigurationDescriptor.getConfs();
        if (confs == null) {
            return;
        }
        Set<FileObject> set = new LinkedHashSet<FileObject>();
        for (Configuration conf : confs.toArray()) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
            if (makeConfiguration.isMakefileConfiguration()) {
                MakefileConfiguration makefileConfiguration = makeConfiguration.getMakefileConfiguration();
                FileObject buildCommandFO = makefileConfiguration.getAbsBuildCommandFileObject();
                if (buildCommandFO != null && buildCommandFO.isValid()) {
                    try {
                        FileObject fileObject = CndFileUtils.getCanonicalFileObject(buildCommandFO);
                        if (fileObject != null /*paranoia*/ && fileObject.isValid()) {
                            set.add(fileObject);
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace(System.err);
                    }
                }
            }
        }
        set.add(getProject().getProjectDirectory());
        setFiles(set);
        Folder aFolder = folder;
        if (aFolder != null) {
            List<Folder> allFolders = new ArrayList<Folder>();
            allFolders.add(aFolder);
            allFolders.addAll(aFolder.getAllFolders(true));
            Iterator<Folder> iter = allFolders.iterator();
            while (iter.hasNext()) {
                iter.next().addChangeListener(this);
            }
        }
    }

    @Override
    public String getShortDescription() {
        return MakeLogicalViewProvider.getShortDescription(provider.getProject());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setName(ProjectUtils.getInformation(provider.getProject()).getDisplayName());
        String prop = evt.getPropertyName();
        if (ProjectInformation.PROP_DISPLAY_NAME.equals(prop)) {
            fireDisplayNameChange(null, null);
        } else if (ProjectInformation.PROP_NAME.equals(prop)) {
            fireNameChange(null, null);
        } else if (ProjectInformation.PROP_ICON.equals(prop)) {
            fireIconChange();
        } else if (ProjectConfigurationProvider.PROP_CONFIGURATIONS.equals(prop)) {
            stateChanged(null) ;
        }
    }

    private final class VisualUpdater implements Runnable {

        @Override
        public void run() {
            if (brokenProject || incorrectVersion || incorrectPlatform) {
                MakeLogicalViewRootNode.this.setChildren(Children.LEAF);
            }
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
    }

    private boolean isIncorrectPlatform() {
        if (gotMakeConfigurationDescriptor()) {
            Configuration[] confs = provider.getMakeConfigurationDescriptor().getConfs().toArray();
            for (int i = 0; i < confs.length; i++) {
                MakeConfiguration conf = (MakeConfiguration) confs[i];
                if (conf.getDevelopmentHost().isLocalhost() && 
                    CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getPlatform() != conf.getDevelopmentHost().getBuildPlatformConfiguration().getValue()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /*
     * Something in the folder has changed
     **/
    @Override
    public void stateChanged(ChangeEvent e) {
        brokenLinks = provider.hasBrokenLinks();
        brokenIncludes = hasBrokenIncludes(getProject());
        if (provider.gotMakeConfigurationDescriptor()) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = provider.getMakeConfigurationDescriptor();
            if (makeConfigurationDescriptor != null) {
                brokenProject =  makeConfigurationDescriptor.getState() == State.BROKEN;
                incorrectPlatform = isIncorrectPlatform();
                incorrectVersion = !isCorectVersion(makeConfigurationDescriptor.getVersion());
                if (makeConfigurationDescriptor.getConfs().size() == 0 ) {
                    brokenProject = true;
                }
            }
        }
        updateAnnotationFiles();
        EventQueue.invokeLater(new VisualUpdater()); // IZ 151257
//            fireIconChange(); // MakeLogicalViewRootNode
//            fireOpenedIconChange();
//            fireDisplayNameChange(null, null);
    }

    private boolean isCorectVersion(int version) {
        if (checkVersion) {
            return version <= CommonConfigurationXMLCodec.CURRENT_VERSION;
        }
        return true;
    }    
    
    @Override
    public Object getValue(String valstring) {
        if (valstring == null) {
            return super.getValue(null);
        }
        if (valstring.equals("Folder")) // NOI18N
        {
            return folder;
        } else if (valstring.equals("Project")) // NOI18N
        {
            return getProject();
        } else if (valstring.equals("This")) // NOI18N
        {
            return this;
        }
        return super.getValue(valstring);
    }

    @Override
    public Image getIcon(int type) {
        ProjectInformation pi = provider.getProject().getLookup().lookup(ProjectInformation.class);
        return mergeBadge(annotateIcon(ImageUtilities.icon2Image(pi.getIcon()), type));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    private Image mergeBadge(Image original) {
        if (brokenLinks) {
            return ImageUtilities.mergeImages(original, MakeLogicalViewProvider.brokenLinkBadge, 8, 0);
        } else if (brokenIncludes) {
            return ImageUtilities.mergeImages(original, MakeLogicalViewProvider.brokenIncludeBadge, 8, 0);
        } else if (brokenProject || incorrectVersion || incorrectPlatform) {
            return ImageUtilities.mergeImages(original, MakeLogicalViewProvider.brokenProjectBadge, 8, 0);
        }
        return original;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (!gotMakeConfigurationDescriptor()) {
            return new Action[] {CommonProjectActions.closeProjectAction()};
        }
        List<Action> actions = new ArrayList<Action>();
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();

        // TODO: not clear if we need to call the following method at all
        // but we need to remove remembering the output to prevent memory leak;
        // I think it could be removed
        if (descriptor != null) {
            descriptor.getLogicalFolders();
        }

        // Add standard actions
        Action[] standardActions;
        MakeConfiguration active = (descriptor == null) ? null : descriptor.getActiveConfiguration();
        if (descriptor == null || active == null || active.isMakefileConfiguration()) { // FIXUP: need better check
            standardActions = getAdditionalDiskFolderActions();
        } else {
            standardActions = getAdditionalLogicalFolderActions();
        }
        actions.addAll(Arrays.asList(standardActions));
        actions.add(null);
        //actions.add(new CodeAssistanceAction());
        // makeproject sensitive actions
        final MakeProjectTypeImpl projectKind = provider.getProject().getLookup().lookup(MakeProjectTypeImpl.class);
        final List<? extends Action> actionsForMakeProject = Utilities.actionsForPath(projectKind.projectActionsPath());
        if (!actionsForMakeProject.isEmpty()) {
            actions.addAll(actionsForMakeProject);
            actions.add(null);
        }
        actions.add(SystemAction.get(org.openide.actions.FindAction.class));
        // all project sensitive actions
        actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
        // Add remaining actions
        actions.add(null);
        //actions.add(SystemAction.get(ToolsAction.class));
        if (brokenLinks) {
            actions.add(new ResolveReferenceAction(provider.getProject()));
        }
        if (incorrectVersion) {
            actions.add(new ResolveIncorrectVersionAction(this, new VisualUpdater()));
        }
        if (incorrectPlatform) {
            actions.add(new ResolveIncorrectPlatformAction(this));
        }
        //actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        if (active != null && active.isCustomConfiguration() && active.getProjectCustomizer().getActions(provider.getProject(), actions) != null) {
            return active.getProjectCustomizer().getActions(provider.getProject(), actions);
        } else {
            return actions.toArray(new Action[actions.size()]);
        }
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public PasteType getDropType(Transferable transferable, int action, int index) {
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE)) {
                return super.getDropType(transferable, action, index);
            }
        }
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable transferable, List<PasteType> list) {
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE)) {
                try {
                    ViewItemNode viewItemNode = (ViewItemNode) transferable.getTransferData(flavors[i]);
                    int type = new Integer(flavors[i].getParameter(MakeLogicalViewProvider.MASK)).intValue();
                    list.add(new ViewItemPasteType(this.getFolder(), viewItemNode, type, provider));
                } catch (Exception e) {
                }
            }
        }
        super.createPasteTypes(transferable, list);
    }

    // Private methods -------------------------------------------------
    private Action[] getAdditionalLogicalFolderActions() {

        ResourceBundle bundle = NbBundle.getBundle(MakeLogicalViewProvider.class);

        MoreBuildActionsAction mba = null;        
        if (gotMakeConfigurationDescriptor() && getMakeConfigurationDescriptor().getActiveConfiguration() != null && getMakeConfigurationDescriptor().getActiveConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
            mba = new MoreBuildActionsAction(new Action[]{
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null), // NOI18N
                ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, bundle.getString("LBL_BatchBuildAction_Name"), null), // NOI18N
            });
        } else {
            mba = new MoreBuildActionsAction(new Action[]{
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null), // NOI18N
                ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, bundle.getString("LBL_BatchBuildAction_Name"), null), // NOI18N
                ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BUILD_PACKAGE, bundle.getString("LBL_BuildPackagesAction_Name"), null), // NOI18N
            });
        }
        
        Action[] result = new Action[]{
            CommonProjectActions.newFileAction(),
            null,
            SystemAction.get(AddExistingItemAction.class),
            SystemAction.get(AddExistingFolderItemsAction.class),
            SystemAction.get(NewFolderAction.class),
            //new AddExternalItemAction(project),
            null,
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null), // NOI18N
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null), // NOI18N            
            mba,
            new SetConfigurationAction(getProject()),
            new RemoteDevelopmentAction(getProject()),
            null,
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null), // NOI18N
            //new DebugMenuAction(project, helper),
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null),
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG_STEP_INTO, bundle.getString("LBL_DebugAction_Step_Name"), null),
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null),
            //SystemAction.get(RunTestAction.class),
            null,
            CommonProjectActions.setAsMainProjectAction(),
            CommonProjectActions.openSubprojectsAction(),
            CommonProjectActions.closeProjectAction(),
            null,
            CommonProjectActions.renameProjectAction(),
            CommonProjectActions.moveProjectAction(),
            CommonProjectActions.copyProjectAction(),
            CommonProjectActions.deleteProjectAction(),
            null,};
        if (SYNC_PROJECT_ACTION) {
            result = NodeActionFactory.insertSyncActions(result, RemoteDevelopmentAction.class);
        }
        return result;
    }

    private Action[] getAdditionalDiskFolderActions() {

        ResourceBundle bundle = NbBundle.getBundle(MakeLogicalViewProvider.class);

        MoreBuildActionsAction mba = null;        
        if (gotMakeConfigurationDescriptor() && getMakeConfigurationDescriptor().getActiveConfiguration() != null && getMakeConfigurationDescriptor().getActiveConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
            mba = new MoreBuildActionsAction(new Action[]{
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null), // NOI18N
                ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, bundle.getString("LBL_BatchBuildAction_Name"), null), // NOI18N
            });
        } else {
            mba = new MoreBuildActionsAction(new Action[]{
                ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null), // NOI18N
                ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, bundle.getString("LBL_BatchBuildAction_Name"), null), // NOI18N
                ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BUILD_PACKAGE, bundle.getString("LBL_BuildPackagesAction_Name"), null), // NOI18N
            });
        }
        
        Action[] result = new Action[]{
            CommonProjectActions.newFileAction(),
            //null,
            //new AddExternalItemAction(project),
            null,
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null), // NOI18N
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null), // NOI18N
            mba,
            new SetConfigurationAction(getProject()),
            new RemoteDevelopmentAction(getProject()),
            null,
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null), // NOI18N
            //new DebugMenuAction(project, helper),
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null),
            ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG_STEP_INTO, bundle.getString("LBL_DebugAction_Step_Name"), null),
            null,
            CommonProjectActions.setAsMainProjectAction(),
            CommonProjectActions.openSubprojectsAction(),
            CommonProjectActions.closeProjectAction(),
            null,
            CommonProjectActions.renameProjectAction(),
            CommonProjectActions.moveProjectAction(),
            CommonProjectActions.copyProjectAction(),
            CommonProjectActions.deleteProjectAction(),
            null,};
        if (SYNC_PROJECT_ACTION) {
            result = NodeActionFactory.insertSyncActions(result, RemoteDevelopmentAction.class);
        }
        return result;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        for (BrokenIncludes elem : brokenIncludesResult.allInstances()) {
            elem.addChangeListener(this);
        }
    }

    private boolean hasBrokenIncludes(Project project) {
        BrokenIncludes biProvider = Lookup.getDefault().lookup(BrokenIncludes.class);
        if (biProvider != null) {
            NativeProject id = project.getLookup().lookup(NativeProject.class);
            if (id != null) {
                return biProvider.isBroken(id);
            }
        }
        return false;
    }

    private final static class ProjectRootChildren extends LogicalViewChildren {
        private MakeLogicalViewRootNode parent;
        private ProjectRootChildren(Folder folder, MakeLogicalViewProvider provider) {
            super(folder, provider);
        }

        @Override
        protected void onFolderChange(Folder folder) {
            assert parent != null;
            this.parent.setRealProjectFolder(folder);
        }
        
        private void setMakeLogicalViewRootNode(MakeLogicalViewRootNode parent) {
            assert this.parent == null;
            this.parent = parent;
        }

        @Override
        protected boolean isRoot() {
            return true;
        }
    }
}
