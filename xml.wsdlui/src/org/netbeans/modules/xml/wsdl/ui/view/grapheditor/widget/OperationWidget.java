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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.WidgetEditCookie;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.Node;
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
    private Widget nameHolderWidget;
    
    private Widget operationHolderWidget;
    protected RectangleWidget mOperationRectangleWidget;
    private WidgetAction editorAction;
    protected Widget endFillerWidget;
    
    public OperationWidget(Scene scene, T operation, Lookup lookup) {
        super(scene, operation, lookup);
        setLayout(LayoutFactory.createVerticalFlowLayout());
        nameHolderWidget = new Widget(scene);
        nameHolderWidget.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, 0));
        addChild(nameHolderWidget);
        operationHolderWidget = new Widget(scene);
        operationHolderWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());
        addChild(operationHolderWidget);
        
        mOperationConstruct = operation;
        mOperationNameLabelWidget = new LabelWidget(getScene());
        mOperationNameLabelWidget.setLabel(mOperationConstruct.getName());
        mOperationNameLabelWidget.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        mOperationNameLabelWidget.setAlignment(Alignment.CENTER);
        mOperationNameLabelWidget.setBorder(WidgetConstants.EMPTY_2PX_BORDER);
        editorAction = ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {
            
            public void setText(Widget widget, String text) {
                if (!getWSDLComponent().getName().equals(text.trim())) {
                    SharedUtils.locallyRenameRefactor(getWSDLComponent(), text);
                }
            }
            
            public boolean isEnabled(Widget widget) {
                if (getWSDLComponent() != null) {
                    return !isImported() && XAMUtils.isWritable(getWSDLComponent().getModel());
                }
                return false;
            }
            
            public String getText(Widget widget) {
                return mOperationConstruct.getName();
            }
            
        },  null);
        mOperationNameLabelWidget.getActions().addAction(editorAction);
        getActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State keyPressed (Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_F2 || event.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (editorAction == null || mOperationNameLabelWidget == null) return State.REJECTED;
                    InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                    if (inplaceEditorController.openEditor (mOperationNameLabelWidget)) {
                        return State.createLocked (widget, this);
                    }
                    return State.CONSUMED;
                }
                return State.REJECTED;
            }

        });
        mOperationRectangleWidget = new RectangleWidget(getScene());
        
        if (isImported()) mOperationRectangleWidget.setColor(Color.GRAY);
        
        nameHolderWidget.addChild(mOperationNameLabelWidget);
        
        setBorder(BorderFactory.createEmptyBorder(WidgetConstants.OPERATION_WIDGET_BORDER_THICKNESS));
        endFillerWidget = new Widget(scene);
        endFillerWidget.setMinimumSize(new Dimension(5, 0));
        endFillerWidget.setMaximumSize(new Dimension(5, 0));
        endFillerWidget.setBorder(BorderFactory.createEmptyBorder(0, WidgetConstants.RECTANGLE_WIDGET_THICKNESS, 0, 0));
        
        getLookupContent().add(new WidgetEditCookie() {
        
            public void edit() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                inplaceEditorController.openEditor (mOperationNameLabelWidget);
            }
            
            public void close() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                inplaceEditorController.closeEditor(false);
                
            }
        
        });
    }
    
    
    /**
     * Indicates if this is a right-sided operation.
     *
     * @return  true if right-sided, false if left-sided.
     */
    public boolean isRightSided() {
        Lookup lookup = getLookup();
        DirectionCookie dc = lookup.lookup(DirectionCookie.class);
        return dc == null ? false : dc.isRightSided();
    }
    
    /**
     * Set the direction of this operation.
     *
     * @param  rightSided  true for right-sided, false for left-sided.
     */
    public void setRightSided(boolean rightSided) {
        boolean isSameSided = false;
        Lookup lookup = getLookup();
        DirectionCookie dc = lookup.lookup(DirectionCookie.class);
        if (dc == null) {
            dc = new DirectionCookie(rightSided);
            getLookupContent().add(dc);
        } else {
            if (!(dc.isRightSided() ^ rightSided)) isSameSided = true;
            dc.setRightSided(rightSided);
        }
        if (isSameSided) return;
        Widget verticalWidget = getVerticalWidget();
        endFillerWidget.removeFromParent();
        verticalWidget.removeFromParent();
        mOperationRectangleWidget.removeFromParent();
        if (isRightSided()) {
            operationHolderWidget.addChild(endFillerWidget);
            operationHolderWidget.addChild(verticalWidget, 1);
            operationHolderWidget.addChild(mOperationRectangleWidget);
        } else {
            operationHolderWidget.addChild(mOperationRectangleWidget);
            operationHolderWidget.addChild(verticalWidget, 1);
            operationHolderWidget.addChild(endFillerWidget);
        }

    }
    
    protected abstract Widget getVerticalWidget();
    
    /**
     * Returns the WSDL operation this widget represents.
     *
     * @return  the WSDL operation.
     */
    public T getOperation() {
        return mOperationConstruct;
    }
    
    public Widget getOperationHolderWidget() {
        return operationHolderWidget;
    }
    
    @Override
    public void updated() {
        if (!mOperationNameLabelWidget.getLabel().equals(mOperationConstruct.getName())) {
            mOperationNameLabelWidget.setLabel(mOperationConstruct.getName());
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == getWSDLComponent()) {
            if (evt.getPropertyName().equals(Operation.INPUT_PROPERTY) ||
                    evt.getPropertyName().equals(Operation.OUTPUT_PROPERTY) ||
                    evt.getPropertyName().equals(Operation.FAULT_PROPERTY)) {
                if (evt.getNewValue() == null && evt.getOldValue() != null) {
                    WidgetHelper.removeObjectFromScene(getScene(), evt.getOldValue());
                }
            }
                
        }
    }
    
    protected boolean isImported() {
        if (getWSDLComponent() != null) {
            return getModel() != getWSDLComponent().getModel();
        }
        return false;
    }
    
    @Override
    protected Node getNodeFilter(Node original) {
        if (isImported()) return new ReadOnlyWidgetFilterNode(original);
            
        return super.getNodeFilter(original);
    }
    
}
