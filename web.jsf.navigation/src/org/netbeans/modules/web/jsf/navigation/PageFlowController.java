/*
 * PageFlowController.java
 *
 * Created on March 1, 2007, 1:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.util.Collection;
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
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author joelle lam
 */
public class PageFlowController {
    private PageFlowView view;
    private JSFConfigModel configModel;
    private Project project;
    private Collection<FileObject> webFiles;
    
    /** Creates a new instance of PageFlowController
     * @param context
     * @param view
     */
    public PageFlowController(JSFConfigEditorContext context, PageFlowView view ) {
        this.view = view;
        FileObject configFile = context.getFacesConfigFile();
        configModel = ConfigurationUtils.getConfigModel(configFile,true);
        project = FileOwnerQuery.getOwner(configFile);
        webFiles = getAllProjectRelevantFilesObjects();
        setupGraph();
        
    }
    
    
    private Collection<FileObject> getAllProjectRelevantFilesObjects() {
        FileObject parentFolder = project.getProjectDirectory();
        FileObject webFileObject = parentFolder.getFileObject("web");
        Collection<FileObject> webFiles = getProjectJSPFileOjbects(webFileObject);
        System.out.println("Web Files: " + webFiles);
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
    public void setupGraph(){
        assert configModel !=null;
        assert project != null;
        assert webFiles != null;
        
        view.clearGraph();
        
        FacesConfig facesConfig = configModel.getRootComponent();
        
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
        
        view.validateGraph();
        
    }
    
    private void createAllEdges( List<NavigationRule> rules ){
        for( NavigationRule rule : rules ) {
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                view.createEdge(rule, navCase);
            }
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
                pages.add(toPage);
            }
        }
        return pages;
    }
    
    private void createAllProjectPageNodes(Collection<String> pagesInConfig) {
        
        
        Collection<String> pages = pagesInConfig;
        
        //Create all pages in the project...
        for( FileObject webFile : webFiles ) {
            String webFileName = webFile.getNameExt();
            pages.remove(webFileName);
            DataNode node = null;
            try {
                node = (DataNode)(DataObject.find(webFile)).getNodeDelegate();
            } catch ( DataObjectNotFoundException ex ) {
                ex.printStackTrace();
            } catch( ClassCastException cce ){
                cce.printStackTrace();
            }
            view.createNode(node, null, null);
        }
        
        //Create any pages that don't actually exist but are defined specified by the config file.
        for( String pageName : pages ){
            AbstractNode node = new AbstractNode(Children.LEAF);
            node.setName(pageName);
            view.createNode(node, null, null);
        }
    }
    
    private void createFacesConfigPageNodes(Collection<String> pagesInConfig) {
        Collection<String> pages = pagesInConfig;
        
        for( String pageName : pages ) {
            boolean isFound = false;
            for( FileObject webFile : webFiles ) {
                String webFileName = webFile.getNameExt();
                if( webFileName.equals(pageName)) {
                    DataNode node = null;
                    try {
                        node = (DataNode)(DataObject.find(webFile)).getNodeDelegate();
                    } catch ( DataObjectNotFoundException ex ) {
                        ex.printStackTrace();
                    } catch( ClassCastException cce ){
                        cce.printStackTrace();
                    }
                    view.createNode(node, null, null);
                    isFound = true;
                }
            }
            if( !isFound ) {
                AbstractNode node = new AbstractNode(Children.LEAF);
                node.setName(pageName);
                view.createNode(node, null, null);
            }
            isFound = false;
        }
    }
    
    
    
    
    
}
