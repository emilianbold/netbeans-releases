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
import javax.swing.SwingUtilities;
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
    private final HashMap<String,PageFlowNode> pageName2Node = new HashMap<String,PageFlowNode>();  //Should this be synchronized.
    
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
            pcl = new FacesModelPropertyChangeListener(view);
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
    public NavigationCase createLink(PageFlowNode source, PageFlowNode target, PinNode pinNode) {
        
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
        String caseName = CASE_STRING + Integer.toString(caseNum);
        
        if( pinNode != null ){
            pinNode.setFromOutcome(caseName);
        }
        navCase.setFromOutcome(caseName);
        
        
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
    
    public void updatePageItems( PageFlowNode pageNode ) {
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
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        
        for( NavigationRule rule : rules ){
            String rulefromViewId = rule.getFromViewId();
            if( rulefromViewId != null && rulefromViewId.equals(fromViewId) ){
                //  Match Found
                return rule;
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
        //        pageName2Node.clear();
        clearPageName2Node();
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
        String fromPage = caseNode.getFromViewId();
        
        if( toPage != null && fromPage != null ) {
            //            assert pageName2Node.get(fromPage) != null;
            //            assert pageName2Node.get(toPage) != null;
            assert getPageName2Node(fromPage) != null;
            assert getPageName2Node(toPage) != null;
            
            view.createEdge(caseNode, getPageName2Node(fromPage), getPageName2Node(toPage));
        }
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
                if( toPage != null ) {
                    pages.add(toPage);
                }
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
            try {
                //DISPLAYNAME:
                String webFileName = PageFlowNode.getFolderDisplayName(getWebFolder(), webFile);
                PageFlowNode node = null;
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
                //                PageFlowNode node = new PageFlowNode(this,tmpNode);
                PageFlowNode node = createPageFlowNode(tmpNode);
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
                //                PageFlowNode node = new PageFlowNode(this, wrapNode);
                PageFlowNode node = createPageFlowNode(wrapNode);
                view.createNode(node, null, null);
            }
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
                    if( toPage != null ) {
                        PageFlowNode pageNode = pageName2Node.get(toPage);
                        if( pageNode != null && !isPageInFacesConfig(toPage)){
                            if( !pageNode.isDataNode() || PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_FACESCONFIG){
                                removePageName2Node(pageNode);
                                view.removeNodeWithEdges(pageNode);
                                view.validateGraph();
                                //                                node.destroy(); //only okay because it is an abstract node.
                            }
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
                    
                    if( fromPage != null ){
                        PageFlowNode pageNode = pageName2Node.get(fromPage);
                        if( pageNode != null && !isPageInFacesConfig(fromPage)){
                            if( !pageNode.isDataNode() || PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_FACESCONFIG){
                                removePageName2Node(pageNode);
                                view.removeNodeWithEdges(pageNode);
                                view.validateGraph();
                                //                                node.destroy(); //only okay because it is an abstract node.
                            }
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
                //                System.out.println("Did not catch this event.: " + ev.getPropertyName());
                setupGraph();
            }
        }
    }
    
    public void removePageName2Node(PageFlowNode pageNode ){
        printThreadInfo();
        synchronized ( pageName2Node ) {
            pageName2Node.remove(pageNode);
        }
    }
    public void removePageName2Node( String displayName ) {
        PageFlowNode node = getPageName2Node(displayName);
        if (node != null ){
            removePageName2Node(node);
        }
    }
    
    public void clearPageName2Node(){
        printThreadInfo();
        synchronized ( pageName2Node ) {
            pageName2Node.clear();
        }
    }
    
    public void putPageName2Node(String displayName, PageFlowNode pageNode){
        printThreadInfo();
        if( pageNode == null ){
            throw new RuntimeException("PageFlowEditor: Trying to add Page [" + displayName + "] but it is null.");
        }
        synchronized ( pageName2Node ) {
            pageName2Node.put(displayName, pageNode);
        }
    }
    
    public PageFlowNode getPageName2Node(String displayName){
        printThreadInfo();
        synchronized ( pageName2Node ) {
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
            String fromViewId = navRule.getFromViewId();
            if ( fromViewId != null && fromViewId.equals(oldDisplayName) ){
                navRule.setFromViewId(newDisplayName);
            }
            List<NavigationCase> navCases = navRule.getNavigationCases();
            for( NavigationCase navCase : navCases ) {
                String toViewId = navCase.getToViewId();
                if ( toViewId != null && toViewId.equals(oldDisplayName) ) {
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
            String fromViewId = navRule.getFromViewId();
            if ( fromViewId != null && fromViewId.equals(displayName) ){
                //if the rule is removed, don't check the cases.
                facesConfig.removeNavigationRule(navRule);
            } else {
                List<NavigationCase> navCases = navRule.getNavigationCases();
                for( NavigationCase navCase : navCases ) {
                    String toViewId = navCase.getToViewId();
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
    
    
    public void changeToAbstractNode(PageFlowNode oldNode, String displayName ) {
        //1. Make Old Node an abstract node
        Node tmpNode = new AbstractNode(Children.LEAF);
        tmpNode.setName(displayName);
        oldNode.replaceWrappedNode(tmpNode);  //Does this take care of pageName2Node?
        view.resetNodeWidget(oldNode, false);
    }
    
    
    public DataObject getConfigDataObject() {
        return configDataObj;
    }
    
    public void saveLocation(PageFlowNode node, String newDisplayName) {
        view.saveLocation(node, newDisplayName);
    }
    
    public final boolean removeWebFile(FileObject fileObj ) {
        return webFiles.remove(fileObj);
    }
    
    public final boolean addWebFile( FileObject fileObj ) {
        return webFiles.add(fileObj);
    }
    
    public final boolean containsWebFile(FileObject fileObj){
        return webFiles.contains(fileObj);
    }
    
    public PageFlowView getView() {
        return view;
    }
}
