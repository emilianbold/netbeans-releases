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

    // variable scope (bits 0, 1)
    public static final int NO_VARIABLE = 0x00; // means just name reserved
    public static final int LOCAL = 0x01;
    public static final int FIELD = 0x02;

    // field variable modifiers (bits 2, 3, 4)
    public static final int STATIC = 0x04;
    public static final int FINAL = 0x08;
    public static final int TRANSIENT = 0x10;

    // field variable access (bits 5, 6, 7)
    public static final int DEFAULT_ACCESS = 0x00;
    public static final int PRIVATE = 0x20;
    public static final int PACKAGE_PRIVATE = 0x40;
    public static final int PROTECTED = 0x60;
    public static final int PUBLIC = 0x80;

    // local variable declaration in code (bit 8)
    public static final int EXPLICIT_DECLARATION = 0x100;

    // variable management (bit 9)
    public static final int EXPLICIT_RELEASE = 0x200;

    // masks
    public static final int SCOPE_MASK = 0x03;
    public static final int MODIFIERS_MASK = 0x1C;
    public static final int ACCESS_MASK = 0xE0;
    public static final int DECLARATION_MASK = 0x100;
    public static final int RELEASE_MASK = 0x200;

    public int getType();

    public Class getDeclaredType();

    public String getName();

    // returns null if no expression is attached
    public Collection getAttachedExpressions();

    public CodeStatement getDeclaration();

    public CodeStatement getAssignment(CodeExpression expression);
}
