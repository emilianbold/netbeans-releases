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

package org.netbeans.modules.vmd.midpnb.components.items;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.screen.DisplayableResourceCategoriesPresenter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;
import org.netbeans.modules.vmd.midpnb.components.resources.TableModelCD;
import org.netbeans.modules.vmd.midpnb.screen.display.TableItemDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.PropertyEditorResourceLazyInit;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.propertyeditors.PropertyEditorResourceLazyInitFactory;

/**
 *
 * @author Karol Harezlak
 */
public class TableItemCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.TableItem"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/table_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/table_32.png"; // NOI18N

    public static final String PROP_TITLE = "title"; //NOI18N
    public static final String PROP_MODEL = "model"; //NOI18N
    public static final String PROP_BORDERS  = "borders"; //NOI18N
    public static final String PROP_HEADERS_FONT = "headersFont"; //NOI18N
    public static final String PROP_VALUES_FONT = "valuesFont"; //NOI18N
    public static final String PROP_TITLE_FONT = "titleFont"; //NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY_BASIC);
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_TITLE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_MODEL, TableModelCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_BORDERS, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(Boolean.TRUE), false, false, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_TITLE_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_HEADERS_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_VALUES_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters (presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter (DesignEventFilterResolver.THIS_COMPONENT)
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty(NbBundle.getMessage(TableItemCD.class, "DISP_TableItem_Title"), //NOI18N
                        PropertyEditorString.createTextFieldInstance(NbBundle.getMessage(TableItemCD.class, "LBL_TableItem_Title")), PROP_TITLE) //NOI18N
                    .addProperty(NbBundle.getMessage(TableItemCD.class, "DISP_TableItem_TableModel"),
                        PropertyEditorResourceLazyInitFactory.createTableModelPropertyEditor(), PROP_MODEL) //NOI18N
                    .addProperty(NbBundle.getMessage(TableItemCD.class, "DISP_TableItem_ShowBorders"), PropertyEditorBooleanUC.createInstance(), PROP_BORDERS) //NOI18N   
                    .addProperty(NbBundle.getMessage(TableItemCD.class, "DISP_TableItem_TitleFont"), PropertyEditorResourceLazyInit.createFontPropertyEditor(), PROP_TITLE_FONT) //NOI18N
                    .addProperty(NbBundle.getMessage(TableItemCD.class, "DISP_TableItem_HeadersFont"), PropertyEditorResourceLazyInit.createFontPropertyEditor(), PROP_HEADERS_FONT) //NOI18N
                    .addProperty(NbBundle.getMessage(TableItemCD.class, "DISP_TableItem_ValuesFont"), PropertyEditorResourceLazyInit.createFontPropertyEditor(), PROP_VALUES_FONT); //NOI18N
    }

    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createDisplayParameter ())
            .addParameters (MidpParameter.create (PROP_TITLE, PROP_MODEL, PROP_BORDERS, PROP_TITLE_FONT, PROP_HEADERS_FONT, PROP_VALUES_FONT))
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (MidpCustomCodePresenterSupport.PARAM_DISPLAY, ItemCD.PROP_LABEL))
            .addSetters (MidpSetter.createSetter ("setTitle", MidpVersionable.MIDP_2).addParameters (PROP_TITLE)) //NOI18N
            .addSetters (MidpSetter.createSetter ("setModel", MidpVersionable.MIDP_2).addParameters (PROP_MODEL)) //NOI18N
            .addSetters (MidpSetter.createSetter ("setBorders", MidpVersionable.MIDP_2).addParameters (PROP_BORDERS)) //NOI18N
            .addSetters (MidpSetter.createSetter ("setTitleFont", MidpVersionable.MIDP_2).addParameters (PROP_TITLE_FONT)) //NOI18N
            .addSetters (MidpSetter.createSetter ("setHeadersFont", MidpVersionable.MIDP_2).addParameters (PROP_HEADERS_FONT)) //NOI18N
            .addSetters (MidpSetter.createSetter ("setValuesFont", MidpVersionable.MIDP_2).addParameters (PROP_VALUES_FONT)); //NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // accept
            new MidpAcceptTrensferableKindPresenter().addType(TableModelCD.TYPEID, PROP_MODEL).addType(SimpleTableModelCD.TYPEID, PROP_MODEL),
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter (),
            MidpCodePresenterSupport.createAddImportPresenter (),
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_MODEL),
            // screen
            new DisplayableResourceCategoriesPresenter(),
            new TableItemDisplayPresenter(),
            new MidpAcceptProducerKindPresenter().addType(TableModelCD.TYPEID, PROP_MODEL),
            new MidpAcceptTrensferableKindPresenter().addType(FontCD.TYPEID, PROP_TITLE_FONT)
        );
    }
}
