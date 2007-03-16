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

package org.netbeans.modules.compapp.casaeditor.graph;

import org.netbeans.api.visual.widget.Scene.SceneListener;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.openide.util.NbBundle;

/**
 *
 * @author Josh Sandusky
 */
public class BannerSceneListener implements SceneListener{
    
    private static String STR_BC_BANNER          = "STR_START_HINT_BC";
    private static String STR_SU_INTERNAL_BANNER = "STR_START_HINT_SU_INTERNAL";
    private static String STR_SU_EXTERNAL_BANNER = "STR_START_HINT_SU_EXTERNAL";
    
    
    private CasaModelGraphScene mScene;
    private boolean mIsIgnore;
    
    
    public BannerSceneListener(CasaModelGraphScene scene) {
        mScene = scene;
    }
    
    
    public void sceneRepaint() {
    }
    
    public void sceneValidating() {
    }
    
    public void sceneValidated() {
        updateBanner(mScene.getBindingRegion(),  STR_BC_BANNER);
        updateBanner(mScene.getEngineRegion(),   STR_SU_INTERNAL_BANNER);
        updateBanner(mScene.getExternalRegion(), STR_SU_EXTERNAL_BANNER);
    }
    
    
    private void updateBanner(CasaRegionWidget regionWidget, String bannerKey) {
        if (
                mIsIgnore ||
                regionWidget == null ||
                regionWidget.getParentWidget() == null ||
                regionWidget.getBounds() == null) {
            return;
        }
        boolean isEmpty = mScene.getNodes().size() == 0;
        if (isEmpty && !regionWidget.hasBanner()) {
            try {
                mIsIgnore = true;
                regionWidget.setBanner(NbBundle.getMessage(getClass(), bannerKey));
            } finally {
                mIsIgnore = false;
            }
        } else if (!isEmpty && regionWidget.hasBanner()) {
            try {
                mIsIgnore = true;
                regionWidget.setBanner(null);
            } finally {
                mIsIgnore = false;
            }
        }
    }
}
