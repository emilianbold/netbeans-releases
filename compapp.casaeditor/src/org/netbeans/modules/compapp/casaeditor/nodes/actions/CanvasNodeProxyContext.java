/*
 * CanvasNodeProxyContext.java
 *
 * Created on March 29, 2007, 7:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.Point;

/**
 *
 * @author jsandusky
 */
public interface CanvasNodeProxyContext {
    
    Point getLocalLocation();
    
    Point getSceneLocation();
    
}
