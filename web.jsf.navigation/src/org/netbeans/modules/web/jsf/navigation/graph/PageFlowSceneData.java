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
import org.netbeans.modules.web.jsf.navigation.PageFlowUtilities;

/**
 *
 * @author joelle
 */
public class PageFlowSceneData {
    private PageFlowUtilities utilities;
    private PageFlowScene scene;
    
    private final Map<String,Point> facesConfigSceneData = new HashMap<String,Point>();
    private final Map<String,Point> projectSceneData = new HashMap<String,Point>();
    
    /**
     * PageFlowSceneData keeps scene data for the facesConfigScope and the projectSceneScope.
     * Because the actual Page object is not necessarily the same, it uses the display name
     * as the key.
     * @param scene PageFlowScene
     * @param utilities PageFlowUtilites
     **/
    public PageFlowSceneData(PageFlowScene scene, PageFlowUtilities utilities) {
        this.utilities = utilities;
        this.scene = scene;
    }
    
    /**
     * Saves the Scene Data for the Current Scene Scope
     **/
    public void saveCurrentSceneData( ) {
        if ( utilities.getCurrentScope().equals( PageFlowUtilities.LBL_SCOPE_FACESCONFIG) ){
                facesConfigSceneData.clear();
                facesConfigSceneData.putAll( createSceneInfo() );
        } else if( utilities.getCurrentScope().equals(PageFlowUtilities.LBL_SCOPE_PROJECT)){
                projectSceneData.clear();
                projectSceneData.putAll( createSceneInfo() );
        }
    }
    
    /**
     * Moves the Point for the oldDisplayName and to the newDisplayName
     * @param oldDisplayName String
     * @param newDisplayName String
     **/
    public void savePageWithNewName(String oldDisplayName, String newDisplayName) {
        replaceSceneInfo(facesConfigSceneData, oldDisplayName,  newDisplayName);
        replaceSceneInfo(projectSceneData, oldDisplayName,  newDisplayName);
    }
    
    /**
     * Loads the Scene Data stored for the current scope into the Scene.
     **/
    public void loadSceneData() {
        loadSceneData(getCurrentSceneData());
    }
    
    public Point getPageLocation( String pageDisplayName ){
        Map<String,Point> map = getCurrentSceneData();
        if( map != null )
            return getCurrentSceneData().get(pageDisplayName);
        return null;
    }
    
    private void loadSceneData(Map<String,Point> sceneInfo) {
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
    
    private Map<String, Point> createSceneInfo( ){
        Map<String,Point> sceneInfo = new HashMap<String,Point>();
        Collection<Page> pages =  scene.getNodes();
        for( Page page : pages ){
            sceneInfo.put(page.getDisplayName(), scene.findWidget(page).getPreferredLocation());
        }
        return sceneInfo;
    }
    
    public String getCurrentScope() {
        return utilities.getCurrentScope();
    }
    
    
    public void setScopeData( String scope, Map<String,Point> map){
        if ( scope.equals( PageFlowUtilities.LBL_SCOPE_FACESCONFIG) ){
            facesConfigSceneData.clear();
            facesConfigSceneData.putAll(map);
        } else if( scope.equals(PageFlowUtilities.LBL_SCOPE_PROJECT)){
            projectSceneData.clear();
            projectSceneData.putAll(map);
        }
    }
    
    public Map<String,Point> getScopeData( String scope ) {
        Map<String,Point> sceneInfo = null;
        if ( scope.equals( PageFlowUtilities.LBL_SCOPE_FACESCONFIG) ){
            sceneInfo = facesConfigSceneData;
        } else if( scope.equals(PageFlowUtilities.LBL_SCOPE_PROJECT)){
            sceneInfo = projectSceneData;
        }
        return sceneInfo;
    }
    
    private Map<String,Point> getCurrentSceneData() {
        Map<String,Point> sceneInfo = null;
        if ( utilities.getCurrentScope().equals( PageFlowUtilities.LBL_SCOPE_FACESCONFIG) ){
            sceneInfo = facesConfigSceneData;
        } else if( utilities.getCurrentScope().equals(PageFlowUtilities.LBL_SCOPE_PROJECT)){
            sceneInfo = projectSceneData;
        }
        return sceneInfo;
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
