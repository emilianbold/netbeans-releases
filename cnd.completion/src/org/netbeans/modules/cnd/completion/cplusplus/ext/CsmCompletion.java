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
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.CompletionView;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ListCompletionView;
import org.netbeans.modules.cnd.api.model.CsmClass;

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
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
* Java completion query specifications
*
* @author Miloslav Metelka
* @version 1.00
*/

abstract public class CsmCompletion extends Completion {

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

    public static final BaseType BOOLEAN_TYPE = new BaseType(BOOLEAN_CLASS, 0);
    public static final BaseType BYTE_TYPE = new BaseType(BYTE_CLASS, 0);
    public static final BaseType CHAR_TYPE = new BaseType(CHAR_CLASS, 0);
    public static final BaseType DOUBLE_TYPE = new BaseType(DOUBLE_CLASS, 0);
    public static final BaseType FLOAT_TYPE = new BaseType(FLOAT_CLASS, 0);
    public static final BaseType INT_TYPE = new BaseType(INT_CLASS, 0);
    public static final BaseType LONG_TYPE = new BaseType(LONG_CLASS, 0);
    public static final BaseType SHORT_TYPE = new BaseType(SHORT_CLASS, 0);
    public static final BaseType VOID_TYPE = new BaseType(VOID_CLASS, 0);

    public static final SimpleClass INVALID_CLASS = new SimpleClass("", ""); // NOI18N
    public static final BaseType INVALID_TYPE = new BaseType(INVALID_CLASS, 0);

    public static final SimpleClass NULL_CLASS = new SimpleClass("null", ""); // NOI18N
    public static final BaseType NULL_TYPE = new BaseType(NULL_CLASS, 0);

    public static final SimpleClass OBJECT_CLASS_ARRAY
    = new SimpleClass("java.lang.Object[]", "java.lang".length(), true); // NOI18N
    public static final BaseType OBJECT_TYPE_ARRAY = new BaseType(OBJECT_CLASS_ARRAY, 0);

    public static final SimpleClass OBJECT_CLASS
    = new SimpleClass("java.lang.Object", "java.lang".length(), true); // NOI18N
    public static final BaseType OBJECT_TYPE = new BaseType(OBJECT_CLASS, 0);

    public static final SimpleClass CLASS_CLASS
    = new SimpleClass("java.lang.Class", "java.lang".length(), true); // NOI18N
    public static final BaseType CLASS_TYPE = new BaseType(CLASS_CLASS, 0);

    public static final SimpleClass STRING_CLASS
    = new SimpleClass("char", 0, true); // NOI18N
    public static final BaseType STRING_TYPE = new BaseType(STRING_CLASS, 1, false, 0);
    public static final SimpleClass CONST_STRING_CLASS
    = new SimpleClass("const char", 0, true); // NOI18N
    public static final BaseType CONST_STRING_TYPE = new BaseType(CONST_STRING_CLASS, 1, false, 0);

    /** @deprecated flag that is used for backward compatibility only. 
     *  Modifier.INTERFACE has been used instead. 
     */
    static final int INTERFACE_BIT = (1 << 30); // no neg nums in modifiers, 
    //static final int INTERFACE_BIT_FILTER = (~INTERFACE_BIT);
    
    // the bit for local member. the modificator is not saved within this bit.
    public static final int LOCAL_MEMBER_BIT = (1 << 29);

    // the bit for deprecated flag. it is saved to copde completion  DB
    public static final int DEPRECATED_BIT = (1 << 20);

    private static final HashMap str2PrimitiveClass = new HashMap();
    private static final HashMap str2PrimitiveType = new HashMap();
    private static final HashMap str2PredefinedType = new HashMap();

    static {
        // initialize primitive types cache
        BaseType[] types = new BaseType[] {
            BOOLEAN_TYPE, BYTE_TYPE, CHAR_TYPE, DOUBLE_TYPE, FLOAT_TYPE,
            INT_TYPE, LONG_TYPE, SHORT_TYPE, VOID_TYPE 
        };

        for (int i = types.length - 1; i >= 0; i--) {
            String typeName = types[i].getClassifier().getName().toString();
            str2PrimitiveClass.put(typeName, types[i].getClassifier());
            str2PrimitiveType.put(typeName, types[i]);
        }

        // initialize predefined types cache
        types = new BaseType[] {
            NULL_TYPE, OBJECT_TYPE_ARRAY, OBJECT_TYPE, CLASS_TYPE, STRING_TYPE, CONST_STRING_TYPE
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

    private static CsmFinder finder;

    private static int debugMode;

    /** Map holding the simple class instances */
//    private static HashMap classCache = new HashMap(5003);

    /** Map holding the cached types */
    private static HashMap typeCache = new HashMap(5003);

    /** Debug expression creation */
    public static final int DEBUG_EXP = 1;
    /** Debug finding packages/classes/fields/methods */
    public static final int DEBUG_FIND = 2;

    /** Callback for initing completion.  See EditorModule.restored(). */
    private static CsmFinderInitializer initializer;
    
    
    /** Gets the current default finder. */
    public static synchronized CsmFinder getFinder() {
        if(finder == null) {
            if(initializer == null) {
                throw new IllegalStateException("Editor: Java completion can't be initialized."); // NOI18N
            }

            initializer.initCsmFinder();
        }
        
        return finder;
    }
    
    /** Set the current default finder */
    public static synchronized void setFinder(CsmFinder f) {
        finder = f;
    }

    /** Sets initializer to init finder for case it's needed and was not done yet. */
//    public static void setFinderInitializer(CsmFinderInitializer initializer) {
//        JavaCompletion.initializer = initializer;
//    }
    

    public CsmCompletion(ExtEditorUI extEditorUI) {
        super(extEditorUI);
    }

    protected CompletionView createView() {
        return new ListCompletionView(new CsmCellRenderer());
    }

    abstract protected CompletionQuery createQuery();

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
        return CCTokenContext.isTypeOrVoid(s);
    }

    public static boolean isPrimitiveClass(CsmClassifier c) {
//        return (c.getPackageName().length() == 0)
//               && isPrimitiveClassName(c.getName());
        return isPrimitiveClassName(c.getName().toString());
    }

    public static CsmClassifier getPrimitiveClass(String s) {
        return (CsmClassifier)str2PrimitiveClass.get(s);
    }

    public static CsmType getPrimitiveType(String s) {
        return (CsmType)str2PrimitiveType.get(s);
    }

    public static CsmType getPredefinedType(String s) {
        CsmType ret = getPrimitiveType(s);
        if (ret == null) {
            ret = (CsmType)str2PredefinedType.get(s);
        }
        return ret;
    }

    public static Iterator getPrimitiveClassIterator() {
        return str2PrimitiveClass.values().iterator();
    }

//    public static CsmClassifier getSimpleClass(String fullClassName, int packageNameLen) {
//        CsmClassifier cls = (CsmClassifier)classCache.get(fullClassName);
//        if (cls == null // not in cache yet
////                || packageNameLen != cls.getPackageName().length() // different class
//           ) {
//            cls = new SimpleClass(fullClassName, packageNameLen, true);
//            classCache.put(fullClassName, cls);
//        }
//        return cls;
//    }

    public static CsmClassifier getSimpleClass(CsmClassifier clazz) {
        CharSequence fullClassName = clazz.getQualifiedName();
        CsmClassifier cls = null;//(CsmClassifier)classCache.get(fullClassName);
        if (clazz != null 
//        if (cls == null// not in cache yet
//                || packageNameLen != cls.getPackageName().length() // different class
           ) {
            cls = new SimpleClass(clazz);
//            classCache.put(fullClassName, cls);
        }
        return cls;        
//        return getSimpleClass(cls.getQualifiedName(), 0/*cls.getPackageName().length()*/);
    }

    public static CsmClassifier createSimpleClass(String fullClassName) {
        int nameInd = fullClassName.lastIndexOf(CsmCompletion.SCOPE) + 1;
        return createSimpleClass(fullClassName.substring(nameInd),
                                 (nameInd > 0) ? fullClassName.substring(0, nameInd - 1) : ""); // NOI18N
    }

    public static CsmClassifier createSimpleClass(String name, String packageName) {
        return new SimpleClass(name, packageName, CsmDeclaration.Kind.CLASS);
    }

    public static CsmType createType(CsmClassifier cls, int arrayDepth) {
        return new BaseType(cls, 0, false, arrayDepth);
    }

    /** returns type for dereferenced object
     * @param obj
     * @return
     */
    public static CsmType getObjectType(CsmObject obj) {
        CsmType type = null;
        if (CsmKindUtilities.isClassifier(obj)) {
            type = CsmCompletion.getType((CsmClassifier)obj, 0);
        } else if (CsmKindUtilities.isConstructor((CsmFunction)obj)) {
            CsmClassifier cls = ((CsmConstructor)obj).getContainingClass();
            type = CsmCompletion.getType(cls, 0);                  
        } else if (CsmKindUtilities.isFunction(obj)) {
            type = ((CsmFunction)obj).getReturnType();
        } else if (CsmKindUtilities.isVariable(obj)) {
            type = ((CsmVariable)obj).getType();
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
    public static CsmType getType(CsmClassifier cls, int arrayDepth) {
        if (cls == null) {
            return null;
        }

        CsmType[] types = (CsmType[])typeCache.get(cls);
        if (types != null) {
            if (arrayDepth < types.length) {
                if (types[arrayDepth] == null) {
//                    types[arrayDepth] = new BaseType(types[0].getClassifier(), arrayDepth);
                    types[arrayDepth] = new BaseType(cls, arrayDepth);
                }
            } else { // array length depth too small for given array depth
                cls = types[0].getClassifier();
                CsmType[] tmp = new CsmType[arrayDepth + 1];
                System.arraycopy(types, 0, tmp, 0, types.length);
                types = tmp;
                types[arrayDepth] = new BaseType(cls, arrayDepth);
                typeCache.put(cls, types);
            }
        } else { // types array not yet created
//            cls = getSimpleClass(cls.getQualifiedName(), cls.getPackageName().length());
            cls = getSimpleClass(cls);
            if (arrayDepth > 0) {
                types = new CsmType[arrayDepth + 1];
                types[arrayDepth] = new BaseType(cls, arrayDepth);
            } else {
                types = new CsmType[2];
            }
            types[0] = new BaseType(cls, 0);
            typeCache.put(cls, types);
        }

        return types[arrayDepth];
    }

//    public static class BasePackage implements CsmNamespace {
//
//        private String name;
//
//        private CsmClassifier[] classes;
//
//        private int dotCnt = -1;
//
//        private String lastName;
//
//        public BasePackage(String name) {
//            this(name, EMPTY_CLASSES);
//        }
//
//        public BasePackage(String name, CsmClassifier[] classes) {
//            this.name = name;
//            this.classes = classes;
//        }
//
//        /** Get full name of this package */
//        public final String getName() {
//            return name;
//        }
//
//        public String getLastName() {
//            if (lastName == null) {
//                lastName = name.substring(name.lastIndexOf('.') + 1);
//            }
//            return lastName;
//        }
//
//        /** Get classes contained in this package */
//        public CsmClassifier[] getClasses() {
//            return classes;
//        }
//
//        public void setClasses(CsmClassifier[] classes) {
//            this.classes = classes;
//        }
//
//        public int getDotCount() {
//            if (dotCnt < 0) {
//                int i = 0;
//                do {
//                    dotCnt++;
//                    i = name.indexOf('.', i) + 1;
//                } while (i > 0);
//            }
//            return dotCnt;
//        }
//
//        public int compareTo(Object o) {
//            if (this == o) {
//                return 0;
//            }
//            CsmNamespace p = (CsmNamespace)o;
//            return name.compareTo(p.getName());
//        }
//
//        public int hashCode() {
//            return name.hashCode();
//        }
//
//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            }
//            if (o instanceof CsmNamespace) {
//                return name.equals(((CsmNamespace)o).getName());
//            }
//            if (o instanceof String) {
//                return name.equals((String)o);
//            }
//            return false;
//        }
//
//        public String toString() {
//            return name;
//        }
//
//    }

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
                    name = ((String)name).intern();
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
            CsmClassifier c = (CsmClassifier)o;

//XXX            int order = packageName.compareTo(c.getPackageName());
            int order = 0;
            if (order == 0) {
                order = name.toString().compareTo(c.getName().toString());
            }
            return order;
        }

        public int hashCode() {
            return name.hashCode() ^ packageName.hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CsmClassifier) {
                CsmClassifier c = (CsmClassifier)o;
                String className = (c.getName() == null) ? null : c.getName().toString().replace('.','$');
                String thisName = name.toString().replace('.','$');
                return thisName.equals(className);//XXX && packageName.equals(c.getPackageName());
            }
            return false;
        }

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

        public CsmUID<CsmClass> getUID() {
            if (clazz != null) {
                return clazz.getUID();
            }
            return null;
        }

    }

//    /** Abstract class that assumes lazy initialization */
//    public static abstract class AbstractClass extends SimpleClass {
//
//        protected int modifiers;
//
//        protected Body body;
//
//        public AbstractClass(String name, String packageName,
//                             boolean iface, int modifiers) {
//            super(name, packageName);
//            this.modifiers = modifiers;
//            if (iface) {
//                this.modifiers |= Modifier.INTERFACE;
//            }
//        }
//
//        public AbstractClass(String name, String packageName,
//                             boolean iface, boolean deprecated, int modifiers) {
//            super(name, packageName);
//            this.modifiers = modifiers;
//            if (iface) {
//                this.modifiers |= Modifier.INTERFACE;
//            }
//            if (deprecated){
//                this.modifiers |= DEPRECATED_BIT;
//            }
//        }
//        
//        AbstractClass() {
//            super();
//        }
//
//        /** Init internal representation */
//        protected abstract void init();
//
//        /** Is this class an interface? */
//        public boolean isInterface() {
//            return (((modifiers & Modifier.INTERFACE) != 0) || ((modifiers & INTERFACE_BIT) != 0)) ;
//        }
//
//        /** Get modifiers for this class */
//        public int getModifiers() {
//            return modifiers; // & INTERFACE_BIT_FILTER;
//        }
//
//        public synchronized int getTagOffset() {
//            if (body == null) {
//                init();
//            }
//            return body.tagOffset;
//        }
//
//        /** Get superclass of this class */
//        public synchronized CsmClassifier getSuperclass() {
//            if (body == null) {
//                init();
//            }
//            return body.superClass;
//        }
//
//        /** Get interfaces this class implements */
//        public synchronized CsmClassifier[] getInterfaces() {
//            if (body == null) {
//                init();
//            }
//            return body.interfaces;
//        }
//
//        /** Get fields that this class contains */
//        public synchronized CsmField[] getFields() {
//            if (body == null) {
//                init();
//            }
//            return body.fields;
//        }
//
//        /** Get constructors that this class contains */
//        public synchronized CsmConstructor[] getConstructors() {
//            if (body == null) {
//                init();
//            }
//            return body.constructors;
//        }
//
//        /** Get methods that this class contains */
//        public synchronized CsmMethod[] getMethods() {
//            if (body == null) {
//                init();
//            }
//            return body.methods;
//        }
//
//        public static class Body {
//
//            public int tagOffset;
//
//            public CsmClassifier superClass;
//
//            public CsmClassifier[] interfaces;
//
//            public CsmField[] fields;
//
//            public CsmConstructor[] constructors;
//
//            public CsmMethod[] methods;
//
//        }
//
//    }

    /** Description of the type */
    public static class BaseType implements CsmType {

        protected CsmClassifier clazz;

        protected int arrayDepth;
        protected int pointerDepth;
        protected boolean reference;

        public BaseType(CsmClassifier clazz, int arrayDepth) {
            this.clazz = clazz;
            this.arrayDepth = arrayDepth;
            this.pointerDepth = 0;
            this.reference = false;
            if (arrayDepth < 0) {
                throw new IllegalArgumentException("Array depth " + arrayDepth + " < 0."); // NOI18N
            }
        }
        
        public BaseType(CsmClassifier clazz, int pointerDepth, boolean reference, int arrayDepth) {
            this.clazz = clazz;
            this.arrayDepth = arrayDepth;
            this.pointerDepth = pointerDepth;
            this.reference = reference;
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
            StringBuilder sb = new StringBuilder(useFullName ? getClassifier().getQualifiedName()
                                               : getClassifier().getName());
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
            CsmType t = (CsmType)o;
            int order = clazz.getQualifiedName().toString().compareTo(t.getClassifier().getQualifiedName().toString());
            if (order == 0) {
                order = arrayDepth - t.getArrayDepth();
            }
            return order;
        }

        public int hashCode() {
            return clazz.hashCode() + arrayDepth;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CsmType) {
                CsmType t = (CsmType)o;
                return clazz.equals(t.getClassifier()) && arrayDepth == t.getArrayDepth();
            }
            return false;
        }

        public String toString() {
            return format(true);
        }

        public CsmClassifier getClassifier() {
            if (clazz instanceof SimpleClass) {
                return ((SimpleClass)clazz).clazz == null ? clazz : ((SimpleClass)clazz).clazz;
            } else {
                return clazz;
            }
        }

        public List<CsmType> getInstantiationParams() {
            return Collections.emptyList();
        }

        public boolean isInstantiation() {
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
            return false;
        }

        public String getText() {
            return format(true);
        }
	
	public String getCanonicalText() {
	    return getText();
	}

        public CsmFile getContainingFile() {
            return null;
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

//    /** Description of the method parameter */
//    public static class BaseParameter implements CsmParameter {
//
//        protected String name;
//
//        protected CsmType type;
//
//        public BaseParameter(String name, CsmType type) {
//            this.name = name;
//            this.type = type;
//        }
//
//        BaseParameter() {
//        }
//
//        /** Name of the parameter */
//        public String getName() {
//            return name;
//        }
//
//        /** Type of the parameter */
//        public CsmType getType() {
//            return type;
//        }
//
//        public int compareTo(Object o) {
//            if (this == o) {
//                return 0;
//            }
//            CsmParameter p = (CsmParameter)o;
//            return type.compareTo(p.getType()); // only by type
//        }
//
//        public int hashCode() {
//            return type.hashCode() ^ name.hashCode();
//        }
//
//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            }
//            if (o instanceof CsmParameter) {
//                CsmParameter p = (CsmParameter)o;
//                return type.equals(p.getType()); // only by type
//            }
//            return false;
//        }
//
//        public String toString() {
//            return type.toString() + ' ' + name;
//        }
//
//    }

//    public static class BaseField extends BaseParameter
//        implements CsmField {
//
//        protected CsmClassifier clazz;
//
//        protected int modifiers;
//
//        protected int tagOffset;
//
//        public BaseField(CsmClassifier clazz, String name, CsmType type, int modifiers) {
//            super(name, type);
//            this.clazz = clazz;
//            this.modifiers = modifiers;
//        }
//
//        BaseField() {
//        }
//
//        public int getModifiers() {
//            return modifiers;
//        }
//
//        public CsmClassifier getClassifier() {
//            return clazz;
//        }
//
//        public int getTagOffset() {
//            return tagOffset;
//        }
//
//        public int compareTo(Object o) {
//            if (this == o) {
//                return 0;
//            }
//            CsmField f = (CsmField)o;
//            int order = super.compareTo(o);
//            if (order == 0) {
//                order = name.compareTo(f.getName());
//            }
//            return order;
//        }
//
//        public int hashCode() {
//            return type.hashCode() ^ name.hashCode() ^ modifiers;
//        }
//
//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            }
//            if (o instanceof CsmField) {
//                CsmField p = (CsmField)o;
//                return name.equals(p.getName()) && type.equals(p.getType());
//            }
//            return false;
//        }
//
//        public String toString() {
//            return Modifier.toString(modifiers) + ' ' + super.toString();
//        }
//
//    }

//    public static class BaseConstructor implements CsmConstructor {
//
//        protected CsmClassifier clazz;
//
//        protected int tagOffset;
//
//        protected int modifiers;
//
//        protected CsmParameter[] parameters;
//
//        protected CsmClassifier[] exceptions;
//
//        public BaseConstructor(CsmClassifier clazz, int modifiers,
//                               CsmParameter[] parameters, CsmClassifier[] exceptions) {
//            this.clazz = clazz;
//            this.modifiers = modifiers;
//            this.parameters = parameters;
//            this.exceptions = exceptions;
//        }
//
//        BaseConstructor() {
//        }
//
//        public CsmClassifier getClassifier() {
//            return clazz;
//        }
//
//        public int getTagOffset() {
//            return tagOffset;
//        }
//
//        public int getModifiers() {
//            return modifiers;
//        }
//
//        public CsmParameter[] getParameters() {
//            return parameters;
//        }
//
//        public CsmClassifier[] getExceptions() {
//            return exceptions;
//        }
//
//        /** This implementation expects
//        * that only the constructors inside one class will
//        * be compared.
//        */
//        public int compareTo(Object o) {
//            if (this == o) {
//                return 0;
//            }
//            CsmConstructor c = (CsmConstructor)o;
//            int order = 0;
//            CsmParameter[] mp = c.getParameters();
//            int commonCnt = Math.min(parameters.length, mp.length);
//            for (int i = 0; i < commonCnt; i++) {
//                order = parameters[i].compareTo(mp[i]);
//                if (order != 0) {
//                    return order;
//                }
//            }
//            order = parameters.length - mp.length;
//            return order;
//        }
//
//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            }
//            if (o instanceof CsmConstructor) {
//                return (compareTo(o) == 0);
//            }
//            return false;
//        }
//
//        public int hashCode() {
//            int h = 0;
//            for (int i = 0; i < parameters.length; i++) {
//                h ^= parameters[i].hashCode();
//            }
//            return h;
//        }
//
//        String toString(String returnTypeName, String methodName) {
//            StringBuilder sb = new StringBuilder(Modifier.toString(modifiers));
//            sb.append(' '); //NOI18N
//            sb.append(returnTypeName);
//            sb.append(methodName);
//            // Add parameters
//            sb.append('('); //NOI18N
//            int cntM1 = parameters.length - 1;
//            for (int i = 0; i <= cntM1; i++) {
//                sb.append(parameters[i].toString());
//                if (i < cntM1) {
//                    sb.append(", "); // NOI18N
//                }
//            }
//            sb.append(')');
//            // Add exceptions
//            cntM1 = exceptions.length - 1;
//            if (cntM1 >= 0) {
//                sb.append(" throws "); // NOI18N
//                for (int i = 0; i <= cntM1; i++) {
//                    sb.append(exceptions[i].toString());
//                    if (i < cntM1) {
//                        sb.append(", "); // NOI18N
//                    }
//                }
//            }
//            return sb.toString();
//        }
//
//        public String toString() {
//            return toString("", getClassifier().getName()); // NOI18N
//        }
//
//    }

//    public static class BaseMethod extends BaseConstructor
//            implements CsmMethod {
//        
//        protected String name;
//        
//        protected CsmType returnType;
//        
//        public BaseMethod(CsmClassifier clazz, String name, int modifiers, CsmType returnType,
//                CsmParameter[] parameters, CsmClassifier[] exceptions) {
//            super(clazz, modifiers, parameters, exceptions);
//            this.name = name;
//            this.returnType = returnType;
//        }
//        
//        BaseMethod() {
//        }
//        
//        public String getName() {
//            return name;
//        }
//        
//        public CsmType getReturnType() {
//            return returnType;
//        }
//        
//        public int compareTo(Object o) {
//            if (this == o) {
//                return 0;
//            }
//            CsmMethod m = (CsmMethod)o;
//            int order = name.compareTo(m.getName());
//            if (order == 0) {
//                order = super.compareTo(o);
//            }
//            return order;
//        }
//        
//        public int hashCode() {
//            return name.hashCode() ^ super.hashCode();
//        }
//        
//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            }
//            if (o instanceof CsmMethod) {
//                return (compareTo(o) == 0);
//            }
//            return false;
//        }
//        
//        public String toString() {
//            String rtn = getReturnType().toString();
//            return toString((rtn.length() > 0) ? rtn + ' ' : "", name); // NOI18N
//        }
//        
//    }

//    public abstract static class AbstractProvider {
//        implements JCClassProvider2 {
//
//        public abstract Iterator getClasses();
//        
//        public boolean remove(JCClassProvider cp) {
//            Iterator i = cp.getClasses();
//            while (i.hasNext()) {
//                CsmClassifier c = (CsmClassifier)i.next();
//                if (!cp.notifyAppend(c, false)) {
//                    return false;
//                }
//                if (!removeClass(c)) {
//                    return false;
//                }
//                if (!cp.notifyAppend(c, true)) {
//                    return false;
//                }
//            }
//            return true;
//        }
//
//        public boolean append(JCClassProvider cp) {
//            Iterator i = cp.getClasses();
//            while (i.hasNext()) {
//                CsmClassifier c = (CsmClassifier)i.next();
//                if (!cp.notifyAppend(c, false)) {
//                    return false;
//                }
//                if (!appendClass(c)) {
//                    return false;
//                }
//                if (!cp.notifyAppend(c, true)) {
//                    return false;
//                }
//            }
//            return true;
//        }
//
//        protected boolean appendClass(CsmClassifier c) {
//            return true;
//        }
//
//        protected boolean removeClass(CsmClassifier c) {
//            return true;
//        }
//
//        public void reset() {
//        }
//
//        /** This method is executed by the target Class Provider
//        * to notify this provider about the class appending.
//        * @param c JC class that was appended
//        * @return true to continue building, false to stop build
//        */
//        public boolean notifyAppend(CsmClassifier c, boolean appendFinished) {
//            return true;
//        }
//
//    }
//
//    public static class ListProvider extends AbstractProvider {
//
//        private List classList;
//
//        public ListProvider() {
//            classList = new ArrayList();
//        }
//
//        public ListProvider(List classList) {
//            this.classList = classList;
//        }
//
//        protected boolean appendClass(CsmClassifier c) {
//            classList.add(c);
//            return true;
//        }
//
//        public Iterator getClasses() {
//            return classList.iterator();
//        }
//
//        public int getClassCount() {
//            return classList.size();
//        }
//
//    }
//
//    public static class SingleProvider extends AbstractProvider
//        implements Iterator {
//
//        CsmClassifier c;
//
//        boolean next = true;
//
//        public SingleProvider(CsmClassifier c) {
//            this.c = c;
//        }
//
//        public Iterator getClasses() {
//            if (next) {
//                return this;
//            } else {
//                throw new IllegalStateException();
//            }
//        }
//
//        public boolean hasNext() {
//            return next;
//        }
//
//        public Object next() {
//            next = false;
//            return c;
//        }
//
//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
//
//    }

    public static int getDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(int newDebugMode) {
        debugMode = newDebugMode;
    }


    /** Interface for providing callback initialization of JavaCompletion. */
    public static interface CsmFinderInitializer {
        public void initCsmFinder();
    }
}
