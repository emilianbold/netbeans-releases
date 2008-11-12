/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.api.model.utils;

import org.netbeans.modules.vmd.api.model.PrimitiveDescriptor;
import org.netbeans.modules.vmd.api.model.PrimitiveDescriptorFactory;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;


@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.model.PrimitiveDescriptorFactory.class)
public final class TestPrimitiveDescriptor implements PrimitiveDescriptorFactory {

    private static final IntPD intPD = new IntPD();
    private static final LongPD longPD = new LongPD();
    private static final StringPD stringPD = new StringPD();
    private static final BooleanPD booleanPD = new BooleanPD();
    
    public PrimitiveDescriptor getDescriptorForTypeIDString(String string) {
        if (TYPEID_INT.getString().equals(string))
            return intPD;
        if (TYPEID_LONG.getString().equals(string))
            return longPD;
        if (TYPEID_BOOLEAN.getString().equals(string))
            return booleanPD;
        if (TYPEID_JAVA_LANG_STRING.getString().equals(string))
            return stringPD;
        if (TYPEID_JAVA_CODE.getString().equals(string))
            return stringPD;
        //TODO
        return null;
    }

    public String getProjectType() {
        return "PrimitiveDescriptorTest"; //NOI18N
    }
    
    private static class IntPD implements PrimitiveDescriptor {
        
        public String serialize(Object value) {
            return value.toString();
        }
        
        public Object deserialize(String serialized) {
            return Integer.parseInt(serialized);
        }
        
        public boolean isValidInstance(Object object) {
            return object instanceof Integer;
        }
        
    }
    
    private static class LongPD implements PrimitiveDescriptor {
        
        public String serialize(Object value) {
            return value.toString();
        }
        
        public Object deserialize(String serialized) {
            return Long.parseLong(serialized);
        }
        
        public boolean isValidInstance(Object object) {
            return object instanceof Long;
        }
        
    }
    
    private static class BooleanPD implements PrimitiveDescriptor {
        
        public String serialize(Object value) {
            return value.toString();
        }
        
        public Object deserialize(String serialized) {
            return Integer.parseInt(serialized);
        }
        
        public boolean isValidInstance(Object object) {
            return object instanceof Boolean;
        }
        
    }
    
    private static class StringPD implements PrimitiveDescriptor {
        
        public String serialize(Object value) {
            return (String) value;
        }
        
        public Object deserialize(String serialized) {
            return serialized;
        }
        
        public boolean isValidInstance(Object object) {
            return object instanceof String;
        }
    }
}
