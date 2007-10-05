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

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.netbeans.spi.palette.PaletteController;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Drag Layer on which icons for drag events are rendered.   
 * Need to provide methods for changing custom icon?
 *
 * @author radval
 * @author skini
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
        getScene().validate();
    }


    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        if (icon == null) {
            try {
                Transferable t = event.getTransferable();
                if (t != null) {
                    if (t.isDataFlavorSupported(PaletteController.ITEM_DATA_FLAVOR)) {
                        Lookup lookup = (Lookup) t.getTransferData(
                                PaletteController.ITEM_DATA_FLAVOR);
                        Node node = lookup.lookup(Node.class);
                        icon = new IconNodeWidget(getScene(),
                                IconNodeWidget.TextOrientation.BOTTOM_CENTER);
                        icon.setOpaque(false);
                        addChild(icon);
                        icon.setLabel(node.getDisplayName());
                        Image image = node.getIcon(BeanInfo.ICON_COLOR_16x16);
                        if (image == null) {
                            image = node.getIcon(BeanInfo.ICON_COLOR_32x32);
                        }
                        if (image != null) {
                            icon.setImage(image);
                        }
                        getScene().validate();
                    }
                }
            } catch (Exception e) {
            }
        }
        if (icon != null) {
            Rectangle bounds = icon.getBounds();
            if (bounds != null) {
                bounds = icon.convertLocalToScene(bounds);

                //make bounds bigger than it actually is.
                int boundInc = 200;
                bounds.y -= boundInc;
                bounds.height += boundInc;
                bounds.x -= boundInc;
                bounds.width += boundInc;
                getScene().getView().scrollRectToVisible(bounds);
            }
            icon.setPreferredLocation(convertSceneToLocal(scenePoint));
            getScene().validate();
        }
        return false;
    }
}
