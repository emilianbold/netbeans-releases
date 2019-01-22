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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midp.components.sources;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.points.SwitchPointCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowSwitchPointPinOrderPresenter;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorJavaString;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 */
public final class SwitchCaseEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SwitchCaseEventSource"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/switch_case_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/switch_case_32.png"; // NOI18N

    public static final String PROP_OPTION = "option"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor (PROP_OPTION, MidpTypes.TYPEID_JAVA_CODE, PropertyValue.createNull (), false, false, Versionable.FOREVER)
        );
    }

    @Override
    public PaletteDescriptor getPaletteDescriptor () {
        return new PaletteDescriptor (MidpPaletteProvider.CATEGORY_PROCESS_FLOW, NbBundle.getMessage(SwitchCaseEventSourceCD.class, "DISP_SwitchCaseEventSource"), NbBundle.getMessage(SwitchCaseEventSourceCD.class, "TTIP_SwitchCaseEventSource"), ICON_PATH, LARGE_ICON_PATH); // NOI18N
    }

    public static DesignComponent getSwitchPointComponent (DesignComponent switchCaseEventSourceComponent) {
        return switchCaseEventSourceComponent.getParentComponent ();
    }

    public DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                    .addProperty (NbBundle.getMessage(SwitchCaseEventSourceCD.class, "DISP_SwitchCaseEventSource_Switch_Case_Option"), PropertyEditorJavaString.createInstance(TYPEID), PROP_OPTION); // NOI18N
    }
    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        //MidpActionsSupport.addCommonActionsPresenters (presenters, true, true, true, true, true);
        MidpActionsSupport.addMoveActionPresenter(presenters, SwitchPointCD.PROP_CASES);
        super.gatherPresenters(presenters);
    }
    
    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // properties
            createPropertiesPresenter (),
            // info
            InfoPresenter.create (new InfoPresenter.Resolver() {
                public DesignEventFilter getEventFilter (DesignComponent component) {
                    return new DesignEventFilter ().addComponentFilter (component, false);
                }
                public String getDisplayName (DesignComponent component, InfoPresenter.NameType nameType) {
                    switch (nameType) {
                        case PRIMARY:
                            return MidpValueSupport.getHumanReadableString (component.readProperty (SwitchCaseEventSourceCD.PROP_OPTION));
                        case SECONDARY:
                            return NbBundle.getMessage(SwitchCaseEventSourceCD.class, "TYPE_SwitchCaseEventSource"); // NOI18N
                        case TERTIARY:
                            return null;
                        default:
                            throw Debug.illegalState ();
                    }
                }
                public boolean isEditable (DesignComponent component) {
                    return false; // TODO
                }
                public String getEditableName (DesignComponent component) {
                    return null; // TODO
                }
                public void setEditableName (DesignComponent component, String enteredName) {
                    // TODO
                }
                public Image getIcon (DesignComponent component, InfoPresenter.IconType iconType) {
                    return ImageUtilities.loadImage (ICON_PATH);
                }
            }),
            // flow
            new FlowEventSourcePinPresenter () {
                protected DesignComponent getComponentForAttachingPin () {
                    return SwitchCaseEventSourceCD.getSwitchPointComponent (getComponent ());
                }
                protected String getDisplayName () {
                    return NbBundle.getMessage(SwitchCaseEventSourceCD.class, "DISP_SwitchCaseEventSource_FlowPin", MidpValueSupport.getHumanReadableString (getComponent ().readProperty (PROP_OPTION))); // NOI18N
                }
                protected String getOrder () {
                    return FlowSwitchPointPinOrderPresenter.CATEGORY_ID;
                }
            },
            // delete
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter (),
            new DeletePresenter () {
                protected void delete () {
                    DesignComponent component = getComponent ();
                    DesignComponent switchPoint = component.getParentComponent ();
                    ArraySupport.remove (switchPoint, SwitchPointCD.PROP_CASES, component);
                }
            }
        );
    }


}
