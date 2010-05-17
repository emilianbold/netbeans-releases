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

package org.netbeans.modules.vmd.midpnb.components.svg;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuElementEventSourceCD;
import org.netbeans.modules.vmd.midpnb.flow.FlowSVGMenuElementPinOrderPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public class SVGMenuCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGMenu"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_32.png"; // NOI18N

    public static final String PROP_ELEMENTS = "elements"; // NOI18N
    public static final String PROP_INDEX_BASED_SWITCH = "indexBasedSwitch";  // NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    @Override
    public void postInitialize (DesignComponent component) {
        MidpProjectSupport.addLibraryToProject (component.getDocument (), SVGPlayerCD.MIDP_NB_SVG_LIBRARY);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(SVGAnimatorWrapperCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_ELEMENTS, SVGMenuElementEventSourceCD.TYPEID.getArrayType(), PropertyValue.createEmptyArray(SVGMenuElementEventSourceCD.TYPEID), false, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_INDEX_BASED_SWITCH, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, false, Versionable.FOREVER)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGMenuCD.class, "DISP_SVGMenu_IndexBasedSwitch"), PropertyEditorBooleanUC.createInstance(), PROP_INDEX_BASED_SWITCH); //NOI18N
        
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createSVGMenuElementParameter ())
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (SVGPlayerCD.PROP_SVG_IMAGE, MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            .addSetters (MidpSetter.createSetter ("addMenuElement", MidpVersionable.MIDP_2).setArrayParameter (MidpCustomCodePresenterSupport.PARAM_SVG_MENU_ELEMENT).addParameters (MidpCustomCodePresenterSupport.PARAM_SVG_MENU_ELEMENT)); // NOI18N
    }


    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addNewActionPresenter(presenters, SVGMenuElementEventSourceCD.TYPEID);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // accept
                new AcceptTypePresenter(SVGMenuElementEventSourceCD.TYPEID) {
                    @Override
                    protected void notifyCreated (DesignComponent component) {
                        super.notifyCreated (component);
                        ArraySupport.append (getComponent (), SVGMenuCD.PROP_ELEMENTS, component);
                        if (component.isDefaultValue(SVGMenuElementEventSourceCD.PROP_STRING)) {
                            List<PropertyValue> list = getComponent ().readProperty(SVGMenuCD.PROP_ELEMENTS).getArray ();
                            component.writeProperty (SVGMenuElementEventSourceCD.PROP_STRING, MidpTypes.createStringValue (NbBundle.getMessage(SVGMenuCD.class, "DISP_SVGMenu_NewMenuItem", list.size()))); // NOI18N
                        }
                    }
                },
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter (),
                MidpCustomCodePresenterSupport.createSVGMenuCodePresenter (),
                MidpCustomCodePresenterSupport.createSVGMenuEventHandlerCodeNamePresenter (),
                // flow
                new FlowSVGMenuElementPinOrderPresenter ()
        
        );
    }
    
}
