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

/*
 * OperationSceneLayer.java
 *
 * Created on November 6, 2006, 6:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.OperationWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.PartnerLinkTypeContentWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.RoleWidget;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.WidgetFactory;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class OperationSceneLayer extends Widget implements ComponentListener {
    
    private static final int OPERATION_GAP = 25;
    private PartnerLinkTypeContentWidget mOuterWidget;
    private Widget dummyOperationWidget;
    private PortType leftPortType;
    private PortType rightPortType;
    private Widget dummyEndWidget;
    
    public OperationSceneLayer(Scene scene, PartnerLinkTypeContentWidget outerWidget) {
        super(scene);
        mOuterWidget = outerWidget;
        mOuterWidget.getWSDLComponent().getModel().addComponentListener(this);
        // Get the port types involved so we can detect when they are deleted.
        rightPortType = getPortType(mOuterWidget.getRightRoleWidget());
        leftPortType = getPortType(mOuterWidget.getLeftRoleWidget());
        init();
    }
    
    private void init() {
//        setBorder(BorderFactory.createLineBorder(Color.CYAN));
        setLayout(LayoutFactory.createVerticalLayout(SerialAlignment.JUSTIFY, OPERATION_GAP));
        dummyOperationWidget = new Widget(getScene());
        dummyOperationWidget.setPreferredBounds(new Rectangle(0, 67));//67 = height of a operation widget
        dummyEndWidget = new Widget(getScene());
        dummyEndWidget.setPreferredBounds(new Rectangle(0, 105));//67 = height of a operation widget
        refreshOperations();
        setMinimumSize(new Dimension(400, 0));
    }

/*    @Override
    public Rectangle calculateClientArea() {
        Rectangle rect = super.calculateClientArea();
        rect.width = mOuterWidget.getDistanceBetweenPortTypeColumns() + 2;
        rect.height = 400;
        return rect;
    }*/


    public void childrenAdded(ComponentEvent evt) {
        if (evt.getSource() instanceof PortType) {
            Role rightRole = mOuterWidget.getRightRoleWidget().getWSDLComponent();
            if (rightRole != null && rightRole.getPortType() != null) {
                refreshOperations();
            } else {
                PortType pt = (PortType) evt.getSource();
                Role leftRole = mOuterWidget.getLeftRoleWidget().getWSDLComponent();
                if (leftRole != null && leftRole.getPortType() != null) {
                    if (pt == leftRole.getPortType().get()) {
                        refreshOperations();
                    }
                }
            }
        }
    }

    /**
     * Remove all of the children and reconstruct based on the current
     * state of the port types.
     */
    private void refreshOperations() {
        this.removeChildren();
        mOuterWidget.getRightRoleWidget().showHotSpot(false);
        PortType pt = getPortType(mOuterWidget.getRightRoleWidget());
        if (pt != null) {
            renderOperations(true, pt);
        }
        mOuterWidget.getLeftRoleWidget().showHotSpot(false);
        pt = getPortType(mOuterWidget.getLeftRoleWidget());
        if (pt != null) {
            renderOperations(false, pt);
        }
        addChild(dummyEndWidget);
    }

    /**
     * Renders the operations for the given PortType.
     *
     * @param  right  true if right-sided, false if left.
     * @param  pt     the port type (must not be null).
     */
    private void renderOperations(boolean right, PortType pt) {
        Collection<Operation> operations = pt.getOperations();
        WidgetFactory factory = WidgetFactory.getInstance();
        for (Operation operation : operations) {
            OperationWidget operationWidget =
                    (OperationWidget) factory.createWidget(getScene(), operation);
            operationWidget.setRightSided(right);
            addChild(operationWidget);
        }
        getScene().revalidate();
    }

    public void childrenDeleted(ComponentEvent evt) {
        if (evt.getSource() instanceof PortType) {
            PortType pt = (PortType) evt.getSource();
            Role rightRole = mOuterWidget.getRightRoleWidget().getWSDLComponent();
            if (rightRole != null && rightRole.getPortType() != null) {
                refreshOperations();
            } else {
                Role leftRole = mOuterWidget.getLeftRoleWidget().getWSDLComponent();
                if (leftRole != null && leftRole.getPortType() != null) {
                    if (pt == leftRole.getPortType().get()) {
                        refreshOperations();
                    }
                }
            }
        } else {
            // Assume the source is the Definitions instance.
            PortType rpt = getPortType(mOuterWidget.getRightRoleWidget());
            PortType lpt = getPortType(mOuterWidget.getLeftRoleWidget());
            if (lpt != leftPortType || rpt != rightPortType) {
                // Looks like one or more of our port types changed.
                leftPortType = lpt;
                rightPortType = rpt;
                refreshOperations();
            }
        }
    }

    /**
     * Retrieve the PortType from the given RoleWidget.
     *
     * @param  rw  RoleWidget from which to get PortType.
     * @return  PortType, or null if not available.
     */
    private PortType getPortType(RoleWidget rw) {
        try {
            if (rw != null) {
                Role role = rw.getWSDLComponent();
                if (role != null) {
                    NamedComponentReference<PortType> ptref = role.getPortType();
                    if (ptref != null) {
                        return ptref.get();
                    }
                }
            }
        } catch (IllegalStateException ise) {
            // Indicates the referencing component is no longer in the model.
            // Fall through and return null.
        }
        return null;
    }

    public void valueChanged(ComponentEvent evt) {
        if (evt.getSource() instanceof Role) {
            Role role = (Role) evt.getSource();
            if (role.equals(mOuterWidget.getRightRoleWidget().getWSDLComponent())) {
                refreshOperations(); //TODO: do more intelligently : check what changed and if necessary
            } else if (role.equals(mOuterWidget.getLeftRoleWidget().getWSDLComponent())) {
                refreshOperations(); //TODO: do more intelligently : check what changed and if necessary
            }
        }
    }

    public void showBlankWidget(int i) {
        if (i == -1) return;
        
        removeBlankWidget();
        addChild(i, dummyOperationWidget);
    }

    public void removeBlankWidget() {
        if (getChildren().contains(dummyOperationWidget)) {
            removeChild(dummyOperationWidget);
        }
    }
}
