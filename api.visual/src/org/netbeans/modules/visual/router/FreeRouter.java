/*
 * FreeRouter.java
 *
 * Created on August 28, 2006, 6:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visual.router;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author alex
 */
public class FreeRouter implements Router {
    
    public FreeRouter() {
    }
    
    public List<Point> routeConnection(ConnectionWidget widget) {
        ArrayList<Point> list = new ArrayList<Point> ();
        
        Anchor sourceAnchor = widget.getSourceAnchor();
        Anchor targetAnchor = widget.getTargetAnchor();
        if (sourceAnchor == null  ||  targetAnchor == null)
            return Collections.emptyList();

        list.add(sourceAnchor.compute(widget.getSourceAnchorEntry()).getAnchorSceneLocation());

        List<Point> oldControlPoints = widget.getControlPoints ();
        if(oldControlPoints !=null) {
            ArrayList<Point> oldList = new ArrayList<Point> (oldControlPoints);
            oldList.remove(widget.getFirstControlPoint());
            oldList.remove(widget.getLastControlPoint());
            list.addAll(oldList);
        }

        list.add(targetAnchor.compute(widget.getTargetAnchorEntry()).getAnchorSceneLocation());

        return list;
    }
    
}
