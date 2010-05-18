/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.components.svg.form;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.inspector.controllers.ComponentsCategoryPC;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class SVGListElementEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#SVGListElementEventSource"); // NOI18N
    
    private static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/element_16.png"; // NOI18N
    private static final Image ICON = ImageUtilities.loadImage(ICON_PATH);

    public static final String PROP_STRING = "name"; //NOI18N


    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(EventSourceCD.TYPEID, TYPEID, true, false);
    }

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    @Override
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_STRING, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), false, true, MidpVersionable.MIDP)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
            .addProperty (NbBundle.getMessage(ListElementEventSourceCD.class, "DISP_ListElementEventSource_String"), // NOI18N
                PropertyEditorString.createInstance(NbBundle.getMessage(ListElementEventSourceCD.class, "LBL_ListElementEventSource_String")), PROP_STRING);
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, InspectorPositionPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, InfoPresenter.class);
        MidpActionsSupport.addCommonActionsPresenters(presenters, false, true, false, true, true);
        MidpActionsSupport.addMoveActionPresenter(presenters, SVGListCD.PROP_ELEMENTS);
        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                //delete
                new DeletePresenter() {
                    @Override
                    protected void delete() {
                        if (getComponent().getParentComponent() == null) {
                            return;
                        }
                        PropertyValue arrayValue = getComponent().getParentComponent().readProperty(SVGListCD.PROP_ELEMENTS);
                        if (arrayValue.getArray() != null) {
                            for (PropertyValue value : arrayValue.getArray()) {
                                if (value.getComponent() == getComponent()) {
                                    ArraySupport.remove(getComponent().getParentComponent(), SVGListCD.PROP_ELEMENTS, getComponent());
                                    break;
                                }
                            }
                        }
                    }
                },
                // info
                InfoPresenter.create(SVGElementSupport.createListElementInfoResolver()),
                //properties
                createPropertiesPresenter(),
                //flow
                new SVGListElementFlowEventSourcePinPresenter(),
                //inspector
                InspectorPositionPresenter.create(new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_ELEMENTS)),
                // general
                new GoToSourcePresenter () {
                    protected boolean matches (GuardedSection section) {
                        if (getComponent().getParentComponent() == null) {
                            return false;
                        }
                        return MultiGuardedSection.matches(section, getComponent().getParentComponent().getComponentID() + "-getter" , 1); // NOI18N
                    }
                }
                );
    }

    private static String getName(DesignComponent component) {
        
        return MidpValueSupport.getHumanReadableString (component.readProperty (PROP_STRING));
    }

    private class SVGListElementFlowEventSourcePinPresenter  extends   FlowEventSourcePinPresenter {
        protected DesignComponent getComponentForAttachingPin () {
            if (getComponent().getParentComponent() == null) {
                return null;
            }
            return getComponent().getParentComponent().getParentComponent();
        }

        protected String getDisplayName() {
            return getName(getComponent());
        }

        protected String getOrder() {
            return SVGFlowListElementPinOrderPresenter.CATEGORY_ID;
        }

        @Override
        protected boolean canRename () {
            if (getComponent().readProperty(PROP_STRING).getKind() == PropertyValue.Kind.USERCODE) {
                return false;
            }
            return getComponent () != null;
        }

        @Override
        protected String getRenameName() {
            return (String) getComponent ().readProperty (PROP_STRING).getPrimitiveValue ();
        }

        @Override
        protected void setRenameName(String name) {
            getComponent ().writeProperty (PROP_STRING, MidpTypes.createStringValue (name));
        }

        @Override
        protected DesignEventFilter getEventFilter() {
            return super.getEventFilter().setGlobal(true);
        }
    }
}
