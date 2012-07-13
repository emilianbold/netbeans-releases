/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.java.j2seproject.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class J2SELogicalViewProvider implements LogicalViewProvider2 {
    
    private static final RequestProcessor RP = new RequestProcessor(J2SELogicalViewProvider.class);
    
    private final J2SEProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper resolver;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final PropertyChangeListener pcl;
    private Map<URL,Object[]> activeLibManLocs;
    private volatile boolean listenersInited;
    
    public J2SELogicalViewProvider(J2SEProject project, UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.resolver = resolver;
        assert resolver != null;
        pcl = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                if (LibraryManager.PROP_OPEN_LIBRARY_MANAGERS.equals(evt.getPropertyName())) {
                    addLibraryManagerListener();
                }
                testBroken();
            }
        };
    }

    private void initListeners() {
        if (listenersInited) {
            return;
        }
        ProjectManager.mutex().readAccess(new Runnable() {
            @Override
            public void run() {
                synchronized (J2SELogicalViewProvider.class) {
                    if (!listenersInited) {
                        evaluator.addPropertyChangeListener(pcl);
                        JavaPlatformManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(pcl, JavaPlatformManager.getDefault()));
                        LibraryManager.addOpenManagersPropertyChangeListener(new OpenManagersWeakListener(pcl));
                        addLibraryManagerListener();
                        listenersInited = true;
                    }
                }
            }
        });
    }

    private void addLibraryManagerListener() {
        final Map<URL,Object[]> oldLMs;
        final boolean attachToDefault;
        synchronized (this) {
            attachToDefault = activeLibManLocs == null;
            if (attachToDefault) {
                activeLibManLocs = new HashMap<URL,Object[]>();
            }
            oldLMs = new HashMap<URL,Object[]>(activeLibManLocs);
        }
        if (attachToDefault) {
            final LibraryManager manager = LibraryManager.getDefault();
            manager.addPropertyChangeListener(WeakListeners.propertyChange(pcl, manager));
        }
        final Collection<? extends LibraryManager> managers = LibraryManager.getOpenManagers();
        final Map<URL,LibraryManager> managerByLocation = new HashMap<URL, LibraryManager>();
        for (LibraryManager manager : managers) {
            final URL url = manager.getLocation();
            if (url != null) {
                managerByLocation.put(url, manager);
            }
        }
        final HashMap<URL,Object[]> toRemove = new HashMap<URL,Object[]>(oldLMs);
        toRemove.keySet().removeAll(managerByLocation.keySet());
        for (Object[] pair : toRemove.values()) {
            ((LibraryManager)pair[0]).removePropertyChangeListener((PropertyChangeListener)pair[1]);
        }
        managerByLocation.keySet().removeAll(oldLMs.keySet());
        final HashMap<URL,Object[]> toAdd = new HashMap<URL,Object[]>();
        for (Map.Entry<URL,LibraryManager> e : managerByLocation.entrySet()) {
            final LibraryManager manager = e.getValue();
            final PropertyChangeListener listener = WeakListeners.propertyChange(pcl, manager);
            manager.addPropertyChangeListener(listener);
            toAdd.put(e.getKey(), new Object[] {manager, listener});
        }
        synchronized (this) {
            activeLibManLocs.keySet().removeAll(toRemove.keySet());
            activeLibManLocs.putAll(toAdd);
        }
    }
    
    @Override public Node createLogicalView() {
        initListeners();
        return new J2SELogicalViewRootNode();
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
    
    @Override
    public Node findPath(Node root, Object target) {
        Project prj = root.getLookup().lookup(Project.class);
        if (prj == null) {
            return null;
        }
        
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            if (isOtherProjectSource(fo, prj)) {
                return null; // Don't waste time if project does not own the fo among sources
            }
            
            for (Node n : root.getChildren().getNodes(true)) {
                Node result = PackageView.findPath(n, target);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }

    private static boolean isOtherProjectSource(
            @NonNull final FileObject fo,
            @NonNull final Project me) {
        final Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            return false;
        }
        if (me.equals(owner)) {
            return false;
        }
        for (SourceGroup sg : ProjectUtils.getSources(owner).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (FileUtil.isParentOf(sg.getRootFolder(), fo)) {
                return true;
            }
        }
        return false;
    }
    
    
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private final RequestProcessor.Task task = RP.create(new Runnable() {
        public @Override void run() {
            boolean old = broken;
            boolean _broken = hasBrokenLinks();
            if (old != _broken) {
                setBroken(_broken);
            }
            old = illegalState;
            boolean _illegalState = hasInvalidJdkVersion();
            if (old != _illegalState) {
                setIllegalState(_illegalState);
            }
            old = compileOnSaveDisabled;
            boolean _compileOnSaveDisabled = isCompileOnSaveDisabled();
            if (old != _compileOnSaveDisabled) {
                setCompileOnSaveDisabled(_compileOnSaveDisabled);
            }
        }
    });

    /**
     * Used by J2SEProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advises him to use BrokenLinksAction to correct it.
     */
    public @Override void testBroken() {
        task.schedule(500);
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static final String[] BREAKABLE_PROPERTIES = {
        ProjectProperties.JAVAC_CLASSPATH,
        ProjectProperties.RUN_CLASSPATH,
        J2SEProjectProperties.DEBUG_CLASSPATH,
        ProjectProperties.RUN_TEST_CLASSPATH,
        J2SEProjectProperties.DEBUG_TEST_CLASSPATH,
        ProjectProperties.ENDORSED_CLASSPATH,
        ProjectProperties.JAVAC_TEST_CLASSPATH,
    };
    
    public boolean hasBrokenLinks () {
        return BrokenReferencesSupport.isBroken(helper.getAntProjectHelper(), resolver, getBreakableProperties(),
                new String[] {J2SEProjectProperties.JAVA_PLATFORM});
    }
    
    private boolean hasInvalidJdkVersion () {
        String javaSource = this.evaluator.getProperty("javac.source");     //NOI18N
        String javaTarget = this.evaluator.getProperty("javac.target");    //NOI18N
        if (javaSource == null && javaTarget == null) {
            //No need to check anything
            return false;
        }
        
        final String platformId = this.evaluator.getProperty("platform.active");  //NOI18N
        final JavaPlatform activePlatform = CommonProjectUtils.getActivePlatform (platformId);
        if (activePlatform == null) {
            return true;
        }        
        SpecificationVersion platformVersion = activePlatform.getSpecification().getVersion();
        try {
            return (javaSource != null && new SpecificationVersion (javaSource).compareTo(platformVersion)>0)
                   || (javaTarget != null && new SpecificationVersion (javaTarget).compareTo(platformVersion)>0);
        } catch (NumberFormatException nfe) {
            ErrorManager.getDefault().log("Invalid javac.source: "+javaSource+" or javac.target: "+javaTarget+" of project:"
                +this.project.getProjectDirectory().getPath());
            return true;
        }
    }

    private boolean isCompileOnSaveDisabled() {
         return !J2SEProjectUtil.isCompileOnSaveEnabled(project) && J2SEProjectUtil.isCompileOnSaveSupported(project);
    }
    
    public String[] getBreakableProperties() {
        SourceRoots roots = this.project.getSourceRoots();
        String[] srcRootProps = roots.getRootProperties();
        roots = this.project.getTestSourceRoots();
        String[] testRootProps = roots.getRootProperties();
        String[] result = new String [BREAKABLE_PROPERTIES.length + srcRootProps.length + testRootProps.length];
        System.arraycopy(BREAKABLE_PROPERTIES, 0, result, 0, BREAKABLE_PROPERTIES.length);
        System.arraycopy(srcRootProps, 0, result, BREAKABLE_PROPERTIES.length, srcRootProps.length);
        System.arraycopy(testRootProps, 0, result, BREAKABLE_PROPERTIES.length + srcRootProps.length, testRootProps.length);
        return result;
    }

    public String[] getPlatformProperties() {
        return new String[] {J2SEProjectProperties.JAVA_PLATFORM};
    }
    
    private static Image brokenProjectBadge = ImageUtilities.loadImage("org/netbeans/modules/java/j2seproject/ui/resources/brokenProjectBadge.gif", true);
    private static final String COMPILE_ON_SAVE_DISABLED_BADGE_PATH = "org/netbeans/modules/java/j2seproject/ui/resources/compileOnSaveDisabledBadge.gif";
    private static final Image compileOnSaveDisabledBadge;

    static {
        URL errorBadgeIconURL = J2SELogicalViewProvider.class.getClassLoader().getResource(COMPILE_ON_SAVE_DISABLED_BADGE_PATH);
        String compileOnSaveDisabledTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + NbBundle.getMessage(J2SELogicalViewProvider.class, "TP_CompileOnSaveDisabled");
        compileOnSaveDisabledBadge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(COMPILE_ON_SAVE_DISABLED_BADGE_PATH), compileOnSaveDisabledTP); // NOI18N
    }

    private final class J2SELogicalViewRootNode extends AbstractNode implements ChangeListener, PropertyChangeListener {

        private final ProjectInformation info = ProjectUtils.getInformation(project);
        
        @SuppressWarnings("LeakingThisInConstructor")
        public J2SELogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-java-j2seproject/Nodes"), 
                  Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png");
            if (hasBrokenLinks()) {
                broken = true;
            }
            else if (hasInvalidJdkVersion ()) {
                illegalState = true;
            }
            compileOnSaveDisabled = isCompileOnSaveDisabled();
            addChangeListener(WeakListeners.change(this, J2SELogicalViewProvider.this));
            info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
        }

        @Override
        public String getShortDescription() {
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(J2SELogicalViewProvider.class, "HINT_project_root_node", prjDirDispName);
        }
        
        @Override
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }            
            return broken || illegalState ? "<font color=\"#"+Integer.toHexString(getErrorForeground().getRGB() & 0xffffff) +"\">" + dispName + "</font>" : null; //NOI18N
        }
        
        @Override
        public Image getIcon(int type) {
            Image original = super.getIcon(type);

            if (broken || illegalState) {
                return ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0);
            } else {
                return compileOnSaveDisabled ? ImageUtilities.mergeImages(original, compileOnSaveDisabledBadge, 8, 0) : original;
            }
        }
        
        @Override
        public Image getOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            
            if (broken || illegalState) {
                return ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0);
            } else {
                return compileOnSaveDisabled ? ImageUtilities.mergeImages(original, compileOnSaveDisabledBadge, 8, 0) : original;
            }
        }
        
        public @Override void stateChanged(ChangeEvent e) {
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }

        public @Override void propertyChange(PropertyChangeEvent evt) {
            RP.post(new Runnable() {
                public @Override void run() {
                    fireNameChange(null, null);
                    fireDisplayNameChange(null, null);
                }
            });
        }

        @Override
        public Action[] getActions( boolean context ) {
            return CommonProjectActions.forType("org-netbeans-modules-java-j2seproject"); // NOI18N
        }
        
        @Override
        public boolean canRename() {
            return true;
        }
        
        public @Override String getName() {
            return info.getDisplayName();
        }

        @Override
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(J2SELogicalViewRootNode.class);
        }

    }

    private boolean broken;         //Represents a state where project has a broken reference repairable by broken reference support
    private boolean illegalState;   //Represents a state where project is not in legal state, eg invalid source/target level
    private boolean compileOnSaveDisabled;  //true iff Compile-on-Save is disabled

    // Private methods -------------------------------------------------

    private void setBroken(boolean broken) {
        this.broken = broken;
        changeSupport.fireChange();
    }

    private void setIllegalState (boolean illegalState) {
        this.illegalState = illegalState;
        changeSupport.fireChange();
    }

    private void setCompileOnSaveDisabled (boolean value) {
        this.compileOnSaveDisabled = value;
        changeSupport.fireChange();
    }

    private static @NonNull Color getErrorForeground() {
        Color result = UIManager.getDefaults().getColor("nb.errorForeground");  //NOI18N
        if (result == null) {
            result = Color.RED;
        }
        return result;
    }

    @ActionID(id = "org.netbeans.modules.java.j2seproject.ui.J2SELogicalViewProvider$BrokenLinksActionFactory", category = "Project")
    @ActionRegistration(displayName = "#LBL_Fix_Broken_Links_Action", lazy=false)
    @ActionReference(position = 2600, path = "Projects/org-netbeans-modules-java-j2seproject/Actions")
    public static final class BrokenLinksActionFactory extends AbstractAction implements ContextAwareAction {

        /** for layer registration */
        public BrokenLinksActionFactory() {
            putValue(Action.NAME, NbBundle.getMessage(J2SELogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        public @Override void actionPerformed(ActionEvent e) {
            assert false;
        }

        public @Override Action createContextAwareInstance(Lookup actionContext) {
            Collection<? extends Project> p = actionContext.lookupAll(Project.class);
            if (p.size() != 1) {
                return this;
            }
            J2SELogicalViewProvider lvp = p.iterator().next().getLookup().lookup(J2SELogicalViewProvider.class);
            if (lvp == null) {
                return this;
            }
            return lvp.new BrokenLinksAction();
        }

    }

    /** This action is created only when project has broken references.
     * Once these are resolved the action is disabled.
     */
    private class BrokenLinksAction extends AbstractAction {

        public BrokenLinksAction() {
            putValue(Action.NAME, NbBundle.getMessage(J2SELogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
            setEnabled(broken);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                helper.requestUpdate();
                BrokenReferencesSupport.showCustomizer(helper.getAntProjectHelper(), resolver, getBreakableProperties(), getPlatformProperties());
                testBroken();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }

    }

    private static class OpenManagersWeakListener extends WeakReference<PropertyChangeListener> implements Runnable, PropertyChangeListener {

        public OpenManagersWeakListener(final PropertyChangeListener listener) {
            super(listener, Utilities.activeReferenceQueue());
        }

        @Override
        public void run() {
            LibraryManager.removeOpenManagersPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final PropertyChangeListener listener = get();
            if (listener != null) {
                listener.propertyChange(evt);
            }
        }

    }

}
