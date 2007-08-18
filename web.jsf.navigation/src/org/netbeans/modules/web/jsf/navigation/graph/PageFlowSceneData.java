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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.jsf.navigation.graph;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities;
import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities.Scope;

/**
 *
 * @author joelle
 */
public class PageFlowSceneData {
    private final PageFlowToolbarUtilities utilities;
    //    private PageFlowScene scene;
    
    private final Map<String,Point> facesConfigSceneData = new HashMap<String,Point>();
    private final Map<String,Point> projectSceneData = new HashMap<String,Point>();
    private final Map<String,Point> allFacesConfigSceneData = new HashMap<String,Point>();
    
    /**
     * PageFlowSceneData keeps scene data for the facesConfigScope and the projectSceneScope.
     * Because the actual Page object is not necessarily the same, it uses the display name
     * as the key.
     * @param scene PageFlowScene
     * @param utilities PageFlowUtilites
     **/
    public PageFlowSceneData(PageFlowToolbarUtilities utilities) {
        this.utilities = utilities;
        //        this.scene = scene;
    }
    
    /**
     * Saves the Scene Data for the Current Scene Scope
     **/
    public void saveCurrentSceneData( PageFlowScene scene ) {
        switch( utilities.getCurrentScope()){
        case SCOPE_FACESCONFIG:
            facesConfigSceneData.clear();
            facesConfigSceneData.putAll( createSceneInfo(scene ) );
            break;
        case SCOPE_PROJECT:
            projectSceneData.clear();
            projectSceneData.putAll( createSceneInfo(scene) );
            break;
        case SCOPE_ALL_FACESCONFIG:
            allFacesConfigSceneData.clear();
            allFacesConfigSceneData.putAll(createSceneInfo(scene));
            break;
        default:
            System.out.println("PageFlowSceneData: Unknown State");
            
        }
        //
        //        if ( utilities.getCurrentScope().equals( PageFlowUtilities.Scope.SCOPE_FACESCONFIG) ){
        //            facesConfigSceneData.clear();
        //            facesConfigSceneData.putAll( createSceneInfo(scene ) );
        //        } else if( utilities.getCurrentScope().equals( PageFlowUtilities.Scope.SCOPE_PROJECT)){
        //            projectSceneData.clear();
        //            projectSceneData.putAll( createSceneInfo(scene) );
        //        }
    }
    
    /**
     * Moves the Point for the oldDisplayName and to the newDisplayName
     * @param oldDisplayName String
     * @param newDisplayName String
     **/
    public void savePageWithNewName(String oldDisplayName, String newDisplayName) {
        replaceSceneInfo(facesConfigSceneData, oldDisplayName,  newDisplayName);
        replaceSceneInfo(projectSceneData, oldDisplayName,  newDisplayName);
        replaceSceneInfo(allFacesConfigSceneData, oldDisplayName,  newDisplayName);
    }
    
    /**
     * Loads the Scene Data stored for the current scope into the Scene.
     **/
    public void loadSceneData(PageFlowScene scene) {
        loadSceneData(scene, getCurrentSceneData());
    }
    
    public Point getPageLocation( String pageDisplayName ){
        Map<String,Point> map = getCurrentSceneData();
        if( map != null ) {
            return getCurrentSceneData().get(pageDisplayName);
        }
        return null;
    }
    
    private void loadSceneData(PageFlowScene scene, Map<String,Point> sceneInfo) {
        if( sceneInfo == null ){
            return;
        }
        Collection<Page> pages = scene.getNodes();
        for ( Page page : pages ){
            Point point = sceneInfo.get(page.getDisplayName());
            if (point != null ) {
                scene.findWidget(page).setPreferredLocation(point);
            }
        }
    }
    
    private Map<String, Point> createSceneInfo(PageFlowScene scene ){
        Map<String,Point> sceneInfo = new HashMap<String,Point>();
        Collection<Page> pages =  scene.getNodes();
        for( Page page : pages ){
            if( scene.isValidated() ) {
                sceneInfo.put(page.getDisplayName(), scene.findWidget(page).getLocation());
            } else {
                sceneInfo.put(page.getDisplayName(), scene.findWidget(page).getPreferredLocation());
            }
        }
        return sceneInfo;
    }
    
    public String getCurrentScopeStr() {
        return PageFlowToolbarUtilities.getScopeLabel(utilities.getCurrentScope());
    }
    
    public void setCurrentScope(Scope newScope) {
        utilities.setCurrentScope(newScope);
    }
    
    
    public void setScopeData( String scope, Map<String,Point> map){
        switch(PageFlowToolbarUtilities.getScope(scope)){
                case SCOPE_FACESCONFIG:
            facesConfigSceneData.clear();
            facesConfigSceneData.putAll(map);
            break;
        case SCOPE_PROJECT:
            projectSceneData.clear();
            projectSceneData.putAll(map);
            break;
        case SCOPE_ALL_FACESCONFIG:
            allFacesConfigSceneData.clear();
            allFacesConfigSceneData.putAll(map);
            break;
        }
        //        if ( scope.equals( PageFlowUtilities.getScopeLabel(PageFlowUtilities.Scope.SCOPE_FACESCONFIG) ) ){
        //            facesConfigSceneData.clear();
        //            facesConfigSceneData.putAll(map);
        //        } else if( scope.equals(PageFlowUtilities.getScopeLabel(PageFlowUtilities.Scope.SCOPE_PROJECT))){
        //            projectSceneData.clear();
        //            projectSceneData.putAll(map);
        //        }
    }
    
    public Map<String,Point> getScopeData( String scopeStr ) {
        Map<String,Point> sceneInfo = null;
        PageFlowToolbarUtilities.Scope scope = PageFlowToolbarUtilities.getScope(scopeStr);
        switch( scope ){
        case SCOPE_FACESCONFIG:
            sceneInfo = facesConfigSceneData;
            break;
        case SCOPE_PROJECT:
            sceneInfo = projectSceneData;
            break;
        case SCOPE_ALL_FACESCONFIG:
            sceneInfo = allFacesConfigSceneData;
        }
        //        if ( scope.equals( PageFlowUtilities.LBL_SCOPE_FACESCONFIG) ){
        //            sceneInfo = facesConfigSceneData;
        //        } else if( scope.equals(PageFlowUtilities.LBL_SCOPE_PROJECT)){
        //            sceneInfo = projectSceneData;
        //        }
        return sceneInfo;
    }
    
    private Map<String,Point> getCurrentSceneData() {
        Map<String,Point> sceneInfo = null;
        switch( utilities.getCurrentScope()){
        case SCOPE_FACESCONFIG:
            return facesConfigSceneData;
        case SCOPE_PROJECT:
            return projectSceneData;
        case SCOPE_ALL_FACESCONFIG:
            return allFacesConfigSceneData;
        default:
            return null;
        }
        //        if ( utilities.getCurrentScope().equals( PageFlowUtilities.LBL_SCOPE_FACESCONFIG) ){
        //            sceneInfo = facesConfigSceneData;
        //        } else if( utilities.getCurrentScope().equals(PageFlowUtilities.LBL_SCOPE_PROJECT)){
        //            sceneInfo = projectSceneData;
        //        }
        //        return sceneInfo;
    }
    
    private void replaceSceneInfo(Map<String,Point> sceneInfo, String oldDisplayName, String newDisplayName) {
        assert oldDisplayName != null;
        assert newDisplayName != null;
        
        if( sceneInfo == null || sceneInfo.size() < 1){
            return;
        }
        
        Point p = sceneInfo.remove(oldDisplayName);
        if ( p != null ) {
            sceneInfo.put(newDisplayName, p);
        }
    }
    
}
