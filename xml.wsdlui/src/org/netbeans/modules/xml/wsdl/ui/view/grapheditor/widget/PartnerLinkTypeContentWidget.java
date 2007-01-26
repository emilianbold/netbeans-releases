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

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;

import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.OperationSceneLayer;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.PartnerLinkTypeContentLayout;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Class PartnerLinkTypeContentWidget is a widget container for the
 * PartnerLinkType widgets.
 */
public class PartnerLinkTypeContentWidget extends Widget {

    private Lookup mLookup;
    private final PartnerLinkType mPartnerLinkType;
    private RoleWidget mRightRoleWidget;
    private RoleWidget mLeftRoleWidget;

    private OperationSceneLayer mOperationSceneLayer;

    public PartnerLinkTypeContentWidget(Scene scene, PartnerLinkType partnerLinkType) {
        super(scene);
        assert partnerLinkType != null : "partnerLinkTypeWidget cannot be created";
        mPartnerLinkType = partnerLinkType;
        init();
    }
    
    private void init() {
        mLookup = Lookups.fixed(new Object[] {
            mPartnerLinkType,
            this
        });
        refreshRoles();
        //setBorder(BorderFactory.createLineBorder(Color.RED));

    }
    
    public OperationSceneLayer getOperationSceneLayer() {
        return mOperationSceneLayer;
    }
    
    @Override
    public Rectangle calculateClientArea() {
        return super.calculateClientArea();
    }
    
    private void refreshRoles() {
        removeChildren();
        Scene scene = getScene();
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLUE.darker()), BorderFactory.createEmptyBorder(10, 14, 0, 14)));
        
        setLayout(new PartnerLinkTypeContentLayout(75));
        
        
        WidgetFactory factory = WidgetFactory.getInstance();
        List<Role> roles = getRoles();
        if (roles.size() == 0) {
            //draw dummy roles
            mRightRoleWidget = (RoleWidget) factory.createWidget(scene, Role.class, mLookup);
            mLeftRoleWidget = (RoleWidget) factory.createWidget(scene, Role.class, mLookup);
        } else if (roles.size() == 1) {
            //draw only right side
            mRightRoleWidget = (RoleWidget) factory.createWidget(scene, roles.get(0), mLookup, true);
            mLeftRoleWidget = (RoleWidget) factory.createWidget(scene, Role.class, mLookup);
        } else if (roles.size() == 2) {
            //draw both sides
            mRightRoleWidget = (RoleWidget) factory.createWidget(scene, roles.get(0), mLookup, true);
            mLeftRoleWidget = (RoleWidget) factory.createWidget(scene, roles.get(1), mLookup, true);
        }

        addChild(mLeftRoleWidget);
        addChild(mRightRoleWidget);
        
        mOperationSceneLayer = new OperationSceneLayer(scene, this);
        addChild(mOperationSceneLayer);
        
        LabelWidget roleLabel = new LabelWidget(scene, "Roles");
        roleLabel.setForeground(Color.GRAY);
        
        LabelWidget portTypesLabel = new LabelWidget(scene, "Port Types");
        portTypesLabel.setForeground(Color.GRAY);
        
        addChild(roleLabel);
        addChild(portTypesLabel);
        
    }
    
    private List<Role> getRoles() {
        List<Role> roles = new ArrayList<Role>();
        if (mPartnerLinkType.getRole1() != null) {
            roles.add(mPartnerLinkType.getRole1());
        }
        
        if (mPartnerLinkType.getRole2() != null) {
            roles.add(mPartnerLinkType.getRole2());
        }
        
        return roles;
    }
    
    public RoleWidget getLeftRoleWidget() {
        return mLeftRoleWidget;
    }
    
    public RoleWidget getRightRoleWidget() {
        return mRightRoleWidget;
    }

    public PartnerLinkType getWSDLComponent() {
        return mPartnerLinkType;
    }

    public void updateContent() {
        refreshRoles();
    }
    
}
