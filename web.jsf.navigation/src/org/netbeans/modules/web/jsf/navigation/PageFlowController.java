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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import java.util.*;
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
    
    private final HashMap<NavigationCase,NavigationCaseEdge> case2Node = new HashMap<NavigationCase,NavigationCaseEdge>();
    
    //This should always match what is inside the scene.
    private HashMap<NavigationRule,String> navRule2String = new HashMap<NavigationRule,String>();
    
    /**
     * Temporarily Make Public for Work Around.
     */
    private final HashMap<String,Page> pageName2Node = new HashMap<String,Page>();  //Should this be synchronized.
    
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
        
    }
    
    
    private PropertyChangeListener pcl;
    private FileChangeListener fcl;
    
    public void registerListeners() {
        if( pcl == null ) {
            pcl = new FacesModelPropertyChangeListener(this);
            if( configModel != null ) {
                configModel.addPropertyChangeListener(pcl);
            }
        }
        FileObject webFolder = getWebFolder();
        if( fcl == null ){
            fcl = new WebFolderListener(this);
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
     * @param pinNode if null then it was not conntect to a pin.
     * @return
     */
    public NavigationCase createLink(Page source,Page target, PinNode pinNode) {
        
        String sourceName = source.getDisplayName();
        int caseNum = 1;
        
        configModel.startTransaction();
        
        
        FacesConfig facesConfig = configModel.getRootComponent();
        NavigationRule navRule = getRuleWithFromViewID(facesConfig, source.getDisplayName());
        NavigationCase navCase = configModel.getFactory().createNavigationCase();
        try {
            if (navRule == null) {
                navRule = configModel.getFactory().createNavigationRule();
//                navRule.setFromViewId(source.getDisplayName());
                FacesModelUtility.setFromViewId(navRule, source.getDisplayName());
                facesConfig.addNavigationRule(navRule);
//                navRule2String.put(navRule, navRule.getFromViewId());
                navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
            } else {
                caseNum = getNewCaseNumber(navRule);
            }
            String caseName = CASE_STRING + Integer.toString(caseNum);
            
            if( pinNode != null ){
                pinNode.setFromOutcome(caseName);
            }
            navCase.setFromOutcome(caseName);
            
            
//            navCase.setToViewId(target.getDisplayName());
            FacesModelUtility.setToViewId(navCase, target.getDisplayName());
            navRule.addNavigationCase(navCase);
        } catch ( Exception e ){
            Exceptions.printStackTrace(e);
        } finally {
            
            configModel.endTransaction();
            try {
                configModel.sync();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return navCase;
    }
    
    public void updatePageItems( Page pageNode ) {
        view.resetNodeWidget(pageNode, true);
        view.validateGraph();
    }
    
    private final static String CASE_STRING = "case";
    
    private int getNewCaseNumber( NavigationRule navRule ) {
        Collection<String> caseOutcomes = new HashSet<String>();
        List<NavigationCase> navCases = navRule.getNavigationCases();
        for( NavigationCase navCase : navCases ){
            caseOutcomes.add(navCase.getFromOutcome());
            //            caseOutcomes.add(navCase.getFromAction());
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
        
        for( NavigationRule navRule : facesConfig.getNavigationRules() ){
//            String rulefromViewId = navRule.getFromViewId();
            String rulefromViewId = FacesModelUtility.getFromViewIdFiltered(navRule);
            if( rulefromViewId != null && rulefromViewId.equals(fromViewId) ){
                //  Match Found
                return navRule;
            }
        }
        
        return null;
    }
    
    
    
    
    private Collection<FileObject> getAllProjectRelevantFilesObjects() {
        Collection<FileObject> webFiles = getProjectKnownFileOjbects(webFolder);
        return webFiles;
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
    
    public final boolean isKnownFile(FileObject file) {
        if( file.getMIMEType().equals("text/x-jsp")) {
            return true;
        } else if ( file.getMIMEType().equals("text/html") ){
            return true;
        }
        return false;
    }
    
    public final boolean isKnownFolder( FileObject folder ){
        /* If it is not a folder return false*/
        if( !folder.isFolder()  ) {
            return false;
        }
        /* If it does not exist within WebFolder return false */
        if(  !folder.getPath().contains(getWebFolder().getPath() )){
            return false;
        }
        /* If it exists withing WEB-INF or META-INF return false */
        if( folder.getPath().contains("WEB-INF") ||folder.getPath().contains("META-INF") ) {
            return false;
        }
        return true;
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
        clearPageName2Node();
        case2Node.clear();
        navRule2String.clear();
        
        FacesConfig facesConfig = configModel.getRootComponent();
        
        if( facesConfig == null ) {
            return false;
        }
        
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        for( NavigationRule navRule : rules ){
            navRule2String.put(navRule, FacesModelUtility.getFromViewIdFiltered(navRule));
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
        
        System.out.println(pageName2Node);
        
        //        view.layoutSceneImmediately();
        return true;
        
    }
    
    private void createAllEdges( List<NavigationRule> rules ){
        for( NavigationRule rule : rules ) {
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                NavigationCaseEdge node = new NavigationCaseEdge(this, navCase);
                case2Node.put(navCase, node);
                
                createEdge(node);
            }
        }
    }
    
    public void createEdge(NavigationCaseEdge caseNode ){
        String fromPage = caseNode.getFromViewId();
        String toPage = caseNode.getToViewId();
        if( getPageName2Node(fromPage) == null || getPageName2Node(toPage) == null ){
            System.err.println("Why is this node null? CaseNode: " + caseNode );
            System.err.println("FromPage: " + fromPage );
            System.err.println("ToPage: " + toPage );
            Thread.dumpStack();
        } else {
            view.createEdge(caseNode, getPageName2Node(fromPage), getPageName2Node(toPage));
        }
    }
    
    
    private Collection<String> getFacesConfigPageNames(List<NavigationRule>navRules) {
        // Get all the pages in the faces config.
        Collection<String> pages = new HashSet<String>();
        for( NavigationRule navRule : navRules ){
            String pageName = FacesModelUtility.getFromViewIdFiltered(navRule);
            pages.add(pageName);
            List<NavigationCase> navCases = navRule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                //                String toPage = navCase.getToViewId();
                String toPage = FacesModelUtility.getToViewIdFiltered(navCase);
                if( toPage != null ) {
                    pages.add(toPage);
                }
            }
        }
        return pages;
    }
    
    public java.util.Stack<String> PageFlowCreationStack = new java.util.Stack<String>();
    int PageFlowCreationCount = 0;
    public Page createPageFlowNode(Node node) {
        Page pageNode =  new Page(this, node);
        Calendar rightNow = Calendar.getInstance();
        PageFlowCreationStack.push("\n" + PageFlowCreationCount + ". " + rightNow.get(Calendar.MINUTE)+ ":" + rightNow.get(Calendar.SECOND) + " -  " + pageNode);
        PageFlowCreationCount++;
        return pageNode;
        
    }
    public java.util.Stack<String> PageFlowDestroyStack = new java.util.Stack<String>();
    int PageFlowDestroyCount = 0;
    public void destroyPageFlowNode(Page pageNode){
        pageNode.destroy2();
        Calendar rightNow = Calendar.getInstance();
        PageFlowDestroyStack.push("\n" + PageFlowDestroyCount + ". " + rightNow.get(Calendar.MINUTE)+ ":" + rightNow.get(Calendar.SECOND) + " -  " + pageNode);
        PageFlowDestroyCount++;
    }
    
    private void createAllProjectPageNodes(Collection<String> pagesInConfig) {
        
        
        Collection<String> pages = new HashSet<String>(pagesInConfig);
        
        //Create all pages in the project...
        for( FileObject webFile : webFiles ) {
            try {
                //DISPLAYNAME:
                String webFileName = Page.getFolderDisplayName(getWebFolder(), webFile);
                Page node = null;
                node = createPageFlowNode((DataObject.find(webFile)).getNodeDelegate());
                view.createNode(node, null, null);
                //Do not remove the webFile page until it has been created with a data Node.  If the dataNode throws and exception, then it can be created with an Abstract node.
                pages.remove(webFileName);
            } catch ( DataObjectNotFoundException ex ) {
                ex.printStackTrace();
            } catch( ClassCastException cce ){
                cce.printStackTrace();
            }
        }
        
        //Create any pages that don't actually exist but are defined specified by the config file.
        for( String pageName : pages ){
            if( pageName != null ){
                Node tmpNode = new AbstractNode(Children.LEAF);
                tmpNode.setName(pageName);
                Page node = createPageFlowNode(tmpNode);
                view.createNode(node, null, null);
            }
        }
    }
    
    /**
     * Givena pageName, look through the list of predefined webFiles and return the matching fileObject
     * @return FileObject for which the match was found or null of none was found.
     **/
    private FileObject getFileObject(String pageName){
        for( FileObject webFile : webFiles ) {
            //DISPLAYNAME:
            String webFileName = Page.getFolderDisplayName(getWebFolder(), webFile);
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
            if( pageName != null ) {
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
                Page node = createPageFlowNode(wrapNode);
                view.createNode(node, null, null);
            }
        }
    }
    
    
    public Page removePageName2Node(Page pageNode, boolean destroy  ){
        return removePageName2Node(pageNode.getDisplayName(), destroy);
    }
    
    public Page removePageName2Node( String displayName, boolean destroy ) {
        printThreadInfo();
        synchronized ( pageName2Node ) {
            Page node = pageName2Node.remove(displayName);
            if( destroy ) {
                destroyPageFlowNode(node);
            }
            return node;
            
        }
    }
    
    public void replacePageName2Node(Page node, String newName, String oldName  ){
        printThreadInfo();
        synchronized ( pageName2Node ) {
            Page node2 = pageName2Node.remove(oldName);
            if( node == null || node2 == null ){
                System.err.println("PageFlowEditor: Trying to add Page [" + oldName + "] but it is null.");
            }
            pageName2Node.put(newName, node);
        }
    }
    
    public void clearPageName2Node(){
        //        printThreadInfo();
        Set<String> keys;
        synchronized ( pageName2Node ) {
            keys = new HashSet<String>(pageName2Node.keySet());
        }
        for( String key : keys ){
            Page node = removePageName2Node(key, true);
        }
        //            pageName2Node.clear();
        //        }
    }
    
    public void putPageName2Node(String displayName,Page pageNode){
        printThreadInfo();
        if( pageNode == null ){
            throw new RuntimeException("PageFlowEditor: Trying to add Page [" + displayName + "] but it is null.");
        }
        synchronized ( pageName2Node ) {
            pageName2Node.put(displayName, pageNode);
        }
    }
    
    public Page getPageName2Node(String displayName){
        printThreadInfo();
        synchronized ( pageName2Node ) {
            /*
             * Begin Test
             */
            Page pageNode = pageName2Node.remove(displayName);
            if( pageNode != null ) {
                Page pageNode2 = pageName2Node.get(displayName);
                if( pageNode2 != null ){
                    throw new RuntimeException("Why are there two of the same page?: " + displayName +"\n PageNode1: " + pageNode + "\n PageNode2:" + pageNode2);
                }
                putPageName2Node(displayName, pageNode);
            }
            /*
             * End Test
             */
            return pageName2Node.get(displayName);
        }
    }
    
    private Thread t = null;
    public void printThreadInfo() {
        if( !SwingUtilities.isEventDispatchThread() ){
            Thread.dumpStack();
            throw new RuntimeException("Not a Dispatched Thread");
        }
    }
    
    
    public void renamePageInModel(String oldDisplayName, String newDisplayName){
        FacesModelUtility.renamePageInModel(configModel, oldDisplayName, newDisplayName);
    }
    
    
    public void removeSceneNodeEdges(Page pageNode) {
        
        Collection<NavigationCaseEdge> navCaseNodes = view.getNodeEdges(pageNode);
        for( NavigationCaseEdge navCaseNode : navCaseNodes ){
            try         {
                navCaseNode.destroy();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            //            view.removeEdge(navCaseNode);
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
            String fromViewId = FacesModelUtility.getFromViewIdFiltered(navRule);
            if ( fromViewId != null && fromViewId.equals(displayName) ){
                //if the rule is removed, don't check the cases.
                facesConfig.removeNavigationRule(navRule);
            } else {
                List<NavigationCase> navCases = navRule.getNavigationCases();
                for( NavigationCase navCase : navCases ) {
                    //                    String toViewId = navCase.getToViewId();
                    String toViewId = FacesModelUtility.getToViewIdFiltered(navCase);
                    if ( toViewId != null && toViewId.equals(displayName) ) {
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
    
    
    public void changeToAbstractNode(Page oldNode, String displayName  ) {
        //1. Make Old Node an abstract node
        Node tmpNode = new AbstractNode(Children.LEAF);
        tmpNode.setName(displayName);
        oldNode.replaceWrappedNode(tmpNode);  //Does this take care of pageName2Node?
        view.resetNodeWidget(oldNode, true);
    }
    
    
    public DataObject getConfigDataObject() {
        return configDataObj;
    }
    
    public void saveLocation(Page node, String newDisplayName) {
        view.saveLocation(node, newDisplayName);
    }
    
    // WebFiles Wrappers
    public final boolean removeWebFile(FileObject fileObj ) {
        return webFiles.remove(fileObj);
    }
    
    public final boolean addWebFile( FileObject fileObj ) {
        return webFiles.add(fileObj);
    }
    
    public final boolean containsWebFile(FileObject fileObj){
        return webFiles.contains(fileObj);
    }
    
    //    /**
    //     * Return the file if the file name is in the webfiles collection
    //     * @param displayName
    //     * @return fileObject, returns null if nothing is found.
    //     **/
    //    public final FileObject getFileFromWebFiles( String displayName ){
    //        for( FileObject fileObject : webFiles ){
    //            String fileDisplayName = PageFlowNode.getFolderDisplayName(webFolder, fileObject);
    //            if( fileDisplayName.equals(displayName)){
    //                return fileObject;
    //            }
    //        }
    //        return null;
    //    }
    
    // case2Node Wrappers
    public final void putCase2Node(NavigationCase navCase,NavigationCaseEdge navCaseNode ){
        case2Node.put(navCase, navCaseNode);
    }
    
    public final NavigationCaseEdge removeCase2Node(NavigationCase navCase ){
        return case2Node.remove(navCase);
    }
    
    //NavRule2String wrappers
    public final String removeNavRule2String( NavigationRule navRule ) {
        return navRule2String.remove(navRule);
    }
    
    public final String putNavRule2String( NavigationRule navRule, String navRuleName ){
        return navRule2String.put(navRule, navRuleName);
    }
    
    public PageFlowView getView() {
        return view;
    }
    
    
    public void setModelNavigationCaseName( NavigationCase navCase, String newName ) {
        configModel.startTransaction();
        
        //By default check from outcome first.  Maybe this should be the expectation.
        if (navCase.getFromOutcome() != null ) {
            navCase.setFromOutcome(newName);
        }
        if( navCase.getFromAction() != null) {
            navCase.setFromAction(newName);
        }
        configModel.endTransaction();
        
        try     {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void removeModelNavigationCase( NavigationCase navCase ) throws IOException {
        configModel.startTransaction();
        NavigationRule navRule = (NavigationRule)navCase.getParent();
        if( navRule !=null && navRule.getNavigationCases().contains(navCase) ) {  //Only delete if it is still valid.
            navRule.removeNavigationCase(navCase);
            if( navRule.getNavigationCases().size() < 1 ){
                configModel.removeChildComponent(navRule);  //put this back once you remove hack
            }
        }
        configModel.endTransaction();
        configModel.sync();
    }
    

}
