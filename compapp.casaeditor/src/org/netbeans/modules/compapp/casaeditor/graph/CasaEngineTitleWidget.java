/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.border.LineBorder;
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
import org.netbeans.modules.compapp.casaeditor.graph.actions.EditablePropertiesAction;
import org.netbeans.modules.compapp.casaeditor.nodes.ServiceUnitNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Josh Sandusky
 */
public class CasaEngineTitleWidget extends Widget implements CasaMinimizable {
    
    private static final Image DEFAULT_ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/ServiceUnitNode.png");     // NOI18N
    
    private static final int   TITLE_GAP          = 3;
    
    private static final int TITLE_MINIMIZE_BUTTON_DISPLACEMENT = CasaNodeWidgetEngine.MARGIN_SE_ROUNDED_RECTANGLE + 8;
    
    private static final Image IMAGE_EXPAND       = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/expand.png"); // NOI18N
    private static final Image IMAGE_COLLAPSE     = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/graph/resources/collapse.png"); // NOI18N
    private static final Image IMAGE_UNCONFIGURED = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/palette/resources/question_violet.png");   // NOI18N
    
    private static final Border BORDER_MINIMIZE = BorderFactory.createRoundedBorder(
            2, 2, null, new Color(96, 96, 96));

    private ImageWidget mDeleteWidget;
    private ImageWidget mEditWidget;
    private ImageWidget mHideWidget;
    
    private ImageWidget configureWidget;
    private ImageWidget minimizeWidget;
//    private ImageWidget mProjectIconImageWidget;
    private LabelWidget mNameWidget;
    private LabelWidget typeWidget;
    
    private static int GAP_BELOW_AND_ABOVE_TITLE = 4;
    
    private boolean mConfigurationStatus = true;
    private Widget mTitleWidget;
    
    private static final boolean DEBUG = false;
    
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
        
//        mProjectIconImageWidget = new ImageWidget(scene);
//        mProjectIconImageWidget.setImage(DEFAULT_ICON);
//        mTitleWidget.addChild(mProjectIconImageWidget);
        
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
        
        if (DEBUG) {
            setBorder(new LineBorder(Color.black));
            mTitleWidget.setBorder(new LineBorder(Color.blue));
            minimizeWidget.setBorder(new LineBorder(Color.red));
            mDeleteWidget.setBorder(new LineBorder(Color.blue));
            configureWidget.setBorder(new LineBorder(Color.red));
            mNameWidget.setBorder(new LineBorder(Color.blue));
            typeWidget.setBorder(new LineBorder(Color.red));
        }
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
    
//    public void setComponentName(String compName) {
//        Image image = ServiceUnitNode.getProjectIconImage(compName);
//        mProjectIconImageWidget.setImage(image);
//    }
//        
//    public void setIcon(Image image) {
//        mProjectIconImageWidget.setImage(image);
//    }
    
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
