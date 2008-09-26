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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Action;
import javax.swing.border.EmptyBorder;
import javax.xml.namespace.QName;

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
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.WidgetEditCookie;
import org.netbeans.modules.xml.xam.dom.Utils;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents a partner link type WSDL component.
 */
public class PartnerLinkTypeWidget extends AbstractWidget<PartnerLinkType>
        implements ExpandableWidget, DnDHandler {

    private static final boolean EXPANDED_DEFAULT = true;
    private ImageLabelWidget mLabelWidget;

    private PartnerLinkTypeContentWidget mContentWidget;

    private Widget mHeaderWidget;
    private ExpanderWidget expander;
	private WidgetAction editorAction;

    private static final Image IMAGE = ImageUtilities.loadImage("org/netbeans/modules/xml/wsdl/ui/view/treeeditor/extension/bpel/resources/partnerlinktype.png");

    public PartnerLinkTypeWidget(Scene scene, PartnerLinkType partnerLinkType, Lookup lookup) {
        super(scene, partnerLinkType, lookup);
        //mSizeRect = scene.getBounds();
        assert partnerLinkType != null : "partnerLinkTypeWidget cannot be created";
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
            	
            	if (text != null && text.equals(getWSDLComponent().getName())) return;

            	WSDLModel model = getWSDLComponent().getModel();
            	try {
            		if (model.startTransaction()) {
            			getWSDLComponent().setName(text);
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
                return getWSDLComponent().getName();
            }

        }, null);
        Widget actionsWidget = createActionsWidget();
        mHeaderWidget = new HeaderWidget(getScene(), expander);
        mHeaderWidget.setLayout(WidgetConstants.HEADER_LAYOUT);
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

        mContentWidget = new PartnerLinkTypeContentWidget(getScene(), getWSDLComponent());
        addChild(mContentWidget);
        mContentWidget.setVisible(ExpanderWidget.isExpanded(this, EXPANDED_DEFAULT));

        getActions().addAction(((PartnerScene) getScene()).getDnDAction());
        
        getLookupContent().add(new WidgetEditCookie() {
            
            public void edit() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                inplaceEditorController.openEditor (mLabelWidget);
            }
            
            public void close() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                inplaceEditorController.closeEditor(false);
            }
        
        });
    }

    private ImageLabelWidget createLabelWidget() {
        ImageLabelWidget labelWidget = new ImageLabelWidget(getScene(), IMAGE, getName());
        labelWidget.setBorder(new EmptyBorder(4, 4, 1, 1));
        labelWidget.getActions().addAction(editorAction);
        return labelWidget;
    }
    
    @Override
    public void updated() {
        mLabelWidget.setLabel(getName());
    }
    
    private String getName() {
        return getWSDLComponent().getName();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (evt.getSource() == getWSDLComponent()) {
            if (evt.getOldValue() != null && evt.getOldValue() instanceof Role) {
                mContentWidget.roleDeleted((Role)evt.getOldValue());
                getScene().validate();
            } else if (evt.getNewValue() != null && evt.getNewValue() instanceof Role) {
                mContentWidget.roleAdded((Role)evt.getNewValue());
                getScene().validate();
            }
        }
    }
    
    public void expandWidget(ExpanderWidget expander) {
        mContentWidget.setVisible(true);
    }

    public void collapseWidget(ExpanderWidget expander) {
        Rectangle bounds = getBounds();
        Dimension d = getMinimumSize();
        if (bounds != null && d.width < bounds.width) {
            d.width = bounds.width;
            setMinimumSize(d);
        }
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
        if (comp != null) {
            QName qname = Utility.getQNameForWSDLComponent(comp, comp.getModel());
            if (qname != null) {
                return qname;
            }
        }
        return this;
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
