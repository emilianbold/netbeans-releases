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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.compapp.test.ui.wizards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.openide.actions.RenameAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node.Cookie;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Support for creating logical views.
 * @author Jesse Glick, Petr Hrebejk
 */
public class WsdlViewNodes {
    
    //private static Image badgedImage = loadImage("org/netbeans/modules/compapp/test/ui/resources/errorbadge.gif"); // NOI18N
    
    
    static final class GroupNode extends FilterNode implements PropertyChangeListener {
        
        
        final String GROUP_NAME_PATTERN = NbBundle.getMessage(
                WsdlViewNodes.class, "FMT_WsdlViewNodes_GroupName"); // NOI18N
        
        private Project project;
        private ProjectInformation pi;
        private SourceGroup group;
        
        public GroupNode(Project mainProject, Project srcGroupProject, SourceGroup group, SourceGroup[] allGroups, DataFolder dataFolder) {
            super(dataFolder.getNodeDelegate(),
                    new SourceGroupsChildren(dataFolder.getPrimaryFile(), group, allGroups, mainProject),
                    createLookup(srcGroupProject, group, dataFolder));
            
            this.project = srcGroupProject;
            this.pi = ProjectUtils.getInformation(project);
            this.group = group;
            pi.addPropertyChangeListener(WeakListeners.propertyChange(this, pi));
            group.addPropertyChangeListener(WeakListeners.propertyChange(this, group));
        }
        
        // XXX May need to change icons as well
        
        @Override
        public String getName() {
            return group.getName();
            
        }
        
        @Override
        public String getDisplayName() {
            return MessageFormat.format(GROUP_NAME_PATTERN,
                    new Object[] { group.getDisplayName(), pi.getDisplayName(), getOriginal().getDisplayName() });
            
        }
        
        @Override
        public String getShortDescription() {
            FileObject gdir = group.getRootFolder();
            String dir = FileUtil.getFileDisplayName(gdir);
            return NbBundle.getMessage(WsdlViewNodes.class,
                    "HINT_group", // NOI18N
                    dir);
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
        public boolean canCopy() {
            // At least for now.
            return false;
        }
        
        @Override
        public boolean canDestroy() {
            return false;
        }
        
        @Override
        public Action[] getActions(boolean context) {
            
            if (context) {
                return super.getActions(true);
            } else {
                Action[] folderActions = super.getActions(false);
                return folderActions;
            }
        }
        
        // Private methods -------------------------------------------------
        
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(prop)) {
                fireDisplayNameChange(null, null);
            } else if (ProjectInformation.PROP_NAME.equals(prop)) {
                fireNameChange(null, null);
            } else if (ProjectInformation.PROP_ICON.equals(prop)) {
                // OK, ignore
            } else if ("name".equals(prop)) { // NOI18N
                fireNameChange(null, null);
            } else if ("displayName".equals(prop)) { // NOI18N
                fireDisplayNameChange(null, null);
            } else if ("icon".equals(prop)) { // NOI18N
                // OK, ignore
            } else if ("rootFolder".equals(prop)) { // NOI18N
                // XXX Do something to children and lookup
                fireNameChange(null, null);
                fireDisplayNameChange(null, null);
                fireShortDescriptionChange(null, null);
            } else {
                assert false : "Attempt to fire an unsupported property change event from " + pi.getClass().getName() + ": " + prop; // NOI18N
            }
        }
        
        private static Lookup createLookup(Project p, SourceGroup group, DataFolder dataFolder) {
            return new ProxyLookup(new Lookup[] {
                dataFolder.getNodeDelegate().getLookup(),
                p.getLookup(),
            });
        }
    }
    
    public static final class SourceGroups extends Children.Keys {
        private Project ownerProject;
        
        private SourceGroup[] allGroups;
        
        public SourceGroups(Project owner,
                SourceGroup[] ownerProjectGroups,
                SourceGroup[] depedentProjectGroups) {
            ownerProject = owner;
            allGroups = new SourceGroup[ownerProjectGroups.length + depedentProjectGroups.length];
            System.arraycopy(ownerProjectGroups, 0, allGroups, 0, ownerProjectGroups.length);
            System.arraycopy(depedentProjectGroups, 0, allGroups, ownerProjectGroups.length, depedentProjectGroups.length);
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(getKeys());
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
                
        protected Node[] createNodes(Object key) {
            FileObject folder = null;
            SourceGroup group = null;
            
            if (key instanceof SourceGroup) {
                group = (SourceGroup)key;
                folder = group.getRootFolder();
                Project project = FileOwnerQuery.getOwner(folder);
                DataFolder dFolder = DataFolder.findFolder(folder);
                
                GroupNode gNode = new GroupNode(ownerProject,
                        project,
                        group,
                        this.allGroups,
                        DataFolder.findFolder(folder));
                return new Node[] { gNode };
            } else {
                return new Node[0];
            }
        }
        
        private Collection getKeys() {
            return Arrays.asList(allGroups);
        }
    }
    
    public static final class SourceGroupsChildren extends Children.Keys {
        
        private Project ownerProject;
        
        private SourceGroup[] allGroups;
        
        private SourceGroup ownerGroup;
        
        //root folder of source group or any other sub folder
        private FileObject fo;
        
        public SourceGroupsChildren(FileObject fo , SourceGroup owner, SourceGroup[] groups, Project project) {
            this.fo = fo;
            this.ownerGroup = owner;
            this.allGroups = groups;
            this.ownerProject = project;
        }
       
        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(getKeys());
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
                
        protected Node[] createNodes(Object key) {
            
            FileObject folder = null;
            
            if (key instanceof Key) {
                folder = ((Key)key).folder;
                Node delegate = null;
                //if folder which is a sub folder in a source group.
                if(folder.isFolder()) {
                    delegate = DataFolder.findFolder(folder).getNodeDelegate();
                    if(delegate != null) {
                        FilterNode fn = new FilterNode(
                                delegate,
                                new SourceGroupsChildren(folder, this.ownerGroup, this.allGroups, this.ownerProject));
                        return new Node[] { fn };
                    }
                } else { //file
                    try {
                        delegate = DataFolder.find(folder).getNodeDelegate();
                        
                        if(delegate != null) {
                            FilterNode fn = new DepedentProjectFileNode(delegate, folder, this.ownerGroup, allGroups);
                            return new Node[] { fn };
                        }                        
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }                
            }
            
            return new Node[0];            
        }
        
        private Collection getKeys() {
            FileObject files[] = fo.getChildren();
            ArrayList children = new ArrayList(files.length);
            
            for(int i = 0; i < files.length; i++) {
                //we have a file we want to allow olny wsdls and xsds
                if (!files[i].isFolder()) {
                    if(VisibilityQuery.getDefault().isVisible(files[i])
                    && isAcceptableFile(files[i])) {
                        children.add(new Key(files[i]));
                    }
                } else {
                    //we have a folder
                    if (isAcceptableFolder(files[i])) {
                        children.add(new Key(files[i]));
                    }
                }
            }
            
            //sort files
            Collections.sort(children, new KeyComparator());
            
            return children;
        }
        
        private class Key {
            
            private FileObject folder;
            
            private Key(FileObject folder) {
                this.folder = folder;
            }
            
            FileObject getFileObject() {
                return folder;
            }
            
        }
        
        private  class KeyComparator implements Comparator {
            
            public int compare(Object arg0, Object arg1) {
                FileObject file0 = ((Key) arg0).getFileObject();
                FileObject file1 = ((Key) arg1).getFileObject();
                return file0.getNameExt().compareToIgnoreCase(file1.getNameExt());
                
            }
        }
    }
    
    private static boolean isAcceptableFolder(FileObject fo) {
        if (!fo.isFolder()) {
            return false;
        }
        boolean recursive = false;
        Enumeration e = fo.getData(recursive);
        while (e.hasMoreElements()) {
            FileObject f = (FileObject)e.nextElement();
            if (isAcceptableFile(f)) {
                return true;
            }
        }
        e = fo.getFolders(recursive);
        while (e.hasMoreElements()) {
            FileObject f = (FileObject)e.nextElement();
            if (isAcceptableFolder(f)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isAcceptableFile(FileObject fo) {
        if(fo.getExt().equalsIgnoreCase("wsdl")) { // NOI18N
            InputStream inputStream = null;
            try {
                // Make sure the wsdl contains service definitions.
                // This masks out empty portmap.wsdl.
                inputStream = fo.getInputStream();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(inputStream);
                
                XPath xpath = XPathFactory.newInstance().newXPath();
                String expression = "/definitions/service/port"; // NOI18N
                
                NodeList nodes = (NodeList) xpath.evaluate(expression, document,
                        XPathConstants.NODESET);
                
                if (nodes.getLength() > 0) {
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                }
            }
        }
        
        return false;
    }
    
    
    static final class DepedentProjectFileNode extends FilterNode {
        
        private FileObject fileObject;
        private SourceGroup ownerGroup;
        private SourceGroup[] allGroups;
        private CookieSet set;
        
        public DepedentProjectFileNode(Node original, FileObject fileObj, SourceGroup owner,SourceGroup[] groups) {
            super(original, Children.LEAF);
            this.fileObject = fileObj;
            this.ownerGroup = owner;
            this.allGroups = groups;
            this.set = new CookieSet();
            this.set.add(new FileObjectCookie(fileObject));
        }
        
        @Override
        public Cookie getCookie(Class type) {
            // #122711 Do not allow the WSDL file to be opened.
            if (DataObject.class.isAssignableFrom(type) 
                    || OpenCookie.class.isAssignableFrom(type) 
                    /*|| EditCookie.class.isAssignableFrom(type) 
                    || EditorCookie.class.isAssignableFrom(type)*/ ) {
                return null;
            }            
            
            Cookie c = this.set.getCookie(type);
            if (c != null) {
                return c;
            }
            
            return super.getCookie(type);
        }
        
        @Override
        public boolean canRename() {
            return false;
        }
    }
    
    /**
     * Marker cookie to indicate that node represents a duplicate file.
     */
    
    public static final class FileObjectCookie implements Node.Cookie {
        private FileObject fileObject;
        
        public FileObjectCookie(FileObject file) {
            this.fileObject = file;
        }
        
        public FileObject getFileObject() {
            return fileObject;
        }
    }
    
    /** Loads the <code>Image</code> object from the relative file path.
     * @param   path    Image relative file path.
     * @return  Corresponding <code>Image</code> object.
     */
//    private static Image loadImage(String path) {
//        return (new ImageIcon(WsdlViewNodes.class.getClassLoader().getResource(path))).getImage();
//    }
}
