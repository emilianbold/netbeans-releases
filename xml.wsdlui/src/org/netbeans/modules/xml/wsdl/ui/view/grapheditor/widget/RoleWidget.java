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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Action;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.api.visual.widget.LabelWidget.VerticalAlignment;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.WidgetEditCookie;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PortTypeNode;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.dom.Utils;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewAction;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Represents a role in the WSDL model.
 */
public class RoleWidget extends AbstractWidget<Role> implements DnDHandler{
    private PortTypeWidget mPortTypeWidget;
    private LabelWidget mLabelWidget;
    private boolean leftSided;
    private int GAP = 25;
    private WidgetAction editorAction;

    /**
     * Creates a new instance of RoleWidget.
     *
     * @param  scene   the Scene to contain this widget.
     * @param  role    the WSDL component.
     * @param  lookup  the Lookup for this widget.
     */
    public RoleWidget(Scene scene, Role role, Lookup lookup) {
        super(scene, role, lookup);
        setBorder(BorderFactory.createEmptyBorder(10, 0));
        init();
    }

    private void init() {
        setMinimumSize(new Dimension(WidgetConstants.ROLE_WIDGET_MINIMUM_WIDTH, 0));
        setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, GAP));
        setOpaque(true);
        mLabelWidget = new LabelWidget(getScene(), getName());
        mLabelWidget.setAlignment(Alignment.CENTER);
        mLabelWidget.setVerticalAlignment(VerticalAlignment.CENTER);
        mLabelWidget.setBorder(WidgetConstants.EMPTY_2PX_BORDER);
        editorAction = ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

            public void setText(Widget widget, String text) {
                String errorMessage = null;
                if (text == null || text.trim().length() == 0) {
                    errorMessage = NbBundle.getMessage(RoleWidget.class, "MSG_BlankRoleName", text);
                } else if (!Utils.isValidNCName(text)) { 
                    errorMessage = NbBundle.getMessage(RoleWidget.class, "MSG_InvalidRoleName", text);
                }
                
                if (errorMessage != null) {
                    NotifyDescriptor desc = new NotifyDescriptor.Message(errorMessage, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                    return;
                }
                
                PartnerLinkType partnerLinkType = getPartnerLinkType();
                WSDLModel model = partnerLinkType.getModel();
                Role role = getWSDLComponent();
                if (role != null && text != null && text.equals(role.getName())) return;
                try {
                    if (model.startTransaction()) {
                        if (role == null) {
                            role = (Role) partnerLinkType.getModel().
                                    getFactory().create(partnerLinkType,
                                    BPELQName.ROLE.getQName());
                            if (partnerLinkType.getRole1() == null) {
                                partnerLinkType.setRole1(role);
                            } else if (partnerLinkType.getRole2() == null) {
                                partnerLinkType.setRole2(role);
                            }
                        }
                        role.setName(text);
                    }
                } finally {
                    model.endTransaction();
                }

                if (role != null) {
                    ActionHelper.selectNode(role);
                }
                
            }

            public boolean isEnabled(Widget widget) {
                PartnerLinkType partnerLinkType = getPartnerLinkType();
                if (partnerLinkType != null) {
                    return XAMUtils.isWritable(partnerLinkType.getModel());
                }
                return false;
            }

            public String getText(Widget widget) {
                Role role = getWSDLComponent();
                if (role == null) {
                    PartnerLinkType plt = getPartnerLinkType();
                    String name = plt.getName() + "Role"; //generate a new name;
                    if (plt.getRole1() != null && plt.getRole1().getName().equals(name)
                            || plt.getRole2() != null && plt.getRole2().getName().equals(name)) {
                        name = name + "1";
                    }
                    
                    return name; 
                }
                return role.getName();
            }

        }, null);
        
        mLabelWidget.setMinimumSize(new Dimension(WidgetConstants.ROLE_WIDGET_MINIMUM_WIDTH, WidgetConstants.TEXT_LABEL_HEIGHT));
        addChild(mLabelWidget);
        mLabelWidget.getActions().addAction(editorAction);
        
        setPortType();
        
        if (getWSDLComponent() != null)
            getActions().addAction(((PartnerScene)getScene()).getDnDAction());
        getActions().addAction(new WidgetAction.Adapter() {

            @Override
            public State keyPressed (Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_F2 || event.getKeyCode() == KeyEvent.VK_ENTER) {
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
    
    protected PartnerLinkType getPartnerLinkType() {
        PartnerLinkType plt = getLookup().lookup(PartnerLinkType.class);
        return plt;
    }

    private String getName() {
        Role role = getWSDLComponent();
        if (role == null) {
            return NbBundle.getMessage(RoleWidget.class, "ROLEWIDGET_PLACE_HOLDER_ROLENAME");
        }
        
        return role.getName();
    }

    protected void setLeftSided(boolean isLeftSided) {
        leftSided = isLeftSided;
        if (mPortTypeWidget != null) {
            mPortTypeWidget.setRightSided(!leftSided);
        }
    }
    
    void refreshPortTypeWidget() {
        WidgetHelper.removeWidgetFromScene(getScene(), mPortTypeWidget);
        setPortType();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (evt.getSource() == getWSDLComponent()) {
            if (evt.getPropertyName() == Role.NAME_PROPERTY) {
                mLabelWidget.setLabel(getName());
            }
            getScene().validate();
        } else if (evt.getPropertyName().equals(Definitions.PORT_TYPE_PROPERTY) && evt.getSource() instanceof Definitions) {
            PortType widgetPT = mPortTypeWidget.getWSDLComponent();
            
            if (widgetPT != null && !widgetPT.isInDocumentModel()) {
                if (getWSDLComponent().getAttribute(Role.PORT_TYPE_PROPERTY) != null) {// 
                    WidgetHelper.removeWidgetFromScene(getScene(), mPortTypeWidget);
                    setPortType();
                }
            }
            getScene().validate();
        }
    }
    
    /**
     * Retrieve the PortType for this RoleWidget.
     *
     * @return  PortType, or null if not available.
     */
    private PortType getPortType() {
        try {
            Role role = getWSDLComponent();
            if (role != null) {
                NamedComponentReference<PortType> ptref = role.getPortType();
                if (ptref != null) {
                    return ptref.get();
                }
            }
        } catch (IllegalStateException ise) {
            // Indicates the referencing component is no longer in the model.
            // Fall through and return null.
        }
        return null;
    }

    private void setPortType() {
        WidgetFactory factory = WidgetFactory.getInstance();
        PortType portType = getPortType();
        if (portType != null) {
            mPortTypeWidget = (PortTypeWidget) factory.getOrCreateWidget(
                    getScene(), portType, getLookup(), this);
        } else {
            mPortTypeWidget = (PortTypeWidget) factory.createWidget(
                    getScene(), PortType.class, getLookup());
        }
        mPortTypeWidget.setRightSided(!leftSided);
        
        addChild(mPortTypeWidget, 1);
    }

    public void dragExit() {
        mPortTypeWidget.setDefaultBorder();
        
    }

    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        Transferable t = event.getTransferable();

        if (t != null) {
            for (Node node : Utility.getNodes(t)) {
                if (node instanceof PortTypeNode) {
                    mPortTypeWidget.setHitPointBorder();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean drop(Point scenePoint, WidgetDropTargetDropEvent event) {
        Transferable t = event.getTransferable();
        if (t != null) {
            for (Node node : Utility.getNodes(t)) {
                if (node instanceof PortTypeNode) {
                    mPortTypeWidget.setDefaultBorder();
                    setPortType((PortTypeNode)node);
                }
                return true;
            }
        }
        return false;
    }

    private void setPortType(PortTypeNode node) {
        PortType pt = node.getWSDLComponent();
        if (getWSDLComponent().getModel().startTransaction()) {
            try {
                getWSDLComponent().setPortType(getWSDLComponent().createReferenceTo(pt, PortType.class));
            } finally {
                getWSDLComponent().getModel().endTransaction();
            }
        }
        ActionHelper.selectNode(pt);
    }

    public void expandForDragAndDrop() {
        //No expansion supported.
    }

    public boolean isCollapsed() {
        return false;
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
    
    @Override
    protected Shape createSelectionShape() {
        int labelHeight = WidgetConstants.TEXT_LABEL_HEIGHT;
        if (mLabelWidget != null && mLabelWidget.getBounds() != null) {
            labelHeight = mLabelWidget.getBounds().height;
        }
        
        return new Rectangle2D.Double(getBounds().x, getBounds().y, getBounds().width, labelHeight + mPortTypeWidget.getLabelHeight() + GAP - 1);
    }
    
    @Override
    protected void paintWidget() {
        Graphics2D g = getGraphics();
        Color old = g.getColor();
        Stroke stk = g.getStroke();
        
        if (getWSDLComponent() != null) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(Color.GRAY);
            BasicStroke dotted = new BasicStroke(1, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_MITER, 5.0f, new float[]{10,5,10,5}, 0);
            g.setStroke(dotted);
        }
        if (getState().isSelected()) {
            g.setColor(WidgetConstants.SELECTION_COLOR);
        }
        g.draw(createSelectionShape());
        g.setColor(old);
        g.setStroke(stk);
    }

    PortTypeWidget getPortTypeWidget() {
        return mPortTypeWidget;
    }
}
