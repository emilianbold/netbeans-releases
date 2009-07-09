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
package org.netbeans.modules.refactoring.php.findusages;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.Parameters;

/**
 *
 * @author tomslot
 */
public class CodeUtils {
    public static final String FUNCTION_TYPE_PREFIX = "@fn:";
    public static final String METHOD_TYPE_PREFIX = "@mtd:";
    public static final String STATIC_METHOD_TYPE_PREFIX = "@static.mtd:";
    private static final Logger LOGGER = Logger.getLogger(CodeUtils.class.getName());

    private CodeUtils() {
    }

    //TODO: extracting name needs to be take into account namespaces
    public static Identifier extractIdentifier(Expression clsName) {
        Parameters.notNull("clsName", clsName);
        if (clsName instanceof Identifier) {
            return (Identifier) clsName;
        } else if (clsName instanceof NamespaceName) {
            return extractIdentifier((NamespaceName)clsName);
        }
        //TODO: php5.3 !!!
        assert false : "[php5.3] className Expression instead of Identifier"; //NOI18N
        return null;
    }

    public static String extractTypeName(Expression clsName) {
        Parameters.notNull("clsName", clsName);
        if (clsName instanceof Identifier) {
            return ((Identifier) clsName).getName();
        } else if (clsName instanceof NamespaceName) {
            return extractTypeName((NamespaceName)clsName);
        }

        //TODO: php5.3 !!!
        assert false : "[php5.3] className Expression instead of Identifier"; //NOI18N
        return null;
    }

    public static String extractClassName(StaticDispatch dispatch) {
        Parameters.notNull("dispatch", dispatch);
        Expression clsName = dispatch.getClassName();
        return extractTypeName(clsName);
    }

    public static String extractParameterTypeName(FormalParameter param) {
        Parameters.notNull("param", param);
        Expression typeName = param.getParameterType();
        return typeName != null ? extractTypeName(typeName) : null;
    }

    public static String extractTypeName(CatchClause catchClause) {
        Parameters.notNull("catchClause", catchClause);
        Expression typeName = catchClause.getClassName();
        return typeName != null ? extractTypeName(typeName) : null;
    }

    public static String extractSuperClassName(ClassDeclaration clsDeclaration) {
        Parameters.notNull("clsDeclaration", clsDeclaration);
        Expression clsName = clsDeclaration.getSuperClass();
        return clsName != null ? extractTypeName(clsName) : null;
    }

    public static String extractTypeName(NamespaceName name) {
        if (name instanceof NamespaceName) {
            NamespaceName namespaceName = (NamespaceName) name;
            final List<Identifier> segments = namespaceName.getSegments();
            if (segments.size() >= 1) {
                return segments.get(segments.size()-1).getName();
            }
        }
        //TODO: php5.3 !!!
        //assert false : "[php5.3] className Expression instead of Identifier"; //NOI18N
        return null;
    }
    public static Identifier extractIdentifier(NamespaceName name) {
        if (name instanceof NamespaceName) {
            NamespaceName namespaceName = (NamespaceName) name;
            final List<Identifier> segments = namespaceName.getSegments();
            if (segments.size() >= 1) {
                return segments.get(segments.size()-1);
            }
        }
        //TODO: php5.3 !!!
        //assert false : "[php5.3] className Expression instead of Identifier"; //NOI18N
        return null;
    }
    //TODO: rewrite for php53
    public static String extractClassName(ClassName clsName) {
        Expression name = clsName.getName();
        while (name instanceof Variable || name instanceof FieldAccess) {
            if (name instanceof Variable) {
                Variable var = (Variable) name;
                name = var.getName();
            } else if (name instanceof FieldAccess) {
                FieldAccess fld = (FieldAccess) name;
                name = fld.getField().getName();
            }
        }
        if (name instanceof NamespaceName) {
            return extractTypeName((NamespaceName)name);
        }
        return (name instanceof Identifier) ? ((Identifier) name).getName() : "";//NOI18N
    }

    public static String extractClassName(ClassDeclaration clsDeclaration) {
        return clsDeclaration.getName().getName();
    }
    @CheckForNull // null for RelectionVariable
    public static String extractVariableName(Variable var) {
        if (var instanceof ReflectionVariable) {
            Expression name = ((ReflectionVariable) var).getName();
            if (name instanceof Scalar) {
                Scalar scalar = (Scalar) name;
                return scalar.getStringValue();
            } else if (name instanceof Variable) {
                var = (Variable)name;
                return extractVariableName(var);
            } else if (name instanceof FieldAccess) {
                var = ((FieldAccess)name).getField();
                return extractVariableName(var);
            } else if (name instanceof InfixExpression) {
                //#157750
                return null;
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Cannot extract variable name of ReflectionVariable: " + name.getClass().toString());
            }
        }
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            StringBuilder varName = new StringBuilder();

            if (var.isDollared()) {
                varName.append("$");
            }

            varName.append(id.getName());
            return varName.toString();
        } else if (var.getName() instanceof Variable) {
            Variable name = (Variable) var.getName();
            return extractVariableName(name);
        } else if (var.getName() instanceof MethodInvocation){
            // no variable name
            return null; 
        }
        
        LOGGER.fine("Cannot extract variable name of type: " + var.getName().getClass().toString());
        return null;
    }

}
