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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.OperationSceneLayer;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.PartnerLinkTypeContentLayout;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Class PartnerLinkTypeContentWidget is a widget container for the
 * PartnerLinkType widgets.
 */
public class PartnerLinkTypeContentWidget extends Widget implements PropertyChangeListener {

    private Lookup mLookup;
    private RoleWidget mRightRoleWidget;
    private RoleWidget mLeftRoleWidget;

    private OperationSceneLayer mOperationSceneLayer;
    private PropertyChangeListener weakModelListener;

    public PartnerLinkTypeContentWidget(Scene scene, PartnerLinkType partnerLinkType) {
        super(scene);
        assert partnerLinkType != null : "partnerLinkTypeWidget cannot be created";
        mLookup = Lookups.fixed(new Object[] {
                partnerLinkType,
                this
        });
        init();
    }
    
    private void init() {
        
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(10, 14, 0, 14)));
        
        setLayout(new PartnerLinkTypeContentLayout(90));
        createRoles();
        mOperationSceneLayer = new OperationSceneLayer(getScene(), this);
        addChild(mOperationSceneLayer);

    }
    
    public OperationSceneLayer getOperationSceneLayer() {
        return mOperationSceneLayer;
    }
    
    private void createRoles() {
        Scene scene = getScene();
        
        WidgetFactory factory = WidgetFactory.getInstance();
        PartnerLinkType partnerLinkType = getWSDLComponent();
        Role role1 = partnerLinkType.getRole1();
        Role role2 = partnerLinkType.getRole2();
        
        //if role1 is deleted then role1 becomes previous role2.
        //we need to find out which rolewidget was for role2 and keep it in the same place.
        RoleWidget role1Widget = null;
        if (role1 != null) {
            role1Widget = (RoleWidget) factory.getOrCreateWidget(scene, role1, mLookup, this);
        } else {
            role1Widget = (RoleWidget) factory.createWidget(scene, Role.class, mLookup);
        }
        RoleWidget role2Widget = null;
        if (role2 != null) {
            role2Widget = (RoleWidget) factory.getOrCreateWidget(scene, role2, mLookup, this);
        } else {
            role2Widget = (RoleWidget) factory.createWidget(scene, Role.class, mLookup);
        }

        //Check did left one become right one?
        mRightRoleWidget = role1Widget;
        mLeftRoleWidget = role2Widget;
        
        mLeftRoleWidget.setLeftSided(true);
        mRightRoleWidget.setLeftSided(false);
        addChild(mLeftRoleWidget);
        addChild(mRightRoleWidget);
        
    }
    
    public RoleWidget getLeftRoleWidget() {
        return mLeftRoleWidget;
    }
    
    public RoleWidget getRightRoleWidget() {
        return mRightRoleWidget;
    }

    public PartnerLinkType getWSDLComponent() {
        return mLookup.lookup(PartnerLinkType.class);
    }

    void roleDeleted(Role deletedRole) {
        if (mRightRoleWidget.getWSDLComponent() == deletedRole) {
            WidgetHelper.removeObjectFromScene(getScene(), deletedRole);
            mRightRoleWidget = (RoleWidget) WidgetFactory.getInstance().createWidget(getScene(), Role.class, mLookup);
            addChild(1, mRightRoleWidget);
            mOperationSceneLayer.updateOperations(true);
        } else if (mLeftRoleWidget.getWSDLComponent() == deletedRole) {
            WidgetHelper.removeObjectFromScene(getScene(), deletedRole);
            mLeftRoleWidget = (RoleWidget) WidgetFactory.getInstance().createWidget(getScene(), Role.class, mLookup);
            addChild(0, mLeftRoleWidget);
            mOperationSceneLayer.updateOperations(false);
        }
    }

    void roleAdded(Role role) {
        RoleWidget rw = (RoleWidget) WidgetFactory.getInstance().getOrCreateWidget(getScene(), role, mLookup, this);
            
        if (rw == mRightRoleWidget || mRightRoleWidget.getWSDLComponent() == null) {
            rw.setLeftSided(false);
            mRightRoleWidget.removeFromParent();
            mRightRoleWidget = rw;
            addChild(1, rw);
            mOperationSceneLayer.updateOperations(true);
        } else if (rw == mLeftRoleWidget || mLeftRoleWidget.getWSDLComponent() == null) {
            rw.setLeftSided(true);
            mLeftRoleWidget.removeFromParent();
            mLeftRoleWidget = rw;
            addChild(0, rw);
            mOperationSceneLayer.updateOperations(false);
        }
    }
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        Object newObj = evt.getNewValue();
        Object oldObj = evt.getOldValue();
        if (evt.getPropertyName().equals(Role.PORT_TYPE_PROPERTY)) {
            if (source instanceof Role) {
                boolean right = false;
                //Role's porttype changed.
                RoleWidget affectedRoleWidget = null;
                if (source == mRightRoleWidget.getWSDLComponent()) {
                    affectedRoleWidget = mRightRoleWidget;
                    right = true;
                } else if (source == mLeftRoleWidget.getWSDLComponent()) {
                    affectedRoleWidget = mLeftRoleWidget;
                } else {
                    return;
                }
                affectedRoleWidget.refreshPortTypeWidget();
                mOperationSceneLayer.updateOperations(right);
            } else if (source instanceof Definitions) {
                //Port type deleted. act if any of the roles have it.
                if (oldObj != null && oldObj instanceof PortType) {
                    if (mRightRoleWidget.getPortTypeWidget().getWSDLComponent() == oldObj) {
                        mRightRoleWidget.refreshPortTypeWidget();
                        mOperationSceneLayer.updateOperations(true);
                    }

                    if (mLeftRoleWidget.getPortTypeWidget().getWSDLComponent() == oldObj) {
                        mLeftRoleWidget.refreshPortTypeWidget();
                        mOperationSceneLayer.updateOperations(false);
                    }
                }
            }

            revalidate();
            getScene().validate();
            
        } else if (evt.getPropertyName().equals(PortType.OPERATION_PROPERTY)) {
            if (source instanceof PortType) {
                PortType left = mLeftRoleWidget.getPortTypeWidget().getWSDLComponent();
                PortType right = mRightRoleWidget.getPortTypeWidget().getWSDLComponent();
                if (source == left || source == right) {
                    Operation operation = null;
                    if (newObj != null && oldObj == null) {
                        if (source == left) {
                            mOperationSceneLayer.updateOperations(false);
                        }

                        if (source == right) {
                            mOperationSceneLayer.updateOperations(true);
                        }
                        getScene().revalidate(true);
                        getScene().validate();
                        ActionHelper.selectNode(operation);
                    } else if (oldObj != null && newObj == null) {
                        operation = (Operation) oldObj;
                        WidgetHelper.removeObjectFromScene(getScene(), operation);
                    }
                    revalidate();
                    getScene().validate();
                }
            }
        }
    }

    public void postDeleteComponent(Model model) {
        if (weakModelListener != null) {
            model.removePropertyChangeListener(weakModelListener);
        }
    }
    
    @Override
    protected void notifyAdded() {
        super.notifyAdded();
        WSDLComponent comp = getWSDLComponent();
        if (comp != null) {
            if (weakModelListener == null) {
                weakModelListener = WeakListeners.propertyChange(this, comp.getModel());
            }
            comp.getModel().addPropertyChangeListener(weakModelListener);
        }
    }
    
    @Override
    protected void notifyRemoved() {
        super.notifyRemoved();
        if (getWSDLComponent() != null && getWSDLComponent().getModel() != null && weakModelListener != null) {
            getWSDLComponent().getModel().removePropertyChangeListener(weakModelListener);
            weakModelListener = null;
        }
    }
}
