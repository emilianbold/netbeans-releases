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
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseNode;

/**
 *
 * @author joelle lam
 */
public class PageFlowController {
    private PageFlowView view;
    private JSFConfigModel configModel;
    //    private Project project;
    private FileObject webFolder;
    private Collection<FileObject> webFiles;
    
    private HashMap<NavigationCase,NavigationCaseNode> case2Node = new HashMap<NavigationCase,NavigationCaseNode>();
    private  HashMap<String,PageFlowNode> pageName2Node = new HashMap<String,PageFlowNode>();
    
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18NF
    
    /** Creates a new instance of PageFlowController
     * @param context
     * @param view
     */
    public PageFlowController(JSFConfigEditorContext context, PageFlowView view ) {
        this.view = view;
        FileObject configFile = context.getFacesConfigFile();
        configModel = ConfigurationUtils.getConfigModel(configFile,true);
        Project project = FileOwnerQuery.getOwner(configFile);
        webFolder = project.getProjectDirectory().getFileObject(DEFAULT_DOC_BASE_FOLDER);
        webFiles = getAllProjectRelevantFilesObjects();
        
        setupGraph();
        view.layoutSceneImmediately();
        
        
        //        configModel.addComponentListener(new ComponentListener(){
        //            public void valueChanged(ComponentEvent evt) {
        //                //                System.out.println("ValueChanged: " + evt);
        //            }
        //
        //            public void childrenAdded(ComponentEvent evt) {
        //                //                System.out.println("childrenAdded: " + evt);
        //            }
        //
        //            public void childrenDeleted(ComponentEvent evt) {
        //                //                System.out.println("\n\n\n\n\n\nchildrenDeleted: " + evt);
        //            }
        //        });
    }
    
    PropertyChangeListener pcl;
    
    public void registerListeners() {
        if( pcl == null ) {
            pcl = new FacesModelPropertyChangeListener(view);
            if( configModel != null ) {
                configModel.addPropertyChangeListener(pcl);
            }
        }
        
    }
    
    public void unregisterListeners() {
        if ( pcl != null && configModel != null )
            configModel.removePropertyChangeListener(pcl);
    }
    
    /**
     * Set From outcome by default.
     * @param source
     * @param target
     * @param comp
     * @return
     */
    public NavigationCase createLink(Node source, Node target, String comp) {
        
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
        Collection<FileObject> webFiles = getProjectJSPFileOjbects(webFolder);
        //        System.out.println("Web Files: " + webFiles);
        return webFiles;
        
        //Add a listener to the Filesystem that listens to fileDelete, fileCreated, etc.
        //DataObject.find
        //        DataObject.find(parentFolder)
        
    }
    
    
    private Collection<FileObject> getProjectJSPFileOjbects(FileObject folder ) {
        Collection<FileObject> webFiles = new HashSet<FileObject>();
        FileObject[] childrenFiles = folder.getChildren();
        for( FileObject file : childrenFiles ){
            if( !file.isFolder() ) {
                if( file.getMIMEType().equals("text/x-jsp"))
                    webFiles.add(file);
            } else {
                webFiles.addAll(getProjectJSPFileOjbects(file));
            }
        }
        
        return webFiles;
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
        
        view.clearGraph();
        
        FacesConfig facesConfig = configModel.getRootComponent();
        
        if( facesConfig == null ) {
            return false;
        }
        
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        String currentScope = PageFlowUtilities.getInstance().getCurrentScope();
        Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
        if (currentScope.equals(PageFlowUtilities.LBL_SCOPE_FACESCONFIG)){
            createFacesConfigPageNodes(pagesInConfig);
        } else if (currentScope.equals(PageFlowUtilities.LBL_SCOPE_PROJECT)) {
            createAllProjectPageNodes(pagesInConfig);
        }
        createAllEdges(rules);
        //        view.layoutGraph();
        
        view.layoutSceneImmediately();
        //        view.validate();
        return true;
        
    }
    
    private void createAllEdges( List<NavigationRule> rules ){
        for( NavigationRule rule : rules ) {
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                NavigationCaseNode node = new NavigationCaseNode(navCase);
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
    
    private void createAllProjectPageNodes(Collection<String> pagesInConfig) {
        
        
        Collection<String> pages = new HashSet<String>(pagesInConfig);
        
        //Create all pages in the project...
        for( FileObject webFile : webFiles ) {
            String webFileName = webFile.getNameExt();
            pages.remove(webFileName);
            PageFlowNode node = null;
            try {
                //                                node = (DataNode)(DataObject.find(webFile)).getNodeDelegate();
                node = new PageFlowNode((DataObject.find(webFile)).getNodeDelegate());
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
            PageFlowNode node = new PageFlowNode(tmpNode);
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
            String webFileName = webFile.getNameExt();
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
            PageFlowNode node = new PageFlowNode(wrapNode);
            view.createNode(node, null, null);
        }
    }
    
    
    private  class FacesModelPropertyChangeListener implements PropertyChangeListener {
        public FacesModelPropertyChangeListener( PageFlowView view ){
            
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            if( ev.getOldValue() == State.NOT_WELL_FORMED ){
                view.removeUserMalFormedFacesConfig();
            }
            
            if ( ev.getPropertyName() == "navigation-case"){
                
                NavigationCase myNavCase = (NavigationCase)ev.getNewValue();
                if( myNavCase != null ){
                    NavigationCaseNode node = new NavigationCaseNode(myNavCase);
                    case2Node.put(myNavCase, node);
                    createEdge(node);
                } else {
                    //                    NavigationCaseNode node = case2Node.get((NavigationCase)ev.getOldValue());
                    NavigationCaseNode node = case2Node.remove((NavigationCase)ev.getOldValue());
                    view.removeEdge(node);
                }
                view.validate();
            } else if (ev.getPropertyName() == "navigation-rule" ) {
                NavigationRule myNavRule = (NavigationRule)ev.getNewValue();
                //You can actually do nothing.
            } else if ( ev.getNewValue() == State.NOT_SYNCED ) {
                // Do nothing.
                //            } else if ( ev.getPropertyName("to-view-id")) {
                //                String newToView = (String)ev.getNewValue();
                //                String oldToView = (String)ev.getOldValue();
                //                NavigationCase navCase = (NavigationCase)ev.getSource();
                //                     } else if ( ev.getPropertyName("from-view-id")) {
                //                String newFromView = (String)ev.getNewValue();
                //                String oldFromView = (String)ev.getOldValue();
                //                NavigationRule navRule = (NavigationRule)ev.getSource();
                //
            }else if (ev.getNewValue() == State.NOT_WELL_FORMED ){
                view.warnUserMalFormedFacesConfig();
                //                System.out.println("NOT WELL FORMED!!!");
            } else {
                if ( !setupGraph() ){
                    System.out.println("Something is wrong.  Why did setup not work?");
                }
            }
            //            System.out.println("New Value: " + ev.getNewValue());
            //            System.out.println("Old Value: " + ev.getOldValue());
            //
            //            System.out.println("PropertyName: " + ev.getPropertyName());
            //            System.out.println("ID: " + ev.getPropagationId());
            //            System.out.println("PropertyChangeListener");
            
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
     * Gets the WebFolder which contains the jsp pages.
     * @return FileObject webfolder
     */
    public FileObject getWebFolder() {
        return webFolder;
    }
    
    
    /**
     * A Filter Node for a given DataNode or non File Node.
     */
    public final class PageFlowNode extends FilterNode {
        
        /**
         *
         * @param original
         */
        public PageFlowNode( Node original ){
            super(original, Children.LEAF);
            pageName2Node.put(original.getDisplayName(), this);
        }
        
        @Override
        public void setName(String s) {
            
            String oldDisplayName = getDisplayName();
            try {
                super.setName(s);
                pageName2Node.remove(oldDisplayName);
                pageName2Node.put(getDisplayName(), this);
                renamePageInModel(oldDisplayName, getDisplayName());
            } catch (IllegalArgumentException iae ) {
                iae.printStackTrace();
                //                throw iae;
            }
            
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
            return false;
        }
        
        @Override
        public void destroy() throws IOException {
            pageName2Node.remove(getDisplayName());
            super.destroy();
        }
        
        
        
        
        
    }
    
    
    
    
}
