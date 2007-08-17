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

package org.netbeans.modules.websvc.design.view;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author Ajit Bhate
 * Determines the focus traversal policy or the web service designer.
 * Works on depth first search algorithm.
 * 
 */
public class DesignerWidgetIdentityCode implements Comparable<DesignerWidgetIdentityCode> {
    
    private Object object;
    private ObjectScene scene;

    public DesignerWidgetIdentityCode(ObjectScene scene, Object object) {
        this.scene = scene;
        this.object = object;
    }

    /**
     * @see Comparable.compareTo(T o)
     * compareTo works on depth first algorithm
     * So when compared to another DesignerWidgetIdentityCode, 
     * it returns the difference in the indices of widgets represented by the two objects.
     * @return the result of comparison
     * @param DesignerWidgetIdentityCode object to be compared.
     */
    public int compareTo(DesignerWidgetIdentityCode o) {
        return getIdentityCode(scene,object) - getIdentityCode(o.scene, o.object);
    }
    
    /**
     * Returns the index of visible widget represented by given object using depth first search.
     * Integer.MAX_VALUE if none found.
     */ 
    private static int getIdentityCode(ObjectScene scene, Object object) {
        List<Widget> widgets = scene.findWidgets(object);
        Widget w = null;
        ArrayList<Widget> pathToRoot = new ArrayList<Widget>();
        if(widgets!=null) {
            for(Widget w1:widgets) {
                pathToRoot = new ArrayList<Widget>();
                if(!w1.isVisible()) continue;
                Widget parent = w1.getParentWidget();
                while(parent!=null && parent.isVisible()) {
                    pathToRoot.add(0,parent);
                    parent = parent.getParentWidget();
                }
                if(!pathToRoot.isEmpty()&&pathToRoot.get(0)==scene) {
                    w = w1;
                    pathToRoot.add(w);
                    break;
                }
            }
        }
        if(w==null) 
            return Integer.MAX_VALUE;
        int code = 0;
        for(int i=0;i<pathToRoot.size();) {
            Widget widgetOnPath=pathToRoot.get(i++);
            code++;
            if(i == pathToRoot.size()) break;
            int nextWidgetOnPathIndex = widgetOnPath.getChildren().indexOf(pathToRoot.get(i));
            for(int j=0;j<nextWidgetOnPathIndex;j++) {
                code+=getTreeSize(widgetOnPath.getChildren().get(j));
            }
        }
        return code;
    }
    
    private static int getTreeSize(Widget w) {
        if(!w.isVisible()) return 0;
        int size = 1;
        for(Widget ch:w.getChildren()) {
            size+= getTreeSize(ch);
        }
        return size;
    }
}
