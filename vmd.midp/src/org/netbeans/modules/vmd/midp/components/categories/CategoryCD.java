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

import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddAction;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class CategoryCD extends ComponentDescriptor {
    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#Category"); // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor (null, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.FOREVER;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return null;
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // inspector
            InspectorFolderPresenter.create(false),
            // actions
            ActionsPresenter.create(10, AddAction.getInstance()),
            // delete
            DeletePresenter.createIndeliblePresenter ()
        );
         
    }

}
