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
package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.Point;
import org.netbeans.modules.compapp.casaeditor.api.PluginDropHandler;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;

/**
 *
 * @author Josh Sandusky
 */
public class DefaultPluginDropHandler implements PluginDropHandler {

    private CasaModelGraphScene mScene;
    private Point mSceneLocation;
    
    
    public DefaultPluginDropHandler(CasaModelGraphScene scene, Point sceneLocation) {
        mScene = scene;
        mSceneLocation = sceneLocation;
    }

    
    private Point getDropLocationForWSDLEndpointRegion() {
        return mScene.getBindingRegion().convertSceneToLocal(mSceneLocation);
    }
    
    private Point getDropLocationForJBIModuleRegion() {
        return mScene.getEngineRegion().convertSceneToLocal(mSceneLocation);
    }
    
    private Point getDropLocationForExternalRegion() {
        return mScene.getExternalRegion().convertSceneToLocal(mSceneLocation);
    }
    
    public void addCasaPort(String name, String type) {
        Point regionLoc = getDropLocationForWSDLEndpointRegion();
        mScene.getModel().addCasaPort(name, type, regionLoc.x, regionLoc.y);
    }
    
    public void addServiceEngineServiceUnit(boolean isInternal) {
        Point regionLoc = isInternal ?
            getDropLocationForJBIModuleRegion() :
            getDropLocationForExternalRegion();
        mScene.getModel().addServiceEngineServiceUnit(isInternal, regionLoc.x, regionLoc.y);
    }
}
