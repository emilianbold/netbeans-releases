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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageFileAcceptPresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midpnb.screen.display.AbstractInfoDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.PropertyEditorResourceLazyInit;

/**
 *
 * @author Karol Harezlak
 */
public class AbstractInfoScreenCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.AbstractInfoScreen"); // NOI18N

    public static final String PROP_IMAGE = "image"; // NOI18N
    public static final String PROP_TEXT = "text"; // NOI18N
    public static final String PROP_TEXT_FONT = "textFont"; // NOI18N
    
    public static final String[] MIDP_NB_LIBRARY_PDA = {"NetBeans MIDP Components PDA"}; // NOI18N
    public static final String[] MIDP_NB_LIBRARY_WMA = {"NetBeans MIDP Components WMA"}; // NOI18N
    public static final String[] MIDP_NB_LIBRARY_BASIC = {"NetBeans MIDP Components"}; // NOI18N

    public AbstractInfoScreenCD () {
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList (
            new PropertyDescriptor(PROP_IMAGE, ImageCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_TEXT, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_TEXT_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters (presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(AbstractInfoScreenCD.class, "DISP_AbstractInfoScreen_text"), // NOI18N
                    PropertyEditorString.createInstance(NbBundle.getMessage(AbstractInfoScreenCD.class,
                        "LBL_AbstractInfoScreen_text")), PROP_TEXT) // NOI18N
                .addProperty(NbBundle.getMessage(AbstractInfoScreenCD.class, "DISP_AbstractInfoScreen_image"), // NOI18N
                    PropertyEditorResourceLazyInit.createImagePropertyEditor(), PROP_IMAGE)
                .addProperty(NbBundle.getMessage(AbstractInfoScreenCD.class, "DISP_AbstractInfoScreen_textFont"), // NOI18N
                    PropertyEditorResourceLazyInit.createFontPropertyEditor(), PROP_TEXT_FONT);
    }

    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpParameter.create (PROP_TEXT, PROP_IMAGE, PROP_TEXT_FONT))
            .addSetters (MidpSetter.createSetter ("setImage", MidpVersionable.MIDP_2).addParameters (PROP_IMAGE)) // NOI18N
            .addSetters (MidpSetter.createSetter ("setText", MidpVersionable.MIDP_2).addParameters (PROP_TEXT)) // NOI18N
            .addSetters (MidpSetter.createSetter ("setTextFont", MidpVersionable.MIDP_2).addParameters (PROP_TEXT_FONT)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            // accept
            new ImageFileAcceptPresenter(ImageCD.PROP_IMAGE, ImageCD.TYPEID, "jpg", "png", "gif"), //NOI18N
            new MidpAcceptTrensferableKindPresenter().addType(FontCD.TYPEID, PROP_TEXT_FONT),
            new MidpAcceptTrensferableKindPresenter().addType(ImageCD.TYPEID, PROP_IMAGE),
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter (),
            MidpCodePresenterSupport.createAddImportPresenter (),
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_IMAGE),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_TEXT_FONT),
            // screen
            new AbstractInfoDisplayPresenter(),
            new MidpAcceptProducerKindPresenter().addType(FontCD.TYPEID, PROP_TEXT_FONT).addType(ImageCD.TYPEID, PROP_IMAGE)
        );
    }

}
