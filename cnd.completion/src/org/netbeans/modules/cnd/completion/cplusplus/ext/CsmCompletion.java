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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 * Java completion query specifications
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
abstract public class CsmCompletion {

    public static final int PUBLIC_LEVEL = 3;
    public static final int PROTECTED_LEVEL = 2;
    public static final int PACKAGE_LEVEL = 1;
    public static final int PRIVATE_LEVEL = 0;
    public static final SimpleClass BOOLEAN_CLASS = new SimpleClass("boolean", ""); // NOI18N
    public static final SimpleClass BYTE_CLASS = new SimpleClass("byte", ""); // NOI18N
    public static final SimpleClass CHAR_CLASS = new SimpleClass("char", ""); // NOI18N
    public static final SimpleClass DOUBLE_CLASS = new SimpleClass("double", ""); // NOI18N
    public static final SimpleClass FLOAT_CLASS = new SimpleClass("float", ""); // NOI18N
    public static final SimpleClass INT_CLASS = new SimpleClass("int", ""); // NOI18N
    public static final SimpleClass LONG_CLASS = new SimpleClass("long", ""); // NOI18N
    public static final SimpleClass SHORT_CLASS = new SimpleClass("short", ""); // NOI18N
    public static final SimpleClass VOID_CLASS = new SimpleClass("void", ""); // NOI18N
    public static final BaseType BOOLEAN_TYPE = new BaseType(BOOLEAN_CLASS, 0, false, 0, false);
    public static final BaseType BYTE_TYPE = new BaseType(BYTE_CLASS, 0, false, 0, false);
    public static final BaseType CHAR_TYPE = new BaseType(CHAR_CLASS, 0, false, 0, false);
    public static final BaseType DOUBLE_TYPE = new BaseType(DOUBLE_CLASS, 0, false, 0, false);
    public static final BaseType FLOAT_TYPE = new BaseType(FLOAT_CLASS, 0, false, 0, false);
    public static final BaseType INT_TYPE = new BaseType(INT_CLASS, 0, false, 0, false);
    public static final BaseType LONG_TYPE = new BaseType(LONG_CLASS, 0, false, 0, false);
    public static final BaseType SHORT_TYPE = new BaseType(SHORT_CLASS, 0, false, 0, false);
    public static final BaseType VOID_TYPE = new BaseType(VOID_CLASS, 0, false, 0, false);
    public static final SimpleClass INVALID_CLASS = new SimpleClass("", ""); // NOI18N
    public static final BaseType INVALID_TYPE = new BaseType(INVALID_CLASS, 0, false, 0, false);
    public static final SimpleClass NULL_CLASS = new SimpleClass("null", ""); // NOI18N
    public static final BaseType NULL_TYPE = new BaseType(NULL_CLASS, 0, false, 0, false);
    public static final SimpleClass OBJECT_CLASS_ARRAY = new SimpleClass("java.lang.Object[]", "java.lang".length(), true); // NOI18N
    public static final BaseType OBJECT_TYPE_ARRAY = new BaseType(OBJECT_CLASS_ARRAY, 0, false, 0, false);
    public static final SimpleClass OBJECT_CLASS = new SimpleClass("java.lang.Object", "java.lang".length(), true); // NOI18N
    public static final BaseType OBJECT_TYPE = new BaseType(OBJECT_CLASS, 0, false, 0, false);
    public static final SimpleClass CLASS_CLASS = new SimpleClass("java.lang.Class", "java.lang".length(), true); // NOI18N
    public static final BaseType CLASS_TYPE = new BaseType(CLASS_CLASS, 0, false, 0, false);
    public static final SimpleClass STRING_CLASS = new SimpleClass("char", 0, true); // NOI18N
    public static final BaseType STRING_TYPE = new BaseType(STRING_CLASS, 1, false, 0, false);
    public static final SimpleClass CONST_STRING_CLASS = new SimpleClass("const char", 0, true); // NOI18N
    public static final BaseType CONST_STRING_TYPE = new BaseType(CONST_STRING_CLASS, 1, false, 0, true);
    public static final BaseType CONST_BOOLEAN_TYPE = new BaseType(BOOLEAN_CLASS, 0, false, 0, true);
    public static final BaseType CONST_BYTE_TYPE = new BaseType(BYTE_CLASS, 0, false, 0, true);
    public static final BaseType CONST_CHAR_TYPE = new BaseType(CHAR_CLASS, 0, false, 0, true);
    public static final BaseType CONST_DOUBLE_TYPE = new BaseType(DOUBLE_CLASS, 0, false, 0, true);
    public static final BaseType CONST_FLOAT_TYPE = new BaseType(FLOAT_CLASS, 0, false, 0, true);
    public static final BaseType CONST_INT_TYPE = new BaseType(INT_CLASS, 0, false, 0, true);
    public static final BaseType CONST_LONG_TYPE = new BaseType(LONG_CLASS, 0, false, 0, true);
    public static final BaseType CONST_SHORT_TYPE = new BaseType(SHORT_CLASS, 0, false, 0, true);
    public static final BaseType CONST_VOID_TYPE = new BaseType(VOID_CLASS, 0, false, 0, true);

    // the bit for local member. the modificator is not saved within this bit.
    public static final int LOCAL_MEMBER_BIT = (1 << 29);

    // the bit for deprecated flag. it is saved to copde completion  DB
    public static final int DEPRECATED_BIT = (1 << 20);
    private static final Map<CharSequence, CsmClassifier> str2PrimitiveClass = new HashMap<CharSequence, CsmClassifier>();
    private static final Map<CharSequence, BaseType> str2PrimitiveType = new HashMap<CharSequence, BaseType>();
    private static final Map<CharSequence, BaseType> str2PredefinedType = new HashMap<CharSequence, BaseType>();


    static {
        // initialize primitive types cache
        BaseType[] types = new BaseType[]{
            BOOLEAN_TYPE, BYTE_TYPE, CHAR_TYPE, DOUBLE_TYPE, FLOAT_TYPE,
            INT_TYPE, LONG_TYPE, SHORT_TYPE, VOID_TYPE
        };

        for (int i = types.length - 1; i >= 0; i--) {
            String typeName = types[i].getClassifier().getName().toString();
            str2PrimitiveClass.put(typeName, types[i].getClassifier());
            str2PrimitiveType.put(typeName, types[i]);
        }

        // initialize predefined types cache
        types = new BaseType[]{
                    NULL_TYPE, OBJECT_TYPE_ARRAY, OBJECT_TYPE, CLASS_TYPE, STRING_TYPE, CONST_STRING_TYPE,
                    CONST_BOOLEAN_TYPE, CONST_BYTE_TYPE, CONST_CHAR_TYPE, CONST_DOUBLE_TYPE, CONST_FLOAT_TYPE,
                    CONST_INT_TYPE, CONST_LONG_TYPE, CONST_SHORT_TYPE, CONST_VOID_TYPE
                 };

        for (int i = types.length - 1; i >= 0; i--) {
            String typeName = types[i].getClassifier().getName().toString();
            str2PredefinedType.put(typeName, types[i]);
            str2PredefinedType.put(types[i].getClassifier().getQualifiedName(), types[i]);
            str2PredefinedType.put(types[i].format(true), types[i]);
        }
    }
    public static final CsmParameter[] EMPTY_PARAMETERS = new CsmParameter[0];
    public static final CsmClassifier[] EMPTY_CLASSES = new CsmClassifier[0];
    public static final CsmNamespace[] EMPTY_NAMESPACES = new CsmNamespace[0];
    public static final CsmField[] EMPTY_FIELDS = new CsmField[0];
    public static final CsmConstructor[] EMPTY_CONSTRUCTORS = new CsmConstructor[0];
    public static final CsmMethod[] EMPTY_METHODS = new CsmMethod[0];
    public static final String SCOPE = "::";  //NOI18N
    private static int debugMode;
    /** Map holding the simple class instances */
//    private static HashMap classCache = new HashMap(5003);
    /** Map holding the cached types */
    //private static HashMap typeCache = new HashMap(5003);
    /** Debug expression creation */
    public static final int DEBUG_EXP = 1;
    /** Debug finding packages/classes/fields/methods */
    public static final int DEBUG_FIND = 2;

    private CsmCompletion() {
    }

    /** Get level from modifiers. */
    public static int getLevel(int modifiers) {
        if ((modifiers & Modifier.PUBLIC) != 0) {
            return PUBLIC_LEVEL;
        } else if ((modifiers & Modifier.PROTECTED) != 0) {
            return PROTECTED_LEVEL;
        } else if ((modifiers & Modifier.PRIVATE) == 0) {
            return PACKAGE_LEVEL;
        } else {
            return PRIVATE_LEVEL;
        }
    }

    public static boolean isPrimitiveClassName(String s) {
        return CndLexerUtilities.isType(s);
    }

    public static boolean isPrimitiveClass(CsmClassifier c) {
//        return (c.getPackageName().length() == 0)
//               && isPrimitiveClassName(c.getName());
        return isPrimitiveClassName(c.getName().toString());
    }
//
//    public static CsmClassifier getPrimitiveClass(String s) {
//        return str2PrimitiveClass.get(s);
//    }

    private static BaseType getPrimitiveType(String s) {
        return str2PrimitiveType.get(s);
    }

    public static CsmType getPredefinedType(CsmFile containingFile, int start, int end, String s) {
        BaseType baseType = getPrimitiveType(s);
        if (baseType == null) {
            baseType = str2PredefinedType.get(s);
        }
        if (baseType != null) {
            // wrap with correct offsetable information
            return new OffsetableType(baseType, containingFile, start, end);
        } else {
            return null;
        }
    }

    public static Iterator getPrimitiveClassIterator() {
        return str2PrimitiveClass.values().iterator();
    }

    public static CsmClassifier getSimpleClass(CsmClassifier clazz) {
        CsmClassifier cls = null;//(CsmClassifier)classCache.get(fullClassName);
        if (clazz != null) {
            cls = new SimpleClass(clazz);
        }
        return cls;
    }

    public static CsmClassifier createSimpleClass(String fullClassName) {
        int nameInd = fullClassName.lastIndexOf(CsmCompletion.SCOPE) + 1;
        return createSimpleClass(fullClassName.substring(nameInd),
                (nameInd > 0) ? fullClassName.substring(0, nameInd - 1) : ""); // NOI18N
    }

    public static CsmClassifier createSimpleClass(String name, String packageName) {
        return new SimpleClass(name, packageName, CsmDeclaration.Kind.CLASS);
    }

    public static CsmType createType(CsmClassifier cls, int ptrDepth, int arrayDepth, boolean _const) {
        return new BaseType(cls, ptrDepth, false, arrayDepth, _const);
    }

    /** returns type for dereferenced object
     * @param obj
     * @return
     */
    public static CsmType getObjectType(CsmObject obj) {
        CsmType type = null;
        if (CsmKindUtilities.isClassifier(obj)) {
            type = CsmCompletion.getType((CsmClassifier) obj, 0, false, 0, false);
        } else if (CsmKindUtilities.isFunction(obj)) {
            CsmFunction fun = (CsmFunction) obj;
            if (CsmKindUtilities.isConstructor(fun)) {
                CsmClassifier cls = ((CsmConstructor) obj).getContainingClass();
                type = CsmCompletion.getType(cls, 0, false, 0, false);
            } else {
                type = fun.getReturnType();
            }
        } else if (CsmKindUtilities.isVariable(obj)) {
            type = ((CsmVariable) obj).getType();
        } else if (CsmKindUtilities.isEnumerator(obj)) {
            type = INT_TYPE;
        } else {
            type = null;
        }
        return type;
    }

    /** Create new type or get the existing one from the cache. The cache holds
     * the arrays with the increasing array depth for the particular class
     * as the members. Simple class is used for the caching to make it independent
     * on the real completion classes that can become obsolete and thus should
     * be garbage collected.
     */
    public static CsmType getType(CsmClassifier cls, int pointerDepth, boolean reference, int arrayDepth, boolean _const) {
        if (cls == null) {
            return null;
        }
        return new BaseType(cls, pointerDepth, reference, arrayDepth, _const);
    }

    public static class SimpleClass implements CsmClassifier {

        protected CharSequence name;
        protected String packageName = "";
        protected CharSequence fullName;
        protected CsmDeclaration.Kind kind;
        // a cache
        // our toString() is called very often by JCCellRenderer and is too
        // expensive due to string replace operations and string concatenation
        private String stringValue;
        private CsmClassifier clazz;

        public SimpleClass(CsmClassifier clazz) {
            this.clazz = clazz;
            this.name = clazz.getName();
            this.fullName = clazz.getQualifiedName();
        }

        public SimpleClass(String name, String packageName, CsmDeclaration.Kind kind) {
            this.name = name;
            this.packageName = packageName != null ? packageName : "";
//            if (name == null || packageName == null) {
            this.kind = kind;
            if (name == null || kind == null) {
                throw new NullPointerException(
                        "className=" + name + ", kind=" + kind); // NOI18N
            }
        }

        public SimpleClass(String name, String packageName) {
            this(name, packageName, CsmDeclaration.Kind.BUILT_IN);
        }

        public SimpleClass(String fullName, int packageNameLen, boolean intern) {
            this.fullName = fullName;
            // <> Fix BugId 056449, java.lang.StringIndexOutOfBoundsException: String index out of range: -12
            if (packageNameLen <= 0 || packageNameLen >= fullName.length()) {
                // </>
                name = fullName;
                packageName = ""; // NOI18N
            } else {
                // use interned strings here
                name = fullName.substring(packageNameLen + 1);
                packageName = fullName.substring(0, packageNameLen);
                if (intern) {
                    name = ((String) name).intern();
                    packageName = packageName.intern();
                }
            }
        }

        SimpleClass() {
        }

        public final CharSequence getName() {
            if (clazz != null) {
                return clazz.getName();
            }
            return name;
        }

        public final String getPackageName() {
            return packageName;
        }

        public CharSequence getQualifiedName() {
            if (clazz != null) {
                return clazz.getQualifiedName();
            }
            if (fullName == null) {
                fullName = (packageName.length() > 0) ? (packageName + "." + name) : name; // NOI18N
            }
            return fullName;
        }

        public CharSequence getUniqueName() {
            return getQualifiedName();
        }

        public int getTagOffset() {
            return -1;
        }

        public boolean isInterface() {
            return false;
        }

        public int getModifiers() {
            return 0;
        }

        public CsmClassifier getSuperclass() {
            return null;
        }

        public CsmClassifier[] getInterfaces() {
            return EMPTY_CLASSES;
        }

        public CsmField[] getFields() {
            return EMPTY_FIELDS;
        }

        public CsmConstructor[] getConstructors() {
            return EMPTY_CONSTRUCTORS;
        }

        public CsmMethod[] getMethods() {
            return EMPTY_METHODS;
        }

        public int compareTo(Object o) {
            if (this == o) {
                return 0;
            }
            CsmClassifier c = (CsmClassifier) o;

//XXX            int order = packageName.compareTo(c.getPackageName());
            int order = 0;
            if (order == 0) {
                order = name.toString().compareTo(c.getName().toString());
            }
            return order;
        }

        @Override
        public int hashCode() {
            return name.hashCode() ^ packageName.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CsmClassifier) {
                CsmClassifier c = (CsmClassifier) o;
                String className = (c.getName() == null) ? null : c.getName().toString().replace('.', '$');
                String thisName = name.toString().replace('.', '$');
                return thisName.equals(className);//XXX && packageName.equals(c.getPackageName());
            }
            return false;
        }

        @Override
        public String toString() {
            if (stringValue == null) {
                stringValue = (getPackageName().length() > 0)
                        ? getPackageName() + '.' + getName().toString().replace('.', '$')
                        : getName().toString().replace('.', '$');
            }
            return stringValue;
        }

        public CsmDeclaration.Kind getKind() {
            if (clazz != null) {
                return clazz.getKind();
            }
            return kind;
        }

        public CsmScope getScope() {
            if (clazz != null) {
                return clazz.getScope();
            }
            return null;
        }

        public boolean isValid() {
            return CsmBaseUtilities.isValid(clazz);
        }
    }

    /** Description of the type */
    public static class BaseType implements CsmType {

        protected CsmClassifier clazz;
        protected int arrayDepth;
        protected int pointerDepth;
        protected boolean reference;
        protected boolean _const;

        public BaseType(CsmClassifier clazz, int pointerDepth, boolean reference, int arrayDepth, boolean _const) {
            this.clazz = clazz;
            this.arrayDepth = arrayDepth;
            this.pointerDepth = pointerDepth;
            this.reference = reference;
            this._const = _const;
            if (arrayDepth < 0) {
                throw new IllegalArgumentException("Array depth " + arrayDepth + " < 0."); // NOI18N
            }
        }

        BaseType() {
        }

        public int getArrayDepth() {
            return arrayDepth;
        }

        public String format(boolean useFullName) {
            StringBuilder sb = new StringBuilder();
            if(_const) {
                sb.append("const "); // NOI18N
            }
            if (false && this.isInstantiation()) {
                sb.append(CsmInstantiationProvider.getDefault().getInstantiatedText(this));
            } else {
                CsmClassifier classifier = getClassifier();
                if (false && CsmKindUtilities.isTemplate(classifier)) {
                    sb.append(CsmInstantiationProvider.getDefault().getTemplateSignature(((CsmTemplate)classifier)));
                } else {
                    sb.append(classifier.getQualifiedName());
                }
            }
            int pd = pointerDepth;
            while (pd > 0) {
                sb.append("*"); // NOI18N
                pd--;
            }
            int ad = arrayDepth;
            while (ad > 0) {
                sb.append("[]"); // NOI18N
                ad--;
            }
            return sb.toString();
        }

        public int compareTo(Object o) {
            if (this == o) {
                return 0;
            }
            CsmType t = (CsmType) o;
            int order = clazz.getQualifiedName().toString().compareTo(t.getClassifier().getQualifiedName().toString());
            if (order == 0) {
                order = arrayDepth - t.getArrayDepth();
            }
            return order;
        }

        @Override
        public int hashCode() {
            return clazz.hashCode() + arrayDepth;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CsmType) {
                CsmType t = (CsmType) o;
                return clazz.equals(t.getClassifier()) &&
                        arrayDepth == t.getArrayDepth() &&
                        pointerDepth == t.getPointerDepth() &&
                        _const == t.isConst();
             }
            return false;
        }

        @Override
        public String toString() {
            return format(true);
        }

        public CsmClassifier getClassifier() {
            if (clazz instanceof SimpleClass) {
                return ((SimpleClass) clazz).clazz == null ? clazz : ((SimpleClass) clazz).clazz;
            } else {
                return clazz;
            }
        }

        public List<CsmSpecializationParameter> getInstantiationParams() {
            return Collections.emptyList();
        }

        public boolean isInstantiation() {
            return false;
        }

        public boolean isTemplateBased() {
            return false;
        }

        public CharSequence getClassifierText() {
            return clazz.getName();
        }

        public boolean isPointer() {
            return pointerDepth > 0;
        }

        public int getPointerDepth() {
            return pointerDepth;
        }

        public boolean isReference() {
            return reference;
        }

        public boolean isConst() {
            return _const;
        }

        public CharSequence getText() {
            return format(true);
        }

        public CharSequence getCanonicalText() {
            return getText();
        }

        public CsmFile getContainingFile() {
            if (CsmKindUtilities.isOffsetable(clazz)) {
                return ((CsmOffsetable)clazz).getContainingFile();
            } else {
                return null;
            }
        }

        public int getStartOffset() {
            return 0;
        }

        public int getEndOffset() {
            return 0;
        }

        public CsmOffsetable.Position getStartPosition() {
            return null;
        }

        public CsmOffsetable.Position getEndPosition() {
            return null;
        }

        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return CsmKindUtilities.isBuiltIn(clazz);
        }
    }

    public static class OffsetableType implements CsmType {

        private final CsmType delegate;
        private final CsmFile container;
        private final int start;
        private final int end;

        public OffsetableType(BaseType delegate, CsmFile container, int start, int end) {
            assert delegate != null;
            assert container != null;
            this.delegate = delegate;
            this.container = container;
            this.start = start;
            this.end = end;
        }

        public int getArrayDepth() {
            return delegate.getArrayDepth();
        }

        @Override
        public int hashCode() {
            return delegate.hashCode() + container.hashCode() + start + end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CsmType) {
                CsmType t = (CsmType) o;
                return delegate.equals(t) && container.equals(t.getContainingFile()) && (start == t.getStartOffset()) && (end == t.getEndOffset());
            }
            return false;
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        public CsmClassifier getClassifier() {
            return delegate.getClassifier();
        }

        public List<CsmSpecializationParameter> getInstantiationParams() {
            return delegate.getInstantiationParams();
        }

        public boolean isInstantiation() {
            return delegate.isInstantiation();
        }

        public boolean isTemplateBased() {
            return delegate.isTemplateBased();
        }

        public CharSequence getClassifierText() {
            return delegate.getClassifierText();
        }

        public boolean isPointer() {
            return delegate.isPointer();
        }

        public int getPointerDepth() {
            return delegate.getPointerDepth();
        }

        public boolean isReference() {
            return delegate.isReference();
        }

        public boolean isConst() {
            return delegate.isConst();
        }

        public CharSequence getText() {
            return delegate.getText();
        }

        public CharSequence getCanonicalText() {
            return delegate.getCanonicalText();
        }

        public CsmFile getContainingFile() {
            return container;
        }

        public int getStartOffset() {
            return start;
        }

        public int getEndOffset() {
            return end;
        }

        public CsmOffsetable.Position getStartPosition() {
            return null;
        }

        public CsmOffsetable.Position getEndPosition() {
            return null;
        }

        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return delegate.isBuiltInBased(resolveTypeChain);
        }
    }

    public static int getDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(int newDebugMode) {
        debugMode = newDebugMode;
    }
}
