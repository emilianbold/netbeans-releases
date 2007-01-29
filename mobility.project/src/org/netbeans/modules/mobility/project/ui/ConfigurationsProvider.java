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
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
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
    
    //HashMap for listeners of resources per project and per configuration
    static final WeakHashMap<FileObject,WeakHashMap<J2MEProject,WeakHashMap<String,FileChangeListener>>> lstCache = new WeakHashMap<FileObject,WeakHashMap<J2MEProject,WeakHashMap<String,FileChangeListener>>>();
    
    static private List<Node> createPackage(final J2MEProject project,final ProjectConfiguration conf,final ClassPath path, final HashMap<FileObject,VisualClassPathItem> map, final boolean actions)
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
                        node=PackageView.createPackageView(GenericSources.group(project,roots[i],file.getNameExt(),file.getNameExt(),icon, openedIcon));
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
                    node.setValue("VCPI",item);
                    if (item != null)
                    {   
                        final File f=FileUtil.toFile(file);
                        final Lookup lookup=Lookups.fixed(new Object[] {project, conf, f} );
                        
                        node=new FilterNode(node,null,lookup) 
                        {
                            final Action act[]=new Action[] 
                            {
                                RemoveResourceAction.getStaticInstance(),
                                null,
                                SystemAction.get(CopyAction.class),
                            };
                            
                            final Action gract[]=new Action[] 
                            {
                                SystemAction.get(CopyAction.class),
                            };
                            
                            public Action[] getActions(boolean context)
                            {
                                return actions?act:gract;
                            }
                            
                            public Image getIcon(int i) {
                                return ((ImageIcon)item.getIcon()).getImage();
                            }
                            
                            public Image getOpenedIcon(int i) {
                                return ((ImageIcon)item.getIcon()).getImage();
                            }
                            
                            public boolean canDestroy()
                            {
                                return false;
                            }
                            
                            public boolean canRename()
                            {
                                return false;
                            }
                            
                            public boolean canCut()
                            {
                                return false;
                            }
                            
                            public boolean canCopy()
                            {
                                return true;
                            }
                        };
                        node.setDisplayName(item.getDisplayName());
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
                String itemPath=helper.getStandardPropertyEvaluator().evaluate(raw);
                final File f=FileUtil.normalizeFile(new File(itemPath));
                final FileObject fo=FileUtil.toFileObject(f);  
                FileObject fRoot=fo;                      
                assert f != null;
                if (fo==null)
                {
                    Object o=item.getIcon();
                    final Lookup lookup = Lookups.fixed( new Object[] {project,conf, item, f} );
                    final Action actions[]=gray ? new Action[] {} : new Action[] { 
                                                                                   RemoveResourceAction.getStaticInstance(),
                                                                                 };
                    final Node n=new FilterNode(new ActionNode(Children.LEAF,lookup,itemPath,item.getDisplayName(),null,actions),null,lookup)
                    {
                            public Image getIcon(int i) {
                                return ((ImageIcon)item.getIcon()).getImage();
                            }   
                    };
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
                 * each resource has a listner on an associated file onject and reacts on actions with this file
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
            }
            path=ClassPathSupport.createClassPath(list.toArray(new FileObject[list.size()]));        
            brokenArray.addAll(createPackage(project,conf,path,map,!gray));   
        }        
        return brokenArray;
    }
}
