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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.actions.EditablePropertiesAction;
import org.openide.util.Utilities;

/**
 *
 * @author Josh Sandusky
 */
public class CasaEngineTitleWidget extends Widget implements CasaMinimizable {
    
    private static final int   TITLE_GAP          = 3;
    
    private static final int TITLE_MINIMIZE_BUTTON_DISPLACEMENT = CasaNodeWidgetEngine.MARGIN_SE_ROUNDED_RECTANGLE + 8;
    
    private static final Image IMAGE_EXPAND       = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/expand.png"); // NOI18N
    private static final Image IMAGE_COLLAPSE     = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/collapse.png"); // NOI18N
    private static final Image IMAGE_UNCONFIGURED = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/question_violet.png");   // NOI18N
    
    private static final Border BORDER_MINIMIZE = BorderFactory.createRoundedBorder(
            2, 2, null, new Color(96, 96, 96));

    private ImageWidget mDeleteWidget;
    private ImageWidget mEditWidget;
    private ImageWidget mHideWidget;
    
    private ImageWidget configureWidget;
    private ImageWidget minimizeWidget;
    private LabelWidget mNameWidget;
    private LabelWidget typeWidget;
    
    private static int GAP_BELOW_AND_ABOVE_TITLE = 4;
    
    private boolean mConfigurationStatus = true;
    private Widget mTitleWidget;
    
    
    public CasaEngineTitleWidget(Scene scene, StateModel stateModel) {
        super(scene);
        
        mTitleWidget = new Widget(scene);
        mTitleWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.LEFT_TOP, TITLE_GAP));
        Widget emptyWidget = new Widget(scene); //Placeholder to place MinimizeIcon inside rounded rectangle
        
        emptyWidget.setPreferredBounds(new Rectangle(CasaNodeWidgetEngine.ARROW_PIN_WIDTH, 0));
        mTitleWidget.addChild(emptyWidget);
        
        minimizeWidget = new ImageWidget(scene, IMAGE_COLLAPSE);
        minimizeWidget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        minimizeWidget.setBorder(BORDER_MINIMIZE);
        minimizeWidget.getActions().addAction(new ToggleMinimizedAction(stateModel));
        mTitleWidget.addChild(minimizeWidget);

        mDeleteWidget = new ImageWidget(scene);
        mEditWidget = new ImageWidget(scene);
        mEditWidget.getActions().addAction(new EditablePropertiesAction());
        mHideWidget= new ImageWidget(scene);

        mTitleWidget.addChild(mEditWidget);

        configureWidget = new ImageWidget(scene);
        mTitleWidget.addChild(configureWidget);
        
        mNameWidget = new LabelWidget(scene);
        mNameWidget.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        mNameWidget.setForeground(CasaFactory.getCasaCustomizer().getCOLOR_SU_REGION_TITLE());
        mTitleWidget.addChild(mNameWidget);
        
        typeWidget = new LabelWidget(scene);
        mTitleWidget.addChild(typeWidget);
        
        Widget rightEmptyWidget = new Widget(scene); //Placeholder to place MinimizeIcon inside rounded rectangle
        rightEmptyWidget.setPreferredBounds(new Rectangle(CasaNodeWidgetEngine.ARROW_PIN_WIDTH, 0));
        mTitleWidget.addChild(rightEmptyWidget);
        
        Widget topHolderWidget = new Widget(scene);
        Widget topEmptyWidget = new Widget(scene);
        topEmptyWidget.setPreferredBounds(new Rectangle(0,0,0, GAP_BELOW_AND_ABOVE_TITLE));
        Widget bottomEmptyWidget = new Widget(scene);
        bottomEmptyWidget.setPreferredBounds(new Rectangle(0,0,0, GAP_BELOW_AND_ABOVE_TITLE));

        topHolderWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
        topHolderWidget.addChild(topEmptyWidget);
        topHolderWidget.addChild(mTitleWidget);
        topHolderWidget.addChild(bottomEmptyWidget);
        
        addChild(topHolderWidget);
    }

    public void setTitleColor(Color color) {
        mNameWidget.setForeground(color);
    }
    
    public void setTitleFont(Font font) {
        mNameWidget.setFont(font);
    }
            
    public void setLabel(String label) {
        mNameWidget.setLabel(label);
    }
    
    public boolean getConfigurationStatus(){
        return mConfigurationStatus;
    }
    
    public void setConfigurationStatus(boolean bConfStatus) {
        mConfigurationStatus = bConfStatus;
        configureWidget.setImage(getConfigurationStatus() ? null : IMAGE_UNCONFIGURED);
    }
    
    public void setEditable(boolean bValue) {
        mEditWidget.setImage(bValue ? RegionUtilities.IMAGE_EDIT_16_ICON : null);
    }
    
    public void setMinimized(boolean isMinimized) {
        minimizeWidget.setImage(isMinimized ? IMAGE_EXPAND : IMAGE_COLLAPSE);
    }



    private final class ToggleMinimizedAction extends WidgetAction.Adapter {
        private StateModel mStateModel;
        public ToggleMinimizedAction(StateModel stateModel) {
            mStateModel = stateModel;
        }
        public State mousePressed(Widget widget, WidgetMouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1 || event.getButton() == MouseEvent.BUTTON2) {
                mStateModel.toggleBooleanState();
                return State.CONSUMED;
            }
            return State.REJECTED;
        }
        public State keyPressed (Widget widget, WidgetKeyEvent event) {
            State retState = State.REJECTED;
            if ((event.getModifiers () & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK  &&  event.getKeyCode () == KeyEvent.VK_ENTER) {
                mStateModel.toggleBooleanState();
                retState = State.CONSUMED;
            }
            return retState;
        }
    }
}
