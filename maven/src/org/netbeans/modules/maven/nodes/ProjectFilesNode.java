/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.nodes;

import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * maven project related aggregator node..
 * @author Milos Kleint
 */
public class ProjectFilesNode extends AnnotatedAbstractNode {
    
    private NbMavenProjectImpl project;
    /** Creates a new instance of ProjectFilesNode */
    public ProjectFilesNode(NbMavenProjectImpl project) {
        super(new ProjectFilesChildren(project), Lookups.fixed(project.getProjectDirectory(), new OthersRootNode.ChildDelegateFind()));
        setName("projectfiles"); //NOI18N
        setDisplayName(org.openide.util.NbBundle.getMessage(ProjectFilesNode.class, "LBL_Project_Files"));
        this.project = project;
        setMyFiles();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Collection<Action> col = new ArrayList<Action>();
        if (project.getProjectDirectory().getFileObject("profiles.xml") == null) { //NOI18N
            col.add(new AddProfileXmlAction());
        }
        if (! new File(MavenSettingsSingleton.getInstance().getM2UserDir(), "settings.xml").exists()) { //NOI18N
            col.add(new AddSettingsXmlAction());
        }
        return col.toArray(new Action[col.size()]);
    }
    
    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/projectfiles-badge.png", true); //NOI18N
        Image img = ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
        return img;
    }
    
    @Override
    protected Image getIconImpl(int param) {
        return getIcon(false);
    }

    @Override
    protected Image getOpenedIconImpl(int param) {
        return getIcon(true);
    }
    
    private void setMyFiles() {
        Set<FileObject> fobs = new HashSet<FileObject>();
        FileObject fo = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
        if (fo != null) {
            //#119134 for some unknown reason, the pom.xml might be missing from the project directory in some cases.
            // prevent passing null to the list that causes problems down the stream.
            fobs.add(fo);
        }
        FileObject fo2 = project.getProjectDirectory().getFileObject("profiles.xml"); //NOI18N
        if (fo2 != null) {
            fobs.add(fo2);
        }
        setFiles(fobs);
    }
    
    private static class ProjectFilesChildren extends Children.Keys<File> implements PropertyChangeListener {
        private NbMavenProjectImpl project;
        private FileChangeAdapter fileChangeListener;
        
        public ProjectFilesChildren(NbMavenProjectImpl proj) {
            super();
            project = proj;
            fileChangeListener = new FileChangeAdapter() {
                @Override
                public void fileDataCreated(FileEvent fe) {
                    regenerateKeys(true);
                }
                @Override
                public void fileDeleted(FileEvent fe) {
                    regenerateKeys(true);
                }
            };
        }
        
        protected Node[] createNodes(File fil) {
            FileObject fo = FileUtil.toFileObject(fil);
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    FilterNode node = new FilterNode(dobj.getNodeDelegate().cloneNode());
                    return new Node[] { node };
                } catch (DataObjectNotFoundException e) {
                    //NOPMD
                }
                
            }
            return new Node[0];
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                regenerateKeys(true);
            }
        }
        
//        public void refreshChildren() {
//            Node[] nods = getNodes();
//            for (int i = 0; i < nods.length; i++) {
//                if (nods[i] instanceof DependencyNode) {
//                    ((DependencyNode)nods[i]).refreshNode();
//                }
//            }
//        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            NbMavenProject.addPropertyChangeListener(project, this);
            project.getProjectDirectory().addFileChangeListener(fileChangeListener);
            regenerateKeys(false);
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<File>emptySet());
            NbMavenProject.removePropertyChangeListener(project, this);
            project.getProjectDirectory().removeFileChangeListener(fileChangeListener);
            super.removeNotify();
        }
        
        private void regenerateKeys(final boolean refresh) {
            //#149566 prevent setting keys under project mutex.
            if (ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess()) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        regenerateKeys(refresh);
                    }
                });
                return;
            }
            Collection<File> keys = new ArrayList<File>();
            keys.add(new File(FileUtil.toFile(project.getProjectDirectory()), "pom.xml")); //NOI18N
            keys.add(new File(FileUtil.toFile(project.getProjectDirectory()), "profiles.xml")); //NOI18N
            keys.add(new File(MavenSettingsSingleton.getInstance().getM2UserDir(), "settings.xml")); //NOI18N
            setKeys(keys);
            ((ProjectFilesNode)getNode()).setMyFiles();
            if (refresh) {
                for (File key : keys) {
                    refreshKey(key);
                }
            }
        }
    }

    private class AddProfileXmlAction extends AbstractAction {
        AddProfileXmlAction() {
            putValue(Action.NAME, org.openide.util.NbBundle.getMessage(ProjectFilesNode.class, "BTN_Create_profile_xml"));
        }
        public void actionPerformed(ActionEvent e) {
            try {
                DataFolder folder = DataFolder.findFolder(project.getProjectDirectory());
                // path to template...
                FileObject temp = FileUtil.getConfigFile("Maven2Templates/profiles.xml"); //NOI18N
                DataObject dobj = DataObject.find(temp);
                DataObject newOne = dobj.createFromTemplate(folder);
                EditCookie cook = newOne.getCookie(EditCookie.class);
                if (cook != null) {
                    cook.edit();
                }
                
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
    }
    
    private class AddSettingsXmlAction extends AbstractAction {
        AddSettingsXmlAction() {
            putValue(Action.NAME, NbBundle.getMessage(ProjectFilesNode.class, "BTN_Create_settings_xml"));
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                File fil = MavenSettingsSingleton.getInstance().getM2UserDir();
                
                DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(fil));
                // path to template...
                FileObject temp = FileUtil.getConfigFile("Maven2Templates/settings.xml"); //NOI18N
                DataObject dobj = DataObject.find(temp);
                DataObject newOne = dobj.createFromTemplate(folder);
                EditCookie cook = newOne.getCookie(EditCookie.class);
                if (cook != null) {
                    cook.edit();
                }
                
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
    }

}
