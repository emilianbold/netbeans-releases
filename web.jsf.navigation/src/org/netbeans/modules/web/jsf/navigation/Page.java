/*
 * PageFlowNode.java
 *
 * Created on April 4, 2007, 12:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.jsf.navigation.graph.PageSceneElement;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author joelle
 */
public class Page extends PageSceneElement implements SaveCookie {
    
    public PageFlowController pc;
    private Node original;
    private PageContentModel pageContentModel = null;
    
    /**
     * Creates a PageFlowNode
     * @param pc
     * @param original
     */
    public Page( PageFlowController pc, Node original ){
        this.pc = pc;
        //            super(original, Children.LEAF);
        setNode(original);
        //        pc.pageName2Node.put(getDisplayName(), this);
        //        pc.putPageName2Node(getDisplayName(), this);
        updateContentModel();
        initListeners();
    }
    
    public void updateContentModel() {
//        if ( pageContentModel != null ){
//            try         {
//                pageContentModel.destroy();
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
        if( !isDataNode() ){
            return;
        }
        FileObject fileObject = ((DataNode)original).getDataObject().getPrimaryFile();
        Lookup.Template templ = new Lookup.Template(PageContentModelProvider.class);
        final Lookup.Result result = Lookup.getDefault().lookup(templ);
        Collection<PageContentModelProvider> impls = result.allInstances();
        
        for( PageContentModelProvider provider : impls){
            pageContentModel = provider.getPageContentModel(fileObject);
            //exit when you find one.
            if(pageContentModel != null){
                return;
            }
        }
        // use Java Collections API to get iterator, ...
        
        //I think it is okay to ask users to restart the ide if the install a new module with the PageContentModel.
        // Pay attention to subsequent changes in the result.
        //        result.addLookupListener(new LookupListener() {
        //            public void resultChanged(LookupEvent ev) {
        //                Collection impls2 = result.allInstances();
        //                // use the new list of instances...
        //            }
        //});
        
    }
    
    
    //    public Node getWrappedNode() {
    //        return original;
    //    }
    
    
    private String nodeDisplayName;
    
    private Node _oldNode;
    private void setNode(Node newNode ){
        _oldNode = original;
        original = newNode;
        nodeDisplayName = original.getDisplayName();
        //HACK sometimes the datanode name isn't updated as fast as the filename.
        if( original instanceof DataNode ){
            assert pc != null;
            
            FileObject fileObj = ((DataNode)original).getDataObject().getPrimaryFile();
            assert fileObj != null;
            String oldNodeDisplayName = nodeDisplayName;
            nodeDisplayName = getFolderDisplayName(pc.getWebFolder(), fileObj );
            
            if( !nodeDisplayName.equals(oldNodeDisplayName) ){
                //DISPLAYNAME:
                //                pc.replacePageName2Node(this, nodeDisplayName, oldNodeDisplayName );
                //                pc.removePageName2Node(nodeDisplayName);
                //                pc.putPageName2Node(nodeDisplayName, this);
            }
            
        }
        pc.putPageName2Node(nodeDisplayName, this);
    }
    
    public void updateNode_HACK(){
        setNode(original);
    }
    
    
    /* We may want this to notify listeners of changes.*/
    public void replaceWrappedNode(Node newNode ){
        //        pc.pageName2Node.remove(getDisplayName());
        pc.removePageName2Node(getDisplayName(), false);
        setNode(newNode);
        //        pc.putPageName2Node(getDisplayName(), this);
    }
    
    private boolean renaming = false;;
    public boolean isRenaming(){
        return renaming;
    }
    
    @Override
    public void setName(String s) {
        
        String oldDisplayName = getDisplayName();
        try {
            if( !pc.isPageInFacesConfig(oldDisplayName) ) {
                original.setName(s);
            } else {
                renaming = true;
                original.setName(s);
                String newDisplayName = original.getDisplayName();
                if( isDataNode() ){
                    newDisplayName = getFolderDisplayName(pc.getWebFolder(),((DataNode) original).getDataObject().getPrimaryFile());
                }
                pc.saveLocation(this, newDisplayName);
                renaming= false;
                pc.renamePageInModel(oldDisplayName, newDisplayName);
            }
            
        } catch (IllegalArgumentException iae ) {
            
            // determine if "printStackTrace"  and  "new annotation" of this exception is needed
            boolean needToAnnotate = Exceptions.findLocalizedMessage(iae) == null;
            
            // annotate new localized message only if there is no localized message yet
            if (needToAnnotate) {
                Exceptions.attachLocalizedMessage(iae,NbBundle.getMessage(Page.class,
                        "MSG_BadFormat",
                        oldDisplayName,
                        s));
            }
            
            Exceptions.printStackTrace(iae);
        }
    }
    
    public String getDisplayName() {
        return nodeDisplayName;
        //        return original.getDisplayName();
    }
    
    @Override
    public String getName() {
        //        Thread.dumpStack();
        return original.getName();
        //        return nodeDisplayName;
    }
    
    
    /**
     *
     * @return
     */
    @Override
    public boolean canRename() {
        return true;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    /* Joelle: Temporarily I need not use destroy for the other purpose.  I plan to fix after stabilization */
    public void destroy2(){
        destroyListeners();
    }
    

    public void destroy() throws IOException {
        
        Object input = DialogDescriptor.NO_OPTION;       //This should be the default option especially if not a DataNode.
        boolean removePageName2NodeReference = true;    //By default remove it.
        
        
        if ( isDataNode() ){
            //Don't even ask unless DataNode. 
            DialogDescriptor dialog = new DialogDescriptor(
                    NbBundle.getMessage(Page.class, "MSG_DELETE_QUESTION", getDisplayName()),
                    NbBundle.getMessage(Page.class, "MSG_DELETE_TITLE"),
                    true,
                    DialogDescriptor.YES_NO_CANCEL_OPTION,
                    DialogDescriptor.NO_OPTION,
                    null);
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            input = dialog.getValue();
            if ( pc.isProjectCurrentScope() ) {
                removePageName2NodeReference = false;  //if it is a data node and we are in project scope make sure to not remove it.
            }
        }
        
        String displayName = getDisplayName();
        // Would you like to delete this file too?
        if ( input == DialogDescriptor.YES_OPTION ){
            pc.removeSceneNodeEdges(this);
            original.destroy();
        } else if ( input == DialogDescriptor.NO_OPTION ) {
            pc.removeSceneNodeEdges(this);
            if ( removePageName2NodeReference ) {  //HACK Should I remove the node myself until Petr fixes this bug?
                //                pc.removePageName2Node(displayName);
                destroy();
            }
            //            System.out.println("Only Node Removed");
        } else if ( input == DialogDescriptor.CANCEL_OPTION ) {
            //            System.out.println("Cancel... Do Nothing.");
        }
        destroyListeners();
        //
        //        original.destroy();
        //        pc.pageName2Node.remove(getDisplayName());
    }
    
    
    private static final Image ABSTRACTNODE = Utilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/abstract.gif"); // NOI18N
    public Image getIcon(int type) {
        if ( !isDataNode())
            return ABSTRACTNODE;
        return original.getIcon(type);
    }
    
    
    @Override
    public HelpCtx getHelpCtx() {
        return original.getHelpCtx();
    }

    public Node getNode() {
        return original;
    }
    
    
    
    public boolean isDataNode(){
        return ( original instanceof DataNode );
    }
    
    public void save() throws IOException {
        //            pc.getConfigDataObject().getEditorSupport().saveDocument();
        getCookie(SaveCookie.class).save();
    }
    
    
    private SaveCookie saveCookie;
    public <T extends Cookie> T getCookie(Class<T> type) {
        if( type.equals(SaveCookie.class)) {
            saveCookie = pc.getConfigDataObject().getCookie(SaveCookie.class);
            return (T) saveCookie;
        }
        return original.getCookie(type);
    }
    
    /**
     * Solves a fileobjects display name.
     * @param webFolder
     * @param fileObject
     * @return
     */
    public static String getFolderDisplayName( FileObject webFolder, FileObject  fileObject ){
        String folderpath = webFolder.getPath();
        String filepath = fileObject.getPath();
        return filepath.replaceFirst(folderpath+"/", "");
    }
    
    public static String getFolderDisplayName( FileObject webFolder, String path, String fileNameExt ){
        String folderpath = webFolder.getPath();
        return path.replaceFirst(folderpath +"/", "") + fileNameExt;
    }
    
    public Collection<PageContentItem> getPageContentItems() {
        if( pageContentModel == null ){
            return new ArrayList<PageContentItem>();
        }
        return pageContentModel.getPageContentItems();
    }
    
    public Collection<PinNode> getPinNodes() {
        if( pageContentModel == null ){
            return Arrays.asList();
        }
        Collection<PageContentItem> pageContentItems = pageContentModel.getPageContentItems();
        Collection<PinNode> pinNodes = new ArrayList<PinNode>(pageContentItems.size());
        for( PageContentItem pageContentItem : pageContentItems ){
            pinNodes.add(new PinNode(this, pageContentItem));
        }
        return pinNodes;
    }
    
    
    private PageContentChangeListener pccl;
    private void initListeners(){
        if( pageContentModel != null && pccl == null ) {
            pccl = new PageContentChangeListener(pc, this);
            pageContentModel.addChangeListener(pccl);
        }
    }
    private void destroyListeners() {
        if( pccl != null && pageContentModel != null ) {
            try         {
                pageContentModel.removeChangeListener(pccl);
                pageContentModel.destroy();
                pageContentModel = null;
                pccl = null;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    private class PageContentChangeListener implements ChangeListener{
        final PageFlowController pc;
        final Page pageNode;
        public PageContentChangeListener(PageFlowController pc,Page pageNode) {
            this.pc = pc;
            this.pageNode = pageNode;
        }
        
        public void stateChanged(ChangeEvent arg0) {
            pc.updatePageItems(pageNode);
        }
    }
    
    
    public Action[] getActions(boolean context) {
        if( pageContentModel != null ){
            return pageContentModel.getActions();
        }
        return new SystemAction[]{};
        //        if( pageContentModel != null ){
        //            SystemAction[] pageModelActions = pageContentModel.getActions();
        //            SystemAction[] nodeActions = super.getActions();
        //
        //            if( pageModelActions == null || pageModelActions.length == 0 ){
        //                return nodeActions;
        //            } else if ( nodeActions == null || nodeActions.length == 0 ){
        //                return pageModelActions;
        //            } else {
        //                int size = pageModelActions.length + nodeActions.length;
        //                SystemAction[] sysActions = new SystemAction[size];
        //                System.arraycopy(nodeActions, 0, sysActions, 0, nodeActions.length);
        //                System.arraycopy(pageModelActions, 0, sysActions, nodeActions.length, pageModelActions.length);
        //                return sysActions;
        //            }
        //        } else {
        //            return super.getActions();
        //        }
        
    }
    
    public boolean equals(Object obj) {
        return (this == obj);
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    
    
    
    //    @Override
    //    public boolean equals(Object obj) {
    ////        if( original instanceof DataNode )
    ////            return original.equals(obj);
    ////        else {
    //            if( !(obj instanceof PageFlowNode) ){
    //                return false;
    //            }
    //            return getDisplayName().equals(((PageFlowNode)obj).getDisplayName());
    ////        }
    //    }
    
    //    public int hashCode() {
    //        return getDisplayName().hashCode();
    //    }
    
    
}
