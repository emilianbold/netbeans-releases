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
        webFiles = getAllRelevantFiles();
        setupGraph();
        
        
    }
    
    
    private Collection<FileObject> getAllRelevantFiles() {
        FileObject parentFolder = project.getProjectDirectory();
        FileObject webFileObject = parentFolder.getFileObject("web");
        Collection<FileObject> webFiles = getAllJSPFiles(webFileObject);
        System.out.println("Web Files: " + webFiles);
        return webFiles;
        
        //Add a listener to the Filesystem that listens to fileDelete, fileCreated, etc.
        //DataObject.find
        //        DataObject.find(parentFolder)
        
    }
    
    
    
    private Collection<FileObject> getAllJSPFiles(FileObject folder ) {
        Collection<FileObject> webFiles = new HashSet<FileObject>();
        FileObject[] childrenFiles = folder.getChildren();
        for( FileObject file : childrenFiles ){
            if( !file.isFolder() ) {
                if( file.getMIMEType().equals("text/x-jsp"))
                    webFiles.add(file);
            } else {
                webFiles.addAll(getAllJSPFiles(file));
            }
        }
        
        return webFiles;
    }
    
     /*
      * Setup The Graph
      * Should only be called by init();
      **/
    private void setupGraph(){
        assert configModel !=null;
        
        FacesConfig facesConfig = configModel.getRootComponent();
        
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        createAllPageNodes(rules);
        createAllEdges(rules);
        view.layoutGraph();
        
    }
    
    private void createAllEdges( List<NavigationRule> rules ){
        for( NavigationRule rule : rules ) {
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                view.createEdge(rule, navCase);
            }
        }
    }
    
    private void createAllPageNodes(List<NavigationRule> rules) {
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
        for( String pageName : pages ) {
            boolean isFound = false;
            for( FileObject webFile : webFiles ) {
                String webFileName = webFile.getNameExt();
//                String webFileName = webFile.getName() + "." + webFile.getExt();
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
