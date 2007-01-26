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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.EnumSet;
import java.util.List;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.HoverActionProvider;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.Lookup;

/**
 * Represents an Operation in the WSDL model. In general this class is
 * not instantiated directly, but rather its subclasses are created.
 * In that case, the caller should use the WidgetFactory to create the
 * appropriate instance for the model component.
 */
public abstract class OperationWidget<T extends Operation>
        extends AbstractWidget<Operation> {
    private T mOperationConstruct;
    private LabelWidget mOperationNameLabelWidget;
    protected Widget mOperationRectangleWidget;
    
    public OperationWidget(Scene scene, T operation, Lookup lookup) {
        super(scene, operation, lookup);
        mOperationConstruct = operation;
        mOperationNameLabelWidget = new LabelWidget(getScene());
        mOperationNameLabelWidget.setLabel(mOperationConstruct.getName());
        mOperationNameLabelWidget.setOpaque(true);
        mOperationNameLabelWidget.setAlignment(Alignment.CENTER);
        mOperationNameLabelWidget.setFont(getScene().getDefaultFont().deriveFont(Font.BOLD));
        mOperationNameLabelWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {
            
            public void setText(Widget widget, String text) {
                Model model = mOperationConstruct.getModel();
                try {
                    if (model.startTransaction()) {
                        mOperationConstruct.setName(text);
                    }
                } finally {
                    model.endTransaction();
                }
            }
            
            public boolean isEnabled(Widget widget) {
                return true;
            }
            
            public String getText(Widget widget) {
                return mOperationConstruct.getName();
            }
            
        },
                EnumSet.<InplaceEditorProvider.ExpansionDirection>of(InplaceEditorProvider.ExpansionDirection.LEFT,
                InplaceEditorProvider.ExpansionDirection.RIGHT)));
        mOperationNameLabelWidget.getActions().addAction(HoverActionProvider.getDefault(getScene()).getHoverAction());
        
        mOperationRectangleWidget = new RectangleWidget(getScene(), 10, 67);
    }
    
    /**
     * Indicates if this is a right-sided operation.
     *
     * @return  true if right-sided, false if left-sided.
     */
    public boolean isRightSided() {
        Lookup lookup = getLookup();
        DirectionCookie dc = (DirectionCookie) lookup.lookup(DirectionCookie.class);
        return dc == null ? false : dc.isRightSided();
    }
    
    /**
     * Set the direction of this operation.
     *
     * @param  rightSided  true for right-sided, false for left-sided.
     */
    public void setRightSided(boolean rightSided) {
        Lookup lookup = getLookup();
        DirectionCookie dc = (DirectionCookie) lookup.lookup(DirectionCookie.class);
        if (dc == null) {
            dc = new DirectionCookie(rightSided);
            getLookupContent().add(dc);
        } else {
            dc.setRightSided(rightSided);
        }
    }
    
    /**
     * Returns the WSDL operation this widget represents.
     *
     * @return  the WSDL operation.
     */
    public T getOperation() {
        return mOperationConstruct;
    }
    
    protected LabelWidget getLabel() {
        return mOperationNameLabelWidget;
    }
    
    @Override
    public void updateContent() {
        if (!mOperationNameLabelWidget.getLabel().equals(mOperationConstruct.getName())) {
            mOperationNameLabelWidget.setLabel(mOperationConstruct.getName());
        }
    }
    
    public class OneSideJustifiedOtherNotLayout implements Layout {
        
        boolean isRightSided;
        
        public OneSideJustifiedOtherNotLayout(boolean rightSided) {
            isRightSided = rightSided;
        }
        
        public void justify(Widget widget) {
            List<Widget> children = widget.getChildren();
            assert children.size() == 2 : "this layout cannot take more than 2 child widgets";
            
            Widget first = children.get(0);
            Widget second = children.get(1);
            
            Rectangle parentBounds = widget.getClientArea();
            
            
            Rectangle secondBounds = second.getBounds();
            Rectangle firstBounds = first.getBounds();
            
            
            firstBounds.width = parentBounds.width - secondBounds.width;
            secondBounds.height= Math.max(firstBounds.height + 6, secondBounds.height);
            
            if (isRightSided) {
                first.resolveBounds(new Point(0,0), firstBounds);
                second.resolveBounds(new Point(parentBounds.width - secondBounds.width, 0), secondBounds);
            } else {
                first.resolveBounds(new Point(secondBounds.width,0), firstBounds);
                second.resolveBounds(new Point(0, 0), secondBounds);
            }
        }
        
        public void layout(Widget widget) {
            List<Widget> children = widget.getChildren();
            assert children.size() == 2 : "this layout cannot take more than 2 child widgets";
            
            Widget first = children.get(0);
            Widget second = children.get(1);
            
            
            Rectangle secondBounds = second.getPreferredBounds();
            Rectangle firstBounds = first.getPreferredBounds();
            
            
            secondBounds.height= Math.max(firstBounds.height + 6, secondBounds.height);
            
            if (isRightSided) {
                first.resolveBounds(new Point(0,0), firstBounds);
                second.resolveBounds(new Point(firstBounds.width, 0), secondBounds);
            } else {
                first.resolveBounds(new Point(secondBounds.width,0), firstBounds);
                second.resolveBounds(new Point(0, 0), secondBounds);
            }
        }
        
        public boolean requiresJustification(Widget widget) {
            return true;
        }
        
    }
}
