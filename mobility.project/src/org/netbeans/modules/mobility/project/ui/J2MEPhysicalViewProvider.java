/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.project.ui;
import java.io.CharConversionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.project.*;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.J2MEActionProvider;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.java.platform.JavaPlatformManager; 
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ProxyLookup;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;


/**
 * Support for creating logical views.
 * @author Petr Hrebejk, Adam Sotona
 */
public class J2MEPhysicalViewProvider implements LogicalViewProvider {
        
    protected final ReferenceHelper refHelper;
    protected final ProjectConfigurationsHelper pcp;
    protected final AntProjectHelper helper;
    protected final J2MEProject project;
    J2MEProjectRootNode rootNode;
    
    public J2MEPhysicalViewProvider(Project project, AntProjectHelper helper, ReferenceHelper refHelper, ProjectConfigurationsHelper pcp) {
        this.project = (J2MEProject)project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.refHelper = refHelper;
        assert refHelper != null;
        this.pcp = pcp;
        assert pcp != null;
    }
    
    public Node createLogicalView() {
        try {
            return rootNode=new J2MEProjectRootNode();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            return Node.EMPTY;
        }
    }
    
    public Node findPath(final Node root, final Object target) {
        final Project project = root.getLookup().lookup(Project.class);
        if ( project == null ) {
            return null;
        }
        if ( target instanceof FileObject ) {
            final FileObject fo = (FileObject)target;
            final Project owner = FileOwnerQuery.getOwner( fo );
            if ( !project.equals( owner ) ) {
                return null; // Don't waste time if project does not own the fo
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
    
    protected String[] getBreakableProperties() {
        final ProjectConfiguration pc[] = pcp.getConfigurations().toArray(new ProjectConfiguration[0]);
        String s[] = new String[2*pc.length+1];
        s[0] = DefaultPropertiesDescriptor.SRC_DIR;
        for (int i= 0; i<pc.length; i++) {
            if (pcp.getDefaultConfiguration().equals(pc[i])) {
                s[2*i+1] = DefaultPropertiesDescriptor.LIBS_CLASSPATH;
                s[2*i+2] = DefaultPropertiesDescriptor.SIGN_KEYSTORE;
            } else {
                s[2*i+1] = J2MEProjectProperties.CONFIG_PREFIX + pc[i].getDisplayName() + "." + DefaultPropertiesDescriptor.LIBS_CLASSPATH; //NOI18N
                s[2*i+2] = J2MEProjectProperties.CONFIG_PREFIX + pc[i].getDisplayName() + "." + DefaultPropertiesDescriptor.SIGN_KEYSTORE; //NOI18N
            }
        }
        return s;
    }
    
    protected String[] getBreakablePlatformProperties() {
        final ProjectConfiguration pc[] = pcp.getConfigurations().toArray(new ProjectConfiguration[0]);
        String s[] = new String[pc.length];
        for (int i= 0; i<pc.length; i++) {
            if (pcp.getDefaultConfiguration().equals(pc[i])) {
                s[i] = DefaultPropertiesDescriptor.PLATFORM_ACTIVE;
            } else {
                s[i] = J2MEProjectProperties.CONFIG_PREFIX + pc[i].getDisplayName() + "." + DefaultPropertiesDescriptor.PLATFORM_ACTIVE; //NOI18N
            }
        }
        return s;
    }
    
    public void refreshNode(String name)
    {
        if (rootNode != null)
        {            
            LogicalViewChildren children=(LogicalViewChildren)rootNode.getChildren();            
            children.refreshNode(name);
            rootNode.checkBroken();
        }
    }
    
    public boolean hasBrokenLinks() {
        return BrokenReferencesSupport.isBroken( helper, refHelper, getBreakableProperties(), getBreakablePlatformProperties());
    }
    
    // Common class for all nodes in our project
    abstract static class ChildLookup extends ProxyLookup
    {
        abstract public Node[] createNodes(J2MEProject project) ;
    }

    // Private innerclasses ----------------------------------------------------
    final class LogicalViewChildren extends Children.Keys  
    {
        final private J2MEProject project;
        final private NodeCache cache;
        final private HashMap<String,ChildLookup> keyMap = new HashMap<String,ChildLookup>();
        
        private class CfgListener implements PropertyChangeListener, Runnable {
            public void propertyChange(PropertyChangeEvent evt) {
                RequestProcessor.getDefault().post(this);
            }
            
            public void run() {
                refreshResources();
            }
        }
        
        LogicalViewChildren(J2MEProject proj)
        {
            project=proj;
            cache=new NodeCache(proj);
            keyMap.put("Sources",new SourcesViewProvider());
            keyMap.put("Resources",new ResViewProvider(cache));
            keyMap.put("Configurations",new LibResViewProvider(cache));
            project.getConfigurationHelper().addPropertyChangeListener(new CfgListener());            
            setKeys(getKeys());            
        }
        
        public void refreshResources()
        {
            refreshKey("Resources");
        }
        
        public void refreshConfigurations()
        {
            refreshKey("Configurations");
        }
        
        public void refreshNode(String name)
        {
            cache.update(name);
        }
        
        
        protected Node[] createNodes(final Object key)
        {
            ChildLookup creator=keyMap.get(key);
            return creator != null ? creator.createNodes(project) : null;
        }

        
        private Collection<String> getKeys() {
            //#60800, #61584 - when the project is deleted externally do not try to create children, the source groups
            //are not valid
            if (project.getProjectDirectory() == null || !project.getProjectDirectory().isValid()) {
                return Collections.EMPTY_LIST;
            }
            final java.util.List<String> result =  new java.util.ArrayList<String>();
            result.add("Sources");
            result.add("Resources");
            result.add("Configurations");
            return result;
        }
    }
    
    /** Filter node containin additional features for the J2ME physical
     */
    final class J2MEProjectRootNode extends AbstractNode implements AntProjectListener, PropertyChangeListener, Runnable {
        
        private Action[] actions, actionsBroken;
        
        boolean broken;
        Image icon;
        final Task nodeUpdateTask;
        PropertyChangeListener ref1,ref3;
        
        public J2MEProjectRootNode() {
            super(new LogicalViewChildren(project), Lookups.singleton(project));
            this.broken = hasBrokenLinks();
            this.nodeUpdateTask = RequestProcessor.getDefault().create(this);
            setName( ProjectUtils.getInformation( project ).getDisplayName() );
            helper.addAntProjectListener(this);
            this.ref1 = WeakListeners.propertyChange(this, JavaPlatformManager.getDefault());
            this.ref3 = WeakListeners.propertyChange(this, LibraryManager.getDefault());
            LibraryManager.getDefault().addPropertyChangeListener(ref3);
            JavaPlatformManager.getDefault().addPropertyChangeListener(ref1);
        }
     
        protected boolean testSourceRoot() {
            return helper.resolveFileObject(helper.getStandardPropertyEvaluator().getProperty("src.dir")) != null;
        }
        
        protected void checkBroken() {
            nodeUpdateTask.schedule(50);
        }
        
        public void run() {
            boolean br=hasBrokenLinks();
            boolean changed = false;
            synchronized(J2MEProjectRootNode.this)
            {
                if (broken != br) {
                    broken ^= true; //faster way of negation
                    changed=true;
                }
            }
            if (changed) {
                icon = createIcon();
            }
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }

        protected boolean isBroken() {
            return hasBrokenLinks();
        }
        
        public boolean canCopy() {
            return false;
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        protected Image createIcon() {
            final Image icon = ImageUtilities.loadImage( "org/netbeans/modules/mobility/project/ui/resources/mobile-project.png", true ); // NOI18N
            return broken ? ImageUtilities.mergeImages(icon, ImageUtilities.loadImage( "org/netbeans/modules/mobility/project/ui/resources/brokenProjectBadge.gif" ), 8, 0) : icon; //NOI18N
        }
        
        public Image getIcon( final int type ) {
            if ( icon == null ) {
                icon = createIcon();
            }
            return icon;
        }
        
        public Image getOpenedIcon( final int type ) {
            return getIcon( type );
        }
        
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                // ignore
            }
            return broken ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }
        
        public Node.PropertySet[] getPropertySets() {
            return new Node.PropertySet[0];
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(J2MEPhysicalViewProvider.J2MEProjectRootNode.class);
        }
        
        public synchronized Action[] getActions( final boolean context ) {
            if (context) return new Action[0];
            if (actions == null) {
                final ArrayList<Action> act = new ArrayList<Action>();
                final ResourceBundle bundle = NbBundle.getBundle( J2MEPhysicalViewProvider.class );
                act.add(CommonProjectActions.newFileAction());
                act.add(null);
                act.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( J2MEActionProvider.COMMAND_JAVADOC, bundle.getString( "LBL_JavadocAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( J2MEActionProvider.COMMAND_DEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null )); // NOI18N
                act.add(null);
                act.add(ProjectSensitiveActions.projectCommandAction( J2MEActionProvider.COMMAND_BUILD_ALL, bundle.getString( "LBL_BuildAllAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( J2MEActionProvider.COMMAND_REBUILD_ALL, bundle.getString( "LBL_RebuildAllAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( J2MEActionProvider.COMMAND_CLEAN_ALL, bundle.getString( "LBL_CleanAllAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( J2MEActionProvider.COMMAND_DEPLOY_ALL, bundle.getString( "LBL_DeployAllAction_Name" ), null )); // NOI18N
                act.add(null);
                act.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_RUN, bundle.getString( "LBL_RunAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( J2MEActionProvider.COMMAND_RUN_WITH, bundle.getString( "LBL_RunWithAction_Name" ), null )); // NOI18N
                act.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null )); // NOI18N
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
                act.add(new RefreshPackagesAction());
                act.add(null);
                // honor 57874 contact
                act.add(null);
                act.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
                act.add(null);
                act.add(CommonProjectActions.customizeProjectAction());
                actions = act.toArray(new Action[act.size()]);
                act.add(act.size() - 1, createBrokenLinksAction());
                actionsBroken = act.toArray(new Action[act.size()]);
            }
            return broken ? actionsBroken : actions;
        }
        
        private Action createBrokenLinksAction() {
            final Action action = new AbstractAction() {
                public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
                    // here is required list of all platforms, not just the default one !!!!!!!!!!!
                    BrokenReferencesSupport.showCustomizer(helper, refHelper, getBreakableProperties(), getBreakablePlatformProperties());
                    checkBroken();
                }
            };
            action.putValue(Action.NAME, NbBundle.getMessage(J2MEPhysicalViewProvider.class, "LAB_ResolveReferenceProblems")); //NOI18N
            return action;
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
    
    private class RefreshPackagesAction extends AbstractAction {
        
        public RefreshPackagesAction() {
            super(NbBundle.getMessage(J2MEPhysicalViewProvider.class, "LAB_RefreshFolders")); //NOI18N
        }
        
        public void actionPerformed(@SuppressWarnings("unused")
		final ActionEvent e) {
            refreshRecursivelly(helper.resolveFileObject(helper.getStandardPropertyEvaluator().getProperty("src.dir")));//NOI18N
        }
        
        private void refreshRecursivelly(final FileObject fo) {
            if (fo == null) return ;
            fo.refresh();
            final Enumeration en = fo.getChildren(false);
            while (en.hasMoreElements()) {
                refreshRecursivelly((FileObject)en.nextElement());
            }
        }
    }
}
