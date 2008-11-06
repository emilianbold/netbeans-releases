package org.netbeans.modules.mobility.project.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.mobility.project.J2MEActionProvider;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

final class J2MEProjectRootNode extends AbstractNode implements AntProjectListener, PropertyChangeListener, Runnable {
    private volatile boolean broken;
    final Task nodeUpdateTask;
    PropertyChangeListener ref1;
    PropertyChangeListener ref3;
    ProjectRootNodeChildren childFactory;

    public J2MEProjectRootNode(J2MEProject project) {
        this(project, new ProjectRootNodeChildren(project));
    }

    private J2MEProjectRootNode(J2MEProject project, ProjectRootNodeChildren childFactory) {
        super(Children.create(childFactory, true), Lookups.singleton(project));
        this.broken = project.hasBrokenLinks();
        this.nodeUpdateTask = RequestProcessor.getDefault().create(this);
        setName(ProjectUtils.getInformation(project).getDisplayName());
        AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
        assert helper != null;
        helper.addAntProjectListener(this);
        this.ref1 = WeakListeners.propertyChange(this, JavaPlatformManager.getDefault());
        this.ref3 = WeakListeners.propertyChange(this, LibraryManager.getDefault());
        LibraryManager.getDefault().addPropertyChangeListener(ref3);
        JavaPlatformManager.getDefault().addPropertyChangeListener(ref1);
    }

    protected boolean testSourceRoot() {
        AntProjectHelper helper = getLookup().lookup(J2MEProject.class).getLookup().lookup(AntProjectHelper.class);
        return helper.resolveFileObject(helper.getStandardPropertyEvaluator().getProperty("src.dir")) != null;
    }

    protected void checkBroken() {
        nodeUpdateTask.schedule(50);
    }

    public void run() {
        J2MEProject project = getLookup().lookup(J2MEProject.class);
        assert project != null;
        boolean br = project.hasBrokenLinks();
        boolean changed = false;
        if (broken != br) {
            broken ^= true;
            //faster way of negation
            changed = true;
        }
        fireIconChange();
        fireOpenedIconChange();
        fireDisplayNameChange(null, null);
    }

    protected boolean isBroken() {
        J2MEProject project = getLookup().lookup(J2MEProject.class);
        assert project != null;
        return project.hasBrokenLinks();
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }
    private static final String ICON =
            "org/netbeans/modules/mobility/project/ui/resources/mobile-project.png"; //NOI18N
    private static final String BROKEN_ICON_BADGE =
            "org/netbeans/modules/mobility/project/ui/resources/brokenProjectBadge.gif"; //NOI18N

    @Override
    public Image getIcon(final int type) {
        final Image image = ImageUtilities.loadImage(
                ICON, true); //NOI18N
        return broken ?
            ImageUtilities.mergeImages(image, ImageUtilities.loadImage(
                BROKEN_ICON_BADGE), 8, 0)
            : image; //NOI18N
    }

    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        String dispName = super.getDisplayName();
        try {
            dispName = XMLUtil.toElementContent(dispName);
        } catch (CharConversionException ex) {
        }
        return broken ? "<font color=\"!nb.errorForeground\">" + dispName + "</font>" : null;
    }

    @Override
    public Node.PropertySet[] getPropertySets() {
        return new Node.PropertySet[0];
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(J2MEProjectRootNode.class);
    }

    @Override
    public synchronized Action[] getActions(final boolean context) {
        if (context) {
            return new Action[0];
        }
        Action[] actions = null;
        Action[] actionsBroken = null;
        if (actions == null) {
            final ArrayList<Action> act = new ArrayList<Action>();
            final ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
            act.add(CommonProjectActions.newFileAction());
            act.add(null);
            act.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(J2MEActionProvider.COMMAND_JAVADOC, bundle.getString("LBL_JavadocAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(J2MEActionProvider.COMMAND_DEPLOY, bundle.getString("LBL_DeployAction_Name"), null));
            // NOI18N
            act.add(null);
            act.add(ProjectSensitiveActions.projectCommandAction(J2MEActionProvider.COMMAND_BUILD_ALL, bundle.getString("LBL_BuildAllAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(J2MEActionProvider.COMMAND_REBUILD_ALL, bundle.getString("LBL_RebuildAllAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(J2MEActionProvider.COMMAND_CLEAN_ALL, bundle.getString("LBL_CleanAllAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(J2MEActionProvider.COMMAND_DEPLOY_ALL, bundle.getString("LBL_DeployAllAction_Name"), null));
            // NOI18N
            act.add(null);
            act.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(J2MEActionProvider.COMMAND_RUN_WITH, bundle.getString("LBL_RunWithAction_Name"), null));
            // NOI18N
            act.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null));
            // NOI18N
            act.add(null);
            act.add(CommonProjectActions.setAsMainProjectAction());
            act.add(CommonProjectActions.openSubprojectsAction());
            act.add(CommonProjectActions.closeProjectAction());
            act.add(null);
            act.add(CommonProjectActions.renameProjectAction());
            act.add(CommonProjectActions.moveProjectAction());
            act.add(CommonProjectActions.copyProjectAction());
            act.add(CommonProjectActions.deleteProjectAction());
            act.add(null);
            act.add(SystemAction.get(FindAction.class));
            act.add(null);
            act.add(new RefreshPackagesAction(getLookup().lookup(J2MEProject.class)));
            act.add(null);
            // honor 57874 contact
            act.add(null);
            act.addAll(Utilities.actionsForPath("Projects/Actions"));
            // NOI18N
            act.add(null);
            act.add(CommonProjectActions.customizeProjectAction());
            actions = act.toArray(new Action[act.size()]);
            act.add(act.size() - 1, createBrokenLinksAction());
            actionsBroken = act.toArray(new Action[act.size()]);
        }
        return broken ? actionsBroken : actions;
    }

    private Action createBrokenLinksAction() {
        return new BrokenLinksAction ();
    }

    private final class BrokenLinksAction extends AbstractAction {
        BrokenLinksAction () {
            putValue(NAME, NbBundle.getMessage(BrokenLinksAction.class,
                "LAB_ResolveReferenceProblems")); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            J2MEProject project = getLookup().lookup(J2MEProject.class);
            assert project != null;
            ReferenceHelper refHelper = project.getLookup().lookup(ReferenceHelper.class);
            assert refHelper != null;
            AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
            assert helper != null;
            // here is required list of all platforms, not just the default one !!!!!!!!!!!
            BrokenReferencesSupport.showCustomizer(helper, refHelper, project.getBreakableProperties(),
                    project.getBreakablePlatformProperties());
            checkBroken();
        }
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
    }

    public void propertiesChanged(AntProjectEvent ev) {
        checkBroken();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        checkBroken();
    }

    private static class RefreshPackagesAction extends AbstractAction {
        private final J2MEProject project;
        public RefreshPackagesAction(J2MEProject project) {
            super(NbBundle.getMessage(RefreshPackagesAction.class, "LAB_RefreshFolders")); //NOI18N
            this.project = project;
        }

        public void actionPerformed (final ActionEvent e) {
            AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
            refreshRecursively(helper.resolveFileObject(helper.getStandardPropertyEvaluator().getProperty("src.dir")));//NOI18N
        }

        private void refreshRecursively(final FileObject fo) {
            if (fo == null) return ;
            fo.refresh();
            for (FileObject curr : NbCollections.iterable(fo.getChildren(false))) {
                refreshRecursively (curr);
            }
        }
    }
}
