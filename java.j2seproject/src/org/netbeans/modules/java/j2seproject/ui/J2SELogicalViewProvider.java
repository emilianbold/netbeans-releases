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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.net.URL;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
@ActionReferences({
    @ActionReference(
        id=@ActionID(id="org.netbeans.modules.project.ui.problems.BrokenProjectActionFactory",category="Project"),
        position = 2600,
        path = "Projects/org-netbeans-modules-java-j2seproject/Actions")
})
public class J2SELogicalViewProvider implements LogicalViewProvider2 {
    
    private static final RequestProcessor RP = new RequestProcessor(J2SELogicalViewProvider.class);
    private static final String[] BREAKABLE_PROPERTIES = {
        ProjectProperties.JAVAC_CLASSPATH,
        ProjectProperties.RUN_CLASSPATH,
        J2SEProjectProperties.DEBUG_CLASSPATH,
        ProjectProperties.RUN_TEST_CLASSPATH,
        J2SEProjectProperties.DEBUG_TEST_CLASSPATH,
        ProjectProperties.ENDORSED_CLASSPATH,
        ProjectProperties.JAVAC_TEST_CLASSPATH,
    };
    private static final String COMPILE_ON_SAVE_DISABLED_BADGE_PATH = "org/netbeans/modules/java/j2seproject/ui/resources/compileOnSaveDisabledBadge.gif";
    private static final Image compileOnSaveDisabledBadge;

    static {
        URL errorBadgeIconURL = J2SELogicalViewProvider.class.getClassLoader().getResource(COMPILE_ON_SAVE_DISABLED_BADGE_PATH);
        String compileOnSaveDisabledTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + NbBundle.getMessage(J2SELogicalViewProvider.class, "TP_CompileOnSaveDisabled");
        compileOnSaveDisabledBadge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(COMPILE_ON_SAVE_DISABLED_BADGE_PATH), compileOnSaveDisabledTP); // NOI18N
    }
    
    private final J2SEProject project;
    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper resolver;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final PropertyChangeListener pcl;
    private final RequestProcessor.Task task = RP.create(new Runnable() {
        public @Override void run() {
            setBroken(ProjectProblems.isBroken(project));
            setCompileOnSaveDisabled(isCompileOnSaveDisabled());
        }
    });

    private volatile boolean listenersInited;
    private volatile boolean broken;         //Represents a state where project has a broken reference repairable by broken reference support
    private volatile boolean compileOnSaveDisabled;  //true iff Compile-on-Save is disabled
    
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
                final String propName = evt.getPropertyName();
                if (propName == null ||
                    ProjectProblemsProvider.PROP_PROBLEMS.equals(evt.getPropertyName()) ||
                    ProjectProperties.COMPILE_ON_SAVE.equals(evt.getPropertyName()) ||
                    propName.startsWith(ProjectProperties.COMPILE_ON_SAVE_UNSUPPORTED_PREFIX)) {
                    testBroken();
                }
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
                        final ProjectProblemsProvider ppp = project.getLookup().lookup(ProjectProblemsProvider.class);
                        if (ppp != null) {
                            ppp.addPropertyChangeListener(pcl);
                        }
                        listenersInited = true;
                    }
                }
            }
        });
    }
    
    @Override
    public Node createLogicalView() {
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

    @Override
    public void testBroken() {
        task.schedule(500);
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
    
    private boolean isCompileOnSaveDisabled() {
         return !J2SEProjectUtil.isCompileOnSaveEnabled(project) && J2SEProjectUtil.isCompileOnSaveSupported(project);
    }                

    private final class J2SELogicalViewRootNode extends AbstractNode implements ChangeListener, PropertyChangeListener {

        private final ProjectInformation info = ProjectUtils.getInformation(project);        
        
        @SuppressWarnings("LeakingThisInConstructor")
        public J2SELogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-java-j2seproject/Nodes"), 
                  Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png");
            broken = ProjectProblems.isBroken(project);
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
            return broken ? "<font color=\"#"+Integer.toHexString(getErrorForeground().getRGB() & 0xffffff) +"\">" + dispName + "</font>" : null; //NOI18N
        }
        
        @Override
        public Image getIcon(int type) {
            final Image original = super.getIcon(type);
            return !broken && compileOnSaveDisabled ? ImageUtilities.mergeImages(original, compileOnSaveDisabledBadge, 8, 0) : original;
        }
        
        @Override
        public Image getOpenedIcon(int type) {
            final Image original = super.getOpenedIcon(type);
            return !broken && compileOnSaveDisabled ? ImageUtilities.mergeImages(original, compileOnSaveDisabledBadge, 8, 0) : original;
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

    // Private methods -------------------------------------------------

    private void setBroken(boolean broken) {
        //Weak consistent, only visibility required
        if (this.broken != broken) {
            this.broken = broken;
            changeSupport.fireChange();
        }
    }    

    private void setCompileOnSaveDisabled (boolean value) {
        //Weak consistent, only visibility required
        if (this.compileOnSaveDisabled != value) {
            this.compileOnSaveDisabled = value;
            changeSupport.fireChange();
        }
    }

    private static @NonNull Color getErrorForeground() {
        Color result = UIManager.getDefaults().getColor("nb.errorForeground");  //NOI18N
        if (result == null) {
            result = Color.RED;
        }
        return result;
    }    
}
