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
 */

/*
 * DragOverSceneLayer.java
 *
 * Created on November 6, 2006, 6:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;

import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.DnDHandler;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.ExScene;
import org.openide.nodes.Node;

/**
 * Drag Layer on which icons for drag events are rendered.   
 * Need to provide methods for changing custom icon?
 *
 * @author radval
 */
public class DragOverSceneLayer extends LayerWidget {


    private IconNodeWidget icon;


    /** Creates a new instance of DragOverSceneLayer */
    public DragOverSceneLayer(Scene scene) {
        super(scene);
    }

    public void resetLayer() {
        removeChildren();
        icon = null;
        setPreferredBounds(new Rectangle());
        setPreferredLocation(new Point());
    }


    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        if (icon == null) {
            try {
                Transferable t = event.getTransferable();
                if (t != null) {
                    for (DataFlavor flavor : t.getTransferDataFlavors()) {
                        Class repClass = flavor.getRepresentationClass();
                        Object data = t.getTransferData(flavor);

                        if (Node.class.isAssignableFrom(repClass)) {
                            Node node = (Node) data;

                            icon = new IconNodeWidget(getScene(),
                                    IconNodeWidget.TextOrientation.BOTTOM_CENTER);
                            icon.setOpaque(false);
                            addChild(icon);                   
                            icon.setLabel(node.getName());
                            Image image = node.getIcon(BeanInfo.ICON_COLOR_16x16);
                            if (image == null) {
                                image = node.getIcon(BeanInfo.ICON_COLOR_32x32);
                            }
                            // DnD from palette
                            if (image != null) {
                                icon.setImage(image);
                            }
                            getScene().revalidate();
                        }
                    }
                }
            } catch (Exception ex) {
            }
        }

        if (icon != null)
            icon.setPreferredLocation(convertSceneToLocal(scenePoint));

        return false;
    }
}
