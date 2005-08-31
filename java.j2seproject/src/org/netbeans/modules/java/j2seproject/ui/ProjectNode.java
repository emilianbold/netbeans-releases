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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
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

    private final AntArtifact antArtifact;
    private final URI artifactLocation;
    private Image cachedIcon;

    ProjectNode (AntArtifact antArtifact, URI artifactLocation, UpdateHelper helper,ReferenceHelper refHelper, String classPathId, String entryId) {
        super (Children.LEAF, createLookup (antArtifact, artifactLocation, helper, refHelper, classPathId, entryId));
        this.antArtifact = antArtifact;
        this.artifactLocation = artifactLocation;
    }

    public String getDisplayName () {        
        ProjectInformation info = getProjectInformation();        
        if (info != null) {
            return MessageFormat.format(NbBundle.getMessage(ProjectNode.class,"TXT_ProjectArtifactFormat"),
                    new Object[] {info.getDisplayName(), artifactLocation.toString()});
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
            ProjectInformation info = getProjectInformation();
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
    
    private ProjectInformation getProjectInformation () {
        Project p = this.antArtifact.getProject();
        if (p != null) {
            return ProjectUtils.getInformation(p);
        }
        return null;
    }
    
    private static Lookup createLookup (AntArtifact antArtifact, URI artifactLocation, 
            UpdateHelper helper, ReferenceHelper refHelper, String classPathId, String entryId) {
        Project p = antArtifact.getProject();
        Object[] content;
        if (p == null) {
            content = new Object[1];
        }
        else {
            content = new Object[3];
            content[1] = new JavadocProvider(antArtifact, artifactLocation);
            content[2] = p;
        }
        content[0] = new Removable(helper,refHelper, classPathId, entryId);        
        Lookup lkp = Lookups.fixed(content);
        return lkp;
    }

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {

        private final AntArtifact antArtifact;
        private final URI artifactLocation;

        JavadocProvider (AntArtifact antArtifact, URI artifactLocation) {
            this.antArtifact = antArtifact;
            this.artifactLocation = artifactLocation;
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
            ProjectInformation info = null;
            Project p = this.antArtifact.getProject ();
            if (p != null) {
                info = ProjectUtils.getInformation(p);
            }
            ShowJavadocAction.showJavaDoc (pageURL, info == null ?
                NbBundle.getMessage (ProjectNode.class,"TXT_UnknownProjectName") : info.getDisplayName());
        }
        
        private Set findJavadoc() {            
            File scriptLocation = this.antArtifact.getScriptLocation();            
            Set urls = new HashSet();
            try {
                URL artifactURL = scriptLocation.toURI().resolve(this.artifactLocation).normalize().toURL();
                if (FileUtil.isArchiveFile(artifactURL)) {
                    artifactURL = FileUtil.getArchiveRoot(artifactURL);
                }
                urls.addAll(Arrays.asList(JavadocForBinaryQuery.findJavadoc(artifactURL).getRoots()));                
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify (mue);                
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
