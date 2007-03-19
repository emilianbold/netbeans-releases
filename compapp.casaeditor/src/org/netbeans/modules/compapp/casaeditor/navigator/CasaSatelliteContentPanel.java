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

package org.netbeans.modules.compapp.casaeditor.navigator;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.visual.widget.SatelliteComponent;

/**
 *
 * @author Josh Sandusky
 */
public class CasaSatelliteContentPanel extends JPanel {
    
    private SatelliteTracker mTracker = new SatelliteTracker();
    
    
    public CasaSatelliteContentPanel() {
        setLayout(new BorderLayout());
    }
    

    public void navigate(CasaDataObject dataObject) {
        if (dataObject == null && mTracker.hasSatelliteComponent()) {
            removeAll();
        }
        if (dataObject == null) {
            return;
        }
        
        Scene scene = dataObject.getEditorSupport().getScene();
        
        if (scene != null) {
            boolean isSceneChanged = false;
            if (!mTracker.isSameScene(scene)) {
                isSceneChanged = true;
                removeAll();
            }
            if (
                    !mTracker.hasSatelliteComponent() ||
                    isSceneChanged) {
                SatelliteComponent satellite = mTracker.newSatelliteComponent(scene);
                add(satellite, BorderLayout.CENTER);
                revalidate();
            }
        }
    }
    
    
    private static class SatelliteTracker {
        private Scene mScene;
        private SatelliteComponent mSatelliteComponent;
        public boolean isSameScene(Scene scene) {
            return mScene != null && mScene == scene;
        }
        public boolean hasSatelliteComponent() {
            return mSatelliteComponent != null;
        }
        public SatelliteComponent newSatelliteComponent(Scene scene) {
            mScene = scene;
            mSatelliteComponent = new SatelliteComponent(scene);
            return mSatelliteComponent;
        }
    }
}
