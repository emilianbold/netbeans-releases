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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Action;
import javax.swing.border.EmptyBorder;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.LeftRightLayout;
import org.netbeans.modules.xml.xam.dom.Utils;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
	private WidgetAction editorAction;

    private static final Image IMAGE = Utilities.loadImage("org/netbeans/modules/xml/wsdl/ui/view/treeeditor/extension/bpel/resources/partnerlinktype.png");

    public PartnerLinkTypeWidget(Scene scene, PartnerLinkType partnerLinkType, Lookup lookup) {
        super(scene, partnerLinkType, lookup);
        //mSizeRect = scene.getBounds();
        assert partnerLinkType != null : "partnerLinkTypeWidget cannot be created";
        mPartnerLinkType = partnerLinkType;
        init();
    }

    private void init() {
        setBorder(WidgetConstants.OUTER_BORDER);
        setLayout(LayoutFactory.createVerticalFlowLayout());
        editorAction = ActionFactory.createInplaceEditorAction(
                new TextFieldInplaceEditor() {

            public void setText(Widget widget, String text) {
            	String errorMessage = null;
            	if (text == null || text.trim().length() == 0) {
            		errorMessage = NbBundle.getMessage(PartnerLinkTypeWidget.class, "MSG_BlankPartnerLinkTypeName", text);
            	} else if (!Utils.isValidNCName(text)) { 
            		errorMessage = NbBundle.getMessage(PartnerLinkTypeWidget.class, "MSG_InvalidPartnerLinkTypeName", text);
            	}

            	if (errorMessage != null) {
            		NotifyDescriptor desc = new NotifyDescriptor.Message(errorMessage, NotifyDescriptor.ERROR_MESSAGE);
            		DialogDisplayer.getDefault().notify(desc);
            		return;
            	}

            	WSDLModel model = mPartnerLinkType.getModel();
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

        }, null);
        Widget actionsWidget = createActionsWidget();
        mHeaderWidget = new HeaderWidget(getScene(), expander);
        mHeaderWidget.setLayout(new LeftRightLayout(32));
        addChild(mHeaderWidget);

        mLabelWidget = createLabelWidget();
        mHeaderWidget.setBorder(WidgetConstants.GRADIENT_BLUE_WHITE_BORDER);
        mHeaderWidget.addChild(mLabelWidget);
        
        getActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State keyPressed (Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER && (event.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
                    if (mHeaderWidget != null) {
                        return mHeaderWidget.getActions().keyPressed(widget, event);
                    }
                } else if (event.getKeyCode() == KeyEvent.VK_F2 || event.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (editorAction == null || mLabelWidget == null) return State.REJECTED;
                    InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                    if (inplaceEditorController.openEditor (mLabelWidget)) {
                        return State.createLocked (widget, this);
                    }
                    return State.CONSUMED;
                }
                return State.REJECTED;
            }

        });
        
        setMinimumSize(new Dimension(WidgetConstants.PARTNERLLINKTYPE_MINIMUM_WIDTH, 0));
        mHeaderWidget.addChild(actionsWidget);
        mHeaderWidget.setOpaque(true);
        
        mHeaderWidget.setMinimumSize(new Dimension(0, 30));

        mContentWidget = new PartnerLinkTypeContentWidget(getScene(), mPartnerLinkType);
        addChild(mContentWidget);
        mContentWidget.setVisible(ExpanderWidget.isExpanded(this, EXPANDED_DEFAULT));

        getActions().addAction(((PartnerScene) getScene()).getDnDAction());
    }

    private Widget createLabelWidget() {
        Widget labelWidget = new ImageLabelWidget(getScene(), IMAGE, mPartnerLinkType.getName());
        labelWidget.setBorder(new EmptyBorder(4, 4, 1, 1));
        labelWidget.getActions().addAction(editorAction);
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
        mContentWidget.setVisible(true);
    }

    public void collapseWidget(ExpanderWidget expander) {
        mContentWidget.setVisible(false);
    }
    
    public void expandWidget() {
        expander.setExpanded(true);
    }
    
    public void collapseWidget() {
        expander.setExpanded(false);
    }
    
    private Widget createActionsWidget() {
        Widget actionsHolderWidget = new Widget(getScene());
        actionsHolderWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(SerialAlignment.JUSTIFY, 8));
        
        expander = new ExpanderWidget(getScene(), this,
                ExpanderWidget.isExpanded(this, EXPANDED_DEFAULT));
        actionsHolderWidget.addChild(expander);

        return actionsHolderWidget;
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
        return !mContentWidget.isVisible();
    }

    public Object hashKey() {
        PartnerLinkType comp = getWSDLComponent();
        return comp != null ? comp.getName() : this;
    }

    @Override
    protected void updateActions(List<Action> actions) {
        super.updateActions(actions);
        ListIterator<Action> liter = actions.listIterator();
        while (liter.hasNext()) {
            Action action = liter.next();
            if (action instanceof NewAction) {
                liter.remove();
            }
        }
    }
}
