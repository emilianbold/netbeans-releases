/*
 * WebFolderListener.java
 *
 * Created on April 17, 2007, 6:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.EventQueue;
import java.io.IOException;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */

public class WebFolderListener extends FileChangeAdapter{
    private PageFlowController pfc;
    private PageFlowView view;
    FileObject webFolder;
    
    /**
     * This web folder listener listens to any modifications related to WebFolder and updates the faces config accordingly.
     * @param pfc
     */
    public WebFolderListener(PageFlowController pfc ) {
        super();
        this.pfc = pfc;
        view = pfc.getView();
        webFolder = pfc.getWebFolder();
    }
    
    
    private boolean isKnownFileEvent(FileObject potentialChild ) {
        if ( FileUtil.isParentOf(webFolder, potentialChild) ) {
            if( potentialChild.isFolder() ){
                return pfc.isKnownFolder(potentialChild);
            } else {
                return pfc.isKnownFile(potentialChild);
            }
        }
        return false;
    }
    
    public void fileDataCreated(FileEvent fe) {
        final FileObject fileObj = fe.getFile();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                fileCreatedEventHandler( fileObj );
            }
        });
        
    }
    
    public void fileChanged(FileEvent fe) {
        //            System.out.println("File Changed Event: " + fe);
    }
    
    public void fileDeleted(FileEvent fe) {
        final FileObject fileObj = fe.getFile();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                fileDeletedEventHandler( fileObj );
            }
        });
        
    }
    public void fileRenamed(FileRenameEvent fe) {
        /* fileRenamed should not modify the faces-config because it should
        * be up to refactoring to do this. If that is the case, FacesModelPropertyChangeListener
        * should reload it.
        */
        final FileObject fileObj = fe.getFile();
        final FileRenameEvent event = fe;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                fileRenamedEventHandler( fileObj, event.getName(), event.getExt());
            }
        });
    }
    
    
    public void fileFolderCreated(FileEvent fe) {
        //            fe.getFile().addFileChangeListener( fcl);
    }
    
    private void fileDeletedEventHandler(FileObject fileObj ) {
        if ( !pfc.removeWebFile(fileObj) ) {
            return;
        }
        
        //DISPLAYNAME:
        String pageDisplayName = PageFlowNode.getFolderDisplayName(webFolder, fileObj);
        
        PageFlowNode oldNode = pfc.getPageName2Node(pageDisplayName);
        if( oldNode != null ) {
            if( pfc.isPageInFacesConfig(oldNode.getDisplayName()) ) {
//                Node tmpNode = new AbstractNode(Children.LEAF);
//                tmpNode.setName(pageDisplayName);
//                oldNode.replaceWrappedNode(tmpNode);
//                view.resetNodeWidget(oldNode, false);  /* If I add a listener to PageFlowNode, then I won't have to do this*/ 
                pfc.changeToAbstractNode(oldNode, pageDisplayName );
            } else {
                view.removeNodeWithEdges(oldNode);
                pfc.removePageName2Node(oldNode, true);
            }
            view.validateGraph();   //Either action validate graph
        }
    }
    
    private void fileCreatedEventHandler( FileObject fileObj ){
        if( !isKnownFileEvent(fileObj) ){
            return;
        }
        
        try         {
            if( pfc.isKnownFile(fileObj) ){
                pfc.addWebFile(fileObj);
                DataObject dataObj = DataObject.find(fileObj);
                Node dataNode = dataObj.getNodeDelegate();
                //                    PageFlowNode pageNode = pageName2Node.get(dataNode.getDisplayName());
                //DISPLAYNAME:
                PageFlowNode pageNode = pfc.getPageName2Node(PageFlowNode.getFolderDisplayName(webFolder, fileObj));
                if( pageNode != null  ) {
                    pageNode.replaceWrappedNode(dataNode);
                    view.resetNodeWidget(pageNode, false);
                    view.validateGraph();
                } else if ( PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_PROJECT ){
                    PageFlowNode node = pfc.createPageFlowNode(dataNode);
                    view.createNode(node, null, null);
                    view.validateGraph();
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    private String oldFolderName;
    private String newFolderName;
    private void fileRenamedEventHandler( FileObject fileObj, String oldName, String extName ) {
        if( !pfc.containsWebFile(fileObj) && !pfc.isKnownFolder(fileObj) ){
            return;
        }
        
        if( fileObj.isFolder() ){
            //I may still need to modify display names.
            
            if( oldName.equals(oldFolderName) && fileObj.getName().equals(newFolderName) ){
                //Folder rename triggers two listeners.  Only pay attention to the first one.
                return;
            }
            oldFolderName = oldName;
            newFolderName = fileObj.getName();
            renameFolder( fileObj, oldFolderName, newFolderName);
            
        } else {
            //DISPLAYNAME:
            String newDisplayName  = PageFlowNode.getFolderDisplayName(webFolder, fileObj);
            String path = fileObj.getPath().replace(fileObj.getNameExt(), "");
            String oldDisplayName = PageFlowNode.getFolderDisplayName(webFolder, path, oldName+ "." + extName);
            
            renameFile(fileObj, oldDisplayName, newDisplayName);
        }
        view.validateGraph();
    }
    
    private void renameFolder( FileObject folderObject, String oldFolderName, String newFolderName ){
        FileObject[] fileObjs = folderObject.getChildren();
        for( FileObject file : fileObjs) {
            
            if( file.isFolder() ){
                renameFolder( file, oldFolderName, newFolderName);
            } else {
                String newDisplayName = PageFlowNode.getFolderDisplayName(webFolder, file);
                String oldDisplayName = newDisplayName.replaceFirst(newFolderName, oldFolderName);
                renameFile(file, oldDisplayName, newDisplayName);
            }
        }
    }
    
    
    private void renameFile(FileObject fileObj, String oldDisplayName, String newDisplayName ){
        
        PageFlowNode oldNode = pfc.getPageName2Node(oldDisplayName);
        
        if ( oldNode == null || oldNode.isRenaming()){
            return;
        }
        
        PageFlowNode abstractNode = pfc.getPageName2Node(newDisplayName);
        Node newNodeDelegate = null;
        try {
            newNodeDelegate = (DataObject.find(fileObj)).getNodeDelegate();
        } catch ( DataObjectNotFoundException donfe ){
            Exceptions.printStackTrace(donfe);
        }
        
        //If we are in project view scope
        if( PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_PROJECT ){
            assert oldNode != null;
        }
        
        if( abstractNode != null ){
//            assert !abstractNode.isDataNode();  //Never should this have already been a file node.
            if( abstractNode.isDataNode()) {
                System.err.println("So Called Abstract Node: " + abstractNode);
                Thread.dumpStack();
            }
            
            
            //Figure out what to do with old node.
            if (pfc.isPageInFacesConfig(oldDisplayName)){
                pfc.changeToAbstractNode(oldNode, oldDisplayName);
            } else if ( oldNode != null ){
                view.removeNodeWithEdges(oldNode);
            }
            abstractNode.replaceWrappedNode(newNodeDelegate);
            view.resetNodeWidget(abstractNode, false);
        } else if ( oldNode != null ){
            if( pfc.isPageInFacesConfig(oldDisplayName) ){
                pfc.changeToAbstractNode(oldNode, oldDisplayName);
                if( PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_PROJECT ) {
                    PageFlowNode newNode = pfc.createPageFlowNode(newNodeDelegate);
                    view.createNode(newNode, null, null);
                }
            } else {
                view.resetNodeWidget(oldNode, false);
            }
        }
        view.validateGraph();
        
    }
    
}
