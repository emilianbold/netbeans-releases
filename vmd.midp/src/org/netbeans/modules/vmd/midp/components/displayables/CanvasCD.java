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
package org.netbeans.modules.vmd.midp.components.displayables;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;

import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */

public final class CanvasCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Canvas"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/canvas_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midp/resources/components/canvas_32.png"; // NOI18N

    public static final Integer VALUE_DOWN = 6;
    public static final Integer VALUE_FIRE = 8;
    public static final Integer VALUE_GAME_A = 9;
    public static final Integer VALUE_GAME_B = 10;
    public static final Integer VALUE_GAME_C = 11;
    public static final Integer VALUE_GAME_D = 12;

    public static final Integer VALUE_KEY_NUM0 = 48;
    public static final Integer VALUE_KEY_NUM1 = 49;
    public static final Integer VALUE_KEY_NUM2 = 50;
    public static final Integer VALUE_KEY_NUM3 = 51;
    public static final Integer VALUE_KEY_NUM4 = 52;
    public static final Integer VALUE_KEY_NUM5 = 53;
    public static final Integer VALUE_KEY_NUM6 = 54;
    public static final Integer VALUE_KEY_NUM7 = 55;
    public static final Integer VALUE_KEY_NUM8 = 56;
    public static final Integer VALUE_KEY_NUM9 = 57;
    public static final Integer VALUE_KEY_POUND = 35;
    public static final Integer VALUE_KEY_STAR = 42;
    public static final Integer VALUE_LEFT = 2;
    public static final Integer VALUE_RIGHT = 5;
    public static final Integer VALUE_UP = 1;
    
    public static final String PROP_IS_FULL_SCREEN = "isFullScreen"; //NOI18N

    static {
        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (DisplayableCD.TYPEID, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
                //MIDP 2.0
                new PropertyDescriptor(PROP_IS_FULL_SCREEN, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(false), false, true, MidpVersionable.MIDP_2)
                );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(CanvasCD.class, "DISP_Canvas_Full_Screen"), // NOI18N
                    NbBundle.getMessage(CanvasCD.class, "TTIP_Canvas_Full_Screen"), // NOI18N
                    PropertyEditorBooleanUC.createInstance(NbBundle.getMessage(CanvasCD.class, "LBL_Canvas_Full_Screen")), PROP_IS_FULL_SCREEN); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_IS_FULL_SCREEN))
                .addSetters(MidpSetter.createSetter("setFullScreenMode", MidpVersionable.MIDP_2).addParameters(PROP_IS_FULL_SCREEN)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter()
        );
    }

}
