/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax.javacc.lib;

/**
 * This class has the same name as JavaCC generated CharStream for
 * <pre>
 * UNICODE_INPUT = TRUE
 * <pre>
 * but it behaves as a user char stream. Reason is that 
 * <pre>
 * USER_CHAR_STREAM = TRUE
 * <pre>
 * disables generation of unicode aware code i.e. unicode aware code is generated only
 * for <tt>UNICODE_INPUT</tt> which is mutually exclusive with user char input.
 * <p>
 * Note: Delete JavaCC generated UCode_CharStream and add import statement
 * that makes this class visible.
 * 
 * @author  Petr Kuzel
 * @version 
 */
public final class UCode_CharStream extends StringParserInput {

    /** this implementation is dynamic, I hope so. */
    public static final boolean staticFlag = false;

    /** Creates new UCode_CharStream */
    public UCode_CharStream() {
    }

}
