/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui;

import java.awt.Component;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.j2seproject.UpdateHelper;



/**
 * ProjectNode represents a dependent project under the Libraries Node.
 * It is a leaf node with the following actions: {@link OpenProjectAction},
 * {@link ShowJavadocAction} and {@link RemoveClassPathRootAction}
 * @author Tomas Zezula
 */
class ProjectNode extends AbstractNode {

    private static final String PROJECT_ICON = "org/netbeans/modules/java/j2seproject/ui/resources/projectDependencies.gif";    //NOI18N
    private static final Component CONVERTOR_COMPONENT = new Panel();

    private final Project project;
    private Image cachedIcon;

    ProjectNode (Project project, UpdateHelper helper,ReferenceHelper refHelper, String classPathId, String entryId) {
        super (Children.LEAF, Lookups.fixed(new Object[] {
            project,
            new JavadocProvider(project),
            new Removable(helper,refHelper, classPathId, entryId)}));
        this.project = project;
    }

    public String getDisplayName () {
        ProjectInformation info = (ProjectInformation) this.project.getLookup().lookup(ProjectInformation.class);
        if (info != null) {
            return info.getDisplayName();
        }
        else {
            return NbBundle.getMessage (ProjectNode.class,"TXT_UnknownProjectName");
        }
    }

    public String getName () {
        return this.getDisplayName();
    }

    public Image getIcon(int type) {
        if (cachedIcon == null) {
            ProjectInformation info = (ProjectInformation) this.project.getLookup().lookup(ProjectInformation.class);
            if (info != null) {
                Icon icon = info.getIcon();
                //XXX: There should be an API for Icon -> Image conversion,
                //issue: http://www.netbeans.org/issues/show_bug.cgi?id=52562
                if (icon instanceof ImageIcon) {
                    cachedIcon = ((ImageIcon)icon).getImage();
                }
                else {
                    int height = icon.getIconHeight();
                    int width = icon.getIconWidth();
                    cachedIcon = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
                    icon.paintIcon( CONVERTOR_COMPONENT, cachedIcon.getGraphics(), 0, 0 );
                }
            }
            else {
                cachedIcon = Utilities.loadImage(PROJECT_ICON);
            }
        }
        return cachedIcon;
    }

    public Image getOpenedIcon(int type) {
        return this.getIcon(type);
    }

    public boolean canCopy() {
        return false;
    }

    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get (OpenProjectAction.class),
            SystemAction.get (ShowJavadocAction.class),
            SystemAction.get (RemoveClassPathRootAction.class),
        };
    }

    public Action getPreferredAction () {
        return getActions(false)[0];
    }

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {

        private final Project project;

        JavadocProvider (Project project) {
            this.project = project;
        }


        public boolean hasJavadoc() {
            return findJavadoc().size() > 0;
        }

        public void showJavadoc() {
            Set us = findJavadoc();
            URL[] urls = (URL[])us.toArray(new URL[us.size()]);
            URL pageURL = ShowJavadocAction.findJavadoc("overview-summary.html",urls);
            if (pageURL == null) {
                pageURL = ShowJavadocAction.findJavadoc("index.html",urls);
            }
            ProjectInformation info = (ProjectInformation) project.getLookup().lookup(ProjectInformation.class);
            ShowJavadocAction.showJavaDoc (pageURL, info == null ?
                NbBundle.getMessage (ProjectNode.class,"TXT_UnknownProjectName") : info.getDisplayName());
        }
        
        private Set findJavadoc() {
            AntArtifactProvider provider = (AntArtifactProvider) project.getLookup().lookup(AntArtifactProvider.class);
            if (provider == null) {
                return new HashSet();
            }
            AntArtifact[] artifacts = provider.getBuildArtifacts();
            if (artifacts.length==0) {
                return new HashSet();
            }
            File scriptLocation = artifacts[0].getScriptLocation();
            URI artifactLocations[] = artifacts[0].getArtifactLocations();
            Set urls = new HashSet();
            URL artifactURL;
            for (int i=0; i<artifactLocations.length; i++) {
                try {
                    artifactURL = scriptLocation.toURI().resolve(artifactLocations[i]).normalize().toURL();
                } catch (MalformedURLException mue) {
                    continue;
                }
                if (FileUtil.isArchiveFile(artifactURL)) {
                    artifactURL = FileUtil.getArchiveRoot(artifactURL);
                }
                urls.addAll(Arrays.asList(JavadocForBinaryQuery.findJavadoc(artifactURL).getRoots()));
            }
            return urls;
        }

    }

    private static class OpenProjectAction extends NodeAction {

        protected void performAction(Node[] activatedNodes) {
            Project[] projects = new Project[activatedNodes.length];
            for (int i=0; i<projects.length;i++) {
                projects[i] = (Project) activatedNodes[i].getLookup().lookup(Project.class);
            }
            OpenProjects.getDefault().open(projects, false);
        }

        protected boolean enable(Node[] activatedNodes) {
            for (int i=0; i<activatedNodes.length; i++) {
                if (activatedNodes[i].getLookup().lookup(Project.class) == null) {
                    return false;
                }
            }
            return true;
        }

        public String getName() {
            return NbBundle.getMessage (ProjectNode.class,"CTL_OpenProject");
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx (OpenProjectAction.class);
        }

        protected boolean asynchronous() {
            return false;
        }
    }

    private static class Removable implements RemoveClassPathRootAction.Removable {

        private final UpdateHelper helper;
        private final ReferenceHelper refHelper;
        private final String classPathId;
        private final String entryId;

        Removable (UpdateHelper helper, ReferenceHelper refHelper, String classPathId, String entryId) {
            this.helper = helper;
            this.refHelper = refHelper;
            this.classPathId = classPathId;
            this.entryId = entryId;
        }

        public boolean canRemove () {
            //Allow to remove only entries from PROJECT_PROPERTIES, same behaviour as the project customizer
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return props.getProperty (classPathId) != null;
        }

        public void remove() {
            ProjectManager.mutex().writeAccess ( new Runnable () {
               public void run() {
                   EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                   String cp = props.getProperty (classPathId);
                   if (cp != null) {
                       String[] entries = PropertyUtils.tokenizePath(cp);
                       List/*<String>*/ result = new ArrayList ();
                       for (int i=0; i<entries.length; i++) {
                           if (!entryId.equals(J2SEProjectProperties.getAntPropertyName(entries[i]))) {
                               int size = result.size();
                               if (size>0) {
                                   result.set (size-1,(String)result.get(size-1) + ':'); //NOI18N
                               }
                               result.add (entries[i]);                                                              
                           }
                       }
                       props.setProperty (classPathId, (String[])result.toArray(new String[result.size()]));
                       helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
                       String ref = "${"+entryId+"}";  //NOI18N
                       if (!RemoveClassPathRootAction.isReferenced (new EditableProperties[] {
                           props,
                           helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH)}, ref)) {
                           refHelper.destroyReference(ref);
                       }
                       Project project = FileOwnerQuery.getOwner(helper.getAntProjectHelper().getProjectDirectory());
                       assert project != null;
                       try {
                        ProjectManager.getDefault().saveProject(project);
                       } catch (IOException ioe) {
                           ErrorManager.getDefault().notify(ioe);
                       }
                   }
               }
           });
        }
    }
}
