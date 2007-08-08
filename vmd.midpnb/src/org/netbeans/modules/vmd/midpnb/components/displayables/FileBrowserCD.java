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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCode;
import org.netbeans.modules.vmd.midp.components.displayables.ScreenCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.DisplayablePC;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Karol Harezlak
 */

public final class FileBrowserCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.pda.FileBrowser"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/file_browser_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/file_browser_32.png"; // NOI18N

    public static final String ICON_PATH_SD = "org/netbeans/modules/vmd/midpnb/resources/file_browser_sd.png"; // NOI18N

    public static final String PROP_FILTER = "filter"; //NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ScreenCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_FILTER, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
       return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
               .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                   .addProperty(NbBundle.getMessage(FileBrowserCD.class, "DISP_FileBrowser_filter"), // NOI18N
                        NbBundle.getMessage(FileBrowserCD.class, "TTIP_FileBrowser_filter"), // NOI18N
                        PropertyEditorString.createInstanceWithComment(NbBundle.getMessage(FileBrowserCD.class, "PROPERTY_EDITOR_COMMENT")), // NOI18N
                        PROP_FILTER);
    }
     
    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpParameter.create(DisplayableCD.PROP_TITLE, DisplayableCD.PROP_TICKER))
            .addParameters (DisplayableCode.createCommandParameter())
            .addParameters (DisplayableCode.createCommandListenerParameter())
            .addParameters (MidpCustomCodePresenterSupport.createDisplayParameter())
            .addParameters(MidpParameter.create(PROP_FILTER))
            .addSetters (MidpSetter.createSetter("setTitle", MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_TITLE)) // NOI18N
            .addSetters (MidpSetter.createSetter("setTicker", MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_TICKER)) // NOI18N
            .addSetters (MidpSetter.createSetter("setCommandListener", MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_COMMAND_LISTENER)) // NOI18N
            .addSetters(MidpSetter.createSetter("setFilter", MidpVersionable.MIDP).addParameters(PROP_FILTER)) // NOI18N
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            .addSetters (MidpSetter.createSetter ("addCommand", MidpVersionable.MIDP).setArrayParameter (DisplayableCode.PARAM_COMMAND).addParameters (DisplayableCode.PARAM_COMMAND)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter(),
            MidpCodePresenterSupport.createAddImportPresenter(),
            // actions
            AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, CommandCD.TYPEID),
            // inspector
            InspectorPositionPresenter.create(new DisplayablePC()),
            // screen
            new DisplayableDisplayPresenter(Utilities.loadImage(ICON_PATH_SD))
        );
    }

    @Override
    public void postInitialize(DesignComponent component) {
        super.postInitialize(component);
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY_BASIC); //NOI18N
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY_PDA); //NOI18N
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, AddActionPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, CodeSetterPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, DisplayableDisplayPresenter.class);
        MidpActionsSupport.addUnusedCommandsAddActionForDisplayable(presenters);
        super.gatherPresenters(presenters);
    }

}
