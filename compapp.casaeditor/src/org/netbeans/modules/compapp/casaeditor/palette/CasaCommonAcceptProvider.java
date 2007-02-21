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

/*
 * CasaCommonAcceptProvider.java
 *
 * Created on January 22, 2007, 3:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.openide.nodes.Node;

/**
 *
 * @author rdara
 */
public class CasaCommonAcceptProvider implements CasaAcceptProvider {
    
    private CasaModelGraphScene mScene;
    
    private Image mIconImage;
    private String mIconLable;
    
    
    /** Creates a new instance of CasaCommonAcceptProvider */
    public CasaCommonAcceptProvider(CasaModelGraphScene scene) {
        mScene = scene;
    }

    public CasaModelGraphScene getScene() {
        return mScene;
    }
    
    public void createIcon(Transferable t) {
        try {
            if (t != null) {
                for (DataFlavor flavor : t.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    Object data = t.getTransferData(flavor);
                    if (Node.class.isAssignableFrom(repClass)) {
                        Node node = (Node) data;
                        mIconLable = "    " + node.getName();
                        mIconImage = node.getIcon(BeanInfo.ICON_COLOR_16x16);
                        // DnD from palette
                        IconNodeWidget iconNodeWidget = new IconNodeWidget(mScene);
                        iconNodeWidget.setOpaque(false);
                        if(mScene.getDragLayer().getChildren().size() < 1) {
                            mScene.getDragLayer().addChild(iconNodeWidget);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
    
    public void positionIcon(Widget widget, Point point, ConnectorState state) {
        boolean bValue = state == ConnectorState.REJECT ? false : true;
        if(mScene.getDragLayer().getChildren().size() > 0) {
            IconNodeWidget iconNodeWidget = (IconNodeWidget) mScene.getDragLayer().getChildren().get(0);
            iconNodeWidget.setImage(bValue ? mIconImage : null);
            iconNodeWidget.setLabel(bValue ? mIconLable : null);
            iconNodeWidget.setPreferredLocation(widget.convertLocalToScene(point));
        }
    }
    
    public void removeIcon() {
        mScene.getDragLayer().removeChildren();
    }
    
    public ConnectorState isAcceptable (Widget widget, Point point, Transferable transferable){
        return ConnectorState.REJECT;
    }
    public void accept(Widget widget, Point point, Transferable transferable) {
        
    }
    
}
