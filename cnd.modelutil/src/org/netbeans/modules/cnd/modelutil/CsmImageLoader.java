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

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author jec
 */
public class CsmImageLoader implements CsmImageName {
    private static Map<String,Icon> map = new HashMap<String,Icon>();
    
    /** Creates a new instance of CsmImageLoader */
    private CsmImageLoader() {
    }
    
    public static Image getImage(CsmObject o) {
        String iconPath = getImagePath(o);
        return Utilities.loadImage(iconPath);
    }

    public static Image getFriendFunctionImage(CsmFriend o) {
        String iconPath;
        if (CsmKindUtilities.isFriendClass(o)) {
            iconPath = FRIEND_CLASS;
        } else {
            int modifiers = CsmUtilities.getModifiers(o);
            if ((modifiers & CsmUtilities.OPERATOR) == CsmUtilities.OPERATOR) {
                iconPath = FRIEND_OPERATOR;
            } else {
                iconPath = FRIEND_METHOD;
            }
        }
        return Utilities.loadImage(iconPath);
    }
    
    public static Icon getIcon(CsmDeclaration.Kind kind, int modifiers) {
        String iconPath = getImagePath(kind, modifiers);
        Icon icon = map.get(iconPath);
        if (icon == null) {
            icon = new ImageIcon(Utilities.loadImage(iconPath));
            map.put(iconPath, icon);
        }
        return icon;
    }
    
    public static String getIncludeIcon(Boolean usrIncludeKind) {
        if (usrIncludeKind == Boolean.TRUE) {
            return INCLUDE_USER;
        } else if (usrIncludeKind == Boolean.FALSE) {
            return INCLUDE_SYSTEM;
        } else {
            return INCLUDE_FOLDER;
        }
    }
    
    public static String getImagePath(CsmObject o) {
        CsmDeclaration.Kind kind = CsmDeclaration.Kind.BUILT_IN;
        int modifiers = CsmUtilities.getModifiers(o);
        if (CsmKindUtilities.isEnumerator(o)) {
            kind = CsmDeclaration.Kind.ENUM;
            modifiers |= CsmUtilities.ENUMERATOR;
        } else if (CsmKindUtilities.isUsingDirective(o)) {
            return USING;
        } else if (CsmKindUtilities.isUsingDeclaration(o)) {
            return USING_DECLARATION;
        } else if (CsmKindUtilities.isClassForwardDeclaration(o)) {
            CsmClass cls = ((CsmClassForwardDeclaration)o).getCsmClass();
            if (cls != null && cls.getKind() == CsmDeclaration.Kind.CLASS) {
                return CLASS_FORWARD;
            }
            return STRUCT_FORWARD;
        } else if (CsmKindUtilities.isDeclaration(o)) {
            kind = ((CsmDeclaration)o).getKind();
        } else if (CsmKindUtilities.isNamespace(o)) {
            // FIXUP: consider namespace same as namespace definition
            // because namespace is not declaration
            kind = CsmDeclaration.Kind.NAMESPACE_DEFINITION;
        } else if (CsmKindUtilities.isMacro(o)) {
            return MACRO;
        } else if (CsmKindUtilities.isInclude(o)) {
            if (((CsmInclude)o).isSystem()){
                return INCLUDE_SYSTEM;
            } else {
                return INCLUDE_USER;
            }
        }
        return getImagePath(kind, modifiers);
    }
    
    static String getImagePath(CsmDeclaration.Kind kind, int modifiers) {

        String iconPath = DEFAULT;
        if (kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
            iconPath = NAMESPACE;
        } else if (kind == CsmDeclaration.Kind.ENUM) { 
            if ((modifiers & CsmUtilities.ENUMERATOR) == 0)
                iconPath = ENUMERATION;
            else
                iconPath = ENUMERATOR;
        } else if (kind == CsmDeclaration.Kind.MACRO){
            iconPath = MACRO;
        } else if (kind == CsmDeclaration.Kind.CLASS) {
            iconPath = CLASS;
        } else if (kind == CsmDeclaration.Kind.STRUCT) {
            iconPath = STRUCT;
        } else if (kind == CsmDeclaration.Kind.UNION) {
            iconPath = UNION; 
        } else if (kind == CsmDeclaration.Kind.TYPEDEF) {
            iconPath = TYPEDEF;
        } else if (kind == CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION) {
            iconPath = FRIEND_CLASS;
        } else if (kind == CsmDeclaration.Kind.VARIABLE || kind == CsmDeclaration.Kind.VARIABLE_DEFINITION ) {
            boolean isLocal = (modifiers & CsmUtilities.LOCAL) != 0;
            boolean isFileLocal = (modifiers & CsmUtilities.FILE_LOCAL) != 0;
            boolean isGlobal = !(isLocal | isFileLocal);
            boolean isField = (modifiers & CsmUtilities.MEMBER) != 0;
            boolean isStatic = (modifiers & CsmUtilities.STATIC) != 0;
            boolean isConst = (modifiers & CsmUtilities.CONST_MEMBER_BIT) != 0;
            boolean isExtern = (modifiers & CsmUtilities.EXTERN) != 0;
            if (isGlobal) {
                iconPath = VARIABLE_GLOBAL;
                if (isStatic) {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_ST_GLOBAL;
                    } else {
                        iconPath = VARIABLE_ST_GLOBAL;
                    }
                } else {
                    if (isConst) {
                        if (isExtern) {
                            iconPath = VARIABLE_EX_GLOBAL;
                        } else {
                            iconPath = VARIABLE_CONST_GLOBAL;
                        }
                    } else {
                        if (isExtern) {
                            iconPath = VARIABLE_EX_GLOBAL;
                        } else {
                            iconPath = VARIABLE_GLOBAL;
                        }
                    }
                }
            }

            if (isLocal) {
                iconPath = VARIABLE_LOCAL;
                if (isStatic) {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_ST_LOCAL;
                    } else {
                        iconPath = VARIABLE_ST_LOCAL;
                    }
                } else {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_LOCAL;
                    } else {
                        iconPath = VARIABLE_LOCAL;
                    }
                }
            }

            if (isFileLocal) {
                iconPath = VARIABLE_LOCAL;
                if (isStatic) {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_ST_FILE_LOCAL;
                    } else {
                        iconPath = VARIABLE_ST_FILE_LOCAL;
                    }
                } else {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_FILE_LOCAL;
                    } else {
                        iconPath = VARIABLE_FILE_LOCAL;
                    }
                }
            }

            if (isField) {
                int level = CsmUtilities.getLevel(modifiers);
                iconPath = FIELD_PUBLIC;
                if (isStatic){
                    //static field
                    switch (level) {
                        case CsmUtilities.PRIVATE_LEVEL:
                            iconPath = isConst ? FIELD_ST_CONST_PRIVATE : FIELD_ST_PRIVATE;
                            break;
                        case CsmUtilities.PROTECTED_LEVEL:
                            iconPath = isConst ? FIELD_ST_CONST_PROTECTED : FIELD_ST_PROTECTED;
                            break;
                        case CsmUtilities.PUBLIC_LEVEL:
                            iconPath = isConst ? FIELD_ST_CONST_PUBLIC : FIELD_ST_PUBLIC;
                            break;
                    }
                }else{
                    switch (level) {
                        case CsmUtilities.PRIVATE_LEVEL:
                            iconPath = isConst ? FIELD_CONST_PRIVATE : FIELD_PRIVATE;
                            break;
                        case CsmUtilities.PROTECTED_LEVEL:
                            iconPath = isConst ? FIELD_CONST_PROTECTED : FIELD_PROTECTED;
                            break;
                        case CsmUtilities.PUBLIC_LEVEL:
                            iconPath = isConst ? FIELD_CONST_PUBLIC : FIELD_PUBLIC;
                            break;
                    }
                }
          }
        } else if (kind == CsmDeclaration.Kind.FUNCTION || kind == CsmDeclaration.Kind.FUNCTION_DEFINITION) {
            boolean isMethod = (modifiers & CsmUtilities.MEMBER) != 0;
            boolean isGlobal = !(isMethod);
            boolean isConstructor = (modifiers & CsmUtilities.CONSTRUCTOR) != 0;
            boolean isDestructor = (modifiers & CsmUtilities.DESTRUCTOR) != 0;
            boolean isOperator =  (modifiers & CsmUtilities.OPERATOR) != 0;
            boolean isStatic = (modifiers & CsmUtilities.STATIC) != 0;
            int level = CsmUtilities.getLevel(modifiers);
            if (isGlobal) {
                if (isOperator) {
                    iconPath = OPERATOR_GLOBAL;
                } else {
                    if (kind == CsmDeclaration.Kind.FUNCTION) {
                        iconPath = FUNCTION_DECLARATION_GLOBAL;
                    } else {
                        iconPath = FUNCTION_GLOBAL;
                    }
                }
                if (isStatic) {
                    if (isOperator) {
                        iconPath = OPERATOR_ST_GLOBAL;
                    } else {
                        iconPath = FUNCTION_ST_GLOBAL;
                    }
                }
            }
            if (isMethod) {
                if (isOperator) {
                    iconPath = OPERATOR_PUBLIC;
                } else {
                    iconPath = METHOD_PUBLIC;
                }
                if (isStatic){
                    //static method
                    switch (level) {
                        case CsmUtilities.PRIVATE_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_ST_PRIVATE;
                            } else {
                                iconPath = METHOD_ST_PRIVATE;
                            }
                            break;
                        case CsmUtilities.PROTECTED_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_ST_PROTECTED;
                            } else {
                                iconPath = METHOD_ST_PROTECTED;
                            }
                            break;
                        case CsmUtilities.PUBLIC_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_ST_PUBLIC;
                            } else {
                                iconPath = METHOD_ST_PUBLIC;
                            }
                            break;
                    }
                }else{
                    switch (level) {
                        case CsmUtilities.PRIVATE_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_PRIVATE;
                            } else {
                                iconPath = METHOD_PRIVATE;
                            }
                            break;
                        case CsmUtilities.PROTECTED_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_PROTECTED;
                            } else {
                                iconPath = METHOD_PROTECTED;
                            }
                            break;
                        case CsmUtilities.PUBLIC_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_PUBLIC;
                            } else {
                                iconPath = METHOD_PUBLIC;
                            }
                            break;
                    }
                }
            }
            if (isConstructor) {
                iconPath = CONSTRUCTOR_PUBLIC;
                switch (level) {
                    case CsmUtilities.PRIVATE_LEVEL:
                        iconPath = CONSTRUCTOR_PRIVATE;
                        break;
                    case CsmUtilities.PROTECTED_LEVEL:
                        iconPath = CONSTRUCTOR_PROTECTED;
                        break;
                    case CsmUtilities.PUBLIC_LEVEL:
                        iconPath = CONSTRUCTOR_PUBLIC;
                        break;
                }
            }
            if (isDestructor) {
                iconPath = DESTRUCTOR_PUBLIC;
                switch (level) {
                    case CsmUtilities.PRIVATE_LEVEL:
                        iconPath = DESTRUCTOR_PRIVATE;
                        break;
                    case CsmUtilities.PROTECTED_LEVEL:
                        iconPath = DESTRUCTOR_PROTECTED;
                        break;
                    case CsmUtilities.PUBLIC_LEVEL:
                        iconPath = DESTRUCTOR_PUBLIC;
                        break;
                }
            }
        }
        return iconPath;
    }
}
