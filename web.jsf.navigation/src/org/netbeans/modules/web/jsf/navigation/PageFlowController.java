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
 *
 * PageFlowController.java
 *
 * Created on March 1, 2007, 1:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseNode;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author joelle lam
 */
public class PageFlowController {
    private PageFlowView view;
    private JSFConfigModel configModel;
    private FileObject webFolder;
    private Collection<FileObject> webFiles;
    private DataObject configDataObj;
    
    private HashMap<NavigationCase,NavigationCaseNode> case2Node = new HashMap<NavigationCase,NavigationCaseNode>();
    
    //This should always match what is inside the scene.
    private HashMap<NavigationRule,String> navRule2String = new HashMap<NavigationRule,String>();
    
    /**
     * Temporarily Make Public for Work Around.
     */
    public HashMap<String,PageFlowNode> pageName2Node = new HashMap<String,PageFlowNode>();  //Should this be synchronized.
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18NF
    
    /** Creates a new instance of PageFlowController
     * @param context
     * @param view
     */
    public PageFlowController(JSFConfigEditorContext context, PageFlowView view ) {
        this.view = view;
        FileObject configFile = context.getFacesConfigFile();
        
        try {
            configDataObj = (DataObject) DataObject.find(configFile);
            
        } catch (DataObjectNotFoundException donfe ){
            donfe.printStackTrace();
        }
        configModel = ConfigurationUtils.getConfigModel(configFile,true);
        Project project = FileOwnerQuery.getOwner(configFile);
        webFolder = project.getProjectDirectory().getFileObject(DEFAULT_DOC_BASE_FOLDER);
        webFiles = getAllProjectRelevantFilesObjects();
        
        //        setupGraph();
    }
    
    
    private PropertyChangeListener pcl;
    private FileChangeListener fcl;
    
    public void registerListeners() {
        if( pcl == null ) {
            pcl = new FacesModelPropertyChangeListener(view);
            if( configModel != null ) {
                configModel.addPropertyChangeListener(pcl);
            }
        }
        FileObject webFolder = getWebFolder();
        if( fcl == null ){
            fcl = new WebFolderListener();
            if( webFolder != null ){
                try             {
                    webFolder.getFileSystem().addFileChangeListener(fcl);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
    }
    
    /**
     * Unregister any listeners.
     */
    public void unregisterListeners() {
        if ( pcl != null && configModel != null ){
            configModel.removePropertyChangeListener(pcl);
            pcl = null;
        }
        
        FileObject webFolder = getWebFolder();
        if (fcl != null && webFolder != null ) {
            try         {
                webFolder.getFileSystem().removeFileChangeListener(fcl);
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            //            webFolder.removeFileChangeListener(fcl);
            //            for (Enumeration<FileObject> e = (Enumeration<FileObject>) webFolder.getFolders(true); e.hasMoreElements() ;) {
            //                e.nextElement().removeFileChangeListener(fcl);
            //            }
            //            fcl = null;
        }
    }
    
    /**
     * Set From outcome by default.
     * @param source
     * @param target
     * @param comp
     * @return
     */
    public NavigationCase createLink(PageFlowNode source, PageFlowNode target, String comp) {
        
        String sourceName = source.getDisplayName();
        int caseNum = 1;
        
        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        NavigationRule navRule = getRuleWithFromViewID(facesConfig, source.getDisplayName());
        NavigationCase navCase = configModel.getFactory().createNavigationCase();
        
        if (navRule == null) {
            navRule = configModel.getFactory().createNavigationRule();
            navRule.setFromViewId(source.getDisplayName());
            facesConfig.addNavigationRule(navRule);
            navRule2String.put(navRule, navRule.getFromViewId());
        } else {
            caseNum = getNewCaseNumber(navRule);
        }
        
        navCase.setFromOutcome(CASE_STRING + Integer.toString(caseNum));
        navCase.setToViewId(target.getDisplayName());
        navRule.addNavigationCase(navCase);
        
        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return navCase;
        
    }
    
    private final static String CASE_STRING = "case";
    
    private int getNewCaseNumber( NavigationRule navRule ) {
        Collection<String> caseOutcomes = new HashSet<String>();
        List<NavigationCase> navCases = navRule.getNavigationCases();
        for( NavigationCase navCase : navCases ){
            caseOutcomes.add(navCase.getFromOutcome());
            caseOutcomes.add(navCase.getFromAction());
        }
        
        int caseNum = 1;
        while( true ){
            if( !caseOutcomes.contains(CASE_STRING + Integer.toString(caseNum)) ){
                return caseNum;
            }
            caseNum++;
        }
    }
    
    /**
     * @return the navigation rule.  This will be null if none was found
     **/
    private NavigationRule getRuleWithFromViewID(FacesConfig facesConfig, String fromViewId ){
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        
        for( NavigationRule rule : rules ){
            //            System.out.println("\nDo these match?");
            //            System.out.println(rule.getFromViewId() + " == " + fromViewId);
            if( rule.getFromViewId().equals(fromViewId) ){
                //                System.out.println("Match Found.");
                return rule;
            }
        }
        
        return null;
    }
    
    
    
    
    private Collection<FileObject> getAllProjectRelevantFilesObjects() {
        Collection<FileObject> webFiles = getProjectKnownFileOjbects(webFolder);
        //        System.out.println("Web Files: " + webFiles);
        return webFiles;
        
        //Add a listener to the Filesystem that listens to fileDelete, fileCreated, etc.
        //DataObject.find
        //        DataObject.find(parentFolder)
        
    }
    
    
    private Collection<FileObject> getProjectKnownFileOjbects(FileObject folder ) {
        Collection<FileObject> webFiles = new HashSet<FileObject>();
        FileObject[] childrenFiles = folder.getChildren();
        for( FileObject file : childrenFiles ){
            if( !file.isFolder() ) {
                if( isKnownFile(file) ) {
                    webFiles.add(file);
                }
            } else if(isKnownFolder(file)){
                webFiles.addAll(getProjectKnownFileOjbects(file));
            }
        }
        
        return webFiles;
    }
    
    private boolean isKnownFile(FileObject file) {
        if( file.getMIMEType().equals("text/x-jsp")) {
            return true;
        } else if ( file.getMIMEType().equals("text/html") ){
            return true;
        }
        return false;
    }
    
    private boolean isKnownFolder( FileObject folder ){
        
        if( !folder.getPath().contains("WEB-INF") && !folder.getPath().contains("META-INF") ) {
            return true;
        }
        return false;
    }
    
    /**
     * Setup The Graph
     * Should only be called by init();
     *
     **/
    public boolean setupGraph(){
        assert configModel !=null;
        assert webFolder != null;
        assert webFiles != null;
        
        
        view.saveLocations();
        view.clearGraph();
        pageName2Node.clear();
        case2Node.clear();
        navRule2String.clear();
        
        FacesConfig facesConfig = configModel.getRootComponent();
        
        if( facesConfig == null ) {
            return false;
        }
        
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        for( NavigationRule navRule : rules ){
            navRule2String.put(navRule, navRule.getFromViewId());
        }
        
        String currentScope = PageFlowUtilities.getInstance().getCurrentScope();
        Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
        if (currentScope.equals(PageFlowUtilities.LBL_SCOPE_FACESCONFIG)){
            createFacesConfigPageNodes(pagesInConfig);
        } else if (currentScope.equals(PageFlowUtilities.LBL_SCOPE_PROJECT)) {
            createAllProjectPageNodes(pagesInConfig);
        }
        createAllEdges(rules);
        //view.layoutGraph();
        view.validateGraph();
        //        view.layoutSceneImmediately();
        return true;
        
    }
    
    private void createAllEdges( List<NavigationRule> rules ){
        for( NavigationRule rule : rules ) {
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                NavigationCaseNode node = new NavigationCaseNode(this, navCase);
                case2Node.put(navCase, node);
                
                createEdge(node);
            }
        }
    }
    
    private void createEdge(NavigationCaseNode caseNode ){
        String toPage = caseNode.getToViewId();
        String action = caseNode.getFromAction();
        String fromPage = caseNode.getFromViewId();
        view.createEdge(caseNode, pageName2Node.get(fromPage), pageName2Node.get(toPage));
    }
    
    
    private Collection<String> getFacesConfigPageNames(List<NavigationRule>rules) {
        // Get all the pages in the faces config.
        Collection<String> pages = new HashSet<String>();
        for( NavigationRule rule : rules ){
            String pageName = rule.getFromViewId();
            pages.add(pageName);
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                String toPage = navCase.getToViewId();
                pages.add(toPage);
            }
        }
        return pages;
    }
    
    public PageFlowNode createPageFlowNode(Node node) {
        return new PageFlowNode(this, node);
    }
    
    private void createAllProjectPageNodes(Collection<String> pagesInConfig) {
        
        
        Collection<String> pages = new HashSet<String>(pagesInConfig);
        
        //Create all pages in the project...
        for( FileObject webFile : webFiles ) {
            //DISPLAYNAME:
            String webFileName = PageFlowNode.getFolderDisplayName(getWebFolder(), webFile);
            //            String webFileName = webFile.getNameExt();
            pages.remove(webFileName);
            PageFlowNode node = null;
            try {
                //                                node = (DataNode)(DataObject.find(webFile)).getNodeDelegate();
                node = createPageFlowNode((DataObject.find(webFile)).getNodeDelegate());
                view.createNode(node, null, null);
            } catch ( DataObjectNotFoundException ex ) {
                ex.printStackTrace();
            } catch( ClassCastException cce ){
                cce.printStackTrace();
            }
        }
        
        //Create any pages that don't actually exist but are defined specified by the config file.
        for( String pageName : pages ){
            Node tmpNode = new AbstractNode(Children.LEAF);
            tmpNode.setName(pageName);
            PageFlowNode node = new PageFlowNode(this,tmpNode);
            //            Node node = new AbstractNode(Children.LEAF);
            //            node.setName(pageName);
            view.createNode(node, null, null);
        }
    }
    
    /**
     * Givena pageName, look through the list of predefined webFiles and return the matching fileObject
     * @return FileObject for which the match was found or null of none was found.
     **/
    private FileObject getFileObject(String pageName){
        for( FileObject webFile : webFiles ) {
            //DISPLAYNAME:
            String webFileName = PageFlowNode.getFolderDisplayName(getWebFolder(), webFile);
            //            String webFileName = webFile.getNameExt();
            if( webFileName.equals(pageName)) {
                return webFile;
            }
        }
        return null;
    }
    
    private void createFacesConfigPageNodes(Collection<String> pagesInConfig) {
        Collection<String> pages = new HashSet<String>(pagesInConfig);
        
        for( String pageName : pages ) {
            FileObject file = getFileObject(pageName);
            Node wrapNode = null;
            if( file == null ) {
                wrapNode = new AbstractNode(Children.LEAF);
                wrapNode.setName(pageName);
                
            } else {
                try {
                    wrapNode = (DataObject.find(file)).getNodeDelegate();
                } catch(DataObjectNotFoundException donfe ){
                    donfe.printStackTrace();
                }
            }
            PageFlowNode node = new PageFlowNode(this, wrapNode);
            view.createNode(node, null, null);
        }
    }
    
    
    private  class FacesModelPropertyChangeListener implements PropertyChangeListener {
        public FacesModelPropertyChangeListener( PageFlowView view ){
            
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            if( ev.getOldValue() == State.NOT_WELL_FORMED ){
                view.removeUserMalFormedFacesConfig();  // Does clear graph take care of this?
                setupGraph();
            }
            
            if ( ev.getPropertyName() == "navigation-case"){
                
                NavigationCase myNewCase = (NavigationCase)ev.getNewValue();  //Should also check if the old one is null.
                NavigationCase myOldCase = (NavigationCase)ev.getOldValue();
                if( myNewCase != null ){
                    NavigationCaseNode node = new NavigationCaseNode(view.getPageFlowController(), myNewCase);
                    case2Node.put(myNewCase, node);
                    createEdge(node);
                }
                if ( myOldCase != null ){
                    NavigationCaseNode caseNode = case2Node.remove(myOldCase);
                    view.removeEdge(caseNode);
                    
                    String toPage = caseNode.getToViewId();
                    PageFlowNode pageNode = pageName2Node.get(toPage);
                    assert pageNode != null;
                    if( pageNode != null && !isPageInFacesConfig(toPage)){
                        if( !pageNode.isDataNode() || PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_FACESCONFIG){
                            pageName2Node.remove(pageNode);
                            view.removeNodeWithEdges(pageNode);
                            view.validateGraph();
                            //                                node.destroy(); //only okay because it is an abstract node.
                        }
                    }
                }
                view.validateGraph();
            } else if (ev.getPropertyName() == "navigation-rule" ) {
                //You can actually do nothing.
                NavigationRule myNewRule = (NavigationRule) ev.getNewValue();
                NavigationRule myOldRule = (NavigationRule) ev.getOldValue();
                //This has side effects in PageFlowNode destroy.
                //Because it does not consistantly work, I can't account for reactions.
                if( myOldRule != null ){
                    String fromPage = navRule2String.remove(myOldRule);
                    PageFlowNode pageNode = pageName2Node.get(fromPage);
                    assert pageNode != null;
                    
                    if( pageNode != null && !isPageInFacesConfig(fromPage)){
                        if( !pageNode.isDataNode() || PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_FACESCONFIG){
                            pageName2Node.remove(pageNode);
                            view.removeNodeWithEdges(pageNode);
                            view.validateGraph();
                            //                                node.destroy(); //only okay because it is an abstract node.
                        }
                    }
                }
                if( myNewRule != null ){
                    navRule2String.put(myNewRule, myNewRule.getFromViewId());
                }
            } else if ( ev.getNewValue() == State.NOT_SYNCED ) {
                // Do nothing.
            } else if (ev.getNewValue() == State.NOT_WELL_FORMED ){
                view.clearGraph();
                view.warnUserMalFormedFacesConfig();
            } else if (ev.getPropertyName() == "textContent" ){
                setupGraph();
            } else if ( ev.getPropertyName() == "from-view-id"  || ev.getPropertyName() == "to-view-id"){
                /* Going to have to do this another day. */
//                String oldName = (String) ev.getOldValue();
//                String newName = (String) ev.getNewValue();
//                PageFlowNode oldPageNode = pageName2Node.get(oldName);
//                PageFlowNode newPageNode = pageName2Node.get(oldName);
//                boolean isNewPageLinked = false;
//                if( newPageNode != null && view.getNodeEdges(newPageNode).size() > 0 ){
//                    isNewPageLinked = true;
//                }
//                
//                if ( oldPageNode != null && !isPageInFacesConfig(oldName) && !isNewPageLinked ) {
//                    FileObject fileObj = getWebFolder().getFileObject(newName);
//                    if ( fileObj != null && webFiles.contains(fileObj) ){
//                        try                 {
//                            Node delegate = DataObject.find(fileObj).getNodeDelegate();
//                            oldPageNode.replaceWrappedNode(createPageFlowNode(delegate));
//                            view.resetNodeWidget(oldPageNode);
//                            view.validateGraph();
//                        } catch (DataObjectNotFoundException ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    } else {
//                        changeToAbstractNode(oldPageNode, newName);
//                    }
//                } else {
                    setupGraph();
//                }
            } else {
                System.out.println("Did not catch this event.: " + ev.getPropertyName());
                setupGraph();
            }
        }
    }
    
   
    
    public void removeSceneNodeEdges(PageFlowNode pageNode) {
        
        Collection<NavigationCaseNode> navCaseNodes = view.getNodeEdges(pageNode);
        for( NavigationCaseNode navCaseNode : navCaseNodes ){
            try         {
                navCaseNode.destroy();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            //            view.removeEdge(navCaseNode);
        }
    }
    
    
    /**
     * Renames a page in the faces configuration file.
     * @param oldDisplayName
     * @param newDisplayName
     */
    public void renamePageInModel(String oldDisplayName, String newDisplayName ) {
        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        List<NavigationRule> navRules = facesConfig.getNavigationRules();
        for( NavigationRule navRule : navRules ){
            if ( navRule.getFromViewId().equals(oldDisplayName) ){
                navRule.setFromViewId(newDisplayName);
            }
            List<NavigationCase> navCases = navRule.getNavigationCases();
            for( NavigationCase navCase : navCases ) {
                if ( navCase.getToViewId().equals(oldDisplayName) ) {
                    navCase.setToViewId(newDisplayName);
                }
            }
        }
        
        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    /**
     * Remove all rules and cases with this pagename.
     * @param displayName
     */
    public void removePageInModel( String displayName ){
        configModel.startTransaction();
        FacesConfig facesConfig = configModel.getRootComponent();
        List<NavigationRule> navRules = facesConfig.getNavigationRules();
        for( NavigationRule navRule : navRules ){
            if ( navRule.getFromViewId().equals(displayName) ){
                //if the rule is removed, don't check the cases.
                facesConfig.removeNavigationRule(navRule);
            } else {
                List<NavigationCase> navCases = navRule.getNavigationCases();
                for( NavigationCase navCase : navCases ) {
                    if ( navCase.getToViewId().equals(displayName) ) {
                        navRule.removeNavigationCase(navCase);
                    }
                }
            }
        }
        
        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    /**
     * Gets the WebFolder which contains the jsp pages.
     * @return FileObject webfolder
     */
    public FileObject getWebFolder() {
        return webFolder;
    }
    
    //     /**** HACK until Petr. P's bug is fixed *****/
    //    public void removeNode( PageFlowNode node) {
    //        /*There should be any edges though*/
    //        view.removeNodeWithEdges(node);
    //    }
    
    public boolean isPageInFacesConfig(String name){
        List<NavigationRule> rules = configModel.getRootComponent().getNavigationRules();
        Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
        return pagesInConfig.contains(name);
    }
    
    
    public void changeToAbstractNode(PageFlowNode oldNode, String displayName ) {
        //1. Make Old Node an abstract node
        Node tmpNode = new AbstractNode(Children.LEAF);
        tmpNode.setName(displayName);
        oldNode.replaceWrappedNode(tmpNode);  //Does this take care of pageName2Node?
        view.resetNodeWidget(oldNode);
    }
    
    private class WebFolderListener extends FileChangeAdapter{
        
        
        private boolean isKnownFileEvent(FileObject potentialChild ) {
            if ( FileUtil.isParentOf(getWebFolder(), potentialChild) ) {
                if( potentialChild.isFolder() ){
                    return isKnownFolder(potentialChild);
                } else {
                    return isKnownFile(potentialChild);
                }
            }
            return false;
        }
        
        public void fileDataCreated(FileEvent fe) {
            FileObject fileObj = fe.getFile();
            if( !isKnownFileEvent(fileObj) ){
                return;
            }
            
            try         {
                if( isKnownFile(fileObj)){
                    webFiles.add(fileObj);
                    DataObject dataObj = DataObject.find(fileObj);
                    Node dataNode = dataObj.getNodeDelegate();
                    //                    PageFlowNode pageNode = pageName2Node.get(dataNode.getDisplayName());
                    //DISPLAYNAME:
                    PageFlowNode pageNode = pageName2Node.get(PageFlowNode.getFolderDisplayName(getWebFolder(), fileObj));
                    if( pageNode != null  ) {
                        pageNode.replaceWrappedNode(dataNode);
                        view.resetNodeWidget(pageNode);
                        view.validateGraph();
                    } else if ( PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_PROJECT ){
                        PageFlowNode node = createPageFlowNode(dataNode);
                        view.createNode(node, null, null);
                        view.validateGraph();
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public void fileChanged(FileEvent fe) {
            //            System.out.println("File Changed Event: " + fe);
        }
        
        public void fileDeleted(FileEvent fe) {
            FileObject fileObj = fe.getFile();
            if ( !webFiles.remove(fileObj) ) {
                return;
            }
            //            if( fileObj.isFolder() ){
            //                return;
            //            }
            
            //DISPLAYNAME:
            String pageDisplayName = PageFlowNode.getFolderDisplayName(getWebFolder(), fileObj);
            
            PageFlowNode oldNode = pageName2Node.get(pageDisplayName);
            if( oldNode != null ) {
                if( isPageInFacesConfig(oldNode.getDisplayName()) ) {
                    Node tmpNode = new AbstractNode(Children.LEAF);
                    tmpNode.setName(pageDisplayName);
                    oldNode.replaceWrappedNode(tmpNode);
                    view.resetNodeWidget(oldNode);  /* If I add a listener to PageFlowNode, then I won't have to do this*/
                } else {
                    pageName2Node.remove(oldNode);
                    view.removeNodeWithEdges(oldNode);
                }
                view.validateGraph();   //Either action validate graph
            }
            
            
        }
        
        String oldFolderName;
        String newFolderName;
        public void fileRenamed(FileRenameEvent fe) {
            /* fileRenamed should not modify the faces-config because it should
             * be up to refactoring to do this. If that is the case, FacesModelPropertyChangeListener
             * should reload it.
             * WARNING: Will get setup twice.*/
            FileObject fileObj = fe.getFile();
            if( !webFiles.contains(fileObj) && !isKnownFolder(fileObj) ){
                return;
            }
            
            if( fileObj.isFolder() ){
                //I may still need to modify display names.
                
                if( fe.getName().equals(oldFolderName) && fileObj.getName().equals(newFolderName) ){
                    //Folder rename triggers two listeners.  Only pay attention to the first one.
                    return;
                }
                oldFolderName = fe.getName();
                newFolderName = fileObj.getName();
                folderRename( fileObj, oldFolderName, newFolderName);
                
            } else {
                //DISPLAYNAME:
                String newDisplayName  = PageFlowNode.getFolderDisplayName(getWebFolder(), fileObj);
                String path = fileObj.getPath().replace(fileObj.getNameExt(), "");
                String oldDisplayName = PageFlowNode.getFolderDisplayName(getWebFolder(), path, fe.getName() + "." + fe.getExt());
                
                fileRename(fileObj, oldDisplayName, newDisplayName);
            }
            view.validateGraph();
            
        }
        
        private void folderRename( FileObject folderObject, String oldFolderName, String newFolderName ){
            FileObject[] fileObjs = folderObject.getChildren();
            for( FileObject file : fileObjs) {
                
                if( file.isFolder() ){
                    folderRename( file, oldFolderName, newFolderName);
                } else {
                    String newDisplayName = PageFlowNode.getFolderDisplayName(getWebFolder(), file);
                    String oldDisplayName = newDisplayName.replaceFirst(newFolderName, oldFolderName);
                    fileRename(file, oldDisplayName, newDisplayName);
                }
            }
        }
        
        
        private void fileRename(FileObject fileObj, String oldDisplayName, String newDisplayName ){

            PageFlowNode oldNode = pageName2Node.get(oldDisplayName);
            
            if ( oldNode.isRenaming()){
                return;
            }
            
            PageFlowNode abstractNode = pageName2Node.get(newDisplayName);
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
            
            //If new node has an abstract node representation already.
            //  True:  Replace the abstract node with the new data node file.
            //             * if( oldNode in facesConfig ) -> turn old node into abstract
            //             * else -> remove old node altogether.
            //             * make abstract node represent file node.
            // False:
            //             * if( oldNode in facesConfig ) -> make abstract node and create new datanode
            //             * else -> reset node.
            if( abstractNode != null ){
                assert !abstractNode.isDataNode();  //Never should this have already been a file node.
                //Figure out what to do with old node.
                if (isPageInFacesConfig(oldDisplayName)){
                    changeToAbstractNode(oldNode, oldDisplayName);
                } else if ( oldNode != null ){
                    view.removeNodeWithEdges(oldNode);
                }
                abstractNode.replaceWrappedNode(newNodeDelegate);
                view.resetNodeWidget(abstractNode);
            } else if ( oldNode != null ){
                if( isPageInFacesConfig(oldDisplayName) ){
                    changeToAbstractNode(oldNode, oldDisplayName);
                    if( PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_PROJECT ) {
                        PageFlowNode newNode = createPageFlowNode(newNodeDelegate);
                        view.createNode(newNode, null, null);
                    }
                } else {
                    view.resetNodeWidget(oldNode);
                }
            }
            view.validateGraph();
            
        }
        
        
        public void fileFolderCreated(FileEvent fe) {
            //            fe.getFile().addFileChangeListener( fcl);
        }
        
        
    }
    
    public DataObject getConfigDataObject() {
        return configDataObj;
    }
    
    public void saveLocation(PageFlowNode node, String newDisplayName) {
        view.saveLocation(node, newDisplayName);
    }
    
}
