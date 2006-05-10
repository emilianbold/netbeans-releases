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

package org.netbeans.bluej;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.bluej.nodes.BluejLogicalViewRootNode;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Support for creating logical views.
 * @author Milos Kleint
 */
public class BluejLogicalViewProvider implements LogicalViewProvider, org.netbeans.bluej.api.BluejLogicalViewProvider {
    
//    private static final RequestProcessor BROKEN_LINKS_RP = new RequestProcessor("BluejPhysicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    
    private final BluejProject project;
    private List changeListeners;

    // Web service client
//    private static final Object KEY_SERVICE_REFS = "serviceRefs"; // NOI18N
    
    public BluejLogicalViewProvider(BluejProject project) {
        this.project = project;
        assert project != null;
    }
    
    public Node createLogicalView() {
        return new BluejLogicalViewRootNode(createLookup(project));
    }
    
    public Node findPath(Node root, Object target) {
        Project project = (Project) root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
        }
        
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }
            
            Node[] nodes = root.getChildren().getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                Node result = PackageView.findPath(nodes[i], target);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    
    
    public synchronized void addChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            this.changeListeners = new ArrayList();
        }
        this.changeListeners.add(l);
    }
    
    public synchronized void removeChangeListener(ChangeListener l) {
        if (this.changeListeners == null) {
            return;
        }
        this.changeListeners.remove(l);
    }
    
    /**
     * Used by J2SEProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advices him to use BrokenLinksAction to correct it.
     *
     */
    public void testBroken() {
        ChangeListener[] _listeners;
        synchronized (this) {
            if (this.changeListeners == null) {
                return;
            }
            _listeners = (ChangeListener[]) this.changeListeners.toArray(
                    new ChangeListener[this.changeListeners.size()]);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (int i=0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(event);
        }
    }
    
    private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed(new Object[] {project, rootFolder});
    }

    public Node getBigIconRootNode() {
        return new BigIconFilterNode(createLogicalView());
    }
    
    private static class BigIconFilterNode extends FilterNode {
        private String iconPath = null;
        private FileBuiltQuery.Status status = null;
        private boolean attached = false;
    
        BigIconFilterNode(Node original) {
            this(original, new BigIconFilterChilden(original));
            
        }
        BigIconFilterNode(Node original, org.openide.nodes.Children children) {
            super(original, children);
            DataObject dobj = (DataObject)original.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                if ("java".equalsIgnoreCase(dobj.getPrimaryFile().getExt())) {
                    String name = dobj.getPrimaryFile().getName();
                    if (name.endsWith("Test")) {
                        name = name.substring(0, name.length() - "Test".length());
                        if (dobj.getPrimaryFile().getParent().getFileObject(name, "java") != null) {
                            iconPath = "org/netbeans/bluej/resources/bluej-testclass.png";
                            attached = true;
                        } else {
                            iconPath = "org/netbeans/bluej/resources/bluej-testclass-unattached.png";
                        }
                    } else {
                        iconPath = "org/netbeans/bluej/resources/bluej-class.png";
                    }
                    status = FileBuiltQuery.getStatus(dobj.getPrimaryFile());
                }
                if ("readme.txt".equalsIgnoreCase(dobj.getPrimaryFile().getNameExt())) {
                    iconPath = "org/netbeans/bluej/resources/readme.png";
                }
            }
            
        }

        public Image getIcon(int type) {
            Image retValue;
            if (iconPath != null) {
                retValue = Utilities.loadImage(iconPath);
                if (status != null && !status.isBuilt()) {
                    retValue = Utilities.mergeImages(retValue, Utilities.loadImage("org/netbeans/bluej/resources/compiled.png"),
                                                     attached ? 8 : 3, attached ? 11 : 13);
                }
                if (hasMain()) {
                    retValue = Utilities.mergeImages(retValue, Utilities.loadImage("org/netbeans/bluej/resources/executable-badge.gif"),
                                                     attached ? 44 : 41, attached ? 5 : 7);
                }
            } else {
                retValue = super.getIcon(type);
            }
//            if (type == BeanInfo.ICON_COLOR_32x32 && retValue.getHeight(null) == 16) {
//                System.out.println("scaling");
                retValue = retValue.getScaledInstance(-1, 32, Image.SCALE_DEFAULT);
//            }
            return retValue;
        }
        
        private boolean hasMain() {
            DataObject dobj = (DataObject)getLookup().lookup(DataObject.class);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                if(!fo.isValid()) {
                    return false;
                }
                JavaModel.getJavaRepository().beginTrans(false);
                try {
                    JavaModel.setClassPath(fo);
                    Resource r = JavaModel.getResource(fo);
                    
                    return r!=null && !r.getMain().isEmpty();
                } finally {
                    JavaModel.getJavaRepository().endTrans();
                }
            }
            return false;
        }
        
        
    } 
    
    private static class BigIconFilterChilden extends FilterNode.Children {
        FileObject rootDir;
        private RootFileOobjectListener listener = null;
        BigIconFilterChilden(Node orig) {
            super(orig);
            DataObject dobj = (DataObject)orig.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                Project prj = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
                if (prj != null) {
                    rootDir = prj.getProjectDirectory();
                }
            }
        }

        protected Node[] createNodes(Object object) {
            Node orig = (Node)object;
            DataObject dobj = (DataObject)orig.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                // this has to be copied from the logicalviewrootnode, because
                // we need to construct the children from the root package directly from DataObjects..
                
                FileObject fo = dobj.getPrimaryFile();
                if ("bluej.pkg".equals(fo.getNameExt()) ||
                        "build.xml".equals(fo.getNameExt()) ||
                        "bluej.pkh".equals(fo.getNameExt()) ||
                        ("+libs".equals(fo.getName()) && fo.isFolder()) ||
                        ".DS_Store".equals(fo.getNameExt()) ||
                        "ctxt".equals(fo.getExt()) ||
                        "class".equals(fo.getExt()) ||
                        (fo.isFolder() && fo.getFileObject("bluej.pkg") == null)) {
                    return new Node[0];
                }
                if (rootDir != null && rootDir.equals(fo)) {
                    if (listener == null) {
                        //add just once..
                        listener = new RootFileOobjectListener(this, orig, fo);
                        fo.addFileChangeListener(listener);
                    }
                    Enumeration en = ((DataFolder)dobj).children();
                    Collection col = new ArrayList();
                    while (en.hasMoreElements()) {
                        DataObject d2 = (DataObject)en.nextElement();
                        if (d2.getPrimaryFile().isData()) {
                            col.addAll(Arrays.asList(createNodes(d2.getNodeDelegate().cloneNode())));
                        }
                    }
                    return (Node[])col.toArray(new Node[col.size()]);
                }
                return new Node[] {new BigIconFilterNode(orig, fo.isData() ? Children.LEAF : new BigIconFilterChilden(orig))};
            }
            return new Node[0];
        }
        
        public void doRefresh(Node original) {
            refreshKey(original);
        }
        
    private static class RootFileOobjectListener implements FileChangeListener {

        private BigIconFilterChilden children;

        private Node node;

        private FileObject fileObject;
        
        RootFileOobjectListener(BigIconFilterChilden childs, Node nd, FileObject fo) {
            children = childs;
            node = nd;
            fileObject = fo;
        }
        
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }
        public void fileChanged(FileEvent fileEvent) {
            //#75991 not posting it results in ugly assertions/exceptions from mdr.
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    children.doRefresh(node);
                }
            });
        }
        public void fileDataCreated(FileEvent fileEvent) {
            children.doRefresh(node);
        }
        public void fileDeleted(FileEvent fileEvent) {
            children.doRefresh(node);
        }
        public void fileFolderCreated(FileEvent fileEvent) {
            children.doRefresh(node);
        }
        public void fileRenamed(FileRenameEvent fileRenameEvent) {
            children.doRefresh(node);
        }
        
    }
        
    }
}