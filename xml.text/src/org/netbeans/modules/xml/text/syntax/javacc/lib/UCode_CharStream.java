/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * @see https://javacc.dev.java.net/issues/show_bug.cgi?id=77 
 * @author  Petr Kuzel
 */
public final class UCode_CharStream extends StringParserInput {

    /** this implementation is dynamic, I hope so. */
    public static final boolean staticFlag = false;

    /** Creates new UCode_CharStream */
    public UCode_CharStream() {
    }

}
