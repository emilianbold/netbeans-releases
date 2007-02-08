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

package org.netbeans.modules.vmd.midpnb.components.items;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.properties.common.TextFieldBC;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorResourcesComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class TableItemCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.TableItem"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/table_16.png"; // NOI18N

    private static final String PROP_TITLE = "title"; //NOI18N
    private static final String PROP_MODEL = "tableModel"; //NOI18N
    private static final String PROP_SHOW_BORDERS  = "showBoarders"; //NOI18N
    private static final String PROP_HEADERS_FONT = "headersFont"; //NOI18N
    private static final String PROP_VALUES_FONT = "valuesFont"; //NOI18N
    private static final String PROP_TITLE_FONT = "titleFont"; //NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_TITLE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_MODEL, SimpleTableModelCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_SHOW_BORDERS, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(Boolean.TRUE), false, false, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_TITLE_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_HEADERS_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_VALUES_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter (DesignEventFilterResolver.THIS_COMPONENT)
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty("Title", PropertyEditorString.createInstance(), new TextFieldBC(), PROP_TITLE)
                    .addProperty("Table Model", PropertyEditorResourcesComboBox.creater(TableItemCD.TYPEID, NbBundle.getMessage(TableItemCD.class, "LBL_TABLEMODEL_NEW"), NbBundle.getMessage(TableItemCD.class, "LBL_TABLEMODEL_NONE")), PROP_MODEL)
                    .addProperty("Show Borders", Boolean.class, PROP_SHOW_BORDERS)
                    .addProperty("Title Font", PropertyEditorResourcesComboBox.createFontPropertyEditor(), PROP_TITLE_FONT)
                    .addProperty("Headers Font", PropertyEditorResourcesComboBox.createFontPropertyEditor(), PROP_HEADERS_FONT)
                    .addProperty("Values Font", PropertyEditorResourcesComboBox.createFontPropertyEditor(), PROP_VALUES_FONT);
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                createPropertiesPresenter()
        );
    }
    
    public void postInitialize (DesignComponent component) {
        MidpProjectSupport.addLibraryToProject(component.getDocument(), AbstractInfoScreenCD.MIDP_NB_LIBRARY);
    }
    
}
