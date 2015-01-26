/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.codemodel.utils;

import java.awt.Image;
import javax.swing.Icon;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMCursorKind;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntity;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Vladimir Kvashin
 */
public enum CMImageLoader {

    DEFAULT("org/openide/resources/defaultNode.gif"), // NOI18N

    NAMESPACE("namespace_16.png"), // NOI18N
    CLASS("class_16.png"), // NOI18N
    CLASS_FORWARD("class_forward_decl.png"), // NOI18N
    STRUCT_FORWARD("struct_forward_decl.png"), // NOI18N
    STRUCT("struct_16.png"), // NOI18N
    UNION("union_16.png"), // NOI18N
    ENUMERATION("enumeration_16.png"), // NOI18N
    ENUMERATION_FWD("enumeration_fwd_16.png"), // NOI18N
    ENUMERATOR("enumeration_item_16.png"), // NOI18N
    TYPEDEF("typedef_16.png"), // NOI18N

    FIELD_PUBLIC("fields.png"), // NOI18N
    FIELD_PROTECTED("fields_protected.png"), // NOI18N
    FIELD_PRIVATE("fields_private.png"), // NOI18N

    FIELD_CONST_PUBLIC("const_public.png"), // NOI18N
    FIELD_CONST_PROTECTED("const_protected.png"), // NOI18N
    FIELD_CONST_PRIVATE("const_private.png"), // NOI18N

    FIELD_ST_PUBLIC("fields_static.png"), // NOI18N
    FIELD_ST_PROTECTED("fields_static_protected.png"), // NOI18N
    FIELD_ST_PRIVATE("fields_static_private.png"), // NOI18N

    FIELD_ST_CONST_PUBLIC("const_static_public.png"), // NOI18N
    FIELD_ST_CONST_PROTECTED("const_static_protected.png"), // NOI18N
    FIELD_ST_CONST_PRIVATE("const_static_private.png"), // NOI18N

    CONSTRUCTOR_PUBLIC("constructors.png"), // NOI18N
    CONSTRUCTOR_PROTECTED("constructors_protected.png"), // NOI18N
    CONSTRUCTOR_PRIVATE("constructors_private.png"), // NOI18N

    DESTRUCTOR_PUBLIC("deconstructors.png"), // NOI18N
    DESTRUCTOR_PROTECTED("deconstructors_protected.png"), // NOI18N
    DESTRUCTOR_PRIVATE("deconstructors_private.png"), // NOI18N

    METHOD_PUBLIC("methods.png"), // NOI18N
    METHOD_PROTECTED("methods_protected.png"), // NOI18N
    METHOD_PRIVATE("methods_private.png"), // NOI18N

    METHOD_ST_PUBLIC("methods_static.png"), // NOI18N
    METHOD_ST_PROTECTED("methods_static_protected.png"), // NOI18N
    METHOD_ST_PRIVATE("methods_static_private.png"), // NOI18N

    FRIEND_METHOD("friend_method.png"), // NOI18N
    FRIEND_OPERATOR("friend_operator.png"), // NOI18N
    FRIEND_CLASS("friend_class.png"), // NOI18N

    FUNCTION_GLOBAL("global_function.png"), // NOI18N
    FUNCTION_DECLARATION_GLOBAL("global_function_decl.png"), // NOI18N
    FUNCTION_ST_GLOBAL("static_function.png"), // NOI18N

    OPERATOR_PUBLIC("operator_public.png"), // NOI18N
    OPERATOR_PROTECTED("operator_protected.png"), // NOI18N
    OPERATOR_PRIVATE("operator_privat.png"), // NOI18N

    OPERATOR_ST_PUBLIC("operator_static.png"), // NOI18N
    OPERATOR_ST_PROTECTED("operator_static_protected.png"), // NOI18N
    OPERATOR_ST_PRIVATE("operator_static_privat.png"), // NOI18N

    OPERATOR_GLOBAL("global_operator.png"), // NOI18N
    OPERATOR_ST_GLOBAL("global_operator_static.png"), // NOI18N

    VARIABLE_GLOBAL("global_variable.png"), // NOI18N
    VARIABLE_EX_GLOBAL("extern_variable_16.png"), // NOI18N
    VARIABLE_CONST_GLOBAL("global_constant.png"), // NOI18N

    VARIABLE_LOCAL("local_variable.png"), // NOI18N
    VARIABLE_ST_LOCAL("local_variable.png"), // NOI18N

    VARIABLE_CONST_LOCAL("local_constant.png"), // NOI18N
    VARIABLE_CONST_ST_LOCAL("local_constant.png"), // NOI18N

    VARIABLE_FILE_LOCAL("static_variable.png"), // NOI18N
    VARIABLE_CONST_FILE_LOCAL("global_constant.png"), // NOI18N
    VARIABLE_CONST_ST_FILE_LOCAL("global_constant.png"), // NOI18N

    MACRO("code_macro_16.png"), // NOI18N
    INCLUDE_USER("include_user_16.png"), // NOI18N
    INCLUDE_SYSTEM("include_sys_16.png"), // NOI18N
    INCLUDE_SYS_FOLDER("include_sys_folder_16.png"), // NOI18N
    INCLUDE_USR_FOLDER("include_usr_folder_16.png"), // NOI18N
    NAMESPACE_ALIAS("namespace_alias.png"), // NOI18N
    USING("using_namespace_16.png"), // NOI18N
    USING_DECLARATION( "using_declaration_16.png"), // NOI18N

    PROJECT("project.png"), // NOI18N
    PROJECT_OPENED("project_open.png"), // NOI18N
    LIB_PROJECT("libraries.png"), // NOI18N
    LIB_PROJECT_OPENED("libraries.png"), // NOI18N

    HEADER_FILE("header.gif"), // NOI18N
    CPP_SOUCE_FILE("cppsource.gif"), // NOI18N
    C_SOUCE_FILE("csource.gif"), // NOI18N
    FORTRAN_SOUCE_FILE("fortran.gif"), // NOI18N

    TEMPLATE_PARAMETER("template_parameter_16.png"), // NOI18N

    MODULE("methods_container.png"); // NOI18N

    //private static Map<String,ImageIcon> map = new HashMap<>();

    private final String iconPath;

    private CMImageLoader(String iconPath) {
        this.iconPath = "org/netbeans/modules/cnd/modelutil/resources/types16x16/" + iconPath; // NOI18N
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon(iconPath, false);
    }

    public Image getImage() {
        return ImageUtilities.loadImage(iconPath);
    }

    public static Image getImage(CMCursor cursor) {
        CMImageLoader img = get(cursor);
        return (img == null ? DEFAULT : img).getImage();
    }

    public static Icon getIcon(CMCursor cursor) {
        CMImageLoader img = get(cursor);
        return (img == null ? DEFAULT : img).getIcon();
    }

    public static Image getImage(CMEntity entity) {
        CMImageLoader img = get(entity);
        return (img == null ? DEFAULT : img).getImage();
    }

    public static Icon getIcon(CMEntity entity) {
        CMImageLoader img = get(entity);
        return (img == null ? DEFAULT : img).getIcon();
    }
    
    public static Icon getIcon(NativeProject o, boolean opened) {
        CMImageLoader iconPath;
        boolean library = false;    // FIXME
        if (library) {
            iconPath = opened ? LIB_PROJECT_OPENED : LIB_PROJECT;
        } else {
            iconPath = opened ? PROJECT_OPENED : PROJECT;
        }
        return iconPath.getIcon();
    }

    private static CMImageLoader get(CMEntity entity) {
        if (entity == null) {
            return null;
        }
        CMEntity.Kind kind = entity.getKind();
        switch (kind) {
            case CXXClass:
                return CLASS;
            case Enum:
                return ENUMERATION;
            case Union:
                return UNION;
            case Struct:
                return STRUCT;
            case Function:
                return FUNCTION_GLOBAL;
            default:
                return null;
        }
    }

    private static CMImageLoader get(CMCursor cursor) {
        if (cursor == null) {
            return null;
        }
        CMCursorKind kind = cursor.getKind();
        if (kind == null) {
            return null;
        }
        switch (kind) {
            case ClassDecl:
            case ClassTemplate:
                return CLASS;
            case FunctionDecl:
            case FunctionTemplate:
                return FUNCTION_GLOBAL; //TODO: check what functin is this
            case StructDecl:
                return STRUCT;
            case EnumDecl:
                return ENUMERATION;
            case UnionDecl:
                return UNION;
            default:
                return null;
        }
    }
}
