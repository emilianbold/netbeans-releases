/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 *
 * @author rdara
 */
public class CasaCommonAcceptProvider implements CasaAcceptProvider {
    
    private CasaModelGraphScene mScene;
    private Image mIconImage = null;
    private String mIconLable;
    private Rectangle mSceneBounds;
    private Dimension mIconOriginalSize;
    private static final Point msInvisiblePointLocation = new Point(-1210,-1210);
    
    private static final int TOP_VISIBLE_HEIGHT = 8;    //This is gap, which will force scroll bar start scrolling.
    private static final int HORIZONTAL_VISIBLE_LEFT_WIDTH = 8;
            
    public CasaCommonAcceptProvider(CasaModelGraphScene scene) {
        mScene = scene;
        mIconImage = null;
    }
    

    public CasaModelGraphScene getScene() {
        return mScene;
    }
    
    public void acceptFinished() {
        mScene.getDragLayer().removeChildren();
        mIconOriginalSize = null;
        mIconImage = null;
    }   
    
    public void acceptStarted(Transferable t) {
        mSceneBounds = mScene.getBounds();
        populateIconInfo(t);
    }
    
    
    public void positionIcon(Widget widget, Point point, ConnectorState state) {
        boolean bValue = state == ConnectorState.REJECT ? false : true;
        if(!bValue) {
            makeIconNodeWidgetAsInvisible();
            mIconOriginalSize = null;   //??
            return;
        } 
        if(mSceneBounds == null) {
            mSceneBounds = mScene.getBounds();
        }

        IconNodeWidget iconNodeWidget = getIconNodeWidget();
        if(iconNodeWidget != null && iconNodeWidget.getBounds() != null) {
            Rectangle iconBounds = iconNodeWidget.getBounds();
            Dimension newDimension = new Dimension(0,0);
            Point curPoint = widget.convertLocalToScene(point);
            
            if(mIconOriginalSize == null) {
                mIconOriginalSize = new Dimension(iconNodeWidget.getBounds().width, iconNodeWidget.getBounds().height);
            }
            newDimension = new Dimension(mIconOriginalSize.width, mIconOriginalSize.height);
                
            if(curPoint.x + mIconOriginalSize.width > mSceneBounds.width) {
                newDimension.width = mSceneBounds.width - curPoint.x;
            } 
            if(curPoint.y + mIconOriginalSize.height > mSceneBounds.height) {
                newDimension.height = mSceneBounds.height - curPoint.y;
            }
            iconNodeWidget.setPreferredSize(newDimension);
            iconNodeWidget.setPreferredLocation(curPoint);

            Rectangle visibleRect = new Rectangle(curPoint.x - HORIZONTAL_VISIBLE_LEFT_WIDTH, 
                                                  curPoint.y - TOP_VISIBLE_HEIGHT,
                                                  HORIZONTAL_VISIBLE_LEFT_WIDTH + newDimension.width,
                                                  TOP_VISIBLE_HEIGHT + newDimension.height); 
            mScene.getView().scrollRectToVisible(visibleRect);
        }
    }

    public ConnectorState isAcceptable (Widget widget, Point point, Transferable transferable){
        return ConnectorState.REJECT;
    }
    
    public void accept(Widget widget, Point point, Transferable transferable) {
    }
    
    protected void populateIconInfo(Transferable t) {
        if((mIconImage == null) || (mScene.getDragLayer().getChildren().size() < 1)) {
            try {
                if (t != null) {
                    DataFlavor[] dfs = t.getTransferDataFlavors();
                    if (dfs.length > 0) {
                        if(dfs[0].getRepresentationClass().equals(MultiTransferObject.class)){
                            MultiTransferObject mto = (MultiTransferObject)t.getTransferData(dfs[0]);
                            DataFlavor[] df = mto.getTransferDataFlavors(0);
                            if(df.length > 0) {
                                for(int i = 0; i < mto.getCount(); i++) {
                                    if(extractIcon(df[0].getRepresentationClass(), mto.getTransferData(i, df[0]), true)) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            extractIcon(dfs[0].getRepresentationClass(), t.getTransferData(dfs[0]),false);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
            IconNodeWidget iconNodeWidget = new IconNodeWidget(mScene);
            iconNodeWidget.setImage(mIconImage);
            iconNodeWidget.setLabel(mIconLable);
            makeIconNodeWidgetAsInvisible();
            mScene.getDragLayer().addChild(iconNodeWidget);
        }
    }
    
    public boolean extractIcon(Class repClass, Object data, boolean bMultipleObjects) {
        boolean bExtracted = false;
        if (Node.class.isAssignableFrom(repClass)) {
            Node node = (Node) data;
            mIconLable = "    " + node.getName();   // NOI18N
            if(bMultipleObjects) {
                mIconLable += Constants.STRING_EXTENSION;
            }
            mIconImage = node.getIcon(BeanInfo.ICON_COLOR_16x16);
            bExtracted = true;
        }
        return bExtracted;
    }
    private void makeIconNodeWidgetAsInvisible() {
        if(mScene.getDragLayer().getChildren().size() > 0) {
            Widget widget = (Widget) mScene.getDragLayer().getChildren().get(0);
            if(widget != null) {
                widget.setPreferredLocation(msInvisiblePointLocation);
            }
        }
    }
    private IconNodeWidget getIconNodeWidget() {
        IconNodeWidget iconNodeWidget = null;
        if(mScene.getDragLayer().getChildren().size() > 0) {
            iconNodeWidget = (IconNodeWidget) mScene.getDragLayer().getChildren().get(0);
        }
        return iconNodeWidget;
    }
}
