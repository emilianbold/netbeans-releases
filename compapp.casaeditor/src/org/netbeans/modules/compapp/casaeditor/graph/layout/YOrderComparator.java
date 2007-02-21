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

package org.netbeans.modules.compapp.casaeditor.graph.layout;

import java.util.Comparator;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;

/**
 *
 * @author Josh Sandusky
 */
public class YOrderComparator implements Comparator<CasaNodeWidget> {
    
    private CasaModelGraphScene mScene;
    
    
    public YOrderComparator(CasaModelGraphScene scene) {
        mScene = scene;
    }
    
    
    public int compare(CasaNodeWidget w1, CasaNodeWidget w2) {
        if (mScene.findObject(w1) instanceof CasaPort) {    // TMP
            CasaPort port1 = (CasaPort) mScene.findObject(w1);
            CasaPort port2 = (CasaPort) mScene.findObject(w2);
            int y1 = w1.getLocation().y;
            int y2 = w2.getLocation().y;
            if (y1 == y2) {
                int order1 = getOrderNumber(port1);
                int order2 = getOrderNumber(port2);
                return order1 - order2;
            } else if (y1 < y2) {
                return -1;
            } else {
                return 1;
            }
        } else if (mScene.findObject(w1) instanceof CasaServiceEngineServiceUnit) {
            CasaServiceEngineServiceUnit su1 = (CasaServiceEngineServiceUnit) mScene.findObject(w1);
            CasaServiceEngineServiceUnit su2 = (CasaServiceEngineServiceUnit) mScene.findObject(w2);
            int y1 = w1.getLocation().y;
            int y2 = w2.getLocation().y;
            return y1 - y2;
//            if (y1 == y2) {
//                int order1 = getOrderNumber(su1);
//                int order2 = getOrderNumber(su2);
//                return order1 - order2;
//                return 0;
//            } else if (y1 < y2) {
//                return -1;
//            } else {
//                return 1;
//            }
            
        } else {
            return -1;
        }
    }
    
    public boolean equals(Object object) {
        return this == object;
    }
    
    private int getOrderNumber(CasaPort port) {
        if (port.getConsumes() != null && port.getProvides() == null) {
            return 1;
        } else if (port.getConsumes() != null && port.getProvides() != null) {
            return 2;
        } else if (port.getConsumes() == null && port.getProvides() != null) {
            return 3;
        } else {
            return -1;
        }
    }
}
