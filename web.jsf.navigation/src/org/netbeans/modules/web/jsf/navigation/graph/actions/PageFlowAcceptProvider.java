/*
 * PageFlowAcceptProvider.java
 *
 * Created on March 5, 2007, 1:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author joelle
 */
public class PageFlowAcceptProvider implements AcceptProvider {
    
    /** Creates a new instance of PageFlowAcceptProvider */
    public PageFlowAcceptProvider() {
    }

    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
        
        System.out.print("\nPageFlowAcceptProvider: IS ACCEPTABLE HAS BEEN CALLED.");
        System.out.println("Widget: " + widget);
        System.out.println("Point: " + point);
        System.out.println("Transferable: " + transferable);
        return ConnectorState.REJECT_AND_STOP;
    }

    public void accept(Widget widget, Point point, Transferable transferable) {
    }
    
}
