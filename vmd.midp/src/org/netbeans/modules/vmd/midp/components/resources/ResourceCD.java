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
package org.netbeans.modules.vmd.midp.components.resources;

import java.util.Arrays;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;

import java.util.List;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.FolderPositionControllerFactory;
import org.netbeans.modules.vmd.midp.inspector.controllers.ResourcePC;

/**
 * @author David Kaspar
 */

public final class ResourceCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#Resource"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/resource_16.png"; // NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ClassCD.TYPEID, ResourceCD.TYPEID, false, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return null;
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // inspector
            InspectorFolderPresenter.create(true),
            InspectorPositionPresenter.create(new ResourcePC(), FolderPositionControllerFactory.createHierarchical())
        );
    }
    
}
