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


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.api.model.common.DefaultVersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.PaletteSupport;
import org.netbeans.modules.vmd.api.model.common.TypesSupport;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter1;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter2;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 * @author Karol Harezlak
 */
public final class FirstCD extends ComponentDescriptor {
    
    public static final String PROPERTY_TEST = "test" ;//NOI18N
    public static final String PROPERTY_REFERENCE = "reference"; //NOI18N
    public static final String PROPERTY_READ_ONLY = "readOnly"; //NOI18N
    public static final String PROPERTY_READ_WRITE = "readWrite"; //NOI18N
    public static final String PROPERTY_ARRAY = "array"; //NOI18N
    
    public static final String DEFAULT_VALUE = "value"; //NOI18N
    public static final Object DEFAULT_VALUE_REFERENCE = null; //NOI18N
    public static final String DEFAULT_VALUE_READ_ONLY = "readOnly"; //NOI18N
    public static final String DEFAULT_VALUE_READ_WRITE = "readWrite"; //NOI18N
    
    
    public static final TypeID TYPEID_CLASS = new TypeID(TypeID.Kind.COMPONENT, "#Class"); // NOI18N
    public static final TypeID TYPEID_ARRAY = new TypeID (TypeID.Kind.PRIMITIVE, "#Array"); // NOI18N

    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(SuperFirstCD.TYPEID_CLASS, TYPEID_CLASS, true, true);
    }
    
    public Collection<String> getExcludedPropertyDescriptorNames() {
        return null;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROPERTY_TEST, TYPEID_JAVA_LANG_STRING, TypesSupport.createStringValue(DEFAULT_VALUE), true, true, Versionable.FOREVER), //NOI18N
                new PropertyDescriptor(PROPERTY_REFERENCE, SecondCD.TYPEID_CLASS, PropertyValue.createNull(), true, true, Versionable.FOREVER), // NOI18N
                new PropertyDescriptor(PROPERTY_READ_ONLY, TYPEID_JAVA_LANG_STRING, TypesSupport.createStringValue(DEFAULT_VALUE_READ_ONLY), true, true, Versionable.FOREVER), //NOI18N
                new PropertyDescriptor(PROPERTY_READ_WRITE , TYPEID_JAVA_LANG_STRING, TypesSupport.createStringValue(DEFAULT_VALUE_READ_WRITE), false, false, Versionable.FOREVER), // NOI18N
                new PropertyDescriptor(PROPERTY_ARRAY ,TYPEID_ARRAY.getArrayType().getArrayType() , PropertyValue.createEmptyArray(TYPEID_ARRAY), false, false, Versionable.FOREVER) // NOI18N
                );
    }
    
    public PaletteDescriptor getPaletteDescriptor() {
        return PaletteSupport.create(); 
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return DefaultVersionDescriptor.createForeverCompatibleVersionDescriptor();
    }
    
    public List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                new TestPresenter1(),
                new TestPresenter2(),
                new TestPresenter2()
        );
    }
    
}
