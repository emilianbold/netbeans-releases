/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.vmd.api.model;

import java.util.*;

/**
 * This immutable class represents a property value.
 * <p>
 * This class holds information about kind, type id, user code, component, value, array.
 * The kind could be user code, null, component reference, non-array value, enum value, array value.
 * This type id represents the type id of the property value for all kinds expect user code and null value.
 * This user code holds the text of the user code in target language. The component is a reference to a component in case
 * of reference kind. The value holds the value of enum or a non-array value property. The array holds a value of an array.
 * <p>
 * @author David Kaspar
 */
public final class PropertyValue {

    private static final char USER_CODE_ID = 'U'; // NOI18N
    private static final char NULL_ID = 'N'; // NOI18N
    private static final char REFERENCE_ID = 'R'; // NOI18N
    private static final char VALUE_ID = 'V'; // NOI18N
    private static final char ENUM_ID = 'E'; // NOI18N
    private static final char ARRAY_ID = 'A'; // NOI18N
    private static final char ARRAY_SIZE_SEPARATOR = ':'; // could be any characted except digits // NOI18N
    private static final char ENCODED_LENGTH_SEPARATOR = '_'; // could be any characted except digits // NOI18N
    private static final PropertyValue NULL = new PropertyValue(Kind.NULL);
    private static final List<PropertyValue> EMPTY_ARRAY = new ArrayList<PropertyValue>(0);

/**
     * The property value kind.
     */
    public enum Kind {

        // WARNING - when changing this enum, review DesignComponent.setComponentDescriptor also
        /**
         * The user code.
         */
        USERCODE /**
         * The null value.
         */
        , NULL /**
         * Component reference.
         */
        , REFERENCE /**
         * Non-array value.
         */
        , VALUE /**
         * Enum value.
         */
        , ENUM /**
         * Array value.
         */
        , ARRAY
    }
    private final Kind kind;
    private TypeID type;
    private String userCode;
    private DesignComponent component; // HINT - maybe it should be UID
    private PrimitiveDescriptor descriptor;
    private Object value;
    private List<PropertyValue> array;

    /**
     * Create a new property value representing a user code.
     * @param userCode the user code in target language
     * @return the propert value
     */
    public static PropertyValue createUserCode(String userCode) {
        assert userCode != null;

        PropertyValue value = new PropertyValue(Kind.USERCODE);
        value.userCode = userCode;
        return value;
    }

    /**
     * Creates a new property value representing a component reference.
     * @param component the component
     * @return the property value
     */
    public static PropertyValue createComponentReference(DesignComponent component) {
        assert component != null;
        return component.getReferenceValue();
    }

    static PropertyValue createComponentReferenceCore(DesignComponent component) {
        assert Debug.isFriend(DesignComponent.class, "<init>"); // NOI18N
        assert component != null;
        TypeID type = component.getType();
        assert type.getKind() == TypeID.Kind.COMPONENT;

        PropertyValue val = new PropertyValue(Kind.REFERENCE);
        val.type = type;
        val.component = component;
        return val;
    }

    /**
     * Creates a new property value representing a null.
     * @return the property value
     */
    public static PropertyValue createNull() {
        return NULL;
    }

    /**
     * Creates a property value representing a primitive value.
     * @param descriptor the primitive descriptor
     * @param type the non-array type id that is primitive
     * @param value the object that represents the value in the design time
     * @return the property value
     */
    public static PropertyValue createValue(PrimitiveDescriptor descriptor, TypeID type, Object value) {
        assert descriptor != null;
        assert type != null;
        assert type.getKind() == TypeID.Kind.PRIMITIVE;
        assert type.getDimension() == 0;
        assert value != null;
        assert descriptor.isValidInstance(value);

        PropertyValue val = new PropertyValue(Kind.VALUE);

        val.descriptor = descriptor;
        val.type = type;
        val.value = value;
        return val;
    }

    /**
     * Creates a property value representing a primitive or an enum value.
     * @param projectType the project type
     * @param type the non-array type id that is primitive
     * @param value the object that represents the value in the design time
     * @return the property value
     */
    public static PropertyValue createValue(String projectType, TypeID type, Object value) {
        if (type.getKind() == TypeID.Kind.ENUM) {
            return createEnumValue(EnumDescriptorFactoryRegistry.getDescriptor(projectType, type), type, value);
        } else {
            return createValue(PrimitiveDescriptorFactoryRegistry.getDescriptor(projectType, type), type, value);
        }
    }

    /**
     * Creates a property value representing a enum value.
     * @param descriptor the enum descriptor
     * @param type the non-array type id that is enum
     * @param value the object that represents the value in the design time
     * @return the property value
     */
    public static PropertyValue createEnumValue(EnumDescriptor descriptor, TypeID type, Object value) {
        assert descriptor != null;
        assert type != null;
        assert type.getKind() == TypeID.Kind.ENUM;
        assert type.getDimension() == 0;
        assert value != null;
        assert descriptor.isValidInstance(value);

        PropertyValue val = new PropertyValue(Kind.ENUM);
        assert descriptor.values().contains(value);

        val.descriptor = descriptor;
        val.type = type;
        val.value = value;
        return val;
    }

    /**
     * Creates a property value representing an array value.
     * @param componentType the type id of held objects (not array type id)
     * @param array the list of property values that represent the array of values in the design time
     * @return the property value
     */
    public static PropertyValue createArray(TypeID componentType, List<PropertyValue> array) {
        assert componentType != null;
        assert array != null;
        assert createArrayAssert(array, componentType);

        PropertyValue val = new PropertyValue(Kind.ARRAY);
        val.type = componentType.getArrayType();
        val.array = Collections.unmodifiableList(new ArrayList<PropertyValue>(array));
        return val;
    }

    private static boolean createArrayAssert(List<PropertyValue> array, TypeID componentType) {
        for (PropertyValue propertyValue : array) {
            assert propertyValue != null;
            assert propertyValue.isCompatible(componentType);
        }
        return true;
    }

    /**
     * Create a property value representng an empty array value.
     * @param componentType the type id of held objects (not array type id)
     * @return the property value
     */
    public static PropertyValue createEmptyArray(TypeID componentType) {
        return createArray(componentType, EMPTY_ARRAY);
    }

    private PropertyValue(Kind valueType) {
        this.kind = valueType;
    }

    /**
     * Returns a kind.
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Returns a type id.
     * @return the type id
     */
    public TypeID getType() {
//        assert kind != Kind.USERCODE  &&  kind != Kind.NULL;
        return type;
    }

    /**
     * Returns a user code.
     * @return the user code
     */
    public String getUserCode() {
//        assert kind == Kind.USERCODE;
        return userCode;
    }

    /**
     * Returns a component.
     * @return the component
     */
    public DesignComponent getComponent() {
//        assert kind == Kind.REFERENCE;
        return component;
    }

    /**
     * Returns primitive or enum value (not component, not array).
     * @return the value
     */
    public Object getPrimitiveValue() {
//        assert kind == Kind.VALUE  ||  kind == Kind.ENUM;
        return value;
    }

    /**
     * Returns a list of property values.
     * @return the list
     */
    public List<PropertyValue> getArray() {
//        assert kind == Kind.ARRAY;
        return array;
    }

    void collectAllComponentReferences(Collection<DesignComponent> references) {
        assert Debug.isFriend(DesignComponent.class, "writeProperty") || Debug.isFriend(DesignComponent.class, "setComponentDescriptor") || Debug.isFriend(PropertyValue.class, "collectAllComponentReferences") || Debug.isFriend(Debug.class, "collectAllComponentReferences"); // NOI18N
        switch (kind) {
            case USERCODE:
            case NULL:
            case VALUE:
            case ENUM:
                return;
            case REFERENCE:
                references.add(component);
                return;
            case ARRAY:
                if (type.getKind() == TypeID.Kind.COMPONENT) {
                    for (PropertyValue propertyValue : array) {
                        propertyValue.collectAllComponentReferences(references);
                    }
                }
                return;
        }
        Debug.error("Invalid state", this); // NOI18N
    }

    /**
     * Returns whether the required type id is compatible with this type id. Compatible means the required type equals
     * to the one stored inside this property value. In Reference case, it checks its compatibility using descriptor registry.
     * @param requiredType the required type
     * @return true if compatible
     */
    public boolean isCompatible(TypeID requiredType) {
        if (type == null) {
            return true;
        }
        if (kind == Kind.REFERENCE) {
            return component.getDocument().getDescriptorRegistry().isInHierarchy(requiredType, component.getType());
        }
        return type.equals(requiredType);
    }

    /**
     * Returns whether the property value is compatible with a specified property descriptor.
     * @param propertyDescriptor the property descriptor
     * @return true, if the property value is allowed for the property descriptor
     */
    public boolean isCompatible(PropertyDescriptor propertyDescriptor) {
        if (propertyDescriptor == null) {
            return false;
        }
        if (kind == Kind.NULL) {
            if (!propertyDescriptor.isAllowNull()) {
                PropertyValue defaultValue = propertyDescriptor.getDefaultValue();
                if (defaultValue == null || defaultValue.getKind() != PropertyValue.Kind.NULL) {
                    // HACK for PropertyDescriptor for disallowing to use null values
                    return false;
                }
            }
        } else if (kind == Kind.USERCODE) {
            if (!propertyDescriptor.isAllowUserCode()) {
                return false;
            }
        }
        return isCompatible(propertyDescriptor.getType());
    }

    /**
     * Returns an encoded string of the property value.
     * @return the encoded string
     */
    public String serialize() {
        switch (kind) {
            case USERCODE:
                return new StringBuilder().append(USER_CODE_ID).append(userCode).toString();
            case NULL:
                return Character.toString(NULL_ID);
            case REFERENCE:
                return new StringBuilder().append(REFERENCE_ID).append(component.getComponentID()).toString();
            case VALUE:
                {
                    return new StringBuilder().append(VALUE_ID).append(descriptor.serialize(value)).toString();
                }
            case ENUM:
                {
                    return new StringBuilder().append(ENUM_ID).append(descriptor.serialize(value)).toString();
                }
            case ARRAY:
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ARRAY_ID).append(array.size()).append(ARRAY_SIZE_SEPARATOR);
                    for (PropertyValue propertyValue : array) {
                        String serialized = propertyValue.serialize();
                        sb.append(serialized.length()).append(ENCODED_LENGTH_SEPARATOR).append(serialized);
                    }
                    return sb.toString();
                }
        }

        throw Debug.error("Cannot serialize property value", type); // NOI18N
    }

    /**
     * Creates a property value from the encoded string that represents the value.
     * @param serialized the encoded string
     * @param document the document for resolving component references
     * @param type the type id of the encoded string
     * @return the property value
     */
    public static PropertyValue deserialize(String serialized, DesignDocument document, TypeID type) throws Exception {
        assert serialized != null && serialized.length() >= 1;
        assert document != null && type != null;

        switch (serialized.charAt(0)) {
            case USER_CODE_ID:
                if (serialized.substring(1) == null) {
                    throw new IllegalArgumentException();
                }
                return createUserCode(serialized.substring(1));
            case NULL_ID:
                return createNull();
            case REFERENCE_ID:
                {
                    int componentID;
                    componentID = Integer.parseInt(serialized.substring(1));
                    if (document.getComponentByUID(componentID) == null) {
                        throw new IllegalArgumentException("No component for given serilezed value"); // NOI18N
                    }
                    return createComponentReference(document.getComponentByUID(componentID));
                }
            case VALUE_ID:
                {
                    PrimitiveDescriptor descriptor = PrimitiveDescriptorFactoryRegistry.getDescriptor(document.getDocumentInterface().getProjectType(), type);
                    if (descriptor == null) {
                        throw new IllegalArgumentException();
                    }
                    return createValue(descriptor, type, descriptor.deserialize(serialized.substring(1)));
                }
            case ENUM_ID:
                {
                    EnumDescriptor descriptor = EnumDescriptorFactoryRegistry.getDescriptor(document.getDocumentInterface().getProjectType(), type);
                    if (descriptor == null) {
                        throw new IllegalArgumentException();
                    }
                    return createEnumValue(descriptor, type, descriptor.deserialize(serialized.substring(1)));
                }
            case ARRAY_ID:
                {
                    int pos = 1;
                    int arrayLengthIndex = serialized.indexOf(ARRAY_SIZE_SEPARATOR, pos);
                    assert arrayLengthIndex > pos;

                    int arrayLength;
                    arrayLength = Integer.parseInt(serialized.substring(pos, arrayLengthIndex));
                    assert arrayLength >= 0;
                    pos = arrayLengthIndex + 1;

                    TypeID componentType = type.getComponentType();
                    ArrayList<PropertyValue> propertyValues = new ArrayList<PropertyValue>();

                    for (int a = 0; a < arrayLength; a++) {
                        int index = serialized.indexOf(ENCODED_LENGTH_SEPARATOR, pos);
                        assert index > pos;

                        int elementLength;
                        elementLength = Integer.parseInt(serialized.substring(pos, index));
                        assert elementLength >= 0;
                        pos = index + 1;
                        propertyValues.add(deserialize(serialized.substring(pos, pos + elementLength), document, componentType));
                        pos += elementLength;
                    }

                    return createArray(componentType, propertyValues);
                }
        }
        Debug.warning("Cannot deserialize property value", type, serialized); // NOI18N
        throw new IllegalArgumentException("Cannot deserialize property value " + type + " " + serialized); //NOI18N
    }

    /**
     * Returns the encoded string.
     * @return the encoded string
     */
    @Override
    public String toString() {
        return serialize();
    }
}
