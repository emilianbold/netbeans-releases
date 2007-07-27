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
 *
 */
package org.netbeans.modules.vmd.midpnb.components.resources;

import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.FolderPositionControllerFactory;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.resources.ResourcesSupport;
import org.netbeans.modules.vmd.midp.inspector.controllers.ResourcePC;
import org.netbeans.modules.vmd.midp.screen.ResourceSRItemPresenter;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public class TableModelCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.TableModel"); // NOI18N

    public static final TypeID TYPEID_VALUES = MidpTypes.TYPEID_JAVA_LANG_STRING.getArrayType().getArrayType ();
    public static final TypeID TYPEID_COLUMN_NAMES = MidpTypes.TYPEID_JAVA_LANG_STRING.getArrayType ();

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/resource_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/resource_32.png"; // NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ClassCD.TYPEID, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (ClassCD.PROP_INSTANCE_NAME, InstanceNameResolver.createFromSuggested (component, "tableModel")); // NOI18N
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY);
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return null;
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, InfoPresenter.class);
        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            //info
            ResourcesSupport.createResourceInfoResolver(),
             // inspector
            new InspectorFolderComponentPresenter (true),
            InspectorPositionPresenter.create(new ResourcePC (), FolderPositionControllerFactory.createHierarchical()),
             // screen
            new ResourceSRItemPresenter (InfoPresenter.NameType.TERTIARY)
        );
    }

}
