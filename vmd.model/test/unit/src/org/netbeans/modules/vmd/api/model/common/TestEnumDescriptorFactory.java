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
package org.netbeans.modules.vmd.api.model.common;

import java.util.Arrays;
import java.util.Collection;

import org.netbeans.modules.vmd.api.model.EnumDescriptor;
import org.netbeans.modules.vmd.api.model.EnumDescriptorFactory;

/**
 *
 * @author Karol Harezlak
 */
public class TestEnumDescriptorFactory implements EnumDescriptorFactory {
    
    private PrimitiveDescriptorSupport descriptor = new PrimitiveDescriptorSupport();
    private String[] vals =
            new String[] { "VALUE1", // NOI18N
            "VALUE2", // NOI18N
            "TEST1_VALUE1", // NOI18N
            "TEST1_VALUE2", // NOI18N
            "TEST2_VALUE1", // NOI18N
            "TEST2_VALUE2" }; // NOI18N
    
    /**
     * Creates a new instance of TestEnumDescriptorFactory
     */
    public TestEnumDescriptorFactory() {
    }
    
    public EnumDescriptor getDescriptorForTypeIDString(String string) {
        if ("ENUM".equals(string)) // NOI18N
            return new EnumDescriptor() {
               
                public boolean isValidInstance(Object object) {
                    return true;
                }
                
                public Object deserialize(String serialized) {
                    return serialized;
                }
                public String serialize(Object value) {
                    return (String) value;
                }
                public Collection<?> values() {
                    return Arrays.asList(vals);
                }
                
            };
            return null;
    }

    public String getProjectType() {
        return descriptor.getProjectType();
    }
    
}
