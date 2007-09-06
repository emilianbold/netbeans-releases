/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigurationsProvider.java
 *
 * Created on 02 May 2006, 18:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.filesystems.FileChangeListener;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Lukas Waldmann
 */
class ConfigurationsProvider
{
    static final String ARCHIVE_ICON = "org/netbeans/modules/mobility/project/ui/resources/libraries.gif";
    static final Action[] emptyAction = new Action[] {};
    //HashMap for listeners of resources per project and per configuration
    static final WeakHashMap<FileObject,WeakHashMap<J2MEProject,WeakHashMap<String,FileChangeListener>>> lstCache = new WeakHashMap<FileObject,WeakHashMap<J2MEProject,WeakHashMap<String,FileChangeListener>>>();
    
    static private class FNode extends FilterNode
    {
        final private Action[] actions;
        final private Image icon;
        final boolean origProp;
        
        FNode(Node original, Lookup lookup,Action[] acts,VisualClassPathItem it) {
            super(original,new ActionFilterChildren(original),lookup);
            actions=acts==null?act:acts;
            icon=((ImageIcon)it.getIcon()).getImage();
            origProp=false;
        }
        
        FNode(Node original, Image it) {
            super(original,new ActionFilterChildren(original),null);
            origProp=true;
            actions=original.getActions(false);
            icon=it;
        }
        
        final Action act[]=new Action[] 
        {
            NodeActions.RemoveResourceAction.getStaticInstance(),
            SystemAction.get(CopyAction.class),
        };


        public Action[] getActions(boolean context)
        {
            return actions==null?act:actions;
        }

        public Image getIcon(int i) {
            return icon;
        }

        public Image getOpenedIcon(int i) {
            return icon;
        }

        public boolean canDestroy()
        {
            return origProp==true?super.canDestroy():false;
        }

        public boolean canRename()
        {
            return origProp==true?super.canRename():false;
        }

        public boolean canCut()
        {
            return origProp==true?super.canCut():false;
        }

        public boolean canCopy()
        {
            return origProp==true?super.canCopy():true;
        }
        
        private static class ActionFilterChildren extends FilterNode.Children {

            ActionFilterChildren (Node original) {
                super (original);
            }

            protected Node[] createNodes(Node n) {
                return new Node[] {new FNode(n, n.getIcon(1))};                       
            }
    }

    };
    
    static private List<Node> createPackage(final J2MEProject project,final ProjectConfiguration conf,final ClassPath path, 
            final HashMap<FileObject,VisualClassPathItem> map, final boolean actions, final boolean multi)
    {
        final FileObject[] roots = path == null ? new FileObject[] {} : path.getRoots();
        final List<Node> list=new ArrayList<Node>();
        
        
        for (int i=0; i<roots.length; i++)
        {
            if (roots[i].isValid())
            {
                try
                {
                    FileObject file=null;
                    Icon icon;
                    Icon openedIcon;
                    Node node=null;                    
                    //Add a jar file
                    if ("jar".equals(roots[i].getURL().getProtocol()))
                    { //NOI18N
                        file = FileUtil.getArchiveFile(roots[i]);
                        icon = openedIcon = new ImageIcon(Utilities.loadImage(ARCHIVE_ICON));
                        node=PackageView.createPackageView(new LibrariesSourceGroup(roots[i],file.getNameExt(),icon, openedIcon));
                    }
                    //Add a file or folder
                    else
                    {
                        file = roots[i];
                        if (file.isFolder())
                        {
                            node=DataFolder.findFolder(file).getNodeDelegate();
                        }
                        else
                        {
                            try
                            {
                                node=DataObject.find(file).getNodeDelegate();
                            } catch (DataObjectNotFoundException ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                    }    
                    
                    final VisualClassPathItem item=map.get(file);
                    
                    if (item != null)
                    {   
                        node.setValue("VCPI",item);
                        
                        final File f=FileUtil.toFile(file);
                        final Lookup lookup=Lookups.fixed(new Object[] {project, conf, item} );
                        
                        if (!multi)
                        {
                            node=new FNode(node,lookup,actions?null:emptyAction,item);
                            node.setDisplayName(item.getDisplayName());
                        }
                        else
                            node=new FNode(node,lookup,emptyAction,item);
                            
                        node.setValue("grey",!actions);
                        node.setValue("resource","Resource");
                        list.add(node);
                        
                    }
                }
                catch (FileStateInvalidException e)
                {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return list;
    }
    
    static Collection<Node> createResourcesNodes(final J2MEProject project, final ProjectConfiguration conf)
    {
        final AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);  
        final J2MEPhysicalViewProvider view = project.getLookup().lookup(J2MEPhysicalViewProvider.class);
        final ArrayList<Node> brokenArray=new ArrayList<Node>();
        final Node nodes[]=new Node[0];
        ArrayList<VisualClassPathItem> libs=null;
        HashMap<FileObject,VisualClassPathItem> map=new HashMap<FileObject,VisualClassPathItem>();
        ClassPath path=null;
        final ArrayList<FileObject> list=new ArrayList<FileObject>();
        boolean gray=false;
        StringBuffer libspath=new StringBuffer();
        
        final J2MEProjectProperties j2meProperties = new J2MEProjectProperties( project, 
                project.getLookup().lookup(AntProjectHelper.class),
                project.getLookup().lookup(ReferenceHelper.class), 
                project.getConfigurationHelper() );
        
        if (conf.getDisplayName().equals(project.getConfigurationHelper().getDefaultConfiguration().getDisplayName()))
        {
            libs=(ArrayList<VisualClassPathItem>)j2meProperties.get(DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        }
        else
        {
            libs=(ArrayList<VisualClassPathItem>)j2meProperties.get(J2MEProjectProperties.CONFIG_PREFIX+conf.getDisplayName()+"."+DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        }
        
        if (libs==null)
        /* Using resources of default configuration */
        {
            libs=(ArrayList<VisualClassPathItem>)j2meProperties.get(DefaultPropertiesDescriptor.LIBS_CLASSPATH);
            gray=true;
        }
        
        
        if (libs!=null)
        {
            for (final VisualClassPathItem item : libs)
            {
                String raw=item.getRawText();
                String xPath=helper.getStandardPropertyEvaluator().evaluate(raw);               
                StringTokenizer tokens=new StringTokenizer(xPath,File.pathSeparator);
                Node libNode=null;
                final boolean multi=tokens.countTokens()>1;
                final boolean empty=tokens.countTokens()!=0;
                FileObject fo=null;
                
                do
                {
                    String iPath=empty ? tokens.nextToken() : xPath;
                    final File f=FileUtil.normalizeFile(new File(iPath));
                    fo=iPath.equals("")?null:FileUtil.toFileObject(f);  
                    FileObject fRoot=fo;                      
                    assert f != null;
                    if (fo==null)
                    {
                        final Lookup lookup = Lookups.fixed( new Object[] {project,conf, item} );
                        final Action actions[]=gray ? new Action[] {} : new Action[] { 
                                                                                       NodeActions.RemoveResourceAction.getStaticInstance(),
                                                                                     };
                        final Node n=new FNode(new NodeFactory.ActionNode(Children.LEAF,lookup,iPath,item.getDisplayName(),null,actions),lookup,gray?emptyAction:null,item);
                        n.setValue("error",Boolean.TRUE);
                        brokenArray.add(n);
                        File parent=f.getParentFile();
                        while (parent != null && !(parent.isFile() || parent.isDirectory()))
                            parent=parent.getParentFile();
                        fRoot=FileUtil.toFileObject(parent);
                    }
                    else 
                    {
                        if (!FileUtil.isArchiveFile(fo))
                            list.add(fo);
                        else
                            list.add(FileUtil.getArchiveRoot(fo));         
                        map.put(fo,item);
                    }

                    final FileObject root=fRoot;
                    /*
                     * The following code sequence takes care of refreshing resources and missing resources 
                     * each resource has a listner on an associated file object and reacts on actions with this file
                     * */

                    //map of projects which depends on particular file
                    WeakHashMap<J2MEProject,WeakHashMap<String,FileChangeListener>> pcls=lstCache.get(root);
                    if (pcls == null)
                    {
                        pcls = new WeakHashMap<J2MEProject,WeakHashMap<String,FileChangeListener>>();
                        lstCache.put(root, pcls);
                    }
                    //map of configurations in a project which depeends on particular file
                    WeakHashMap<String,FileChangeListener> fcls=pcls.get(project);
                    if (fcls == null)
                    {
                        fcls = new WeakHashMap<String,FileChangeListener>();
                        pcls.put(project,fcls);
                    }
                    //Is there a listener for particular configuration, project and file?
                    if (fcls.get(conf.getDisplayName()) == null)
                    {
                        FileChangeListener lst=new FileChangeListener() {
                            public void fileFolderCreated(FileEvent fe) {
                                if (FileUtil.toFile(fe.getFile()).getAbsolutePath().equals(f.getAbsolutePath()))
                                {  
                                    //The file we are looking for was detected by parent directory, now we can delete
                                    //the listner for this directory
                                    if (root.isFolder())
                                    {
                                        lstCache.remove(root);
                                        root.removeFileChangeListener(this);
                                    }
                                    view.refreshNode(conf.getDisplayName());
                                }
                            }

                            public void fileDataCreated(FileEvent fe) {
                                if (FileUtil.toFile(fe.getFile()).getAbsolutePath().equals(f.getAbsolutePath()))
                                {
                                    //The file we are looking for was detected by parent directory, now we can delete
                                    //the listner for this directory
                                    if (root.isFolder())
                                    {
                                        lstCache.remove(root);
                                        root.removeFileChangeListener(this);
                                    }
                                    view.refreshNode(conf.getDisplayName());
                                }
                            }

                            public void fileChanged(FileEvent fe) {
                            }

                            public void fileDeleted(FileEvent fe) {
                                if (FileUtil.toFile(fe.getFile()).getAbsolutePath().equals(f.getAbsolutePath()))
                                {
                                    view.refreshNode(conf.getDisplayName());
                                }
                            }

                            public void fileRenamed(FileRenameEvent fe) {
                            }

                            public void fileAttributeChanged(FileAttributeEvent fe) {
                            }
                        };
                        root.addFileChangeListener(lst);
                        fcls.put(conf.getDisplayName(),lst);
                    }
                } while (tokens.hasMoreTokens());
                
                path=ClassPathSupport.createClassPath(list.toArray(new FileObject[list.size()]));        
                if (fo != null)
                    if (multi==false)
                        brokenArray.addAll(createPackage(project,conf,path,map,!gray,multi)); 
                    else
                    {
                        final Lookup lookup = Lookups.fixed( new Object[] {project,conf, item} );
                        Children ch=new Children.Array();
                        ch.add(createPackage(project,conf,path,map,!gray,multi).toArray(new Node[0]));
                        libNode=new FNode(new NodeFactory.ActionNode(ch,lookup,xPath,item.getDisplayName(),null,null),lookup,gray?emptyAction:null,item);
                        brokenArray.add(libNode);

                    }
                list.clear();
            }  
        }        
        return brokenArray;
    }
    
    static class LibrariesSourceGroup implements SourceGroup {

        private final FileObject root;
        private final String displayName;
        private final Icon icon;
        private final Icon openIcon;

        /**
         * Creates new LibrariesSourceGroup
         * @param root the classpath root
         * @param displayName the display name presented to user
         * @param icon closed icon
         * @param openIcon opened icon
         */          
        LibrariesSourceGroup (FileObject root, String displayName, Icon icon, Icon openIcon) {
            assert root != null;
            this.root = root;
            this.displayName = displayName;
            this.icon = icon;
            this.openIcon = openIcon;
        }


        public FileObject getRootFolder() {
            return this.root;
        }

        public String getName() {
            try {        
                return root.getURL().toExternalForm();
            } catch (FileStateInvalidException fsi) { 
                ErrorManager.getDefault().notify (fsi);
                return root.toString();
            }
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public Icon getIcon(boolean opened) {
            return opened ? openIcon : icon;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            return root.equals(file) || FileUtil.isParentOf(root,file);
        }

        public boolean equals (Object other) {
            if (!(other instanceof LibrariesSourceGroup)) {
                return false;
            }
            LibrariesSourceGroup osg = (LibrariesSourceGroup) other;
            return displayName == null ? osg.displayName == null : displayName.equals (osg.displayName) &&
                root == null ? osg.root == null : root.equals (osg.root);  
        }

        public int hashCode () {
            return ((displayName == null ? 0 : displayName.hashCode())<<16) | ((root==null ? 0 : root.hashCode()) & 0xffff);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            //Not needed
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            //Not needed
        }
    }

}
