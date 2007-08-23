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

import javax.swing.BorderFactory;

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

    }
    
    public OperationSceneLayer getOperationSceneLayer() {
        return mOperationSceneLayer;
    }
    
    private void refreshRoles() {
        removeChildren();
        Scene scene = getScene();
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(10, 14, 0, 14)));
        
        setLayout(new PartnerLinkTypeContentLayout(90));
        
        
        WidgetFactory factory = WidgetFactory.getInstance();

        Role role1 = mPartnerLinkType.getRole1();
        Role role2 = mPartnerLinkType.getRole2();
        
        //if role1 is deleted then role1 becomes previous role2.
        //we need to find out which rolewidget was for role2 and keep it in the same place.
        RoleWidget role1Widget = null;
        if (role1 != null) {
            role1Widget = (RoleWidget) factory.createWidget(scene, role1, mLookup, true);
        } else {
            role1Widget = (RoleWidget) factory.createWidget(scene, Role.class, mLookup);
        }
        RoleWidget role2Widget = null;
        if (role2 != null) {
            role2Widget = (RoleWidget) factory.createWidget(scene, role2, mLookup, true);
        } else {
            role2Widget = (RoleWidget) factory.createWidget(scene, Role.class, mLookup);
        }

        //Check did left one become right one?
        if (role1Widget.isLeftSided() || role2Widget.isLeftSided()) {
            if (role1Widget.isLeftSided()) {
                mLeftRoleWidget = role1Widget;
                mRightRoleWidget = role2Widget;
            } else if (role2Widget.isLeftSided()){
                mLeftRoleWidget = role2Widget;
                mRightRoleWidget = role1Widget;
            }
        } else {
            mRightRoleWidget = role1Widget;
            mLeftRoleWidget = role2Widget;
        }
        
        mLeftRoleWidget.setLeftSided(true);
        mRightRoleWidget.setLeftSided(false);
        addChild(mLeftRoleWidget);
        addChild(mRightRoleWidget);
        
        mOperationSceneLayer = new OperationSceneLayer(scene, this);
        addChild(mOperationSceneLayer);

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
