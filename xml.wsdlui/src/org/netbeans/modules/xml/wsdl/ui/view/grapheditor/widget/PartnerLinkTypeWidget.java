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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.GradientFillBorder;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.LeftRightLayout;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Represents a partner link type WSDL component.
 */
public class PartnerLinkTypeWidget extends AbstractWidget<PartnerLinkType>
        implements ExpandableWidget, DnDHandler {

    private static final boolean EXPANDED_DEFAULT = true;
    private final PartnerLinkType mPartnerLinkType;
    private Widget mLabelWidget;

    private PartnerLinkTypeContentWidget mContentWidget;

    private Widget mHeaderWidget;
    private ExpanderWidget expander;

    private static final Image IMAGE = Utilities.loadImage("org/netbeans/modules/xml/wsdl/ui/view/treeeditor/extension/bpel/resources/partnerlinktype.png");

    private static final Border HEADER_BORDER = new GradientFillBorder(0, 0, 4, 8,
            null, WidgetConstants.PARTNERLINKTYPE_GRADIENT_TOP_COLOR, WidgetConstants.PARTNERLINKTYPE_GRADIENT_BOTTOM_COLOR);
    
    public PartnerLinkTypeWidget(Scene scene, PartnerLinkType partnerLinkType, Lookup lookup) {
        super(scene, partnerLinkType, lookup);
        //mSizeRect = scene.getBounds();
        assert partnerLinkType != null : "partnerLinkTypeWidget cannot be created";
        mPartnerLinkType = partnerLinkType;
        init();
    }

    private void init() {
        setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE.darker()));
        setLayout(LayoutFactory.createVerticalLayout());
        Widget actionsWidget = createActionsWidget();
        mHeaderWidget = new HeaderWidget(getScene(), expander);
        mHeaderWidget.setLayout(new LeftRightLayout(32));
        addChild(mHeaderWidget);

        mLabelWidget = createLabelWidget();
        mHeaderWidget.setBorder(HEADER_BORDER);
        mHeaderWidget.addChild(mLabelWidget);
        
        
        mHeaderWidget.addChild(actionsWidget);
        mHeaderWidget.setOpaque(true);
        
        mHeaderWidget.setMinimumSize(new Dimension(0, 30));

        mContentWidget = new PartnerLinkTypeContentWidget(getScene(), mPartnerLinkType);
        if (ExpanderWidget.isExpanded(this, EXPANDED_DEFAULT)) {
            addChild(mContentWidget);
        }

        getActions().addAction(((PartnerScene) getScene()).getDnDAction());
    }

    private Widget createLabelWidget() {
        Widget labelWidget = new ImageLabelWidget(getScene(), IMAGE, mPartnerLinkType.getName());
        labelWidget.setBorder(new EmptyBorder(4, 4, 1, 1));
        labelWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(
                new TextFieldInplaceEditor() {

            public void setText(Widget widget, String text) {
                Model model = mPartnerLinkType.getModel();
                try {
                    if (model.startTransaction()) {
                        mPartnerLinkType.setName(text);
                    }
                } finally {
                    model.endTransaction();
                }
            }

            public boolean isEnabled(Widget widget) {
                if (getWSDLComponent() != null) {
                    return XAMUtils.isWritable(getWSDLComponent().getModel());
                }
                return false;
            }

            public String getText(Widget widget) {
                return mPartnerLinkType.getName();
            }

        }, null));
        return labelWidget;
    }

    @Override
    public void updateContent() {
        if (mHeaderWidget.getChildren().contains(mLabelWidget)) {
            mHeaderWidget.removeChild(mLabelWidget);
        }
        mLabelWidget = createLabelWidget();
        mHeaderWidget.addChild(0, mLabelWidget);
        
        mContentWidget.updateContent();
    }

    public void expandWidget(ExpanderWidget expander) {
        if (mContentWidget.getParentWidget() == null) {
            addChild(mContentWidget);
            getScene().revalidate();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    // It is quite likely that this widget just got wider
                    // because of the new children widgets. Save this width
                    // as the minimum size so we do not shrink when we are
                    // collapsed, which would probably annoy the user.
                    Rectangle bounds = getBounds();
                    setMinimumSize(new Dimension(bounds.width, 0));
                }
            });
        }
    }

    public void collapseWidget(ExpanderWidget expander) {
        if (mContentWidget.getParentWidget() != null) {
            removeChild(mContentWidget);
            getScene().revalidate();
        }
    }
    
    public void collapseWidget() {
        expander.setExpanded(false);
    }
    
    private Widget createActionsWidget() {
        Widget actionsHolderWidget = new Widget(getScene());
        actionsHolderWidget.setLayout(LayoutFactory.createHorizontalLayout(SerialAlignment.JUSTIFY, 8));
/*        ButtonWidget removeButton = new ButtonWidget(getScene(), "LBL_CollaborationsWidget_RemovePartnerLinkType");
        removeButton.setActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e) {
                deleteComponent();
            }
        
        });
        
        actionsHolderWidget.addChild(removeButton);*/
        
        expander = new ExpanderWidget(getScene(), this,
                ExpanderWidget.isExpanded(this, EXPANDED_DEFAULT));
        actionsHolderWidget.addChild(expander);

        return actionsHolderWidget;
    }
    
    
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        
        if (state.isSelected()) {
            CollaborationsWidget collaborationsWidget = getCollaborationWidget();
            if (collaborationsWidget != null) {
                collaborationsWidget.childPartnerLinkTypeSelected(this);
            }
        } else {
            CollaborationsWidget collaborationsWidget = getCollaborationWidget();
            if (collaborationsWidget != null) {
                collaborationsWidget.childPartnerLinkTypeUnSelected(this);
            }
        }
    }
    
    private CollaborationsWidget getCollaborationWidget() {
        for (Widget w = this; w != null; w = w.getParentWidget()) {
            if (w instanceof CollaborationsWidget) return (CollaborationsWidget) w;
        }
        return null;
    }

    public void dragExit() {
        
    }

    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        return false;
    }

    public boolean drop(Point scenePoint, WidgetDropTargetDropEvent event) {
        return false;
    }

    public void expandForDragAndDrop() {
        expander.setExpanded(true);
    }

    public boolean isCollapsed() {
        return mContentWidget.getParentWidget() == null;
    }

    public Object hashKey() {
        PartnerLinkType comp = getWSDLComponent();
        return comp != null ? comp.getName() : this;
    }
}
