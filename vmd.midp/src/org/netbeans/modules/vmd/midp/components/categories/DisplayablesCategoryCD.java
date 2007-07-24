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
package org.netbeans.modules.vmd.midp.components.categories;

import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.OrderingControllerByTypeID;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.displayables.*;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.inspector.controllers.CategoryPC;

import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar 
 */

public final class DisplayablesCategoryCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#DisplayablesCategory"); // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor (CategoryCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return null;
    }

    private InspectorOrderingController[] creatOrderingControllers() {
       return new InspectorOrderingController[]{ new OrderingControllerByTypeID(10,DisplayableCD.TYPEID)};
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            // general
            InfoPresenter.createStatic (NbBundle.getMessage(DisplayablesCategoryCD.class, "DISP_Displayables"), null, CategorySupport.ICON_PATH_CATEGORY_DISPLAYABLES), // NOI18N
            // accept
            new AcceptTypePresenter (DisplayableCD.TYPEID),
            // inspector
            InspectorPositionPresenter.create(new CategoryPC()),
            InspectorOrderingPresenter.create(creatOrderingControllers()),
            // actions
            AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 20, DisplayableCD.TYPEID)
        );
    }

}
