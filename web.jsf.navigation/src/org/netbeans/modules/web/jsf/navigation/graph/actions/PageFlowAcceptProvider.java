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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * PageFlowAcceptProvider.java
 *
 * Created on March 5, 2007, 1:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
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
        DataFlavor[] dfs = transferable.getTransferDataFlavors();
        for( DataFlavor flavor: dfs){
            System.out.println("Data Flavor: " + flavor);
        }
        return ConnectorState.REJECT_AND_STOP;
    }

    public void accept(Widget widget, Point point, Transferable transferable) {
    }
    
}
