/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.codestructure;

import java.util.Collection;

/**
 * @author Tomas Pavek
 */

public interface CodeVariable {

    // variable type constants

    // variable scope type (bits 12, 13)
    public static final int NO_VARIABLE = 0x0000; // means just name reserved
    public static final int LOCAL = 0x1000;
    public static final int FIELD = 0x2000;

    // access modifiers - conforms to Modifier class (bits 0, 1, 2)
    public static final int PUBLIC = 0x0001;
    public static final int PRIVATE = 0x0002;
    public static final int PROTECTED = 0x0004;
    public static final int PACKAGE_PRIVATE = 0x0000;

    // other modifiers  - conforms to Modifier class (bits 3, 4, 6, 7)
    public static final int STATIC = 0x0008;
    public static final int FINAL = 0x0010;
    public static final int VOLATILE = 0x0040;
    public static final int TRANSIENT = 0x0080;

    static final int DEFAULT_TYPE = 0x30DF;

    // explicit local variable declaration in code (bit 14)
    public static final int EXPLICIT_DECLARATION = 0x4000;

    // variable management according to number of expressions attached (bit 15)
    public static final int EXPLICIT_RELEASE = 0x8000;

    // masks
    public static final int SCOPE_MASK = 0x3000;
    public static final int ACCESS_MODIF_MASK = 0x0007;
    public static final int OTHER_MODIF_MASK = 0x00D8;
    public static final int ALL_MODIF_MASK = 0x00DF;
    public static final int DECLARATION_MASK = 0x4000;
    public static final int RELEASE_MASK = 0x8000;
    public static final int ALL_MASK = 0xF0DF;

    // ------

    public int getType();

    public Class getDeclaredType();

    public String getName();

    public Collection getAttachedExpressions();

    public CodeStatement getDeclaration();

    public CodeStatement getAssignment(CodeExpression expression);
}
