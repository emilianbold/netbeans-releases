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

            //Rectangle visibleRect = new Rectangle(curPoint.x, curPoint.y - newDimension.height,newDimension.width,2 * newDimension.height); 
            Rectangle visibleRect = new Rectangle(curPoint.x, curPoint.y - TOP_VISIBLE_HEIGHT,newDimension.width,TOP_VISIBLE_HEIGHT + newDimension.height); 
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
