/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.selector.java.nodes;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;

/**
 *
 * @author Jaroslav Bachorik
 */
class JavacUtils {
    private static final String BOOLEAN_CODE = "Z"; // NOI18N
    private static final String CHAR_CODE = "C"; // NOI18N
    private static final String BYTE_CODE = "B"; // NOI18N
    private static final String SHORT_CODE = "S"; // NOI18N
    private static final String INT_CODE = "I"; // NOI18N
    private static final String LONG_CODE = "J"; // NOI18N
    private static final String FLOAT_CODE = "F"; // NOI18N
    private static final String DOUBLE_CODE = "D"; // NOI18N
    private static final String VOID_CODE = "V"; // NOI18N

    private static final String BOOLEAN_STRING = "boolean"; // NOI18N
    private static final String CHAR_STRING = "char"; // NOI18N
    private static final String BYTE_STRING = "byte"; // NOI18N
    private static final String SHORT_STRING = "short"; // NOI18N
    private static final String INT_STRING = "int"; // NOI18N
    private static final String LONG_STRING = "long"; // NOI18N
    private static final String FLOAT_STRING = "float"; // NOI18N
    private static final String DOUBLE_STRING = "double"; // NOI18N
    private static final String VOID_STRING = "void"; // NOI18N
    private static final DeclaredTypeResolver declaredTypeResolver = new DeclaredTypeResolver();

    private static final Logger LOGGER = Logger.getLogger(JavacUtils.class.getName());

    public static String getVMMethodSignature(ExecutableElement method, CompilationInfo ci) {
        return getSignature(method, ci);
    }

    public static String typeToVMSignature(final String type) {
        //    System.err.println("sig for: "+type);
        String ret = type.replaceAll("\\.", "/"); // NOI18N

        // 1. replace primitive types or surround class name
        if (ret.startsWith(BOOLEAN_STRING)) {
            ret = ret.replaceAll(BOOLEAN_STRING, BOOLEAN_CODE);
        } else if (ret.startsWith(CHAR_STRING)) {
            ret = ret.replaceAll(CHAR_STRING, CHAR_CODE);
        } else if (ret.startsWith(BYTE_STRING)) {
            ret = ret.replaceAll(BYTE_STRING, BYTE_CODE);
        } else if (ret.startsWith(SHORT_STRING)) {
            ret = ret.replaceAll(SHORT_STRING, SHORT_CODE);
        } else if (ret.startsWith(INT_STRING)) {
            ret = ret.replaceAll(INT_STRING, INT_CODE);
        } else if (ret.startsWith(LONG_STRING)) {
            ret = ret.replaceAll(LONG_STRING, LONG_CODE);
        } else if (ret.startsWith(FLOAT_STRING)) {
            ret = ret.replaceAll(FLOAT_STRING, FLOAT_CODE);
        } else if (ret.startsWith(DOUBLE_STRING)) {
            ret = ret.replaceAll(DOUBLE_STRING, DOUBLE_CODE);
        } else if (ret.startsWith(VOID_STRING)) {
            ret = ret.replaceAll(VOID_STRING, VOID_CODE);
        } else {
            // if the remainder is a class, surround it with "L...;"
            final int arIdx = ret.indexOf('['); // NOI18N

            if (arIdx == -1) {
                ret = "L" + ret + ";"; // NOI18N
            } else {
                ret = "L" + ret.substring(0, arIdx) + ";" + ret.substring(arIdx); // NOI18N
            }
        }

        // 2. put all array marks to the beginning in the VM-signature style
        while (ret.endsWith("[]")) { // NOI18N
            ret = "[" + ret.substring(0, ret.length() - 2); // NOI18N
        }

        //    System.err.println("is: "+ret);
        return ret;
    }


    /**
     * @param method Method
     * @param types javax.lang.model.util.Types instance
     * @return String representation of VM-type method signature
     */
    private static String getSignature(ExecutableElement method, CompilationInfo ci) {
        try {
            switch (method.getKind()) {
                case METHOD:
                case CONSTRUCTOR:
                case STATIC_INIT:

                    //case INSTANCE_INIT: // not supported
                    String paramsVMSignature = getParamsSignature(method.getParameters(), ci);
                    String retTypeVMSignature = typeToVMSignature(getRealTypeName(method.getReturnType(), ci));

                    return "(" + paramsVMSignature + ")" + retTypeVMSignature; //NOI18N
                default:
                    return null;
            }

        } catch (IllegalArgumentException e) {
            LOGGER.warning(e.getMessage());
        }

        return null;
    }

    /**
     * Converts list of parameters to a single string with the signature
     * @param params A list of method parameters
     * @return string with the vm signature of the parameters
     */
    private static String getParamsSignature(List<? extends VariableElement> params, CompilationInfo ci) {
        StringBuffer ret = new StringBuffer();
        Iterator<? extends VariableElement> it = params.iterator();

        while (it.hasNext()) {
            TypeMirror type = it.next().asType();
            String realTypeName = getRealTypeName(type, ci);
            String typeVMSignature = typeToVMSignature(realTypeName);
            ret.append(typeVMSignature);
        }

        return ret.toString();
    }

    private static String getRealTypeName(TypeMirror type, CompilationInfo ci) {
        TypeKind typeKind = type.getKind();

        if (typeKind.isPrimitive()) {
            return type.toString(); // primitive type, return its name
        }

        switch (typeKind) {
            case VOID:

                // VOID type, return "void" - will be converted later by VMUtils.typeToVMSignature
                return type.toString();
            case DECLARED:

                // Java class (also parametrized - "ArrayList<String>" or "ArrayList<T>"), need to generate correct innerclass signature using "$"
                return ElementUtilities.getBinaryName(getDeclaredType(type));
            case ARRAY:

                // Array means "String[]" or "T[]" and also varargs "Object ... args"
                return getRealTypeName(((ArrayType) type).getComponentType(), ci) + "[]"; // NOI18N
            case TYPEVAR:

                // TYPEVAR means "T" or "<T extends String>" or "<T extends List&Runnable>"
                List<? extends TypeMirror> subTypes = ci.getTypes().directSupertypes(type);

                if (subTypes.size() == 0) {
                    return "java.lang.Object"; // NOI18N // Shouldn't happen
                }

                if ((subTypes.size() > 1) && subTypes.get(0).toString().equals("java.lang.Object") && getDeclaredType(subTypes.get(1)).getKind().isInterface()) {
                    // NOI18N
                    // Master type is interface
                    return getRealTypeName(subTypes.get(1), ci);
                } else {
                    // Master type is class
                    return getRealTypeName(subTypes.get(0), ci);
                }

            case WILDCARD:

                // WILDCARD means "<?>" or "<? extends Number>" or "<? super T>", shouldn't occur here
                throw new IllegalArgumentException("Unexpected WILDCARD parameter: " + type); // NOI18N
            default:

                // Unexpected parameter type
                throw new IllegalArgumentException("Unexpected type parameter: " + type + " of kind " + typeKind); // NOI18N
        }

    }

    private static TypeElement getDeclaredType(TypeMirror type) {
        return type.accept(declaredTypeResolver, null);
    }



    private static final class DeclaredTypeResolver implements TypeVisitor<TypeElement, Void> {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public TypeElement visit(TypeMirror t, Void p) {
            return null;
        }

        public TypeElement visit(TypeMirror t) {
            return null;
        }

        public TypeElement visitArray(ArrayType t, Void p) {
            return null;
        }

        public TypeElement visitDeclared(DeclaredType t, Void p) {
            return (TypeElement) t.asElement();
        }

        public TypeElement visitError(ErrorType t, Void p) {
            return null;
        }

        public TypeElement visitExecutable(ExecutableType t, Void p) {
            return null;
        }

        public TypeElement visitNoType(NoType t, Void p) {
            return null;
        }

        public TypeElement visitNull(NullType t, Void p) {
            return null;
        }

        public TypeElement visitPrimitive(PrimitiveType t, Void p) {
            return null;
        }

        public TypeElement visitTypeVariable(TypeVariable t, Void p) {
            return null;
        }

        public TypeElement visitUnknown(TypeMirror t, Void p) {
            return null;
        }

        public TypeElement visitWildcard(WildcardType t, Void p) {
            return null;
        }
    }
}
