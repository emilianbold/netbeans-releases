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
package org.netbeans.modules.vmd.api.model.descriptors;


import java.util.Collection;
import java.util.List;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.DefaultVersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.PaletteSupport;

/**
 * @author Karol Harezlak
 */
public final class SuperFirstCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID_CLASS = new TypeID(TypeID.Kind.COMPONENT, "#Super"); // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(null, TYPEID_CLASS, false, true);
    }
    
    public Collection<String> getExcludedPropertyDescriptorNames() {
        return null;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return null;
    }
    
    public PaletteDescriptor getPaletteDescriptor() {
        return PaletteSupport.create();
    }
    
    public List<Presenter> createPresenters() {
        return null;
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return DefaultVersionDescriptor.createForeverCompatibleVersionDescriptor();
    }
}
