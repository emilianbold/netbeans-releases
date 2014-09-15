/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.lib.v8debug.vars;

import java.util.Map;
import org.netbeans.lib.v8debug.PropertyLong;

/**
 *
 * @author Martin Entlicher
 */
public class V8Object extends V8Value {
    
    private final String className;
    private final PropertyLong constructorFunctionHandle;
    private final PropertyLong protoObjectHandle;
    private final PropertyLong prototypeObjectHandle;
    private final Map<String, Property> properties;
    
    public V8Object(long handle, String className,
                    PropertyLong constructorFunctionHandle,
                    PropertyLong protoObjectHandle, PropertyLong prototypeObjectHandle,
                    Map<String, Property> properties, String text) {
        this(handle, V8Value.Type.Object, className, constructorFunctionHandle,
             protoObjectHandle, prototypeObjectHandle, properties, text);
    }
    
    protected V8Object(long handle, V8Value.Type type, String className,
                       PropertyLong constructorFunctionHandle,
                       PropertyLong protoObjectHandle, PropertyLong prototypeObjectHandle,
                       Map<String, Property> properties, String text) {
        super(handle, type, text);
        this.className = className;
        this.constructorFunctionHandle = constructorFunctionHandle;
        this.protoObjectHandle = protoObjectHandle;
        this.prototypeObjectHandle = prototypeObjectHandle;
        this.properties = properties;
    }

    public String getClassName() {
        return className;
    }

    public PropertyLong getConstructorFunctionHandle() {
        return constructorFunctionHandle;
    }

    public PropertyLong getProtoObjectHandle() {
        return protoObjectHandle;
    }

    public PropertyLong getPrototypeObjectHandle() {
        return prototypeObjectHandle;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }
    
    public static final class Property {
        
        public static enum Type {
            Normal,
            Field,
            Constant,
            Callbacks
        }
        
        public static final int ATTR_NONE = 0;
        public static final int ATTR_READ_ONLY = 1;
        public static final int ATTR_DONT_ENUM = 2;
        public static final int ATTR_DONT_DELETE = 4;
        public static final int ATTR_SEALED = ATTR_DONT_DELETE;
        public static final int ATTR_FROZEN = ATTR_SEALED | ATTR_READ_ONLY;
        public static final int ATTR_STRING = 8;
        public static final int ATTR_SYMBOLIC = 16;
        public static final int ATTR_PRIVATE_SYMBOL = 32;
        public static final int ATTR_DONT_SHOW = ATTR_DONT_ENUM | ATTR_SYMBOLIC | ATTR_PRIVATE_SYMBOL;
        public static final int ATTR_ABSENT = 64;
        
        private final String name;
        private final Type type;
        private final int attributes;
        private final long reference;
        
        public Property(String name, Type type, int attributes, long reference) {
            this.name = name;
            this.type = type;
            this.attributes = attributes;
            this.reference = reference;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }

        public int getAttributes() {
            return attributes;
        }

        public long getReference() {
            return reference;
        }
        
    }
}
