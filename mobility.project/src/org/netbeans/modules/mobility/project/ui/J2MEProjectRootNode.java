package org.netbeans.modules.mobility.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import javax.swing.Action;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.actions.Actions;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

final class J2MEProjectRootNode extends AbstractNode implements AntProjectListener, PropertyChangeListener, Runnable {
    private volatile boolean broken;
    final Task nodeUpdateTask;
    PropertyChangeListener ref1;
    PropertyChangeListener ref3;
    ProjectRootNodeChildren childFactory;

    public J2MEProjectRootNode(J2MEProject project) {
        this(project, new ProjectRootNodeChildren(project), new BrokenCheckerImpl());
    }

    private J2MEProjectRootNode(J2MEProject project, ProjectRootNodeChildren childFactory, BrokenCheckerImpl bc) {
        super(Children.create(childFactory, true), Lookups.fixed(project, bc));
        bc.node = this;
        this.broken = project.hasBrokenLinks();
        this.nodeUpdateTask = project.getRequestProcessor().create(this);
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

    void doCheckBroken() {
        J2MEProject target = getLookup().lookup(J2MEProject.class);
        assert target != null;
        ReferenceHelper refHelper = target.getLookup().lookup(ReferenceHelper.class);
        assert refHelper != null;
        AntProjectHelper helper = target.getLookup().lookup(AntProjectHelper.class);
        assert helper != null;
        // here is required list of all platforms, not just the default one !!!!!!!!!!!
        BrokenReferencesSupport.showCustomizer(helper, refHelper, target.getBreakableProperties(),
                target.getBreakablePlatformProperties());
        checkBroken();
    }

    static class BrokenCheckerImpl implements BrokenChecker {
        J2MEProjectRootNode node;
        public void checkBroken() {
            node.doCheckBroken();
        }
    }

    public void run() {
        J2MEProject project = getLookup().lookup(J2MEProject.class);
        assert project != null;
        boolean br = project.hasBrokenLinks();
        boolean changed = broken != br;
        broken = br;
        if (changed) {
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
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
        return broken ? "<font color=\"!nb.errorForeground\">" + dispName + "</font>" : null; //NOI18N
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
    public Action[] getActions (boolean context) {
        return context ? new Action[0] : Actions.actions(broken);
    }


    public void configurationXmlChanged(AntProjectEvent ev) {
    }

    public void propertiesChanged(AntProjectEvent ev) {
        checkBroken();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        checkBroken();
    }
}
