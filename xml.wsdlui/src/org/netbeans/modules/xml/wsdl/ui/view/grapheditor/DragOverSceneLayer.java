/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;

import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.ImageLabelWidget;
import org.openide.nodes.Node;

/**
 * Drag Layer on which icons for drag events are rendered.   
 * Need to provide methods for changing custom icon?
 *
 * @author radval
 * @author skini
 */
public class DragOverSceneLayer extends LayerWidget {


    private ImageLabelWidget icon;


    /** Creates a new instance of DragOverSceneLayer */
    public DragOverSceneLayer(Scene scene) {
        super(scene);
    }

    public void resetLayer() {
        removeChildren();
        icon = null;
        setPreferredBounds(new Rectangle());
        setPreferredLocation(new Point());
        getScene().validate();
    }


    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        if (icon == null) {
            try {
                Transferable t = event.getTransferable();
                if (t != null) {
                    String name = null;
                    Node node = Utility.getPaletteNode(t);
                    if (node != null) {
                        name = node.getDisplayName();
                    } else {
                        Node[] nodes = Utility.getNodes(t);
                        if (nodes.length == 1) {
                            node = nodes[0];
                            name = node.getName();
                        }
                    }
                    if (node != null) {
                        Image image = node.getIcon(BeanInfo.ICON_COLOR_16x16);
                        if (image == null) {
                            image = node.getIcon(BeanInfo.ICON_COLOR_32x32);
                        }
                        icon = new ImageLabelWidget(getScene(), image, name);
                        icon.setForeground(Color.LIGHT_GRAY);
                        icon.setOpaque(false);
                        addChild(icon);

                        getScene().validate();
                    }
                }
            } catch (Exception e) {
                //do nothing
            }
        }
        if (icon != null) {
            
            //cursor icon size, otherwise icon gets settled near the mouse point.
            scenePoint.x += 16;
            
            icon.setPreferredLocation(convertSceneToLocal(scenePoint));
            getScene().validate();
            
            Rectangle sceneBounds = getScene().getBounds();
            Rectangle visibleRect = getScene().getView().getVisibleRect();

            //35 seems to be good.
            int boundInc = 35;

            Rectangle reducedVisibleRect = new Rectangle(visibleRect.x + boundInc, visibleRect.y + boundInc, visibleRect.height - boundInc, visibleRect.width - boundInc);
            Rectangle iconBounds = icon.getBounds();
            Rectangle scrollRect = new Rectangle (scenePoint.x - boundInc, scenePoint.y - boundInc,
                    iconBounds.width + boundInc, iconBounds.height + boundInc);
            Rectangle sceneIntersect = sceneBounds.intersection(scrollRect);
            
            if (!reducedVisibleRect.contains(sceneIntersect)) {
                getScene().getView().scrollRectToVisible(sceneIntersect);
            }

            getScene().validate();
        }
        return false;
    }
}
