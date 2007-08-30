/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelutil;

/**
 *
 * @author jec
 */
public interface CsmImageName {
    static final String DEFAULT = "org/openide/resources/defaultNode.gif"; // NOI18N
    static final String RESOURCE_BASE = "org/netbeans/modules/cnd/modelutil/resources/types16x16/"; // NOI18N

    static final String NAMESPACE = RESOURCE_BASE+"namespace_16.png"; // NOI18N
    static final String CLASS = RESOURCE_BASE+"class_16.png"; // NOI18N
    static final String CLASS_FORWARD = RESOURCE_BASE+"class_forward_decl.png"; // NOI18N
    static final String STRUCT_FORWARD = RESOURCE_BASE+"struct_forward_decl.png"; // NOI18N
    static final String STRUCT = RESOURCE_BASE+"struct_16.png"; // NOI18N
    static final String UNION = RESOURCE_BASE+"union_16.png"; // NOI18N
    static final String ENUMERATION = RESOURCE_BASE+"enumeration_16.png"; // NOI18N
    static final String ENUMERATOR = RESOURCE_BASE+"enumeration_item_16.png"; // NOI18N
    static final String TYPEDEF = RESOURCE_BASE+"typedef_16.png"; // NOI18N

    static final String FIELD_PUBLIC = RESOURCE_BASE+"fields.png"; // NOI18N
    static final String FIELD_PROTECTED = RESOURCE_BASE+"fields_protected.png"; // NOI18N
    static final String FIELD_PRIVATE = RESOURCE_BASE+"fields_private.png"; // NOI18N
    
    static final String FIELD_CONST_PUBLIC = RESOURCE_BASE+"const_public.png"; // NOI18N
    static final String FIELD_CONST_PROTECTED = RESOURCE_BASE+"const_protected.png"; // NOI18N
    static final String FIELD_CONST_PRIVATE = RESOURCE_BASE+"const_private.png"; // NOI18N

    static final String FIELD_ST_PUBLIC = RESOURCE_BASE+"fields_static.png"; // NOI18N
    static final String FIELD_ST_PROTECTED = RESOURCE_BASE+"fields_static_protected.png"; // NOI18N
    static final String FIELD_ST_PRIVATE = RESOURCE_BASE+"fields_static_private.png"; // NOI18N

    static final String FIELD_ST_CONST_PUBLIC = RESOURCE_BASE+"const_static_public.png"; // NOI18N
    static final String FIELD_ST_CONST_PROTECTED = RESOURCE_BASE+"const_static_protected.png"; // NOI18N
    static final String FIELD_ST_CONST_PRIVATE = RESOURCE_BASE+"const_static_private.png"; // NOI18N

    static final String CONSTRUCTOR_PUBLIC = RESOURCE_BASE+"constructors.png"; // NOI18N
    static final String CONSTRUCTOR_PROTECTED = RESOURCE_BASE+"constructors_protected.png"; // NOI18N
    static final String CONSTRUCTOR_PRIVATE = RESOURCE_BASE+"constructors_private.png"; // NOI18N

    static final String DESTRUCTOR_PUBLIC = RESOURCE_BASE+"deconstructors.png"; // NOI18N
    static final String DESTRUCTOR_PROTECTED = RESOURCE_BASE+"deconstructors_protected.png"; // NOI18N
    static final String DESTRUCTOR_PRIVATE = RESOURCE_BASE+"deconstructors_private.png"; // NOI18N

    static final String METHOD_PUBLIC = RESOURCE_BASE+"methods.png"; // NOI18N
    static final String METHOD_PROTECTED = RESOURCE_BASE+"methods_protected.png"; // NOI18N
    static final String METHOD_PRIVATE = RESOURCE_BASE+"methods_private.png"; // NOI18N

    static final String METHOD_ST_PUBLIC = RESOURCE_BASE+"methods_static.png"; // NOI18N
    static final String METHOD_ST_PROTECTED = RESOURCE_BASE+"methods_static_protected.png"; // NOI18N
    static final String METHOD_ST_PRIVATE = RESOURCE_BASE+"methods_static_private.png"; // NOI18N
    
    static final String FRIEND_METHOD = RESOURCE_BASE+"friend_method.png"; // NOI18N
    static final String FRIEND_OPERATOR = RESOURCE_BASE+"friend_operator.png"; // NOI18N
    static final String FRIEND_CLASS = RESOURCE_BASE+"friend_class.png"; // NOI18N

    static final String FUNCTION_GLOBAL = RESOURCE_BASE+"global_function.png"; // NOI18N
    static final String FUNCTION_DECLARATION_GLOBAL = RESOURCE_BASE+"global_function_decl.png"; // NOI18N
    static final String FUNCTION_ST_GLOBAL = RESOURCE_BASE+"global_function.png"; // NOI18N

    static final String OPERATOR_PUBLIC = RESOURCE_BASE+"operator_public.png"; // NOI18N
    static final String OPERATOR_PROTECTED = RESOURCE_BASE+"operator_protected.png"; // NOI18N
    static final String OPERATOR_PRIVATE = RESOURCE_BASE+"operator_privat.png"; // NOI18N

    static final String OPERATOR_ST_PUBLIC = RESOURCE_BASE+"operator_static.png"; // NOI18N
    static final String OPERATOR_ST_PROTECTED = RESOURCE_BASE+"operator_static_protected.png"; // NOI18N
    static final String OPERATOR_ST_PRIVATE = RESOURCE_BASE+"operator_static_privat.png"; // NOI18N
    
    static final String OPERATOR_GLOBAL = RESOURCE_BASE+"global_operator.png"; // NOI18N
    static final String OPERATOR_ST_GLOBAL = RESOURCE_BASE+"global_operator_static.png"; // NOI18N

    static final String VARIABLE_GLOBAL = RESOURCE_BASE+"global_variable.png"; // NOI18N
    static final String VARIABLE_EX_GLOBAL = RESOURCE_BASE+"extern_variable_16.png"; // NOI18N
    static final String VARIABLE_ST_GLOBAL = RESOURCE_BASE+"global_variable.png"; // NOI18N

    static final String VARIABLE_CONST_GLOBAL = RESOURCE_BASE+"global_constant.png"; // NOI18N
    static final String VARIABLE_CONST_ST_GLOBAL = RESOURCE_BASE+"global_constant.png"; // NOI18N

    static final String VARIABLE_LOCAL = RESOURCE_BASE+"local_variable.png"; // NOI18N
    static final String VARIABLE_ST_LOCAL = RESOURCE_BASE+"local_variable.png"; // NOI18N

    static final String VARIABLE_CONST_LOCAL = RESOURCE_BASE+"local_constant.png"; // NOI18N
    static final String VARIABLE_CONST_ST_LOCAL = RESOURCE_BASE+"local_constant.png"; // NOI18N

    static final String VARIABLE_FILE_LOCAL = RESOURCE_BASE+"global_variable.png"; // NOI18N
    static final String VARIABLE_ST_FILE_LOCAL = RESOURCE_BASE+"global_variable.png"; // NOI18N

    static final String VARIABLE_CONST_FILE_LOCAL = RESOURCE_BASE+"global_constant.png"; // NOI18N
    static final String VARIABLE_CONST_ST_FILE_LOCAL = RESOURCE_BASE+"global_constant.png"; // NOI18N
    
    static final String MACRO = RESOURCE_BASE+"code_macro_16.png"; // NOI18N
    static final String INCLUDE_USER = RESOURCE_BASE+"include_user_16.png"; // NOI18N
    static final String INCLUDE_SYSTEM = RESOURCE_BASE+"include_sys_16.png"; // NOI18N
    static final String INCLUDE_FOLDER = RESOURCE_BASE+"include_folder_16.png"; // NOI18N
    static final String USING = RESOURCE_BASE+"using_namespace_16.png"; // NOI18N
    static final String USING_DECLARATION =  RESOURCE_BASE+"using_declaration_16.png"; // NOI18N
    
    static final String PROJECT =  RESOURCE_BASE+"project.png"; // NOI18N
}
